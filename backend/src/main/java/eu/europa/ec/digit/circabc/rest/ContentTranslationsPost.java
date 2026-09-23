package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.ContentApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
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
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * REST endpoint that uploads a translation for an existing content node.
 *
 * <p>This Alfresco Declarative Web Script handles an HTTP {@code POST} request (as implied by the
 * {@code Post} suffix in the class name) for the content identified by the {@code id} path
 * variable. The request must be a multipart form submission carrying the translation file together
 * with a {@code lang} field indicating the target language of the translation.</p>
 *
 * <p>Processing steps:</p>
 * <ul>
 *   <li>Verifies that the current user holds at least the
 *       {@link LibraryPermissions#LIBMANAGEOWN} permission on the target node; otherwise an
 *       {@link AccessDeniedException} is raised and the response is set to
 *       {@code 403 Forbidden}.</li>
 *   <li>Parses the multipart form, extracting the uploaded file (its input stream, MIME type and
 *       file name) and the {@code lang} form field.</li>
 *   <li>Delegates persistence of the translation to
 *       {@link ContentApi#contentIdTranslationsPost(String, String, InputStream, String, String)}
 *       and returns the resulting translation set.</li>
 * </ul>
 *
 * <p>The model returned on success contains the {@code translationSet} entry, which is rendered by
 * the associated FreeMarker template. Invalid input (non-multipart request or missing language)
 * results in a {@code 400 Bad Request} response.</p>
 */
public class ContentTranslationsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentTranslationsPost.class);

  /** API providing content operations, including creation of translations. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to check whether the current user holds the required library permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the translation upload request for a content node.
   *
   * <p>Reads the {@code id} path variable to locate the target node, enforces the
   * {@link LibraryPermissions#LIBMANAGEOWN} permission, parses the multipart form to obtain the
   * uploaded file and its {@code lang} field, and stores the translation via {@link ContentApi}.</p>
   *
   * @param req the web script request; expects an {@code id} template variable and a multipart
   *            body containing the translation file and a {@code lang} field
   * @param status the response status, set to {@code 403 Forbidden} on access denial or
   *               {@code 400 Bad Request} on invalid input
   * @param cache the response cache directives
   * @return a model map containing the {@code translationSet} entry on success, or {@code null}
   *         when an error status is set (access denied or bad request)
   * @throws IllegalArgumentException if the request is not multipart or the {@code lang} field is
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
    try {
      if (
        !currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          id,
          LibraryPermissions.LIBMANAGEOWN
        )
      ) {
        throw new AccessDeniedException(
          "Cannot update content, not enough permissions"
        );
      }
      String lang = "";
      String mimeType = "";
      String fileName = "translation";
      InputStream file = null;
      final FormData form = (FormData) req.parseContent();

      if (form == null || !form.getIsMultiPart()) {
        throw new IllegalArgumentException("Not a multipart request.");
      }

      for (FormData.FormField field : form.getFields()) {
        if (field.getIsFile()) {
          mimeType = field.getMimetype();
          fileName = field.getFilename();
          file = field.getInputStream();
        }

        if (field.getName().equals("lang")) {
          lang = field.getValue();
        }
      }
      if (lang == null || lang.isEmpty() || lang.equals("null")) {
        throw new IllegalArgumentException("Language is not present.");
      }
      model.put(
        "translationSet",
        contentApi.contentIdTranslationsPost(id, lang, file, mimeType, fileName)
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
