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

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * Business service helper that centralises metadata- and filename-related operations against the
 * Alfresco repository.
 *
 * <p>
 * It provides utilities to:
 * </p>
 * <ul>
 *   <li>extract content metadata from a {@link ContentReader} using the registered metadata
 *       extracters;</li>
 *   <li>guess the mimetype and character encoding of content;</li>
 *   <li>compute valid and unique child filenames for a given parent node;</li>
 *   <li>determine whether the current user owns the lock on a working copy;</li>
 *   <li>compute a display title for a node, falling back to its name.</li>
 * </ul>
 *
 * <p>
 * Repository collaborators are injected by Spring via {@link Autowired}.
 * </p>
 *
 * @author Yanick Pignot
 */
public class MetadataManager {

  /** Regular expression matching characters that are not allowed in Alfresco filenames. */
  private static final Pattern NAME_REPLACEALL_PATTERN = Pattern.compile(
    "[\"*\\\\><?\\/:|]"
  );

  /**
   * {@link MessageFormat} pattern used to build a unique filename by appending a counter, e.g.
   * {@code name(0).ext}. Arguments are: {0} base name, {1} counter, {2} extension.
   */
  private static final String UNIQUE_NAME = "{0}({1}){2}";

  /** Replacement string substituted for each invalid filename character. */
  private static final String INVALID_CAR_REPLACEMENT = "_";

  /** File extension separator character. */
  private static final char DOT = '.';

  private final Log logger = LogFactory.getLog(MetadataManager.class);

  @Autowired
  private MetadataExtracterRegistry metadataExtracterRegistry;

  @Autowired
  private MimetypeService mimetypeService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private NodeTypeManager nodeTypeManager;

  //--------------
  //-- public methods

  /**
   * Extract the metadata properties from the content pointed to by the given reader.
   *
   * <p>
   * The appropriate {@link MetadataExtracter} is looked up from the registry based on the reader's
   * mimetype. If no extracter is available for that mimetype, an empty map is returned. Any failure
   * during extraction is logged and results in an empty (or partially filled) property map rather
   * than a propagated exception.
   * </p>
   *
   * @param contentReader the reader providing access to the content and its mimetype (must expose a
   *                      non-empty mimetype)
   * @return a map of extracted properties keyed by their {@link QName}; never {@code null}, and
   *         empty when no extracter is registered for the mimetype
   * @throws IllegalArgumentException if the reader's mimetype is {@code null} or empty
   */
  public Map<QName, Serializable> extractContentMetadata(
    final ContentReader contentReader
  ) {
    final String mimetype = contentReader.getMimetype();
    Assert.hasText(mimetype, "Reader mimetype");

    // look for a transformer
    final MetadataExtracter extracter = metadataExtracterRegistry.getExtracter(
      mimetype
    );
    if (extracter == null) {
      return Collections.emptyMap();
    } else {
      final Map<QName, Serializable> properties = HashMap.newHashMap(10);

      try {
        // we have a transformer, so do it
        extracter.extract(contentReader, properties);
      } catch (Exception silentFailure) {
        // it failed
        logger.warn(
          "Metadata extraction failed: \n" +
            "   mimetype: " +
            mimetype +
            "\n" +
            "   extracter: " +
            extracter.getClass()
        );
      }

      return properties;
    }
  }

  /**
   * Indicate whether content of the given mimetype can be edited inline (in the browser).
   *
   * <p>
   * Only plain text content ({@code text/plain}) is currently considered inline editable.
   * </p>
   *
   * @param mimetype the mimetype to test; may be {@code null}
   * @return {@link Boolean#TRUE} if the mimetype is inline editable, {@link Boolean#FALSE} otherwise
   *         (including when {@code mimetype} is {@code null})
   */
  public Boolean isInlineEditable(final String mimetype) {
    if (mimetype == null) {
      return Boolean.FALSE;
    } else {
      // mime of text files
      final List<String> editableMimetypes = new ArrayList<>();
      editableMimetypes.add("text/plain");

      return editableMimetypes.contains(mimetype);
    }
  }

  /**
   * Guess the mimetype of a file from its extension.
   *
   * @param fileName the file name (optionally including an extension)
   * @return the mimetype resolved from the file extension, or {@link MimetypeMap#MIMETYPE_BINARY}
   *         when the extension is missing or unknown
   */
  public String guessMimetype(final String fileName) {
    // fall back to binary mimetype if no match found
    String mimetype = MimetypeMap.MIMETYPE_BINARY;
    final int extIndex = fileName.lastIndexOf('.');
    if (extIndex != -1) {
      String ext = fileName.substring(extIndex + 1).toLowerCase();
      String mt = mimetypeService.getMimetypesByExtension().get(ext);
      if (mt != null) {
        mimetype = mt;
      }
    }

    return mimetype;
  }

