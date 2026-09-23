package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.user.BulkUserImportService;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetails;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetailsService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.InvalidBulkImportFileFormatException;
import io.swagger.model.BulkImportUser;
import io.swagger.model.BulkImportUserData;
import io.swagger.model.BulkImportUserDataModel;
import io.swagger.model.BulkImportUsers;
import io.swagger.model.BulkInviteData;
import io.swagger.model.Category;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.NameValue;
import io.swagger.model.PreferenceConfiguration;
import io.swagger.model.Profile;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.SearchResultRecordComparator;
import io.swagger.model.User;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserIGMembershipRecord;
import io.swagger.model.alfresco.CircabcUploadedFile;
import io.swagger.model.alfresco.UserModel;
import io.swagger.model.db.IGData;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.RestInputSanitizer;
import io.swagger.util.parsers.UserJsonParser;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Implementation of the {@link UsersApi} business layer.
 *
 * <p>Contains the logic backing the CIRCABC user-related REST endpoints. It is wired via Spring
 * XML configuration and injected into the corresponding webscript classes in
 * {@code eu.europa.ec.digit.circabc.rest}. Responsibilities include:</p>
 *
 * <ul>
 *   <li>User lookup and search (Alfresco {@link PersonService} and LDAP).</li>
 *   <li>Creation, update and deletion of users and their profile details (avatar, contact
 *       information, notification preferences, UI/content-filter languages).</li>
 *   <li>Reporting a user's Interest Group and Category memberships.</li>
 *   <li>Bulk invitation of users into an Interest Group, including parsing uploaded XLS/XLSX,
 *       CSV and XML files and generating the Excel invitation template.</li>
 *   <li>Storing and retrieving per-user preference configuration.</li>
 * </ul>
 *
 * <p>Collaborating services are supplied through Spring {@code @Autowired} dependency injection.</p>
 */
public class UsersApiImpl implements UsersApi {

  /** Property key used for a user's web/URL address. */
  private static final String URL_ADDRESS = "urlAddress";
  /** Property key used for a user's postal address. */
  private static final String POSTAL_ADDRESS = "postalAddress";
  /** Property key used for a user's free-text description. */
  private static final String DESCRIPTION = "description";
  /** Property key indicating whether global notifications are enabled for the user. */
  private static final String GLOBAL_NOTIFICATION_ENABLED =
    "globalNotificationEnabled";
  /** Property key used for a user's e-mail signature. */
  private static final String SIGNATURE = "signature";
  /** Message prefix used when a user lacks access to an Interest Group. */
  private static final String NO_ACCESS_ON_IG = "No access on IG:";

  /** Property key used for a user's title. */
  private static final String TITLE = "title";
  /** Property key used for a user's organisation. */
  private static final String ORGANISATION = "organisation";

  /**
   * Ordered list of the mandatory column headers expected in a bulk-invite template
   * (XLS/XLSX/CSV). The order is significant: it defines both the layout of the generated
   * template and the validation applied to uploaded files.
   */
  private static final List<String> definedColumns = new ArrayList<>(
    Arrays.asList(
      "username",
      TITLE,
      "first name",
      "last name",
      "email",
      "profile",
      "profile title",
      ORGANISATION,
      "postal address"
    )
  );

  @Autowired
  private UserService userService;

  @Autowired
  private GroupsApi groupsApi;

  /** Node service used to read/write person node properties (application authority). */
  @Autowired
  private NodeService nodeService;

  /** Security-enforcing node service that applies the current user's permissions. */
  @Autowired
  @Qualifier("NodeService") // NOSONAR
  private NodeService secureNodeService;

  @Autowired
  private PersonService personService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private MutableAuthenticationService authenticationService;

  @Autowired
  private UserDetailsService userDetailsService;

  /** LDAP (or Lucene-backed) user service used to resolve users from the directory. */
  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  @Autowired
  private CircabcService circabcService;

  @Autowired
  private ConfigurableService configurableService;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Maximum number of users returned by {@link #usersGet}; configurable, defaults to 5. */
  @Value("${max.user.number:5}")
  private int maxUserNumber;

  @Autowired
  private BulkUserImportService bulkUserImportService;

  /** Logger for this class. */
  private static final Log logger = LogFactory.getLog(UsersApiImpl.class);

  /**
   * Searches for users and returns a capped, de-duplicated list of matches.
   *
   * <p>If the query contains an {@code '@'} it is treated as an e-mail domain search; otherwise
   * it is matched against domain, first name, last name and e-mail. Results are sorted, filtered
   * to unique usernames and truncated to at most {@link #maxUserNumber} entries.</p>
   *
   * @param query the search term (username fragment or e-mail/domain)
   * @param filter whether to apply the underlying service's result filtering
   * @param matchQuery when {@code true}, only users whose e-mail matches the query exactly are kept
   * @return the list of matching {@link User} objects (never {@code null})
   */
  @Override
  public List<User> usersGet(String query, boolean filter, boolean matchQuery) {
    //we return only the first 5 results
    List<User> result = new ArrayList<>();

    List<SearchResultRecord> users;

    if (query.indexOf('@') == -1) {
      users = userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        query,
        filter
      );
    } else {
      users = userService.getUsersByMailDomain(query, "allusers", filter);
    }

    //sort the list
    users.sort(SearchResultRecordComparator.getInstance());

    Set<String> userNames = new HashSet<>();
    int nbOfUsers = 0;

    for (int i = 0; i < users.size() && nbOfUsers < maxUserNumber; i++) {
      SearchResultRecord user = users.get(i);
      if (!userNames.contains(user.getUserName())) {
        if (matchQuery && !query.equalsIgnoreCase(user.getEmail())) {
          continue;
        }
        userNames.add(user.getUserName());
        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setFirstname(user.getFirstName());
        newUser.setLastname(user.getLastName());
        newUser.setUserId(user.getUserName());
        result.add(newUser);
        ++nbOfUsers;
      }
    }
    return result;
  }

  /**
   * Creates a new Alfresco person together with its authentication credentials.
   *
   * <p>Maps the supplied {@link User} fields and additional properties (title, phone, fax, URL,
   * postal address, description, company id) onto Alfresco content-model properties, then runs
   * as the admin user to create both the authentication and the person node.</p>
   *
   * @param user the user to create; its {@code properties} map must include a {@code "password"} entry
   */
  @Override
  public void usersPost(User user) {
    final Map<QName, Serializable> properties = new HashMap<>();

    properties.put(ContentModel.PROP_USERNAME, user.getUserId());
    properties.put(ContentModel.PROP_FIRSTNAME, user.getFirstname());
    properties.put(ContentModel.PROP_LASTNAME, user.getLastname());
    properties.put(ContentModel.PROP_EMAIL, user.getEmail());

    properties.put(ContentModel.PROP_TITLE, user.getProperties().get(TITLE));
    properties.put(UserModel.PROP_PHONE, user.getPhone());
    properties.put(UserModel.PROP_FAX, user.getProperties().get("fax"));
    properties.put(UserModel.PROP_URL, user.getProperties().get(URL_ADDRESS));
    properties.put(
      UserModel.PROP_POSTAL_ADDRESS,
      user.getProperties().get(POSTAL_ADDRESS)
    );
    properties.put(
      UserModel.PROP_DESCRIPTION,
      user.getProperties().get(DESCRIPTION)
    );
    properties.put(
      ContentModel.PROP_ORGID,
      user.getProperties().get("companyId")
    );

    AuthenticationUtil.runAs(
      () -> {
        authenticationService.createAuthentication(
          user.getUserId(),
          user.getProperties().get("password").toCharArray()
        );
        personService.createPerson(properties);
        return null;
      },
      AuthenticationUtil.getAdminUserName()
    );
  }

