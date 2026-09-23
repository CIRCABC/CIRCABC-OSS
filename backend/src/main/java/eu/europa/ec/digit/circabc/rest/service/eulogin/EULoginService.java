package eu.europa.ec.digit.circabc.rest.service.eulogin;

import jakarta.servlet.http.HttpServletResponse;
import org.apereo.cas.client.validation.TicketValidationException;

/**
 * Service that encapsulates EU Login (ECAS) CAS configuration, ticket validation,
 * user provisioning, and authentication cookie management.
 */
public interface EULoginService {
  /** Returns the base URL of the CAS server (e.g. https://ecas.ec.europa.eu/cas). */
  String getCasBaseUrl();

  /** Returns the service URL that ECAS redirects back to after authentication. */
  String getCasServiceUrl();

  /** Returns the frontend URL to redirect users after successful login. */
  String getFrontendRedirectUrl();

  /** Returns the cookie path to use when setting authentication cookies. */
  String getCookiePath();

  /**
   * Builds the full CAS service URL, optionally appending a route parameter.
   *
   * @param route the frontend route to redirect to after login, or null
   * @return the service URL to pass to CAS
   */
  String buildServiceUrl(String route);

  /**
   * Builds the CAS login redirect URL for unauthenticated users.
   *
   * @param route the frontend route to redirect to after login, or null
   * @return the full ECAS login URL
   */
  String buildLoginRedirectUrl(String route);

  /**
   * Validates a CAS service ticket against ECAS using the laxValidate endpoint.
   *
   * @param ticket the CAS service ticket to validate
   * @param serviceUrl the service URL used during authentication
   * @return the authenticated user details extracted from the CAS assertion
   * @throws TicketValidationException if the ticket is invalid
   */
  EULoginUserDetails validateCasTicket(String ticket, String serviceUrl)
    throws TicketValidationException;

  /**
   * Ensures the user exists in Alfresco. Creates from LDAP if new,
   * or updates from LDAP if existing user data has changed.
   * If LDAP is unavailable for a new user, falls back to the assertion details.
   * Must be called within a transaction as system user.
   *
   * @param userName the ECAS username
   * @param assertionDetails user details from the CAS assertion, used as fallback
   */
  void ensureUserExists(String userName, EULoginUserDetails assertionDetails);

  /**
   * Generates an Alfresco authentication ticket for the given user.
   * Must be called within a transaction as system user.
   *
   * @param userName the username to generate a ticket for
   * @return the Alfresco authentication ticket string
   */
  String generateAlfrescoTicket(String userName);

  /**
   * Sets authentication cookies (username, ticket, route) on the HTTP response.
   *
   * @param response the HTTP servlet response
   * @param username the authenticated username
   * @param alfrescoTicket the Alfresco authentication ticket
   * @param route the frontend route, or null
   */
  void setCookies(
    HttpServletResponse response,
    String username,
    String alfrescoTicket,
    String route
  );

  /**
   * Builds the frontend redirect URL based on the route parameter.
   *
   * @param route the frontend route to redirect to, or null
   * @return the target redirect URL
   */
  String buildRedirectTarget(String route);
}
