package io.swagger.api;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.repo.app.CircabcDaoServiceImpl;
import eu.cec.digit.circabc.repo.app.model.ExportedProfileItem;
import eu.cec.digit.circabc.repo.app.model.InterestGroupItem;
import eu.cec.digit.circabc.repo.statistics.ReportFile;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.category.group.requests.GroupRequestsDaoService;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.statistics.global.GlobalStatisticsService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;

import javax.transaction.UserTransaction;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static eu.cec.digit.circabc.model.KeywordModel.TYPE_KEYWORD_CONTAINER;

/**
 * @author beaurpi
 */
public class CategoriesApiImpl implements CategoriesApi {

    private static final String LOGO_CONTAINER = "logoContainer";
    private static final String SPACE_ICON_DEFAULT = "space-icon-default";
    private static final String ADMIN = "admin";
    private static final String AUTHOR = "Author";
    private static final String GUEST = "guest";
    private static final String EVERYONE = "EVERYONE";
    private static final String ACCESS = "Access";
    private static final String LEADER = "Leader";
    private static final String NEWSGROUPS = "newsgroups";
    private static final String MEMBERS = "members";
    private static final String EVENTS = "events";
    private static final String LIBRARY = "library";
    private static final String INFORMATION = "information";

    private static final Log logger = LogFactory.getLog(CategoriesApiImpl.class);

    private static final QName PROP_CIRCABC_PROFILE_GROUP_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaBCProfileGroupName");
    private static final QName PROP_CIRCABC_PROFILE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaBCProfileName");

