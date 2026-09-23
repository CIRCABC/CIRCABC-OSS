package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.action.SystemAddFeaturesActionExecuter;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.model.User;
import io.swagger.model.alfresco.CircabcModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link CircabcApi} providing CIRCABC-wide (root
 * level) operations against the Alfresco repository.
 *
 * <p>This service is responsible for lazily bootstrapping the CIRCABC root
 * folder ("CircaBC") under Company Home and its four associated Alfresco
 * authority groups (master, subscribers, invited users and administrators). On
 * first access it either locates the existing root node and loads the group
 * names from its properties, or creates the node, applies the required
 * permissions and inbound rules, and provisions the groups.
 *
 * <p>Beyond administrator management it also resolves and caches the key
 * structural {@link NodeRef}s used throughout the application: the CIRCABC root
 * folder, the CIRCABC data dictionary folder, the machine-translation ("MT")
 * folder, Company Home, Guest Home and the root category header
 * ("CircaBCHeader").
 *
 * <p>Collaborating Alfresco services are injected by Spring via
 * {@link Autowired}.
 */
public class CircabcApiImpl implements CircabcApi {

  /** Prefix Alfresco uses to qualify authority (group) names. */
  private static final String GROUP = "GROUP_";

  /** Name of the CIRCABC root folder created under Company Home. */
  private static final String CIRCA_BC = "CircaBC";

  /** Logger for this service. */
  static final Log logger = LogFactory.getLog(CircabcApiImpl.class);

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private NodeLocatorService nodeLocatorService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private RuleService ruleService;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private ActionService actionService;

  @Autowired
  private PersonService personService;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private UserService userService;

  @Autowired
  private CategoryService categoryService;

  /** Cached reference to the CIRCABC root folder; resolved lazily. */
  private NodeRef circabcNodeRef = null;
  /** Cached reference to the machine-translation ("MT") folder; resolved lazily. */
  private NodeRef machineTranslationNodeRef = null;

  /** Name of the CIRCABC master authority group (parent of all others). */
  private String circabcMasterGroup = null;
  /** Name of the CIRCABC subscribers authority group. */
  private String circabcSubsGroup = null;
  /** Name of the CIRCABC invited-users authority group. */
  private String circabcInvitedUsersGroup = null;
  /** Name of the CIRCABC administrators authority group. */
  private String circabcAdminGroup = null;
  /** Cached reference to the CIRCABC data dictionary folder; resolved lazily. */
  private NodeRef circabcDictionary = null;
  /** Cached reference to the root category header ("CircaBCHeader"); resolved lazily. */
  private NodeRef circabcCategoryRoot = null;

  /**
   * {@inheritDoc}
   *
   * <p>Ensures the CIRCABC root and its groups exist, then for each supplied
   * user provisions the account from LDAP when unknown to the repository, adds
   * the user to the CIRCABC administrators group and registers them as a
   * CIRCABC administrator.
   *
   * @param userIds the identifiers (user names) of the users to promote to
   *     CIRCABC administrators
   */
  @Override
  public void circabcAdminsPost(List<String> userIds) {
    initCircabc();
    if (this.circabcInvitedUsersGroup != null) {
      for (String userAuthority : userIds) {
        if (personService.getPersonOrNull(userAuthority) == null) {
          userService.createUser(
            userService.getLDAPUserDataNoFilterByUid(userAuthority),
            true
          );
        }

        authorityService.addAuthority(this.circabcAdminGroup, userAuthority);
        if (!circabcService.isUserExists(userAuthority)) {
          circabcService.addUser(userAuthority);
        }
        circabcService.addCircabcAdmin(userAuthority);
      }
    }
  }

