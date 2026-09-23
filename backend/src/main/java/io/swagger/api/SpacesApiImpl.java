package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.LinksBusinessSrv;
import eu.europa.ec.digit.circabc.rest.service.ShareSpaceService;
import eu.europa.ec.digit.circabc.rest.service.helper.MetadataManager;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import io.swagger.config.CircabcConfig;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.PagedShares;
import io.swagger.model.Share;
import io.swagger.model.ShareIGsAndPermissions;
import io.swagger.model.ShareSpaceItem;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.SharedSpaceModel;
import io.swagger.model.db.InterestGroupLinkItem;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.RestInputSanitizer;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.ExecuteAllRulesActionExecuter;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Business logic implementation of the {@link SpacesApi} contract.
 *
 * <p>A "space" in CIRCABC is an Alfresco folder that lives within the Library or
 * Information service of an Interest Group. This class provides the operations
 * behind the corresponding REST endpoints, including:
 *
 * <ul>
 *   <li>Listing the children of a space (plain and paginated, with sorting and
 *       expired/folder/file filtering);</li>
 *   <li>Creating, renaming/updating and deleting sub-spaces (folders);</li>
 *   <li>Creating URL (link) content nodes inside a library space;</li>
 *   <li>Managing "shared spaces": inviting Interest Groups to a space, changing
 *       or removing their permissions, notifying library leaders by e-mail and
 *       creating cross-Interest-Group shared-space links;</li>
 *   <li>Computing the recursive size of a folder.</li>
 * </ul>
 *
 * <p>Collaborating Alfresco and CIRCABC services are wired in through Spring
 * {@code @Autowired} fields. Multilingual (ML) translation documents that are
 * not the pivot translation are filtered out of listing results.
 *
 * @author beaurpi
 */
public class SpacesApiImpl implements SpacesApi {

  //	 File prefix to use for link nodes
  private static final String LINK_TO_PREFIX = "Link to ";
  //	 File extension to use for link nodes
  private static final String LINK_NODE_EXTENSION = ".url";

  private static final String EXPIRATION_DATE = "expiration_date";

  private static final String PATTERN_HTML_EXTENSION = ".*\\.htm(l)?";

  private static final String HTML = ".html";

  private static final String FIRSTNAME_REGEX = "<USER_FIRST_NAME>";
  private static final String LASTTNAME_REGEX = "<USER_LAST_NAME>";
  private static final String KEY_PROFILE = "profile";
  private static final String KEY_YOUR_IG = "yourinterestGroup";

  /**
   * A logger for the class
   */
  private static final Log logger = LogFactory.getLog(SpacesApiImpl.class);

  /** Permission-aware node service: enforces the caller's access rights. */
  @Autowired
  @Qualifier("NodeService") // NOSONAR
  private NodeService secureNodeService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private LinksBusinessSrv linksBusinessSrv;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private RuleService ruleService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private PersonService personService;

  @Autowired
  private EmailApi emailApi;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  private ActionService actionService;

  @Autowired
  private DictionaryService dictionaryService;

  /** Raw node service that bypasses permission checks (system-level access). */
  @Autowired
  @Qualifier("nodeService")
  private NodeService unsecureNodeService;

  /** Permission-aware search service used to query spaces the caller may see. */
  @Autowired
  @Qualifier("SearchService") // NOSONAR
  private SearchService secureSearchService;

  @Autowired
  private ShareSpaceService shareSpaceService;

  @Autowired
  private MetadataManager metadataManager;

  @Autowired
  private org.alfresco.service.cmr.ml.MultilingualContentService multilingualContentService;

  /**
   * Checks if the given node is a multilingual translation (non-pivot).
   * Returns true if the node should be filtered out (hidden from listing).
   *
   * @param nodeRef The node reference to check
   * @param nodeService The node service to use for aspect checking
   * @return true if this is a translation document (not pivot), false otherwise
   */
  private boolean isNonPivotTranslation(
    NodeRef nodeRef,
    NodeService nodeService
  ) {
    if (
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ) {
      try {
        NodeRef pivotRef = multilingualContentService.getPivotTranslation(
          nodeRef
        );
        return !pivotRef.equals(nodeRef);
      } catch (Exception e) {
        // If we can't determine the pivot, don't filter
        logger.warn("Failed to get pivot translation for " + nodeRef, e);
        return false;
      }
    }
    return false;
  }

