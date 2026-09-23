package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.api.HeadersApi;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a CIRCABC header (a top-level container
 * in the Headers &rarr; Categories &rarr; Interest Groups hierarchy).
 *
 * <p>The class name follows the {@code <Entity><Method>} convention, so this
 * endpoint is bound to an HTTP {@code DELETE} request. The header to remove is
 * identified by the {@code id} path/template variable extracted from the
 * request URL.
 *
 * <p>Only a CIRCABC administrator or an Alfresco administrator is allowed to
 * perform the deletion; any other caller results in an
 * {@link AccessDeniedException} and an HTTP {@code 403 Forbidden} response.
 * Depending on the failure, the endpoint may also return HTTP
 * {@code 400 Bad Request} (for an invalid id or a non-empty header). On success
 * the response model exposes the {@code id} of the deleted header.
 */
public class HeaderDelete extends CircabcDeclarativeWebScript {

  /** API providing the header business operations, including deletion. */
  @Autowired
  private HeadersApi headerApi;

  /** Service used to verify that the current user has administrative rights. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Application service used to perform the underlying header cleanup. */
  @Autowired
  private CircabcService circabcService;

  /**
   * Handles the header deletion request.
   *
   * <p>Reads the {@code id} template variable from the request, checks that the
   * caller is a CIRCABC or Alfresco administrator, and delegates the deletion to
   * {@link HeadersApi#deleteHeader(String)}. Regardless of the outcome, the
   * {@link CircabcService} is invoked in the {@code finally} block to complete
   * the header cleanup for the resolved node reference.
   *
   * @param req the web script request, whose template variables must contain
   *            the {@code id} of the header to delete
   * @param status the response status, updated with the appropriate HTTP code
   *               and message when an error occurs
   * @param cache the cache directives for the response
   * @return a model map containing the {@code id} of the deleted header on
   *         success, or {@code null} when an error has been reported through
   *         {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");

    try {
      if (
        !this.currentUserPermissionCheckerService.isCircabcAdmin() &&
        !this.currentUserPermissionCheckerService.isAlfrescoAdmin()
      ) {
        throw new AccessDeniedException(
          "Not enough rights for deleting a header"
        );
      }

      this.headerApi.deleteHeader(id);
      Map<String, Object> model = new HashMap<>(7, 1.0f);
      model.put("id", id);
      return model;
    } catch (IllegalArgumentException iae) {
      logger.error(ERROR_OCCURRED, iae);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Header is not empty");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Invalid id");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      circabcService.deleteHeader(Converter.createNodeRefFromId(id));
    }
  }
}
