package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.CociContentBusinessSrv;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
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
 * Alfresco Declarative Web Script endpoint that checks in a checked-out (working copy) document.
 *
 * <p>Given the node id of the <em>original</em> document (path variable {@code id}), this endpoint
 * resolves its working copy and commits the pending changes back to the original node, effectively
 * ending the check-out. The class name suffix {@code Checkin} maps to a mutating (POST-style)
 * operation in the CIRCABC REST layer.
 *
 * <p>Inputs:
 *
 * <ul>
 *   <li>{@code id} (path variable) - the node id of the original document in the
 *       {@link StoreRef#STORE_REF_WORKSPACE_SPACESSTORE} store.
 *   <li>{@code minorChange} (optional boolean request parameter) - whether the check-in creates a
 *       minor version; defaults to {@code false}.
 *   <li>{@code keepCheckedOut} (optional boolean request parameter) - whether the document should
 *       remain checked out after the check-in; defaults to {@code false}.
 *   <li>{@code endEditInline} (optional boolean request parameter) - whether the inline-editable
 *       aspect should be removed on check-in; defaults to {@code false}.
 *   <li>{@code comment} (optional request parameter) - the version note associated with the
 *       check-in.
 * </ul>
 *
 * <p>The caller must have check-in permission and be the owner of the working copy; otherwise the
 * response status is set to {@code 403 Forbidden}. Other failures result in a
 * {@code 406 Not Acceptable} status.
 *
 * @author schwerr
 */
public class ContentIdCheckin extends CircabcDeclarativeWebScript {

  /** Business service used to resolve the working copy and perform the actual check-in. */
  @Autowired
  private CociContentBusinessSrv cociContentBusinessSrv;

  /** Alfresco node service used to inspect and update repository nodes. */
  @Autowired
  private NodeService nodeService;

  /** Service used to verify that the current user may check in and owns the working copy. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Logger for this web script. */
  static final Log logger = LogFactory.getLog(ContentIdCheckin.class);

  /**
   * Checks in the working copy of the document identified by the {@code id} path variable.
   *
   * <p>The method validates that the working copy exists and carries the working-copy aspect,
   * parses the optional boolean request parameters, enforces check-in permission and working-copy
   * ownership, optionally removes the inline-editable aspect, and delegates the check-in to
   * {@link CociContentBusinessSrv#checkIn}. Multilingual awareness is disabled during the operation
   * and restored afterwards.
   *
   * @param req the web script request; supplies the {@code id} path variable and the optional
   *     {@code minorChange}, {@code keepCheckedOut}, {@code endEditInline} and {@code comment}
   *     parameters
   * @param status the web script response status; set to {@code 403} on access denial or
   *     {@code 406} on other errors
   * @param cache the web script cache directives (unused)
   * @return an (empty) model map on success, or {@code null} when an error status/redirect has been
   *     set
   * @throws IllegalArgumentException if the working copy cannot be found, the referenced item is not
   *     a working copy, or a boolean parameter has an invalid value
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
        "The working copy for document with id '" + id + "' could not be found."
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

    boolean minor = false;

    try {
      String minorChangeString = req.getParameter("minorChange");
      if (minorChangeString != null) {
        minor = Boolean.parseBoolean(minorChangeString);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "The 'minorChange' parameter must be a boolean."
      );
    }

    boolean keepCheckOut = false;

    try {
      String keepCheckOutString = req.getParameter("keepCheckedOut");
      if (keepCheckOutString != null) {
        keepCheckOut = Boolean.parseBoolean(keepCheckOutString);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "The 'keepCheckedOut' parameter must be a boolean."
      );
    }

    boolean endEditInline = false;

    try {
      String endEditInlineString = req.getParameter("endEditInline");
      if (endEditInlineString != null) {
        endEditInline = Boolean.parseBoolean(endEditInlineString);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "The 'endEditInline' parameter must be a boolean."
      );
    }

    String versionNote = req.getParameter("comment");

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfCheckinPermission(
          workingCopyRef.getId()
        )
      ) {
        throw new AccessDeniedException(
          "Cannot checkin, not enough permissions"
        );
      }

      if (
        !this.currentUserPermissionCheckerService.isWorkingCopyOwner(
          workingCopyRef.getId()
        )
      ) {
        throw new AccessDeniedException(
          "Cannot checkin, not enough permissions"
        );
      }
      MLPropertyInterceptor.setMLAware(false);

      if (endEditInline) {
        this.nodeService.removeAspect(
          workingCopyRef,
          ApplicationModel.ASPECT_INLINEEDITABLE
        );
      }

      this.cociContentBusinessSrv.checkIn(
        workingCopyRef,
        minor,
        (versionNote == null) ? "" : versionNote,
        keepCheckOut
      );
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when try to checkin node " + orginalNodeRef.toString(),
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error(
        "Error when try to checkin node " + orginalNodeRef.toString(),
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
