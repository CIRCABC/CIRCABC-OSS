package eu.europa.ec.digit.circabc.rest.service.eulogin;

import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.alfresco.UserModel;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.cas.client.validation.Assertion;
import org.apereo.cas.client.validation.Cas20ServiceTicketValidator;
import org.apereo.cas.client.validation.TicketValidationException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link EULoginService} that holds CAS configuration
 * and encapsulates all EU Login authentication business logic.
 *
 * <p>Properties are resolved via {@link CircabcConfig} which reads from
 * Alfresco's {@code global-properties} bean, ensuring environment variable
 * overrides in {@code alfresco-global.properties} are correctly applied.
 */
public class EULoginServiceImpl implements EULoginService {

  /** Logger for authentication events and diagnostic details. */
  private static final Log logger = LogFactory.getLog(EULoginServiceImpl.class);

  /**
   * Provides CAS/EU Login configuration values (base URL, service URL,
   * frontend redirect URL, cookie path) sourced from Alfresco global
   * properties.
   */
  @Autowired
  private CircabcConfig circabcConfig;

  /** Alfresco component used to issue authentication tickets for a user. */
  @Autowired
  private TicketComponent ticketComponent;

  /** Alfresco service used to check for and resolve person nodes. */
  @Autowired
  private PersonService personService;

  /** CIRCABC user service used to create, update and look up user data. */
  @Autowired
  private UserService userService;

  /** Alfresco service used to read and write node properties (e.g. last login time). */
  @Autowired
  private NodeService nodeService;

  /** CIRCABC API facade used here to resolve the guest home node reference. */
  @Autowired
  private CircabcApi circabcApi;

  /**
   * Returns the base URL of the CAS / EU Login server.
   *
   * @return the configured CAS base URL
   */
  @Override
  public String getCasBaseUrl() {
    return circabcConfig.getCasBaseUrl();
  }

  /**
   * Returns the service URL registered with CAS that the ticket is validated
   * against.
   *
   * @return the configured CAS service URL
   */
  @Override
  public String getCasServiceUrl() {
    return circabcConfig.getCasServiceUrl();
  }

  /**
   * Returns the frontend URL the user is redirected to after a successful
   * login.
   *
   * @return the configured frontend redirect URL
   */
  @Override
  public String getFrontendRedirectUrl() {
    return circabcConfig.getCasFrontendRedirectUrl();
  }

  /**
   * Returns the path scope applied to the authentication cookies.
   *
   * @return the configured cookie path
   */
  @Override
  public String getCookiePath() {
    return circabcConfig.getCasCookiePath();
  }

  /**
   * Builds the CAS service URL, optionally appending the original frontend
   * route as a URL-encoded {@code route} query parameter so it can be restored
   * after login.
   *
   * @param route the frontend route to preserve across the login flow; may be
   *              {@code null} or empty
   * @return the service URL, with the encoded {@code route} parameter appended
   *         when a non-empty route is supplied
   */
  @Override
  public String buildServiceUrl(String route) {
    String serviceUrl = getCasServiceUrl();
    if (route != null && !route.isEmpty()) {
      return (
        serviceUrl +
        "?route=" +
        URLEncoder.encode(route, StandardCharsets.UTF_8)
      );
    }
    return serviceUrl;
  }

  /**
   * Builds the full CAS login redirect URL, wrapping the (route-aware) service
   * URL as the URL-encoded {@code service} parameter of the CAS
   * {@code /login} endpoint.
   *
   * @param route the frontend route to preserve across the login flow; may be
   *              {@code null} or empty
   * @return the absolute CAS login URL the browser should be redirected to
   */
  @Override
  public String buildLoginRedirectUrl(String route) {
    String serviceUrl = buildServiceUrl(route);
    return (
      getCasBaseUrl() +
      "/login?service=" +
      URLEncoder.encode(serviceUrl, StandardCharsets.UTF_8)
    );
  }

