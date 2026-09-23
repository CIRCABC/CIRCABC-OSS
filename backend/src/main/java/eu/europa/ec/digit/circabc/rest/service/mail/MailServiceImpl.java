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
package eu.europa.ec.digit.circabc.rest.service.mail;

import eu.europa.ec.digit.circabc.rest.exception.MailServiceException;
import eu.europa.ec.digit.circabc.rest.service.event.AppointmentUtils;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.AudienceStatus;
import io.swagger.model.Meeting;
import io.swagger.model.UpdateMode;
import io.swagger.util.Converter;
import io.swagger.util.EmailUtil;
import jakarta.activation.*;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Range;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.CompatibilityHints;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Default implementation of {@link MailService} for CIRCABC.
 *
 * <p>This service is responsible for composing and sending all outgoing e-mails of the
 * application. It supports:
 *
 * <ul>
 *   <li>Plain text and HTML messages sent to one or several recipients (optionally using BCC),
 *   <li>Messages carrying repository ({@link NodeRef}) or local {@link File} attachments,
 *   <li>Inline CIRCABC branding logos referenced by their content-id (CID) in HTML bodies,
 *   <li>Calendar (iCalendar / iCal4j) meeting invitations and cancellations for CIRCABC events.
 * </ul>
 *
 * <p>Behaviour is driven by the injected {@link CircabcConfig}: mailing can be globally disabled,
 * an environment banner can be prepended to subjects/bodies (e.g. for non-production instances),
 * and all recipients can be transparently redirected to a single test address. Actual delivery is
 * delegated to the Spring {@link JavaMailSender}.
 *
 * @author Yanick Pignot
 */
public class MailServiceImpl implements MailService {

  /** Error message used when no destination address is provided to a send operation. */
  private static final String AT_LEAST_ONE_DESTINATION_EMAIL_ADDRESS_MUST_BE_SPECIFIED =
    "At least one destination email address must be specified  ";

  /** URI scheme prefix used when building iCalendar attendee/organizer addresses. */
  private static final String MAILTO = "mailto:";

  private static final Log logger = LogFactory.getLog(MailServiceImpl.class);

  /** {@link MessageFormat} template rendering the support address as an HTML {@code mailto:} link. */
  private static final String MAIL_TO_HREF =
    "<a href=\"mailto:{0}\" title=\"The circabc support team\" >{0}</a>";

  /** Prefix for the error logged when a message could not be delivered. */
  private static final String FAILED_TO_SEND_EMAIL = "Failed to send email to ";

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private FileFolderService fileFolderService;

  @Autowired
  private MailPreferencesService mailPreferencesService;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private ContentService contentService;

  /** Default "no-reply" sender address used for automated notifications. */
  private String noReply;
  /** Support team e-mail address exposed to end users and used in disclaimers. */
  private String support;
  /** Development team e-mail address. */
  private String devTeam;
  /** Help desk e-mail address. */
  private String helpDesk;

  /** Lazily resolved organizer address used for calendar invitations when the listener is active. */
  private String organizer = null;
  /** Lazily resolved flag indicating whether the event listener overrides the meeting organizer. */
  private Boolean isListenerActive = null;

  @Autowired
  private CircabcConfig circabcConfig;

  /** Plain text disclaimer template appended to messages (formatted at {@link #init()}). */
  private String disclamerText;
  /** HTML disclaimer template appended to messages (formatted at {@link #init()}). */
  private String disclamerHtml;
  /** Content-id (CID) referencing the inline disclaimer logo in HTML bodies. */
  private String logoCid;
  /** Content-id (CID) referencing the inline header logo in HTML bodies. */
  private String headerLogoCid = "mailHeaderLogoCid";
  /** Content-id (CID) referencing the inline EU header logo in HTML bodies. */
  private String headerEULogoCid = "mailHeaderEULogoCid";
  /** Content-id (CID) referencing the inline header background image in HTML bodies. */
  private String headerBackgroundCid = "mailHeaderBackgroundCid";

  /** Whether the environment name banner is prepended to subjects and bodies. */
  private boolean environmentNameEnabled = false;
  /** Environment name shown in the banner when {@link #environmentNameEnabled} is {@code true}. */
  private String environmentName = "";

  /** Whether all recipients are redirected to {@link #redirectEmailAddress} (e.g. for testing). */
  private boolean redirectEmailAddressEnabled = false;
  /** Single address all mail is redirected to when redirection is enabled. */
  private String redirectEmailAddress = null;

  /** Application name resolved from configuration and used in disclaimers. */
  private String applicationName;

  /**
   * Initializes the service after dependency injection.
   *
   * <p>Validates that all mandatory configuration values (addresses, disclaimer templates and logo
   * CIDs) are present, resolves the application name from configuration and formats the plain-text
   * and HTML disclaimer templates with the support link, web root URL and application name.
   */
  public void init() {
    ParameterCheck.mandatoryString("No reply email address", noReply);
    ParameterCheck.mandatoryString("No support email address", support);
    ParameterCheck.mandatoryString("A plain text disclamer", disclamerText);
    ParameterCheck.mandatoryString("An HTML disclamer", disclamerHtml);
    ParameterCheck.mandatoryString("An logo CID", logoCid);
    ParameterCheck.mandatoryString("A header logo CID", headerLogoCid);
    ParameterCheck.mandatoryString("A header eu logo CID", headerEULogoCid);

    applicationName = circabcConfig.getApplicationName();

    final String htmlSupport = MessageFormat.format(MAIL_TO_HREF, support);
    this.disclamerHtml = MessageFormat.format(
      disclamerHtml,
      htmlSupport,
      circabcConfig.getWebRootUrl(),
      applicationName
    );
    this.disclamerText = MessageFormat.format(
      disclamerText,
      support,
      circabcConfig.getWebRootUrl(),
      applicationName
    );
  }