  /**
   * Lazily initializes the CIRCABC root node and its authority group names.
   *
   * <p>If the cached root node reference is missing or no longer exists in the
   * repository, the root is (re)created via {@link #checkAndCreateCircaBC()};
   * otherwise the group names are (re)loaded from the root node properties via
   * {@link #setCircabcGroups()}.
   */
  private void initCircabc() {
    if (circabcNodeRef == null || !nodeService.exists(circabcNodeRef)) {
      checkAndCreateCircaBC();
    } else {
      setCircabcGroups();
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Ensures the CIRCABC root and its groups exist, removes the user from the
   * CIRCABC administrators group and unregisters them as a CIRCABC
   * administrator when applicable.
   *
   * @param userId the identifier (user name) of the user whose administrator
   *     rights are to be revoked
   */
  @Override
  public void circabcAdminsUserIdDelete(String userId) {
    initCircabc();
    if (this.circabcInvitedUsersGroup != null) {
      authorityService.removeAuthority(this.circabcAdminGroup, userId);
      if (circabcService.isCircabcAdmin(userId)) {
        circabcService.removeCircabcAdmin(userId);
      }
    }
  }

  /**
   * Locates the CIRCABC root folder under Company Home, creating it if absent.
   *
   * <p>When the folder is created it is named "CircaBC", tagged with the
   * CIRCABC root aspect, has its permissions configured, is given the inbound
   * aspect rule and has its authority groups provisioned. When it already
   * exists, its cached reference is stored and the group names are loaded from
   * its properties.
   */
  private void checkAndCreateCircaBC() {
    NodeRef companyHome = nodeLocatorService.getNode("companyhome", null, null);

    // Look for the child node
    QName circabcQname = QName.createQName(
      "{http://www.alfresco.org/model/content/1.0}CircaBC"
    );

    NodeRef circabc = nodeService.getChildByName(
      companyHome,
      ContentModel.ASSOC_CONTAINS,
      CIRCA_BC
    );

    // Check if node exists
    if (circabc == null) {
      // Node does not exist, create it
      circabc = nodeService
        .createNode(
          companyHome,
          ContentModel.ASSOC_CONTAINS,
          circabcQname,
          ContentModel.TYPE_FOLDER
        )
        .getChildRef();
      this.circabcNodeRef = circabc;
      nodeService.setProperty(
        circabc,
        ContentModel.PROP_NAME,
        circabcQname.getLocalName()
      );
      nodeService.addAspect(
        circabc,
        CircabcModel.ASPECT_CIRCABC_ROOT,
        new HashMap<>()
      );
      setPermissions(circabc);
      createAddAspectRule(circabc);

      createCircabcGroups();
    } else {
      this.circabcNodeRef = circabc;
      // set the groups from the properties
      setCircabcGroups();
    }
  }

  /**
   * Loads the four CIRCABC authority group names from the root node properties
   * into the corresponding fields, prefixing each with {@link #GROUP}.
   */
  private void setCircabcGroups() {
    // get the groups from the properties
    this.circabcMasterGroup =
      GROUP +
      (String) nodeService.getProperty(
        circabcNodeRef,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      );
    this.circabcSubsGroup =
      GROUP +
      (String) nodeService.getProperty(
        circabcNodeRef,
        CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY
      );
    this.circabcInvitedUsersGroup =
      GROUP +
      (String) nodeService.getProperty(
        circabcNodeRef,
        CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY
      );

    this.circabcAdminGroup =
      GROUP +
      (String) nodeService.getProperty(
        circabcNodeRef,
        CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY
      );
  }

  /**
   * Creates the four CIRCABC authority groups (master, subscribers, invited
   * users and administrators) with unique names, wires their containment
   * hierarchy (master contains subscribers and invited users; invited users
   * contains administrators) and stores the unprefixed group names as
   * properties on the CIRCABC root node.
   *
   * <p>Group creation is performed as the admin user.
   */
  private void createCircabcGroups() {
    String masterGroupName =
      CircabcModel.CIRCA_BC_MASTER_GROUP + UUID.randomUUID();

    // Create the root group
    this.circabcMasterGroup = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(AuthorityType.GROUP, masterGroupName),
      AuthenticationUtil.getAdminUserName()
    );

    String subsGroupName = CircabcModel.CIRCA_BC_SUBS_GROUP + UUID.randomUUID();

    this.circabcSubsGroup = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(AuthorityType.GROUP, subsGroupName),
      AuthenticationUtil.getAdminUserName()
    );

    String invitedUsersGroupName =
      CircabcModel.CIRCA_BC_INVITED_USERS_GROUP + UUID.randomUUID();

    this.circabcInvitedUsersGroup = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(
          AuthorityType.GROUP,
          invitedUsersGroupName
        ),
      AuthenticationUtil.getAdminUserName()
    );

    String circabcAdminGroupName =
      CircabcModel.CIRCA_BC_ADMIN_GROUP + UUID.randomUUID();

    this.circabcAdminGroup = AuthenticationUtil.runAs(
      () ->
        authorityService.createAuthority(
          AuthorityType.GROUP,
          circabcAdminGroupName
        ),
      AuthenticationUtil.getAdminUserName()
    );

    authorityService.addAuthority(
      this.circabcMasterGroup,
      this.circabcSubsGroup
    );
    authorityService.addAuthority(
      this.circabcMasterGroup,
      this.circabcInvitedUsersGroup
    );

    authorityService.addAuthority(
      this.circabcInvitedUsersGroup,
      this.circabcAdminGroup
    );

    // Set the properties
    nodeService.setProperty(
      circabcNodeRef,
      CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY,
      this.circabcMasterGroup.replace(GROUP, "")
    );
    nodeService.setProperty(
      circabcNodeRef,
      CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY,
      this.circabcSubsGroup.replace(GROUP, "")
    );
    nodeService.setProperty(
      circabcNodeRef,
      CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY,
      this.circabcInvitedUsersGroup.replace(GROUP, "")
    );

