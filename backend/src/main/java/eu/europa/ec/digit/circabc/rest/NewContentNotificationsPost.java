package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.model.NotifiableUser;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import java.io.IOException;
import java.util.*;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * REST endpoint (HTTP POST) that triggers "new content" notifications for a set of
 * newly added nodes located under a given parent node.
 *
 * <p>The parent node is identified by the {@code id} template variable in the request
 * URL. The request body is expected to contain a JSON list of node identifiers (parsed
 * via {@link SimpleIdJsonParser}) representing the newly created content items to notify
 * about. Before sending, the endpoint verifies that the current user holds Alfresco write
 * permission on every referenced node; if any node fails the check the whole request is
 * rejected with HTTP 403 (Forbidden).
 *
 * <p>When validation succeeds, the subscribers of the parent node are resolved through the
 * {@link NotificationSubscriptionService} and a bulk document notification
 * ({@link MailTemplate#NOTIFY_DOC_BULK}) is dispatched via the {@link NotificationService}.
 * An optional {@code language} request parameter controls the locale used to render the
 * notification content.
 *
 * <p>This class extends {@link CircabcDeclarativeWebScript} and implements the endpoint
 * logic in {@link #executeImpl(WebScriptRequest, Status, Cache)}.
 */
public class NewContentNotificationsPost extends CircabcDeclarativeWebScript {

  /** Logger used to report errors raised while processing the request. */
  static final Log logger = LogFactory.getLog(
    NewContentNotificationsPost.class
  );

  /** Service used to verify that the current user has write permission on the target nodes. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Service responsible for building and sending the notification e-mails. */
  @Autowired
  @Qualifier("CircabcNotificationService")
  @SuppressWarnings("java:S6830") // Application-defined bean name
  private NotificationService notificationService;

  /** Service used to resolve the users subscribed to notifications for the parent node. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Handles the POST request: validates permissions and sends "new content" notifications
   * for the requested nodes to the subscribers of the parent node.
   *
   * <p>The parent node is read from the {@code id} URL template variable and the list of
   * newly added node identifiers is parsed from the request body. The optional
   * {@code language} request parameter is used to set up the locale for the notification
   * content. On success an empty model map is returned; on failure the response status is
   * set accordingly and {@code null} is returned.
   *
   * @param req the incoming web script request; provides the {@code id} template variable,
   *            the optional {@code language} parameter and the JSON body of node identifiers
   * @param status the web script response status, updated with the appropriate HTTP code and
   *               message when an error occurs
   * @param cache the web script cache control object (unused)
   * @return an (empty) model map when the notifications were sent successfully, or
   *         {@code null} when an error occurred and the status has been set
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    setupLocale(req.getParameter("language"));

    try {
      NodeRef parentRef = Converter.createNodeRefFromId(
        req.getServiceMatch().getTemplateVars().get("id")
      );
      List<String> reqNodeRefs = SimpleIdJsonParser.parseListOfId(req);
      validatePermissions(reqNodeRefs);

      List<NodeRef> nodeRefs = convertToNodeRefs(reqNodeRefs);
      Set<NotifiableUser> notifiableUsers =
        notificationSubscriptionService.getNotifiableUsers(parentRef);
      notificationService.notifyNewFiles(
        parentRef,
        nodeRefs,
        notifiableUsers,
        MailTemplate.NOTIFY_DOC_BULK
      );
    } catch (AccessDeniedException e) {
      return handleError(
        status,
        Status.STATUS_FORBIDDEN,
        "Access denied for guest",
        e
      );
    } catch (IOException | ParseException e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error - bad arguments",
        e
      );
    } catch (Exception e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Internal server error",
        e
      );
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  private void setupLocale(String language) {
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
  }

  private void validatePermissions(List<String> nodeRefs) {
    for (String nodeRef : nodeRefs) {
      if (
        !currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeRef)
      ) {
        throw new AccessDeniedException(
          "Cannot fire notifications on all nodes ! Not enough permissions"
        );
      }
    }
  }

  private List<NodeRef> convertToNodeRefs(List<String> nodeRefStrings) {
    List<NodeRef> nodeRefs = new ArrayList<>();
    for (String nodeRef : nodeRefStrings) {
      nodeRefs.add(Converter.createNodeRefFromId(nodeRef));
    }
    return nodeRefs;
  }

  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    logger.error(ERROR_OCCURRED, e);
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
