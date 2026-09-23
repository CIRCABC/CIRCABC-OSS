package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco declarative web script endpoint that updates the avatar (profile picture) of a user.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention: it handles the
 * update (implied HTTP {@code POST}/{@code PUT}) of a user avatar. The target user is identified
 * by the {@code userId} template variable extracted from the request URL, and the new avatar image
 * is supplied as a multipart file upload in the request body.
 *
 * <p>Only the user themselves or an Alfresco administrator is allowed to change a given user's
 * avatar; any other caller receives an HTTP {@code 403 Forbidden} response. Malformed requests
 * (non-multipart bodies or missing file parts) result in an HTTP {@code 406 Not Acceptable}
 * response. The actual persistence is delegated to {@link UsersApi#updateAvatar}.
 *
 * @author schwerr
 */
public class UserAvatarUpdate extends CircabcDeclarativeWebScript {

  /** Logger used to report access-denied and processing errors. */
  private static final Log logger = LogFactory.getLog(UserAvatarUpdate.class);

  /** API used to persist the uploaded avatar image for the target user. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to verify that the caller is the target user or an Alfresco administrator. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the avatar update request.
   *
   * <p>Resolves the target {@code userId} from the URL template variables, enforces that the
   * current user is either that same user or an Alfresco administrator, then reads the uploaded
   * image from the multipart request and stores it via {@link UsersApi#updateAvatar}. While
   * processing, multilingual (ML) property awareness is temporarily disabled and restored
   * afterwards. Authorization and processing failures are translated into the appropriate HTTP
   * status codes on the response instead of propagating as exceptions.
   *
   * @param req the web script request, expected to carry a {@code userId} template variable and a
   *     multipart body containing the avatar image file
   * @param status the response status object, updated with {@code 403} on access denial or
   *     {@code 406} when the request is malformed or processing fails
   * @param cache the response cache directives (unused by this endpoint)
   * @return an empty model map on success, or {@code null} when an error status has been set on the
   *     response
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("userId");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !(currentUserPermissionCheckerService.isCurrentUserEqualTo(id) ||
          currentUserPermissionCheckerService.isAlfrescoAdmin())
      ) {
        throw new AccessDeniedException(
          "Impossible to update the avatar of somebody else"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      FormData form = (FormData) req.parseContent();

      if ((form == null) || !form.getIsMultiPart()) {
        throw new IllegalArgumentException("Not a multipart request.");
      }

      // Find the File Upload file, and process the contents
      boolean processed = false;

      for (FormData.FormField field : form.getFields()) {
        if (field.getIsFile()) {
          InputStream inputStream = null;
          try {
            String fileName = field.getFilename();
            inputStream = field.getInputStream();
            this.usersApi.updateAvatar(id, inputStream, fileName);
          } finally {
            if (inputStream != null) {
              inputStream.close();
            }
          }
          processed = true;
          break;
        }
      }

      if (!processed) {
        throw new IllegalArgumentException("Uploaded file could not be found.");
      }
    } catch (AccessDeniedException ade) {
      logger.error(ade.getMessage(), ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error updating user avatar: " + e.getMessage(), e);
      status.setCode(Status.STATUS_NOT_ACCEPTABLE);
      status.setMessage(e.getMessage());
      status.setException(e);
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }
}
