package io.swagger.api;

import io.swagger.model.CircabcServiceName;
import io.swagger.model.InterestGroup;
import io.swagger.model.Node;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.model.permissions.VisibilityPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link NodesApi} providing the business logic for
 * CIRCABC "node" operations.
 *
 * <p>A node represents any content item stored in the Alfresco repository
 * (documents, folders, topics, posts, links, etc.). This service is responsible
 * for turning low-level Alfresco {@link NodeRef} instances into the rich
 * {@link Node} domain model exposed by the REST layer. It resolves and
 * aggregates node metadata such as:
 *
 * <ul>
 *   <li>Core Alfresco properties (name, type, parent, title, description,
 *       multilingual title/description via {@link MLText}).</li>
 *   <li>The CIRCABC service the node belongs to (Library, Information, Events,
 *       Newsgroups) based on its aspects.</li>
 *   <li>Content specific attributes (size, mimetype, encoding, lock state,
 *       working-copy / check-out state, translations).</li>
 *   <li>Folder-link destination and origin Interest Group references.</li>
 *   <li>Effective permissions for the current user and their groups, guest
 *       access and notification status.</li>
 *   <li>Favourite status and whether a folder contains sub-folders.</li>
 * </ul>
 *
 * <p>It also offers utility operations for path resolution, ownership changes,
 * unique name generation, file-name extension handling and Interest Group
 * lookup for a given node.
 */
public class NodesApiImpl implements NodesApi {

  /** Property key used to expose a user's notification subscription state on a node. */
  public static final String NOTIFICATION_STATUS = "NotificationStatus";

  /** Value used to flag a granted (allowed) permission. */
  public static final String ALLOWED = "ALLOWED";

  /**
   * Permission names that represent an explicit "no access" grant across the
   * CIRCABC services. Used to decide whether the {@code guest} authority
   * actually grants any visibility on a node.
   */
  private final List<String> noAccessStrings = Arrays.asList(
    DirectoryPermissions.DIRNOACCESS.toString(),
    InformationPermissions.INFNOACCESS.toString(),
    LibraryPermissions.LIBNOACCESS.toString(),
    NewsGroupPermissions.NWSNOACCESS.toString(),
    VisibilityPermissions.NOVISIBILITY.toString()
  );

  /** QName of the {@code cm:destination} property that points to a folder-link target. */
  public static final QName PROP_DESTINATION = QName.createQName(
    NamespaceService.CONTENT_MODEL_1_0_URI,
    "destination"
  );

  /** Logger for this service. */
  static final Log logger = LogFactory.getLog(NodesApiImpl.class);

  @Autowired
  private FavouritesService favouritesService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  @Qualifier("NodeService") // NOSONAR
  private NodeService secureNodeService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private OwnableService ownableService;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private LockService lockService;

  @Autowired
  private MimetypeService mimetypeService;

  @Autowired
  MultilingualContentService multilingualContentService;

  @Autowired
  CheckOutCheckInService checkOutCheckInService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private SearchService searchService;

  /**
   * Reads the original (source) node reference stored on a node via the
   * {@code ci:migrated} aspect.
   *
   * @param id the node id
   * @return the original node reference, or {@code null} if the node is not migrated
   */
  @Override
  public String getOriginalNodeRef(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (!nodeService.exists(nodeRef)) {
      throw new InvalidNodeRefException(nodeRef);
    }
    Serializable v = nodeService.getProperty(
      nodeRef,
      CircabcModel.PROP_ORIGINAL_NODE_REF
    );
    return v == null ? null : v.toString();
  }