  /**
   * Guess the character encoding of the given content stream.
   *
   * @param inputStream the content stream to inspect
   * @param mimetype    the mimetype of the content, used to help resolve the charset
   * @return the name of the detected {@link Charset}
   */
  public String guessEncoding(
    final InputStream inputStream,
    final String mimetype
  ) {
    final ContentCharsetFinder charsetFinder =
      mimetypeService.getContentCharsetFinder();
    final Charset charset = charsetFinder.getCharset(inputStream, mimetype);

    return charset.name();
  }

  /**
   * Compute a valid and unique child filename for a given parent, using the default
   * {@link ContentModel#ASSOC_CONTAINS} child association.
   *
   * @param parent an existing parent node
   * @param name   a filename (not null)
   * @return a sanitised filename that is unique among the parent's children
   */
  public String getValidUniqueName(final NodeRef parent, final String name) {
    return getValidUniqueName(parent, ContentModel.ASSOC_CONTAINS, name);
  }

  /**
   * Compute a valid and unique child filename for a given parent.
   *
   * <p>
   * The name is first sanitised via {@link #getValidName(String)} and then, while a child with that
   * name already exists under the given association, a counter is appended (e.g. {@code name(0).ext})
   * until the name is unique.
   * </p>
   *
   * @param parent     an existing parent node
   * @param assocQname the child association type to check for existing children
   * @param name       a filename (not null)
   * @return a sanitised filename that is unique among the parent's children for the given association
   */
  public String getValidUniqueName(
    final NodeRef parent,
    final QName assocQname,
    final String name
  ) {
    String cleanName = getValidName(name);
    final Pair<String, String> split = splitFilename(cleanName);

    int counter = -1;
    while (exists(parent, assocQname, cleanName)) {
      cleanName = MessageFormat.format(
        UNIQUE_NAME,
        split.getFirst(),
        ++counter,
        split.getSecond()
      );
    }

    return cleanName;
  }

  /**
   * Compute a valid file name replacing unsuported caraters by -
   *
   * @param name The filname (not null)
   * @return A valid file name
   */
  public String getValidName(final String name) {
    return getValidName(name, INVALID_CAR_REPLACEMENT);
  }

  /**
   * Compute a valid file name replacing unsuported caraters by the given replacement String
   *
   * @param name        The clean filname (not null)
   * @param replacement The replacement string (not null)
   * @return A valid file name
   */
  public String getValidName(final String name, final String replacement) {
    final Matcher matcher = NAME_REPLACEALL_PATTERN.matcher(name.trim());

    return matcher.replaceAll(replacement);
  }

  /**
   * Return true if the current user is lock owner on a given working copy
   *
   * @param workingCopy The working copy
   * @return True is the nodeRef is a working copy and the user is the lock owner
   */
  public boolean isLockOwner(final NodeRef workingCopy) {
    if (nodeTypeManager.isWorkingCopyDocument(workingCopy)) {
      final Serializable obj = nodeService.getProperty(
        workingCopy,
        ContentModel.PROP_WORKING_COPY_OWNER
      );

      return (
        obj != null &&
        AuthenticationUtil.getFullyAuthenticatedUser().equals(obj)
      );
    } else {
      return false;
    }
  }

  /**
   * Return the title of the node. If the title doesn't exists, return the name.
   *
   * <p>
   * For performance issues, if you already get the properties of the node, use
   * <i>{@code computeTitle(Map<Qname, Serializable>)}</i>
   * </p>
   *
   * @param nodeRef The node ref to compute the title
   * @return Never a null or empty string
   */
  public String computeTitle(final NodeRef nodeRef) {
    return computeTitle(nodeService.getProperties(nodeRef));
  }

  /**
   * Return the title of the already retreived node properties. If the title doesn't exists, return
   * the name.
   *
   * <p>
   * For performance issues, use this method if the properties are already retreived from the
   * repository
   * </p>
   *
   * @param properties The node properties
   * @return Never a null or empty string
   */
  public String computeTitle(final Map<QName, Serializable> properties) {
    final String title = (String) properties.get(ContentModel.PROP_TITLE);

    if (title == null || title.trim().isEmpty()) {
      return (String) properties.get(ContentModel.PROP_NAME);
    } else {
      return title;
    }
  }

  //--------------
  //-- private helpers

  private boolean exists(
    final NodeRef parent,
    final QName assocQname,
    final String name
  ) {
    return nodeService.getChildByName(parent, assocQname, name) != null;
  }

  private Pair<String, String> splitFilename(final String name) {
    final int dotPosition = name.lastIndexOf(DOT);
    if (dotPosition > -1) {
      return new Pair<>(
        name.substring(0, dotPosition),
        name.substring(dotPosition)
      );
    } else {
      return new Pair<>(name, "");
    }
  }
}
