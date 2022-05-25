package io.swagger.api;

import io.swagger.model.PagedArchiveNodes;
import io.swagger.model.RestoreNodeMetadata;

/**
 * @author beaurpi
 */
public interface ArchiveApi {

    PagedArchiveNodes groupsIdDocumentsDeletedGet(
            String id, Integer limit, Integer page, String order);

    void groupsIdDocumentsDeletedPost(String id, RestoreNodeMetadata restoreNodeMetadata);

    void groupsIdDocumentsDeletedNodeIdDelete(String id, String nodeId);
}
