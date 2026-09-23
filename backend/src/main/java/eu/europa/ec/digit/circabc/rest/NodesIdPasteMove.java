package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.ClipboardAction;
import io.swagger.api.ClipboardApi;
import io.swagger.model.NotifiableUser;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Alfresco webscript endpoint that performs a clipboard "move" paste operation.
 *
 * <p>Mapped to an HTTP {@code POST} request (as implied by the {@code Move}
 * suffix combined with the mutating clipboard action), this endpoint moves the
 * nodes identified by the {@code nodeIds} request parameter into the target
 * folder whose node id is supplied in the URL path.</p>
 *
 * <p>Key inputs:</p>
 * <ul>
 *   <li>{@code id} (path variable) &mdash; the node id of the destination
 *       folder; the target must exist and be of type {@code cm:folder}.</li>
 *   <li>{@code nodeIds} (request parameter, multi-valued) &mdash; the ids of
 *       the nodes to be moved into the destination folder.</li>
 *   <li>{@code notify} (optional request parameter) &mdash; when {@code "true"}
 *       (the default), subscribers of the destination folder are notified of
 *       the newly moved files.</li>
 * </ul>
 *
 * <p>The caller must hold the Alfresco "add children" permission on the target
 * folder; otherwise the request is rejected with an HTTP {@code 403 Forbidden}
 * status. Unexpected failures are reported with an HTTP {@code 406 Not
 * Acceptable} status.</p>
 *
 * @author schwerr
 */
public class NodesIdPasteMove extends CircabcDeclarativeWebScript {

  /** Logger for this webscript. */
  static final Log logger = LogFactory.getLog(NodesIdPasteMove.class);

  /** Alfresco node service used to validate the destination folder. */
  @Autowired
  private NodeService nodeService;

  /** Clipboard API used to perform the actual move (paste) of the nodes. */
  @Autowired
  private ClipboardApi clipboardApi;

  /** Service used to send notifications about the moved files. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /** Service used to verify the current user's permissions on the target. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Service used to resolve the users subscribed to the destination folder. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Moves the requested nodes into the destination folder and, optionally,
   * notifies the folder's subscribers.
   *
   * <p>Resolves the destination folder from the {@code id} path variable and
   * validates that it exists and is a folder. It then checks that the current
   * user has the Alfresco "add children" permission before delegating the move
   * to the clipboard API. When notification is enabled, subscribers of the
   * destination folder are informed of the moved files using the
   * {@link MailTemplate#NOTIFY_MOVE_BULK} template.</p>
   *
   * @param req the web script request; provides the {@code id} path variable
   *            and the {@code nodeIds} and {@code notify} parameters
   * @param status the response status, updated to {@code 403} on access denial
   *               or {@code 406} on other errors
   * @param cache the cache directives for the response (unused)
   * @return an empty model map on success, or {@code null} when an error has
   *         been signalled through {@code status}
   * @throws IllegalArgumentException if the folder identified by {@code id}
   *                                  does not exist or is not a folder
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

    boolean notify = true;
    if (req.getParameter("notify") != null) {
      notify = "true".equals(req.getParameter("notify"));
    }

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
        ClipboardAction.MOVE.getValue()
      );

      List<NodeRef> nodeRefs = new ArrayList<>();
      for (String nodeRef : nodeIds) {
        nodeRefs.add(Converter.createNodeRefFromId(nodeRef));
      }

      if (notify) {
        Set<NotifiableUser> notifiableUsers =
          notificationSubscriptionService.getNotifiableUsers(folderNodeRef);
        notificationService.notifyNewFiles(
          folderNodeRef,
          nodeRefs,
          notifiableUsers,
          MailTemplate.NOTIFY_MOVE_BULK
        );
      }
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when trying to paste move nodes for id: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Error when trying to paste move nodes for id: " + id, e);
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