  /**
   * Sends a message to a single recipient.
   *
   * @param from the sender address
   * @param to the destination address
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the message body
   * @param html {@code true} to send the body as HTML, {@code false} for plain text
   * @param useBCC {@code true} to add recipients as BCC instead of TO
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if the message cannot be built or addresses are invalid
   */
  public boolean send(
    final String from,
    final String to,
    final String replyTo,
    final String subject,
    final String body,
    final boolean html,
    final boolean useBCC
  ) throws MessagingException {
    MailSendRequest req = new MailSendRequest();
    req.setFrom(from);
    req.setTo(to);
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(html);
    req.setUseBCC(useBCC);
    return sendImplInternal(req);
  }

  private boolean isValidEmailList(final List<String> emails) {
    for (final String email : emails) {
      if (!EmailUtil.isValidEmailAddress(email)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Sends a message to a list of recipients. The first address becomes the primary recipient and
   * the remaining addresses are added as additional recipients.
   *
   * @param from the sender address
   * @param to the destination addresses; must be non-empty and all valid
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the message body
   * @param html {@code true} to send the body as HTML, {@code false} for plain text
   * @param useBCC {@code true} to add recipients as BCC instead of TO
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if the recipient list is empty/invalid or the message cannot be built
   */
  public boolean send(
    final String from,
    final List<String> to,
    final String replyTo,
    final String subject,
    final String body,
    final boolean html,
    boolean useBCC
  ) throws MessagingException {
    if (to == null || to.isEmpty() || !isValidEmailList(to)) {
      throw new MessagingException(
        AT_LEAST_ONE_DESTINATION_EMAIL_ADDRESS_MUST_BE_SPECIFIED + to
      );
    }

    MailSendRequest req = new MailSendRequest();
    req.setFrom(from);
    req.setTo(to.get(0));
    req.setOthers(to.subList(1, to.size()));
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(html);
    req.setUseBCC(useBCC);
    return sendImplInternal(req);
  }

  /**
   * Sends an HTML message with repository attachments to a list of recipients (using BCC). The
   * first address becomes the primary recipient and the remaining addresses are added as
   * additional BCC recipients.
   *
   * @param from the sender address
   * @param to the destination addresses; must be non-empty and all valid
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the HTML message body
   * @param attachments repository nodes to attach to the message
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if the recipient list is empty/invalid or the message cannot be built
   */
  public boolean send(
    final String from,
    final List<String> to,
    final String replyTo,
    final String subject,
    final String body,
    final List<NodeRef> attachments
  ) throws MessagingException {
    if (to == null || to.isEmpty() || !isValidEmailList(to)) {
      throw new MessagingException(
        AT_LEAST_ONE_DESTINATION_EMAIL_ADDRESS_MUST_BE_SPECIFIED + to
      );
    }

    MailSendRequest req = new MailSendRequest();
    req.setFrom(from);
    req.setTo(to.get(0));
    req.setOthers(to.subList(1, to.size()));
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(true);
    req.setAttachments(attachments);
    req.setUseBCC(true);
    return sendImplInternal(req);
  }

  /**
   * Sends an HTML message with repository attachments to a single recipient (using BCC).
   *
   * @param from the sender address
   * @param to the destination address; must be non-empty
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the HTML message body
   * @param attachments repository nodes to attach to the message
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if the destination address is empty or the message cannot be built
   */
  public boolean send(
    final String from,
    final String to,
    final String replyTo,
    final String subject,
    final String body,
    final List<NodeRef> attachments
  ) throws MessagingException {
    if (to == null || to.isEmpty()) {
      throw new MessagingException(
        "Cannot send one email to invalid address  " + to
      );
    }

    MailSendRequest req = new MailSendRequest();
    req.setFrom(from);
    req.setTo(to);
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(true);
    req.setAttachments(attachments);
    req.setUseBCC(true);
    return sendImplInternal(req);
  }

  /**
   * Sends a message to a single recipient with a repository node attached as content.
   *
   * @param content the mandatory repository node to attach; must not be {@code null}
   * @param from the sender address
   * @param to the destination address
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the message body
   * @param html {@code true} to send the body as HTML, {@code false} for plain text
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if {@code content} is {@code null} or the message cannot be built
   */
  public boolean sendNode(
    final NodeRef content,
    final String from,
    final String to,
    final String replyTo,
    final String subject,
    final String body,
    final boolean html
  ) throws MessagingException {
    if (content == null) {
      throw new MessagingException("The content is a mandatory parameter  ");
    }

    MailSendRequest req = new MailSendRequest();
    req.setContent(content);
    req.setFrom(from);
    req.setTo(to);
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(html);
    req.setUseBCC(false);
    return sendImplInternal(req);
  }

  /**
   * Sends a message to a list of recipients (using BCC) with a repository node attached as content.
   * The first address becomes the primary recipient and the remaining addresses are added as
   * additional BCC recipients.
   *
   * @param content the mandatory repository node to attach; must not be {@code null}
   * @param from the sender address
   * @param to the destination addresses; must be non-empty and all valid
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the message body
   * @param html {@code true} to send the body as HTML, {@code false} for plain text
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if the recipient list is empty/invalid, {@code content} is
   *     {@code null}, or the message cannot be built
   */
  public boolean sendNode(
    final NodeRef content,
    final String from,
    final List<String> to,
    final String replyTo,
    final String subject,
    final String body,
    final boolean html
  ) throws MessagingException {
    if (to == null || to.isEmpty() || !isValidEmailList(to)) {
      throw new MessagingException(
        AT_LEAST_ONE_DESTINATION_EMAIL_ADDRESS_MUST_BE_SPECIFIED + to
      );
    }

    if (content == null) {
      throw new MessagingException("The content is a mandatory parameter");
    }

    MailSendRequest req = new MailSendRequest();
    req.setContent(content);
    req.setFrom(from);
    req.setTo(to.get(0));
    req.setOthers(to.subList(1, to.size()));
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(html);
    req.setUseBCC(true);
    return sendImplInternal(req);
  }

  /**
   * @see http://static.springframework.org/spring/docs/2.0.x/api/org/springframework/mail/javamail/MimeMessageHelper.html
   */
  private boolean sendImplInternal(MailSendRequest req)
    throws MessagingException {
    if (!circabcConfig.isMailEnabled()) {
      if (logger.isDebugEnabled()) {
        logger.debug(
          "Mail sending is disabled, skipping email to: " + req.getTo()
        );
      }
      return true;
    }

    validateEmailAddresses(req.getFrom(), req.getTo());

    final MimeMessagePreparator mailPreparer = mimeMessage -> {
      MimeMessageHelper message = new MimeMessageHelper(
        mimeMessage,
        true,
        "UTF-8"
      );
      message.setFrom(req.getFrom());
      setRecipients(
        message,
        req.getTo(),
        req.getOthers(),
        req.getReplyTo(),
        req.isUseBCC()
      );
      String[] subjectAndBody = buildSubjectAndBody(
        req.getSubject(),
        req.getBody(),
        req.isHtml()
      );
      message.setSubject(subjectAndBody[0]);
      message.setText(subjectAndBody[1], req.isHtml());
      addInlineLogos(message);
      addAttachments(
        message,
        req.getContent(),
        req.getAttachments(),
        req.getFileAttachments()
      );
    };

    try {
      getMailSender().send(mailPreparer);
      return true;
    } catch (final Exception t) {
      if (logger.isErrorEnabled()) {
        logger.error(FAILED_TO_SEND_EMAIL + req.getTo(), t);
      }
      return false;
    }
  }

  private void validateEmailAddresses(String from, String to)
    throws MessagingException {
    if (!EmailUtil.isValidEmailAddress(from)) {
      throw new MessagingException(
        "The from email address is mandatory and must not be set as " + from
      );
    }

    String sanitizedTo = EmailUtil.sanitizeEmailAddresses(to);
    if (sanitizedTo == null || sanitizedTo.isEmpty()) {
      throw new MessagingException(
        "No valid destination email addresses found after sanitization: " + to
      );
    }
  }

  private void setRecipients(
    MimeMessageHelper message,
    String to,
    List<String> others,
    String replyTo,
    boolean useBCC
  ) throws MessagingException {
    String sanitizedTo = EmailUtil.sanitizeEmailAddresses(to);
    if (sanitizedTo != null && !sanitizedTo.isEmpty()) {
      String[] sanitizedToArray = sanitizedTo.split("\\s*,\\s*"); // NOSONAR - no backtracking risk, literal comma separates quantifiers
      for (String address : sanitizedToArray) {
        addRecipient(message, getTargetAddress(address), useBCC);
      }
    }

    if (others != null) {
      for (String otherTo : others) {
        if (EmailUtil.isValidEmailAddress(otherTo)) {
          addRecipient(message, getTargetAddress(otherTo), useBCC);
        } else if (
          otherTo != null && !otherTo.isEmpty() && logger.isWarnEnabled()
        ) {
          logger.warn(
            "Skipping invalid email address in others list: " + otherTo
          );
        }
      }
    }

    if (EmailUtil.isValidEmailAddress(replyTo)) {
      message.setReplyTo(getTargetAddress(replyTo));
    }
  }

  private String getTargetAddress(String address) {
    return redirectEmailAddressEnabled ? redirectEmailAddress : address;
  }

  private void addRecipient(
    MimeMessageHelper message,
    String address,
    boolean useBCC
  ) throws MessagingException {
    if (useBCC) {
      message.addBcc(address);
    } else {
      message.addTo(address);
    }
  }

  private String[] buildSubjectAndBody(
    String subject,
    String body,
    boolean html
  ) {
    String environmentSubject = "";
    String environmentBody = "";
    if (environmentNameEnabled) {
      environmentSubject = "[" + environmentName + "] ";
      String crlf = html ? "<br />" : "\n";
      String containerStart = html
        ? "<table style=\"color:#591E06; font-weight:bold; font-size:30px; background-color: orange; border:1px solid #591E06; font-family:monospace\"><tr><td>"
        : "";
      String containerEnd = html ? "</td></tr></table>" : "";
      String stars = "*".repeat(environmentName.length() + 12);
      environmentBody =
        containerStart +
        stars +
        crlf +
        "***** " +
        environmentName +
        " *****" +
        crlf +
        stars +
        crlf +
        containerEnd;
    }
    return new String[] {
      environmentSubject + subject,
      environmentBody + body,
    };
  }

  private void addInlineLogos(MimeMessageHelper message) {
    try {
      NodeRef circabcRootRef = circabcApi.getCircabcNodeRef();
      addInlineLogo(
        message,
        mailPreferencesService.getDisclamerLogo(circabcRootRef),
        logoCid
      );
      addInlineLogo(
        message,
        mailPreferencesService.getHeaderLogo(circabcRootRef),
        headerLogoCid
      );
      addInlineLogo(
        message,
        mailPreferencesService.getHeaderEULogo(circabcRootRef),
        headerEULogoCid
      );
    } catch (Exception ex) {
      if (logger.isErrorEnabled()) {
        logger.error("The logo resource is not accessible");
      }
    }
  }

  private void addInlineLogo(
    MimeMessageHelper message,
    NodeRef logoNodeRef,
    String cid
  ) throws MessagingException {
    ContentReader logoContent = contentService.getReader(
      logoNodeRef,
      ContentModel.PROP_CONTENT
    );
    if (logoContent != null) {
      message.addInline(
        cid,
        new InputSourceWrapper(logoContent),
        logoContent.getMimetype()
      );
    }
  }

  private void addAttachments(
    MimeMessageHelper message,
    NodeRef content,
    List<NodeRef> attachments,
    List<File> fileAttachments
  ) throws MessagingException {
    if (content != null) {
      addNodeAttachment(content, message);
    } else if (attachments != null) {
      for (NodeRef attachment : attachments) {
        addNodeAttachment(attachment, message);
      }
    }
    if (fileAttachments != null) {
      for (File file : fileAttachments) {
        String fileName = Converter.getOriginalFileName(file.getName());
        message.addAttachment(fileName, file);
      }
    }
  }

  private void addNodeAttachment(NodeRef content, MimeMessageHelper message)
    throws MessagingException {
    ContentReader cr = getFileFolderService().getReader(content);
    FileInfo fi = fileFolderService.getFileInfo(content);
    if (cr != null) {
      String name = fi.getName();
      try {
        name = MimeUtility.encodeText(name);
      } catch (UnsupportedEncodingException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Unable to encode file name : " + name, e);
        }
      }
      message.addAttachment(name, new InputSourceWrapper(cr), cr.getMimetype());
    }
  }

  /**
   * @return the configured "no-reply" sender address
   */
  public String getNoReplyEmailAddress() {
    return this.noReply;
  }

  /**
   * @return the configured support e-mail address
   */
  public String getSupportEmailAddress() {
    return this.support;
  }

  /**
   * @return the configured development team e-mail address
   */
  public final String getDevTeamEmailAddress() {
    return devTeam;
  }

  /**
   * @return the configured help desk e-mail address
   */
  public String getHelpdeskAddress() {
    return helpDesk;
  }

  /**
   * @param helpDesk the help desk e-mail address to set
   */
  public void setHelpdeskAddress(String helpDesk) {
    this.helpDesk = helpDesk;
  }

  /**
   * @param noReply the "no-reply" sender address to set
   */
  public void setNoReply(final String noReply) {
    this.noReply = noReply;
  }

  /**
   * @param support the support e-mail address to set
   */
  public void setSupport(final String support) {
    this.support = support;
  }

  /**
   * @param devTeam the development team e-mail address to set
   */
  public void setDevTeam(String devTeam) {
    this.devTeam = devTeam;
  }

  /**
   * @return the mailSender
   */
  public JavaMailSender getMailSender() {
    return mailSender;
  }

  /**
   * @param mailSender the mailSender to set
   */
  public void setMailSender(final JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * @return the fileFolderService
   */
  public FileFolderService getFileFolderService() {
    return fileFolderService;
  }

  /**
   * @param fileFolderService the fileFolderService to set
   */
  public void setFileFolderService(final FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * Sends an iCalendar meeting invitation (a {@code REQUEST} calendar method) to the given
   * recipients.
   *
   * @param from the sender address
   * @param to the recipients of the invitation
   * @param replyTo the reply-to address
   * @param meeting the meeting to invite to
   * @param oldMeeting the previous state of the meeting, used when updating a single or future
   *     occurrence of a recurring event; may be {@code null} for a new invitation
   * @param mode the update mode (all, single or future occurrences); {@code null} for a new meeting
   * @param useBCC {@code true} to add recipients as BCC instead of TO
   * @return {@code true} if the invitation was sent, {@code false} otherwise
   * @throws MailServiceException if the invitation could not be built or sent
   */
  public boolean sendMeetingRequest(
    final String from,
    final List<String> to,
    final String replyTo,
    final Meeting meeting,
    final Meeting oldMeeting,
    final UpdateMode mode,
    boolean useBCC
  ) throws MailServiceException {
    try {
      return doSendMeetingRequest(to, meeting, oldMeeting, mode, useBCC);
    } catch (Exception e) {
      throw new MailServiceException("Failed to send meeting request", e);
    }
  }

  private void setupMeetingMessage(
    final MimeMessage mimeMessage,
    final Meeting meeting,
    final List<String> to,
    final Message.RecipientType recipientType,
    final String logContext
  ) throws MessagingException {
    mimeMessage.setSubject(meeting.getTitle());
    final InternetAddress addressFrom = Boolean.TRUE.equals(
      getIsListenerActive()
    )
      ? new InternetAddress(getOrganizer())
      : new InternetAddress(meeting.getEmail());
    mimeMessage.addFrom(new InternetAddress[] { addressFrom });

    final InternetAddress[] addressTo = new InternetAddress[to.size()];
    int validCount = 0;
    for (int i = 0; i < to.size(); i++) {
      final String email = to.get(i);
      if (EmailUtil.isValidEmailAddress(email)) {
        addressTo[validCount++] = new InternetAddress(email);
      } else if (logger.isWarnEnabled()) {
        logger.warn(
          "Skipping invalid email address in " + logContext + ": " + email
        );
      }
    }
    if (validCount > 0) {
      final InternetAddress[] validAddresses = new InternetAddress[validCount];
      System.arraycopy(addressTo, 0, validAddresses, 0, validCount);
      mimeMessage.addRecipients(recipientType, validAddresses);
    }
  }

  private String getMeetingContent(final Meeting meeting) {
    if (
      meeting.getInvitationMessage() != null &&
      !meeting.getInvitationMessage().equalsIgnoreCase("")
    ) {
      return meeting.getInvitationMessage();
    }
    return meeting.getTitle();
  }

  private boolean sendMeetingMimeMessage(
    final MimeMessage mimeMessage,
    final List<String> to
  ) {
    boolean done = false;
    try {
      final MimetypesFileTypeMap mimetypes =
        (MimetypesFileTypeMap) FileTypeMap.getDefaultFileTypeMap();
      mimetypes.addMimeTypes("text/calendar ics ICS");

      final MailcapCommandMap mailcap =
        (MailcapCommandMap) CommandMap.getDefaultCommandMap();
      mailcap.addMailcap(
        "text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain"
      );

      getMailSender().send(mimeMessage);
      done = true;
    } catch (final Exception t) {
      if (logger.isErrorEnabled()) {
        // don't stop the action but let admins know email is not getting sent
        logger.error(FAILED_TO_SEND_EMAIL + to, t);
      }
    }
    return done;
  }

  private DateTime[] calculateRequestDateTimes(
    final Meeting meeting,
    final Meeting oldMeeting,
    final UpdateMode mode,
    final TimeZone timezone
  ) throws java.text.ParseException {
    DateTime start;
    DateTime end;
    DateTime recurrenceId = null;

    if (mode == null || mode == UpdateMode.AllOccurences) {
      start = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getStartTime(),
          meeting.getStartDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
      end = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getEndTime(),
          meeting.getStartDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
    } else if (mode == UpdateMode.Single) {
      recurrenceId = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          oldMeeting.getStartTime(),
          oldMeeting.getDate(),
          oldMeeting.getTimeZoneId()
        ),
        timezone
      );
      start = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getStartTime(),
          meeting.getDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
      end = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getEndTime(),
          meeting.getDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
    } else {
      recurrenceId = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          oldMeeting.getStartTime(),
          oldMeeting.getDate(),
          oldMeeting.getTimeZoneId()
        ),
        timezone
      );
      start = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getStartTime(),
          meeting.getStartDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
      end = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getEndTime(),
          meeting.getStartDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
    }
    return new DateTime[] { start, end, recurrenceId };
  }

