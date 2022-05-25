/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.sharespace;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.SharedSpaceModel;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.report.ReportDaoService;
import eu.cec.digit.circabc.service.sharespace.ShareSpaceService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.WebClientHelper;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @author Slobodan Filipovic
 */
public class ShareSpaceServiceImpl implements ShareSpaceService {

    private static final String GROUP_CIRCA_CATEGORY_ADMIN =
            "GROUP_" + CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN;

    private static final String LIBRARY = "Library";

    /**
     * Shallow search for nodes with a name pattern
     */
    private static final String XPATH_QUERY_NODE_MATCH = "./*[like(@cm:name, $cm:name, false)]";
    private static final Log logger = LogFactory.getLog(ShareSpaceServiceImpl.class);
    /**
     * The node service reference
     */
    private NodeService nodeService;
    /**
     * The permission service reference
     */
    private PermissionService permissionService;
    /**
     * The management service reference
     */
    private ManagementService managementService;
    private DictionaryService dictionaryService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private ReportDaoService reportDaoService;

    public void inviteInterestGroup(
            final NodeRef shareSpace, final NodeRef interestGroup, final String libraryPermission) {
        validateInviteParameters(shareSpace, interestGroup, libraryPermission);
        createNode(shareSpace, interestGroup, libraryPermission);
        copyPermissions(shareSpace, interestGroup, libraryPermission);
    }

    private void copyPermissions(
            NodeRef shareSpace, NodeRef interestGroup, String libraryPermission) {
        Map<String, String> libPermissionsMap = craetePermissionMap(libraryPermission);

        NodeRef libraryNodeRef =
                nodeService.getChildByName(interestGroup, ContentModel.ASSOC_CONTAINS, LIBRARY);
        Set<AccessPermission> allSetPermissions =
                permissionService.getAllSetPermissions(libraryNodeRef);

        for (AccessPermission permission : allSetPermissions) {
            final String oldPermission = permission.getPermission();
            if (libPermissionsMap.containsKey(oldPermission)) {

                String newPermission = libPermissionsMap.get(oldPermission);
                final String authority = permission.getAuthority();
                if (!authority.contains(GROUP_CIRCA_CATEGORY_ADMIN)
                        && !authority.contains(CircabcRootProfileManagerService.ALL_CIRCA_USERS_AUTHORITY)) {
                    permissionService.setPermission(shareSpace, authority, newPermission, true);
                }
            }
        }
    }

    private Map<String, String> craetePermissionMap(String libraryPermission) {
        Map<String, String> result = new HashMap<>();
        boolean isStrongerPermission = false;
        for (String permission : LibraryPermissions.getOrderedLibraryPermissions()) {
            if (!isStrongerPermission && permission.equalsIgnoreCase(libraryPermission)) {
                isStrongerPermission = true;
            }
            if (isStrongerPermission) {
                result.put(permission, libraryPermission);
            } else {
                result.put(permission, permission);
            }
        }

        return result;
    }

