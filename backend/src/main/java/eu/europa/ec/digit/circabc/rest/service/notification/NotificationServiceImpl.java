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
package eu.europa.ec.digit.circabc.rest.service.notification;

import eu.europa.ec.digit.circabc.rest.exception.NotificationException;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import io.swagger.api.CircabcApi;
import io.swagger.api.ProfilesApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.AppMessage;
import io.swagger.model.LogRecord;
import io.swagger.model.News;
import io.swagger.model.NotifiableUser;
import io.swagger.model.TemplatableNode;
import io.swagger.model.UserProfile;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.PathUtils;
import jakarta.mail.MessagingException;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSendException;

/**
 * Default implementation of {@link NotificationService}.
 *
 * <p>This service builds and sends CIRCABC e-mail notifications triggered by repository events such
 * as new documents, new document editions, forum posts, news items, bulk uploads, membership
 * changes and system messages. For each recipient it resolves the appropriate {@link MailTemplate},
 * builds a FreeMarker model (via {@link MailPreferencesService}), localizes the subject/body to the
 * recipient's preferred language (falling back to {@link #DEFAULT_MAIL_LOCALE}) and delegates the
 * actual delivery to the {@link MailService}. Each delivery attempt is audited through the {@link
 * LogService}.
 *
 * <p>Collaborators are wired by Spring using {@code @Autowired}. The class dispatches to different
 * private {@code notifyImplFor*} helpers depending on the type of the target node (post, news or
 * generic content).
 *
 * @author filipsl
 */
public class NotificationServiceImpl implements NotificationService {

  private static final String DIRECTORY = "Directory";
  private static final String PROFILE = "profile";
  private static final String AUTO_UPLOAD = "Auto-upload";
  private static final String LIBRARY = "Library";
  private static final String EMAIL_AND_USER_NAME_ARE_NULL =
    "email and user name are null!";
  private static final String COULD_NOT_SEND_NOTIFICATION_TO_USER =
    "Could not send notification to user:";
  private static final String COULD_NOT_CONNECT_TO_MAIL_SERVER =
    "Could not connect to Mail Server:";
  private static final Log logger = LogFactory.getLog(
    NotificationServiceImpl.class
  );
  /** Locale used for e-mail rendering when a recipient has no preferred notification language. */
  private static final Locale DEFAULT_MAIL_LOCALE = Locale.of("en");

  @Autowired
  private NodeService nodeService;

  @Autowired
  @Qualifier("DynamicPropertyService") // NOSONAR
  private DynamicPropertyService dynamicPropertyService;

  @Autowired
  @Qualifier("CircabcMailService") // NOSONAR
  private MailService mailService;

  @Autowired
  private DictionaryService dictionaryService;

  @Autowired
  private MailPreferencesService mailPreferencesService;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private LogService logService;

  @Autowired
  private PersonService personService;

  @Autowired
  private CircabcConfig circabcConfig;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private ProfilesApi profilesApi;

  @Autowired
  private CircabcApi circabcApi;

  /** Lazily resolved and cached reference to the CIRCABC repository root node. */
  private NodeRef circabcNodeRef;

  /**
   * Returns the CIRCABC repository root node, resolving and caching it on first access.
   *
   * @return the {@link NodeRef} of the CIRCABC root node
   */
  public NodeRef getCircabcNodeRef() {
    if (circabcNodeRef == null) {
      circabcNodeRef = circabcApi.getCircabcNodeRef();
    }
    return circabcNodeRef;
  }

  /**
   * Notifies the given users about a change to the specified node.
   *
   * <p>No action is taken when the user set or the node is {@code null}/empty, when the node does
   * not exist, or when it carries the {@link ContentModel#ASPECT_HIDDEN} aspect. Otherwise the node
   * type is inspected and the notification is dispatched using the matching template: forum posts,
   * information news items and generic content are each handled differently.
   *
   * @param nodeRef the node the notification is about; ignored when {@code null} or non-existent
   * @param users the users to notify; ignored when {@code null} or empty
   * @throws NotificationException if building or sending a notification fails
   */
  public void notify(final NodeRef nodeRef, final Set<NotifiableUser> users)
    throws NotificationException {
    if (users == null || users.isEmpty()) {
      // no user to notify ... exit ...
      return;
    }

    if (nodeRef == null) {
      // node not specfied
      return;
    }

    if (nodeService.exists(nodeRef)) {
      if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN)) {
        // node has hidden rendition aspect so we do not notify
        return;
      }
      // find it's type so we can see if it's a node we are interested in
      final QName type = nodeService.getType(nodeRef);

