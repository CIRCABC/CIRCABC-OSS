/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.europa.ec.digit.circabc.rest.service.helper;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import io.swagger.model.Util;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper service that centralises the creation and update of Alfresco content nodes
 * for the CIRCABC REST layer.
 *
 * <p>
 * It takes care of the recurring low-level concerns involved when a document is
 * uploaded or generated: resolving a valid and unique node name, guessing the
 * MIME type and encoding, writing the binary payload (from a {@link File} or an
 * {@link InputStream}), extracting metadata, applying the relevant Alfresco
 * aspects (author, titled, inline-editable and, optionally, the content
 * notification aspect) and taking ownership of the created node.
 * </p>
 *
 * <p>
 * Collaborating Alfresco services and the CIRCABC {@link MetadataManager} are
 * injected by Spring via {@code @Autowired}.
 * </p>
 *
 * @author Yanick Pignot
 */
public class ContentManager {

  /** Logger used to report non-fatal errors during content and metadata handling. */
  private final Log logger = LogFactory.getLog(ContentManager.class);

  /** Alfresco service used to create nodes, read/write properties and manage aspects. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco service used to obtain content writers and persist the binary payload. */
  @Autowired
  private ContentService contentService;

  /** Alfresco service used to take ownership of newly created content nodes. */
  @Autowired
  private OwnableService ownableService;

  /** CIRCABC helper used for name validation, MIME type/encoding guessing and metadata extraction. */
  @Autowired
  private MetadataManager metadataManager;

  /**
   * Creates a new content node under the given parent from a {@link File} payload.
   *
   * @param parent            the parent node under which the content is created
   * @param name              the requested node name; it is validated and made unique
   * @param associationQname  the child association type to use; when {@code null},
   *                          {@link ContentModel#ASSOC_CONTAINS} is used
   * @param childTypeQName    the content type of the new node; when {@code null},
   *                          {@link ContentModel#TYPE_CONTENT} is used
   * @param file              the file whose bytes are stored as the node content
   * @param applyNotification when {@code true}, the content notification aspect is
   *                          added so that notification triggers are fired
   * @return the {@link NodeRef} of the newly created content node
   */
  public NodeRef createContent(
    final NodeRef parent,
    final String name,
    final QName associationQname,
    final QName childTypeQName,
    final File file,
    final boolean applyNotification
  ) {
    return createContent(
      parent,
      name,
      associationQname,
      childTypeQName,
      file,
      null,
      applyNotification
    );
  }

  /**
   * Creates a new content node under the given parent from an {@link InputStream} payload.
   *
   * @param parent            the parent node under which the content is created
   * @param name              the requested node name; it is validated and made unique
   * @param associationQname  the child association type to use; when {@code null},
   *                          {@link ContentModel#ASSOC_CONTAINS} is used
   * @param childTypeQName    the content type of the new node; when {@code null},
   *                          {@link ContentModel#TYPE_CONTENT} is used
   * @param inputStream       the stream whose bytes are stored as the node content
   * @param applyNotification when {@code true}, the content notification aspect is
   *                          added so that notification triggers are fired
   * @return the {@link NodeRef} of the newly created content node
   */
  public NodeRef createContent(
    final NodeRef parent,
    final String name,
    final QName associationQname,
    final QName childTypeQName,
    final InputStream inputStream,
    final boolean applyNotification
  ) {
    return createContent(
      parent,
      name,
      associationQname,
      childTypeQName,
      null,
      inputStream,
      applyNotification
    );
  }

