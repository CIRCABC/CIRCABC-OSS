package io.swagger.api;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.action.CircabcImporterActionExecuter;
import eu.cec.digit.circabc.action.config.ImportConfig;
import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.model.ModerationModel;
import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.repo.app.CircabcDaoServiceImpl;
import eu.cec.digit.circabc.repo.app.model.InterestGroupResult;
import eu.cec.digit.circabc.repo.app.model.UserWithProfile;
import eu.cec.digit.circabc.repo.customisation.CustomizationException;
import eu.cec.digit.circabc.repo.log.ActivityCountDAO;
import eu.cec.digit.circabc.repo.notification.NotifiableUserImpl;
import eu.cec.digit.circabc.repo.statistics.ig.Child;
import eu.cec.digit.circabc.repo.statistics.ig.ServiceTreeRepresentation;
import eu.cec.digit.circabc.repo.statistics.ig.StatData;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.customisation.logo.DefaultLogoConfiguration;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.iam.SynchronizationService;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationType;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileException;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.statistics.ig.IgStatisticsService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.mail.MessagingException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author beaurpi
 */
public class GroupsApiImpl implements GroupsApi {

    private static final String GUEST = "guest";
    private static final String GROUP_EVERYONE = "GROUP_EVERYONE";
    private static final String EVERYONE = "EVERYONE";
    private static final String NO_VISIBILITY = "NoVisibility";
    private static final String VISIBILITY = "Visibility";
    private static final String MODIFIED = "modified";
    private static final String ESCAPE_QUOTES = "\" ";
    private static final String START_PROP_SEARCH = "@";
    private static final String CLOSE_QUERY = " )";
    private static final String OPEN_QUERY = "( ";
    private static final String TO = " TO ";
    private static final String OPEN_BRACKETS = ":[";
    private static final String CLOSE_BRACKETS = "] ";
    private static final String AND = " AND ";
    private static final String PATH = "PATH:";
    private static final String ESCAPE4 = "\\\\:";
    private static final String ESCAPE3 = "\\:";
    private static final String ESCAPE1 = "\\-";
    private static final String ESCAPE2 = "\\\\-";
    @SuppressWarnings("java:S5361")
    private static final String PROP_MODIFIED_ESCAPED =
            ContentModel.PROP_MODIFIED
                    .toString()
                    .replaceAll(":", ESCAPE4)
                    .replaceAll("\\{", "\\\\{")
                    .replaceAll("\\}", "\\\\}");
    @SuppressWarnings("java:S5361")
    private static final String PROP_CREATED_ESCAPED =
            ContentModel.PROP_CREATED
                    .toString()
                    .replaceAll(":", ESCAPE4)
                    .replaceAll("\\{", "\\\\{")
                    .replaceAll("\\}", "\\\\}");
    private static final String CLEAN = "clean";
    private static final String DECLINE = "decline";
    private static final String KEY_APPLICATION_MESSAGE = "applicationMessage";
    private static final String KEY_APPLICATION_DATE = "applicationDate";
    private static final String MEMBERS = "members";
    private static final String DIRECTORY_ADMIN_PERMISSION = "DirAdmin";
    private static final String MESSAGE = "message";
    private static final String KEY_REASON = "reason";

