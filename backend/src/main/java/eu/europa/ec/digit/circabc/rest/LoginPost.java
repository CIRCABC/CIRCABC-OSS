package eu.europa.ec.digit.circabc.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

/**
 * Alfresco webscript endpoint that authenticates a user and issues a session ticket.
 *
 * <p>Handles the {@code POST} login request (as implied by the {@code Post} suffix in the class
 * name). The endpoint expects a JSON request body containing {@code username} and {@code password}
 * fields. On successful authentication it returns a model map holding the {@code username} and the
 * freshly issued authentication {@code ticket}, which is rendered by the associated FreeMarker
 * template.
 *
 * <p>When authentication fails, the response status is set to {@link Status#STATUS_FORBIDDEN}.
 * Disabled accounts are never re-enabled by this endpoint: a disabled account simply fails to
 * authenticate.
 */
public class LoginPost extends CircabcDeclarativeWebScript {

  /** Logger for authentication and request-parsing diagnostics. */
  static final Log logger = LogFactory.getLog(LoginPost.class);

  /** Alfresco authentication service used to verify credentials and manage the login ticket. */
  @Autowired
  @Qualifier("AuthenticationService") // NOSONAR
  private MutableAuthenticationService authenticationService;

  /**
   * Authenticates the user from the JSON POST body and builds the response model.
   *
   * <p>Parses the {@code username}/{@code password} credentials and attempts to log in. When login
   * succeeds the returned model contains the {@code username} and the issued {@code ticket};
   * otherwise the status is set to {@link Status#STATUS_FORBIDDEN} and {@code null} is returned.
   * Disabled accounts are never re-enabled: authentication simply fails for them.
   *
   * @param req the incoming web script request carrying the JSON credentials body
   * @param status the response status, updated to {@code FORBIDDEN} when authentication fails
   * @param cache the response cache directives (unused)
   * @return a model map with {@code username} and {@code ticket} on success, or {@code null} on
   *     authentication failure
   * @throws WebScriptException if the request body is missing, cannot be read, or is not valid JSON
   */
  @Override
  public Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    String[] credentials = parseCredentials(req);
    String username = credentials[0];
    String password = credentials[1];

    Map<String, Object> model = login(username, password);

    if (model == null || model.isEmpty()) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Login failed");
      status.setRedirect(true);
      return null; // NOSONAR
    } else {
      return model;
    }
  }

  /**
   * Extracts and validates the login credentials from the request's JSON body.
   *
   * @param req the web script request whose content holds the JSON credentials
   * @return a two-element array with the {@code username} at index 0 and {@code password} at index 1
   * @throws WebScriptException with {@code BAD_REQUEST} if the body is missing, unparseable, or a
   *     required field is absent, or {@code INTERNAL_SERVER_ERROR} if the body cannot be read
   */
  private String[] parseCredentials(WebScriptRequest req) {
    try {
      Content c = req.getContent();
      if (c == null) {
        throw new WebScriptException(
          Status.STATUS_BAD_REQUEST,
          "Missing POST body."
        );
      }

      JSONObject json = new JSONObject(c.getContent());
      String username = json.getString("username");
      String password = json.getString("password");
      if (username == null || username.isEmpty()) {
        throw new WebScriptException(
          Status.STATUS_BAD_REQUEST,
          "Username not specified"
        );
      }

      if (password == null) {
        throw new WebScriptException(
          Status.STATUS_BAD_REQUEST,
          "Password not specified"
        );
      }
      return new String[] { username, password };
    } catch (JSONException jErr) {
      if (logger.isErrorEnabled()) {
        logger.error("Unable to parse JSON POST body", jErr);
      }
      throw new WebScriptException(
        Status.STATUS_BAD_REQUEST,
        "Unable to parse JSON POST body: " + jErr.getMessage()
      );
    } catch (IOException ioErr) {
      if (logger.isErrorEnabled()) {
        logger.error("Unable to retrieve POST body", ioErr);
      }
      throw new WebScriptException(
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Unable to retrieve POST body: " + ioErr.getMessage()
      );
    }
  }

  /**
   * Attempts to authenticate the given credentials and build the login model.
   *
   * <p>The current security context is always cleared afterwards so that no ticket leaks into the
   * calling thread.
   *
   * @param username the user name to authenticate
   * @param password the plain-text password to verify
   * @return a model map containing the {@code username} and the issued {@code ticket}, or
   *     {@code null} if authentication fails
   */
  private Map<String, Object> login(String username, String password) {
    try {
      // get ticket
      authenticationService.authenticate(username, password.toCharArray());

      // add ticket to model for javascript and template access
      Map<String, Object> model = new HashMap<>(7, 1.0f);
      model.put("username", username);
      model.put("ticket", authenticationService.getCurrentTicket());

      return model;
    } catch (AuthenticationException e) {
      logger.error("Authentication failed", e);
      return null; // NOSONAR
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }
}
