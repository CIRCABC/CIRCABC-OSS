package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import io.swagger.api.InformationApi;
import io.swagger.model.News;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.NewsJsonParser;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class GroupsInformationNewsPost extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(GroupsInformationNewsPost.class);

    private InformationApi informationApi;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private ThreadPoolExecutor asyncThreadPoolExecutor;
    private TransactionService transactionService;
    private NotificationService notificationService;
    private NotificationSubscriptionService notificationSubscriptionService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("igId");

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

        News finalNews = null;

        try {

            UserTransaction trx = transactionService.getNonPropagatingUserTransaction(false);
            trx.begin();

            NodeRef infRef =
                    this.nodeService.getChildByName(
                            Converter.createNodeRefFromId(id), ContentModel.ASSOC_CONTAINS, "Information");

            if (!this.currentUserPermissionCheckerService.hasAnyOfInformationPermission(
                    infRef.getId(), InformationPermissions.INFMANAGE)) {
                throw new AccessDeniedException(
                        "Not enought permissions to create a News on the information service");
            }

            News news = NewsJsonParser.parse(req);
            finalNews = this.informationApi.groupsIdInformationNewsPost(id, news);
            model.put("newsInfo", finalNews);

            trx.commit();

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (InvalidNodeRefException
                | java.text.ParseException
                | ParseException
                | IOException inre) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad request");
            status.setRedirect(true);
            return null;
        } catch (NotSupportedException
                | HeuristicRollbackException
                | HeuristicMixedException
                | RollbackException
                | IllegalStateException
                | SecurityException
                | SystemException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Internal server error");
            status.setRedirect(true);
            return null;
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
     * @return the informationApi
     */
    public InformationApi getInformationApi() {
        return this.informationApi;
    }

    /**
     * @param informationApi the informationApi to set
     */
    public void setInformationApi(InformationApi informationApi) {
        this.informationApi = informationApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    public NodeService getNodeService() {
        return this.nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public ThreadPoolExecutor getAsyncThreadPoolExecutor() {
        return asyncThreadPoolExecutor;
    }

    public void setAsyncThreadPoolExecutor(ThreadPoolExecutor asyncThreadPoolExecutor) {
        this.asyncThreadPoolExecutor = asyncThreadPoolExecutor;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
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

    private class NewsCreationNotificationRunnable implements Runnable {

        protected final NodeRef newsRef;

        public NewsCreationNotificationRunnable(NodeRef newsRef) {
            this.newsRef = newsRef;
        }

        public void run() {

            transactionService
                    .getRetryingTransactionHelper()
                    .doInTransaction(
                            new RetryingTransactionHelper.RetryingTransactionCallback<Object>() {

                                public Object execute() {

                                    AuthenticationUtil.runAs(
                                            new AuthenticationUtil.RunAsWork<String>() {

                                                public String doWork() {
                                                    try {
                                                        if (newsRef != null) {
                                                            Set<NotifiableUser> users =
                                                                    notificationSubscriptionService.getNotifiableUsers(newsRef);
                                                            notificationService.notify(newsRef, users);
                                                        }
                                                    } catch (Exception e) {
                                                        if (logger.isErrorEnabled()) {
                                                            logger.error(e);
                                                        }
                                                    }

                                                    return null;
                                                }
                                            },
                                            AuthenticationUtil.getSystemUserName());

                                    return null;
                                }
                            },
                            false,
                            false);
        }
    }
}
