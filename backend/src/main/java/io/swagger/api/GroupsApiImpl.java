package io.swagger.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.digit.circabc.rest.action.CircabcImporterActionExecuter;
import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.customization.logo.DefaultLogoConfiguration;
import eu.europa.ec.digit.circabc.rest.service.customization.logo.LogoPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.iam.SynchronizationService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationType;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.Child;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsParameter;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsService;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.ServiceTreeRepresentation;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CustomizationException;
import io.swagger.exception.ProfileException;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.CircabcUploadedFile;
import io.swagger.model.alfresco.EventModel;
import io.swagger.model.alfresco.ModerationModel;
import io.swagger.model.alfresco.UserModel;
import io.swagger.model.db.ActivityCountDAO;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.db.UserWithProfile;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.PathUtils;
import io.swagger.util.RestInputSanitizer;
import jakarta.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Default implementation of {@link GroupsApi}, providing the business logic for
 * managing CIRCABC Interest Groups (IGs) and their related resources.
 *
 * <p>This class is the service layer sitting behind the CIRCABC REST webscript
 * endpoints for Interest Groups. It is wired via Spring (see the
 * {@code io.swagger.api} package) and orchestrates the underlying Alfresco
 * repository services (node, permission, person, search, content, ...) together
 * with CIRCABC-specific services (membership, notifications, mail, statistics,
 * synchronization, logo customization).
 *
 * <p>Responsibilities include:
 *
 * <ul>
 *   <li>Reading Interest Group definitions, dashboards ("what's new") and recent
 *       discussions;</li>
 *   <li>Creating, updating and deleting Interest Groups and their configuration;</li>
 *   <li>Managing members, membership application requests (applicants), profile
 *       changes and membership expiration dates;</li>
 *   <li>Computing and persisting summary statistics, activity timelines and the
 *       service structure of an IG;</li>
 *   <li>Exporting summaries as CSV/XML/XLS and importing content from ZIP
 *       archives;</li>
 *   <li>Managing the logos displayed for an Interest Group.</li>
 * </ul>
 *
 * <p>Most methods resolve their {@code String id} argument to an Alfresco
 * {@link NodeRef} through {@link Converter#createNodeRefFromId(String)} and
 * expect it to point to an Interest Group root node
 * (aspect {@code CircabcModel.ASPECT_IGROOT}).
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

  /**
   * Lucene/SOLR-escaped form of the {@code cm:modified} property QName, used to
   * build date-range search queries.
   */
  @SuppressWarnings("java:S5361")
  private static final String PROP_MODIFIED_ESCAPED =
    ContentModel.PROP_MODIFIED.toString()
      .replaceAll(":", ESCAPE4)
      .replaceAll("\\{", "\\\\{")
      .replaceAll("\\}", "\\\\}");

  /**
   * Lucene/SOLR-escaped form of the {@code cm:created} property QName, used to
   * build date-range search queries.
   */
  @SuppressWarnings("java:S5361")
  private static final String PROP_CREATED_ESCAPED =
    ContentModel.PROP_CREATED.toString()
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

  /**
   * Maps internal statistics message keys to their human-readable labels, used
   * when adapting statistics data for export. Populated once in a static
   * initializer and exposed as an unmodifiable map.
   */
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
    aMap.put(
      "summary.statistics.library.document.count",
      "Library document count"
    );
    aMap.put("summary.statistics.library.size", "Library size");
    aMap.put(
      "summary.statistics.information.folder.count",
      "Information folder count"
    );
    aMap.put(
      "summary.statistics.information.document.count",
      "Information document count"
    );
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

  /** Logger for this service implementation. */
  static final Log logger = LogFactory.getLog(GroupsApiImpl.class);

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PersonService personService;

  @Autowired
  private UserService userService;

  @Autowired
  private UsersApi usersApi;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private HistoryApi historyApi;

  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private LogService logService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private ContentService contentService;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private RuleService ruleService;

  @Autowired
  private NodeArchiveService nodeArchiveService;

  @Autowired
  private CircabcDaoServiceImpl circabcDaoService;

  @Autowired
  private MimetypeService mimetypeService;

  @Autowired
  private LogoPreferencesService logoPreferencesService;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  @Qualifier("circabcMailService")
  private MailService mailService;

  @Autowired
  private MailPreferencesService mailPreferencesService;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private ActionService actionService;

  @Autowired
  private BehaviourFilter policyBehaviourFilter;

  @Autowired
  private SynchronizationService synchronizationService;

  @Autowired
  private IgStatisticsService igStatisticsService;

  /**
   * Returns the full definition and properties of a single Interest Group.
   *
   * @param id the Alfresco node id of the Interest Group
   * @return the {@link InterestGroup}, or {@code null} if the node is not an
   *     Interest Group
   */
  @Override
  public InterestGroup getInterestGroup(String id) {
    return getInterestGroup(id, false);
  }

  /**
   * Returns the definition of a single Interest Group, optionally in a reduced
   * ("light") representation.
   *
   * @param interestGroupNodeId the Alfresco node id of the Interest Group
   * @param lightMode {@code true} to return only the essential properties
   *     (id, name, title); {@code false} to return the full details
   * @return the {@link InterestGroup}, or {@code null} if the node is not an
   *     Interest Group
   */
  @Override
  public InterestGroup getInterestGroup(
    String interestGroupNodeId,
    boolean lightMode
  ) {
    InterestGroup interestGroup = null;

    NodeRef groupNodeRef = Converter.createNodeRefFromId(interestGroupNodeId);

    if (isInterestGroup(groupNodeRef) && !lightMode) {
      interestGroup = getInterestGroupDetails(groupNodeRef);
    } else if (isInterestGroup(groupNodeRef) && lightMode) {
      interestGroup = getInterestGroupDetails(groupNodeRef, true);
    }

    return interestGroup;
  }

  /**
   * Returns the full details of the Interest Group identified by the given
   * repository node reference.
   *
   * @param igRef the {@link NodeRef} of the Interest Group node
   * @return the populated {@link InterestGroup} details
   */
  @Override
  public InterestGroup getInterestGroupDetails(final NodeRef igRef) {
    return getInterestGroupDetails(igRef, false);
  }

  /**
   * Returns the details of the Interest Group identified by the given repository
   * node reference. In light mode only the id, name and title are populated; the
   * full mode additionally resolves description, contact, logo, service
   * permissions and child service node ids.
   *
   * @param igRef the {@link NodeRef} of the Interest Group node
   * @param lightMode {@code true} for a lightweight representation,
   *     {@code false} for the full representation
   * @return the {@link InterestGroup} details
   */
  @Override
  public InterestGroup getInterestGroupDetails(
    final NodeRef igRef,
    final boolean lightMode
  ) {
    InterestGroup interestGroup = new InterestGroup();
    interestGroup.setId(igRef.getId());
    interestGroup.setName(
      (String) nodeService.getProperty(igRef, ContentModel.PROP_NAME)
    );

    interestGroup.setTitle(toI18nProperty(igRef, ContentModel.PROP_TITLE));

    if (!lightMode) {
      populateInterestGroupDetails(igRef, interestGroup);
    }

    return interestGroup;
  }

  private I18nProperty toI18nProperty(NodeRef nodeRef, QName propName) {
    final Serializable value = nodeService.getProperty(nodeRef, propName);
    if (value instanceof MLText mlText) {
      return Converter.toI18NProperty(mlText);
    } else if (value instanceof String str) {
      return Converter.toI18NProperty(str);
    }
    return new I18nProperty();
  }

  private void populateInterestGroupDetails(
    NodeRef igRef,
    InterestGroup interestGroup
  ) {
    interestGroup.setDescription(
      toI18nProperty(igRef, ContentModel.PROP_DESCRIPTION)
    );
    interestGroup.setContact(
      toI18nProperty(igRef, CircabcModel.PROP_CONTACT_INFORMATION)
    );

    InterestGroupResult igResult = circabcService.getInterestGroup(igRef);
    if (igResult != null) {
      interestGroup.setIsPublic(igResult.getIsPublic());
      interestGroup.setIsRegistered(igResult.getIsRegistered());
      interestGroup.setAllowApply(igResult.getIsApplyForMembership());
    }

    try {
      final DefaultLogoConfiguration logoPreference =
        logoPreferencesService.getOrCreateConfiguraton(igRef, false);
      if (
        logoPreference != null &&
        logoPreference.isLogoDisplayedOnMainPage() &&
        logoPreference.getLogo() != null
      ) {
        NodeRef logoRef = logoPreference.getLogo().getReference();
        String logoName = logoPreference.getLogo().getName();
        interestGroup.setLogoUrl(logoRef.getId() + "/" + logoName);
      }
    } catch (Exception e) {
      logger.error("Error during retrieving logo definition", e);
    }

    populateServicePermissions(igRef, interestGroup);

    if (
      permissionService
        .hasPermission(igRef, "IgDelete")
        .equals(AccessStatus.ALLOWED)
    ) {
      interestGroup.getPermissions().put("IgDelete", "true");
    }

    populateChildNodeIds(igRef, interestGroup);
  }

  private <E extends Enum<E>> void findServicePermission(
    NodeRef serviceRef,
    String permKey,
    E noAccessValue,
    E[] values,
    InterestGroup interestGroup
  ) {
    for (E permDef : values) {
      if (
        !permDef.equals(noAccessValue) &&
        permissionService
          .hasPermission(serviceRef, permDef.toString())
          .equals(AccessStatus.ALLOWED)
      ) {
        interestGroup.getPermissions().put(permKey, permDef.toString());
        return;
      }
    }
    interestGroup.getPermissions().put(permKey, noAccessValue.toString());
  }

  private void populateServicePermissions(
    NodeRef igRef,
    InterestGroup interestGroup
  ) {
    NodeRef libRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Library"
    );
    findServicePermission(
      libRef,
      LIBRARY,
      LibraryPermissions.LIBNOACCESS,
      LibraryPermissions.values(),
      interestGroup
    );

    NodeRef infRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Information"
    );
    findServicePermission(
      infRef,
      INFORMATION,
      InformationPermissions.INFNOACCESS,
      InformationPermissions.values(),
      interestGroup
    );

    NodeRef eventRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Events"
    );
    findServicePermission(
      eventRef,
      "event",
      EventPermissions.EVENOACCESS,
      EventPermissions.values(),
      interestGroup
    );

    NodeRef newsRef = nodeService.getChildByName(
      igRef,
      ContentModel.ASSOC_CONTAINS,
      "Newsgroups"
    );
    findServicePermission(
      newsRef,
      "newsgroup",
      NewsGroupPermissions.NWSNOACCESS,
      NewsGroupPermissions.values(),
      interestGroup
    );

    findServicePermission(
      igRef,
      DIRECTORY,
      DirectoryPermissions.DIRNOACCESS,
      DirectoryPermissions.values(),
      interestGroup
    );
  }

  private void populateChildNodeIds(
    NodeRef igRef,
    InterestGroup interestGroup
  ) {
    for (ChildAssociationRef igChild : nodeService.getChildAssocs(igRef)) {
      final NodeRef childRef = igChild.getChildRef();
      if (nodeService.hasAspect(childRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
        interestGroup.setLibraryId(childRef.getId());
      } else if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_NEWSGROUP_ROOT)
      ) {
        interestGroup.setNewsgroupId(childRef.getId());
      } else if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_INFORMATION_ROOT)
      ) {
        interestGroup.setInformationId(childRef.getId());
      } else if (
        nodeService.hasAspect(childRef, CircabcModel.ASPECT_EVENT_ROOT)
      ) {
        interestGroup.setEventId(childRef.getId());
      }
    }
  }

  /**
   * Builds the dashboard ("what's new") for an Interest Group, aggregating the
   * content items created or modified in the last 30 days (bounded by the IG
   * creation date). Each qualifying node is wrapped as an {@link EntryEvent}
   * tagged as a {@code create} or {@code update}.
   *
   * @param id the Alfresco node id of the Interest Group
   * @return the {@link GroupDashboard}, or {@code null} if the node is not an
   *     Interest Group
   */
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

    Date igCreated = (Date) nodeService.getProperty(
      groupNodeRef,
      ContentModel.PROP_CREATED
    );

    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(buildWhatNewQuery(groupNodeRef, 30, igCreated));
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    sp.addSort("@cm:modified", false);

    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodeRefs = rs.getNodeRefs();

    GroupDashboardEntry groupDashboardEntry = new GroupDashboardEntry();
    groupDashboardEntry.setDate(new DateTime());
    groupDashboardEntry.setNews(new ArrayList<>());

    for (NodeRef node : nodeRefs) {
      if (!nodeService.exists(node)) {
        continue;
      }
      QName nodeType = nodeService.getType(node);
      if (!filterType(nodeType)) {
        EntryEvent entryEvent = new EntryEvent();
        entryEvent.setNode(nodesApi.getNode(node));
        entryEvent.setDate(new DateTime());

        Date created = (Date) nodeService.getProperty(
          node,
          ContentModel.PROP_CREATED
        );
        Date modified = (Date) nodeService.getProperty(
          node,
          ContentModel.PROP_MODIFIED
        );
        if (created.equals(modified)) {
          entryEvent.setType("create");
        } else {
          entryEvent.setType("update");
        }

        if (
          nodeType.equals(CircabcModel.TYPE_INFORMATION_NEWS) &&
          (!nodeService.getChildAssocs(node).isEmpty())
        ) {
          ChildAssociationRef child = nodeService.getChildAssocs(node).get(0);
          entryEvent
            .getNode()
            .getProperties()
            .put("newsDocId", child.getChildRef().getId());
          entryEvent
            .getNode()
            .getProperties()
            .put(
              "newsDocName",
              nodeService
                .getProperty(child.getChildRef(), ContentModel.PROP_NAME)
                .toString()
            );
        }

        groupDashboardEntry.getNews().add(entryEvent);
      }
    }

    List<GroupDashboardEntry> groupDashboardEntries = new ArrayList<>();
    groupDashboardEntries.add(groupDashboardEntry);
    groupDashboard.setEntries(groupDashboardEntries);

    return groupDashboard;
  }

  private boolean filterType(QName nodeType) {
    return !(
      nodeType.equals(ContentModel.TYPE_CONTENT) ||
      nodeType.equals(ContentModel.TYPE_FOLDER) ||
      nodeType.equals(ContentModel.TYPE_LINK) ||
      nodeType.equals(ForumModel.TYPE_FORUMS) ||
      nodeType.equals(ForumModel.TYPE_FORUM) ||
      nodeType.equals(ForumModel.TYPE_POST) ||
      nodeType.equals(ForumModel.TYPE_TOPIC) ||
      nodeType.equals(EventModel.TYPE_EVENT) ||
      nodeType.equals(CircabcModel.TYPE_INFORMATION_NEWS)
    );
  }

  private String buildWhatNewQuery(
    final NodeRef searchNodeRef,
    final int interval,
    final Date igCreatedDate
  ) {
    final StringBuilder query = new StringBuilder();

    // set the creation and modification date
    final GregorianCalendar calendarNow = new GregorianCalendar();
    calendarNow.set(Calendar.HOUR, 0);
    calendarNow.set(Calendar.MINUTE, 0);
    calendarNow.set(Calendar.SECOND, 1);

    // From: subtract the interval
    calendarNow.add(Calendar.DATE, -interval);

    // Use the later of (now - interval) or IG creation date as the lower bound
    // This prevents returning orphan nodes from previously deleted IGs
    Date earliestDate = calendarNow.getTime();
    if (igCreatedDate != null && igCreatedDate.after(earliestDate)) {
      earliestDate = igCreatedDate;
    }

    final String strDateFrom = escape(
      CachingDateFormat.getDateFormat().format(earliestDate)
    );

    // To: add the interval plus one day to be compatible with Lucene & SOLR
    calendarNow.add(Calendar.DATE, interval);
    calendarNow.add(Calendar.DAY_OF_MONTH, 1);

    final String strDateTo = escape(
      CachingDateFormat.getDateFormat().format(calendarNow.getTime())
    );

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
   * Returns the most recent forum discussions of an Interest Group, keeping only
   * the last post per topic and ignoring posts still awaiting moderation
   * approval. Searches posts modified within the last 30 days.
   *
   * @param id the Alfresco node id of the Interest Group
   * @return the list of {@link RecentDiscussion} items (empty if the node is not
   *     an Interest Group)
   */
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
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    sp.addSort(MODIFIED, true);

    ResultSet rs = searchService.query(sp);
    List<NodeRef> nodeRefs = rs.getNodeRefs();
    Set<NodeRef> alreadyCheckTopics = new HashSet<>();

    for (NodeRef postRef : nodeRefs) {
      NodeRef topicRef = nodeService.getPrimaryParent(postRef).getParentRef();
      // only add the first occurence (last post)
      if (
        !alreadyCheckTopics.contains(topicRef) &&
        !nodeService.hasAspect(postRef, ModerationModel.ASPECT_WAITING_APPROVAL)
      ) {
        alreadyCheckTopics.add(topicRef);
        RecentDiscussion recentPost = new RecentDiscussion();
        recentPost.setPost(getPostLight(postRef));
        recentPost.setTopic(nodesApi.getNode(topicRef));
        result.add(recentPost);
      }
    }

    return result;
  }

  private Node getPostLight(final NodeRef childRef) {
    Node postNode = nodesApi.getNode(childRef);

    String content = "";
    if (contentService.getReader(childRef, ContentModel.PROP_CONTENT) != null) {
      content = contentService
        .getReader(childRef, ContentModel.PROP_CONTENT)
        .getContentString();
    }

    String cleanFromHtml = Jsoup.parse(content).text();

    postNode.getProperties().put(MESSAGE, cleanFromHtml);

    return postNode;
  }

  private String buildRecentDiscussionQuery(
    NodeRef groupNodeRef,
    int interval
  ) {
    NodeRef forumRef = nodeService.getChildByName(
      groupNodeRef,
      ContentModel.ASSOC_CONTAINS,
      "Newsgroups"
    );

    final StringBuilder query = new StringBuilder();

    // set the creation and modification date
    final GregorianCalendar calendarNow = new GregorianCalendar();
    calendarNow.set(Calendar.HOUR, 0);
    calendarNow.set(Calendar.MINUTE, 0);
    calendarNow.set(Calendar.SECOND, 1);

    // From: subtract the interval
    calendarNow.add(Calendar.DATE, -interval);

    final String strDateFrom = escape(
      CachingDateFormat.getDateFormat().format(calendarNow.getTime())
    );

    // To: add the interval plus one day to be compatible with Lucene & SOLR
    calendarNow.add(Calendar.DATE, interval);
    calendarNow.add(Calendar.DAY_OF_MONTH, 1);

    final String strDateTo = escape(
      CachingDateFormat.getDateFormat().format(calendarNow.getTime())
    );

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

  private String escape(final String str) {
    return str.replace(ESCAPE1, ESCAPE2).replace(ESCAPE3, ESCAPE4);
  }

  /**
   * Deletes an Interest Group node and, optionally, its associated data and logs.
   * Rules are temporarily disabled during the deletion; the archived node is
   * purged and the corresponding CIRCABC database record is removed. The
   * operation is always logged.
   *
   * @param id the Alfresco node id of the Interest Group to delete
   * @param purgeData {@code true} to mark the node as temporary so that its
   *     content is physically purged rather than archived
   * @param purgeLogs {@code true} to also delete the Interest Group's log records
   */
  @Override
  public void groupsIdDelete(String id, Boolean purgeData, Boolean purgeLogs) {
    NodeRef groupNode = Converter.createNodeRefFromId(id);
    LogRecord logRecord = prepareLogDelete(groupNode);
    // delete the node
    try {
      // A rule was trying to add LibraryAspect in an archived node,
      // where a non-owner hasn't right to perform AddAspect.
      ruleService.disableRules();
      if (Boolean.TRUE.equals(purgeData)) {
        nodeService.addAspect(groupNode, ContentModel.ASPECT_TEMPORARY, null);
      }

      Long interestGroupID = (Long) nodeService.getProperty(
        groupNode,
        ContentModel.PROP_NODE_DBID
      );

      this.nodeService.deleteNode(groupNode);

      if (Boolean.TRUE.equals(purgeLogs) && interestGroupID > 0) {
        logService.deleteInterestgroupLog(interestGroupID);
      }

      NodeRef archivedNode = nodeArchiveService.getArchivedNode(groupNode);
      if (archivedNode != null && nodeService.exists(archivedNode)) {
        nodeArchiveService.purgeArchivedNode(archivedNode);
      }

      circabcService.deleteIntestGroupByID(interestGroupID);
      logRecord.setOK(true);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
      logRecord.setOK(false);
    } finally {
      ruleService.enableRules();
      logService.log(logRecord);
    }
  }

  private LogRecord prepareLogDelete(NodeRef groupNode) {
    LogRecord logRecord = new LogRecord();

    logRecord.setDate(new Date());
    Long igId = (Long) nodeService.getProperty(
      groupNode,
      ContentModel.PROP_NODE_DBID
    );
    logRecord.setIgID(igId);
    logRecord.setDocumentID(igId);
    logRecord.setUser(authenticationService.getCurrentUserName());
    logRecord.setActivity("Delete interest group");
    logRecord.setService("Administration");
    String name = String.valueOf(
      nodeService.getProperty(groupNode, ContentModel.PROP_NAME)
    );
    logRecord.setIgName(name);
    logRecord.setPath(
      PathUtils.getCircabcPath(nodeService.getPath(groupNode), true)
    );
    return logRecord;
  }

  /**
   * Returns the pending membership applications (applicants) of an Interest
   * Group, sorted by submission date in descending order.
   *
   * @param id the Alfresco node id of the Interest Group
   * @return the list of {@link Applicant} entries (empty if there are none)
   */
  @Override
  public List<Applicant> groupsIdMembersApplicantsGet(String id) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

    List<Applicant> result = new ArrayList<>();
    @SuppressWarnings("unchecked")
    Map<String, io.swagger.model.alfresco.Applicant> applicantMap = (Map<
      String,
      io.swagger.model.alfresco.Applicant
    >) nodeService.getProperty(groupNodeRef, CircabcModel.PROP_APPLICANTS);
    if (applicantMap != null) {
      for (Entry<
        String,
        io.swagger.model.alfresco.Applicant
      > entry : applicantMap.entrySet()) {
        io.swagger.model.alfresco.Applicant applicant = entry.getValue();

        Applicant newApplicant = new Applicant();
        newApplicant.setUser(usersApi.usersUserIdGet(applicant.getUserName()));
        newApplicant.setJustification(applicant.getMessage());

        DateTime dt = new DateTime(applicant.getDate());
        newApplicant.setSubmitted(dt);
        result.add(newApplicant);
      }
    }
    Collections.sort(
      result,
      Comparator.comparing(Applicant::getSubmitted, Comparator.reverseOrder())
    );
    return result;
  }

  /**
   * Returns the members of an Interest Group with their profile, filtered by a
   * free-text search query. Only the first profile in {@code profiles} is used
   * as an Alfresco group filter.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param profiles optional profile filter; when non-empty, the first entry
   *     (with its {@code GROUP_} prefix stripped) restricts results to that
   *     Alfresco group
   * @param language the interface language code used to resolve profile titles
   *     (defaults to {@code en})
   * @param searchQuery a free-text filter applied to users (may be {@code null})
   * @return the list of matching {@link UserProfile} entries
   */
  @Override
  public List<UserProfile> groupsIdMembersGet(
    String id,
    List<String> profiles,
    String language,
    String searchQuery
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
    List<UserProfile> result = new ArrayList<>();

    String lang = "en";
    if (language != null && !language.isEmpty()) {
      lang = language;
    }

    if (searchQuery == null) {
      searchQuery = "";
    }

    String alfGroupName = (profiles != null && !profiles.isEmpty()
      ? profiles.get(0).substring(6)
      : "");

    List<UserWithProfile> users = circabcService.getFilteredUsers(
      groupNodeRef,
      circabcDaoService.getAllAlfrescoLocale().get(lang + "_"),
      alfGroupName,
      searchQuery,
      ""
    );
    for (UserWithProfile userWithProfile : users) {
      Profile profile = new Profile();

      UserProfile userProfile = setUserProfile(userWithProfile, profile);
      NodeRef profileRef = Converter.createNodeRefFromId(
        userWithProfile.getNodeRef().substring(24)
      );
      Map<String, String> title = circabcService.getProfileTitle(profileRef);
      profile.setTitle(Converter.convertMlToI18nProperty(title));

      result.add(userProfile);
    }

    return result;
  }

  /**
   * Returns the members of an Interest Group with their profile, filtered by
   * individual first name, last name and email criteria. Only the first profile
   * in {@code profiles} is used as an Alfresco group filter.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param profiles optional profile filter; when non-empty, the first entry
   *     (with its {@code GROUP_} prefix stripped) restricts results to that
   *     Alfresco group
   * @param language the interface language code used to resolve profile titles
   *     (defaults to {@code en})
   * @param firstName first-name filter (may be {@code null})
   * @param lastName last-name filter (may be {@code null})
   * @param email email filter (may be {@code null})
   * @return the list of matching {@link UserProfile} entries
   */
  @Override
  public List<UserProfile> groupsIdMembersGet(
    String id,
    List<String> profiles,
    String language,
    String firstName,
    String lastName,
    String email
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
    List<UserProfile> result = new ArrayList<>();

    String lang = "en";
    if (language != null && !language.isEmpty()) {
      lang = language;
    }

    if (firstName == null) {
      firstName = "";
    }

    if (lastName == null) {
      lastName = "";
    }

    if (email == null) {
      email = "";
    }

    String alfGroupName = (profiles != null && !profiles.isEmpty()
      ? profiles.get(0).substring(6)
      : "");

    List<UserWithProfile> users = circabcService.getFilteredUsers(
      groupNodeRef,
      circabcDaoService.getAllAlfrescoLocale().get(lang + "_"),
      alfGroupName,
      firstName,
      lastName,
      email,
      ""
    );
    for (UserWithProfile userWithProfile : users) {
      Profile profile = new Profile();

      UserProfile userProfile = setUserProfile(userWithProfile, profile);
      NodeRef profileRef = Converter.createNodeRefFromId(
        userWithProfile.getNodeRef().substring(24)
      );
      Map<String, String> title = circabcService.getProfileTitle(profileRef);
      profile.setTitle(Converter.convertMlToI18nProperty(title));

      result.add(userProfile);
    }

    return result;
  }

  /**
   * Returns a paginated page of Interest Group members with their profile,
   * filtered by a free-text search query. Members flagged as auto-expired have
   * their expiration date populated.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param profile optional profile filter; when non-empty, the first entry
   *     (with its {@code GROUP_} prefix stripped) restricts results to that
   *     Alfresco group
   * @param language the interface language code used to resolve profile titles
   *     (defaults to {@code en})
   * @param limit the page size; {@code -1} (or {@code null}) disables paging
   * @param page the 1-based page number; {@code -1} (or {@code null}) disables
   *     paging
   * @param order the sort order applied to the filtered users
   * @param searchQuery a free-text filter applied to users (may be {@code null})
   * @return a {@link PagedUserProfile} holding the requested page and the total
   *     number of matching members
   */
  @Override
  public PagedUserProfile groupsIdMembersGet(
    String id,
    List<String> profile,
    String language,
    Integer limit,
    Integer page,
    String order,
    String searchQuery
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
    PagedUserProfile result = new PagedUserProfile();

    String lang = "en";
    if (language != null && !language.equals("")) {
      lang = language;
    }

    String alfGroup = "";
    if (profile != null && !profile.isEmpty() && !"".equals(profile.get(0))) {
      alfGroup = profile.get(0).substring(6);
    }

    if (searchQuery == null) {
      searchQuery = "";
    }

    List<UserWithProfile> users = circabcService.getFilteredUsers(
      groupNodeRef,
      circabcDaoService.getAllAlfrescoLocale().get(lang + "_"),
      alfGroup,
      searchQuery,
      order
    );

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
      NodeRef profileRef = nodeService.getNodeRef(
        userWithProfile.getProfileId()
      );
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

  private UserProfile setUserProfile(
    UserWithProfile userWithProfile,
    Profile profile
  ) {
    profile.setName(userWithProfile.getProfileName());
    profile
      .getPermissions()
      .put(INFORMATION, userWithProfile.getInformationPermission());
    profile
      .getPermissions()
      .put(LIBRARY, userWithProfile.getLibraryPermission());
    profile.getPermissions().put(EVENTS, userWithProfile.getEventPermission());
    profile
      .getPermissions()
      .put(NEWSGROUPS, userWithProfile.getNewsgroupPermission());
    profile
      .getPermissions()
      .put(DIRECTORY, userWithProfile.getDirectoryPermission());
    // need to add "GROUP_" because it is saved like that in the DB
    profile.setGroupName(GROUP + userWithProfile.getAlfrescoGroup());

    User user = usersApi.usersUserIdGet(userWithProfile.getUserName());

    UserProfile userProfile = new UserProfile();
    userProfile.setUser(user);
    userProfile.setProfile(profile);
    return userProfile;
  }

  /**
   * Returns a paginated page of Interest Group members with their profile,
   * filtered by individual first name, last name and email criteria. Members
   * flagged as auto-expired have their expiration date populated.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param profile optional profile filter; when non-empty, the first entry
   *     (with its {@code GROUP_} prefix stripped) restricts results to that
   *     Alfresco group
   * @param language the interface language code used to resolve profile titles
   *     (defaults to {@code en})
   * @param limit the page size; {@code -1} (or {@code null}) disables paging
   * @param page the 1-based page number; {@code -1} (or {@code null}) disables
   *     paging
   * @param order the sort order applied to the filtered users
   * @param firstName first-name filter (may be {@code null})
   * @param lastName last-name filter (may be {@code null})
   * @param email email filter (may be {@code null})
   * @return a {@link PagedUserProfile} holding the requested page and the total
   *     number of matching members
   */
  @Override
  public PagedUserProfile groupsIdMembersGet(
    String id,
    List<String> profile,
    String language,
    Integer limit,
    Integer page,
    String order,
    String firstName,
    String lastName,
    String email
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
    PagedUserProfile result = new PagedUserProfile();

    String lang = (language != null && !language.isEmpty()) ? language : "en";

    String alfGroup = "";
    if (profile != null && !profile.isEmpty() && !"".equals(profile.get(0))) {
      alfGroup = profile.get(0).substring(6);
    }

    String safeFirstName = firstName != null ? firstName : "";
    String safeLastName = lastName != null ? lastName : "";
    String safeEmail = email != null ? email : "";

    List<UserWithProfile> users = circabcService.getFilteredUsers(
      groupNodeRef,
      circabcDaoService.getAllAlfrescoLocale().get(lang + "_"),
      alfGroup,
      safeFirstName,
      safeLastName,
      safeEmail,
      order
    );

    int startingAt = 0;
    int stopAt = users.size();

    if (limit != null && limit != -1 && page != null && page != -1) {
      startingAt = Math.min(limit * page - limit, users.size());
      stopAt = Math.min(startingAt + limit, users.size());
    }

    Map<String, Date> autoExpiredUsers = historyApi.getAutoExpiredUsers(id);

    for (int i = startingAt; i < stopAt; i++) {
      UserWithProfile userWithProfile = users.get(i);
      Profile profileTmp = new Profile();
      NodeRef profileRef = nodeService.getNodeRef(
        userWithProfile.getProfileId()
      );
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
   * Invites one or more users into an Interest Group according to the supplied
   * membership definition, and (in ENT deployments) grants the corresponding
   * ECORDA theme roles for each linked ECORDA theme.
   *
   * @param groupNodeRef the {@link NodeRef} of the Interest Group
   * @param body the memberships to create, including notification flags and
   *     optional expiration date
   * @return a {@link MembershipPostDefinition} listing the memberships that were
   *     actually added
   */
  @Override
  public MembershipPostDefinition groupsIdMembersPost(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  ) {
    MembershipPostDefinition membershipPostDefinition = inviteMemberInternal(
      groupNodeRef,
      body
    );

    if (circabcConfig.isENT()) {
      List<String> ecordaThemeIds =
        this.synchronizationService.getEcordaThemeIds(groupNodeRef);
      for (String ecordaThemeId : ecordaThemeIds) {
        List<UserProfile> memberships = body.getMemberships();
        for (UserProfile membership : memberships) {
          this.synchronizationService.grantThemeRole(
            membership.getUser().getUserId(),
            ecordaThemeId,
            SynchronizationService.DEFAULT_ECORDA_ROLE
          );
        }
      }
    }
    return membershipPostDefinition;
  }

  /**
   * Changes the profile of existing members of an Interest Group. For each
   * membership the user is removed from their current profile group and added to
   * the requested one; optional expiration date and notifications are applied.
   * Individual failures are logged and skipped.
   *
   * @param groupNodeRef the {@link NodeRef} of the Interest Group
   * @param body the memberships whose profile should be changed, including
   *     notification flags and optional expiration date
   * @return a {@link MembershipPostDefinition} listing the memberships that were
   *     actually updated
   */
  @Override
  public MembershipPostDefinition groupsIdMembersPut(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  ) {
    MembershipPostDefinition result = new MembershipPostDefinition();
    for (UserProfile userProfile : body.getMemberships()) {
      try {
        changeMemberProfile(groupNodeRef, body, userProfile, result);
      } catch (ProfileException e) {
        logger.error(
          "Impossible to change the profile of the following user:" +
            userProfile.toString(),
          e
        );
      }
    }
    return result;
  }

  private void changeMemberProfile(
    NodeRef groupNodeRef,
    MembershipPostDefinition body,
    UserProfile userProfile,
    MembershipPostDefinition result
  ) {
    String userId = userProfile.getUser().getUserId();
    if (
      !personService.personExists(userId) ||
      !isAlreadyMember(groupNodeRef, userId)
    ) {
      return;
    }

    removeFromCurrentProfiles(groupNodeRef, userId);

    AuthenticationUtil.runAs(
      () -> {
        authorityService.addAuthority(
          userProfile.getProfile().getGroupName(),
          userId
        );
        return null;
      },
      AuthenticationUtil.getAdminUserName()
    );

    result.addMembershipsItem(userProfile);
    circabcService.changePersonProfile(
      groupNodeRef,
      userId,
      userProfile.getProfile().getName()
    );

    Date expirationDate = body.getExpirationDate();
    if (expirationDate != null) {
      historyApi.updateExpirationDate(
        groupNodeRef.getId(),
        userId,
        expirationDate
      );
    }
    if (Boolean.TRUE.equals(body.getUserNotifications())) {
      notifyUser(groupNodeRef, userProfile, true, null, expirationDate);
    }
    if (Boolean.TRUE.equals(body.getAdminNotifications())) {
      notifyMembershipAdministrators(groupNodeRef, userProfile, true);
    }
  }

  private void removeFromCurrentProfiles(NodeRef groupNodeRef, String userId) {
    List<InterestGroupProfile> memberships = usersApi.getUserMembership(
      userId,
      false
    );
    for (InterestGroupProfile profile : memberships) {
      if (!profile.getInterestGroup().getId().equals(groupNodeRef.getId())) {
        continue;
      }
      String profileGroupName = profile.getProfile().getGroupName();
      profileGroupName = profileGroupName.contains(GROUP)
        ? profileGroupName
        : GROUP + profile.getProfile().getGroupName();
      final String finalProfileGroupName = profileGroupName;
      AuthenticationUtil.runAs(
        () -> {
          authorityService.removeAuthority(finalProfileGroupName, userId);
          return null;
        },
        AuthenticationUtil.getAdminUserName()
      );
    }
  }

  /**
   * Removes a user from the members of an Interest Group. The former membership
   * is logged, the user is removed from their profile authority group, their
   * permissions are cleaned and any expiration date is deleted. In ENT
   * deployments the corresponding ECORDA theme roles are also revoked.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param userId the username of the member to remove
   */
  @Override
  public void groupsIdMembersUserIdDelete(String id, String userId) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
    this.historyApi.logOldMembership(userId, id);
    String profileName = null;
    if (this.circabcService.isUserMember(groupNodeRef, userId)) {
      io.swagger.model.db.Profile profile =
        this.circabcDaoService.selectProfileByInterestGroupNodeRefUserName(
          groupNodeRef.toString(),
          userId
        );
      if (profile != null) {
        profileName = profile.getName();
        AuthenticationUtil.runAs(
          () -> {
            authorityService.removeAuthority(
              GROUP + profile.getAlfrescoGroup(),
              userId
            );

            return null;
          },
          AuthenticationUtil.getAdminUserName()
        );
      }
      this.circabcService.deletePersonFromGroup(groupNodeRef, userId);
    }
    this.historyApi.registerCleanPermissions(groupNodeRef, userId);
    this.historyApi.deleteExpirationDate(userId, id);
    if (circabcConfig.isENT() && profileName != null) {
      List<String> ecordaThemeIds =
        this.synchronizationService.getEcordaThemeIds(groupNodeRef);
      for (String ecordaThemeId : ecordaThemeIds) {
        this.synchronizationService.revokeThemeRole(
          userId,
          ecordaThemeId,
          SynchronizationService.DEFAULT_ECORDA_ROLE
        );
      }
    }
  }

  /**
   * Updates the properties of an Interest Group (name, title, description,
   * contact, apply-for-membership flag and public/registered visibility). When
   * the group is made public it is implicitly also made visible to registered
   * users, and the associated visibility profiles/permissions are updated
   * accordingly.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param body the new Interest Group properties to apply
   */
  @Override
  public void groupsIdPut(String id, InterestGroup body) {
    NodeRef igRef = Converter.createNodeRefFromId(id);

    if (!"".equals(body.getName())) {
      nodeService.setProperty(igRef, ContentModel.PROP_NAME, body.getName());
    }

    if (!body.getTitle().isEmpty()) {
      nodeService.setProperty(
        igRef,
        ContentModel.PROP_TITLE,
        Converter.toMLText(body.getTitle())
      );
    }

    if (!body.getDescription().isEmpty()) {
      nodeService.setProperty(
        igRef,
        ContentModel.PROP_DESCRIPTION,
        Converter.toMLText(
          RestInputSanitizer.sanitizeRichText(body.getDescription())
        )
      );
    }

    if (!body.getContact().isEmpty()) {
      nodeService.setProperty(
        igRef,
        CircabcModel.PROP_CONTACT_INFORMATION,
        Converter.toMLText(
          RestInputSanitizer.sanitizeRichText(body.getContact())
        )
      );
    }

    nodeService.setProperty(
      igRef,
      CircabcModel.PROP_CAN_REGISTERED_APPLY,
      body.getAllowApply()
    );
    circabcService.updateInterestGroupApplication(igRef, body.getAllowApply());

    // if public visible, then registered must be as well visible
    if (
      body.getIsPublic() != null &&
      body.getIsRegistered() != null &&
      Boolean.TRUE.equals(body.getIsPublic()) &&
      Boolean.FALSE.equals(body.getIsRegistered())
    ) {
      body.setIsRegistered(true);
    }

    if (body.getIsPublic() != null) {
      circabcService.updateInterestGroupPublic(igRef, body.getIsPublic());
      updateVisibilityProfile(id, igRef, GUEST, GUEST, body.getIsPublic());
    }

    if (body.getIsRegistered() != null) {
      circabcService.updateInterestGroupRegistered(
        igRef,
        body.getIsRegistered()
      );
      updateVisibilityProfile(
        id,
        igRef,
        EVERYONE,
        GROUP_EVERYONE,
        body.getIsRegistered()
      );
    }
    circabcService.updateIntestGroupProperties(igRef);
  }

  private void updateVisibilityProfile(
    String id,
    NodeRef igRef,
    String profileFilter,
    String authority,
    boolean visible
  ) {
    Profile profile = profilesApi
      .groupsIdProfilesGet(id, profileFilter, false)
      .get(0);
    if (visible) {
      permissionService.deletePermission(igRef, authority, NO_VISIBILITY);
      permissionService.setPermission(igRef, authority, VISIBILITY, true);
      profile.getPermissions().put(VISIBILITY_SMALL, VISIBILITY);
    } else {
      permissionService.deletePermission(igRef, authority, VISIBILITY);
      permissionService.setPermission(igRef, authority, NO_VISIBILITY, true);
      profile.getPermissions().put(VISIBILITY_SMALL, NO_VISIBILITY);
      profile.getPermissions().put(INFORMATION, "InfNoAccess");
      profile.getPermissions().put(LIBRARY, "LibNoAccess");
      profile.getPermissions().put(MEMBERS, "DirNoAccess");
      profile.getPermissions().put(EVENTS, "EveNoAccess");
      profile.getPermissions().put(NEWSGROUPS, "NwsNoAccess");
    }
    NodeRef profileRef = Converter.createNodeRefFromId(profile.getId());
    profilesApi.profilesIdPut(profileRef, profile);
  }

  /**
   * Processes an action on a membership applicant: {@code clean} silently removes
   * the applicant, while {@code decline} removes the applicant and notifies them
   * by email. Any other action is ignored.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param body the applicant action, carrying the target username, the action
   *     name and an optional message
   */
  @Override
  public void groupsIdMembersApplicantsPut(String id, ApplicantAction body) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

    String action = body.getAction();

    if (CLEAN.equals(action)) {
      removeFromApplicantsMap(body, groupNodeRef);
    } else if (DECLINE.equals(action)) {
      removeFromApplicantsMapInternal(body, groupNodeRef, true);
    }
  }

  /**
   *
   */
  private void removeFromApplicantsMap(
    ApplicantAction body,
    NodeRef groupNodeRef
  ) {
    removeFromApplicantsMapInternal(body, groupNodeRef, false);
  }

  /**
   *
   */
  private void removeFromApplicantsMapInternal(
    ApplicantAction body,
    NodeRef groupNodeRef,
    Boolean sendNotification
  ) {
    @SuppressWarnings("unchecked")
    Map<String, io.swagger.model.alfresco.Applicant> applicantMap = (Map<
      String,
      io.swagger.model.alfresco.Applicant
    >) nodeService.getProperty(groupNodeRef, CircabcModel.PROP_APPLICANTS);
    applicantMap.remove(body.getUsername());
    nodeService.setProperty(
      groupNodeRef,
      CircabcModel.PROP_APPLICANTS,
      (Serializable) applicantMap
    );

    if (
      Boolean.TRUE.equals(sendNotification) && !"".equals(body.getMessage())
    ) {
      final NodeRef personNodeRef = personService.getPerson(body.getUsername());
      final String receiverEmail = (String) nodeService.getProperty(
        personNodeRef,
        ContentModel.PROP_EMAIL
      );

      final Map<String, Object> model =
        mailPreferencesService.buildDefaultModel(
          groupNodeRef,
          personNodeRef,
          null
        );

      model.put(KEY_APPLICATION_DATE, new Date());
      model.put(KEY_REASON, body.getMessage().trim());

      final NodeRef circabcRoot = circabcApi.getCircabcNodeRef();

      final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
        circabcRoot,
        MailTemplate.REFUSE_APPLICATION
      );

      final Serializable langObject = userService.getPreference(
        personNodeRef,
        UserService.PREF_INTERFACE_LANGUAGE
      );
      final Locale locale;
      if (langObject == null) {
        locale = null;
      } else if (langObject instanceof Locale loc) {
        locale = loc;
      } else {
        locale = Locale.of(langObject.toString());
      }

      try {
        mailService.send(
          mailService.getNoReplyEmailAddress(),
          receiverEmail,
          null,
          mail.getSubject(model, locale),
          mail.getBody(model, locale),
          true,
          false
        );
      } catch (Exception t) {
        // don't stop the action but let admins know email is not getting sent
        logger.warn("Failed to send email to " + receiverEmail, t);
      }
    }
  }

  /**
   * Registers a new membership application. When the action is {@code submitNew}
   * and a username is provided, the applicant is added to the group's applicant
   * map and the membership administrators (profiles with directory
   * manage-members or admin permission) are notified by email.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param body the applicant action, carrying the applicant username, the
   *     action name and the justification message
   */
  @Override
  public void groupsIdMembersApplicantsPost(String id, ApplicantAction body) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);
    String action = body.getAction();
    if ("submitNew".equals(action) && !"".equals(body.getUsername())) {
      @SuppressWarnings("unchecked")
      Map<String, io.swagger.model.alfresco.Applicant> applicantMap = (Map<
        String,
        io.swagger.model.alfresco.Applicant
      >) nodeService.getProperty(groupNodeRef, CircabcModel.PROP_APPLICANTS);
      if (applicantMap == null) {
        applicantMap = new HashMap<>();
      }
      io.swagger.model.alfresco.Applicant applicant =
        new io.swagger.model.alfresco.Applicant(
          body.getUsername(),
          new Date(),
          body.getMessage()
        );
      applicantMap.put(body.getUsername(), applicant);
      nodeService.setProperty(
        groupNodeRef,
        CircabcModel.PROP_APPLICANTS,
        (Serializable) applicantMap
      );

      List<Profile> lProfiles = profilesApi.groupsIdProfilesGet(
        id,
        null,
        false
      );
      for (Profile p : lProfiles) {
        if (
          p
            .getPermissions()
            .get(MEMBERS)
            .equals(DirectoryPermissions.DIRMANAGEMEMBERS.toString()) ||
          p
            .getPermissions()
            .get(MEMBERS)
            .equals(DirectoryPermissions.DIRADMIN.toString())
        ) {
          notifyMembershipApplication(groupNodeRef, p, body);
        }
      }
    }
  }

  private void notifyMembershipApplication(
    NodeRef groupNodeRef,
    Profile p,
    ApplicantAction body
  ) {
    List<String> selectedProfile = new ArrayList<>();

    NodeRef applicantRef = personService.getPerson(body.getUsername());

    String applicantEmail = nodeService
      .getProperty(applicantRef, ContentModel.PROP_EMAIL)
      .toString();

    selectedProfile.add(p.getGroupName());
    PagedUserProfile users = groupsIdMembersGet(
      groupNodeRef.getId(),
      selectedProfile,
      null,
      -1,
      -1,
      null,
      null
    );
    for (UserProfile u : users.getData()) {
      NodeRef adminRef = personService.getPerson(u.getUser().getUserId());

      final Map<String, Object> model =
        mailPreferencesService.buildDefaultModel(groupNodeRef, adminRef, null);
      model.put(KEY_APPLICATION_MESSAGE, body.getMessage().trim());
      model.put(KEY_APPLICATION_DATE, new Date());

      final Serializable langObject = userService.getPreference(
        adminRef,
        UserService.PREF_INTERFACE_LANGUAGE
      );

      final Locale locale;
      if (langObject == null) {
        locale = null;
      } else if (langObject instanceof Locale loc) {
        locale = loc;
      } else {
        locale = Locale.of(langObject.toString());
      }

      boolean mlAware = MLPropertyInterceptor.isMLAware();

      MLPropertyInterceptor.setMLAware(false);

      try {
        final MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
          groupNodeRef,
          MailTemplate.APPLY_FOR_MEMBERSHIP
        );
        final String subject = mail.getSubject(model, locale);
        final String bodyTxt = mail.getBody(model, locale);
        mailService.send(
          mailService.getNoReplyEmailAddress(),
          u.getUser().getEmail(),
          applicantEmail,
          subject,
          bodyTxt,
          true,
          false
        );
      } catch (MessagingException e) {
        if (logger.isErrorEnabled()) {
          logger.error(
            "Problem during sending email for application of membership",
            e
          );
        }
      } finally {
        MLPropertyInterceptor.setMLAware(mlAware);
      }
    }
  }

  /**
   * Returns the summary statistics of an Interest Group. When {@code calculate}
   * is {@code true} the statistics are (re)computed and persisted to the
   * {@code statistics.json} content node; otherwise the previously stored values
   * are read back.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param calculate {@code true} to recompute and persist the statistics before
   *     returning them
   * @param forExport {@code true} to translate the internal statistic keys into
   *     human-readable labels suitable for export
   * @return the list of {@link StatData} statistics (empty if none are available)
   */
  @Override
  public List<StatData> getIGSummaryStatistics(
    String id,
    boolean calculate,
    boolean forExport
  ) {
    if (calculate) {
      List<StatData> data = getIGSummaryStatistics(id);
      writeSummaryContent(id, data, "statistics.json");
    }

    @SuppressWarnings("unchecked")
    List<StatData> data = (List<StatData>) readSummaryContent(
      id,
      new TypeReference<List<StatData>>() {},
      "statistics.json"
    );
    if (data == null) {
      data = new ArrayList<>();
    }

    return forExport ? adaptNamesForExport(data) : data;
  }

  private Object readSummaryContent(
    String id,
    @SuppressWarnings("rawtypes") TypeReference reference,
    String name
  ) {
    InputStream inputStream = null;

    try {
      NodeRef igNodeRef = Converter.createNodeRefFromId(id);
      NodeRef dataNodeRef = nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        name
      );
      if (dataNodeRef != null) {
        ContentReader reader = contentService.getReader(
          dataNodeRef,
          ContentModel.PROP_CONTENT
        );
        inputStream = reader.getContentInputStream();
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Object result = mapper.readValue(inputStream, reference);
        return result;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Could not read " + name + " content.",
        e
      );
    } finally {
      IOUtils.closeQuietly(inputStream);
    }

    return null;
  }

  private void writeSummaryContent(String id, Object data, String name) {
    OutputStream outputStream = null;
    NodeRef igNodeRef = Converter.createNodeRefFromId(id);
    NodeRef dataNodeRef;

    try {
      ContentWriter writer;
      dataNodeRef = nodeService.getChildByName(
        igNodeRef,
        ContentModel.ASSOC_CONTAINS,
        name
      );
      if (dataNodeRef == null) {
        // create new
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_NAME, name);
        dataNodeRef = nodeService
          .createNode(
            igNodeRef,
            ContentModel.ASSOC_CONTAINS,
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
            ContentModel.TYPE_CONTENT,
            properties
          )
          .getChildRef();
      }
      writer = contentService.getWriter(
        dataNodeRef,
        ContentModel.PROP_CONTENT,
        dataNodeRef != null
      );
      outputStream = writer.getContentOutputStream();
      ObjectMapper mapper = new ObjectMapper();
      mapper.writeValue(outputStream, data);
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Could not write " + name + " content.",
        e
      );
    } finally {
      IOUtils.closeQuietly(outputStream);
    }
    // denied access to non admin users
    try {
      if (dataNodeRef != null) {
        long interestGroupID = (Long) nodeService.getProperty(
          igNodeRef,
          ContentModel.PROP_NODE_DBID
        );
        List<String> leaderGroups = circabcDaoService.selectLeaderAlfGroups(
          interestGroupID
        );
        this.removeNotAdminPermissions(dataNodeRef, leaderGroups);
      }
    } catch (Exception e) {
      logger.error("Error setting permissions on statistics.json content.", e);
    }
  }

  private void removeNotAdminPermissions(
    NodeRef nodeRef,
    List<String> leaderGroups
  ) {
    boolean isInherited = this.permissionService.getInheritParentPermissions(
      nodeRef
    );
    Set<AccessPermission> allSetPermissions;

    if (isInherited) {
      allSetPermissions = this.permissionService.getAllSetPermissions(nodeRef);
      this.permissionService.setInheritParentPermissions(nodeRef, false);
    } else {
      NodeRef parentNodeRef = this.nodeService.getPrimaryParent(
        nodeRef
      ).getParentRef();
      allSetPermissions = this.permissionService.getAllSetPermissions(
        parentNodeRef
      );
    }
    for (AccessPermission accessPermission : allSetPermissions) {
      if (
        accessPermission
          .getAuthority()
          .startsWith("GROUP_CircaCategoryAdmin--") ||
        leaderGroups.contains(accessPermission.getAuthority())
      ) {
        permissionService.setPermission(
          nodeRef,
          accessPermission.getAuthority(),
          accessPermission.getPermission(),
          accessPermission.getAccessStatus() == AccessStatus.ALLOWED
        );
      }
    }
  }

  private List<StatData> adaptNamesForExport(List<StatData> statData) {
    List<StatData> newStatData = new ArrayList<>();

    for (StatData statDatum : statData) {
      newStatData.add(
        new StatData(
          statisticsIdentifierMap.get(statDatum.getDataName()),
          statDatum.getDataValue()
        )
      );
    }

    return newStatData;
  }

  private List<StatData> getIGSummaryStatistics(String id) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

    IgStatisticsParameter statistics = igStatisticsService.buildStatsData(
      groupNodeRef
    );

    List<StatData> properties = new ArrayList<>();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    properties.add(
      new StatData(
        "summary.statistics.created.date",
        dateFormat.format(statistics.getCreationDate())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.number.of.users",
        String.valueOf(statistics.getNbUsers())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.library.folder.count",
        String.valueOf(statistics.getLibraryFolderCount())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.library.document.count",
        String.valueOf(statistics.getLibraryDocumentCount())
      )
    );
    Long libSize = statistics.getLibrarySize();
    properties.add(
      new StatData(
        "summary.statistics.library.size",
        CircabcUploadedFile.humanReadableByteCount(libSize, false)
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.information.folder.count",
        String.valueOf(statistics.getInformationFolderCount())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.information.document.count",
        String.valueOf(statistics.getInformationDocumentCount())
      )
    );
    Long infSize = statistics.getInformationSize();
    properties.add(
      new StatData(
        "summary.statistics.information.size",
        CircabcUploadedFile.humanReadableByteCount(infSize, false)
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.version.count",
        String.valueOf(
          statistics.getVersionCount() +
            statistics.getCustomizationAndHiddenContentCount()
        )
      )
    );
    Long verSize = statistics.getVersionSize();
    Long custSize = statistics.getCustomizationAndHiddenContentSize();
    properties.add(
      new StatData(
        "summary.statistics.version.size",
        CircabcUploadedFile.humanReadableByteCount(verSize, false)
      )
    );

    properties.add(
      new StatData(
        "summary.statistics.total.size",
        CircabcUploadedFile.humanReadableByteCount(
          (libSize + infSize + custSize),
          false
        )
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.event.count",
        String.valueOf(statistics.getEventCount())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.meeting.count",
        String.valueOf(statistics.getMeetingCount())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.forum.count",
        String.valueOf(statistics.getForumCount())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.topic.count",
        String.valueOf(statistics.getTopicCount())
      )
    );
    properties.add(
      new StatData(
        "summary.statistics.post.count",
        String.valueOf(statistics.getPostCount())
      )
    );

    return properties;
  }

  /**
   * Returns the activity timeline of an Interest Group, i.e. the per-month
   * activity counts across its services.
   *
   * @param id the Alfresco node id of the Interest Group
   * @return the list of {@link ActivityCountDAO} entries
   */
  @Override
  public List<ActivityCountDAO> getIGSummaryTimeline(String id) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

    return igStatisticsService.getListOfActivityCount(groupNodeRef);
  }

  /**
   * Returns the service structure (Information, Library and Newsgroups trees) of
   * an Interest Group serialized as a JSON string.
   *
   * @param id the Alfresco node id of the Interest Group
   * @return the JSON representation of the IG structure, or {@code null} if
   *     serialization fails
   */
  @Override
  public String getIGSummaryStructure(String id) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(id);

    ServiceTreeRepresentation informationRepresent =
      igStatisticsService.getInformationStructure(groupNodeRef);
    ServiceTreeRepresentation libraryRepresent =
      igStatisticsService.getLibraryStructure(groupNodeRef);
    ServiceTreeRepresentation newsgroupsRepresent =
      igStatisticsService.getNewsgroupsStructure(groupNodeRef);

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
   * Exports an Interest Group summary to the HTTP response in the requested
   * format. Supports {@code statistics} and {@code timeline} data types, each in
   * {@code csv}, {@code xml} or {@code xls} format, streaming the result with the
   * appropriate content type and attachment headers. Errors are logged and the
   * output stream is closed in all cases.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param format the output format ({@code csv}, {@code xml} or {@code xls})
   * @param type the data type to export ({@code statistics} or {@code timeline})
   * @param response the {@link WebScriptResponse} to which the export is written
   */
  @Override
  public void exportSummary(
    String id,
    String format,
    String type,
    WebScriptResponse response
  ) {
    if (type == null || format == null) {
      return;
    }

    OutputStream outStream = null;

    try {
      outStream = response.getOutputStream();

      if ("statistics".equalsIgnoreCase(type)) {
        switch (format.toLowerCase()) {
          case "csv":
            response.setHeader(
              CONTENT_DISPOSITION,
              "attachment;filename=Statistics.csv"
            );
            response.setContentType("text/csv;charset=UTF-8");
            writeCSVData(getIGSummaryStatistics(id, false, true), outStream);
            break;
          case "xml":
            response.setHeader(
              CONTENT_DISPOSITION,
              "attachment;filename=Statistics.xml"
            );
            response.setContentType("text/xml;charset=UTF-8");
            writeXMLData(getIGSummaryStatistics(id, false, true), outStream);
            break;
          case "xls":
            response.setHeader(
              CONTENT_DISPOSITION,
              "attachment;filename=Statistics.xls"
            );
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
            response.setHeader(
              CONTENT_DISPOSITION,
              "attachment;filename=TimelineActivity.xml"
            );
            response.setContentType("text/xml;charset=UTF-8");
            writeXMLTimeline(getIGSummaryTimeline(id), outStream);
            break;
          case "xls":
            response.setHeader(
              CONTENT_DISPOSITION,
              "attachment;filename=TimelineActivity.xls"
            );
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

  /**
   * Generates and streams a tab-separated index file template (with a UTF-8 byte
   * order mark and the full set of import column headers) used as the starting
   * point for bulk content imports.
   *
   * @param response the {@link WebScriptResponse} to which the template is
   *     written as a downloadable {@code index.txt} attachment
   * @throws IOException if writing to the response output stream fails
   */
  @Override
  public void generateImportIndexFileTemplate(WebScriptResponse response)
    throws IOException {
    try (OutputStream outStream = response.getOutputStream()) {
      response.setHeader(CONTENT_DISPOSITION, "attachment;filename=index.txt");
      response.setContentType("text/csv;charset=UTF-8");

      OutputStreamWriter outStreamWriter = new OutputStreamWriter(
        outStream,
        StandardCharsets.UTF_8
      );

      // write byte order mark
      outStream.write(0xEF);
      outStream.write(0xBB);
      outStream.write(0xBF);

      String[] headers = {
        "NAME",
        "TITLE",
        "DESCRIPTION",
        "AUTHOR",
        "KEYWORDS",
        "STATUS",
        "ISSUE DATE",
        "REFERENCE",
        "EXPIRDATE",
        "SECRANK",
        "ATTRI1",
        "ATTRI2",
        "ATTRI3",
        "ATTRI4",
        "ATTRI5",
        "ATTRI6",
        "ATTRI7",
        "ATTRI8",
        "ATTRI9",
        "ATTRI10",
        "ATTRI11",
        "ATTRI12",
        "ATTRI13",
        "ATTRI14",
        "ATTRI15",
        "ATTRI16",
        "ATTRI17",
        "ATTRI18",
        "ATTRI19",
        "ATTRI20",
        "TYPE",
        "TRANSLATOR",
        "LANG",
        "NOCONTENT",
        "ORILANG",
        "RELTRANS",
        "OVERWRITE",
      };
      outStreamWriter.write(String.join("\t", headers));
      outStreamWriter.write('\n');

      outStreamWriter.flush();
      outStreamWriter.close();
    } catch (Exception ex) {
      logger.error(
        "Error when generating empty index template for the import file.",
        ex
      );
    }
  }

  /**
   * Imports a ZIP archive into a target folder. The uploaded stream is stored as
   * a temporary content node (normalizing the ZIP mime type and rejecting files
   * larger than 1&nbsp;MB), then imported asynchronously through the CIRCABC
   * importer action. Content notifications can optionally be suppressed during
   * the import.
   *
   * @param folderId the Alfresco node id of the destination folder
   * @param fileInputStream the input stream of the ZIP file to import
   * @param fileName the name of the uploaded file
   * @param mimeType the mime type of the uploaded file (ZIP variants are
   *     normalized)
   * @param notifyUser {@code true} to notify the importing user on completion
   * @param deleteFile {@code true} to delete the uploaded archive after import
   * @param disableNotification {@code true} to disable content notifications
   *     during the import
   * @param encoding the character encoding used to read the archive entries
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
    String encoding
  ) {
    try {
      if (disableNotification) {
        policyBehaviourFilter.disableBehaviour(
          ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
        );
      }

      NodeRef folderNodeRef = Converter.createNodeRefFromId(folderId);

      String userName = AuthenticationUtil.getFullyAuthenticatedUser();

      // create the node to import and write its content
      Map<QName, Serializable> properties = new HashMap<>();

      properties.put(ContentModel.PROP_NAME, fileName);

      NodeRef nodeRefToImport = nodeService
        .createNode(
          folderNodeRef,
          ContentModel.ASSOC_CONTAINS,
          QName.createQName(
            ContentModel.TYPE_CONTENT.getNamespaceURI(),
            fileName
          ),
          ContentModel.TYPE_CONTENT,
          properties
        )
        .getChildRef();

      ContentWriter contentWriter = contentService.getWriter(
        nodeRefToImport,
        ContentModel.PROP_CONTENT,
        true
      );

      // only imports ZIP files with MimetypeMap.MIMETYPE_ZIP mime type...
      if ("application/x-zip-compressed".equals(mimeType)) {
        mimeType = MimetypeMap.MIMETYPE_ZIP;
      }

      contentWriter.setMimetype(mimeType);
      contentWriter.putContent(fileInputStream);

      ContentReader contentReader = contentService.getReader(
        nodeRefToImport,
        ContentModel.PROP_CONTENT
      );
      long fileSize = contentReader.getSize();

      final long maxSizeInBytes = 1024L * 1024L;

      if (fileSize > maxSizeInBytes) {
        throw new IllegalStateException(
          "File is too big to be imported maximum size in bytes : " +
            maxSizeInBytes +
            " current size in bytes " +
            fileSize
        );
      }

      // do the actual import
      Map<String, Serializable> params = new HashMap<>(2, 1.0f);
      params.put(
        ImporterActionExecuter.PARAM_DESTINATION_FOLDER,
        folderNodeRef
      );
      params.put(ImporterActionExecuter.PARAM_ENCODING, encoding);
      params.put(CircabcImporterActionExecuter.PARAM_DELETE_FILE, deleteFile);
      params.put(
        CircabcImporterActionExecuter.PARAM_DISABLE_FILE_NOTIFICATION,
        disableNotification
      );

      String notifyUserName = "";

      if (notifyUser) {
        notifyUserName = userName;
      }
      params.put(
        CircabcImporterActionExecuter.PARAM_NOTIFY_USER,
        notifyUserName
      );

      // build the action to execute
      Action action = actionService.createAction(
        CircabcImporterActionExecuter.NAME,
        params
      );
      action.setExecuteAsynchronously(true);

      // execute the action on the ACP file
      actionService.executeAction(action, nodeRefToImport);
    } catch (DuplicateChildNodeNameException e) {
      throw new IllegalArgumentException(
        "A file with this name already exists",
        e
      );
    } catch (Exception e) {
      throw e;
    }
  }

  /**
   * Invites one or more users into an Interest Group without synchronizing with
   * the CIRCABC (CBC) tables. Used during Interest Group creation, where the
   * synchronization performed by {@link #groupsIdMembersPost} is not desired.
   *
   * @param groupNodeRef the {@link NodeRef} of the Interest Group
   * @param body the memberships to create
   * @return a {@link MembershipPostDefinition} listing the memberships that were
   *     actually added
   */
  @Override
  public MembershipPostDefinition groupsIdMembersPostNoSync(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  ) {
    return inviteMemberInternal(groupNodeRef, body);
  }

  private MembershipPostDefinition inviteMemberInternal(
    NodeRef groupNodeRef,
    MembershipPostDefinition body
  ) {
    MembershipPostDefinition result = new MembershipPostDefinition();
    for (UserProfile userProfile : body.getMemberships()) {
      try {
        ensureUserExists(userProfile.getUser().getUserId());
        inviteSingleMember(groupNodeRef, body, userProfile, result);
      } catch (RuntimeException e) {
        logger.error(
          "Impossible to invite the following user:" + userProfile.toString(),
          e
        );
      }
    }
    return result;
  }

  private void ensureUserExists(String userName) {
    if (personService.personExists(userName)) {
      return;
    }
    final CircabcUserDataBean user = new CircabcUserDataBean();
    user.setUserName(userName);
    final CircabcUserDataBean ldapUserDetail =
      userService.getLDAPUserDataNoFilterByUid(userName);
    user.copyLdapProperties(ldapUserDetail);
    userService.createUser(user, true);
  }

  private void inviteSingleMember(
    NodeRef groupNodeRef,
    MembershipPostDefinition body,
    UserProfile userProfile,
    MembershipPostDefinition result
  ) {
    String userName = userProfile.getUser().getUserId();
    if (isAlreadyMember(groupNodeRef, userName)) {
      return;
    }

    final String parentName = userProfile.getProfile().getGroupName();
    AuthenticationUtil.runAs(
      () -> {
        authorityService.addAuthority(parentName, userName);
        return null;
      },
      AuthenticationUtil.getAdminUserName()
    );

    if (!circabcService.isUserExists(userName)) {
      circabcService.addUser(userName);
    }
    circabcService.addPersonToProfile(
      groupNodeRef,
      userName,
      userProfile.getProfile().getName()
    );

    result.addMembershipsItem(userProfile);
    historyApi.cancelWaitingCleanPermissions(userName, groupNodeRef);

    Date expirationDate = body.getExpirationDate();
    if (expirationDate != null) {
      historyApi.addExpirationDate(
        userName,
        groupNodeRef.getId(),
        userProfile.getProfile().getId(),
        userProfile.getProfile().getGroupName(),
        expirationDate
      );
    }
    if (Boolean.TRUE.equals(body.getUserNotifications())) {
      notifyUser(
        groupNodeRef,
        userProfile,
        false,
        body.getNotifyText(),
        expirationDate
      );
    }
    if (Boolean.TRUE.equals(body.getAdminNotifications())) {
      notifyMembershipAdministrators(groupNodeRef, userProfile, false);
    }
  }

  private void notifyMembershipAdministrators(
    NodeRef groupNodeRef,
    UserProfile userProfile,
    Boolean changeProfile
  ) {
    List<Profile> groupProfiles = profilesApi.groupsIdProfilesGet(
      groupNodeRef.getId(),
      null,
      false
    );
    Set<NotifiableUser> users = new HashSet<>();
    for (Profile profile : groupProfiles) {
      if (
        !profile
          .getPermissions()
          .get(MEMBERS)
          .equals(DIRECTORY_ADMIN_PERMISSION)
      ) {
        continue;
      }
      List<String> profileMatch = new ArrayList<>();
      profileMatch.add(profile.getGroupName());
      PagedUserProfile admins = groupsIdMembersGet(
        groupNodeRef.getId(),
        profileMatch,
        "en",
        -1,
        -1,
        null,
        null
      );
      for (UserProfile admin : admins.getData()) {
        addNotifiableUser(users, admin.getUser().getUserId());
      }
    }

    try {
      if (Boolean.TRUE.equals(changeProfile)) {
        notificationService.notifyUpdateMemberships(
          groupNodeRef,
          users,
          userProfile
        );
      } else {
        notificationService.notifyNewMemberships(
          groupNodeRef,
          users,
          userProfile
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during notification of user: " +
            userProfile.getUser().getUserId() +
            " in the invitation.",
          e
        );
      }
    }
  }

  private void addNotifiableUser(Set<NotifiableUser> users, String userId) {
    NodeRef person = personService.getPerson(userId);
    Map<QName, Serializable> properties = nodeService.getProperties(person);
    Locale locale = resolveUserLocale(person);

    Boolean globalNotif = true;
    try {
      properties = nodeService.getProperties(person);
      globalNotif = (Boolean) properties.get(
        UserModel.PROP_GLOBAL_NOTIFICATION
      );
    } catch (Exception e) {
      logger.error(" Can not read global notification for user " + userId, e);
    }

    if (globalNotif == null || globalNotif) {
      users.add(new NotifiableUserImpl(person, locale, properties));
    }
  }

  private Locale resolveUserLocale(NodeRef person) {
    Serializable langObject = userService.getPreference(
      person,
      UserService.PREF_INTERFACE_LANGUAGE
    );
    if (langObject == null) {
      return null;
    } else if (langObject instanceof Locale loc) {
      return loc;
    }
    return Locale.of(langObject.toString());
  }

  private void notifyUser(
    NodeRef groupNodeRef,
    UserProfile userProfile,
    boolean membershipUpdate,
    String notificationText,
    Date expirationDate
  ) {
    Set<NotifiableUser> users = new HashSet<>();

    NodeRef person = personService.getPerson(userProfile.getUser().getUserId());
    Map<QName, Serializable> properties = nodeService.getProperties(person);
    Locale locale = resolveUserLocale(person);

    users.add(new NotifiableUserImpl(person, locale, properties));

    try {
      if (!membershipUpdate) {
        notificationService.notify(
          groupNodeRef,
          users,
          NotificationType.NOTIFY_USER_INVITATION,
          notificationText,
          expirationDate
        );
      } else {
        notificationService.notify(
          groupNodeRef,
          users,
          NotificationType.NOTIFY_USER_MEMBERSHIP_UPDATE,
          null,
          expirationDate
        );
      }
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during notification of user: " +
            userProfile.getUser().getUserId() +
            " in the invitation.",
          e
        );
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

  /**
   * Returns the Interest Groups most recently visited by the current user.
   *
   * @param amount the maximum number of groups to return; {@code 0} means return
   *     all visited groups
   * @return the list of recently visited {@link InterestGroup}s
   * @throws IllegalAccessError if the current username cannot be resolved
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
   * Returns the Interest Groups most recently visited by the given user, based on
   * the REST visit logs. Non-existent and duplicate groups are skipped.
   *
   * @param username the username whose visit history is read
   * @param amount the maximum number of groups to return; {@code 0} means return
   *     all visited groups
   * @return the list of recently visited {@link InterestGroup}s
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
      NodeRef igNodeRef = Converter.createNodeRefFromId(igId);
      if (!igIds.contains(igId) && nodeService.exists(igNodeRef)) {
        InterestGroup ig = getInterestGroupDetails(igNodeRef);
        interestGroups.add(ig);
        igIds.add(igId);
        amount--;
      }
    }

    return interestGroups;
  }

  /**
   * Returns the configuration of an Interest Group (currently the newsgroups
   * "new forum/topic" flag settings). If no configuration is stored, sensible
   * defaults are returned.
   *
   * @param groupIp the Alfresco node id of the Interest Group
   * @return the {@link GroupConfiguration}
   */
  @Override
  public GroupConfiguration getInterestGroupConfiguration(String groupIp) {
    NodeRef groupRef = Converter.createNodeRefFromId(groupIp);

    GroupConfiguration result = new GroupConfiguration();
    Serializable groupConf = nodeService.getProperty(
      groupRef,
      CircabcModel.PROP_IG_ROOT_CONFIGURATION
    );

    if (groupConf != null) {
      result = extractConfiguration(groupConf.toString());
    } else {
      fillDefaultGroupConfiguration(result);
    }

    return result;
  }

  private GroupConfiguration extractConfiguration(String body) {
    JSONParser parser = new JSONParser();
    GroupConfiguration result = new GroupConfiguration();
    try {
      JSONObject json = (JSONObject) parser.parse(body);
      JSONObject newsgroups = (JSONObject) json.get(NEWSGROUPS);
      GroupConfigurationNewsgroups confNews =
        new GroupConfigurationNewsgroups();

      if (newsgroups != null && newsgroups.get("enableFlagNewForum") != null) {
        boolean enableFlagNewForum = (boolean) newsgroups.get(
          "enableFlagNewForum"
        );
        confNews.setEnableFlagNewForum(enableFlagNewForum);
      } else {
        confNews.setEnableFlagNewForum(false);
      }

      if (newsgroups != null && newsgroups.get("enableFlagNewTopic") != null) {
        boolean enableFlagNewTopic = (boolean) newsgroups.get(
          "enableFlagNewTopic"
        );
        confNews.setEnableFlagNewTopic(enableFlagNewTopic);
      } else {
        confNews.setEnableFlagNewTopic(false);
      }

      if (newsgroups != null && newsgroups.get("ageFlagNewTopic") != null) {
        Integer ageFlagNewTopic = Integer.valueOf(
          newsgroups.get("ageFlagNewTopic").toString()
        );
        confNews.setAgeFlagNewTopic(ageFlagNewTopic);
      } else {
        confNews.setAgeFlagNewTopic(7);
      }

      if (newsgroups != null && newsgroups.get("ageFlagNewForum") != null) {
        Integer ageFlagNewForum = Integer.valueOf(
          newsgroups.get("ageFlagNewForum").toString()
        );
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

  private void fillDefaultGroupConfiguration(GroupConfiguration result) {
    GroupConfigurationNewsgroups confNews = new GroupConfigurationNewsgroups();
    confNews.setEnableFlagNewForum(false);
    confNews.setEnableFlagNewTopic(false);
    confNews.setAgeFlagNewForum(7);
    confNews.setAgeFlagNewTopic(7);
    result.setNewsgroups(confNews);
  }

  /**
   * Persists the configuration of an Interest Group, serializing it to JSON and
   * storing it on the IG root node.
   *
   * @param groupIp the Alfresco node id of the Interest Group
   * @param body the configuration to store
   * @return the stored {@link GroupConfiguration} (the same instance passed in)
   */
  @Override
  public GroupConfiguration putInterestGroupConfiguration(
    String groupIp,
    GroupConfiguration body
  ) {
    String jsonBody = body.toJsonString();

    NodeRef groupRef = Converter.createNodeRefFromId(groupIp);

    nodeService.setProperty(
      groupRef,
      CircabcModel.PROP_IG_ROOT_CONFIGURATION,
      jsonBody
    );

    return body;
  }

  /**
   * Returns the logo images available for an Interest Group (the content of its
   * look-and-feel images container).
   *
   * @param groupId the Alfresco node id of the Interest Group
   * @return the list of logo {@link Node}s (empty if none exist)
   */
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
   * Uploads a new logo image for an Interest Group. The filename is validated
   * against an allowed set of image extensions, the images container is created
   * on demand, and the image is stored under a timestamp-prefixed name after the
   * file size is validated against the configured maximum.
   *
   * @param groupId the Alfresco node id of the Interest Group
   * @param inputStream the input stream of the image to upload
   * @param filename the original name of the uploaded image
   * @return the created logo {@link Node}, or {@code null} if no input stream was
   *     provided
   * @throws IllegalArgumentException if the file type is not an allowed image
   *     type
   */
  @Override
  public Node postGroupLogoByGroupId(
    String groupId,
    InputStream inputStream,
    String filename
  ) {
    try {
      ESAPI.validator().getValidFileName(
        "submitted file",
        filename,
        new ArrayList<>(Arrays.asList(".jpg", ".jpeg", ".bmp", ".gif", ".png")),
        false
      );
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
        calDate.get(Calendar.YEAR) +
        "-" +
        calDate.get(Calendar.MONTH) +
        "-" +
        calDate.get(Calendar.DAY_OF_MONTH) +
        "-" +
        calDate.get(Calendar.HOUR) +
        "-" +
        calDate.get(Calendar.MINUTE) +
        "-" +
        calDate.get(Calendar.SECOND) +
        "-" +
        filename;

      QName associationNameQName = QName.createQName(
        ContentModel.PROP_NAME.getNamespaceURI(),
        newfilename
      );

      Map<QName, Serializable> props = new HashMap<>();
      props.put(ContentModel.PROP_NAME, newfilename);

      NodeRef nodeRef = nodeService
        .createNode(
          imagesRef,
          ContentModel.ASSOC_CHILDREN,
          associationNameQName,
          CircabcModel.TYPE_CUSTOMIZATION_CONTENT,
          props
        )
        .getChildRef();

      final ContentWriter writer = contentService.getWriter(
        nodeRef,
        CircabcModel.PROP_CONTENT,
        true
      );
      writer.setMimetype(mimetypeService.guessMimetype(filename));

      File tempFile = null;

      try {
        long attachmentTotalSize = Long.parseLong(
          circabcConfig.getLogoAllowedSizeinBytes()
        );

        tempFile = ApiToolBox.checkAndGetImageFile(
          filename,
          inputStream,
          attachmentTotalSize
        );
        writer.putContent(tempFile);
      } finally {
        if (tempFile != null) {
          try {
            java.nio.file.Files.deleteIfExists(tempFile.toPath());
          } catch (java.io.IOException e) {
            if (logger.isErrorEnabled()) {
              logger.error("Can not delete file" + tempFile.toString());
            }
          }
        }
      }

      return nodesApi.getNode(nodeRef);
    }

    return null;
  }

  private NodeRef findChildByName(NodeRef parent, String name) {
    for (ChildAssociationRef child : nodeService.getChildAssocs(parent)) {
      if (
        name.equals(
          nodeService
            .getProperty(child.getChildRef(), ContentModel.PROP_NAME)
            .toString()
        )
      ) {
        return child.getChildRef();
      }
    }
    return null;
  }

  private NodeRef getGroupLogosContainer(NodeRef groupRef) {
    Set<QName> typesContainer = new HashSet<>();
    typesContainer.add(CircabcModel.TYPE_CUSTOMIZATION_CONTAINER);
    List<ChildAssociationRef> customContainers = nodeService.getChildAssocs(
      groupRef,
      typesContainer
    );
    if (customContainers.isEmpty()) {
      return null;
    }

    NodeRef customizationContainerRef = customContainers.get(0).getChildRef();

    Set<QName> typesChildrenContainer = new HashSet<>();
    typesChildrenContainer.add(CircabcModel.TYPE_CUSTOMIZATION_FOLDER);
    List<ChildAssociationRef> customFolders = nodeService.getChildAssocs(
      customizationContainerRef,
      typesChildrenContainer
    );

    NodeRef igLookRef = null;
    for (ChildAssociationRef customFolder : customFolders) {
      if (
        IG_LOOK_AND_FEEL.equals(
          nodeService
            .getProperty(customFolder.getChildRef(), ContentModel.PROP_NAME)
            .toString()
        )
      ) {
        igLookRef = customFolder.getChildRef();
        break;
      }
    }

    if (igLookRef == null) {
      return null;
    }

    NodeRef iconRef = findChildByName(igLookRef, "icon");
    if (iconRef == null) {
      return null;
    }

    return findChildByName(iconRef, IMAGES);
  }

  private NodeRef createImagesContainer(NodeRef groupRef) {
    NodeRef customizationContainerRef;
    Set<QName> typesContainer = new HashSet<>();
    typesContainer.add(CircabcModel.TYPE_CUSTOMIZATION_CONTAINER);
    List<ChildAssociationRef> customContainers = nodeService.getChildAssocs(
      groupRef,
      typesContainer
    );
    if (customContainers.isEmpty()) {
      customizationContainerRef = nodeService
        .createNode(
          groupRef,
          CircabcModel.ASSOC_CUSTOMIZE,
          QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
            "customizationContainer"
          ),
          CircabcModel.TYPE_CUSTOMIZATION_CONTAINER
        )
        .getChildRef();
    } else {
      customizationContainerRef = customContainers.get(0).getChildRef();
    }

    NodeRef igLookRef = findOrCreateCustomizationFolder(
      customizationContainerRef,
      IG_LOOK_AND_FEEL
    );
    NodeRef iconRef = findOrCreateCustomizationFolder(igLookRef, "icon");
    return findOrCreateCustomizationFolder(iconRef, IMAGES);
  }

  private NodeRef findOrCreateCustomizationFolder(
    NodeRef parentRef,
    String name
  ) {
    NodeRef found = findChildByName(parentRef, name);
    if (found != null) {
      return found;
    }
    NodeRef created = nodeService
      .createNode(
        parentRef,
        ContentModel.ASSOC_CHILDREN,
        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
        CircabcModel.TYPE_CUSTOMIZATION_FOLDER
      )
      .getChildRef();
    nodeService.setProperty(created, ContentModel.PROP_NAME, name);
    return created;
  }

  /**
   * Selects the logo to be displayed on the Interest Group's main page. If the
   * given logo is already the current default it is unset (toggle off);
   * otherwise it becomes the default.
   *
   * @param groupId the Alfresco node id of the Interest Group
   * @param logoId the Alfresco node id of the logo to select
   * @throws CustomizationException if the logo configuration cannot be read or
   *     updated
   */
  @Override
  public void putSelectedLogo(String groupId, String logoId)
    throws CustomizationException {
    NodeRef groupRef = Converter.createNodeRefFromId(groupId);
    NodeRef logoReference = Converter.createNodeRefFromId(logoId);
    logoPreferencesService.getOrCreateConfiguraton(groupRef, true);
    logoPreferencesService.setMainPageLogoConfig(
      groupRef,
      true,
      -1,
      -1,
      false,
      false
    );
    logoPreferencesService.forceClearCache();
    DefaultLogoConfiguration defaultLogoConfiguration =
      logoPreferencesService.getDefault(groupRef);
    if (
      defaultLogoConfiguration.getLogo() != null &&
      defaultLogoConfiguration.getLogo().getReference() != null &&
      defaultLogoConfiguration.getLogo().getReference().equals(logoReference)
    ) {
      logoPreferencesService.setDefault(groupRef, null);
    } else {
      logoPreferencesService.setDefault(groupRef, logoReference);
    }
  }

  /**
   * Deletes a logo image from an Interest Group.
   *
   * @param groupId the Alfresco node id of the Interest Group
   * @param logoId the Alfresco node id of the logo to delete
   * @throws CustomizationException if the logo cannot be removed
   */
  @Override
  public void deleteLogo(String groupId, String logoId)
    throws CustomizationException {
    NodeRef groupRef = Converter.createNodeRefFromId(groupId);
    NodeRef logoReference = Converter.createNodeRefFromId(logoId);
    String fileName = nodeService
      .getProperty(logoReference, ContentModel.PROP_NAME)
      .toString();
    logoPreferencesService.removeLogo(groupRef, fileName);
  }

  /**
   * Returns the number of members in an Interest Group.
   *
   * @param igId the Alfresco node id of the Interest Group
   * @return the member count
   */
  @Override
  public int countMembersInIg(String igId) {
    return circabcService.countMembersInIg(igId);
  }

  /**
   * Removes the membership expiration date of a user in an Interest Group.
   *
   * @param groupId the Alfresco node id of the Interest Group
   * @param userId the username whose expiration date is removed
   */
  @Override
  public void groupsIdMembersUserIdExpirationDelete(
    String groupId,
    String userId
  ) {
    historyApi.deleteExpirationDate(userId, groupId);
  }

  /**
   * Updates the membership expiration date of a user in an Interest Group.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param userId the username whose expiration date is updated
   * @param expirationDate the new expiration date
   */
  @Override
  public void groupsIdMembersUserIdExpirationPut(
    String id,
    String userId,
    Date expirationDate
  ) {
    historyApi.updateExpirationDate(id, userId, expirationDate);
  }

  /**
   * Adds a membership expiration date for a user in an Interest Group, tied to a
   * specific profile and Alfresco group.
   *
   * @param id the Alfresco node id of the Interest Group
   * @param userId the username for which the expiration date is added
   * @param expirationDate the expiration date to set
   * @param profileId the id of the profile the membership belongs to
   * @param alfrescoGroup the Alfresco authority group of the membership
   */
  @Override
  public void groupsIdMembersUserIdExpirationPost(
    String id,
    String userId,
    Date expirationDate,
    String profileId,
    String alfrescoGroup
  ) {
    historyApi.addExpirationDate(
      userId,
      id,
      profileId,
      alfrescoGroup,
      expirationDate
    );
  }

  private boolean isInterestGroup(NodeRef igNodeRef) {
    return nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT);
  }

  private void writeCSVData(
    List<StatData> interestGroupData,
    OutputStream outStream
  ) throws IOException {
    // write byte order mark
    try (
      OutputStreamWriter outStreamWriter = new OutputStreamWriter(
        outStream,
        StandardCharsets.UTF_8
      );
    ) {
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
    }
  }

  private void writeXMLData(
    List<StatData> interestGroupData,
    OutputStream outStream
  ) throws XMLStreamException {
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

  private void writeXLS(List<StatData> statData, OutputStream outStream)
    throws IOException {
    try (Workbook workbook = new HSSFWorkbook()) {
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
  }

  private void writeCSVTimeline(
    List<ActivityCountDAO> igTimeLineActivity2,
    OutputStream outStream
  ) {
    OutputStreamWriter outStreamWriter;

    try {
      outStreamWriter = new OutputStreamWriter(
        outStream,
        StandardCharsets.UTF_8
      );

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

  private void writeXMLTimeline(
    List<ActivityCountDAO> igTimeLineActivity2,
    OutputStream outStream
  ) {
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

        xtw.writeAttribute(
          "date",
          simpleDateFormat.format(dim.getMonthActivity())
        );
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

  private void writeXLSTimeline(
    List<ActivityCountDAO> igTimeLineActivities,
    OutputStream outStream
  ) throws IOException {
    try (Workbook workbook = new HSSFWorkbook()) {
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
        row
          .createCell(0)
          .setCellValue(simpleDateFormat.format(dim.getMonthActivity()));
        row.createCell(1).setCellValue(dim.getService());
        row.createCell(2).setCellValue(dim.getActivity());
        row.createCell(3).setCellValue(dim.getActionNumber());
        idx++;
      }

      workbook.write(outStream);
    }
  }

  /**
   * Flags an Interest Group as (not) pending deletion in the CIRCABC database.
   *
   * @param interestGroupID the CIRCABC database id of the Interest Group
   * @param b {@code true} to mark the group as to-be-deleted, {@code false}
   *     otherwise
   */
  @Override
  public void updateIgToBeDeleted(long interestGroupID, boolean b) {
    circabcDaoService.updateIgToBeDeleted(interestGroupID, b);
  }
}