  private boolean doSendMeetingRequest(
    final List<String> to,
    final Meeting meeting,
    final Meeting oldMeeting,
    final UpdateMode mode,
    boolean useBCC
  ) throws IOException, MessagingException, java.text.ParseException {
    CompatibilityHints.setHintEnabled(
      CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY,
      true
    );
    final MimeMessage mimeMessage = getMailSender().createMimeMessage();
    final Message.RecipientType recipientType = useBCC
      ? Message.RecipientType.BCC
      : Message.RecipientType.TO;
    setupMeetingMessage(
      mimeMessage,
      meeting,
      to,
      recipientType,
      "meeting request"
    );

    final TimeZoneRegistry registry = new TimeZoneRegistryImpl(
      "/zoneinfo-outlook/"
    );
    final TimeZone timezone = registry.getTimeZone("GMT");
    final VTimeZone tz = timezone.getVTimeZone();

    final DateTime[] dates = calculateRequestDateTimes(
      meeting,
      oldMeeting,
      mode,
      timezone
    );
    final String rrule = (mode == null || mode == UpdateMode.AllOccurences)
      ? meeting.getRRule()
      : null;

    final String invite = createICalInvitation(
      meeting.getId(),
      meeting.getTitle(),
      getMeetingContent(meeting),
      dates[0],
      dates[1],
      tz,
      meeting,
      to,
      rrule,
      meeting.getLocation(),
      meeting.getUrl(),
      dates[2],
      mode
    );

    final Multipart multipart = new MimeMultipart();
    final MimeBodyPart iCalAttachment = new MimeBodyPart();
    iCalAttachment.setDataHandler(
      new DataHandler(invite, "text/calendar;method=REQUEST;charset=\"UTF-8\"")
    );
    multipart.addBodyPart(iCalAttachment);
    mimeMessage.setContent(multipart);

    return sendMeetingMimeMessage(mimeMessage, to);
  }

