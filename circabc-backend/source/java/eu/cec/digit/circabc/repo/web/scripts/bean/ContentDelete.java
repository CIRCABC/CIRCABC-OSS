package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import io.swagger.api.ContentApi;
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

public class ContentDelete extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(ContentDelete.class);

  private ContentApi contentApi;
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
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    final String id = templateVars.get("id");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = new Locale(language);
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
   * @return the contentApi
   */
  public ContentApi getContentApi() {
    return this.contentApi;
  }

  /**
   * @param contentApi the contentApi to set
   */
  public void setContentApi(ContentApi contentApi) {
    this.contentApi = contentApi;
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
