package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AutoUploadApi;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Web Script endpoint that deletes an existing auto-upload
 * configuration entry for an Interest Group (IG).
 *
 * <p>The {@code Delete} suffix in the class name maps this endpoint to the HTTP
 * {@code DELETE} method. The IG identifier is taken from the {@code id} URL
 * template variable and the configuration to remove is identified by the
 * {@code configurationId} request parameter.
 *
 * <p>Only group administrators of the target IG are allowed to invoke this
 * endpoint; requests from other users are rejected with an HTTP 403 (Forbidden)
 * response. On success the endpoint returns a model containing
 * {@code result = 1}. Multilingual property interception is temporarily
 * disabled while the deletion is performed and restored afterwards.
 *
 * @see CircabcDeclarativeWebScript
 * @see AutoUploadApi
 */
public class AutoUploadDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AutoUploadDelete.class);

  /**
   * API providing the business operations for auto-upload configurations,
   * including removal of an existing entry.
   */
  @Autowired
  private AutoUploadApi autoUploadApi;

  /**
   * Service used to verify that the current user holds group administrator
   * rights on the target Interest Group.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the DELETE request by removing the specified auto-upload
   * configuration entry.
   *
   * <p>The IG identifier is read from the {@code id} URL template variable and
   * the {@code configurationId} request parameter identifies the entry to
   * delete. Access is restricted to group administrators of the IG. Any error
   * is logged and translated into the appropriate HTTP status code (400 for an
   * invalid node reference, 403 for access denial, 500 for any other failure),
   * in which case {@code null} is returned to trigger a redirect to the status
   * template.
   *
   * @param req the web script request; supplies the {@code id} template
   *            variable and the {@code configurationId} parameter
   * @param status the web script response status, updated with an error code
   *               and message when the operation fails
   * @param cache the cache directives for the response
   * @return a model map containing {@code result = 1} on success, or
   *         {@code null} when an error occurred and a redirect is required
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

      String configurationIdStr = req.getParameter("configurationId");

      int configurationId;
      configurationId = getConfigurationId(configurationIdStr);

      this.autoUploadApi.removeAutoUploadEntry(igId, configurationId);

      model.put("result", 1);
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when deleting auto upload configuration",
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when deleting auto upload configuration",
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Unexpected error when deleting auto upload configuration",
        e
      );
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * Parses the supplied configuration identifier string into an integer.
   *
   * @param configurationIdStr the raw {@code configurationId} request
   *                           parameter value
   * @return the parsed configuration identifier
   * @throws IllegalArgumentException if the value is not a valid integer
   */
  private int getConfigurationId(String configurationIdStr) {
    int configurationId;
    try {
      configurationId = Integer.parseInt(configurationIdStr);
    } catch (NumberFormatException e) {
      logger.error("Invalid configuration ID format: " + configurationIdStr, e);
      throw new IllegalArgumentException(
        "Wrong numeric value for 'configurationId': " + configurationIdStr,
        e
      );
    }
    return configurationId;
  }
}
