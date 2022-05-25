package io.swagger.api;

import io.swagger.model.PagedNodes;

/**
 * @author beaurpi
 */
public interface ExpiredApi {

    PagedNodes groupsIdDocumentsExpiredGet(String id, Integer limit, Integer page, String order);
}