  /**
   * Retrieves a single user by id, including profile properties and avatar information.
   *
   * <p>If the person does not exist, an empty {@link User} (with an empty properties map) is
   * returned. Otherwise core fields plus additional properties (title, organisation, address,
   * description, fax, URL, signature, notification flag, admin flags, EC moniker and domain) are
   * populated.</p>
   *
   * @param userId the username to look up
   * @return the populated {@link User}, or an empty one if the user does not exist
   */
  @Override
  public User usersUserIdGet(String userId) {
    User user = new User();
    user.setProperties(new HashMap<>());
    if (personService.personExists(userId)) {
      final NodeRef personNodeRef = personService.getPerson(userId);

      Map<QName, Serializable> properties = nodeService.getProperties(
        personNodeRef
      );

      user.setUserId((String) properties.get(ContentModel.PROP_USERNAME));
      user.setFirstname((String) properties.get(ContentModel.PROP_FIRSTNAME));
      user.setLastname((String) properties.get(ContentModel.PROP_LASTNAME));
      user.setEmail((String) properties.get(ContentModel.PROP_EMAIL));

      try {
        NodeRef userAvatar = userDetailsService.getAvatar(personNodeRef);
        NodeRef defaultAvatar = userDetailsService.getDefaultAvatar();
        if (userAvatar != null) {
          user.setDefaultAvatar(defaultAvatar.equals(userAvatar));
          user.setAvatar(userAvatar.getId());
        } else {
          user.setDefaultAvatar(true);
          user.setAvatar("");
        }
      } catch (Exception e) {
        user.setDefaultAvatar(true);
        user.setAvatar("");
      }

      user.setPhone((String) properties.get(UserModel.PROP_PHONE));
      user.setVisibility((Boolean) properties.get(UserModel.PROP_VISISBILITY));
      user.setUiLang("en");
      user.setContentFilterLang("all");

      // add user's additional properties
      Map<String, String> propertiesMap = new HashMap<>();

      String ecMoniker = (String) properties.get(UserModel.PROP_ECAS_USER_NAME);
      if (ecMoniker == null) {
        propertiesMap.put(
          "ecMoniker",
          (String) properties.get(ContentModel.PROP_USERNAME)
        );
      } else {
        propertiesMap.put("ecMoniker", ecMoniker);
      }

      propertiesMap.put(TITLE, (String) properties.get(UserModel.PROP_TITLE));
      propertiesMap.put(
        ORGANISATION,
        (String) properties.get(UserModel.PROP_ORGDEPNUMBER)
      );
      propertiesMap.put(
        POSTAL_ADDRESS,
        (String) properties.get(UserModel.PROP_POSTAL_ADDRESS)
      );
      propertiesMap.put(
        DESCRIPTION,
        (String) properties.get(UserModel.PROP_DESCRIPTION)
      );
      propertiesMap.put("fax", (String) properties.get(UserModel.PROP_FAX));
      propertiesMap.put(
        URL_ADDRESS,
        (String) properties.get(UserModel.PROP_URL)
      );

      propertiesMap.put(
        GLOBAL_NOTIFICATION_ENABLED,
        String.valueOf(properties.get(UserModel.PROP_GLOBAL_NOTIFICATION))
      );
      propertiesMap.put(
        SIGNATURE,
        (String) properties.get(UserModel.PREF_SIGNATURE)
      );
      propertiesMap.put(
        "isAdmin",
        String.valueOf(authorityService.hasAdminAuthority())
      );

      propertiesMap.put("isCircabcAdmin", String.valueOf(isCircabcAdmin()));

      String domain = (String) properties.get(UserModel.PROP_DOMAIN);
      if (domain == null) {
        domain = "";
      }
      propertiesMap.put("domain", domain);

      user.setProperties(propertiesMap);
    }

    return user;
  }

