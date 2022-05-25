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
package eu.cec.digit.circabc.repo.event;

import com.google.ical.values.DateValue;
import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.service.customisation.mail.MailPreferencesService;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.customisation.mail.MailWrapper;
import eu.cec.digit.circabc.service.event.*;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.mail.MailService;
import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.PathUtils;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.ISO9075;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.*;

import static eu.cec.digit.circabc.model.CircabcModel.ASPECT_EVENT_ROOT;
import static eu.cec.digit.circabc.model.EventModel.*;

/**
 * @author Slobodan Filipovic
 */
public class EventServiceImpl implements EventService {

    protected static final String GIF = ".gif";
    protected static final String ICON = "icon";
    protected static final String IMAGES_ICONS = "/images/icons/";
    private static final String APPOINTMENT_DEFINITION_IS_NULL = "appointmentDefinition is null";
    private static final String EVENT_NODE_REF_WRONG_TYPE_OR_NULL = "eventNodeRef wrong type or null";
    private static final String SEPARATOR = "|";
    private static final String MAX = "MAX";
    private static final String MIN = "MIN";
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(EventServiceImpl.class);
    private static final String DATE_OF_EVENT = "dateOfEvent";
    private static final String ESCAPE4 = "\\\\:";
    private static final String ESCAPE3 = "\\:";
    private static final String ESCAPE1 = "\\-";
    private static final String ESCAPE2 = "\\\\-";
    private static final String PROP_EVENT_DATE_OF_EVENT_ESCAPED =
            EventModel.PROP_EVENT_DATE
                    .toString()
                    .replaceAll(":", ESCAPE4)
                    .replaceAll("\\{", "\\\\{")
                    .replaceAll("\\}", "\\\\}");
    private static final String PROP_EVENT_USER_LIST_ESCAPED =
            EventModel.PROP_EVENT_INVITED_USERS
                    .toString()
                    .replaceAll(":", ESCAPE4)
                    .replaceAll("\\{", "\\\\{")
                    .replaceAll("\\}", "\\\\}");
    /**
     * The node service reference
     */
    private NodeService nodeService;
    /**
     * The permission service reference
     */
    private PermissionService permissionService;
    /**
     * The namespace service reference
     */
    private NamespaceService namespaceService;
    /**
     * The search service reference
     */
    private SearchService searchService;
    /**
     * The user service reference
     */
    private UserService userService;
    /**
     * The authority service reference
     */
    private AuthorityService authorityService;
    /**
     * The email service reference
     */
    private MailService mailService;
    /**
     * The person service reference
     */
    private PersonService personService;
    /**
     * The mailPreferencesService service reference used to generate mail body
     */
    private MailPreferencesService mailPreferencesService;
    /**
     * The management service
     */
    private ManagementService managementService;
    private LogService logService;

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the permissionService
     */
    protected final PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public final void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the namespaceService
     */
    protected final NamespaceService getNamespaceService() {
        return namespaceService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @return the search service
     */
    protected final SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public final void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @return the nodeService
     */
    protected final UserService getUserService() {
        return userService;
    }

    /**
     * @param userService the userService to set
     */
    public final void setUserService(final UserService userService) {
        this.userService = userService;
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

    public final NodeRef createEvent(NodeRef eventRoot, Event event) {
        return createEvent(eventRoot, event, true);
    }

    private void LogNotification(NodeRef nodeRef, String to, boolean ok, boolean AdminLog) {
        LogRecord logRecord = new LogRecord();
        logRecord.setActivity("Send Notification");
        logRecord.setService("Event");
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
        final String title = (String) getNodeService().getProperty(nodeRef, ContentModel.PROP_TITLE);

        if (title == null || title.trim().length() < 1) {
            return name;
        } else {
            return title;
        }
    }

    public final NodeRef createEvent(NodeRef eventRoot, Event event, boolean enableMailSending) {
        validateEvent(eventRoot, event);

        PropertyMap properties = event.getProperties();

        // Create the event
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        eventRoot, ASSOC_EVENT, TYPE_EVENT_DEFINITION, TYPE_EVENT_DEFINITION, properties);

        NodeRef eventNodeRef = assocRef.getChildRef();

        event.setId(eventNodeRef.getId());

        // Get the eventsDatesContainer
        NodeRef eventsDatesContainer = getEventsDatesContainer(eventNodeRef);
        // if container does not exists create it
        if (eventsDatesContainer == null) {
            eventsDatesContainer = createEventDatesContainer(eventNodeRef);
        }

        List<PropertyMap> eventDatesProperties = event.getEventDatesProperties(AppointmentType.Event);
        boolean isFirstEvent = true;
        NodeRef firstEvent = null;

        for (PropertyMap map : eventDatesProperties) {
            // Create the property
            ChildAssociationRef car =
                    nodeService.createNode(
                            eventsDatesContainer, ASSOC_EVENT_DATES, TYPE_EVENT, TYPE_EVENT, map);
            if (isFirstEvent) {
                isFirstEvent = false;
                firstEvent = car.getChildRef();
            }
        }

        if (enableMailSending) {
            sendEventNotificationMessage(
                    eventRoot, eventNodeRef, firstEvent, event, MailTemplate.EVENT_REMINDER);
            sendEventMeesage(
                    eventRoot, eventNodeRef, firstEvent, event, MailTemplate.EVENT_CREATE_NOTIFICATION);
        }
        return eventNodeRef;
    }

    private List<String> getUsersEmails(NodeRef eventRoot) {
        final List<String> users = new ArrayList<>();

        users.addAll(
                getUserService().getUsersWithPermission(eventRoot, EventPermissions.EVEACCESS.toString()));
        users.addAll(
                getUserService().getUsersWithPermission(eventRoot, EventPermissions.EVEADMIN.toString()));

        return users;
    }

    private void validateEvent(NodeRef eventRoot, Event event) {
        ParameterCheck.mandatory("The evenRoot node reference is mandatory param", eventRoot);
        ParameterCheck.mandatory("The event is mandatory param ", event);

        ParameterCheck.mandatory("The eventRoot node reference is mandatory param", eventRoot);
        ParameterCheck.mandatory("The meeting is mandatory param ", event);

        ParameterCheck.mandatory("Event Language is mandatory", event.getLanguage());
        ParameterCheck.mandatory("Event Title is mandatory", event.getTitle());
        ParameterCheck.mandatory("Event Type is mandatory", event.getEventType());

        ParameterCheck.mandatory("Event Date is mandatory", event.getStartDate());
        ParameterCheck.mandatory("Event occurs is mandatory", event.getOccurenceRate());
        ParameterCheck.mandatory("Event start time is mandatory", event.getStartTime());
        ParameterCheck.mandatory("Event end time is mandatory", event.getEndTime());
        ParameterCheck.mandatory("Event timezone is mandatory", event.getTimeZoneId());

        ParameterCheck.mandatory("Event audience status is mandatory", event.getAudienceStatus());

        ParameterCheck.mandatory("Event name is mandatory", event.getName());
        ParameterCheck.mandatory("Event phone is mandatory", event.getPhone());
        ParameterCheck.mandatory("Event email is mandatory", event.getEmail());

        if (event.getAudienceStatus() == AudienceStatus.Open && !event.getInvitedUsers().isEmpty()) {
            throw new IllegalStateException("Open event  should not have invated users.");
        }

        if (!nodeService.hasAspect(eventRoot, ASPECT_EVENT_ROOT)) {
            throw new IllegalArgumentException(
                    "The event root noderef must have the aspect applied " + ASPECT_EVENT_ROOT);
        }
    }

    private void sendEventNotificationMessage(
            NodeRef eventRoot,
            NodeRef eventDefinition,
            NodeRef eventItem,
            Event event,
            MailTemplate mailTemplate) {
        Map<String, Object> model =
                buildDefaultTemplateModel(eventRoot, event, eventDefinition, eventItem);

        MailWrapper bodyReminder =
                getMailPreferencesService().getDefaultMailTemplate(eventRoot, mailTemplate);

        String from = mailService.getNoReplyEmailAddress();
        String to = event.getEmail();

        model.put("eventRef", eventItem);

        boolean html = true;
        boolean result = false;
        try {
            result =
                    mailService.send(
                            from,
                            to,
                            event.getEmail(),
                            bodyReminder.getSubject(model),
                            bodyReminder.getBody(model),
                            html,
                            event.getUseBCC());
        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }
        LogNotification(eventItem, to, result, false);
        LogNotification(eventItem, to, result, true);
    }

    private Map<String, Object> buildDefaultTemplateModel(
            NodeRef eventService,
            Appointment appointment,
            NodeRef appointmentDefinition,
            NodeRef appointmentItem) {
        final Map<String, Object> model =
                getMailPreferencesService().buildDefaultModel(eventService, null, null);

        if (model.get(MailTemplate.KEY_ME) == null) {
            model.put(MailTemplate.KEY_ME, getCurrentPerson());
        }

        model.put(MailTemplate.KEY_EVENT_SERVICE, eventService);
        model.put(MailTemplate.KEY_APPOINTMENT, appointment);
        // I need here the Root appointment definition ID !!! //
        model.put(MailTemplate.KEY_APPOINTMENT_ID, appointmentDefinition.getId());

        if (appointmentItem != null) {
            model.put(MailTemplate.KEY_APPOINTMENT_FIRST_OCCURENCE, appointmentItem);
        }
        return model;
    }

    private NodeRef getCurrentPerson() {
        final String currentUsername = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef currentUserRef = null;

        if (currentUsername != null && getPersonService().personExists(currentUsername)) {
            currentUserRef = getPersonService().getPerson(currentUsername);
        }

        return currentUserRef;
    }

    private void sendEventMeesage(
            NodeRef eventRoot,
            NodeRef eventDefinition,
            NodeRef eventItem,
            Event event,
            MailTemplate mailTemplate) {
        Map<String, Object> model =
                buildDefaultTemplateModel(eventRoot, event, eventDefinition, eventItem);
        MailWrapper bodyReminder =
                getMailPreferencesService().getDefaultMailTemplate(eventRoot, mailTemplate);

        List<String> emails = getAppointmentEmails(eventRoot, event);

        if (emails.isEmpty()) {
            return;
        }

        String from = mailService.getNoReplyEmailAddress();

        model.put("eventRef", eventItem);

        boolean html = true;
        boolean result = false;
        try {
            result =
                    mailService.send(
                            from,
                            emails,
                            event.getEmail(),
                            bodyReminder.getSubject(model),
                            bodyReminder.getBody(model),
                            html,
                            event.getUseBCC());
        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }
        String to = StringUtils.join(emails.toArray(), ", ");
        LogNotification(eventItem, to, result, false);
        LogNotification(eventItem, to, result, true);
    }

    private List<String> getAppointmentEmails(NodeRef eventRoot, Appointment appointment) {
        List<String> emails = new ArrayList<>();
        List<String> users = new ArrayList<>();

        if (appointment.getAudienceStatus() == AudienceStatus.Closed) {
            users = appointment.getInvitedUsers();
        } else if (appointment.getAudienceStatus() == AudienceStatus.Open
                && appointment.getEnableNotification()) {
            users = getUsersEmails(eventRoot);
        }

        for (String user : users) {
            if (user.contains("@")) {
                emails.add(user);
            } else {
                String email = userService.getUserEmail(user);
                if (!(email.length() == 0)) {
                    emails.add(email);
                }
            }
        }

        return emails;
    }

    private void sendMeetingNotificationMeesage(
            NodeRef eventRoot,
            NodeRef meetingDefinition,
            NodeRef meetingItem,
            Meeting meeting,
            MailTemplate mailTemplate) {
        Map<String, Object> model =
                buildDefaultTemplateModel(eventRoot, meeting, meetingDefinition, meetingItem);
        MailWrapper bodyReminder =
                getMailPreferencesService().getDefaultMailTemplate(eventRoot, mailTemplate);

        String from = mailService.getNoReplyEmailAddress();
        String to = meeting.getEmail();

        model.put("eventRef", meetingItem);

        boolean html = true;
        boolean result = false;
        try {
            result =
                    mailService.send(
                            from,
                            to,
                            null,
                            bodyReminder.getSubject(model),
                            bodyReminder.getBody(model),
                            html,
                            meeting.getUseBCC());
        } catch (MessagingException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when sending meeting notification: ", e);
            }
        }
        LogNotification(meetingItem, to, result, false);
        LogNotification(meetingItem, to, result, true);
    }

