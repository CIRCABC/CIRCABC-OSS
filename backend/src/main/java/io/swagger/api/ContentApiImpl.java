package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.bulk.BulkService;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexRecord;
import eu.europa.ec.digit.circabc.rest.service.bulk.indexes.IndexService;
import eu.europa.ec.digit.circabc.rest.service.compress.ZipService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.translation.TranslationService;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.FileUtil;
import io.swagger.util.RestInputSanitizer;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import javax.xml.stream.XMLStreamException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.*;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Default implementation of {@link ContentApi}, providing the business logic behind the CIRCABC
 * content-related REST operations.
 *
 * <p>This service manipulates Alfresco content nodes (documents, URLs, folders) and their
 * associated metadata. Its responsibilities include:
 *
 * <ul>
 *   <li>Reading version history for a document (full history or the most recent versions).
 *   <li>Creating and updating content nodes, including their document properties (title,
 *       description, author, reference, status, security ranking, issue/expiration dates and
 *       dynamic attributes).
 *   <li>Deleting documents, including cascading deletion of translations for multilingual
 *       documents.
 *   <li>Managing multilingual documents: making a node multilingual, adding manual translations,
 *       requesting machine translations and listing the translations of a node.
 *   <li>Managing discussion topics attached to a document.
 *   <li>Building and storing downloadable ZIP archives (bulk download) of selected nodes.
 * </ul>
 *
 * <p>Collaborating Alfresco and CIRCABC services are injected via Spring {@link Autowired}. This
 * class is not itself a web script; it is invoked by the web script endpoints under
 * {@code eu.europa.ec.digit.circabc.rest}.
 *
 * @author beaurpi
 */
public class ContentApiImpl implements ContentApi {

  /** Error message used when a multilingual-only operation is attempted on a non-multilingual node. */
  public static final String THIS_NODE_IS_NOT_MULTILINGUAL =
    "this node is not multilingual";
  /** Prefix of the dynamic (custom) document attribute names, suffixed with an index (e.g. {@code dynAttr1}). */
  public static final String DYN_ATTR = "dynAttr";
  /** Local name of the forum discussion child association attached to a document. */
  public static final String DISCUSSION = "discussion";
  /**
   * A logger for the class
   */
  private static final Log logger = LogFactory.getLog(ContentApiImpl.class);
  /** Request property name carrying the document issue date. */
  private static final String ISSUE_DATE = "issue_date";
  /** Request property name carrying the document expiration date. */
  private static final String EXPIRATION_DATE = "expiration_date";
  /** Request property name carrying the document reference. */
  private static final String REFERENCE = "reference";
  /** Request property name carrying the document author. */
  private static final String AUTHOR = "author";
  /** Request property name carrying the content mimetype. */
  private static final String MIMETYPE = "mimetype";
  /** Request property name carrying the content encoding. */
  private static final String ENCODING = "encoding";
  /** Request property name carrying the document status. */
  private static final String STATUS = "status";
  /** Request property name carrying the document security ranking. */
  private static final String SECURITY = "security";
  /** Request property name carrying the URL of a URL-type document. */
  private static final String URL = "url";

  @Autowired
  private VersionService versionService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private TranslationService translationService;

  @Autowired
  private MultilingualContentService multilingualContentService;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private ZipService zipService = null;

  @Autowired
  private IndexService indexService = null;

  @Autowired
  private BulkService bulkService = null;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  @Autowired
  private ServiceRegistry serviceRegistry;

  @Autowired
  private LogService logService;

  @Autowired
  private ActionService actionService;

  @Autowired
  private MimetypeService mimetypeService;

