package eu.europa.ec.digit.circabc.rest.service.user;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserCategoryMembershipRecordComparator;
import io.swagger.model.UserIGMembershipRecord;
import io.swagger.model.UserIGMembershipRecordComparator;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.UserModel;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Default implementation of {@link UserService} that manages CIRCABC users on top of the Alfresco
 * repository.
 *
 * <p>This service centralises user-related operations such as:
 *
 * <ul>
 *   <li>creating repository {@code Person} nodes and their authentication (optionally seeded from
 *       LDAP/EU Login data);
 *   <li>reading and updating user profile properties, mapping them to and from
 *       {@link CircabcUserDataBean};
 *   <li>resolving a user's Category and Interest Group memberships;
 *   <li>searching for users by email, domain, first/last name via LDAP and/or Lucene;
 *   <li>managing user preferences, passwords and authentication enablement.
 * </ul>
 *
 * <p>User lookups can be resolved against LDAP or the Alfresco (Lucene) repository. When LDAP is
 * enabled ({@link CircabcConfig#isUseLDAP()}) global searches additionally query the local
 * repository so that non EU Login/LDAP users are also returned.
 *
 * <p>Collaborating Alfresco and CIRCABC services are injected via Spring {@code @Autowired}.
 */
public class UserServiceImpl implements UserService {

  /** Log message used when an Alfresco {@link ResultSet} fails to close cleanly. */
  private static final String RESULT_SET_CLOSE_EXCEPTION =
    "ResultSet Close Exception";

  private static final Log logger = LogFactory.getLog(UserServiceImpl.class);

  /** Character set used when generating a random secure password. */
  private static final String VALID_CHARS =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_-+=<>?";

  /** Cryptographically strong random generator backing {@link #generateSecurePassword(int)}. */
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  @SuppressWarnings("unused")
  private static final int DEFAULT_EXIPRATION_NUMBER_OF_DAYS = 60;

  /** Lucene query fragment matching the master group that marks an Interest Group root node. */
  private static final String CI_CIRCA_IGROOT_MASTER_GROUP =
    "@ci\\:circaIGRootMasterGroup:";

  @SuppressWarnings("unused")
  private static final String CI_CIRCA_CATEGORY_MASTER_GROUP =
    "@ci\\:circaCategoryMasterGroup:";

  /** Prefix used by Alfresco to identify group authorities. */
  private static final String GROUP_PREFIX = "GROUP_";

  /** Name of the top-level CIRCABC master group, which is skipped when walking memberships. */
  private static final String GROUP_CIRCA_BC_MASTER_GROUP =
    "GROUP_CircaBC--MasterGroup";

  /** Marker token identifying master group authorities. */
  private static final String MASTER_GROUP = "--MasterGroup--";

  /** Reversed form of {@link #MASTER_GROUP} used to detect reversed authority names. */
  private static final String MASTER_GROUP_REVERSE = "--puorGretsaM--";

  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  private LdapUserService luceneUserService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PersonService personService;

  @Autowired
  private ConfigurableService configurableService;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private MutableAuthenticationService authenticationService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Creates a repository user whose profile is seeded from LDAP/EU Login data.
   *
   * <p>The supplied {@code userId} is normalised by stripping any {@code @domain} suffix to obtain
   * the authority, LDAP properties are looked up and copied onto a new
   * {@link CircabcUserDataBean}, and sensible defaults are applied before delegating to
   * {@link #createUser(CircabcUserDataBean, boolean)}.
   *
   * @param userId the raw user identifier, optionally containing an {@code @domain} suffix
   * @param enabled {@code true} to enable authentication for the newly created user
   * @return the {@link NodeRef} of the created {@code Person} node
   */
  @Override
  public NodeRef createLdapUser(final String userId, final boolean enabled) {
    String authority = userId;

    // clean user id
    if (authority.contains("@")) {
      authority = authority.substring(0, authority.indexOf('@'));
    }

    final CircabcUserDataBean user = new CircabcUserDataBean();
    user.setUserName(authority);
    final CircabcUserDataBean ldapUserDetail =
      ldapUserService.getLDAPUserDataByUid(authority);
    user.copyLdapProperties(ldapUserDetail);

    // initialize other properties.
    user.setCompanyId("");
    user.setURL("");
    user.setVisibility(Boolean.FALSE);
    user.setGlobalNotification(Boolean.TRUE);
    user.setLastLoginTime(new Date());
    user.setLastModificationDetailsTime(new Date());
    user.setCreationDate(new Date());

    return createUser(user, enabled);
  }

  /**
   * Creates a repository user and sets its authentication enablement state.
   *
   * @param circabcUser the profile data of the user to create
   * @param enabled {@code true} to enable authentication, {@code false} to create it disabled
   * @return the {@link NodeRef} of the created {@code Person} node
   */
  @Override
  public NodeRef createUser(CircabcUserDataBean circabcUser, boolean enabled) {
    // create the user
    final NodeRef userNodeRef = createUser(circabcUser);

    authenticationService.setAuthenticationEnabled(
      circabcUser.getUserName(),
      enabled
    );

    return userNodeRef;
  }

  /**
   * Creates the {@code Person} node and authentication for a user.
   *
   * <p>The whole creation runs as the admin user to bypass permission checks. A random secure
   * password is generated when the bean does not carry one, an authentication entry is created if
   * none exists, the user is granted full permission on their own {@code Person} node and the
   * CIRCABC aspect is applied.
   *
   * @param circabcUser the profile data of the user to create
   * @return the {@link NodeRef} of the created {@code Person} node
   */
  @Override
  public NodeRef createUser(final CircabcUserDataBean circabcUser) {
    // DIGIT-CIRCABC-213

    // Run user creation as admin to bypass permission checks
    return AuthenticationUtil.runAs(
      () -> {
        // create the node to represent the Person
        final NodeRef newPerson = this.personService.createPerson(
          circabcUser.getAllAttributesInMap()
        );

        // fix DIGIT-CIRCABC-407
        String newPassword = generateSecurePassword(50);

        final String password = (circabcUser.getPassword() == null)
          ? newPassword
          : circabcUser.getPassword();

        if (
          !authenticationService.authenticationExists(circabcUser.getUserName())
        ) {
          authenticationService.createAuthentication(
            circabcUser.getUserName(),
            password.toCharArray()
          );
        }

        // ensure the user can access their own Person object
        this.permissionService.setPermission(
          newPerson,
          circabcUser.getUserName(),
          permissionService.getAllPermission(),
          true
        );

        // add the circa aspect
        nodeService.addAspect(
          newPerson,
          UserModel.TYPE_CIRCA_ASPECT,
          circabcUser.getAspectAttributesInMap()
        );

        return newPerson;
      },
      AuthenticationUtil.getAdminUserName()
    );
  }

  /**
   * Returns the Category memberships of the given user, sorted using
   * {@link UserCategoryMembershipRecordComparator}.
   *
   * @param pUserName the user whose Category memberships are requested
   * @return the sorted list of Category membership records
   */
  @Override
  public List<UserCategoryMembershipRecord> getCategories(
    final String pUserName
  ) {
    final List<UserCategoryMembershipRecord> result =
      circabcService.getCategories(pUserName);
    Collections.sort(
      result,
      UserCategoryMembershipRecordComparator.getInstance()
    );
    return result;
  }

  /**
   * Loads the profile of a user from the repository as a {@link CircabcUserDataBean}.
   *
   * @param userName the user name to look up
   * @return the profile data read from the user's {@code Person} node
   */
  @Override
  public CircabcUserDataBean getCircabcUserDataBean(String userName) {
    // Get the node to represent the Person
    final NodeRef person = this.personService.getPerson(userName);
    final Map<QName, Serializable> properties = nodeService.getProperties(
      person
    );
    return UserServiceImpl.getCircabcUserDataBean(properties);
  }

  /**
   * Builds a {@link CircabcUserDataBean} from a map of {@code Person} node properties, copying only
   * the properties that are present (non-{@code null}).
   *
   * @param properties the property map of a {@code Person} node
   * @return a populated {@link CircabcUserDataBean}
   */
  private static CircabcUserDataBean getCircabcUserDataBean(
    final Map<QName, Serializable> properties
  ) {
    final CircabcUserDataBean bean = new CircabcUserDataBean();

    setStringProperty(
      bean::setFirstName,
      properties,
      ContentModel.PROP_FIRSTNAME
    );
    setStringProperty(
      bean::setLastName,
      properties,
      ContentModel.PROP_LASTNAME
    );
    setStringProperty(
      bean::setUserName,
      properties,
      ContentModel.PROP_USERNAME
    );
    setStringProperty(bean::setEmail, properties, ContentModel.PROP_EMAIL);
    setStringProperty(bean::setCompanyId, properties, ContentModel.PROP_ORGID);
    setStringProperty(bean::setPhone, properties, UserModel.PROP_PHONE);
    setStringProperty(bean::setFax, properties, UserModel.PROP_FAX);
    setStringProperty(bean::setURL, properties, UserModel.PROP_URL);
    setStringProperty(
      bean::setPostalAddress,
      properties,
      UserModel.PROP_POSTAL_ADDRESS
    );
    setStringProperty(
      bean::setDescription,
      properties,
      UserModel.PROP_DESCRIPTION
    );
    setStringProperty(
      bean::setOrgdepnumber,
      properties,
      UserModel.PROP_ORGDEPNUMBER
    );
    setStringProperty(bean::setTitle, properties, UserModel.PROP_TITLE);
    setStringProperty(bean::setDomain, properties, UserModel.PROP_DOMAIN);
    setStringProperty(
      bean::setEcasUserName,
      properties,
      UserModel.PROP_ECAS_USER_NAME
    );

    NodeRef homeSpaceNodeRef = (NodeRef) properties.get(
      ContentModel.PROP_HOMEFOLDER
    );
    if (homeSpaceNodeRef != null) bean.setHomeSpaceNodeRef(homeSpaceNodeRef);

    setDateProperty(
      bean::setCreationDate,
      properties,
      UserModel.PROP_CREATION_DATE
    );
    setDateProperty(
      bean::setLastModificationDetailsTime,
      properties,
      UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME
    );
    setDateProperty(
      bean::setLastLoginTime,
      properties,
      UserModel.PROP_LAST_LOGIN_TIME
    );

    Boolean globalNotification = (Boolean) properties.get(
      UserModel.PROP_GLOBAL_NOTIFICATION
    );
    if (globalNotification != null) bean.setGlobalNotification(
      globalNotification
    );

    Boolean visibility = (Boolean) properties.get(UserModel.PROP_VISISBILITY);
    if (visibility != null) bean.setVisibility(visibility);

    return bean;
  }

  /**
   * Applies a {@code String} property to the target setter when the value is present.
   *
   * @param setter the consumer that receives the value
   * @param props the source property map
   * @param key the property key to read
   */
  private static void setStringProperty(
    java.util.function.Consumer<String> setter,
    Map<QName, Serializable> props,
    QName key
  ) {
    String value = (String) props.get(key);
    if (value != null) setter.accept(value);
  }

  /**
   * Applies a {@code Date} property to the target setter when the value is present.
   *
   * @param setter the consumer that receives the value
   * @param props the source property map
   * @param key the property key to read
   */
  private static void setDateProperty(
    java.util.function.Consumer<Date> setter,
    Map<QName, Serializable> props,
    QName key
  ) {
    Date value = (Date) props.get(key);
    if (value != null) setter.accept(value);
  }

  /**
   * Resolves the event root nodes of all Interest Groups the user belongs to.
   *
   * <p>The user's authorities are scanned for master group markers; the top-level CIRCABC master
   * group is ignored. For each matching Interest Group the corresponding IG root node is located
   * and, if it carries the event-root aspect, its event root child is collected.
   *
   * @param pUserName the user whose Interest Group event roots are requested
   * @return the list of event root {@link NodeRef}s (possibly empty)
   */
  @Override
  public List<NodeRef> getEventRootNodes(final String pUserName) {
    final List<NodeRef> result = new ArrayList<>();
    final Set<String> authorities = authorityService.getAuthoritiesForUser(
      pUserName
    );
    for (String authority : authorities) {
      if (
        authority.contains(MASTER_GROUP) ||
        authority.contains(MASTER_GROUP_REVERSE)
      ) {
        // ignore CIRCABC root
        if (authority.startsWith(GROUP_CIRCA_BC_MASTER_GROUP)) {
          continue;
        }
        final String searchItem = authority.replace(GROUP_PREFIX, "");
        final NodeRef igRootNodeRef = getIGNoderef(searchItem);
        if (igRootNodeRef != null) {
          final NodeRef eventRoot = getChildByAspect(
            igRootNodeRef,
            CircabcModel.ASPECT_EVENT_ROOT
          );
          if (eventRoot != null) {
            result.add(eventRoot);
          }
        }
      }
    }
    return result;
  }

  /**
   * Performs a Lucene search for the Interest Group root node whose master group matches the given
   * search item. The search is executed with an English locale which is restored afterwards.
   *
   * @param searchItem the master group value identifying the Interest Group
   * @return the matching IG root {@link NodeRef}, or {@code null} if none is found or the search
   *     fails
   */
  private NodeRef getIGNoderef(final String searchItem) {
    NodeRef result = null;

    Locale previousLocal = I18NUtil.getLocale();
    I18NUtil.setLocale(Locale.of("en"));
    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
    sp.setQuery(CI_CIRCA_IGROOT_MASTER_GROUP + "\"" + searchItem + "\"");

    ResultSet rs = null;
    try {
      rs = searchService.query(sp);
      if (rs.length() != 0) {
        result = rs.getRow(0).getNodeRef();
      }
    } catch (final Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error when serching for  interest group " + searchItem,
          e
        );
      }
    } finally {
      I18NUtil.setLocale(previousLocal);
      if (rs != null) {
        try {
          rs.close();
        } catch (final Exception e2) {
          if (logger.isWarnEnabled()) {
            logger.warn(RESULT_SET_CLOSE_EXCEPTION, e2);
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns the first child of the given parent node that carries the specified aspect.
   *
   * @param parent the parent node whose children are inspected
   * @param aspect the aspect to look for on the children
   * @return the first matching child {@link NodeRef}, or {@code null} if none matches
   */
  private NodeRef getChildByAspect(final NodeRef parent, final QName aspect) {
    final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(parent);
    NodeRef ref;
    for (final ChildAssociationRef assoc : assocs) {
      ref = assoc.getChildRef();
      if (nodeService.hasAspect(ref, aspect)) {
        return ref;
      }
    }

    return null;
  }

  /**
   * Returns all Interest Group memberships of the given user, sorted using
   * {@link UserIGMembershipRecordComparator}.
   *
   * @param pUserName the user whose Interest Group memberships are requested
   * @return the sorted list of Interest Group membership records
   */
  @Override
  public List<UserIGMembershipRecord> getInterestGroups(String pUserName) {
    final List<UserIGMembershipRecord> interestGroups =
      circabcService.getInterestGroups(pUserName);
    Collections.sort(
      interestGroups,
      UserIGMembershipRecordComparator.getInstance()
    );
    return interestGroups;
  }

  /**
   * Returns the Interest Group memberships of the given user, restricted to the supplied
   * categories.
   *
   * @param userName the user whose Interest Group memberships are requested
   * @param categories the categories to filter on; when {@code null} or empty no filtering is
   *     applied
   * @return the filtered list of Interest Group membership records
   */
  @Override
  public List<UserIGMembershipRecord> getInterestGroups(
    final String userName,
    List<NodeRef> categories
  ) {
    return getFileteredIGs(userName, categories);
  }

  /**
   * Retrieves the user's Interest Group memberships and, when a non-empty category list is
   * supplied, keeps only the memberships whose category belongs to that list.
   *
   * @param pUserName the user whose Interest Group memberships are requested
   * @param categories the categories to filter on; when {@code null} or empty no filtering is
   *     applied
   * @return the (optionally) filtered list of Interest Group membership records
   */
  private List<UserIGMembershipRecord> getFileteredIGs(
    final String pUserName,
    final List<NodeRef> categories
  ) {
    List<UserIGMembershipRecord> result = circabcService.getInterestGroups(
      pUserName
    );
    if (categories == null || categories.isEmpty()) {
      return result;
    } else {
      // filter the result where category is in the list
      List<UserIGMembershipRecord> filteredResult = new ArrayList<>();
      for (UserIGMembershipRecord userMembership : result) {
        if (
          categories.contains(
            Converter.createNodeRefFromId(userMembership.getCategoryNodeId())
          )
        ) {
          filteredResult.add(userMembership);
        }
      }
      return filteredResult;
    }
  }

  /**
   * Retrieves user profile data from LDAP by its unique identifier (uid).
   *
   * @param ldapUserID the LDAP uid to look up
   * @return the LDAP user data, or {@code null} if not found
   */
  @Override
  public CircabcUserDataBean getLDAPUserDataByUid(String ldapUserID) {
    return ldapUserService.getLDAPUserDataByUid(ldapUserID);
  }

  /**
   * Searches LDAP for user identifiers matching the given attributes.
   *
   * @param uid the uid criterion
   * @param moniker the moniker criterion
   * @param email the email criterion
   * @param cn the common name criterion
   * @param conjunction {@code true} to combine the criteria with AND, {@code false} to combine
   *     them with OR
   * @return the list of matching LDAP user identifiers
   */
  @Override
  public List<String> getLDAPUserIDByIdMonikerEmailCn(
    String uid,
    String moniker,
    String email,
    String cn,
    boolean conjunction
  ) {
    return ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
      uid,
      moniker,
      email,
      cn,
      conjunction
    );
  }

  /**
   * Returns the {@code Person} node for the given user name.
   *
   * @param pUserName the user name to resolve
   * @return the {@link NodeRef} of the user's {@code Person} node
   */
  @Override
  public NodeRef getPerson(String pUserName) {
    return personService.getPerson(pUserName);
  }

  /**
   * Reads a single preference value stored against the given {@code Person} node.
   *
   * <p>For the interface-language preference a {@link Locale} value is converted to its language
   * code; other preferences are returned as stored.
   *
   * @param person the {@code Person} node whose preference is read
   * @param preferenceQname the qualified name of the preference to read
   * @return the preference value, or {@code null} if the user has no preferences node
   */
  @Override
  public Serializable getPreference(NodeRef person, QName preferenceQname) {
    final NodeRef prefRef = getUserPreferencesRefDoNotCreate(person);

    if (prefRef == null) {
      return null;
    } else {
      if (UserService.PREF_INTERFACE_LANGUAGE.equals(preferenceQname)) {
        final Serializable value = nodeService.getProperty(
          prefRef,
          preferenceQname
        );
        if (value instanceof Locale loc) {
          return loc.getLanguage();
        } else {
          return value;
        }
      }
      return nodeService.getProperty(prefRef, preferenceQname);
    }
  }

  /**
   * Get the node used to store user preferences without creating the {@code configurable} aspect.
   * Utilises the 'configurable' aspect on the Person linked to this user; if the preferences node
   * does not yet exist within the configurations folder it is created.
   *
   * @param person the {@code Person} node whose preferences node is requested
   * @return the preferences {@link NodeRef}, or {@code null} if the person is not configurable
   * @throws IllegalStateException if the person is configurable but has no configurations folder
   */

  private NodeRef getUserPreferencesRefDoNotCreate(final NodeRef person) {
    NodeRef prefRef = null;

    if (!nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE)) {
      return null;
    }

    // target of the assoc is the configurations folder ref
    final NodeRef configRef = configurableService.getConfigurationFolder(
      person
    );
    if (configRef == null) {
      throw new IllegalStateException(
        "Unable to find associated 'configurations' folder for node: " + person
      );
    }

    final String xpath =
      NamespaceService.APP_MODEL_PREFIX + ":" + "preferences";
    final List<NodeRef> nodes = searchService.selectNodes(
      configRef,
      xpath,
      null,
      namespaceService,
      false
    );

    if (nodes.size() == 1) {
      prefRef = nodes.get(0);
    } else {
      // create the preferences Node for this user
      final ChildAssociationRef childRef = nodeService.createNode(
        configRef,
        ContentModel.ASSOC_CONTAINS,
        QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "preferences"),
        ContentModel.TYPE_CMOBJECT
      );

      prefRef = childRef.getChildRef();
    }

    return prefRef;
  }

  /**
   * Finds the user name of the repository user whose email matches the given value, searching the
   * people container store.
   *
   * @param email the email address to search for
   * @return the matching user name, or {@code null} if no user is found
   */
  @Override
  public String getUserByEmail(String email) {
    String userName = null;

    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.setQuery(
      "TYPE:\\{http\\://www.alfresco.org/model/content/1.0\\}person +@cm\\:email:\"" +
        email +
        "\""
    );

    sp.addStore(personService.getPeopleContainer().getStoreRef());
    sp.excludeDataInTheCurrentTransaction(false);

    ResultSet rs = null;
    try {
      rs = searchService.query(sp);

      if (rs.length() != 0) {
        final NodeRef person = rs.getNodeRef(0);
        userName = (String) nodeService.getProperty(
          person,
          ContentModel.PROP_USERNAME
        );
      }
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (final Exception e2) {
          if (logger.isWarnEnabled()) {
            logger.warn(RESULT_SET_CLOSE_EXCEPTION, e2);
          }
        }
      }
    }

    return userName;
  }

  /**
   * Returns the domain stored on the given user's profile.
   *
   * @param pUserName the user name to resolve
   * @return the user's domain, or {@code null} if not set
   */
  @Override
  public String getUserDomain(String pUserName) {
    final NodeRef nodeRef = getPerson(pUserName);
    final Serializable domain = nodeService.getProperty(
      nodeRef,
      UserModel.PROP_DOMAIN
    );
    return (domain == null) ? null : domain.toString();
  }

  /**
   * Returns the email address stored on the given user's profile.
   *
   * @param pUserName the user name to resolve
   * @return the user's email address, or {@code null} if not set
   */
  @Override
  public String getUserEmail(String pUserName) {
    final NodeRef nodeRef = getPerson(pUserName);
    return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL);
  }

  /**
   * Builds the display full name of a user by concatenating first and last name.
   *
   * @param pUserName the user name to resolve
   * @return the trimmed "first last" name; empty parts are omitted
   */
  @Override
  public String getUserFullName(String pUserName) {
    final NodeRef nodeRef = getPerson(pUserName);
    final String firstName = DefaultTypeConverter.INSTANCE.convert(
      String.class,
      nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME)
    );
    final String lastName = DefaultTypeConverter.INSTANCE.convert(
      String.class,
      nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME)
    );
    return (
      ((firstName == null) ? "" : firstName + " ") +
      ((lastName == null) ? "" : lastName)
    );
  }

  /**
   * Finds the user name of the repository user whose email matches the given value, searching the
   * workspace SpacesStore.
   *
   * @param email the email address to search for
   * @return the matching user name, or {@code null} if no user is found or the search fails
   */
  @Override
  public String getUserNameByEmail(String email) {
    String result = null;
    final SearchParameters sp = new SearchParameters();
    sp.setLanguage(SearchService.LANGUAGE_LUCENE);
    sp.addStore(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));

    sp.setQuery(
      "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\" AND @cm\\:email:" +
        "\"" +
        email +
        "\""
    );

    ResultSet rs = null;
    try {
      rs = searchService.query(sp);
      if (rs.length() != 0) {
        result = nodeService
          .getProperty(rs.getRow(0).getNodeRef(), ContentModel.PROP_USERNAME)
          .toString();
      }
    } catch (final Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when serching for user with email:" + email, e);
      }
    } finally {
      if (rs != null) {
        try {
          rs.close();
        } catch (final Exception e2) {
          if (logger.isWarnEnabled()) {
            logger.warn(RESULT_SET_CLOSE_EXCEPTION, e2);
          }
        }
      }
    }

    return result;
  }

  /**
   * Searches for users matching a domain and a free-text criterion (first name, last name or
   * email).
   *
   * <p>The LDAP/EU Login service is queried first. When LDAP is enabled and this is a global
   * (non-filtered) search, the local Alfresco repository is also queried via the Lucene-based
   * service so that non EU Login/LDAP users are included; results are merged without duplicates
   * (DIGITCIRCABC-5063).
   *
   * @param pDomain the domain to search within
   * @param pCriteria the free-text criterion to match
   * @param filter {@code true} to restrict the search (e.g. to the current domain), {@code false}
   *     for a global search
   * @return the merged list of matching search result records
   */
  @Override
  public List<SearchResultRecord> getUsersByDomainFirstNameLastNameEmail(
    final String pDomain,
    final String pCriteria,
    boolean filter
  ) {
    List<SearchResultRecord> result =
      ldapUserService.getUsersByDomainFirstNameLastNameEmail(
        pDomain,
        pCriteria,
        filter
      );
    // DIGITCIRCABC-5063 if we do a global search, search also the users in Alfresco
    // Repo to retrieve non EULogin/Ldap users
    // It is only necessary when we have access to the LDAP (CircabcConfig.USE_LDAP
    // is true) because otherwise we already used luceneUserService to search the
    // users before
    if (circabcConfig.isUseLDAP() && !filter) {
      List<SearchResultRecord> resultLucene =
        luceneUserService.getUsersByDomainFirstNameLastNameEmail(
          pDomain,
          pCriteria,
          filter
        );
      // merge the 2 lists
      for (SearchResultRecord x : resultLucene) {
        if (!result.contains(x)) {
          result.add(x);
        }
      }
    }
    return result;
  }

  /**
   * Searches for users matching a mail fragment and a domain.
   *
   * <p>The LDAP/EU Login service is queried first. When LDAP is enabled and this is a global
   * (non-filtered) search, the local Alfresco repository is also queried via the Lucene-based
   * service so that non EU Login/LDAP users are included; results are merged without duplicates
   * (DIGITCIRCABC-5063).
   *
   * @param mail the mail fragment to match
   * @param domain the domain to search within
   * @param filter {@code true} to restrict the search, {@code false} for a global search
   * @return the merged list of matching search result records
   */
  @Override
  public List<SearchResultRecord> getUsersByMailDomain(
    final String mail,
    final String domain,
    boolean filter
  ) {
    List<SearchResultRecord> result = ldapUserService.getUsersByMailDomain(
      mail,
      domain,
      filter
    );
    // DIGITCIRCABC-5063 if we do a global search, search also the users in Alfresco
    // Repo to retrieve non EULogin/Ldap users
    // It is only necessary when we have access to the LDAP (CircabcConfig.USE_LDAP
    // is true) because otherwise we already used luceneUserService to search the
    // users before
    if (circabcConfig.isUseLDAP() && !filter) {
      List<SearchResultRecord> resultLucene =
        luceneUserService.getUsersByMailDomain(mail, domain, filter);
      // merge the 2 lists
      for (SearchResultRecord x : resultLucene) {
        if (!result.contains(x)) {
          result.add(x);
        }
      }
    }
    return result;
  }

  /**
   * Returns the set of user names that are granted the given permission on the given node.
   *
   * <p>All set permissions with {@link AccessStatus#ALLOWED} for the requested permission are
   * inspected; user authorities are added directly and group authorities (except
   * {@code GROUP_EVERYONE}) are expanded to their contained users.
   *
   * @param nodeRef the node whose permissions are inspected
   * @param permission the permission to match
   * @return the set of user names holding the permission
   */
  @Override
  public Set<String> getUsersWithPermission(
    final NodeRef nodeRef,
    final String permission
  ) {
    final Set<String> users = new HashSet<>();
    final Set<AccessPermission> accessPermissions =
      permissionService.getAllSetPermissions(nodeRef);

    for (final AccessPermission accessPermission : accessPermissions) {
      if (isAllowedPermission(accessPermission, permission)) {
        addUsersFromAuthority(users, accessPermission.getAuthority());
      }
    }

    return users;
  }

  /**
   * Tests whether an access permission grants the requested permission with an ALLOWED status.
   *
   * @param accessPermission the access permission to test
   * @param permission the permission name to match
   * @return {@code true} if the permission matches and is allowed
   */
  private boolean isAllowedPermission(
    AccessPermission accessPermission,
    String permission
  ) {
    return (
      accessPermission.getPermission().equals(permission) &&
      accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED)
    );
  }

  /**
   * Adds the users represented by an authority to the accumulator set. A USER authority is added
   * directly; a GROUP authority (other than {@code GROUP_EVERYONE}) is expanded to its contained
   * users.
   *
   * @param users the accumulator set of user names
   * @param authority the authority to resolve
   */
  private void addUsersFromAuthority(Set<String> users, String authority) {
    AuthorityType authType = AuthorityType.getAuthorityType(authority);

    if (authType.equals(AuthorityType.USER)) {
      users.add(authority);
    } else if (
      authType.equals(AuthorityType.GROUP) &&
      !authority.equals("GROUP_EVERYONE")
    ) {
      addUsersFromGroup(users, authority);
    }
  }

  /**
   * Adds all users contained in the given group authority to the accumulator set. Logs an error if
   * the authority does not exist.
   *
   * @param users the accumulator set of user names
   * @param authority the group authority to expand
   */
  private void addUsersFromGroup(Set<String> users, String authority) {
    if (authorityService.authorityExists(authority)) {
      users.addAll(
        authorityService.getContainedAuthorities(
          AuthorityType.USER,
          authority,
          false
        )
      );
    } else if (logger.isErrorEnabled()) {
      logger.error("Authority does bot exists: " + authority);
    }
  }

  /**
   * Sets the authentication password for the given user.
   *
   * @param pUserName the user whose password is changed
   * @param pNewPassword the new password
   */
  @Override
  public void setPassword(String pUserName, char[] pNewPassword) {
    this.authenticationService.setAuthentication(pUserName, pNewPassword);
  }

  /**
   * Updates the user's profile properties (aspect properties only).
   *
   * @param pCircabcUser the profile data to persist
   */
  @Override
  public void updateUser(CircabcUserDataBean pCircabcUser) {
    this.updateUser(pCircabcUser, false);
  }

  /**
   * Updates the user's profile properties on their {@code Person} node.
   *
   * <p>When {@code nonAspectProperties} is {@code true} the update is applied only if the currently
   * stored last-modification time precedes the one carried by the bean (used by batch
   * synchronisation to avoid overwriting newer data). The last-modification time is always refreshed
   * on a successful update.
   *
   * @param circabcUser the profile data to persist
   * @param nonAspectProperties {@code true} to apply the batch-safe, timestamp-guarded update
   */
  @Override
  public void updateUser(
    final CircabcUserDataBean circabcUser,
    final boolean nonAspectProperties
  ) {
    // Get the node to represent the Person
    final NodeRef person = this.personService.getPerson(
      circabcUser.getUserName()
    );

    // update the properties
    Map<QName, Serializable> prop = null;
    if (nonAspectProperties) {
      // modify only if modify time is before time from batch
      final Date lastModify = (Date) nodeService.getProperty(
        person,
        UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME
      );
      if (lastModify.before(circabcUser.getLastModificationDetailsTime())) {
        prop = nodeService.getProperties(person);
        prop.putAll(circabcUser.getAttributesAsMap());
        prop.put(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME, new Date());
        nodeService.setProperties(person, prop);
      }
    } else {
      prop = nodeService.getProperties(person);
      prop.putAll(circabcUser.getAttributesAsMap());
      prop.put(UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME, new Date());
      nodeService.setProperties(person, prop);
    }
  }

  /**
   * Enables or disables authentication for the given user.
   *
   * @param userName the user whose authentication state is changed
   * @param enabled {@code true} to enable authentication, {@code false} to disable it
   */
  @Override
  public void setAuthenticationEnabled(String userName, boolean enabled) {
    authenticationService.setAuthenticationEnabled(userName, enabled);
  }

  /**
   * Returns whether authentication is currently enabled for the given user.
   *
   * @param userName the user to check
   * @return {@code true} if authentication is enabled, {@code false} otherwise
   */
  @Override
  public boolean getAuthenticationEnabled(String userName) {
    return authenticationService.getAuthenticationEnabled(userName);
  }

  /**
   * Retrieves user profile data from LDAP by identifier without applying the default LDAP filter.
   *
   * @param userID the LDAP identifier to look up
   * @return the LDAP user data, or {@code null} if not found
   */
  @Override
  public CircabcUserDataBean getLDAPUserDataNoFilterByUid(String userID) {
    return ldapUserService.getLDAPUserDataNoFilterByUid(userID);
  }

  /**
   * Generates a random password of the requested length using {@link #SECURE_RANDOM} and the
   * {@link #VALID_CHARS} character set.
   *
   * @param length the number of characters in the generated password
   * @return the generated password
   */
  public static String generateSecurePassword(int length) {
    StringBuilder password = new StringBuilder();

    for (int i = 0; i < length; i++) {
      int randomIndex = SECURE_RANDOM.nextInt(VALID_CHARS.length());
      password.append(VALID_CHARS.charAt(randomIndex));
    }

    return password.toString();
  }
}