  /**
   * Lists the direct children of a Library or Information space.
   *
   * <p>Uses {@link FileFolderService} so that multilingual documents are
   * filtered by Alfresco; non-pivot translation documents are additionally
   * skipped. If the given node is neither a Library nor an Information space, an
   * empty list is returned.
   *
   * @param id the store-qualified id of the parent space
   * @param folderOnly when {@code true}, only sub-folders are returned;
   *     otherwise both folders and files are returned
   * @return the list of child {@link Node}s (never {@code null})
   */
  @Override
  public List<Node> spaceGetChildren(String id, boolean folderOnly) {
    List<Node> result = new ArrayList<>();
    NodeRef spaceNodeRef = Converter.createNodeRefFromId(id);
    if (
      secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_LIBRARY) ||
      secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_INFORMATION)
    ) {
      // use FileFolderService because it filter ML documents

      List<FileInfo> list;

      if (folderOnly) {
        list = fileFolderService.listFolders(spaceNodeRef);
      } else {
        list = fileFolderService.list(spaceNodeRef);
      }

      for (FileInfo item : list) {
        final NodeRef childRef = item.getNodeRef();

        // Skip multilingual translation documents (non-pivot)
        if (isNonPivotTranslation(childRef, secureNodeService)) {
          continue;
        }

        result.add(nodesApi.getNode(childRef));
      }
    }
    return result;
  }

  /**
   * Creates a new sub-space (folder) under the given parent folder.
   *
   * <p>Sets the name, (multilingual) title and description from the request
   * body. If the body carries an {@code expiration_date} property, the
   * {@code bproperties} aspect is applied and the expiration date is stored; an
   * unparseable date is logged and ignored.
   *
   * @param id the store-qualified id of the parent folder
   * @param body the {@link Node} describing the space to create (name, title,
   *     description and optional properties)
   * @return the newly created space as a {@link Node}
   * @throws InvalidTypeException if the parent node is not a {@code cm:folder}
   */
  @Override
  public Node spacesIdSpacesPost(String id, Node body) {
    NodeRef parentRef = Converter.createNodeRefFromId(id);

    if (
      !secureNodeService.getType(parentRef).equals(ContentModel.TYPE_FOLDER)
    ) {
      throw new InvalidTypeException(
        "Node creation failed, the parent noderef does not have the type folder",
        ContentModel.TYPE_FOLDER
      );
    }

    QName nameQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      QName.createValidLocalName(body.getName().trim())
    );

    ChildAssociationRef newSpace = secureNodeService.createNode(
      parentRef,
      ContentModel.ASSOC_CONTAINS,
      nameQName,
      ContentModel.TYPE_FOLDER
    );
    NodeRef nodeRef = newSpace.getChildRef();
    secureNodeService.setProperty(
      nodeRef,
      ContentModel.PROP_NAME,
      body.getName().trim()
    );
    MLText titles = Converter.toMLText(body.getTitle());
    secureNodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);

    MLText descriptions = Converter.toMLText(
      RestInputSanitizer.sanitizeRichText(body.getDescription())
    );
    secureNodeService.setProperty(
      nodeRef,
      ContentModel.PROP_DESCRIPTION,
      descriptions
    );

    if (body.getProperties().containsKey(EXPIRATION_DATE)) {
      String expirationDate = body.getProperties().get(EXPIRATION_DATE);
      if (
        expirationDate != null &&
        !expirationDate.isEmpty() &&
        !expirationDate.equals("null")
      ) {
        try {
          secureNodeService.addAspect(
            nodeRef,
            DocumentModel.ASPECT_BPROPERTIES,
            null
          );
          secureNodeService.setProperty(
            nodeRef,
            DocumentModel.PROP_EXPIRATION_DATE,
            Converter.convertStringToDate(expirationDate)
          );
        } catch (ParseException e) {
          logger.error("Invalid expiration date: " + expirationDate, e);
        }
      }
    }

    return nodesApi.getNode(nodeRef);
  }

  /**
   * Deletes a space (folder or folder-link).
   *
   * <p>Before deletion, the id of the owning Interest Group root is recorded on
   * the node (archived-IG-root property) so the deleted content can be traced
   * back to its Interest Group.
   *
   * @param id the store-qualified id of the space to delete
   * @throws InvalidTypeException if the node is neither a {@code cm:folder} nor
   *     an {@code app:folderlink}
   */
  @Override
  public void spaceDelete(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    if (
      !secureNodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER) &&
      !secureNodeService
        .getType(nodeRef)
        .equals(ApplicationModel.TYPE_FOLDERLINK)
    ) {
      throw new InvalidTypeException(
        "Node deletion failed, noderef does not have the type folder",
        ContentModel.TYPE_FOLDER
      );
    }

    NodeRef igRoot = apiToolBox.getCurrentInterestGroup(nodeRef);
    secureNodeService.setProperty(
      nodeRef,
      CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED,
      igRoot.getId()
    );

    secureNodeService.deleteNode(nodeRef);
  }

  /**
   * Lists the children of a Library or Information space with pagination,
   * sorting and optional filtering.
   *
   * <p>Results are de-duplicated by node reference, non-pivot multilingual
   * translations are skipped and, when requested, expired items are omitted. If
   * the node is neither a Library nor an Information space, an empty
   * {@link PagedNodes} is returned.
   *
   * @param id the store-qualified id of the parent space
   * @param page zero-based page index (a page window is applied only when
   *     {@code limit > 0} and {@code page >= 0})
   * @param limit maximum number of items per page ({@code 0} or negative means
   *     no paging)
   * @param sort sort descriptor (property local name optionally suffixed with
   *     {@code _ASC}/{@code _DESC}); may be empty for default ordering
   * @param folderOnly when {@code true}, restricts results to folders
   * @param fileOnly when {@code true}, restricts results to files
   * @param skipExpiredItems when {@code true}, items past their expiration date
   *     are excluded
   * @return the matching children together with the total count
   */
  @Override
  @SuppressWarnings("java:S3776")
  public PagedNodes spaceGetChildren(
    String id,
    int page,
    int limit,
    String sort,
    boolean folderOnly,
    boolean fileOnly,
    boolean skipExpiredItems
  ) {
    PagedNodes pagedResult = new PagedNodes();

    List<Node> result = new ArrayList<>();
    NodeRef spaceNodeRef = Converter.createNodeRefFromId(id);
    if (
      secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_LIBRARY) ||
      secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_INFORMATION)
    ) {
      List<Pair<QName, Boolean>> sortProps = buildSortProps(sort);

      FileFilterMode.setClient(Client.cmis);
      final PagingResults<FileInfo> list = fileFolderService.list(
        spaceNodeRef,
        fileOnly,
        folderOnly,
        null,
        sortProps,
        new PagingRequest(0, 100000)
      );
      FileFilterMode.clearClient();
      List<FileInfo> items = deduplicateByNodeRef(list.getPage());

      int start = 0;
      int end = items.size();

      if (limit > 0 && page >= 0) {
        start = page * limit;
        end = (Math.min(start + limit, items.size()));
      }

      for (int i = start; i < end; i++) {
        final NodeRef childRef = items.get(i).getNodeRef();

        if (isNonPivotTranslation(childRef, secureNodeService)) {
          continue;
        }

        if (!skipExpiredItems || !isExpired(items.get(i))) {
          result.add(nodesApi.getNode(childRef));
        }
      }

      pagedResult.setTotal((long) items.size());
      pagedResult.setData(result);
    }

    return pagedResult;
  }

  /**
   * Builds the sort properties for a folder listing.
   *
   * <p>Folders are always ordered before files. When a sort descriptor is
   * provided, the corresponding property is appended; CIRCABC-specific
   * properties ({@code security_ranking}, {@code expiration_date},
   * {@code status}) are resolved against the CIRCABC document model namespace,
   * all others against the default content model namespace.
   *
   * @param sort the sort descriptor (property local name optionally suffixed
   *     with {@code _ASC}/{@code _DESC}); may be empty
   * @return the list of (property, ascending) pairs to sort by
   */
  private List<Pair<QName, Boolean>> buildSortProps(String sort) {
    List<Pair<QName, Boolean>> sortProps = new ArrayList<>(2);
    sortProps.add(
      new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false)
    );

    if (!"".contentEquals(sort)) {
      String localName = sort.replace("_DESC", "").replace("_ASC", "");
      String namespace = NamespaceService.CONTENT_MODEL_1_0_URI;
      if (
        localName.equals("security_ranking") ||
        localName.equals(EXPIRATION_DATE) ||
        localName.equals("status")
      ) {
        namespace = DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI;
      }
      sortProps.add(
        new Pair<>(
          QName.createQName(namespace, localName),
          sort.endsWith("ASC")
        )
      );
    }

    return sortProps;
  }

  /**
   * Removes duplicate entries that share the same node reference, preserving the
   * original order of first occurrence.
   *
   * @param items the file infos to de-duplicate
   * @return a new list containing each node reference at most once
   */
  private List<FileInfo> deduplicateByNodeRef(List<FileInfo> items) {
    Set<NodeRef> seen = new HashSet<>();
    List<FileInfo> result = new ArrayList<>(items.size());
    for (FileInfo item : items) {
      if (seen.add(item.getNodeRef())) {
        result.add(item);
      }
    }
    return result;
  }

  /**
   * Determines whether an item's expiration date lies in the past.
   *
   * @param item the file info to inspect
   * @return {@code true} if the item has an expiration date that is before the
   *     current time; {@code false} if it has no (or an empty) expiration date
   * @throws AlfrescoRuntimeException if the stored expiration date cannot be
   *     parsed
   */
  private boolean isExpired(FileInfo item) {
    Object expirationValue = item
      .getProperties()
      .get(DocumentModel.PROP_EXPIRATION_DATE);
    if (expirationValue == null) {
      return false;
    }
    String dateStr = expirationValue.toString().trim();
    if (dateStr.isEmpty()) {
      return false;
    }
    try {
      Date parsedDate = new SimpleDateFormat(
        "EEE MMM dd HH:mm:ss zzz yyyy"
      ).parse(dateStr);
      return new Timestamp(parsedDate.getTime()).before(
        new Timestamp(System.currentTimeMillis())
      );
    } catch (ParseException e) {
      throw new AlfrescoRuntimeException("ParseException.", e);
    }
  }

  /**
   * Updates an existing space: renames it and updates its title, description and
   * expiration date.
   *
   * <p>If the rename fails (name clash or missing node) the name property is set
   * directly instead. An {@code expiration_date} property that is {@code null},
   * empty or the string {@code "null"} clears the stored date; otherwise the
   * {@code bproperties} aspect is ensured and the date is stored. An unparseable
   * date is logged and ignored.
   *
   * @param id the store-qualified id of the space to update
   * @param body the {@link Node} carrying the new name, title, description and
   *     optional {@code expiration_date} property
   */
  @Override
  public void spacesIdPut(String id, Node body) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    try {
      fileFolderService.rename(nodeRef, body.getName().trim());
    } catch (FileExistsException | FileNotFoundException e) {
      this.secureNodeService.setProperty(
        nodeRef,
        ContentModel.PROP_NAME,
        body.getName().trim()
      );
    }
    this.secureNodeService.setProperty(
      nodeRef,
      ContentModel.PROP_TITLE,
      Converter.toMLText(body.getTitle())
    );
    this.secureNodeService.setProperty(
      nodeRef,
      ContentModel.PROP_DESCRIPTION,
      Converter.toMLText(
        RestInputSanitizer.sanitizeRichText(body.getDescription())
      )
    );

    try {
      if (body.getProperties().containsKey(EXPIRATION_DATE)) {
        String expirationDate = body.getProperties().get(EXPIRATION_DATE);
        if (
          expirationDate == null ||
          expirationDate.equals("") ||
          expirationDate.equals("null")
        ) {
          this.secureNodeService.setProperty(
            nodeRef,
            DocumentModel.PROP_EXPIRATION_DATE,
            null
          );
        } else {
          if (
            !this.secureNodeService.hasAspect(
              nodeRef,
              DocumentModel.ASPECT_BPROPERTIES
            )
          ) {
            this.secureNodeService.addAspect(
              nodeRef,
              DocumentModel.ASPECT_BPROPERTIES,
              null
            );
          }
          this.secureNodeService.setProperty(
            nodeRef,
            DocumentModel.PROP_EXPIRATION_DATE,
            Converter.convertStringToDate(expirationDate)
          );
        }
      }
    } catch (ParseException e) {
      logger.error(
        "Invalid expiration date:" + body.getProperties().get(EXPIRATION_DATE),
        e
      );
    }
  }

  /**
   * Creates a URL (web link) content node inside a Library space.
   *
   * <p>The requested name has spaces replaced by underscores, is given an
   * {@code .html} extension when missing, and is made unique within the parent.
   * The created node receives the {@code urlable} aspect and its {@code url}
   * property is set from the request body. If the target node does not carry the
   * Library aspect, nothing is created and {@code null} is returned.
   *
   * @param id the store-qualified id of the Library space to create the link in
   * @param body the {@link Node} providing the link name and its {@code url}
   *     property
   * @return the created link {@link Node}, or the result of resolving a
   *     {@code null} node reference when the parent is not a Library
   */
  @Override
  public Node spacesIdUrlPost(String id, Node body) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    NodeRef createdNode = null;

    if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      String name = body.getName();
      name = name.replace(" ", "_");

      if (!name.matches(PATTERN_HTML_EXTENSION)) {
        name = name + HTML;
      }

      name = nodesApi.generateUniqueName(nodeRef, name);

      final FileInfo fileInfo = fileFolderService.create(
        nodeRef,
        name,
        ContentModel.TYPE_CONTENT
      );
      createdNode = fileInfo.getNodeRef();

      secureNodeService.addAspect(
        createdNode,
        DocumentModel.ASPECT_URLABLE,
        null
      );
      secureNodeService.setProperty(
        createdNode,
        DocumentModel.PROP_URL,
        RestInputSanitizer.requireSafeHttpUrl(body.getProperties().get("url"))
      );
    }

    return nodesApi.getNode(createdNode);
  }

  /**
   * Returns the Interest Groups that have been invited to a shared space,
   * together with their granted permission, as a paginated result.
   *
   * @param spaceId the store-qualified id of the shared space
   * @param startItem the zero-based index of the first item to return
   * @param limit the maximum number of items to return; {@code 0} returns all
   *     items
   * @return a {@link PagedShares} holding the requested window of shares and the
   *     total number of invited Interest Groups
   */
  @Override
  public PagedShares getInvitedInterestGroups(
    String spaceId,
    int startItem,
    int limit
  ) {
    List<Pair<NodeRef, String>> listOfIGs = getInvitedInterestGroups(spaceId);

    int resultSize = listOfIGs.size();

    List<Share> shares = new ArrayList<>();

    int endItem = Math.min(startItem + limit, resultSize);

    if (limit == 0) {
      // amount == 0 means that we want all items
      startItem = 0;
      endItem = resultSize;
    }

    for (int index = startItem; index < endItem; index++) {
      Pair<NodeRef, String> invitedIG = listOfIGs.get(index);

      String igName = (String) unsecureNodeService.getProperty(
        invitedIG.getFirst(),
        ContentModel.PROP_NAME
      );

      shares.add(
        new Share(invitedIG.getFirst().getId(), igName, invitedIG.getSecond())
      );
    }

    return new PagedShares(shares, resultSize);
  }

  /**
   * Resolves the Interest Groups invited to a shared space.
   *
   * <p>Navigates the shared-space container association to its Interest Group
   * child associations, keeping only entries whose referenced Interest Group
   * node still exists, and pairs each with its granted permission.
   *
   * @param spaceId the store-qualified id of the shared space
   * @return a list of (Interest Group node reference, permission) pairs; empty
   *     if the space has no shared-space container
   */
  private List<Pair<NodeRef, String>> getInvitedInterestGroups(String spaceId) {
    NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

    ArrayList<Pair<NodeRef, String>> result = new ArrayList<>();

    List<ChildAssociationRef> childAssocs = secureNodeService.getChildAssocs(
      shareSpace,
      SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
      RegexQNamePattern.MATCH_ALL
    );

    final ChildAssociationRef assocRef;

    if (childAssocs.isEmpty()) {
      return result;
    } else {
      assocRef = childAssocs.get(0);
    }

    NodeRef container = assocRef.getChildRef();

    List<ChildAssociationRef> igChildAssocs = secureNodeService.getChildAssocs(
      container,
      SharedSpaceModel.ASSOC_ITEREST_GROUP,
      RegexQNamePattern.MATCH_ALL
    );

    for (ChildAssociationRef ref : igChildAssocs) {
      NodeRef childRef = ref.getChildRef();

      final NodeRef igNodeRef = (NodeRef) secureNodeService.getProperty(
        childRef,
        SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF
      );

      if ((igNodeRef != null) && secureNodeService.exists(igNodeRef)) {
        final String permission = (String) secureNodeService.getProperty(
          childRef,
          SharedSpaceModel.PROP_PERMISSION
        );
        result.add(new Pair<>(igNodeRef, permission));
      }
    }

    return result;
  }

  /**
   * Removes the sharing of a space with a given Interest Group.
   *
   * @param spaceId the store-qualified id of the shared space
   * @param sharedIGId the store-qualified id of the Interest Group to un-share
   */
  @Override
  public void deleteShare(String spaceId, String sharedIGId) {
    NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);
    NodeRef interestGroup = Converter.createNodeRefFromId(sharedIGId);

    linksBusinessSrv.removeSharing(shareSpace, interestGroup);
  }

  /**
   * Shares a space with an Interest Group, optionally notifying its leaders.
   *
   * @param spaceId the store-qualified id of the space to share
   * @param share the sharing definition (target Interest Group id and
   *     permission)
   * @param notifyLeaders when {@code true}, library leaders/administrators of
   *     the target Interest Group are e-mailed
   */
  @Override
  public void addShare(String spaceId, Share share, boolean notifyLeaders) {
    addShare(spaceId, share, notifyLeaders, false);
  }

  /**
   * Shares a space with an Interest Group, applying the requested permission and
   * optionally notifying its leaders.
   *
   * <p>When the space does not inherit its parent permissions, rules are
   * disabled during the sharing and then re-enabled and re-applied (recursively
   * to child folders) so that permission-driven rules take effect.
   *
   * @param spaceId the store-qualified id of the space to share
   * @param share the sharing definition (target Interest Group id and
   *     permission)
   * @param notifyLeaders when {@code true}, library leaders/administrators are
   *     e-mailed
   * @param permissionUpdate when {@code true}, the notification uses the
   *     permission-update template; otherwise the new-share template is used
   */
  private void addShare(
    String spaceId,
    Share share,
    boolean notifyLeaders,
    boolean permissionUpdate
  ) {
    NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);
    NodeRef igNodeRef = Converter.createNodeRefFromId(share.getIgId());

    boolean inheritParentPermissions =
      permissionService.getInheritParentPermissions(shareSpace);

    if (!inheritParentPermissions) {
      ruleService.disableRules();
    }

    final LibraryPermissions libraryPermissions =
      LibraryPermissions.withPermissionString(share.getPermission());
    linksBusinessSrv.applySharing(shareSpace, igNodeRef, libraryPermissions);

    if (notifyLeaders) {
      notifyLeaders(share, shareSpace, igNodeRef, permissionUpdate);
    }

    if (!inheritParentPermissions) {
      ruleService.enableRules(shareSpace);
      reapplyRules(shareSpace, true, true);
    }
  }

  /**
   * Re-applies the folder rules of a space, optionally cascading to descendant
   * folders.
   *
   * @param space the space whose rules are to be re-applied
   * @param executeInherited when {@code true}, inherited rules are executed as
   *     well
   * @param toChildren when {@code true}, the operation recurses into child
   *     folders
   */
  private void reapplyRules(
    NodeRef space,
    boolean executeInherited,
    boolean toChildren
  ) {
    // Create the the apply rules action
    Action action = actionService.createAction(
      ExecuteAllRulesActionExecuter.NAME
    );
    action.setParameterValue(
      ExecuteAllRulesActionExecuter.PARAM_EXECUTE_INHERITED_RULES,
      executeInherited
    );

    // Execute the action
    actionService.executeAction(action, space);

    if (toChildren) {
      List<ChildAssociationRef> assocs = secureNodeService.getChildAssocs(
        space,
        ContentModel.ASSOC_CONTAINS,
        RegexQNamePattern.MATCH_ALL
      );
      for (ChildAssociationRef assoc : assocs) {
        NodeRef nodeRef = assoc.getChildRef();
        QName className = secureNodeService.getType(nodeRef);
        if (dictionaryService.isSubClass(className, ContentModel.TYPE_FOLDER)) {
          reapplyRules(nodeRef, executeInherited, true);
        }
      }
    }
  }

  /**
   * Notifies the library administrators/leaders of an Interest Group that a
   * space has been shared with (or its share updated for) their group.
   *
   * <p>Locates the library-admin (or leader) sub-group of the Interest Group's
   * invited-user group and sends an e-mail to every user it contains, using the
   * new-share or permission-update template depending on {@code permissionUpdate}.
   *
   * @param share the sharing definition being notified
   * @param shareSpace the space that was shared
   * @param igNodeRef the Interest Group root the notified users belong to
   * @param permissionUpdate when {@code true}, uses the permission-update
   *     notification template; otherwise the new-share template
   */
  private void notifyLeaders(
    Share share,
    NodeRef shareSpace,
    NodeRef igNodeRef,
    boolean permissionUpdate
  ) {
    // get all lib admins
    String groupName = (String) secureNodeService.getProperty(
      igNodeRef,
      CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP
    );
    Set<String> libraryGroups = authorityService.getContainedAuthorities(
      AuthorityType.GROUP,
      "GROUP_" + groupName,
      true
    );

    String libAdminGroup = null;

    for (String libraryGroup : libraryGroups) {
      // added || libraryGroup.contains("Leader") to notify Leaders (originally it was
      // not there)
      if (
        libraryGroup.contains("LibAdmin") || libraryGroup.contains("Leader")
      ) {
        libAdminGroup = libraryGroup;
        break;
      }
    }

    if (libAdminGroup != null) {
      Set<String> libAdmins = authorityService.getContainedAuthorities(
        AuthorityType.USER,
        libAdminGroup,
        false
      );

      final Map<String, Object> extraModelParams = buildModelParam(share);

      for (final String libAdmin : libAdmins) {
        final NodeRef person = personService.getPerson(libAdmin);

        emailApi.mailToUser(
          person,
          shareSpace,
          null,
          extraModelParams,
          buildBodyParams(person),
          true,
          null,
          permissionUpdate
            ? MailTemplate.SHARE_SPACE_PERMISSION_UPDATE_NOTIFICATION
            : MailTemplate.SHARE_SPACE_NOTIFICATION
        );
      }
    }
  }

  /**
   * Builds the FreeMarker model parameters for a share-notification e-mail.
   *
   * @param share the sharing definition (its Interest Group id and permission
   *     feed the model)
   * @return a map of model keys to values, including the notified Interest
   *     Group, the granted profile/permission, the Company Home and CIRCABC
   *     node references and the application name
   */
  private Map<String, Object> buildModelParam(Share share) {
    final Map<String, Object> params = HashMap.newHashMap(2);
    if (share.getIgId() != null) {
      NodeRef igNodeRef = Converter.createNodeRefFromId(share.getIgId());
      params.put(KEY_YOUR_IG, igNodeRef);
    }
    params.put(KEY_PROFILE, share.getPermission());
    params.put(
      MailTemplate.KEY_COMPANY_HOME,
      circabcApi.getCompanyHomeNodeRef()
    );
    params.put(MailTemplate.KEY_CIRCABC, circabcApi.getCircabcNodeRef());
    circabcConfig.addApplicationNameToModel(params);

    return params;
  }

  /**
   * Builds the placeholder substitution parameters (first/last name) used to
   * personalise a notification e-mail body for a recipient.
   *
   * @param person the node reference of the recipient person
   * @return a map of body placeholders to the recipient's first and last name
   */
  private Map<String, String> buildBodyParams(final NodeRef person) {
    final Map<QName, Serializable> personProperties =
      secureNodeService.getProperties(person);
    final Map<String, String> params = HashMap.newHashMap(2);
    params.put(
      FIRSTNAME_REGEX,
      (String) personProperties.get(ContentModel.PROP_FIRSTNAME)
    );
    params.put(
      LASTTNAME_REGEX,
      (String) personProperties.get(ContentModel.PROP_LASTNAME)
    );

    return params;
  }

  /**
   * Changes the permission granted to an Interest Group on a shared space.
   *
   * <p>Implemented by removing the existing share and re-adding it with the new
   * permission, flagging the change as a permission update for notification
   * purposes.
   *
   * @param sharedSpaceId the store-qualified id of the shared space
   * @param igId the store-qualified id of the Interest Group whose permission
   *     changes
   * @param newPermission the new library permission to grant
   * @param notifyLeaders when {@code true}, library leaders are notified of the
   *     change
   */
  @Override
  public void changeSharePermission(
    String sharedSpaceId,
    String igId,
    String newPermission,
    boolean notifyLeaders
  ) {
    deleteShare(sharedSpaceId, igId);

    Share share = new Share(igId, null, newPermission);
    addShare(sharedSpaceId, share, notifyLeaders, true);
  }

  /**
   * Returns the Interest Groups eligible for sharing a space and the library
   * permissions that may be assigned.
   *
   * <p>The {@code LibNoAccess} permission is excluded from the returned list.
   *
   * @param spaceId the store-qualified id of the shared space
   * @return a {@link ShareIGsAndPermissions} pairing candidate Interest Groups
   *     (title, id) with the assignable permission names
   */
  @Override
  public ShareIGsAndPermissions getShareIGsAndPermissions(String spaceId) {
    NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

    List<Pair<String, String>> igs = new ArrayList<>();

    final List<InterestGroupLinkItem> interestGroups =
      linksBusinessSrv.getInterestGroupForSharing(shareSpace);
    for (final InterestGroupLinkItem item : interestGroups) {
      igs.add(new Pair<>(item.getTitle(), item.getNodeRef().getId()));
    }

    List<String> permissions = new ArrayList<>();

    final List<String> perms =
      LibraryPermissions.getOrderedLibraryPermissions();
    for (final String perm : perms) {
      if (perm.equalsIgnoreCase("LibNoAccess")) {
        continue;
      }
      permissions.add(perm);
    }

    return new ShareIGsAndPermissions(igs, permissions);
  }

  /**
   * Returns the shared spaces that are available to be linked from the given
   * space.
   *
   * @param spaceId the store-qualified id of the space requesting available
   *     shared spaces
   * @return the list of {@link ShareSpaceItem}s that may be linked
   */
  @Override
  public List<ShareSpaceItem> getAvailableSharedSpaces(String spaceId) {
    NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

    return linksBusinessSrv.getAvailableSharedSpaces(shareSpace);
  }

  /**
   * Creates a link to a shared space under a given parent space.
   *
   * <p>A unique link name is derived from the target space name (prefixed with
   * "Link to " and suffixed with ".url") before the link is created.
   *
   * @param spaceId the store-qualified id of the shared (target) space to link
   *     to
   * @param parentId the store-qualified id of the parent space to create the
   *     link in
   * @param title the title to give the link
   * @param description the description to give the link
   */
  @Override
  public void createSharedSpaceLink(
    String spaceId,
    String parentId,
    String title,
    String description
  ) {
    NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

    NodeRef parentSpace = Converter.createNodeRefFromId(parentId);

    final String name = getLinkName(parentSpace, shareSpace);

    shareSpaceService.linkSharedSpace(
      parentSpace,
      shareSpace,
      name,
      title,
      description
    );
  }

  /**
   * Builds a unique link node name for linking {@code target} under
   * {@code parent}.
   *
   * @param parent the folder the link will be created in
   * @param target the space the link points to
   * @return a name of the form "Link to &lt;target name&gt;.url" made unique
   *     within the parent
   */
  private String getLinkName(final NodeRef parent, final NodeRef target) {
    final String targetName = (String) unsecureNodeService.getProperty(
      target,
      ContentModel.PROP_NAME
    );

    return metadataManager.getValidUniqueName(
      parent,
      LINK_TO_PREFIX + targetName + LINK_NODE_EXTENSION
    );
  }

  /**
   * Lists the children of a restricted Library or Information space using a
   * search query (rather than a folder listing), with pagination and sorting.
   *
   * <p>Non-pivot multilingual translations are filtered out of the results. If
   * the node is neither a Library nor an Information space, an empty
   * {@link PagedNodes} is returned.
   *
   * @param id the store-qualified id of the parent space
   * @param nbPage zero-based page index used to compute the search skip count
   * @param nbLimit maximum number of results per page; {@code -1} means no limit
   * @param sort sort descriptor (property local name optionally suffixed with
   *     {@code _ASC}/{@code _DESC}); may be {@code null} or empty
   * @param folderOnly reserved filter flag for folder-only results
   * @param fileOnly reserved filter flag for file-only results
   * @return the matching children together with the total number found
   */
  @Override
  public PagedNodes restrictedSpaceGetChildren(
    String id,
    int nbPage,
    int nbLimit,
    String sort,
    boolean folderOnly,
    boolean fileOnly
  ) {
    PagedNodes pagedResult = new PagedNodes();

    List<Node> result = new ArrayList<>();
    NodeRef spaceNodeRef = Converter.createNodeRefFromId(id);
    if (
      !unsecureNodeService.hasAspect(
        spaceNodeRef,
        CircabcModel.ASPECT_LIBRARY
      ) &&
      !unsecureNodeService.hasAspect(
        spaceNodeRef,
        CircabcModel.ASPECT_INFORMATION
      )
    ) {
      return pagedResult;
    }

    SearchParameters searchParameters = buildSearchParameters(
      id,
      nbPage,
      nbLimit,
      sort
    );

    FileFilterMode.setClient(Client.cmis);

    ResultSet rs = secureSearchService.query(searchParameters);

    for (NodeRef item : rs.getNodeRefs()) {
      if (!isNonPivotTranslation(item, unsecureNodeService)) {
        result.add(nodesApi.getNode(item));
      }
    }

    FileFilterMode.clearClient();

    pagedResult.setTotal(rs.getNumberFound());
    pagedResult.setData(result);

    return pagedResult;
  }

  /**
   * Builds the Lucene {@link SearchParameters} used by
   * {@link #restrictedSpaceGetChildren}.
   *
   * <p>The query is scoped to the path of the given space and restricted to
   * folders and content, targets the workspace SpacesStore, applies paging when
   * {@code nbLimit != -1} and appends sorting when a sort descriptor is
   * supplied.
   *
   * @param id the store-qualified id of the space to scope the search to
   * @param nbPage zero-based page index used to compute the skip count
   * @param nbLimit maximum number of results per page; {@code -1} means no limit
   * @param sort sort descriptor; may be {@code null} or empty
   * @return the configured search parameters
   */
  private SearchParameters buildSearchParameters(
    String id,
    int nbPage,
    int nbLimit,
    String sort
  ) {
    StringBuilder queryBuilder = new StringBuilder();
    if (id != null) {
      NodeRef targetRef = Converter.createNodeRefFromId(id);
      queryBuilder
        .append("(PATH:\"")
        .append(apiToolBox.getPathFromSpaceRef(targetRef, true))
        .append("\")");
    }
    queryBuilder.append(" AND  (TYPE:\"cm:folder\"  OR  TYPE:\"cm:content\" )");

    SearchParameters searchParameters = new SearchParameters();
    if (nbLimit != -1) {
      searchParameters.setLimit(nbLimit);
      searchParameters.setSkipCount(nbPage * nbLimit);
    }
    searchParameters.setMaxItems(-1);
    searchParameters.setQuery(queryBuilder.toString());
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

    if (sort != null && !sort.equalsIgnoreCase("")) {
      applySortParameters(searchParameters, sort);
    }

    return searchParameters;
  }

  /**
   * Adds a sort clause to the given search parameters.
   *
   * <p>CIRCABC-specific properties ({@code security_ranking},
   * {@code expiration_date}, {@code status}) are resolved against the CIRCABC
   * document model namespace, all others against the default content model
   * namespace. The sort direction is ascending when the descriptor ends with
   * {@code ASC}.
   *
   * @param searchParameters the search parameters to augment
   * @param sort the sort descriptor (property local name optionally suffixed
   *     with {@code _ASC}/{@code _DESC})
   */
  private void applySortParameters(
    SearchParameters searchParameters,
    String sort
  ) {
    String localName = sort.replace("_DESC", "").replace("_ASC", "");
    String namespace = NamespaceService.CONTENT_MODEL_1_0_URI;
    if (
      localName.equals("security_ranking") ||
      localName.equals(EXPIRATION_DATE) ||
      localName.equals("status")
    ) {
      namespace = DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI;
    }
    String sortField = "@" + QName.createQName(namespace, localName).toString();
    searchParameters.addSort(sortField, sort.endsWith("ASC"));
  }

  /**
   * Computes the total size, in bytes, of a space by summing the sizes of all
   * contained files recursively.
   *
   * <p>Sub-folders are descended into; for non-folder children the {@code size}
   * property is parsed and added.
   *
   * @param id the store-qualified id of the space to measure
   * @return the aggregated size of all files contained in the space and its
   *     sub-spaces
   * @throws NumberFormatException if a child's {@code size} property is not a
   *     valid integer
   */
  @Override
  public int getFolderSize(String id) {
    int totalSize = 0;
    List<Node> childNodes = spaceGetChildren(id, false);

    for (Node child : childNodes) {
      if (child.getType().contains("folder")) {
        totalSize += getFolderSize(child.getId());
      } else {
        totalSize += Integer.parseInt(child.getProperties().get("size"));
      }
    }

    return totalSize;
  }
}
