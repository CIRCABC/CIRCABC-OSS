package eu.europa.ec.digit.circabc.rest.job;

import com.sun.mail.pop3.POP3Message;
import eu.europa.ec.digit.circabc.rest.service.CircabcServiceRegistry;
import eu.europa.ec.digit.circabc.rest.service.event.EventService;
import eu.europa.ec.digit.circabc.rest.service.lock.LockService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.UpdateMode;
import jakarta.mail.*;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Scheduled Quartz job that polls a configured mailbox for meeting (calendar)
 * responses and reflects the attendees' answers back into CIRCABC events.
 *
 * <p>On each execution the job runs as the {@code admin} user, acquires a
 * cluster-wide lock to guarantee a single active listener, and connects to the
 * mailbox over POP3 (optionally with STARTTLS). For every message that carries
 * an iCalendar {@code METHOD:REPLY} payload it:
 * <ul>
 *   <li>resolves the responding CIRCABC user from the sender's email address,</li>
 *   <li>extracts the meeting node reference from the iCalendar {@code UID},</li>
 *   <li>maps the {@code PARTSTAT} value to a {@link MeetingRequestStatus}
 *       (accepted, declined/rejected or tentative/pending),</li>
 *   <li>determines whether a single occurrence or the whole series is affected
 *       via the {@code RECURRENCE-ID} line, and</li>
 *   <li>updates the meeting request status for that user before deleting the
 *       processed message.</li>
 * </ul>
 *
 * <p>All configuration (mailbox coordinates, credentials, protocol and activation
 * flags) is supplied through the Quartz {@link JobDataMap} and the
 * {@link CircabcConfig} obtained from the injected service registry. Failures are
 * logged and never propagated so that a single bad message does not abort the job.
 */
public class MeetingResponseListener implements Job {

  private static final String MEETING_RESPONSE_LISTENER =
    "MeetingResponseListener";
  private static final String ADMIN_USER = "admin";
  private static final String RECURRENCE_ID = "RECURRENCE-ID";
  private static final String CRLF = "\r\n";
  private static final String UID = "UID:";
  private static final String PARTSTAT_TENTATIVE = "PARTSTAT=TENTATIVE";
  private static final String PARTSTAT_DECLINED = "PARTSTAT=DECLINED";
  private static final String METHOD_REPLY = "METHOD:REPLY";
  private static final String BEGIN_VEVENT = "BEGIN:VEVENT";
  private static final String BEGIN_VCALENDAR = "BEGIN:VCALENDAR";
  private static final String PARTSTAT_ACCEPTED = "PARTSTAT=ACCEPTED";
  private static Log logger = LogFactory.getLog(MeetingResponseListener.class);

  /** Alfresco service registry used to obtain the transaction service. */
  private ServiceRegistry serviceRegistry;
  /** CIRCABC event service used to look up meetings and persist responses. */
  private EventService eventService;
  /** Service used to resolve a CIRCABC user name from a sender email address. */
  private UserService userService;
  /** Distributed lock service ensuring only one listener runs at a time. */
  private LockService lockService;
  /** Mail protocol used to reach the mailbox (e.g. {@code pop3}). */
  private String emailProtocol;
  /** Host name of the mail server to poll. */
  private String emailServer;
  /** Name of the mailbox/folder to read messages from. */
  private String emailBox;
  /** User name used to authenticate against the mail server. */
  private String emailUsername;
  /** Password used to authenticate against the mail server. */
  private String emailPassword;
  /** Whether the email listener is active according to {@link CircabcConfig}. */
  private Boolean isActive;
  /** Port of the mail server to connect to. */
  private Integer emailServerPort;
  /** Whether STARTTLS should be used for the mail connection. */
  private Boolean isEmailUseTls;
  /** Whether this job execution is enabled (from the merged job data map). */
  private boolean enabled;

