package io.swagger.api;

import io.swagger.model.PagedArchiveNodes;
import io.swagger.model.RestoreNodeMetadata;

/**
 * Business operations for managing deleted (archived) documents of an Interest
 * Group.
 *
 * <p>When content is deleted within an Interest Group it is moved to the
 * Alfresco archive store rather than being permanently removed. This interface
 * exposes the operations backing the REST endpoints that let clients list the
 * archived nodes of a group, restore a previously deleted node and permanently
 * purge an archived node.</p>
 *
 * <p>Implementations contain the actual logic and are wired to the webscript
 * endpoint classes that expose these operations over HTTP.</p>
 *
 * @author beaurpi
 */
public interface ArchiveApi {
  /**
   * Retrieves a paginated list of the deleted (archived) documents belonging to
   * the given Interest Group.
   *
   * @param id the identifier of the Interest Group whose archived documents are
   *     requested
   * @param limit the maximum number of archived nodes to return per page
   * @param page the zero-based (or one-based, per implementation) index of the
   *     page to retrieve
   * @param order the ordering criterion to apply to the returned nodes
   * @return a {@link PagedArchiveNodes} holding the requested page of archived
   *     nodes together with pagination metadata
   */
  PagedArchiveNodes groupsIdDocumentsDeletedGet(
    String id,
    Integer limit,
    Integer page,
    String order
  );

  /**
   * Restores a previously deleted document from the archive back into the given
   * Interest Group.
   *
   * @param id the identifier of the Interest Group the node is restored into
   * @param restoreNodeMetadata metadata describing the archived node to restore
   *     and any parameters governing the restore operation
   */
  void groupsIdDocumentsDeletedPost(
    String id,
    RestoreNodeMetadata restoreNodeMetadata
  );

  /**
   * Permanently deletes a single archived node from the given Interest Group,
   * removing it from the archive store for good.
   *
   * @param id the identifier of the Interest Group the archived node belongs to
   * @param nodeId the identifier of the archived node to permanently delete
   */
  void groupsIdDocumentsDeletedNodeIdDelete(String id, String nodeId);
}
