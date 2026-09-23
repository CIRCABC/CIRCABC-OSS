package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationManagerService;
import io.swagger.model.alfresco.aspect.DisableNotificationThreadLocal;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the {@link ClipboardApi} that carries out the repository-side clipboard
 * operations of CIRCABC: copying, moving and linking nodes between folders.
 *
 * <p>The {@code paste} methods interpret an integer action code (see {@link ClipboardAction} —
 * {@code COPY}, {@code MOVE} or {@code LINK}) and delegate to the appropriate Alfresco services.
 * Copying handles regular content/folders, multilingual translation containers and generic nodes,
 * automatically resolving name clashes by prefixing the copy with {@code "Copy_"}. Linking creates
 * file/folder link nodes, and moving relocates the node (or re-parents secondary associations).
 * Notifications are toggled per operation through {@link DisableNotificationThreadLocal} based on
 * the notification settings of the enclosing Interest Group.
 *
 * @author schwerr
 */
public class ClipboardApiImpl implements ClipboardApi {

  /** File name suffix appended to link nodes created by the LINK action. */
  private static final String LINK_NODE_EXTENSION = ".url";
  /**
   * Shallow search for nodes with a name pattern
   */
  private static final String XPATH_QUERY_NODE_MATCH =
    "./*[like(@cm:name, $cm:name, false)]";

  /** Resolves content model type hierarchy (used to distinguish content, folders and containers). */
  @Autowired
  private DictionaryService dictionaryService;

  /** Core Alfresco node service used for property access, node creation and re-parenting. */
  @Autowired
  private NodeService nodeService;

  /** Provides high-level copy/move operations for content and folders. */
  @Autowired
  private FileFolderService fileFolderService;

  /** Performs deep copy-and-rename operations for generic (non file/folder) node types. */
  @Autowired
  private CopyService copyService;

  /** Handles copy/move of multilingual translation containers. */
  @Autowired
  private MultilingualContentService multilingualContentService;

  /** Executes the XPath name-match query used to detect existing names in a destination folder. */
  @Autowired
  private SearchService searchService;

  /** Resolves namespace prefixes for search queries and node creation. */
  @Autowired
  private NamespaceService namespaceService;

  /** Exposes the paste-notification settings of the enclosing Interest Group. */
  @Autowired
  private NotificationManagerService notificationManagerService;

  /** Utility helper used to locate the Interest Group that contains a given node. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Pastes a list of nodes given as a comma separated string into the destination folder. According
   * to the action, the node is copied, linked or moved.
   *
   * <p>The paste-all notification status is evaluated once from the first node and, when disabled,
   * suppresses notifications for the whole batch. Nodes that no longer exist cause the batch to
   * stop early.
   *
   * @param nodeIds the identifiers of the nodes to paste
   * @param destRef the destination folder to paste into
   * @param action the operation to perform (see {@link ClipboardAction}: {@code COPY}, {@code MOVE}
   *     or {@code LINK})
   * @throws FileNotFoundException if a source or destination node cannot be resolved during the
   *     paste
   */
  public void paste(
    final String[] nodeIds,
    final NodeRef destRef,
    final int action
  ) throws FileNotFoundException {
    if (nodeIds.length > 0) {
      NodeRef firstNodeRef = Converter.createNodeRefFromId(nodeIds[0]);
      boolean shouldNotify = getNotificationStatus(firstNodeRef, true);
      DisableNotificationThreadLocal disableNotificationThreadLocal =
        new DisableNotificationThreadLocal();
      if (!shouldNotify) {
        disableNotificationThreadLocal.set(true);
      }
    }

    for (String nodeId : nodeIds) {
      NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

      if (!nodeService.exists(nodeRef)) {
        // if the item does not exist or has been deleted, return
        return;
      }

      paste(nodeRef, destRef, action, false);
    }
  }

  /**
   * Pastes a node into the destination folder. According to the action, the node is copied, linked
   * or moved.
   *
   * <p>The paste notification status for this single node is evaluated and applied before the
   * operation is executed.
   *
   * @param nodeRef the source node to paste
   * @param destRef the destination folder to paste into
   * @param action the operation to perform (see {@link ClipboardAction}: {@code COPY}, {@code MOVE}
   *     or {@code LINK})
   * @throws FileNotFoundException if the source or destination node cannot be resolved during the
   *     paste
   */
  public void paste(
    final NodeRef nodeRef,
    final NodeRef destRef,
    final int action
  ) throws FileNotFoundException {
    paste(nodeRef, destRef, action, true);
  }

