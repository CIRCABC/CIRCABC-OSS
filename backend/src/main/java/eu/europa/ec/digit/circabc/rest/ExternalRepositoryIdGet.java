package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.AresBridgeApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that handles the HTTP {@code GET} request for retrieving the
 * external repositories (via the ARES bridge) associated with a given interest group.
 *
 * <p>The interest group identifier is supplied as the {@code id} path template variable. Before
 * returning any data, the endpoint verifies that the current user is an administrator of that
 * group: if not, it responds with HTTP {@code 403 Forbidden}. When authorized, it delegates to
 * {@link AresBridgeApi#getExternalRepositories(String)} and exposes the result under the
 * {@code repos} key of the model consumed by the associated FreeMarker template. Any failure while
 * fetching the repositories is logged and reported as HTTP {@code 400 Bad Request}.
 */
public class ExternalRepositoryIdGet extends DeclarativeWebScript {

  /** Logger used to record errors raised while resolving the external repositories. */
  static final Log logger = LogFactory.getLog(ExternalRepositoryIdGet.class);

  /** API used to bridge to ARES and fetch the external repositories for an interest group. */
  @Autowired
  private AresBridgeApi aresBridgeApi;

  /** Service used to verify that the current user is an administrator of the target group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming GET request. Reads the {@code id} path variable identifying the interest
   * group, checks that the current user is a group administrator, and, when authorized, populates
   * the model with the group's external repositories under the {@code repos} key.
   *
   * @param req the web script request; must expose an {@code id} template variable identifying the
   *     interest group
   * @param status the response status; set to {@code 403} when the user is not a group admin or to
   *     {@code 400} when fetching the repositories fails
   * @param cache the cache directives for the response
   * @return the model containing the {@code repos} entry on success, or {@code null} when the
   *     request is forbidden or fails
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    if (!currentUserPermissionCheckerService.isGroupAdmin(id)) {
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    try {
      model.put("repos", this.aresBridgeApi.getExternalRepositories(id));
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
