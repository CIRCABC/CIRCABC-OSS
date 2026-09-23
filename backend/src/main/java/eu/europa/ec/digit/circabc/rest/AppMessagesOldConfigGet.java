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
 * Read-only Alfresco Web Script endpoint that returns the current configuration flag
 * indicating whether the legacy ("old") application message feature is enabled.
 *
 * <p>As implied by the {@code Get} suffix in the class name, this endpoint handles an
 * HTTP {@code GET} request. It takes no request parameters and delegates to
 * {@link io.swagger.api.AppMessageApi#getEnableOldMessage()} to resolve the flag, which
 * is exposed to the response template under the {@code config} model key.</p>
 *
 * <p>Error handling: an {@link org.alfresco.repo.security.permissions.AccessDeniedException}
 * results in an HTTP {@code 403 Forbidden} status, while any other failure results in an
 * HTTP {@code 500 Internal Server Error} status.</p>
 */
public class AppMessagesOldConfigGet extends DeclarativeWebScript {

  /** Logger used to report access-denied and unexpected errors during execution. */
  static final Log logger = LogFactory.getLog(AppMessagesOldConfigGet.class);

  /** API used to retrieve the "old message" enablement configuration flag. */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Handles the GET request by retrieving the "old message" enablement flag and placing it
   * in the response model under the {@code config} key.
   *
   * @param req the incoming web script request (no parameters are consumed)
   * @param status the response status, updated to {@code 403} on access denial or
   *     {@code 500} on any other error
   * @param cache the response cache directives
   * @return a model map containing the {@code config} flag on success, or {@code null} when
   *     an error occurs and a corresponding error status/redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      model.put("config", appMessageApi.getEnableOldMessage());
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting old message config", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error getting old message config", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
