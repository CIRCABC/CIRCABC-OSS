package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.InformationApi;
import io.swagger.model.News;
import io.swagger.model.NotifiableUser;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NewsJsonParser;
import jakarta.transaction.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
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
 * Alfresco Web Script endpoint that handles the HTTP {@code POST} request used to create a
 * news item in the Information service of a given Interest Group.
 *
 * <p>The endpoint expects the Interest Group identifier ({@code igId}) as a URL template
 * variable and the news content as a JSON payload in the request body (parsed via
 * {@link io.swagger.util.parsers.NewsJsonParser}). An optional {@code language} request
 * parameter controls the locale used when persisting the multilingual content; when it is
 * absent the endpoint operates in ML-aware mode.
 *
 * <p>Before creating the news item, the endpoint verifies that the current user holds the
 * {@link io.swagger.model.permissions.InformationPermissions#INFMANAGE} permission on the
 * Information container of the target Interest Group. Once the news item is created and the
 * transaction has committed, subscribed users are notified asynchronously through a background
 * task.
 *
 * <p>On success the returned model contains the created {@link io.swagger.model.News} under the
 * {@code newsInfo} key. Failures are reported by setting the appropriate HTTP status code
 * (403 for permission errors, 400 for malformed input, 500 for transaction/system errors).
 */
public class GroupsInformationNewsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsInformationNewsPost.class);

  /** API providing the business operations for the Information service. */
  @Autowired
  private InformationApi informationApi;

  /** Alfresco node service used to resolve the Information container node. */
  @Autowired
  private NodeService nodeService;

  /** Service used to check the current user's permissions on the Information service. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Thread pool used to run the news creation notification asynchronously. */
  @Autowired
  @Qualifier("defaultAsyncThreadPool")
  private ThreadPoolExecutor asyncThreadPoolExecutor;

  /** Service used to create and manage explicit user transactions. */
  @Autowired
  private TransactionService transactionService;

  /** Service that sends notifications to users about the newly created news item. */
  @Autowired
  @Qualifier("CircabcNotificationService") // NOSONAR
  private NotificationService notificationService;

  /** Service used to resolve the set of users subscribed to notifications for a node. */
  @Autowired
  private NotificationSubscriptionService notificationSubscriptionService;

  /**
   * Creates a news item in the Information service of the Interest Group identified by the
   * {@code igId} URL template variable.
   *
   * <p>The method resolves the Information container, checks that the current user has the
   * {@code INFMANAGE} permission, parses the news payload from the request body and delegates
   * the creation to {@link InformationApi#groupsIdInformationNewsPost(String, News)} within a
   * dedicated user transaction. After a successful commit, subscribers are notified
   * asynchronously.
   *
   * @param req the web script request; provides the {@code igId} template variable, the optional
   *        {@code language} parameter and the JSON body describing the news item
   * @param status the response status, updated with an error code and message when the request
   *        cannot be processed
   * @param cache the cache directives for the response
   * @return a model map containing the created {@link News} under the {@code newsInfo} key on
   *         success, or {@code null} when an error occurs (in which case {@code status} carries
   *         the corresponding HTTP error code)
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

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

    News finalNews = null;

    try {
      UserTransaction trx = transactionService.getNonPropagatingUserTransaction(
        false
      );
      trx.begin();

      NodeRef infRef = this.nodeService.getChildByName(
        Converter.createNodeRefFromId(id),
        ContentModel.ASSOC_CONTAINS,
        "Information"
      );

      if (
        !this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
          infRef.getId(),
          InformationPermissions.INFMANAGE
        )
      ) {
        throw new AccessDeniedException(
          "Not enought permissions to create a News on the information service"
        );
      }

      News news = NewsJsonParser.parse(req);
      finalNews = this.informationApi.groupsIdInformationNewsPost(id, news);
      model.put("newsInfo", finalNews);

      trx.commit();
    } catch (AccessDeniedException ade) {
      logger.error(ERROR_OCCURRED, ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (
      InvalidNodeRefException
      | java.text.ParseException
      | ParseException
      | IOException inre
    ) {
      logger.error(ERROR_OCCURRED, inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null; // NOSONAR
    } catch (
      NotSupportedException
      | HeuristicRollbackException
      | HeuristicMixedException
      | RollbackException
      | IllegalStateException
      | SecurityException
      | SystemException e
    ) {
      logger.error(ERROR_OCCURRED, e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null; // NOSONAR
    } finally {
      if (finalNews != null) {
        NodeRef newsRef = Converter.createNodeRefFromId(finalNews.getId());

        Runnable runnable = new NewsCreationNotificationRunnable(newsRef);
        asyncThreadPoolExecutor.execute(runnable);
      }

      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * Background task that notifies subscribed users about a newly created news item.
   *
   * <p>It runs as the system user inside a retrying transaction so that notification delivery
   * does not affect the outcome of the request that created the news item.
   */
  private class NewsCreationNotificationRunnable implements Runnable {

    /** Reference to the newly created news node for which subscribers must be notified. */
    protected final NodeRef newsRef;

    /**
     * Creates the notification task for the given news node.
     *
     * @param newsRef the reference to the news node that has just been created
     */
    public NewsCreationNotificationRunnable(NodeRef newsRef) {
      this.newsRef = newsRef;
    }

    /**
     * Resolves the users subscribed to notifications for the news node and sends them a
     * notification. Any error raised while notifying is logged and swallowed so that it does
     * not propagate out of the asynchronous task.
     */
    public void run() {
      transactionService
        .getRetryingTransactionHelper()
        .doInTransaction(
          () -> {
            AuthenticationUtil.runAs(
              () -> {
                try {
                  if (newsRef != null) {
                    Set<NotifiableUser> users =
                      notificationSubscriptionService.getNotifiableUsers(
                        newsRef
                      );
                    notificationService.notify(newsRef, users);
                  }
                } catch (Exception e) {
                  if (logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                  }
                }

                return null; // NOSONAR
              },
              AuthenticationUtil.getSystemUserName()
            );

            return null; // NOSONAR
          },
          false,
          false
        );
    }
  }
}
