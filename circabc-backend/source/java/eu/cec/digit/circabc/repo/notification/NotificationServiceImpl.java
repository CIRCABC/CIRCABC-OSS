/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.notification;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationType;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.Services;
import io.swagger.model.AppMessage;
import io.swagger.model.News;
import io.swagger.model.UserProfile;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.MailSendException;

import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author filipsl
 */
public class NotificationServiceImpl implements NotificationService {

    private static final Log logger = LogFactory.getLog(NotificationServiceImpl.class);
    private static final Locale DEFAULT_MAIL_LOCALE = new Locale("en");

    // the services to inject
    private NodeService nodeService;
    private DynamicPropertyService dynamicPropertyService;
    /**
     * Mail Service reference
     */
    private MailService mailService;
    /**
     * Dictionary Service reference
     */
    private DictionaryService dictionaryService;
    /**
     * The mail Preferences Service
     */
    private MailPreferencesService mailPreferencesService;

    private IGRootProfileManagerService igRootProfileManagerService;

    private transient ManagementService managementService;

    private AuthenticationService authenticationService;

    private LogService logService;

    private PersonService personService;

    /*
     * (non-Javadoc)
     *
     * @see
     * eu.cec.digit.circabc.service.notification.NotificationService#notify(org.
     * alfresco.service.cmr.repository.NodeRef)
     */
    public void notify(final NodeRef nodeRef, final Set<NotifiableUser> users) throws Exception {
        if (users == null || users.size() < 1) {
            // no user to notify ... exit ...
            return;
        }

        if (nodeRef == null) {
            // node not specfied
            return;
        }

        if (getNodeService().exists(nodeRef)) {
            if (getNodeService().hasAspect(nodeRef, RenditionModel.ASPECT_HIDDEN_RENDITION)) {
                // node has hidden rendition aspect so we do not notify
                return;
            }
            // find it's type so we can see if it's a node we are interested in
            final QName type = getNodeService().getType(nodeRef);

            if (ForumModel.TYPE_POST.equals(type)
                    || getDictionaryService().isSubClass(type, ForumModel.TYPE_POST)) {
                notifyImplForPost(nodeRef, users);
            } else if (CircabcModel.TYPE_INFORMATION_NEWS.equals(type)) {
                notifyImplForNews(nodeRef, users);
            } else {
                notifyImplForContent(nodeRef, users);
            }
        }
    }

    public void notifyNewEdition(final NodeRef nodeRef, final Set<NotifiableUser> users)
            throws Exception {
        if (users == null || users.size() < 1) {
            // no user to notify ... exit ...
            return;
        }

        if (nodeRef == null) {
            // node not specfied
            return;
        }

        if (getNodeService().exists(nodeRef)) {
            if (getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
                notifyImplForNewEdition(nodeRef, users);
            }
        }
    }

    private void LogNotification(
            NodeRef nodeRef, String to, String service, boolean ok, boolean AdminLog) {
        LogRecord logRecord = new LogRecord();
        logRecord.setActivity("Send Notification");
        logRecord.setService(service);
        logRecord.setInfo("Node: " + getBestTitle(nodeRef) + "; To: " + to);
        logRecord.setOK(ok);

        logRecord.setDocumentID(
                (Long) getNodeService().getProperty(nodeRef, ContentModel.PROP_NODE_DBID));

        if (AdminLog) {
            final NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
            logRecord.setIgID(
                    (Long) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord.setIgName(
                    (String) getNodeService().getProperty(circabcNodeRef, ContentModel.PROP_NAME));
        } else {
            final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(nodeRef);
            if (igNodeRef != null) {
                logRecord.setIgID(
                        (Long) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NODE_DBID));
                logRecord.setIgName(
                        (String) getNodeService().getProperty(igNodeRef, ContentModel.PROP_NAME));
            }
        }

        logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
        Path path = getNodeService().getPath(nodeRef);
        String displayPath = PathUtils.getCircabcPath(path, true);
        displayPath =
                displayPath.endsWith("contains")
                        ? displayPath.substring(0, displayPath.length() - "contains".length())
                        : displayPath;
        logRecord.setPath(displayPath);

        getLogService().log(logRecord);
    }

