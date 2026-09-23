package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.LogRecord;
import io.swagger.model.alfresco.UserModel;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that issues an authentication ticket for a user.
 *
 * <p>As implied by the class name, this handles the HTTP <em>GET</em> request for a
 * user's ticket. The target user is identified by the {@code id} template variable
 * taken from the request URL.</p>
 *
 * <p>When invoked, the endpoint:</p>
 * <ul>
 *   <li>ensures the user exists in the repository, creating them from LDAP data if
 *       necessary or synchronising their existing details from LDAP;</li>
 *   <li>records a "Login" log entry against the CIRCABC root node;</li>
 *   <li>returns the user's current authentication ticket in the response model under
 *       the {@code ticket} key.</li>
 * </ul>
 *
 * <p>On failure it sets an appropriate HTTP status: {@code 403 Forbidden} when access
 * is denied, or {@code 500 Internal Server Error} for any other error.</p>
 */
public class TicketGet extends CircabcDeclarativeWebScript {

  /** Logger used to report errors raised while issuing a ticket. */
  static final Log logger = LogFactory.getLog(TicketGet.class);

  /** Alfresco component used to obtain the user's current authentication ticket. */
  @Autowired
  private TicketComponent ticketComponent;

  /** Alfresco service used to check for and resolve the person/user node. */
  @Autowired
  private PersonService personService;

  /** CIRCABC service handling user creation, updates and LDAP synchronisation. */
  @Autowired
  private UserService userService;

  /** Alfresco service used to read and update node properties (e.g. last login time). */
  @Autowired
  private NodeService nodeService;

  /** CIRCABC API used to resolve well-known nodes such as the guest home and CIRCABC root. */
  @Autowired
  private CircabcApi circabcApi;

  /**
   * Handles the request: ensures the target user exists (creating or updating from LDAP
   * as needed), logs the login, and returns the user's current authentication ticket.
   *
   * @param req the webscript request; the target user is read from the {@code id}
   *            template variable of the matched URL
   * @param status the response status, updated with an error code when the request fails
   * @param cache the response cache directives (unused)
   * @return a model map containing the {@code ticket} entry on success, or {@code null}
   *         when an error occurs (in which case {@code status} carries the error details)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String userName = req.getServiceMatch().getTemplateVars().get("id");

    try {
      ensureUserExists(userName);
      logLogin(userName);
      model.put("ticket", ticketComponent.getCurrentTicket(userName, true));
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e
      );
    }
    return model;
  }

  /**
   * Ensures the given user is present in the repository, creating a new user from LDAP
   * data when absent or synchronising the existing user otherwise.
   *
   * @param userName the user identifier to check
   */
  private void ensureUserExists(String userName) {
    if (!personService.personExists(userName)) {
      createNewUser(userName);
    } else {
      updateExistingUser(userName);
    }
  }

  /**
   * Creates a new repository user from the user's LDAP details, assigning the guest
   * home space as the home folder.
   *
   * @param userName the identifier of the user to create
   */
  private void createNewUser(String userName) {
    CircabcUserDataBean user = new CircabcUserDataBean();
    user.setUserName(userName);
    CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(
      userName
    );
    user.copyLdapProperties(ldapUserDetail);
    user.setHomeSpaceNodeRef(circabcApi.getGuestHomeNodeRef());
    userService.createUser(user, true);
  }

  /**
   * Updates an existing repository user: synchronises their details from LDAP when
   * newer data is available, denies access when authentication is disabled, and records
   * the current time as the last login time.
   *
   * @param userName the identifier of the user to update
   */
  private void updateExistingUser(String userName) {
    NodeRef nodeRef = personService.getPerson(userName);
    CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(
      userName
    );

    if (ldapUserDetail != null) {
      updateUserFromLdapIfNeeded(userName, nodeRef, ldapUserDetail);
    }

    denyIfAuthenticationDisabled(userName);
    nodeService.setProperty(
      nodeRef,
      UserModel.PROP_LAST_LOGIN_TIME,
      new Date()
    );
  }

  /**
   * Copies LDAP properties onto the repository user when the LDAP record has been
   * modified more recently than the stored repository record.
   *
   * @param userName the identifier of the user being synchronised
   * @param nodeRef the person node reference for the user
   * @param ldapUserDetail the user's details as retrieved from LDAP
   */
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
   * Denies access when the user's authentication is currently disabled.
   *
   * <p>A disabled account must never be re-enabled as a side effect of issuing a
   * ticket; instead an {@link AccessDeniedException} is thrown so the endpoint
   * returns HTTP 403 Forbidden.
   *
   * @param userName the identifier of the user whose authentication is checked
   * @throws AccessDeniedException if authentication is disabled for the user
   */
  private void denyIfAuthenticationDisabled(String userName) {
    if (!userService.getAuthenticationEnabled(userName)) {
      throw new AccessDeniedException(
        "Authentication is disabled for user: " + userName
      );
    }
  }

  /**
   * Records a "Login" activity log entry for the user against the CIRCABC root node.
   * Does nothing if the CIRCABC root node cannot be resolved.
   *
   * @param userName the identifier of the user who logged in
   */
  private void logLogin(String userName) {
    NodeRef circabcNodeRef = circabcApi.getCircabcNodeRef();
    if (circabcNodeRef == null) return;

    LogRecord logRecord = new LogRecord();
    logRecord.setService("Directory");
    logRecord.setActivity("Login");
    logRecord.setUser(userName);
    logRecord.setIgID(
      (Long) nodeService.getProperty(
        circabcNodeRef,
        ContentModel.PROP_NODE_DBID
      )
    );
    logRecord.setIgName(
      (String) nodeService.getProperty(circabcNodeRef, ContentModel.PROP_NAME)
    );
    logService.log(logRecord);
  }

  /**
   * Logs the given error and configures the response status to signal the failure to
   * the client.
   *
   * @param status the response status to update
   * @param code the HTTP status code to set (e.g. {@code 403} or {@code 500})
   * @param message the human-readable status message to set
   * @param e the exception that caused the failure, used for logging
   * @return {@code null}, signalling that no response model is produced
   */
  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    if (logger.isErrorEnabled()) {
      logger.error("Error when getting ticket", e);
    }
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
