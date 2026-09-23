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
 * Alfresco webscript endpoint handling the HTTP {@code PUT} request that enables
 * or disables an auto-upload configuration entry for an Interest Group (IG).
 *
 * <p>The endpoint reads the IG identifier from the URL template variable
 * {@code id} and expects two request parameters:</p>
 * <ul>
 *   <li>{@code configurationId} &mdash; the numeric identifier of the auto-upload
 *       configuration entry to toggle.</li>
 *   <li>{@code enable} &mdash; a boolean flag indicating whether the entry should
 *       be enabled ({@code true}) or disabled ({@code false}).</li>
 * </ul>
 *
 * <p>Access is restricted to group administrators of the target IG; a request
 * from any other user is rejected with an HTTP {@code 403 Forbidden}. On success
 * the endpoint delegates to {@link AutoUploadApi#toggleAutoUploadEntry(int, boolean)}
 * and returns a model with {@code result} set to {@code 1}.</p>
 */
public class AutoUploadPut extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AutoUploadPut.class);

  /** Business API used to toggle the auto-upload configuration entry. */
  @Autowired
  private AutoUploadApi autoUploadApi;

  /** Service used to verify that the current user is an administrator of the IG. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the auto-upload toggle request.
   *
   * <p>Resolves the IG identifier from the URL, verifies that the current user is
   * a group administrator, parses the {@code configurationId} and {@code enable}
   * parameters and enables or disables the corresponding auto-upload entry.
   * ML-aware property handling is temporarily disabled while the request is
   * processed and restored afterwards.</p>
   *
   * @param req the incoming web script request; provides the {@code id} template
   *            variable and the {@code configurationId} and {@code enable}
   *            request parameters
   * @param status the response status object, updated with the appropriate HTTP
   *               error code when the request fails
   * @param cache the response cache directives (unused)
   * @return a model map containing {@code result} = {@code 1} on success, or
   *         {@code null} when an error occurs and an error status has been set
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

      String enableStr = req.getParameter("enable");
      boolean enable;
      enable = isEnable(enableStr);

      this.autoUploadApi.toggleAutoUploadEntry(igId, configurationId, enable);

      model.put("result", 1);
    } catch (InvalidNodeRefException inre) {
      logger.error("Bad request when processing auto upload toggle", inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when processing auto upload toggle", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when processing auto upload toggle", e);
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
   * Parses the {@code enable} request parameter into a boolean.
   *
   * @param enableStr the raw string value of the {@code enable} parameter
   * @return {@code true} if the value represents boolean {@code true}, {@code false} otherwise
   * @throws IllegalArgumentException if the value cannot be interpreted as a boolean
   */
  private boolean isEnable(String enableStr) {
    boolean enable;
    try {
      enable = Boolean.parseBoolean(enableStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong boolean value for 'enable': " + enableStr,
        e
      );
    }
    return enable;
  }

  /**
   * Parses the {@code configurationId} request parameter into an integer.
   *
   * @param configurationIdStr the raw string value of the {@code configurationId} parameter
   * @return the parsed configuration identifier
   * @throws IllegalArgumentException if the value is not a valid integer
   */
  private int getConfigurationId(String configurationIdStr) {
    int configurationId;
    try {
      configurationId = Integer.parseInt(configurationIdStr);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "Wrong numeric value for 'configurationId': " + configurationIdStr,
        e
      );
    }
    return configurationId;
  }
}
