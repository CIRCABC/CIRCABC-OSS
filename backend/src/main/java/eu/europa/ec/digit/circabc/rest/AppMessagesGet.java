package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script backing the HTTP {@code GET} endpoint that
 * retrieves the application-wide messages (e.g. banners or system-wide
 * notices) exposed by CIRCABC.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this
 * endpoint responds to {@code GET} requests and takes no request parameters or
 * body. It delegates the lookup to {@link AppMessageApi#getAppMessages()} and
 * places the resulting collection under the {@code messages} key of the model
 * map, which the associated FreeMarker template renders as JSON.</p>
 *
 * <p>Error handling maps failures to appropriate HTTP statuses: an
 * {@link AccessDeniedException} results in {@code 403 Forbidden} while any other
 * exception results in {@code 500 Internal Server Error}.</p>
 *
 * @author beaurpi
 */
public class AppMessagesGet extends DeclarativeWebScript {

  /**
   * Logger used to report access-denied and unexpected errors raised while
   * retrieving the application messages.
   */
  static final Log logger = LogFactory.getLog(AppMessagesGet.class);

  /**
   * API used to fetch the application messages; injected by Spring.
   */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Handles the {@code GET} request by retrieving the application messages and
   * exposing them to the response template.
   *
   * <p>On success the returned model contains a single {@code messages} entry
   * holding the collection returned by {@link AppMessageApi#getAppMessages()}.
   * If access is denied the status is set to {@code 403} and {@code null} is
   * returned; on any other error the status is set to {@code 500} and
   * {@code null} is returned.</p>
   *
   * @param req the web script request (no parameters are read from it)
   * @param status the response status, updated on error to signal a redirect
   *     with the appropriate HTTP code and message
   * @param cache the response cache directives (unused)
   * @return a model map with the {@code messages} entry on success, or
   *     {@code null} when an error occurs and the status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      model.put("messages", appMessageApi.getAppMessages());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied while getting app messages", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error while getting app messages", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
