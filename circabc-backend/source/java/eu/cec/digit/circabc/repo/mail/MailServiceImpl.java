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
package eu.cec.digit.circabc.repo.mail;

import eu.cec.digit.circabc.action.InputSourceWrapper;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.repo.event.AppointmentUtils;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.event.AudienceStatus;
import eu.cec.digit.circabc.service.event.Meeting;
import eu.cec.digit.circabc.service.event.UpdateMode;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.*;
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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

/**
 * Implementation of the mail service.
 *
 * @author Yanick Pignot
 */
public class MailServiceImpl implements MailService {

    private static final String MAILTO = "mailto:";

    private static final Log logger = LogFactory.getLog(MailServiceImpl.class);

    private static final String MAIL_TO_HREF =
            "<a href=\"mailto:{0}\" title=\"The circabc support team\" >{0}</a>";

    /**
     * JavaMailSender bean reference
     */
    private JavaMailSender mailSender;
    /**
     * The file folder service reference
     */
    private FileFolderService fileFolderService;

    private MailPreferencesService mailPreferencesService;
    private ManagementService managementService;
    private ContentService contentService;

    private String noReply;
    private String support;
    private String devTeam;
    private String helpDesk;

    private String organizer = null;
    private String isListenerActive = null;
    private String webRootUrl = CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);

    private String disclamerText;
    private String disclamerHtml;
    private String logoCid;
    private String headerLogoCid = "mailHeaderLogoCid";
    private String headerEULogoCid = "mailHeaderEULogoCid";
    private String headerBackgroundCid = "mailHeaderBackgroundCid";

    private boolean environmentNameEnabled = false;
    private String environmentName = "";

    private boolean redirectEmailAddressEnabled = false;
    private String redirectEmailAddress = null;

    private String applicationName;

    public void init() {
        ParameterCheck.mandatoryString("No reply email address", noReply);
        ParameterCheck.mandatoryString("No support email address", support);
        ParameterCheck.mandatoryString("A plain text disclamer", disclamerText);
        ParameterCheck.mandatoryString("An HTML disclamer", disclamerHtml);
        ParameterCheck.mandatoryString("WebRootUrl", webRootUrl);
        ParameterCheck.mandatoryString("An logo CID", logoCid);
        ParameterCheck.mandatoryString("A header logo CID", headerLogoCid);
        ParameterCheck.mandatoryString("A header eu logo CID", headerEULogoCid);

        applicationName = CircabcConfiguration.getApplicationName();

        final String htmlSupport = MessageFormat.format(MAIL_TO_HREF, support);
        this.disclamerHtml =
                MessageFormat.format(disclamerHtml, htmlSupport, webRootUrl, applicationName);
        this.disclamerText = MessageFormat.format(disclamerText, support, webRootUrl, applicationName);
    }

    public boolean send(
            final String from,
            final String to,
            final String replyTo,
            final String subject,
            final String body,
            final boolean html,
            final boolean useBCC)
            throws MessagingException {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
        }

        return sendImpl(null, from, to, null, replyTo, subject, body, html, null, useBCC);
    }

    private boolean isValidEmailList(final List<String> emails) {

        for (final String email : emails) {
            if (!email.contains("@")) {
                return false;
            }
        }
        return true;
    }

    public boolean send(
            final String from,
            final List<String> to,
            final String replyTo,
            final String subject,
            final String body,
            final boolean html,
            boolean useBCC)
            throws MessagingException {
        if (to == null || to.size() < 1 || !isValidEmailList(to)) {
            throw new MessagingException(
                    "At least one destination email address must be specified  " + to);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
        }

        return sendImpl(
                null,
                from,
                to.get(0),
                to.subList(1, to.size()),
                replyTo,
                subject,
                body,
                html,
                null,
                useBCC);
    }

    public boolean send(
            final String from,
            final List<String> to,
            final String replyTo,
            final String subject,
            final String body,
            final List<NodeRef> attachments)
            throws MessagingException {
        if (to == null || to.size() < 1 || !isValidEmailList(to)) {
            throw new MessagingException(
                    "At least one destination email address must be specified  " + to);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
        }

        return sendImpl(
                null,
                from,
                to.get(0),
                to.subList(1, to.size()),
                replyTo,
                subject,
                body,
                true,
                attachments,
                true);
    }

    public boolean send(
            final String from,
            final String to,
            final String replyTo,
            final String subject,
            final String body,
            final List<NodeRef> attachments)
            throws MessagingException {
        if (to == null || to.length() < 1) {
            throw new MessagingException("Cannot send one email to invalid address  " + to);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
        }

        return sendImpl(null, from, to, null, replyTo, subject, body, true, attachments, true);
    }

    public boolean sendNode(
            final NodeRef content,
            final String from,
            final String to,
            final String replyTo,
            final String subject,
            final String body,
            final boolean html)
            throws MessagingException {
        if (content == null) {
            throw new MessagingException("The content is a mandatory parameter  ");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: "
                            + to
                            + "\n...with subject:\n"
                            + subject
                            + "\n...with body:\n"
                            + body
                            + "\n...with content:\n"
                            + content);
        }

        return sendImpl(content, from, to, null, replyTo, subject, body, html, null, false);
    }

    public boolean sendNode(
            final NodeRef content,
            final String from,
            final List<String> to,
            final String replyTo,
            final String subject,
            final String body,
            final boolean html)
            throws MessagingException {
        if (to == null || to.size() < 1 || !isValidEmailList(to)) {
            throw new MessagingException(
                    "At least one destination email address must be specified  " + to);
        }

        if (content == null) {
            throw new MessagingException("The content is a mandatory parameter");
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: "
                            + to
                            + "\n...with subject:\n"
                            + subject
                            + "\n...with body:\n"
                            + body
                            + "\n...with content:\n"
                            + content);
        }

        return sendImpl(
                content,
                from,
                to.get(0),
                to.subList(1, to.size()),
                replyTo,
                subject,
                body,
                html,
                null,
                true);
    }

    private boolean sendImpl(
            final NodeRef content,
            final String from,
            final String to,
            final List<String> others,
            final String replyTo,
            final String subject,
            final String body,
            final boolean html,
            final List<NodeRef> attachments,
            final boolean useBCC)
            throws MessagingException {
        return sendImplInternal(
                content, from, to, others, replyTo, subject, body, html, attachments, useBCC, null);
    }

    /**
     * @see http://static.springframework.org/spring/docs/2.0.x/api/org/springframework/mail/javamail/MimeMessageHelper.html
     */
    private boolean sendImplInternal(
            final NodeRef content,
            final String from,
            final String to,
            final List<String> others,
            final String replyTo,
            final String subject,
            final String body,
            final boolean html,
            final List<NodeRef> attachments,
            final boolean useBCC,
            final List<File> attachements)
            throws MessagingException {
        if (from == null || from.length() < 3 || from.indexOf('@') == -1) {
            throw new MessagingException(
                    "The from email address is mandatory and must not be set as " + from);
        }

        if (to == null || to.length() < 3 || to.indexOf('@') == -1) {
            throw new MessagingException(
                    "The destination email address is mandatory and must not be set as " + to);
        }

        final MimeMessagePreparator mailPreparer =
                new MimeMessagePreparator() {

                    public void prepare(final MimeMessage mimeMessage) throws MessagingException {

                        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                        // set the email addresses
                        message.setFrom(from);

                        if (redirectEmailAddressEnabled) {

                            if (useBCC) {
                                message.setBcc(redirectEmailAddress);
                            } else {
                                message.setTo(redirectEmailAddress);
                            }

                            if (others != null) {
                                for (final String otherTo : others) {
                                    if (otherTo != null) {
                                        if (useBCC) {
                                            message.addBcc(redirectEmailAddress);
                                        } else {
                                            message.addTo(redirectEmailAddress);
                                        }
                                    }
                                }
                            }

                            if (replyTo != null) {
                                if (replyTo.indexOf('@') != -1) {
                                    message.setReplyTo(redirectEmailAddress);
                                }
                            }
                        } else {
                            if (useBCC) {
                                message.setBcc(to);
                            } else {
                                message.setTo(to);
                            }

                            if (others != null) {
                                for (final String otherTo : others) {
                                    if (otherTo != null) {
                                        if (useBCC) {
                                            message.addBcc(otherTo);
                                        } else {
                                            message.addTo(otherTo);
                                        }
                                    }
                                }
                            }

                            if (replyTo != null) {
                                if (replyTo.indexOf('@') != -1) {
                                    message.setReplyTo(replyTo);
                                }
                            }
                        }

                        String crlf = "\n";
                        String containerStart = "";
                        String containerEnd = "";
                        final String disclamer;
                        if (html) {
                            disclamer = disclamerHtml;
                            crlf = "<br />";
                            containerStart =
                                    "<table style=\"color:#591E06; font-weight:bold; font-size:30px; background-color: orange; border:1px solid #591E06; font-family:monospace\"><tr><td>";
                            containerEnd = "</td></tr></table>";
                        } else {
                            disclamer = disclamerText;
                        }

                        // add the environment name to the emails to determine from
                        // which one the email came from
                        String environmentSubject = "";
                        String environmentBody = "";
                        if (environmentNameEnabled) {

                            environmentSubject = "[" + environmentName + "] ";

                            StringBuilder environmentStringBuffer = new StringBuilder(containerStart);
                            for (int idx = 0; idx < environmentName.length() + 12; idx++) {
                                environmentStringBuffer.append("*");
                            }
                            environmentStringBuffer
                                    .append(crlf)
                                    .append("*****")
                                    .append(" ")
                                    .append(environmentName)
                                    .append(" ")
                                    .append("*****")
                                    .append(crlf);
                            for (int idx = 0; idx < environmentName.length() + 12; idx++) {
                                environmentStringBuffer.append("*");
                            }
                            environmentStringBuffer.append(crlf).append(containerEnd);
                            environmentBody = environmentStringBuffer.toString();
                        }

                        // set the subject
                        message.setSubject(environmentSubject + subject);
                        // set the body
                        String msgBody = environmentBody + body; // + disclamer;
                        message.setText(msgBody, html);
                        // set the inline logo
                        try {
                            final NodeRef circabcRootRef = managementService.getCircabcNodeRef();
                            final NodeRef logoNodeRef = mailPreferencesService.getDisclamerLogo(circabcRootRef);
                            final NodeRef headerLogoNodeRef =
                                    mailPreferencesService.getHeaderLogo(circabcRootRef);
                            final NodeRef headerEULogoNodeRef =
                                    mailPreferencesService.getHeaderEULogo(circabcRootRef);
                            // final NodeRef headerBackgroundNodeRef =
                            // mailPreferencesService.getHeaderBackground(circabcRootRef);

                            final ContentReader logoContent =
                                    contentService.getReader(logoNodeRef, ContentModel.PROP_CONTENT);
                            final ContentReader headerLogoContent =
                                    contentService.getReader(headerLogoNodeRef, ContentModel.PROP_CONTENT);
                            final ContentReader headerEULogoContent =
                                    contentService.getReader(headerEULogoNodeRef, ContentModel.PROP_CONTENT);
                            // final ContentReader headerBackgroundContent =
                            // contentService.getReader(headerBackgroundNodeRef, ContentModel.PROP_CONTENT);

                            message.addInline(
                                    logoCid, new InputSourceWrapper(logoContent), logoContent.getMimetype());
                            message.addInline(
                                    headerLogoCid,
                                    new InputSourceWrapper(headerLogoContent),
                                    headerLogoContent.getMimetype());
                            message.addInline(
                                    headerEULogoCid,
                                    new InputSourceWrapper(headerEULogoContent),
                                    headerEULogoContent.getMimetype());
                            // message.addInline(headerBackgroundCid, new
                            // InputSourceWrapper(headerBackgroundContent),
                            // headerBackgroundContent.getMimetype());

                        } catch (final Exception ex) {
                            // The logo is not accessible. Skip the addInline method.
                            if (logger.isErrorEnabled()) {
                                logger.error("The logo ressourc e is not accessible");
                            }
                        }

                        // ad attachement if required
                        if (content != null) {
                            addAttachment(content, message);
                        } else if (attachments != null) {
                            for (NodeRef content : attachments) {
                                addAttachment(content, message);
                            }
                        }

                        if (attachements != null) {
                            for (File file : attachements) {
                                String fileName = file.getName();

                                // cbctmp = 6 + random long number generated by alfresco temp file = 19 char
                                if (fileName.contains("cbctmp") && (fileName.length() - 6 - 19) > 0) {
                                    fileName = fileName.substring(0, fileName.length() - 6 - 19);
                                }

                                message.addAttachment(fileName, file);
                            }
                        }
                    }

                    /**
                     * @param content
                     * @param message
                     * @throws MessagingException
                     */
                    private void addAttachment(final NodeRef content, final MimeMessageHelper message)
                            throws MessagingException {
                        final ContentReader cr = getFileFolderService().getReader(content);
                        // get file info
                        final FileInfo fi = fileFolderService.getFileInfo(content);

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
                };

        boolean done = false;

        try {
            // Send the message in a not blocking process
            getMailSender().send(mailPreparer);
            done = true;
        } catch (final Throwable t) {
            if (logger.isErrorEnabled())
            // don't stop the action but let admins know email is not getting sent
            {
                logger.error("Failed to send email to " + to, t);
            }
        }

        return done;
    }

    public String getNoReplyEmailAddress() {
        return this.noReply;
    }

    public String getSupportEmailAddress() {
        return this.support;
    }

    public final String getDevTeamEmailAddress() {
        return devTeam;
    }

    public String getHelpdeskAddress() {
        return helpDesk;
    }

    public void setHelpdeskAddress(String helpDesk) {
        this.helpDesk = helpDesk;
    }

    public void setNoReply(final String noReply) {
        this.noReply = noReply;
    }

    public void setSupport(final String support) {
        this.support = support;
    }

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

    public boolean sendMeetingRequest(
            final String from,
            final List<String> to,
            final String replyTo,
            final Meeting meeting,
            final Meeting oldMeeting,
            final UpdateMode mode,
            boolean useBCC)
            throws Exception {
        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);
        final MimeMessage mimeMessage = getMailSender().createMimeMessage();

        mimeMessage.setSubject(meeting.getTitle());
        final InternetAddress[] addressFrom = new InternetAddress[1];
        if (getIsListenerActive()) {
            addressFrom[0] = new InternetAddress(getOrganizer());
        } else {
            addressFrom[0] = new InternetAddress(meeting.getEmail());
        }

        mimeMessage.addFrom(addressFrom);
        final InternetAddress[] addressTo = new InternetAddress[to.size()];
        for (int i = 0; i < to.size(); i++) {
            addressTo[i] = new InternetAddress(to.get(i));
        }
        if (useBCC) {
            mimeMessage.addRecipients(Message.RecipientType.BCC, addressTo);
        } else {
            mimeMessage.addRecipients(Message.RecipientType.TO, addressTo);
        }
        final Multipart multipart = new MimeMultipart();
        final MimeBodyPart iCalAttachment = new MimeBodyPart();
        final String meetingID = meeting.getId();
        final String subject = meeting.getTitle();
        String content = meeting.getTitle();
        if (meeting.getInvitationMessage() != null
                && !meeting.getInvitationMessage().equalsIgnoreCase("")) {
            content = meeting.getInvitationMessage();
        }

        final TimeZoneRegistry registry = new TimeZoneRegistryImpl("/zoneinfo-outlook/");
        final TimeZone timezone = registry.getTimeZone("GMT");
        final VTimeZone tz = timezone.getVTimeZone();
        DateTime start = null;
        DateTime end = null;
        DateTime recurrenceId = null;

        String rrule = null;

        if (mode == null) {
            start =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getStartTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
            end =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getEndTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
            rrule = meeting.getRRule();
        } else if (mode == UpdateMode.AllOccurences) {
            start =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getStartTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
            end =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getEndTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
            rrule = meeting.getRRule();
        } else if (mode == UpdateMode.Single) {
            final boolean isDateChanged = !oldMeeting.getDate().equals(meeting.getDate());
            final boolean isTimeChanged =
                    !oldMeeting.getStartTime().equals(meeting.getStartTime())
                            || !oldMeeting.getEndTime().equals(meeting.getEndTime());
            if (isDateChanged || isTimeChanged) {
                recurrenceId =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                        oldMeeting.getStartTime(), oldMeeting.getDate(), oldMeeting.getTimeZoneId()),
                                timezone);
                start =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                        meeting.getStartTime(), meeting.getDate(), meeting.getTimeZoneId()),
                                timezone);
                end =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                        meeting.getEndTime(), meeting.getDate(), meeting.getTimeZoneId()),
                                timezone);
            } else {
                recurrenceId =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                        oldMeeting.getStartTime(), oldMeeting.getDate(), oldMeeting.getTimeZoneId()),
                                timezone);
                start =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                        meeting.getStartTime(), meeting.getDate(), meeting.getTimeZoneId()),
                                timezone);
                end =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                        meeting.getEndTime(), meeting.getDate(), meeting.getTimeZoneId()),
                                timezone);
            }
        } else if (mode == UpdateMode.FuturOccurences) {
            recurrenceId =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    oldMeeting.getStartTime(), oldMeeting.getDate(), oldMeeting.getTimeZoneId()),
                            timezone);
            start =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getStartTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
            end =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getEndTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
        }

        final String location = meeting.getLocation();
        final String url = meeting.getUrl();
        final String invite =
                createICalInvitation(
                        meetingID,
                        subject,
                        content,
                        start,
                        end,
                        tz,
                        meeting,
                        to,
                        rrule,
                        location,
                        url,
                        recurrenceId,
                        mode);
        iCalAttachment.setDataHandler(
                new DataHandler(invite, "text/calendar;method=REQUEST;charset=\"UTF-8\""));
        multipart.addBodyPart(iCalAttachment);
        mimeMessage.setContent(multipart);

        boolean done = false;

        try {
            final MimetypesFileTypeMap mimetypes =
                    (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
            mimetypes.addMimeTypes("text/calendar ics ICS");

            final MailcapCommandMap mailcap =
                    (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
            mailcap.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");

            getMailSender().send(mimeMessage);
            done = true;
        } catch (final Throwable t) {
            if (logger.isErrorEnabled())
            // don't stop the action but let admins know email is not getting sent
            {
                logger.error("Failed to send email to " + to, t);
            }
        }
        return done;
    }

    private String createICalInvitation(
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
            final UpdateMode mode)
            throws Exception {

        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

        final VEvent vEvent = new VEvent();
        vEvent.getProperties().add(new Uid(meetingID));
        vEvent.getProperties().add(timeZone.getTimeZoneId());
        if (location != null && !location.equalsIgnoreCase("")) {
            vEvent.getProperties().add(new Location(location));
        }

        if (url != null && !url.equalsIgnoreCase("")) {
            try {
                final URI uri = new URI(url);
                vEvent.getProperties().add(new Url(uri));
            } catch (final Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Invalid url:" + url, e);
                }
            }
        }
        vEvent.getProperties().add(new Summary(subject));
        vEvent.getProperties().add(new Description(content));

        if (start != null) {
            vEvent.getProperties().add(new DtStart(start));
        }
        if (end != null) {
            vEvent.getProperties().add(new DtEnd(end));
        }

        if (mode == null) {
            if (rrule != null) {
                final RRule rule = new RRule(rrule);
                vEvent.getProperties().add(rule);
            }
        } else {
            vEvent.getProperties().add(Status.VEVENT_CONFIRMED);
        }

        if (mode == UpdateMode.FuturOccurences) {
            final ParameterList pl = new ParameterList();
            pl.add(Range.THISANDFUTURE);
            vEvent.getProperties().add(new RecurrenceId(pl, recurrenceId));

        } else if (mode == UpdateMode.Single) {
            if (recurrenceId != null) {
                vEvent.getProperties().add(new RecurrenceId(recurrenceId));
            }
        }

        for (final String user : emails) {
            final Attendee attendee = new Attendee(URI.create(MAILTO + user));
            attendee.getParameters().add(Role.REQ_PARTICIPANT);
            attendee.getParameters().add(PartStat.NEEDS_ACTION);
            attendee.getParameters().add(Rsvp.TRUE);
            vEvent.getProperties().add(attendee);
        }

        String organizerEmail = meeting.getEmail();
        if (getIsListenerActive()) {
            organizerEmail = getOrganizer();
        }

        final Organizer organizer = new Organizer(URI.create(MAILTO + organizerEmail));
        vEvent.getProperties().add(organizer);
        if (meeting.getSequence() != null) {
            vEvent.getProperties().add(new Sequence(meeting.getSequence()));
        }

        vEvent
                .getProperties()
                .add(meeting.getAudienceStatus() == AudienceStatus.Closed ? Clazz.PRIVATE : Clazz.PUBLIC);

        net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar();
        cal.getProperties().add(new ProdId("-//CIRCABC//iCal4j 1.0//EN"));
        cal.getProperties().add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        cal.getProperties().add(net.fortuna.ical4j.model.property.Method.REQUEST);

        cal.getComponents().add(timeZone);
        cal.getComponents().add(vEvent);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(cal, bout);
        return bout.toString("UTF-8");
    }

    public boolean cancelMeeting(
            final String from,
            final List<String> to,
            final String replyTo,
            final Meeting meeting,
            final java.util.Date eventDate,
            final UpdateMode mode)
            throws Exception {

        final MimeMessage mimeMessage = getMailSender().createMimeMessage();

        mimeMessage.setSubject(meeting.getTitle());
        final InternetAddress[] addressFrom = new InternetAddress[1];
        if (getIsListenerActive()) {
            addressFrom[0] = new InternetAddress(getOrganizer());
        } else {
            addressFrom[0] = new InternetAddress(meeting.getEmail());
        }

        mimeMessage.addFrom(addressFrom);
        final InternetAddress[] addressTo = new InternetAddress[to.size()];
        for (int i = 0; i < to.size(); i++) {
            addressTo[i] = new InternetAddress(to.get(i));
        }
        mimeMessage.addRecipients(Message.RecipientType.TO, addressTo);
        final Multipart multipart = new MimeMultipart();
        final MimeBodyPart iCalAttachment = new MimeBodyPart();
        final String meetingID = meeting.getId();
        final String subject = meeting.getTitle();
        String content = meeting.getTitle();
        if (meeting.getInvitationMessage() != null
                && !meeting.getInvitationMessage().equalsIgnoreCase("")) {
            content = meeting.getInvitationMessage();
        }

        final TimeZoneRegistry registry = new TimeZoneRegistryImpl("/zoneinfo-outlook/");
        final TimeZone timezone = registry.getTimeZone("GMT");
        final VTimeZone tz = timezone.getVTimeZone();

        DateTime start = null;
        DateTime end = null;
        DateTime icalEventDate = null;
        if (mode == UpdateMode.AllOccurences) {
            start =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getStartTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);
            end =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateValueTimezoneToGMTString(
                                    meeting.getEndTime(), meeting.getStartDate(), meeting.getTimeZoneId()),
                            timezone);

            if (eventDate != null) {
                icalEventDate =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
                                        meeting.getStartTime(), eventDate, meeting.getTimeZoneId()));
            }
        } else {
            start =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
                                    meeting.getStartTime(), eventDate, meeting.getTimeZoneId()),
                            timezone);
            end =
                    new DateTime(
                            AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
                                    meeting.getEndTime(), eventDate, meeting.getTimeZoneId()),
                            timezone);
            if (eventDate != null) {
                icalEventDate =
                        new DateTime(
                                AppointmentUtils.convertLocalTimeDateTimezoneToGMTString(
                                        meeting.getStartTime(), eventDate, meeting.getTimeZoneId()),
                                timezone);
            }
        }

        final String location = meeting.getLocation();

        final String invite =
                createICalCancelation(
                        meetingID, subject, content, start, end, icalEventDate, tz, meeting, to, mode);
        iCalAttachment.setDataHandler(
                new DataHandler(invite, "text/calendar;method=CANCEL;charset=\"UTF-8\""));
        multipart.addBodyPart(iCalAttachment);

        mimeMessage.setContent(multipart);

        boolean done = false;

        try {

            final MimetypesFileTypeMap mimetypes =
                    (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
            mimetypes.addMimeTypes("text/calendar ics ICS");

            final MailcapCommandMap mailcap =
                    (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
            mailcap.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");

            getMailSender().send(mimeMessage);
            done = true;
        } catch (final Throwable t) {
            if (logger.isErrorEnabled())
            // don't stop the action but let admins know email is not getting sent
            {
                logger.error("Failed to send email to " + to, t);
            }
        }
        return done;
    }

    private String createICalCancelation(
            final String meetingID,
            final String subject,
            final String content,
            final Date start,
            final DateTime end,
            final Date eventDate,
            final VTimeZone timeZone,
            final Meeting meeting,
            final List<String> emails,
            final UpdateMode mode)
            throws Exception {

        CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_OUTLOOK_COMPATIBILITY, true);

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
        if (getIsListenerActive()) {
            organizerEmail = getOrganizer();
        }

        final Organizer organizer = new Organizer(URI.create(MAILTO + organizerEmail));

        vEvent.getProperties().add(organizer);
        vEvent.getProperties().add(new Sequence(meeting.getSequence()));

        net.fortuna.ical4j.model.Calendar cal = new net.fortuna.ical4j.model.Calendar();
        cal.getProperties().add(new ProdId("-//CIRCABC//iCal4j 1.0//EN"));
        cal.getProperties().add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        cal.getProperties().add(CalScale.GREGORIAN);
        cal.getProperties().add(net.fortuna.ical4j.model.property.Method.CANCEL);

        cal.getComponents().add(timeZone);
        cal.getComponents().add(vEvent);

        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final CalendarOutputter outputter = new CalendarOutputter();
        // outputter.setValidating(false);
        outputter.output(cal, bout);
        if (logger.isInfoEnabled()) {
            logger.info(bout.toString("UTF-8"));
        }
        return bout.toString("UTF-8");
    }

    /**
     * @return the organizer
     */
    public String getOrganizer() {
        if (organizer == null) {
            organizer = CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_ADDRESS);
        }
        return organizer;
    }

    /**
     * @return the isListenerActive
     */
    public Boolean getIsListenerActive() {
        if (isListenerActive == null) {
            isListenerActive =
                    CircabcConfiguration.getProperty(CircabcConfiguration.IS_EMAIL_LISTENER_ACTIVE);
        }
        return Boolean.valueOf(isListenerActive);
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

    public String getHeaderLogoCid() {
        return headerLogoCid;
    }

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

    public String getHeaderBackgroundCid() {
        return headerBackgroundCid;
    }

    public void setHeaderBackgroundCid(String headerBackgroundCid) {
        this.headerBackgroundCid = headerBackgroundCid;
    }

    public void setManagementService(final ManagementService managementService) {
        this.managementService = managementService;
    }

    public void setMailPreferencesService(final MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    public void setContentService(final ContentService contentService) {
        this.contentService = contentService;
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
    public void setRedirectEmailAddressEnabled(boolean redirectEmailAddressEnabled) {
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

    @Override
    public boolean sendWithAttachment(
            String from,
            String to,
            String replyTo,
            String subject,
            String body,
            boolean html,
            boolean useBCC,
            List<File> attachements)
            throws MessagingException {

        if (to == null || to.length() < 1) {
            throw new MessagingException(
                    "At least one destination email address must be specified  " + to);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Send email to: " + to + "\n...with subject:\n" + subject + "\n...with body:\n" + body);
        }

        return sendImplInternal(
                null, from, to, null, replyTo, subject, body, true, null, useBCC, attachements);
    }
}