      try {
        if (
          ForumModel.TYPE_POST.equals(type) ||
          dictionaryService.isSubClass(type, ForumModel.TYPE_POST)
        ) {
          notifyImplForPost(nodeRef, users);
        } else if (CircabcModel.TYPE_INFORMATION_NEWS.equals(type)) {
          notifyImplForNews(nodeRef, users);
        } else {
          notifyImplForContent(nodeRef, users);
        }
      } catch (Exception e) {
        throw new NotificationException("Failed to send notification", e);
      }
    }
  }

  /**
   * Notifies the given users that a new edition (translation) of a multilingual document is
   * available.
   *
   * <p>No action is taken when the user set or the node is {@code null}/empty, or when the node does
   * not exist or lacks the {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT} aspect.
   *
   * @param nodeRef the multilingual document node; ignored when {@code null} or non-existent
   * @param users the users to notify; ignored when {@code null} or empty
   * @throws NotificationException if building or sending the notification fails
   */
  public void notifyNewEdition(
    final NodeRef nodeRef,
    final Set<NotifiableUser> users
  ) throws NotificationException {
    if (users == null || users.isEmpty()) {
      // no user to notify ... exit ...
      return;
    }

    if (nodeRef == null) {
      // node not specfied
      return;
    }

    if (
      nodeService.exists(nodeRef) &&
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ) {
      try {
        notifyImplForNewEdition(nodeRef, users);
      } catch (Exception e) {
        throw new NotificationException(
          "Failed to send new edition notification",
          e
        );
      }
    }
  }

  private void logNotification(
    NodeRef nodeRef,
    String to,
    String service,
    boolean ok,
    boolean adminLog
  ) {
    LogRecord logRecord = new LogRecord();
    logRecord.setActivity("Send Notification");
    logRecord.setService(service);
    logRecord.setInfo("Node: " + getBestTitle(nodeRef) + "; To: " + to);
    logRecord.setOK(ok);

    logRecord.setDocumentID(
      (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID)
    );

    if (adminLog) {
      logRecord.setIgID(
        (Long) nodeService.getProperty(
          getCircabcNodeRef(),
          ContentModel.PROP_NODE_DBID
        )
      );
      logRecord.setIgName(
        (String) nodeService.getProperty(
          getCircabcNodeRef(),
          ContentModel.PROP_NAME
        )
      );
    } else {
      final NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
      if (igNodeRef != null) {
        logRecord.setIgID(
          (Long) nodeService.getProperty(igNodeRef, ContentModel.PROP_NODE_DBID)
        );
        logRecord.setIgName(
          (String) nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME)
        );
      }
    }

    logRecord.setUser(AuthenticationUtil.getFullyAuthenticatedUser());
    Path path = nodeService.getPath(nodeRef);
    String displayPath = PathUtils.getCircabcPath(path, true);
    displayPath = displayPath.endsWith("contains")
      ? displayPath.substring(0, displayPath.length() - "contains".length())
      : displayPath;
    logRecord.setPath(displayPath);

    logService.log(logRecord);
  }

  /**
   * Resolves the best available human-readable title for a node.
   *
   * <p>Prefers the node's {@code cm:title} (using the default value for multilingual titles) and
   * falls back to the node's {@code cm:name} when no title is set.
   *
   * @param nodeRef the node whose title is requested
   * @return the node title, or its name when no title is available
   */
  protected String getBestTitle(final NodeRef nodeRef) {
    final String name = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NAME
    );

    final Serializable titleObj = nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_TITLE
    );

    return switch (titleObj) {
      case MLText mlText -> mlText.getDefaultValue();
      case String str -> str;
      default -> name;
    };
  }

  private void notifyImplForNewEdition(
    final NodeRef nodeRef,
    final Set<NotifiableUser> users
  ) {
    notifyUsers(
      nodeRef,
      users,
      MailTemplate.ADD_NEW_TRANSLATION_EDITION,
      LIBRARY,
      null
    );
  }

  private void notifyImplForContent(
    final NodeRef nodeRef,
    final Set<NotifiableUser> users
  ) {
    notifyUsers(nodeRef, users, MailTemplate.NOTIFY_DOC, LIBRARY, null);
  }

  private void notifyImplForNews(
    final NodeRef nodeRef,
    final Set<NotifiableUser> users
  ) {
    notifyUsers(
      nodeRef,
      users,
      MailTemplate.NOTIFY_NEWS,
      "Information",
      model -> {
        String pattern = nodeService
          .getProperty(nodeRef, CircabcModel.PROP_NEWS_PATTERN)
          .toString();
        if (
          News.PatternEnum.IFRAME.equals(News.PatternEnum.fromValue(pattern))
        ) {
          model.put(
            "urlLink",
            nodeService.getProperty(nodeRef, CircabcModel.PROP_NEWS_URL)
          );
        }
      }
    );
  }

  private void notifyImplForPost(
    final NodeRef nodeRef,
    final Set<NotifiableUser> users
  ) {
    notifyUsers(nodeRef, users, MailTemplate.NOTIFY_POST, "Newsgroup", null);
  }

  private void notifyUsers(
    NodeRef nodeRef,
    Set<NotifiableUser> users,
    MailTemplate template,
    String service,
    java.util.function.Consumer<Map<String, Object>> modelCustomizer
  ) {
    boolean connectFailed = false;
    String igTitle = null;

    for (NotifiableUser user : users) {
      if (igTitle == null) {
        igTitle = getCurrentIgTitle(nodeRef);
      }
      NotificationResult result = sendNotificationToUser(
        nodeRef,
        user,
        template,
        igTitle,
        modelCustomizer,
        connectFailed
      );
      connectFailed = result.connectFailed;
      logNotificationResult(nodeRef, user, service, result.success);
    }
  }

  private NotificationResult sendNotificationToUser(
    NodeRef nodeRef,
    NotifiableUser user,
    MailTemplate template,
    String igTitle,
    java.util.function.Consumer<Map<String, Object>> modelCustomizer,
    boolean connectFailed
  ) {
    String email = user.getEmailAddress();
    if (email == null) {
      return new NotificationResult(false, connectFailed);
    }

    Locale userLocale =
      user.getNotificationLanguage() != null
        ? user.getNotificationLanguage()
        : DEFAULT_MAIL_LOCALE;
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      nodeRef,
      user.getPerson(),
      null
    );
    if (modelCustomizer != null) {
      modelCustomizer.accept(model);
    }

    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      nodeRef,
      template
    );
    try {
      boolean success = mailService.send(
        getMailFrom(),
        email,
        null,
        mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
        mail.getBody(model, userLocale),
        true,
        false
      );
      return new NotificationResult(success, connectFailed);
    } catch (MailSendException mse) {
      if (!connectFailed && logger.isErrorEnabled()) {
        logger.error(COULD_NOT_CONNECT_TO_MAIL_SERVER, mse);
      }
      return new NotificationResult(false, true);
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(COULD_NOT_SEND_NOTIFICATION_TO_USER + user, e);
      }
      return new NotificationResult(false, connectFailed);
    }
  }

  private void logNotificationResult(
    NodeRef nodeRef,
    NotifiableUser user,
    String service,
    boolean success
  ) {
    String to;
    if (user.getEmailAddress() != null) {
      to = user.getEmailAddress();
    } else if (user.getUserName() != null) {
      to = user.getUserName();
    } else {
      to = EMAIL_AND_USER_NAME_ARE_NULL;
    }
    logNotification(nodeRef, to, service, success, false);
  }

  private static class NotificationResult {

    final boolean success;
    final boolean connectFailed;

    NotificationResult(boolean success, boolean connectFailed) {
      this.success = success;
      this.connectFailed = connectFailed;
    }
  }

  private String getCurrentIgTitle(NodeRef nodeRef) {
    NodeRef igRef = apiToolBox.getCurrentInterestGroup(nodeRef);
    Serializable title = nodeService.getProperty(
      igRef,
      ContentModel.PROP_TITLE
    );
    String result = nodeService
      .getProperty(igRef, ContentModel.PROP_NAME)
      .toString();
    if (title instanceof MLText mlText) {
      String defaultTitle = mlText.getDefaultValue();
      String mailTitle = mlText.get(DEFAULT_MAIL_LOCALE);
      if (defaultTitle != null && !defaultTitle.trim().isEmpty()) {
        result = defaultTitle;
      } else if (mailTitle != null && !mailTitle.trim().isEmpty()) {
        result = mailTitle;
      }
    } else if (title instanceof String str) {
      result = str;
    }
    return result;
  }

  private String getSafeProperty(
    Locale locale,
    final QName qname,
    final Map<QName, Serializable> props
  ) {
    if (locale == null) {
      locale = DEFAULT_MAIL_LOCALE;
    }

    final Serializable value = props.get(qname);

    return getSafeValue(locale, value);
  }

  private String getSafeValue(Locale locale, final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof MLText mlValues) {
      if (mlValues.containsKey(locale)) {
        return mlValues.get(locale);
      } else if (mlValues.containsKey(DEFAULT_MAIL_LOCALE)) {
        return mlValues.get(DEFAULT_MAIL_LOCALE);
      } else {
        return mlValues.getClosestValue(locale);
      }
    } else if (value instanceof List<?> list) {
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
    } else if (value instanceof NodeRef nodeRef) {
      // for node ref, get arbitrary the title
      return getSafeProperty(
        locale,
        ContentModel.PROP_TITLE,
        nodeService.getProperties(nodeRef)
      );
    } else {
      return value.toString();
    }
  }

  /**
   * Returns the sender address used as the {@code From} header for outgoing notifications.
   *
   * @return the configured "no-reply" e-mail address
   */
  public String getMailFrom() {
    return mailService.getNoReplyEmailAddress();
  }

  /**
   * Notifies the given e-mail addresses about a change to the specified node.
   *
   * <p>No action is taken when the address list or the node is {@code null}/empty, or when the node
   * does not exist. Notifications for forum posts are currently not supported through this variant;
   * all other node types are treated as generic content.
   *
   * @param nodeRef the node the notification is about; ignored when {@code null} or non-existent
   * @param mails the recipient e-mail addresses; ignored when {@code null} or empty
   * @param templateType the mail template to use for generic content notifications
   * @throws NotificationException if building or sending a notification fails
   */
  @Override
  public void notify(
    NodeRef nodeRef,
    List<String> mails,
    MailTemplate templateType
  ) throws NotificationException {
    if (mails == null || mails.isEmpty()) {
      // no user to notify ... exit ...
      return;
    }

    if (nodeRef == null) {
      // node not specfied
      return;
    }

    if (nodeService.exists(nodeRef)) {
      // find it's type so we can see if it's a node we are interested in
      final QName nodeType = nodeService.getType(nodeRef);

      if (
        ForumModel.TYPE_POST.equals(nodeType) ||
        dictionaryService.isSubClass(nodeType, ForumModel.TYPE_POST)
      ) {
        notifyImplForPost();
      } else {
        notifyImplForContent(nodeRef, mails, templateType);
      }
    }
  }

  private void notifyImplForContent(
    NodeRef nodeRef,
    List<String> mails,
    MailTemplate templateType
  ) {
    boolean connectFailed = false;
    String igTitle = null;

    for (final String email : mails) {
      if (email == null) {
        continue;
      }
      if (igTitle == null) {
        igTitle = getCurrentIgTitle(nodeRef);
      }
      connectFailed = sendContentNotification(
        nodeRef,
        email,
        templateType,
        igTitle,
        connectFailed
      );
    }
  }

  private boolean sendContentNotification(
    NodeRef nodeRef,
    String email,
    MailTemplate templateType,
    String igTitle,
    boolean connectFailed
  ) {
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      nodeRef,
      null,
      null
    );
    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      nodeRef,
      templateType
    );

    boolean result = false;
    try {
      result = mailService.send(
        getMailFrom(),
        email,
        null,
        mail.getSubject(model, DEFAULT_MAIL_LOCALE) + " [ " + igTitle + " ]",
        mail.getBody(model, DEFAULT_MAIL_LOCALE),
        true,
        false
      );
      logNotification(nodeRef, email, AUTO_UPLOAD, result, true);
      return connectFailed;
    } catch (final MailSendException mse) {
      if (!connectFailed && logger.isErrorEnabled()) {
        logger.error(COULD_NOT_CONNECT_TO_MAIL_SERVER, mse);
      }
      logNotification(nodeRef, email, AUTO_UPLOAD, false, false);
      return true;
    } catch (final Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(COULD_NOT_SEND_NOTIFICATION_TO_USER + email, e);
      }
      logNotification(nodeRef, email, AUTO_UPLOAD, false, false);
      return connectFailed;
    }
  }

  // Post notification via mail list is not supported
  private void notifyImplForPost() {
    // intentionally empty
  }

  /**
   * Generic notification entry point for membership-related events.
   *
   * <p>The notification service originally only supported upload and edition notifications. This
   * more generic method allows sending different kinds of notifications, currently user invitations
   * ({@link NotificationType#NOTIFY_USER_INVITATION}) and membership updates ({@link
   * NotificationType#NOTIFY_USER_MEMBERSHIP_UPDATE}). Nothing is sent when {@code nodeRef} is {@code
   * null}.
   *
   * @param nodeRef the node the notification relates to (e.g. the interest group)
   * @param users the users to notify
   * @param notificationType the kind of notification to send
   * @param notificationText free-text message included with invitation notifications
   * @param expirationDate optional membership expiration date passed to the mail model
   * @throws NotificationException if sending a notification fails
   */
  @Override
  @Auditable(parameters = { "nodeRef", "users", "notificationType" })
  public void notify(
    NodeRef nodeRef,
    Set<NotifiableUser> users,
    NotificationType notificationType,
    String notificationText,
    Date expirationDate
  ) throws NotificationException {
    if (nodeRef != null) {
      try {
        for (NotifiableUser nUser : users) {
          if (
            notificationType.equals(NotificationType.NOTIFY_USER_INVITATION)
          ) {
            //we only add the notification text for the invite user when c
            notifyImplInviteUser(
              nodeRef,
              nUser,
              notificationText,
              expirationDate
            );
          } else if (
            notificationType.equals(
              NotificationType.NOTIFY_USER_MEMBERSHIP_UPDATE
            )
          ) {
            notifyImplUpdateUser(nodeRef, nUser, expirationDate);
          }
        }
      } catch (MessagingException e) {
        throw new NotificationException("Failed to send notification", e);
      }
    }
  }

  private void notifyImplMembershipChangeUserToAdmin(
    NodeRef nodeRef,
    NotifiableUser nUser,
    UserProfile newMember,
    Boolean updateOfProfile
  ) throws MessagingException {
    String email = nUser.getEmailAddress();
    Locale userLocale = nUser.getNotificationLanguage();

    if (userLocale == null) {
      userLocale = DEFAULT_MAIL_LOCALE;
    }

    String igTitle = getCurrentIgTitle(nodeRef);

    NodeRef person = personService.getPerson(newMember.getUser().getUserId());

    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      nodeRef,
      person,
      null
    );

    MailWrapper mail = null;

    if (Boolean.TRUE.equals(updateOfProfile)) {
      mail = mailPreferencesService.getDefaultMailTemplate(
        nodeRef,
        MailTemplate.UPDATE_MEMBERSHIP_NOTIFICATION
      );
    } else {
      mail = mailPreferencesService.getDefaultMailTemplate(
        nodeRef,
        MailTemplate.ADD_MEMBERSHIP_NOTIFICATION
      );
    }

    String profile = newMember.getProfile().getName();
    if (
      newMember.getProfile().getTitle() != null &&
      !"".equals(newMember.getProfile().getTitle().getDefaultValue())
    ) {
      profile = newMember.getProfile().getTitle().getDefaultValue();
    }
    model.put(PROFILE, profile);

    String currentUsername = authenticationService.getCurrentUserName();
    NodeRef currentPerson = personService.getPerson(currentUsername);

    model.put("me", currentPerson);

    model.put("targetUser", nUser.getPerson());

    boolean result = false;
    if (email != null && mail != null) {
      result = mailService.send(
        getMailFrom(),
        email,
        null,
        mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
        mail.getBody(model, userLocale),
        true,
        false
      );
    }

    String to = (email == null ? nUser.getUserName() : email);
    to = (to == null ? EMAIL_AND_USER_NAME_ARE_NULL : to);

    logNotification(nodeRef, to, DIRECTORY, result, false);
  }

  /**
   * @param nodeRef
   * @param nUser
   * @param notificationText
   * @param expirationDate
   * @throws MessagingException
   */
  private void notifyImplInviteUser(
    NodeRef nodeRef,
    NotifiableUser nUser,
    String notificationText,
    Date expirationDate
  ) throws MessagingException {
    String email = nUser.getEmailAddress();
    Locale userLocale = nUser.getNotificationLanguage();
    if (userLocale == null) {
      userLocale = DEFAULT_MAIL_LOCALE;
    }
    String igTitle = getCurrentIgTitle(nodeRef);
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      nodeRef,
      nUser.getPerson(),
      null
    );
    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      nodeRef,
      MailTemplate.INVITE_USER
    );

    String profile = profilesApi.getPersonProfile(nodeRef, nUser.getUserName());

    model.put(PROFILE, profile);

    model.put("notifyText", notificationText != null ? notificationText : "");
    model.put("expirationDate", expirationDate);

    boolean result = false;
    if (email != null) {
      result = mailService.send(
        getMailFrom(),
        email,
        null,
        mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
        mail.getBody(model, userLocale),
        true,
        false
      );
    }

    String to = (email == null ? nUser.getUserName() : email);
    to = (to == null ? EMAIL_AND_USER_NAME_ARE_NULL : to);

    logNotification(nodeRef, to, DIRECTORY, result, false);
  }

  /**
   * @param nodeRef
   * @param nUser
   * @param expirationDate
   * @throws MessagingException
   */
  private void notifyImplUpdateUser(
    NodeRef nodeRef,
    NotifiableUser nUser,
    Date expirationDate
  ) throws MessagingException {
    String email = nUser.getEmailAddress();
    Locale userLocale = nUser.getNotificationLanguage();

    if (userLocale == null) {
      userLocale = DEFAULT_MAIL_LOCALE;
    }

    String igTitle = getCurrentIgTitle(nodeRef);

    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      nodeRef,
      nUser.getPerson(),
      null
    );
    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      nodeRef,
      MailTemplate.UPDATE_USER_PROFILE
    );

    String profile = profilesApi.getPersonProfile(nodeRef, nUser.getUserName());

    model.put(PROFILE, profile);
    model.put("expirationDate", expirationDate);

    boolean result = false;
    if (email != null) {
      result = mailService.send(
        getMailFrom(),
        email,
        null,
        mail.getSubject(model, userLocale) + " [ " + igTitle + " ]",
        mail.getBody(model, userLocale),
        true,
        false
      );
    }

    String to = (email == null ? nUser.getUserName() : email);
    to = (to == null ? EMAIL_AND_USER_NAME_ARE_NULL : to);

    logNotification(nodeRef, to, DIRECTORY, result, false);
  }

  /**
   * Notifies interest-group administrators that a new member has been added.
   *
   * @param nodeRef the interest group node the membership relates to
   * @param admins the administrators to notify
   * @param newMember the profile of the newly added member
   * @throws NotificationException if sending a notification fails
   */
  @Override
  public void notifyNewMemberships(
    NodeRef nodeRef,
    Set<NotifiableUser> admins,
    UserProfile newMember
  ) throws NotificationException {
    try {
      for (NotifiableUser admin : admins) {
        notifyImplMembershipChangeUserToAdmin(nodeRef, admin, newMember, false);
      }
    } catch (MessagingException e) {
      throw new NotificationException(
        "Failed to send membership notification",
        e
      );
    }
  }

  /**
   * Notifies interest-group administrators that an existing member's profile has been updated.
   *
   * @param nodeRef the interest group node the membership relates to
   * @param admins the administrators to notify
   * @param newMember the updated member profile
   * @throws NotificationException if sending a notification fails
   */
  @Override
  public void notifyUpdateMemberships(
    NodeRef nodeRef,
    Set<NotifiableUser> admins,
    UserProfile newMember
  ) throws NotificationException {
    try {
      for (NotifiableUser admin : admins) {
        notifyImplMembershipChangeUserToAdmin(nodeRef, admin, newMember, true);
      }
    } catch (MessagingException e) {
      throw new NotificationException(
        "Failed to send membership update notification",
        e
      );
    }
  }

  /**
   * @return the {@link PersonService} used to resolve person nodes
   */
  public PersonService getPersonService() {
    return personService;
  }

  /**
   * @param personService the {@link PersonService} to set
   */
  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  /**
   * Notifies users about a set of newly added files under a parent node.
   *
   * <p>The files are converted into template-friendly nodes and included in the mail model. When a
   * single file is added its title and URL are appended to the subject line. Recipients without an
   * e-mail address are logged as failed deliveries.
   *
   * @param parentRef the parent (folder) node the files were added to
   * @param nodeRefs the newly added file nodes
   * @param notifiableUsers the users to notify
   * @param notifyDocBulk the mail template to use
   */
  @Override
  public void notifyNewFiles(
    NodeRef parentRef,
    List<NodeRef> nodeRefs,
    Set<NotifiableUser> notifiableUsers,
    MailTemplate notifyDocBulk
  ) {
    String igTitle = getCurrentIgTitle(parentRef);
    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      parentRef,
      notifyDocBulk
    );
    List<TemplatableNode> nodes = convertToTemplateNodes(nodeRefs, parentRef);

    boolean connectFailed = false;
    for (final NotifiableUser user : notifiableUsers) {
      connectFailed = sendNewFilesNotification(
        parentRef,
        user,
        mail,
        nodes,
        igTitle,
        connectFailed
      );
    }
  }

  private boolean sendNewFilesNotification(
    NodeRef parentRef,
    NotifiableUser user,
    MailWrapper mail,
    List<TemplatableNode> nodes,
    String igTitle,
    boolean connectFailed
  ) {
    String email = user.getEmailAddress();
    if (email == null) {
      logNotification(
        parentRef,
        getRecipientIdentifier(user),
        LIBRARY,
        false,
        false
      );
      return connectFailed;
    }

    Locale userLocale =
      user.getNotificationLanguage() != null
        ? user.getNotificationLanguage()
        : DEFAULT_MAIL_LOCALE;
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      parentRef,
      user.getPerson(),
      null
    );
    model.put("parent", parentRef);
    model.put("nodes", nodes);

    String subject = buildNewFilesSubject(
      mail,
      model,
      userLocale,
      nodes,
      igTitle
    );
    boolean result = false;

    try {
      result = mailService.send(
        getMailFrom(),
        email,
        null,
        subject,
        mail.getBody(model, userLocale),
        true,
        false
      );
    } catch (final MailSendException mse) {
      if (!connectFailed && logger.isErrorEnabled()) {
        logger.error(COULD_NOT_CONNECT_TO_MAIL_SERVER, mse);
      }
      logNotification(
        parentRef,
        getRecipientIdentifier(user),
        LIBRARY,
        false,
        false
      );
      return true;
    } catch (final Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(COULD_NOT_SEND_NOTIFICATION_TO_USER + user, e);
      }
    }

    logNotification(
      parentRef,
      getRecipientIdentifier(user),
      LIBRARY,
      result,
      false
    );
    return connectFailed;
  }

  private String buildNewFilesSubject(
    MailWrapper mail,
    Map<String, Object> model,
    Locale userLocale,
    List<TemplatableNode> nodes,
    String igTitle
  ) {
    if (nodes.size() == 1) {
      TemplatableNode firstNode = nodes.get(0);
      String title =
        firstNode.getTitle() == null
          ? firstNode.getName()
          : firstNode.getTitle();
      return (
        mail.getSubject(model, userLocale) +
        " [ " +
        igTitle +
        "," +
        title +
        " ]" +
        " " +
        firstNode.getUrl()
      );
    }
    return mail.getSubject(model, userLocale) + " [ " + igTitle + " ]";
  }

  private String getRecipientIdentifier(NotifiableUser user) {
    if (user.getEmailAddress() != null) {
      return user.getEmailAddress();
    }
    return user.getUserName() != null
      ? user.getUserName()
      : EMAIL_AND_USER_NAME_ARE_NULL;
  }

  /**
   * Sends a notification with file attachments to a list of e-mail addresses.
   *
   * <p>When {@code nodeRef} is {@code null} the CIRCABC root node is used as the context for
   * building the mail model and the interest-group title is omitted from the subject. The current
   * user (or the admin user when running as system) is used as the model's acting user. Delivery
   * failures are logged and do not stop processing of the remaining recipients.
   *
   * @param nodeRef the context node, or {@code null} to use the CIRCABC root
   * @param mails the recipient e-mail addresses
   * @param mailTemplate the mail template to use
   * @param files the files to attach
   */
  @Override
  public void notify(
    NodeRef nodeRef,
    List<String> mails,
    MailTemplate mailTemplate,
    List<File> files
  ) {
    NodeRef currentNodeRef = null;
    if (nodeRef == null) {
      currentNodeRef = getCircabcNodeRef();
    } else {
      currentNodeRef = nodeRef;
    }

    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      currentNodeRef,
      mailTemplate
    );
    String userName = authenticationService.getCurrentUserName();
    if (userName.equals(AuthenticationUtil.getSystemUserName())) {
      userName = AuthenticationUtil.getAdminUserName();
    }
    NodeRef userRef = personService.getPerson(userName);
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      currentNodeRef,
      userRef,
      null
    );

    String titleComplement = "";

    if (nodeRef != null) {
      titleComplement = "[" + getCurrentIgTitle(nodeRef) + "]";
    }

    for (String emailAddress : mails) {
      try {
        mailService.sendWithAttachment(
          getMailFrom(),
          emailAddress,
          null,
          mail.getSubject(model, DEFAULT_MAIL_LOCALE) + titleComplement,
          mail.getBody(model, DEFAULT_MAIL_LOCALE),
          true,
          false,
          files
        );
      } catch (MessagingException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Impossible to send distribution list", e);
        }
      }
    }
  }

  /**
   * Sends a system-wide message to a list of e-mail addresses.
   *
   * <p>The message content is taken from the supplied {@link AppMessage} and rendered with the
   * {@link MailTemplate#NOTIFY_SYSTEM_MESSAGE} template against the CIRCABC root node. An unsubscribe
   * link is enabled for these messages. Delivery failures are logged.
   *
   * @param mailAddress the recipient e-mail addresses
   * @param template the application message providing the notification content
   */
  @Override
  public void notifySystemMessage(
    List<String> mailAddress,
    AppMessage template
  ) {
    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      getCircabcNodeRef(),
      MailTemplate.NOTIFY_SYSTEM_MESSAGE
    );
    String userName = authenticationService.getCurrentUserName();
    if (userName.equals(AuthenticationUtil.getSystemUserName())) {
      userName = AuthenticationUtil.getAdminUserName();
    }
    NodeRef userRef = personService.getPerson(userName);
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      getCircabcNodeRef(),
      userRef,
      null
    );

    model.put("systemMessage", template.getContent());
    model.put("showUnsubscribe", true);

    try {
      mailService.send(
        getMailFrom(),
        mailAddress,
        null,
        mail.getSubject(model, DEFAULT_MAIL_LOCALE) +
          " [ " +
          circabcConfig.getApplicationName() +
          " ]",
        mail.getBody(model, DEFAULT_MAIL_LOCALE),
        true,
        true
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Impossible to send distribution list", e);
      }
    }
  }

  private List<TemplatableNode> convertToTemplateNodes(
    List<NodeRef> nodeRefs,
    NodeRef parentRef
  ) {
    List<TemplatableNode> result = new ArrayList<>();
    if (nodeRefs == null) {
      return result;
    }
    NodeRef groupRef = findInterestGroupRoot(parentRef);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
    for (NodeRef nodeRef : nodeRefs) {
      result.add(createTemplatableNode(nodeRef, groupRef, sdf));
    }
    return result;
  }

  private TemplatableNode createTemplatableNode(
    NodeRef nodeRef,
    NodeRef groupRef,
    SimpleDateFormat sdf
  ) {
    TemplatableNode node = new TemplatableNode();
    node.setName(
      (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
    );
    setMLTextProperty(node::setTitle, nodeRef, ContentModel.PROP_TITLE);
    setMLTextProperty(
      node::setDescription,
      nodeRef,
      ContentModel.PROP_DESCRIPTION
    );
    setModifierInfo(node, nodeRef);
    Date modified = (Date) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_MODIFIED
    );
    node.setModified(sdf.format(modified));
    setKeywords(node, nodeRef);
    node.setUrl(buildNodeUrl(nodeRef, groupRef));
    node.setPath(PathUtils.getLibraryPath(nodeService.getPath(nodeRef), true));
    setDynamicProperties(node, nodeRef);
    return node;
  }

  private void setMLTextProperty(
    java.util.function.Consumer<String> setter,
    NodeRef nodeRef,
    QName prop
  ) {
    Object obj = nodeService.getProperty(nodeRef, prop);
    if (obj instanceof String s) {
      setter.accept(s);
    } else if (obj instanceof MLText mlText) {
      setter.accept(mlText.getDefaultValue());
    }
  }

  private void setModifierInfo(TemplatableNode node, NodeRef nodeRef) {
    String modifier = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_MODIFIER
    );
    if (modifier != null && !modifier.equals("System")) {
      NodeRef personRef = personService.getPerson(modifier);
      String firstname = (String) nodeService.getProperty(
        personRef,
        ContentModel.PROP_FIRSTNAME
      );
      String lastname = (String) nodeService.getProperty(
        personRef,
        ContentModel.PROP_LASTNAME
      );
      node.setModifier(firstname + " " + lastname);
    }
  }

  private void setKeywords(TemplatableNode node, NodeRef nodeRef) {
    Object keywords = nodeService.getProperty(
      nodeRef,
      DocumentModel.PROP_KEYWORD
    );
    if (keywords != null) {
      String keywordsStr = keywords.toString();
      node.setKeywords(
        keywordsStr.length() > 2
          ? keywordsStr.substring(1, keywordsStr.length() - 1)
          : ""
      );
    }
  }

  private String buildNodeUrl(NodeRef nodeRef, NodeRef groupRef) {
    String context = circabcConfig.getNewUiContext();
    String separator = context.endsWith("/") ? "group/" : "/group/";
    return (
      circabcConfig.getNewUiUrl() +
      context +
      separator +
      (groupRef != null ? groupRef.getId() : "") +
      "/library/" +
      nodeRef.getId() +
      "/details"
    );
  }

  private void setDynamicProperties(TemplatableNode node, NodeRef nodeRef) {
    List<DynamicProperty> dynProps =
      dynamicPropertyService.getDynamicProperties(nodeRef);
    for (DynamicProperty dn : dynProps) {
      String value = (String) nodeService.getProperty(
        nodeRef,
        QName.createQName(
          DocumentModel.CIRCABC_DOCUMENT_MODEL_1_0_URI,
          "dynAttr" + dn.getIndex()
        )
      );
      node.getDynamicProperties().put(dn.getName(), value);
    }
  }

  private NodeRef findInterestGroupRoot(NodeRef nodeRef) {
    NodeRef parent = null;

    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
      while (!nodeService.hasAspect(parent, CircabcModel.ASPECT_IGROOT)) {
        parent = nodeService.getPrimaryParent(parent).getParentRef();
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
  public void setDynamicPropertyService(
    DynamicPropertyService dynamicPropertyService
  ) {
    this.dynamicPropertyService = dynamicPropertyService;
  }

  /**
   * Sends a bulk notification about a node to the given users.
   *
   * <p>Builds template-friendly nodes for the target and sends the notification using the supplied
   * template. Recipients without an e-mail address are logged as failed deliveries and delivery
   * failures do not stop processing of the remaining recipients.
   *
   * @param nodeRef the node the notification is about
   * @param notifiableUsers the users to notify
   * @param notifyDocBulk the mail template to use
   */
  @Override
  public void notify(
    NodeRef nodeRef,
    Set<NotifiableUser> notifiableUsers,
    MailTemplate notifyDocBulk
  ) {
    String igTitle = getCurrentIgTitle(nodeRef);
    MailWrapper mail = mailPreferencesService.getDefaultMailTemplate(
      nodeRef,
      notifyDocBulk
    );
    List<TemplatableNode> nodes = convertToTemplateNodes(null, nodeRef);

    boolean connectFailed = false;
    for (final NotifiableUser user : notifiableUsers) {
      connectFailed = sendBulkNotification(
        nodeRef,
        user,
        mail,
        nodes,
        igTitle,
        connectFailed
      );
    }
  }

  private boolean sendBulkNotification(
    NodeRef nodeRef,
    NotifiableUser user,
    MailWrapper mail,
    List<TemplatableNode> nodes,
    String igTitle,
    boolean connectFailed
  ) {
    String email = user.getEmailAddress();
    if (email == null) {
      logNotification(
        nodeRef,
        getRecipientIdentifier(user),
        LIBRARY,
        false,
        false
      );
      return connectFailed;
    }

    Locale userLocale =
      user.getNotificationLanguage() != null
        ? user.getNotificationLanguage()
        : DEFAULT_MAIL_LOCALE;
    Map<String, Object> model = mailPreferencesService.buildDefaultModel(
      nodeRef,
      user.getPerson(),
      null
    );
    model.put("parent", nodeRef);
    model.put("nodes", nodes);

    String subject = buildNewFilesSubject(
      mail,
      model,
      userLocale,
      nodes,
      igTitle
    );
    boolean result = false;

    try {
      result = mailService.send(
        getMailFrom(),
        email,
        null,
        subject,
        mail.getBody(model, userLocale),
        true,
        false
      );
    } catch (final MailSendException mse) {
      if (!connectFailed && logger.isErrorEnabled()) {
        logger.error(COULD_NOT_CONNECT_TO_MAIL_SERVER, mse);
      }
      logNotification(
        nodeRef,
        getRecipientIdentifier(user),
        LIBRARY,
        false,
        false
      );
      return true;
    } catch (final Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(COULD_NOT_SEND_NOTIFICATION_TO_USER + user, e);
      }
    }

    logNotification(
      nodeRef,
      getRecipientIdentifier(user),
      LIBRARY,
      result,
      false
    );
    return connectFailed;
  }
}
