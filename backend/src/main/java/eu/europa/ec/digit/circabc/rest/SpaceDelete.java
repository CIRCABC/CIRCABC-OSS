package eu.europa.ec.digit.circabc.rest;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.SpacesApi;
import io.swagger.model.NotifiableUser;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST webscript endpoint that deletes a space (folder-like node) from the
 * Alfresco workspace store.
 *
 * <p>The class name follows the CIRCABC {@code <Entity><Method>} convention, so
 * this endpoint implements the HTTP {@code DELETE} behavior for a single space.
 * The space to delete is identified by the {@code id} path template variable,
 * which is resolved against {@link StoreRef#STORE_REF_WORKSPACE_SPACESSTORE}.
 *
 * <p>Behavior and inputs:
 * <ul>
 *   <li>{@code id} (path variable) &mdash; node id of the space to delete.</li>
 *   <li>{@code language} (optional request parameter) &mdash; when supplied,
 *       sets the content/UI locale and disables multilingual awareness;
 *       otherwise multilingual awareness is enabled.</li>
 *   <li>{@code notify} (optional request parameter) &mdash; defaults to
 *       {@code true}; when enabled, subscribed users are notified of the
 *       deletion using the {@link MailTemplate#NOTIFY_DELETE_BULK} template.</li>
 * </ul>
 *
 * <p>Deletion is only performed if the current user has the Alfresco delete
 * permission on the target node; otherwise an {@link AccessDeniedException} is
 * raised and translated into an HTTP 403 response. Invalid node references or
 * node types map to HTTP 400, and any other failure maps to HTTP 500.
 *
 * @see CircabcDeclarativeWebScript
 * @see SpacesApi
 */
public class SpaceDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpaceDelete.class);

  /** API providing the space business operations, including deletion. */
  @Autowired
  private SpacesApi spacesApi;

  /** Service used to send deletion notifications to subscribed users. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /** Service used to verify the current user's Alfresco permissions. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Service resolving the set of users subscribed to notifications for a node. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Executes the space deletion request.
   *
   * <p>Resolves the target space from the {@code id} path variable, optionally
   * adjusts the locale based on the {@code language} parameter, optionally
   * notifies subscribed users (controlled by the {@code notify} parameter),
   * verifies the current user's delete permission, records pre-delete state and
   * then deletes the space via {@link SpacesApi#spaceDelete(String)}.
   *
   * <p>On success the returned model contains {@code result=ok}. On failure the
   * appropriate HTTP status is set on {@code status} and {@code null} is
   * returned so the framework renders the error redirect. The previous
   * multilingual-awareness flag is always restored before returning.
   *
   * @param req the web script request; supplies the {@code id} path variable
   *            and the optional {@code language} and {@code notify} parameters
   * @param status the response status, updated with an error code, message and
   *               redirect flag when the deletion cannot be completed
   * @param cache the response cache control (unused)
   * @return a model map containing {@code result=ok} on success, or
   *         {@code null} when an error status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    boolean notify = true;
    if (req.getParameter("notify") != null) {
      notify = "true".equals(req.getParameter("notify"));
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("id");
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

    if (notify) {
      Set<NotifiableUser> users =
        notificationSubscriptionService.getNotifiableUsers(nodeRef);
      final List<NodeRef> nodeRefs = new ArrayList<>();

      nodeRefs.add(nodeRef);

      notificationService.notifyNewFiles(
        nodeRef,
        nodeRefs,
        users,
        MailTemplate.NOTIFY_DELETE_BULK
      );
    }

    try {
      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoDeletePermission(
          id
        )
      ) {
        throw new AccessDeniedException(
          "Cannot delete the space, not enough permissions"
        );
      }
      this.recordBeforeDelete(id);
      this.spacesApi.spaceDelete(id);
      model.put("result", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(
        "Access denied when trying to delete space with ID: " + id,
        ade
      );
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error("Invalid node reference for space with ID: " + id, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error("Invalid node type for space with ID: " + id, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (Exception e) {
      logger.error("Unexpected error when deleting space with ID: " + id, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