  private void paste(
    final NodeRef nodeRef,
    final NodeRef destRef,
    final int action,
    boolean checkShouldNotify
  ) throws FileNotFoundException {
    DisableNotificationThreadLocal disableNotificationThreadLocal =
      new DisableNotificationThreadLocal();
    disableNotificationThreadLocal.set(false);

    if (checkShouldNotify && !getNotificationStatus(nodeRef, false)) {
      disableNotificationThreadLocal.set(true);
    }

    ChildAssociationRef assocRef = resolveAssocRef(nodeRef);
    boolean isPrimaryParent =
      getParent(nodeRef) == null ||
      getParent(nodeRef).equals(
        nodeService.getPrimaryParent(nodeRef).getParentRef()
      );

    String originalName = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NAME
    );
    String name =
      action == ClipboardAction.LINK.getValue()
        ? "Link_" + originalName
        : originalName;
    String translationPrefix = "";
    QName type = nodeService.getType(nodeRef);

    for (;;) {
      try {
        PasteContext ctx = new PasteContext(
          nodeRef,
          destRef,
          assocRef,
          name,
          originalName,
          translationPrefix,
          type,
          isPrimaryParent
        );
        executePasteAction(action, ctx, disableNotificationThreadLocal);
        break;
      } catch (FileExistsException fileExistsErr) {
        if (action == ClipboardAction.COPY.getValue()) {
          name = new StringBuilder("Copy_").append(name).toString();
          translationPrefix = new StringBuilder("Copy_ ")
            .append(translationPrefix)
            .toString();
        } else {
          throw fileExistsErr;
        }
      }
    }
  }

  private record PasteContext(
    NodeRef nodeRef,
    NodeRef destRef,
    ChildAssociationRef assocRef,
    String name,
    String originalName,
    String translationPrefix,
    QName type,
    boolean isPrimaryParent
  ) {}

  private void executePasteAction(
    int action,
    PasteContext ctx,
    DisableNotificationThreadLocal disableNotification
  ) throws FileNotFoundException {
    if (action == ClipboardAction.LINK.getValue()) {
      handleLinkAction(
        ctx.nodeRef(),
        ctx.destRef(),
        ctx.assocRef(),
        ctx.name(),
        ctx.type()
      );
    } else if (action == ClipboardAction.COPY.getValue()) {
      handleCopyAction(
        ctx.nodeRef(),
        ctx.destRef(),
        ctx.assocRef(),
        ctx.name(),
        ctx.originalName(),
        ctx.translationPrefix(),
        ctx.type()
      );
    } else if (action == ClipboardAction.MOVE.getValue()) {
      disableNotification.set(true);
      if (!ctx.destRef().equals(ctx.assocRef().getParentRef())) {
        handleMoveAction(
          ctx.nodeRef(),
          ctx.destRef(),
          ctx.assocRef(),
          ctx.name(),
          ctx.type(),
          ctx.isPrimaryParent()
        );
      }
    } else {
      throw new IllegalArgumentException(
        "Invalid action code, must be 0 = COPY, 1 = MOVE or 2 = LINK"
      );
    }
  }

  private ChildAssociationRef resolveAssocRef(NodeRef nodeRef) {
    if (getParent(nodeRef) == null) {
      return nodeService.getPrimaryParent(nodeRef);
    }
    NodeRef parentNodeRef = getParent(nodeRef);
    List<ChildAssociationRef> assocList = nodeService.getParentAssocs(nodeRef);
    if (assocList != null) {
      for (ChildAssociationRef entry : assocList) {
        if (parentNodeRef.equals(entry.getParentRef())) {
          return entry;
        }
      }
    }
    throw new IllegalStateException(
      "Can not find assocRef for: " + nodeRef.toString()
    );
  }

  private void handleLinkAction(
    NodeRef nodeRef,
    NodeRef destRef,
    ChildAssociationRef assocRef,
    String name,
    QName type
  ) {
    if (!notExists(name + LINK_NODE_EXTENSION, destRef)) {
      return;
    }
    String newName = name + LINK_NODE_EXTENSION;
    Map<QName, Serializable> props = new HashMap<>(2, 1.0f);
    props.put(ContentModel.PROP_NAME, newName);
    props.put(ContentModel.PROP_LINK_DESTINATION, nodeRef);

    if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT)) {
      ChildAssociationRef childRef = nodeService.createNode(
        destRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(assocRef.getQName().getNamespaceURI(), newName),
        ApplicationModel.TYPE_FILELINK,
        props
      );
      Map<QName, Serializable> titledProps = new HashMap<>(2, 1.0f);
      titledProps.put(ContentModel.PROP_TITLE, name);
      titledProps.put(ContentModel.PROP_DESCRIPTION, name);
      nodeService.addAspect(
        childRef.getChildRef(),
        ContentModel.ASPECT_TITLED,
        titledProps
      );
    } else {
      ChildAssociationRef childRef = nodeService.createNode(
        destRef,
        ContentModel.ASSOC_CONTAINS,
        assocRef.getQName(),
        ApplicationModel.TYPE_FOLDERLINK,
        props
      );
      Map<QName, Serializable> uiFacetsProps = new HashMap<>(4, 1.0f);
      uiFacetsProps.put(ApplicationModel.PROP_ICON, "space-icon-link");
      uiFacetsProps.put(ContentModel.PROP_TITLE, name);
      uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, name);
      nodeService.addAspect(
        childRef.getChildRef(),
        ApplicationModel.ASPECT_UIFACETS,
        uiFacetsProps
      );
    }
  }

  private void handleCopyAction(
    NodeRef nodeRef,
    NodeRef destRef,
    ChildAssociationRef assocRef,
    String name,
    String originalName,
    String translationPrefix,
    QName type
  ) throws FileNotFoundException {
    if (destRef.equals(assocRef.getParentRef()) && name.equals(originalName)) {
      throw new FileExistsException(destRef, name);
    }
    for (FileInfo file : fileFolderService.list(destRef)) {
      if (name.equals(file.getName())) {
        throw new FileExistsException(destRef, name);
      }
    }
    if (
      dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)
    ) {
      fileFolderService.copy(nodeRef, destRef, name);
    } else if (
      dictionaryService.isSubClass(
        type,
        ContentModel.TYPE_MULTILINGUAL_CONTAINER
      )
    ) {
      try {
        multilingualContentService.copyTranslationContainer(
          nodeRef,
          destRef,
          translationPrefix
        );
      } catch (FileNotFoundException | RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new IllegalStateException(
          "Failed to copy translation container",
          e
        );
      }
    } else if (notExists(name, destRef)) {
      copyService.copyAndRename(
        nodeRef,
        destRef,
        ContentModel.ASSOC_CONTAINS,
        assocRef.getQName(),
        true
      );
    }
  }

  private void handleMoveAction(
    NodeRef nodeRef,
    NodeRef destRef,
    ChildAssociationRef assocRef,
    String name,
    QName type,
    boolean isPrimaryParent
  )
    throws org.alfresco.service.cmr.model.FileNotFoundException, FileExistsException {
    if (
      dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) ||
      dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER)
    ) {
      fileFolderService.moveFrom(nodeRef, getParent(nodeRef), destRef, name);
    } else if (
      dictionaryService.isSubClass(
        type,
        ContentModel.TYPE_MULTILINGUAL_CONTAINER
      )
    ) {
      multilingualContentService.moveTranslationContainer(nodeRef, destRef);
    } else if (isPrimaryParent) {
      nodeService.moveNode(
        nodeRef,
        destRef,
        ContentModel.ASSOC_CONTAINS,
        assocRef.getQName()
      );
    } else {
      nodeService.removeChild(getParent(nodeRef), nodeRef);
      nodeService.addChild(
        destRef,
        nodeRef,
        assocRef.getTypeQName(),
        assocRef.getQName()
      );
    }
  }

  private boolean getNotificationStatus(NodeRef nodeRef, boolean pasteAll) {
    boolean result = false;
    final NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
    if (igNodeRef != null) {
      if (pasteAll) {
        result = notificationManagerService.isPasteAllNotificationEnabled(
          igNodeRef
        );
      } else {
        result = notificationManagerService.isPasteNotificationEnabled(
          igNodeRef
        );
      }
    }
    return result;
  }

  private NodeRef getParent(NodeRef nodeRef) {
    return nodeService.getPrimaryParent(nodeRef).getParentRef();
  }

  private boolean notExists(String name, NodeRef parent) {
    QueryParameterDefinition[] params = new QueryParameterDefinition[1];
    params[0] = new QueryParameterDefImpl(
      ContentModel.PROP_NAME,
      dictionaryService.getDataType(DataTypeDefinition.TEXT),
      true,
      name
    );

    // execute the query
    List<NodeRef> nodeRefs = searchService.selectNodes(
      parent,
      XPATH_QUERY_NODE_MATCH,
      params,
      namespaceService,
      false
    );

    return nodeRefs.isEmpty();
  }
}
