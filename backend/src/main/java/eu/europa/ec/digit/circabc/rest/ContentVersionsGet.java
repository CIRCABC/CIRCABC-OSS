package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
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
 * Alfresco Web Script endpoint that handles HTTP {@code GET} requests to
 * retrieve the version history of a content item stored in the CIRCABC
 * repository.
 *
 * <p>The target content node is identified by the {@code id} URL template
 * variable. An optional {@code language} request parameter controls the locale
 * used when resolving multilingual (ML) content: when it is absent the endpoint
 * remains ML-aware, and when it is provided the corresponding {@link Locale} is
 * applied to the content and interface locale so that language-specific
 * property values are returned.</p>
 *
 * <p>Before returning any data, the caller must hold at least
 * {@link LibraryPermissions#LIBACCESS} permission on the content node;
 * otherwise the request is rejected with an HTTP {@code 403 Forbidden}
 * response. An invalid or unknown node reference results in an HTTP
 * {@code 400 Bad Request} response.</p>
 *
 * <p>On success the returned model contains a {@code versions} entry holding
 * the list of versions of the requested content, which is subsequently
 * rendered by the associated FreeMarker template.</p>
 */
public class ContentVersionsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentVersionsGet.class);

  /** API used to resolve the version history of a content item. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to verify the current user's library permissions on the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the endpoint logic: resolves the requested content node, checks
   * that the current user is allowed to read it and populates the model with
   * its version history.
   *
   * <p>The content {@code id} is read from the URL template variables and the
   * optional {@code language} request parameter is used to configure the
   * multilingual resolution locale. The original ML-aware state is always
   * restored before the method returns.</p>
   *
   * @param req the web script request; provides the {@code id} template
   *            variable and the optional {@code language} parameter
   * @param status the response status, updated to {@code 403} on access denial
   *               or {@code 400} on an invalid node reference
   * @param cache the cache directives for the response
   * @return a model map containing the {@code versions} entry with the content
   *         version history, or {@code null} when the request is rejected and a
   *         redirect status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");
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
          "Cannot read versions of content, not enough permissions"
        );
      }
      model.put("versions", this.contentApi.contentIdVersionsGet(id, language));
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
