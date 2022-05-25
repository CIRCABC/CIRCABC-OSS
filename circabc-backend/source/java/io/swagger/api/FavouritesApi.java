package io.swagger.api;

import io.swagger.model.PagedNodes;
import io.swagger.model.SimpleId;

/**
 * @author beaurpi
 */
public interface FavouritesApi {

    PagedNodes usersUserIdFavouritesGet(String userName, Integer page, Integer limit);

    void usersUserIdFavouritesNodeIdDelete(String userName, String nodeId);

    void usersUserIdFavouritesPost(String userName, SimpleId nodeId);
}
