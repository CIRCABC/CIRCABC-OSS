package io.swagger.api;

import io.swagger.model.PagedNodes;
import io.swagger.model.SimpleId;
import io.swagger.util.Converter;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.favourites.PersonFavourite;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link FavouritesApi} that manages a user's favourite nodes
 * (files and folders) on top of the Alfresco {@link FavouritesService}.
 *
 * <p>It supports listing a user's favourites in a paged fashion, adding a node to the
 * favourites and removing a node from the favourites. Node identifiers received from the
 * REST layer are resolved into Alfresco {@link NodeRef}s and each favourite is enriched with
 * its full node representation via {@link NodesApi}.
 *
 * @author beaurpi
 */
public class FavouritesApiImpl implements FavouritesApi {

  /** Alfresco service used to query, add and remove a person's favourite nodes. */
  @Autowired
  private FavouritesService favouritesService;

  /** API used to resolve an Alfresco {@link NodeRef} into its full node representation. */
  @Autowired
  private NodesApi nodesApi;

  /**
   * Returns the paged list of a user's favourite files and folders.
   *
   * <p>Only favourites of type {@link Type#FILE} and {@link Type#FOLDER} are considered.
   * Each favourite is resolved to its full node representation and the total count of
   * favourites is included in the returned result.
   *
   * @param userName the user name whose favourites are retrieved
   * @param page the zero-based page index; the offset is computed as {@code page * limit}
   * @param limit the maximum number of favourites to return per page
   * @return a {@link PagedNodes} containing the resolved favourite nodes for the requested
   *     page and the total number of favourites
   */
  @Override
  public PagedNodes usersUserIdFavouritesGet(
    String userName,
    Integer page,
    Integer limit
  ) {
    Set<Type> types = new HashSet<>();
    types.add(Type.FILE);
    types.add(Type.FOLDER);

    PagedNodes result = new PagedNodes();

    PagingRequest pagingRequest = new PagingRequest(page * limit, limit);

    PagingResults<PersonFavourite> favourites =
      favouritesService.getPagedFavourites(
        userName,
        types,
        FavouritesService.DEFAULT_SORT_PROPS,
        pagingRequest
      );

    for (PersonFavourite favourite : favourites.getPage()) {
      NodeRef nodeRef = favourite.getNodeRef();
      result.getData().add(nodesApi.getNode(nodeRef));
    }

    Pair<Integer, Integer> pair = favourites.getTotalResultCount();
    int total = 0;
    if (pair.getFirst().equals(pair.getSecond())) {
      total = pair.getFirst();
    }
    result.setTotal((long) total);

    return result;
  }

  /**
   * Removes a node from the given user's favourites.
   *
   * <p>The supplied node identifier is converted into an Alfresco {@link NodeRef} before the
   * favourite is removed.
   *
   * @param userName the user name whose favourite is removed
   * @param nodeId the identifier of the node to remove from the user's favourites
   */
  @Override
  public void usersUserIdFavouritesNodeIdDelete(
    String userName,
    String nodeId
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
    favouritesService.removeFavourite(userName, nodeRef);
  }

  /**
   * Adds a node to the given user's favourites.
   *
   * <p>The identifier wrapped by the supplied {@link SimpleId} is converted into an Alfresco
   * {@link NodeRef} before the favourite is added.
   *
   * @param userName the user name for which the favourite is added
   * @param nodeId a {@link SimpleId} wrapping the identifier of the node to mark as favourite
   */
  @Override
  public void usersUserIdFavouritesPost(String userName, SimpleId nodeId) {
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId.getId());
    favouritesService.addFavourite(userName, nodeRef);
  }
}
