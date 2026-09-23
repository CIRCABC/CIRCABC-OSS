package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.action.SystemAddFeaturesActionExecuter;
import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.group.request.GroupRequestsDaoService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.statistic.global.GlobalStatisticsService;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.AdminContactRequest;
import io.swagger.model.Category;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.EmailDefinition;
import io.swagger.model.GroupCreationRequest;
import io.swagger.model.GroupCreationRequestApproval;
import io.swagger.model.GroupDeletionRequest;
import io.swagger.model.GroupDeletionRequestApproval;
import io.swagger.model.I18nProperty;
import io.swagger.model.InterestGroup;
import io.swagger.model.InterestGroupPostModel;
import io.swagger.model.MembershipPostDefinition;
import io.swagger.model.Node;
import io.swagger.model.PagedGroupCreationRequests;
import io.swagger.model.PagedGroupDeletionRequests;
import io.swagger.model.PagedStatisticsContents;
import io.swagger.model.Profile;
import io.swagger.model.ReportFile;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.KeywordModel;
import io.swagger.model.db.CategoryAdmin;
import io.swagger.model.db.ExportedProfileItem;
import io.swagger.model.db.InterestGroupItem;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.IgPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.model.permissions.VisibilityPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.RestInputSanitizer;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default implementation of {@link CategoriesApi} containing the business logic
 * for the CIRCABC Category domain.
 *
 * <p>A Category is an Alfresco node (carrying the {@code CircabcModel.ASPECT_CATEGORY}
 * aspect) that lives directly under the CircaBC root and groups a set of Interest
 * Groups (IGs). This class handles the full lifecycle around categories and their
 * child interest groups, including:</p>
 *
 * <ul>
 *   <li>listing categories and their interest groups, honouring the caller's access
 *       rights (administrators see every IG, other users only the ones they belong to);</li>
 *   <li>creating a category under a header and creating an interest group under a
 *       category, together with the required Alfresco groups (master / subscription /
 *       invited-user groups), service folders (Library, Newsgroups, Events, Information,
 *       Directory), keyword container, predefined profiles and permissions;</li>
 *   <li>managing category administrators (add / list / remove);</li>
 *   <li>managing the category logo (upload, select, delete, list);</li>
 *   <li>handling group creation and group deletion requests together with the
 *       associated approval workflow and e-mail notifications;</li>
 *   <li>computing and paging category / interest-group statistics.</li>
 * </ul>
 *
 * <p>Collaborating Alfresco services and CIRCABC APIs are injected via Spring
 * {@code @Autowired} fields. This class is not itself a REST endpoint; the
 * webscript classes in {@code eu.europa.ec.digit.circabc.rest} delegate to the
 * methods declared by {@link CategoriesApi}.</p>
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

  private static final QName CIRCA_BC_ADMIN_GROUP_PROPERTY = QName.createQName(
    "http://www.cc.cec/circabc/model/content/1.0",
    "circaBCAdminGroup"
  );

  private static final QName PROP_CIRCABC_PROFILE_GROUP_NAME =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaBCProfileGroupName"
    );
  private static final QName PROP_CIRCABC_PROFILE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaBCProfileName"
  );

  public static final String CIRCA_CATEGORY_ADMIN = "CircaCategoryAdmin";
  private static final QName CATEGORY_ADMIN_PROFILE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    CIRCA_CATEGORY_ADMIN
  );
  private static final QName ALL_CIRCA_USERS_PROFILE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    EVERYONE
  );
  private static final QName GUEST_PROFILE_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    GUEST
  );

  private static final QName QNAME_CATEGORY_ADMIN_SERVICE = QName.createQName(
    "CATEGORY"
  );

  private static final QName CATEGORY_PROFILE_GROUP_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryProfileGroupName"
  );
  private static final QName CATEGORY_PROFILE_PROP_NAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryProfileName"
  );

  private static final QName ASSOC_CATEGORY_SERVICE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryServiceAssoc"
  );
  private static final QName TYPE_CATEGORY_SERVICE = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaCategoryService"
  );

  private static final QName PROP_CATEGORY_ADMIN_SERVICE_NAME =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryServiceName"
    );
  private static final QName PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryPermissionSet"
    );

  private static final QName PROP_CIRCABC_SUBGROUP = QName.createQName(
    "http://www.cc.cec/circabc/model/content/1.0",
    "circaBCSubsGroup"
  );

  private static final QName LOGO_CONTAINER_QNAME = QName.createQName(
    LOGO_CONTAINER
  );
  private static final String NO_VISIBILITY = "NoVisibility";
  private static final String GROUP_EVERYONE = "GROUP_EVERYONE";
  private static final String ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION =
    "Add CircabcNotify Aspect Description";
  private static final String CATEG_STAT_JOB = "categStatJob-";
  private static final String CIRCA_CATEGORY_ACCESS = "CircaCategoryAccess";
  private static final String ADD_CIRCABC_NOTIFY_ASPECT =
    "Add CircabcNotify Aspect";

  // Controls whether Alfresco rules execute asynchronously; default is false
  private boolean executeAsync = true;

  @Autowired
  private NodeService nodeService;

  @Autowired
  @Qualifier("authorityService")
  private AuthorityService authorityService;

  @Autowired
  private PersonService personService;

  @Autowired
  @Qualifier("NodeService") // NOSONAR
  private NodeService secureNodeService;

  @Autowired
  private MimetypeService mimetypeService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private NodesApi nodesApi;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private GroupsApi groupsApi;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private ActionService actionService;

  @Autowired
  private UserService userService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private OwnableService ownableService;

  @Autowired
  private CircabcDaoServiceImpl circabcDaoService;

  @Autowired
  private UsersApi usersApi;

  @Autowired
  private RuleService ruleService;

  @Autowired
  private CircabcDaoServiceImpl circabcDaoServiceImpl;

  @Autowired
  private GroupRequestsDaoService groupRequestsDaoService;

  @Autowired
  private EmailApi emailApi;

  @Autowired
  private GlobalStatisticsService globalStatisticsService;

  @Autowired
  private LockService circabcLockService;

  @Autowired
  @Qualifier("defaultAsyncThreadPool")
  private ThreadPoolExecutor asyncThreadPoolExecutor;

  @Autowired
  private TransactionService transactionService;

  @Autowired
  private CircabcApi circabcApi;

  /**
   * Returns every Category defined directly under the CircaBC root node.
   *
   * <p>Child nodes are filtered on the {@code CircabcModel.ASPECT_CATEGORY}
   * aspect; for each matching node the id, name and (localised) title are
   * populated.</p>
   *
   * @return the list of categories; never {@code null}, possibly empty
   */
  public List<Category> getCategories() {
    List<Category> categories = new ArrayList<>();
    List<ChildAssociationRef> children = nodeService.getChildAssocs(
      circabcApi.getCircabcNodeRef()
    );
    for (ChildAssociationRef child : children) {
      if (
        nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_CATEGORY)
      ) {
        Category category = new Category();
        final NodeRef childRef = child.getChildRef();
        category.setId(childRef.getId());
        category.setName(
          (String) nodeService.getProperty(childRef, ContentModel.PROP_NAME)
        );
        final Serializable property = nodeService.getProperty(
          child.getChildRef(),
          ContentModel.PROP_TITLE
        );
        if (property instanceof MLText mlText) {
          category.setTitle(Converter.toI18NProperty(mlText));
        } else if (property instanceof String str) {
          category.setTitle(Converter.toI18NProperty(str));
        }
        categories.add(category);
      }
    }
    return categories;
  }

  /**
   * Returns the interest groups contained in the given category that are
   * visible to the currently authenticated user.
   *
   * <p>If the caller is an administrator all interest groups of the category are
   * returned; otherwise only the interest groups the user is a member of are
   * returned. When the supplied id does not identify a category an empty list is
   * returned.</p>
   *
   * @param id the node id of the category
   * @return the list of visible interest groups; never {@code null}, possibly empty
   */
  @Override
  public List<InterestGroup> getInterestGroupByCategoryId(String id) {
    List<InterestGroup> result = new ArrayList<>();
    NodeRef categoryNodeRef = Converter.createNodeRefFromId(id);
    if (!isCategory(categoryNodeRef)) {
      return result;
    }

    String userName = AuthenticationUtil.getFullyAuthenticatedUser();
    User user = usersApi.usersUserIdGet(userName);

    if (authorityService.isAdminAuthority(userName)) {
      List<NodeRef> allGroups = getInterestGroups(categoryNodeRef);
      for (NodeRef igRef : allGroups) {
        InterestGroup ig = groupsApi.getInterestGroup(igRef.getId(), true);
        result.add(ig);
      }
    } else {
      List<InterestGroupItem> interestGroups =
        circabcService.getInterestGroupByCategoryUser(
          categoryNodeRef,
          userName
        );
      for (InterestGroupItem interestGroupItem : interestGroups) {
        InterestGroup ig = Converter.toInterestGroup(
          interestGroupItem,
          user.getUiLang()
        );
        NodeRef igRef = Converter.createNodeRefFromId(ig.getId());
        Map<String, String> title = circabcService.getInterestGroupTitle(igRef);
        ig.setTitle(Converter.convertMlToI18nProperty(title));
        ig.setDescription(Converter.convertMlToI18nProperty(title));
        result.add(ig);
      }
    }

    return result;
  }

  /**
   * Tests whether the given node is a Category.
   *
   * @param categoryNodeRef the node to test
   * @return {@code true} if the node carries the category aspect, {@code false} otherwise
   */
  private boolean isCategory(NodeRef categoryNodeRef) {
    return nodeService.hasAspect(categoryNodeRef, CircabcModel.ASPECT_CATEGORY);
  }

  /**
   * Returns the profiles exported from the interest groups of the given category,
   * so they can be imported/reused when configuring another interest group.
   *
   * @param id         the node id of the category
   * @param ignoreIgId optional node id of an interest group whose own exported
   *                   profiles must be excluded from the result; may be {@code null}
   *                   or empty to include all
   * @return the list of exported profiles; never {@code null}, possibly empty
   */
  @Override
  public List<Profile> categoriesIdExportedProfilesGet(
    String id,
    String ignoreIgId
  ) {
    NodeRef categoryRef = Converter.createNodeRefFromId(id);
    Long categoryId = (Long) nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_NODE_DBID
    );
    List<Profile> result = new ArrayList<>();

    List<ExportedProfileItem> exportedProfiles;

    if (ignoreIgId != null && !"".equals(ignoreIgId)) {
      NodeRef igRef = Converter.createNodeRefFromId(ignoreIgId);
      Long igId = (Long) nodeService.getProperty(
        igRef,
        ContentModel.PROP_NODE_DBID
      );
      exportedProfiles =
        circabcDaoServiceImpl.selectExpProfilesByCategoryIDInterestGroupID(
          categoryId,
          igId
        );
    } else {
      exportedProfiles = circabcDaoServiceImpl.selectExpProfilesByCategoryID(
        categoryId
      );
    }

    for (ExportedProfileItem expProf : exportedProfiles) {
      Profile profile = new Profile();
      profile.setId(
        expProf
          .getProfileRef()
          .substring(expProf.getProfileRef().lastIndexOf('/') + 1)
      );
      profile.setExported(true);
      profile.setImported(false);
      // need to substring because nodeRef from DB contains
      // 'workspace//...'
      profile.setImportedRef(
        expProf
          .getNodeRef()
          .substring(expProf.getNodeRef().lastIndexOf('/') + 1)
      );
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

  /**
   * Creates a new interest group under the given category.
   *
   * <p>This provisions the full interest-group structure: the IG node and its
   * properties, the master and invited-user Alfresco groups, the service folders
   * (Library, Newsgroups, Events, Information, Directory), the keyword container,
   * the predefined profiles (Guest, Registered, Leader, Access, Author) with the
   * configured leaders, and the associated permissions. Leaders that do not yet
   * exist as users are created, and the interest group is finally resynchronised.</p>
   *
   * @param id the node id of the parent category
   * @param ig the interest group definition; its name must be non-null, non-empty
   *           and unique within the category
   * @return the details of the newly created interest group
   * @throws IllegalArgumentException if the name is null/empty, if an interest
   *                                  group with the same name already exists, or
   *                                  if the created IG reference is {@code null}
   */
  @Override
  public InterestGroup categoriesIdGroupsPost(
    String id,
    InterestGroupPostModel ig
  ) {
    final NodeRef categoryRef = Converter.createNodeRefFromId(id);
    if (ig.getName() == null || ig.getName().isEmpty()) {
      throw new IllegalArgumentException(
        "Interest group name must not be null or empty."
      );
    }
    if (
      nodeService.getChildByName(
        categoryRef,
        ContentModel.ASSOC_CONTAINS,
        ig.getName()
      ) !=
      null
    ) {
      throw new IllegalArgumentException(
        "Interest group with name " + ig.getName() + " already exists."
      );
    }
    NodeRef igRef;

    // CREATE IG NODE and setup properties
    igRef = createIgNode(ig, categoryRef);
    createMasterGroup(igRef);
    createInvitedGroup(igRef);
    final List<NodeRef> igFolders = createIGFolders(igRef);
    createKeywordContainer(igRef);
    createPredefinedProfiles(
      igRef,
      ig.getLeaders(),
      ig.getNotify(),
      ig.getNotifyText().getDefaultValue()
    );
    setInterestGropPermission(igRef);
    for (NodeRef folder : igFolders) {
      cutInheritanceAndReApplyPermissions(folder);
    }

    if (igRef == null) {
      throw new IllegalArgumentException("IG reference 'igRef' is null.");
    }

    InterestGroup interestGroupDetails = groupsApi.getInterestGroupDetails(
      igRef,
      false
    );

    if (interestGroupDetails != null) {
      for (String leader : ig.getLeaders()) {
        if (!circabcService.isUserExists(leader)) {
          circabcService.addUser(leader);
        }
      }
      circabcService.resyncInterestGroup(igRef);
    }

    return interestGroupDetails;
  }

  /**
   * Restricts the visibility of a freshly created interest group: removes the
   * default visibility permissions for guest and everyone, applies the
   * {@code NoVisibility} permission, disables self-registration and cuts
   * permission inheritance from the parent category.
   *
   * @param igRef the interest group node
   */
  private void setInterestGropPermission(NodeRef igRef) {
    permissionService.deletePermission(igRef, GUEST, "Visibility");
    permissionService.setPermission(igRef, GUEST, NO_VISIBILITY, true);
    permissionService.deletePermission(igRef, GROUP_EVERYONE, "Visibility");
    permissionService.setPermission(igRef, GROUP_EVERYONE, NO_VISIBILITY, true);
    nodeService.setProperty(
      igRef,
      CircabcModel.PROP_CAN_REGISTERED_APPLY,
      false
    );
    permissionService.setInheritParentPermissions(igRef, false);
  }

  /**
   * Cuts permission inheritance from the parent for the given service folder so
   * that permissions can be applied explicitly.
   *
   * @param folder the service folder node
   */
  private void cutInheritanceAndReApplyPermissions(NodeRef folder) {
    permissionService.setInheritParentPermissions(folder, false);
  }

  /**
   * Creates the set of predefined profiles for a new interest group (Guest,
   * Registered/Everyone, Leader, Access and Author) and enrols the supplied
   * users into the Leader profile, creating any missing user accounts from LDAP
   * first.
   *
   * @param igRef      the interest group node
   * @param users      the user ids to enrol as leaders
   * @param notify     whether the enrolled leaders should be notified
   * @param notifyText the notification message body
   */
  private void createPredefinedProfiles(
    NodeRef igRef,
    List<String> users,
    Boolean notify,
    String notifyText
  ) {
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
        CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(
          user
        );

        userService.createUser(userDataBean, true);
      }
      groupsApi.groupsIdMembersPostNoSync(igRef, body);
    }

    createAccessProfile(igRef);

    createAuthorProfile(igRef);
  }

  /**
   * Creates the predefined "Author" profile (edit access to Library and post
   * access to Newsgroups, read access elsewhere) on the interest group.
   *
   * @param igRef the interest group node
   */
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

  /**
   * Creates the predefined "Access" profile (read access to every service) on
   * the interest group.
   *
   * @param igRef the interest group node
   */
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

  /**
   * Creates the predefined "Leader" profile (administrator access to every
   * service) on the interest group.
   *
   * @param igRef the interest group node
   * @return the persisted Leader profile
   */
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
    leaderProfile = profilesApi.groupsIdProfilesPostNoSync(
      igRef,
      leaderProfile
    );
    return leaderProfile;
  }

  /**
   * Creates the predefined "Registered" (Everyone) profile with no access to any
   * service and no visibility, on the interest group.
   *
   * @param igRef the interest group node
   */
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

  /**
   * Creates the predefined "Guest" profile with no access to any service and no
   * visibility, on the interest group.
   *
   * @param igRef the interest group node
   */
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

  /**
   * Creates the keyword container node under the interest group and grants read
   * access to all authorities and to guests.
   *
   * @param igRef the interest group node
   */
  private void createKeywordContainer(NodeRef igRef) {
    QName assocIgKeywordContainer = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "igKwContainer"
    );
    final ChildAssociationRef assocRef = nodeService.createNode(
      igRef,
      assocIgKeywordContainer,
      KeywordModel.TYPE_KEYWORD_CONTAINER,
      KeywordModel.TYPE_KEYWORD_CONTAINER,
      new PropertyMap()
    );

    final NodeRef kwContainerNodeRef = assocRef.getChildRef();

    permissionService.setPermission(
      kwContainerNodeRef,
      PermissionService.ALL_AUTHORITIES,
      PermissionService.ALL_PERMISSIONS,
      true
    );

    permissionService.setPermission(
      kwContainerNodeRef,
      GUEST,
      PermissionService.ALL_PERMISSIONS,
      true
    );
  }

  /**
   * Creates the standard service folders under the interest group (Directory,
   * Library, Events, Newsgroups and Information).
   *
   * <p>Note: the Directory folder is created but deliberately excluded from the
   * returned list, because its permission inheritance must remain enabled while
   * the callers cut inheritance on the returned folders.</p>
   *
   * @param igRef the interest group node
   * @return the service folders whose permission inheritance must be cut
   *         (Library, Newsgroups, Events, Information)
   */
  private List<NodeRef> createIGFolders(NodeRef igRef) {
    List<NodeRef> result = new ArrayList<>(5);
    // DIRECTORY
    nodeService
      .createNode(
        igRef,
        CircabcModel.ASSOC_IG_DIRECTORY_CONTAINER,
        CircabcModel.TYPE_DIRECTORY_SERVICE,
        CircabcModel.TYPE_DIRECTORY_SERVICE,
        new PropertyMap()
      )
      .getChildRef();
    NodeRef libRef = createLibrary(igRef);
    NodeRef eventRef = createEvent(igRef);
    NodeRef newsRef = createNewsGroup(igRef);
    NodeRef infoRef = createInformation(igRef);

    // directoryRef should not be set in the returned list. Because later on, the
    // inheritance will
    // be cut
    // the directoryRef should have inheritance set to true

    result.add(libRef);
    result.add(newsRef);
    result.add(eventRef);
    result.add(infoRef);
    return result;
  }

  /**
   * Creates the Information service folder for the interest group, applies the
   * information aspects, grants administrator permission to the category admin
   * group, and installs the inbound rules that add the Information and
   * content-notify aspects to child content.
   *
   * @param igRef the interest group node
   * @return the created Information folder node
   */
  private NodeRef createInformation(NodeRef igRef) {
    // INFORMATION
    String information = "Information";
    QName assocInfQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      information
    );
    Map<QName, Serializable> infProps = new HashMap<>();
    infProps.put(ContentModel.PROP_NAME, information);
    infProps.put(
      ContentModel.PROP_TITLE,
      Converter.toMLText(Converter.toI18NProperty(information))
    );
    NodeRef infoRef = nodeService
      .createNode(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        assocInfQName,
        ContentModel.TYPE_FOLDER,
        infProps
      )
      .getChildRef();

    nodeService.addAspect(infoRef, CircabcModel.ASPECT_INFORMATION, null);
    nodeService.addAspect(infoRef, CircabcModel.ASPECT_INFORMATION_ROOT, null);

    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
    permissionService.setPermission(
      infoRef,
      categoryAdminProfileGroupName,
      InformationPermissions.INFADMIN.toString(),
      true
    );

    // Add Information
    final CompositeAction compositeActionInformation =
      actionService.createCompositeAction();

    // Create Action
    final Action actionInformation = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionInformation.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      CircabcModel.ASPECT_INFORMATION
    );
    compositeActionInformation.addAction(actionInformation);
    compositeActionInformation.setTitle("Add Information Aspect");
    compositeActionInformation.setDescription(
      "Add Information Aspect Description"
    );

    // Create Condition
    final ActionCondition actionConditionInformation =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);
    actionConditionInformation.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CMOBJECT
    );

    compositeActionInformation.addActionCondition(actionConditionInformation);

    // Create a rule
    final Rule ruleInfo = new Rule();
    ruleInfo.setRuleType(RuleType.INBOUND);

    ruleInfo.applyToChildren(true);
    ruleInfo.setExecuteAsynchronously(executeAsync);
    ruleInfo.setAction(compositeActionInformation);
    ruleInfo.setTitle(compositeActionInformation.getTitle());
    ruleInfo.setDescription(compositeActionInformation.getDescription());
    ruleService.saveRule(infoRef, ruleInfo);

    final CompositeAction compositeActionInfoNotify =
      actionService.createCompositeAction();

    // Create Action
    final Action actionInfoNotify = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionInfoNotify.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
    compositeActionInfoNotify.addAction(actionInfoNotify);
    compositeActionInfoNotify.setTitle(ADD_CIRCABC_NOTIFY_ASPECT);
    compositeActionInfoNotify.setDescription(
      ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION
    );

    // Create Condition
    final ActionCondition actionConditionInfoNotify =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);
    actionConditionInfoNotify.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CONTENT
    );
    compositeActionInfoNotify.addActionCondition(actionConditionInfoNotify);

    // Create a rule
    final Rule ruleInfoNotify = new Rule();
    ruleInfoNotify.setRuleType(RuleType.INBOUND);

    ruleInfoNotify.applyToChildren(true);
    ruleInfoNotify.setExecuteAsynchronously(executeAsync);
    ruleInfoNotify.setAction(compositeActionInfoNotify);
    ruleInfoNotify.setTitle(compositeActionInfoNotify.getTitle());
    ruleInfoNotify.setDescription(compositeActionInfoNotify.getDescription());
    ruleService.saveRule(infoRef, ruleInfoNotify);
    return infoRef;
  }

  /**
   * Creates the Newsgroups (forums) service folder for the interest group,
   * applies the newsgroup aspects, grants administrator permission to the
   * category admin group, and installs the inbound rules that add the newsgroup
   * and content-notify aspects to child content.
   *
   * @param igRef the interest group node
   * @return the created Newsgroups folder node
   */
  private NodeRef createNewsGroup(NodeRef igRef) {
    // NEWSGROUPS
    String newsgroups = "Newsgroups";
    QName assocNwsQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      newsgroups
    );
    Map<QName, Serializable> nwsProps = new HashMap<>();
    nwsProps.put(ContentModel.PROP_NAME, newsgroups);
    nwsProps.put(
      ContentModel.PROP_TITLE,
      Converter.toMLText(Converter.toI18NProperty(newsgroups))
    );
    NodeRef newsRef = nodeService
      .createNode(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        assocNwsQName,
        ForumModel.TYPE_FORUMS,
        nwsProps
      )
      .getChildRef();

    nodeService.addAspect(newsRef, CircabcModel.ASPECT_NEWSGROUP, null);
    nodeService.addAspect(newsRef, CircabcModel.ASPECT_NEWSGROUP_ROOT, null);

    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
    permissionService.setPermission(
      newsRef,
      categoryAdminProfileGroupName,
      NewsGroupPermissions.NWSADMIN.toString(),
      true
    );

    // Add CircaNewsGroup
    final CompositeAction compositeActionNewsGroup =
      actionService.createCompositeAction();

    // Create Action
    final Action actionNewsGroup = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionNewsGroup.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      CircabcModel.ASPECT_NEWSGROUP
    );
    compositeActionNewsGroup.addAction(actionNewsGroup);
    compositeActionNewsGroup.setTitle("Add CircaNewsGroup Aspect");
    compositeActionNewsGroup.setDescription(
      "Add CircaNewsGroup Aspect Description"
    );

    // Create Condition
    final ActionCondition actionConditionNewsGroup =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);
    actionConditionNewsGroup.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CMOBJECT
    );

    compositeActionNewsGroup.addActionCondition(actionConditionNewsGroup);

    // Create a rule
    final Rule ruleNews = new Rule();
    ruleNews.setRuleType(RuleType.INBOUND);

    ruleNews.applyToChildren(true);
    ruleNews.setExecuteAsynchronously(executeAsync);
    ruleNews.setAction(compositeActionNewsGroup);
    ruleNews.setTitle(compositeActionNewsGroup.getTitle());
    ruleNews.setDescription(compositeActionNewsGroup.getDescription());
    ruleService.saveRule(newsRef, ruleNews);

    final CompositeAction compositeActionNewsNotify =
      actionService.createCompositeAction();

    // Create Action
    final Action actionNewsNotify = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionNewsNotify.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
    compositeActionNewsNotify.addAction(actionNewsNotify);
    compositeActionNewsNotify.setTitle(ADD_CIRCABC_NOTIFY_ASPECT);
    compositeActionNewsNotify.setDescription(
      ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION
    );

    // Create Condition
    final ActionCondition actionConditionNewsNotify =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);
    actionConditionNewsNotify.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CONTENT
    );
    compositeActionNewsNotify.addActionCondition(actionConditionNewsNotify);

    // Create a rule
    final Rule ruleNewsNotify = new Rule();
    ruleNewsNotify.setRuleType(RuleType.INBOUND);

    ruleNewsNotify.applyToChildren(true);
    ruleNewsNotify.setExecuteAsynchronously(executeAsync);
    ruleNewsNotify.setAction(compositeActionNewsNotify);
    ruleNewsNotify.setTitle(compositeActionNewsNotify.getTitle());
    ruleNewsNotify.setDescription(compositeActionNewsNotify.getDescription());
    ruleService.saveRule(newsRef, ruleNewsNotify);
    return newsRef;
  }

  /**
   * Creates the Events service folder for the interest group, applies the event
   * root aspect, grants administrator permission to the category admin group, and
   * installs the inbound rule that adds the event aspect to child content.
   *
   * @param igRef the interest group node
   * @return the created Events folder node
   */
  private NodeRef createEvent(NodeRef igRef) {
    // EVENTS
    String events = "Events";
    QName assocEvtQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      events
    );
    Map<QName, Serializable> evtProps = new HashMap<>();
    evtProps.put(ContentModel.PROP_NAME, events);
    evtProps.put(
      ContentModel.PROP_TITLE,
      Converter.toMLText(Converter.toI18NProperty(events))
    );
    NodeRef eventRef = nodeService
      .createNode(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        assocEvtQName,
        ContentModel.TYPE_FOLDER,
        evtProps
      )
      .getChildRef();
    nodeService.addAspect(
      eventRef,
      CircabcModel.ASPECT_EVENT_ROOT,
      new HashMap<>()
    );

    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
    permissionService.setPermission(
      eventRef,
      categoryAdminProfileGroupName,
      EventPermissions.EVEADMIN.toString(),
      true
    );

    // Add CircaNewsGroup
    final CompositeAction compositeActionEvent =
      actionService.createCompositeAction();

    // Create Action
    final Action actionEvent = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionEvent.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      CircabcModel.ASPECT_EVENT
    );
    compositeActionEvent.addAction(actionEvent);
    compositeActionEvent.setTitle("Add Event Aspect");
    compositeActionEvent.setDescription("Add Event Aspect Description");

    // Create Condition
    final ActionCondition actionConditionEvent =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);
    actionConditionEvent.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CMOBJECT
    );

    compositeActionEvent.addActionCondition(actionConditionEvent);

    // Create a rule
    final Rule ruleEvent = new Rule();
    ruleEvent.setRuleType(RuleType.INBOUND);

    ruleEvent.applyToChildren(true);
    ruleEvent.setExecuteAsynchronously(executeAsync);
    ruleEvent.setAction(compositeActionEvent);
    ruleEvent.setTitle(compositeActionEvent.getTitle());
    ruleEvent.setDescription(compositeActionEvent.getDescription());
    ruleService.saveRule(eventRef, ruleEvent);
    return eventRef;
  }

  /**
   * Creates the Library service folder for the interest group, applies the
   * library/ui-facets/ownable aspects, grants administrator permission to the
   * category admin group, and installs the inbound rules that add the
   * CircaDocument, CircaLibrary and content-notify aspects to child content.
   *
   * @param igRef the interest group node
   * @return the created Library folder node
   */
  private NodeRef createLibrary(NodeRef igRef) {
    // LIBRARY
    String library = "Library";
    QName assocLibQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      library
    );
    Map<QName, Serializable> libProps = new HashMap<>();
    libProps.put(ContentModel.PROP_NAME, library);
    libProps.put(
      ContentModel.PROP_TITLE,
      Converter.toMLText(Converter.toI18NProperty(library))
    );
    NodeRef libRef = nodeService
      .createNode(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        assocLibQName,
        ContentModel.TYPE_FOLDER,
        libProps
      )
      .getChildRef();
    nodeService.addAspect(
      libRef,
      CircabcModel.ASPECT_LIBRARY_ROOT,
      new HashMap<>()
    );
    nodeService.addAspect(
      libRef,
      ApplicationModel.ASPECT_UIFACETS,
      new HashMap<>()
    );

    Map<QName, Serializable> ownableProps = new HashMap<>();
    ownableProps.put(ContentModel.PROP_OWNER, ADMIN);
    nodeService.addAspect(libRef, ContentModel.ASPECT_OWNABLE, ownableProps);

    NodeRef categoryRef = nodeService.getPrimaryParent(igRef).getParentRef();
    String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);
    permissionService.setPermission(
      libRef,
      categoryAdminProfileGroupName,
      LibraryPermissions.LIBADMIN.toString(),
      true
    );

    // Add CircaDocument
    final CompositeAction compositeAction =
      actionService.createCompositeAction();

    // Create Action
    final Action action = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    action.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      DocumentModel.ASPECT_CIRCABC_DOCUMENT
    );
    compositeAction.addAction(action);
    compositeAction.setTitle("Add CircaDocument Aspect");
    compositeAction.setDescription("Add CircaDocument Aspect Description");

    // Create Condition
    final ActionCondition actionConditionCircaDocument =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);

    actionConditionCircaDocument.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CONTENT
    );

    compositeAction.addActionCondition(actionConditionCircaDocument);

    // Create a rule
    final Rule rule = new Rule();
    rule.setRuleType(RuleType.INBOUND);

    rule.applyToChildren(true);
    rule.setExecuteAsynchronously(executeAsync);
    rule.setAction(compositeAction);
    rule.setTitle(compositeAction.getTitle());
    rule.setDescription(compositeAction.getDescription());
    ruleService.saveRule(libRef, rule);

    // Add CircaLibrary
    final CompositeAction compositeActionLibrary =
      actionService.createCompositeAction();

    // Create ActionN
    final Action actionLibrary = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionLibrary.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      CircabcModel.ASPECT_LIBRARY
    );
    compositeActionLibrary.addAction(actionLibrary);
    compositeActionLibrary.setTitle("Add CircaLibrary Aspect");
    compositeActionLibrary.setDescription(
      "Add CircaLibrary Aspect Description"
    );

    // Create Condition
    final ActionCondition actionConditionLibrary =
      actionService.createActionCondition(NoConditionEvaluator.NAME);
    compositeActionLibrary.addActionCondition(actionConditionLibrary);

    // Create a rule
    final Rule ruleLibrary = new Rule();
    ruleLibrary.setRuleType(RuleType.INBOUND);

    ruleLibrary.applyToChildren(true);
    ruleLibrary.setExecuteAsynchronously(executeAsync);
    ruleLibrary.setAction(compositeActionLibrary);
    ruleLibrary.setTitle(compositeActionLibrary.getTitle());
    ruleLibrary.setDescription(compositeActionLibrary.getDescription());
    ruleService.saveRule(libRef, ruleLibrary);

    // Add CircaManagement
    final CompositeAction compositeActionNotify =
      actionService.createCompositeAction();

    // Create Action
    final Action actionNotify = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionNotify.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      ContentNotifyAspect.ASPECT_CONTENT_NOTIFY
    );
    compositeActionNotify.addAction(actionNotify);
    compositeActionNotify.setTitle(ADD_CIRCABC_NOTIFY_ASPECT);
    compositeActionNotify.setDescription(ADD_CIRCABC_NOTIFY_ASPECT_DESCRIPTION);

    // Create Condition
    final ActionCondition actionConditionNotify =
      actionService.createActionCondition(IsSubTypeEvaluator.NAME);
    actionConditionNotify.setParameterValue(
      IsSubTypeEvaluator.PARAM_TYPE,
      ContentModel.TYPE_CONTENT
    );
    compositeActionNotify.addActionCondition(actionConditionNotify);

    // Create a rule
    final Rule ruleNotify = new Rule();
    ruleNotify.setRuleType(RuleType.INBOUND);

    ruleNotify.applyToChildren(true);
    ruleNotify.setExecuteAsynchronously(executeAsync);
    ruleNotify.setAction(compositeActionNotify);
    ruleNotify.setTitle(compositeActionNotify.getTitle());
    ruleNotify.setDescription(compositeActionNotify.getDescription());
    ruleService.saveRule(libRef, ruleNotify);
    return libRef;
  }

  /**
   * Creates the interest group root node under the category and configures it:
   * sets the name, applies the IG-root, CircaBC-management and ui-facets aspects
   * (title, description, icon and optional contact information), grants the
   * category admin group the directory-admin, IG-delete and visibility
   * permissions, and sets the node owner.
   *
   * @param ig          the interest group definition
   * @param categoryRef the parent category node
   * @return the created interest group node
   */
  private NodeRef createIgNode(InterestGroupPostModel ig, NodeRef categoryRef) {
    QName assocQName = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      ig.getName()
    );
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, ig.getName());

    NodeRef igRef = nodeService
      .createNode(
        categoryRef,
        ContentModel.ASSOC_CONTAINS,
        assocQName,
        ContentModel.TYPE_FOLDER,
        props
      )
      .getChildRef();
    Map<QName, Serializable> igRootprops = new HashMap<>();
    if (ig.getContact() != null) {
      igRootprops.put(
        CircabcModel.PROP_CONTACT_INFORMATION,
        Converter.toMLText(RestInputSanitizer.sanitizeRichText(ig.getContact()))
      );
    }

    nodeService.addAspect(igRef, CircabcModel.ASPECT_IGROOT, igRootprops);
    Map<QName, Serializable> cbcManagementrops = new HashMap<>();
    nodeService.addAspect(
      igRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      cbcManagementrops
    );

    // apply the uifacets aspect - icon, title and description props
    final Map<QName, Serializable> uiFacetsProps = HashMap.newHashMap(5);
    uiFacetsProps.put(ApplicationModel.PROP_ICON, SPACE_ICON_DEFAULT);

    if (ig.getTitle() != null) {
      uiFacetsProps.put(
        ContentModel.PROP_TITLE,
        Converter.toMLText(ig.getTitle())
      );
    } else {
      uiFacetsProps.put(ContentModel.PROP_TITLE, null);
    }

    if (ig.getDescription() != null) {
      uiFacetsProps.put(
        ContentModel.PROP_DESCRIPTION,
        Converter.toMLText(
          RestInputSanitizer.sanitizeRichText(ig.getDescription())
        )
      );
    } else {
      uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, null);
    }

    nodeService.addAspect(
      igRef,
      ApplicationModel.ASPECT_UIFACETS,
      uiFacetsProps
    );

    String categoryAdminProfileGroupName = getCategoryGroupName(categoryRef);

    permissionService.setPermission(
      igRef,
      categoryAdminProfileGroupName,
      DirectoryPermissions.DIRADMIN.toString(),
      true
    );
    permissionService.setPermission(
      igRef,
      categoryAdminProfileGroupName,
      IgPermissions.IGDELETE.toString(),
      true
    );
    permissionService.setPermission(
      igRef,
      categoryAdminProfileGroupName,
      VisibilityPermissions.VISIBILITY.toString(),
      true
    );

    ownableService.setOwner(igRef, ADMIN);

    return igRef;
  }

  /**
   * Returns a page of the statistics report files generated for the given
   * category, sorted by descending modification date.
   *
   * @param id        the node id of the category
   * @param startItem the zero-based index of the first item to return
   * @param amount    the maximum number of items to return; {@code 0} means
   *                  return all items
   * @return the requested page of report files together with the total count
   * @throws IllegalArgumentException if the node is not a Category
   */
  @Override
  public PagedStatisticsContents getIGStatisticsContents(
    String id,
    int startItem,
    int amount
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (!nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      throw new IllegalArgumentException(
        "Node with id '" + id + "' is not a Category node."
      );
    }

    String categoryName = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NAME
    );

    List<FileInfo> contents =
      globalStatisticsService.getCategoryGroupStatsFiles(categoryName, nodeRef);

    List<ReportFile> result = new ArrayList<>();

    for (FileInfo fileInfo : contents) {
      result.add(new ReportFile(fileInfo, fileInfo.getName()));
    }

    Collections.sort(result, (ReportFile rf1, ReportFile rf2) ->
      rf2
        .getFileInfo()
        .getModifiedDate()
        .compareTo(rf1.getFileInfo().getModifiedDate())
    );

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
   * Triggers asynchronous computation of the interest-group statistics for the
   * given category. The work is submitted to a thread pool and skipped if a
   * statistics job is already running (locked) for the category.
   *
   * @param id the node id of the category
   * @throws IllegalArgumentException if the node is not a Category
   */
  @Override
  public void calculateIGStatistics(String id) {
    NodeRef categoryRef = Converter.createNodeRefFromId(id);

    if (!nodeService.hasAspect(categoryRef, CircabcModel.ASPECT_CATEGORY)) {
      throw new IllegalArgumentException(
        "Node with id '" + id + "' is not a Category node."
      );
    }

    if (!circabcLockService.isLocked(CATEG_STAT_JOB + categoryRef.getId())) {
      Runnable runnable = new CategoryIgStatisticsRunnable(
        AuthenticationUtil.getSystemUserName(),
        categoryRef,
        transactionService
      );
      asyncThreadPoolExecutor.execute(runnable);
    }
  }

  /**
   * Persists a group-creation request for the given category and notifies the
   * category administrators by e-mail. E-mail failures are logged and do not
   * abort the request.
   *
   * @param categoryId the node id of the category
   * @param body       the group-creation request payload
   */
  @Override
  public void categoriesIdGroupRequestPost(
    String categoryId,
    GroupCreationRequest body
  ) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    body.setCategoryRef(categoryRef.getId());
    groupRequestsDaoService.saveRequest(body);

    try {
      List<User> admins = categoriesIdAdminsGet(categoryId);
      EmailDefinition email = emailApi.prepareEmailForGroupRequest(
        body,
        admins
      );
      emailApi.mailPost(email);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("problem sending email", e);
      }
    }
  }

  /**
   * Returns the administrators of the given category as fully resolved users.
   *
   * @param categoryId the node id of the category
   * @return the list of category administrators; never {@code null}, possibly empty
   */
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
   * Returns the logo image nodes stored in the given category's logo folder.
   *
   * @param categoryId the node id of the category
   * @return the list of logo nodes; never {@code null}, empty when no logo
   *         folder or no logos exist
   */
  @Override
  public List<Node> getCategoryLogoByCategoryId(String categoryId) {
    List<Node> result = new ArrayList<>();
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    NodeRef logoFolderRef = getLogoFolderRef(categoryRef);

    if (logoFolderRef != null) {
      List<ChildAssociationRef> children = secureNodeService.getChildAssocs(
        logoFolderRef
      );
      for (ChildAssociationRef child : children) {
        result.add(nodesApi.getNode(child.getChildRef()));
      }
    }

    return result;
  }

  /**
   * Returns the child node that holds the logos of the given category, if any.
   *
   * @param categoryRef the category node
   * @return the logo folder node, or {@code null} if the category has none
   */
  private NodeRef getLogoFolderRef(NodeRef categoryRef) {
    NodeRef logoFolderRef = null;
    for (ChildAssociationRef child : nodeService.getChildAssocs(categoryRef)) {
      if (
        child
          .getTypeQName()
          .getLocalName()
          .equals(CircabcModel.ASSOC_CATEGORY_LOGOS.getLocalName())
      ) {
        logoFolderRef = child.getChildRef();
        break;
      }
    }
    return logoFolderRef;
  }

  /**
   * Resolves the given logo id and verifies it is a logo stored under the
   * authorized category's own logo folder, rejecting the request with
   * {@link AccessDeniedException} otherwise. This prevents pointing a category's
   * logo at, or deleting, a node that belongs to a different category.
   *
   * @param categoryRef the authorized category node
   * @param logoId      the id of the logo node to validate
   * @return the validated logo node reference
   */
  private NodeRef requireCategoryLogo(NodeRef categoryRef, String logoId) {
    NodeRef logoRef = Converter.createNodeRefFromId(logoId);
    NodeRef logoFolderRef = getLogoFolderRef(categoryRef);

    if (logoFolderRef != null && nodeService.exists(logoRef)) {
      ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(logoRef);
      if (
        parentAssoc != null && logoFolderRef.equals(parentAssoc.getParentRef())
      ) {
        return logoRef;
      }
    }

    throw new AccessDeniedException(
      "Logo does not belong to the authorized category"
    );
  }

  /**
   * Uploads a logo image for the given category.
   *
   * <p>The file name is validated to be an allowed image type
   * ({@code .jpg, .jpeg, .bmp, .gif, .png}). A logo folder is created if it does
   * not yet exist, the content is stored under a timestamped name (limited to the
   * configured maximum size) and the temporary file used during upload is cleaned
   * up on a best-effort basis.</p>
   *
   * @param categoryId  the node id of the category
   * @param inputStream the image content; when {@code null} nothing is stored
   * @param fileName    the original file name, used for validation and mimetype detection
   * @throws IllegalArgumentException if the file name is not an allowed image type
   */
  @Override
  public void postCategoryLogoByCategoryId(
    String categoryId,
    InputStream inputStream,
    String fileName
  ) {
    try {
      ESAPI.validator().getValidFileName(
        "submitted file",
        fileName,
        new ArrayList<>(Arrays.asList(".jpg", ".jpeg", ".bmp", ".gif", ".png")),
        false
      );
    } catch (ValidationException | IntrusionException vex) {
      throw new IllegalArgumentException("Invalid file type: " + fileName);
    }

    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    NodeRef logoFolderRef = getLogoFolderRef(categoryRef);

    if (logoFolderRef == null) {
      ChildAssociationRef childAssoc = nodeService.createNode(
        categoryRef,
        CircabcModel.ASSOC_CATEGORY_LOGOS,
        LOGO_CONTAINER_QNAME,
        ContentModel.TYPE_FOLDER
      );
      logoFolderRef = childAssoc.getChildRef();
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
        fileName;

      QName associationNameQName = QName.createQName(
        ContentModel.PROP_NAME.getNamespaceURI(),
        newfilename
      );

      Map<QName, Serializable> props = new HashMap<>();
      props.put(ContentModel.PROP_NAME, newfilename);

      NodeRef nodeRef = nodeService
        .createNode(
          logoFolderRef,
          ContentModel.ASSOC_CONTAINS,
          associationNameQName,
          ContentModel.TYPE_CONTENT,
          props
        )
        .getChildRef();

      final ContentWriter writer = contentService.getWriter(
        nodeRef,
        ContentModel.PROP_CONTENT,
        true
      );
      writer.setMimetype(mimetypeService.guessMimetype(fileName));

      File tempFile = null;

      try {
        long attachmentTotalSize = Long.parseLong(
          circabcConfig.getLogoAllowedSizeinBytes()
        );

        tempFile = ApiToolBox.checkAndGetImageFile(
          fileName,
          inputStream,
          attachmentTotalSize
        );
        writer.putContent(tempFile);
      } finally {
        if (tempFile != null) {
          try {
            java.nio.file.Files.deleteIfExists(tempFile.toPath());
          } catch (java.io.IOException e) {
            // best-effort cleanup
          }
        }
      }
    }
  }

  /**
   * Selects the given logo as the active logo of the category by setting the
   * category's logo reference property.
   *
   * @param categoryId the node id of the category
   * @param logoId     the node id of the logo to activate
   */
  @Override
  public void selectCategoryLogoByLogoId(String categoryId, String logoId) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    NodeRef logoRef = requireCategoryLogo(categoryRef, logoId);

    nodeService.setProperty(categoryRef, CircabcModel.PROP_LOGO_REF, logoRef);
  }

  /**
   * Deletes the given logo from the category. If the deleted logo was the active
   * one, the category's logo reference is cleared first.
   *
   * @param categoryId the node id of the category
   * @param logoId     the node id of the logo to delete
   * @return the remaining logos of the category after deletion
   */
  @Override
  public List<Node> deleteCategoryLogoByLogoId(
    String categoryId,
    String logoId
  ) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    NodeRef logoRef = requireCategoryLogo(categoryRef, logoId);

    Serializable logoRefSerialized = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_LOGO_REF
    );
    if (logoRef.equals(logoRefSerialized)) {
      nodeService.setProperty(categoryRef, CircabcModel.PROP_LOGO_REF, null);
    }

    nodeService.deleteNode(logoRef);

    return getCategoryLogoByCategoryId(categoryId);
  }

  /**
   * Returns the details of a single category: name, localised title, logo
   * reference, single-contact flag, contact-verified flag and contact e-mails.
   *
   * @param categoryId the node id of the category
   * @return the populated {@link Category}
   */
  @Override
  public Category categoriesIdGet(String categoryId) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    Category result = new Category();
    result.setId(categoryId);

    Serializable name = nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_NAME
    );
    if (name != null) {
      result.setName(name.toString());
    }

    Serializable title = nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_TITLE
    );
    if (title != null) {
      if (title instanceof String str) {
        result.setTitle(Converter.toI18NProperty(str));
      } else if (title instanceof MLText mlText) {
        result.setTitle(Converter.toI18NProperty(mlText));
      }
    }

    Serializable logoRef = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_LOGO_REF
    );
    if (logoRef != null) {
      result.setLogoRef(logoRef.toString());
    }

    Serializable useSingleContact = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_SINGLE_CONTACT
    );
    if (useSingleContact != null) {
      result.setUseSingleContact(
        Boolean.parseBoolean(useSingleContact.toString())
      );
    }

    Serializable contactVerified = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_CONTACT_VERIFIED
    );
    if (contactVerified != null) {
      result.setContactVerified(
        Boolean.parseBoolean(contactVerified.toString())
      );
    }

    Serializable contactMails = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_CONTACT_EMAILS
    );
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

  /**
   * Updates the mutable properties of a category (name, localised title,
   * single-contact flag and contact e-mails). If either argument is {@code null}
   * the input is returned unchanged.
   *
   * @param categoryId the node id of the category
   * @param category   the new category values
   * @return the supplied category, with its id set to {@code categoryId}
   */
  @Override
  public Category categoriesIdPut(String categoryId, Category category) {
    if (categoryId == null || category == null) {
      return category;
    }

    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    category.setId(categoryId);

    String name = category.getName();
    if (name != null && !name.isEmpty()) {
      this.nodeService.setProperty(
        categoryRef,
        ContentModel.PROP_NAME,
        name.trim()
      );
    }

    if (category.getTitle() != null) {
      MLText title = Converter.toMLText(category.getTitle());
      this.nodeService.setProperty(categoryRef, ContentModel.PROP_TITLE, title);
    }

    if (category.getUseSingleContact() != null) {
      nodeService.setProperty(
        categoryRef,
        CircabcModel.PROP_SINGLE_CONTACT,
        category.getUseSingleContact()
      );
    }

    updateContactEmails(categoryRef, category);

    return category;
  }

  /**
   * Replaces the contact e-mail list of the category with the values from the
   * supplied category (stored as a semicolon-separated string). When the stored
   * value changes and single-contact mode is enabled, the contact-verified flag
   * is reset to {@code false}. Does nothing when the category has no contact
   * e-mails set.
   *
   * @param categoryRef the category node
   * @param category    the category holding the new contact e-mails
   */
  private void updateContactEmails(NodeRef categoryRef, Category category) {
    if (category.getContactEmails() == null) {
      return;
    }

    StringBuilder emails = new StringBuilder();
    for (String email : category.getContactEmails()) {
      emails.append(email).append(";");
    }

    nodeService.setProperty(
      categoryRef,
      CircabcModel.PROP_CONTACT_EMAILS,
      emails.toString()
    );

    Serializable contactMails = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_CONTACT_EMAILS
    );
    if (
      !contactMails.equals(emails.toString()) &&
      Boolean.TRUE.equals(category.getUseSingleContact())
    ) {
      nodeService.setProperty(
        categoryRef,
        CircabcModel.PROP_CONTACT_VERIFIED,
        false
      );
    }
  }

  /**
   * Adds the given users as administrators of the category.
   *
   * <p>For each user id: the account is created from LDAP if it does not yet
   * exist, the user is added to the category admin authority (running as the
   * admin user) and a category-admin record is inserted. Nothing happens if the
   * category admin group does not exist or if either argument is {@code null}.</p>
   *
   * @param categoryId the node id of the category
   * @param userIds    the ids of the users to promote to category administrators
   * @return the ids of the users that were actually added
   */
  @Override
  public List<String> categoriesIdAdminsPost(
    String categoryId,
    List<String> userIds
  ) {
    List<String> userIdsAdded = new ArrayList<>();

    if (categoryId != null && userIds != null) {
      NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
      String categoryAdminGroup = getCategoryGroupName(categoryRef);

      if (authorityService.authorityExists(categoryAdminGroup)) {
        for (String userId : userIds) {
          if (!personService.personExists(userId)) {
            CircabcUserDataBean uData = ldapUserService.getLDAPUserDataByUid(
              userId
            );
            userService.createUser(uData, true);
          }
          AuthenticationUtil.runAs(
            () -> {
              authorityService.addAuthority(categoryAdminGroup, userId);
              return null;
            },
            AuthenticationUtil.getAdminUserName()
          );
          userIdsAdded.add(userId);

          insertCategoryAdmin(categoryRef, userId);
        }
      }
    }

    return userIdsAdded;
  }

  /**
   * Inserts a category-admin record linking the user (by database id) to the
   * category (by database id), creating the CIRCABC user record first if needed.
   *
   * @param categoryRef the category node
   * @param userId      the id of the user to record as administrator
   */
  private void insertCategoryAdmin(NodeRef categoryRef, String userId) {
    long categoryDbId = (long) nodeService.getProperty(
      categoryRef,
      ContentModel.PROP_NODE_DBID
    );
    NodeRef personNodeRef = personService.getPerson(userId);
    long userDbId = (long) nodeService.getProperty(
      personNodeRef,
      ContentModel.PROP_NODE_DBID
    );
    CategoryAdmin categoryAdmin = new CategoryAdmin(userDbId, categoryDbId);

    if (!circabcService.isUserExists(userId)) {
      circabcService.addUser(userId);
    }
    circabcDaoService.insertCategoryAdmin(categoryAdmin);
  }

  /**
   * Resolves the name of the Alfresco authority (group) associated with the
   * category's administrator profile.
   *
   * @param categoryRef the category node
   * @return the category admin group name
   */
  private String getCategoryGroupName(NodeRef categoryRef) {
    NodeRef categoryAdminProfileRef = nodeService
      .getChildAssocs(
        categoryRef,
        CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
        CATEGORY_ADMIN_PROFILE_NAME
      )
      .get(0)
      .getChildRef();
    return nodeService
      .getProperty(categoryAdminProfileRef, CATEGORY_PROFILE_GROUP_NAME)
      .toString();
  }

  /**
   * Removes the given user from the administrators of the category (both from the
   * Alfresco authority, running as the admin user, and from the CIRCABC
   * category-admin records). Does nothing if the category admin group does not
   * exist or the category id is {@code null}.
   *
   * @param categoryId the node id of the category
   * @param userId     the id of the user to demote
   */
  @Override
  public void categoriesIdAdminsDelete(String categoryId, String userId) {
    if (categoryId != null) {
      NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
      String circabcAdminGroup = getCategoryGroupName(categoryRef);

      if (authorityService.authorityExists(circabcAdminGroup)) {
        AuthenticationUtil.runAs(
          () -> {
            authorityService.removeAuthority(circabcAdminGroup, userId);
            return null;
          },
          AuthenticationUtil.getAdminUserName()
        );
        circabcService.removeCategoryAdmin(categoryRef, userId);
      }
    }
  }

  /**
   * Creates a new category under the given header.
   *
   * <p>The category node is created under the CircaBC root with the category,
   * management, classifiable and ui-facets aspects, linked to the header, and
   * provisioned with its master / subscription / invited-user Alfresco groups and
   * predefined category profiles (admin, registered, guest). The CircaBC admin
   * group is granted category-admin permission, inheritance is cut, ownership is
   * set and the category node is registered in the CIRCABC service.</p>
   *
   * @param headerId     the node id of the parent header
   * @param categoryBody the category definition (name and title)
   * @return the supplied category, with its id set to the created node id
   */
  @Override
  public Category headersIdCategoryPost(
    String headerId,
    Category categoryBody
  ) {
    NodeRef headerRef = Converter.createNodeRefFromId(headerId);

    NodeRef circabcNodeRef = getCircabcNode();

    NodeRef categoryRef = nodeService
      .createNode(
        circabcNodeRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(
          NamespaceService.CONTENT_MODEL_1_0_URI,
          categoryBody.getName()
        ),
        ContentModel.TYPE_FOLDER
      )
      .getChildRef();

    nodeService.setProperty(
      categoryRef,
      ContentModel.PROP_NAME,
      categoryBody.getName()
    );

    categoryBody.setId(categoryRef.getId());

    Map<QName, Serializable> circabcCategoryProps = new HashMap<>();

    nodeService.addAspect(
      categoryRef,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT,
      null
    );
    nodeService.addAspect(
      categoryRef,
      CircabcModel.ASPECT_CATEGORY,
      circabcCategoryProps
    );

    nodeService.addAspect(
      categoryRef,
      ContentModel.ASPECT_GEN_CLASSIFIABLE,
      null
    );

    Map<QName, Serializable> uiFacetsProps = new HashMap<>();
    uiFacetsProps.put(
      ContentModel.PROP_TITLE,
      Converter.toMLText(categoryBody.getTitle())
    );
    uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, "");
    uiFacetsProps.put(ApplicationModel.PROP_ICON, SPACE_ICON_DEFAULT);
    nodeService.addAspect(
      categoryRef,
      ApplicationModel.ASPECT_UIFACETS,
      uiFacetsProps
    );

    ArrayList<NodeRef> categories = new ArrayList<>();
    categories.add(headerRef);
    nodeService.setProperty(
      categoryRef,
      ContentModel.PROP_CATEGORIES,
      categories
    );
    createMasterGroup(categoryRef);
    createSubsGroup(categoryRef);
    createInvitedGroup(categoryRef);
    createCategoryPredefinedProfiles(categoryRef);
    setCategoryAdminPermissionToCircabcAdmin(circabcNodeRef, categoryRef);
    permissionService.setInheritParentPermissions(categoryRef, false);
    ownableService.setOwner(categoryRef, ADMIN);

    circabcService.addCategoryNode(headerRef, categoryRef);

    return categoryBody;
  }

  /**
   * Grants the CircaBC administrator group the category-admin permission on the
   * new category. The admin group name is taken from the CircaBC root property
   * when present, otherwise it is located by scanning the CircaBC child profiles
   * for the "CircaBCAdmin" profile.
   *
   * @param circabcNodeRef the CircaBC root node
   * @param categoryRef    the category node to secure
   */
  private void setCategoryAdminPermissionToCircabcAdmin(
    NodeRef circabcNodeRef,
    NodeRef categoryRef
  ) {
    String circabcAdminGroupName = (String) nodeService.getProperty(
      circabcNodeRef,
      CIRCA_BC_ADMIN_GROUP_PROPERTY
    );

    if (circabcAdminGroupName != null) {
      permissionService.setPermission(
        categoryRef,
        circabcAdminGroupName,
        CIRCA_CATEGORY_ADMIN,
        true
      );
      return;
    }

    for (ChildAssociationRef child : nodeService.getChildAssocs(
      circabcNodeRef
    )) {
      NodeRef childRef = child.getChildRef();
      if (
        nodeService
          .getProperty(childRef, PROP_CIRCABC_PROFILE_NAME)
          .toString()
          .equals("CircaBCAdmin")
      ) {
        circabcAdminGroupName = nodeService
          .getProperty(childRef, PROP_CIRCABC_PROFILE_GROUP_NAME)
          .toString();
        permissionService.setPermission(
          categoryRef,
          circabcAdminGroupName,
          CIRCA_CATEGORY_ADMIN,
          true
        );
        break;
      }
    }
  }

  /**
   * Locates the CircaBC root node ({@code Company Home/CircaBC}) in the
   * {@code workspace://SpacesStore}.
   *
   * @return the CircaBC root node, or {@code null} if it cannot be found
   */
  private NodeRef getCircabcNode() {
    StoreRef storeRef = new StoreRef(
      StoreRef.PROTOCOL_WORKSPACE,
      "SpacesStore"
    );
    NodeRef appRootNodeRef = nodeService.getRootNode(storeRef);
    NodeRef circabcRootNodeRef = null;
    for (ChildAssociationRef assoc : nodeService.getChildAssocs(
      appRootNodeRef
    )) {
      NodeRef childRef = assoc.getChildRef();
      if (
        nodeService
          .getProperty(childRef, ContentModel.PROP_NAME)
          .toString()
          .equals("Company Home")
      ) {
        circabcRootNodeRef = nodeService.getChildByName(
          childRef,
          ContentModel.ASSOC_CONTAINS,
          "CircaBC"
        );
      }
    }
    return circabcRootNodeRef;
  }

  /**
   * Creates the "master" Alfresco group for a category or interest group and
   * links it under the parent's subscription group when applicable. The generated
   * group name is stored in the appropriate node property (IG-root or category
   * master group). All authority operations run as the admin user.
   *
   * @param nodeRef the category or interest-group node
   * @return the name of the created master group
   */
  private String createMasterGroup(final NodeRef nodeRef) {
    String parentSubsGroupName = null;
    String prefixedParentSubsGroupName = null;

    NodeRef parentNodeRef = nodeService
      .getPrimaryParent(nodeRef)
      .getParentRef();

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      parentSubsGroupName = nodeService
        .getProperty(parentNodeRef, CircabcModel.PROP_CATEGORY_SUBS_GROUP)
        .toString();
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      parentSubsGroupName = nodeService
        .getProperty(parentNodeRef, PROP_CIRCABC_SUBGROUP)
        .toString();
    }

    if (parentSubsGroupName != null) {
      // Full AuthorityIdentifier
      prefixedParentSubsGroupName = authorityService.getName(
        AuthorityType.GROUP,
        parentSubsGroupName
      );
    }

    final String folderName = (
      (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
    ).replace(" ", "");
    // create the group introducing folder name in the group name
    final String masterGroupName =
      folderName + "--MasterGroup--" + GUID.generate();

    String createdAuthority = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(AuthorityType.GROUP, masterGroupName),
      AuthenticationUtil.getAdminUserName()
    );

    if (prefixedParentSubsGroupName != null && createdAuthority != null) {
      String prefixedMasterGroupName = authorityService.getName(
        AuthorityType.GROUP,
        masterGroupName
      );

      final String parentSubsGroupNameFinal = prefixedParentSubsGroupName;
      final String prefixedMasterGroupNameFinal = prefixedMasterGroupName;

      AuthenticationUtil.runAs(
        () -> {
          authorityService.addAuthority(
            parentSubsGroupNameFinal,
            prefixedMasterGroupNameFinal
          );
          return null;
        },
        AuthenticationUtil.getAdminUserName()
      );
    }

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      nodeService.setProperty(
        nodeRef,
        CircabcModel.PROP_IG_ROOT_MASTER_GROUP,
        masterGroupName
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      nodeService.setProperty(
        nodeRef,
        CircabcModel.PROP_CATEGORY_MASTER_GROUP,
        masterGroupName
      );
    }

    return masterGroupName;
  }

  /**
   * Creates the "subscription" Alfresco group for the node, nests it under the
   * node's master group and, for categories, stores its name in the category
   * subscription-group property. All authority operations run as the admin user.
   *
   * @param nodeRef the category or interest-group node
   * @return the name of the created subscription group
   */
  private String createSubsGroup(NodeRef nodeRef) {
    String prefixedMasterGroupName = null;
    String masterGroupName = "";

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      masterGroupName = nodeService
        .getProperty(nodeRef, CircabcModel.PROP_IG_ROOT_MASTER_GROUP)
        .toString();
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      masterGroupName = nodeService
        .getProperty(nodeRef, CircabcModel.PROP_CATEGORY_MASTER_GROUP)
        .toString();
    }

    if (masterGroupName != null) {
      // Full AuthorityIdentifier
      prefixedMasterGroupName = authorityService.getName(
        AuthorityType.GROUP,
        masterGroupName
      );
    }

    // get name of the folder
    final String folderName = (
      (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
    ).replace(" ", "");
    // create the group introducing folder name in the group name
    final String subsGroupName = folderName + "--SubsGroup--" + GUID.generate();

    String createdAuthority = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(AuthorityType.GROUP, subsGroupName),
      AuthenticationUtil.getAdminUserName()
    );

    if (createdAuthority != null) {
      String prefixedInvitedUsersGroupName = authorityService.getName(
        AuthorityType.GROUP,
        subsGroupName
      );

      final String prefixedMasterGroupNameFinal = prefixedMasterGroupName;
      final String prefixedInvitedUsersGroupNameFinal =
        prefixedInvitedUsersGroupName;

      AuthenticationUtil.runAs(
        () -> {
          authorityService.addAuthority(
            prefixedMasterGroupNameFinal,
            prefixedInvitedUsersGroupNameFinal
          );
          return null;
        },
        AuthenticationUtil.getAdminUserName()
      );
    }

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      nodeService.setProperty(
        nodeRef,
        CircabcModel.PROP_CATEGORY_SUBS_GROUP,
        subsGroupName
      );
    }

    return subsGroupName;
  }

  /**
   * Creates the "invited users" Alfresco group for the node, nests it under the
   * node's master group and stores its name in the appropriate property (IG-root
   * or category invited-user group). All authority operations run as the admin
   * user.
   *
   * @param nodeRef the category or interest-group node
   * @return the name of the created invited-users group
   */
  private String createInvitedGroup(NodeRef nodeRef) {
    String prefixedMasterGroupName = null;
    String masterGroupName = "";

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      masterGroupName = nodeService
        .getProperty(nodeRef, CircabcModel.PROP_IG_ROOT_MASTER_GROUP)
        .toString();
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      masterGroupName = nodeService
        .getProperty(nodeRef, CircabcModel.PROP_CATEGORY_MASTER_GROUP)
        .toString();
    }

    if (masterGroupName != null) {
      // Full AuthorityIdentifier
      prefixedMasterGroupName = authorityService.getName(
        AuthorityType.GROUP,
        masterGroupName
      );
    }

    // get name of the folder
    final String folderName = (
      (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
    ).replace(" ", "");
    // create the group introducing folder name in the group name
    final String subsGroupName =
      folderName + "--InvitedUsersGroup--" + GUID.generate();

    String createdAuthority = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(AuthorityType.GROUP, subsGroupName),
      AuthenticationUtil.getAdminUserName()
    );

    if (createdAuthority != null) {
      String prefixedInvitedUsersGroupName = authorityService.getName(
        AuthorityType.GROUP,
        subsGroupName
      );
      final String prefixedMasterGroupNameFinal = prefixedMasterGroupName;
      final String prefixedInvitedUsersGroupNameFinal =
        prefixedInvitedUsersGroupName;

      AuthenticationUtil.runAs(
        () -> {
          authorityService.addAuthority(
            prefixedMasterGroupNameFinal,
            prefixedInvitedUsersGroupNameFinal
          );
          return null;
        },
        AuthenticationUtil.getAdminUserName()
      );
    }

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
      nodeService.setProperty(
        nodeRef,
        CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP,
        subsGroupName
      );
    } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
      nodeService.setProperty(
        nodeRef,
        CircabcModel.PROP_CATEGORY_INVITED_USER_GROUP,
        subsGroupName
      );
    }

    return subsGroupName;
  }

  /**
   * Creates the predefined category profiles and their CATEGORY-service
   * permissions: the Category Administrator profile (with its own Alfresco group
   * nested under the invited-users group and CircaCategoryAdmin permission), the
   * Registered/Everyone profile and the Guest profile (both with
   * CircaCategoryAccess permission).
   *
   * @param categoryRef the category node
   */
  private void createCategoryPredefinedProfiles(NodeRef categoryRef) {
    // circaCategoryAdmin
    NodeRef categoryAdminProfileRef = nodeService
      .createNode(
        categoryRef,
        CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
        CATEGORY_ADMIN_PROFILE_NAME,
        CircabcModel.TYPE_CATEGORY_PROFILE
      )
      .getChildRef();
    nodeService.setProperty(
      categoryAdminProfileRef,
      ContentModel.PROP_TITLE,
      Converter.toMLTextEN("Category Administrator")
    );
    nodeService.setProperty(
      categoryAdminProfileRef,
      CATEGORY_PROFILE_PROP_NAME,
      CATEGORY_ADMIN_PROFILE_NAME.getLocalName()
    );

    String newAlfGroupName = "CircaCategoryAdmin--" + GUID.generate();
    String finalAlfGroupName = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(AuthorityType.GROUP, newAlfGroupName),
      AuthenticationUtil.getAdminUserName()
    );

    String prefixedInvitedGroupName = authorityService.getName(
      AuthorityType.GROUP,
      nodeService
        .getProperty(categoryRef, CircabcModel.PROP_CATEGORY_INVITED_USER_GROUP)
        .toString()
    );

    final String prefixedInvitedGroupNameFinal = prefixedInvitedGroupName;
    final String finalAlfGroupNameFinal = finalAlfGroupName;

    AuthenticationUtil.runAs(
      () -> {
        authorityService.addAuthority(
          prefixedInvitedGroupNameFinal,
          finalAlfGroupNameFinal
        );
        return null;
      },
      AuthenticationUtil.getAdminUserName()
    );

    nodeService.setProperty(
      categoryAdminProfileRef,
      CATEGORY_PROFILE_GROUP_NAME,
      finalAlfGroupName
    );

    permissionService.setPermission(
      categoryRef,
      finalAlfGroupName,
      CIRCA_CATEGORY_ADMIN,
      true
    );

    // circaCategoryAdmin - CATEGORY permissions
    NodeRef categoryAdminCategoryRef = nodeService
      .createNode(
        categoryAdminProfileRef,
        ASSOC_CATEGORY_SERVICE,
        QNAME_CATEGORY_ADMIN_SERVICE,
        TYPE_CATEGORY_SERVICE
      )
      .getChildRef();
    nodeService.setProperty(
      categoryAdminCategoryRef,
      PROP_CATEGORY_ADMIN_SERVICE_NAME,
      QNAME_CATEGORY_ADMIN_SERVICE.getLocalName()
    );
    ArrayList<String> permissions = new ArrayList<>();
    permissions.add(CIRCA_CATEGORY_ADMIN);
    nodeService.setProperty(
      categoryAdminCategoryRef,
      PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET,
      permissions
    );

    // ALL_CIRCA_USERS
    NodeRef registeredProfileRef = nodeService
      .createNode(
        categoryRef,
        CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
        ALL_CIRCA_USERS_PROFILE_NAME,
        CircabcModel.TYPE_CATEGORY_PROFILE
      )
      .getChildRef();
    nodeService.setProperty(
      registeredProfileRef,
      CATEGORY_PROFILE_PROP_NAME,
      EVERYONE
    );
    nodeService.setProperty(
      registeredProfileRef,
      ContentModel.PROP_TITLE,
      Converter.toMLTextEN("Registered")
    );
    nodeService.setProperty(
      registeredProfileRef,
      CATEGORY_PROFILE_GROUP_NAME,
      GROUP_EVERYONE
    );

    permissionService.setPermission(
      categoryRef,
      GROUP_EVERYONE,
      CIRCA_CATEGORY_ACCESS,
      true
    );

    // circaCategoryAdmin - CATEGORY permissions
    NodeRef registeredCategoryRef = nodeService
      .createNode(
        registeredProfileRef,
        ASSOC_CATEGORY_SERVICE,
        QNAME_CATEGORY_ADMIN_SERVICE,
        TYPE_CATEGORY_SERVICE
      )
      .getChildRef();
    nodeService.setProperty(
      registeredCategoryRef,
      PROP_CATEGORY_ADMIN_SERVICE_NAME,
      QNAME_CATEGORY_ADMIN_SERVICE.getLocalName()
    );
    ArrayList<String> regPermissions = new ArrayList<>();
    regPermissions.add(CIRCA_CATEGORY_ACCESS);
    nodeService.setProperty(
      registeredCategoryRef,
      PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET,
      regPermissions
    );

    // GUEST
    NodeRef guestProfileRef = nodeService
      .createNode(
        categoryRef,
        CircabcModel.ASSOC_CIRCA_CATEGORY_PROFILE,
        GUEST_PROFILE_NAME,
        CircabcModel.TYPE_CATEGORY_PROFILE
      )
      .getChildRef();
    nodeService.setProperty(guestProfileRef, CATEGORY_PROFILE_PROP_NAME, GUEST);
    nodeService.setProperty(
      guestProfileRef,
      ContentModel.PROP_TITLE,
      Converter.toMLTextEN("Guest")
    );
    nodeService.setProperty(
      guestProfileRef,
      CATEGORY_PROFILE_GROUP_NAME,
      GUEST
    );

    permissionService.setPermission(
      categoryRef,
      GUEST,
      CIRCA_CATEGORY_ACCESS,
      true
    );

    // circaCategoryAdmin - CATEGORY permissions
    NodeRef guestCategoryRef = nodeService
      .createNode(
        guestProfileRef,
        ASSOC_CATEGORY_SERVICE,
        QNAME_CATEGORY_ADMIN_SERVICE,
        TYPE_CATEGORY_SERVICE
      )
      .getChildRef();

    nodeService.setProperty(
      guestCategoryRef,
      PROP_CATEGORY_ADMIN_SERVICE_NAME,
      QNAME_CATEGORY_ADMIN_SERVICE.getLocalName()
    );
    ArrayList<String> guestPermissions = new ArrayList<>();
    guestPermissions.add(CIRCA_CATEGORY_ACCESS);
    nodeService.setProperty(
      guestCategoryRef,
      PROP_CATEGORY_ADMIN_SERVICE_PERMISSION_SET,
      guestPermissions
    );
  }

  /**
   * Sends a "contact the administrators" e-mail for the given category.
   *
   * <p>The message is sent to the category's contact e-mails (or its
   * administrators when none are configured). When the sender requested a copy,
   * an additional confirmation e-mail is sent back to them.</p>
   *
   * @param categoryId the node id of the category
   * @param body       the admin-contact request (subject, content, copy flag)
   */
  @Override
  public void categoriesIdAdminContactPost(
    String categoryId,
    AdminContactRequest body
  ) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);

    List<String> emails = getAdminEmails(categoryId);

    EmailDefinition email = emailApi.prepareEmailForAdminContact(
      categoryRef,
      body,
      emails
    );
    email.setCopyToSender(body.getSendCopy());
    emailApi.mailPost(email);

    if (Boolean.TRUE.equals(body.getSendCopy())) {
      EmailDefinition emailConfirmation =
        emailApi.prepareConfirmationForAdminContact(
          categoryRef,
          body.getContent()
        );
      emailApi.mailPost(emailConfirmation);
    }
  }

  /**
   * Returns the e-mail addresses to use for contacting the category
   * administrators: the configured contact e-mails when present, otherwise the
   * e-mail addresses of the category administrators.
   *
   * @param categoryId the node id of the category
   * @return the list of contact e-mail addresses; never {@code null}
   */
  private List<String> getAdminEmails(String categoryId) {
    NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
    List<String> emails = new ArrayList<>();

    Serializable listOfAdminMails = nodeService.getProperty(
      categoryRef,
      CircabcModel.PROP_CONTACT_EMAILS
    );
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

  /**
   * Returns a filtered, paged list of group-creation requests for the given
   * category together with the total matching count.
   *
   * @param categoryRef the node id of the category
   * @param limit       the maximum number of requests per page
   * @param page        the page index
   * @param filter      an optional filter applied to the requests
   * @return the paged group-creation requests
   */
  @Override
  public PagedGroupCreationRequests categoriesIdGroupRequestsGet(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  ) {
    PagedGroupCreationRequests result = new PagedGroupCreationRequests();
    result.setData(
      groupRequestsDaoService.getCategoryGroupCreationRequests(
        categoryRef,
        limit,
        page,
        filter
      )
    );
    result.setTotal(
      groupRequestsDaoService
        .getCountCategoryGroupCreationRequests(categoryRef, filter)
        .longValue()
    );

    return result;
  }

  /**
   * Records the approval decision for a group-creation request and notifies the
   * requester by e-mail.
   *
   * <p>An agreement of {@code -1} rejects the request (a refusal e-mail is sent),
   * an agreement of {@code 1} accepts it (an acceptance e-mail is sent); any other
   * value is ignored.</p>
   *
   * @param categoryId the node id of the category
   * @param body       the approval decision (request id, agreement, argument)
   * @param username   the id of the user taking the decision
   */
  @Override
  public void categoriesIdGroupRequestApprovalPost(
    String categoryId,
    GroupCreationRequestApproval body,
    String username
  ) {
    requireGroupCreationRequestInCategory(
      categoryId,
      String.valueOf(body.getId())
    );

    if (body.getAgreement() == -1) {
      groupRequestsDaoService.updateGroupCreationRequestApproval(
        username,
        body.getId(),
        body.getAgreement(),
        body.getArgument()
      );
      GroupCreationRequest intialRequest = groupRequestGet(
        String.valueOf(body.getId())
      );
      EmailDefinition emailRefusal = emailApi.prepareRefusalGroupRequest(
        intialRequest,
        body.getArgument()
      );
      emailApi.mailPost(emailRefusal);
    } else if (body.getAgreement() == 1) {
      groupRequestsDaoService.updateGroupCreationRequestApproval(
        username,
        body.getId(),
        body.getAgreement(),
        body.getArgument()
      );
      GroupCreationRequest intialRequest = groupRequestGet(
        String.valueOf(body.getId())
      );
      EmailDefinition emailRefusal = emailApi.prepareAcceptationGroupRequest(
        intialRequest,
        body.getArgument()
      );
      emailApi.mailPost(emailRefusal);
    }
  }

  /**
   * Loads a single group-creation request by its id.
   *
   * @param requestId the id of the group-creation request
   * @return the matching group-creation request
   */
  private GroupCreationRequest groupRequestGet(String requestId) {
    return groupRequestsDaoService.getCategoryGroupCreationRequests(requestId);
  }

  /**
   * Updates an existing group-creation request.
   *
   * @param requestId the id of the request to update
   * @param body      the new request values
   */
  @Override
  public void categoriesGroupRequestPut(
    String categoryId,
    String requestId,
    GroupCreationRequest body
  ) {
    requireGroupCreationRequestInCategory(categoryId, requestId);
    groupRequestsDaoService.putCategoryGroupCreationRequest(requestId, body);
  }

  /**
   * Verifies that the group-creation request identified by {@code requestId} belongs to the
   * authorized {@code categoryId}, rejecting the request with {@link AccessDeniedException}
   * otherwise.
   *
   * @param categoryId the authorized category id
   * @param requestId  the group-creation request id to validate
   */
  private void requireGroupCreationRequestInCategory(
    String categoryId,
    String requestId
  ) {
    GroupCreationRequest request = groupRequestGet(requestId);

    if (
      request == null || !Objects.equals(categoryId, request.getCategoryRef())
    ) {
      throw new AccessDeniedException(
        "Group creation request does not belong to the authorized category"
      );
    }
  }

  /**
   * Loads the group-deletion request identified by {@code requestId} and verifies it belongs to the
   * authorized {@code categoryId}, rejecting the request with {@link AccessDeniedException}
   * otherwise.
   *
   * @param categoryId the authorized category id
   * @param requestId  the group-deletion request id to validate
   * @return the validated group-deletion request
   */
  private GroupDeletionRequest requireGroupDeletionRequestInCategory(
    String categoryId,
    String requestId
  ) {
    GroupDeletionRequest request =
      groupRequestsDaoService.getCategoryGroupDeletionRequests(requestId);

    if (
      request == null || !Objects.equals(categoryId, request.getCategoryRef())
    ) {
      throw new AccessDeniedException(
        "Group deletion request does not belong to the authorized category"
      );
    }

    return request;
  }

  /**
   * Returns the interest-group nodes directly contained in the given category.
   * Only nodes carrying the IG-root aspect are returned, so other kinds of
   * spaces are excluded.
   *
   * @param category the category node
   * @return the list of interest-group nodes; never {@code null}, possibly empty
   */
  @Override
  public List<NodeRef> getInterestGroups(NodeRef category) {
    final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(
      category
    );

    List<NodeRef> interestGroupsNodes = new ArrayList<>(assocs.size());

    NodeRef ref = null;

    for (ChildAssociationRef assoc : assocs) {
      ref = assoc.getChildRef();

      // Secure the list of ig. No other kind of spaces can be returned
      if (nodeService.hasAspect(ref, CircabcModel.ASPECT_IGROOT)) {
        interestGroupsNodes.add(ref);
      }
    }

    return interestGroupsNodes;
  }

  /**
   * Records a group-deletion request and notifies the relevant recipients.
   *
   * <p>The request is enriched with the interest group's title, name,
   * description and leaders, then persisted. Notification e-mails are sent to the
   * category administrators and to the interest-group leaders (each leader is
   * removed from the admin list to avoid duplicate messages). E-mail failures are
   * logged and do not abort the request.</p>
   *
   * @param body the group-deletion request payload (must reference a category,
   *             group and originating user)
   */
  @Override
  public void groupIdDeleteRequestPost(GroupDeletionRequest body) {
    List<User> catAdmins = categoriesIdAdminsGet(body.getCategoryRef());
    List<User> igLeaders = getLeaders(body.getGroupId());
    InterestGroup interestGroup = this.groupsApi.getInterestGroup(
      body.getGroupId()
    );
    User userFrom = usersApi.usersUserIdGet(body.getFrom().getUserId());

    // Populate IG details in the deletion request
    if (
      interestGroup.getTitle() != null && !interestGroup.getTitle().isEmpty()
    ) {
      String titleValue = interestGroup.getTitle().getDefaultValue();
      if (titleValue != null && !titleValue.isEmpty()) {
        body.setTitle(titleValue);
      }
    }
    body.setName(interestGroup.getName());
    if (
      interestGroup.getDescription() != null &&
      !interestGroup.getDescription().isEmpty()
    ) {
      String descValue = interestGroup.getDescription().getDefaultValue();
      if (descValue != null && !descValue.isEmpty()) {
        body.setDescription(descValue);
      }
    }
    body.setLeaders(igLeaders);

    groupRequestsDaoService.saveRequestDeletion(body);

    try {
      //email for Cat Admins
      catAdmins.forEach(user -> {
        EmailDefinition email = emailApi.prepareEmailForGroupDeletionRequest(
          user,
          body,
          MailTemplate.GROUP_DELETE_REQUEST,
          interestGroup,
          userFrom
        );
        emailApi.mailPost(email, false);
      });
      //email for Leaders
      igLeaders.forEach(user -> {
        // Remove the user from the list of CatAdmins if they are also a leader
        List<User> filteredCatAdmins = new ArrayList<>(catAdmins);
        filteredCatAdmins.removeIf(admin ->
          admin.getEmail().equals(user.getEmail())
        );

        EmailDefinition email =
          emailApi.prepareEmailForGroupDeletionRequestLeaders(
            user,
            filteredCatAdmins,
            body,
            MailTemplate.GROUP_DELETE_REQUEST_LEADERS,
            interestGroup,
            userFrom
          );
        emailApi.mailPost(email, false);
      });
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("problem sending email", e);
      }
    }
  }

  /**
   * Returns the leaders of the given interest group, i.e. the users belonging to
   * profiles that hold the administrator permission for every service (Library,
   * Events, Members, Newsgroups and Information).
   *
   * @param groupId the id of the interest group
   * @return the list of leader users; never {@code null}, possibly empty
   */
  private List<User> getLeaders(String groupId) {
    List<Profile> profiles = profilesApi.groupsIdProfilesGet(groupId, "", true);
    List<User> leaders = new ArrayList<>();

    for (Profile groupProfile : profiles) {
      Map<String, String> perms = groupProfile.getPermissions();

      if (
        perms != null &&
        LibraryPermissions.LIBADMIN.toString().equals(perms.get(LIBRARY)) &&
        EventPermissions.EVEADMIN.toString().equals(perms.get(EVENTS)) &&
        DirectoryPermissions.DIRADMIN.toString().equals(perms.get(MEMBERS)) &&
        NewsGroupPermissions.NWSADMIN.toString().equals(
          perms.get(NEWSGROUPS)
        ) &&
        InformationPermissions.INFADMIN.toString().equals(
          perms.get(INFORMATION)
        )
      ) {
        List<String> profileGroupName = Collections.singletonList(
          groupProfile.getGroupName()
        );

        List<UserProfile> groupAdmins = groupsApi.groupsIdMembersGet(
          groupId,
          profileGroupName,
          null,
          null
        );
        for (UserProfile leader : groupAdmins) {
          leaders.add(leader.getUser());
        }
      }
    }

    return leaders;
  }

  /**
   * Runnable that computes and persists the interest-group statistics for a
   * category in its own retrying transaction, running as a given user.
   *
   * <p>Execution is guarded by the CircaBC lock service keyed on the category id:
   * if a statistics job is already locked for the category the run is a no-op;
   * otherwise the category is locked for the duration of the computation and
   * unlocked afterwards.</p>
   */
  private class CategoryIgStatisticsRunnable implements Runnable {

    /** The user the statistics computation runs as. */
    protected String userName;
    /** The category whose statistics are computed. */
    protected NodeRef categoryRef;
    /** Service used to run the computation inside a retrying transaction. */
    private TransactionService transactionService;

    /**
     * Creates the runnable.
     *
     * @param userName           the user to run the computation as
     * @param categoryRef        the category whose statistics are computed
     * @param transactionService the service providing the retrying transaction helper
     */
    public CategoryIgStatisticsRunnable(
      String userName,
      NodeRef categoryRef,
      TransactionService transactionService
    ) {
      this.userName = userName;
      this.categoryRef = categoryRef;
      this.transactionService = transactionService;
    }

    /**
     * Computes and stores the category group statistics inside a retrying
     * transaction, honouring the per-category lock.
     */
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
                    if (
                      circabcLockService.isLocked(
                        CATEG_STAT_JOB + categoryRef.getId()
                      )
                    ) {
                      return null;
                    }

                    circabcLockService.lock(
                      CATEG_STAT_JOB + categoryRef.getId()
                    );

                    String categoryName = (String) nodeService.getProperty(
                      categoryRef,
                      ContentModel.PROP_NAME
                    );

                    try {
                      globalStatisticsService.saveCategoryGroupStatistics(
                        globalStatisticsService.computeCategoryGroupStatistics(
                          categoryRef
                        ),
                        categoryName
                      );
                    } finally {
                      circabcLockService.unlock(
                        CATEG_STAT_JOB + categoryRef.getId()
                      );
                    }

                    return null;
                  }
                },
                userName
              );

              return null;
            }
          },
          false,
          true
        );
    }
  }

  /**
   * Indicates whether a pending group-deletion request already exists for the
   * given interest group.
   *
   * @param groupId the id of the interest group
   * @return {@code true} if a deletion request exists, {@code false} otherwise
   */
  @Override
  public boolean existsGroupDeleteRequest(String groupId) {
    return groupRequestsDaoService.existsGroupDeleteRequest(groupId);
  }

  /**
   * Returns a filtered, paged list of group-deletion requests for the given
   * category together with the total matching count.
   *
   * <p>Stale requests are pruned while iterating (see
   * {@link #shouldDeleteRequest(GroupDeletionRequest)}): they are removed from the
   * store and excluded from the result.</p>
   *
   * @param categoryRef the node id of the category
   * @param limit       the maximum number of requests per page
   * @param page        the page index
   * @param filter      an optional filter applied to the requests
   * @return the paged group-deletion requests
   */
  @Override
  public PagedGroupDeletionRequests categoriesIdGroupDeleteRequestsGet(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  ) {
    PagedGroupDeletionRequests result = new PagedGroupDeletionRequests();

    List<GroupDeletionRequest> listGroupDeletionRequest =
      groupRequestsDaoService.getCategoryGroupDeletionRequests(
        categoryRef,
        limit,
        page,
        filter
      );

    Iterator<GroupDeletionRequest> iterator =
      listGroupDeletionRequest.iterator();
    while (iterator.hasNext()) {
      GroupDeletionRequest groupDeletionRequest = iterator.next();

      if (shouldDeleteRequest(groupDeletionRequest)) {
        groupRequestsDaoService.deleteRequestDeletion(
          groupDeletionRequest.getGroupId()
        );
        iterator.remove();
      }
    }

    result.setData(listGroupDeletionRequest);
    Long total = groupRequestsDaoService.getCountCategoryGroupDeletionRequests(
      categoryRef,
      filter
    );
    result.setTotal(total != null ? total.longValue() : 0L);

    return result;
  }

  /**
   * Records the approval decision for a group-deletion request and notifies the
   * relevant recipients.
   *
   * <p>An agreement of {@code -1} with an argument rejects the request: a refusal
   * e-mail is sent to the requester and to the leaders (excluding the requester).
   * An agreement of {@code 1} accepts it: the interest group is flagged to be
   * deleted and an acceptance e-mail is sent to the combined set of category
   * administrators and leaders. The request itself is then updated.</p>
   *
   * @param categoryId the node id of the category
   * @param body       the deletion-approval decision (request id, agreement, argument)
   * @param username   the id of the user taking the decision
   */
  @Override
  public void categoriesIdGroupRequestDeleteApprovalPost(
    String categoryId,
    GroupDeletionRequestApproval body,
    String username
  ) {
    GroupDeletionRequest groupDeletionRequest =
      requireGroupDeletionRequestInCategory(
        categoryId,
        String.valueOf(body.getId())
      );
    List<User> admins = categoriesIdAdminsGet(categoryId);
    InterestGroup ig = groupsApi.getInterestGroup(
      groupDeletionRequest.getGroupId()
    );

    List<User> leaders = getLeaders(ig.getId());
    groupDeletionRequest.setLeaders(leaders);
    NodeRef igNodeRef = Converter.createNodeRefFromId(ig.getId());

    long interestGroupID = (Long) nodeService.getProperty(
      igNodeRef,
      ContentModel.PROP_NODE_DBID
    );

    groupDeletionRequest.setAgreement(body.getAgreement());

    Set<User> uniqueUsers = new HashSet<>(admins);
    uniqueUsers.addAll(groupDeletionRequest.getLeaders());
    List<User> toUsers = new ArrayList<>(uniqueUsers);

    if (body.getArgument() != null && body.getAgreement() == -1) {
      //send email to requester
      groupDeletionRequest.setRejectedMessage(body.getArgument());
      EmailDefinition emailDef = emailApi.prepareRefusalGroupDeleteRequest(
        groupDeletionRequest,
        ig.getName()
      );
      emailApi.mailPost(emailDef, false);

      //send email to leaders
      leaders
        .stream()
        .filter(leader -> !leader.equals(groupDeletionRequest.getFrom())) // Exclude the requester
        .forEach(leader -> {
          EmailDefinition leadersDeletionNotificationEmail =
            emailApi.prepareRefusalGroupDeleteRequestLeaders(
              groupDeletionRequest,
              ig.getName(),
              leader
            );
          emailApi.mailPost(leadersDeletionNotificationEmail, false);
        });
    }

    if (body.getAgreement() == 1) {
      groupsApi.updateIgToBeDeleted(interestGroupID, true);
      toUsers.forEach(user -> {
        EmailDefinition emailDef =
          emailApi.prepareAcceptationGroupDeleteRequest(
            user,
            groupDeletionRequest,
            ig.getName()
          );
        emailApi.mailPost(emailDef, false);
      });
    }
    groupRequestsDaoService.updateRequestDeletion(
      groupDeletionRequest,
      username
    );
  }

  /**
   * Determines whether a group-deletion request has become stale and should be
   * purged.
   *
   * <p>A pending request (agreement {@code null} or {@code 0}) is stale when its
   * target group no longer exists; an accepted request (agreement {@code 1}) is
   * stale when its target group still exists.</p>
   *
   * @param groupDeletionRequest the request to evaluate
   * @return {@code true} if the request should be deleted, {@code false} otherwise
   */
  private boolean shouldDeleteRequest(
    GroupDeletionRequest groupDeletionRequest
  ) {
    if (
      groupDeletionRequest.getAgreement() == null ||
      groupDeletionRequest.getAgreement() == 0
    ) {
      NodeRef groupRef = Converter.createNodeRefFromId(
        groupDeletionRequest.getGroupId()
      );
      if (!nodeService.exists(groupRef)) {
        return true;
      }
    }
    if (
      groupDeletionRequest.getAgreement() != null &&
      groupDeletionRequest.getAgreement() == 1
    ) {
      NodeRef groupRef = Converter.createNodeRefFromId(
        groupDeletionRequest.getGroupId()
      );
      if (nodeService.exists(groupRef)) {
        return true;
      }
    }
    return false;
  }
}
