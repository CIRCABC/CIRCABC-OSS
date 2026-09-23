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
 * Alfresco declarative web script that implements the "paste copy" clipboard
 * operation for content nodes.
 *
 * <p>As the class name ({@code NodesIdPasteCopy}) implies, this endpoint is
 * invoked with an HTTP {@code POST} request. It copies (as opposed to moving)
 * the source nodes identified by the {@code nodeIds} request parameter into the
 * destination folder whose node identifier ({@code id}) is provided in the URL
 * path.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li><b>id</b> (path variable) — the identifier of the destination folder
 *       node within the workspace {@code SpacesStore}. The referenced node must
 *       exist and be of type {@code cm:folder}.</li>
 *   <li><b>nodeIds</b> (request parameter, multi-valued) — the identifiers of
 *       the source nodes to be copied into the destination folder.</li>
 * </ul>
 *
 * <p>The caller must hold the Alfresco "add children" permission on the
 * destination folder; otherwise the operation is rejected with an HTTP
 * {@code 403 Forbidden} response.</p>
 *
 * @author schwerr
 */
public class NodesIdPasteCopy extends CircabcDeclarativeWebScript {

  /** Logger for reporting errors raised while pasting the copied nodes. */
  static final Log logger = LogFactory.getLog(NodesIdPasteCopy.class);

  /** Alfresco service used to verify the existence and type of the destination folder. */
  @Autowired
  private NodeService nodeService;

  /** API that performs the clipboard paste (copy) of the source nodes. */
  @Autowired
  private ClipboardApi clipboardApi;

  /** Service used to check whether the current user may add children to the destination folder. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Copies the requested source nodes into the destination folder.
   *
   * <p>The destination folder is resolved from the {@code id} path variable and
   * validated to ensure it exists and is a folder. The source node identifiers
   * are read from the {@code nodeIds} request parameter, and the current user's
   * "add children" permission on the destination is verified before the copy is
   * performed via {@link ClipboardApi#paste}. Multilingual property interception
   * is temporarily disabled during the paste and restored afterwards.</p>
   *
   * @param req the web script request; supplies the {@code id} path variable and
   *            the {@code nodeIds} request parameter
   * @param status the web script response status, set to
   *               {@code 403 Forbidden} on access-denied errors and
   *               {@code 406 Not Acceptable} on other failures
   * @param cache the web script cache directive (unused)
   * @return an empty model map on success, or {@code null} when the request
   *         fails and an error status/redirect has been set
   * @throws IllegalArgumentException if no folder with the given {@code id}
   *         exists or the node is not a folder
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
          "Impossible to paste the elements, not enough permissions"
        );
      }

      MLPropertyInterceptor.setMLAware(false);

      this.clipboardApi.paste(
        nodeIds,
        folderNodeRef,
        ClipboardAction.COPY.getValue()
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when trying to paste copy nodes for id: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when trying to paste copy nodes for id: " + id, e);
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