    private static final Map<String, String> statisticsIdentifierMap;
    private static final String DIRECTORY = "directory";
    private static final String VISIBILITY_SMALL = "visibility";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String NEWSGROUPS = "newsgroups";
    private static final String INFORMATION = "information";
    private static final String IG_LOOK_AND_FEEL = "iglookAndFeel";
    private static final String LIBRARY = "library";
    private static final String GROUP = "GROUP_";
    private static final String EVENTS = "events";
    private static final String MM_YYYY = "MM-yyyy";
    private static final String IMAGES = "images";

    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("summary.statistics.created.date", "Library created date");
        aMap.put("summary.statistics.number.of.users", "Library number of users");
        aMap.put("summary.statistics.library.folder.count", "Library folder count");
        aMap.put("summary.statistics.library.document.count", "Library document count");
        aMap.put("summary.statistics.library.size", "Library size");
        aMap.put("summary.statistics.information.folder.count", "Information folder count");
        aMap.put("summary.statistics.information.document.count", "Information document count");
        aMap.put("summary.statistics.information.size", "Information size");
        aMap.put("summary.statistics.version.count", "Version count");
        aMap.put("summary.statistics.version.size", "Version size");
        aMap.put("summary.statistics.total.size", "Total size");
        aMap.put("summary.statistics.event.count", "Event count");
        aMap.put("summary.statistics.meeting.count", "Meeting count");
        aMap.put("summary.statistics.forum.count", "Forum count");
        aMap.put("summary.statistics.topic.count", "Topic count");
        aMap.put("summary.statistics.post.count", "Post count");
        statisticsIdentifierMap = Collections.unmodifiableMap(aMap);
    }

    private final Log logger = LogFactory.getLog(GroupsApiImpl.class);
    private NodeService nodeService;
    private NodeService secureNodeService;
    private PermissionService permissionService;
    private SearchService searchService;
    private NamespaceService namespaceService;
    private CircabcService circabcService;
    private CircabcDaoServiceImpl circabcDaoService;
    private IGRootProfileManagerService igRootProfileManagerService;
    private PersonService personService;
    private UserService userService;
    private NotificationService notificationService;
    private LdapUserService ldapUserService;
    private LogoPreferencesService logoPreferencesService;
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    private MailService mailService;
    private ProfilesApi profilesApi;
    private MailPreferencesService mailPreferencesService;
    private ApiToolBox apiToolBox;
    private IgStatisticsService statisticsService;
    private ContentService contentService;
    private ActionService actionService;
    private BehaviourFilter policyBehaviourFilter;
    private UsersApi usersApi;
    private NodesApi nodesApi;
    private SynchronizationService synchronizationService;
    private LogService logService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private MimetypeService mimetypeService;
    private ManagementService managementService;
    private ImportConfig config;
    private ThreadPoolExecutor asyncThreadPoolExecutor;
    private TransactionService transactionService;
    private LockService circabcLockService;
    private HistoryApi historyApi;
    private RuleService ruleService;
    private NodeArchiveService nodeArchiveService;

    @Override
    public InterestGroup getInterestGroupDetails(final NodeRef igRef) {
        return getInterestGroupDetails(igRef, false);
    }

    @Override
    public InterestGroup getInterestGroupDetails(final NodeRef igRef, final boolean lightMode) {
        InterestGroup interestGroup = new InterestGroup();
        interestGroup.setId(igRef.getId());
        interestGroup.setName((String) nodeService.getProperty(igRef, ContentModel.PROP_NAME));

        final Serializable property = nodeService.getProperty(igRef, ContentModel.PROP_TITLE);
        if (property instanceof MLText) {
            interestGroup.setTitle(Converter.toI18NProperty((MLText) property));
        } else if (property instanceof String) {
            interestGroup.setTitle(Converter.toI18NProperty((String) property));
        }

        if (!lightMode) {
            final Serializable description =
                    nodeService.getProperty(igRef, ContentModel.PROP_DESCRIPTION);
            if (description instanceof MLText) {
                interestGroup.setDescription(Converter.toI18NProperty((MLText) description));
            } else if (description instanceof String) {
                interestGroup.setDescription(Converter.toI18NProperty((String) description));
            }

            final Serializable contact =
                    nodeService.getProperty(igRef, CircabcModel.PROP_CONTACT_INFORMATION);
            if (contact instanceof MLText) {
                interestGroup.setContact(Converter.toI18NProperty((MLText) contact));
            } else if (contact instanceof String) {
                interestGroup.setContact(Converter.toI18NProperty((String) contact));
            }

            if (circabcService.readFromDatabase()) {
                InterestGroupResult interestGroupResult = circabcService.getInterestGroup(igRef);
                if (interestGroupResult != null) {
                    interestGroup.setIsPublic(interestGroupResult.getIsPublic());
                    interestGroup.setIsRegistered(interestGroupResult.getIsRegistered());
                    interestGroup.setAllowApply(interestGroupResult.getIsApplyForMembership());
                }

            } else {
                final Set<AccessPermission> allSetPermissions =
                        permissionService.getAllSetPermissions(igRef);
                interestGroup.setIsPublic(isAuthorityVisible(allSetPermissions, GUEST));
                boolean isRegistered = isAuthorityVisible(allSetPermissions, GROUP_EVERYONE);
                interestGroup.setIsRegistered(isRegistered);
                if (isRegistered) {
                    interestGroup.setAllowApply(allowApply(igRef));
                } else {
                    interestGroup.setAllowApply(false);
                }
            }

            try {
                final DefaultLogoConfiguration logoPreference =
                        logoPreferencesService.getOrCreateConfiguraton(igRef, false);
                if ((logoPreference != null) && logoPreference.isLogoDisplayedOnMainPage()) {
                    if (logoPreference.getLogo() != null) {
                        NodeRef logoRef = logoPreference.getLogo().getReference();
                        String logoName = logoPreference.getLogo().getName();
                        interestGroup.setLogoUrl(logoRef.getId() + "/" + logoName);
                    }
                }
            } catch (Exception e) {
                logger.error("Error during retrieving logo definition", e);
            }

            NodeRef libRef = nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Library");
            boolean libPermFound = false;
            for (LibraryPermissions libPermDef : LibraryPermissions.values()) {
                if (!libPermDef.equals(LibraryPermissions.LIBNOACCESS)) {
                    if (permissionService
                            .hasPermission(libRef, libPermDef.toString())
                            .equals(AccessStatus.ALLOWED)) {
                        interestGroup.getPermissions().put(LIBRARY, libPermDef.toString());
                        libPermFound = true;
                        break;
                    }
                }
            }
            if (!libPermFound) {
                interestGroup.getPermissions().put(LIBRARY, LibraryPermissions.LIBNOACCESS.toString());
            }

            NodeRef infRef =
                    nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Information");
            boolean infPermFound = false;
            for (InformationPermissions infPermDef : InformationPermissions.values()) {
                if (!infPermDef.equals(InformationPermissions.INFNOACCESS)) {
                    if (permissionService
                            .hasPermission(infRef, infPermDef.toString())
                            .equals(AccessStatus.ALLOWED)) {
                        interestGroup.getPermissions().put(INFORMATION, infPermDef.toString());
                        infPermFound = true;
                        break;
                    }
                }
            }
            if (!infPermFound) {
                interestGroup
                        .getPermissions()
                        .put(INFORMATION, InformationPermissions.INFNOACCESS.toString());
            }

            NodeRef eventRef = nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Events");
            boolean eventPermFound = false;
            for (EventPermissions evtPermDef : EventPermissions.values()) {
                if (!evtPermDef.equals(EventPermissions.EVENOACCESS)) {
                    if (permissionService
                            .hasPermission(eventRef, evtPermDef.toString())
                            .equals(AccessStatus.ALLOWED)) {
                        interestGroup.getPermissions().put("event", evtPermDef.toString());
                        eventPermFound = true;
                        break;
                    }
                }
            }
            if (!eventPermFound) {
                interestGroup.getPermissions().put("event", EventPermissions.EVENOACCESS.toString());
            }

            NodeRef newsRef =
                    nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Newsgroups");
            boolean newsPermFound = false;
            for (NewsGroupPermissions newsPermDef : NewsGroupPermissions.values()) {
                if (!newsPermDef.equals(NewsGroupPermissions.NWSNOACCESS)) {
                    if (permissionService
                            .hasPermission(newsRef, newsPermDef.toString())
                            .equals(AccessStatus.ALLOWED)) {
                        interestGroup.getPermissions().put("newsgroup", newsPermDef.toString());
                        newsPermFound = true;
                        break;
                    }
                }
            }
            if (!newsPermFound) {
                interestGroup
                        .getPermissions()
                        .put("newsgroup", NewsGroupPermissions.NWSNOACCESS.toString());
            }

            boolean dirPermFound = false;

            for (DirectoryPermissions dirPermDef : DirectoryPermissions.values()) {
                if (!dirPermDef.equals(DirectoryPermissions.DIRNOACCESS)) {
                    if (permissionService
                            .hasPermission(igRef, dirPermDef.toString())
                            .equals(AccessStatus.ALLOWED)) {
                        interestGroup.getPermissions().put(DIRECTORY, dirPermDef.toString());
                        dirPermFound = true;
                        break;
                    }
                }
            }

            if (!dirPermFound) {
                interestGroup
                        .getPermissions()
                        .put(DIRECTORY, DirectoryPermissions.DIRNOACCESS.toString());
            }

            if (permissionService.hasPermission(igRef, "IgDelete").equals(AccessStatus.ALLOWED)) {
                interestGroup.getPermissions().put("IgDelete", "true");
            }

            List<ChildAssociationRef> igChildren = nodeService.getChildAssocs(igRef);
            for (ChildAssociationRef igChild : igChildren) {
                final NodeRef igChildRef = igChild.getChildRef();
                if (nodeService.hasAspect(igChildRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
                    interestGroup.setLibraryId(igChildRef.getId());
                } else if (nodeService.hasAspect(igChildRef, CircabcModel.ASPECT_NEWSGROUP_ROOT)) {
                    interestGroup.setNewsgroupId(igChildRef.getId());
                } else if (nodeService.hasAspect(igChildRef, CircabcModel.ASPECT_INFORMATION_ROOT)) {
                    interestGroup.setInformationId(igChildRef.getId());
                } else if (nodeService.hasAspect(igChildRef, CircabcModel.ASPECT_EVENT_ROOT)) {
                    interestGroup.setEventId(igChildRef.getId());
                }
            }
        }

        return interestGroup;
    }

    private boolean allowApply(NodeRef nodeRef) {
        Boolean result =
                (Boolean) nodeService.getProperty(nodeRef, CircabcModel.PROP_CAN_REGISTERED_APPLY);
        if (result == null) {
            result = true;
        }

        String userName = authenticationService.getCurrentUserName();
        if (!(userName.equals("admin") || userName.equals(GUEST))) {
            for (UserProfile up : groupsIdMembersGet(nodeRef.getId(), null, null, null)) {
                if (up.getUser().getUserId().equals(userName)) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    private boolean isAuthorityVisible(Set<AccessPermission> allSetPermissions, String authority) {
        for (AccessPermission accessPermission : allSetPermissions) {
            if (accessPermission.getAuthority().equals(authority)
                    && accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED)) {
                if (accessPermission.getPermission().equals(NO_VISIBILITY)) {
                    return false;
                } else if (accessPermission.getPermission().equals(VISIBILITY)) {
                    return true;
                }
            }
        }
        return false;
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

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * @param actionService the actionService to set
     */
    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * @param policyBehaviourFilter the policyBehaviourFilter to set
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    @Override
    public InterestGroup getInterestGroup(String id) {
        return getInterestGroup(id, false);
    }

    @Override
    public InterestGroup getInterestGroup(String interestGroupNodeId, boolean lightMode) {
        InterestGroup interestGroup = null;

        NodeRef groupNodeRef = Converter.createNodeRefFromId(interestGroupNodeId);

        if (isInterestGroup(groupNodeRef) && !lightMode) {
            interestGroup = getInterestGroupDetails(groupNodeRef);
        } else if (isInterestGroup(groupNodeRef) && lightMode) {
            interestGroup = getInterestGroupDetails(groupNodeRef, true);
        }

        return interestGroup;
    }

    private boolean isInterestGroup(NodeRef igNodeRef) {
        return nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT);
    }

    @Override
    public GroupDashboard getGroupDashboard(String id) {
        GroupDashboard groupDashboard = new GroupDashboard();

        InterestGroup interestGroup;

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
        if (isInterestGroup(groupNodeRef)) {
            interestGroup = getInterestGroupDetails(groupNodeRef);
        } else {
            return null;
        }

        groupDashboard.setGroup(interestGroup);

        NodeRef libraryRef = Converter.createNodeRefFromId(interestGroup.getLibraryId());

        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(buildWhatNewQuery(groupNodeRef, 30));
        sp.addStore(Repository.getStoreRef());
        sp.addSort("@cm:modified", false);

        ResultSet rs = searchService.query(sp);
        List<NodeRef> nodeRefs = rs.getNodeRefs();

        GroupDashboardEntry groupDashboardEntry = new GroupDashboardEntry();
        groupDashboardEntry.setDate(new DateTime());
        groupDashboardEntry.setNews(new ArrayList<EntryEvent>());

        for (NodeRef node : nodeRefs) {
            QName nodeType = nodeService.getType(node);
            if (filterType(nodeType)) {
                continue;
            }
            EntryEvent entryEvent = new EntryEvent();
            entryEvent.setNode(nodesApi.getNode(node));
            entryEvent.setDate(new DateTime());

            Date created = (Date) nodeService.getProperty(node, ContentModel.PROP_CREATED);
            Date modified = (Date) nodeService.getProperty(node, ContentModel.PROP_MODIFIED);
            if (created.equals(modified)) {
                entryEvent.setType("create");
            } else {
                entryEvent.setType("update");
            }

            if (nodeType.equals(CircabcModel.TYPE_INFORMATION_NEWS)) {
                if (!nodeService.getChildAssocs(node).isEmpty()) {
                    ChildAssociationRef child = nodeService.getChildAssocs(node).get(0);
                    entryEvent.getNode().getProperties().put("newsDocId", child.getChildRef().getId());
                    entryEvent
                            .getNode()
                            .getProperties()
                            .put(
                                    "newsDocName",
                                    nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString());
                }
            }

            groupDashboardEntry.getNews().add(entryEvent);
        }

        List<GroupDashboardEntry> groupDashboardEntries = new ArrayList<>();
        groupDashboardEntries.add(groupDashboardEntry);
        groupDashboard.setEntries(groupDashboardEntries);

        return groupDashboard;
    }

    private boolean filterType(QName nodeType) {
        return !(nodeType.equals(ContentModel.TYPE_CONTENT)
                || nodeType.equals(ContentModel.TYPE_FOLDER)
                || nodeType.equals(ContentModel.TYPE_LINK)
                || nodeType.equals(ForumModel.TYPE_FORUMS)
                || nodeType.equals(ForumModel.TYPE_FORUM)
                || nodeType.equals(ForumModel.TYPE_POST)
                || nodeType.equals(ForumModel.TYPE_TOPIC)
                || nodeType.equals(EventModel.TYPE_EVENT)
                || nodeType.equals(CircabcModel.TYPE_INFORMATION_NEWS));
    }

    /**
     * Helper that build the query to perform the what's ne search
     */
    private String buildWhatNewQuery(final NodeRef searchNodeRef, final int interval) {
        final StringBuilder query = new StringBuilder();

        // set the creation and modification date
        final GregorianCalendar calendarNow = new GregorianCalendar();
        calendarNow.set(Calendar.HOUR, 0);
        calendarNow.set(Calendar.MINUTE, 0);
        calendarNow.set(Calendar.SECOND, 1);

        // From: subtract the interval
        calendarNow.add(Calendar.DATE, -interval);

        final String strDateFrom =
                escape(CachingDateFormat.getDateFormat().format(calendarNow.getTime()));

        // To: add the interval plus one day to be compatible with Lucene & SOLR
        calendarNow.add(Calendar.DATE, interval);
        calendarNow.add(Calendar.DAY_OF_MONTH, 1);

        final String strDateTo =
                escape(CachingDateFormat.getDateFormat().format(calendarNow.getTime()));

        query
                .append(OPEN_QUERY)
                .append(PATH)
                .append(ESCAPE_QUOTES)
                .append(apiToolBox.getPathFromSpaceRef(searchNodeRef, true))
                .append(ESCAPE_QUOTES)
                .append(CLOSE_QUERY)
                .append(AND)
                .append(OPEN_QUERY)
                .append(START_PROP_SEARCH)
                .append(PROP_MODIFIED_ESCAPED)
                .append(OPEN_BRACKETS)
                .append(strDateFrom)
                .append(TO)
                .append(strDateTo)
                .append(CLOSE_BRACKETS)
                .append(" OR ")
                .append(START_PROP_SEARCH)
                .append(PROP_CREATED_ESCAPED)
                .append(OPEN_BRACKETS)
                .append(strDateFrom)
                .append(TO)
                .append(strDateTo)
                .append(CLOSE_BRACKETS)
                .append(CLOSE_QUERY)
                .append(AND)
                .append(OPEN_QUERY)
                .append("TYPE:\"cm:folder\"")
                .append(" OR ")
                .append("TYPE:\"cm:content\"")
                .append(" OR ")
                .append("TYPE:\"ci:news\"")
                .append(CLOSE_QUERY);

        return query.toString();
    }

    /**
     * Return the string escaped
     */
    @SuppressWarnings("java:S5361")
    private String escape(final String str) {
        return str.replaceAll(ESCAPE1, ESCAPE2).replaceAll(ESCAPE3, ESCAPE4);
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

    @Override
    public List<InterestGroup> groupsGet(String language) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void groupsIdDelete(String id, Boolean purgeData, Boolean purgeLogs) {
        NodeRef groupNode = Converter.createNodeRefFromId(id);
        LogRecord logRecord = prepareLogDelete(groupNode);
        // delete the node
        try {
            // A rule was trying to add LibraryAspect in an archived node,
            // where a non-owner hasn't right to perform AddAspect.
            getRuleService().disableRules();
            if (purgeData) {
                this.getNodeService().addAspect(groupNode, ContentModel.ASPECT_TEMPORARY, null);
            }

            Long interestGroupID = (Long) nodeService.getProperty(groupNode, ContentModel.PROP_NODE_DBID);

            this.getNodeService().deleteNode(groupNode);

            if (purgeLogs && interestGroupID > 0) {
                logService.deleteInterestgroupLog(interestGroupID);
            }

            NodeRef archivedNode = nodeArchiveService.getArchivedNode(groupNode);
            if (archivedNode != null && nodeService.exists(archivedNode)) {
                nodeArchiveService.purgeArchivedNode(archivedNode);
            }

            getCircabcService().deleteIntestGroupByID(interestGroupID);
            logRecord.setOK(true);

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
            logRecord.setOK(false);
        } finally {
            getRuleService().enableRules();
            logService.log(logRecord);
        }
    }

    private LogRecord prepareLogDelete(NodeRef groupNode) {
        LogRecord logRecord = new LogRecord();

        logRecord.setDate(new Date());
        Long igId = (Long) nodeService.getProperty(groupNode, ContentModel.PROP_NODE_DBID);
        logRecord.setIgID(igId);
        logRecord.setDocumentID(igId);
        logRecord.setUser(authenticationService.getCurrentUserName());
        logRecord.setActivity("Delete interest group");
        logRecord.setService("Administration");
        String name = String.valueOf(nodeService.getProperty(groupNode, ContentModel.PROP_NAME));
        logRecord.setIgName(name);
        logRecord.setPath(PathUtils.getCircabcPath(nodeService.getPath(groupNode), true));
        return logRecord;
    }

    @Override
    public List<Applicant> groupsIdMembersApplicantsGet(String id) {
        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

        List<Applicant> result = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, eu.cec.digit.circabc.repo.applicant.Applicant> applicantMap =
                (Map<String, eu.cec.digit.circabc.repo.applicant.Applicant>)
                        nodeService.getProperty(groupNodeRef, CircabcModel.PROP_APPLICANTS);
        if (applicantMap != null) {
            for (Entry entry : applicantMap.entrySet()) {
                eu.cec.digit.circabc.repo.applicant.Applicant applicant =
                        (eu.cec.digit.circabc.repo.applicant.Applicant) entry.getValue();

                Applicant newApplicant = new Applicant();
                newApplicant.setUser(usersApi.usersUserIdGet(applicant.getUserName()));
                newApplicant.setJustification(applicant.getMessage());

                DateTime dt = new DateTime(applicant.getDate());
                newApplicant.setSubmitted(dt);
                result.add(newApplicant);
            }
        }
        return result;
    }

    @Override
    public List<UserProfile> groupsIdMembersGet(
            String id, List<String> profiles, String language, String searchQuery) {

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
        List<UserProfile> result = new ArrayList<>();

        String lang = "en";
        if (language != null && !language.isEmpty()) {
            lang = language;
        }

        if (searchQuery == null) {
            searchQuery = "";
        }

        String alfGroupName =
                (profiles != null && !profiles.isEmpty() ? profiles.get(0).substring(6) : "");

        List<UserWithProfile> users =
                circabcService.getFilteredUsers(
                        groupNodeRef,
                        circabcDaoService.getAllAlfrescoLocale().get(lang + "_"),
                        alfGroupName,
                        searchQuery,
                        "");
        for (UserWithProfile userWithProfile : users) {

            Profile profile = new Profile();

            UserProfile userProfile = setUserProfile(userWithProfile, profile);
            NodeRef profileRef =
                    Converter.createNodeRefFromId(userWithProfile.getNodeRef().substring(24));
            Map<String, String> title = circabcService.getProfileTitle(profileRef);
            profile.setTitle(Converter.convertMlToI18nProperty(title));

            result.add(userProfile);
        }

        return result;
    }

    /**
     * @see io.swagger.api.GroupsApi#countMembersInIg(java.lang.String)
     */
    @Override
    public int countMembersInIg(String igId) {
        return circabcService.countMembersInIg(igId);
    }

    private UserProfile setUserProfile(UserWithProfile userWithProfile, Profile profile) {
        profile.setName(userWithProfile.getProfileName());
        profile.getPermissions().put(INFORMATION, userWithProfile.getInformationPermission());
        profile.getPermissions().put(LIBRARY, userWithProfile.getLibraryPermission());
        profile.getPermissions().put(EVENTS, userWithProfile.getEventPermission());
        profile.getPermissions().put(NEWSGROUPS, userWithProfile.getNewsgroupPermission());
        profile.getPermissions().put(DIRECTORY, userWithProfile.getDirectoryPermission());
        // need to add "GROUP_" because it is saved like that in the DB
        profile.setGroupName(GROUP + userWithProfile.getAlfrescoGroup());

        User user = usersApi.usersUserIdGet(userWithProfile.getUserName());

        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);
        userProfile.setProfile(profile);
        return userProfile;
    }

    @Override
    public MembershipPostDefinition groupsIdMembersPost(
            NodeRef groupNodeRef, MembershipPostDefinition body) {

        MembershipPostDefinition membershipPostDefinition = inviteMemberInternal(groupNodeRef, body);
        if (CircabcConfig.ENT) {
            List<String> ecordaThemeIds = this.synchronizationService.getEcordaThemeIds(groupNodeRef);
            for (String ecordaThemeId : ecordaThemeIds) {
                List<UserProfile> memberships = body.getMemberships();
                for (UserProfile membership : memberships) {
                    this.synchronizationService.grantThemeRole(
                            membership.getUser().getUserId(), ecordaThemeId, SynchronizationService.DEFAULT_ECORDA_ROLE);
                }
            }
        }
        return membershipPostDefinition;
    }

    @Override
    public MembershipPostDefinition groupsIdMembersPut(
            NodeRef groupNodeRef, MembershipPostDefinition body) {

        return updateMemberInternal(groupNodeRef, body);
    }

    @Override
    public MembershipPostDefinition groupsIdMembersPostNoSync(
            NodeRef groupNodeRef, MembershipPostDefinition body) {

        return inviteMemberInternal(groupNodeRef, body);
    }

    /**
     * @see io.swagger.api.GroupsApi#getVisitedGroups(int)
     */
    @Override
    public List<InterestGroup> getVisitedGroups(int amount) {

        String username = authenticationService.getCurrentUserName();

        if (username == null) {
            throw new IllegalAccessError("Username could not be retrieved.");
        }

        return getVisitedGroups(username, amount);
    }

    /**
     * @see io.swagger.api.GroupsApi#getVisitedGroups(java.lang.String, int)
     */
    @Override
    public List<InterestGroup> getVisitedGroups(String username, int amount) {

        List<String> visitedIGRestLogs = logService.getVisitedIGRestLogs(username);

        List<InterestGroup> interestGroups = new ArrayList<>();

        List<String> igIds = new ArrayList<>();

        // take all elements if amount == 0
        boolean takeAll = amount == 0;

        for (String igId : visitedIGRestLogs) {
            if (amount == 0 && !takeAll) {
                break;
            }
            // filter duplicates
            if (igIds.contains(igId)) {
                continue;
            }
            NodeRef igNodeRef = Converter.createNodeRefFromId(igId);
            // check if the created node exists
            if (!nodeService.exists(igNodeRef)) {
                continue;
            }
            // once all is clear, get the details of the IG
            InterestGroup ig = getInterestGroupDetails(igNodeRef);
            interestGroups.add(ig);
            igIds.add(igId);
            amount--;
        }

        return interestGroups;
    }

    /**
     *
     */
    private MembershipPostDefinition inviteMemberInternal(
            NodeRef groupNodeRef, MembershipPostDefinition body) {
        MembershipPostDefinition result = new MembershipPostDefinition();
        Date expirationDate = null;
        for (UserProfile userProfile : body.getMemberships()) {
            try {
                if (!personService.personExists(userProfile.getUser().getUserId())) {

                    final CircabcUserDataBean user = new CircabcUserDataBean();
                    final String userName = userProfile.getUser().getUserId();
                    user.setUserName(userName);

                    final CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataNoFilterByUid(userName);
                    user.copyLdapProperties(ldapUserDetail);
                    
                
                    user.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
                    userService.createUser(user, false);
                }

                if (!isAlreadyMember(groupNodeRef, userProfile.getUser().getUserId())) {
                    authorityService.addAuthority(
                            userProfile.getProfile().getGroupName(), userProfile.getUser().getUserId());
                    result.addMembershipsItem(userProfile);
                    historyApi.cancelWaitingCleanPermissions(userProfile.getUser().getUserId(), groupNodeRef);
                    expirationDate = body.getExpirationDate();
                    if (expirationDate != null) {
                        historyApi.addExpirationDate(
                                userProfile.getUser().getUserId(),
                                groupNodeRef.getId(),
                                userProfile.getProfile().getId(),
                                userProfile.getProfile().getGroupName(),
                                expirationDate);
                    }

                    if (body.getUserNotifications() != null && body.getUserNotifications()) {
                        notifyUser(groupNodeRef, userProfile, false, body.getNotifyText(), expirationDate);
                    }

                    if (body.getAdminNotifications() != null && body.getAdminNotifications()) {
                        notifyMembershipAdministrators(groupNodeRef, userProfile, false);
                    }
                }
            } catch (ProfileException e) {
                logger.error("Impossible to invite the following user:" + userProfile.toString(), e);
            }
        }

        return result;
    }

    /**
     *
     */
    private MembershipPostDefinition updateMemberInternal(
            NodeRef groupNodeRef, MembershipPostDefinition body) {
        MembershipPostDefinition result = new MembershipPostDefinition();
        for (UserProfile userProfile : body.getMemberships()) {
            String userId = userProfile.getUser().getUserId();
            try {
                if (personService.personExists(userId) && isAlreadyMember(groupNodeRef, userId)) {

                    List<InterestGroupProfile> memberships = usersApi.getUserMembership(userId, false);
                    for (InterestGroupProfile profile : memberships) {
                        if (profile.getInterestGroup().getId().equals(groupNodeRef.getId())) {
                            String profileName = profile.getProfile().getGroupName();
                            profileName =
                                    (profileName.contains(GROUP)
                                            ? profileName
                                            : GROUP + profile.getProfile().getGroupName());
                            authorityService.removeAuthority(profileName, userId);
                        }
                    }

                    authorityService.addAuthority(userProfile.getProfile().getGroupName(), userId);
                    result.addMembershipsItem(userProfile);

                    Date expirationDate = body.getExpirationDate();
                    if (expirationDate != null) {
                        historyApi.updateExpirationDate(groupNodeRef.getId(), userId, expirationDate);
                    }

                    if (body.getUserNotifications()) {
                        notifyUser(groupNodeRef, userProfile, true, null, expirationDate);
                    }

                    if (body.getAdminNotifications()) {
                        notifyMembershipAdministrators(groupNodeRef, userProfile, true);
                    }
                }
            } catch (ProfileException e) {
                logger.error(
                        "Impossible to change the profile of the following user:" + userProfile.toString(), e);
            }
        }

        return result;
    }

    /**
     * @param notificationText
     */
    private void notifyUser(NodeRef groupNodeRef, UserProfile userProfile, boolean membershipUpdate, String notificationText, Date expirationDate) {
        Set<NotifiableUser> users = new HashSet<>();

        NodeRef person = personService.getPerson(userProfile.getUser().getUserId());
        Map<QName, Serializable> properties = getNodeService().getProperties(person);

        Serializable langObject =
                userService.getPreference(person, UserService.PREF_INTERFACE_LANGUAGE);

        Locale locale;

        if (langObject == null) {
            locale = null;
        } else if (langObject instanceof Locale) {
            locale = (Locale) langObject;
        } else {
            locale = new Locale(langObject.toString());
        }

        users.add(new NotifiableUserImpl(person, locale, properties));

        try {

            if (!membershipUpdate) {
                notificationService.notify(groupNodeRef, users, NotificationType.NOTIFY_USER_INVITATION, notificationText, expirationDate);
            } else {
                notificationService.notify(
                        groupNodeRef, users, NotificationType.NOTIFY_USER_MEMBERSHIP_UPDATE, null, expirationDate);
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during notification of user: "
                                + userProfile.getUser().getUserId()
                                + " in the invitation.",
                        e);
            }
        }
    }

    private void notifyMembershipAdministrators(
            NodeRef groupNodeRef, UserProfile userProfile, Boolean changeProfile) {
        List<Profile> groupProfiles =
                profilesApi.groupsIdProfilesGet(groupNodeRef.getId(), null, false);
        Set<NotifiableUser> users = new HashSet<>();
        for (Profile profile : groupProfiles) {
            if (profile.getPermissions().get(MEMBERS).equals(DIRECTORY_ADMIN_PERMISSION)) {
                List<String> profileMatch = new ArrayList<>();
                profileMatch.add(profile.getGroupName());
                PagedUserProfile admins =
                        groupsIdMembersGet(groupNodeRef.getId(), profileMatch, "en", -1, -1, null, null);
                for (UserProfile admin : admins.getData()) {
                    NodeRef person = personService.getPerson(admin.getUser().getUserId());
                    Map<QName, Serializable> properties = getNodeService().getProperties(person);
                    Serializable langObject =
                            userService.getPreference(person, UserService.PREF_INTERFACE_LANGUAGE);

                    Locale locale;
                    if (langObject == null) {
                        locale = null;
                    } else if (langObject instanceof Locale) {
                        locale = (Locale) langObject;
                    } else {
                        locale = new Locale(langObject.toString());
                    }
                    Boolean globalNotif = true;
                    try {
                        properties = getNodeService().getProperties(person);
                        globalNotif = (Boolean) properties.get(UserModel.PROP_GLOBAL_NOTIFICATION);
                    } catch (Exception e) {
                        logger.error(
                                " Can not read global notification for user " + admin.getUser().getUserId(), e);
                    }

                    if (globalNotif) {
                        users.add(new NotifiableUserImpl(person, locale, properties));
                    }
                }
            }
        }

        try {

            if (changeProfile) {
                notificationService.notifyUpdateMemberships(groupNodeRef, users, userProfile);
            } else {
                notificationService.notifyNewMemberships(groupNodeRef, users, userProfile);
            }

        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during notification of user: "
                                + userProfile.getUser().getUserId()
                                + " in the invitation.",
                        e);
            }
        }
    }

    private boolean isAlreadyMember(NodeRef groupNodeRef, String userId) {
        List<InterestGroupProfile> memberships = usersApi.getUserMembership(userId);
        for (InterestGroupProfile profile : memberships) {
            if (profile.getInterestGroup().getId().equals(groupNodeRef.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Member> groupsIdMembersSearchGet(String id, String query, String language) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void groupsIdMembersUserIdDelete(String id, String userId) {
        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
        this.historyApi.logOldMembership(userId, id);
        String profileName = null;
        if (this.igRootProfileManagerService.getInvitedUsers(groupNodeRef).contains(userId)) {
            this.igRootProfileManagerService.uninvitePerson(groupNodeRef, userId, false);
            profileName = this.igRootProfileManagerService.getPersonProfile(groupNodeRef, userId);
        }
        this.historyApi.registerCleanPermissions(groupNodeRef, userId);
        this.historyApi.deleteExpirationDate(userId, id);
        if (CircabcConfig.ENT && profileName != null) {
            List<String> ecordaThemeIds = this.synchronizationService.getEcordaThemeIds(groupNodeRef);
            for (String ecordaThemeId : ecordaThemeIds) {
                this.synchronizationService.revokeThemeRole(userId, ecordaThemeId, SynchronizationService.DEFAULT_ECORDA_ROLE );
            }
        }
    }

    @Override
    public void groupsIdPut(String id, InterestGroup body) {
        NodeRef igRef = Converter.createNodeRefFromId(id);

        if (!"".equals(body.getName())) {
            secureNodeService.setProperty(igRef, ContentModel.PROP_NAME, body.getName());
        }

        if (!body.getTitle().isEmpty()) {
            secureNodeService.setProperty(
                    igRef, ContentModel.PROP_TITLE, Converter.toMLText(body.getTitle()));
        }

        if (!body.getDescription().isEmpty()) {
            secureNodeService.setProperty(
                    igRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(body.getDescription()));
        }

        if (!body.getContact().isEmpty()) {
            secureNodeService.setProperty(
                    igRef, CircabcModel.PROP_CONTACT_INFORMATION, Converter.toMLText(body.getContact()));
        }

        secureNodeService.setProperty(
                igRef, CircabcModel.PROP_CAN_REGISTERED_APPLY, body.getAllowApply());

        // if public visible, then registered must be as well visible
        if (body.getIsPublic() != null && body.getIsRegistered() != null) {
            if (body.getIsPublic() && !body.getIsRegistered()) {
                body.setIsRegistered(true);
            }
        }

        if (body.getIsPublic() != null) {
            Profile guestProfile = profilesApi.groupsIdProfilesGet(id, GUEST, false).get(0);
            if (body.getIsPublic()) {
                permissionService.deletePermission(igRef, GUEST, NO_VISIBILITY);
                permissionService.setPermission(igRef, GUEST, VISIBILITY, true);
                guestProfile.getPermissions().put(VISIBILITY_SMALL, VISIBILITY);
            } else {
                permissionService.deletePermission(igRef, GUEST, VISIBILITY);
                permissionService.setPermission(igRef, GUEST, NO_VISIBILITY, true);
                guestProfile.getPermissions().put(VISIBILITY_SMALL, NO_VISIBILITY);
                guestProfile.getPermissions().put(INFORMATION, "InfNoAccess");
                guestProfile.getPermissions().put(LIBRARY, "LibNoAccess");
                guestProfile.getPermissions().put(MEMBERS, "DirNoAccess");
                guestProfile.getPermissions().put(EVENTS, "EveNoAccess");
                guestProfile.getPermissions().put(NEWSGROUPS, "NwsNoAccess");
            }
            NodeRef profileRef = Converter.createNodeRefFromId(guestProfile.getId());
            profilesApi.profilesIdPut(profileRef, guestProfile);
        }

        if (body.getIsRegistered() != null) {
            Profile registeredProfile = profilesApi.groupsIdProfilesGet(id, EVERYONE, false).get(0);
            if (body.getIsRegistered()) {
                permissionService.deletePermission(igRef, GROUP_EVERYONE, NO_VISIBILITY);
                permissionService.setPermission(igRef, GROUP_EVERYONE, VISIBILITY, true);
                registeredProfile.getPermissions().put(VISIBILITY_SMALL, VISIBILITY);
            } else {
                permissionService.deletePermission(igRef, GROUP_EVERYONE, VISIBILITY);
                permissionService.setPermission(igRef, GROUP_EVERYONE, NO_VISIBILITY, true);
                registeredProfile.getPermissions().put(VISIBILITY_SMALL, NO_VISIBILITY);
                registeredProfile.getPermissions().put(INFORMATION, "InfNoAccess");
                registeredProfile.getPermissions().put(LIBRARY, "LibNoAccess");
                registeredProfile.getPermissions().put(MEMBERS, "DirNoAccess");
                registeredProfile.getPermissions().put(EVENTS, "EveNoAccess");
                registeredProfile.getPermissions().put(NEWSGROUPS, "NwsNoAccess");
            }
            NodeRef profileRef = Converter.createNodeRefFromId(registeredProfile.getId());
            profilesApi.profilesIdPut(profileRef, registeredProfile);
        }
    }

    @Override
    public void groupsPost(InterestGroup body) {
        // TODO Auto-generated method stub

    }

    /**
     * @return the circabcService
     */
    public CircabcService getCircabcService() {
        return circabcService;
    }

    /**
     * @param circabcService the circabcService to set
     */
    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    /**
     * @return the circabcDaoService
     */
    public CircabcDaoServiceImpl getCircabcDaoService() {
        return circabcDaoService;
    }

    /**
     * @param circabcDaoService the circabcDaoService to set
     */
    public void setCircabcDaoService(CircabcDaoServiceImpl circabcDaoService) {
        this.circabcDaoService = circabcDaoService;
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @Override
    public PagedUserProfile groupsIdMembersGet(
            String id,
            List<String> profile,
            String language,
            Integer limit,
            Integer page,
            String order,
            String searchQuery) {

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
        PagedUserProfile result = new PagedUserProfile();

        String lang = "en";
        if (language != null && !language.equals("")) {
            lang = language;
        }

        String alfGroup = "";
        if (profile != null) {
            // remove the 'GROUP_'
            if (!profile.isEmpty() && !"".equals(profile.get(0))) {
                alfGroup = profile.get(0).substring(6);
            }
        }

        if (searchQuery == null) {
            searchQuery = "";
        }

        List<UserWithProfile> users =
                circabcService.getFilteredUsers(
                        groupNodeRef,
                        circabcDaoService.getAllAlfrescoLocale().get(lang + "_"),
                        alfGroup,
                        searchQuery,
                        order);

        int startingAt = 0;
        int stopAt = users.size();

        if (limit != null && limit != -1 && page != null && page != -1) {
            startingAt = limit * page - limit;
            if (startingAt > users.size()) {
                startingAt = 0;
            }

            if (startingAt + limit <= users.size()) {
                stopAt = startingAt + limit;
            }
        }

        Map<String, Date> autoExpiredUsers = historyApi.getAutoExpiredUsers(id);

        for (int i = startingAt; i < stopAt; i++) {

            UserWithProfile userWithProfile = users.get(i);
            Profile profileTmp = new Profile();
            NodeRef profileRef = nodeService.getNodeRef(userWithProfile.getProfileId());
            profileTmp.setId(profileRef.getId());
            Map<String, String> title = circabcService.getProfileTitle(profileRef);
            profileTmp.setTitle(Converter.convertMlToI18nProperty(title));

            UserProfile userProfile = setUserProfile(userWithProfile, profileTmp);
            String userID = userProfile.getUser().getUserId();
            if (autoExpiredUsers.containsKey(userID)) {
                userProfile.setExpirationDate(autoExpiredUsers.get(userID));
            }

            result.getData().add(userProfile);
        }

        result.setTotal(users.size());

        return result;
    }

    /**
     * @return the igRootProfileManagerService
     */
    public IGRootProfileManagerService getIgRootProfileManagerService() {
        return igRootProfileManagerService;
    }

    /**
     * @param igRootProfileManagerService the igRootProfileManagerService to set
     */
    public void setIgRootProfileManagerService(
            IGRootProfileManagerService igRootProfileManagerService) {
        this.igRootProfileManagerService = igRootProfileManagerService;
    }

    /**
     * @return the personService
     */
    public PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the userService
     */
    public UserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @return the notificationService
     */
    public NotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * @param notificationService the notificationService to set
     */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * @return the ldapUserService
     */
    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }

    /**
     * @param ldapUserService the ldapUserService to set
     */
    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    public LogoPreferencesService getLogoPreferencesService() {
        return logoPreferencesService;
    }

    public void setLogoPreferencesService(LogoPreferencesService logoPreferencesService) {
        this.logoPreferencesService = logoPreferencesService;
    }

    /**
     * @return the authorityService
     */
    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    @Override
    public void groupsIdMembersApplicantsPut(String id, ApplicantAction body) {
        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

        String action = body.getAction();

        if (CLEAN.equals(action)) {
            removeFromApplicantsMap(body, groupNodeRef);
        } else if (DECLINE.equals(action)) {
            // TODO implement the message sending
            removeFromApplicantsMapInternal(body, groupNodeRef, true);
        }
    }

    /**
     *
     */
    private void removeFromApplicantsMap(ApplicantAction body, NodeRef groupNodeRef) {
        removeFromApplicantsMapInternal(body, groupNodeRef, false);
    }

    /**
     *
     */
    private void removeFromApplicantsMapInternal(
            ApplicantAction body, NodeRef groupNodeRef, Boolean sendNotification) {
        @SuppressWarnings("unchecked")
        Map<String, eu.cec.digit.circabc.repo.applicant.Applicant> applicantMap =
                (Map<String, eu.cec.digit.circabc.repo.applicant.Applicant>)
                        nodeService.getProperty(groupNodeRef, CircabcModel.PROP_APPLICANTS);
        applicantMap.remove(body.getUsername());
        nodeService.setProperty(
                groupNodeRef, CircabcModel.PROP_APPLICANTS, (Serializable) applicantMap);

        if (sendNotification && !"".equals(body.getMessage())) {

            final NodeRef personNodeRef = getPersonService().getPerson(body.getUsername());
            final String receiverEmail =
                    (String) getNodeService().getProperty(personNodeRef, ContentModel.PROP_EMAIL);

            final Map<String, Object> model =
                    getMailPreferencesService().buildDefaultModel(groupNodeRef, personNodeRef, null);

            model.put(KEY_APPLICATION_DATE, new Date());
            model.put(KEY_REASON, body.getMessage().trim());

            final NodeRef circabcRoot = getManagementService().getCircabcNodeRef();

            final MailWrapper mail =
                    getMailPreferencesService()
                            .getDefaultMailTemplate(circabcRoot, MailTemplate.REFUSE_APPLICATION);

            final Serializable langObject =
                    getUserService().getPreference(personNodeRef, UserService.PREF_INTERFACE_LANGUAGE);
            final Locale locale;
            if (langObject == null) {
                locale = null;
            } else if (langObject instanceof Locale) {
                locale = (Locale) langObject;
            } else {
                locale = new Locale(langObject.toString());
            }

            try {
                getMailService()
                        .send(
                                getMailService().getNoReplyEmailAddress(),
                                receiverEmail,
                                null,
                                mail.getSubject(model, locale),
                                mail.getBody(model, locale),
                                true,
                                false);

            } catch (Throwable t) {
                // don't stop the action but let admins know email is not getting sent
                logger.warn("Failed to send email to " + receiverEmail, t);
            }
        }
    }

    /**
     * @return the apiToolBox
     */
    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    /**
     * @param apiToolBox the apiToolBox to set
     */
    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void groupsIdMembersApplicantsPost(String id, ApplicantAction body) {
        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
        String action = body.getAction();
        if ("submitNew".equals(action) && !"".equals(body.getUsername())) {
            @SuppressWarnings("unchecked")
            Map<String, eu.cec.digit.circabc.repo.applicant.Applicant> applicantMap =
                    (Map<String, eu.cec.digit.circabc.repo.applicant.Applicant>)
                            nodeService.getProperty(groupNodeRef, CircabcModel.PROP_APPLICANTS);
            if (applicantMap == null) {
                applicantMap = new HashMap<>();
            }
            eu.cec.digit.circabc.repo.applicant.Applicant applicant =
                    new eu.cec.digit.circabc.repo.applicant.Applicant(
                            body.getUsername(), new Date(), body.getMessage());
            applicantMap.put(body.getUsername(), applicant);
            nodeService.setProperty(
                    groupNodeRef, CircabcModel.PROP_APPLICANTS, (Serializable) applicantMap);

            List<Profile> lProfiles = profilesApi.groupsIdProfilesGet(id, null, false);
            for (Profile p : lProfiles) {
                if (p.getPermissions()
                        .get(MEMBERS)
                        .equals(DirectoryPermissions.DIRMANAGEMEMBERS.toString())
                        || p.getPermissions().get(MEMBERS).equals(DirectoryPermissions.DIRADMIN.toString())) {
                    notifyMembershipApplication(groupNodeRef, p, body);
                }
            }
        }
    }

    private void notifyMembershipApplication(NodeRef groupNodeRef, Profile p, ApplicantAction body) {

        List<String> selectedProfile = new ArrayList<>();

        NodeRef applicantRef = personService.getPerson(body.getUsername());

        String applicantEmail =
                nodeService.getProperty(applicantRef, ContentModel.PROP_EMAIL).toString();

        selectedProfile.add(p.getGroupName());
        PagedUserProfile users =
                groupsIdMembersGet(groupNodeRef.getId(), selectedProfile, null, -1, -1, null, null);
        for (UserProfile u : users.getData()) {

            NodeRef adminRef = personService.getPerson(u.getUser().getUserId());

            final Map<String, Object> model =
                    mailPreferencesService.buildDefaultModel(groupNodeRef, adminRef, null);
            model.put(KEY_APPLICATION_MESSAGE, body.getMessage().trim());
            model.put(KEY_APPLICATION_DATE, new Date());

            final Serializable langObject =
                    getUserService().getPreference(adminRef, UserService.PREF_INTERFACE_LANGUAGE);

            final Locale locale;
            if (langObject == null) {
                locale = null;
            } else if (langObject instanceof Locale) {
                locale = (Locale) langObject;
            } else {
                locale = new Locale(langObject.toString());
            }

            boolean mlAware = MLPropertyInterceptor.isMLAware();

            MLPropertyInterceptor.setMLAware(false);

            try {
                final MailWrapper mail =
                        getMailPreferencesService()
                                .getDefaultMailTemplate(groupNodeRef, MailTemplate.APPLY_FOR_MEMBERSHIP);
                final String subject = mail.getSubject(model, locale);
                final String bodyTxt = mail.getBody(model, locale);
                mailService.send(
                        mailService.getNoReplyEmailAddress(),
                        u.getUser().getEmail(),
                        applicantEmail,
                        subject,
                        bodyTxt,
                        true,
                        false);
            } catch (MessagingException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Problem during sending email for application of membership", e);
                }
            } finally {
                MLPropertyInterceptor.setMLAware(mlAware);
            }
        }
    }

    /**
     * @see io.swagger.api.GroupsApi#getIGSummaryStatistics(java.lang.String, boolean, boolean)
     */
    @Override
    public List<StatData> getIGSummaryStatistics(String id, boolean calculate, boolean forExport) {

        if (calculate) {
            Runnable runnable = new IgSummaryStatisticsRunnable(id);
            asyncThreadPoolExecutor.execute(runnable);
        }

        @SuppressWarnings("unchecked")
        List<StatData> data =
                (List<StatData>)
                        readSummaryContent(id, new TypeReference<List<StatData>>() {
                        }, "statistics.json");
        if (data == null) {
            data = new ArrayList<>();
        }

        return forExport ? adaptNamesForExport(data) : data;
    }

    private Object readSummaryContent(String id, TypeReference reference, String name) {

        InputStream inputStream = null;

        try {
            NodeRef igNodeRef = Converter.createNodeRefFromId(id);
            NodeRef dataNodeRef =
                    nodeService.getChildByName(igNodeRef, ContentModel.ASSOC_CONTAINS, name);
            if (dataNodeRef != null) {
                ContentReader reader = contentService.getReader(dataNodeRef, ContentModel.PROP_CONTENT);
                inputStream = reader.getContentInputStream();
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(inputStream, reference);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read " + name + " content.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        return null;
    }

    private void writeSummaryContent(String id, Object data, String name) {

        OutputStream outputStream = null;
        NodeRef dataNodeRef = null;
        try {
            ContentWriter writer;
            NodeRef igNodeRef = Converter.createNodeRefFromId(id);
            dataNodeRef =
                    nodeService.getChildByName(igNodeRef, ContentModel.ASSOC_CONTAINS, name);
            if (dataNodeRef == null) {
                // create new
                Map<QName, Serializable> properties = new HashMap<>();
                properties.put(ContentModel.PROP_NAME, name);
                dataNodeRef =
                        nodeService
                                .createNode(
                                        igNodeRef,
                                        ContentModel.ASSOC_CONTAINS,
                                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                                        ContentModel.TYPE_CONTENT,
                                        properties)
                                .getChildRef();
            }
            writer =
                    contentService.getWriter(dataNodeRef, ContentModel.PROP_CONTENT, dataNodeRef != null);
            outputStream = writer.getContentOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, data);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not write " + name + " content.", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        // denied access to guest and registered users
        try {
            if (dataNodeRef != null) {
                this.permissionService.setPermission(dataNodeRef, CircabcConstant.GUEST_AUTHORITY,
                        PermissionService.READ,
                        false);
                this.permissionService.setPermission(dataNodeRef, CircabcConstant.REGISTERED_AUTHORITY,
                        PermissionService.READ, false);
            }
        } catch (Exception e) {
            logger.error("Error setting permissions on statistics.json content.", e);
        }
    }

    private List<StatData> adaptNamesForExport(List<StatData> statData) {

        List<StatData> newStatData = new ArrayList<>();

        for (StatData statDatum : statData) {
            newStatData.add(
                    new StatData(
                            statisticsIdentifierMap.get(statDatum.getDataName()), statDatum.getDataValue()));
        }

        return newStatData;
    }

    private List<StatData> getIGSummaryStatistics(String id) {

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

        statisticsService.buildStatsData(groupNodeRef);

        List<StatData> properties = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        properties.add(
                new StatData(
                        "summary.statistics.created.date",
                        dateFormat.format(statisticsService.getIGCreationDate(groupNodeRef))));
        properties.add(new StatData("summary.statistics.number.of.users", countMembersInIg(id)));
        properties.add(
                new StatData(
                        "summary.statistics.library.folder.count",
                        String.valueOf(statisticsService.getNumberOfLibrarySpaces())));
        properties.add(
                new StatData(
                        "summary.statistics.library.document.count",
                        String.valueOf(statisticsService.getNumberOfLibraryDocuments())));
        Long libSize = statisticsService.getContentSizeOfLibrary();
        properties.add(
                new StatData(
                        "summary.statistics.library.size",
                        CircabcUploadedFile.humanReadableByteCount(libSize, false)));
        properties.add(
                new StatData(
                        "summary.statistics.information.folder.count",
                        String.valueOf(statisticsService.getNumberOfInformationSpaces())));
        properties.add(
                new StatData(
                        "summary.statistics.information.document.count",
                        String.valueOf(statisticsService.getNumberOfInformationDocuments())));
        Long infSize = statisticsService.getContentSizeOfInformation();
        properties.add(
                new StatData(
                        "summary.statistics.information.size",
                        CircabcUploadedFile.humanReadableByteCount(infSize, false)));
        properties.add(
                new StatData(
                        "summary.statistics.version.count",
                        String.valueOf(
                                statisticsService.getNumberOfVersions()
                                        + statisticsService.getNumberOfCustomizationAndHiddenContent())));
        Long verSize = statisticsService.getSizeOfVersions();
        Long custSize = statisticsService.getSizeOfCustomizationAndHiddenContent();
        properties.add(
                new StatData(
                        "summary.statistics.version.size",
                        CircabcUploadedFile.humanReadableByteCount(verSize, false)));
        properties.add(
                new StatData(
                        "summary.statistics.total.size",
                        CircabcUploadedFile.humanReadableByteCount((libSize + infSize + custSize), false)));
        properties.add(
                new StatData(
                        "summary.statistics.event.count",
                        String.valueOf(statisticsService.getNumbetOfEvents())));
        properties.add(
                new StatData(
                        "summary.statistics.meeting.count",
                        String.valueOf(statisticsService.getNumbetOfMeetings())));
        properties.add(
                new StatData(
                        "summary.statistics.forum.count",
                        String.valueOf(statisticsService.getNumberOfForums())));
        properties.add(
                new StatData(
                        "summary.statistics.topic.count",
                        String.valueOf(statisticsService.getNumberOfTopics())));
        properties.add(
                new StatData(
                        "summary.statistics.post.count", String.valueOf(statisticsService.getNumberOfPosts())));

        return properties;
    }

    /**
     * @see io.swagger.api.GroupsApi#getIGSummaryTimeline(java.lang.String)
     */
    @Override
    public List<ActivityCountDAO> getIGSummaryTimeline(String id) {

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

        return this.statisticsService.getListOfActivityCount(groupNodeRef);
    }

    /**
     * @see io.swagger.api.GroupsApi#getIGSummaryStructure(java.lang.String)
     */
    @Override
    public String getIGSummaryStructure(String id) {

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

        ServiceTreeRepresentation informationRepresent =
                statisticsService.getInformationStructure(groupNodeRef);
        ServiceTreeRepresentation libraryRepresent =
                statisticsService.getLibraryStructure(groupNodeRef);
        ServiceTreeRepresentation newsgroupsRepresent =
                statisticsService.getNewsgroupsStructure(groupNodeRef);

        List<Child> children = new ArrayList<>();

        children.add(informationRepresent.getChild());
        children.add(libraryRepresent.getChild());
        children.add(newsgroupsRepresent.getChild());

        Child rootChild = new Child();
        rootChild.setChildren(children);

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = null;

        try {
            jsonString = objectMapper.writeValueAsString(rootChild);
        } catch (Exception e) {
            logger.error("Error while writing JSON IG structure as a string.", e);
        }

        return jsonString;
    }

    /**
     * @see io.swagger.api.GroupsApi#exportSummary(java.lang.String, java.lang.String,
     * java.lang.String, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void exportSummary(String id, String format, String type, WebScriptResponse response) {

        if (type == null || format == null) {
            return;
        }

        OutputStream outStream = null;

        try {
            outStream = response.getOutputStream();

            if ("statistics".equalsIgnoreCase(type)) {

                switch (format.toLowerCase()) {
                    case "csv":
                        response.setHeader(CONTENT_DISPOSITION, "attachment;filename=Statistics.csv");
                        response.setContentType("text/csv;charset=UTF-8");
                        writeCSVData(getIGSummaryStatistics(id, false, true), outStream);
                        break;
                    case "xml":
                        response.setHeader(CONTENT_DISPOSITION, "attachment;filename=Statistics.xml");
                        response.setContentType("text/xml;charset=UTF-8");
                        writeXMLData(getIGSummaryStatistics(id, false, true), outStream);
                        break;
                    case "xls":
                        response.setHeader(CONTENT_DISPOSITION, "attachment;filename=Statistics.xls");
                        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                        writeXLS(getIGSummaryStatistics(id, false, true), outStream);
                        break;
                    default:
                        break;
                }
            } else if ("timeline".equalsIgnoreCase(type)) {

                switch (format.toLowerCase()) {
                    case "csv":
                        writeCSVTimeline(getIGSummaryTimeline(id), outStream);
                        break;
                    case "xml":
                        response.setHeader(CONTENT_DISPOSITION, "attachment;filename=TimelineActivity.xml");
                        response.setContentType("text/xml;charset=UTF-8");
                        writeXMLTimeline(getIGSummaryTimeline(id), outStream);
                        break;
                    case "xls":
                        response.setHeader(CONTENT_DISPOSITION, "attachment;filename=TimelineActivity.xls");
                        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
                        writeXLSTimeline(getIGSummaryTimeline(id), outStream);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            logger.error("Error during export", ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream", ex);
                }
            }
        }
    }

    private void writeXLS(List<StatData> statData, OutputStream outStream) throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Statistics");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Dimension Name");
        titleRow.createCell(1).setCellValue("Dimension Value");

        int idx = 1;

        for (StatData dim : statData) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(dim.getDataName());
            row.createCell(1).setCellValue(dim.getDataValue().toString());
            idx++;
        }

        workbook.write(outStream);
    }

    private void writeXLSTimeline(List<ActivityCountDAO> igTimeLineActivities, OutputStream outStream)
            throws IOException {

        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet("Timeline Activity");

        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("Date");
        titleRow.createCell(1).setCellValue("Service");
        titleRow.createCell(2).setCellValue("Activity");
        titleRow.createCell(3).setCellValue("Action Number");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MM_YYYY);

        int idx = 1;

        for (ActivityCountDAO dim : igTimeLineActivities) {
            Row row = sheet.createRow(idx);
            row.createCell(0).setCellValue(simpleDateFormat.format(dim.getMonthActivity()));
            row.createCell(1).setCellValue(dim.getService());
            row.createCell(2).setCellValue(dim.getActivity());
            row.createCell(3).setCellValue(dim.getActionNumber());
            idx++;
        }

        workbook.write(outStream);
    }

    private void writeXMLTimeline(
            List<ActivityCountDAO> igTimeLineActivity2, OutputStream outStream) {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;
        try {
            xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
            xtw.writeStartDocument("utf-8", "1.0");
            xtw.writeCharacters("\n");
            xtw.writeStartElement("timeline");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MM_YYYY);

            for (ActivityCountDAO dim : igTimeLineActivity2) {
                xtw.writeCharacters("\n  ");
                xtw.writeStartElement("month");

                xtw.writeAttribute("date", simpleDateFormat.format(dim.getMonthActivity()));
                xtw.writeCharacters("\n  ");

                xtw.writeStartElement("activity");
                xtw.writeAttribute("service", dim.getService());
                xtw.writeAttribute("action", dim.getActivity());
                xtw.writeCharacters(dim.getActionNumber().toString());
                xtw.writeEndElement();

                xtw.writeCharacters("\n  ");
                xtw.writeEndElement();
            }
            xtw.writeCharacters("\n");
            xtw.writeEndElement();
            xtw.writeEndDocument();

        } catch (XMLStreamException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during xml construction of timeline", e);
            }
        }
    }

    private void writeCSVTimeline(
            List<ActivityCountDAO> igTimeLineActivity2, OutputStream outStream) {

        OutputStreamWriter outStreamWriter;

        try {
            outStreamWriter = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("Date");
            outStreamWriter.write(',');
            outStreamWriter.write("Service");
            outStreamWriter.write(',');
            outStreamWriter.write("Activity");
            outStreamWriter.write(',');
            outStreamWriter.write("Action Number");
            outStreamWriter.write('\n');

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(MM_YYYY);

            for (ActivityCountDAO dim : igTimeLineActivity2) {

                outStreamWriter.write(simpleDateFormat.format(dim.getMonthActivity()));
                outStreamWriter.write(',');
                outStreamWriter.write(dim.getService());

                outStreamWriter.write(',');
                outStreamWriter.write(dim.getActivity());

                outStreamWriter.write(',');
                outStreamWriter.write(dim.getActionNumber().toString());

                outStreamWriter.write('\n');
            }
            outStreamWriter.flush();
            outStreamWriter.close();

        } catch (IOException e) {

            if (logger.isErrorEnabled()) {
                logger.error("Error during csv construction of timeline", e);
            }
        }
    }

    private void writeXMLData(List<StatData> interestGroupData, OutputStream outStream)
            throws XMLStreamException {

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;

        xtw = xof.createXMLStreamWriter(outStream, "UTF-8");
        xtw.writeStartDocument("utf-8", "1.0");
        xtw.writeCharacters("\n");
        xtw.writeStartElement("statistics");

        for (StatData dim : interestGroupData) {
            xtw.writeCharacters("\n  ");
            xtw.writeStartElement("dimension");
            xtw.writeAttribute("name", dim.getDataName());
            xtw.writeAttribute("value", dim.getDataValue().toString());
            xtw.writeEndElement();
        }
        xtw.writeCharacters("\n");
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }

    private void writeCSVData(List<StatData> interestGroupData, OutputStream outStream) throws IOException {

        OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);

        // write byte order mark
        outStream.write(0xEF);
        outStream.write(0xBB);
        outStream.write(0xBF);

        outStreamWriter.write("Dimension name");
        outStreamWriter.write(',');
        outStreamWriter.write("Dimension value");
        outStreamWriter.write('\n');

        for (StatData dim : interestGroupData) {
            outStreamWriter.write(dim.getDataName());
            outStreamWriter.write(',');
            outStreamWriter.write(dim.getDataValue().toString());
            outStreamWriter.write('\n');
        }
        outStreamWriter.flush();
        outStreamWriter.close();
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.GroupsApi#importZipFile(java.lang.String,
     * java.io.InputStream, java.lang.String, java.lang.String, boolean, boolean,
     * boolean, java.lang.String)
     */
    @Override
    public void importZipFile(
            String folderId,
            InputStream fileInputStream,
            String fileName,
            String mimeType,
            boolean notifyUser,
            boolean deleteFile,
            boolean disableNotification,
            String encoding) {

        try {
            if (disableNotification) {
                policyBehaviourFilter.disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
            }

            NodeRef folderNodeRef = Converter.createNodeRefFromId(folderId);

            String userName = AuthenticationUtil.getFullyAuthenticatedUser();

            // create the node to import and write its content
            Map<QName, Serializable> properties = new HashMap<>();

            properties.put(ContentModel.PROP_NAME, fileName);

            NodeRef nodeRefToImport =
                    nodeService
                            .createNode(
                                    folderNodeRef,
                                    ContentModel.ASSOC_CONTAINS,
                                    QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), fileName),
                                    ContentModel.TYPE_CONTENT,
                                    properties)
                            .getChildRef();

            ContentWriter contentWriter =
                    contentService.getWriter(nodeRefToImport, ContentModel.PROP_CONTENT, true);

            // only imports ZIP files with MimetypeMap.MIMETYPE_ZIP mime type...
            if ("application/x-zip-compressed".equals(mimeType)) {
                mimeType = MimetypeMap.MIMETYPE_ZIP;
            }

            contentWriter.setMimetype(mimeType);
            contentWriter.putContent(fileInputStream);

            ContentReader contentReader =
                    contentService.getReader(nodeRefToImport, ContentModel.PROP_CONTENT);
            long fileSize = contentReader.getSize();

            final long maxSizeInBytes = this.config.getMaxSizeInMegaBytes() * 1024L * 1024L;

            if (fileSize > maxSizeInBytes) {
                throw new IllegalStateException(
                        "File is too big to be imported maximum size in bytes : "
                                + maxSizeInBytes
                                + " current size in bytes "
                                + fileSize);
            }

            // do the actual import
            Map<String, Serializable> params = new HashMap<>(2, 1.0f);
            params.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, folderNodeRef);
            params.put(ImporterActionExecuter.PARAM_ENCODING, encoding);
            params.put(CircabcImporterActionExecuter.PARAM_DELETE_FILE, deleteFile);
            params.put(
                    CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION, disableNotification);

            String notifyUserName = "";

            if (notifyUser) {
                notifyUserName = userName;
            }
            params.put(CircabcImporterActionExecuter.PARAM_NOTIFY_USER, notifyUserName);

            // build the action to execute
            Action action = actionService.createAction(CircabcImporterActionExecuter.NAME, params);
            action.setExecuteAsynchronously(true);

            // execute the action on the ACP file
            actionService.executeAction(action, nodeRefToImport);
        } catch (Exception e) {
            if (e instanceof DuplicateChildNodeNameException) {
                throw new IllegalArgumentException("A file with this name already exists", e);
            } else {
                throw e;
            }
        }
    }

    /**
     * @see io.swagger.api.GroupsApi#generateImportIndexFileTemplate(org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void generateImportIndexFileTemplate(WebScriptResponse response) throws IOException {

        OutputStream outStream = response.getOutputStream();

        try {

            response.setHeader(CONTENT_DISPOSITION, "attachment;filename=index.txt");
            response.setContentType("text/csv;charset=UTF-8");

            OutputStreamWriter outStreamWriter =
                    new OutputStreamWriter(outStream, StandardCharsets.UTF_8);

            // write byte order mark
            outStream.write(0xEF);
            outStream.write(0xBB);
            outStream.write(0xBF);

            outStreamWriter.write("NAME");
            outStreamWriter.write("\t");
            outStreamWriter.write("TITLE");
            outStreamWriter.write("\t");
            outStreamWriter.write("DESCRIPTION");
            outStreamWriter.write("\t");
            outStreamWriter.write("AUTHOR");
            outStreamWriter.write("\t");
            outStreamWriter.write("KEYWORDS");
            outStreamWriter.write("\t");
            outStreamWriter.write("STATUS");
            outStreamWriter.write("\t");
            outStreamWriter.write("ISSUE DATE");
            outStreamWriter.write("\t");
            outStreamWriter.write("REFERENCE");
            outStreamWriter.write("\t");
            outStreamWriter.write("EXPIRDATE");
            outStreamWriter.write("\t");
            outStreamWriter.write("SECRANK");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI1");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI2");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI3");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI4");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI5");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI6");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI7");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI8");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI9");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI10");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI11");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI12");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI13");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI14");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI15");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI16");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI17");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI18");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI19");
            outStreamWriter.write("\t");
            outStreamWriter.write("ATTRI20");
            outStreamWriter.write("\t");
            outStreamWriter.write("TYPE");
            outStreamWriter.write("\t");
            outStreamWriter.write("TRANSLATOR");
            outStreamWriter.write("\t");
            outStreamWriter.write("LANG");
            outStreamWriter.write("\t");
            outStreamWriter.write("NOCONTENT");
            outStreamWriter.write("\t");
            outStreamWriter.write("ORILANG");
            outStreamWriter.write("\t");
            outStreamWriter.write("RELTRANS");
            outStreamWriter.write("\t");
            outStreamWriter.write("OVERWRITE");
            outStreamWriter.write('\n');

            outStreamWriter.flush();
            outStreamWriter.close();

        } catch (Exception ex) {
            logger.error("Error when generating empty index template for the import file.", ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException ex) {
                    logger.error("Error closing stream.", ex);
                }
            }
        }
    }

    /**
     * @return the mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public ProfilesApi getProfilesApi() {
        return profilesApi;
    }

    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    public MailPreferencesService getMailPreferencesService() {
        return mailPreferencesService;
    }

    public void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    /**
     * @param statisticsService the statisticsService to set
     */
    public void setStatisticsService(IgStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public SynchronizationService getSynchronizationService() {
        return synchronizationService;
    }

    public void setSynchronizationService(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    public MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public List<RecentDiscussion> getGroupRecentDiscussions(String id) {

        List<RecentDiscussion> result = new ArrayList<>();

        NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
        if (!isInterestGroup(groupNodeRef)) {
            return result;
        }

        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(buildRecentDiscussionQuery(groupNodeRef, 30));
        sp.addStore(Repository.getStoreRef());
        sp.addSort(MODIFIED, true);

        ResultSet rs = searchService.query(sp);
        List<NodeRef> nodeRefs = rs.getNodeRefs();
        Set<NodeRef> alreadyCheckTopics = new HashSet<>();

        for (NodeRef postRef : nodeRefs) {
            NodeRef topicRef = secureNodeService.getPrimaryParent(postRef).getParentRef();
            // only add the first occurence (last post)
            if (!alreadyCheckTopics.contains(topicRef)
                    && !nodeService.hasAspect(postRef, ModerationModel.ASPECT_WAITING_APPROVAL)) {
                alreadyCheckTopics.add(topicRef);
                RecentDiscussion recentPost = new RecentDiscussion();
                recentPost.setPost(getPostLight(postRef));
                recentPost.setTopic(nodesApi.getNode(topicRef));
                result.add(recentPost);
            }
        }

        return result;
    }

    private String buildRecentDiscussionQuery(NodeRef groupNodeRef, int interval) {

        NodeRef forumRef =
                secureNodeService.getChildByName(groupNodeRef, ContentModel.ASSOC_CONTAINS, "Newsgroups");

        final StringBuilder query = new StringBuilder();

        // set the creation and modification date
        final GregorianCalendar calendarNow = new GregorianCalendar();
        calendarNow.set(Calendar.HOUR, 0);
        calendarNow.set(Calendar.MINUTE, 0);
        calendarNow.set(Calendar.SECOND, 1);

        // From: subtract the interval
        calendarNow.add(Calendar.DATE, -interval);

        final String strDateFrom =
                escape(CachingDateFormat.getDateFormat().format(calendarNow.getTime()));

        // To: add the interval plus one day to be compatible with Lucene & SOLR
        calendarNow.add(Calendar.DATE, interval);
        calendarNow.add(Calendar.DAY_OF_MONTH, 1);

        final String strDateTo =
                escape(CachingDateFormat.getDateFormat().format(calendarNow.getTime()));

        query
                .append(OPEN_QUERY)
                .append(PATH)
                .append(ESCAPE_QUOTES)
                .append(apiToolBox.getPathFromSpaceRef(forumRef, true))
                .append(ESCAPE_QUOTES)
                .append(CLOSE_QUERY)
                .append(AND)
                .append(OPEN_QUERY)
                .append(START_PROP_SEARCH)
                .append(PROP_MODIFIED_ESCAPED)
                .append(OPEN_BRACKETS)
                .append(strDateFrom)
                .append(TO)
                .append(strDateTo)
                .append(CLOSE_BRACKETS)
                .append(" OR ")
                .append(START_PROP_SEARCH)
                .append(PROP_CREATED_ESCAPED)
                .append(OPEN_BRACKETS)
                .append(strDateFrom)
                .append(TO)
                .append(strDateTo)
                .append(CLOSE_BRACKETS)
                .append(CLOSE_QUERY)
                .append(AND)
                .append(OPEN_QUERY)
                .append("TYPE:\"fm:post\"")
                .append(CLOSE_QUERY);

        return query.toString();
    }

    /**
     *
     */
    private Node getPostLight(final NodeRef childRef) {
        Node postNode = nodesApi.getNode(childRef);

        String content = "";
        if (contentService.getReader(childRef, ContentModel.PROP_CONTENT) != null) {
            content = contentService.getReader(childRef, ContentModel.PROP_CONTENT).getContentString();
        }

        String cleanFromHtml = Jsoup.parse(content).text();

        postNode.getProperties().put(MESSAGE, cleanFromHtml);

        return postNode;
    }

    @Override
    public GroupConfiguration getInterestGroupConfiguration(String groupIp) {
        NodeRef groupRef = Converter.createNodeRefFromId(groupIp);

        GroupConfiguration result = new GroupConfiguration();
        Serializable groupConf =
                nodeService.getProperty(groupRef, CircabcModel.PROP_IG_ROOT_CONFIGURATION);

        if (groupConf != null) {
            result = extractConfiguration(groupConf.toString());
        } else {
            fillDefaultGroupConfiguration(result);
        }

        return result;
    }

    /**
     *
     */
    private void fillDefaultGroupConfiguration(GroupConfiguration result) {
        GroupConfigurationNewsgroups confNews = new GroupConfigurationNewsgroups();
        confNews.setEnableFlagNewForum(false);
        confNews.setEnableFlagNewTopic(false);
        confNews.setAgeFlagNewForum(7);
        confNews.setAgeFlagNewTopic(7);
        result.setNewsgroups(confNews);
    }

    private GroupConfiguration extractConfiguration(String body) {
        JSONParser parser = new JSONParser();
        GroupConfiguration result = new GroupConfiguration();
        try {
            JSONObject json = (JSONObject) parser.parse(body);
            JSONObject newsgroups = (JSONObject) json.get(NEWSGROUPS);
            GroupConfigurationNewsgroups confNews = new GroupConfigurationNewsgroups();

            if (newsgroups != null && newsgroups.get("enableFlagNewForum") != null) {
                boolean enableFlagNewForum = (boolean) newsgroups.get("enableFlagNewForum");
                confNews.setEnableFlagNewForum(enableFlagNewForum);
            } else {
                confNews.setEnableFlagNewForum(false);
            }

            if (newsgroups != null && newsgroups.get("enableFlagNewTopic") != null) {
                boolean enableFlagNewTopic = (boolean) newsgroups.get("enableFlagNewTopic");
                confNews.setEnableFlagNewTopic(enableFlagNewTopic);
            } else {
                confNews.setEnableFlagNewTopic(false);
            }

            if (newsgroups != null && newsgroups.get("ageFlagNewTopic") != null) {
                Integer ageFlagNewTopic = Integer.parseInt(newsgroups.get("ageFlagNewTopic").toString());
                confNews.setAgeFlagNewTopic(ageFlagNewTopic);
            } else {
                confNews.setAgeFlagNewTopic(7);
            }

            if (newsgroups != null && newsgroups.get("ageFlagNewForum") != null) {
                Integer ageFlagNewForum = Integer.parseInt(newsgroups.get("ageFlagNewForum").toString());
                confNews.setAgeFlagNewForum(ageFlagNewForum);
            } else {
                confNews.setAgeFlagNewForum(7);
            }

            result.setNewsgroups(confNews);

        } catch (ParseException e) {
            fillDefaultGroupConfiguration(result);
        }
        return result;
    }

    @Override
    public GroupConfiguration putInterestGroupConfiguration(String groupIp, GroupConfiguration body) {
        String jsonBody = body.toJsonString();

        NodeRef groupRef = Converter.createNodeRefFromId(groupIp);

        nodeService.setProperty(groupRef, CircabcModel.PROP_IG_ROOT_CONFIGURATION, jsonBody);

        return body;
    }

    @Override
    public List<Node> getInterestGroupLogos(String groupId) {
        NodeRef groupRef = Converter.createNodeRefFromId(groupId);
        List<Node> result = new ArrayList<>();

        NodeRef imagesRef = getGroupLogosContainer(groupRef);

        if (imagesRef != null) {
            List<ChildAssociationRef> images = nodeService.getChildAssocs(imagesRef);
            for (ChildAssociationRef child : images) {
                result.add(nodesApi.getNode(child.getChildRef()));
            }
        }

        return result;
    }

    /**
     *
     */
    private NodeRef getGroupLogosContainer(NodeRef groupRef) {
        NodeRef customizationContainerRef = null;
        Set<QName> typesContainer = new HashSet<>();
        typesContainer.add(CircabcModel.TYPE_CUSTOMIZATION_CONTAINER);
        List<ChildAssociationRef> customContainers =
                nodeService.getChildAssocs(groupRef, typesContainer);
        if (!customContainers.isEmpty()) {
            customizationContainerRef = customContainers.get(0).getChildRef();
        }

        NodeRef igLookRef = null;
        if (customizationContainerRef != null) {
            Set<QName> typesChildrenContainer = new HashSet<>();
            typesChildrenContainer.add(CircabcModel.TYPE_CUSTOMIZATION_FOLDER);
            List<ChildAssociationRef> customFolders =
                    nodeService.getChildAssocs(customizationContainerRef, typesChildrenContainer);
            for (ChildAssociationRef customFolder : customFolders) {
                if (IG_LOOK_AND_FEEL
                        .equals(
                                nodeService
                                        .getProperty(customFolder.getChildRef(), ContentModel.PROP_NAME)
                                        .toString())) {
                    igLookRef = customFolder.getChildRef();
                    break;
                }
            }
        }

        NodeRef iconRef = null;
        if (igLookRef != null) {
            List<ChildAssociationRef> iconFolders = nodeService.getChildAssocs(igLookRef);
            for (ChildAssociationRef child : iconFolders) {
                if ("icon"
                        .equals(
                                nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString())) {
                    iconRef = child.getChildRef();
                    break;
                }
            }
        }

        NodeRef imagesRef = null;
        if (iconRef != null) {
            List<ChildAssociationRef> imageFolders = nodeService.getChildAssocs(iconRef);
            for (ChildAssociationRef child : imageFolders) {
                if (IMAGES
                        .equals(
                                nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString())) {
                    imagesRef = child.getChildRef();
                    break;
                }
            }
        }
        return imagesRef;
    }

    @Override
    public Node postGroupLogoByGroupId(String groupId, InputStream inputStream, String filename) {

        try {
            ESAPI
                    .validator()
                    .getValidFileName(
                            "submitted file",
                            filename,
                            new ArrayList<>(Arrays.asList(".jpg", ".jpeg", ".bmp", ".gif", ".png")),
                            false);
        } catch (ValidationException | IntrusionException vex) {
            throw new IllegalArgumentException("Invalid file type: " + filename);
        }

        NodeRef groupRef = Converter.createNodeRefFromId(groupId);
        NodeRef imagesRef = getGroupLogosContainer(groupRef);

        try {
            logoPreferencesService.getOrCreateConfiguraton(groupRef, true);
        } catch (CustomizationException e) {
            logger.error("problem during get or creation of default configuration");
        }

        if (imagesRef == null) {
            imagesRef = createImagesContainer(groupRef);
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
                            + filename;

            QName associationNameQName =
                    QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), newfilename);

            Map<QName, Serializable> props = new HashMap<>();
            props.put(ContentModel.PROP_NAME, newfilename);

            NodeRef nodeRef =
                    nodeService
                            .createNode(
                                    imagesRef,
                                    ContentModel.ASSOC_CHILDREN,
                                    associationNameQName,
                                    CircabcModel.TYPE_CUSTOMIZATION_CONTENT,
                                    props)
                            .getChildRef();

            final ContentWriter writer =
                    contentService.getWriter(nodeRef, CircabcModel.PROP_CONTENT, true);
            writer.setMimetype(mimetypeService.guessMimetype(filename));

            File tempFile = null;

            try {
                long attachmentTotalSize =
                        Long.parseLong(
                                CircabcConfiguration.getProperty(CircabcConfiguration.LOGO_ALLOWED_SIZE_BYTES));

                tempFile = ApiToolBox.checkAndGetImageFile(filename, inputStream, attachmentTotalSize);
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

            return nodesApi.getNode(nodeRef);
        }

        return null;
    }

    private NodeRef createImagesContainer(NodeRef groupRef) {

        NodeRef customizationContainerRef;
        Set<QName> typesContainer = new HashSet<>();
        typesContainer.add(CircabcModel.TYPE_CUSTOMIZATION_CONTAINER);
        List<ChildAssociationRef> customContainers =
                nodeService.getChildAssocs(groupRef, typesContainer);
        if (customContainers.isEmpty()) {
            customizationContainerRef =
                    nodeService
                            .createNode(
                                    groupRef,
                                    CircabcModel.ASSOC_CUSTOMIZE,
                                    QName.createQName(
                                            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "customizationContainer"),
                                    CircabcModel.TYPE_CUSTOMIZATION_CONTAINER)
                            .getChildRef();
        } else {
            customizationContainerRef = customContainers.get(0).getChildRef();
        }

        NodeRef igLookRef = null;
        if (customizationContainerRef != null) {
            Set<QName> typesChildrenContainer = new HashSet<>();
            typesChildrenContainer.add(CircabcModel.TYPE_CUSTOMIZATION_FOLDER);
            List<ChildAssociationRef> customFolders =
                    nodeService.getChildAssocs(customizationContainerRef, typesChildrenContainer);
            for (ChildAssociationRef child : customFolders) {
                if (IG_LOOK_AND_FEEL
                        .equals(
                                nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString())) {
                    igLookRef = child.getChildRef();
                    break;
                }
            }
        }

        if (igLookRef == null) {
            igLookRef =
                    nodeService
                            .createNode(
                                    customizationContainerRef,
                                    ContentModel.ASSOC_CHILDREN,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, IG_LOOK_AND_FEEL),
                                    CircabcModel.TYPE_CUSTOMIZATION_FOLDER)
                            .getChildRef();
            nodeService.setProperty(igLookRef, ContentModel.PROP_NAME, IG_LOOK_AND_FEEL);
        }

        NodeRef iconRef = null;
        if (igLookRef != null) {
            List<ChildAssociationRef> iconFolders = nodeService.getChildAssocs(igLookRef);
            for (ChildAssociationRef child : iconFolders) {
                if ("icon"
                        .equals(
                                nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString())) {
                    iconRef = child.getChildRef();
                    break;
                }
            }
        }

        if (iconRef == null) {
            iconRef =
                    nodeService
                            .createNode(
                                    igLookRef,
                                    ContentModel.ASSOC_CHILDREN,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "icon"),
                                    CircabcModel.TYPE_CUSTOMIZATION_FOLDER)
                            .getChildRef();
            nodeService.setProperty(iconRef, ContentModel.PROP_NAME, "icon");
        }

        NodeRef imagesRef = null;
        if (iconRef != null) {
            List<ChildAssociationRef> imageFolders = nodeService.getChildAssocs(iconRef);
            for (ChildAssociationRef child : imageFolders) {
                if (IMAGES
                        .equals(
                                nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME).toString())) {
                    imagesRef = child.getChildRef();
                    break;
                }
            }
        }

        if (imagesRef == null) {
            imagesRef =
                    nodeService
                            .createNode(
                                    iconRef,
                                    ContentModel.ASSOC_CHILDREN,
                                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, IMAGES),
                                    CircabcModel.TYPE_CUSTOMIZATION_FOLDER)
                            .getChildRef();
            nodeService.setProperty(imagesRef, ContentModel.PROP_NAME, IMAGES);
        }

        return imagesRef;
    }

    @Override
    public void putSelectedLogo(String groupId, String logoId) throws CustomizationException {
        NodeRef groupRef = Converter.createNodeRefFromId(groupId);
        NodeRef logoReference = Converter.createNodeRefFromId(logoId);
        logoPreferencesService.getOrCreateConfiguraton(groupRef, true);
        logoPreferencesService.setMainPageLogoConfig(groupRef, true, -1, -1, false, false);
        logoPreferencesService.forceClearCache();
        DefaultLogoConfiguration defaultLogoConfiguration = logoPreferencesService.getDefault(groupRef);
        if (defaultLogoConfiguration.getLogo() != null &&
            defaultLogoConfiguration.getLogo().getReference() != null &&
            defaultLogoConfiguration.getLogo().getReference().equals(logoReference)){
            logoPreferencesService.setDefault(groupRef, null);
        }else {
            logoPreferencesService.setDefault(groupRef, logoReference);
        }

    }

    @Override
    public void deleteLogo(String groupId, String logoId) throws CustomizationException {
        NodeRef groupRef = Converter.createNodeRefFromId(groupId);
        NodeRef logoReference = Converter.createNodeRefFromId(logoId);
        String fileName = nodeService.getProperty(logoReference, ContentModel.PROP_NAME).toString();
        logoPreferencesService.removeLogo(groupRef, fileName);
    }

    /**
     * @param config the config to set
     */
    public void setConfig(ImportConfig config) {
        this.config = config;
    }

    /**
     * @param asyncThreadPoolExecutor the asyncThreadPoolExecutor to set
     */
    public void setAsyncThreadPoolExecutor(ThreadPoolExecutor asyncThreadPoolExecutor) {
        this.asyncThreadPoolExecutor = asyncThreadPoolExecutor;
    }

    /**
     * @param transactionService the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public LockService getCircabcLockService() {
        return circabcLockService;
    }

    public void setCircabcLockService(LockService circabcLockService) {
        this.circabcLockService = circabcLockService;
    }

    public HistoryApi getHistoryApi() {
        return historyApi;
    }

    public void setHistoryApi(HistoryApi historyApi) {
        this.historyApi = historyApi;
    }

    @Override
    public void groupsIdMembersUserIdExpirationDelete(String groupId, String userId) {
        historyApi.deleteExpirationDate(userId, groupId);
    }

    @Override
    public void groupsIdMembersUserIdExpirationPut(String id, String userId, Date expirationDate) {
        historyApi.updateExpirationDate(id, userId, expirationDate);
    }

    @Override
    public void groupsIdMembersUserIdExpirationPost(
            String id, String userId, Date expirationDate, String profileId, String alfrescoGroup) {
        historyApi.addExpirationDate(userId, id, profileId, alfrescoGroup, expirationDate);
    }

    /**
     * @return the ruleService
     */
    public RuleService getRuleService() {
        return ruleService;
    }

    /**
     * @param ruleService the ruleService to set
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * @return the nodeArchiveService
     */
    public NodeArchiveService getNodeArchiveService() {
        return nodeArchiveService;
    }

    /**
     * @param nodeArchiveService the nodeArchiveService to set
     */
    public void setNodeArchiveService(NodeArchiveService nodeArchiveService) {
        this.nodeArchiveService = nodeArchiveService;
    }

    private class IgSummaryStatisticsRunnable implements Runnable {

        protected String id;

        public IgSummaryStatisticsRunnable(String id) {
            this.id = id;
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
                                                    String lockName = "IG_SS_" + id;
                                                    boolean isLocked = circabcLockService.tryLock(lockName);
                                                    if (!isLocked) {
                                                        return null;
                                                    }
                                                    try {
                                                        List<StatData> data = getIGSummaryStatistics(id);
                                                        writeSummaryContent(id, data, "statistics.json");
                                                    } catch (Exception e) {
                                                        throw new IllegalArgumentException(
                                                                "Could not calculate statistics.json content.", e);
                                                    } finally {
                                                        circabcLockService.unlock(lockName);
                                                    }
                                                    return null;
                                                }
                                            },
                                            AuthenticationUtil.getSystemUserName());

                                    return null;
                                }
                            },
                            false,
                            true);
        }
    }
}