  /**
   * Validates a CAS service ticket against EU Login and extracts the
   * authenticated user's details.
   *
   * <p>Because CAS service tickets are single-use, this method captures the raw
   * XML validation response and parses the EU Login/ECAS specific attributes
   * (email, first/last name, domain, department, phone, locale, org id,
   * moniker) directly, since these are returned as direct children of
   * {@code <cas:authenticationSuccess>} rather than inside the standard
   * {@code <cas:attributes>} block understood by the default parser.
   *
   * @param ticket     the CAS service ticket to validate
   * @param serviceUrl the service URL the ticket was issued for; must match the
   *                   URL used when the ticket was requested
   * @return the validated user's details, populated with the username and any
   *         available EU Login attributes
   * @throws TicketValidationException if the ticket is invalid, expired or
   *                                   cannot be validated
   */
  @Override
  public EULoginUserDetails validateCasTicket(String ticket, String serviceUrl)
    throws TicketValidationException {
    // Holder to capture the raw XML response during CAS validation.
    // CAS tickets are single-use so we cannot call the endpoint twice.
    final String[] rawResponseHolder = new String[1];

    Cas20ServiceTicketValidator validator = new Cas20ServiceTicketValidator(
      getCasBaseUrl()
    ) {
      @Override
      protected String getUrlSuffix() {
        return "laxValidate";
      }

      @Override
      protected Assertion parseResponseFromServer(String response)
        throws TicketValidationException {
        rawResponseHolder[0] = response;
        return super.parseResponseFromServer(response);
      }
    };

    validator.setCustomParameters(Map.of("userDetails", "true"));

    Assertion assertion = validator.validate(ticket, serviceUrl);
    String username = assertion.getPrincipal().getName();
    String rawResponse = rawResponseHolder[0];

    // The standard Apereo CAS 2.0 parser only extracts attributes from a
    // <cas:attributes> wrapper block. ECAS/EU Login returns user details as
    // direct child elements of <cas:authenticationSuccess> (e.g.
    // <cas:email>value</cas:email>). We parse the captured raw XML to extract them.
    EULoginUserDetails details = new EULoginUserDetails(username);
    if (rawResponse != null) {
      details.setEmail(extractXmlElement(rawResponse, "cas:email"));
      details.setFirstName(extractXmlElement(rawResponse, "cas:firstName"));
      details.setLastName(extractXmlElement(rawResponse, "cas:lastName"));
      details.setDomain(extractXmlElement(rawResponse, "cas:domain"));
      details.setDepartmentNumber(
        extractXmlElement(rawResponse, "cas:departmentNumber")
      );
      details.setTelephoneNumber(
        extractXmlElement(rawResponse, "cas:telephoneNumber")
      );
      details.setLocale(extractXmlElement(rawResponse, "cas:locale"));
      details.setOrgId(extractXmlElement(rawResponse, "cas:orgId"));
      details.setMoniker(extractXmlElement(rawResponse, "cas:moniker"));
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
        "ECAS user details for " +
          username +
          ": email=" +
          details.getEmail() +
          ", firstName=" +
          details.getFirstName() +
          ", lastName=" +
          details.getLastName() +
          ", domain=" +
          details.getDomain()
      );
    }

