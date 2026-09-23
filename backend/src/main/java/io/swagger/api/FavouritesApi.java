package io.swagger.api;

import io.swagger.model.PagedNodes;
import io.swagger.model.SimpleId;

/**
 * Business operations for managing a user's favourite nodes.
 *
 * <p>This interface defines the service contract backing the {@code favourites} REST endpoints. It
 * allows retrieving the list of nodes a user has marked as favourite, adding a node to that list,
 * and removing a node from it. Implementations contain the actual logic, while the corresponding
 * Alfresco webscripts delegate to these operations.
 *
 * @author beaurpi
 */
public interface FavouritesApi {
  /**
   * Retrieves the paginated list of nodes marked as favourite by the given user.
   *
   * @param userName the identifier (user name) of the user whose favourites are requested
   * @param page the zero-based (or one-based, per implementation) index of the page to return
   * @param limit the maximum number of favourite nodes to include per page
   * @return a {@link PagedNodes} holding the requested page of favourite nodes and pagination
   *     metadata
   */
  PagedNodes usersUserIdFavouritesGet(
    String userName,
    Integer page,
    Integer limit
  );

  /**
   * Removes a node from the given user's list of favourites.
   *
   * @param userName the identifier (user name) of the user whose favourite is removed
   * @param nodeId the identifier of the node to remove from the user's favourites
   */
  void usersUserIdFavouritesNodeIdDelete(String userName, String nodeId);

  /**
   * Adds a node to the given user's list of favourites.
   *
   * @param userName the identifier (user name) of the user for whom the favourite is added
   * @param nodeId a {@link SimpleId} wrapping the identifier of the node to mark as favourite
   */
  void usersUserIdFavouritesPost(String userName, SimpleId nodeId);
}