  // NOSONAR: Method has 13 parameters due to iCal invitation requirements.
  // All parameters are needed for proper calendar event creation.
  // Refactoring to use a builder/DTO would require significant API changes.
  private String createICalInvitation( // NOSONAR
    final String meetingID,
    final String subject,
    final String content,
    final Date start,
    final Date end,
    final VTimeZone timeZone,
    final Meeting meeting,
    final List<String> emails,
    final String rrule,
    final String location,
    final String url,
    final Date recurrenceId,
    final UpdateMode mode
  ) throws java.text.ParseException, java.io.IOException {
    CompatibilityHints.setHintEnabled(
      CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY,
      true
    );

    final VEvent vEvent = new VEvent();
    vEvent.getProperties().add(new Uid(meetingID));
    vEvent.getProperties().add(timeZone.getTimeZoneId());

    addOptionalProperty(vEvent, location, Location::new);
    addUrlProperty(vEvent, url);

    vEvent.getProperties().add(new Summary(subject));
    vEvent.getProperties().add(new Description(content));

    addDateProperties(vEvent, start, end);
    addRecurrenceProperties(vEvent, rrule, mode, recurrenceId);
    addAttendees(vEvent, emails);
    addOrganizer(vEvent, meeting);

    if (meeting.getSequence() != null) {
      vEvent.getProperties().add(new Sequence(meeting.getSequence()));
    }
    vEvent
      .getProperties()
      .add(
        meeting.getAudienceStatus() == AudienceStatus.Closed
          ? Clazz.PRIVATE
          : Clazz.PUBLIC
      );

    return buildCalendarString(vEvent, timeZone);
  }

