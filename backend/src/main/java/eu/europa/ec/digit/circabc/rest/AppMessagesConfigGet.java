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
 * Read-only Alfresco web script endpoint that exposes the global application
 * message display configuration.
 *
 * <p>Backing an HTTP {@code GET} request (as implied by the {@code Get} suffix
 * in the class name), this endpoint retrieves the current
 * {@link io.swagger.model.DisplayConfiguration} that indicates whether the
 * previous/old application message should be displayed to users. The
 * configuration is obtained from {@link AppMessageApi#getDisplayOldMessage()}
 * and placed in the response model under the {@code "config"} key, from which
 * the associated FreeMarker template renders the JSON response.</p>
 *
 * <p>The endpoint takes no request parameters. On failure it does not populate
 * the model but instead sets an appropriate HTTP status: {@code 403 Forbidden}
 * when access is denied, or {@code 500 Internal Server Error} for any other
 * unexpected error.</p>
 */
public class AppMessagesConfigGet extends DeclarativeWebScript {

  /** Logger used to report access-denied and internal errors for this endpoint. */
  static final Log logger = LogFactory.getLog(AppMessagesConfigGet.class);

  /** API providing access to the application message display configuration. */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Handles the web script request and builds the response model containing the
   * application message display configuration.
   *
   * @param req the incoming web script request (no parameters are read)
   * @param status the response status; set to {@code 403} on access denial or
   *     {@code 500} on any other error, with the redirect flag enabled so the
   *     framework renders the corresponding status template
   * @param cache the response cache directives (unused)
   * @return a model map containing the {@code "config"} entry with the current
   *     {@link io.swagger.model.DisplayConfiguration}, or {@code null} if an
   *     error occurred and an error status was set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      model.put("config", appMessageApi.getDisplayOldMessage());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting app messages config", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error getting app messages config", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
