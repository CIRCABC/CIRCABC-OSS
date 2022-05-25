package io.swagger.api;

import eu.cec.digit.circabc.business.api.link.InterestGroupItem;
import eu.cec.digit.circabc.business.api.link.LinksBusinessSrv;
import eu.cec.digit.circabc.business.api.link.ShareSpaceItem;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.model.SharedSpaceModel;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.action.executer.ExecuteAllRulesActionExecuter;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.Pair;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

/**
 * @author beaurpi
 */
public class SpacesApiImpl implements SpacesApi {

    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(SpacesApiImpl.class);

    private static final String EXPIRATION_DATE = "expiration_date";
    private static final String PATTERN_HTML_EXTENSION = ".*\\.htm(l)?";
    private static final String HTML = ".html";
    private static final String FIRSTNAME_REGEX = "<USER_FIRST_NAME>";
    private static final String LASTTNAME_REGEX = "<USER_LAST_NAME>";
    private static final String KEY_PROFILE = "profile";
    private static final String KEY_YOUR_IG = "yourinterestGroup";
    private NodesApi nodesApi;
    private EmailApi emailApi;
    private NodeService secureNodeService;
    private NodeService unsecureNodeService;
    private FileFolderService fileFolderService;
    private ManagementService managementService;
    private LinksBusinessSrv linksBusinessSrv;
    private RuleService ruleService;
    private PermissionService permissionService;
    private DictionaryService dictionaryService;
    private PersonService personService;
    private ActionService actionService;
    private AuthorityService authorityService;
    private SearchService secureSearchService;
    private ApiToolBox apiToolBox;

