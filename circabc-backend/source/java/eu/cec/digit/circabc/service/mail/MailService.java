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
package eu.cec.digit.circabc.service.mail;

import eu.cec.digit.circabc.service.event.Meeting;
import eu.cec.digit.circabc.service.event.UpdateMode;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Interface for Mail Service. Each mail should be sent via this service. An email is always sent in
 * a non-blocking process. It means that if any error appears, it is will be never throwed.
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface MailService {

    /**
     * @return the no-reply address defined for circabc.
     */
    @NotAuditable
    String getNoReplyEmailAddress();

    /**
     * @return the support team address defined for circabc.
     */
    @NotAuditable
    String getSupportEmailAddress();

    /**
     * @return the developer team email address defined for circabc
     */
    @NotAuditable
    String getDevTeamEmailAddress();

    /**
     * @return the developer team email address defined for circabc
     */
    @NotAuditable
    String getHelpdeskAddress();

    /**
     * Sent an email as text or html email to a person.
     *
     * @param from    The email that send the email
     * @param to      The email that receive the email
     * @param subject The subject of the email
     * @param body    The body of the email
     * @param html    True is the email is in HTML format
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {"from", "to", "replyTo", "subject", "body"})
    boolean send(
            String from,
            String to,
            String replyTo,
            String subject,
            String body,
            boolean html,
            boolean useBCC)
            throws MessagingException;

    /**
     * Sent an email as text or html email to each specified person
     *
     * @param from    The email that send the email
     * @param to      The emails that receive the email
     * @param subject The subject of the email
     * @param body    The body of the email
     * @param html    True is the email is in HTML format
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {"from", "to", "replyTo", "subject", "body"})
    boolean send(
            String from,
            List<String> to,
            String replyTo,
            String subject,
            String body,
            boolean html,
            boolean useBCC)
            throws MessagingException;

    /**
     * Sent an email as text or html email to a person.
     *
     * @param from        The email that send the email
     * @param to          The email that receive the email
     * @param subject     The subject of the email
     * @param body        The body of the email
     * @param html        True is the email is in HTML format
     * @param attachments List of content to add
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {"from", "to", "replyTo", "subject", "body"})
    boolean send(
            String from,
            List<String> to,
            String replyTo,
            String subject,
            String body,
            List<NodeRef> attachments)
            throws MessagingException;

    /**
     * Sent an email as text or html email to a person.
     *
     * @param from        The email that send the email
     * @param to          The email that receive the email
     * @param subject     The subject of the email
     * @param body        The body of the email
     * @param html        True is the email is in HTML format
     * @param attachments List of content to add
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {"from", "to", "replyTo", "subject", "body"})
    boolean send(
            String from,
            String to,
            String replyTo,
            String subject,
            String body,
            List<NodeRef> attachments)
            throws MessagingException;

    /**
     * Sent an email as text or html email to a person with the given node attached
     *
     * @param from    The person that send the email
     * @param to      The person that receive the email
     * @param subject The subject of the email
     * @param body    The body of the email
     * @param html    True is the email is in HTML format
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {"from", "to", "replyTo", "subject", "body"})
    boolean sendNode(
            NodeRef content,
            String from,
            String to,
            String replyTo,
            String subject,
            String body,
            boolean html)
            throws MessagingException;

    /**
     * Sent an email as text or html email to each specified person with the given node attached
     *
     * @param from    The person that send the email
     * @param to      The persons that receive the email
     * @param subject The subject of the email
     * @param body    The body of the email
     * @param html    True is the email is in HTML format
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.ARG_0, */ parameters = {
            "content", "from", "to", "replyTo", "subject", "body"
    })
    boolean sendNode(
            NodeRef content,
            String from,
            List<String> to,
            String replyTo,
            String subject,
            String body,
            boolean html)
            throws MessagingException;

    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {
            "subject",
            "from",
            "to",
            "replyTo",
            "meeting",
            "oldMeeting",
            "mode"
    })
    boolean sendMeetingRequest(
            String from,
            List<String> to,
            String replyTo,
            Meeting meeting,
            Meeting oldMeeting,
            UpdateMode mode,
            boolean useBCC)
            throws Exception;

    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {
            "subject",
            "from",
            "to",
            "replyTo",
            "meeting",
            "eventDate",
            "mode"
    })
    boolean cancelMeeting(
            String from,
            List<String> to,
            String replyTo,
            Meeting meeting,
            Date eventDate,
            UpdateMode mode)
            throws Exception;

    /**
     * Sent an email as text or html email to a person.
     *
     * @param from       The email that send the email
     * @param to         The email that receive the email
     * @param subject    The subject of the email
     * @param body       The body of the email
     * @param attachment List of content to add
     * @return If whethever the mail is sent or not.
     */
    @Auditable(
            /* key = Auditable.Key.NO_KEY, */ parameters = {"from", "to", "replyTo", "subject", "body"})
    boolean sendWithAttachment(
            String from,
            String to,
            String replyTo,
            String subject,
            String body,
            boolean html,
            boolean useBCC,
            List<File> attachements)
            throws MessagingException;
}