    private void sendMeetingRequest(
            NodeRef eventRoot,
            Meeting meeting,
            Meeting oldMeeting,
            UpdateMode mode,
            NodeRef nodeRef,
            AppointmentUpdateInfo updateInfo,
            boolean useBCC) {
        List<String> emails;

        if (updateInfo == null || updateInfo != AppointmentUpdateInfo.Audience) {
            emails = getAppointmentEmails(eventRoot, meeting);
        } else {
            emails = getAppointmentEmails(eventRoot, meeting, oldMeeting);
        }

        if (emails.isEmpty()) {
            return;
        }
        String from = mailService.getNoReplyEmailAddress();
        boolean result = false;
        try {
            result =
                    mailService.sendMeetingRequest(from, emails, null, meeting, oldMeeting, mode, useBCC);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when sending meeting request: ", e);
            }
        }
        String to = StringUtils.join(emails.toArray(), ", ");
        LogNotification(nodeRef, to, result, false);
        LogNotification(nodeRef, to, result, true);
    }

    private List<String> getAppointmentEmails(
            NodeRef eventRoot, Meeting meeting, Meeting oldMeeting) {
        List<String> emails = getAppointmentEmails(eventRoot, meeting);
        List<String> oldEmails = getAppointmentEmails(eventRoot, oldMeeting);
        emails.removeAll(oldEmails);
        return emails;
    }

    public final NodeRef createMeeting(NodeRef eventRoot, Meeting meeting) {
        return createMeeting(eventRoot, meeting, true);
    }

    public NodeRef createMeeting(NodeRef eventRoot, Meeting meeting, boolean enableMailSending) {

        validateMeeting(eventRoot, meeting);

        meeting.setSequence(0);

        PropertyMap properties = meeting.getProperties();

        // Create the property
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        eventRoot,
                        ASSOC_EVENT,
                        TYPE_EVENT_MEETING_DEFINITION,
                        TYPE_EVENT_MEETING_DEFINITION,
                        properties);

        NodeRef meetingNodeRef = assocRef.getChildRef();
        meeting.setId(meetingNodeRef.getId());

        // Get the eventsDatesContainer
        NodeRef eventsDatesContainer = getEventsDatesContainer(meetingNodeRef);
        // if container does not exists create it
        if (eventsDatesContainer == null) {
            eventsDatesContainer = createEventDatesContainer(meetingNodeRef);
        }

        List<PropertyMap> eventDatesProperties =
                meeting.getEventDatesProperties(AppointmentType.Meeting);

        boolean isFirstMeeting = true;
        NodeRef firstMeeting = null;

        for (PropertyMap map : eventDatesProperties) {
            // Create the property
            ChildAssociationRef car =
                    nodeService.createNode(
                            eventsDatesContainer, ASSOC_EVENT_DATES, TYPE_EVENT, TYPE_EVENT, map);
            if (isFirstMeeting) {
                isFirstMeeting = false;
                firstMeeting = car.getChildRef();
            }
        }

        if (enableMailSending) {
            sendMeetingNotificationMeesage(
                    eventRoot, meetingNodeRef, firstMeeting, meeting, MailTemplate.MEETING_REMINDER);
            sendMeetingRequest(eventRoot, meeting, null, null, meetingNodeRef, null, false);
        }
        return meetingNodeRef;
    }

    private void validateMeeting(NodeRef eventRoot, Meeting meeting) {
        ParameterCheck.mandatory("The eventRoot node reference is mandatory param", eventRoot);
        ParameterCheck.mandatory("The meeting is mandatory param ", meeting);

        ParameterCheck.mandatory("Meeting Language is mandatory", meeting.getLanguage());
        ParameterCheck.mandatory("Meeting Title is mandatory", meeting.getTitle());
        ParameterCheck.mandatory("Meeting Type is mandatory", meeting.getMeetingTypeString());
        ParameterCheck.mandatory("Meeting Date is mandatory", meeting.getStartDate());

        ParameterCheck.mandatory("Meeting occurs is mandatory", meeting.getOccurenceRate());

        ParameterCheck.mandatory("Meeting start time is mandatory", meeting.getStartTime());
        ParameterCheck.mandatory("Meeting end time is mandatory", meeting.getEndTime());
        ParameterCheck.mandatory("Meeting timezone is mandatory", meeting.getTimeZoneId());

        ParameterCheck.mandatory("Meeting availability is mandatory", meeting.getAvailability());
        ParameterCheck.mandatory("Meeting audience status is mandatory", meeting.getAudienceStatus());

        ParameterCheck.mandatory("Meeting name is mandatory", meeting.getName());
        ParameterCheck.mandatory("Meeting phone is mandatory", meeting.getPhone());
        ParameterCheck.mandatory("Meeting email is mandatory", meeting.getEmail());

        if (meeting.getAudienceStatus() == AudienceStatus.Open
                && !meeting.getInvitedUsers().isEmpty()) {
            throw new IllegalStateException("Open meeting  should not have invated users.");
        }

        if (!nodeService.hasAspect(eventRoot, ASPECT_EVENT_ROOT)) {
            throw new IllegalArgumentException(
                    "The event root noderef must have the aspect applied " + ASPECT_EVENT_ROOT);
        }
    }

    public final void deleteAppointment(NodeRef appointmentNodeRef, UpdateMode mode) {
        final Appointment appointment = getAppointmentByNodeRef(appointmentNodeRef);
        final NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(appointmentNodeRef);
        final NodeRef eventRoot = getEventRootFromAppointmentDefinition(appointmentDefinition);
        Date eventDate = null;
        List<NodeRef> futureEvents = null;

        if (mode == UpdateMode.Single) {
            eventDate =
                    (Date) getNodeService().getProperty(appointmentNodeRef, EventModel.PROP_EVENT_DATE);
        }
        if (mode == UpdateMode.FuturOccurences) {
            futureEvents = getFutureAppointmens(appointmentNodeRef);
            if (futureEvents.size() == 0) {
                return;
            }
            boolean isFirst = true;
            for (NodeRef ref : futureEvents) {
                if (isFirst) {
                    eventDate = (Date) getNodeService().getProperty(ref, EventModel.PROP_EVENT_DATE);
                    break;
                }
            }
        }

        if (appointment instanceof Meeting) {
            Integer sequence = incrementSequence(appointmentNodeRef);
            Meeting meeting = (Meeting) appointment;
            meeting.setId(appointmentDefinition.getId());
            meeting.setSequence(sequence);
            sendMeetingNotificationMeesage(
                    eventRoot,
                    appointmentDefinition,
                    appointmentNodeRef,
                    meeting,
                    MailTemplate.MEETING_DELETE_REMINDER);
            sendCancelMeetingMessage(eventRoot, meeting, eventDate, mode);
        } else if (appointment instanceof Event) {
            Event event = (Event) appointment;

            sendEventNotificationMessage(
                    eventRoot,
                    appointmentDefinition,
                    appointmentNodeRef,
                    event,
                    MailTemplate.EVENT_DELETE_REMINDER);
            sendEventMeesage(
                    eventRoot,
                    appointmentDefinition,
                    appointmentNodeRef,
                    event,
                    MailTemplate.EVENT_DELETE_NOTIFICATION);
        }

        switch (mode) {
            case AllOccurences:
                deleteAllAppointments(appointmentNodeRef);
                break;
            case FuturOccurences:
                for (NodeRef ref : futureEvents) {
                    deleteSingleAppointment(ref);
                }
                break;
            case Single:
                eventDate =
                        (Date) getNodeService().getProperty(appointmentNodeRef, EventModel.PROP_EVENT_DATE);
                if (appointment.getOccurenceRate().getMainOccurence() == MainOccurence.OnlyOnce) {
                    deleteAllAppointments(appointmentNodeRef);
                } else {
                    deleteSingleAppointment(appointmentNodeRef);
                }
                break;
            default:
                break;
        }
    }

    public final List<EventItem> getAllOccurences(final NodeRef appointmentNodeRef) {
        if (appointmentNodeRef == null) {
            throw new IllegalArgumentException("appointmentNodeRef is null");
        }

        final NodeRef dateContainer;

        if (isSingleEvent(appointmentNodeRef)) {
            dateContainer = nodeService.getPrimaryParent(appointmentNodeRef).getParentRef();
        } else if (isMeetingDefinition(appointmentNodeRef) || isEventDefinition(appointmentNodeRef)) {
            dateContainer = getEventsDatesContainer(appointmentNodeRef);
        } else {
            throw new IllegalArgumentException("Bad node type.");
        }

        final List<ChildAssociationRef> singleEventAssoc = nodeService.getChildAssocs(dateContainer);
        final List<EventItem> allOccurences = new ArrayList<>(singleEventAssoc.size());

        final NodeRef interestGroup = managementService.getCurrentInterestGroup(appointmentNodeRef);
        final String interestGroupName =
                (String) nodeService.getProperty(interestGroup, ContentModel.PROP_NAME);
        final String interesGroupTitle =
                (String) nodeService.getProperty(interestGroup, ContentModel.PROP_TITLE);

        for (final ChildAssociationRef child : singleEventAssoc) {
            allOccurences.add(buildEventItem(interestGroupName, interesGroupTitle, child.getChildRef()));
        }

        return allOccurences;
    }

    private void sendCancelMeetingMessage(
            NodeRef eventRoot, Meeting meeting, Date eventDate, UpdateMode mode) {

        List<String> to = getAppointmentEmails(eventRoot, meeting);

        if (to.isEmpty()) {
            return;
        }

        String from = mailService.getNoReplyEmailAddress();

        try {
            mailService.cancelMeeting(from, to, null, meeting, eventDate, mode);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
        }
    }

    private List<NodeRef> getAllAppointmens(NodeRef appointment) {
        List<NodeRef> result = new ArrayList<>();
        if (appointment == null || !isSingleEvent(appointment)) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }
        NodeRef appointmentDateContainer =
                getNodeService().getPrimaryParent(appointment).getParentRef();
        String luceneQuery = null;

        boolean directStoreAccess =
                Boolean.parseBoolean(CircabcConfiguration.getProperty("events.direct.store.access"));

        if (!directStoreAccess) {
            luceneQuery = buildLuceneQuery(null, appointmentDateContainer, null, null);
        }

        ResultSet resultSet = null;
        try {
            if (!directStoreAccess) {
                resultSet = executeLuceneQuery(luceneQuery);
            } else {
                resultSet = getEventsDirect(null, appointmentDateContainer, null, null);
            }

            for (final ResultSetRow row : resultSet) {
                result.add(row.getNodeRef());
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return result;
    }

    private List<NodeRef> getFutureAppointmens(NodeRef appointment) {
        List<NodeRef> result = new ArrayList<>();
        if (appointment == null || !isSingleEvent(appointment)) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }
        NodeRef appointmentDateContainer =
                getNodeService().getPrimaryParent(appointment).getParentRef();
        String luceneQuery = null;

        boolean directStoreAccess =
                Boolean.parseBoolean(CircabcConfiguration.getProperty("events.direct.store.access"));

        if (!directStoreAccess) {
            luceneQuery = buildLuceneQuery(null, appointmentDateContainer, new Date(), null);
        }

        ResultSet resultSet = null;
        try {
            if (!directStoreAccess) {
                resultSet = executeLuceneQuery(luceneQuery);
            } else {
                resultSet = getEventsDirect(null, appointmentDateContainer, new Date(), null);
            }

            for (ResultSetRow row : resultSet) {
                result.add(row.getNodeRef());
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return result;
    }

    public final void setMeetingRequestStatus(
            NodeRef meetingNodeRef,
            String userName,
            MeetingRequestStatus meetingRequestStatus,
            UpdateMode mode) {

        if (isMeetingDefinition(meetingNodeRef)) {
            if (meetingRequestStatus == MeetingRequestStatus.Accepted) {
                acceptMeetingRequest(meetingNodeRef, userName);
            } else if (meetingRequestStatus == MeetingRequestStatus.Rejected) {
                rejectMeetingRequest(meetingNodeRef, userName);
            }
            return;
        }
        switch (mode) {
            case AllOccurences:
                NodeRef meetingDefinition = getAppointmentDefinitionNodeFromEvent(meetingNodeRef);
                if (meetingRequestStatus == MeetingRequestStatus.Accepted) {
                    acceptMeetingRequest(meetingDefinition, userName);
                } else if (meetingRequestStatus == MeetingRequestStatus.Rejected) {
                    rejectMeetingRequest(meetingDefinition, userName);
                }
                break;
            case FuturOccurences:
                List<NodeRef> futureEvents = getFutureAppointmens(meetingNodeRef);
                for (NodeRef ref : futureEvents) {
                    if (meetingRequestStatus == MeetingRequestStatus.Accepted) {
                        acceptMeetingRequest(ref, userName);
                    } else if (meetingRequestStatus == MeetingRequestStatus.Rejected) {
                        rejectMeetingRequest(ref, userName);
                    }
                }
                break;
            case Single:
                if (meetingRequestStatus == MeetingRequestStatus.Accepted) {
                    acceptMeetingRequest(meetingNodeRef, userName);
                } else if (meetingRequestStatus == MeetingRequestStatus.Rejected) {
                    rejectMeetingRequest(meetingNodeRef, userName);
                }
                break;
            default:
                break;
        }
    }

    private NodeRef createEventDatesContainer(NodeRef nodeRef) {

        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        nodeRef,
                        ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER,
                        TYPE_EVENT_DATES_CONTAINER,
                        TYPE_EVENT_DATES_CONTAINER,
                        new PropertyMap());

        final NodeRef eventContainerNodeRef = assocRef.getChildRef();

        return eventContainerNodeRef;
    }

    private NodeRef getEventsDatesContainer(NodeRef nodeRef) {
        if (nodeRef == null) {
            throw new IllegalArgumentException("nodeRef can not be null");
        }
        QName type = nodeService.getType(nodeRef);
        if (!(TYPE_EVENT_DEFINITION.isMatch(type) || TYPE_EVENT_MEETING_DEFINITION.isMatch(type))) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }

        NodeRef eventdatesContainer = null;

        final List<ChildAssociationRef> childAssocRefs =
                nodeService.getChildAssocs(
                        nodeRef, ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER, RegexQNamePattern.MATCH_ALL);

        if (childAssocRefs.size() == 0) {

            eventdatesContainer = null;
        } else if (childAssocRefs.size() == 1) {
            final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
            eventdatesContainer = toKeepAssocRef.getChildRef();

        } else if (childAssocRefs.size() > 1) {
            // This is a problem - destroy all but the first
            if (logger.isWarnEnabled()) {
                logger.warn("Too many event containers: " + nodeRef);
            }
            throw new RuntimeException("Too many event containers");
        }
        return eventdatesContainer;
    }

    private void deleteSingleAppointment(NodeRef eventNodeRef) {
        if (eventNodeRef == null || !isSingleEvent(eventNodeRef)) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }
        getNodeService().deleteNode(eventNodeRef);
    }

    private boolean isEventDefinition(NodeRef nodeRef) {
        return TYPE_EVENT_DEFINITION.isMatch(nodeService.getType(nodeRef));
    }

    private void deleteAllAppointments(NodeRef appointment) {

        if (appointment == null || !isSingleEvent(appointment)) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }

        NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(appointment);
        if (appointmentDefinition == null) {
            throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
        }

        boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
        boolean isEventDefinition = isEventDefinition(appointmentDefinition);
        if ((!isMeetingDefinition && !isEventDefinition)) {
            throw new IllegalArgumentException("appointmentDefinition wrong type ");
        }
        getNodeService().deleteNode(appointmentDefinition);
    }

    private boolean isSingleEvent(NodeRef nodeRef) {
        return TYPE_EVENT.isMatch(nodeService.getType(nodeRef));
    }

    private boolean isMeetingDefinition(NodeRef nodeRef) {
        return TYPE_EVENT_MEETING_DEFINITION.isMatch(nodeService.getType(nodeRef));
    }

    private NodeRef getAppointmentDefinitionNodeFromEvent(NodeRef appointment) {
        ChildAssociationRef parent = nodeService.getPrimaryParent(appointment);
        ChildAssociationRef parentOfParent = nodeService.getPrimaryParent(parent.getParentRef());
        return parentOfParent.getParentRef();
    }

    private NodeRef getEventRootFromAppointmentDefinition(NodeRef appointmentDefinition) {
        ChildAssociationRef parent = nodeService.getPrimaryParent(appointmentDefinition);
        return parent.getParentRef();
    }

    public final List<EventItem> getEventsByMonth(NodeRef eventRoot, int month, int year) {
        DateTime firstDateInMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
        DateTime lastDateInMonth = firstDateInMonth.dayOfMonth().withMaximumValue();
        return getEvents(null, eventRoot, firstDateInMonth.toDate(), lastDateInMonth.toDate());
    }

    private List<EventItem> getEvents(
            String userName, NodeRef eventRoot, Date dateFrom, Date dateTo) {
        String luceneQuery = null;
        // get all events for interest group between two dates in calendar

        boolean directStoreAccess =
                Boolean.parseBoolean(CircabcConfiguration.getProperty("events.direct.store.access"));

        if (!directStoreAccess) {
            if (isCurrentUserEventAdmin(eventRoot)) {
                luceneQuery = buildLuceneQuery(null, eventRoot, dateFrom, dateTo);
            } else {
                luceneQuery = buildLuceneQuery(userName, eventRoot, dateFrom, dateTo);
            }
        }

        final NodeRef ig = nodeService.getPrimaryParent(eventRoot).getParentRef();
        final String interesGroupName = (String) nodeService.getProperty(ig, ContentModel.PROP_NAME);
        final String interesGroupTitle = (String) nodeService.getProperty(ig, ContentModel.PROP_TITLE);

        if (logger.isDebugEnabled()) {
            logger.debug("trying to execute lucene query: ");
            logger.debug(luceneQuery);
        }
        ResultSet resultSet = null;
        final List<EventItem> resultList = new ArrayList<>();
        try {
            if (!directStoreAccess) {
                resultSet = executeLuceneQuery(luceneQuery);
            } else {
                if (isCurrentUserEventAdmin(eventRoot)) {
                    resultSet = getEventsDirect(null, eventRoot, dateFrom, dateTo);
                } else {
                    resultSet = getEventsDirect(userName, eventRoot, dateFrom, dateTo);
                }
            }

            for (final ResultSetRow row : resultSet) {
                NodeRef nodeRef = row.getNodeRef();
                if (nodeService.exists(nodeRef)) {
                    EventItem eventItem = buildEventItem(interesGroupName, interesGroupTitle, nodeRef);
                    MeetingRequestStatus meetingStatus = getMeetingStatus(nodeRef, userName);
                    String status = "";
                    switch (meetingStatus) {
                        case Accepted:
                            status = "Accepted";
                            break;
                        case Rejected:
                            status = "Rejected";
                            break;
                        default:
                            break;
                    }
                    eventItem.setMeetingStatus(status);
                    resultList.add(eventItem);
                }
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return resultList;
    }

    /**
     * @param interesGroupName
     * @param nodeRef
     * @return
     */
    public EventItem buildEventItem(
            final String interestGroupName, final String interestGroupTitle, NodeRef nodeRef) {
        Date d = (Date) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_DATE);
        EventItem eventItem = new EventItem();
        eventItem.setEventNodeRef(nodeRef);
        eventItem.setContact((String) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_NAME));
        eventItem.setDate(d);
        eventItem.setInterestGroup(interestGroupName);
        eventItem.setInterestGroupTitle(interestGroupTitle);
        String eventType = (String) nodeService.getProperty(nodeRef, EventModel.PROP_KIND_OF_EVENT);
        eventItem.setEventType(AppointmentType.valueOf(eventType));
        eventItem.setTitle((String) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_TITLE));
        eventItem.setStartTime(
                new LocalTime(
                        (String) (nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_START_TIME))));
        eventItem.setEndTime(
                new LocalTime((String) (nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_END_TIME))));
        String theAbstract = (String) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_ABSTRACT);
        String location = (String) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_LOCATION);

        // get info from the (parent, parent) to complete the EventItem
        NodeRef eventDefinitionNodeRef =
                nodeService
                        .getPrimaryParent(nodeService.getPrimaryParent(nodeRef).getParentRef())
                        .getParentRef();
        eventItem.setOccurrenceRate(
                (String)
                        nodeService.getProperty(eventDefinitionNodeRef, EventModel.PROP_EVENT_OCCURENCE_RATE));
        String appointmentContainerId = eventDefinitionNodeRef.getId();
        eventItem.setAppointmentContainerId(
                appointmentContainerId == null ? "" : appointmentContainerId);
        if (theAbstract == null || theAbstract.trim().length() == 0) {
            theAbstract =
                    (String) nodeService.getProperty(eventDefinitionNodeRef, EventModel.PROP_EVENT_ABSTRACT);
        }
        if (location == null || location.trim().length() == 0) {
            location =
                    (String) nodeService.getProperty(eventDefinitionNodeRef, EventModel.PROP_EVENT_LOCATION);
        }
        eventItem.setDescription(theAbstract == null ? "" : theAbstract);
        eventItem.setLocation(location == null ? "" : location);

        return eventItem;
    }

    private ResultSet executeLuceneQuery(final String query) {
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        sp.addSort(DATE_OF_EVENT, true);
        return searchService.query(sp);
    }

    private String buildLuceneQuery(
            final String userName, final NodeRef searchNodeRef, Date dateFrom, Date dateTo) {

        final StringBuilder query = new StringBuilder();
        String strDateFrom = null;
        if (dateFrom == null) {
            strDateFrom = MIN;
        } else {
            strDateFrom = escape(CachingDateFormat.getDateFormat().format(dateFrom));
        }

        String strDateTo = null;
        if (dateTo == null) {
            strDateTo = MAX;
        } else {
            strDateTo = escape(CachingDateFormat.getDateFormat().format(dateTo));
        }

        query
                .append("( PATH:\"")
                .append(getPathFromSpaceRef(searchNodeRef, true))
                .append("\" ) ")
                .append("AND")
                .append(" ( TYPE: \"ce:event\"  ) ")
                .append("AND")
                .append(" ( @")
                .append(PROP_EVENT_DATE_OF_EVENT_ESCAPED)
                .append(":[")
                .append(strDateFrom)
                .append(" TO ")
                .append(strDateTo)
                .append("] )");
        if (userName != null) {
            query
                    .append(" AND ")
                    .append("( @")
                    .append(PROP_EVENT_USER_LIST_ESCAPED)
                    .append(":*|")
                    .append(userName)
                    .append("|*")
                    .append(" OR  @")
                    .append(PROP_EVENT_USER_LIST_ESCAPED)
                    .append(":\"||\" )");
        }

        return query.toString();
    }

    /**
     * Retrieves the events directly from the store instead of using search.
     */
    private ResultSet getEventsDirect(
            final String userName, final NodeRef searchNodeRef, Date dateFrom, Date dateTo) {

        List<EventResultSetRow> events = new ArrayList<EventResultSetRow>();

        collectEvents(searchNodeRef, events, userName, dateFrom, dateTo);

        return new EventResultSet(events);
    }

    /**
     * Recursive function that collects all events according to their dates and invited users. It is
     * used by getEventsDirect to traverse the events tree of the IG/Events node and retrieve all
     * events without using search.
     */
    private void collectEvents(
            NodeRef nodeRef,
            List<EventResultSetRow> events,
            String userName,
            Date dateFrom,
            Date dateTo) {

        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            return;
        }

        List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);

        for (ChildAssociationRef childRef : children) {

            final NodeRef childNodeRef = childRef.getChildRef();

            if (!nodeService.exists(childNodeRef)) {
                continue;
            }

            // Check for type ce:event
            if (nodeService.getType(childNodeRef).equals(EventModel.TYPE_EVENT)) {

                // Check for date
                Date date = (Date) nodeService.getProperty(childNodeRef, EventModel.PROP_EVENT_DATE);

                // Check: a < b <=> ~ (a >= b), to include the edges
                boolean dateAfter = dateFrom == null ? true : !date.before(dateFrom);
                boolean dateBefore = dateTo == null ? true : !date.after(dateTo);

                // Compare for equality without time part
                DateTime dateFromDT = null;
                LocalDate dateFromLT = null;
                DateTime dateDT = null;
                LocalDate dateLT = null;
                if (dateFrom != null && date != null) {
                    dateFromDT = new DateTime(dateFrom);
                    dateFromLT = dateFromDT.toLocalDate();
                    dateDT = new DateTime(date);
                    dateLT = dateDT.toLocalDate();
                }

                if (dateAfter && dateBefore
                        || (dateFrom != null
                        && dateFromLT != null
                        && dateLT != null
                        && dateFrom.equals(dateTo)
                        && dateFromLT.compareTo(dateLT) == 0)) {

                    if (userName == null) {
                        userName = AuthenticationUtil.getFullyAuthenticatedUser();
                    }

                    final String finalUserName = userName;

                    boolean shouldAddTheAppointment =
                            AuthenticationUtil.runAs(
                                    new RunAsWork<Boolean>() {

                                        public Boolean doWork() {

                                            // if the user is EveAdmin, let her see everything
                                            if (permissionService.hasPermission(
                                                    childNodeRef, EventPermissions.EVEADMIN.toString())
                                                    == AccessStatus.ALLOWED) {
                                                // add if the user is event admin
                                                return true;
                                            } else if (permissionService.hasPermission(
                                                    childNodeRef, EventPermissions.EVEACCESS.toString())
                                                    == AccessStatus.ALLOWED) {

                                                String audience =
                                                        (String)
                                                                nodeService.getProperty(
                                                                        childNodeRef, EventModel.PROP_EVENT_AUDIENCE);

                                                if ("Closed".equals(audience)) {

                                                    String invitedUsers =
                                                            (String)
                                                                    nodeService.getProperty(
                                                                            childNodeRef, EventModel.PROP_EVENT_INVITED_USERS);

                                                    // if the user has been invited to the private appointment or if the
                                                    // appointment
                                                    // is "open
                                                    // to anyone", then add it. If the appointment is closed and the user has
                                                    // not been
                                                    // invited,
                                                    // don't show the appointment for that user
                                                    if (invitedUsers.contains(finalUserName)
                                                            || (SEPARATOR + SEPARATOR).equals(invitedUsers)
                                                            || SEPARATOR.equals(invitedUsers)) {
                                                        // add if the appointment is closed, the user is invited and has access
                                                        return true;
                                                    }
                                                } else {
                                                    // add if the appointment is open and the user has access
                                                    return true;
                                                }
                                            }

                                            // don't add if the user has no access or if the appointment is closed and the
                                            // user is
                                            // not invited
                                            return false;
                                        }
                                    },
                                    userName);

                    if (!shouldAddTheAppointment) {
                        continue;
                    }

                    events.add(new EventResultSetRow(childNodeRef));
                }
            } else {
                // Recursive call for the next level
                collectEvents(childNodeRef, events, userName, dateFrom, dateTo);
            }
        }
    }

    private String getPathFromSpaceRef(NodeRef searchNodeRef, boolean children) {

        final Path path = getNodeService().getPath(searchNodeRef);
        final NamespaceService ns = getNamespaceService();
        final StringBuilder buf = new StringBuilder(64);
        String elementString;
        Path.Element element;
        ChildAssociationRef elementRef;
        Collection prefixes;
        for (int i = 0; i < path.size(); i++) {
            elementString = "";
            element = path.get(i);
            if (element instanceof Path.ChildAssocElement) {
                elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null) {
                    prefixes = ns.getPrefixes(elementRef.getQName().getNamespaceURI());
                    if (prefixes.size() > 0) {
                        elementString =
                                '/'
                                        + (String) prefixes.iterator().next()
                                        + ':'
                                        + ISO9075.encode(elementRef.getQName().getLocalName());
                    }
                }
            }

            buf.append(elementString);
        }
        if (children) {
            // append syntax to get all children of the path
            buf.append("//*");
        } else {
            // append syntax to just represent the path, not the children
            buf.append("/*");
        }

        return buf.toString();
    }

    private String escape(final String str) {
        return str.replaceAll(ESCAPE1, ESCAPE2).replaceAll(ESCAPE3, ESCAPE4);
    }

    public final void deleteAllEvents(NodeRef eventRoot) {
        List<ChildAssociationRef> cars =
                nodeService.getChildAssocs(eventRoot, ASSOC_EVENT, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef ref : cars) {
            if (isEventDefinition(ref.getChildRef()) || isMeetingDefinition(ref.getChildRef())) {
                nodeService.deleteNode(ref.getChildRef());
            }
        }
    }

    public final List<EventItem> getCurrentFutureEvents(NodeRef eventRoot) {
        return getEvents(null, eventRoot, new Date(), null);
    }

    public final List<EventItem> getCurrentPreviousEvents(NodeRef eventRoot) {
        return getEvents(null, eventRoot, null, new Date());
    }

    public List<EventItem> getCalendarEventsOnDate(NodeRef eventRoot, DateValue date) {

        Date dateFrom = AppointmentUtils.convertDateValueToDate(date);
        return getEvents(null, eventRoot, dateFrom, dateFrom);
    }

    public final Appointment getAppointmentByNodeRef(NodeRef eventItemId) {
        if (eventItemId == null || !isSingleEvent(eventItemId)) {
            throw new IllegalArgumentException("eventItemId wrong type or null");
        }

        NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(eventItemId);
        if (appointmentDefinition == null) {
            throw new IllegalArgumentException("meetingNodeRef is null");
        }

        boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
        boolean isEventDefinition = isEventDefinition(appointmentDefinition);
        if ((!isMeetingDefinition && !isEventDefinition)) {
            throw new IllegalArgumentException("meetingNodeRef wrong type ");
        }
        Map<QName, Serializable> properties = nodeService.getProperties(appointmentDefinition);
        Appointment appointment = null;

        if (isMeetingDefinition) {
            appointment = new MeetingImpl();
        } else if (isEventDefinition) {
            appointment = new EventImpl();
        } else {
            throw new IllegalArgumentException(
                    "can not find event or meeting definition for noderef: "
                            + appointmentDefinition.toString());
        }

        Map<QName, Serializable> itemProperties = nodeService.getProperties(eventItemId);
        properties.putAll(itemProperties);
        appointment.init(properties);

        if (isMeetingDefinition && appointment.getAudienceStatus() == AudienceStatus.Closed) {
            final List<String> invitedUsers =
                    getUserListFromProperty(eventItemId, PROP_EVENT_INVITED_USERS);
            final List<String> acceptedUsers =
                    getUserListFromProperty(eventItemId, PROP_MEETING_ACCEPTED_USERS);
            final List<String> rejectedUsers =
                    getUserListFromProperty(eventItemId, PROP_MEETING_REJECTED_USERS);
            for (String user : invitedUsers) {
                if (acceptedUsers.contains(user)) {
                    appointment.addAudience(user, MeetingRequestStatus.Accepted);
                } else if (rejectedUsers.contains(user)) {
                    appointment.addAudience(user, MeetingRequestStatus.Rejected);
                }
            }
        }

        return appointment;
    }

    private void acceptMeetingRequest(NodeRef meetingNodeRef, String userName) {
        final String userNameItem = SEPARATOR + userName + SEPARATOR;
        final String userNameItemToAdd = userName + SEPARATOR;
        String invitedUsers =
                (String) nodeService.getProperty(meetingNodeRef, PROP_EVENT_INVITED_USERS);
        if (invitedUsers.contains(userNameItem)) {
            acceptUser(meetingNodeRef, userNameItem, userNameItemToAdd);
        } else {
            final String newUserName = getInvitedUserWithSameEmail(userName, invitedUsers);
            if (newUserName != null) {
                final String newUserNameItem = SEPARATOR + newUserName + SEPARATOR;
                final String newUserNameItemToAdd = newUserName + SEPARATOR;
                acceptUser(meetingNodeRef, newUserNameItem, newUserNameItemToAdd);
            } else {
                throw new IllegalStateException("User " + userName + " is not invated for this meeting");
            }
        }
    }

    private String getInvitedUserWithSameEmail(String userName, String invitedUsers) {
        String result = null;
        if (!userName.contains("@")) {
            final String[] users = invitedUsers.split("\\|");
            final NodeRef personNodeRef = getUserService().getPerson(userName);
            final String email =
                    (String) getNodeService().getProperty(personNodeRef, ContentModel.PROP_EMAIL);
            for (String user : users) {
                if (user.isEmpty()) {
                    continue;
                }
                final NodeRef userNodeRef = getUserService().getPerson(user);
                final String userEmail =
                        (String) getNodeService().getProperty(userNodeRef, ContentModel.PROP_EMAIL);
                if (userEmail.equals(email)) {
                    result = user;
                    break;
                }
            }
        }
        return result;
    }

    private void acceptUser(
            NodeRef meetingNodeRef, final String userNameItem, final String userNameItemToAdd) {
        String acceptedUsers =
                (String) nodeService.getProperty(meetingNodeRef, PROP_MEETING_ACCEPTED_USERS);
        if (acceptedUsers == null || acceptedUsers.equalsIgnoreCase("")) {
            acceptedUsers = SEPARATOR;
        }
        if (!acceptedUsers.contains(userNameItem)) {
            acceptedUsers = acceptedUsers.concat(userNameItemToAdd);
            nodeService.setProperty(meetingNodeRef, PROP_MEETING_ACCEPTED_USERS, acceptedUsers);
        }
        String rejectedUsers =
                (String) nodeService.getProperty(meetingNodeRef, PROP_MEETING_REJECTED_USERS);
        if (rejectedUsers != null) {
            nodeService.setProperty(
                    meetingNodeRef,
                    PROP_MEETING_REJECTED_USERS,
                    rejectedUsers.replace(userNameItemToAdd, ""));
        }
    }

    private void rejectMeetingRequest(NodeRef meetingNodeRef, String userName) {
        final String userNameItem = SEPARATOR + userName + SEPARATOR;
        final String userNameItemToAdd = userName + SEPARATOR;
        String invitedUsers =
                (String) nodeService.getProperty(meetingNodeRef, PROP_EVENT_INVITED_USERS);
        if (invitedUsers.contains(userNameItem)) {
            rejectUser(meetingNodeRef, userNameItem, userNameItemToAdd);
        } else {
            final String newUserName = getInvitedUserWithSameEmail(userName, invitedUsers);
            if (newUserName != null) {
                final String newUserNameItem = SEPARATOR + newUserName + SEPARATOR;
                final String newUserNameItemToAdd = newUserName + SEPARATOR;
                rejectUser(meetingNodeRef, newUserNameItem, newUserNameItemToAdd);
            } else {
                throw new IllegalStateException("User " + userName + " is not invated for this meeting");
            }
        }
    }

    private void rejectUser(
            NodeRef meetingNodeRef, final String userNameItem, final String userNameItemToAdd) {
        String rejectedUsers =
                (String) nodeService.getProperty(meetingNodeRef, PROP_MEETING_REJECTED_USERS);
        if (rejectedUsers == null || rejectedUsers.equalsIgnoreCase("")) {
            rejectedUsers = SEPARATOR;
        }
        if (!rejectedUsers.contains(userNameItem)) {
            rejectedUsers = rejectedUsers.concat(userNameItemToAdd);

            nodeService.setProperty(meetingNodeRef, PROP_MEETING_REJECTED_USERS, rejectedUsers);
        }
        String acceptedUsers =
                (String) nodeService.getProperty(meetingNodeRef, PROP_MEETING_ACCEPTED_USERS);
        if (acceptedUsers != null) {
            nodeService.setProperty(
                    meetingNodeRef,
                    PROP_MEETING_ACCEPTED_USERS,
                    acceptedUsers.replace(userNameItemToAdd, ""));
        }
    }

    public MeetingRequestStatus getMeetingStatus(NodeRef eventItemId, String userName) {
        if (userName == null) {
            return MeetingRequestStatus.NotApplicable;
        }

        if (!isSingleMeeting(eventItemId)) {
            return MeetingRequestStatus.NotApplicable;
        }
        final String userNameItem = SEPARATOR + userName + SEPARATOR;
        NodeRef appointment = getAppointmentDefinitionNodeFromEvent(eventItemId);
        // check node
        MeetingRequestStatus result = checkMeetingStatus(eventItemId, userNameItem);
        // check definition
        if (result == MeetingRequestStatus.NotApplicable) {
            result = checkMeetingStatus(appointment, userNameItem);
        }
        return result;
    }

    private MeetingRequestStatus checkMeetingStatus(NodeRef nodeRef, final String userNameItem) {
        MeetingRequestStatus result = MeetingRequestStatus.NotApplicable;
        String invitedUsers = (String) nodeService.getProperty(nodeRef, PROP_EVENT_INVITED_USERS);
        if (invitedUsers.contains(userNameItem)) {
            String acceptedUsers = (String) nodeService.getProperty(nodeRef, PROP_MEETING_ACCEPTED_USERS);
            if (acceptedUsers != null && acceptedUsers.contains(userNameItem)) {
                result = MeetingRequestStatus.Accepted;
            } else {
                String rejectedUsers =
                        (String) nodeService.getProperty(nodeRef, PROP_MEETING_REJECTED_USERS);
                if (rejectedUsers != null && rejectedUsers.contains(userNameItem)) {
                    result = MeetingRequestStatus.Rejected;
                } else {
                    result = MeetingRequestStatus.Pending;
                }
            }
        }
        return result;
    }

    private boolean isSingleMeeting(NodeRef nodeRef) {
        boolean result = false;
        if (isSingleEvent(nodeRef)) {
            Serializable eventType = nodeService.getProperty(nodeRef, EventModel.PROP_KIND_OF_EVENT);
            if (eventType != null) {
                result = (AppointmentType.Meeting == AppointmentType.valueOf(eventType.toString()));
            }
        }
        return result;
    }

    public final List<EventItem> getEventsBetweenDates(
            NodeRef eventRoot, DateValue dateFrom, DateValue dateTo) {
        Date beginDate = setStartTime(AppointmentUtils.convertDateValueToDate(dateFrom));
        Date endDate = setEndTime(AppointmentUtils.convertDateValueToDate(dateTo));
        return getEvents(null, eventRoot, beginDate, endDate);
    }

    private Date setStartTime(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date setEndTime(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    private List<String> getUserListFromProperty(NodeRef nodeRef, QName qname) {
        List<String> result = new ArrayList<>();
        Serializable property = nodeService.getProperty(nodeRef, qname);
        if (!(property == null)) {
            String userList = property.toString();
            if (userList.length() > 1) {
                userList = userList.substring(1, userList.length() - 1);
                String[] elements = userList.split("\\" + SEPARATOR);
                result = Arrays.asList(elements);
            }
        }
        return result;
    }

    public final List<EventItem> getEventsBetweenDates(
            String userName, DateValue dateFrom, DateValue dateTo) {
        List<EventItem> result = new ArrayList<>();
        List<NodeRef> eventroots = userService.getEventRootNodes(userName);
        for (NodeRef ref : eventroots) {
            result.addAll(getEventsBetweenDates(ref, dateFrom, dateTo));
        }
        return result;
    }

    private boolean isCurrentUserEventAdmin(NodeRef eventRoot) {
        AccessStatus status =
                permissionService.hasPermission(eventRoot, EventPermissions.EVEADMIN.toString());
        return (status == AccessStatus.ALLOWED);
    }

    public void updateAppointment(
            NodeRef appointmentNodeRef,
            Appointment appointment,
            UpdateMode mode,
            AppointmentUpdateInfo updateInfo) {
        updateAppointment(appointmentNodeRef, appointment, mode, updateInfo, true);
    }

    @Override
    public void updateAppointment(
            NodeRef appointmentNodeRef,
            Appointment appointment,
            UpdateMode mode,
            AppointmentUpdateInfo updateInfo,
            Boolean sendNotification) {
        // TODO Auto-generated method stub

        List<NodeRef> futureEvents = null;
        Appointment oldAppointment = null;

        if (mode == UpdateMode.Single || mode == UpdateMode.AllOccurences) {
            oldAppointment = getAppointmentByNodeRef(appointmentNodeRef);
        }
        if (mode == UpdateMode.FuturOccurences) {
            futureEvents = getFutureAppointmens(appointmentNodeRef);
            boolean isFirst = true;
            for (NodeRef ref : futureEvents) {
                if (isFirst) {
                    oldAppointment = getAppointmentByNodeRef(ref);
                    break;
                }
            }
        }

        final NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(appointmentNodeRef);
        final NodeRef eventRoot = getEventRootFromAppointmentDefinition(appointmentDefinition);
        switch (mode) {
            case AllOccurences:
                updateAllAppointments(appointmentNodeRef, appointment, updateInfo);
                List<NodeRef> allAppointemnets = getAllAppointmens(appointmentNodeRef);
                for (NodeRef ref : allAppointemnets) {
                    updateAppointment(ref, appointment, updateInfo);
                }
                break;
            case FuturOccurences:
                for (NodeRef ref : futureEvents) {
                    updateAppointment(ref, appointment, updateInfo);
                }
                break;
            case Single:
                updateAppointment(appointmentNodeRef, appointment, updateInfo);
                if (oldAppointment != null && !appointment.getDate().equals(oldAppointment.getDate())) {
                    Date javaDate =
                            AppointmentUtils.convertLocalTimeDateValueToDate(
                                    appointment.getStartTime(), appointment.getDate());
                    getNodeService().setProperty(appointmentNodeRef, PROP_EVENT_DATE, javaDate);
                }
                break;
            default:
                break;
        }

        if (sendNotification) {
            sendNotificationsAfterUpdate(
                    appointmentNodeRef,
                    appointment,
                    mode,
                    updateInfo,
                    oldAppointment,
                    appointmentDefinition,
                    eventRoot);
        }
    }

    public void sendNotificationsAfterUpdate(
            NodeRef appointmentNodeRef,
            Appointment appointment,
            UpdateMode mode,
            AppointmentUpdateInfo updateInfo,
            Appointment oldAppointment,
            final NodeRef appointmentDefinition,
            final NodeRef eventRoot) {
        if (appointment instanceof Meeting) {

            Meeting meeting = (Meeting) appointment;
            Meeting oldMeeting = (Meeting) oldAppointment;

            Integer sequence = getSequence(appointmentNodeRef);
            // rescheduling event
            if (!(oldMeeting.getDate().equals(meeting.getDate())
                    && oldMeeting.getStartTime().equals(meeting.getStartTime())
                    && oldMeeting.getEndTime().equals(meeting.getEndTime()))) {
                sequence = incrementSequence(appointmentNodeRef);
            }
            meeting.setId(appointmentDefinition.getId());
            meeting.setSequence(sequence);
            sendMeetingNotificationMeesage(
                    eventRoot,
                    appointmentDefinition,
                    appointmentNodeRef,
                    meeting,
                    MailTemplate.MEETING_REMINDER);
            sendMeetingRequest(
                    eventRoot,
                    meeting,
                    oldMeeting,
                    mode,
                    appointmentNodeRef,
                    updateInfo,
                    appointment.getUseBCC());
        } else if (appointment instanceof Event) {
            Event event = (Event) appointment;

            sendEventNotificationMessage(
                    eventRoot, appointmentDefinition, appointmentNodeRef, event, MailTemplate.EVENT_REMINDER);
            sendEventMeesage(
                    eventRoot,
                    appointmentDefinition,
                    appointmentNodeRef,
                    event,
                    MailTemplate.EVENT_UPDATE_NOTIFICATION);
        }
    }

    private Integer incrementSequence(NodeRef appointmentNodeRef) {
        NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(appointmentNodeRef);
        if (appointmentDefinition == null) {
            throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
        }
        boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
        if ((!isMeetingDefinition)) {
            throw new IllegalArgumentException("appointmentDefinition wrong type ");
        }
        Serializable property = getNodeService().getProperty(appointmentDefinition, PROP_SEQUENCE);
        Integer sequence = 0;
        if (property != null) {
            sequence = Integer.valueOf(property.toString());
        }
        sequence = sequence + 1;
        getNodeService().setProperty(appointmentDefinition, PROP_SEQUENCE, sequence);

        return sequence;
    }

    private Integer getSequence(NodeRef appointmentNodeRef) {
        NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(appointmentNodeRef);
        if (appointmentDefinition == null) {
            throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
        }
        boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
        if ((!isMeetingDefinition)) {
            throw new IllegalArgumentException("appointmentDefinition wrong type ");
        }
        Serializable property = getNodeService().getProperty(appointmentDefinition, PROP_SEQUENCE);
        Integer sequence = 0;
        if (property != null) {
            sequence = Integer.valueOf(property.toString());
        }
        return sequence;
    }

    private void updateAppointment(
            NodeRef appointmentNodeRef, Appointment appointment, AppointmentUpdateInfo updateInfo) {
        if (appointmentNodeRef == null || !isSingleEvent(appointmentNodeRef)) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }

        PropertyMap newProperties = appointment.getProperties(updateInfo);
        Map<QName, Serializable> oldProperties = getNodeService().getProperties(appointmentNodeRef);
        oldProperties.putAll(newProperties);
        oldProperties.remove(PROP_EVENT_OCCURENCE_RATE);
        oldProperties.remove(PROP_SEQUENCE);
        getNodeService().setProperties(appointmentNodeRef, oldProperties);
    }

    private void updateAllAppointments(
            NodeRef appointmentNodeRef, Appointment appointment, AppointmentUpdateInfo updateInfo) {
        if (appointmentNodeRef == null || !isSingleEvent(appointmentNodeRef)) {
            throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
        }

        NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(appointmentNodeRef);
        if (appointmentDefinition == null) {
            throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
        }

        boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
        boolean isEventDefinition = isEventDefinition(appointmentDefinition);
        if ((!isMeetingDefinition && !isEventDefinition)) {
            throw new IllegalArgumentException("meetingNodeRef wrong type ");
        }
        PropertyMap newProperties = appointment.getProperties(updateInfo);
        Map<QName, Serializable> oldProperties = getNodeService().getProperties(appointmentDefinition);
        oldProperties.putAll(newProperties);

        oldProperties.remove(PROP_SEQUENCE);
        getNodeService().setProperties(appointmentDefinition, oldProperties);
    }

    public final List<EventItem> getAppointments(
            EventFilter filter, NodeRef eventRoot, String userName, DateValue date) {
        List<EventItem> result = new ArrayList<>();
        switch (filter) {
            case Exact:
                if (eventRoot == null) {
                    result = getExactEventsOnDate(userName, date);
                } else {
                    result = getExactEventsOnDate(eventRoot, userName, date);
                }
                break;

            case Future:
                if (eventRoot == null) {
                    result = getCurrentFutureEvents(userName);
                } else {
                    result = getCurrentFutureEvents(eventRoot, userName);
                }
                break;
            case Previous:
                if (eventRoot == null) {
                    result = getCurrentPreviousEvents(userName);
                } else {
                    result = getCurrentPreviousEvents(eventRoot, userName);
                }
                break;
            default:
                break;
        }

        return result;
    }

    private List<EventItem> getCurrentPreviousEvents(String userName) {
        List<EventItem> result = new ArrayList<>();
        List<NodeRef> eventRootNodes = userService.getEventRootNodes(userName);
        for (NodeRef ref : eventRootNodes) {
            result.addAll(getCurrentPreviousEvents(ref, userName));
        }
        return result;
    }

    private List<EventItem> getCurrentPreviousEvents(NodeRef eventRoot, String userName) {
        return getEvents(userName, eventRoot, null, new Date());
    }

    private List<EventItem> getExactEventsOnDate(String userName, DateValue date) {
        List<EventItem> result = new ArrayList<>();
        List<NodeRef> eventroots = userService.getEventRootNodes(userName);
        for (NodeRef ref : eventroots) {
            result.addAll(getExactEventsOnDate(ref, userName, date));
        }
        return result;
    }

    private List<EventItem> getExactEventsOnDate(NodeRef eventRoot, String userName, DateValue date) {
        Date dateFrom = AppointmentUtils.convertDateValueToDate(date);
        return getEvents(userName, eventRoot, dateFrom, dateFrom);
    }

    private List<EventItem> getCurrentFutureEvents(NodeRef eventRoot, String userName) {
        return getEvents(userName, eventRoot, new Date(), null);
    }

    private List<EventItem> getCurrentFutureEvents(String userName) {
        List<EventItem> result = new ArrayList<>();
        List<NodeRef> eventroots = userService.getEventRootNodes(userName);
        for (NodeRef ref : eventroots) {
            result.addAll(getCurrentFutureEvents(ref, userName));
        }
        return result;
    }

    public NodeRef getMeetingNodeRef(NodeRef meetingDefinitionNodeRef, String recurrenceId) {

        Date date = getDateFromRecurrenceId(recurrenceId);
        NodeRef appointmentDateContainer = getEventsDatesContainer(meetingDefinitionNodeRef);
        return getAppointmentOnDate(appointmentDateContainer, date);
    }

    @SuppressWarnings("deprecation")
    private Date getDateFromRecurrenceId(String recurrenceId) {
        final int beginIndex = recurrenceId.indexOf(':');
        final String dateString = recurrenceId.substring(beginIndex + 1, beginIndex + 9);

        int year = Integer.valueOf(dateString.substring(0, 4));
        int month = Integer.valueOf(dateString.substring(4, 6));
        int day = Integer.valueOf(dateString.substring(6, 8));
        return new Date(year - 1900, month - 1, day);
    }

    private NodeRef getAppointmentOnDate(final NodeRef appointmentDateContainer, final Date date) {
        String luceneQuery = null;

        boolean directStoreAccess =
                Boolean.parseBoolean(CircabcConfiguration.getProperty("events.direct.store.access"));

        if (!directStoreAccess) {
            luceneQuery = buildLuceneQuery(null, appointmentDateContainer, date, date);
        }

        ResultSet resultSet = null;
        NodeRef nodeRef = null;
        try {
            if (!directStoreAccess) {
                resultSet = executeLuceneQuery(luceneQuery);
            } else {
                resultSet = getEventsDirect(null, appointmentDateContainer, date, date);
            }

            nodeRef = resultSet.getRow(0).getNodeRef();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return nodeRef;
    }

    /**
     * @return the authorityService
     */
    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @return the mailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * @param mailService the mailService to set
     */
    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * @return the nodePreferencesService
     */
    protected final MailPreferencesService getMailPreferencesService() {
        return mailPreferencesService;
    }

    /**
     * @param mailPreferencesService the mailPreferencesService to set
     */
    public final void setMailPreferencesService(MailPreferencesService mailPreferencesService) {
        this.mailPreferencesService = mailPreferencesService;
    }

    /**
     * @return the personService
     */
    protected final PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService the personService to set
     */
    public final void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return the managementService
     */
    public final ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public final List<Appointment> getAllAppointments(NodeRef eventRoot) {
        ParameterCheck.mandatory("The evenRoot node reference is mandatory param", eventRoot);
        if (!nodeService.hasAspect(eventRoot, ASPECT_EVENT_ROOT)) {
            throw new IllegalArgumentException(
                    "The event root noderef must have the aspect applied " + ASPECT_EVENT_ROOT);
        }

        List<Appointment> result = new ArrayList<>();
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(eventRoot);
        for (ChildAssociationRef ref : childAssocs) {

            final NodeRef nodeRef = ref.getChildRef();
            if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {
                final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
                Appointment item = null;
                if (nodeService.getType(nodeRef).equals(EventModel.TYPE_EVENT_DEFINITION)) {
                    item = new EventImpl();
                }
                if (nodeService.getType(nodeRef).equals(EventModel.TYPE_EVENT_MEETING_DEFINITION)) {
                    item = new MeetingImpl();
                }
                item.init(properties);
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Get the event root of the given IG (given by igId)
     */
    public NodeRef getIGsEventRoot(String id) {

        // get the IG root and the its child the Event root nodeRef
        NodeRef igRootRef = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore", id);

        if (!nodeService.exists(igRootRef)) {
            throw new RuntimeException("The IG with id '" + id + "' does not exist.");
        }

        NodeRef eventRoot =
                nodeService.getChildByName(igRootRef, ContentModel.ASSOC_CONTAINS, "Events");

        if (eventRoot == null) {
            throw new RuntimeException("The Event root for IG with id '" + id + "' does not exist.");
        }
        return eventRoot;
    }

    /**
     * Gets the IGRoot of a node.
     */
    public NodeRef getIGRoot(String eventId) {

        NodeRef eventNodeRef = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore", eventId);

        if (!nodeService.exists(eventNodeRef)) {
            throw new RuntimeException("The Event with id '" + eventId + "' does not exist.");
        }

        NodeRef parentRef = eventNodeRef;

        do {
            parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
        } while (parentRef != null && !nodeService.hasAspect(parentRef, CircabcModel.ASPECT_IGROOT));

        if (nodeService.hasAspect(parentRef, CircabcModel.ASPECT_IGROOT)) {
            return parentRef;
        }

        throw new RuntimeException("The node with id '" + eventId + "' has no IG root.");
    }
}
