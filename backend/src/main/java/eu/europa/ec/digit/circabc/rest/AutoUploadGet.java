package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AutoUploadApi;
import io.swagger.model.Configuration;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
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
 * Alfresco webscript endpoint that retrieves the auto-upload configuration for a
 * node within an Interest Group (IG).
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this handles
 * the HTTP {@code GET} method. Given an IG identifier (from the URL template variable
 * {@code id}) and a {@code nodeId} request parameter, it returns the auto-upload
 * {@link Configuration} associated with the node, if any.</p>
 *
 * <p>Access is restricted to group administrators of the target IG; a non-admin caller
 * results in an HTTP {@code 403 Forbidden} response. When a configuration exists, the
 * endpoint also derives the day and hour scheduling choices from the configuration's
 * cron-style date restriction and exposes them in the response model.</p>
 *
 * <p>The resulting model exposes the following keys:</p>
 * <ul>
 *   <li>{@code autoupload} &ndash; the {@link Configuration} for the node (may be {@code null});</li>
 *   <li>{@code dayChoice} &ndash; the day frequency parsed from the cron expression (present only when a configuration exists);</li>
 *   <li>{@code hourChoice} &ndash; the hour frequency parsed from the cron expression (present only when a configuration exists).</li>
 * </ul>
 */
public class AutoUploadGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AutoUploadGet.class);

  /**
   * API providing access to the auto-upload configuration business logic.
   */
  @Autowired
  private AutoUploadApi autoUploadApi;

  /**
   * Service used to verify that the current user is an administrator of the target IG.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Extracts the day frequency from a cron-style date restriction expression.
   *
   * <p>The 6th field (index 5) of the space-separated cron expression is interpreted
   * as the day component. A wildcard ({@code *}) is mapped to {@code -1} to indicate
   * that no specific day is set.</p>
   *
   * @param dateRestriction the space-separated cron expression to parse
   * @return the day value from the cron expression, or {@code -1} if the day field is a wildcard
   * @throws NumberFormatException if the day field is neither a wildcard nor a valid integer
   */
  private static int retrieveDayFrequencyFromCron(String dateRestriction) {
    String[] strings = dateRestriction.split(" ");
    int result;

    if (strings[5].equals("*")) {
      result = -1;
    } else {
      result = Integer.parseInt(strings[5]);
    }

    return result;
  }

  /**
   * Extracts the hour frequency from a cron-style date restriction expression.
   *
   * <p>The 3rd field (index 2) of the space-separated cron expression is interpreted
   * as the hour component. A wildcard ({@code *}) is mapped to {@code -1} to indicate
   * that no specific hour is set.</p>
   *
   * @param dateRestriction the space-separated cron expression to parse
   * @return the hour value from the cron expression, or {@code -1} if the hour field is a wildcard
   * @throws NumberFormatException if the hour field is neither a wildcard nor a valid integer
   */
  private static int retrieveHourFrequencyFromCron(String dateRestriction) {
    String[] strings = dateRestriction.split(" ");
    int result;

    if (strings[2].equals("*")) {
      result = -1;
    } else {
      result = Integer.parseInt(strings[2]);
    }

    return result;
  }

  /**
   * Handles the webscript request by resolving and returning the auto-upload
   * configuration for the requested node.
   *
   * <p>The IG identifier is read from the {@code id} URL template variable and the
   * target node from the {@code nodeId} request parameter. The caller must be a group
   * administrator of the IG. Multilingual property interception is temporarily disabled
   * while the configuration is resolved and is restored afterwards.</p>
   *
   * <p>On error the method sets the appropriate HTTP status on {@code status}, flags the
   * response as a redirect and returns {@code null}:</p>
   * <ul>
   *   <li>{@code 400 Bad Request} for an invalid node reference;</li>
   *   <li>{@code 403 Forbidden} when the caller is not a group administrator;</li>
   *   <li>{@code 500 Internal Server Error} for any other unexpected failure.</li>
   * </ul>
   *
   * @param req the webscript request, providing the {@code id} template variable and {@code nodeId} parameter
   * @param status the response status used to signal errors to the framework
   * @param cache the cache directives for the response
   * @return the model map containing the auto-upload configuration and derived scheduling
   *         choices, or {@code null} if an error occurred
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    MLPropertyInterceptor.setMLAware(false);

    try {
      Map<String, String> templateVars = req
        .getServiceMatch()
        .getTemplateVars();
      String igId = templateVars.get("id");

      if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
        throw new AccessDeniedException("No access on IG: " + igId);
      }

      String nodeId = req.getParameter("nodeId");

      Configuration configuration = this.autoUploadApi.getAutoUploadEntry(
        igId,
        nodeId
      );

      model.put("autoupload", configuration);

      if (configuration != null) {
        model.put(
          "dayChoice",
          retrieveDayFrequencyFromCron(configuration.getDateRestriction())
        );
        model.put(
          "hourChoice",
          retrieveHourFrequencyFromCron(configuration.getDateRestriction())
        );
      }
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference while processing auto upload request",
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied while processing auto upload request", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error while processing auto upload request", e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