    private void createNode(
            final NodeRef shareSpace, final NodeRef interestGroup, final String libraryPermission) {
        if (!nodeService.hasAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE)) {
            nodeService.addAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE, null);
        }

        List<ChildAssociationRef> childAssocs =
                nodeService.getChildAssocs(
                        shareSpace, SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER, RegexQNamePattern.MATCH_ALL);
        final ChildAssociationRef assocRef;
        if (childAssocs.size() == 0) {
            assocRef =
                    nodeService.createNode(
                            shareSpace,
                            SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
                            SharedSpaceModel.TYPE_CONTAINER,
                            SharedSpaceModel.TYPE_CONTAINER,
                            new PropertyMap());

        } else {
            assocRef = childAssocs.get(0);
        }

        NodeRef container = assocRef.getChildRef();
        PropertyMap properties = new PropertyMap();
        properties.put(SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF, interestGroup);
        properties.put(SharedSpaceModel.PROP_PERMISSION, libraryPermission);

        boolean nodeExists = false;
        List<ChildAssociationRef> igChildAssocs =
                nodeService.getChildAssocs(
                        container, SharedSpaceModel.ASSOC_ITEREST_GROUP, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef ref : igChildAssocs) {
            NodeRef childRef = ref.getChildRef();
            NodeRef igNodeRef =
                    (NodeRef)
                            nodeService.getProperty(childRef, SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF);
            if (igNodeRef.equals(interestGroup)) {
                nodeExists = true;
                break;
            }
        }
        if (nodeExists) {
            throw new IllegalStateException(
                    "Interestgroup (" + interestGroup + ") is already invited to space (" + shareSpace + ")");
        }

        nodeService.createNode(
                container,
                SharedSpaceModel.ASSOC_ITEREST_GROUP,
                SharedSpaceModel.TYPE_INVITED_INTEREST_GROUP,
                SharedSpaceModel.TYPE_INVITED_INTEREST_GROUP,
                properties);
    }

    private void validateInviteParameters(
            final NodeRef shareSpace, final NodeRef interestGroup, final String libraryPermission) {
        ParameterCheck.mandatory("shareSpaceNodeRef is mandatory param", shareSpace);
        ParameterCheck.mandatory("The targetInterestGroupNodeRef is mandatory param ", interestGroup);
        ParameterCheck.mandatory("The libraryPermission is mandatory param ", libraryPermission);

        if (!nodeService.hasAspect(interestGroup, CircabcModel.ASPECT_IGROOT)) {
            throw new IllegalStateException("This node is not ig root (" + interestGroup + ")");
        }

        final NodeRef currentInterestGroup = managementService.getCurrentInterestGroup(shareSpace);
        if (currentInterestGroup.equals(interestGroup)) {
            throw new IllegalStateException(
                    "Shared space("
                            + shareSpace
                            + ") already belong to Interestgroup ("
                            + interestGroup
                            + ")");
        }

        final NodeRef shareSpaceCategory = managementService.getCurrentCategory(shareSpace);
        final NodeRef interestGroupCategory = managementService.getCurrentCategory(interestGroup);
        if (!shareSpaceCategory.equals(interestGroupCategory)) {
            throw new IllegalStateException(
                    "Shared space("
                            + shareSpace
                            + ") and  Interestgroup ("
                            + interestGroup
                            + ") shoul belong to same category");
        }

        if (currentInterestGroup.equals(interestGroup)) {
            throw new IllegalStateException(
                    "Shared space("
                            + shareSpace
                            + ") already belong to Interestgroup ("
                            + interestGroup
                            + ")");
        }
    }

    public void unInviteInterestGroup(final NodeRef shareSpace, final NodeRef interestGroup) {
        validateUnInviteParameters(shareSpace, interestGroup);

        deleteNode(shareSpace, interestGroup);

        removePermissions(shareSpace, interestGroup);
    }

    private void removePermissions(NodeRef shareSpace, NodeRef interestGroup) {
        NodeRef libraryNodeRef =
                nodeService.getChildByName(interestGroup, ContentModel.ASSOC_CONTAINS, LIBRARY);
        Set<AccessPermission> libraryPermissions =
                permissionService.getAllSetPermissions(libraryNodeRef);
        for (AccessPermission permission : libraryPermissions) {
            permissionService.clearPermission(shareSpace, permission.getAuthority());
        }
    }

    private void deleteNode(final NodeRef shareSpace, final NodeRef interestGroup) {
        List<ChildAssociationRef> childAssocs =
                nodeService.getChildAssocs(
                        shareSpace, SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER, RegexQNamePattern.MATCH_ALL);
        final ChildAssociationRef assocRef;
        if (childAssocs.size() == 0) {
            throw new IllegalStateException(
                    "Space is missing association " + SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER);

        } else {
            assocRef = childAssocs.get(0);
        }

        NodeRef container = assocRef.getChildRef();

        List<ChildAssociationRef> igChildAssocs =
                nodeService.getChildAssocs(
                        container, SharedSpaceModel.ASSOC_ITEREST_GROUP, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef ref : igChildAssocs) {
            NodeRef childRef = ref.getChildRef();
            NodeRef igNodeRef =
                    (NodeRef)
                            nodeService.getProperty(childRef, SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF);
            if (igNodeRef.equals(interestGroup)) {
                nodeService.deleteNode(childRef);
                break;
            }
        }
        List<ChildAssociationRef> restChildAssocs =
                nodeService.getChildAssocs(
                        container, SharedSpaceModel.ASSOC_ITEREST_GROUP, RegexQNamePattern.MATCH_ALL);
        if (restChildAssocs.size() == 0) {
            nodeService.deleteNode(container);
            nodeService.removeAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE);
        }
    }

    private void validateUnInviteParameters(
            final NodeRef shareSpaceNodeRef, final NodeRef interestGroupNodeRef) {
        ParameterCheck.mandatory("shareSpaceNodeRef is mandatory param", shareSpaceNodeRef);
        ParameterCheck.mandatory(
                "The targetInterestGroupNodeRef is mandatory param ", interestGroupNodeRef);

        if (!nodeService.hasAspect(shareSpaceNodeRef, CircabcModel.ASPECT_SHARED_SPACE)) {
            throw new IllegalStateException(
                    "Space is missing aspect " + CircabcModel.ASPECT_SHARED_SPACE);
        }
    }

    public NodeRef linkSharedSpace(NodeRef currentSpace, NodeRef sharedSpace, String name) {
        if (checkExists(name, currentSpace)) {
            throw new IllegalStateException("Name should be unique " + name);
        }
        ChildAssociationRef assocRef = nodeService.getPrimaryParent(sharedSpace);
        PropertyMap props = new PropertyMap(2, 1.0f);
        props.put(ContentModel.PROP_NAME, name);
        props.put(ContentModel.PROP_LINK_DESTINATION, sharedSpace);
        //		 create Folder link node
        ChildAssociationRef childRef =
                nodeService.createNode(
                        currentSpace,
                        ContentModel.ASSOC_CONTAINS,
                        assocRef.getQName(),
                        ApplicationModel.TYPE_FOLDERLINK,
                        props);

        return childRef.getChildRef();
    }

    private boolean checkExists(String name, NodeRef parent) {
        QueryParameterDefinition[] params = new QueryParameterDefinition[1];
        params[0] =
                new QueryParameterDefImpl(
                        ContentModel.PROP_NAME,
                        dictionaryService.getDataType(DataTypeDefinition.TEXT),
                        true,
                        name);

        // execute the query
        List<NodeRef> nodeRefs =
                searchService.selectNodes(parent, XPATH_QUERY_NODE_MATCH, params, namespaceService, false);

        return (nodeRefs.size() != 0);
    }

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the dictionaryService
     */
    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
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

    /**
     * @return the namespaceService
     */
    public NamespaceService getNamespaceService() {
        return namespaceService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public List<Pair<NodeRef, String>> getInvitedInterestGroups(NodeRef shareSpace) {
        ArrayList<Pair<NodeRef, String>> result = new ArrayList<>();

        List<ChildAssociationRef> childAssocs =
                nodeService.getChildAssocs(
                        shareSpace, SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER, RegexQNamePattern.MATCH_ALL);
        final ChildAssociationRef assocRef;
        if (childAssocs.size() == 0) {
            return result;

        } else {
            assocRef = childAssocs.get(0);
        }

        NodeRef container = assocRef.getChildRef();

        List<ChildAssociationRef> igChildAssocs =
                nodeService.getChildAssocs(
                        container, SharedSpaceModel.ASSOC_ITEREST_GROUP, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef ref : igChildAssocs) {
            NodeRef childRef = ref.getChildRef();
            final NodeRef igNodeRef =
                    (NodeRef)
                            nodeService.getProperty(childRef, SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF);

            if ((igNodeRef != null) && nodeService.exists(igNodeRef)) {
                final String permission =
                        (String) nodeService.getProperty(childRef, SharedSpaceModel.PROP_PERMISSION);
                result.add(new Pair<>(igNodeRef, permission));
            }
        }
        return result;
    }

    public List<NodeRef> getAvailableInterestGroups(NodeRef shareSpace) {
        final NodeRef interestGroup = managementService.getCurrentInterestGroup(shareSpace);
        final NodeRef shareSpaceCategory = managementService.getCurrentCategory(interestGroup);

        List<Pair<NodeRef, String>> invitedIgNodes = getInvitedInterestGroups(shareSpace);
        List<NodeRef> candidates = getAllIgNodes(shareSpaceCategory);
        candidates.remove(interestGroup);
        for (final Pair<NodeRef, String> invited : invitedIgNodes) {
            candidates.remove(invited.getFirst());
        }

        return candidates;
    }

    private List<NodeRef> getAllIgNodes(NodeRef categoryNode) {
        List<NodeRef> result = new ArrayList<>();

        List<ChildAssociationRef> childAssocs =
                nodeService.getChildAssocs(
                        categoryNode, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

        if (childAssocs != null) {
            List<ChildAssociationRef> igChildAssocs =
                    nodeService.getChildAssocs(
                            categoryNode, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef ref : igChildAssocs) {
                NodeRef childRef = ref.getChildRef();
                result.add(childRef);
            }
        }
        return result;
    }

    public List<NodeRef> getAvailableShareSpaces(NodeRef space) {
        final NodeRef interestGroup = managementService.getCurrentInterestGroup(space);
        final NodeRef interestGroupCategory = managementService.getCurrentCategory(interestGroup);
        List<NodeRef> result = new ArrayList<>();
        ResultSet resultSet = null;
        final boolean useLucene = false;
        try {
            if (useLucene) {
                String luceneQuery =
                        buildLuceneQueryAvailableShareSpaces(interestGroup, interestGroupCategory);
                resultSet = executeLuceneQuery(luceneQuery);
                for (final ResultSetRow row : resultSet) {
                    final NodeRef nodeRef = row.getNodeRef();
                    final ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
                    final ChildAssociationRef parentOfParent =
                            nodeService.getPrimaryParent(primaryParent.getParentRef());
                    final NodeRef shareSpaceRef = parentOfParent.getParentRef();
                    if (nodeService.hasAspect(shareSpaceRef, CircabcModel.ASPECT_SHARED_SPACE)) {
                        result.add(shareSpaceRef);
                    }
                }
            } else {

                final List<NodeRef> availibleShareSpaces =
                        reportDaoService.getAvailibleShareSpaces(interestGroup);
                for (final NodeRef nodeRef : availibleShareSpaces) {
                    final ChildAssociationRef primaryParent = nodeService.getPrimaryParent(nodeRef);
                    final ChildAssociationRef parentOfParent =
                            nodeService.getPrimaryParent(primaryParent.getParentRef());
                    final NodeRef shareSpaceRef = parentOfParent.getParentRef();
                    if (nodeService.hasAspect(shareSpaceRef, CircabcModel.ASPECT_SHARED_SPACE)) {
                        result.add(shareSpaceRef);
                    }
                }
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return result;
    }

    private String buildLuceneQueryAvailableShareSpaces(
            NodeRef interestGroup, NodeRef interestGroupCategory) {
        String query =
                "( PATH:\""
                        + WebClientHelper.getPathFromSpaceRef(interestGroupCategory, true)
                        + "\" ) "
                        + "AND"
                        + " ( TYPE: \"ss:invitedInterestGroup\"  ) "
                        + "AND"
                        + " ( @ss\\:ignoderef:"
                        + "\""
                        + interestGroup.toString()
                        + "\""
                        + ")";

        return query;
    }

    private String buildLuceneQueryInvitedIG(NodeRef interestGroup) {
        String query =
                "((PATH:\""
                        + WebClientHelper.getPathFromSpaceRef(interestGroup, true)
                        + "\") AND (ASPECT: \"ci:circabcSharedSpace\"))";

        return query;
    }

    private ResultSet executeLuceneQuery(final String query) {
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        return searchService.query(sp);
    }

    public List<NodeRef> getAllSharedSpaceInInterestGroup(NodeRef interestGroup) {
        List<NodeRef> result = new ArrayList<>();
        String luceneQuery = buildLuceneQueryInvitedIG(interestGroup);
        ResultSet resultSet = null;
        try {
            resultSet = executeLuceneQuery(luceneQuery);
            for (final ResultSetRow row : resultSet) {
                result.add(row.getNodeRef());
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return result;
    }

    public ReportDaoService getReportDaoService() {
        return reportDaoService;
    }

    public void setReportDaoService(ReportDaoService reportDaoService) {
        this.reportDaoService = reportDaoService;
    }
}
