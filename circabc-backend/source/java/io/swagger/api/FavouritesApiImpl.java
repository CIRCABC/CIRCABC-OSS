package io.swagger.api;

import io.swagger.model.PagedNodes;
import io.swagger.model.SimpleId;
import io.swagger.util.Converter;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.favourites.PersonFavourite;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * @author beaurpi
 */
public class FavouritesApiImpl implements FavouritesApi {

    private FavouritesService favouritesService;
    private NodesApi nodesApi;

    @Override
    public PagedNodes usersUserIdFavouritesGet(String userName, Integer page, Integer limit) {

        Set<Type> types = new HashSet<>();
        types.add(Type.FILE);
        types.add(Type.FOLDER);

        PagedNodes result = new PagedNodes();

        PagingRequest pagingRequest = new PagingRequest(page * limit, limit);

        PagingResults<PersonFavourite> favourites =
                favouritesService.getPagedFavourites(
                        userName, types, FavouritesService.DEFAULT_SORT_PROPS, pagingRequest);

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

    @Override
    public void usersUserIdFavouritesNodeIdDelete(String userName, String nodeId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        favouritesService.removeFavourite(userName, nodeRef);
    }

    @Override
    public void usersUserIdFavouritesPost(String userName, SimpleId nodeId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId.getId());
        favouritesService.addFavourite(userName, nodeRef);
    }

    public FavouritesService getFavouritesService() {
        return favouritesService;
    }

    public void setFavouritesService(FavouritesService favouritesService) {
        this.favouritesService = favouritesService;
    }

    /**
     * @return the nodesApi
     */
    public NodesApi getNodesApi() {
        return nodesApi;
    }

    /**
     * @param nodesApi the nodesApi to set
     */
    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }
}
