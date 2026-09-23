package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco declarative web script that updates the binary content of an existing working copy
 * without performing a check-in.
 *
 * <p>The class name ends in {@code Update}, so this endpoint is exposed as an HTTP {@code PUT}
 * (or {@code POST}) request that carries a multipart form payload. The working-copy node is
 * identified by the {@code id} template variable taken from the request URL, and it is resolved
 * against the workspace {@code SpacesStore}.
 *
 * <p>Processing performs the following validations before updating the content:
 *
 * <ul>
 *   <li>the node identified by {@code id} must exist;</li>
 *   <li>the node must carry the {@link ContentModel#ASPECT_WORKING_COPY} aspect;</li>
 *   <li>the current user must hold the
 *       {@link LibraryPermissions#LIBMANAGEOWN} library permission and be the owner of the
 *       working copy;</li>
 *   <li>the request must be multipart and contain an uploaded file.</li>
 * </ul>
 *
 * <p>When these conditions are met, the uploaded stream and its MIME type are handed to
 * {@link CociContentBusinessSrv#update} to overwrite the working copy content. Multilingual
 * property interception is temporarily disabled during the update and restored afterwards.
 *
 * @author schwerr
 */
public class ContentIdUpdate extends CircabcDeclarativeWebScript {

  /** Alfresco node service used to look up the working copy and inspect its aspects. */
  @Autowired
  private NodeService nodeService;

  /** Business service that performs the actual content update on the working copy. */
  @Autowired
  private CociContentBusinessSrv cociContentBusinessSrv;

  /** Service used to verify the caller's library permissions and working-copy ownership. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to update the content of an existing working copy.
   *
   * <p>Resolves the working copy from the {@code id} template variable, validates that it exists,
   * is a working copy and that the caller is authorised, then replaces its content with the file
   * found in the multipart request body.
   *
   * @param req the web script request; must provide an {@code id} template variable and a
   *     multipart body containing the file to upload
   * @param status the response status object, updated to {@link Status#STATUS_FORBIDDEN} on an
   *     access-denied error or {@link Status#STATUS_NOT_ACCEPTABLE} on any other failure
   * @param cache the cache control object for the response
   * @return an empty model map on success, or {@code null} when an error has been handled by
   *     setting the response status and redirecting
   * @throws IllegalArgumentException if the node does not exist, is not a working copy, the
   *     request is not multipart or no uploaded file can be found
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
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    NodeRef workingCopyRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );

    if (!this.nodeService.exists(workingCopyRef)) {
      throw new IllegalArgumentException(
        "The item with id '" + id + "' could not be found."
      );
    }

    if (
      !this.nodeService.hasAspect(
        workingCopyRef,
        ContentModel.ASPECT_WORKING_COPY
      )
    ) {
      throw new IllegalArgumentException(
        "The item with id '" + id + "' is not a working copy."
      );
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

      if (
        !this.currentUserPermissionCheckerService.isWorkingCopyOwner(
          workingCopyRef.getId()
        )
      ) {
        throw new AccessDeniedException(
          "Cannot update content, not enough permissions"
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
          String mimeType = field.getMimetype();
          try (InputStream inputStream = field.getInputStream()) {
            this.cociContentBusinessSrv.update(
              workingCopyRef,
              inputStream,
              mimeType
            );
          }
          processed = true;
          break;
        }
      }

      if (!processed) {
        throw new IllegalArgumentException("Uploaded file could not be found.");
      }
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(ERROR_OCCURRED, e);
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
