package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.GroupsApi;
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
 * Alfresco webscript endpoint that handles the bulk import of a ZIP archive into a
 * target folder (Library/Interest Group node) via an HTTP {@code POST} request.
 *
 * <p>The endpoint expects a {@code multipart/form-data} request whose {@code folderId}
 * is supplied as a URL template variable identifying the destination folder. The uploaded
 * ZIP file is read from the single file form field, while additional behavioural options
 * are read as request parameters:
 *
 * <ul>
 *   <li>{@code notifyUser} - whether the initiating user should be notified once the import completes.</li>
 *   <li>{@code deleteFile} - whether the uploaded ZIP file should be deleted after import.</li>
 *   <li>{@code disableNotification} - whether notifications to other members should be suppressed.</li>
 *   <li>{@code encoding} - the character encoding of the archive entries; only {@code UTF-8}
 *       or {@code CP437} are accepted.</li>
 * </ul>
 *
 * <p>The caller must hold the Alfresco "add children" permission on the target folder,
 * otherwise the import is rejected. The actual extraction and node creation is delegated
 * to {@link GroupsApi#importZipFile}.
 *
 * @see CircabcDeclarativeWebScript
 * @see GroupsApi
 */
public class ImportZipFilePost extends CircabcDeclarativeWebScript {

  /** Logger used to report permission, validation and import failures. */
  static final Log logger = LogFactory.getLog(ImportZipFilePost.class);

  /** Business API used to perform the actual ZIP extraction and node import. */
  @Autowired
  private GroupsApi groupsApi;

  /** Service used to verify the current user's permissions on the target folder. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the ZIP import request.
   *
   * <p>Resolves the target {@code folderId} from the URL template variables, verifies the
   * current user has the Alfresco "add children" permission on that folder, validates the
   * request parameters and the multipart payload, then delegates the import to
   * {@link GroupsApi#importZipFile}.
   *
   * <p>On failure the method does not throw; instead it sets the appropriate HTTP status on
   * {@code status} (403 for access denied, 400 for invalid node/aspect, 500 for any other
   * error), logs the error and returns {@code null}.
   *
   * @param req the webscript request; provides the {@code folderId} template variable, the
   *            {@code notifyUser}, {@code deleteFile}, {@code disableNotification} and
   *            {@code encoding} parameters, and the multipart form containing the ZIP file
   * @param status the response status, populated with an error code and message when the
   *               import fails
   * @param cache the cache control for the response
   * @return a model map containing {@code "message"} set to {@code "ok"} on success, or
   *         {@code null} when the import fails
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String folderId = templateVars.get("folderId");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
          folderId
        )
      ) {
        throw new AccessDeniedException(
          "Cannot import ZIP, not enough permissions"
        );
      }

      //FIX by ALMO - DIGITCIRCABC-4834 Bulk Import
      boolean notifyUser = "true".equals(req.getParameter("notifyUser"));
      boolean deleteFile = "true".equals(req.getParameter("deleteFile"));
      boolean disableNotification = "true".equals(
        req.getParameter("disableNotification")
      );

      String encoding = req.getParameter("encoding");
      if (
        !("UTF-8".equalsIgnoreCase(encoding) ||
          "CP437".equalsIgnoreCase(encoding))
      ) {
        throw new IllegalArgumentException(
          "The 'encoding' must be UTF-8 or CP437"
        );
      }

      String mimeType = null;
      String fileName = null;

      InputStream fileInputStream = null;

      FormData form = (FormData) req.parseContent();

      if ((form == null) || !form.getIsMultiPart()) {
        throw new IllegalArgumentException("Not a multipart request.");
      }

      //ALMO - it seems that there is only one form field, the field with type File.
      //It's the reason why I extracted the parameters notifyUser, deleteFile, disableNotification and encoding from the WebScriptRequest
      for (FormData.FormField field : form.getFields()) {
        if (field.getIsFile()) {
          mimeType = field.getMimetype();
          fileName = field.getFilename();
          fileInputStream = field.getInputStream();
        }
      }

      this.groupsApi.importZipFile(
        folderId,
        fileInputStream,
        fileName,
        mimeType,
        notifyUser,
        deleteFile,
        disableNotification,
        encoding
      );

      model.put("message", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied importing ZIP file for folderId: " + folderId,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException | InvalidAspectException inre) {
      logger.error(
        "Invalid node reference or aspect importing ZIP for folderId: " +
          folderId,
        inre
      );
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error importing ZIP file for folderId: " + folderId, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    }

    return model;
  }
}
