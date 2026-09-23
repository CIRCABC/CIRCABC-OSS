package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.GroupsApi;
import io.swagger.model.db.ActivityCountDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that serves summary information for an Interest
 * Group (IG).
 *
 * <p>Implied HTTP method: <b>GET</b> (class name suffix {@code Get}). A single
 * endpoint implementation backs three related read-only resources, and the
 * concrete piece of data returned is selected from the request service path:
 *
 * <ul>
 *   <li>a path ending in {@code statistics} returns the IG summary statistics
 *       under the {@code statistics} model key;</li>
 *   <li>a path ending in {@code timeline} returns the IG activity timeline
 *       (a list of {@link ActivityCountDAO}) under the {@code timeline} model
 *       key;</li>
 *   <li>a path ending in {@code structure} returns the IG structure under the
 *       {@code structure} model key.</li>
 * </ul>
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code id} template variable &mdash; the identifier of the target
 *       Interest Group;</li>
 *   <li>{@code calculate} request parameter &mdash; when {@code "true"}, forces
 *       recomputation of the statistics rather than using cached values (only
 *       relevant for the statistics resource).</li>
 * </ul>
 *
 * <p>Access is restricted to group administrators: an
 * {@link AccessDeniedException} is raised (and mapped to HTTP 403) when the
 * current user is not an administrator of the requested group. An invalid group
 * reference is mapped to HTTP 400.
 */
public class GroupsSummaryGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsSummaryGet.class);

  /**
   * Business service exposing Interest Group summary operations (statistics,
   * timeline and structure).
   */
  @Autowired
  private GroupsApi groupsApi;

  /**
   * Service used to verify that the current user has the required
   * administrative rights on the target group.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Builds the response model for the requested Interest Group summary resource.
   *
   * <p>Reads the {@code id} template variable and the optional {@code calculate}
   * request parameter, verifies that the current user is an administrator of the
   * group, and then populates the model with statistics, timeline or structure
   * data depending on the suffix of the request service path. Multilingual
   * property awareness is enabled for the duration of the call and restored to
   * its previous state afterwards.
   *
   * @param req the web script request; provides the service path, the
   *     {@code id} template variable and the {@code calculate} parameter
   * @param status the response status object, updated to 400 (bad request) or
   *     403 (forbidden) on error
   * @param cache the response cache control object
   * @return a map holding the requested summary data under the
   *     {@code statistics}, {@code timeline} and/or {@code structure} keys, or
   *     {@code null} when the request is rejected (invalid node reference or
   *     access denied) and a redirect status is set instead
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    String servicePath = req.getServicePath();

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    try {
      MLPropertyInterceptor.setMLAware(true);

      String id = templateVars.get("id");
      boolean calculate = "true".equals(req.getParameter("calculate"));

      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "Not enough rights for accessing group statistics"
        );
      }

      if (servicePath.endsWith("statistics")) {
        model.put(
          "statistics",
          this.groupsApi.getIGSummaryStatistics(id, calculate, false)
        );
      }
      if (servicePath.endsWith("timeline")) {
        List<ActivityCountDAO> list = this.groupsApi.getIGSummaryTimeline(id);
        model.put("timeline", list);
      }
      if (servicePath.endsWith("structure")) {
        model.put("structure", this.groupsApi.getIGSummaryStructure(id));
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
