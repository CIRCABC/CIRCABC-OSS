package io.swagger.api;

import eu.cec.digit.circabc.model.DocumentModel;
import io.swagger.model.PagedNodes;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.web.bean.repository.Repository;

/**
 * @author beaurpi
 */
public class ExpiredApiImpl implements ExpiredApi {

    private static final String PROP_EXPIRATION_DATE =
            Repository.escapeQName(DocumentModel.PROP_EXPIRATION_DATE);
    private static final String QUERY_SEARCH_ALL = "PARENT:\"%s\"";
    private static final String CLOSE_QUERY = " )";
    private static final String OPEN_QUERY = "( ";
    private static final String PATH = "PATH:";
    private static final String ESCAPE_QUOTES = "\" ";
    private SearchService internalSearchService;
    private NodesApi nodesApi;
    private NodeService nodeService;
    private ApiToolBox apiToolBox;

    @Override
    public PagedNodes groupsIdDocumentsExpiredGet(
            String id, Integer limit, Integer page, String order) {

        final NodeRef nodeRef = Converter.createNodeRefFromId(id);

        return (PagedNodes)
                AuthenticationUtil.runAs(
                        new AuthenticationUtil.RunAsWork<Object>() {

                            public Object doWork() {

                                PagedNodes expiredNodes = new PagedNodes();

                                ResultSet results;
                                // fire the query to find the items
                                final String query = buildSearchQuery(nodeRef);
                                results =
                                        getInternalSearchService()
                                                .query(
                                                        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                                                        SearchService.LANGUAGE_LUCENE,
                                                        query);

                                expiredNodes.setTotal(results.getNumberFound());

                                if (results.length() != 0) {
                                    for (final ResultSetRow row : results) {
                                        NodeRef expiredRef = row.getNodeRef();
                                        expiredNodes.getData().add(nodesApi.getNode(expiredRef));
                                    }
                                }

                                return expiredNodes;
                            }
                        },
                        AuthenticationUtil.getSystemUserName());
    }

    /**
     * @return the search query to use when displaying the list of deleted items
     */
    private String buildSearchQuery(NodeRef nodeRef) {
        String query = String.format(QUERY_SEARCH_ALL, nodeRef.toString());

        query +=
                OPEN_QUERY
                        + PATH
                        + ESCAPE_QUOTES
                        + apiToolBox.getPathFromSpaceRef(nodeRef, true)
                        + ESCAPE_QUOTES
                        + CLOSE_QUERY;

        String buf = " AND @" + PROP_EXPIRATION_DATE + ":[MIN TO NOW]";
        query += buf;

        return query;
    }

    public SearchService getInternalSearchService() {
        return internalSearchService;
    }

    public void setInternalSearchService(SearchService internalSearchService) {
        this.internalSearchService = internalSearchService;
    }

    public NodesApi getNodesApi() {
        return nodesApi;
    }

    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }
}
