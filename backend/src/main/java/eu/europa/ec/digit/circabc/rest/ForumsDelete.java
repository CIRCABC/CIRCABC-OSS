package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ForumsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a forum.
 *
 * <p>Handles the HTTP {@code DELETE} request (implied by the {@code Delete} suffix in the class
 * name) targeting a single forum identified by the {@code id} path template variable. Before
 * deleting, it verifies that the current authority holds Alfresco delete permission on the forum
 * node; if the permission is missing the request is rejected with {@code 403 Forbidden}.
 *
 * <p>On success the response model contains a {@code "message"} entry set to {@code "ok"}. Failure
 * scenarios are mapped to HTTP status codes: {@link AccessDeniedException} results in
 * {@code 403 Forbidden} and {@link InvalidNodeRefException} results in {@code 400 Bad Request}.
 */
public class ForumsDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ForumsDelete.class);

  /** API providing the forum business operations, including deletion by id. */
  @Autowired
  private ForumsApi forumsApi;

  /** Service used to check the current user's Alfresco permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the forum deletion request.
   *
   * <p>Reads the {@code id} path template variable, checks that the current authority has Alfresco
   * delete permission on the corresponding forum node and, when authorized, deletes the forum via
   * {@link ForumsApi#forumsIdDelete(String)}.
   *
   * @param req the web script request; its service match supplies the {@code id} template variable
   *     identifying the forum to delete
   * @param status the response status, updated to {@code 403 Forbidden} on access denial or
   *     {@code 400 Bad Request} on an invalid node reference
   * @param cache the cache control for the response
   * @return a model map containing {@code "message" -> "ok"} on success, or {@code null} when the
   *     request fails and an error status has been set
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

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoDeletePermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Current Authority cannot delete the forum, not enough permission"
        );
      }

      this.forumsApi.forumsIdDelete(id);
      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
