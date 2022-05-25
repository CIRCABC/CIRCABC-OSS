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
package eu.cec.digit.circabc.service.event;

import com.sun.mail.pop3.POP3Message;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.user.UserService;
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

import javax.mail.*;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * @author Slobodan Filipovic
 */
public class MeetingResponseListener implements Job {

    private static final String MEETING_RESPONSE_LISTENER = "MeetingResponseListener";

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

    private static boolean isInitialized = false;

    private static ServiceRegistry serviceRegistry;

    private static CircabcServiceRegistry circabcServiceRegistry;

    private static Log logger = LogFactory.getLog(MeetingResponseListener.class);

    private static EventService eventService;

    private static UserService userService;

    private static LockService lockService;

    private static String emailProtocol;

    private static String emailServer;

    private static String emailBox;

    private static String emailUsername;

    private static String emailPassword;

    private static Boolean isActive;

    private static Integer emailServerPort;

    private static Boolean isEmailUseTls;

    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            boolean isLocked = false;
            AuthenticationUtil.setRunAsUser(ADMIN_USER);
            initialize(context);
            if (!lockService.isLocked(MEETING_RESPONSE_LISTENER)) {
                try {
                    lockService.lock(MEETING_RESPONSE_LISTENER);
                    isLocked = true;
                    checkEmail();
                } finally {
                    if (isLocked) {
                        lockService.unlock(MEETING_RESPONSE_LISTENER);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Can not run job MeetingResponseListener", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("finished...");
        }
    }

    private void initialize(final JobExecutionContext context) {
        if (!isInitialized) {

            AuthenticationUtil.setRunAsUser(ADMIN_USER);
            final JobDataMap jobData = context.getJobDetail().getJobDataMap();

            final Object serviceRegistryObj = jobData.get("serviceRegistry");
            serviceRegistry = (ServiceRegistry) serviceRegistryObj;
            final Object circabcServiceRegistryObj = jobData.get("circabcServiceRegistry");
            circabcServiceRegistry = (CircabcServiceRegistry) circabcServiceRegistryObj;
            eventService = circabcServiceRegistry.getNonSecureEventService();
            userService = circabcServiceRegistry.getUserService();
            lockService = circabcServiceRegistry.getLockService();

            emailProtocol = CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_PROTOCOL);
            emailServer = CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_SERVER);
            emailBox = CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_BOX);
            emailUsername = CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_USERNAME);
            emailPassword = CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_PASSWORD);
            emailServerPort =
                    Integer.valueOf(CircabcConfiguration.getProperty(CircabcConfiguration.EMAIL_SERVER_PORT));
            isActive =
                    Boolean.valueOf(
                            CircabcConfiguration.getProperty(CircabcConfiguration.IS_EMAIL_LISTENER_ACTIVE));
            isEmailUseTls =
                    Boolean.valueOf(CircabcConfiguration.getProperty(CircabcConfiguration.IS_EMAIL_USE_TLS));

            isInitialized = true;
        }
    }

    private void checkEmail() {
    	//DIGITCIRCABC-4756 use a proper Properties object instead of setting the properties to System
    	Properties props = new Properties();
    	
        if (isEmailUseTls) {
            props.setProperty("mail.pop3.ssl.enable", "false");
            props.setProperty("mail.pop3.starttls.enable", "true");
            props.setProperty("mail.pop3.starttls.required", "true");
            
            //AMO - add debugging params
//        	props.setProperty("java.security.debug","certpath");
//        	props.setProperty("javax.net.debug","trustmanager");
//        	props.setProperty("mail.debug.auth", "true");
        }

        if (!isActive) {
            return;
        }

        final Session session = Session.getDefaultInstance(props, null);

        if (logger.isInfoEnabled()) {
            try {
                final Provider provider = session.getProvider("pop3");
                logger.info("Using pop3 provider " + provider.toString());
            } catch (NoSuchProviderException e) {
                logger.info("Provider for pop3 does not exists", e);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("set mail session to debug mode");
            session.setDebug(true);
        }

        Store store = null;
        Folder folder = null;

        try {
            final URLName url =
                    new URLName(
                            emailProtocol, emailServer, emailServerPort, emailBox, emailUsername, emailPassword);
            store = session.getStore(url);
            try {
                store.connect();
            } catch (final MessagingException me) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not connect Email Server:" + me.getMessage());
                }
            }

            if (store.isConnected()) {
                folder = store.getFolder(emailBox);
                if (folder == null) {
                    throw new MessagingException("Invalid folder : " + emailBox);
                }

                folder.open(Folder.READ_WRITE);

                for (final Message message : folder.getMessages()) {
                    ContentType ct = new ContentType(message.getContentType());
                    if (ct.getPrimaryType().equals("multipart")
                            || (ct.getPrimaryType().equals("text") && ct.getSubType().equals("calendar"))) {
                        if (message instanceof POP3Message) {
                            processMessage((POP3Message) message);
                        }
                    }
                }
            }

        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when checking email ", e);
            }

        } finally {
            try {
                if (folder != null) {
                    folder.close(true);
                }
                if (store != null) {
                    store.close();
                }
            } catch (final Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void processMessage(final POP3Message message) throws Exception {
        NodeRef meetingNodeRef = null;
        MeetingRequestStatus meetingRequestStatus = null;
        UpdateMode mode = UpdateMode.AllOccurences;

        boolean isResponse = false;
        final InternetAddress address = (InternetAddress) message.getFrom()[0];
        final String from = address.getAddress();
        if (logger.isDebugEnabled()) {
            logger.debug("from : " + from);
        }

        String userName = userService.getUserNameByEmail(from);
        if (userName == null) {
            userName = from;
        }

        final Object messageContent = message.getContent();

        int count = 0;
        String body = null;
        MimeMultipart content = null;
        if (messageContent instanceof MimeMultipart) {
            content = (MimeMultipart) messageContent;
            try {
                count = content.getCount();
            } catch (MessagingException e) {
                logger.error("error when try to get count of message", e);
            }
        } else if (messageContent instanceof String) {
            body = getBodyFromString(message, messageContent);
            count = 1;
        }

        final TransactionService transactionService = serviceRegistry.getTransactionService();

        for (int i = 0; i < count; i++) {

            meetingNodeRef = null;
            meetingRequestStatus = null;
            mode = UpdateMode.AllOccurences;

            if (body == null) {
                final MimeBodyPart part = (MimeBodyPart) content.getBodyPart(i);
                final InputStream inputStream = part.getInputStream();
                final int available = inputStream.available();
                final byte[] b = new byte[available];
                inputStream.read(b);
                body = new String(b);
            }
            isResponse =
                    body.contains(BEGIN_VCALENDAR)
                            && body.contains(BEGIN_VEVENT)
                            && body.contains(METHOD_REPLY);

            if (isResponse) {
                meetingRequestStatus = getMeetingRequestStatus(meetingRequestStatus, body);
                meetingNodeRef = getMeetingNodeRef(body);

                final int startPositionRecurrence = body.indexOf(RECURRENCE_ID);
                // RECURRENCE-ID;TZID="GMT +0100 (Standard) / GMT +0200 (Daylight)":20090422T143000
                String recurrenceLine = "";
                if (startPositionRecurrence > -1) {
                    mode = UpdateMode.Single;

                    recurrenceLine = getRecurrenceLine(body, startPositionRecurrence);
                    meetingNodeRef =
                            getMeetingNodeRefByRecurrenceLine(meetingNodeRef, transactionService, recurrenceLine);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Found event reply" + body);
                }
                break;
            }
            body = null;
        }

        if (isResponse) {
            updateMeetingRequestStatus(
                    meetingNodeRef, meetingRequestStatus, mode, userName, transactionService);
        }

        message.setFlag(Flags.Flag.DELETED, true);
    }

    private NodeRef getMeetingNodeRef(String body) {
        NodeRef meetingNodeRef;
        final int startPositionUid = body.indexOf(UID);
        final int endPositionUid = body.indexOf(CRLF, startPositionUid);
        final String uid = body.substring(startPositionUid + 4, endPositionUid);
        meetingNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, uid);
        return meetingNodeRef;
    }

    private MeetingRequestStatus getMeetingRequestStatus(
            MeetingRequestStatus meetingRequestStatus, String body) {
        final boolean isAccepted = body.contains(PARTSTAT_ACCEPTED);
        final boolean isDeclined = body.contains(PARTSTAT_DECLINED);
        final boolean isTentative = body.contains(PARTSTAT_TENTATIVE);
        if (isAccepted) {
            meetingRequestStatus = MeetingRequestStatus.Accepted;
        }
        if (isDeclined) {
            meetingRequestStatus = MeetingRequestStatus.Rejected;
        }
        if (isTentative) {
            meetingRequestStatus = MeetingRequestStatus.Pending;
        }
        return meetingRequestStatus;
    }

    private NodeRef getMeetingNodeRefByRecurrenceLine(
            NodeRef meetingNodeRef, final TransactionService transactionService, String recurrenceLine)
            throws SystemException {
        UserTransaction tx = null;
        try {
            tx = transactionService.getUserTransaction();
            tx.begin();
            meetingNodeRef = eventService.getMeetingNodeRef(meetingNodeRef, recurrenceLine);
            tx.commit();
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Did not get recurence node :" + recurrenceLine, e);
            }
            if (tx != null) {
                tx.rollback();
            }
        }
        return meetingNodeRef;
    }

    private String getRecurrenceLine(String body, final int startPositionRecurrence) {
        String recurrenceLine;
        final int endPositionRecurrence = body.indexOf(CRLF, startPositionRecurrence);
        recurrenceLine = body.substring(startPositionRecurrence, endPositionRecurrence);
        return recurrenceLine;
    }

    private void updateMeetingRequestStatus(
            NodeRef meetingNodeRef,
            MeetingRequestStatus meetingRequestStatus,
            UpdateMode mode,
            String userName,
            final TransactionService transactionService)
            throws SystemException {
        UserTransaction tx = null;
        try {
            tx = transactionService.getUserTransaction();
            tx.begin();
            eventService.setMeetingRequestStatus(meetingNodeRef, userName, meetingRequestStatus, mode);
            tx.commit();
        } catch (final Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when update meeting ", e);
            }
            if (tx != null) {
                tx.rollback();
            }
        }
    }

    private String getBodyFromString(final POP3Message message, final Object messageContent)
            throws IOException, MessagingException, NoSuchFieldException, IllegalAccessException {
        String body;
        body = (String) messageContent;
        if (body.isEmpty()) {
            // workaround to make it work with james and thunderbird
            InputStream is = message.getInputStream();
            if (is instanceof ByteArrayInputStream) {
                ByteArrayInputStream bais = (ByteArrayInputStream) is;
                Field f = ByteArrayInputStream.class.getDeclaredField("buf");
                f.setAccessible(true);
                byte[] buf = (byte[]) f.get(bais);
                body = new String(buf);
            }
        }
        return body;
    }
}
