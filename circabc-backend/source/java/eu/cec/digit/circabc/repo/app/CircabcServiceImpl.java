package eu.cec.digit.circabc.repo.app;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.ProfileModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.repo.app.model.*;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.repo.profile.category.CategoryProfileManagerServiceImpl;
import eu.cec.digit.circabc.repo.profile.interestGroup.IGRootProfileManagerServiceImpl;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserCategoryMembershipRecord;
import eu.cec.digit.circabc.service.user.UserIGMembershipRecord;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeader;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryItem;
import io.swagger.util.Converter;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class CircabcServiceImpl implements CircabcService {

    public static final String ERROR_UPDATE_PROFILE = "Error updateProfile : ";
    public static final String EN_US_UNDERSCORE = "en_US_";
    public static final String EN_GB_UNDERSCORE = "en_GB_";
    public static final String ERROR_GET_ALL_ALFRESCO_LOCALE = "Error getAllAlfrescoLocale : ";
    public static final String VISIBILITY_UPPER = "VISIBILITY";
    public static final String VISIBILITY = "Visibility";
    private static final String LOAD_MODEL = "LOAD_MODEL";
    private static final String RELOAD_GROUP_LOGOS = "RELOAD_GROUP_LOGOS";
    private static final Log logger = LogFactory.getLog(CircabcServiceImpl.class);
    private ManagementService managementService;
    private NodeService nodeService;
    private CircabcDaoServiceImpl circabcDaoService;
    private PersonService personService;
    private CircabcRootProfileManagerService circabcRootProfileManagerService;
    private CategoryProfileManagerServiceImpl categoryProfileManagerService;
    private IGRootProfileManagerServiceImpl igRootProfileManagerService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private ConfigurableService configurableService;
    private LockService lockService;
    private LogoPreferencesService logoPreferencesService;

    private String syncEnabled;
    private boolean isSyncEnabled;
    private String readFromDatabase;
    private boolean isReadFromDatabase;

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public CircabcDaoServiceImpl getCircabcDaoService() {
        return circabcDaoService;
    }

    public void setCircabcDaoService(CircabcDaoServiceImpl circabcDaoService) {
        this.circabcDaoService = circabcDaoService;
    }

    @Override
    public void loadModel() {
        if (!lockService.isLocked(LOAD_MODEL)) {
            lockService.lockForever(LOAD_MODEL);
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("Start load users");
            loadUsers();
            stopWatch.stop();
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.shortSummary());
            }
            loadCircabcAdmins();
            // to do load circabc admin
            stopWatch.start("Start load category and headers");
            final Set<NodeRef> categories = loadCategoriesAndHeaders();
            stopWatch.stop();
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.shortSummary());
            }
            stopWatch.start("Start load interest groups");
            loadInterestGroups(categories);
            stopWatch.stop();
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.shortSummary());
            }
            stopWatch.start("Start load ml properties");
            loadMultiLingualProperties();
            stopWatch.stop();
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.shortSummary());
            }
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.prettyPrint());
            }
            stopWatch.start("Start reload all group logo");
            syncGroupLogos();
            stopWatch.stop();
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.shortSummary());
            }
            if (logger.isInfoEnabled()) {
                logger.info(stopWatch.prettyPrint());
            }
        }
    }

    private void loadMultiLingualProperties() {
        try {
            circabcDaoService.insertMultilingualProperties();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertMultilingualProperties : ", e);
            }
        }
    }

    private void loadInterestGroups(final Set<NodeRef> categories) {
        for (NodeRef category : categories) {
            loadInterestGroups(category);
        }
    }

    private void loadInterestGroups(NodeRef category) {
        final List<NodeRef> interestGroups = getInterestGroups(category);
        final long categoryID =
                (Long) getNodeService().getProperty(category, ContentModel.PROP_NODE_DBID);
        for (NodeRef interestGroup : interestGroups) {
            final long interestGroupID = insertInterestGroup(categoryID, interestGroup);
            final List<Profile> profiles = igRootProfileManagerService.getProfiles(interestGroup);
            for (Profile profile : profiles) {
                insertProfile(interestGroupID, interestGroup, profile);
            }
        }
    }

    private void insertProfile(long interestGroupID, NodeRef interestGroup, Profile profile) {

        eu.cec.digit.circabc.repo.app.model.Profile prof = loadProfileInfo(interestGroupID, profile);
        if (prof != null) {
            try {
                circabcDaoService.insertProfile(prof);
                circabcDaoService.updateProfileTitles(prof.getId());
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error insertProfile : " + prof.toString(), e);
                }
            }
            final Set<String> personInProfile =
                    igRootProfileManagerService.getPersonInProfile(interestGroup, profile.getProfileName());

            if (!prof.isImported()) {
                for (String userName : personInProfile) {
                    if (!profile.getAlfrescoGroupName().equalsIgnoreCase("ALL_CIRCA_USERS")) {
                        insertProfileUser(profile.getAlfrescoGroupName(), userName);
                    }
                }
            }
        }
    }

    private eu.cec.digit.circabc.repo.app.model.Profile loadProfileInfo(
            long interestGroupID, Profile profile) {
        eu.cec.digit.circabc.repo.app.model.Profile prof = null;
        try {

            final NodeRef nodeRef = profile.getNodeRef();
            final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            String title = "";

            Serializable titleSerialized = properties.get(ContentModel.PROP_TITLE);
            if (titleSerialized instanceof String) {
                title = (String) titleSerialized;
            } else if (titleSerialized instanceof MLText) {
                title = ((MLText) titleSerialized).getDefaultValue();
            }

            String name = (String) properties.get(ProfileModel.PROP_IG_ROOT_PROFILE_NAME);

            long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
            boolean isExported = profile.isExported();
            boolean isImported = profile.isImported();
            String alfrescoGroup = profile.getAlfrescoGroupName();
            final HashMap<String, Set<String>> servicesPermissions = profile.getServicesPermissions();

            String newsgroupPermission = servicesPermissions.get("NEWSGROUP").iterator().next();
            String eventPermission = servicesPermissions.get("EVENT").iterator().next();
            String informationPermission = servicesPermissions.get("INFORMATION").iterator().next();
            String libraryPermission = servicesPermissions.get("LIBRARY").iterator().next();
            String directoryPermission = servicesPermissions.get("DIRECTORY").iterator().next();

            boolean isVisible = true;
            Set<String> visibilityPerms = servicesPermissions.get(VISIBILITY_UPPER);
            if (!visibilityPerms.isEmpty()) {
                String visibilityPermission = servicesPermissions.get(VISIBILITY_UPPER).iterator().next();
                isVisible = visibilityPermission.equalsIgnoreCase(VISIBILITY);
            }

            String igFromNodeRef = null;
            if (isImported) {
                igFromNodeRef = profile.getImportedNodeRef().toString();
            }

            prof =
                    new eu.cec.digit.circabc.repo.app.model.Profile(
                            interestGroupID,
                            id,
                            alfrescoGroup,
                            name,
                            title,
                            directoryPermission,
                            informationPermission,
                            libraryPermission,
                            newsgroupPermission,
                            eventPermission,
                            isExported,
                            isImported,
                            isVisible,
                            nodeRef.toString(),
                            igFromNodeRef);
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error while loading profile for IG id "
                                + interestGroupID
                                + " profile "
                                + profile.toString(),
                        ex);
            }
        }
        return prof;
    }

    private long insertInterestGroup(long categoryID, NodeRef interestGroup) {

        final Map<QName, Serializable> properties = nodeService.getProperties(interestGroup);

        String title = "";
        Object titleProp = properties.get(ContentModel.PROP_TITLE);
        if (titleProp instanceof MLText) {
            title = ((MLText) titleProp).getDefaultValue();
        } else if (titleProp instanceof String) {
            title = (String) titleProp;
        }

        String name = (String) properties.get(ContentModel.PROP_NAME);
        Boolean isApplyForMembership = false;
        Boolean isPublic = managementService.hasGuestVisibility(interestGroup);
        Boolean isRegistered = false;
        try {
            isRegistered = managementService.hasAllCircabcUsersVisibility(interestGroup);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error when call hasAllCircabcUsersVisibility for ig nodeRef : "
                                + interestGroup.toString(),
                        e);
            }
        }

        if (isRegistered) {
            isApplyForMembership = (Boolean) properties.get(CircabcModel.PROP_CAN_REGISTERED_APPLY);
            if (isApplyForMembership == null) {
                isApplyForMembership = true;
            }
        }

        long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
        String logoRef = "";

        InterestGroup ig =
                new InterestGroup(
                        categoryID,
                        id,
                        name,
                        title,
                        interestGroup.toString(),
                        isPublic,
                        isRegistered,
                        isApplyForMembership,
                        logoRef);
        try {
            circabcDaoService.insertInterestGroup(ig);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertInterestGroup : " + ig.toString(), e);
            }
        }
        return id;
    }

    private void loadUsers() {
        final Set<NodeRef> allPeople = personService.getAllPeople();
        Map<String, Long> allAlfrescoLocale = null;
        try {
            allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GET_ALL_ALFRESCO_LOCALE, e);
            }
        }
        if (allAlfrescoLocale != null) {
            for (NodeRef nodeRef : allPeople) {
                insertUser(nodeRef, allAlfrescoLocale);
            }
        }
    }

    private void insertUser(NodeRef nodeRef, Map<String, Long> allAlfrescoLocale) {
        final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        final long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
        final String userName = (String) properties.get(ContentModel.PROP_USERNAME);
        final String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
        final String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
        final String emailName = (String) properties.get(ContentModel.PROP_EMAIL);

        final String domain = (String) properties.get(UserModel.PROP_DOMAIN);

        final String ecasUserName = (String) properties.get(UserModel.PROP_ECAS_USER_NAME);

        final Boolean globalNotification = (Boolean) properties.get(UserModel.PROP_GLOBAL_NOTIFICATION);
        final Boolean visibility = (Boolean) properties.get(UserModel.PROP_VISISBILITY);

        String localeStr = getUILanguage(nodeRef);
        final Long localeID = allAlfrescoLocale.get(localeStr);

        User user =
                new User(
                        id,
                        userName,
                        firstName,
                        lastName,
                        emailName,
                        nodeRef.toString(),
                        localeID,
                        ecasUserName,
                        domain,
                        visibility,
                        globalNotification);
        try {
            circabcDaoService.insertUser(user);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertUser : " + user.toString(), e);
            }
        }
    }

    private String getUILanguage(NodeRef nodeRef) {
        NodeRef prefRef = null;
        if (!nodeService.hasAspect(nodeRef, ApplicationModel.ASPECT_CONFIGURABLE)) {
            return null;
        }

        // target of the assoc is the configurations folder ref
        NodeRef configRef = configurableService.getConfigurationFolder(nodeRef);
        if (configRef == null) {
            return null;
        }

        String xpath = NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
        List<NodeRef> nodes =
                searchService.selectNodes(configRef, xpath, null, namespaceService, false);

        if (nodes.size() == 1) {
            prefRef = nodes.get(0);
        } else {
            return null;
        }

        final String userInterfaceLang =
                (String) nodeService.getProperty(prefRef, UserService.PREF_INTERFACE_LANGUAGE);
        if (userInterfaceLang != null) {
            Locale locale = new Locale(userInterfaceLang);
            return DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        } else {
            return null;
        }
    }

    private void deleteCircabcAdmins() {
        circabcDaoService.deleteAllCircabcAdmins();
    }

    private void loadCircabcAdmins() {

        NodeRef circabcNodeRef = managementService.getCircabcNodeRef();
        final Set<String> personInProfile =
                circabcRootProfileManagerService.getPersonInProfile(
                        circabcNodeRef, CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN);
        for (String userName : personInProfile) {
            insertCircbcAdmin(userName);
        }
    }

    private void insertCircbcAdmin(String userName) {
        final NodeRef person = getPersonService().getPerson(userName);
        final Map<QName, Serializable> personProperties = nodeService.getProperties(person);
        final long personID = (Long) personProperties.get(ContentModel.PROP_NODE_DBID);
        CircabcAdmin circabcAdmin = new CircabcAdmin(personID);

        try {
            circabcDaoService.insertCircabcAdmin(circabcAdmin);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertCircabcAdmin : " + circabcAdmin.toString(), e);
            }
        }
    }

    private Set<NodeRef> loadCategoriesAndHeaders() {

        Set<NodeRef> headers = new HashSet<>();
        final Map<NodeRef, List<NodeRef>> categoryMap = managementService.getCategoryMap();
        for (List<NodeRef> value : categoryMap.values()) {
            // ignore categories without header
            if (!value.isEmpty() && !headers.contains(value.get(0))) {
                headers.add(value.get(0));
            }
        }

        for (NodeRef nodeRef : headers) {
            insertHeader(nodeRef);
        }
        Set<NodeRef> result = new HashSet<>(categoryMap.size());
        for (Entry<NodeRef, List<NodeRef>> item : categoryMap.entrySet()) {
            NodeRef key = item.getKey();
            List<NodeRef> value = item.getValue();
            // ignore categories without header
            if (!value.isEmpty()) {
                result.add(key);
                final Map<QName, Serializable> headerProperties = nodeService.getProperties(value.get(0));
                final long headerID = (Long) headerProperties.get(ContentModel.PROP_NODE_DBID);

                final long categoryID = insertCategory(headerID, key);
                final Set<String> personInProfile =
                        categoryProfileManagerService.getPersonInProfile(
                                key, CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN);
                for (String userName : personInProfile) {
                    insertCategoryAdmin(categoryID, userName);
                }
            }
        }
        return result;
    }

    private void insertCategoryAdmin(final long categoryID, String userName) {
        final NodeRef person = getPersonService().getPerson(userName);
        final Map<QName, Serializable> personProperties = nodeService.getProperties(person);
        final long personID = (Long) personProperties.get(ContentModel.PROP_NODE_DBID);
        CategoryAdmin catAdmin = new CategoryAdmin(personID, categoryID);
        try {
            circabcDaoService.insertCategoryAdmin(catAdmin);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertCategoryAdmin : " + catAdmin.toString(), e);
            }
        }
    }

    private void deleteCategoryAdmin(final long categoryID, String userName) {
        final NodeRef person = getPersonService().getPerson(userName);
        final Map<QName, Serializable> personProperties = nodeService.getProperties(person);
        final long personID = (Long) personProperties.get(ContentModel.PROP_NODE_DBID);
        CategoryAdmin catAdmin = new CategoryAdmin(personID, categoryID);
        try {
            circabcDaoService.deleteCategoryAdmin(catAdmin);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error deleteCategoryAdmin : " + catAdmin.toString(), e);
            }
        }
    }

    private void insertProfileUser(final String alfrescoGroup, String userName) {
        final NodeRef person = getPersonService().getPerson(userName);
        final Map<QName, Serializable> personProperties = nodeService.getProperties(person);
        final long personID = (Long) personProperties.get(ContentModel.PROP_NODE_DBID);
        ProfileUser profileUser = new ProfileUser(personID, alfrescoGroup);
        try {
            circabcDaoService.insertProfileUser(profileUser);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertProfileUse : " + profileUser.toString(), e);
            }
        }
    }

    private long insertCategory(long headerID, NodeRef key) {
        final Map<QName, Serializable> properties = nodeService.getProperties(key);
        final long categoryID = (Long) properties.get(ContentModel.PROP_NODE_DBID);
        final String name = (String) properties.get(ContentModel.PROP_NAME);
        final String title = (String) properties.get(ContentModel.PROP_TITLE);
        Category category = new Category(categoryID, name, title, key.toString(), headerID);
        try {
            circabcDaoService.insertCategory(category);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertCategory : " + category.toString(), e);
            }
        }
        return categoryID;
    }

    private void insertHeader(NodeRef nodeRef) {
        final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        final long headerID = (Long) properties.get(ContentModel.PROP_NODE_DBID);
        final String name = (String) properties.get(ContentModel.PROP_NAME);
        final String description = (String) properties.get(ContentModel.PROP_DESCRIPTION);
        Header header = new Header(headerID, name, description, nodeRef.toString());
        try {
            circabcDaoService.insertHeader(header);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertHeader : " + header.toString(), e);
            }
        }
    }

    private List<NodeRef> getInterestGroups(NodeRef category) {
        final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(category);

        final List<NodeRef> interestGroupsNodes = new ArrayList<>(assocs.size());

        NodeRef ref = null;

        for (final ChildAssociationRef assoc : assocs) {
            ref = assoc.getChildRef();

            // Secure the list of ig. No other kind of spaces can be returned
            if (nodeService.hasAspect(ref, CircabcModel.ASPECT_IGROOT)) {
                interestGroupsNodes.add(ref);
            }
        }

        return interestGroupsNodes;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public CategoryProfileManagerServiceImpl getCategoryProfileManagerService() {
        return categoryProfileManagerService;
    }

    public void setCategoryProfileManagerService(
            CategoryProfileManagerServiceImpl categoryProfileManagerService) {
        this.categoryProfileManagerService = categoryProfileManagerService;
    }

    public IGRootProfileManagerServiceImpl getIgRootProfileManagerService() {
        return igRootProfileManagerService;
    }

    public void setIgRootProfileManagerService(
            IGRootProfileManagerServiceImpl igRootProfileManagerService) {
        this.igRootProfileManagerService = igRootProfileManagerService;
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public NamespaceService getNamespaceService() {
        return namespaceService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public ConfigurableService getConfigurableService() {
        return configurableService;
    }

    public void setConfigurableService(ConfigurableService configurableService) {
        this.configurableService = configurableService;
    }

    @Override
    public void addCategoryAdmin(NodeRef categoryNodeRef, String userName) {

        if (!isSyncEnabled) {
            return;
        }
        long categoryID =
                (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID);

        Long id = getUserIDByUserName(userName);
        if (id == 0L) {
            Map<String, Long> allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
            NodeRef nodeRef = personService.getPerson(userName);
            insertUser(nodeRef, allAlfrescoLocale);
        }
        insertCategoryAdmin(categoryID, userName);
    }

    @Override
    public void removeCategoryAdmin(NodeRef categoryNodeRef, String userName) {
        if (!isSyncEnabled) {
            return;
        }
        long categoryID =
                (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID);
        deleteCategoryAdmin(categoryID, userName);
    }

    @Override
    public void addPersonToProfile(NodeRef igNodeRef, String userName, String profileName) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        String alfrescoGroup =
                getAlfrescoGroupByInterestGroupIDAndProfileName(interestGroupID, profileName);
        if (!alfrescoGroup.isEmpty()) {
            insertProfileUser(alfrescoGroup, userName);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error can not found profile "
                                + profileName
                                + " for interest group  "
                                + igNodeRef.toString());
            }
        }
    }

    private String getAlfrescoGroupByInterestGroupIDAndProfileName(
            long interestGroupID, String profileName) {
        String result = "";
        eu.cec.digit.circabc.repo.app.model.Profile profile =
                new eu.cec.digit.circabc.repo.app.model.Profile();
        profile.setInterestGroupID(interestGroupID);
        profile.setName(profileName);
        try {
            eu.cec.digit.circabc.repo.app.model.Profile resultProfile =
                    circabcDaoService.selectProfileByInterestGroupIDProfileName(profile);
            if (resultProfile != null) {
                result = resultProfile.getAlfrescoGroup();
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error getProfileIDByInterestGroupIDAndProfileName : " + profile.toString(), e);
            }
        }
        return result;
    }

    @Override
    public void uninvitePerson(NodeRef igNodeRef, String userName) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        int result = 0;
        try {
            result = circabcDaoService.deleteProfileByInterestGroupUserName(interestGroupID, userName);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error deleteProfileByInterestGroupUserName: ig id"
                                + interestGroupID
                                + " user name "
                                + userName,
                        e);
            }
        }
        if (result != 1 && logger.isErrorEnabled()) {
            logger.error(
                    "Error uninvitePerson expected one row to delete but deleted " + result + " rows");
        }
    }

    @Override
    public void changePersonProfile(NodeRef igNodeRef, String userName, String profileName) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        int result = 0;
        try {
            result = circabcDaoService.deleteProfileByInterestGroupUserName(interestGroupID, userName);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error deleteProfileByInterestGroupUserName: ig id"
                                + interestGroupID
                                + " user name "
                                + userName,
                        e);
            }
        }
        if (result == 1) {
            long userID = getUserIDByUserName(userName);
            String alfrescoGroup =
                    getAlfrescoGroupByInterestGroupIDAndProfileName(interestGroupID, profileName);
            ProfileUser profileUser = new ProfileUser(userID, alfrescoGroup);
            try {
                circabcDaoService.insertProfileUser(profileUser);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error insertProfileUse : " + profileUser.toString(), e);
                }
            }
        }
    }

    private long getUserIDByUserName(String userName) {
        long result = 0;
        try {
            result = circabcDaoService.selectUserIDByUserName(userName);

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error selectUserIDByUserName : " + userName, e);
            }
        }
        return result;
    }

    @Override
    public void updateProfile(
            NodeRef igNodeRef,
            String profileName,
            Map<String, Set<String>> servicesPermissions,
            Profile profile) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);

        NodeRef nodeRef = profile.getNodeRef();
        final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        String name = (String) properties.get(ProfileModel.PROP_IG_ROOT_PROFILE_NAME);
        String title = (String) properties.get(ContentModel.PROP_TITLE);
        if (title == null) {
            title = name;
        }

        long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
        String newsgroupPermission = servicesPermissions.get("NEWSGROUP").iterator().next();
        String eventPermission = servicesPermissions.get("EVENT").iterator().next();
        String informationPermission = servicesPermissions.get("INFORMATION").iterator().next();
        String libraryPermission = servicesPermissions.get("LIBRARY").iterator().next();
        String directoryPermission = servicesPermissions.get("DIRECTORY").iterator().next();
        String visibilityPermission = servicesPermissions.get(VISIBILITY_UPPER).iterator().next();

        final boolean isVisible = visibilityPermission.equalsIgnoreCase(VISIBILITY);
        String alfrescoGroup = profile.getAlfrescoGroupName();
        boolean isExported = profile.isExported();
        boolean isImported = profile.isImported();
        String igFromNodeRef = null;
        if (isImported) {
            igFromNodeRef = profile.getImportedNodeRef().toString();
        }
        eu.cec.digit.circabc.repo.app.model.Profile prof =
                new eu.cec.digit.circabc.repo.app.model.Profile(
                        interestGroupID,
                        id,
                        alfrescoGroup,
                        name,
                        title,
                        directoryPermission,
                        informationPermission,
                        libraryPermission,
                        newsgroupPermission,
                        eventPermission,
                        isExported,
                        isImported,
                        isVisible,
                        nodeRef.toString(),
                        igFromNodeRef);

        try {
            if (circabcDaoService.profileExists(prof.getId())) {
                circabcDaoService.updateProfile(prof);
            } else {
                circabcDaoService.insertProfile(prof);
            }
            circabcDaoService.updateProfileTitles(prof.getId());

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_UPDATE_PROFILE + prof.toString(), e);
            }
        }

        if (name.equals(CircabcConstant.GUEST_AUTHORITY)) {
            try {
                circabcDaoService.updateInterestGroupPublic(interestGroupID, isVisible);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error updateInterestGroupPublic : ", e);
                }
            }
            updateCanApplyForMembership(igNodeRef, interestGroupID);

        } else if (name.equals(CircabcConstant.REGISTERED_AUTHORITY)) {
            try {
                circabcDaoService.updateInterestGroupRegistered(interestGroupID, isVisible);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error updateInterestGroupRegistered : ", e);
                }
            }
            updateCanApplyForMembership(igNodeRef, interestGroupID);
        }
    }

    private void updateCanApplyForMembership(NodeRef igNodeRef, long interestGroupID) {

        boolean canRegisteredApply = true;
        final Serializable property =
                nodeService.getProperty(igNodeRef, CircabcModel.PROP_CAN_REGISTERED_APPLY);
        if (property != null) {
            canRegisteredApply = (Boolean) property;
        }
        try {
            circabcDaoService.updateInterestGroupApplyForMemberhip(interestGroupID, canRegisteredApply);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error updateInterestGroupApplyForMemberhip : ", e);
            }
        }
    }

    public LockService getLockService() {
        return lockService;
    }

    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    @Override
    public void exportProfile(NodeRef igNodeRef, String profileName, boolean export) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        final eu.cec.digit.circabc.repo.app.model.Profile profile =
                getProfileByInterestGroupIDAndProfileName(interestGroupID, profileName);
        if (profile != null) {
            long profileID = profile.getId();
            try {
                circabcDaoService.updateProfileExported(profile.getId(), export);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error(ERROR_UPDATE_PROFILE + profileID, e);
                }
            }
        }
    }

    @Override
    public void importProfile(
            NodeRef toIgNoderef,
            NodeRef fromIgNoderef,
            String fromProfileName,
            Map<String, Set<String>> servicesPermissions) {

        if (!isSyncEnabled) {
            return;
        }

        long fromInterestGroupID =
                (Long) getNodeService().getProperty(fromIgNoderef, ContentModel.PROP_NODE_DBID);

        final eu.cec.digit.circabc.repo.app.model.Profile sourceProfile =
                getProfileByInterestGroupIDAndProfileName(fromInterestGroupID, fromProfileName);

        long toInterestGroupID =
                (Long) getNodeService().getProperty(toIgNoderef, ContentModel.PROP_NODE_DBID);
        final List<Profile> profiles = igRootProfileManagerService.getProfiles(toIgNoderef);
        if (sourceProfile != null) {
            final String alfrescoGroup = sourceProfile.getAlfrescoGroup();

            for (Profile profile : profiles) {
                if (profile.getAlfrescoGroupName().equals(alfrescoGroup)) {
                    insertProfile(toInterestGroupID, toIgNoderef, profile);
                    break;
                }
            }
        }
    }

    private eu.cec.digit.circabc.repo.app.model.Profile getProfileByInterestGroupIDAndProfileName(
            long interestGroupID, String profileName) {
        eu.cec.digit.circabc.repo.app.model.Profile result = null;
        eu.cec.digit.circabc.repo.app.model.Profile profile =
                new eu.cec.digit.circabc.repo.app.model.Profile();
        profile.setInterestGroupID(interestGroupID);
        profile.setName(profileName);
        try {
            result = circabcDaoService.selectProfileByInterestGroupIDProfileName(profile);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error getProfileIDByInterestGroupIDAndProfileName : " + profile.toString(), e);
            }
        }
        return result;
    }

    @Override
    public void deleteProfile(NodeRef igNodeRef, String profileName) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        final eu.cec.digit.circabc.repo.app.model.Profile profile =
                getProfileByInterestGroupIDAndProfileName(interestGroupID, profileName);
        if (profile != null) {
            final long profileID = profile.getId();
            try {
                circabcDaoService.deleteProfileTitleTranslationsByID(profileID);
                circabcDaoService.deleteProfileByID(profileID);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error deleteProfileByID : " + profileID, e);
                }
            }
        }
    }

    @Override
    public void updateCanApplyForMemberhip(NodeRef igNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        updateCanApplyForMembership(igNodeRef, interestGroupID);
    }

    @Override
    public void updateProfileTitles(long igID, String profileName) {
        if (!isSyncEnabled) {
            return;
        }
        try {
            eu.cec.digit.circabc.repo.app.model.Profile profile =
                    new eu.cec.digit.circabc.repo.app.model.Profile();
            profile.setInterestGroupID(igID);
            profile.setName(profileName);
            eu.cec.digit.circabc.repo.app.model.Profile result =
                    circabcDaoService.selectProfileByInterestGroupIDProfileName(profile);
            circabcDaoService.updateProfileTitles(result.getId());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error updateProfileTitles : ", e);
            }
        }
    }

    @Override
    public void setUILangauge(String userName, String language) {
        if (!isSyncEnabled) {
            return;
        }
        Locale locale = new Locale(language);
        String localeStr = DefaultTypeConverter.INSTANCE.convert(String.class, locale);
        try {
            circabcDaoService.updateUserUILangauge(userName, localeStr);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error setUILangauge : " + userName + "," + localeStr, e);
            }
        }
    }

    @Override
    public void addCategoryNode(NodeRef headerNodeRef, NodeRef categoryNodeRef) {
        if (!isSyncEnabled) {
            return;
        }

        String nodeRef = categoryNodeRef.toString();
        long id = (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID);
        String name = (String) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NAME);
        String title = null;
        Serializable titleObj = getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_TITLE);
        if (titleObj instanceof String) {
            title = (String) titleObj;
        } else if (titleObj instanceof MLText) {
            title = ((MLText) titleObj).getDefaultValue();
        }

        if (Objects.equals(title, "") || title == null) {
            title = name;
        }
        long headerID = (Long) getNodeService().getProperty(headerNodeRef, ContentModel.PROP_NODE_DBID);
        Category category = new Category(id, name, title, nodeRef, headerID);
        try {
            circabcDaoService.insertCategory(category);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertCategory : " + category.toString(), e);
            }
        }

        updateCategoryProperties(categoryNodeRef);
    }

    @Override
    public void addInterestGroupNode(NodeRef catNodeRef, NodeRef igRootNodeRef) {

        if (!isSyncEnabled) {
            return;
        }
        long categoryID = (Long) getNodeService().getProperty(catNodeRef, ContentModel.PROP_NODE_DBID);
        long interestGroupID =
                (Long) getNodeService().getProperty(igRootNodeRef, ContentModel.PROP_NODE_DBID);
        insertInterestGroup(categoryID, igRootNodeRef);
        final List<Profile> profiles = igRootProfileManagerService.getProfiles(igRootNodeRef);
        for (Profile profile : profiles) {
            insertProfile(interestGroupID, igRootNodeRef, profile);
        }
        updateIntestGroupProperties(igRootNodeRef);
    }

    @Override
    public void addUser(NodeRef userNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        Map<String, Long> allAlfrescoLocale = null;
        try {
            allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GET_ALL_ALFRESCO_LOCALE, e);
            }
        }
        if (allAlfrescoLocale != null) {
            insertUser(userNodeRef, allAlfrescoLocale);
        }
    }

    @Override
    public void updateIntestGroupProperties(NodeRef igNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        String name = (String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME);

        Object titleObj = getNodeService().getProperty(igNodeRef, ContentModel.PROP_TITLE);
        String title = "";
        if (titleObj instanceof String) {
            title = (String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_TITLE);
        } else if (titleObj instanceof MLText) {
            title =
                    ((MLText) getNodeService().getProperty(igNodeRef, ContentModel.PROP_TITLE))
                            .getDefaultValue();
        }

        try {
            final InterestGroup interestGroup = new InterestGroup();
            interestGroup.setId(interestGroupID);
            interestGroup.setName(name);
            interestGroup.setTitle(title);
            circabcDaoService.updateInterestGroup(interestGroup);
            circabcDaoService.updateInterestGroupTitles(interestGroupID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error updateIntestGroupTitles : ", e);
            }
        }
    }

    @Override
    public void updateCategoryProperties(NodeRef catNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        long categoryID = (Long) getNodeService().getProperty(catNodeRef, ContentModel.PROP_NODE_DBID);
        String name = (String) getNodeService().getProperty(catNodeRef, ContentModel.PROP_NAME);
        String title = null;
        Serializable titleObj = getNodeService().getProperty(catNodeRef, ContentModel.PROP_TITLE);
        if (titleObj instanceof String) {
            title = (String) titleObj;
        } else if (titleObj instanceof MLText) {
            title = ((MLText) titleObj).getDefaultValue();
        }

        if (Objects.equals(title, "") || title == null) {
            title = name;
        }

        try {
            final Category category = new Category();
            category.setId(categoryID);
            category.setName(name);
            category.setTitle(title);
            circabcDaoService.updateCategory(category);
            if (titleObj instanceof String) {
                circabcDaoService.updateCategoryTitles(categoryID);
            } else if (titleObj instanceof MLText) {
                circabcDaoService.updateCategoryTitles(
                        categoryID, Converter.toI18NProperty((MLText) titleObj));
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error updateCategoryTitles : ", e);
            }
        }
    }

    @Override
    public void deleteCategory(NodeRef catNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        long categoryID = (Long) circabcDaoService.selectCategoryIDByNodeRef(catNodeRef.toString());
        try {
            circabcDaoService.deleteCategory(categoryID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error deleteCategory : ", e);
            }
        }
    }

    @Override
    public void deleteIntestGroup(NodeRef igNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        try {
            circabcDaoService.deleteInterestGroup(interestGroupID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error deleteInterestGroup : ", e);
            }
        }
    }

    @Override
    public void deleteIntestGroupByID(Long interestGroupID) {
        if (!isSyncEnabled) {
            return;
        }
        try {
            circabcDaoService.deleteInterestGroup(interestGroupID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error deleteInterestGroup : ", e);
            }
        }
    }

    @Override
    public void moveIG(NodeRef oldCategoryRef, NodeRef newCategoryRef, NodeRef igRef) {
        if (!isSyncEnabled) {
            return;
        }
        long oldCategoryID =
                (Long) getNodeService().getProperty(oldCategoryRef, ContentModel.PROP_NODE_DBID);
        long newCategoryID =
                (Long) getNodeService().getProperty(newCategoryRef, ContentModel.PROP_NODE_DBID);
        long interestGroupID = (Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID);

        try {
            circabcDaoService.moveInterestGroup(interestGroupID, oldCategoryID, newCategoryID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error moveInterestGroup : ", e);
            }
        }
    }

    @Override
    public void addHeaderNode(NodeRef headerNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        insertHeader(headerNodeRef);
    }

    @Override
    public List<InterestGroupItem> getInterestGroupByCategoryUser(
            NodeRef categoryRef, String userName) {
        if (!isReadFromDatabase) {
            return Collections.emptyList();
        }
        List<InterestGroupItem> result = new ArrayList<>();
        long categoryID = (Long) getNodeService().getProperty(categoryRef, ContentModel.PROP_NODE_DBID);
        try {
            final List<InterestGroupResult> selectIgByCategoryIDUserName =
                    circabcDaoService.selectIgByCategoryIDUserName(categoryID, userName);
            for (InterestGroupResult interestGroupResult : selectIgByCategoryIDUserName) {
                InterestGroupItem item;
                item = new InterestGroupItem(interestGroupResult);
                result.add(item);
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error getInterestGroupByCategoryUser : ", e);
            }
        }

        return result;
    }

    @Override
    public List<ExportedProfileItem> getExportedProfiles(NodeRef categoryRef, NodeRef igRef) {
        if (!isReadFromDatabase) {
            return Collections.emptyList();
        }
        long categoryID = (Long) getNodeService().getProperty(categoryRef, ContentModel.PROP_NODE_DBID);
        long interestGroupID = (Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID);
        List<ExportedProfileItem> result = new ArrayList<>();
        try {
            result =
                    circabcDaoService.selectExpProfilesByCategoryIDInterestGroupID(
                            categoryID, interestGroupID);
            final List<String> selectImporetedAlfGroups =
                    circabcDaoService.selectImporetedAlfGroups(interestGroupID);
            for (Iterator<ExportedProfileItem> iterator = result.iterator(); iterator.hasNext(); ) {
                ExportedProfileItem exportedProfileItem = iterator.next();
                if (selectImporetedAlfGroups.contains(exportedProfileItem.getPrefixedAlfrescoGroup())) {
                    iterator.remove();
                }
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error getExportedProfiles : ", e);
            }
        }

        return result;
    }

    @Override
    public Set<String> getCategoryAlfrescoGroupsExceptCurrentIG(NodeRef category, NodeRef igNodeRef) {
        if (!isReadFromDatabase) {
            return Collections.emptySet();
        }
        long categoryID = (Long) getNodeService().getProperty(category, ContentModel.PROP_NODE_DBID);
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        Set<String> result = Collections.emptySet();
        try {
            result = circabcDaoService.selectAlfrescoGroupNotInIneterstGroup(categoryID, interestGroupID);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error selectAlfrescoGroupNotInIneterstGroup : ", e);
            }
        }
        return result;
    }

    @Override
    public List<UserIGMembershipRecord> getInterestGroups(String userName) {
        if (!isReadFromDatabase) {
            return Collections.emptyList();
        }
        List<UserIGMembershipRecord> result = new ArrayList<>();
        try {
            List<UserIGMembership> list = circabcDaoService.selectInterestGroups(userName);
            for (UserIGMembership userIGMembership : list) {
                NodeRef categoNodeRef = new NodeRef(userIGMembership.getCatNodeRef());
                NodeRef igRootNodeRef = new NodeRef(userIGMembership.getIgNodeRef());
                String profileName = userIGMembership.getProfileName();
                final String category =
                        capitalize((String) nodeService.getProperty(categoNodeRef, ContentModel.PROP_NAME));

                Serializable propTitle = nodeService.getProperty(categoNodeRef, ContentModel.PROP_TITLE);
                String categoryTitle = "";
                if (propTitle instanceof MLText) {
                    MLText titles = (MLText) propTitle;
                    categoryTitle = capitalize(titles.getDefaultValue());
                } else if (propTitle instanceof String) {
                    categoryTitle = (String) propTitle;
                }

                final String interesGroup =
                        capitalize((String) nodeService.getProperty(igRootNodeRef, ContentModel.PROP_NAME));

                Serializable propIgTitle = nodeService.getProperty(igRootNodeRef, ContentModel.PROP_TITLE);
                String interesGroupTitle = "";
                if (propIgTitle instanceof MLText) {
                    MLText titles = (MLText) propIgTitle;
                    interesGroupTitle = capitalize(titles.getDefaultValue());
                } else if (propIgTitle instanceof String) {
                    interesGroupTitle = (String) propIgTitle;
                }

                String profileDisplayName = profileName;
                String alfrescoGroup = userIGMembership.getAlfrescoGroup();
                String profileNodeRefId = "";
                if (userIGMembership.getProfileNodeRefId() != null) {
                    NodeRef n = new NodeRef(userIGMembership.getProfileNodeRefId());
                    profileNodeRefId = n.getId();
                }

                Map<String, Long> allLocales = circabcDaoService.getAllAlfrescoLocale();

                if (isReadFromDatabase) {
                    final List<TranslationEntry> profileTitles =
                            circabcDaoService.selectProfileTitles(userIGMembership.getProfileId());
                    if (profileTitles.size() == 1) {
                        profileDisplayName = profileTitles.get(0).getTranslation();
                    } else if (profileTitles.size() > 1) {
                        Map<Long, String> trans = new HashMap<>();
                        for (TranslationEntry translation : profileTitles) {
                            trans.put(translation.getAlfLocaleId(), translation.getTranslation());
                        }
                        if (allLocales.containsKey(EN_US_UNDERSCORE)
                                && trans.containsKey(allLocales.get(EN_US_UNDERSCORE))) {
                            profileDisplayName = trans.get(allLocales.get(EN_US_UNDERSCORE));
                        } else if (allLocales.containsKey("en_") && trans.containsKey(allLocales.get("en_"))) {
                            profileDisplayName = trans.get(allLocales.get("en_"));
                        } else if (allLocales.containsKey(EN_GB_UNDERSCORE)
                                && trans.containsKey(allLocales.get(EN_GB_UNDERSCORE))) {
                            profileDisplayName = trans.get(allLocales.get(EN_GB_UNDERSCORE));
                        }
                    }
                } else {
                    final Profile profile =
                            getIgRootProfileManagerService().getProfile(igRootNodeRef, profileName);
                    profileDisplayName = profile.getProfileDisplayName();
                }

                result.add(
                        new UserIGMembershipRecord(
                                igRootNodeRef.getId(),
                                interesGroup,
                                categoNodeRef.getId(),
                                category,
                                profileName,
                                categoryTitle,
                                interesGroupTitle,
                                profileDisplayName,
                                alfrescoGroup,
                                profileNodeRefId));
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error getInterestGroups : ", e);
            }
        }
        return result;
    }

    private String capitalize(final String text) {
        if (text == null || text.isEmpty()) {
            return text;
        } else {
            return Character.toUpperCase(text.charAt(0)) + text.substring(1);
        }
    }

    @Override
    public List<UserCategoryMembershipRecord> getCategories(String userName) {
        if (!isReadFromDatabase) {
            return Collections.emptyList();
        }
        List<UserCategoryMembershipRecord> result = new ArrayList<>();
        try {
            List<String> list = circabcDaoService.selectCategories(userName);
            for (String item : list) {
                NodeRef categoNodeRef = new NodeRef(item);
                String category =
                        capitalize((String) nodeService.getProperty(categoNodeRef, ContentModel.PROP_NAME));

                Serializable categoryTitle =
                        nodeService.getProperty(categoNodeRef, ContentModel.PROP_TITLE);
                if (categoryTitle instanceof String) {
                    if (categoryTitle != null && categoryTitle.toString().length() > 0) {
                        category = capitalize(categoryTitle.toString());
                    }
                } else if (categoryTitle instanceof MLText) {
                    category = capitalize(((MLText) categoryTitle).getDefaultValue());
                }

                final String profile =
                        getCategoryProfileManagerService().getPersonProfile(categoNodeRef, userName);
                if ((profile != null)
                        && !profile.equals(CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME)) {
                    result.add(new UserCategoryMembershipRecord(category, profile, categoNodeRef.getId()));
                }
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error getCategories : ", e);
            }
        }
        return result;
    }

    @Override
    public void updateUser(NodeRef userNodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        final Map<QName, Serializable> properties = nodeService.getProperties(userNodeRef);
        final long id = (Long) properties.get(ContentModel.PROP_NODE_DBID);
        final String userName = (String) properties.get(ContentModel.PROP_USERNAME);
        final String firstName = (String) properties.get(ContentModel.PROP_FIRSTNAME);
        final String lastName = (String) properties.get(ContentModel.PROP_LASTNAME);
        final String emailName = (String) properties.get(ContentModel.PROP_EMAIL);

        final String domain = (String) properties.get(UserModel.PROP_DOMAIN);

        final String ecasUserName = (String) properties.get(UserModel.PROP_ECAS_USER_NAME);

        final Boolean globalNotification = (Boolean) properties.get(UserModel.PROP_GLOBAL_NOTIFICATION);
        final Boolean visibility = (Boolean) properties.get(UserModel.PROP_VISISBILITY);

        String localeStr = getUILanguage(userNodeRef);
        Map<String, Long> allAlfrescoLocale = null;
        try {
            allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GET_ALL_ALFRESCO_LOCALE, e);
            }
        }
        Long localeID = 0L;
        if (allAlfrescoLocale != null) {
            localeID = allAlfrescoLocale.get(localeStr);
        }

        User user =
                new User(
                        id,
                        userName,
                        firstName,
                        lastName,
                        emailName,
                        userNodeRef.toString(),
                        localeID,
                        ecasUserName,
                        domain,
                        visibility,
                        globalNotification);
        try {
            circabcDaoService.updateUser(user);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error insertUser : " + user.toString(), e);
            }
        }
    }

    @Override
    public boolean syncEnabled() {
        return isSyncEnabled;
    }

    @Override
    public boolean readFromDatabase() {
        return isReadFromDatabase;
    }

    public String getSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(String syncEnabled) {
        this.syncEnabled = syncEnabled;
        isSyncEnabled = Boolean.valueOf(syncEnabled);
    }

    public String getReadFromDatabase() {
        return readFromDatabase;
    }

    public void setReadFromDatabase(String readFromDatabase) {
        this.readFromDatabase = readFromDatabase;
        isReadFromDatabase = Boolean.valueOf(readFromDatabase);
    }

    @Override
    public void deleteHeader(NodeRef nodeRef) {
        if (!isSyncEnabled) {
            return;
        }
        try {
            circabcDaoService.deleteHeader(nodeRef.toString());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error deleteHeader : ", e);
            }
        }
    }

    @Override
    public void updateHeader(NodeRef headerNodeRef, String name, String description) {
        if (!isSyncEnabled) {
            return;
        }
        try {
            Header header = new Header();
            header.setNodeRef(headerNodeRef.getId());
            header.setName(name);
            header.setDescription(description);
            circabcDaoService.updateHeader(header);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error updateHeader : ", e);
            }
        }
    }

    @Override
    public void resyncInterestGroup(NodeRef nodeRef) {
        if (getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            deleteIntestGroup(nodeRef);
            final NodeRef categoryNodeRef =
                    getNodeService().getParentAssocs(nodeRef).iterator().next().getParentRef();
            long categoryID = circabcDaoService.selectCategoryIDByNodeRef(categoryNodeRef.toString());
            long interestGroupNodeDBID =
                    (Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
            final long interestGroupID = insertInterestGroup(categoryID, nodeRef);

            try {
                DefaultLogoConfiguration logoDefinition = logoPreferencesService.getDefault(nodeRef);
                String logoId = "";
                if (logoDefinition != null && logoDefinition.getLogo() != null) {
                    NodeRef logoRef = logoDefinition.getLogo().getReference();
                    if (logoRef != null && nodeService.exists(logoRef)) logoId = logoRef.toString();
                }
                circabcDaoService.updateLogoIdForGroup(interestGroupNodeDBID, logoId);
            } catch (CustomizationException e) {
                logger.error("Impossible to update the logo ref in the group resync", e);
            }

            final List<Profile> profiles = igRootProfileManagerService.getProfiles(nodeRef);
            for (Profile profile : profiles) {
                insertProfile(interestGroupID, nodeRef, profile);
            }
            circabcDaoService.insertMultilingualPropertiesByInterestGroupID(interestGroupNodeDBID);
        }
    }

    @Override
    public void resyncCategory(NodeRef nodeRef) {
        final List<NodeRef> interestGroups = getInterestGroups(nodeRef);
        for (NodeRef igNodeRef : interestGroups) {
            resyncInterestGroup(igNodeRef);
        }
    }

    @Override
    public void resyncUsers() {
        final Set<NodeRef> allPeople = personService.getAllPeople();
        Map<String, Long> allAlfrescoLocale = null;
        try {
            allAlfrescoLocale = circabcDaoService.getAllAlfrescoLocale();
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_GET_ALL_ALFRESCO_LOCALE, e);
            }
        }
        if (allAlfrescoLocale != null) {
            final List<String> selectAllUserNodeRef = circabcDaoService.selectAllUserNodeRef();
            Set<String> userNodeRefs = new HashSet<>(selectAllUserNodeRef.size());
            userNodeRefs.addAll(selectAllUserNodeRef);
            for (NodeRef nodeRef : allPeople) {
                if (!userNodeRefs.contains(nodeRef.toString())) {
                    insertUser(nodeRef, allAlfrescoLocale);
                }
            }
        }
    }

    @Override
    public List<CategoryHeader> getHeadersAndCategories() {
        List<CategoryHeader> result = new ArrayList<>();
        final List<HeaderCategory> headerCategoryList = circabcDaoService.selectHeadersCategories();
        String currentHeaderNodeRef = null;
        CategoryHeader currentCategoryHeader = null;
        for (HeaderCategory headerCategory : headerCategoryList) {
            if (Objects.equals(headerCategory.getHeaderNodeRef(), currentHeaderNodeRef)) {
                String categoryTitle = headerCategory.getCategoryTitle();
                String categoryName = headerCategory.getCategoryName();
                NodeRef categoryNodeRef = new NodeRef(headerCategory.getCategoryNodeRef());
                String categoryID = categoryNodeRef.getId();
                Boolean isLink = false;
                CategoryItem categoryItem =
                        new CategoryItem(categoryID, categoryTitle, isLink, categoryName, categoryNodeRef);
                currentCategoryHeader.getCategories().add(categoryItem);
            } else {
                currentCategoryHeader = new CategoryHeader();
                currentCategoryHeader.setCategories(new ArrayList<CategoryItem>());
                currentCategoryHeader.setLinks(Collections.<CategoryItem>emptyList());

                // add header
                String headerTitle = headerCategory.getHeaderTitle();
                String headerName = headerCategory.getHeaderName();
                NodeRef headerNodeRef = new NodeRef(headerCategory.getHeaderNodeRef());
                String headerID = headerNodeRef.getId();
                CategoryItem headerItem =
                        new CategoryItem(headerID, headerTitle, false, headerName, headerNodeRef);

                currentCategoryHeader.setCategoryHeaderItem(headerItem);

                // add category
                String categoryTitle = headerCategory.getCategoryTitle();
                String categoryName = headerCategory.getCategoryName();
                NodeRef categoryNodeRef = new NodeRef(headerCategory.getCategoryNodeRef());
                String categoryID = categoryNodeRef.getId();
                Boolean isLink = false;
                CategoryItem categoryItem =
                        new CategoryItem(categoryID, categoryTitle, isLink, categoryName, categoryNodeRef);
                currentCategoryHeader.getCategories().add(categoryItem);

                result.add(currentCategoryHeader);
                currentHeaderNodeRef = headerCategory.getHeaderNodeRef();
            }
        }

        return result;
    }

    @Override
    public List<ProfileWithUsersCount> getProfiles(NodeRef nodeRef, long localeID) {
        List<ProfileWithUsersCount> result = Collections.emptyList();
        if (getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            long interestGroupID =
                    (Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
            result = circabcDaoService.selectProfiles(interestGroupID, localeID);
        }
        return result;
    }

    @Override
    public Long getUserLocaleID(String userName) {
        final Long localeID = circabcDaoService.selectLocaleIDByUserName(userName);
        if (localeID == null) {
            // default locale
            return 1L;
        } else {
            return localeID;
        }
    }

    @Override
    public List<UserWithProfile> getFilteredUsers(
            NodeRef igNodeRef, long localeID, String alfrescoGroup, String text) {

        return getFilteredUsers(igNodeRef, localeID, alfrescoGroup, text, null);
    }

    @Override
    public List<UserWithProfile> getFilteredUsers(
            NodeRef igNodeRef, long localeID, String alfrescoGroup, String text, String order) {
        List<UserWithProfile> result = Collections.emptyList();
        if (getNodeService().hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
            long igID = (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);

            result = circabcDaoService.selectUsersProfiles(igID, localeID, alfrescoGroup, text, order);
        }
        return result;
    }

    @Override
    public List<eu.cec.digit.circabc.repo.app.model.Profile> getProfilesForIg(
            NodeRef igNodeRef, long localeID, String searchQuery) {
        List<eu.cec.digit.circabc.repo.app.model.Profile> result = Collections.emptyList();
        if (getNodeService().hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
            long igID = (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
            result = circabcDaoService.selectProfileTitleAlfGroupByIgId(igID, localeID, searchQuery);
        }
        return result;
    }

    @Override
    public Integer getNumberOfAdmintrators(NodeRef igNodeRef) {
        Integer result = 0;
        if (getNodeService().hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
            long igID = (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
            result = circabcDaoService.getCountOfIGAdmins(igID);
        }
        return result;
    }

    @Override
    public void postOrUpdateProfile(io.swagger.model.Profile profile) {

        if (!isSyncEnabled) {
            return;
        }

        NodeRef profileRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, profile.getId());

        NodeRef igNodeRef = nodeService.getPrimaryParent(profileRef).getParentRef();
        long interestGroupID =
                (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);

        long id = (Long) nodeService.getProperty(profileRef, ContentModel.PROP_NODE_DBID);
        Map<String, String> permissions = profile.getPermissions();
        String newsgroupPermission = permissions.get("newsgroups");
        String eventPermission = permissions.get("events");
        String informationPermission = permissions.get("information");
        String libraryPermission = permissions.get("library");
        String directoryPermission = permissions.get("members");
        String visibilityPermission = permissions.get("visibility");

        boolean isVisible = visibilityPermission.equalsIgnoreCase(VISIBILITY);
        String alfrescoGroup =
                nodeService
                        .getProperty(profileRef, CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME)
                        .toString();
        boolean isExported = profile.getExported();
        boolean isImported = profile.getImported();
        String igFromNodeRef = null;
        if (isImported) {
            igFromNodeRef =
                    nodeService.getProperty(profileRef, ProfileModel.PROP_PROFILE_IMPORTED_REF).toString();
        }

        String title = profile.getName();
        if (profile.getTitle().containsKey("en")) {
            title = profile.getTitle().get("en");
        }

        eu.cec.digit.circabc.repo.app.model.Profile prof =
                new eu.cec.digit.circabc.repo.app.model.Profile(
                        interestGroupID,
                        id,
                        alfrescoGroup,
                        profile.getName(),
                        title,
                        directoryPermission,
                        informationPermission,
                        libraryPermission,
                        newsgroupPermission,
                        eventPermission,
                        isExported,
                        isImported,
                        isVisible,
                        profileRef.toString(),
                        igFromNodeRef);

        try {

            if (prof.getAlfrescoGroup().startsWith("GROUP_")) {
                prof.setAlfrescoGroup(prof.getAlfrescoGroup().replace("GROUP_", ""));
            }

            if (circabcDaoService.profileExists(prof.getId())) {
                circabcDaoService.updateProfile(prof);
            } else {
                circabcDaoService.insertProfile(prof);
            }

            circabcDaoService.updateProfileTitle(prof.getId(), profile.getTitle());

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(ERROR_UPDATE_PROFILE + prof.toString(), e);
            }
        }

        if (profile.getName().equals(CircabcConstant.GUEST_AUTHORITY)) {
            try {
                circabcDaoService.updateInterestGroupPublic(interestGroupID, isVisible);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error updateInterestGroupPublic : ", e);
                }
            }
            updateCanApplyForMembership(igNodeRef, interestGroupID);

        } else if (profile.getName().equals(CircabcConstant.REGISTERED_AUTHORITY)) {
            try {
                circabcDaoService.updateInterestGroupRegistered(interestGroupID, isVisible);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error updateInterestGroupRegistered : ", e);
                }
            }
            updateCanApplyForMembership(igNodeRef, interestGroupID);
        }
    }

    @Override
    public boolean isUserMember(NodeRef igNodeRef, String userName) {
        long igID = (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.getIsMemberOfGroup(igID, userName) > 0;
    }

    @Override
    public boolean isCategoryAdmin(NodeRef categoryNodeRef, String userName) {
        long categoryID =
                (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.getIsCategoryAdmin(categoryID, userName) > 0;
    }

    @Override
    public boolean isCategoryAdminOfInterestGroup(NodeRef interestGroupNodeRef, String userName) {
        long interestGroupID =
                (Long) getNodeService().getProperty(interestGroupNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.getIsCategoryAdminOfInterestGroup(interestGroupID, userName) > 0;
    }

    @Override
    public boolean isCircabcAdmin(String userName) {
        return circabcDaoService.getIsCircabcAdmin(userName) > 0;
    }

    @Override
    public void addCircabcAdmin(String userName) {
        if (!isSyncEnabled) {
            return;
        }
        insertCircbcAdmin(userName);
    }

    @Override
    public void removeCircabcAdmin(String userName) {
        if (!isSyncEnabled) {
            return;
        }
        deleteCircabcAdmin(userName);
    }

    private void deleteCircabcAdmin(String userName) {
        final NodeRef person = getPersonService().getPerson(userName);
        final Map<QName, Serializable> personProperties = nodeService.getProperties(person);
        final long personID = (Long) personProperties.get(ContentModel.PROP_NODE_DBID);
        CircabcAdmin circabcAdmin = new CircabcAdmin(personID);
        try {
            circabcDaoService.deleteCircabcAdmin(circabcAdmin);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error deleteCircabcAdmin : " + circabcAdmin.toString(), e);
            }
        }
    }

    @Override
    public void resyncCircabcAdmins() {
        deleteCircabcAdmins();
        loadCircabcAdmins();
    }

    @Override
    public InterestGroupResult getInterestGroup(NodeRef interestGroupNodeRef) {
        long igID =
                (Long) getNodeService().getProperty(interestGroupNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.selectIgByID(igID);
    }

    public CircabcRootProfileManagerService getCircabcRootProfileManagerService() {
        return circabcRootProfileManagerService;
    }

    public void setCircabcRootProfileManagerService(
            CircabcRootProfileManagerService circabcRootProfileManagerService) {
        this.circabcRootProfileManagerService = circabcRootProfileManagerService;
    }

    @Override
    public List<String> getCategoryAdmins(NodeRef categoryNodeRef) {
        long categID =
                (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.selectCategoryAdmins(categID);
    }

    @Override
    public void deletePersonFromGroup(NodeRef igNodeRef, String userName) {

        Long groupId = (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);

        List<ProfileUser> userProfile = circabcDaoService.getUserProfileInGroup(groupId, userName);
        for (ProfileUser membership : userProfile) {
            circabcDaoService.deleteUserInGroup(membership);
        }
    }

    @Override
    public void updateLogoRefProperty(NodeRef categoryNodeRef) {
        long categID =
                (Long) getNodeService().getProperty(categoryNodeRef, ContentModel.PROP_NODE_DBID);
        NodeRef logoRef =
                (NodeRef) getNodeService().getProperty(categoryNodeRef, CircabcModel.PROP_LOGO_REF);
        String logoId = "";
        if (logoRef != null) {
            logoId = logoRef.getId();
        }
        circabcDaoService.updateLogoIdForCategory(categID, logoId);
    }

    @Override
    public void setGroupLogoNode(NodeRef igNodeRef, NodeRef logoNodeRef) {
        long igID = (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);
        if (logoNodeRef != null) {
            circabcDaoService.updateLogoIdForGroup(igID, logoNodeRef.toString());
        } else {
            circabcDaoService.updateLogoIdForGroup(igID, null);
        }
    }

    @Override
    public Map<String, String> getInterestGroupTitle(NodeRef igRef) {
        long igID = (Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.getGroupTitleTranslations(igID);
    }

    @Override
    public Map<String, String> getProfileTitle(NodeRef profileNodeRef) {
        long profileId =
                (Long) getNodeService().getProperty(profileNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.getProfileTitleTranslations(profileId);
    }

    @Override
    public Map<String, String> getCategoryTitle(NodeRef categNodeRef) {
        long categId = (Long) getNodeService().getProperty(categNodeRef, ContentModel.PROP_NODE_DBID);
        return circabcDaoService.getCategoryTitleTranslations(categId);
    }

    @Override
    public boolean isUserExists(String userName) {
        try {
            Long id = getUserIDByUserName(userName);
            return (id > 0L);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error in isUserExists", e);
            }
            return false;
        }
    }

    @Override
    public void updateInterestGroupPublic(NodeRef igRef, Boolean isPublic) {
        if (igRef != null && isPublic != null) {
            long igId = (Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID);
            circabcDaoService.updateInterestGroupPublic(igId, isPublic);
        }
    }

    @Override
    public void updateInterestGroupRegistered(NodeRef igRef, Boolean isRegistered) {
        if (igRef != null && isRegistered != null) {
            long igId = (Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID);
            circabcDaoService.updateInterestGroupRegistered(igId, isRegistered);
        }
    }

    @Override
    public void updateInterestGroupApplication(NodeRef igRef, Boolean applicationAllowed) {
        if (igRef != null && applicationAllowed != null) {
            long igId = (Long) getNodeService().getProperty(igRef, ContentModel.PROP_NODE_DBID);
            circabcDaoService.updateInterestGroupApplyForMemberhip(igId, applicationAllowed);
        }
    }

    @Override
    public void deleteAll() {
        circabcDaoService.deleteAll();
    }

    @Override
    public void resyncAll() {
        boolean isLocked = lockService.isLocked(LOAD_MODEL);
        if (isLocked) {
            lockService.unlock(LOAD_MODEL);
        }
        isLocked = lockService.isLocked(RELOAD_GROUP_LOGOS);
        if (isLocked) {
            lockService.unlock(RELOAD_GROUP_LOGOS);
        }
        deleteAll();
        loadModel();
    }

    /**
     * @see eu.cec.digit.circabc.service.app.CircabcService#countMembersInIg(java.lang.String)
     */
    @Override
    public int countMembersInIg(String id) {

        NodeRef igNodeRef = Converter.createNodeRefFromId(id);

        long igDBId = (long) nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);

        return circabcDaoService.countUsersInIg(igDBId);
    }

    @Override
    public Set<String> getUserIds(String interestGroupId) {

        NodeRef igNodeRef = Converter.createNodeRefFromId(interestGroupId);

        long igDBId = (long) nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID);

        return circabcDaoService.getUserIds(igDBId);
    }

    @Override
    public void addUser(String userName) {
        NodeRef userRef = personService.getPerson(userName);
        if (userRef != null) {
            addUser(userRef);
        }
    }

    public void setLogoPreferencesService(LogoPreferencesService logoPreferencesService) {
        this.logoPreferencesService = logoPreferencesService;
    }

    @Override
    public boolean isUserDirAdminOrCategoryAdminOrCircabcAdmin(String userName) {
        return circabcDaoService.selectCountAdminDByUserName(userName) > 0L;
    }

    /**
     * RUNS ONCE + LOCK Never release the lock
     */
    @Override
    public void syncGroupLogos() {
        if (!lockService.isLocked(RELOAD_GROUP_LOGOS)) {
            lockService.lockForever(RELOAD_GROUP_LOGOS);
            List<InterestGroupResult> allGroups = circabcDaoService.getAllInterestGroups();
            for (InterestGroupResult group : allGroups) {
                NodeRef nodeRef = new NodeRef(group.getNodeRef());
                if (nodeService.exists(nodeRef)) {
                    Long interestGroupNodeDBID =
                            (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
                    try {
                        DefaultLogoConfiguration logoDefinition = logoPreferencesService.getDefault(nodeRef);
                        String logoId = "";
                        if (logoDefinition != null && logoDefinition.getLogo() != null) {
                            NodeRef logoRef = logoDefinition.getLogo().getReference();
                            if (logoRef != null && nodeService.exists(logoRef)) logoId = logoRef.toString();
                        }
                        circabcDaoService.updateLogoIdForGroup(interestGroupNodeDBID, logoId);
                    } catch (CustomizationException e) {
                        logger.error("Impossible to update the logo ref in the ALL group resync logos", e);
                    }
                }
            }
        }
    }
}