  /**
   * Determines whether the current user belongs to a CIRCABC administrator group.
   *
   * @return {@code true} if any of the current user's authorities starts with the CIRCABC admin
   *     group prefix, {@code false} otherwise
   */
  private boolean isCircabcAdmin() {
    Set<String> authorities = authorityService.getAuthorities();

    for (String authority : authorities) {
      if (authority.startsWith("GROUP_CircaBCAdmin--")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Retrieves a user and overlays details fetched from the LDAP directory.
   *
   * <p>Loads the base user via {@link #usersUserIdGet(String)} and, when a matching LDAP entry is
   * found, overrides the corresponding fields. If no LDAP entry exists the base user is returned
   * unchanged and an error is logged.</p>
   *
   * @param userId the username to look up
   * @return the user enriched with LDAP details where available
   */
  @Override
  public User usersUserIdGetFromLdap(String userId) {
    User user = this.usersUserIdGet(userId);

    final CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(
      userId
    );

    if (ldapUserDetail != null) {
      applyLdapDetails(user, ldapUserDetail);
    } else {
      if (logger.isErrorEnabled()) {
        logger.error("Impossible to found " + userId + " in the ldap.");
      }
    }

    return user;
  }

  /**
   * Overlays non-null values from an LDAP data bean onto the given user.
   *
   * @param user the user to update in place
   * @param ldap the LDAP data bean providing override values
   */
  private void applyLdapDetails(User user, CircabcUserDataBean ldap) {
    if (ldap.getFirstName() != null) {
      user.setFirstname(ldap.getFirstName());
    }
    if (ldap.getLastName() != null) {
      user.setLastname(ldap.getLastName());
    }
    if (ldap.getEmail() != null) {
      user.setEmail(ldap.getEmail());
    }
    if (ldap.getPhone() != null) {
      user.setPhone(ldap.getPhone());
    }

    Map<String, String> props = user.getProperties();
    if (ldap.getTitle() != null) {
      props.put(TITLE, ldap.getTitle());
    }
    if (ldap.getOrgdepnumber() != null) {
      props.put(ORGANISATION, ldap.getOrgdepnumber());
    }
    if (ldap.getPostalAddress() != null) {
      props.put(POSTAL_ADDRESS, ldap.getPostalAddress());
    }
    if (ldap.getFax() != null) {
      props.put("fax", ldap.getFax());
    }
    if (ldap.getDescription() != null) {
      props.put(DESCRIPTION, ldap.getDescription());
    }
    if (ldap.getURL() != null) {
      props.put("url", ldap.getURL());
    }
  }

  /**
   * Updates a user's details from the supplied body and returns the resulting state.
   *
   * <p>Ensures the person node is configurable, applies the changed fields and properties, then
   * persists the updated details and refreshes the CIRCABC user record.</p>
   *
   * @param userId the username of the user to update
   * @param body the requested changes; only non-null fields/properties are applied
   * @return the updated {@link User} reflecting the persisted state
   */
  @Override
  public User usersUserIdPut(String userId, User body) {
    User result = new User();
    final NodeRef personNodeRef = personService.getPerson(userId);
    final PersonInfo person = personService.getPerson(personNodeRef);
    final UserDetails userDetails = userDetailsService.getUserDetails(
      personNodeRef
    );
    userDetails.setNodeRef(personNodeRef);
    if (configurableService.getConfigurationFolder(personNodeRef) == null) {
      configurableService.makeConfigurable(personNodeRef);
    }

    result.setAvatar(userDetails.getAvatar().getId());
    result.setEmail(userDetails.getEmail());
    result.setUserId(person.getUserName());
    result.setFirstname(person.getFirstName());
    result.setLastname(person.getLastName());
    result.setPhone(userDetails.getPhone());
    result.setVisibility(userDetails.getVisibility());
    result.setUiLang(userDetails.getUserInterfaceLanguage());
    result.setContentFilterLang(
      userDetails.getContentFilterLanguage() != null
        ? userDetails.getContentFilterLanguage().getLanguage()
        : "all"
    );

    applyUserFields(body, result, userDetails);
    applyUserProperties(body, result, userDetails);

    userDetailsService.updateUserDetails(personNodeRef, userDetails);
    circabcService.updateUser(personNodeRef);

    return result;
  }

  /**
   * Copies the non-null core fields (avatar, e-mail, names, phone, visibility, languages) from the
   * request body onto both the persisted details and the response object.
   *
   * @param body the incoming user data
   * @param result the response user to update
   * @param userDetails the details object to be persisted
   */
  private void applyUserFields(
    User body,
    User result,
    UserDetails userDetails
  ) {
    if (body.getAvatar() != null) {
      userDetails.setAvatar(Converter.createNodeRefFromId(body.getAvatar()));
      result.setAvatar(body.getAvatar());
    }
    if (body.getEmail() != null) {
      userDetails.setEmail(body.getEmail());
      result.setEmail(body.getEmail());
    }
    if (body.getFirstname() != null) {
      userDetails.setFirstName(body.getFirstname());
      result.setFirstname(body.getFirstname());
    }
    if (body.getLastname() != null) {
      userDetails.setLastName(body.getLastname());
      result.setLastname(body.getLastname());
    }
    if (body.getPhone() != null) {
      userDetails.setPhone(body.getPhone());
      result.setPhone(body.getPhone());
    }
    if (body.getVisibility() != null) {
      userDetails.setVisibility(body.getVisibility());
      result.setVisibility(body.getVisibility());
    }
    if (body.getUiLang() != null) {
      userDetails.setUserInterfaceLanguage(body.getUiLang());
      result.setUiLang(body.getUiLang());
    }
    if (body.getContentFilterLang() != null) {
      userDetails.setContentFilterLanguage(
        Locale.of(body.getContentFilterLang())
      );
      result.setContentFilterLang(body.getContentFilterLang());
    }
  }

  /**
   * Copies the non-null additional properties (title, organisation, address, description, fax,
   * URL, global-notification flag, signature) from the request body onto the persisted details,
   * and echoes them back on the response object.
   *
   * @param body the incoming user data (its {@code properties} map may be {@code null})
   * @param result the response user to update
   * @param userDetails the details object to be persisted
   */
  private void applyUserProperties(
    User body,
    User result,
    UserDetails userDetails
  ) {
    if (body.getProperties() == null) {
      return;
    }

    Map<String, String> properties = body.getProperties();

    if (properties.get(TITLE) != null) {
      userDetails.setTitle(properties.get(TITLE));
    }
    if (properties.get(ORGANISATION) != null) {
      userDetails.setOrganisation(properties.get(ORGANISATION));
    }
    if (properties.get(POSTAL_ADDRESS) != null) {
      userDetails.setPostalAddress(properties.get(POSTAL_ADDRESS));
    }
    if (properties.get(DESCRIPTION) != null) {
      userDetails.setDescription(properties.get(DESCRIPTION));
    }
    if (properties.get("fax") != null) {
      userDetails.setFax(properties.get("fax"));
    }
    if (properties.get(URL_ADDRESS) != null) {
      userDetails.setUrl(properties.get(URL_ADDRESS));
    }
    if (properties.get(GLOBAL_NOTIFICATION_ENABLED) != null) {
      userDetails.setGlobalNotification(
        "true".equals(properties.get(GLOBAL_NOTIFICATION_ENABLED))
      );
    }
    properties.computeIfPresent(SIGNATURE, (k, v) ->
      RestInputSanitizer.sanitizeRichText(v)
    );
    if (properties.get(SIGNATURE) != null) {
      userDetails.setSignature(properties.get(SIGNATURE));
    }

    result.setProperties(properties);
  }

  /**
   * Returns the Interest Group memberships of a user, using the full (non-light) representation.
   *
   * @param userId the username whose memberships are requested
   * @return the list of {@link InterestGroupProfile} entries (never {@code null})
   */
  @Override
  public List<InterestGroupProfile> getUserMembership(String userId) {
    return getUserMembershipInternal(userId, true);
  }

  /**
   * Returns the Interest Group memberships of a user.
   *
   * @param userId the username whose memberships are requested
   * @param lightMode when {@code true}, Interest Groups are returned in a lightweight form
   * @return the list of {@link InterestGroupProfile} entries (never {@code null})
   */
  @Override
  public List<InterestGroupProfile> getUserMembership(
    String userId,
    Boolean lightMode
  ) {
    return getUserMembershipInternal(userId, lightMode);
  }

  /**
   * Builds the list of Interest Group memberships (with associated profile) for a user.
   *
   * @param userId the username whose memberships are requested; a blank id yields an empty list
   * @param lightMode when {@code true}, Interest Groups are returned in a lightweight form
   * @return the list of {@link InterestGroupProfile} entries (never {@code null})
   */
  private List<InterestGroupProfile> getUserMembershipInternal(
    String userId,
    Boolean lightMode
  ) {
    List<InterestGroupProfile> result = new ArrayList<>();

    if (!"".equals(userId)) {
      List<UserIGMembershipRecord> interestGroupsMemberships =
        userService.getInterestGroups(userId);
      for (UserIGMembershipRecord membership : interestGroupsMemberships) {
        InterestGroupProfile interestGroupProfile = new InterestGroupProfile();
        interestGroupProfile.setInterestGroup(
          groupsApi.getInterestGroup(
            membership.getInterestGroupNodeId(),
            lightMode
          )
        );

        Profile p = new Profile();
        p.setId(membership.getprofileNodeRefId());
        p.setName(membership.getProfile());
        p.setTitle(Converter.toI18NProperty(membership.getProfileTitle()));
        p.setGroupName(membership.getAlfrescoGroup());
        interestGroupProfile.setProfile(p);

        result.add(interestGroupProfile);
      }
    }

    return result;
  }

  /**
   * Returns the Categories in which a user holds a membership, with localized names/titles.
   *
   * @param userId the username whose categories are requested; a blank id yields an empty list
   * @return the list of {@link Category} objects (never {@code null})
   */
  @Override
  public List<Category> getUserCategories(String userId) {
    List<Category> result = new ArrayList<>();

    if (!"".equals(userId)) {
      List<UserCategoryMembershipRecord> categories = userService.getCategories(
        userId
      );
      for (UserCategoryMembershipRecord categAdmin : categories) {
        NodeRef categRef = Converter.createNodeRefFromId(
          categAdmin.getCategoryNodeId()
        );
        Category categ = new Category();
        categ.setId(categAdmin.getCategoryNodeId());
        categ.setName(
          nodeService.getProperty(categRef, ContentModel.PROP_NAME).toString()
        );

        Serializable title = nodeService.getProperty(
          categRef,
          ContentModel.PROP_TITLE
        );
        if (title instanceof String str) {
          categ.setTitle(Converter.toI18NProperty(str));
        } else if (title instanceof MLText mlText) {
          categ.setTitle(Converter.toI18NProperty(mlText));
        }

        result.add(categ);
      }
    }

    return result;
  }

  /**
   * Removes the avatar of the given user, reverting to the default.
   *
   * @param userId the username whose avatar is to be removed
   */
  @Override
  public void removeAvatar(String userId) {
    final NodeRef personNodeRef = personService.getPerson(userId);
    userDetailsService.removeAvatar(personNodeRef);
  }

  /**
   * Validates, scales and stores a new avatar image for the given user.
   *
   * <p>The file name is validated against an allowed set of image extensions and the image is
   * scaled down (see {@link #scaleImage(InputStream)}) before being stored.</p>
   *
   * @param userId the username whose avatar is being updated
   * @param imageInputStream the stream containing the uploaded image
   * @param fileName the original file name, used for extension validation
   * @throws IllegalArgumentException if the file type is invalid or the image cannot be processed
   */
  @Override
  public void updateAvatar(
    String userId,
    InputStream imageInputStream,
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

    CircabcUploadedFile uploadedFile;

    try {
      uploadedFile = scaleImage(imageInputStream);
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not update avatar.", e);
    }

    final NodeRef personNodeRef = personService.getPerson(userId);
    userDetailsService.updateAvatar(
      personNodeRef,
      uploadedFile.getFileName(),
      uploadedFile.getFile()
    );
  }

  /**
   * Reads an image from the stream and produces a PNG avatar file, scaling it down to a maximum
   * of 64&nbsp;pixels on its largest side when necessary.
   *
   * @param imageInputStream the source image stream
   * @return a {@link CircabcUploadedFile} describing the generated PNG temp file
   * @throws IOException if the image cannot be read or written
   * @throws IllegalArgumentException if the input is not a valid image or writing fails
   */
  private CircabcUploadedFile scaleImage(InputStream imageInputStream)
    throws IOException {
    CircabcUploadedFile uploadedFile = new CircabcUploadedFile();

    uploadedFile.setFileName("avatar.png");
    File file = TempFileProvider.createTempFile(
      uploadedFile.getFileName(),
      ".tmp"
    );
    uploadedFile.setFile(file);

    long attachmentTotalSize = Long.parseLong(
      circabcConfig.getLogoAllowedSizeinBytes()
    );

    ApiToolBox.inputStreamToFile(imageInputStream, file, attachmentTotalSize);

    BufferedImage bufferedImage = ImageIO.read(file);
    if (bufferedImage == null) {
      throw new IllegalArgumentException(
        "Not an image, or image corrupted: " + uploadedFile.getFileName()
      );
    }

    boolean couldWrite;

    double maxSize = 64.0;
    if (
      bufferedImage.getWidth() > maxSize || bufferedImage.getHeight() > maxSize
    ) {
      // image needs to be scaled down, find scale factor
      double scaleFactor;
      if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
        scaleFactor = bufferedImage.getWidth() / maxSize;
      } else {
        scaleFactor = bufferedImage.getHeight() / maxSize;
      }

      // scale it down
      int newWidth = (int) (bufferedImage.getWidth() / scaleFactor);
      int newHeight = (int) (bufferedImage.getHeight() / scaleFactor);
      Image image = bufferedImage.getScaledInstance(
        newWidth,
        newHeight,
        Image.SCALE_FAST
      );
      BufferedImage scaledImg = new BufferedImage(
        newWidth,
        newHeight,
        BufferedImage.TYPE_INT_RGB
      );
      Graphics2D graphic = scaledImg.createGraphics();
      graphic.drawImage(image, 0, 0, null);

      // store the scaled image as png
      couldWrite = ImageIO.write(scaledImg, "png", uploadedFile.getFile());
    } else {
      // store the original image as png
      couldWrite = ImageIO.write(bufferedImage, "png", uploadedFile.getFile());
    }
    if (!couldWrite) {
      throw new IllegalArgumentException("Error saving avatar file.");
    }

    return uploadedFile;
  }

  /**
   * Writes an Excel (.xls) bulk-invite template to the HTTP response as a downloadable attachment.
   *
   * <p>The template contains a single "Members" sheet whose header row lists {@link #definedColumns}.
   * I/O errors during generation are logged rather than propagated.</p>
   *
   * @param response the webscript response to stream the generated workbook to
   */
  @Override
  public void writeBulkInviteTemplate(WebScriptResponse response) {
    response.setContentType("application/vnd.ms-excel;charset=UTF-8");
    response.setHeader(
      "Content-Disposition",
      "attachment;filename=template.xls"
    );

    OutputStream outStream = null;

    try {
      outStream = response.getOutputStream();

      try (Workbook workbook = new HSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Members");

        Row titleRow = sheet.createRow(0);
        for (int idx = 0; idx < definedColumns.size(); idx++) {
          titleRow.createCell(idx).setCellValue(definedColumns.get(idx));
        }

        workbook.write(outStream);
      }
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error during template generation", e);
      }
    } finally {
      if (outStream != null) {
        try {
          outStream.close();
        } catch (IOException e) {
          logger.error("Error closing stream", e);
        }
      }
    }
  }

  /**
   * Lists the Categories available as sources for a bulk invitation for the given user.
   *
   * <p>Combines the categories where the user is a category administrator with those reachable
   * through Interest Group memberships, de-duplicating by category id. Read access is enforced on
   * each category.</p>
   *
   * @param username the user for whom available categories are collected
   * @return the de-duplicated list of {@link Category} objects
   * @throws AccessDeniedException if the current user lacks read permission on a category
   */
  @Override
  public List<Category> getBulkInviteCategories(String username) {
    List<Category> availableCategories = new ArrayList<>();

    Set<String> included = new HashSet<>();

    for (UserCategoryMembershipRecord categoryAdminEntry : userService.getCategories(
      username
    )) {
      if (!included.contains(categoryAdminEntry.getCategoryNodeId())) {
        if (
          !currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            categoryAdminEntry.getCategoryNodeId()
          )
        ) {
          throw new AccessDeniedException(
            "No access on category:" + categoryAdminEntry.getCategoryNodeId()
          );
        }
        availableCategories.add(
          new Category(
            categoryAdminEntry.getCategoryNodeId(),
            categoryAdminEntry.getCategory()
          )
        );
        included.add(categoryAdminEntry.getCategoryNodeId());
      }
    }

    for (UserIGMembershipRecord igEntry : userService.getInterestGroups(
      username
    )) {
      if (!included.contains(igEntry.getCategoryNodeId())) {
        if (
          !currentUserPermissionCheckerService.hasAlfrescoReadPermission(
            igEntry.getCategoryNodeId()
          )
        ) {
          throw new AccessDeniedException(
            "No access on category:" + igEntry.getCategoryNodeId()
          );
        }
        availableCategories.add(
          new Category(igEntry.getCategoryNodeId(), igEntry.getCategoryTitle())
        );
        included.add(igEntry.getCategoryNodeId());
      }
    }

    return availableCategories;
  }

  /**
   * Lists the Interest Groups of a category (excluding the current one) available as bulk-invite
   * sources for the authenticated user.
   *
   * <p>Category administrators see all Interest Groups except the current one; other users see
   * only those they can access via directory access.</p>
   *
   * @param categoryId the node id of the category to list Interest Groups from
   * @param currentIgId the node id of the Interest Group to exclude (the invitation destination)
   * @return the list of {@link IGData} for the eligible Interest Groups
   * @throws IllegalArgumentException if the category id does not correspond to an existing node
   */
  @Override
  public List<IGData> getBulkInviteIGs(String categoryId, String currentIgId) {
    String username = AuthenticationUtil.getFullyAuthenticatedUser();

    NodeRef categoryNodeRef = Converter.createNodeRefFromId(categoryId);
    NodeRef currentIgNodeRef = Converter.createNodeRefFromId(currentIgId);

    if (!secureNodeService.exists(categoryNodeRef)) {
      throw new IllegalArgumentException("Invalid Category id " + categoryId);
    }

    if (circabcService.isCategoryAdmin(categoryNodeRef, username)) {
      return circabcService.getCategoryIGsExceptCurrent(
        categoryNodeRef,
        currentIgNodeRef,
        username
      );
    } else {
      return circabcService.getCategoryIGsExceptCurrentWithDirectoryAccess(
        categoryNodeRef,
        currentIgNodeRef,
        username
      );
    }
  }

  /**
   * Collects the members of the given source Interest Groups as candidate bulk-invite entries for
   * a destination Interest Group.
   *
   * @param igIds the node ids of the source Interest Groups; must not be {@code null}
   * @param destinationIGId the node id of the destination Interest Group; must not be {@code null}
   * @return the aggregated list of {@link BulkImportUserData} for the collected members
   * @throws IllegalArgumentException if {@code igIds} or {@code destinationIGId} is {@code null},
   *     or if a source IG id does not correspond to an existing node
   * @throws AccessDeniedException if the current user lacks read permission on a source IG
   */
  @Override
  public List<BulkImportUserData> getBulkInviteMembers(
    List<String> igIds,
    String destinationIGId
  ) {
    if (igIds == null) {
      throw new IllegalArgumentException("IG ids cannot be null.");
    }

    if (destinationIGId == null) {
      throw new IllegalArgumentException(
        "The destination IG id cannot be null."
      );
    }

    NodeRef destinationIGNodeRef = Converter.createNodeRefFromId(
      destinationIGId
    );

    List<BulkImportUserData> userData = new ArrayList<>();

    for (String igId : igIds) {
      NodeRef nodeRef = Converter.createNodeRefFromId(igId);

      if (!secureNodeService.exists(nodeRef)) {
        throw new IllegalArgumentException("Invalid IG id " + igId);
      }

      if (
        !currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)
      ) {
        throw new AccessDeniedException(NO_ACCESS_ON_IG + igId);
      }

      bulkUserImportService.addAll(
        userData,
        bulkUserImportService.listMembers(nodeRef, false),
        destinationIGNodeRef
      );
    }

    return userData;
  }