  private void addOptionalProperty(
    VEvent vEvent,
    String value,
    java.util.function.Function<String, Property> propertyCreator
  ) {
    if (value != null && !value.equalsIgnoreCase("")) {
      vEvent.getProperties().add(propertyCreator.apply(value));
    }
  }

  private void addUrlProperty(VEvent vEvent, String url) {
    if (url == null || url.equalsIgnoreCase("")) {
      return;
    }
    try {
      vEvent.getProperties().add(new Url(new URI(url)));
    } catch (final Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid url:" + url, e);
      }
    }
  }

  private void addDateProperties(VEvent vEvent, Date start, Date end) {
    if (start != null) {
      vEvent.getProperties().add(new DtStart(start));
    }
    if (end != null) {
      vEvent.getProperties().add(new DtEnd(end));
    }
  }

  private void addRecurrenceProperties(
    VEvent vEvent,
    String rrule,
    UpdateMode mode,
    Date recurrenceId
  ) throws java.text.ParseException {
    if (mode == null) {
      if (rrule != null) {
        vEvent.getProperties().add(new RRule(rrule));
      }
    } else {
      vEvent.getProperties().add(Status.VEVENT_CONFIRMED);
    }

    if (mode == UpdateMode.FuturOccurences) {
      final ParameterList pl = new ParameterList();
      pl.add(Range.THISANDFUTURE);
      vEvent.getProperties().add(new RecurrenceId(pl, recurrenceId));
    } else if (mode == UpdateMode.Single && recurrenceId != null) {
      vEvent.getProperties().add(new RecurrenceId(recurrenceId));
    }
  }

  private void addAttendees(VEvent vEvent, List<String> emails) {
    for (final String user : emails) {
      final Attendee attendee = new Attendee(URI.create(MAILTO + user));
      attendee.getParameters().add(Role.REQ_PARTICIPANT);
      attendee.getParameters().add(PartStat.NEEDS_ACTION);
      attendee.getParameters().add(Rsvp.TRUE);
      vEvent.getProperties().add(attendee);
    }
  }

  private void addOrganizer(VEvent vEvent, Meeting meeting) {
    String organizerEmail = Boolean.TRUE.equals(getIsListenerActive())
      ? getOrganizer()
      : meeting.getEmail();
    vEvent
      .getProperties()
      .add(new Organizer(URI.create(MAILTO + organizerEmail)));
  }

  private String buildCalendarString(VEvent vEvent, VTimeZone timeZone)
    throws java.io.IOException {
    net.fortuna.ical4j.model.Calendar cal =
      new net.fortuna.ical4j.model.Calendar();
    cal.getProperties().add(new ProdId("-//CIRCABC//iCal4j 1.0//EN"));
    cal
      .getProperties()
      .add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
    cal.getProperties().add(CalScale.GREGORIAN);
    cal.getProperties().add(net.fortuna.ical4j.model.property.Method.REQUEST);

    cal.getComponents().add(timeZone);
    cal.getComponents().add(vEvent);

    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    final CalendarOutputter outputter = new CalendarOutputter();
    outputter.output(cal, bout);
    return bout.toString(StandardCharsets.UTF_8);
  }

  /**
   * Sends an iCalendar meeting cancellation (a {@code CANCEL} calendar method) to the given
   * recipients.
   *
   * @param from the sender address
   * @param to the recipients of the cancellation
   * @param replyTo the reply-to address
   * @param meeting the meeting being cancelled
   * @param eventDate the date of the occurrence being cancelled (for single/future occurrences)
   * @param mode the update mode (all, single or future occurrences)
   * @return {@code true} if the cancellation was sent, {@code false} otherwise
   * @throws MailServiceException if the cancellation could not be built or sent
   */
  public boolean cancelMeeting(
    final String from,
    final List<String> to,
    final String replyTo,
    final Meeting meeting,
    final java.util.Date eventDate,
    final UpdateMode mode
  ) throws MailServiceException {
    try {
      return doCancelMeeting(to, meeting, eventDate, mode);
    } catch (Exception e) {
      throw new MailServiceException("Failed to cancel meeting", e);
    }
  }

  private DateTime[] calculateCancelDateTimes(
    final Meeting meeting,
    final java.util.Date eventDate,
    final UpdateMode mode,
    final TimeZone timezone
  ) throws java.text.ParseException {
    DateTime start;
    DateTime end;
    DateTime icalEventDate = null;

    if (mode == UpdateMode.AllOccurences) {
      start = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getStartTime(),
          meeting.getStartDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
      end = new DateTime(
        AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
          meeting.getEndTime(),
          meeting.getStartDate(),
          meeting.getTimeZoneId()
        ),
        timezone
      );
      if (eventDate != null) {
        icalEventDate = new DateTime(
          AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
            meeting.getStartTime(),
            eventDate,
            meeting.getTimeZoneId()
          )
        );
      }
    } else {
      start = new DateTime(
        AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
          meeting.getStartTime(),
          eventDate,
          meeting.getTimeZoneId()
        ),
        timezone
      );
      end = new DateTime(
        AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
          meeting.getEndTime(),
          eventDate,
          meeting.getTimeZoneId()
        ),
        timezone
      );
      if (eventDate != null) {
        icalEventDate = new DateTime(
          AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
            meeting.getStartTime(),
            eventDate,
            meeting.getTimeZoneId()
          ),
          timezone
        );
      }
    }
    return new DateTime[] { start, end, icalEventDate };
  }

  private boolean doCancelMeeting(
    final List<String> to,
    final Meeting meeting,
    final java.util.Date eventDate,
    final UpdateMode mode
  ) throws IOException, MessagingException, java.text.ParseException {
    final MimeMessage mimeMessage = getMailSender().createMimeMessage();
    setupMeetingMessage(
      mimeMessage,
      meeting,
      to,
      Message.RecipientType.TO,
      "meeting cancellation"
    );

    final TimeZoneRegistry registry = new TimeZoneRegistryImpl(
      "/zoneinfo-outlook/"
    );
    final TimeZone timezone = registry.getTimeZone("GMT");
    final VTimeZone tz = timezone.getVTimeZone();

    final DateTime[] dates = calculateCancelDateTimes(
      meeting,
      eventDate,
      mode,
      timezone
    );

    final String invite = createICalCancelation(
      meeting.getId(),
      meeting.getTitle(),
      getMeetingContent(meeting),
      dates[0],
      dates[1],
      dates[2],
      tz,
      meeting,
      to,
      mode
    );

    final Multipart multipart = new MimeMultipart();
    final MimeBodyPart iCalAttachment = new MimeBodyPart();
    iCalAttachment.setDataHandler(
      new DataHandler(invite, "text/calendar;method=CANCEL;charset=\"UTF-8\"")
    );
    multipart.addBodyPart(iCalAttachment);
    mimeMessage.setContent(multipart);

    return sendMeetingMimeMessage(mimeMessage, to);
  }

  // NOSONAR: Method has 10 parameters due to iCal cancellation requirements.
  // All parameters are needed for proper calendar event cancellation.
  private String createICalCancelation( // NOSONAR
    final String meetingID,
    final String subject,
    final String content,
    final Date start,
    final DateTime end,
    final Date eventDate,
    final VTimeZone timeZone,
    final Meeting meeting,
    final List<String> emails,
    final UpdateMode mode
  ) throws java.io.IOException {
    CompatibilityHints.setHintEnabled(
      CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY,
      true
    );

    final VEvent vEvent = new VEvent();
    vEvent.getProperties().add(new Uid(meetingID));
    vEvent.getProperties().add(timeZone.getTimeZoneId());

    vEvent.getProperties().add(new Summary(subject));
    vEvent.getProperties().add(new Description(content));

    for (final String user : emails) {
      final Attendee attendee = new Attendee(URI.create(MAILTO + user));
      attendee.getParameters().add(Role.OPT_PARTICIPANT);
      vEvent.getProperties().add(attendee);
    }

    if (mode == UpdateMode.AllOccurences) {
      vEvent.getProperties().add(Status.VEVENT_CANCELLED);
    } else if (mode == UpdateMode.FuturOccurences) {
      vEvent.getProperties().add(Status.VEVENT_CANCELLED);
      final ParameterList pl = new ParameterList();
      pl.add(Range.THISANDFUTURE);
      vEvent.getProperties().add(new RecurrenceId(pl, eventDate));
    } else if (mode == UpdateMode.Single) {
      vEvent.getProperties().add(Status.VEVENT_CANCELLED);
      if (eventDate != null) {
        vEvent.getProperties().add(new RecurrenceId(eventDate));
      }
    }

    vEvent.getProperties().add(new DtStart(start));
    vEvent.getProperties().add(new DtEnd(end));

    String organizerEmail = meeting.getEmail();
    if (Boolean.TRUE.equals(getIsListenerActive())) {
      organizerEmail = getOrganizer();
    }

    final Organizer localOrganizer = new Organizer(
      URI.create(MAILTO + organizerEmail)
    );

    vEvent.getProperties().add(localOrganizer);
    vEvent.getProperties().add(new Sequence(meeting.getSequence()));

    net.fortuna.ical4j.model.Calendar cal =
      new net.fortuna.ical4j.model.Calendar();
    cal.getProperties().add(new ProdId("-//CIRCABC//iCal4j 1.0//EN"));
    cal
      .getProperties()
      .add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
    cal.getProperties().add(CalScale.GREGORIAN);
    cal.getProperties().add(net.fortuna.ical4j.model.property.Method.CANCEL);

    cal.getComponents().add(timeZone);
    cal.getComponents().add(vEvent);

    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
    final CalendarOutputter outputter = new CalendarOutputter();
    outputter.output(cal, bout);
    if (logger.isInfoEnabled()) {
      logger.info(bout.toString(StandardCharsets.UTF_8));
    }
    return bout.toString(StandardCharsets.UTF_8);
  }

  /**
   * Returns the organizer address used for calendar invitations, lazily resolving it from
   * configuration on first access.
   *
   * @return the organizer e-mail address
   */
  public String getOrganizer() {
    if (organizer == null) {
      organizer = circabcConfig.getEmailAddress();
    }
    return organizer;
  }

  /**
   * Returns whether the event listener overrides the meeting organizer, lazily resolving the flag
   * from configuration on first access.
   *
   * @return {@code true} if the listener is active
   */
  public Boolean getIsListenerActive() {
    if (isListenerActive == null) {
      isListenerActive = circabcConfig.isListenerActive();
    }
    return isListenerActive;
  }

  /**
   * @return the disclamerHtml
   */
  public final String getDisclamerHtml() {
    return disclamerHtml;
  }

  /**
   * @param disclamerHtml the disclamerHtml to set
   */
  public final void setDisclamerHtml(final String disclamerHtml) {
    this.disclamerHtml = disclamerHtml;
  }

  /**
   * @return the disclamerText
   */
  public final String getDisclamerText() {
    return disclamerText;
  }

  /**
   * @param disclamerText the disclamerText to set
   */
  public final void setDisclamerText(final String disclamerText) {
    this.disclamerText = disclamerText;
  }

  /**
   * @return the logoCid
   */
  public final String getLogoCid() {
    return logoCid;
  }

  /**
   * @param logoCid the logoCid to set
   */
  public final void setLogoCid(final String logoCid) {
    this.logoCid = logoCid;
  }

  /**
   * @return the header logo content-id (CID)
   */
  public String getHeaderLogoCid() {
    return headerLogoCid;
  }

  /**
   * @param headerLogoCid the header logo content-id (CID) to set
   */
  public void setHeaderLogoCid(String headerLogoCid) {
    this.headerLogoCid = headerLogoCid;
  }

  /**
   * @return the headerEULogoCid
   */
  public String getHeaderEULogoCid() {
    return headerEULogoCid;
  }

  /**
   * @param headerEULogoCid the headerEULogoCid to set
   */
  public void setHeaderEULogoCid(String headerEULogoCid) {
    this.headerEULogoCid = headerEULogoCid;
  }

  /**
   * @return the header background image content-id (CID)
   */
  public String getHeaderBackgroundCid() {
    return headerBackgroundCid;
  }

  /**
   * @param headerBackgroundCid the header background image content-id (CID) to set
   */
  public void setHeaderBackgroundCid(String headerBackgroundCid) {
    this.headerBackgroundCid = headerBackgroundCid;
  }

  /**
   * @param environmentNameEnabled the environmentNameEnabled to set
   */
  public void setEnvironmentNameEnabled(boolean environmentNameEnabled) {
    this.environmentNameEnabled = environmentNameEnabled;
  }

  /**
   * @param environmentName the environmentName to set
   */
  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  /**
   * @param redirectEmailAddressEnabled the redirectEmailAddressEnabled to set
   */
  public void setRedirectEmailAddressEnabled(
    boolean redirectEmailAddressEnabled
  ) {
    this.redirectEmailAddressEnabled = redirectEmailAddressEnabled;
  }

  /**
   * @param redirectEmailAddress the redirectEmailAddress to set
   */
  public void setRedirectEmailAddress(String redirectEmailAddress) {
    this.redirectEmailAddress = redirectEmailAddress;
  }

  /**
   * @return the applicationName
   */
  public String getApplicationName() {
    return applicationName;
  }

  /**
   * @param applicationName the applicationName to set
   */
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * Sends an HTML message with local file attachments to a single recipient.
   *
   * @param from the sender address
   * @param to the destination address; must be non-empty
   * @param replyTo the reply-to address (ignored if not a valid address)
   * @param subject the message subject
   * @param body the HTML message body
   * @param html retained for interface compatibility; the body is always sent as HTML
   * @param useBCC {@code true} to add the recipient as BCC instead of TO
   * @param attachements local files to attach to the message
   * @return {@code true} if the message was sent (or mailing is disabled), {@code false} on failure
   * @throws MessagingException if the destination address is empty or the message cannot be built
   */
  @Override
  public boolean sendWithAttachment(
    String from,
    String to,
    String replyTo,
    String subject,
    String body,
    boolean html,
    boolean useBCC,
    List<File> attachements
  ) throws MessagingException {
    if (to == null || to.isEmpty()) {
      throw new MessagingException(
        AT_LEAST_ONE_DESTINATION_EMAIL_ADDRESS_MUST_BE_SPECIFIED + to
      );
    }

    MailSendRequest req = new MailSendRequest();
    req.setFrom(from);
    req.setTo(to);
    req.setReplyTo(replyTo);
    req.setSubject(subject);
    req.setBody(body);
    req.setHtml(true);
    req.setUseBCC(useBCC);
    req.setFileAttachments(attachements);
    return sendImplInternal(req);
  }
}
