package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.KeywordsApi;
import io.swagger.model.permissions.LibraryPermissions;
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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco web script endpoint that retrieves the keywords associated with a
 * repository node.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention:
 * it handles the HTTP {@code GET} request for the keywords of a node
 * (typically a library document). The node is identified by the {@code id}
 * URL template variable, and the caller must hold at least
 * {@link io.swagger.model.permissions.LibraryPermissions#LIBACCESS} on that
 * node.</p>
 *
 * <p>Inputs:</p>
 * <ul>
 *   <li>{@code id} — URL template variable identifying the target node.</li>
 *   <li>{@code language} — optional request parameter selecting the locale
 *       used to resolve multilingual (ML) keyword values. When omitted, the
 *       response is rendered in ML-aware mode returning all language
 *       variants.</li>
 * </ul>
 *
 * <p>The resulting model exposes the retrieved keywords under the
 * {@code keywords} key. On failure the endpoint sets an appropriate HTTP
 * status: {@code 403} when the user lacks permission, {@code 400} for an
 * invalid node reference and {@code 500} for any other unexpected error.</p>
 */
public class NodesKeywordsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(NodesKeywordsGet.class);

  /**
   * API used to look up the keywords of a node.
   */
  @Autowired
  private KeywordsApi keywordsApi;

  /**
   * Service used to verify that the current user holds the required library
   * permission on the target node before its keywords are returned.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the {@code GET} request that returns the keywords of a node.
   *
   * <p>Reads the {@code id} URL template variable and the optional
   * {@code language} request parameter, toggles multilingual awareness
   * accordingly, checks that the current user has
   * {@link io.swagger.model.permissions.LibraryPermissions#LIBACCESS} on the
   * node and, if so, populates the model with the node's keywords. The
   * previous ML-aware state is always restored before returning.</p>
   *
   * @param req the web script request; provides the {@code id} template
   *            variable and the optional {@code language} parameter
   * @param status the web script response status, updated to {@code 403},
   *               {@code 400} or {@code 500} when an error occurs
   * @param cache the response cache directives
   * @return a model map containing the {@code keywords} entry on success, or
   *         {@code null} when an error occurred and the status has been set
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
    String id = templateVars.get("id");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBACCESS
        )
      ) {
        throw new AccessDeniedException(
          "Impossible to get the keywords of the document, not enough permissions"
        );
      }

      model.put("keywords", this.keywordsApi.nodesIdKeywordsGet(id));
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when getting keywords for node " + id, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(
        "Invalid node reference when getting keywords for node " + id,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when getting keywords for node " + id, e);
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
   * @return the keywordsApi
   */
  public KeywordsApi getKeywordsApi() {
    return this.keywordsApi;
  }

  /**
   * @param keywordsApi the keywordsApi to set
   */
  public void setKeywordsApi(KeywordsApi keywordsApi) {
    this.keywordsApi = keywordsApi;
  }

  /**
   * Sets the service used to check the current user's library permissions.
   *
   * @param currentUserPermissionCheckerService the permission checker service
   *                                             to set
   */
  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