  /**
   * Invites a batch of users into an Interest Group.
   *
   * <p>Parses the JSON payload, resolves each entry into {@link BulkImportUserData}, optionally
   * derives new profile names from the source IG name, validates that every entry has a profile,
   * builds the IG profile mapping (ignoring the built-in {@code Registered}/{@code Guest}
   * profiles) and delegates the invitation to the bulk import service.</p>
   *
   * @param bulkInviteDataJson the JSON body describing the users and IG profiles to invite
   * @param igId the node id of the target Interest Group
   * @param createNewProfiles when {@code true}, profile names are derived from the source IG name
   * @param notifyUsers when {@code true}, invited users are notified
   * @throws AccessDeniedException if the current user is not a leader/group admin of the IG
   * @throws IllegalArgumentException if the payload is invalid or any user has a blank profile name
   */
  @Override
  public void bulkInviteUsers(
    String bulkInviteDataJson,
    String igId,
    boolean createNewProfiles,
    boolean notifyUsers
  ) {
    if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
      throw new AccessDeniedException("No Leader on IG:" + igId);
    }

    BulkInviteData bulkInviteData = parseBulkInviteBodyJSON(bulkInviteDataJson);

    NodeRef currentIGNodeRef = Converter.createNodeRefFromId(igId);

    List<BulkImportUserData> bulkImportUserData = new ArrayList<>();

