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
 * Alfresco web script endpoint that retrieves a specific version of a content
 * node in the Library.
 *
 * <p>This endpoint responds to an HTTP {@code GET} request (as implied by the
 * {@code Get} suffix in the class name) and returns the metadata of a single
 * historical version of a document. The target document and version are
 * identified through the URL template variables {@code id} (the content node
 * identifier) and {@code versionId} (the version label/identifier).</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (template variable) - the identifier of the content node.</li>
 *   <li>{@code versionId} (template variable) - the identifier of the version
 *       to retrieve.</li>
 *   <li>{@code language} (request parameter, optional) - the locale used to
 *       resolve multilingual content. When omitted, the response is rendered
 *       in a multilingual-aware (ML aware) mode; when provided, the content and
 *       UI locale are set accordingly and ML awareness is disabled.</li>
 * </ul>
 *
 * <p>The caller must hold at least {@link LibraryPermissions#LIBACCESS}
 * permission on the content node; otherwise the request is rejected with an
 * HTTP {@code 403 Forbidden}. Invalid node references result in an HTTP
 * {@code 400 Bad Request}.</p>
 */
public class ContentVersionGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentVersionGet.class);

  /**
   * API used to look up content and its versions in the repository.
   */
  @Autowired
  private ContentApi contentApi;

  /**
   * Service used to verify that the current user holds the required library
   * permissions on the target content node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Retrieves the requested version of the content node and places it in the
   * web script response model under the {@code version} key.
   *
   * <p>Resolves the optional {@code language} request parameter to configure
   * the content/UI locale and multilingual awareness, then checks that the
   * current user has {@link LibraryPermissions#LIBACCESS} on the node before
   * fetching the version. The previous ML-aware state is always restored before
   * the method returns.</p>
   *
   * @param req the web script request; supplies the {@code language} parameter
   *            and the {@code id} and {@code versionId} URL template variables
   * @param status the response status, updated to {@code 403} on access denial
   *               or {@code 400} on an invalid node reference
   * @param cache the response cache control settings
   * @return a model map containing the {@code version} entry, or {@code null}
   *         when the request fails and a redirect status has been set
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
    String versionId = templateVars.get("versionId");
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
      model.put(
        "version",
        this.contentApi.contentIdVersionsVersionIdGet(id, versionId, language)
      );
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
