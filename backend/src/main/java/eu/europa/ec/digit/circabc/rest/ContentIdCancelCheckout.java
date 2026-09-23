package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.model.permissions.LibraryPermissions;
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
 * REST webscript endpoint that cancels the checkout of a document (i.e. discards
 * the working copy created when a document was checked out) in the CIRCABC library.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Id><Action>} convention: it
 * operates on a content node identified by its {@code id} and performs the
 * "cancel checkout" action. It is invoked as a mutating request (the URL template
 * exposes the node {@code id} path variable) and resolves the working copy associated
 * with the given original node.
 *
 * <p>Processing steps:
 * <ul>
 *   <li>Resolve the original node from the {@code id} template variable in the
 *       workspace SpacesStore.</li>
 *   <li>Look up the associated working copy and validate that it exists and actually
 *       carries the {@link ContentModel#ASPECT_WORKING_COPY working copy} aspect.</li>
 *   <li>Verify the caller has the Alfresco cancel-checkout permission and is either the
 *       working copy owner or a library administrator
 *       ({@link LibraryPermissions#LIBADMIN}).</li>
 *   <li>Cancel the checkout, releasing the lock and removing the working copy.</li>
 * </ul>
 *
 * <p>On success the response model exposes {@code originalNodeId}. Access violations
 * result in an HTTP {@code 403 Forbidden}, other failures in an HTTP
 * {@code 406 Not Acceptable}.
 */
public class ContentIdCancelCheckout extends CircabcDeclarativeWebScript {

  /** Business service used to resolve the working copy and perform the cancel-checkout operation. */
  @Autowired
  private CociContentBusinessSrv cociContentBusinessSrv;

  /** Alfresco node service used to check node existence and aspects. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user holds the required permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Logger for this webscript. */
  static final Log logger = LogFactory.getLog(ContentIdCancelCheckout.class);

  /**
   * Handles the cancel-checkout request for the content node identified by the
   * {@code id} URL template variable.
   *
   * <p>Resolves the working copy for the original node, validates it, checks the
   * caller's permissions and cancels the checkout. Failures are reported through the
   * {@code status} object rather than by propagating exceptions.
   *
   * @param req the web script request; must supply the {@code id} template variable
   *            identifying the original node in the workspace SpacesStore
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 406} on any other error
   * @param cache the cache control object for the response
   * @return a model map containing {@code originalNodeId} on success, or {@code null}
   *         when an error occurred and the status has been set accordingly
   * @throws IllegalArgumentException if the working copy cannot be found or the
   *         resolved node is not a working copy
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

    NodeRef orginalNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      id
    );

    NodeRef workingCopyRef = cociContentBusinessSrv.getWorkingCopy(
      orginalNodeRef
    );

    if (!this.nodeService.exists(workingCopyRef)) {
      throw new IllegalArgumentException(
        "the working copy for document with id '" + id + "' could not be found."
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
      MLPropertyInterceptor.setMLAware(false);

      if (
        !this.currentUserPermissionCheckerService.hasAlfCancelCheckoutPermission(
          workingCopyRef.getId()
        )
      ) {
        throw new AccessDeniedException(
          "Cannot cancel checkout, not enough permissions"
        );
      }
      boolean isWorkingCopyOwner =
        this.currentUserPermissionCheckerService.isWorkingCopyOwner(
          workingCopyRef.getId()
        );
      boolean isLibAdmin =
        this.currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
          workingCopyRef.getId(),
          LibraryPermissions.LIBADMIN
        );

      if (!isWorkingCopyOwner && !isLibAdmin) {
        throw new AccessDeniedException(
          "Cannot checkin, not enough permissions"
        );
      }

      NodeRef nodeRef = this.cociContentBusinessSrv.cancelCheckOut(
        workingCopyRef
      );

      model.put("originalNodeId", nodeRef.getId());
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when try to cancel check out node " +
          orginalNodeRef.toString(),
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error when try to cancel check out node " + orginalNodeRef.toString(),
        e
      );
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