    nodeService.setProperty(
      circabcNodeRef,
      CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY,
      this.circabcAdminGroup.replace(GROUP, "")
    );
  }

  /**
   * Configures the access permissions on the CIRCABC root node.
   *
   * <p>Disables permission inheritance from the parent and grants CIRCABC
   * access and consumer rights to everyone, admin rights to administrators,
   * access to guests and all permissions to the node owner.
   *
   * @param nodeRef the CIRCABC root node to secure
   */
  private void setPermissions(NodeRef nodeRef) {
    // Disable inheritance
    permissionService.setInheritParentPermissions(nodeRef, false);

    // Set permissions
    permissionService.setPermission(
      nodeRef,
      "GROUP_EVERYONE",
      "CircaBCAccess",
      true
    );
    permissionService.setPermission(
      nodeRef,
      "GROUP_EVERYONE",
      PermissionService.CONSUMER,
      true
    );
    permissionService.setPermission(
      nodeRef,
      "ROLE_ADMINISTRATOR",
      "CircaBCAdmin",
      true
    );
    permissionService.setPermission(nodeRef, "guest", "CircaBCAccess", true);
    permissionService.setPermission(
      nodeRef,
      PermissionService.OWNER_AUTHORITY,
      PermissionService.ALL_PERMISSIONS,
      true
    );
  }

  /**
   * Creates and saves an inbound rule on the given node that asynchronously
   * applies the CIRCABC management aspect to the node and, recursively, to its
   * children.
   *
   * <p>The rule is executed asynchronously to avoid access-denied errors when
   * categories are created.
   *
   * @param node the node on which the aspect-adding rule is installed
   */
  private void createAddAspectRule(NodeRef node) {
    // Add CircaManagement
    final CompositeAction compositeActionManagement =
      actionService.createCompositeAction();

    // Create Action
    final Action actionManagement = actionService.createAction(
      SystemAddFeaturesActionExecuter.NAME
    );
    actionManagement.setParameterValue(
      AddFeaturesActionExecuter.PARAM_ASPECT_NAME,
      CircabcModel.ASPECT_CIRCABC_MANAGEMENT
    );
    compositeActionManagement.addAction(actionManagement);
    compositeActionManagement.setTitle("Add CircaManagement Aspect");
    compositeActionManagement.setDescription(
      "Add CircaManagement Aspect Description"
    );

    // Create Condition
    final ActionCondition actionConditionLibrary =
      actionService.createActionCondition(NoConditionEvaluator.NAME);
    compositeActionManagement.addActionCondition(actionConditionLibrary);

    // Create a rule
    final Rule rule = new Rule();
    rule.setRuleType(RuleType.INBOUND);

    rule.applyToChildren(true);
    // set to be asynchronous otherwise it when create category there is  access denied error
    rule.setExecuteAsynchronously(true);
    rule.setAction(compositeActionManagement);
    rule.setTitle(compositeActionManagement.getTitle());
    rule.setDescription(compositeActionManagement.getDescription());
    ruleService.saveRule(node, rule);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Resolves the CIRCABC administrators group and maps each contained user
   * authority to a {@link User} model built from the person node properties.
   *
   * @return the users belonging to the CIRCABC administrators group; an empty
   *     list if there are none
   */
  @Override
  public List<User> getCircabcAdmins() {
    initCircabc();
    List<User> admins = new ArrayList<>();
    if (this.circabcAdminGroup != null) {
      Set<String> authorities = authorityService.getContainedAuthorities(
        AuthorityType.USER,
        this.circabcAdminGroup,
        true
      );
      for (String authority : authorities) {
        NodeRef personNodeRef = personService.getPerson(authority);

        admins.add(getUserFromNodeRef(personNodeRef));
      }
    }
    return admins;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the cached CIRCABC root reference, resolving it by name under
   * Company Home on first access.
   *
   * @return the node reference of the CIRCABC root folder
   */
  @Override
  public NodeRef getCircabcNodeRef() {
    if (this.circabcNodeRef == null) {
      getChildrenByName(CIRCA_BC);
    }
    return this.circabcNodeRef;
  }

  /**
   * Locates the direct child of Company Home with the given name and caches it
   * as the CIRCABC root node reference.
   *
   * @param name the name of the child node to search for under Company Home
   */
  private void getChildrenByName(String name) {
    NodeRef companyHome = nodeLocatorService.getNode("companyhome", null, null);
    List<ChildAssociationRef> children = nodeService.getChildAssocs(
      companyHome
    );

    for (ChildAssociationRef childAssoc : children) {
      NodeRef childNodeRef = childAssoc.getChildRef();
      String childName = (String) nodeService.getProperty(
        childNodeRef,
        ContentModel.PROP_NAME
      );
      if (name.equals(childName)) {
        this.circabcNodeRef = childNodeRef;
        break;
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the cached CIRCABC data dictionary folder, locating it under the
   * Alfresco data dictionary and creating it if it does not yet exist.
   *
   * @return the node reference of the CIRCABC data dictionary folder
   */
  @Override
  public NodeRef getCircabcDictionaryNodeRef() {
    if (circabcDictionary == null) {
      final NodeRef dictionaryNodeRef = getAlfrescoDictionaryNodeRef();
      circabcDictionary = nodeService.getChildByName(
        dictionaryNodeRef,
        ContentModel.ASSOC_CONTAINS,
        CIRCA_BC
      );

      if (circabcDictionary == null) {
        circabcDictionary = fileFolderService
          .create(dictionaryNodeRef, CIRCA_BC, ContentModel.TYPE_FOLDER)
          .getNodeRef();

        logger.info(
          "Circabc data dictionanry successfully created: " + circabcDictionary
        );
      }
    }

    return circabcDictionary;
  }

  /**
   * Builds a {@link User} model from the properties of the given person node.
   *
   * @param personNodeRef the person node whose properties are read
   * @return a {@link User} populated with the user name, first name, last name
   *     and email of the person
   */
  private User getUserFromNodeRef(NodeRef personNodeRef) {
    String userName = (String) nodeService.getProperty(
      personNodeRef,
      ContentModel.PROP_USERNAME
    );
    String firstName = (String) nodeService.getProperty(
      personNodeRef,
      ContentModel.PROP_FIRSTNAME
    );
    String lastName = (String) nodeService.getProperty(
      personNodeRef,
      ContentModel.PROP_LASTNAME
    );
    String email = (String) nodeService.getProperty(
      personNodeRef,
      ContentModel.PROP_EMAIL
    );
    return new User(userName, firstName, lastName, email);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Resolves Company Home as the first child of the workspace store root
   * node.
   *
   * @return the node reference of Company Home
   */
  @Override
  public NodeRef getCompanyHomeNodeRef() {
    NodeRef rootNode = nodeService.getRootNode(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
    );

    List<ChildAssociationRef> car = nodeService.getChildAssocs(rootNode);
    return car.get(0).getChildRef();
  }

  /**
   * Resolves the Alfresco "Data Dictionary" folder located directly under
   * Company Home.
   *
   * @return the node reference of the Alfresco data dictionary folder
   */
  private NodeRef getAlfrescoDictionaryNodeRef() {
    // Data Dictionary

    NodeRef companyHome = getCompanyHomeNodeRef();
    return nodeService.getChildByName(
      companyHome,
      ContentModel.ASSOC_CONTAINS,
      "Data Dictionary"
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>Resolves the "Guest Home" folder located directly under Company Home.
   *
   * @return the node reference of Guest Home
   */
  @Override
  public NodeRef getGuestHomeNodeRef() {
    NodeRef companyHome = getCompanyHomeNodeRef();
    return nodeService.getChildByName(
      companyHome,
      ContentModel.ASSOC_CONTAINS,
      "Guest Home"
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the cached root category header, otherwise scans the immediate
   * classifiable categories in the workspace store for the one named
   * "CircaBCHeader" and caches it.
   *
   * @return the node reference of the root category header, or {@code null} if
   *     it cannot be found
   */
  @Override
  public NodeRef getRootCategoryHeader() {
    if (circabcCategoryRoot != null) {
      return circabcCategoryRoot;
    }
    Collection<ChildAssociationRef> rootCategories =
      categoryService.getCategories(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        ContentModel.ASPECT_GEN_CLASSIFIABLE,
        Depth.IMMEDIATE
      );

    for (ChildAssociationRef child : rootCategories) {
      if (
        nodeService
          .getProperty(child.getChildRef(), ContentModel.PROP_NAME)
          .equals("CircaBCHeader")
      ) {
        circabcCategoryRoot = child.getChildRef();
        break;
      }
    }
    return circabcCategoryRoot;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Ensures the CIRCABC root and its groups are initialized, then returns
   * the invited-users group name.
   *
   * @return the invited-users group name
   */
  @Override
  public String getInvitedUsersGroupName() {
    initCircabc();
    return this.circabcInvitedUsersGroup;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the cached machine-translation folder reference, resolving it by
   * the name "MT" on first access.
   *
   * @return the node reference of the machine-translation folder
   */
  @Override
  public NodeRef getMTNodeRef() {
    if (this.machineTranslationNodeRef == null) {
      getChildrenByName("MT");
    }
    return this.machineTranslationNodeRef;
  }
}