    @Override
    public List<Node> spaceGetChildren(String id, boolean folderOnly) {
        List<Node> result = new ArrayList<>();
        NodeRef spaceNodeRef = Converter.createNodeRefFromId(id);
        if (secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_LIBRARY)
                || secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_INFORMATION)) {
            // use FileFolderService because it filter ML documents

            List<FileInfo> list;

            if (folderOnly) {
                list = getFileFolderService().listFolders(spaceNodeRef);
            } else {
                list = getFileFolderService().list(spaceNodeRef);
            }

            for (FileInfo item : list) {
                final NodeRef childRef = item.getNodeRef();
                result.add(nodesApi.getNode(childRef));
            }
        }
        return result;
    }

    @Override
    public PagedNodes spaceGetChildren(
            String id, int nbPage, int nbLimit, String sort, boolean folderOnly, boolean fileOnly) {

        PagedNodes pagedResult = new PagedNodes();

        List<Node> result = new ArrayList<>();
        NodeRef spaceNodeRef = Converter.createNodeRefFromId(id);
        if (secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_LIBRARY)
                || secureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_INFORMATION)) {
            // use FileFolderService because it filter ML documents

            PagingRequest pr = new PagingRequest(0, 100000);
            List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

            Pair<QName, Boolean> sortFolderFirstPair =
                    new Pair<>(GetChildrenCannedQuery.SORT_QNAME_NODE_IS_FOLDER, false);

            sortProps.add(sortFolderFirstPair);

            //BUG DIGITCIRCABC-4900
            if (!"".contentEquals(sort)) {
                String localName = sort.replace("_DESC", "").replace("_ASC", "");
                String namespace = NamespaceService.CONTENT_MODEL_1_0_URI;
                if (localName.equals("security_ranking") ||
                        localName.equals(EXPIRATION_DATE) || 
                        localName.equals("status")) {
                    namespace = DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI;
                }
                Pair<QName, Boolean> sortPair = new Pair<>(
                        QName.createQName(namespace,
                                localName),
                        (sort.endsWith("ASC")));
                sortProps.add(sortPair);
            }

            FileFilterMode.setClient(Client.cmis);
            final PagingResults<FileInfo> list =
                    getFileFolderService().list(spaceNodeRef, fileOnly, folderOnly, null, sortProps, pr);
            FileFilterMode.clearClient();
            List<FileInfo> items = list.getPage();

            int iStart = 0;
            int iEnd = items.size();

            if (nbLimit > 0 && nbPage >= 0) {
                iStart = nbPage * nbLimit;
                iEnd = (Math.min(iStart + nbLimit, items.size()));
            }

            for (int i = iStart; i < iEnd; i++) {
                final NodeRef childRef = items.get(i).getNodeRef();
                result.add(nodesApi.getNode(childRef));
            }

            pagedResult.setTotal((long) items.size());
            pagedResult.setData(result);
        }

        return pagedResult;
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

    /**
     * @return the secureNodeService
     */
    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    /**
     * @param secureNodeService the secureNodeService to set
     */
    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    /**
     * @return the fileFolderService
     */
    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    @Override
    public Node spacesIdSpacesPost(String id, Node body) {

        NodeRef parentRef = Converter.createNodeRefFromId(id);

        if (!secureNodeService.getType(parentRef).equals(ContentModel.TYPE_FOLDER)) {
            throw new InvalidTypeException(
                    "Node creation failed, the parent noderef does not have the type folder",
                    ContentModel.TYPE_FOLDER);
        }

        QName nameQName =
                QName.createQName(
                        NamespaceService.CONTENT_MODEL_1_0_URI,
                        QName.createValidLocalName(body.getName().trim()));

        ChildAssociationRef newSpace =
                secureNodeService.createNode(
                        parentRef, ContentModel.ASSOC_CONTAINS, nameQName, ContentModel.TYPE_FOLDER);
        NodeRef nodeRef = newSpace.getChildRef();
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_NAME, body.getName().trim());
        MLText titles = Converter.toMLText(body.getTitle());
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, titles);

        MLText descriptions = Converter.toMLText(body.getDescription());
        secureNodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, descriptions);

        return nodesApi.getNode(nodeRef);
    }

    @Override
    public void spaceDelete(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (!secureNodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)
                && !secureNodeService.getType(nodeRef).equals(ApplicationModel.TYPE_FOLDERLINK)) {
            throw new InvalidTypeException(
                    "Node deletion failed, noderef does not have the type folder", ContentModel.TYPE_FOLDER);
        }

        NodeRef igRoot = managementService.getCurrentInterestGroup(nodeRef);
        secureNodeService.setProperty(
                nodeRef, CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED, igRoot.getId());

        secureNodeService.deleteNode(nodeRef);
    }

    @Override
    public void spacesIdPut(String id, Node body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        this.secureNodeService.setProperty(nodeRef, ContentModel.PROP_NAME, body.getName().trim());
        this.secureNodeService.setProperty(
                nodeRef, ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));
        this.secureNodeService.setProperty(
                nodeRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(body.getDescription()));

        try {
            if (body.getProperties().get(EXPIRATION_DATE) != null
                    && !body.getProperties().get(EXPIRATION_DATE).equals("")) {
                this.secureNodeService.setProperty(
                        nodeRef,
                        DocumentModel.PROP_EXPIRATION_DATE,
                        Converter.convertStringToDate(body.getProperties().get(EXPIRATION_DATE)));
            }
        } catch (ParseException e) {
            logger.error("Invalid expiration date:" + body.getProperties().get(EXPIRATION_DATE), e);
        }
    }

    @Override
    public Node spacesIdUrlPost(String id, Node body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        NodeRef createdNode = null;

        if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
            String name = body.getName();
            name = name.replace(" ", "_");

            if (!name.matches(PATTERN_HTML_EXTENSION)) {
                name = name + HTML;
            }

            name = nodesApi.generateUniqueName(nodeRef, name);

            final FileInfo fileInfo =
                    getFileFolderService().create(nodeRef, name, ContentModel.TYPE_CONTENT);
            createdNode = fileInfo.getNodeRef();

            secureNodeService.addAspect(createdNode, DocumentModel.ASPECT_URLABLE, null);
            secureNodeService.setProperty(
                    createdNode, DocumentModel.PROP_URL, body.getProperties().get("url"));
        }

        return nodesApi.getNode(createdNode);
    }

    /**
     * @see io.swagger.api.SpacesApi#getInvitedInterestGroups(java.lang.String, int, int)
     */
    @Override
    public PagedShares getInvitedInterestGroups(String spaceId, int startItem, int limit) {

        List<Pair<NodeRef, String>> listOfIGs = getInvitedInterestGroups(spaceId);

        int resultSize = listOfIGs.size();

        List<Share> shares = new ArrayList<>();

        int endItem = Math.min(startItem + limit, resultSize);

        if (limit == 0) {
            // amount == 0 means that we want all items
            startItem = 0;
            endItem = resultSize;
        }

        for (int index = startItem; index < endItem; index++) {

            Pair<NodeRef, String> invitedIG = listOfIGs.get(index);

            String igName =
                    (String) secureNodeService.getProperty(invitedIG.getFirst(), ContentModel.PROP_NAME);

            shares.add(new Share(invitedIG.getFirst().getId(), igName, invitedIG.getSecond()));
        }

        return new PagedShares(shares, resultSize);
    }

    private List<Pair<NodeRef, String>> getInvitedInterestGroups(String spaceId) {

        NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

        ArrayList<Pair<NodeRef, String>> result = new ArrayList<>();

        List<ChildAssociationRef> childAssocs =
                secureNodeService.getChildAssocs(
                        shareSpace, SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER, RegexQNamePattern.MATCH_ALL);

        final ChildAssociationRef assocRef;

        if (childAssocs.isEmpty()) {
            return result;
        } else {
            assocRef = childAssocs.get(0);
        }

        NodeRef container = assocRef.getChildRef();

        List<ChildAssociationRef> igChildAssocs =
                secureNodeService.getChildAssocs(
                        container, SharedSpaceModel.ASSOC_ITEREST_GROUP, RegexQNamePattern.MATCH_ALL);

        for (ChildAssociationRef ref : igChildAssocs) {

            NodeRef childRef = ref.getChildRef();

            final NodeRef igNodeRef =
                    (NodeRef)
                            secureNodeService.getProperty(
                                    childRef, SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF);

            if ((igNodeRef != null) && secureNodeService.exists(igNodeRef)) {
                final String permission =
                        (String) secureNodeService.getProperty(childRef, SharedSpaceModel.PROP_PERMISSION);
                result.add(new Pair<>(igNodeRef, permission));
            }
        }

        return result;
    }

    /**
     * @see io.swagger.api.SpacesApi#changeSharePermission(java.lang.String, java.lang.String,
     * java.lang.String, boolean)
     */
    @Override
    public void changeSharePermission(
            String sharedSpaceId, String igId, String newPermission, boolean notifyLeaders) {

        deleteShare(sharedSpaceId, igId);

        Share share = new Share(igId, null, newPermission);
        addShare(sharedSpaceId, share, notifyLeaders, true);
    }

    /**
     * @see io.swagger.api.SpacesApi#deleteShare(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteShare(String spaceId, String sharedIGId) {

        NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);
        NodeRef interestGroup = Converter.createNodeRefFromId(sharedIGId);

        linksBusinessSrv.removeSharing(shareSpace, interestGroup);
    }

    /**
     * @see io.swagger.api.SpacesApi#addShare(java.lang.String, io.swagger.model.Share, boolean)
     */
    @Override
    public void addShare(String spaceId, Share share, boolean notifyLeaders) {
        addShare(spaceId, share, notifyLeaders, false);
    }

    private void addShare(
            String spaceId, Share share, boolean notifyLeaders, boolean permissionUpdate) {

        NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);
        NodeRef igNodeRef = Converter.createNodeRefFromId(share.getIgId());

        boolean inheritParentPermissions = permissionService.getInheritParentPermissions(shareSpace);

        if (!inheritParentPermissions) {
            ruleService.disableRules();
        }

        final LibraryPermissions libraryPermissions =
                LibraryPermissions.withPermissionString(share.getPermission());
        linksBusinessSrv.applySharing(shareSpace, igNodeRef, libraryPermissions);

        if (notifyLeaders) {
            notifyLeaders(share, shareSpace, igNodeRef, permissionUpdate);
        }

        if (!inheritParentPermissions) {
            ruleService.enableRules(shareSpace);
            reapplyRules(shareSpace, true, true);
        }
    }

    private void notifyLeaders(
            Share share, NodeRef shareSpace, NodeRef igNodeRef, boolean permissionUpdate) {

        // get all lib admins
        String groupName =
                (String)
                        secureNodeService.getProperty(igNodeRef, CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP);
        Set<String> libraryGroups =
                authorityService.getContainedAuthorities(AuthorityType.GROUP, "GROUP_" + groupName, true);

        String libAdminGroup = null;

        for (String libraryGroup : libraryGroups) {
            // added || libraryGroup.contains("Leader") to notify Leaders (originally it was
            // not there)
            if (libraryGroup.contains("LibAdmin") || libraryGroup.contains("Leader")) {
                libAdminGroup = libraryGroup;
                break;
            }
        }

        if (libAdminGroup != null) {

            Set<String> libAdmins =
                    authorityService.getContainedAuthorities(AuthorityType.USER, libAdminGroup, false);

            final Map<String, Object> extraModelParams = buildModelParam(share);

            for (final String libAdmin : libAdmins) {

                final NodeRef person = personService.getPerson(libAdmin);

                emailApi.mailToUser(
                        person,
                        shareSpace,
                        null,
                        extraModelParams,
                        buildBodyParams(person),
                        true,
                        null,
                        permissionUpdate
                                ? MailTemplate.SHARE_SPACE_PERMISSION_UPDATE_NOTIFICATION
                                : MailTemplate.SHARE_SPACE_NOTIFICATION);
            }
        }
    }

    private void reapplyRules(NodeRef space, boolean executeInherited, boolean toChildren) {

        // Create the the apply rules action
        Action action = actionService.createAction(ExecuteAllRulesActionExecuter.NAME);
        action.setParameterValue(
                ExecuteAllRulesActionExecuter.PARAM_EXECUTE_INHERITED_RULES, executeInherited);

        // Execute the action
        actionService.executeAction(action, space);

        if (toChildren) {
            List<ChildAssociationRef> assocs =
                    secureNodeService.getChildAssocs(
                            space, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs) {
                NodeRef nodeRef = assoc.getChildRef();
                QName className = secureNodeService.getType(nodeRef);
                if (dictionaryService.isSubClass(className, ContentModel.TYPE_FOLDER)) {
                    reapplyRules(nodeRef, executeInherited, true);
                }
            }
        }
    }

    private Map<String, Object> buildModelParam(Share share) {

        final Map<String, Object> params = new HashMap<>(2);
        if (share.getIgId() != null) {
            NodeRef igNodeRef = Converter.createNodeRefFromId(share.getIgId());
            params.put(KEY_YOUR_IG, igNodeRef);
        }
        params.put(KEY_PROFILE, share.getPermission());
        params.put(MailTemplate.KEY_COMPANY_HOME, managementService.getCompanyHomeNodeRef());
        params.put(MailTemplate.KEY_CIRCABC, managementService.getCircabcNodeRef());
        CircabcConfiguration.addApplicationNameToModel(params);

        return params;
    }

    private Map<String, String> buildBodyParams(final NodeRef person) {

        final Map<QName, Serializable> personProperties = secureNodeService.getProperties(person);
        final Map<String, String> params = new HashMap<>(2);
        params.put(FIRSTNAME_REGEX, (String) personProperties.get(ContentModel.PROP_FIRSTNAME));
        params.put(LASTTNAME_REGEX, (String) personProperties.get(ContentModel.PROP_LASTNAME));

        return params;
    }

    /**
     * @see io.swagger.api.SpacesApi#getShareIGsAndPermissions(java.lang.String)
     */
    @Override
    public ShareIGsAndPermissions getShareIGsAndPermissions(String spaceId) {

        NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

        List<Pair<String, String>> igs = new ArrayList<>();

        final List<InterestGroupItem> interestGroups =
                linksBusinessSrv.getInterestGroupForSharing(shareSpace);
        for (final InterestGroupItem item : interestGroups) {
            igs.add(new Pair<String,String>(item.getTitle(), item.getNodeRef().getId()));
        }

        List<String> permissions = new ArrayList<>();

        final List<String> perms = LibraryPermissions.getOrderedLibraryPermissions();
        for (final String perm : perms) {
            if (perm.equalsIgnoreCase("LibNoAccess")) {
                continue;
            }
            permissions.add(perm);
        }

        return new ShareIGsAndPermissions(igs, permissions);
    }

    /**
     * @see io.swagger.api.SpacesApi#getAvailableSharedSpaces(java.lang.String)
     */
    @Override
    public List<ShareSpaceItem> getAvailableSharedSpaces(String spaceId) {

        NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

        return linksBusinessSrv.getAvailableSharedSpaces(shareSpace);
    }

    /**
     * @see io.swagger.api.SpacesApi#createSharedSpaceLink(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void createSharedSpaceLink(
            String spaceId, String parentId, String title, String description) {

        NodeRef shareSpace = Converter.createNodeRefFromId(spaceId);

        NodeRef parentSpace = Converter.createNodeRefFromId(parentId);

        linksBusinessSrv.createSharedSpaceLink(parentSpace, shareSpace, title, description);
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param linksBusinessSrv the linksBusinessSrv to set
     */
    public void setLinksBusinessSrv(LinksBusinessSrv linksBusinessSrv) {
        this.linksBusinessSrv = linksBusinessSrv;
    }

    /**
     * @param ruleService the ruleService to set
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param actionService the actionService to set
     */
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param emailApi the emailApi to set
     */
    public void setEmailApi(EmailApi emailApi) {
        this.emailApi = emailApi;
    }

    @Override
    public PagedNodes restrictedSpaceGetChildren(
            String id, int nbPage, int nbLimit, String sort, boolean folderOnly, boolean fileOnly) {
        PagedNodes pagedResult = new PagedNodes();

        List<Node> result = new ArrayList<>();
        NodeRef spaceNodeRef = Converter.createNodeRefFromId(id);
        if (unsecureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_LIBRARY)
                || unsecureNodeService.hasAspect(spaceNodeRef, CircabcModel.ASPECT_INFORMATION)) {
            // use FileFolderService because it filter ML documents

            StringBuilder queryBuilder = new StringBuilder();

            if (id != null) {
                NodeRef targetRef = Converter.createNodeRefFromId(id);
                String path = "(PATH:\"" + apiToolBox.getPathFromSpaceRef(targetRef, true) + "\")";
                queryBuilder.append(path);
            }

            queryBuilder.append(" AND ");
            queryBuilder.append(" (TYPE:\"cm:folder\" ");
            queryBuilder.append(" OR ");
            queryBuilder.append(" TYPE:\"cm:content\" )");

            SearchParameters searchParameters = new SearchParameters();

            if (nbLimit != -1) {
                searchParameters.setLimit(nbLimit);
                searchParameters.setSkipCount(nbPage * nbLimit);
            }

            searchParameters.setMaxItems(-1);
            searchParameters.setQuery(queryBuilder.toString());
            searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
            searchParameters.addStore(Repository.getStoreRef());

            if (!sort.equalsIgnoreCase("")) {
                searchParameters.addSort(sort.split("_")[0], sort.endsWith("ASC"));
            }

            FileFilterMode.setClient(Client.cmis);

            ResultSet rs = secureSearchService.query(searchParameters);

            for (NodeRef item : rs.getNodeRefs()) {
                result.add(nodesApi.getNode(item));
            }

            FileFilterMode.clearClient();

            pagedResult.setTotal(rs.getNumberFound());
            pagedResult.setData(result);
        }

        return pagedResult;
    }

    public SearchService getSecureSearchService() {
        return secureSearchService;
    }

    public void setSecureSearchService(SearchService secureSearchService) {
        this.secureSearchService = secureSearchService;
    }

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    public NodeService getUnsecureNodeService() {
        return unsecureNodeService;
    }

    public void setUnsecureNodeService(NodeService unsecureNodeService) {
        this.unsecureNodeService = unsecureNodeService;
    }

	@Override
	public int getFolderSize(String id) {
		int totalSize = 0;
		List<Node> childNodes = spaceGetChildren(id, false);

		for (Node child : childNodes) {
			if (child.getType().indexOf("folder") != -1) {
				totalSize += getFolderSize(child.getId());
			} else {
				totalSize += Integer.parseInt(child.getProperties().get("size"));
			}
		}

		return totalSize;
	}
}
