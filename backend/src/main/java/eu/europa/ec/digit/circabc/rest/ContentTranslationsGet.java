package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
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
 * Web script endpoint that handles the HTTP {@code GET} request for retrieving the set of
 * translations associated with a given content node.
 *
 * <p>The endpoint expects the target content node identifier as the {@code id} URL template
 * variable. Before returning any data it verifies that the current user holds at least the
 * {@link LibraryPermissions#LIBACCESS} permission on that node; otherwise access is denied.
 *
 * <p>On success the model exposes a single {@code translationSet} entry containing the
 * translations returned by {@link ContentApi#contentIdTranslationsGet(String)}, which is then
 * rendered by the associated FreeMarker template.
 */
public class ContentTranslationsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentTranslationsGet.class);

  /** API providing content-related business operations, including translation lookups. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to verify that the current user holds the required library permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the web script logic to fetch the translations of the requested content node.
   *
   * <p>The content node identifier is read from the {@code id} URL template variable. The current
   * user must hold the {@link LibraryPermissions#LIBACCESS} permission on the node; if not, the
   * response status is set to {@link Status#STATUS_FORBIDDEN}. If the identifier does not resolve
   * to a valid node, the response status is set to {@link Status#STATUS_BAD_REQUEST}.
   *
   * @param req the web script request, carrying the {@code id} URL template variable
   * @param status the response status, updated to signal forbidden or bad-request outcomes
   * @param cache the cache control directives for the response
   * @return a model map containing the {@code translationSet} entry on success, or {@code null}
   *     when an error (access denied or invalid node reference) has been handled via {@code status}
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
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Cannot read content translations, not enough permissions"
        );
      }
      model.put("translationSet", this.contentApi.contentIdTranslationsGet(id));
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