  /**
   * Entry point invoked by the Quartz scheduler.
   *
   * <p>Runs as the {@code admin} user, initializes configuration from the job
   * context, and—when enabled and not already locked—acquires the listener lock
   * and processes the mailbox. Any exception is logged rather than rethrown, and
   * the security context is always cleared on completion.
   *
   * @param context the Quartz execution context carrying the injected services
   *                and configuration
   * @throws JobExecutionException declared by the {@link Job} contract; not
   *                               thrown by this implementation as errors are
   *                               logged internally
   */
  public void execute(final JobExecutionContext context)
    throws JobExecutionException {
    try {
      AuthenticationUtil.setRunAsUser(ADMIN_USER);
      initialize(context);
      if (!enabled) return;

      if (!lockService.isLocked(MEETING_RESPONSE_LISTENER)) {
        boolean isLocked = false;
        try {
          lockService.lock(MEETING_RESPONSE_LISTENER);
          isLocked = true;
          checkEmail();
        } finally {
          if (isLocked) lockService.unlock(MEETING_RESPONSE_LISTENER);
        }
      }
    } catch (final Exception e) {
      logger.error("Can not run job MeetingResponseListener", e);
    } finally {
      AuthenticationUtil.clearCurrentSecurityContext();
    }
  }

  private void initialize(final JobExecutionContext context) {
    enabled = Boolean.valueOf(
      (String) context.getMergedJobDataMap().get("enabled")
    );
    AuthenticationUtil.setRunAsUser(ADMIN_USER);
    final JobDataMap jobData = context.getJobDetail().getJobDataMap();

    serviceRegistry = (ServiceRegistry) jobData.get("serviceRegistry");
    CircabcServiceRegistry circabcServiceRegistry =
      (CircabcServiceRegistry) jobData.get("circabcServiceRegistry");
    eventService = circabcServiceRegistry.getNonSecureEventService();
    userService = circabcServiceRegistry.getUserService();
    lockService = circabcServiceRegistry.getLockService();
    CircabcConfig circabcConfig = circabcServiceRegistry.getCircabcConfig();

    emailProtocol = circabcConfig.getEmailProtocol();
    emailServer = circabcConfig.getEmailServer();
    emailBox = circabcConfig.getEmailBox();
    emailUsername = circabcConfig.getEmailUserName();
    emailPassword = circabcConfig.getEmailPassword();
    emailServerPort = circabcConfig.getEmailServerPort();
    isActive = circabcConfig.isEmailListenerActive();
    isEmailUseTls = circabcConfig.isEmailUseTls();
  }

  private void checkEmail() {
    if (Boolean.FALSE.equals(isActive)) return;

    Properties props = createMailProperties();
    Session session = Session.getDefaultInstance(props, null);
    logProviderInfo(session);

    Store store = null;
    Folder folder = null;
    try {
      store = connectToStore(session);
      if (store == null || !store.isConnected()) return;

      folder = store.getFolder(emailBox);
      if (folder == null) throw new MessagingException(
        "Invalid folder: " + emailBox
      );

      folder.open(Folder.READ_WRITE);
      processMessages(folder);
    } catch (final Exception e) {
      if (logger.isErrorEnabled()) logger.error("Error when checking email", e);
    } finally {
      closeResources(folder, store);
    }
  }

  private Properties createMailProperties() {
    Properties props = new Properties();
    if (Boolean.TRUE.equals(isEmailUseTls)) {
      props.setProperty("mail.pop3.ssl.enable", "false");
      props.setProperty("mail.pop3.starttls.enable", "true");
      props.setProperty("mail.pop3.starttls.required", "true");
      if (logger.isDebugEnabled()) {
        props.setProperty("java.security.debug", "certpath");
        props.setProperty("javax.net.debug", "trustmanager");
        props.setProperty("mail.debug.auth", "true");
      }
    }
    return props;
  }

