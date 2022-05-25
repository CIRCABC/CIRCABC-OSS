package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author beaurpi
 */
public class NewContentNotificationsPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(NewContentNotificationsPost.class);

    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NotificationService notificationService;
    private NotificationSubscriptionService notificationSubscriptionService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        boolean mlAware = MLPropertyInterceptor.isMLAware();
        String language = req.getParameter("language");
        if (language == null) {
            MLPropertyInterceptor.setMLAware(true);
        } else {
            Locale locale = new Locale(language);
            I18NUtil.setContentLocale(locale);
            I18NUtil.setLocale(locale);
            MLPropertyInterceptor.setMLAware(false);
        }

        try {
            NodeRef parentRef = Converter.createNodeRefFromId(templateVars.get("id"));
            List<String> reqNodeRefs = SimpleIdJsonParser.parseListOfId(req);
            boolean hasAllWritePermission = true;
            for (String nodeRef : reqNodeRefs) {
                if (!currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeRef)) {
                    hasAllWritePermission = false;
                    break;
                }
            }

            if (!hasAllWritePermission) {
                throw new AccessDeniedException(
                        "Cannot fire notifications on all nodes ! Not enough permissions");
            }

            List<NodeRef> nodeRefs = new ArrayList<>();
            for (String nodeRef : reqNodeRefs) {
                nodeRefs.add(Converter.createNodeRefFromId(nodeRef));
            }

            Set<NotifiableUser> notifiableUsers =
                    notificationSubscriptionService.getNotifiableUsers(parentRef);
            notificationService.notifyNewFiles(
                    parentRef, nodeRefs, notifiableUsers, MailTemplate.NOTIFY_DOC_BULK);

        } catch (AccessDeniedException e) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied for guest");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } catch (IOException | ParseException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Internal server error - bad arguments");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public NotificationSubscriptionService getNotificationSubscriptionService() {
        return notificationSubscriptionService;
    }

    public void setNotificationSubscriptionService(
            NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }
}
