/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.log4j.net;

import org.alfresco.repo.cache.SimpleCache;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.apache.log4j.xml.UnrecognizedElementHandler;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.Properties;

/**
 * Send an e-mail when a specific logging event occurs, typically on errors or fatal errors. If a
 * mail is sended more than a <b>FloodingLimit</b>, the mail is not send.
 *
 * <p>
 * The number of logging events delivered in this e-mail depend on the value of
 * <b>BufferSize</b> option. The <code>SMTPAppender</code> keeps only the
 * last <code>BufferSize</code> logging events in its cyclic buffer. This keeps memory requirements
 * at a reasonable level while still delivering useful application context.
 * <p>
 * By default, an email message will be sent when an ERROR or higher severity message is appended.
 * The triggering criteria can be modified by setting the evaluatorClass property with the name of a
 * class implementing TriggeringEventEvaluator, setting the evaluator property with an instance of
 * TriggeringEventEvaluator or nesting a triggeringPolicy element where the specified class
 * implements TriggeringEventEvaluator.
 *
 * @author Clinckart Stephane;
 * @since 1.0
 */
public class AntiFloodingSMTPAppender extends AppenderSkeleton implements
        UnrecognizedElementHandler {

    private static final String TRIGGERING_POLICY = "triggeringPolicy";
    protected Message msg;
    protected TriggeringEventEvaluator evaluator;
    private String to;
    /**
     * Comma separated list of cc recipients.
     */
    private String cc;
    /**
     * Comma separated list of bcc recipients.
     */
    private String bcc;
    private String from;
    private String subject;
    private String smtpHost;
    private String smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private boolean smtpDebug = false;
    private int bufferSize = 512;
    protected CyclicBuffer cb = new CyclicBuffer(bufferSize);
    private int floodingLimit = 10;
    private SimpleCache<String, Integer> floodingCache;
    private boolean locationInfo = false;

    /**
     * The default constructor will instantiate the appender with a {@link TriggeringEventEvaluator}
     * that will trigger on events with level ERROR or higher.
     */
    public AntiFloodingSMTPAppender() {
        this(new DefaultEvaluator());
    }

    /**
     * Use <code>evaluator</code> passed as parameter as the {@link TriggeringEventEvaluator} for this
     * SMTPAppender.
     */
    public AntiFloodingSMTPAppender(final TriggeringEventEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Activate the specified options, such as the smtp host, the recipient, from, etc.
     */
    public void activateOptions() {
        final Session session = createSession();
        msg = new MimeMessage(session);

        try {
            addressMessage(msg);
            if (subject != null) {
                msg.setSubject(subject);
            }
        } catch (final MessagingException e) {
            LogLog.error("Could not activate SMTPAppender options.", e);
        }

        if (evaluator instanceof OptionHandler) {
            ((OptionHandler) evaluator).activateOptions();
        }
    }

    /**
     * Address message.
     *
     * @param msg message, may not be null.
     * @throws MessagingException thrown if error addressing message.
     */
    protected void addressMessage(final Message msg) throws MessagingException {
        if (from != null) {
            msg.setFrom(getAddress(from));
        } else {
            msg.setFrom();
        }

        if (to != null && to.length() > 0) {
            msg.setRecipients(Message.RecipientType.TO, parseAddress(to));
        }

        // Add CC receipients if defined.
        if (cc != null && cc.length() > 0) {
            msg.setRecipients(Message.RecipientType.CC, parseAddress(cc));
        }

        // Add BCC receipients if defined.
        if (bcc != null && bcc.length() > 0) {
            msg.setRecipients(Message.RecipientType.BCC, parseAddress(bcc));
        }
    }

    /**
     * Create mail session.
     *
     * @return mail session, may not be null.
     */
    protected Session createSession() {
        Properties props = null;
        try {
            props = new Properties(System.getProperties());
        } catch (final SecurityException ex) {
            props = new Properties();
        }
        if (smtpHost != null) {
            props.put("mail.smtp.host", smtpHost);
        }

        if (smtpPort != null) {
            props.put("mail.smtp.port", smtpPort);
        }

        Authenticator auth = null;
        if (smtpPassword != null && smtpUsername != null) {
            props.put("mail.smtp.auth", "true");
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            };
        }
        final Session session = Session.getInstance(props, auth);
        if (smtpDebug) {
            session.setDebug(smtpDebug);
        }
        return session;
    }

    /**
     * Perform SMTPAppender specific appending actions, mainly adding the event to a cyclic buffer and
     * checking if the event triggers an e-mail to be sent.
     */
    public void append(final LoggingEvent event) {

        if (!checkEntryConditions()) {
            return;
        }
        if (!checkAntiFloodingConditions(event)) {
            return;
        }

        event.getThreadName();
        event.getNDC();
        event.getMDCCopy();
        if (locationInfo) {
            event.getLocationInformation();
        }
        cb.add(event);
        if (evaluator.isTriggeringEvent(event)) {
            sendBuffer();
        }
    }

    /**
     * This method determines if there is a sense in attempting to append.
     *
     * <p>
     * It checks whether there is a set output target and also if there is a set layout. If these
     * checks fail, then the boolean value <code>false</code> is returned.
     */
    protected boolean checkEntryConditions() {
        if (this.msg == null) {
            errorHandler.error("Message object not configured.");
            return false;
        }

        if (this.evaluator == null) {
            errorHandler.error("No TriggeringEventEvaluator is set for appender [" + name + "].");
            return false;
        }

        if (this.layout == null) {
            errorHandler.error("No layout set for appender named [" + name + "].");
            return false;
        }
        return true;
    }

    /**
     * This method determines if there is a sense in attempting to append.
     *
     * <p>
     * It checks whether the current event is not flooding the system. If these checks fail, then the
     * boolean value <code>false</code> is returned.
     */
    protected boolean checkAntiFloodingConditions(final LoggingEvent event) {
        if (event == null || floodingCache == null) {
            return false;
        }
        Integer element = floodingCache.get(event.getRenderedMessage());
        if (element == null) {
            floodingCache.put(event.getRenderedMessage(), element);
        } else {
            Integer count = element;
            count = count + 1;
            floodingCache.put(event.getRenderedMessage(), element);
            if (count > this.floodingLimit) {
                return false;
            }
        }
        return true;
    }

    synchronized public void close() {
        this.closed = true;
    }

    InternetAddress getAddress(final String addressStr) {
        try {
            return new InternetAddress(addressStr);
        } catch (final AddressException e) {
            errorHandler.error("Could not parse address [" + addressStr + "].", e,
                    ErrorCode.ADDRESS_PARSE_FAILURE);
            return null;
        }
    }

    InternetAddress[] parseAddress(final String addressStr) {
        try {
            return InternetAddress.parse(addressStr, true);
        } catch (final AddressException e) {
            errorHandler.error("Could not parse address [" + addressStr + "].", e,
                    ErrorCode.ADDRESS_PARSE_FAILURE);
            return null;
        }
    }

    /**
     * Returns value of the <b>To</b> option.
     */
    public String getTo() {
        return to;
    }

    /**
     * The <b>To</b> option takes a string value which should be a comma separated list of e-mail
     * address of the recipients.
     */
    public void setTo(final String to) {
        this.to = to;
    }

    /**
     * The <code>SMTPAppender</code> requires a {@link org.apache.log4j.Layout layout}.
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Send the contents of the cyclic buffer as an e-mail message.
     */
    protected void sendBuffer() {

        // Note: this code already owns the monitor for this
        // appender. This frees us from needing to synchronize on 'cb'.
        try {
            final MimeBodyPart part = new MimeBodyPart();

            final StringBuilder sbuf = new StringBuilder();
            String t = layout.getHeader();
            if (t != null) {
                sbuf.append(t);
            }
            int len = cb.length();
            for (int i = 0; i < len; i++) {
                // sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
                final LoggingEvent event = cb.get();
                sbuf.append(layout.format(event));
                if (layout.ignoresThrowable()) {
                    final String[] s = event.getThrowableStrRep();
                    if (s != null) {
                        for (String value : s) {
                            sbuf.append(value);
                            sbuf.append(Layout.LINE_SEP);
                        }
                    }
                }
            }
            t = layout.getFooter();
            if (t != null) {
                sbuf.append(t);
            }
            part.setContent(sbuf.toString(), layout.getContentType());

            final Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            msg.setContent(mp);

            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (final SendFailedException sfe) {
            final Throwable nextException = sfe.getNextException();
            if (nextException instanceof javax.mail.MessagingException) {
                if (nextException.getMessage().startsWith("Could not connect to SMTP host: ")) {
                    LogLog.warn("Warn - the email server is not started");
                    return;
                }
            }
            LogLog.error("Error occured while sending e-mail notification.", sfe);
        } catch (final Exception e) {
            LogLog.error("Error occured while sending e-mail notification.", e);
        }
    }

    /**
     * Returns value of the <b>EvaluatorClass</b> option.
     */
    public String getEvaluatorClass() {
        return evaluator == null ? null : evaluator.getClass().getName();
    }

    /**
     * The <b>EvaluatorClass</b> option takes a string value representing the name of the class
     * implementing the {@link TriggeringEventEvaluator} interface. A corresponding object will be
     * instantiated and assigned as the triggering event evaluator for the SMTPAppender.
     */
    public void setEvaluatorClass(final String value) {
        evaluator = (TriggeringEventEvaluator) OptionConverter
                .instantiateByClassName(value, TriggeringEventEvaluator.class,
                        evaluator);
    }

    /**
     * Returns value of the <b>From</b> option.
     */
    public String getFrom() {
        return from;
    }

    /**
     * The <b>From</b> option takes a string value which should be a e-mail address of the sender.
     */
    public void setFrom(final String from) {
        this.from = from;
    }

    /**
     * Returns value of the <b>Subject</b> option.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * The <b>Subject</b> option takes a string value which should be a the subject of the e-mail
     * message.
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * The <b>BufferSize</b> option takes a positive integer representing the maximum number of
     * logging events to collect in a cyclic buffer. When the
     * <code>BufferSize</code> is reached, oldest events are deleted as new
     * events are added to the buffer. By default the size of the cyclic buffer is 512 events.
     */
    public void setFloodingLimit(final int floodingLimit) {
        this.floodingLimit = floodingLimit;
    }

    public void setFloodingCache(SimpleCache<String, Integer> floodingCache) {
        this.floodingCache = floodingCache;
    }

    /**
     * Returns value of the <b>SMTPHost</b> option.
     */
    public String getSMTPHost() {
        return smtpHost;
    }

    /**
     * The <b>SMTPHost</b> option takes a string value which should be a the host name of the SMTP
     * server that will send the e-mail message.
     */
    public void setSMTPHost(final String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public final String getSMTPPort() {
        return smtpPort;
    }

    public final void setSMTPPort(final String smtpPort) {
        this.smtpPort = smtpPort;
    }

    /**
     * Returns value of the <b>BufferSize</b> option.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * The <b>BufferSize</b> option takes a positive integer representing the maximum number of
     * logging events to collect in a cyclic buffer. When the
     * <code>BufferSize</code> is reached, oldest events are deleted as new
     * events are added to the buffer. By default the size of the cyclic buffer is 512 events.
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
        cb.resize(bufferSize);
    }

    /**
     * Returns value of the <b>LocationInfo</b> option.
     */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * The <b>LocationInfo</b> option takes a boolean value. By default, it is set to false which
     * means there will be no effort to extract the location information related to the event. As a
     * result, the layout that formats the events as they are sent out in an e-mail is likely to place
     * the wrong location information (if present in the format).
     *
     * <p>
     * Location information extraction is comparatively very slow and should be avoided unless
     * performance is not a concern.
     */
    public void setLocationInfo(final boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    /**
     * Get the cc recipient addresses.
     *
     * @return recipient addresses as comma separated string, may be null.
     */
    public String getCc() {
        return cc;
    }

    /**
     * Set the cc recipient addresses.
     *
     * @param addresses recipient addresses as comma separated string, may be null.
     */
    public void setCc(final String addresses) {
        this.cc = addresses;
    }

    /**
     * Get the bcc recipient addresses.
     *
     * @return recipient addresses as comma separated string, may be null.
     */
    public String getBcc() {
        return bcc;
    }

    /**
     * Set the bcc recipient addresses.
     *
     * @param addresses recipient addresses as comma separated string, may be null.
     */
    public void setBcc(final String addresses) {
        this.bcc = addresses;
    }

    /**
     * Get SMTP password.
     *
     * @return SMTP password, may be null.
     */
    public String getSMTPPassword() {
        return smtpPassword;
    }

    /**
     * The <b>SmtpPassword</b> option takes a string value which should be the password required to
     * authenticate against the mail server.
     *
     * @param password password, may be null.
     */
    public void setSMTPPassword(final String password) {
        this.smtpPassword = password;
    }

    /**
     * Get SMTP user name.
     *
     * @return SMTP user name, may be null.
     */
    public String getSMTPUsername() {
        return smtpUsername;
    }

    /**
     * The <b>SmtpUsername</b> option takes a string value which should be the username required to
     * authenticate against the mail server.
     *
     * @param username user name, may be null.
     */
    public void setSMTPUsername(final String username) {
        this.smtpUsername = username;
    }

    /**
     * Get SMTP debug.
     *
     * @return SMTP debug flag.
     */
    public boolean getSMTPDebug() {
        return smtpDebug;
    }

    /**
     * Setting the <b>SmtpDebug</b> option to true will cause the mail session to log its server
     * interaction to stdout. This can be useful when debuging the appender but should not be used
     * during production because username and password information is included in the output.
     *
     * @param debug debug flag.
     */
    public void setSMTPDebug(final boolean debug) {
        this.smtpDebug = debug;
    }

    /**
     * Get triggering evaluator.
     *
     * @return triggering event evaluator.
     * @since 1.2.15
     */
    public final TriggeringEventEvaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Sets triggering evaluator.
     *
     * @param trigger triggering event evaluator.
     * @since 1.2.15
     */
    public final void setEvaluator(final TriggeringEventEvaluator trigger) {
        if (trigger == null) {
            throw new NullPointerException("trigger");
        }
        this.evaluator = trigger;
    }

    /**
     * {@inheritDoc}
     */
    public boolean parseUnrecognizedElement(final org.w3c.dom.Element element, final Properties props)
            throws Exception {
        if (TRIGGERING_POLICY.equals(element.getNodeName())) {
            final Object triggerPolicy = org.apache.log4j.xml.DOMConfigurator.parseElement(element, props,
                    TriggeringEventEvaluator.class);
            if (triggerPolicy instanceof TriggeringEventEvaluator) {
                setEvaluator((TriggeringEventEvaluator) triggerPolicy);
            }
            return true;
        }

        return false;
    }
}

class DefaultEvaluator implements TriggeringEventEvaluator {

    /**
     * Is this <code>event</code> the e-mail triggering event?
     *
     * <p>
     * This method returns <code>true</code>, if the event level has ERROR level or higher. Otherwise
     * it returns <code>false</code>.
     */
    public boolean isTriggeringEvent(final LoggingEvent event) {
        return event.getLevel().isGreaterOrEqual(Level.ERROR);
    }
}
