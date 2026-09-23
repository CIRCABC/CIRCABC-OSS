package io.swagger.api;

import io.swagger.model.ArchiveNode;
import io.swagger.model.PagedArchiveNodes;
import io.swagger.model.RestoreNodeMetadata;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.Converter;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Implementation of {@link ArchiveApi} providing access to the archive (deleted-nodes) store for
 * an Interest Group.
 *
 * <p>This service exposes the business logic behind the "deleted documents" REST endpoints of an
 * Interest Group. It allows callers to:
 *
 * <ul>
 *   <li>list the documents that have been deleted (moved to the Alfresco archive store) for a given
 *       Interest Group,
 *   <li>restore a previously deleted document, optionally to a specific target folder,
 *   <li>permanently purge a deleted document from the archive store.
 * </ul>
 *
 * <p>All operations temporarily elevate to the system user via {@link AuthenticationUtil} so that
 * the archive store can be queried and mutated regardless of the caller's permissions, and restore
 * the original run-as user afterwards.</p>
 *
 * @author beaurpi
 */
public class ArchiveApiImpl implements ArchiveApi {

  /**
   * Escaped Lucene attribute name for the Interest Group root node id stored on archived nodes,
   * derived from {@link CircabcModel#PROP_IG_ROOT_NODE_ID_ARCHIVED}. Used to scope archive searches
   * to a single Interest Group.
   */
  private static final String PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR =
    Converter.escapeQName(CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED);

  /**
   * Lucene query template used to find all archived nodes that belong to a given Interest Group.
   * The placeholders are, in order: the archive root {@link NodeRef}, the archived aspect QName and
   * the Interest Group root node id.
   */
  private static final String SEARCH_ALL =
    "PARENT:\"%s\" AND ASPECT:\"%s\"" +
    "  AND @" +
    PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR +
    ":\"%s\"";

  /** Alfresco service used to restore and purge nodes from the archive store. */
  @Autowired
  private NodeArchiveService nodeArchiveService;

  /** Search service used to run Lucene queries against the archive store. */
  @Autowired
  @Qualifier("searchService")
  private SearchService internalSearchService;

  /** API used to convert Alfresco {@link NodeRef}s into REST node representations. */
  @Autowired
  private NodesApi nodesApi;

  /** Alfresco node service used to read archive metadata (deleted by / deleted date). */
  @Autowired
  private NodeService nodeService;

  /**
   * Returns the paginated list of documents that have been deleted (archived) for the given
   * Interest Group.
   *
   * <p>Runs as the system user to query the Alfresco archive store for all nodes bearing the
   * archived aspect and matching the Interest Group root node id. Each returned entry is enriched
   * with the archive metadata ("deleted by" and "deleted date"). Sorting can be requested by name
   * or by archived date, and results can be paged using {@code limit} and {@code page}.</p>
   *
   * @param id the Interest Group root node id whose deleted documents are requested
   * @param limit the maximum number of items per page; a value {@code <= 0} means no limit
   * @param page the 1-based page number; used together with {@code limit} to compute the skip count
   * @param order the sort directive; may be empty for no sorting, otherwise a string containing
   *     either {@code "name"} or {@code "archived"} and suffixed with {@code "ASC"} or {@code
   *     "DESC"} (e.g. {@code "name_ASC"}, {@code "archivedDate_DESC"})
   * @return the paged archive nodes, including the total number of matches and the enriched {@link
   *     ArchiveNode} entries for the requested page
   */
  @Override
  public PagedArchiveNodes groupsIdDocumentsDeletedGet(
    String id,
    Integer limit,
    Integer page,
    String order
  ) {
    PagedArchiveNodes result = new PagedArchiveNodes();
    NodeRef archiveRootNode = nodeArchiveService.getStoreArchiveNode(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
    );

    String username = AuthenticationUtil.getRunAsUser();
    AuthenticationUtil.setRunAsUserSystem();

    try {
      final String query = buildSearchQuery(id, archiveRootNode);
      final SearchParameters sp = new SearchParameters();
      sp.setLanguage(SearchService.LANGUAGE_LUCENE);
      sp.setQuery(query);
      sp.addStore(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE); // the Archived
      // Node store

      applySort(order, sp);
      applyPaging(limit, page, sp);

      ResultSet resultSet = internalSearchService.query(sp);

      result.setTotal(resultSet.getNumberFound());

      List<ArchiveNode> nodes = new ArrayList<>();

      for (ResultSetRow row : resultSet) {
        nodes.add(buildArchiveNode(row.getNodeRef()));
      }

      result.setData(nodes);

      return result;
    } finally {
      AuthenticationUtil.setRunAsUser(username);
    }
  }

  /**
   * Builds the Lucene query used to find all archived nodes belonging to the given Interest Group.
   *
   * @param id the Interest Group root node id to filter archived nodes on
   * @param archiveRootNode the root node of the archive store to scope the search under
   * @return the formatted Lucene query string
   */
  private String buildSearchQuery(String id, NodeRef archiveRootNode) {
    return String.format(
      SEARCH_ALL,
      archiveRootNode,
      ContentModel.ASPECT_ARCHIVED,
      id
    );
  }

  /**
   * Applies sorting to the given search parameters based on the {@code order} argument.
   *
   * @param order the sort directive (may be blank)
   * @param sp the search parameters to configure
   */
  private void applySort(String order, SearchParameters sp) {
    if (order == null || order.isBlank()) {
      return;
    }

    if (order.contains("name")) {
      String[] tokens = order.split("_");
      sp.addSort("@cm:" + tokens[0], order.endsWith("ASC"));
    } else if (order.contains("archived")) {
      String[] tokens = order.split("_");
      sp.addSort("@sys:" + tokens[0], order.endsWith("ASC"));
    }
  }

