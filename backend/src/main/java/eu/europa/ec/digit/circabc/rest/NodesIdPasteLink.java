package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.ClipboardAction;
import io.swagger.api.ClipboardApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that pastes one or more source nodes as <em>links</em> into a
 * target folder.
 *
 * <p>The HTTP method is a <strong>POST</strong> (implied by the {@code PasteLink} suffix of
 * the class name, which represents a mutating clipboard operation). The target folder is
 * identified by the {@code id} path variable, and the nodes to link are supplied through the
 * {@code nodeIds} request parameter (one or more values).</p>
 *
 * <p>Behavior:</p>
 * <ul>
 *   <li>Resolves the target folder from the {@code id} path variable in the workspace
 *       SpacesStore and validates that it exists and is of type {@code cm:folder}.</li>
 *   <li>Verifies that the current user has the Alfresco "add children" permission on the
 *       target folder before performing the paste.</li>
 *   <li>Delegates the link creation to {@link ClipboardApi#paste(String[], NodeRef, int)}
 *       using the {@link ClipboardAction#LINK} action.</li>
 * </ul>
 *
 * @author schwerr
 */
public class NodesIdPasteLink extends CircabcDeclarativeWebScript {

  /** Logger used to report access-denied and generic paste failures. */
  static final Log logger = LogFactory.getLog(NodesIdPasteLink.class);

  /** Alfresco service used to check the target node's existence and type. */
  @Autowired
  private NodeService nodeService;

  /** Business API that performs the clipboard paste (link) operation. */
  @Autowired
  private ClipboardApi clipboardApi;

  /** Service used to verify the current user's Alfresco permissions on the target folder. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the request to paste the supplied nodes as links into the target folder.
   *
   * <p>On success an empty model is returned. When the operation fails, the HTTP status of the
   * response is set accordingly (403 Forbidden on permission errors, 406 Not Acceptable on any
   * other error) and {@code null} is returned so that the framework renders the error status.</p>
   *
   * @param req the web script request; provides the {@code id} path variable identifying the
   *            target folder and the {@code nodeIds} parameter listing the nodes to link
   * @param status the response status, updated with an error code, message and redirect flag
   *               when the paste cannot be completed
   * @param cache the response cache control (unused)
   * @return an empty model map on success, or {@code null} when an error status has been set
   * @throws IllegalArgumentException if no folder with the given {@code id} exists or the node is
   *                                  not a folder
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

    NodeRef folderNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );

    if (
      !this.nodeService.exists(folderNodeRef) ||
      !ContentModel.TYPE_FOLDER.equals(this.nodeService.getType(folderNodeRef))
    ) {
      throw new IllegalArgumentException(
        "The folder with id '" + id + "' could not be found."
      );
    }

    String[] nodeIds = req.getParameterValues("nodeIds");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Impossible to link the elements, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      this.clipboardApi.paste(
        nodeIds,
        folderNodeRef,
        ClipboardAction.LINK.getValue()
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when trying to paste link nodes for id: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when trying to paste link nodes for id: " + id, e);
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
