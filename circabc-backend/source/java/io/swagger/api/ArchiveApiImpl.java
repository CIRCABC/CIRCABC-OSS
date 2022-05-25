package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import io.swagger.model.ArchiveNode;
import io.swagger.model.PagedArchiveNodes;
import io.swagger.model.RestoreNodeMetadata;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.web.bean.repository.Repository;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beaurpi
 */
public class ArchiveApiImpl implements ArchiveApi {

    private static final String PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR =
            Repository.escapeQName(CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED);
    private static final String SEARCH_ALL =
            "PARENT:\"%s\" AND ASPECT:\"%s\""
                    + "  AND @"
                    + PROP_IG_ROOT_NODE_ID_ARCHIVED_ATTR
                    + ":\"%s\"";
    private NodeArchiveService nodeArchiveService;
    private SearchService internalSearchService;
    private NodesApi nodesApi;
    private NodeService nodeService;

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.ArchiveApi#groupsIdDocumentsDeletedGet(java.lang.String, java.lang.Integer,
     * java.lang.Integer, java.lang.String)
     */
    @Override
    public PagedArchiveNodes groupsIdDocumentsDeletedGet(
            String id, Integer limit, Integer page, String order) {
        PagedArchiveNodes result = new PagedArchiveNodes();
        NodeRef archiveRootNode = nodeArchiveService.getStoreArchiveNode(Repository.getStoreRef());

        String username = AuthenticationUtil.getRunAsUser();
        AuthenticationUtil.setRunAsUserSystem();

        final String query = buildSearchQuery(id, archiveRootNode);
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE); // the Archived
        // Node store

        if (!order.equalsIgnoreCase("")) {
            if (order.contains("name")) {
                sp.addSort("@cm:" + order.split("_")[0], order.endsWith("ASC"));
            } else if (order.contains("archived")) {
                sp.addSort("@sys:" + order.split("_")[0], order.endsWith("ASC"));
            }
        }

        if (limit > 0) {
            sp.setMaxItems(limit);
        }

        if (limit > 0 && page > 1) {
            sp.setSkipCount(limit * (page - 1));
        }

        ResultSet resultSet = internalSearchService.query(sp);

        result.setTotal(resultSet.getNumberFound());

        List<ArchiveNode> nodes = new ArrayList<>();

        for (final ResultSetRow row : resultSet) {
            NodeRef nodeRef = row.getNodeRef();
            ArchiveNode aNode = new ArchiveNode();
            aNode.mergeNode(nodesApi.getNode(nodeRef));
            aNode.setDeletedBy(
                    nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_BY).toString());
            DateTime dt = new DateTime(nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_DATE));
            aNode.setDeletedDate(dt);
            nodes.add(aNode);
        }

        AuthenticationUtil.setRunAsUser(username);

        result.setData(nodes);

        return result;
    }

    private String buildSearchQuery(String id, NodeRef archiveRootNode) {
        String query;
        query = String.format(SEARCH_ALL, archiveRootNode, ContentModel.ASPECT_ARCHIVED, id);

        return query;
    }

    public NodeArchiveService getNodeArchiveService() {
        return nodeArchiveService;
    }

    public void setNodeArchiveService(NodeArchiveService nodeArchiveService) {
        this.nodeArchiveService = nodeArchiveService;
    }

    /**
     * @return the internalSearchService
     */
    public SearchService getInternalSearchService() {
        return internalSearchService;
    }

    /**
     * @param internalSearchService the internalSearchService to set
     */
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

    @Override
    public void groupsIdDocumentsDeletedPost(String id, RestoreNodeMetadata restoreNodeMetadata) {
        if (!"".equals(restoreNodeMetadata.getArchiveNodeId())) {

            String userName = AuthenticationUtil.getRunAsUser();

            AuthenticationUtil.setRunAsUserSystem();

            NodeRef nodeRef =
                    Converter.createArchiveNodeRefFromId(restoreNodeMetadata.getArchiveNodeId());

            if (!"".equals(restoreNodeMetadata.getTargetFolderId())) {
                NodeRef spaceRef = Converter.createNodeRefFromId(restoreNodeMetadata.getTargetFolderId());
                nodeArchiveService.restoreArchivedNode(nodeRef, spaceRef, null, null);
            } else {
                nodeArchiveService.restoreArchivedNode(nodeRef);
            }

            AuthenticationUtil.setRunAsUser(userName);
        }
    }

    @Override
    public void groupsIdDocumentsDeletedNodeIdDelete(String id, String nodeId) {

        String userName = AuthenticationUtil.getRunAsUser();

        AuthenticationUtil.setRunAsUserSystem();

        NodeRef nodeRef = Converter.createArchiveNodeRefFromId(nodeId);

        nodeArchiveService.purgeArchivedNode(nodeRef);

        AuthenticationUtil.setRunAsUser(userName);
    }
}