  private void logProviderInfo(Session session) {
    if (logger.isInfoEnabled()) {
      try {
        logger.info(
          "Using pop3 provider " + session.getProvider("pop3").toString()
        );
      } catch (NoSuchProviderException e) {
        logger.info("Provider for pop3 does not exist", e);
      }
    }
    if (logger.isDebugEnabled()) session.setDebug(true);
  }

  private Store connectToStore(Session session) throws MessagingException {
    URLName url = new URLName(
      emailProtocol,
      emailServer,
      emailServerPort,
      emailBox,
      emailUsername,
      emailPassword
    );
    Store store = session.getStore(url);
    try {
      store.connect();
    } catch (MessagingException me) {
      if (logger.isErrorEnabled()) logger.error(
        "Could not connect Email Server: " + me.getMessage()
      );
      return null;
    }
    return store;
  }

  private void processMessages(Folder folder)
    throws MessagingException, IOException, NoSuchFieldException, IllegalAccessException, SystemException {
    for (Message message : folder.getMessages()) {
      ContentType ct = new ContentType(message.getContentType());
      boolean isCalendarMessage =
        ct.getPrimaryType().equals("multipart") ||
        (ct.getPrimaryType().equals("text") &&
          ct.getSubType().equals("calendar"));
      if (isCalendarMessage && message instanceof POP3Message pop3Message) {
        processMessage(pop3Message);
      }
    }
  }