  /*
   * (non-Javadoc)
   *
   * @see io.swagger.api.ContentApi#contentIdVersionsGet(java.lang.String, java.lang.String)
   */
  /**
   * Returns the full version history of a versioned content node.
   *
   * @param id the identifier of the content node
   * @param language the requested language for localized properties (may be {@code null})
   * @return the list of {@link Version}s, ordered as returned by the version service; an empty list
   *     if the node is not versioned
   */
  @Override
  public List<Version> contentIdVersionsGet(String id, String language) {
    List<Version> result = new ArrayList<>();
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (versionService.isVersioned(nodeRef)) {
      VersionHistory vhNode = versionService.getVersionHistory(nodeRef);
      for (org.alfresco.service.cmr.version.Version vTmp : vhNode.getAllVersions()) {
        Version modelVersion = new Version();
        modelVersion.setVersionLabel(vTmp.getVersionLabel());
        Node node = nodesApi.getNode(vTmp.getFrozenStateNodeRef());
        setAdditionalNodeProperties(node, vTmp);
        modelVersion.setNode(node);
        modelVersion.setNotes(
          vTmp.getDescription() != null ? vTmp.getDescription() : ""
        );
        result.add(modelVersion);
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see io.swagger.api.ContentApi#contentIdVersionsGet(java.lang.String, java.lang.String)
   */
  /**
   * Returns the most recent versions of a versioned content node: the current (head) version
   * followed by up to nine of its predecessors, newest first.
   *
   * @param id the identifier of the content node
   * @return the list of up to ten most recent {@link Version}s; an empty list if the node is not
   *     versioned
   */
  @Override
  public List<Version> contentIdFirstVersionsGet(String id) {
    List<Version> result = new ArrayList<>();
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (versionService.isVersioned(nodeRef)) {
      org.alfresco.service.cmr.version.Version vHead =
        versionService.getCurrentVersion(nodeRef);

      Version headVersion = new Version();
      headVersion.setVersionLabel(vHead.getVersionLabel());
      Node node = nodesApi.getNode(vHead.getFrozenStateNodeRef());
      setAdditionalNodeProperties(node, vHead);
      headVersion.setNode(node);
      headVersion.setNotes(
        vHead.getDescription() != null ? vHead.getDescription() : ""
      );
      result.add(headVersion);

      NodeRef nextVersionRef = vHead.getFrozenStateNodeRef();
      VersionHistory history = versionService.getVersionHistory(nextVersionRef);
      org.alfresco.service.cmr.version.Version previousVersion = vHead;

      for (int i = 0; i < 9; i++) {
        previousVersion = history.getPredecessor(previousVersion);

        if (previousVersion != null) {
          Version precedingVersion = new Version();
          precedingVersion.setVersionLabel(previousVersion.getVersionLabel());
          Node previousNode = nodesApi.getNode(
            previousVersion.getFrozenStateNodeRef()
          );
          setAdditionalNodeProperties(previousNode, previousVersion);
          precedingVersion.setNode(previousNode);
          precedingVersion.setNotes(
            previousVersion.getDescription() != null
              ? previousVersion.getDescription()
              : ""
          );
          result.add(precedingVersion);
        } else {
          break;
        }
      }
    }

    return result;
  }

  /**
   * Enriches a version's {@link Node} with additional properties derived from the version metadata,
   * namely the version's modification date and the id of the original container of the versioned
   * node.
   *
   * @param node the node view to enrich (modified in place)
   * @param version the source Alfresco version providing the additional metadata
   */
  private void setAdditionalNodeProperties(
    Node node,
    org.alfresco.service.cmr.version.Version version
  ) {
    // set additional version properties
    String dateString = ISO8601DateFormat.format(
      (Date) version.getVersionProperty("modified")
    );
    node.getProperties().put("modified", dateString);
    node
      .getProperties()
      .put(
        "originalContainerId",
        nodeService
          .getPrimaryParent(version.getVersionedNodeRef())
          .getParentRef()
          .getId()
      );
  }

  /**
   * Returns a single version of a content node identified by its frozen-state node id.
   *
   * @param id the identifier of the content node
   * @param versionId the id of the frozen-state node representing the requested version
   * @param language the requested language for localized properties (may be {@code null})
   * @return the matching {@link Version}, or {@code null} if the node is not versioned or no version
   *     matches the given id
   */
  @Override
  public Version contentIdVersionsVersionIdGet(
    String id,
    String versionId,
    String language
  ) {
    Version result = null;
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (versionService.isVersioned(nodeRef)) {
      VersionHistory vhNode = versionService.getVersionHistory(nodeRef);
      for (org.alfresco.service.cmr.version.Version vTmp : vhNode.getAllVersions()) {
        if (vTmp.getFrozenStateNodeRef().getId().equals(versionId)) {
          result = new Version();
          result.setVersionLabel(vTmp.getVersionLabel());
          result.setNode(nodesApi.getNode(vTmp.getFrozenStateNodeRef()));
          result.setNotes(
            vTmp.getDescription() != null ? vTmp.getDescription() : ""
          );
        }
      }
    }

    return result;
  }

  /**
   * Deletes a content node. Before deletion the id of the current interest group root is recorded
   * on the node so that it can be traced from the archive. When the node is the pivot translation of
   * a multilingual document, all of its sibling translations are deleted as well.
   *
   * @param id the identifier of the content node to delete
   */
  @Override
  public void contentIdDelete(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    NodeRef igRoot = apiToolBox.getCurrentInterestGroup(nodeRef);
    nodeService.setProperty(
      nodeRef,
      CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED,
      igRoot.getId()
    );

    if (
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ) {
      NodeRef pivotRef = multilingualContentService.getPivotTranslation(
        nodeRef
      );
      if (pivotRef.getId().equals(nodeRef.getId())) {
        Map<Locale, NodeRef> modelTrans =
          multilingualContentService.getTranslations(nodeRef);
        for (Entry<Locale, NodeRef> entry : modelTrans.entrySet()) {
          NodeRef tmpRef = entry.getValue();
          if (!tmpRef.equals(nodeRef)) {
            this.nodeService.deleteNode(tmpRef);
          }
        }
      }
    }

    this.nodeService.deleteNode(nodeRef);
  }

  /**
   * Returns the translations of a multilingual document together with its pivot translation.
   *
   * @param id the identifier of any node belonging to the multilingual document
   * @return the {@link Translations} holding the list of translation nodes and the pivot node
   */
  @Override
  public Translations contentIdTranslationsGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    Map<Locale, NodeRef> modelTrans =
      multilingualContentService.getTranslations(nodeRef);
    Translations result = new Translations();
    for (Entry<Locale, NodeRef> entry : modelTrans.entrySet()) {
      NodeRef tmpRef = entry.getValue();
      Node tmpNode = nodesApi.getNode(tmpRef);
      result.getTranslations().add(tmpNode);
    }

    result.setPivot(
      nodesApi.getNode(multilingualContentService.getPivotTranslation(nodeRef))
    );

    return result;
  }

  /**
   * Updates the editable metadata of a content node from the supplied body.
   *
   * <p>Updates the name, title, description, issue and expiration dates, reference, status, security
   * ranking, author and the dynamic attributes ({@code dynAttr1}..{@code dynAttr20}). For URL-type
   * documents the URL property is updated; otherwise the content's encoding and mimetype are updated
   * when provided.
   *
   * @param id the identifier of the content node to update
   * @param body the node carrying the new property values
   */
  @Override
  public void contentIdPut(String id, Node body) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    this.nodeService.setProperty(
      nodeRef,
      ContentModel.PROP_NAME,
      body.getName().trim()
    );
    this.nodeService.setProperty(
      nodeRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(body.getTitle())
    );
    this.nodeService.setProperty(
      nodeRef,
      ContentModel.PROP_DESCRIPTION,
      Converter.toMLText(body.getDescription())
    );

    setDateProperty(body, nodeRef, ISSUE_DATE, DocumentModel.PROP_ISSUE_DATE);
    setDateProperty(
      body,
      nodeRef,
      EXPIRATION_DATE,
      DocumentModel.PROP_EXPIRATION_DATE
    );

    this.nodeService.setProperty(
      nodeRef,
      DocumentModel.PROP_REFERENCE,
      body.getProperties().get(REFERENCE)
    );
    this.nodeService.setProperty(
      nodeRef,
      DocumentModel.PROP_STATUS,
      body.getProperties().get(STATUS)
    );
    String security = body.getProperties().get(SECURITY);
    if (security != null && !security.isEmpty()) {
      this.nodeService.setProperty(
        nodeRef,
        DocumentModel.PROP_SECURITY_RANKING,
        security
      );
    }
    this.nodeService.setProperty(
      nodeRef,
      ContentModel.PROP_AUTHOR,
      body.getProperties().get(AUTHOR)
    );

    for (int i = 1; i < 21; i++) {
      if (body.getProperties().get(DYN_ATTR + i) != null) {
        QName q = QName.createQName(
          DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
          DYN_ATTR + i
        );
        this.nodeService.setProperty(
          nodeRef,
          q,
          body.getProperties().get(DYN_ATTR + i)
        );
      }
    }

    // in case it's a URL
    if (
      nodeService.hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE) &&
      body.getProperties().get(URL) != null
    ) {
      String url = RestInputSanitizer.requireSafeHttpUrl(
        body.getProperties().get(URL)
      );
      this.nodeService.setProperty(nodeRef, DocumentModel.PROP_URL, url);
    } else {
      ContentData cData = (ContentData) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_CONTENT
      );

      // FIX BUG DIGITCIRACB-4844 - ContentData.setEncoding and
      // ContentData.setMimetype methods return a new object
      if (body.getProperties().get(ENCODING) != null) {
        cData = ContentData.setEncoding(
          cData,
          body.getProperties().get(ENCODING)
        );
      }
      if (body.getProperties().get(MIMETYPE) != null) {
        cData = ContentData.setMimetype(
          cData,
          body.getProperties().get(MIMETYPE)
        );
      }

      //FIX BUG DIGITCIRACB-4844 - Save the updated cData
      this.nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, cData);
    }
  }

  /**
   * Sets a date-valued property on a node from a string request property, parsing the string into a
   * {@link Date}. An empty string leaves the property unchanged; a {@code null} or {@code "null"}
   * value clears it. Parse errors are logged and swallowed.
   *
   * @param body the node carrying the raw string property value
   * @param nodeRef the target node reference
   * @param propertyName the request property name holding the date string
   * @param propertyQName the Alfresco property to set with the parsed date
   */
  private void setDateProperty(
    Node body,
    NodeRef nodeRef,
    String propertyName,
    QName propertyQName
  ) {
    try {
      if (!body.getProperties().get(propertyName).equals("")) {
        if (
          body.getProperties().get(propertyName).equals("null") ||
          body.getProperties().get(propertyName) == null
        ) {
          this.nodeService.setProperty(nodeRef, propertyQName, null);
        } else {
          this.nodeService.setProperty(
            nodeRef,
            propertyQName,
            Converter.convertStringToDate(
              body.getProperties().get(propertyName)
            )
          );
        }
      }
    } catch (ParseException e) {
      logger.error(
        "Invalid issue date:" + body.getProperties().get(propertyName),
        e
      );
    }
  }

  /**
   * Returns the discussion topics attached to a content node.
   *
   * @param id the identifier of the content node
   * @return the list of topic {@link Node}s found under the node's discussion forum; an empty list
   *     if the node does not exist or has no discussion
   */
  @Override
  public List<Node> contentIdTopicsGet(String id) {
    NodeRef docRef = Converter.createNodeRefFromId(id);
    List<Node> result = new ArrayList<>();

    if (nodeService.exists(docRef)) {
      List<ChildAssociationRef> children = nodeService.getChildAssocs(docRef);
      NodeRef discussionRef = null;
      for (ChildAssociationRef childAssoc : children) {
        if (
          childAssoc
            .getQName()
            .equals(
              QName.createQName(
                NamespaceService.FORUMS_MODEL_1_0_URI,
                DISCUSSION
              )
            )
        ) {
          discussionRef = childAssoc.getChildRef();
        }
      }

      if (discussionRef != null) {
        List<ChildAssociationRef> topics = nodeService.getChildAssocs(
          discussionRef
        );
        for (ChildAssociationRef item : topics) {
          if (
            nodeService
              .getType(item.getChildRef())
              .equals(ForumModel.TYPE_TOPIC)
          ) {
            final NodeRef childRef = item.getChildRef();
            result.add(nodesApi.getNode(childRef));
          }
        }
      }
    }

    return result;
  }

  /**
   * Creates a new discussion topic under a content node, lazily creating the discussion forum if
   * none exists yet.
   *
   * @param id the identifier of the content or folder node the topic is attached to
   * @param body the node carrying the name of the topic to create
   * @return the created topic {@link Node}
   * @throws InvalidTypeException if the target node is neither a content nor a folder node
   */
  @Override
  public Node contentIdTopicsPost(String id, Node body) {
    NodeRef docRef = Converter.createNodeRefFromId(id);

    if (
      !(nodeService.getType(docRef).equals(ContentModel.TYPE_CONTENT) ||
        (nodeService.getType(docRef).equals(ContentModel.TYPE_FOLDER)))
    ) {
      throw new InvalidTypeException(
        "The node " + docRef + " does not have type content",
        nodeService.getType(docRef)
      );
    }

    List<ChildAssociationRef> children = nodeService.getChildAssocs(docRef);
    NodeRef discussionRef = null;
    for (ChildAssociationRef childAssoc : children) {
      if (
        childAssoc
          .getQName()
          .equals(
            QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, DISCUSSION)
          )
      ) {
        discussionRef = childAssoc.getChildRef();
      }
    }

    // not any discussion node yet created
    if (discussionRef == null) {
      discussionRef = nodeService
        .createNode(
          docRef,
          ForumModel.ASSOC_DISCUSSION,
          QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, DISCUSSION),
          ForumModel.TYPE_FORUM
        )
        .getChildRef();
    }

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, body.getName());
    NodeRef topicRef = nodeService
      .createNode(
        discussionRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.FORUMS_MODEL_1_0_URI,
          body.getName()
        ),
        ForumModel.TYPE_TOPIC,
        props
      )
      .getChildRef();

    return nodesApi.getNode(topicRef);
  }

  /**
   * Adds a manually uploaded translation of a multilingual document.
   *
   * <p>The uploaded file is stored as a new content node in the same parent folder as the source
   * document (with a unique name), given empty author/ownable aspects, and registered as a
   * translation for the requested locale.
   *
   * @param id the identifier of the multilingual source document
   * @param lang the language tag of the uploaded translation
   * @param file the input stream of the uploaded translation content
   * @param mimeType the mimetype of the uploaded content
   * @param fileName the desired file name of the translation
   * @return always {@code null}
   * @throws InvalidAspectException if the source node is not a multilingual document
   */
  @Override
  public Node contentIdTranslationsPost(
    String id,
    String lang,
    InputStream file,
    String mimeType,
    String fileName
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (
      !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ) {
      throw new InvalidAspectException(
        THIS_NODE_IS_NOT_MULTILINGUAL,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      );
    }

    NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

    String uniqueName = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      fileName
    );

    NodeRef createdRef = createContent(parentRef, file, mimeType, uniqueName);
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_AUTHOR, "");
    nodeService.addAspect(createdRef, ContentModel.ASPECT_AUTHOR, props);
    props = new HashMap<>();
    nodeService.addAspect(createdRef, ContentModel.ASPECT_OWNABLE, props);

    multilingualContentService.addTranslation(
      createdRef,
      nodeRef,
      I18NUtil.parseLocale(lang)
    );

    return null;
  }

  /**
   * Requests a machine translation of a multilingual document into the target language.
   *
   * <p>A copy of the source document is created (in its own transaction) and then submitted to the
   * translation service (in a separate transaction) for asynchronous translation.
   *
   * @param id the identifier of the multilingual source document
   * @param lang the target language tag
   * @param notify whether interested users should be notified when the translation completes
   * @return always {@code null}
   * @throws InvalidAspectException if the source node is not a multilingual document
   */
  @Override
  public Node requestMachineTranslation(
    String id,
    String lang,
    boolean notify
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    String sourceLanguage = nodeService
      .getProperty(nodeRef, ContentModel.PROP_LOCALE)
      .toString();

    if (
      !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ) {
      throw new InvalidAspectException(
        THIS_NODE_IS_NOT_MULTILINGUAL,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      );
    }

    NodeRef copyOfDocument = copyDocumentWithTransaction(nodeRef, lang);

    Set<String> languages = HashSet.newHashSet(1);
    languages.add(lang.toUpperCase());
    if (copyOfDocument != null) {
      translateDocumentWithTransaction(
        nodeRef,
        copyOfDocument,
        sourceLanguage,
        languages,
        notify
      );
    }
    return null;
  }

  /**
   * Copies a document to be translated within its own non-propagating transaction, renaming the
   * copy after the source document's database id (preserving the file extension). Validates that the
   * file type and requested language are supported.
   *
   * @param nodeRef the source document to copy
   * @param lang the target language tag
   * @return the node reference of the created copy
   * @throws InvalidOperationException if the file type or language is unsupported, or if the copy
   *     cannot be performed
   */
  private NodeRef copyDocumentWithTransaction(NodeRef nodeRef, String lang) {
    @SuppressWarnings("deprecation")
    UserTransaction trx = serviceRegistry
      .getTransactionService()
      .getNonPropagatingUserTransaction(false);
    try {
      trx.begin();
      NodeRef copyOfDocument = translationService.copyDocumentToBeTranslated(
        nodeRef
      );
      Long dbId = (Long) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_NODE_DBID
      );
      String name = (String) nodeService.getProperty(
        copyOfDocument,
        ContentModel.PROP_NAME
      );
      int lastIndexOf = name.lastIndexOf('.');
      String newName = String.valueOf(dbId);
      if (lastIndexOf > -1) {
        String extension = name.substring(lastIndexOf + 1);
        newName = newName + "." + extension;
      }
      if (!translationService.canBeTranslated(name)) {
        throw new InvalidOperationException(
          "File type extension: " + name + " is not supported"
        );
      }

      if (
        !translationService.getAvailableLanguages().contains(lang.toUpperCase())
      ) {
        throw new InvalidOperationException(
          "Language: " + lang + " is not supported"
        );
      }

      nodeService.setProperty(copyOfDocument, ContentModel.PROP_NAME, newName);
      trx.commit();
      return copyOfDocument;
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Try to rollback transaction" + e);
      }
      try {
        trx.rollback();
      } catch (SystemException e1) {
        if (logger.isErrorEnabled()) {
          logger.error("Can not rollback transaction" + e1);
        }
      }
      throw new InvalidOperationException("Can not copy document");
    }
  }

  /**
   * Submits a document copy to the translation service within its own non-propagating transaction.
   *
   * @param nodeRef the source document
   * @param copyOfDocument the copy created to receive the translation
   * @param sourceLanguage the language of the source document
   * @param languages the set of target language tags
   * @param notify whether interested users should be notified when the translation completes
   * @throws InvalidOperationException if the translation request cannot be performed
   */
  private void translateDocumentWithTransaction(
    NodeRef nodeRef,
    NodeRef copyOfDocument,
    String sourceLanguage,
    Set<String> languages,
    boolean notify
  ) {
    @SuppressWarnings("deprecation")
    UserTransaction trx = serviceRegistry
      .getTransactionService()
      .getNonPropagatingUserTransaction(false);
    try {
      trx.begin();
      translationService.translateDocument(
        nodeRef,
        copyOfDocument,
        sourceLanguage.toUpperCase(),
        languages,
        notify
      );
      trx.commit();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Try to rollback  transaction" + e);
      }
      try {
        trx.rollback();
      } catch (SystemException e1) {
        if (logger.isErrorEnabled()) {
          logger.error("Can not rollback transaction" + e1);
        }
      }
      throw new InvalidOperationException(
        "Can request translation of document"
      );
    }
  }

  /**
   * Creates a plain content node under the given parent, writing the supplied stream as its content
   * and temporarily disabling the content-notify behaviour during creation.
   *
   * @param parentRef the parent folder node reference
   * @param file the input stream providing the content bytes
   * @param mimeType the mimetype of the content
   * @param fileName the name of the node to create
   * @return the node reference of the created content node
   */
  private NodeRef createContent(
    NodeRef parentRef,
    InputStream file,
    String mimeType,
    String fileName
  ) {
    NodeRef nodeRef;

    try {
      QName associationNameQName = QName.createQName(
        ContentModel.PROP_NAME.getNamespaceURI(),
        fileName
      );
      policyBehaviourFilter.disableBehaviour(
        parentRef,
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
      nodeRef = nodeService
        .createNode(
          parentRef,
          ContentModel.ASSOC_CONTAINS,
          associationNameQName,
          ContentModel.TYPE_CONTENT
        )
        .getChildRef();
      nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, fileName);

      QName propContent = Util.getPropContent(nodeService.getType(nodeRef));

      final ContentWriter writer = contentService.getWriter(
        nodeRef,
        propContent,
        true
      );
      writer.setMimetype(mimeType);
      writer.putContent(file);
    } finally {
      policyBehaviourFilter.enableBehaviour(
        parentRef,
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
    }

    return nodeRef;
  }

  /**
   * Makes a content node a multilingual document with the given pivot language.
   *
   * <p>Existing properties are preserved across the conversion, integrity checking is switched to
   * warning mode, the versionable aspect is temporarily removed and re-added around the conversion,
   * and the author is propagated to both the node and the multilingual container.
   *
   * @param id the identifier of the content node to make multilingual
   * @param body the metadata carrying the pivot language and author
   */
  @Override
  public void contentIdMultilingualAspectPost(
    String id,
    MultilingualAspectMetadata body
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
    props.remove(ContentModel.PROP_LOCALE);

    // Added to warn about integrity errors instead of throwing the
    // exception (Alfresco 4)
    if (!IntegrityChecker.isWarnInTransaction()) {
      IntegrityChecker.setWarnInTransaction();
    }

    // Do the jobs
    final Locale locale = I18NUtil.parseLocale(body.getPivotLang());

    // https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2290
    nodeService.removeAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE);

    // make this node multilingual
    multilingualContentService.makeTranslation(nodeRef, locale);
    final NodeRef mlContainer =
      multilingualContentService.getTranslationContainer(nodeRef);

    // backup and reaply properties of non i18n node
    nodeService.addProperties(nodeRef, props);

    // if the author of the node is not set, set it with the default author
    // name of
    // the new ML Container
    String nodeAuthor = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_AUTHOR
    );

    if (
      nodeAuthor == null || (nodeAuthor.isEmpty() && body.getAuthor() != null)
    ) {
      nodeService.setProperty(
        nodeRef,
        ContentModel.PROP_AUTHOR,
        body.getAuthor()
      );
    }

    // set properties of the ml container
    nodeService.setProperty(
      mlContainer,
      ContentModel.PROP_AUTHOR,
      body.getAuthor()
    );

    // https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2290
    nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
  }

  /**
   * Builds a ZIP archive of the given nodes together with a metadata index file and writes it to the
   * supplied output stream. Nodes for which the current user lacks read permission are skipped (and
   * logged).
   *
   * @param nodeIds the identifiers of the nodes to include in the archive
   * @param outputStream the stream the ZIP archive is written to
   * @throws IOException if reading the nodes or writing the archive fails
   * @throws XMLStreamException if generating the index records fails
   * @throws IllegalArgumentException if one of the given node ids does not correspond to an existing
   *     node
   */
  public void buildZip(String[] nodeIds, OutputStream outputStream)
    throws IOException, XMLStreamException {
    List<NodeRef> nodeRefs = new ArrayList<>();

    for (String nodeId : nodeIds) {
      NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

      if (!nodeService.exists(nodeRef)) {
        throw new IllegalArgumentException(nodeId + " is not a valid node id.");
      }

      if (
        currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeId)
      ) {
        nodeRefs.add(nodeRef);
      } else {
        logger.warn(
          "The node " +
            nodeId +
            " has been ignored for the bulk download. User does not have read permission on node"
        );
      }
    }

    File tempZipFile = TempFileProvider.createTempFile(
      "bulk",
      ".zip",
      TempFileProvider.getTempDir()
    );
    File tempIndexFile = TempFileProvider.createTempFile(
      "bulk",
      ".txt",
      TempFileProvider.getTempDir()
    );

    final List<IndexRecord> indexRecords = bulkService.getMetaData(nodeRefs);

    indexService.generateIndexRecords(tempIndexFile, indexRecords);

    zipService.addingFileIntoArchive(nodeRefs, tempZipFile, tempIndexFile);

    try (FileInputStream inputStream = new FileInputStream(tempZipFile)) {
      IOUtils.copy(inputStream, outputStream);
    }
  }

  /**
   * Builds a downloadable ZIP archive of the given nodes and stores it as a new content node under
   * the specified parent folder, tagging it with the library aspect.
   *
   * @param nodeIds the identifiers of the nodes to include in the archive
   * @param parentId the identifier of the parent folder where the ZIP node is created
   * @return the node reference of the created ZIP content node
   * @throws IOException if building the archive or storing it fails
   * @throws XMLStreamException if generating the index records fails
   * @throws AccessDeniedException if the current user lacks write permission on the parent folder
   * @see ContentApi#createDownloadableZip(String[], String)
   */
  @Override
  public NodeRef createDownloadableZip(String[] nodeIds, String parentId)
    throws IOException, XMLStreamException {
    if (
      !currentUserPermissionCheckerService.hasAlfrescoWritePermission(parentId)
    ) {
      throw new AccessDeniedException(
        "You don't have permission to write the content ZIP file in this space."
      );
    }

    NodeRef parentRef = Converter.createNodeRefFromId(parentId);

    File tempFile = TempFileProvider.createTempFile(
      "contents-",
      GUID.generate()
    );

    try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
      buildZip(nodeIds, fileOutputStream);
    }

    try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {
      NodeRef contentNodeRef = createContent(
        parentRef,
        fileInputStream,
        "application/zip;charset=UTF-8",
        "contents-" + GUID.generate() + ".zip"
      );

      nodeService.addAspect(contentNodeRef, CircabcModel.ASPECT_LIBRARY, null);

      return contentNodeRef;
    } finally {
      tempFile.deleteOnExit();
    }
  }

  /**
   * Creates a new content node (document or URL) under a parent folder with the given metadata and
   * content stream.
   *
   * <p>If the browser-supplied mimetype is generic binary, the mimetype is re-guessed from the file
   * name. The content-notify behaviour is disabled during creation. When {@code isPivot} is true the
   * created node is additionally made the pivot of a multilingual document. An upload log record is
   * written on success.
   *
   * @param parentId the identifier of the parent folder (a folder or information-news node)
   * @param name the desired file name (made unique within the parent)
   * @param title the multilingual title (may be {@code null})
   * @param description the multilingual description (may be {@code null})
   * @param author the author (may be {@code null} or empty, in which case metadata extraction runs)
   * @param reference the document reference (may be {@code null})
   * @param securityRanking the security ranking (defaults to normal when empty)
   * @param status the document status (defaults to draft when empty)
   * @param keywords the ids of keyword nodes to associate (may be {@code null})
   * @param expiration the expiration date (may be {@code null})
   * @param mimetype the content mimetype
   * @param file the input stream providing the content bytes
   * @param isPivot whether the created node should become the pivot of a multilingual document
   * @param lang the pivot language tag, used when {@code isPivot} is true
   * @param dynProps the dynamic (custom) document properties (may be {@code null})
   * @return the node reference of the created content node
   * @throws InvalidNodeRefException if the parent node does not exist
   * @throws InvalidTypeException if the parent node is not a folder or information-news node
   */
  @Override
  public NodeRef createContent(
    String parentId,
    String name,
    I18nProperty title,
    I18nProperty description,
    String author,
    String reference,
    String securityRanking,
    String status,
    String[] keywords,
    Date expiration,
    String mimetype,
    InputStream file,
    Boolean isPivot,
    String lang,
    Map<String, Object> dynProps
  ) {
    //BUG DIGITCIRCABC-4849
    //If mime type returned by browser is binary, check if it is the right type!
    if (MimetypeMap.MIMETYPE_BINARY.equals(mimetype)) {
      String guessMimeType = mimetypeService.guessMimetype(name);
      //check if the guessed mime type is not binary
      if (!guessMimeType.equals(mimetype)) {
        logger.warn(
          "Wrong mime type for file " +
            name +
            "! change it from " +
            mimetype +
            " to " +
            guessMimeType
        );
        mimetype = guessMimeType;
      }
    }

    NodeRef parentRef = Converter.createNodeRefFromId(parentId);
    if (!nodeService.exists(parentRef)) {
      throw new InvalidNodeRefException("parent does not exist", parentRef);
    }
    if (
      !(nodeService.getType(parentRef).equals(ContentModel.TYPE_FOLDER) ||
        nodeService
          .getType(parentRef)
          .equals(CircabcModel.TYPE_INFORMATION_NEWS))
    ) {
      throw new InvalidTypeException(
        "parent does not have a correct type" + parentRef,
        ContentModel.TYPE_FOLDER
      );
    }

    boolean wasEnabled = !policyBehaviourFilter.isEnabled();
    policyBehaviourFilter.disableBehaviour(
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
    String newfilename = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      name
    );
    NodeRef nodeRef = createContentInternal(
      parentRef,
      newfilename,
      title,
      description,
      author,
      reference,
      securityRanking,
      status,
      keywords,
      expiration,
      mimetype,
      file,
      dynProps
    );

    if (Boolean.TRUE.equals(isPivot)) {
      MultilingualAspectMetadata body = new MultilingualAspectMetadata();
      if (!isNullEmptyString(author)) {
        body.setAuthor(author);
      } else {
        body.setAuthor("");
      }
      body.setPivotLang(lang);
      contentIdMultilingualAspectPost(nodeRef.getId(), body);
      // reaplyexpiration
    }

    final LogRecord logRecord = logService.prepareLogUploadRequest(
      nodeRef,
      name
    );
    logRecord.setOK(true);
    logService.log(logRecord);

    if (wasEnabled) {
      policyBehaviourFilter.enableBehaviour(
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
    }

    return nodeRef;
  }

  /**
   * Creates the content node and applies its properties, aspects and content.
   *
   * <p>Sets the name, title, description and author properties, adds the document B/C property
   * aspects, writes the content, and, when no author was supplied, triggers asynchronous metadata
   * extraction.
   *
   * @param parentRef the parent folder node reference
   * @param newfilename the unique file name of the node to create
   * @param title the multilingual title (may be {@code null})
   * @param description the multilingual description (may be {@code null})
   * @param author the author (may be {@code null} or empty)
   * @param reference the document reference
   * @param securityRanking the security ranking
   * @param status the document status
   * @param keywords the ids of keyword nodes to associate (may be {@code null})
   * @param expiration the expiration date (may be {@code null})
   * @param mimetype the content mimetype
   * @param file the input stream providing the content bytes
   * @param dynProps the dynamic (custom) document properties (may be {@code null})
   * @return the node reference of the created content node
   */
  @SuppressWarnings("java:S107") // Mirrors interface contract parameters
  private NodeRef createContentInternal(
    NodeRef parentRef,
    String newfilename,
    I18nProperty title,
    I18nProperty description,
    String author,
    String reference,
    String securityRanking,
    String status,
    String[] keywords,
    Date expiration,
    String mimetype,
    InputStream file,
    Map<String, Object> dynProps
  ) {
    QName associationNameQName = QName.createQName(
      ContentModel.PROP_NAME.getNamespaceURI(),
      newfilename
    );

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, newfilename);

    if (title != null) {
      props.put(ContentModel.PROP_TITLE, Converter.toMLText(title));
    }

    if (description != null) {
      props.put(ContentModel.PROP_DESCRIPTION, Converter.toMLText(description));
    }

    boolean isAuthorSet = !isNullEmptyString(author);
    if (isAuthorSet) {
      props.put(ContentModel.PROP_AUTHOR, author);
    }

    NodeRef nodeRef = nodeService
      .createNode(
        parentRef,
        ContentModel.ASSOC_CONTAINS,
        associationNameQName,
        ContentModel.TYPE_CONTENT,
        props
      )
      .getChildRef();

    addBProperties(nodeRef, expiration, securityRanking);
    addCProperties(nodeRef, reference, status, keywords, dynProps);

    ContentWriter writer = contentService.getWriter(
      nodeRef,
      ContentModel.PROP_CONTENT,
      true
    );
    writer.setMimetype(mimetype);
    writer.putContent(file);

    if (!isAuthorSet) {
      Action action = actionService.createAction(
        ContentMetadataExtracter.EXECUTOR_NAME
      );
      action.setExecuteAsynchronously(true);
      actionService.executeAction(action, nodeRef, true, false);
    }

    return nodeRef;
  }

  /**
   * Adds the "B properties" aspect to a node, holding the expiration date and security ranking
   * (defaulting the security ranking to normal when not supplied).
   *
   * @param nodeRef the target node reference
   * @param expiration the expiration date (may be {@code null})
   * @param securityRanking the security ranking (defaults to normal when empty)
   */
  private void addBProperties(
    NodeRef nodeRef,
    Date expiration,
    String securityRanking
  ) {
    Map<QName, Serializable> bprops = new HashMap<>();
    if (expiration != null) {
      bprops.put(DocumentModel.PROP_EXPIRATION_DATE, expiration);
    }
    bprops.put(
      DocumentModel.PROP_SECURITY_RANKING,
      !isNullEmptyString(securityRanking)
        ? securityRanking
        : DocumentModel.SECURITY_RANKINGS_NORMAL
    );
    nodeService.addAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES, bprops);
  }

  /**
   * Adds the "C properties" aspect to a node, holding the reference, status (defaulting to draft),
   * dynamic properties and associated keyword nodes.
   *
   * @param nodeRef the target node reference
   * @param reference the document reference (may be {@code null})
   * @param status the document status (defaults to draft when empty)
   * @param keywords the ids of keyword nodes to associate (may be {@code null})
   * @param dynProps the dynamic (custom) document properties (may be {@code null})
   */
  private void addCProperties(
    NodeRef nodeRef,
    String reference,
    String status,
    String[] keywords,
    Map<String, Object> dynProps
  ) {
    Map<QName, Serializable> cprops = new HashMap<>();

    if (!isNullEmptyString(reference)) {
      cprops.put(DocumentModel.PROP_REFERENCE, reference);
    }
    cprops.put(
      DocumentModel.PROP_STATUS,
      !isNullEmptyString(status) ? status : DocumentModel.STATUS_VALUE_DRAFT
    );

    addDynamicProperties(cprops, dynProps);

    if (keywords != null) {
      HashSet<NodeRef> keywordsToAdd = new HashSet<>();
      for (String keywordId : keywords) {
        keywordsToAdd.add(Converter.createNodeRefFromId(keywordId));
      }
      cprops.put(DocumentModel.PROP_KEYWORD, keywordsToAdd);
    }

    nodeService.addAspect(nodeRef, DocumentModel.ASPECT_BPROPERTIES, cprops);
  }

  /**
   * Copies the given dynamic properties into the target property map, skipping {@code null} values
   * and flattening JSON array values into a comma-separated string.
   *
   * @param cprops the property map to populate
   * @param dynProps the dynamic (custom) document properties (may be {@code null})
   */
  private void addDynamicProperties(
    Map<QName, Serializable> cprops,
    Map<String, Object> dynProps
  ) {
    if (dynProps == null) {
      return;
    }
    for (Entry<String, Object> entry : dynProps.entrySet()) {
      if (entry.getValue() == null || entry.getValue() == "null") {
        continue;
      }
      QName q = QName.createQName(
        DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
        entry.getKey()
      );
      if (entry.getValue() instanceof org.json.simple.JSONArray) {
        Serializable value = entry
          .getValue()
          .toString()
          .replace("[\"", "")
          .replace("\"]", "")
          .replace("\",\"", ",");
        cprops.put(q, value);
      } else {
        cprops.put(q, (Serializable) entry.getValue());
      }
    }
  }

  /**
   * Tests whether a string is considered empty, i.e. {@code null}, the empty string or the literal
   * {@code "null"}.
   *
   * @param string the string to test
   * @return {@code true} if the string is {@code null}, empty or {@code "null"}
   */
  private boolean isNullEmptyString(String string) {
    return string == null || "".equals(string) || "null".equals(string);
  }

  /**
   * Creates a translation of an existing multilingual document by uploading new content.
   *
   * <p>The translation is created as a content node in the source document's parent folder (with a
   * unique name) and the given metadata, tagged with author and ownable aspects, and registered as a
   * translation for the requested language. The content-notify behaviour is disabled during
   * creation.
   *
   * @param id the identifier of the multilingual source document
   * @param defaultName the desired file name (made unique within the parent)
   * @param title the multilingual title (may be {@code null})
   * @param description the multilingual description (may be {@code null})
   * @param author the author
   * @param reference the document reference
   * @param securityRanking the security ranking
   * @param statusProp the document status
   * @param keywords the ids of keyword nodes to associate (may be {@code null})
   * @param expirationDate the expiration date (may be {@code null})
   * @param mimeType the content mimetype
   * @param file the input stream providing the content bytes
   * @param lang the language tag of the translation
   * @param dynProps the dynamic (custom) document properties (may be {@code null})
   * @return the node reference of the created translation node
   * @throws InvalidAspectException if the source node is not a multilingual document
   */
  @Override
  public NodeRef createContentTranslation(
    String id,
    String defaultName,
    I18nProperty title,
    I18nProperty description,
    String author,
    String reference,
    String securityRanking,
    String statusProp,
    String[] keywords,
    Date expirationDate,
    String mimeType,
    InputStream file,
    String lang,
    Map<String, Object> dynProps
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (
      !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ) {
      throw new InvalidAspectException(
        THIS_NODE_IS_NOT_MULTILINGUAL,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      );
    }

    NodeRef parentRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

    boolean wasEnabled = !policyBehaviourFilter.isEnabled();
    policyBehaviourFilter.disableBehaviour(
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
    String newfilename = FileUtil.generateUniqueFilename(
      nodeService,
      parentRef,
      defaultName
    );
    NodeRef createdRef = createContentInternal(
      parentRef,
      newfilename,
      title,
      description,
      author,
      reference,
      securityRanking,
      statusProp,
      keywords,
      expirationDate,
      mimeType,
      file,
      dynProps
    );
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_AUTHOR, author);
    nodeService.addAspect(createdRef, ContentModel.ASPECT_AUTHOR, props);
    props = new HashMap<>();
    nodeService.addAspect(createdRef, ContentModel.ASPECT_OWNABLE, props);

    multilingualContentService.addTranslation(
      createdRef,
      nodeRef,
      I18NUtil.parseLocale(lang)
    );

    if (wasEnabled) {
      policyBehaviourFilter.enableBehaviour(
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
      );
    }

    return createdRef;
  }
}
