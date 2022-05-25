package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.Node;
import io.swagger.model.PagedSearchNodes;
import io.swagger.model.SearchNode;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.web.bean.repository.Repository;

/**
 * @author beaurpi
 */
public class SearchApiImpl implements SearchApi {

    private SearchService searchService;
    private NodeService nodeService;
    private NodesApi nodesApi;
    private ApiToolBox apiToolBox;

    @Override
    public PagedSearchNodes searchGet(String q, String nodeId, Integer limit, Integer page)
            throws EmptyQueryStringException {

        if ("".equals(q) || q == null) {
            throw new EmptyQueryStringException("The query text cannot be empty");
        }

        PagedSearchNodes result = new PagedSearchNodes();

        if (limit == null) {
            limit = -1;
        }

        if (page == null) {
            page = 1;
        }

        StringBuilder queryBuilder = new StringBuilder();

        if (nodeId != null) {
            NodeRef targetRef = Converter.createNodeRefFromId(nodeId);
            String path = "(PATH:\"" + apiToolBox.getPathFromSpaceRef(targetRef, true) + "\")";
            queryBuilder.append(path);
        }

        String and = " AND ";
        queryBuilder.append(and);

        String or = " OR ";
        if (q.trim().startsWith("'") && q.trim().endsWith("'")) {

            queryBuilder.append(" ( ");

            queryBuilder.append("TEXT:").append(q);
            queryBuilder.append(or);
            queryBuilder.append("@name:").append(q);
            queryBuilder.append(or);
            queryBuilder.append("@title:").append(q);

            queryBuilder.append(" ) ");

        } else {
            queryBuilder.append(" ( ");

            String[] queryItems = q.trim().split(" ");
            for (int i = 0; i < queryItems.length; i++) {
                queryBuilder.append("TEXT:").append(queryItems[i]);
                queryBuilder.append(or);
                queryBuilder.append("@name:").append(queryItems[i]);
                queryBuilder.append(or);
                queryBuilder.append("@title:").append(queryItems[i]);
                if (i + 1 < queryItems.length) {
                    queryBuilder.append(or);
                }
            }
            queryBuilder.append(" ) ");
        }

        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setLimit(limit);
        searchParameters.setSkipCount(limit * page);
        searchParameters.setMaxItems(-1);
        searchParameters.setQuery(queryBuilder.toString());
        searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
        searchParameters.addStore(Repository.getStoreRef());

        ResultSet rs = searchService.query(searchParameters);

        for (NodeRef ref : rs.getNodeRefs()) {

            SearchNode psn = new SearchNode();
            Node n = nodesApi.getNode(ref);
            psn.setId(n.getId());
            psn.setDescription(n.getDescription());
            psn.setName(n.getName());
            psn.setParentId(n.getParentId());
            psn.setType(n.getType());
            psn.setPermissions(n.getPermissions());
            psn.setProperties(n.getProperties());
            psn.setService(n.getService());

            if (nodeService.hasAspect(ref, CircabcModel.ASPECT_LIBRARY)) {

                if (nodeService.getType(ref).equals(ForumModel.TYPE_TOPIC)) {
                    psn.setResultType("topic");
                    psn.setTargetNode(getLibraryParentOfTopic(ref));
                } else if (nodeService.getType(ref).equals(ForumModel.TYPE_POST)) {
                    psn.setResultType("post");
                    psn.setTargetNode(getLibraryParentOfPost(ref));
                } else if (nodeService.getType(ref).equals(ContentModel.TYPE_FOLDER)) {
                    psn.setResultType("folder");
                } else if (nodeService.getType(ref).equals(ContentModel.TYPE_CONTENT)) {
                    psn.setResultType("file");
                }
            }

            if (nodeService.hasAspect(ref, CircabcModel.ASPECT_NEWSGROUP)) {

                if (nodeService.getType(ref).equals(ForumModel.TYPE_TOPIC)) {
                    psn.setResultType("topic");
                    psn.setTargetNode(getNewsgroupParentOfTopic(ref));
                } else if (nodeService.getType(ref).equals(ForumModel.TYPE_POST)) {
                    psn.setResultType("post");
                    psn.setTargetNode(getNewsgroupParentOfPost(ref));
                } else if (nodeService.getType(ref).equals(ForumModel.TYPE_FORUM)) {
                    psn.setResultType("forum");
                }
            }

            result.getData().add(psn);
        }

        result.setTotal(rs.getNumberFound());

        return result;
    }

    /**
     * return the topic ref in case the node is in the newsgroups
     */
    private String getNewsgroupParentOfPost(NodeRef ref) {
        return nodeService.getPrimaryParent(ref).getParentRef().getId();
    }

    /**
     * return the forum ref in case the node is in the newsgroups
     */
    private String getNewsgroupParentOfTopic(NodeRef ref) {
        NodeRef topicRef = nodeService.getPrimaryParent(ref).getParentRef();
        return nodeService.getPrimaryParent(topicRef).getParentRef().getId();
    }

    /**
     * return the document ref in case the node is a thread of comment
     */
    private String getLibraryParentOfTopic(NodeRef ref) {
        NodeRef discussionRef = nodeService.getPrimaryParent(ref).getParentRef();
        return nodeService.getPrimaryParent(discussionRef).getParentRef().getId();
    }

    /**
     * return the document ref in case the node is a post in a document
     */
    private String getLibraryParentOfPost(NodeRef ref) {
        NodeRef discussionRef = nodeService.getPrimaryParent(ref).getParentRef();
        NodeRef topicRef = nodeService.getPrimaryParent(discussionRef).getParentRef();
        return nodeService.getPrimaryParent(topicRef).getParentRef().getId();
    }

    /**
     * @return the searchService
     */
    public SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
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

    public NodesApi getNodesApi() {
        return nodesApi;
    }

    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }
}
