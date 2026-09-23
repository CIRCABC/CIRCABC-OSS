package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.TopicsApi;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco declarative web script that handles the HTTP POST request for adding
 * a file attachment to a newsgroup post (topic) identified by its node id.
 *
 * <p>The endpoint expects a multipart/form-data request containing the file to
 * upload. The target post is resolved from the {@code id} URL template variable
 * and the attachment name is taken from the {@code name} request parameter. The
 * first file field found in the submitted form is streamed to
 * {@link TopicsApi#addFileAttachment(String, String, InputStream)}.
 *
 * <p>Before performing the upload, the current user must hold the
 * {@link NewsGroupPermissions#NWSPOST} permission on the target node; otherwise
 * the request is rejected with an HTTP 403 (Forbidden) response. Any other
 * failure (missing/invalid multipart content, missing file, or processing
 * error) results in an HTTP 406 (Not Acceptable) response.
 *
 * @author schwerr
 */
public class PostAttachmentFileAdd extends DeclarativeWebScript {

  /** Logger used to report access-denied and processing errors. */
  private static final Logger logger = LoggerFactory.getLogger(
    PostAttachmentFileAdd.class
  );

  /** API used to attach the uploaded file to the target post. */
  @Autowired
  private TopicsApi topicsApi;

  /** Service used to verify the current user's permissions on the target node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the file-attachment upload request.
   *
   * <p>Reads the target post {@code id} from the URL template variables and the
   * attachment {@code name} from the request parameters, verifies the current
   * user has the required newsgroup post permission, then extracts the first
   * file field from the multipart form and adds it as an attachment to the post.
   * The {@link MLPropertyInterceptor} multilingual-aware flag is temporarily
   * disabled during processing and restored afterwards.
   *
   * @param req the web script request; must contain the {@code id} template
   *     variable, an optional {@code name} parameter and multipart form content
   *     holding the file to upload
   * @param status the web script response status, updated with an error code and
   *     message when the upload cannot be completed
   * @param cache the web script cache directives (unused)
   * @return an empty model map on success, or {@code null} when the request
   *     fails and a redirect status has been set
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
    String name = req.getParameter("name");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
          id,
          NewsGroupPermissions.NWSPOST
        )
      ) {
        throw new AccessDeniedException(
          "Cannot add attachment, not enough permissions"
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
          try (InputStream inputStream = field.getInputStream()) {
            topicsApi.addFileAttachment(id, name, inputStream);
          }
          processed = true;
          break;
        }
      }

      if (!processed) {
        throw new IllegalArgumentException("Uploaded file could not be found.");
      }
    } catch (AccessDeniedException ade) {
      logger.error("Access denied when adding file attachment", ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when adding file attachment", e);
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