  private NodeRef createContent(
    final NodeRef parent,
    final String name,
    final QName associationQname,
    final QName childTypeQName,
    final File file,
    final InputStream inputStream,
    final boolean applyNotification
  ) {
    final QName validAssocQName =
      associationQname == null ? ContentModel.ASSOC_CONTAINS : associationQname;
    final QName validTypeQName =
      childTypeQName == null ? ContentModel.TYPE_CONTENT : childTypeQName;

    // generate a valid and unique name
    final String validName = metadataManager.getValidUniqueName(
      parent,
      validAssocQName,
      name
    );

    // comput mimetype and encoding
    final String mimetype = computeMimeType(validName);
    String encoding = "UTF-8";
    if (file != null) {
      encoding = computeEncoding(file, mimetype);
    }

    // create a content
    final NodeRef contentRef = createContent(
      parent,
      validAssocQName,
      validTypeQName,
      validName,
      mimetype
    );

    // update node content
    updateContent(contentRef, file, inputStream, null, mimetype, encoding);

    if (applyNotification) {
      // Mandatory workaround to ensure that the notification trigger will be not called in another transaction
      nodeService.addAspect(
        contentRef,
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY,
        null
      );
    }

    // extract and set common properties
    final Map<QName, Serializable> extractedProps = extractedProperties(
      file,
      mimetype,
      encoding
    );
    //https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2614
    extractedProps.remove(ContentModel.PROP_AUTHOR);
    extractedProps.remove(ContentModel.PROP_DESCRIPTION);
    extractedProps.remove(ContentModel.PROP_TITLE);
    updateProperties(contentRef, mimetype, extractedProps);

    return contentRef;
  }

  //--------------
  //-- private helpers