  /**
   * Applies paging directives (limit / page) to the given search parameters.
   *
   * @param limit the maximum number of items per page; {@code <= 0} means no limit
   * @param page the 1-based page number; ignored when {@code limit <= 0}
   * @param sp the search parameters to configure
   */
  private void applyPaging(Integer limit, Integer page, SearchParameters sp) {
    if (limit == null || limit <= 0) {
      return;
    }

    sp.setMaxItems(limit);

    if (page != null && page > 1) {
      sp.setSkipCount(limit * (page - 1));
    }
  }

  /**
   * Builds an {@link ArchiveNode} representation for the given archived node reference.
   *
   * @param nodeRef the archived node reference
   * @return the populated {@link ArchiveNode}
   */
  private ArchiveNode buildArchiveNode(NodeRef nodeRef) {
    ArchiveNode archiveNode = new ArchiveNode();
    archiveNode.mergeNode(nodesApi.getNode(nodeRef));
    archiveNode.setDeletedBy(
      nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_BY).toString()
    );
    DateTime archivedDate = new DateTime(
      nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_DATE)
    );
    archiveNode.setDeletedDate(archivedDate);
    return archiveNode;
  }

  /**
   * Restores a previously deleted document from the archive store.
   *
   * <p>Runs as the system user. If a target folder is provided in the metadata the node is restored
   * into that folder; otherwise it is restored to its original location. If no archive node id is
   * supplied the method does nothing.
   *
   * @param id the Interest Group root node id (contextual identifier for the request)
   * @param restoreNodeMetadata the restore instructions, containing the archive node id to restore
   *     and an optional target folder id
   */
  @Override
  public void groupsIdDocumentsDeletedPost(
    String id,
    RestoreNodeMetadata restoreNodeMetadata
  ) {
    if (
      restoreNodeMetadata == null ||
      "".equals(restoreNodeMetadata.getArchiveNodeId())
    ) {
      return;
    }

    NodeRef nodeRef = Converter.createArchiveNodeRefFromId(
      restoreNodeMetadata.getArchiveNodeId()
    );
    requireArchiveNodeInInterestGroup(id, nodeRef);

    NodeRef spaceRef = null;
    if (!"".equals(restoreNodeMetadata.getTargetFolderId())) {
      spaceRef = Converter.createNodeRefFromId(
        restoreNodeMetadata.getTargetFolderId()
      );
      requireNodeInInterestGroup(id, spaceRef);
    }

    String userName = AuthenticationUtil.getRunAsUser();
    AuthenticationUtil.setRunAsUserSystem();

    try {
      if (spaceRef != null) {
        nodeArchiveService.restoreArchivedNode(nodeRef, spaceRef, null, null);
      } else {
        nodeArchiveService.restoreArchivedNode(nodeRef);
      }
    } finally {
      AuthenticationUtil.setRunAsUser(userName);
    }
  }

  /**
   * Permanently purges a deleted document from the archive store, making the deletion irreversible.
   *
   * <p>Runs as the system user and removes the archived node identified by {@code nodeId} from the
   * archive store.</p>
   *
   * @param id the Interest Group root node id (contextual identifier for the request)
   * @param nodeId the id of the archived node to purge permanently
   */
  @Override
  public void groupsIdDocumentsDeletedNodeIdDelete(String id, String nodeId) {
    NodeRef nodeRef = Converter.createArchiveNodeRefFromId(nodeId);
    requireArchiveNodeInInterestGroup(id, nodeRef);

    String userName = AuthenticationUtil.getRunAsUser();
    AuthenticationUtil.setRunAsUserSystem();

    try {
      nodeArchiveService.purgeArchivedNode(nodeRef);
    } finally {
      AuthenticationUtil.setRunAsUser(userName);
    }
  }

  /**
   * Verifies that the given archived node belongs to the interest group identified by {@code id},
   * rejecting the request with {@link AccessDeniedException} otherwise. This prevents restoring or
   * purging an archived node that belongs to a different interest group than the one the caller is
   * authorized on.
   *
   * @param id the authorized interest group root node id
   * @param archiveNodeRef the archived node reference to validate
   */
  private void requireArchiveNodeInInterestGroup(
    String id,
    NodeRef archiveNodeRef
  ) {
    Object igRootId = nodeService.getProperty(
      archiveNodeRef,
      CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED
    );
    if (!id.equals(igRootId)) {
      throw new AccessDeniedException(
        "Archived node does not belong to the authorized interest group"
      );
    }
  }

  /**
   * Verifies that the given (live) node is contained within the interest group identified by
   * {@code id} by walking up its primary parent chain until the IG-root aspect is found. Rejects the
   * request with {@link AccessDeniedException} when the node belongs to a different interest group.
   *
   * @param id the authorized interest group root node id
   * @param nodeRef the restore-target node reference to validate
   */
  private void requireNodeInInterestGroup(String id, NodeRef nodeRef) {
    NodeRef currentNodeRef = nodeRef;
    boolean foundIgRoot = false;

    while (
      !foundIgRoot &&
      currentNodeRef != null &&
      nodeService.exists(currentNodeRef)
    ) {
      if (nodeService.hasAspect(currentNodeRef, CircabcModel.ASPECT_IGROOT)) {
        if (id.equals(currentNodeRef.getId())) {
          return;
        }
        // Found IG root but it belongs to a different group — deny access.
        foundIgRoot = true;
      } else {
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(
          currentNodeRef
        );
        currentNodeRef = (parentAssoc != null)
          ? parentAssoc.getParentRef()
          : null;
      }
    }

    throw new AccessDeniedException(
      "Restore target does not belong to the authorized interest group"
    );
  }
}