    for (BulkImportUserDataModel model : bulkInviteData.getBulkImportUserData()) {
      BulkImportUserData userData = getBulkImportUserData(model);
      if (
        createNewProfiles &&
        userData.getIgName() != null &&
        !userData.getIgName().isEmpty()
      ) {
        userData.setProfile(userData.getIgName());
      }
      bulkImportUserData.add(userData);
    }

    if (containsProfileBlankName(bulkImportUserData)) {
      throw new IllegalArgumentException(
        "The supplied user list contains an user with an empty profile name."
      );
    }

    Map<String, String> igProfilesMap = new HashMap<>();

    for (NameValue nameValue : bulkInviteData.getIgProfiles()) {
      String key = nameValue.getName();
      if ("Registered".equalsIgnoreCase(key) || "Guest".equalsIgnoreCase(key)) {
        continue;
      }
      igProfilesMap.put(nameValue.getName(), nameValue.getValue());
    }

    bulkUserImportService.inviteUsers(
      bulkImportUserData,
      currentIGNodeRef,
      igProfilesMap,
      notifyUsers
    );
  }

  /**
   * Checks whether any entry in the list has a missing or empty profile name.
   *
   * @param model the list of bulk-import entries to inspect
   * @return {@code true} if at least one entry has a {@code null} or empty profile, else {@code false}
   */
  private boolean containsProfileBlankName(List<BulkImportUserData> model) {
    boolean result = false;

    for (BulkImportUserData item : model) {
      if (item.getProfile() == null || item.getProfile().isEmpty()) {
        result = true;
        break;
      }
    }

    return result;
  }

  /**
   * Parses the bulk-invite JSON body into a {@link BulkInviteData} object.
   *
   * @param bulkInviteDataJson the JSON string to parse; must be non-null and non-empty
   * @return the parsed {@link BulkInviteData} containing user data and IG profile mappings
   * @throws IllegalArgumentException if the body is empty, malformed, or has invalid sub-structures
   */
  private BulkInviteData parseBulkInviteBodyJSON(String bulkInviteDataJson) {
    if (bulkInviteDataJson == null || bulkInviteDataJson.isEmpty()) {
      throw new IllegalArgumentException(
        "The body (bulk invite data) cannot be empty."
      );
    }
    JSONParser parser = new JSONParser();

    JSONObject json;

    try {
      json = (JSONObject) parser.parse(bulkInviteDataJson);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Error when parsing the body (bulk invite data).",
        e
      );
    }

    List<BulkImportUserDataModel> bulkImportUserData;
    List<NameValue> igProfiles;

    try {
      bulkImportUserData = parseBulkImportUserDataArray(
        (JSONArray) json.get("bulkImportUserData")
      );
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'usersProfiles' must be a list of NameValue.",
        e
      );
    }

    try {
      igProfiles = parseNameValueArray((JSONArray) json.get("igProfiles"));
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'igProfiles' must be a list of NameValue.",
        e
      );
    }

    return new BulkInviteData(bulkImportUserData, igProfiles);
  }

  /**
   * Converts a JSON array into a list of {@link BulkImportUserDataModel} objects.
   *
   * @param array the JSON array whose elements are user-data objects
   * @return the parsed list of models
   */
  private List<BulkImportUserDataModel> parseBulkImportUserDataArray(
    JSONArray array
  ) {
    List<BulkImportUserDataModel> list = new ArrayList<>();

    for (Object item : array) {
      JSONObject object = (JSONObject) item;
      list.add(
        new BulkImportUserDataModel(
          (String) object.get("username"),
          (String) object.get("igName"),
          (String) object.get("igRef"),
          (String) object.get("fromFile"),
          (String) object.get("email"),
          (String) object.get("status"),
          (String) object.get("profileId")
        )
      );
    }

    return list;
  }

  /**
   * Converts a JSON array into a list of {@link NameValue} pairs.
   *
   * @param array the JSON array whose elements have {@code name} and {@code value} fields
   * @return the parsed list of name/value pairs
   */
  private List<NameValue> parseNameValueArray(JSONArray array) {
    List<NameValue> list = new ArrayList<>();

    for (Object item : array) {
      JSONObject object = (JSONObject) item;
      list.add(
        new NameValue((String) object.get("name"), (String) object.get("value"))
      );
    }

    return list;
  }

  /**
   * Builds a {@link BulkImportUserData} entry from a parsed request model, resolving the user's
   * origin (an Interest Group or an uploaded file) and looking them up in LDAP.
   *
   * <p>The resulting status is {@link BulkImportUserData#STATUS_OK} when the user is found in LDAP,
   * or {@link BulkImportUserData#STATUS_ERROR} otherwise (see BUG&nbsp;5031).</p>
   *
   * @param model the parsed model describing one user to invite
   * @return the constructed {@link BulkImportUserData}
   * @throws IllegalArgumentException if the referenced IG node is invalid, or the user has neither
   *     an IG reference nor a file origin
   */
  private BulkImportUserData getBulkImportUserData(
    BulkImportUserDataModel model
  ) {
    BulkImportUserData user = new BulkImportUserData();

    if (model.getIgRef() != null && !model.getIgRef().isEmpty()) {
      // in case the user comes from an IG
      NodeRef igRef = Converter.createNodeRefFromId(
        Converter.extractNodeRefId(model.getIgRef())
      );

      if (!secureNodeService.exists(igRef)) {
        throw new IllegalArgumentException(
          "NodeRef '" +
            model.getIgRef() +
            "' given as origin for user '" +
            model.getUsername() +
            "' is invalid."
        );
      }

      String igTitle = secureNodeService
        .getProperty(igRef, ContentModel.PROP_TITLE)
        .toString();
      String igName = secureNodeService
        .getProperty(igRef, ContentModel.PROP_NAME)
        .toString();

      user.setIgRef(igRef);
      user.setIgName((igTitle != null ? igTitle : igName));
    } else if (model.getFromFile() != null && !model.getFromFile().isEmpty()) {
      // in case the user comes from an uploaded XLS(X), CSV or XML file
      user.setFromFile(model.getFromFile());
    } else {
      throw new IllegalArgumentException(
        "User must come from an IG or file. Username: " + model.getUsername()
      );
    }

    user.setExpectedUsername(model.getUsername());
    CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(
      model.getUsername()
    );

    // AMO - BUG 5031 - Fix issue if the user is not found in the LDAP
    // For example, when we use ecMoniker instead of UID!
    if (userDataBean != null) {
      user.setUser(userDataBean);
      user.setStatus(BulkImportUserData.STATUS_OK);
    } else {
      user.setUser(new CircabcUserDataBean());
      user.setStatus(BulkImportUserData.STATUS_ERROR);
    }
    user.setProfile(model.getProfileId());

    return user;
  }

  /**
   * Parses an uploaded bulk-invite file into a preview list of users, without performing the
   * invitation.
   *
   * <p>The file format is selected from the file-name extension: {@code .xls}/{@code .xlsx} (Excel),
   * {@code .csv} or {@code .xml}. The parsed entries are combined against the target IG's existing
   * members via the bulk import service. The input stream is always closed.</p>
   *
   * @param igId the node id of the target Interest Group
   * @param inputStream the uploaded file content
   * @param fileName the uploaded file name, used to determine the format
   * @return the list of {@link BulkImportUserData} parsed from the file
   * @throws IllegalArgumentException if the file name is {@code null}, has an unsupported extension,
   *     or the file cannot be imported
   */
  @Override
  public List<BulkImportUserData> bulkInviteUsersDigestFile(
    String igId,
    InputStream inputStream,
    String fileName
  ) {
    NodeRef igNodeRef = Converter.createNodeRefFromId(igId);

    List<BulkImportUserData> userData = new ArrayList<>();
    List<BulkImportUserData> loadedUserData;

    try {
      if (fileName == null) {
        throw new IllegalArgumentException("Null file name.");
      } else if (
        fileName.toLowerCase().endsWith(".xls") ||
        fileName.toLowerCase().endsWith(".xlsx")
      ) {
        loadedUserData = loadExcelFile(inputStream, fileName);
      } else if (fileName.toLowerCase().endsWith(".csv")) {
        loadedUserData = loadCSVFile(inputStream, fileName);
      } else if (fileName.toLowerCase().endsWith(".xml")) {
        loadedUserData = loadXMLFile(inputStream, fileName);
      } else {
        throw new IllegalArgumentException(
          "Invalid file extension: " + fileName
        );
      }

      bulkUserImportService.addAll(userData, loadedUserData, igNodeRef);
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Cannot import file: " + fileName + ", " + e.getMessage(),
        e
      );
    } finally {
      if (inputStream != null) {
        IOUtils.closeQuietly(inputStream);
      }
    }

    return userData;
  }

  /**
   * Loads bulk-invite users from an Excel workbook ({@code .xls} or {@code .xlsx}), processing
   * every sheet.
   *
   * @param inputStream the workbook content
   * @param fileName the file name, used to select the HSSF/XSSF implementation
   * @return the list of users parsed from all sheets
   * @throws InvalidBulkImportFileFormatException if a sheet has invalid columns or rows
   * @throws IOException if the workbook cannot be read
   */
  private List<BulkImportUserData> loadExcelFile(
    InputStream inputStream,
    String fileName
  ) throws InvalidBulkImportFileFormatException, IOException {
    List<BulkImportUserData> result = new ArrayList<>();

    Workbook book = null;
    if (fileName.toLowerCase().endsWith(".xls")) {
      book = new HSSFWorkbook(inputStream);
    } else if (fileName.toLowerCase().endsWith(".xlsx")) {
      book = new XSSFWorkbook(inputStream);
    }

    if (book == null || book.getNumberOfSheets() < 1) {
      return result;
    }

    for (int iSheet = 0; iSheet < book.getNumberOfSheets(); iSheet++) {
      processExcelSheet(book.getSheetAt(iSheet), iSheet, fileName, result);
    }

    return result;
  }

  /**
   * Validates a single Excel sheet's header and parses its data rows into user entries.
   *
   * <p>Empty sheets are skipped (with a warning). The header is validated via
   * {@link #validateExcelColumns(Sheet)}; each subsequent non-empty row is converted using
   * {@link #buildBulkImportUserData}.</p>
   *
   * @param sheet the sheet to process
   * @param sheetIndex the zero-based index of the sheet (used for logging)
   * @param fileName the originating file name, recorded on each parsed user
   * @param result the list to which parsed users are appended
   * @throws InvalidBulkImportFileFormatException if the sheet's columns are invalid or a row fails to parse
   */
  private void processExcelSheet(
    Sheet sheet,
    int sheetIndex,
    String fileName,
    List<BulkImportUserData> result
  ) throws InvalidBulkImportFileFormatException {
    Row rTmp = sheet.getRow(0);
    if (rTmp == null) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Sheet number" +
            sheetIndex +
            "of the template file being read has no users to invite. Please check it."
        );
      }
      return;
    }

    int iCell = rTmp.getFirstCellNum();
    validateExcelColumns(sheet);

    try {
      int nbRows = sheet.getPhysicalNumberOfRows();
      for (int i = sheet.getFirstRowNum() + 1; i < nbRows; i++) {
        Row row = sheet.getRow(i);
        if (row == null) {
          continue;
        }
        String username = getCellString(row, iCell);
        String lastname = getCellString(row, iCell + 3);
        String email = getCellString(row, iCell + 4);
        String profile = getCellString(row, iCell + 5);

        BulkImportUserData tmpUser = buildBulkImportUserData(
          fileName,
          username,
          lastname,
          email,
          profile
        );
        if (tmpUser != null) {
          result.add(tmpUser);
        }
      }
    } catch (Exception e) {
      throw new InvalidBulkImportFileFormatException(e.getMessage());
    }
  }

  /**
   * Returns the string value of a cell, or an empty string when the cell is absent.
   *
   * @param row the row to read from
   * @param cellIndex the zero-based cell index
   * @return the cell's string value, or {@code ""} if the cell is {@code null}
   */
  private String getCellString(Row row, int cellIndex) {
    return row.getCell(cellIndex) != null
      ? row.getCell(cellIndex).getStringCellValue()
      : "";
  }

  /**
   * Validates that the header row of an Excel sheet matches the expected {@link #definedColumns}.
   *
   * @param sheet the sheet whose first row is validated
   * @throws InvalidBulkImportFileFormatException if there are fewer than 9 columns, a column is
   *     missing, or a header does not match the expected value
   */
  private void validateExcelColumns(Sheet sheet)
    throws InvalidBulkImportFileFormatException {
    int iRow = sheet.getFirstRowNum();

    Row row = sheet.getRow(iRow);

    int iCellFirst = row.getFirstCellNum();

    if (row.getPhysicalNumberOfCells() >= 9) {
      for (int idx = 0; idx < 9; idx++) {
        if (row.getCell(iCellFirst + idx) != null) {
          if (
            !row
              .getCell(iCellFirst + idx)
              .getStringCellValue()
              .equals(definedColumns.get(idx))
          ) {
            throw new InvalidBulkImportFileFormatException(
              "ERR::NoColumn:" +
                definedColumns.get(idx) +
                "|" +
                idx +
                "|" +
                row.getCell(iCellFirst + idx).getStringCellValue()
            );
            // ERR::NoColumn:<column_name>|<position>|<found>
          }
        } else {
          throw new InvalidBulkImportFileFormatException(
            "ERR::NullColumn:" + idx
          );
        }
      }
    } else {
      throw new InvalidBulkImportFileFormatException(
        "ERR::NumCol<9:" + row.getPhysicalNumberOfCells()
      );
    }
  }

  /**
   * Loads bulk-invite users from an XML file conforming to the {@link BulkImportUsers} schema.
   *
   * <p>The XML reader is configured to disable external entities and DTD support to guard against
   * XXE attacks.</p>
   *
   * @param inputStream the XML file content
   * @param fileName the originating file name, recorded on each parsed user
   * @return the list of users parsed from the file
   * @throws InvalidBulkImportFileFormatException if the XML cannot be parsed or unmarshalled
   */
  private List<BulkImportUserData> loadXMLFile(
    InputStream inputStream,
    String fileName
  ) throws InvalidBulkImportFileFormatException {
    XMLStreamReader xmlStreamReader = null;

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(BulkImportUsers.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
      xmlInputFactory.setProperty(
        XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
        false
      );
      xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);
      BulkImportUsers bulkImportUsers =
        (BulkImportUsers) jaxbUnmarshaller.unmarshal(xmlStreamReader);

      List<BulkImportUserData> result = new ArrayList<>();

      for (BulkImportUser bulkUser : bulkImportUsers.getUserData()) {
        BulkImportUserData tmpUser = buildBulkImportUserData(
          fileName,
          bulkUser.getUserName(),
          bulkUser.getLastName(),
          bulkUser.getEmail(),
          bulkUser.getProfile()
        );
        if (tmpUser != null) {
          result.add(tmpUser);
        }
      }

      return result;
    } catch (Exception e) {
      throw new InvalidBulkImportFileFormatException(e.getMessage());
    } finally {
      if (xmlStreamReader != null) {
        try {
          xmlStreamReader.close();
        } catch (XMLStreamException e) {
          logger.warn(
            "Issue closing the XMLStreamReader while bulk importing users from a XML file.",
            e
          );
        }
      }
    }
  }

  /**
   * Loads bulk-invite users from a comma-separated (CSV) file.
   *
   * <p>The first line is validated as the header against {@link #definedColumns}; subsequent lines
   * are parsed after stripping any double-quote characters.</p>
   *
   * @param inputStream the CSV file content
   * @param fileName the originating file name, recorded on each parsed user
   * @return the list of users parsed from the file
   * @throws InvalidBulkImportFileFormatException if the file is empty, has invalid columns, or fails to parse
   */
  private List<BulkImportUserData> loadCSVFile(
    InputStream inputStream,
    String fileName
  ) throws InvalidBulkImportFileFormatException {
    List<BulkImportUserData> result = new ArrayList<>();

    try (
      BufferedReader bufferedReader = new BufferedReader(
        new InputStreamReader(inputStream)
      )
    ) {
      String line = bufferedReader.readLine();

      if (line != null) {
        String[] elements = line.split(",");
        validateCSVColumns(elements);
      } else {
        throw new InvalidBulkImportFileFormatException(
          "Cannot process null CSV file."
        );
      }

      while ((line = bufferedReader.readLine()) != null) {
        // AMO remove all the " from the line
        line = line.replace("\"", "");

        String[] elements = line.split(",");
        if (elements.length == 0) {
          continue;
        }
        BulkImportUserData tmpUser = buildBulkImportUserData(
          fileName,
          elements[0],
          elements[3],
          elements[4],
          elements[5]
        );
        if (tmpUser != null) {
          result.add(tmpUser);
        }
      }

      return result;
    } catch (Exception e) {
      throw new InvalidBulkImportFileFormatException(e.getMessage());
    }
  }

  /**
   * Validates that the header fields of a CSV file match the expected {@link #definedColumns}
   * (case-insensitive, trimmed).
   *
   * @param elements the header fields split from the first CSV line
   * @throws InvalidBulkImportFileFormatException if there are fewer than 9 fields, a field is
   *     {@code null}, or a header does not match the expected value
   */
  private void validateCSVColumns(String[] elements)
    throws InvalidBulkImportFileFormatException {
    if (elements.length < 9) {
      throw new InvalidBulkImportFileFormatException(
        "ERR::NumCol<9:" + elements.length
      );
    }

    for (int idx = 0; idx < 9; idx++) {
      if (elements[idx] != null) {
        String element = elements[idx].toLowerCase().trim();
        String definedColumn = definedColumns.get(idx);
        if (!element.equals(definedColumn)) {
          throw new InvalidBulkImportFileFormatException(
            "ERR::NoColumn:" +
              definedColumns.get(idx) +
              "|" +
              idx +
              "|" +
              element
          );
          // ERR::NoColumn:<column_name>|<position>|<found>
        }
      } else {
        throw new InvalidBulkImportFileFormatException(
          "ERR::NullColumn:" + idx
        );
      }
    }
  }

  /**
   * Builds a single {@link BulkImportUserData} entry from raw file fields, resolving the user
   * against LDAP.
   *
   * <p>Fully blank rows (empty username, last name and e-mail) are ignored and yield {@code null}.
   * When exactly one LDAP match is found the entry is marked {@link BulkImportUserData#STATUS_OK};
   * otherwise it is populated with the raw values and marked {@link BulkImportUserData#STATUS_IGNORE}.</p>
   *
   * @param fileName the originating file name, recorded on the entry
   * @param username the username/identifier from the file
   * @param lastname the last name from the file
   * @param email the e-mail from the file
   * @param profile the profile name from the file
   * @return the constructed entry, or {@code null} for a blank line
   */
  private BulkImportUserData buildBulkImportUserData(
    String fileName,
    String username,
    String lastname,
    String email,
    String profile
  ) {
    BulkImportUserData tmpUser = new BulkImportUserData();

    tmpUser.setFromFile(fileName);
    tmpUser.setProfile(profile);

    if (
      Objects.equals(username, "") &&
      Objects.equals(lastname, "") &&
      Objects.equals(email, "")
    ) {
      logger.debug("Ignoring blank line in bulk user invitation.");
      return null;
    } else {
      List<String> possibleUsers =
        ldapUserService.getLDAPUserIDByIdMonikerEmailCn(
          username,
          username,
          email,
          lastname,
          true
        );

      if (possibleUsers != null && possibleUsers.size() == 1) {
        tmpUser.setUser(
          ldapUserService.getLDAPUserDataByUid(possibleUsers.get(0))
        );
        tmpUser.setStatus(BulkImportUserData.STATUS_OK);
      } else {
        tmpUser.setUser(new CircabcUserDataBean());
        tmpUser.getUser().setUserName(username);
        tmpUser.getUser().setEcasUserName(username);
        tmpUser.getUser().setEmail(email);
        tmpUser.setStatus(BulkImportUserData.STATUS_IGNORE);
      }
      return tmpUser;
    }
  }

  /**
   * Refreshes a list of users by re-fetching each one's full details by id.
   *
   * <p>Entries with a {@code null} or empty user id are skipped.</p>
   *
   * @param users the users to refresh (only their ids are used)
   * @return the list of freshly loaded {@link User} objects
   */
  @Override
  public List<User> retrieveUserList(List<User> users) {
    List<User> result = new ArrayList<>();
    for (User user : users) {
      if (user.getUserId() != null && !Objects.equals(user.getUserId(), "")) {
        result.add(usersUserIdGet(user.getUserId()));
      }
    }

    return result;
  }

  /**
   * Persists a user's preference configuration as a raw string on their person node.
   *
   * @param username the username whose preference is being saved
   * @param preference the serialized preference configuration to store
   */
  @Override
  public void saveUserPreferenceConfiguration(
    String username,
    String preference
  ) {
    NodeRef personRef = userService.getPerson(username);
    secureNodeService.setProperty(
      personRef,
      UserModel.PROP_PREFERENCE,
      preference
    );
  }

  /**
   * Retrieves and parses a user's stored preference configuration.
   *
   * <p>Returns a default {@link PreferenceConfiguration} when the user has no stored preference.
   * For backward compatibility, the {@code title} and {@code securityRanking} library columns are
   * disabled when absent from the stored value.</p>
   *
   * @param username the username whose preference is requested
   * @return the parsed (or default) {@link PreferenceConfiguration}
   */
  @Override
  public PreferenceConfiguration getUserPreference(String username) {
    NodeRef personRef = userService.getPerson(username);
    PreferenceConfiguration preference = new PreferenceConfiguration();
    if (personRef != null) {
      Serializable preferenceObj = secureNodeService.getProperty(
        personRef,
        UserModel.PROP_PREFERENCE
      );
      if (preferenceObj != null) {
        String preferenceString = preferenceObj.toString();
        preference = UserJsonParser.parsePreference(preferenceString);
        if (!preferenceString.contains(TITLE)) {
          preference.getLibrary().getColumn().setTitle(false);
        }
        if (!preferenceString.contains("securityRanking")) {
          preference.getLibrary().getColumn().setSecurityRanking(false);
        }
      }
    }

    return preference;
  }

  /**
   * Deletes a user completely, running as the admin user.
   *
   * <p>Removes the Alfresco person node (if present), its authentication (if present) and the
   * associated CIRCABC database records ({@code cbc_} tables). Failures are logged and rethrown as
   * an {@link IllegalStateException}.</p>
   *
   * @param userId the username of the user to delete
   * @throws IllegalStateException if the deletion fails
   */
  @Override
  public void usersUserIdDelete(String userId) {
    AuthenticationUtil.runAs(
      () -> {
        try {
          // Delete from Alfresco
          if (personService.personExists(userId)) {
            personService.deletePerson(userId);
          }

          // Only delete authentication if it exists
          if (authenticationService.authenticationExists(userId)) {
            authenticationService.deleteAuthentication(userId);
          }

          // Delete from cbc_ tables
          circabcService.deleteUserFromDatabase(userId);

          if (logger.isInfoEnabled()) {
            logger.info("User deleted successfully: " + userId);
          }
        } catch (Exception e) {
          if (logger.isErrorEnabled()) {
            logger.error("Error deleting user: " + userId, e);
          }
          throw new IllegalStateException(
            "Failed to delete user: " + userId,
            e
          );
        }
        return null;
      },
      AuthenticationUtil.getAdminUserName()
    );
  }
}
