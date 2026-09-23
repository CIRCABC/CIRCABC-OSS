package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.ContentApi;
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
 * REST webscript endpoint that deletes a content node from the Alfresco
 * repository.
 *
 * <p>The class name follows the CIRCABC convention {@code <Entity><Method>},
 * so this endpoint handles the HTTP {@code DELETE} operation for a single
 * content item. The target node is identified by the {@code id} URL template
 * variable, which is resolved against the workspace {@code SpacesStore}.</p>
 *
 * <p>Behavior overview:</p>
 * <ul>
 *   <li>Optional {@code language} request parameter selects the content locale;
 *   when absent the interceptor is left multilingual (ML) aware.</li>
 *   <li>Optional {@code notify} request parameter (defaults to {@code true})
 *   controls whether subscribed users receive a deletion notification e-mail
 *   before the node is removed.</li>
 *   <li>The current user must hold Alfresco delete permission on the node,
 *   otherwise an {@link AccessDeniedException} is raised and a
 *   {@code 403 Forbidden} status is returned.</li>
 * </ul>
 *
 * <p>On success the response model contains {@code result = "ok"}.</p>
 *
 * @see CircabcDeclarativeWebScript
 */
public class ContentDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentDelete.class);

  /** API providing the content operations, including deletion by node id. */
  @Autowired
  private ContentApi contentApi;

  /** Service used to send deletion notification e-mails to subscribed users. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /** Service used to verify that the current user may delete the node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Service used to resolve the set of users subscribed to notifications for a node. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Deletes the content node identified by the {@code id} URL template
   * variable.
   *
   * <p>Reads the optional {@code language} and {@code notify} request
   * parameters, adjusts the multilingual interceptor and content locale
   * accordingly, checks the caller's delete permission, optionally notifies
   * subscribed users, records the pre-delete audit information and finally
   * removes the node. The multilingual awareness flag is always restored in
   * the {@code finally} block.</p>
   *
   * @param req the web script request; supplies the {@code id} template
   *     variable and the optional {@code language} and {@code notify}
   *     parameters
   * @param status the response status, set to {@code 403 Forbidden} on
   *     permission failures or {@code 400 Bad Request} on invalid node
   *     references or types
   * @param cache the cache directives for the response
   * @return a model map containing {@code result = "ok"} on success, or
   *     {@code null} when an error status/redirect has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    String language = req.getParameter("language");
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    final String id = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    try {
      boolean notify = true;
      if (req.getParameter("notify") != null) {
        notify = "true".equals(req.getParameter("notify"));
      }

      if (
        !this.currentUserPermissionCheckerService.hasAlfrescoDeletePermission(
          id
        )
      ) {
        throw new AccessDeniedException("No delete permission for node");
      }

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

      recordBeforeDelete(id);
      this.contentApi.contentIdDelete(id);
      model.put("result", "ok");
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidNodeRefException inre) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (InvalidTypeException ite) {
      logger.error(ERROR_OCCURRED, ite);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