    return details;
  }

  /**
   * Extracts the text content of the first occurrence of an XML element from a raw XML string.
   * For example, extractXmlElement(xml, "cas:email") on
   * {@code <cas:email>user@example.com</cas:email>} returns "user@example.com".
   */
  private String extractXmlElement(String xml, String elementName) {
    if (xml == null || elementName == null) {
      return null;
    }
    // Match <elementName>content</elementName> - content must not contain '<'
    Pattern pattern = Pattern.compile(
      "<" +
        Pattern.quote(elementName) +
        ">([^<]*)</" +
        Pattern.quote(elementName) +
        ">"
    );
    Matcher matcher = pattern.matcher(xml);
    if (matcher.find()) {
      String value = matcher.group(1).trim();
      return value.isEmpty() ? null : value;
    }
    return null;
  }

  /**
   * Ensures a CIRCABC user account exists for the authenticated EU Login user,
   * creating a new account if none exists or updating the existing one
   * otherwise.
   *
   * @param userName         the authenticated user's login name
   * @param assertionDetails the user details extracted from the CAS assertion,
   *                         used as a fallback when LDAP data is unavailable;
   *                         may be {@code null}
   */
  @Override
  public void ensureUserExists(
    String userName,
    EULoginUserDetails assertionDetails
  ) {
    if (!personService.personExists(userName)) {
      createNewUser(userName, assertionDetails);
    } else {
      updateExistingUser(userName, assertionDetails);
    }
  }

  /**
   * Generates (or retrieves the current) Alfresco authentication ticket for the
   * given user.
   *
   * @param userName the user to issue a ticket for
   * @return the Alfresco authentication ticket
   */
  @Override
  public String generateAlfrescoTicket(String userName) {
    return ticketComponent.getCurrentTicket(userName, true);
  }

  /**
   * Sets the authentication cookies ({@code username}, {@code ticket} and
   * {@code route}) on the HTTP response so the frontend can resume the session.
   *
   * @param response       the HTTP response to add cookies to
   * @param username       the authenticated user's login name
   * @param alfrescoTicket the Alfresco authentication ticket
   * @param route          the frontend route to restore; defaults to {@code "/"}
   *                       when {@code null}
   */
  @Override
  public void setCookies(
    HttpServletResponse response,
    String username,
    String alfrescoTicket,
    String route
  ) {
    setCookie(response, "username", username);
    setCookie(response, "ticket", alfrescoTicket);
    setCookie(response, "route", route != null ? route : "/");
  }

  /**
   * Builds the final frontend redirect target after login, appending the
   * original route (replacing the default {@code /ui/welcome} suffix) when a
   * specific non-root route was requested.
   *
   * @param route the frontend route to redirect to; may be {@code null}, empty
   *              or {@code "/"} to use the default redirect URL
   * @return the absolute frontend URL to redirect the browser to
   */
  @Override
  public String buildRedirectTarget(String route) {
    String redirectUrl = getFrontendRedirectUrl();
    if (route != null && !route.isEmpty() && !route.equals("/")) {
      return redirectUrl.replaceAll("/ui/welcome$", "") + route;
    }
    return redirectUrl;
  }

  private void createNewUser(
    String userName,
    EULoginUserDetails assertionDetails
  ) {
    CircabcUserDataBean user = new CircabcUserDataBean();
    user.setUserName(userName);
    CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(
      userName
    );

    if (ldapUserDetail != null) {
      user.copyLdapProperties(ldapUserDetail);
    } else {
      logger.warn(
        "LDAP lookup returned null for user " +
          userName +
          ", using ECAS assertion attributes"
      );
      populateFromAssertionDetails(user, assertionDetails);
    }

    user.setHomeSpaceNodeRef(circabcApi.getGuestHomeNodeRef());
    user.setCompanyId("");
    user.setURL("");
    user.setVisibility(Boolean.FALSE);
    user.setGlobalNotification(Boolean.TRUE);
    user.setLastLoginTime(new Date());
    user.setLastModificationDetailsTime(new Date());
    user.setCreationDate(new Date());

    userService.createUser(user, true);

    if (logger.isInfoEnabled()) {
      logger.info("Created new user from ECAS login: " + userName);
    }
  }

  private void populateFromAssertionDetails(
    CircabcUserDataBean user,
    EULoginUserDetails details
  ) {
    if (details == null) {
      return;
    }
    if (details.getEmail() != null) {
      user.setEmail(details.getEmail());
    }
    if (details.getFirstName() != null) {
      user.setFirstName(details.getFirstName());
    }
    if (details.getLastName() != null) {
      user.setLastName(details.getLastName());
    }
    if (details.getDomain() != null) {
      user.setDomain(details.getDomain());
    }
    if (details.getDepartmentNumber() != null) {
      user.setOrgdepnumber(details.getDepartmentNumber());
    }
    if (details.getTelephoneNumber() != null) {
      user.setPhone(details.getTelephoneNumber());
    }
    if (details.getMoniker() != null) {
      user.setEcasUserName(details.getMoniker());
    }
  }

  private void updateExistingUser(
    String userName,
    EULoginUserDetails assertionDetails
  ) {
    NodeRef nodeRef = personService.getPerson(userName);
    CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(
      userName
    );

    if (ldapUserDetail != null) {
      updateUserFromLdapIfNeeded(userName, nodeRef, ldapUserDetail);
    } else if (assertionDetails != null) {
      // No LDAP available — update from ECAS assertion if details are missing
      updateUserFromAssertionIfNeeded(userName, assertionDetails);
    }

    if (!userService.getAuthenticationEnabled(userName)) {
      throw new AuthenticationException(
        "Authentication is disabled for user: " + userName
      );
    }

    nodeService.setProperty(
      nodeRef,
      UserModel.PROP_LAST_LOGIN_TIME,
      new Date()
    );
  }

  private void updateUserFromLdapIfNeeded(
    String userName,
    NodeRef nodeRef,
    CircabcUserDataBean ldapUserDetail
  ) {
    Date ldapTime = ldapUserDetail.getLastModificationDetailsTime();
    if (ldapTime == null) return;

    Date repoTime = (Date) nodeService.getProperty(
      nodeRef,
      UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME
    );
    if (repoTime == null || ldapTime.after(repoTime)) {
      CircabcUserDataBean repoUser = userService.getCircabcUserDataBean(
        userName
      );
      repoUser.copyLdapProperties(ldapUserDetail);
      userService.updateUser(repoUser);
    }
  }

  /**
   * Updates user properties from ECAS assertion details if key fields
   * (firstName, lastName, email) are missing in the repository.
   */
  private void updateUserFromAssertionIfNeeded(
    String userName,
    EULoginUserDetails assertionDetails
  ) {
    CircabcUserDataBean repoUser = userService.getCircabcUserDataBean(userName);
    boolean needsUpdate = false;

    needsUpdate |= applyIfBlank(
      repoUser.getFirstName(),
      assertionDetails.getFirstName(),
      repoUser::setFirstName
    );
    needsUpdate |= applyIfBlank(
      repoUser.getLastName(),
      assertionDetails.getLastName(),
      repoUser::setLastName
    );
    needsUpdate |= applyIfBlank(
      repoUser.getEmail(),
      assertionDetails.getEmail(),
      repoUser::setEmail
    );
    needsUpdate |= applyIfBlank(
      repoUser.getDomain(),
      assertionDetails.getDomain(),
      repoUser::setDomain
    );
    needsUpdate |= applyIfBlank(
      repoUser.getOrgdepnumber(),
      assertionDetails.getDepartmentNumber(),
      repoUser::setOrgdepnumber
    );
    needsUpdate |= applyIfBlank(
      repoUser.getPhone(),
      assertionDetails.getTelephoneNumber(),
      repoUser::setPhone
    );
    needsUpdate |= applyIfBlank(
      repoUser.getEcasUserName(),
      assertionDetails.getMoniker(),
      repoUser::setEcasUserName
    );

    if (needsUpdate) {
      userService.updateUser(repoUser);
      if (logger.isInfoEnabled()) {
        logger.info(
          "Updated user " + userName + " with ECAS assertion details"
        );
      }
    }
  }

  /**
   * Applies {@code incoming} through {@code setter} when the current repository value
   * is blank and an incoming value is present.
   *
   * @param current the current repository value
   * @param incoming the value from the ECAS assertion
   * @param setter setter used to apply the incoming value
   * @return {@code true} if a value was applied (i.e. an update is needed)
   */
  private boolean applyIfBlank(
    String current,
    String incoming,
    java.util.function.Consumer<String> setter
  ) {
    if (isBlank(current) && incoming != null) {
      setter.accept(incoming);
      return true;
    }
    return false;
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  @SuppressWarnings({ "java:S3330", "java:S2092" })
  // HttpOnly=false: the frontend JS must read these cookies.
  // secure=false: TLS is terminated at the reverse proxy; revisit if that changes.
  private void setCookie(
    HttpServletResponse response,
    String name,
    String value
  ) {
    String path = getCookiePath();
    Cookie cookie = new Cookie(name, value);
    cookie.setPath(path != null ? path : "/");
    cookie.setHttpOnly(false); // Frontend needs to read these cookies
    cookie.setSecure(false); // Set to true when deployed behind HTTPS
    response.addCookie(cookie);
  }
}
