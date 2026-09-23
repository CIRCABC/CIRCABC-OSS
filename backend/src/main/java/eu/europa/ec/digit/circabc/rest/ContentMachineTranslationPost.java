package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that handles an HTTP {@code POST} request to trigger
 * a machine translation for a content node.
 *
 * <p>The endpoint requests the machine translation of the content identified by
 * the {@code id} path variable into the target {@code language} path variable.
 * The caller must hold at least the {@link LibraryPermissions#LIBMANAGEOWN}
 * library permission on the node, otherwise the request is rejected with an
 * HTTP {@code 403 Forbidden} status.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (path variable) - the reference of the content node to translate.</li>
 *   <li>{@code language} (path variable) - the target language for the translation;
 *       it must be present and non-empty.</li>
 *   <li>{@code notify} (request parameter) - whether interested users should be
 *       notified once the translation is requested.</li>
 * </ul>
 *
 * <p>On success the returned model contains the resulting translation set under
 * the {@code translationSet} key.</p>
 */
public class ContentMachineTranslationPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    ContentMachineTranslationPost.class
  );

  /**
   * API used to request the machine translation of a content node.
   */
  @Autowired
  private ContentApi contentApi;

  /**
   * Service used to verify that the current user holds the required library
   * permissions on the target node.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Executes the machine translation request for the targeted content node.
   *
   * <p>Reads the {@code id} and {@code language} path variables and the
   * {@code notify} request parameter, checks that the current user has the
   * {@link LibraryPermissions#LIBMANAGEOWN} permission on the node, and delegates
   * to {@link ContentApi#requestMachineTranslation(String, String, boolean)}.</p>
   *
   * @param req the web script request; provides the {@code id} and
   *            {@code language} path variables and the {@code notify} parameter
   * @param status the response status; set to {@code 403} when access is denied
   *               or {@code 400} for invalid node/aspect references
   * @param cache the response cache directives
   * @return a model map containing the resulting translation set under the
   *         {@code translationSet} key, or {@code null} when the request fails
   *         and a redirect status has been set
   * @throws IllegalArgumentException if the {@code language} path variable is
   *                                  missing or empty
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
    String language = templateVars.get("language");
    if ((language == null) || language.isEmpty() || language.equals("null")) {
      throw new IllegalArgumentException("Language is not present.");
    }

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBMANAGEOWN
        )
      ) {
        throw new AccessDeniedException(
          "Cannot update content, not enough permissions"
        );
      }
      boolean notify = Boolean.parseBoolean(req.getParameter("notify"));
      model.put(
        "translationSet",
        this.contentApi.requestMachineTranslation(id, language, notify)
      );
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | InvalidAspectException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    }
    return model;
  }
}