    public static final String CIRCA_CATEGORY_ADMIN = "CircaCategoryAdmin";
    private static final QName CATEGORY_ADMIN_PROFILE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, CIRCA_CATEGORY_ADMIN);
    private static final QName ALL_CIRCA_USERS_PROFILE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, EVERYONE);
    private static final QName GUEST_PROFILE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, GUEST);

    private static final QName QNAME_CATEGORY_ADMIN_SERVICE = QName.createQName("CATEGORY");

    private static final QName CATEGORY_PROFILE_GROUP_NAME =
            QName.createQName(
                    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryProfileGroupName");
    private static final QName CATEGORY_PROFILE_PROP_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryProfileName");

    private static final QName ASSOC_CATEGORY_SERVICE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryServiceAssoc");
    private static final QName TYPE_CATEGORY_SERVICE =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryService");

    private static final QName PROP_CATEGORY_ADMIN_SERVICE_NAME =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryServiceName");
    private static final QName PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategoryPermissionSet");

    private static final QName PROP_CIRCABC_SUBGROUP =
            QName.createQName("http://www.cc.cec/circabc/model/content/1.0", "circaBCSubsGroup");

    private static final QName LOGO_CONTAINER_QNAME = QName.createQName(LOGO_CONTAINER);
    private static final String NO_VISIBILITY = "NoVisibility";
    private static final String GROUP_EVERYONE = "GROUP_EVERYONE";
    private static final String ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION = "Add CircabcNotify Aspect Description";
    private static final String CATEG_STAT_JOB = "categStatJob-";
    private static final String CIRCA_CATEGORY_ACCESS = "CircaCategoryAccess";
    private static final String ADD_CIRCABC_NOTIFY_ASPECT = "Add CircabcNotify Aspect";

    private NodeService nodeService;
    private NodeService secureNodeService;
    private NodeLocatorService nodeLocatorService;
    private CircabcDaoServiceImpl circabcDaoServiceImpl;
    private CircabcService circabcService;
    private GlobalStatisticsService globalStatisticsService;
    private LockService circabcLockService;
    private ThreadPoolExecutor asyncThreadPoolExecutor;
    private PersonService personService;
    private LdapUserService ldapUserService;
    private OwnableService ownableService;
    private AuthorityService authorityService;
    private ActionService actionService;
    private RuleService ruleService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private UserService userService;
    private ManagementService managementService;
    private GroupRequestsDaoService groupRequestsDaoService;

    private GroupsApi groupsApi;
    private ProfilesApi profilesApi;
    private UsersApi usersApi;
    private NodesApi nodesApi;
    private EmailApi emailApi;

    private NodeRef circabcRoot;
    private ServiceRegistry serviceRegistry;

    public List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        if (circabcRoot == null) {
            setCircabcRoot();
        }
        setCategories(categories);
        return categories;
    }

    private void setCategories(List<Category> categories) {
        List<ChildAssociationRef> children = nodeService.getChildAssocs(circabcRoot);
        for (ChildAssociationRef child : children) {
            if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_CATEGORY)) {
                Category category = new Category();
                final NodeRef childRef = child.getChildRef();
                category.setId(childRef.getId());
                category.setName((String) nodeService.getProperty(childRef, ContentModel.PROP_NAME));
                final Serializable property =
                        nodeService.getProperty(child.getChildRef(), ContentModel.PROP_TITLE);
                if (property instanceof MLText) {
                    category.setTitle(Converter.toI18NProperty((MLText) property));
                } else if (property instanceof String) {
                    category.setTitle(Converter.toI18NProperty((String) property));
                }
                categories.add(category);
            }
        }
    }

    private void setCircabcRoot() {

        final NodeRef companyHome = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
        List<ChildAssociationRef> children = nodeService.getChildAssocs(companyHome);
        for (ChildAssociationRef child : children) {
            if (nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).equals("CircaBC")) {
                circabcRoot = child.getChildRef();
            }
        }
    }

    @Override
    public List<InterestGroup> getInterestGroupByCategoryId(String id) {
        List<InterestGroup> result = new ArrayList<>();
        NodeRef categoryNodeRef = Converter.createNodeRefFromId(id);
        if (!isCategory(categoryNodeRef)) {
            return result;
        }
        if (circabcService.readFromDatabase()) {
            String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            User user = usersApi.usersUserIdGet(userName);

            List<InterestGroupItem> interestGroups =
                    circabcService.getInterestGroupByCategoryUser(categoryNodeRef, userName);
            for (InterestGroupItem interestGroupItem : interestGroups) {
                InterestGroup ig = Converter.toInterestGroup(interestGroupItem, user.getUiLang());
                NodeRef igRef = Converter.createNodeRefFromId(ig.getId());
                Map<String, String> title = circabcService.getInterestGroupTitle(igRef);
                ig.setTitle(Converter.convertMlToI18nProperty(title));
                ig.setDescription(Converter.convertMlToI18nProperty(title));
                result.add(ig);
            }
        } else {
            setInterestGroups(categoryNodeRef, result);
        }
        return result;
    }

    private boolean isCategory(NodeRef categoryNodeRef) {
        return nodeService.hasAspect(categoryNodeRef, CircabcModel.ASPECT_CATEGORY);
    }

    private boolean isInterestGroup(NodeRef igNodeRef) {
        return nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT);
    }

    private void setInterestGroups(NodeRef categoryNodeRef, List<InterestGroup> result) {
        List<ChildAssociationRef> children = secureNodeService.getChildAssocs(categoryNodeRef);
        for (ChildAssociationRef child : children) {
            final NodeRef igRef = child.getChildRef();
            if (isInterestGroup(igRef)) {
                InterestGroup interestGroup = groupsApi.getInterestGroupDetails(igRef, false);
                result.add(interestGroup);
            }
        }
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
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

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public OwnableService getOwnableService() {
        return ownableService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * @return the nodeLocatorService
     */
    public NodeLocatorService getNodeLocatorService() {
        return nodeLocatorService;
    }

    /**
     * @param nodeLocatorService the nodeLocatorService to set
     */
    public void setNodeLocatorService(NodeLocatorService nodeLocatorService) {
        this.nodeLocatorService = nodeLocatorService;
    }

    /**
     * @param globalStatisticsService the globalStatisticsService to set
     */
    public void setGlobalStatisticsService(GlobalStatisticsService globalStatisticsService) {
        this.globalStatisticsService = globalStatisticsService;
    }

    /**
     * @param circabcLockService the circabcLockService to set
     */
    public void setCircabcLockService(LockService circabcLockService) {
        this.circabcLockService = circabcLockService;
    }

    /**
     * @param asyncThreadPoolExecutor the asyncThreadPoolExecutor to set
     */
    public void setAsyncThreadPoolExecutor(ThreadPoolExecutor asyncThreadPoolExecutor) {
        this.asyncThreadPoolExecutor = asyncThreadPoolExecutor;
    }

    /**
     * @return the groupsApi
     */
    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public ProfilesApi getProfilesApi() {
        return profilesApi;
    }

    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    /**
     * @return the circabcRoot
     */
    public NodeRef getCircabcRoot() {
        return circabcRoot;
    }

    /**
     * @param circabcRoot the circabcRoot to set
     */
    public void setCircabcRoot(NodeRef circabcRoot) {
        this.circabcRoot = circabcRoot;
    }

    /**
     * @return the circabcDaoServiceImpl
     */
    public CircabcDaoServiceImpl getCircabcDaoServiceImpl() {
        return circabcDaoServiceImpl;
    }

    /**
     * @param circabcDaoServiceImpl the circabcDaoServiceImpl to set
     */
    public void setCircabcDaoServiceImpl(CircabcDaoServiceImpl circabcDaoServiceImpl) {
        this.circabcDaoServiceImpl = circabcDaoServiceImpl;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public List<Profile> categoriesIdExportedProfilesGet(String id, String ignoreIgId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(id);
        Long categoryId = (Long) nodeService.getProperty(categoryRef, ContentModel.PROP_NODE_DBID);
        List<Profile> result = new ArrayList<>();

        List<ExportedProfileItem> exportedProfiles;

        if (ignoreIgId != null && !"".equals(ignoreIgId)) {
            NodeRef igRef = Converter.createNodeRefFromId(ignoreIgId);
            Long igId = (Long) nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID);
            exportedProfiles =
                    circabcDaoServiceImpl.selectExpProfilesByCategoryIDInterestGroupID(categoryId, igId);
        } else {
            exportedProfiles = circabcDaoServiceImpl.selectExpProfilesByCategoryID(categoryId);
        }

        for (ExportedProfileItem expProf : exportedProfiles) {
            Profile profile = new Profile();
            profile.setId(
                    expProf.getProfileRef().substring(expProf.getProfileRef().lastIndexOf('/') + 1));
            profile.setExported(true);
            profile.setImported(false);
            // need to substring because nodeRef from DB contains
            // 'workspace//...'
            profile.setImportedRef(
                    expProf.getNodeRef().substring(expProf.getNodeRef().lastIndexOf('/') + 1));
            profile.setGroupName(expProf.getPrefixedAlfrescoGroup());
            String name = expProf.getProfileName();
            if (expProf.getProfileName().endsWith(":")) {
                name = name + expProf.getName();
            }
            profile.setName(name);
            result.add(profile);
        }

        return result;
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public ActionService getActionService() {
        return actionService;
    }

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public RuleService getRuleService() {
        return ruleService;
    }

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @Override
    public InterestGroup categoriesIdGroupsPost(String id, final InterestGroupPostModel ig) {

        final NodeRef categoryRef = Converter.createNodeRefFromId(id);
        if (ig.getName() == null || ig.getName().isEmpty()) {
            throw new IllegalArgumentException("Interest group name must not be null or empty.");
        }
        if (nodeService.getChildByName(categoryRef, ContentModel.ASSOC_CONTAINS, ig.getName())
                != null) {
            throw new IllegalArgumentException(
                    "Interest group with name " + ig.getName() + " already exists.");
        }
        NodeRef igRef;
        try {
            igRef =
                    transactionService
                            .getRetryingTransactionHelper()
                            .doInTransaction(
                                    new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>() {

                                        public NodeRef execute() throws Throwable {
                                            // CREATE IG NODE and setup properties
                                            NodeRef igRef = createIgNode(ig, categoryRef);
                                            createMasterGroup(igRef);
                                            createInvitedGroup(igRef);
                                            final List<NodeRef> igFolders = createIGFolders(igRef);
                                            createKeywordContainer(igRef);
                                            createPredefinedProfiles(igRef, ig.getLeaders(), ig.getNotify(), ig.getNotifyText().getDefaultValue());
                                            setInterestGropPermission(igRef);
                                            for (NodeRef folder : igFolders) {
                                                cutInheritanceAndReApplyPermissions(folder);
                                            }
                                            return igRef;
                                        }
                                    },
                                    false,
                                    true);

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Try to rollback transaction" + e);
            }
            throw new RuntimeException(e);
        }

        if (igRef == null) {
            throw new RuntimeException("IG reference 'igRef' is null.");
        }

        return groupsApi.getInterestGroupDetails(igRef, true);
    }

    private void setInterestGropPermission(NodeRef igRef) {
        permissionService.deletePermission(igRef, GUEST, "Visibility");
        permissionService.setPermission(igRef, GUEST, NO_VISIBILITY, true);
        permissionService.deletePermission(igRef, GROUP_EVERYONE, "Visibility");
        permissionService.setPermission(igRef, GROUP_EVERYONE, NO_VISIBILITY, true);
        nodeService.setProperty(igRef, CircabcModel.PROP_CAN_REGISTERED_APPLY, false);
        permissionService.setInheritParentPermissions(igRef, false);
    }

    private void cutInheritanceAndReApplyPermissions(NodeRef folder) {
        permissionService.setInheritParentPermissions(folder, false);
    }

    private void createKeywordContainer(NodeRef igRef) {

        QName ASSOC_IG_KEYWORDCONTAINER =
                QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "igKwContainer");
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        igRef,
                        ASSOC_IG_KEYWORDCONTAINER,
                        TYPE_KEYWORD_CONTAINER,
                        TYPE_KEYWORD_CONTAINER,
                        new PropertyMap());

        final NodeRef kwContainerNodeRef = assocRef.getChildRef();

        permissionService.setPermission(
                kwContainerNodeRef,
                PermissionService.ALL_AUTHORITIES,
                PermissionService.ALL_PERMISSIONS,
                true);

        permissionService.setPermission(
                kwContainerNodeRef,
                CircabcConstant.GUEST_AUTHORITY,
                PermissionService.ALL_PERMISSIONS,
                true);
    }

    /**
     *
     */
    private void createPredefinedProfiles(NodeRef igRef, List<String> users, Boolean notify, String notifyText) {

        createGuestProfile(igRef);

        createEveryoneProfile(igRef);

        Profile leaderProfile = createLeaderProfile(igRef);

        for (String user : users) {
            MembershipPostDefinition body = new MembershipPostDefinition();
            body.setAdminNotifications(false);
            body.setUserNotifications(false);
            UserProfile membershipsItem = new UserProfile();
            User u = new User();
            u.setUserId(user);
            membershipsItem.setUser(u);
            membershipsItem.setProfile(leaderProfile);
            body.setUserNotifications(notify);
            body.addMembershipsItem(membershipsItem);
            body.setNotifyText(notifyText);
            if (!personService.personExists(user)) {
                CircabcUserDataBean userDataBean =
                        ldapUserService.getLDAPUserDataByUid(user);
                userDataBean.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
                userService.createUser(userDataBean, false);
            }
            groupsApi.groupsIdMembersPostNoSync(igRef, body);
        }

        createAccessProfile(igRef);

        createAuthorProfile(igRef);
    }

    private void createAuthorProfile(NodeRef igRef) {
        Profile authorProfile = new Profile();
        authorProfile.setName(AUTHOR);
        I18nProperty authtitle = new I18nProperty();
        authtitle.put("en", AUTHOR);
        authorProfile.setTitle(authtitle);
        authorProfile.getPermissions().put(INFORMATION, "InfAccess");
        authorProfile.getPermissions().put(LIBRARY, "LibFullEdit");
        authorProfile.getPermissions().put(EVENTS, "EveAccess");
        authorProfile.getPermissions().put(MEMBERS, "DirAccess");
        authorProfile.getPermissions().put(NEWSGROUPS, "NwsPost");
        profilesApi.groupsIdProfilesPostNoSync(igRef, authorProfile);
    }

    private void createAccessProfile(NodeRef igRef) {
        Profile accessProfile = new Profile();
        accessProfile.setName(ACCESS);
        I18nProperty atitle = new I18nProperty();
        atitle.put("en", ACCESS);
        accessProfile.setTitle(atitle);
        accessProfile.getPermissions().put(INFORMATION, "InfAccess");
        accessProfile.getPermissions().put(LIBRARY, "LibAccess");
        accessProfile.getPermissions().put(EVENTS, "EveAccess");
        accessProfile.getPermissions().put(MEMBERS, "DirAccess");
        accessProfile.getPermissions().put(NEWSGROUPS, "NwsAccess");
        profilesApi.groupsIdProfilesPostNoSync(igRef, accessProfile);
    }

    private Profile createLeaderProfile(NodeRef igRef) {
        Profile leaderProfile = new Profile();
        leaderProfile.setName(LEADER);
        I18nProperty ltitle = new I18nProperty();
        ltitle.put("en", LEADER);
        leaderProfile.setTitle(ltitle);
        leaderProfile.getPermissions().put(INFORMATION, "InfAdmin");
        leaderProfile.getPermissions().put(LIBRARY, "LibAdmin");
        leaderProfile.getPermissions().put(EVENTS, "EveAdmin");
        leaderProfile.getPermissions().put(MEMBERS, "DirAdmin");
        leaderProfile.getPermissions().put(NEWSGROUPS, "NwsAdmin");
        leaderProfile = profilesApi.groupsIdProfilesPostNoSync(igRef, leaderProfile);
        return leaderProfile;
    }

    private void createEveryoneProfile(NodeRef igRef) {
        Profile everyoneProfile = new Profile();
        everyoneProfile.setName(EVERYONE);
        I18nProperty rtitle = new I18nProperty();
        rtitle.put("en", "Registered");
        everyoneProfile.setTitle(rtitle);
        everyoneProfile.getPermissions().put(INFORMATION, "InfNoAccess");
        everyoneProfile.getPermissions().put(LIBRARY, "LibNoAccess");
        everyoneProfile.getPermissions().put(EVENTS, "EveNoAccess");
        everyoneProfile.getPermissions().put(MEMBERS, "DirNoAccess");
        everyoneProfile.getPermissions().put(NEWSGROUPS, "NwsNoAccess");
        everyoneProfile.getPermissions().put("visibility", NO_VISIBILITY);
        profilesApi.groupsIdProfilesPostNoSync(igRef, everyoneProfile);
    }

    private void createGuestProfile(NodeRef igRef) {
        Profile guestProfile = new Profile();
        guestProfile.setName(GUEST);
        I18nProperty gtitle = new I18nProperty();
        gtitle.put("en", GUEST);
        guestProfile.setTitle(gtitle);
        guestProfile.getPermissions().put(INFORMATION, "InfNoAccess");
        guestProfile.getPermissions().put(LIBRARY, "LibNoAccess");
        guestProfile.getPermissions().put(EVENTS, "EveNoAccess");
        guestProfile.getPermissions().put(MEMBERS, "DirNoAccess");
        guestProfile.getPermissions().put(NEWSGROUPS, "NwsNoAccess");
        guestProfile.getPermissions().put("visibility", NO_VISIBILITY);
        profilesApi.groupsIdProfilesPostNoSync(igRef, guestProfile);
    }

    private List<NodeRef> createIGFolders(NodeRef igRef) {

        List<NodeRef> result = new ArrayList<>(5);
        ////////////////////////////////////////
        // DIRECTORY
        NodeRef directoryRef =
                nodeService
                        .createNode(
                                igRef,
                                CircabcModel.ASSOC_IG_DIRECTORY_CONTAINER,
                                CircabcModel.TYPE_DIRECTORY_SERVICE,
                                CircabcModel.TYPE_DIRECTORY_SERVICE,
                                new PropertyMap())
                        .getChildRef();
        NodeRef libRef = createLibrary(igRef);
        NodeRef eventRef = createEvent(igRef);
        NodeRef newsRef = createNewsGroup(igRef);
        NodeRef infoRef = createInformation(igRef);

        // directoryRef should not be set in the returned list. Because later on, the inheritance will
        // be cut
        // the directoryRef should have inheritance set to true

        result.add(libRef);
        result.add(newsRef);
        result.add(eventRef);
        result.add(infoRef);
        return result;
    }

    private NodeRef createInformation(NodeRef igRef) {
        //////////////////////////////////////////
        // INFORMATION
        String information = "Information";
        QName assocInfQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, information);
        Map<QName, Serializable> infProps = new HashMap<>();
        infProps.put(ContentModel.PROP_NAME, information);
        infProps.put(
                ContentModel.PROP_TITLE, Converter.toMLText(Converter.toI18NProperty(information)));
        NodeRef infoRef =
                nodeService
                        .createNode(
                                igRef,
                                ContentModel.ASSOC_CONTAINS,
                                assocInfQName,
                                ContentModel.TYPE_FOLDER,
                                infProps)
                        .getChildRef();

        nodeService.addAspect(infoRef, CircabcModel.ASPECT_INFORMATION, null);
        nodeService.addAspect(infoRef, CircabcModel.ASPECT_INFORMATION_ROOT, null);

        NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
        String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
        permissionService.setPermission(
                infoRef, categoryAdminProfileGroupName, InformationPermissions.INFADMIN.toString(), true);

        // Add Information
        final CompositeAction compositeActionInformation = actionService.createCompositeAction();

        // Create Action
        final Action actionInformation = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionInformation.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_INFORMATION);
        compositeActionInformation.addAction(actionInformation);
        compositeActionInformation.setTitle("Add Information Aspect");
        compositeActionInformation.setDescription("Add Information Aspect Description");

        // Create Condition
        final ActionCondition actionConditionInformation =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionInformation.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

        compositeActionInformation.addActionCondition(actionConditionInformation);

        // Create a rule
        final Rule ruleInfo = new Rule();
        ruleInfo.setRuleType(RuleType.INBOUND);

        ruleInfo.applyToChildren(true);
        ruleInfo.setExecuteAsynchronously(false);
        ruleInfo.setAction(compositeActionInformation);
        ruleInfo.setTitle(compositeActionInformation.getTitle());
        ruleInfo.setDescription(compositeActionInformation.getDescription());
        ruleService.saveRule(infoRef, ruleInfo);

        final CompositeAction compositeActionInfoNotify = actionService.createCompositeAction();

        // Create Action
        final Action actionInfoNotify = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionInfoNotify.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        compositeActionInfoNotify.addAction(actionInfoNotify);
        compositeActionInfoNotify.setTitle(ADD_CIRCABC_NOTIFY_ASPECT);
        compositeActionInfoNotify.setDescription(ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION);

        // Create Condition
        final ActionCondition actionConditionInfoNotify =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionInfoNotify.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);
        compositeActionInfoNotify.addActionCondition(actionConditionInfoNotify);

        // Create a rule
        final Rule ruleInfoNotify = new Rule();
        ruleInfoNotify.setRuleType(RuleType.INBOUND);

        ruleInfoNotify.applyToChildren(true);
        ruleInfoNotify.setExecuteAsynchronously(false);
        ruleInfoNotify.setAction(compositeActionInfoNotify);
        ruleInfoNotify.setTitle(compositeActionInfoNotify.getTitle());
        ruleInfoNotify.setDescription(compositeActionInfoNotify.getDescription());
        ruleService.saveRule(infoRef, ruleInfoNotify);
        return infoRef;
    }

    private NodeRef createNewsGroup(NodeRef igRef) {
        ////////////////////////////////////////
        // NEWSGROUPS
        String newsgroups = "Newsgroups";
        QName assocNwsQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newsgroups);
        Map<QName, Serializable> nwsProps = new HashMap<>();
        nwsProps.put(ContentModel.PROP_NAME, newsgroups);
        nwsProps.put(ContentModel.PROP_TITLE, Converter.toMLText(Converter.toI18NProperty(newsgroups)));
        NodeRef newsRef =
                nodeService
                        .createNode(
                                igRef, ContentModel.ASSOC_CONTAINS, assocNwsQName, ForumModel.TYPE_FORUMS, nwsProps)
                        .getChildRef();

        nodeService.addAspect(newsRef, CircabcModel.ASPECT_NEWSGROUP, null);
        nodeService.addAspect(newsRef, CircabcModel.ASPECT_NEWSGROUP_ROOT, null);

        NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
        String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
        permissionService.setPermission(
                newsRef, categoryAdminProfileGroupName, NewsGroupPermissions.NWSADMIN.toString(), true);

        // Add CircaNewsGroup
        final CompositeAction compositeActionNewsGroup = actionService.createCompositeAction();

        // Create Action
        final Action actionNewsGroup = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionNewsGroup.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_NEWSGROUP);
        compositeActionNewsGroup.addAction(actionNewsGroup);
        compositeActionNewsGroup.setTitle("Add CircaNewsGroup Aspect");
        compositeActionNewsGroup.setDescription("Add CircaNewsGroup Aspect Description");

        // Create Condition
        final ActionCondition actionConditionNewsGroup =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionNewsGroup.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

        compositeActionNewsGroup.addActionCondition(actionConditionNewsGroup);

        // Create a rule
        final Rule ruleNews = new Rule();
        ruleNews.setRuleType(RuleType.INBOUND);

        ruleNews.applyToChildren(true);
        ruleNews.setExecuteAsynchronously(false);
        ruleNews.setAction(compositeActionNewsGroup);
        ruleNews.setTitle(compositeActionNewsGroup.getTitle());
        ruleNews.setDescription(compositeActionNewsGroup.getDescription());
        ruleService.saveRule(newsRef, ruleNews);

        final CompositeAction compositeActionNewsNotify = actionService.createCompositeAction();

        // Create Action
        final Action actionNewsNotify = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionNewsNotify.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        compositeActionNewsNotify.addAction(actionNewsNotify);
        compositeActionNewsNotify.setTitle(ADD_CIRCABC_NOTIFY_ASPECT);
        compositeActionNewsNotify.setDescription(ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION);

        // Create Condition
        final ActionCondition actionConditionNewsNotify =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionNewsNotify.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);
        compositeActionNewsNotify.addActionCondition(actionConditionNewsNotify);

        // Create a rule
        final Rule ruleNewsNotify = new Rule();
        ruleNewsNotify.setRuleType(RuleType.INBOUND);

        ruleNewsNotify.applyToChildren(true);
        ruleNewsNotify.setExecuteAsynchronously(false);
        ruleNewsNotify.setAction(compositeActionNewsNotify);
        ruleNewsNotify.setTitle(compositeActionNewsNotify.getTitle());
        ruleNewsNotify.setDescription(compositeActionNewsNotify.getDescription());
        ruleService.saveRule(newsRef, ruleNewsNotify);
        return newsRef;
    }

    private NodeRef createEvent(NodeRef igRef) {
        ////////////////////////////////////////
        // EVENTS
        String events = "Events";
        QName assocEvtQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, events);
        Map<QName, Serializable> evtProps = new HashMap<>();
        evtProps.put(ContentModel.PROP_NAME, events);
        evtProps.put(ContentModel.PROP_TITLE, Converter.toMLText(Converter.toI18NProperty(events)));
        NodeRef eventRef =
                nodeService
                        .createNode(
                                igRef,
                                ContentModel.ASSOC_CONTAINS,
                                assocEvtQName,
                                ContentModel.TYPE_FOLDER,
                                evtProps)
                        .getChildRef();
        nodeService.addAspect(
                eventRef, CircabcModel.ASPECT_EVENT_ROOT, new HashMap<QName, Serializable>());

        NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
        String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
        permissionService.setPermission(
                eventRef, categoryAdminProfileGroupName, EventPermissions.EVEADMIN.toString(), true);

        // Add CircaNewsGroup
        final CompositeAction compositeActionEvent = actionService.createCompositeAction();

        // Create Action
        final Action actionEvent = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionEvent.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_EVENT);
        compositeActionEvent.addAction(actionEvent);
        compositeActionEvent.setTitle("Add Event Aspect");
        compositeActionEvent.setDescription("Add Event Aspect Description");

        // Create Condition
        final ActionCondition actionConditionEvent =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionEvent.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CMOBJECT);

        compositeActionEvent.addActionCondition(actionConditionEvent);

        // Create a rule
        final Rule ruleEvent = new Rule();
        ruleEvent.setRuleType(RuleType.INBOUND);

        ruleEvent.applyToChildren(true);
        ruleEvent.setExecuteAsynchronously(false);
        ruleEvent.setAction(compositeActionEvent);
        ruleEvent.setTitle(compositeActionEvent.getTitle());
        ruleEvent.setDescription(compositeActionEvent.getDescription());
        ruleService.saveRule(eventRef, ruleEvent);
        return eventRef;
    }

    private NodeRef createLibrary(NodeRef igRef) {
        ////////////////////////////////////////
        // LIBRARY
        String library = "Library";
        QName assocLibQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, library);
        Map<QName, Serializable> libProps = new HashMap<>();
        libProps.put(ContentModel.PROP_NAME, library);
        libProps.put(ContentModel.PROP_TITLE, Converter.toMLText(Converter.toI18NProperty(library)));
        NodeRef libRef =
                nodeService
                        .createNode(
                                igRef,
                                ContentModel.ASSOC_CONTAINS,
                                assocLibQName,
                                ContentModel.TYPE_FOLDER,
                                libProps)
                        .getChildRef();
        nodeService.addAspect(
                libRef, CircabcModel.ASPECT_LIBRARY_ROOT, new HashMap<QName, Serializable>());
        nodeService.addAspect(
                libRef, ApplicationModel.ASPECT_UIFACETS, new HashMap<QName, Serializable>());

        Map<QName, Serializable> ownableProps = new HashMap<>();
        ownableProps.put(ContentModel.PROP_OWNER, ADMIN);
        nodeService.addAspect(libRef, ContentModel.ASPECT_OWNABLE, ownableProps);

        NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
        String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
        permissionService.setPermission(
                libRef, categoryAdminProfileGroupName, LibraryPermissions.LIBADMIN.toString(), true);

        // Add CircaDocument
        final CompositeAction compositeAction = actionService.createCompositeAction();

        // Create Action
        final Action action = actionService.createAction(AddFeaturesActionExecuter.NAME);
        action.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, DocumentModel.ASPECT_CIRCABC_DOCUMENT);
        compositeAction.addAction(action);
        compositeAction.setTitle("Add CircaDocument Aspect");
        compositeAction.setDescription("Add CircaDocument Aspect Description");

        // Create Condition
        final ActionCondition actionConditionCircaDocument =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);

        actionConditionCircaDocument.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);

        compositeAction.addActionCondition(actionConditionCircaDocument);

        // Create a rule
        final Rule rule = new Rule();
        rule.setRuleType(RuleType.INBOUND);

        rule.applyToChildren(true);
        rule.setExecuteAsynchronously(false);
        rule.setAction(compositeAction);
        rule.setTitle(compositeAction.getTitle());
        rule.setDescription(compositeAction.getDescription());
        ruleService.saveRule(libRef, rule);

        // Add CircaLibrary
        final CompositeAction compositeActionLibrary = actionService.createCompositeAction();

        // Create ActionN
        final Action actionLibrary = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionLibrary.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, CircabcModel.ASPECT_LIBRARY);
        compositeActionLibrary.addAction(actionLibrary);
        compositeActionLibrary.setTitle("Add CircaLibrary Aspect");
        compositeActionLibrary.setDescription("Add CircaLibrary Aspect Description");

        // Create Condition
        final ActionCondition actionConditionLibrary =
                actionService.createActionCondition(NoConditionEvaluator.NAME);
        compositeActionLibrary.addActionCondition(actionConditionLibrary);

        // Create a rule
        final Rule ruleLibrary = new Rule();
        ruleLibrary.setRuleType(RuleType.INBOUND);

        ruleLibrary.applyToChildren(true);
        ruleLibrary.setExecuteAsynchronously(false);
        ruleLibrary.setAction(compositeActionLibrary);
        ruleLibrary.setTitle(compositeActionLibrary.getTitle());
        ruleLibrary.setDescription(compositeActionLibrary.getDescription());
        ruleService.saveRule(libRef, ruleLibrary);

        // Add CircaManagement
        final CompositeAction compositeActionNotify = actionService.createCompositeAction();

        // Create Action
        final Action actionNotify = actionService.createAction(AddFeaturesActionExecuter.NAME);
        actionNotify.setParameterValue(
                AddFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        compositeActionNotify.addAction(actionNotify);
        compositeActionNotify.setTitle(ADD_CIRCABC_NOTIFY_ASPECT);
        compositeActionNotify.setDescription(ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION);

        // Create Condition
        final ActionCondition actionConditionNotify =
                actionService.createActionCondition(IsSubTypeEvaluator.NAME);
        actionConditionNotify.setParameterValue(
                IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_CONTENT);
        compositeActionNotify.addActionCondition(actionConditionNotify);

        // Create a rule
        final Rule ruleNotify = new Rule();
        ruleNotify.setRuleType(RuleType.INBOUND);

        ruleNotify.applyToChildren(true);
        ruleNotify.setExecuteAsynchronously(false);
        ruleNotify.setAction(compositeActionNotify);
        ruleNotify.setTitle(compositeActionNotify.getTitle());
        ruleNotify.setDescription(compositeActionNotify.getDescription());
        ruleService.saveRule(libRef, ruleNotify);
        return libRef;
    }

    private String createSubsGroup(NodeRef nodeRef) {

        String prefixedMasterGroupName = null;
        String masterGroupName = "";

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            masterGroupName =
                    nodeService.getProperty(nodeRef, CircabcModel.PROP_IG_ROOT_MASTER_GROUP).toString();
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            masterGroupName =
                    nodeService.getProperty(nodeRef, CircabcModel.PROP_CATEGORY_MASTER_GROUP).toString();
        }

        if (masterGroupName != null) {
            // Full AuthorityIdentifier
            prefixedMasterGroupName = authorityService.getName(AuthorityType.GROUP, masterGroupName);
        }

        // get name of the folder
        final String folderName =
                ((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).replace(" ", "");
        // create the group introducing folder name in the group name
        final String subsGroupName = folderName + "--SubsGroup--" + GUID.generate();

        // Migration 3.1 -> 3.4.6 - 09/12/2011
        // createAuthority() method changed for version 3.4
        String createdAuthority =
                authorityService.createAuthority(
                        AuthorityType.GROUP, subsGroupName, subsGroupName, authorityService.getDefaultZones());

        if (createdAuthority != null) {
            String prefixedInvitedUsersGroupName =
                    authorityService.getName(AuthorityType.GROUP, subsGroupName);
            authorityService.addAuthority(prefixedMasterGroupName, prefixedInvitedUsersGroupName);
        }

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            nodeService.setProperty(nodeRef, CircabcModel.PROP_CATEGORY_SUBS_GROUP, subsGroupName);
        }

        return subsGroupName;
    }

    private String createInvitedGroup(NodeRef nodeRef) {

        String prefixedMasterGroupName = null;
        String masterGroupName = "";

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            masterGroupName =
                    nodeService.getProperty(nodeRef, CircabcModel.PROP_IG_ROOT_MASTER_GROUP).toString();
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            masterGroupName =
                    nodeService.getProperty(nodeRef, CircabcModel.PROP_CATEGORY_MASTER_GROUP).toString();
        }

        if (masterGroupName != null) {
            // Full AuthorityIdentifier
            prefixedMasterGroupName = authorityService.getName(AuthorityType.GROUP, masterGroupName);
        }

        // get name of the folder
        final String folderName =
                ((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).replace(" ", "");
        // create the group introducing folder name in the group name
        final String subsGroupName = folderName + "--InvitedUsersGroup--" + GUID.generate();

        // Migration 3.1 -> 3.4.6 - 09/12/2011
        // createAuthority() method changed for version 3.4
        String createdAuthority =
                authorityService.createAuthority(
                        AuthorityType.GROUP, subsGroupName, subsGroupName, authorityService.getDefaultZones());

        if (createdAuthority != null) {
            String prefixedInvitedUsersGroupName =
                    authorityService.getName(AuthorityType.GROUP, subsGroupName);
            authorityService.addAuthority(prefixedMasterGroupName, prefixedInvitedUsersGroupName);
        }

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            nodeService.setProperty(nodeRef, CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP, subsGroupName);
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            nodeService.setProperty(
                    nodeRef, CircabcModel.PROP_CATEGORY_INVITED_USER_GROUP, subsGroupName);
        }

        return subsGroupName;
    }

    /**
     *
     */
    private NodeRef createIgNode(InterestGroupPostModel ig, NodeRef categoryRef) {
        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, ig.getName());
        Map<QName, Serializable> props = new HashMap<>();
        props.put(ContentModel.PROP_NAME, ig.getName());

        NodeRef igRef =
                nodeService
                        .createNode(
                                categoryRef,
                                ContentModel.ASSOC_CONTAINS,
                                assocQName,
                                ContentModel.TYPE_FOLDER,
                                props)
                        .getChildRef();
        Map<QName, Serializable> igRootprops = new HashMap<>();
        if (ig.getContact() != null) {
            igRootprops.put(CircabcModel.PROP_CONTACT_INFORMATION, Converter.toMLText(ig.getContact()));
        }

        nodeService.addAspect(igRef, CircabcModel.ASPECT_IGROOT, igRootprops);
        Map<QName, Serializable> cbcManagementrops = new HashMap<>();
        nodeService.addAspect(igRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT, cbcManagementrops);

        // apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, SPACE_ICON_DEFAULT);

        if (ig.getTitle() != null) {
            uiFacetsProps.put(ContentModel.PROP_TITLE, Converter.toMLText(ig.getTitle()));
        } else {
            uiFacetsProps.put(ContentModel.PROP_TITLE, null);
        }

        if (ig.getDescription() != null) {
            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, Converter.toMLText(ig.getDescription()));
        } else {
            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, null);
        }

        getNodeService().addAspect(igRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);

        permissionService.setPermission(
                igRef, categoryAdminProfileGroupName, DirectoryPermissions.DIRADMIN.toString(), true);
        permissionService.setPermission(
                igRef, categoryAdminProfileGroupName, IgPermissions.IGDELETE.toString(), true);
        permissionService.setPermission(
                igRef, categoryAdminProfileGroupName, VisibilityPermissions.VISIBILITY.toString(), true);

        ownableService.setOwner(igRef, ADMIN);

        return igRef;
    }

    private String createMasterGroup(final NodeRef nodeRef) {

        String parentSubsGroupName = null;
        String prefixedParentSubsGroupName = null;

        NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            parentSubsGroupName =
                    nodeService.getProperty(parentNodeRef, CircabcModel.PROP_CATEGORY_SUBS_GROUP).toString();
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            parentSubsGroupName =
                    nodeService.getProperty(parentNodeRef, PROP_CIRCABC_SUBGROUP).toString();
        }

        if (parentSubsGroupName != null) {
            // Full AuthorityIdentifier
            prefixedParentSubsGroupName =
                    authorityService.getName(AuthorityType.GROUP, parentSubsGroupName);
        }

        final String folderName =
                ((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).replace(" ", "");
        // create the group introducing folder name in the group name
        final String masterGroupName = folderName + "--MasterGroup--" + GUID.generate();

        // create the group as a root authority
        String createdAuthority =
                authorityService.createAuthority(
                        AuthorityType.GROUP,
                        masterGroupName,
                        masterGroupName,
                        authorityService.getDefaultZones());

        if (prefixedParentSubsGroupName != null && createdAuthority != null) {
            String prefixedMasterGroupName =
                    authorityService.getName(AuthorityType.GROUP, masterGroupName);
            authorityService.addAuthority(prefixedParentSubsGroupName, prefixedMasterGroupName);
        }

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            nodeService.setProperty(nodeRef, CircabcModel.PROP_IG_ROOT_MASTER_GROUP, masterGroupName);
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            nodeService.setProperty(nodeRef, CircabcModel.PROP_CATEGORY_MASTER_GROUP, masterGroupName);
        }

        return masterGroupName;
    }

    /**
     * @see io.swagger.api.CategoriesApi#getIGStatisticsContents(java.lang.String, int, int)
     */
    @Override
    public PagedStatisticsContents getIGStatisticsContents(String id, int startItem, int amount) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            throw new IllegalArgumentException("Node with id '" + id + "' is not a Category node.");
        }

        String categoryName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

        List<FileInfo> contents =
                globalStatisticsService.getCategoryGroupStatsFiles(categoryName, nodeRef);

        List<ReportFile> result = new ArrayList<>();

        for (FileInfo fileInfo : contents) {
            result.add(new ReportFile(fileInfo, fileInfo.getName()));
        }

        Collections.sort(
                result,
                new Comparator<ReportFile>() {

                    /** @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
                    @Override
                    public int compare(ReportFile rf1, ReportFile rf2) {
                        return rf2.getFileInfo()
                                .getModifiedDate()
                                .compareTo(rf1.getFileInfo().getModifiedDate());
                    }
                });

        int resultSize = result.size();

        List<ReportFile> pagedReports;

        if (amount == 0) {
            // amount == 0 means that we want all items
            pagedReports = result;
        } else {
            pagedReports = new ArrayList<>();

            int endItem = Math.min(startItem + amount, resultSize);

            for (int index = startItem; index < endItem; index++) {
                pagedReports.add(result.get(index));
            }
        }

        return new PagedStatisticsContents(pagedReports, resultSize);
    }

    /**
     * @see io.swagger.api.CategoriesApi#calculateIGStatistics(java.lang.String)
     */
    @Override
    public void calculateIGStatistics(String id) {

        NodeRef categoryRef = Converter.createNodeRefFromId(id);

        if (!nodeService.hasAspect(categoryRef, CircabcModel.ASPECT_CATEGORY)) {
            throw new IllegalArgumentException("Node with id '" + id + "' is not a Category node.");
        }

        if (!circabcLockService.isLocked(CATEG_STAT_JOB + categoryRef.getId())) {
            Runnable runnable =
                    new CategoryIgStatisticsRunnable(AuthenticationUtil.getSystemUserName(), categoryRef);
            asyncThreadPoolExecutor.execute(runnable);
        }
    }

    @Override
    public void categoriesIdGroupRequestPost(String categoryId, GroupCreationRequest body) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        body.setCategoryRef(categoryRef.getId());
        groupRequestsDaoService.saveRequest(body);

        try {
            List<User> admins = categoriesIdAdminsGet(categoryId);
            EmailDefinition email = emailApi.prepareEmailForGroupRequest(body, admins);
            emailApi.mailPost(email);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("problem sending email", e);
            }
        }
    }

    @Override
    public List<User> categoriesIdAdminsGet(String categoryId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        List<User> result = new ArrayList<>();

        List<String> admins = circabcService.getCategoryAdmins(categoryRef);
        for (String admin : admins) {
            result.add(usersApi.usersUserIdGet(admin));
        }

        return result;
    }

    /**
     * @return the emailApi
     */
    public EmailApi getEmailApi() {
        return emailApi;
    }

    /**
     * @param emailApi the emailApi to set
     */
    public void setEmailApi(EmailApi emailApi) {
        this.emailApi = emailApi;
    }

    @Override
    public List<Node> getCategoryLogoByCategoryId(String categoryId) {
        List<Node> result = new ArrayList<>();
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        NodeRef logoFolderRef = getLogoFolderRef(categoryRef);

        if (logoFolderRef != null) {
            List<ChildAssociationRef> children = secureNodeService.getChildAssocs(logoFolderRef);
            for (ChildAssociationRef child : children) {
                result.add(nodesApi.getNode(child.getChildRef()));
            }
        }

        return result;
    }

    /**
     *
     */
    private NodeRef getLogoFolderRef(NodeRef categoryRef) {
        NodeRef logoFolderRef = null;
        for (ChildAssociationRef child : nodeService.getChildAssocs(categoryRef)) {
            if (child
                    .getTypeQName()
                    .getLocalName()
                    .equals(CircabcModel.ASSOC_CATEGORY_LOGOS.getLocalName())) {
                logoFolderRef = child.getChildRef();
                break;
            }
        }
        return logoFolderRef;
    }

    public NodesApi getNodesApi() {
        return nodesApi;
    }

    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }

    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public void postCategoryLogoByCategoryId(
            String categoryId, InputStream inputStream, String fileName) {

        try {
            ESAPI
                    .validator()
                    .getValidFileName(
                            "submitted file",
                            fileName,
                            new ArrayList<>(Arrays.asList(".jpg", ".jpeg", ".bmp", ".gif", ".png")),
                            false);
        } catch (ValidationException | IntrusionException vex) {
            throw new IllegalArgumentException("Invalid file type: " + fileName);
        }

        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        NodeRef logoFolderRef = getLogoFolderRef(categoryRef);

        if (logoFolderRef == null) {
            ChildAssociationRef childAssoc =
                    nodeService.createNode(
                            categoryRef,
                            CircabcModel.ASSOC_CATEGORY_LOGOS,
                            LOGO_CONTAINER_QNAME,
                            ContentModel.TYPE_FOLDER);
            logoFolderRef = childAssoc.getChildRef();
        }

        if (inputStream != null) {

            Date today = new Date();
            GregorianCalendar calDate = new GregorianCalendar();
            calDate.setTime(today);

            String newfilename =
                    calDate.get(Calendar.YEAR)
                            + "-"
                            + calDate.get(Calendar.MONTH)
                            + "-"
                            + calDate.get(Calendar.DAY_OF_MONTH)
                            + "-"
                            + calDate.get(Calendar.HOUR)
                            + "-"
                            + calDate.get(Calendar.MINUTE)
                            + "-"
                            + calDate.get(Calendar.SECOND)
                            + "-"
                            + fileName;

            QName associationNameQName =
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), newfilename);

            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, newfilename);

            NodeRef nodeRef =
                    nodeService
                            .createNode(
                                    logoFolderRef,
                                    ContentModel.ASSOC_CONTAINS,
                                    associationNameQName,
                                    ContentModel.TYPE_CONTENT,
                                    props)
                            .getChildRef();

            final ContentWriter writer =
                    contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(mimetypeService.guessMimetype(fileName));

            File tempFile = null;

            try {
                long attachmentTotalSize =
                        Long.parseLong(
                                CircabcConfiguration.getProperty(CircabcConfiguration.LOGO_ALLOWED_SIZE_BYTES));

                tempFile = ApiToolBox.checkAndGetImageFile(fileName, inputStream, attachmentTotalSize);
                writer.putContent(tempFile);
            } finally {
                if (tempFile != null) {
                    boolean isDeleted = tempFile.delete();
                    if (!isDeleted) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Can not delete filee" + tempFile.toString());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void selectCategoryLogoByLogoId(String categoryId, String logoId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        NodeRef logoRef = Converter.createNodeRefFromId(logoId);

        nodeService.setProperty(categoryRef, CircabcModel.PROP_LOGO_REF, logoRef);
    }

    @Override
    public List<Node> deleteCategoryLogoByLogoId(String categoryId, String logoId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        NodeRef logoRef = Converter.createNodeRefFromId(logoId);

        Serializable logoRefSerialized =
                nodeService.getProperty(categoryRef, CircabcModel.PROP_LOGO_REF);
        if (logoRef.equals(logoRefSerialized)) {
            nodeService.setProperty(categoryRef, CircabcModel.PROP_LOGO_REF, null);
        }

        nodeService.deleteNode(logoRef);

        return getCategoryLogoByCategoryId(categoryId);
    }

    @Override
    public Category categoriesIdPut(String categoryId, Category category) {

        if (categoryId != null && category != null) {
            NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
            category.setId(categoryId);

            String name = category.getName();
            if (name != null && !"".equals(name)) {
                this.nodeService.setProperty(categoryRef, ContentModel.PROP_NAME, name.trim());
            }

            if (category.getTitle() != null) {
                MLText title = Converter.toMLText(category.getTitle());
                this.nodeService.setProperty(categoryRef, ContentModel.PROP_TITLE, title);
            }

            if (category.getUseSingleContact() != null) {
                nodeService.setProperty(
                        categoryRef, CircabcModel.PROP_SINGLE_CONTACT, category.getUseSingleContact());
            }

            if (category.getContactEmails() != null) {
                StringBuilder emails = new StringBuilder();
                for (String email : category.getContactEmails()) {
                    emails.append(email);
                    emails.append(";");
                }

                nodeService.setProperty(categoryRef, CircabcModel.PROP_CONTACT_EMAILS, emails.toString());

                Serializable contactMails =
                        nodeService.getProperty(categoryRef, CircabcModel.PROP_CONTACT_EMAILS);
                if (!contactMails.equals(emails.toString()) && category.getUseSingleContact()) {
                    nodeService.setProperty(categoryRef, CircabcModel.PROP_CONTACT_VERIFIED, false);
                }
            }
        }

        return category;
    }

    @Override
    public List<String> categoriesIdAdminsPost(String categoryId, List<String> userIds) {
        List<String> userIdsAdded = new ArrayList<>();

        if (categoryId != null && userIds != null) {
            NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
            String circabcAdminGroup = getCategoryGroupName(categoryRef);

            if (authorityService.authorityExists(circabcAdminGroup)) {
                for (String userId : userIds) {

                    if (!personService.personExists(userId)) {
                        CircabcUserDataBean uData = ldapUserService.getLDAPUserDataByUid(userId);
                        uData.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
                        userService.createUser(uData, false);
                    }
                    authorityService.addAuthority(circabcAdminGroup, userId);
                    userIdsAdded.add(userId);
                }
            }
        }

        return userIdsAdded;
    }

    @Override
    public void categoriesIdAdminsDelete(String categoryId, String userId) {
        if (categoryId != null) {
            NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
            String circabcAdminGroup = getCategoryGroupName(categoryRef);

            if (authorityService.authorityExists(circabcAdminGroup)) {
                authorityService.removeAuthority(circabcAdminGroup, userId);
            }
        }
    }

    /**
     *
     */
    private String getCategoryGroupName(NodeRef categoryRef) {
        NodeRef categoryAdminProfileRef =
                nodeService
                        .getChildAssocs(
                                categoryRef, CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE, CATEGORY_ADMIN_PROFILE_NAME)
                        .get(0)
                        .getChildRef();
        return nodeService.getProperty(categoryAdminProfileRef, CATEGORY_PROFILE_GROUP_NAME).toString();
    }

    @Override
    public Category headersIdCategoryPost(String headerId, Category categoryBody) {
        NodeRef headerRef = Converter.createNodeRefFromId(headerId);

        try {
            UserTransaction trx =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            trx.begin();
            NodeRef circabcNodeRef = getCircabcNode();

            NodeRef categoryRef =
                    nodeService
                            .createNode(
                                    circabcNodeRef,
                                    ContentModel.ASSOC_CONTAINS,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, categoryBody.getName()),
                                    ContentModel.TYPE_FOLDER)
                            .getChildRef();

            nodeService.setProperty(categoryRef, ContentModel.PROP_NAME, categoryBody.getName());

            categoryBody.setId(categoryRef.getId());

            Map<QName, Serializable> circabcCategoryProps = new HashMap<>();
            nodeService.addAspect(categoryRef, CircabcModel.ASPECT_CATEGORY, circabcCategoryProps);

            nodeService.addAspect(categoryRef, ContentModel.ASPECT_GEN_CLASSIFIABLE, null);

            Map<QName, Serializable> uiFacetsProps = new HashMap<>();
            uiFacetsProps.put(ContentModel.PROP_TITLE, Converter.toMLText(categoryBody.getTitle()));
            uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, "");
            uiFacetsProps.put(ApplicationModel.PROP_ICON, SPACE_ICON_DEFAULT);
            nodeService.addAspect(categoryRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

            ArrayList<NodeRef> categories = new ArrayList<>();
            categories.add(headerRef);
            nodeService.setProperty(categoryRef, ContentModel.PROP_CATEGORIES, categories);
            trx.commit();
            UserTransaction trx1 =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            trx1.begin();
            createMasterGroup(categoryRef);
            createSubsGroup(categoryRef);
            createInvitedGroup(categoryRef);
            trx1.commit();
            UserTransaction trx2 =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            trx2.begin();
            createCategoryPredefinedProfiles(categoryRef);
            trx2.commit();
            UserTransaction trx3 =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            trx3.begin();
            for (ChildAssociationRef child : nodeService.getChildAssocs(circabcNodeRef)) {
                NodeRef childRef = child.getChildRef();
                if (nodeService
                        .getProperty(childRef, PROP_CIRCABC_PROFILE_NAME)
                        .toString()
                        .equals("CircaBCAdmin")) {
                    String circabcAdminGroupName =
                            nodeService.getProperty(childRef, PROP_CIRCABC_PROFILE_GROUP_NAME).toString();
                    permissionService.setPermission(
                            categoryRef, circabcAdminGroupName, CIRCA_CATEGORY_ADMIN, true);
                    break;
                }
            }
            trx3.commit();
            UserTransaction trx4 =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            trx4.begin();
            permissionService.setInheritParentPermissions(categoryRef, false);
            trx4.commit();
            UserTransaction trx5 =
                    serviceRegistry.getTransactionService().getNonPropagatingUserTransaction(false);
            trx5.begin();
            ownableService.setOwner(categoryRef, ADMIN);
            trx5.commit();

        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("Try to rollback transaction" + e);
            }
        }

        return categoryBody;
    }

    private void createCategoryPredefinedProfiles(NodeRef categoryRef) {

        // circaCategoryAdmin
        NodeRef categoryAdminProfileRef =
                nodeService
                        .createNode(
                                categoryRef,
                                CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
                                CATEGORY_ADMIN_PROFILE_NAME,
                                CircabcModel.TYPE_CATEGORY_PROFILE)
                        .getChildRef();
        nodeService.setProperty(
                categoryAdminProfileRef,
                ContentModel.PROP_TITLE,
                Converter.toMLTextEN("Category Administrator"));
        nodeService.setProperty(
                categoryAdminProfileRef,
                CATEGORY_PROFILE_PROP_NAME,
                CATEGORY_ADMIN_PROFILE_NAME.getLocalName());

        String newAlfGroupName = "CircaCategoryAdmin--" + GUID.generate();
        String finalAlfGroupName =
                authorityService.createAuthority(
                        AuthorityType.GROUP,
                        newAlfGroupName,
                        "GROUP_" + newAlfGroupName,
                        authorityService.getDefaultZones());

        String prefixedInvitedGroupName =
                authorityService.getName(
                        AuthorityType.GROUP,
                        nodeService
                                .getProperty(categoryRef, CircabcModel.PROP_CATEGORY_INVITED_USER_GROUP)
                                .toString());
        authorityService.addAuthority(prefixedInvitedGroupName, finalAlfGroupName);
        nodeService.setProperty(
                categoryAdminProfileRef, CATEGORY_PROFILE_GROUP_NAME, finalAlfGroupName);

        permissionService.setPermission(categoryRef, finalAlfGroupName, CIRCA_CATEGORY_ADMIN, true);

        // circaCategoryAdmin - CATEGORY permissions
        NodeRef categoryAdminCategoryRef =
                nodeService
                        .createNode(
                                categoryAdminProfileRef,
                                ASSOC_CATEGORY_SERVICE,
                                QNAME_CATEGORY_ADMIN_SERVICE,
                                TYPE_CATEGORY_SERVICE)
                        .getChildRef();
        nodeService.setProperty(
                categoryAdminCategoryRef,
                PROP_CATEGORY_ADMIN_SERVICE_NAME,
                QNAME_CATEGORY_ADMIN_SERVICE.getLocalName());
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(CIRCA_CATEGORY_ADMIN);
        nodeService.setProperty(
                categoryAdminCategoryRef, PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET, permissions);

        // ALL_CIRCA_USERS
        NodeRef registeredProfileRef =
                nodeService
                        .createNode(
                                categoryRef,
                                CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
                                ALL_CIRCA_USERS_PROFILE_NAME,
                                CircabcModel.TYPE_CATEGORY_PROFILE)
                        .getChildRef();
        nodeService.setProperty(registeredProfileRef, CATEGORY_PROFILE_PROP_NAME, EVERYONE);
        nodeService.setProperty(
                registeredProfileRef, ContentModel.PROP_TITLE, Converter.toMLTextEN("Registered"));
        nodeService.setProperty(registeredProfileRef, CATEGORY_PROFILE_GROUP_NAME, GROUP_EVERYONE);

        permissionService.setPermission(categoryRef, GROUP_EVERYONE, CIRCA_CATEGORY_ACCESS, true);

        // circaCategoryAdmin - CATEGORY permissions
        NodeRef registeredCategoryRef =
                nodeService
                        .createNode(
                                registeredProfileRef,
                                ASSOC_CATEGORY_SERVICE,
                                QNAME_CATEGORY_ADMIN_SERVICE,
                                TYPE_CATEGORY_SERVICE)
                        .getChildRef();
        nodeService.setProperty(
                registeredCategoryRef,
                PROP_CATEGORY_ADMIN_SERVICE_NAME,
                QNAME_CATEGORY_ADMIN_SERVICE.getLocalName());
        ArrayList<String> regPermissions = new ArrayList<>();
        regPermissions.add(CIRCA_CATEGORY_ACCESS);
        nodeService.setProperty(
                registeredCategoryRef, PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET, regPermissions);

        // GUEST
        NodeRef guestProfileRef =
                nodeService
                        .createNode(
                                categoryRef,
                                CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
                                GUEST_PROFILE_NAME,
                                CircabcModel.TYPE_CATEGORY_PROFILE)
                        .getChildRef();
        nodeService.setProperty(guestProfileRef, CATEGORY_PROFILE_PROP_NAME, GUEST);
        nodeService.setProperty(
                guestProfileRef, ContentModel.PROP_TITLE, Converter.toMLTextEN("Guest"));
        nodeService.setProperty(guestProfileRef, CATEGORY_PROFILE_GROUP_NAME, GUEST);

        permissionService.setPermission(categoryRef, GUEST, CIRCA_CATEGORY_ACCESS, true);

        // circaCategoryAdmin - CATEGORY permissions
        NodeRef guestCategoryRef =
                nodeService
                        .createNode(
                                guestProfileRef,
                                ASSOC_CATEGORY_SERVICE,
                                QNAME_CATEGORY_ADMIN_SERVICE,
                                TYPE_CATEGORY_SERVICE)
                        .getChildRef();

        nodeService.setProperty(
                guestCategoryRef,
                PROP_CATEGORY_ADMIN_SERVICE_NAME,
                QNAME_CATEGORY_ADMIN_SERVICE.getLocalName());
        ArrayList<String> guestPermissions = new ArrayList<>();
        guestPermissions.add(CIRCA_CATEGORY_ACCESS);
        nodeService.setProperty(
                guestCategoryRef, PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET, guestPermissions);
    }

    private NodeRef getCircabcNode() {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        NodeRef appRootNodeRef = nodeService.getRootNode(storeRef);
        NodeRef circabcRootNodeRef = null;
        for (ChildAssociationRef assoc : nodeService.getChildAssocs(appRootNodeRef)) {
            NodeRef childRef = assoc.getChildRef();
            if (nodeService
                    .getProperty(childRef, ContentModel.PROP_NAME)
                    .toString()
                    .equals("Company Home")) {
                circabcRootNodeRef =
                        nodeService.getChildByName(
                                childRef,
                                ContentModel.ASSOC_CONTAINS,
                                CircabcConfiguration.getProperty(
                                        CircabcConfiguration.CIRCABC_ROOT_NODE_NAME_PROPERTIES));
            }
        }
        return circabcRootNodeRef;
    }

    @Override
    public Category categoriesIdGet(String categoryId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        Category result = new Category();
        result.setId(categoryId);

        Serializable name = nodeService.getProperty(categoryRef, ContentModel.PROP_NAME);
        if (name != null) {
            result.setName(name.toString());
        }

        Serializable title = nodeService.getProperty(categoryRef, ContentModel.PROP_TITLE);
        if (title != null) {
            if (title instanceof String) {
                result.setTitle(Converter.toI18NProperty(title.toString()));
            } else if (title instanceof MLText) {
                result.setTitle(Converter.toI18NProperty((MLText) title));
            }
        }

        Serializable logoRef = nodeService.getProperty(categoryRef, CircabcModel.PROP_LOGO_REF);
        if (logoRef != null) {
            result.setLogoRef(logoRef.toString());
        }

        Serializable useSingleContact =
                nodeService.getProperty(categoryRef, CircabcModel.PROP_SINGLE_CONTACT);
        if (useSingleContact != null) {
            result.setUseSingleContact(Boolean.parseBoolean(useSingleContact.toString()));
        }

        Serializable contactVerified =
                nodeService.getProperty(categoryRef, CircabcModel.PROP_CONTACT_VERIFIED);
        if (contactVerified != null) {
            result.setContactVerified(Boolean.parseBoolean(contactVerified.toString()));
        }

        Serializable contactMails =
                nodeService.getProperty(categoryRef, CircabcModel.PROP_CONTACT_EMAILS);
        if (contactMails != null) {
            List<String> emails = new ArrayList<>();
            for (String email : contactMails.toString().split(";")) {
                if (!"".equals(email)) {
                    emails.add(email);
                }
            }

            result.setContactEmails(emails);
        }

        return result;
    }

    @Override
    public void categoriesIdAdminContactPost(String categoryId, AdminContactRequest body) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);

        List<String> emails = getAdminEmails(categoryId);

        EmailDefinition email = emailApi.prepareEmailForAdminContact(categoryRef, body, emails);
        email.setCopyToSender(body.getSendCopy());
        emailApi.mailPost(email);

        if (body.getSendCopy()) {
            EmailDefinition emailConfirmation =
                    emailApi.prepareConfirmationForAdminContact(categoryRef, body.getContent());
            emailApi.mailPost(emailConfirmation);
        }
    }

    private List<String> getAdminEmails(String categoryId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        List<String> emails = new ArrayList<>();

        Serializable listOfAdminMails =
                nodeService.getProperty(categoryRef, CircabcModel.PROP_CONTACT_EMAILS);
        if (listOfAdminMails != null) {
            for (String email : listOfAdminMails.toString().split(";")) {
                if (!"".equals(email)) {
                    emails.add(email);
                }
            }
        } else {
            List<User> admins = categoriesIdAdminsGet(categoryId);
            for (User user : admins) {
                emails.add(user.getEmail());
            }
        }
        return emails;
    }

    @Override
    public PagedGroupCreationRequests categoriesIdGroupRequestsGet(
            String categoryRef, Integer limit, Integer page, String filter) {

        PagedGroupCreationRequests result = new PagedGroupCreationRequests();
        result.setData(
                groupRequestsDaoService.getCategoryGroupCreationRequests(categoryRef, limit, page, filter));
        result.setTotal(
                groupRequestsDaoService
                        .getCountCategoryGroupCreationRequests(categoryRef, filter)
                        .longValue());

        return result;
    }

    public GroupRequestsDaoService getGroupRequestsDaoService() {
        return groupRequestsDaoService;
    }

    public void setGroupRequestsDaoService(GroupRequestsDaoService groupRequestsDaoService) {
        this.groupRequestsDaoService = groupRequestsDaoService;
    }

    @Override
    public void categoriesIdGroupRequestApprovalPost(
            String categoryId, GroupCreationRequestApproval body, String username) {
        if (body.getAgreement() == -1) {
            groupRequestsDaoService.updateGroupCreationRequestApproval(
                    username, body.getId(), body.getAgreement(), body.getArgument());
            GroupCreationRequest intialRequest = groupRequestGet(String.valueOf(body.getId()));
            EmailDefinition emailRefusal =
                    emailApi.prepareRefusalGroupRequest(intialRequest, body.getArgument());
            emailApi.mailPost(emailRefusal);
        } else if (body.getAgreement() == 1) {
            groupRequestsDaoService.updateGroupCreationRequestApproval(
                    username, body.getId(), body.getAgreement(), body.getArgument());
            GroupCreationRequest intialRequest = groupRequestGet(String.valueOf(body.getId()));
            EmailDefinition emailRefusal =
                    emailApi.prepareAcceptationGroupRequest(intialRequest, body.getArgument());
            emailApi.mailPost(emailRefusal);
        }
    }

    private GroupCreationRequest groupRequestGet(String requestId) {
        return groupRequestsDaoService.getCategoryGroupCreationRequests(requestId);
    }

    @Override
    public void categoriesGroupRequestPut(String requestId, GroupCreationRequest body) {
        groupRequestsDaoService.putCategoryGroupCreationRequest(requestId, body);
    }

    private class CategoryIgStatisticsRunnable implements Runnable {

        protected String userName;
        protected NodeRef categoryRef;

        public CategoryIgStatisticsRunnable(String userName, NodeRef categoryRef) {
            this.userName = userName;
            this.categoryRef = categoryRef;
        }

        public void run() {

            transactionService
                    .getRetryingTransactionHelper()
                    .doInTransaction(
                            new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

                                public Object execute() throws Throwable {

                                    AuthenticationUtil.runAs(
                                            new AuthenticationUtil.RunAsWork<String>() {
                                                @SuppressWarnings("java:S3516")
                                                public String doWork() {

                                                    if (circabcLockService.isLocked(CATEG_STAT_JOB + categoryRef.getId())) {
                                                        return null;
                                                    }

                                                    circabcLockService.lock(CATEG_STAT_JOB + categoryRef.getId());

                                                    String categoryName =
                                                            (String) nodeService.getProperty(categoryRef, ContentModel.PROP_NAME);

                                                    try {
                                                        globalStatisticsService.saveCategoryGroupStatistics(
                                                                globalStatisticsService.computeCategoryGroupStatistics(categoryRef),
                                                                categoryName);
                                                    } finally {
                                                        circabcLockService.unlock(CATEG_STAT_JOB + categoryRef.getId());
                                                    }

                                                    return null;
                                                }
                                            },
                                            userName);

                                    return null;
                                }
                            },
                            false,
                            true);
        }
    }
}