  /**
   * Sets (creates or updates) the original (source) node reference on a node,
   * applying the {@code ci:migrated} aspect.
   *
   * @param id the node id
   * @param originalNodeRef the original (source) node reference to store
   * @return the updated node
   */
  @Override
  public Node setOriginalNodeRef(String id, String originalNodeRef) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (!nodeService.exists(nodeRef)) {
      throw new InvalidNodeRefException(nodeRef);
    }
    Map<QName, Serializable> props = HashMap.newHashMap(1);
    props.put(CircabcModel.PROP_ORIGINAL_NODE_REF, originalNodeRef);
    // addAspect is a no-op if already present; setProperty ensures update on existing aspect
    nodeService.addAspect(nodeRef, CircabcModel.ASPECT_MIGRATED, props);
    nodeService.setProperty(
      nodeRef,
      CircabcModel.PROP_ORIGINAL_NODE_REF,
      originalNodeRef
    );
    return getNode(nodeRef);
  }

  /**
   * Removes the {@code ci:migrated} aspect (and its {@code ci:originalNodeRef}
   * property) from a node.
   *
   * @param id the node id
   */
  @Override
  public void deleteOriginalNodeRef(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (!nodeService.exists(nodeRef)) {
      throw new InvalidNodeRefException(nodeRef);
    }
    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_MIGRATED)) {
      nodeService.removeAspect(nodeRef, CircabcModel.ASPECT_MIGRATED);
    }
  }

  /**
   * Resolves the migrated node whose {@code ci:originalNodeRef} matches the given
   * original node id (a {@code workspace://SpacesStore} uuid). The search runs as
   * system so that a missing node (404) can be distinguished from one the current
   * user may not read (403).
   *
   * @param originalId the original (source) node uuid
   * @return the migrated node, or {@code null} if no node references the original id
   * @throws AccessDeniedException if a node is found but the current user has no READ permission
   */
  @Override
  public Node resolveByOriginalNodeRef(String originalId) {
    final String originalRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      originalId
    ).toString();
    // Find permission-agnostically (as system) so we can distinguish 404 from 403.
    NodeRef found = AuthenticationUtil.runAs(
      (AuthenticationUtil.RunAsWork<NodeRef>) () -> {
        SearchParameters sp = new SearchParameters();
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("@ci\\:originalNodeRef:\"" + originalRef + "\"");
        ResultSet rs = null;
        try {
          rs = searchService.query(sp);
          for (NodeRef r : rs.getNodeRefs()) {
            if (nodeService.exists(r)) {
              return r;
            }
          }
        } finally {
          if (rs != null) {
            rs.close();
          }
        }
        return null;
      },
      AuthenticationUtil.getSystemUserName()
    );
    if (found == null) {
      return null; // -> 404
    }
    if (
      !permissionService
        .hasPermission(found, PermissionService.READ)
        .equals(AccessStatus.ALLOWED)
    ) {
      throw new AccessDeniedException(
        "Not enough rights to access the migrated node"
      );
    }
    return getNode(found);
  }

  /**
   * Builds a fully populated {@link Node} model for the given repository node.
   *
   * @param nodeRef reference to the Alfresco node to describe
   * @return a new {@link Node} populated with the node's metadata, properties,
   *     permissions and status flags
   */
  @Override
  public Node getNode(NodeRef nodeRef) {
    return getNode(nodeRef, new Node());
  }

  /**
   * Populates the supplied {@link Node} model with the metadata of the given
   * repository node.
   *
   * <p>This resolves the node's identity, type and parent, determines the
   * owning CIRCABC service from its aspects, gathers its properties (including
   * content, folder-link and multilingual attributes), computes the effective
   * permissions and guest access, and sets favourite and sub-folder status.
   *
   * @param nodeRef reference to the Alfresco node to describe
   * @param node the {@link Node} instance to populate and return
   * @return the supplied {@code node}, fully populated
   */
  @Override
  public Node getNode(NodeRef nodeRef, Node node) {
    node.setId(nodeRef.getId());
    node.setName(
      (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
    );
    node.setType(nodeService.getType(nodeRef).toString());

    // original node ref: present on nodes created by the migration/import process
    // (ci:migrated aspect). Exposed as a dedicated field so the UI can resolve/redirect
    // old (source) deep links to the migrated node.
    final Serializable originalRef = secureNodeService.getProperty(
      nodeRef,
      CircabcModel.PROP_ORIGINAL_NODE_REF
    );
    if (originalRef != null) {
      node.setOriginalNodeRef(originalRef.toString());
    }

    boolean isLibRootAdmin = false;
    boolean isNewsRootAdmin = false;
    boolean hasGuestAccess = false;
    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)) {
      node.setService(CircabcServiceName.INFORMATION);
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      node.setService(CircabcServiceName.LIBRARY);
      isLibRootAdmin = hasServiceAdminPermission(
        apiToolBox.getCurrentLibraryRoot(nodeRef),
        "LibAdmin"
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {
      node.setService(CircabcServiceName.EVENTS);
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {
      node.setService(CircabcServiceName.NEWSGROUPS);
      isNewsRootAdmin = hasServiceAdminPermission(
        apiToolBox.getCurrentNewsgroupRoot(nodeRef),
        "NwsAdmin"
      );
    }

    node.setParentId(
      nodeService.getPrimaryParent(nodeRef).getParentRef().getId()
    );

    final Map<String, String> properties = populateProperties(nodeRef, node);

    populateContentProperties(nodeRef, properties);
    populateFolderLinkProperties(nodeRef, properties);

    if (nodeService.hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)) {
      properties.put("isUrl", "true");
    }

    node.setProperties(properties);

    hasGuestAccess = populatePermissions(nodeRef, node, hasGuestAccess);
    node.setHasGuestAccess(hasGuestAccess);

    if (isLibRootAdmin) {
      node.getPermissions().put("LibAdmin", ALLOWED);
    } else if (isNewsRootAdmin) {
      node.getPermissions().put("NwsAdmin", ALLOWED);
    }

    populateFavouriteStatus(nodeRef, node);
    populateSubFolderStatus(nodeRef, node);

    return node;
  }

  /**
   * Determines whether the current user holds the given service-level admin
   * permission on a service root node.
   *
   * @param serviceRoot the root node of the service (library or newsgroup);
   *     may be {@code null}
   * @param permission the permission name to check (e.g. {@code "LibAdmin"})
   * @return {@code true} if {@code serviceRoot} is non-null and the permission
   *     is granted, {@code false} otherwise
   */
  private boolean hasServiceAdminPermission(
    NodeRef serviceRoot,
    String permission
  ) {
    return (
      serviceRoot != null &&
      permissionService
        .hasPermission(serviceRoot, permission)
        .equals(AccessStatus.ALLOWED)
    );
  }

  /**
   * Extracts the node's Alfresco properties into a string-keyed map, converting
   * dates to strings and diverting the multilingual title and description onto
   * the {@link Node} model. Also adds owner, version and original-container
   * information.
   *
   * @param nodeRef reference to the node whose properties are read
   * @param node the {@link Node} model that receives the title and description
   * @return a map of local property names to their string values
   */
  private Map<String, String> populateProperties(NodeRef nodeRef, Node node) {
    final Map<String, String> properties = new HashMap<>();
    boolean isMLAware = MLPropertyInterceptor.isMLAware();
    MLPropertyInterceptor.setMLAware(true);
    for (Entry<QName, Serializable> property : nodeService
      .getProperties(nodeRef)
      .entrySet()) {
      if (property == null) {
        continue;
      }
      if (property.getValue() == null) {
        properties.put(property.getKey().getLocalName(), "");
      } else {
        Object value = "";
        if (property.getValue().getClass().equals(Date.class)) {
          value = Converter.convertDateToString((Date) property.getValue());
        } else if (property.getKey().equals(ContentModel.PROP_TITLE)) {
          node.setTitle(
            Converter.convertMlToI18nProperty(
              fixMLTextProperty(property.getValue())
            )
          );
        } else if (property.getKey().equals(ContentModel.PROP_DESCRIPTION)) {
          node.setDescription(
            Converter.convertMlToI18nProperty(
              fixMLTextProperty(property.getValue())
            )
          );
        } else {
          value = property.getValue();
        }
        properties.put(property.getKey().getLocalName(), value.toString());
      }
    }
    MLPropertyInterceptor.setMLAware(isMLAware);

    if (this.ownableService.hasOwner(nodeRef)) {
      properties.put("owner", this.ownableService.getOwner(nodeRef));
    }
    boolean isVersion = nodeRef
      .getStoreRef()
      .getIdentifier()
      .startsWith("version");
    properties.put("isVersion", isVersion ? "true" : "false");
    properties.put(
      "originalContainerId",
      nodeService.getPrimaryParent(nodeRef).getParentRef().getId()
    );
    return properties;
  }

  /**
   * Adds content-specific attributes to the properties map when the node is of
   * type {@code cm:content}, including size, mimetype, encoding, multilingual
   * and translation info, lock and working-copy state, the original/working
   * copy node ids and whether the current user currently has access.
   *
   * @param nodeRef reference to the node being described
   * @param properties the properties map to enrich (modified in place)
   */
  private void populateContentProperties(
    NodeRef nodeRef,
    Map<String, String> properties
  ) {
    if (!nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
      return;
    }
    ContentData cData = (ContentData) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_CONTENT
    );
    if (cData != null) {
      properties.put("size", Long.toString(cData.getSize()));
      properties.put("mimetype", cData.getMimetype());
      properties.put(
        "mimetypeName",
        mimetypeService.getDisplaysByMimetype().get(cData.getMimetype())
      );
      properties.put("encoding", cData.getEncoding());
    }

    boolean isMultilingual = nodeService.hasAspect(
      nodeRef,
      ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
    );
    properties.put("multilingual", String.valueOf(isMultilingual));
    if (isMultilingual) {
      properties.put(
        "translations",
        String.valueOf(
          multilingualContentService.getTranslations(nodeRef).size()
        )
      );
    } else {
      properties.put("translations", "0");
    }

    boolean isLocked = lockService.getLockType(nodeRef) != null;
    properties.put("locked", String.valueOf(isLocked));
    boolean isWorkingCopy = nodeService.hasAspect(
      nodeRef,
      ContentModel.ASPECT_WORKING_COPY
    );
    properties.put("workingCopy", String.valueOf(isWorkingCopy));

    if (
      isWorkingCopy &&
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())
    ) {
      NodeRef originalNodeRef = checkOutCheckInService.getCheckedOut(nodeRef);
      properties.put(
        "originalNodeId",
        originalNodeRef != null ? originalNodeRef.getId() : ""
      );
      if (originalNodeRef == null) {
        logger.warn(
          "Working copy aspect present but original node not found for: " +
            nodeRef.toString()
        );
      }
    }

    properties.put(
      "currentUserHasAccess",
      String.valueOf(checkCurrentUserAccess(nodeRef, isWorkingCopy))
    );

    if (isLocked) {
      NodeRef workingCopyNodeRef = checkOutCheckInService.getWorkingCopy(
        nodeRef
      );
      if (workingCopyNodeRef != null) {
        properties.put("workingCopyId", workingCopyNodeRef.getId());
      }
    }
  }

  /**
   * Checks whether the current user can act on the node given its lock and
   * working-copy state.
   *
   * @param nodeRef reference to the node to check
   * @param isWorkingCopy {@code true} if the node is a working copy
   * @return {@code true} if the node is not locked against the current user
   *     and, for working copies, the current user is the working-copy owner
   */
  private boolean checkCurrentUserAccess(
    NodeRef nodeRef,
    boolean isWorkingCopy
  ) {
    boolean hasAccess = true;
    try {
      lockService.checkForLock(nodeRef);
    } catch (NodeLockedException e) {
      hasAccess = false;
    }
    if (isWorkingCopy && hasAccess) {
      String workingCopyOwner = (String) nodeService.getProperty(
        nodeRef,
        ContentModel.PROP_WORKING_COPY_OWNER
      );
      hasAccess = workingCopyOwner.equals(AuthenticationUtil.getRunAsUser());
    }
    return hasAccess;
  }

  /**
   * For folder-link nodes ({@code app:folderlink}), resolves and records the
   * destination node and its Interest Group as well as the origin Interest
   * Group into the properties map.
   *
   * @param nodeRef reference to the folder-link node
   * @param properties the properties map to enrich (modified in place)
   */
  private void populateFolderLinkProperties(
    NodeRef nodeRef,
    Map<String, String> properties
  ) {
    if (
      !nodeService.getType(nodeRef).equals(ApplicationModel.TYPE_FOLDERLINK)
    ) {
      return;
    }
    NodeRef destination = (NodeRef) nodeService.getProperty(
      nodeRef,
      PROP_DESTINATION
    );
    if (destination != null) {
      NodeRef destinationIgNodeRef = apiToolBox.getCurrentInterestGroup(
        destination
      );
      if (destinationIgNodeRef != null) {
        properties.put("destinationIgId", destinationIgNodeRef.getId());
        properties.put("destinationId", destination.getId());
      }
    }
    NodeRef originIgNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
    if (originIgNodeRef != null) {
      properties.put("originIgId", originIgNodeRef.getId());
    }
  }

  /**
   * Computes the effective permissions applicable to the current user (from
   * direct user grants and from the groups they belong to), extracts the
   * notification status, and detects guest access.
   *
   * @param nodeRef reference to the node whose permissions are read
   * @param node the {@link Node} model that receives the permission map and
   *     notification status
   * @param hasGuestAccess the current guest-access flag to start from
   * @return {@code true} if guest access was detected on the node, otherwise
   *     the incoming {@code hasGuestAccess} value
   */
  private boolean populatePermissions(
    NodeRef nodeRef,
    Node node,
    boolean hasGuestAccess
  ) {
    Map<String, String> permissions = new HashMap<>();
    String userName = AuthenticationUtil.getRunAsUser();
    Set<String> authorities = authorityService.getAuthorities();

    for (org.alfresco.service.cmr.security.AccessPermission ac : permissionService.getAllSetPermissions(
      nodeRef
    )) {
      String permission = ac.getPermission();
      if (
        !hasGuestAccess &&
        ac.getAuthority().equals("guest") &&
        !noAccessStrings.contains(permission)
      ) {
        hasGuestAccess = true;
      }
      if (
        ac.getAuthorityType() == AuthorityType.USER &&
        ac.getAuthority().equals(userName)
      ) {
        addUserPermission(permissions, permission, ac);
      } else if (
        ac.getAuthorityType() == AuthorityType.GROUP &&
        authorities.contains(ac.getAuthority())
      ) {
        permissions.put(permission, ac.getAccessStatus().name());
      }
    }

    if (permissions.containsKey(NOTIFICATION_STATUS)) {
      node.setNotifications(permissions.get(NOTIFICATION_STATUS));
      permissions.remove(NOTIFICATION_STATUS);
    } else {
      node.setNotifications("inherit");
    }
    node.setPermissions(permissions);
    return hasGuestAccess;
  }

  /**
   * Adds a permission granted directly to the current user to the permissions
   * map, ensuring an existing {@code DENIED} notification status is not
   * overwritten by an {@code ALLOWED} one.
   *
   * @param permissions the permissions map to update (modified in place)
   * @param permission the permission name being added
   * @param ac the access-permission entry describing the grant
   */
  private void addUserPermission(
    Map<String, String> permissions,
    String permission,
    org.alfresco.service.cmr.security.AccessPermission ac
  ) {
    if (
      permission.equals(NOTIFICATION_STATUS) &&
      ac.getAccessStatus().name().equals(ALLOWED)
    ) {
      if (!"DENIED".equals(permissions.get(NOTIFICATION_STATUS))) {
        permissions.put(permission, ac.getAccessStatus().name());
      }
    } else {
      permissions.put(permission, ac.getAccessStatus().name());
    }
  }

  /**
   * Sets the favourite flag on the node for the current user. Only content and
   * folder nodes that are not archived are considered; the system user is
   * always treated as non-favourite. Any error while resolving the status is
   * logged and results in a {@code false} flag.
   *
   * @param nodeRef reference to the node to check
   * @param node the {@link Node} model whose favourite flag is set
   */
  private void populateFavouriteStatus(NodeRef nodeRef, Node node) {
    String userName = AuthenticationUtil.getRunAsUser();
    QName nodeType = nodeService.getType(nodeRef);
    if (
      (nodeType.equals(ContentModel.TYPE_CONTENT) ||
        nodeType.equals(ContentModel.TYPE_FOLDER)) &&
      !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED) &&
      !AuthenticationUtil.getSystemUserName().equals(userName)
    ) {
      try {
        node.setFavourite(favouritesService.isFavourite(userName, nodeRef));
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Error while getting favourite status for node " +
              nodeRef.toString(),
            e
          );
        }
        node.setFavourite(false);
      }
    } else {
      node.setFavourite(false);
    }
  }

  /**
   * Sets whether the node has sub-folders. For folders, sub-folder children are
   * counted (excluding version-store nodes); for forums, child forums are
   * counted.
   *
   * @param nodeRef reference to the node to inspect
   * @param node the {@link Node} model whose sub-folder flag is set
   */
  private void populateSubFolderStatus(NodeRef nodeRef, Node node) {
    QName nodeType = nodeService.getType(nodeRef);
    if (nodeType.equals(ContentModel.TYPE_FOLDER)) {
      node.setHasSubFolders(
        !nodeRef.toString().startsWith("versionStore") &&
          !nodeService
            .getChildAssocs(nodeRef, Set.of(ContentModel.TYPE_FOLDER))
            .isEmpty()
      );
    } else if (nodeType.equals(ForumModel.TYPE_FORUM)) {
      node.setHasSubFolders(
        !nodeService
          .getChildAssocs(nodeRef, Set.of(ForumModel.TYPE_FORUM))
          .isEmpty()
      );
    }
  }

  /**
   * Normalises a multilingual property into a locale-keyed string map. Handles
   * both {@link MLText} values (one entry per locale) and plain {@link String}
   * values (mapped under the {@code "en"} key).
   *
   * @param property the raw property value, expected to be {@link MLText} or
   *     {@link String}
   * @return a map of locale strings to their text values (never {@code null})
   */
  private Map<String, String> fixMLTextProperty(Serializable property) {
    Map<String, String> ml = new HashMap<>();
    if (property instanceof MLText mlText) {
      for (Entry<Locale, String> item : mlText.entrySet()) {
        if (item.getValue() == null) {
          ml.put(item.getKey().toString(), "");
        } else {
          ml.put(item.getKey().toString(), item.getValue());
        }
      }
    } else if (property instanceof String) {
      ml.put("en", property.toString());
    }
    return ml;
  }

  /**
   * Resolves a node from its string identifier and returns its full
   * {@link Node} model.
   *
   * @param id the node identifier
   * @return the populated {@link Node} for the given id
   */
  @Override
  public Node getNodeById(String id) {
    Node result;
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    result = getNode(nodeRef);
    return result;
  }

  /**
   * Builds the ancestor path for a node within its CIRCABC service, from the
   * service root down to the node itself.
   *
   * <p>Only nodes belonging to the Library, Information or Newsgroup services
   * are considered. Walking upwards from the node, ancestors are included as
   * long as the current user has {@code Read} permission, stopping at the
   * Interest Group root. The resulting list is ordered from the top-most
   * ancestor to the node.
   *
   * @param id the identifier of the node whose path is requested
   * @return the ordered list of {@link Node} elements forming the path (may be
   *     empty if the node is not part of a supported service)
   */
  @Override
  public List<Node> getPathByNode(String id) {
    List<Node> result = new ArrayList<>();
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (
      secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY) ||
      secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION) ||
      secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ) {
      Path path = secureNodeService.getPath(nodeRef);

      int cursor = path.size() - 1;
      boolean stop = false;

      while (!stop) {
        Element element = path.get(cursor);

        if (element instanceof Path.ChildAssocElement childAssocElement) {
          NodeRef elementRef = childAssocElement.getRef().getChildRef();

          if (
            secureNodeService.hasAspect(
              elementRef,
              CircabcModel.ASPECT_IGROOT
            ) ||
            cursor == 0
          ) {
            stop = true;
          } else if (
            permissionService
              .hasPermission(elementRef, PermissionService.READ)
              .equals(AccessStatus.ALLOWED)
          ) {
            Node node = getNode(elementRef);
            setI18nProperties(node, elementRef);
            result.add(node);

            cursor--;
          }
        }
      }
    }

    Collections.reverse(result);

    return result;
  }

  /**
   * Copies the multilingual title and description of a repository node onto the
   * given {@link Node} model as i18n properties.
   *
   * @param node the {@link Node} model to update
   * @param tmpNodeRef reference to the source repository node
   */
  private void setI18nProperties(Node node, NodeRef tmpNodeRef) {
    Serializable serializable = secureNodeService.getProperty(
      tmpNodeRef,
      ContentModel.PROP_TITLE
    );

    if (serializable != null) {
      Map<String, String> ml = fixMLTextProperty(serializable);
      node.setTitle(Converter.convertMlToI18nProperty(ml));
    }

    serializable = secureNodeService.getProperty(
      tmpNodeRef,
      ContentModel.PROP_DESCRIPTION
    );

    if (serializable != null) {
      Map<String, String> ml = fixMLTextProperty(serializable);
      node.setDescription(Converter.convertMlToI18nProperty(ml));
    }
  }

  /**
   * Makes the current user the owner of the given node and returns the updated
   * node model.
   *
   * @param id the identifier of the node to take ownership of
   * @return the refreshed {@link Node} model after ownership change
   */
  @Override
  public Node nodesIdOwnershipPut(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    ownableService.takeOwnership(nodeRef);
    return this.getNode(nodeRef);
  }

  /**
   * Generates a name that is unique among the children of the given parent
   * folder. If the candidate name already exists, a numeric suffix
   * ({@code " (n)"}) is appended before the extension until an unused name is
   * found.
   *
   * @param parent the parent folder whose children are checked
   * @param candidateName the desired name
   * @return {@code candidateName} if free, otherwise a suffixed variant that
   *     does not collide with an existing child
   */
  public String generateUniqueName(
    final NodeRef parent,
    final String candidateName
  ) {
    //
    if (
      secureNodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        candidateName
      ) ==
      null
    ) {
      return candidateName;
    } else {
      final String extension = getFileNameExtension(candidateName);
      final String name = removeFileNameExtension(candidateName);
      String uniqueName;
      int tries = 0;

      do {
        uniqueName =
          name +
          " (" +
          (++tries) +
          ")" +
          ((extension == null) ? "" : "." + extension);
      } while (
        secureNodeService.getChildByName(
          parent,
          ContentModel.ASSOC_CONTAINS,
          uniqueName
        ) !=
        null
      );

      return uniqueName;
    }
  }

  /**
   * Returns the (lower-cased) file extension of a file name.
   *
   * @param fileName the file name to inspect
   * @return the extension without the leading dot, or {@code null} if the name
   *     has no extension
   */
  @Override
  public String getFileNameExtension(final String fileName) {
    int extIndex = fileName.lastIndexOf('.');
    if (extIndex != -1) {
      return fileName.substring(extIndex + 1).toLowerCase();
    } else {
      return null;
    }
  }

  /**
   * Returns the file name with its extension removed and lower-cased.
   *
   * @param fileName the file name to process
   * @return the name without its extension, lower-cased; the original name is
   *     returned (unchanged) if it has no extension
   */
  @Override
  public String removeFileNameExtension(final String fileName) {
    int extIndex = fileName.lastIndexOf('.');
    if (extIndex != -1) {
      return fileName.substring(0, extIndex).toLowerCase();
    } else {
      return fileName;
    }
  }

  /**
   * Resolves the Interest Group that a given node belongs to. The lookup runs
   * as the system user so the containing Interest Group can be found regardless
   * of the caller's permissions on intermediate nodes.
   *
   * @param id the identifier of the node
   * @return the {@link InterestGroup} the node belongs to
   * @throws InvalidArgumentException if the node id is invalid or the Interest
   *     Group cannot be resolved
   */
  @Override
  public InterestGroup nodesIdGroupGet(String id) {
    try {
      AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
      NodeRef nodeRef = Converter.createNodeRefFromId(id);
      NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
      return groupsApi.getInterestGroup(igNodeRef.getId());
    } catch (Exception e) {
      logger.error("Error in nodesIdGroupGet", e);

      throw new InvalidArgumentException("invalid bode id " + id);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }
}
