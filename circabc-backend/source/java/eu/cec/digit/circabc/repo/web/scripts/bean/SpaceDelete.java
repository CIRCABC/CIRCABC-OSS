package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import io.swagger.api.SpacesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpaceDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(SpaceDelete.class);

  private SpacesApi spacesApi;
  private NotificationService notificationService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NotificationSubscriptionService notificationSubscriptionService;

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
      Locale locale = new Locale(language);
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
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    } catch (InvalidTypeException ite) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad noderef type");
      status.setRedirect(true);
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  /**
   * @return the spacesApi
   */
  public SpacesApi getSpacesApi() {
    return this.spacesApi;
  }

  /**
   * @param spacesApi the spacesApi to set
   */
  public void setSpacesApi(SpacesApi spacesApi) {
    this.spacesApi = spacesApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * @param notificationService the notificationService to set
   */
  public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * @param notificationSubscriptionService the notificationSubscriptionService to
   *                                        set
   */
  public void setNotificationSubscriptionService(
    NotificationSubscriptionService notificationSubscriptionService
  ) {
    this.notificationSubscriptionService = notificationSubscriptionService;
  }
}