  private void closeResources(Folder folder, Store store) {
    try {
      if (folder != null) folder.close(true);
      if (store != null) store.close();
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  private void processMessage(final POP3Message message)
    throws MessagingException, IOException, NoSuchFieldException, IllegalAccessException, SystemException {
    String userName = resolveUserName(message);
    MessageContent msgContent = extractMessageContent(message);

    @SuppressWarnings("deprecation")
    TransactionService transactionService =
      serviceRegistry.getTransactionService();

    ResponseData response = findCalendarResponse(
      msgContent,
      transactionService
    );
    if (response.isResponse) {
      updateMeetingRequestStatus(
        response.meetingNodeRef,
        response.status,
        response.mode,
        userName,
        transactionService
      );
    }
    message.setFlag(Flags.Flag.DELETED, true);
  }

  private String resolveUserName(POP3Message message)
    throws MessagingException {
    InternetAddress address = (InternetAddress) message.getFrom()[0];
    String from = address.getAddress();
    String userName = userService.getUserNameByEmail(from);
    return userName != null ? userName : from;
  }

  private MessageContent extractMessageContent(POP3Message message)
    throws IOException, MessagingException, NoSuchFieldException, IllegalAccessException {
    Object messageContent = message.getContent();
    if (messageContent instanceof MimeMultipart mimeMultipart) {
      return new MessageContent(mimeMultipart, null);
    } else if (messageContent instanceof String) {
      return new MessageContent(
        null,
        getBodyFromString(message, messageContent)
      );
    }
    return new MessageContent(null, null);
  }

  private ResponseData findCalendarResponse(
    MessageContent msgContent,
    TransactionService transactionService
  ) throws IOException, MessagingException, SystemException {
    int count;
    if (msgContent.content != null) {
      count = msgContent.content.getCount();
    } else {
      count = msgContent.body != null ? 1 : 0;
    }
    String body = msgContent.body;

    for (int i = 0; i < count; i++) {
      if (body == null && msgContent.content != null) {
        body = extractBodyFromPart(
          (MimeBodyPart) msgContent.content.getBodyPart(i)
        );
      }
      if (body == null) continue;

      if (isCalendarResponse(body)) {
        return parseCalendarResponse(body, transactionService);
      }
      body = null;
    }
    return new ResponseData();
  }

  private String extractBodyFromPart(MimeBodyPart part)
    throws IOException, MessagingException {
    InputStream inputStream = part.getInputStream();
    byte[] b = new byte[inputStream.available()];
    int bytesRead = inputStream.read(b);
    return bytesRead != -1 ? new String(b, 0, bytesRead) : "";
  }

  private boolean isCalendarResponse(String body) {
    return (
      body.contains(BEGIN_VCALENDAR) &&
      body.contains(BEGIN_VEVENT) &&
      body.contains(METHOD_REPLY)
    );
  }

  private ResponseData parseCalendarResponse(
    String body,
    TransactionService transactionService
  ) throws SystemException {
    ResponseData data = new ResponseData();
    data.isResponse = true;
    data.status = getMeetingRequestStatus(body);
    data.meetingNodeRef = getMeetingNodeRef(body);
    data.mode = UpdateMode.AllOccurences;

    int startPositionRecurrence = body.indexOf(RECURRENCE_ID);
    if (startPositionRecurrence > -1) {
      data.mode = UpdateMode.Single;
      String recurrenceLine = body.substring(
        startPositionRecurrence,
        body.indexOf(CRLF, startPositionRecurrence)
      );
      data.meetingNodeRef = getMeetingNodeRefByRecurrenceLine(
        data.meetingNodeRef,
        transactionService,
        recurrenceLine
      );
    }
    return data;
  }

  private NodeRef getMeetingNodeRef(String body) {
    int startPositionUid = body.indexOf(UID);
    int endPositionUid = body.indexOf(CRLF, startPositionUid);
    String uid = body.substring(startPositionUid + 4, endPositionUid);
    return new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, uid);
  }

  private MeetingRequestStatus getMeetingRequestStatus(String body) {
    if (body.contains(PARTSTAT_ACCEPTED)) return MeetingRequestStatus.Accepted;
    if (body.contains(PARTSTAT_DECLINED)) return MeetingRequestStatus.Rejected;
    if (body.contains(PARTSTAT_TENTATIVE)) return MeetingRequestStatus.Pending;
    return null;
  }

  private NodeRef getMeetingNodeRefByRecurrenceLine(
    NodeRef meetingNodeRef,
    TransactionService transactionService,
    String recurrenceLine
  ) throws SystemException {
    UserTransaction tx = null;
    try {
      tx = transactionService.getUserTransaction();
      tx.begin();
      meetingNodeRef = eventService.getMeetingNodeRef(
        meetingNodeRef,
        recurrenceLine
      );
      tx.commit();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error(
        "Did not get recurrence node: " + recurrenceLine,
        e
      );
      if (tx != null) tx.rollback();
    }
    return meetingNodeRef;
  }

  private void updateMeetingRequestStatus(
    NodeRef meetingNodeRef,
    MeetingRequestStatus status,
    UpdateMode mode,
    String userName,
    TransactionService transactionService
  ) throws SystemException {
    UserTransaction tx = null;
    try {
      tx = transactionService.getUserTransaction();
      tx.begin();
      eventService.setMeetingRequestStatus(
        meetingNodeRef,
        userName,
        status,
        mode
      );
      tx.commit();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error("Error when update meeting", e);
      if (tx != null) tx.rollback();
    }
  }

  private String getBodyFromString(POP3Message message, Object messageContent)
    throws IOException, MessagingException, NoSuchFieldException, IllegalAccessException {
    String body = (String) messageContent;
    if (body.isEmpty()) {
      InputStream is = message.getInputStream();
      if (is instanceof ByteArrayInputStream) {
        Field f = ByteArrayInputStream.class.getDeclaredField("buf");
        f.setAccessible(true); // NOSONAR
        body = new String((byte[]) f.get(is));
      }
    }
    return body;
  }

  private static class MessageContent {

    MimeMultipart content;
    String body;

    MessageContent(MimeMultipart content, String body) {
      this.content = content;
      this.body = body;
    }
  }

  private static class ResponseData {

    boolean isResponse = false;
    NodeRef meetingNodeRef = null;
    MeetingRequestStatus status = null;
    UpdateMode mode = UpdateMode.AllOccurences;
  }
}
