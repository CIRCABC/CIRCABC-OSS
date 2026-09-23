package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ArchiveApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that permanently deletes a soft-deleted (archived)
 * document from an Interest Group's trash/recycle area.
 *
 * <p>The class name follows the CIRCABC webscript naming convention
 * {@code <Entity><Method>}, so this handles the HTTP {@code DELETE} method for
 * a deleted document. The endpoint expects two URL template variables:
 * <ul>
 *   <li>{@code igId} - the identifier of the Interest Group whose trash is
 *       being purged;</li>
 *   <li>{@code nodeId} - the identifier of the archived node to remove
 *       permanently.</li>
 * </ul>
 * It also accepts an optional {@code language} request parameter used to set
 * the content/UI locale when resolving multilingual properties.
 *
 * <p>Only a group administrator of the target Interest Group is allowed to
 * perform this operation; otherwise the request is rejected with an HTTP
 * {@code 403 Forbidden}. The actual deletion is delegated to
 * {@link ArchiveApi#groupsIdDocumentsDeletedNodeIdDelete(String, String)}.
 */
public class GroupsDocumentsDeletedDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    GroupsDocumentsDeletedDelete.class
  );

  /**
   * API used to permanently remove archived (soft-deleted) documents.
   */
  @Autowired
  private ArchiveApi archiveApi;

  /**
   * Service used to verify that the current user has group administrator
   * rights over the target Interest Group.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the DELETE request that permanently removes an archived document
   * from an Interest Group's trash.
   *
   * <p>Resolves the {@code igId} and {@code nodeId} URL template variables,
   * optionally configures the locale from the {@code language} request
   * parameter, and verifies that the current user is a group administrator
   * before delegating the deletion to {@link ArchiveApi}. The multilingual
   * awareness flag of {@link MLPropertyInterceptor} is restored to its
   * original value once processing completes.
   *
   * @param req the web script request, providing the {@code language}
   *     parameter and the {@code igId}/{@code nodeId} URL template variables
   * @param status the response status; set to {@code 403 Forbidden} when the
   *     user lacks group admin rights, or {@code 400 Bad Request} when the
   *     referenced node is invalid
   * @param cache the cache directives for the response
   * @return an empty model map on success, or {@code null} when an error
   *     status and redirect have been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String language = req.getParameter("language");

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");
    String nodeId = templateVars.get("nodeId");

    try {
      if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
        throw new AccessDeniedException(
          "User is not Group admin to remove a deleted document"
        );
      }
      this.archiveApi.groupsIdDocumentsDeletedNodeIdDelete(id, nodeId);
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
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