  /**
   * Return the target nodeRef if the given node is a link or the node itself.
   *
   * <p>
   * Usefull to ensure to have a valid node without testing if it is a link or not.
   * </p>
   * <p>
   * <b>Can throw BusinessStackError if the user can't read the target node ref</b>
   * </p>
   *
   * @param nodeRef the node to resolve; may be {@code null}
   * @return {@code null} if {@code nodeRef} is {@code null}; the link destination
   *         node when {@code nodeRef} is a link; otherwise {@code nodeRef} itself
   */
  public NodeRef getTargetRef(final NodeRef nodeRef) {
    if (nodeRef == null) {
      return null;
    } else {
      final NodeRef target = (NodeRef) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_LINK_DESTINATION
      );

      if (target == null) {
        return nodeRef;
      } else {
        return target;
      }
    }
  }

  /**
   * @param fileContent File content to save
   * @param strContent  String content to save (if fileContent is null)
   */
  private NodeRef createContent(
    final NodeRef parent,
    final QName associationQname,
    final QName typeQname,
    final String name,
    String mimetype
  ) {
    //	 guess a mimetype based on the filename
    QName propContent = Util.getPropContent(typeQname);

    final ContentData contentData = new ContentData(
      null,
      mimetype,
      0L,
      "UTF-8"
    );
    final Map<QName, Serializable> properties = HashMap.newHashMap(2);
    properties.put(ContentModel.PROP_NAME, name);
    properties.put(propContent, contentData);

    QName associationNameQName = QName.createQName(
      ContentModel.PROP_NAME.getNamespaceURI(),
      name
    );

    final ChildAssociationRef assocRef = nodeService.createNode(
      parent,
      associationQname,
      associationNameQName,
      typeQname,
      properties
    );

    return assocRef.getChildRef();
  }

  private void updateContent(
    final NodeRef contentRef,
    final File fileContent,
    final InputStream inputStream,
    final String stringContent,
    final String mimetype,
    final String encoding
  ) {
    // get a writer for the content and put the file
    QName propContent = Util.getPropContent(nodeService.getType(contentRef));

    final ContentWriter writer = contentService.getWriter(
      contentRef,
      propContent,
      true
    );

    // set the mimetype and encoding
    writer.setMimetype(mimetype);
    writer.setEncoding(encoding);
    if (fileContent != null) {
      writer.putContent(fileContent);
    } else if (inputStream != null) {
      writer.putContent(inputStream);
    } else {
      writer.putContent(stringContent == null ? "" : stringContent);
    }
  }

  private Map<QName, Serializable> extractedProperties(
    final File file,
    final String mimeType,
    final String encoding
  ) {
    Map<QName, Serializable> props = null;

    if (file == null) {
      return Collections.emptyMap();
    }

    try {
      //	Try and extract metadata from the file
      final ContentReader cr = new FileContentReader(file);
      cr.setMimetype(mimeType);
      cr.setEncoding(encoding);

      props = metadataManager.extractContentMetadata(cr);
    } catch (Exception ignore) {
      logger.error(
        "Error extracting metadata for file '" + file.getPath() + "'.",
        ignore
      );

      props = null;
    }

    if (props == null) {
      return Collections.emptyMap();
    } else {
      return props;
    }
  }

  /**
   * Save the specified content using the currently set wizard attributes
   *
   * @param fileContent File content to save
   * @param strContent  String content to save (if fileContent is null)
   */
  private void updateProperties(
    final NodeRef fileNodeRef,
    final String mimetype,
    final Map<QName, Serializable> extractedProps
  ) {
    final Serializable author = extractedProps.get(ContentModel.PROP_AUTHOR);
    final Serializable title = extractedProps.get(ContentModel.PROP_TITLE);
    final Serializable description = extractedProps.get(
      ContentModel.PROP_DESCRIPTION
    );

    // set the author aspect
    final Map<QName, Serializable> authorProps = Collections.singletonMap(
      ContentModel.PROP_AUTHOR,
      author
    );
    nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_AUTHOR, authorProps);

    // take ownership (added for Improvement DIGIT-CIRCABC-1841)
    ownableService.takeOwnership(fileNodeRef);

    // apply the titled aspect - title and description
    final Map<QName, Serializable> titledProps = new HashMap<>(3, 1.0f);
    titledProps.put(ContentModel.PROP_TITLE, title);
    titledProps.put(ContentModel.PROP_DESCRIPTION, description);
    nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);

    final boolean inlineEdit = isInlineEditable(mimetype);

    // apply the inlineeditable aspect
    if (inlineEdit) {
      final Map<QName, Serializable> editProps = Collections.singletonMap(
        ApplicationModel.PROP_EDITINLINE,
        (Serializable) Boolean.TRUE
      );
      nodeService.addAspect(
        fileNodeRef,
        ApplicationModel.ASPECT_INLINEEDITABLE,
        editProps
      );
    }
  }

  private boolean isInlineEditable(final String mimetype) {
    try {
      return metadataManager.isInlineEditable(mimetype);
    } catch (Exception ignore) {
      logger.error(
        "Error resolving if mimetype '" + mimetype + "' is inline editable.",
        ignore
      );

      return false;
    }
  }

  /**
   * Guesses the MIME type for the given file name.
   *
   * @param fileName the file name whose MIME type is resolved
   * @return the guessed MIME type, or {@link MimetypeMap#MIMETYPE_BINARY} when
   *         resolution fails
   */
  public String computeMimeType(final String fileName) {
    try {
      return metadataManager.guessMimetype(fileName);
    } catch (Exception ignore) {
      logger.error(
        "Error resolving mimetype for content filename: '" + fileName + "'.",
        ignore
      );

      return MimetypeMap.MIMETYPE_BINARY;
    }
  }

  /**
   * Guesses the character encoding of the given file for the specified MIME type.
   *
   * @param fileContent the file whose encoding is resolved; may be {@code null}
   * @param mimetype    the MIME type used to guide encoding detection
   * @return the detected encoding name, or the platform default charset name when
   *         {@code fileContent} is {@code null} or detection fails
   */
  public String computeEncoding(final File fileContent, final String mimetype) {
    try {
      if (fileContent != null) {
        return guessEncodingFromFile(fileContent, mimetype);
      }
    } catch (Exception ignore) {
      logger.error(
        "Error resolving encoding for mimetype '" + mimetype + "'.",
        ignore
      );
    }

    // return default encoding.
    return Charset.defaultCharset().name();
  }

  private String guessEncodingFromFile(
    final File fileContent,
    final String mimetype
  ) {
    try (
      InputStream is = new BufferedInputStream(
        new FileInputStream(fileContent)
      );
    ) {
      return metadataManager.guessEncoding(is, mimetype);
    } catch (final Exception e) {
      logger.error(
        "Failed to get encoding from file: " + fileContent.getPath(),
        e
      );
    }
    return Charset.defaultCharset().name();
  }
}
