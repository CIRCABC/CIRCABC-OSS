package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco Declarative Web Script endpoint that checks out (locks for editing) a
 * content item identified by its node id.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention:
 * {@code ContentIdCheckout} maps to a checkout operation on a content node,
 * exposed as an HTTP {@code POST} request (a mutating, state-changing action).</p>
 *
 * <p>Behavior: given the {@code id} URL template variable, the endpoint resolves
 * the corresponding {@link NodeRef}, verifies it exists and that the current user
 * holds checkout permission, then delegates to
 * {@link CociContentBusinessSrv#checkOut(NodeRef)} to create a working copy.
 * URL-able nodes cannot be checked out. When the optional {@code editInline}
 * request parameter equals {@code "true"}, the working copy is marked as inline
 * editable. Permission inheritance is disabled on the resulting working copy.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} - URL template variable identifying the content node to check out.</li>
 *   <li>{@code editInline} - optional request parameter; when {@code "true"} the
 *       working copy is flagged as inline editable.</li>
 * </ul>
 *
 * <p>On success the model contains the {@code workingCopyId} of the newly created
 * working copy. Access errors yield HTTP {@code 403 Forbidden}; other failures
 * yield HTTP {@code 406 Not Acceptable}.</p>
 *
 * @author schwerr
 */
public class ContentIdCheckout extends CircabcDeclarativeWebScript {

  /** Business service performing the check-in/check-out (COCI) operations. */
  @Autowired
  private CociContentBusinessSrv cociContentBusinessSrv;

  /** Alfresco node service used to resolve nodes and inspect their aspects. */
  @Autowired
  private NodeService nodeService;

  /** Service that verifies the current user's permissions on a node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Alfresco permission service used to manage permission inheritance. */
  @Autowired
  private PermissionService permissionService;

  /** Logger for this web script. */
  static final Log logger = LogFactory.getLog(ContentIdCheckout.class);

  /**
   * Checks out the content node identified by the {@code id} URL template
   * variable, creating a working copy.
   *
   * <p>The method resolves the node, ensures it exists, validates that the
   * current user has checkout permission and that the node is not URL-able,
   * then performs the checkout. If the {@code editInline} request parameter is
   * {@code "true"}, the working copy is marked as inline editable, and permission
   * inheritance is disabled on the working copy.</p>
   *
   * @param req the web script request; supplies the {@code id} template variable
   *            and the optional {@code editInline} parameter
   * @param status the response status, set to {@code 403} on access denial or
   *               {@code 406} on other errors
   * @param cache the cache directives for the response
   * @return a model map containing the {@code workingCopyId} of the created
   *         working copy on success, or {@code null} when an error occurs and a
   *         redirect status has been set
   * @throws IllegalArgumentException if no node exists for the given id or if the
   *         node is URL-able and therefore cannot be checked out
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

    String editInline = req.getParameter("editInline");

    boolean mlAware = MLPropertyInterceptor.isMLAware();

    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    if (!this.nodeService.exists(nodeRef)) {
      throw new IllegalArgumentException(
        "The item with id '" + id + "' could not be found."
      );
    }

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfCheckoutPermission(id)
      ) {
        throw new AccessDeniedException(
          "Cannot checkout, not enough permissions"
        );
      }

      if (nodeService.hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)) {
        throw new IllegalArgumentException("URLs cannot be checked out.");
      }

      MLPropertyInterceptor.setMLAware(false);

      NodeRef workingCopyNodeRef = this.cociContentBusinessSrv.checkOut(
        nodeRef
      );

      if (editInline != null && editInline.equals("true")) {
        final Map<QName, Serializable> editProps = new HashMap<>(1, 1.0f);
        editProps.put(ApplicationModel.PROP_EDITINLINE, true);
        this.nodeService.addAspect(
          workingCopyNodeRef,
          ApplicationModel.ASPECT_INLINEEDITABLE,
          editProps
        );
      }
      permissionService.setInheritParentPermissions(workingCopyNodeRef, false);

      model.put("workingCopyId", workingCopyNodeRef.getId());
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when try to checkout node " + nodeRef.toString(),
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when try to checkout node " + nodeRef.toString(), e);
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
