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
 * REST webscript endpoint handling HTTP {@code POST} requests to create an
 * auto-upload configuration entry for an Interest Group (IG).
 *
 * <p>The IG identifier is taken from the {@code id} template variable of the
 * request URL, and the auto-upload configuration payload is read from the raw
 * request body. Access is restricted to group administrators of the target IG;
 * requests from other users are rejected with an HTTP {@code 403 Forbidden}.
 *
 * <p>On success the endpoint delegates the configuration creation to
 * {@link AutoUploadApi#addAutoUploadEntry(String)} and returns a model
 * containing a {@code result} flag. Multilingual (ML) property interception is
 * temporarily disabled while the request is processed and restored afterwards.
 *
 * @see CircabcDeclarativeWebScript
 * @see AutoUploadApi
 */
public class AutoUploadPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(AutoUploadPost.class);

  /** API providing the business logic for creating auto-upload entries. */
  @Autowired
  private AutoUploadApi autoUploadApi;

  /** Service used to verify that the current user is an administrator of the target IG. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the auto-upload creation request.
   *
   * <p>Resolves the IG identifier from the {@code id} URL template variable,
   * checks that the current user is a group administrator of that IG, reads the
   * configuration payload from the request body and persists it through
   * {@link AutoUploadApi#addAutoUploadEntry(String)}. ML property interception
   * is disabled for the duration of the call and restored in the
   * {@code finally} block.
   *
   * <p>Error handling maps failures to HTTP status codes: an
   * {@link InvalidNodeRefException} results in {@code 400 Bad Request}, an
   * {@link AccessDeniedException} results in {@code 403 Forbidden}, and any
   * other exception results in {@code 406 Not Acceptable}. In all error cases
   * the method returns {@code null} after setting the response status.
   *
   * @param req the web script request; supplies the {@code id} template
   *     variable and the configuration body
   * @param status the response status, updated with an error code and message
   *     when processing fails
   * @param cache the cache directives for the response (unused)
   * @return a model map containing the {@code result} flag on success, or
   *     {@code null} if an error occurred and the response status was set
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

      String configurationBody = req.getContent().getContent();

      this.autoUploadApi.addAutoUploadEntry(igId, configurationBody);

      model.put("result", 1);
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when processing auto upload request",
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when processing auto upload request", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when processing auto upload request", e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