    protected String getBestTitle(final NodeRef nodeRef) {
        final String name = (String) getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME);

        final Serializable titleObj = getNodeService().getProperty(nodeRef, ContentModel.PROP_TITLE);

        if (titleObj != null && titleObj instanceof MLText) {
            MLText title = (MLText) titleObj;
            return title.getDefaultValue();
        } else if (titleObj != null && titleObj instanceof String) {
            return (String) titleObj;
        } else {
            return name;
        }
    }

    private void notifyImplForNewEdition(final NodeRef nodeRef, final Set<NotifiableUser> users)
            throws Exception {

        String email = null;
        Locale userLocale = null;
        MailWrapper mail = null;
        Map<String, Object> model = null;
        boolean connectFailed = false;
        String igTitle = null;
        // iterate users and send them emails
        for (final NotifiableUser user : users) {
            email = user.getEmailAddress();
            userLocale = user.getNotificationLanguage();
            if (userLocale == null) {
                userLocale = DEFAULT_MAIL_LOCALE;
            }

            if (igTitle == null) {

                igTitle = getCurrentIgTitle(nodeRef);
            }

            model = getMailPreferencesService().buildDefaultModel(nodeRef, user.getPerson(), null);
            mail =
                    getMailPreferencesService()
                            .getDefaultMailTemplate(nodeRef, MailTemplate.ADD_NEW_TRANSLATION_EDITION);

            boolean result = false;
            if (email != null) {
                try {
                    result =
                            getMailService()
                                    .send(
                                            getMailFrom(),
                                            email,
                                            null,
                                            mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                            mail.getBody(model, userLocale),
                                            true,
                                            false);
                } catch (final MailSendException mse) {
                    if (!connectFailed) {
                        // Don't want to log all errors for the same kind of
                        // exception
                        connectFailed = true;
                        if (logger.isErrorEnabled()) {
                            logger.error("Could not connect to Mail Server:", mse);
                        }
                    }
                } catch (final Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Could not send notification to user:" + user, e);
                    }
                }
            }
            String to = (email == null ? user.getUserName() : email);
            to = (to == null ? "email and user name are null!" : to);

            LogNotification(nodeRef, to, "Library", result, false);
            // remove duplication of log
            // LogNotification(nodeRef, to, "Library", result, true);

        }
    }

    private void notifyImplForContent(final NodeRef nodeRef, final Set<NotifiableUser> users)
            throws Exception {

        String email = null;
        Locale userLocale = null;
        MailWrapper mail = null;
        Map<String, Object> model = null;
        boolean connectFailed = false;
        String igTitle = null;
        // iterate users and send them emails
        for (final NotifiableUser user : users) {
            email = user.getEmailAddress();
            userLocale = user.getNotificationLanguage();
            if (userLocale == null) {
                userLocale = DEFAULT_MAIL_LOCALE;
            }

            if (igTitle == null) {

                igTitle = getCurrentIgTitle(nodeRef);
            }

            model = getMailPreferencesService().buildDefaultModel(nodeRef, user.getPerson(), null);
            mail = getMailPreferencesService().getDefaultMailTemplate(nodeRef, MailTemplate.NOTIFY_DOC);

            boolean result = false;
            if (email != null) {
                try {
                    result =
                            getMailService()
                                    .send(
                                            getMailFrom(),
                                            email,
                                            null,
                                            mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                            mail.getBody(model, userLocale),
                                            true,
                                            false);
                } catch (final MailSendException mse) {
                    if (!connectFailed) {
                        // Don't want to log all errors for the same kind of
                        // exception
                        connectFailed = true;
                        if (logger.isErrorEnabled()) {
                            logger.error("Could not connect to Mail Server:", mse);
                        }
                    }
                } catch (final Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Could not send notification to user:" + user, e);
                    }
                }
            }
            String to = (email == null ? user.getUserName() : email);
            to = (to == null ? "email and user name are null!" : to);

            LogNotification(nodeRef, to, "Library", result, false);
            // remove duplication of log
            // LogNotification(nodeRef, to, "Library", result, true);

        }
    }

    private void notifyImplForNews(final NodeRef nodeRef, final Set<NotifiableUser> users)
            throws Exception {

        String email = null;
        Locale userLocale = null;
        MailWrapper mail = null;
        Map<String, Object> model = null;
        boolean connectFailed = false;
        String igTitle = null;
        // iterate users and send them emails
        for (final NotifiableUser user : users) {
            email = user.getEmailAddress();
            userLocale = user.getNotificationLanguage();
            if (userLocale == null) {
                userLocale = DEFAULT_MAIL_LOCALE;
            }

            if (igTitle == null) {

                igTitle = getCurrentIgTitle(nodeRef);
            }

            model = getMailPreferencesService().buildDefaultModel(nodeRef, user.getPerson(), null);
            mail = getMailPreferencesService().getDefaultMailTemplate(nodeRef, MailTemplate.NOTIFY_NEWS);

            String pattern = nodeService.getProperty(nodeRef, CircabcModel.PROP_NEWS_PATTERN).toString();

            if (News.PatternEnum.IFRAME.equals(News.PatternEnum.fromValue(pattern))) {
                model.put("urlLink", nodeService.getProperty(nodeRef, CircabcModel.PROP_NEWS_URL));
            }

            boolean result = false;
            if (email != null) {
                try {
                    result =
                            getMailService()
                                    .send(
                                            getMailFrom(),
                                            email,
                                            null,
                                            mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                            mail.getBody(model, userLocale),
                                            true,
                                            false);
                } catch (final MailSendException mse) {
                    if (!connectFailed) {
                        // Don't want to log all errors for the same kind of
                        // exception
                        connectFailed = true;
                        if (logger.isErrorEnabled()) {
                            logger.error("Could not connect to Mail Server:", mse);
                        }
                    }
                } catch (final Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Could not send notification to user:" + user, e);
                    }
                }
            }
            String to = (email == null ? user.getUserName() : email);
            to = (to == null ? "email and user name are null!" : to);

            LogNotification(nodeRef, to, "Information", result, false);
        }
    }

    private void notifyImplForPost(final NodeRef nodeRef, final Set<NotifiableUser> users)
            throws Exception {
        String email = null;
        Locale userLocale = null;
        MailWrapper mail = null;
        Map<String, Object> model = null;
        String igTitle = null;

        // iterate users and send them emails
        for (final NotifiableUser user : users) {
            email = user.getEmailAddress();
            userLocale = user.getNotificationLanguage();
            if (userLocale == null) {
                userLocale = DEFAULT_MAIL_LOCALE;
            }

            if (igTitle == null) {

                igTitle = getCurrentIgTitle(nodeRef);
            }

            model = getMailPreferencesService().buildDefaultModel(nodeRef, user.getPerson(), null);
            mail = getMailPreferencesService().getDefaultMailTemplate(nodeRef, MailTemplate.NOTIFY_POST);

            boolean result = false;
            if (email != null) {
                result =
                        getMailService()
                                .send(
                                        getMailFrom(),
                                        email,
                                        null,
                                        mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                        mail.getBody(model, userLocale),
                                        true,
                                        false);
            }
            String to = (email == null ? user.getUserName() : email);
            to = (to == null ? "email and user name are null!" : to);
            LogNotification(nodeRef, to, "Newsgroup", result, false);
            // remove duplication of log
            // LogNotification(nodeRef, to, "Newsgroup", result, true);
        }
    }

    private String getCurrentIgTitle(NodeRef nodeRef) {
        NodeRef igRef = getManagementService().getCurrentInterestGroup(nodeRef);
        Serializable title = getNodeService().getProperty(igRef, ContentModel.PROP_TITLE);
        if (title != null && title instanceof MLText) {
            return ((MLText) title).getDefaultValue();
        } else if (title != null && title instanceof String) {
            return title.toString();
        } else {
            return getNodeService().getProperty(igRef, ContentModel.PROP_NAME).toString();
        }
    }

    private String getSafeProperty(
            Locale locale, final QName qname, final Map<QName, Serializable> props) {
        if (locale == null) {
            locale = DEFAULT_MAIL_LOCALE;
        }

        final Serializable value = props.get(qname);

        return getSafeValue(locale, value);
    }

    private String getSafeValue(Locale locale, final Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof MLText) {
            final MLText mlValues = (MLText) value;

            if (mlValues.containsKey(locale)) {
                return mlValues.get(locale);
            } else if (mlValues.containsKey(DEFAULT_MAIL_LOCALE)) {
                return mlValues.get(DEFAULT_MAIL_LOCALE);
            } else {
                return mlValues.getClosestValue(locale);
            }

        } else if (value instanceof List) {
            final List list = (List) value;
            final StringBuilder buff = new StringBuilder("");
            boolean first = true;

            for (final Object object : list) {
                if (first) {
                    first = false;
                } else {
                    buff.append(", ");
                }

                buff.append(getSafeValue(locale, object));
            }

            return buff.toString();

        } else if (value instanceof NodeRef) {
            // for node ref, get arbitrary the title
            return getSafeProperty(
                    locale, ContentModel.PROP_TITLE, getNodeService().getProperties((NodeRef) value));
        } else {
            return value.toString();
        }
    }

    /**
     * @return the mailFrom
     */
    public String getMailFrom() {
        return getMailService().getNoReplyEmailAddress();
    }

    /**
     * @return the mailService
     */
    private MailService getMailService() {
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public final void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @return the nodeService
     */
    private NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    /**
     * @param dictionaryService The DictionaryService to set.
     */
    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the nodePreferencesService
     */
    private MailPreferencesService getMailPreferencesService() {
        return mailPreferencesService;
    }

    /**
     * @param nodePreferencesService the nodePreferencesService to set
     */
    public final void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    /**
     * @return the managementService
     */
    public ManagementService getManagementService() {
        if (managementService == null) {
            managementService =
                    Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                            .getManagementService();
        }
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the logService
     */
    public LogService getLogService() {
        return logService;
    }

    /**
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * @return the authenticationService
     */
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void notify(NodeRef nodeRef, List<String> mails, MailTemplate templateType)
            throws Exception {

        if (mails == null || mails.size() < 1) {
            // no user to notify ... exit ...
            return;
        }

        if (nodeRef == null) {
            // node not specfied
            return;
        }

        if (getNodeService().exists(nodeRef)) {
            // find it's type so we can see if it's a node we are interested in
            final QName nodeType = getNodeService().getType(nodeRef);

            if (ForumModel.TYPE_POST.equals(nodeType)
                    || getDictionaryService().isSubClass(nodeType, ForumModel.TYPE_POST)) {
                notifyImplForPost(nodeRef, mails, templateType);
            } else {
                notifyImplForContent(nodeRef, mails, templateType);
            }
        }
    }

    private void notifyImplForContent(
            NodeRef nodeRef, List<String> mails, MailTemplate templateType) {

        MailWrapper mail = null;
        Map<String, Object> model = null;
        boolean connectFailed = false;
        String igTitle = null;

        // iterate users and send them emails

        for (final String email : mails) {

            model = getMailPreferencesService().buildDefaultModel(nodeRef, null, null);
            mail = getMailPreferencesService().getDefaultMailTemplate(nodeRef, templateType);

            if (igTitle == null) {

                igTitle = getCurrentIgTitle(nodeRef);
            }

            boolean result = false;
            if (email != null) {
                try {

                    result =
                            getMailService()
                                    .send(
                                            getMailFrom(),
                                            email,
                                            null,
                                            mail.getSubject(model, DEFAULT_MAIL_LOCALE) + " [ " + igTitle + " ]",
                                            mail.getBody(model, DEFAULT_MAIL_LOCALE),
                                            true,
                                            false);
                    LogNotification(nodeRef, email, "Auto-upload", result, true);
                } catch (final MailSendException mse) {
                    if (!connectFailed) {
                        // Don't want to log all errors for the same kind of
                        // exception
                        connectFailed = true;
                        if (logger.isErrorEnabled()) {
                            logger.error("Could not connect to Mail Server:", mse);
                        }
                    }

                    LogNotification(nodeRef, email, "Auto-upload", result, false);
                } catch (final Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Could not send notification to user:" + email, e);
                    }

                    LogNotification(nodeRef, email, "Auto-upload", result, false);
                }
            }
        }
    }

    private void notifyImplForPost(NodeRef nodeRef, List<String> mails, MailTemplate templateType) {
    }

    /**
     * * currently notification service only provide methods to notify about upload & edition.... As
     * we need to send notification when inviting users, we need another method more generic (can be
     * used in future for many kind of notifications)
     */
    @Override
    @Auditable(parameters = {"nodeRef", "users", "notificationType"})
    public void notify(NodeRef nodeRef, Set<NotifiableUser> users, NotificationType notificationType, String notificationText, Date expirationDate)
            throws Exception {

        if (nodeRef != null) {
            for (NotifiableUser nUser : users) {
                if (notificationType.equals(NotificationType.NOTIFY_USER_INVITATION)) {
                    //we only add the notification text for the invite user when c
                    notifyImplInviteUser(nodeRef, nUser, notificationText, expirationDate);
                } else if (notificationType.equals(NotificationType.NOTIFY_USER_MEMBERSHIP_UPDATE)) {
                    notifyImplUpdateUser(nodeRef, nUser, expirationDate);
                }
            }
        }
    }

    private void notifyImplMembershipChangeUserToAdmin(
            NodeRef nodeRef, NotifiableUser nUser, UserProfile newMember, Boolean updateOfProfile)
            throws MessagingException {
        String email = nUser.getEmailAddress();
        Locale userLocale = nUser.getNotificationLanguage();
        String igTitle = null;

        if (userLocale == null) {
            userLocale = DEFAULT_MAIL_LOCALE;
        }

        if (igTitle == null) {

            igTitle = getCurrentIgTitle(nodeRef);
        }

        NodeRef person = personService.getPerson(newMember.getUser().getUserId());

        Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(nodeRef, person, null);

        MailWrapper mail = null;

        if (updateOfProfile) {
            mail =
                    getMailPreferencesService()
                            .getDefaultMailTemplate(nodeRef, MailTemplate.UPDATE_MEMBERSHIP_NOTIFICATION);

        } else {
            mail =
                    getMailPreferencesService()
                            .getDefaultMailTemplate(nodeRef, MailTemplate.ADD_MEMBERSHIP_NOTIFICATION);
        }

        String profile = newMember.getProfile().getName();
        if (newMember.getProfile().getTitle() != null
                && !"".equals(newMember.getProfile().getTitle().getDefaultValue())) {
            profile = newMember.getProfile().getTitle().getDefaultValue();
        }
        model.put("profile", profile);

        String currentUsername = authenticationService.getCurrentUserName();
        NodeRef currentPerson = personService.getPerson(currentUsername);

        model.put("me", currentPerson);

        model.put("targetUser", nUser.getPerson());

        boolean result = false;
        if (email != null && mail != null) {
            result =
                    getMailService()
                            .send(
                                    getMailFrom(),
                                    email,
                                    null,
                                    mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                    mail.getBody(model, userLocale),
                                    true,
                                    false);
        }

        String to = (email == null ? nUser.getUserName() : email);
        to = (to == null ? "email and user name are null!" : to);

        LogNotification(nodeRef, to, "Directory", result, false);
    }

    /**
     * @param nodeRef
     * @param nUser
     * @param notificationText
     * @param expirationDate
     * @throws MessagingException
     */
    private void notifyImplInviteUser(NodeRef nodeRef, NotifiableUser nUser, String notificationText,
            Date expirationDate)
            throws MessagingException {
        String email = nUser.getEmailAddress();
        Locale userLocale = nUser.getNotificationLanguage();
        String igTitle = null;

        if (userLocale == null) {
            userLocale = DEFAULT_MAIL_LOCALE;
        }

        if (igTitle == null) {

            igTitle = getCurrentIgTitle(nodeRef);
        }

        Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(nodeRef, nUser.getPerson(), null);
        MailWrapper mail =
                getMailPreferencesService().getDefaultMailTemplate(nodeRef, MailTemplate.INVITE_USER);

        String profile = igRootProfileManagerService.getPersonProfile(nodeRef, nUser.getUserName());
        Profile p = igRootProfileManagerService.getProfile(nodeRef, profile);

        model.put("profile", p.getProfileDisplayName());
        model.put("notifyText", notificationText != null ? notificationText : "");
        model.put("expirationDate", expirationDate);

        boolean result = false;
        if (email != null) {
            result =
                    getMailService()
                            .send(
                                    getMailFrom(),
                                    email,
                                    null,
                                    mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                    mail.getBody(model, userLocale),
                                    true,
                                    false);
        }

        String to = (email == null ? nUser.getUserName() : email);
        to = (to == null ? "email and user name are null!" : to);

        LogNotification(nodeRef, to, "Directory", result, false);
    }

    /**
     * @param nodeRef
     * @param nUser
     * @param expirationDate
     * @throws MessagingException
     */
    private void notifyImplUpdateUser(NodeRef nodeRef, NotifiableUser nUser, Date expirationDate)
            throws MessagingException {
        String email = nUser.getEmailAddress();
        Locale userLocale = nUser.getNotificationLanguage();
        String igTitle = null;

        if (userLocale == null) {
            userLocale = DEFAULT_MAIL_LOCALE;
        }

        if (igTitle == null) {

            igTitle = getCurrentIgTitle(nodeRef);
        }

        Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(nodeRef, nUser.getPerson(), null);
        MailWrapper mail =
                getMailPreferencesService()
                        .getDefaultMailTemplate(nodeRef, MailTemplate.UPDATE_USER_PROFILE);

        String profile = igRootProfileManagerService.getPersonProfile(nodeRef, nUser.getUserName());
        Profile p = igRootProfileManagerService.getProfile(nodeRef, profile);

        model.put("profile", p.getProfileDisplayName());
        model.put("expirationDate", expirationDate);

        boolean result = false;
        if (email != null) {
            result =
                    getMailService()
                            .send(
                                    getMailFrom(),
                                    email,
                                    null,
                                    mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
                                    mail.getBody(model, userLocale),
                                    true,
                                    false);
        }

        String to = (email == null ? nUser.getUserName() : email);
        to = (to == null ? "email and user name are null!" : to);

        LogNotification(nodeRef, to, "Directory", result, false);
    }

    /**
     * @return the igRootProfileManagerService
     */
    public IGRootProfileManagerService getIgRootProfileManagerService() {
        return igRootProfileManagerService;
    }

    /**
     * @param igRootProfileManagerService the igRootProfileManagerService to set
     */
    public void setIgRootProfileManagerService(
            IGRootProfileManagerService igRootProfileManagerService) {
        this.igRootProfileManagerService = igRootProfileManagerService;
    }

    @Override
    public void notifyNewMemberships(
            NodeRef nodeRef, Set<NotifiableUser> admins, UserProfile newMember) throws Exception {

        for (NotifiableUser admin : admins) {
            notifyImplMembershipChangeUserToAdmin(nodeRef, admin, newMember, false);
        }
    }

    @Override
    public void notifyUpdateMemberships(
            NodeRef nodeRef, Set<NotifiableUser> admins, UserProfile newMember) throws Exception {

        for (NotifiableUser admin : admins) {
            notifyImplMembershipChangeUserToAdmin(nodeRef, admin, newMember, true);
        }
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public void notifyNewFiles(
            NodeRef parentRef,
            List<NodeRef> nodeRefs,
            Set<NotifiableUser> notifiableUsers,
            MailTemplate notifyDocBulk) {
        String igTitle = null;
        if (igTitle == null) {

            igTitle = getCurrentIgTitle(parentRef);
        }
        MailWrapper mail =
                getMailPreferencesService().getDefaultMailTemplate(parentRef, notifyDocBulk);

        List<TemplatableNode> nodes = convertToTemplateNodes(nodeRefs, parentRef);

        // iterate users and send them emails
        for (final NotifiableUser user : notifiableUsers) {
            String email = user.getEmailAddress();
            Locale userLocale =
                    user.getNotificationLanguage() != null
                            ? user.getNotificationLanguage()
                            : DEFAULT_MAIL_LOCALE;

            Map<String, Object> model =
                    getMailPreferencesService().buildDefaultModel(parentRef, user.getPerson(), null);
            boolean connectFailed = false;
            model.put("parent", parentRef);
            model.put("nodes", nodes);

            String subject;
            if (nodes.size() == 1 ){
                TemplatableNode  firstNode = nodes.get(0);
                String title =  firstNode.getTitle() ==null ? firstNode.getName() :firstNode.getTitle();
                subject = mail.getSubject(model, userLocale) + " [ " + igTitle + "," + title +  " ]" + " " + firstNode.getUrl() ;
            }else{
                subject = mail.getSubject(model, userLocale) + " [ " + igTitle + " ]";
            }


            boolean result = false;
            if (email != null) {
                try {
                    result =
                            getMailService()
                                    .send(
                                            getMailFrom(),
                                            email,
                                            null,
                                            subject,
                                            mail.getBody(model, userLocale),
                                            true,
                                            false);
                } catch (final MailSendException mse) {
                    if (!connectFailed) {
                        // Don't want to log all errors for the same kind of
                        // exception
                        connectFailed = true;
                        if (logger.isErrorEnabled()) {
                            logger.error("Could not connect to Mail Server:", mse);
                        }
                    }
                } catch (final Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Could not send notification to user:" + user, e);
                    }
                }
            }
            String to = (email == null ? user.getUserName() : email);
            to = (to == null ? "email and user name are null!" : to);

            LogNotification(parentRef, to, "Library", result, false);
        }
    }

    @Override
    public void notify(
            NodeRef nodeRef, List<String> mails, MailTemplate mailTemplate, List<File> files) {

        NodeRef currentNodeRef = null;
        if (nodeRef == null) {
            currentNodeRef = managementService.getCircabcNodeRef();
        } else {
            currentNodeRef = nodeRef;
        }

        MailWrapper mail =
                getMailPreferencesService().getDefaultMailTemplate(currentNodeRef, mailTemplate);
        String userName = authenticationService.getCurrentUserName();
        if (userName.equals(AuthenticationUtil.getSystemUserName())) {
            userName = AuthenticationUtil.getAdminUserName();
        }
        NodeRef userRef = personService.getPerson(userName);
        Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(currentNodeRef, userRef, null);

        String titleComplement = "";

        if (nodeRef != null) {
            titleComplement = "[" + getCurrentIgTitle(nodeRef) + "]";
        }

        for (String emailAddress : mails) {
            try {
                getMailService()
                        .sendWithAttachment(
                                getMailFrom(),
                                emailAddress,
                                null,
                                mail.getSubject(model, DEFAULT_MAIL_LOCALE) + titleComplement,
                                mail.getBody(model, DEFAULT_MAIL_LOCALE),
                                true,
                                false,
                                files);
            } catch (MessagingException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Impossible to send distribution list", e);
                }
            }
        }
    }

    @Override
    public void notifySystemMessage(List<String> mailAddress, AppMessage template) {
        NodeRef currentNodeRef = managementService.getCircabcNodeRef();
        MailWrapper mail =
                getMailPreferencesService()
                        .getDefaultMailTemplate(currentNodeRef, MailTemplate.NOTIFY_SYSTEM_MESSAGE);
        String userName = authenticationService.getCurrentUserName();
        if (userName.equals(AuthenticationUtil.getSystemUserName())) {
            userName = AuthenticationUtil.getAdminUserName();
        }
        NodeRef userRef = personService.getPerson(userName);
        Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(currentNodeRef, userRef, null);

        model.put("systemMessage", template.getContent());
        model.put("showUnsubscribe", true);

        try {
            getMailService()
                    .send(
                            getMailFrom(),
                            mailAddress,
                            null,
                            mail.getSubject(model, DEFAULT_MAIL_LOCALE)
                                    + " [ "
                                    + CircabcConfiguration.getApplicationName()
                                    + " ]",
                            mail.getBody(model, DEFAULT_MAIL_LOCALE),
                            true,
                            true);
        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Impossible to send distribution list", e);
            }
        }
    }

    private List<TemplatableNode> convertToTemplateNodes(List<NodeRef> nodeRefs, NodeRef parentRef) {
        List<TemplatableNode> result = new ArrayList<>();

        NodeRef groupRef = findInterestGroupRoot(parentRef);

        for (NodeRef nodeRef : nodeRefs) {
            TemplatableNode node = new TemplatableNode();
            node.setName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
            Object titleObj = nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
            if (titleObj != null && titleObj instanceof String) {
                node.setTitle((String) titleObj);
            } else if (titleObj != null && titleObj instanceof MLText) {
                node.setTitle(((MLText) titleObj).getDefaultValue());
            }
            Object descObj = nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
            if (descObj != null && descObj instanceof String) {
                node.setDescription((String) descObj);
            } else if (descObj != null && descObj instanceof MLText) {
                node.setDescription(((MLText) descObj).getDefaultValue());
            }
            String modifier = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIER);
            if (modifier != null) {
                NodeRef personRef = personService.getPerson(modifier);
                String firstname = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                String lastname = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
                node.setModifier(firstname + " " + lastname);
            }
            Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
            node.setModified(sdf.format(modified));
            Object keywords = nodeService.getProperty(nodeRef, DocumentModel.PROP_KEYWORD);
            if (keywords != null) {
                String keywordsStr = keywords.toString();
                if (keywordsStr.length() > 2) {
                    keywordsStr = keywordsStr.substring(1, keywordsStr.length() - 1);
                } else {
                    keywordsStr = "";
                }
                node.setKeywords(keywordsStr);
            }
            node.setUrl(
                    CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_URL)
                            + CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT)
                            + (CircabcConfiguration.getProperty(CircabcConfiguration.NEW_UI_CONTEXT).endsWith("/")
                            ? "group/"
                            : "/group/")
                            + (groupRef != null ? groupRef.getId() : "")
                            + "/library/"
                            + nodeRef.getId()
                            + "/details");
            Path path = getNodeService().getPath(nodeRef);
            String pathValue = PathUtils.getLibraryPath(path, true);
            node.setPath(pathValue);

            List<DynamicProperty> dynProps = dynamicPropertyService.getDynamicProperties(nodeRef);
            for (DynamicProperty dn : dynProps) {
                String value =
                        (String)
                                nodeService.getProperty(
                                        nodeRef,
                                        QName.createQName(
                                                DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI, "dynAttr" + dn.getIndex()));
                node.getDynamicProperties().put(dn.getName(), value);
            }
            result.add(node);
        }
        return result;
    }

    private NodeRef findInterestGroupRoot(NodeRef nodeRef) {
        NodeRef parent = null;

        if (getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
            parent = getNodeService().getPrimaryParent(nodeRef).getParentRef();
            while (!getNodeService().hasAspect(parent, CircabcModel.ASPECT_IGROOT)) {
                parent = getNodeService().getPrimaryParent(parent).getParentRef();
            }
        }

        return parent;
    }

    /**
     * @return the dynamicPropertyService
     */
    public DynamicPropertyService getDynamicPropertyService() {
        return dynamicPropertyService;
    }

    /**
     * @param dynamicPropertyService the dynamicPropertyService to set
     */
    public void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.dynamicPropertyService = dynamicPropertyService;
    }
}
