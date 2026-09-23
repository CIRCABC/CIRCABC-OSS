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
package eu.europa.ec.digit.circabc.rest.service.event;

import com.google.ical.values.DateValue;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailTemplate;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.exception.CircabcRuntimeException;
import io.swagger.model.Appointment;
import io.swagger.model.AppointmentType;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.AudienceStatus;
import io.swagger.model.Event;
import io.swagger.model.EventFilter;
import io.swagger.model.EventItem;
import io.swagger.model.LogRecord;
import io.swagger.model.MainOccurence;
import io.swagger.model.Meeting;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.UpdateMode;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.EventModel;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.util.ApiToolBox;
import io.swagger.util.PathUtils;
import jakarta.mail.MessagingException;
import java.io.Serializable;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link EventService}.
 *
 * <p>This service manages the full lifecycle of CIRCABC calendar appointments (both simple
 * {@link Event}s and {@link Meeting}s) stored as Alfresco nodes under an "Events" root node of an
 * Interest Group. Its responsibilities include:
 *
 * <ul>
 *   <li>Creating events and meetings, including their recurring occurrences, under an event root
 *       node (the node carrying the {@code CircabcModel.ASPECT_EVENT_ROOT} aspect).
 *   <li>Updating and deleting appointments for a single occurrence, all occurrences or future
 *       occurrences ({@link UpdateMode}).
 *   <li>Querying occurrences for calendar rendering (by month, between dates, on an exact date,
 *       future/previous), either through Lucene/Solr search or by direct traversal of the node
 *       tree depending on {@code CircabcConfig.getEventsDirectStoreAccess()}.
 *   <li>Managing meeting invitation replies (accept/reject) and computing per-user meeting request
 *       status ({@link MeetingRequestStatus}).
 *   <li>Sending the related e-mail notifications and iCalendar meeting requests, and logging those
 *       notifications through the {@link LogService}.
 * </ul>
 *
 * <p>Alfresco node structure assumed by the service: the event root contains appointment
 * definition nodes (event or meeting definitions), each definition contains a single dates
 * container, and the dates container holds one node per individual occurrence (the "single event"
 * nodes).
 *
 * @author Slobodan Filipovic
 */
public class EventServiceImpl implements EventService {

  /** Model key used to expose the current occurrence node reference to mail templates. */
  private static final String EVENT_REF = "eventRef";
  protected static final String GIF = ".gif";
  protected static final String ICON = "icon";
  protected static final String IMAGES_ICONS = "/images/icons/";
  private static final String APPOINTMENT_DEFINITION_IS_NULL =
    "appointmentDefinition is null";
  private static final String EVENT_NODE_REF_WRONG_TYPE_OR_NULL =
    "eventNodeRef wrong type or null";
  private static final String SEPARATOR = "|";
  private static final String MAX = "MAX";
  private static final String MIN = "MIN";
  private static final String EVENT_ROOT_NODE_REF_ASPECT =
    "The event root noderef must have the aspect applied ";
  private static final String APPOINTMENT_WRONG_TYPE =
    "appointmentDefinition wrong type ";
  private static final String DOES_NOT_EXIST = "' does not exist.";
  /**
   * A logger for the class
   */
  private static final Log logger = LogFactory.getLog(EventServiceImpl.class);
  private static final String DATE_OF_EVENT = "@" + EventModel.PROP_EVENT_DATE;
  private static final String ESCAPE4 = "\\\\:";
  private static final String ESCAPE3 = "\\:";
  private static final String ESCAPE1 = "\\-";
  private static final String ESCAPE2 = "\\\\-";
  private static final String PROP_EVENT_USER_LIST_ESCAPED =
    EventModel.PROP_EVENT_INVITED_USERS.toString()
      .replace(":", ESCAPE4)
      .replace("\\{", "\\\\{")
      .replace("\\}", "\\\\}");

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private UserService userService;

  @Autowired
  private AuthorityService authorityService;

  @Autowired
  private MailService mailService;

  @Autowired
  private PersonService personService;

  @Autowired
  private MailPreferencesService mailPreferencesService;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private LogService logService;

  @Autowired
  private ApiToolBox apiToolBox;

  @Autowired
  private CircabcConfig circabcConfig;

  /**
   * Creates an event (and its occurrences) under the given event root, sending the related
   * notification e-mails.
   *
   * <p>Convenience overload equivalent to calling {@link #createEvent(NodeRef, Event, boolean)}
   * with mail sending enabled.
   *
   * @param eventRoot the event root node of the Interest Group; must carry the
   *     {@code CircabcModel.ASPECT_EVENT_ROOT} aspect
   * @param event the event definition to create
   * @return the node reference of the created event definition
   */
  public final NodeRef createEvent(NodeRef eventRoot, Event event) {
    return createEvent(eventRoot, event, true);
  }

  private void logNotification(
    NodeRef nodeRef,
    String to,
    boolean ok,
    boolean adminLog
  ) {
    LogRecord logRecord = new LogRecord();
    logRecord.setActivity("Send Notification");
    logRecord.setService("Event");
    logRecord.setInfo("Node: " + getBestTitle(nodeRef) + "; To: " + to);
    logRecord.setOK(ok);

    logRecord.setDocumentID(
      (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID)
    );

    if (adminLog) {
      final NodeRef circabcNodeRef = circabcApi.getCircabcNodeRef();
      logRecord.setIgID(
        (Long) nodeService.getProperty(
          circabcNodeRef,
          ContentModel.PROP_NODE_DBID
        )
      );
      logRecord.setIgName(
        (String) nodeService.getProperty(circabcNodeRef, ContentModel.PROP_NAME)
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
   * Returns the best available human-readable title for a node.
   *
   * @param nodeRef the node to describe
   * @return the node's {@code cm:title} if set and not blank, otherwise its {@code cm:name}
   */
  protected String getBestTitle(final NodeRef nodeRef) {
    final String name = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_NAME
    );
    final String title = (String) nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_TITLE
    );

    if (title == null || title.trim().isEmpty()) {
      return name;
    } else {
      return title;
    }
  }

  /**
   * Creates an event under the given event root together with a node for every occurrence produced
   * by its recurrence rule.
   *
   * <p>The event definition node is created first, then a dates container is (re)used to hold one
   * node per occurrence. When {@code enableMailSending} is {@code true} a reminder and a creation
   * notification e-mail are sent for the first occurrence.
   *
   * @param eventRoot the event root node of the Interest Group; must carry the
   *     {@code CircabcModel.ASPECT_EVENT_ROOT} aspect
   * @param event the event definition to create; validated by {@code validateEvent}
   * @param enableMailSending whether creation/reminder notifications should be sent
   * @return the node reference of the created event definition
   * @throws IllegalArgumentException if the event root does not carry the event root aspect
   * @throws IllegalStateException if an open event declares invited users
   */
  public final NodeRef createEvent(
    NodeRef eventRoot,
    Event event,
    boolean enableMailSending
  ) {
    validateEvent(eventRoot, event);

    PropertyMap properties = event.getProperties();

    // Create the event
    final ChildAssociationRef assocRef = nodeService.createNode(
      eventRoot,
      EventModel.ASSOC_EVENT,
      EventModel.TYPE_EVENT_DEFINITION,
      EventModel.TYPE_EVENT_DEFINITION,
      properties
    );

    NodeRef eventNodeRef = assocRef.getChildRef();

    event.setId(eventNodeRef.getId());

    // Get the eventsDatesContainer
    NodeRef eventsDatesContainer = getEventsDatesContainer(eventNodeRef);
    // if container does not exists create it
    if (eventsDatesContainer == null) {
      eventsDatesContainer = createEventDatesContainer(eventNodeRef);
    }

    List<PropertyMap> eventDatesProperties = event.getEventDatesProperties(
      AppointmentType.Event
    );
    boolean isFirstEvent = true;
    NodeRef firstEvent = null;

    for (PropertyMap map : eventDatesProperties) {
      // Create the property
      ChildAssociationRef car = nodeService.createNode(
        eventsDatesContainer,
        EventModel.ASSOC_EVENT_DATES,
        EventModel.TYPE_EVENT,
        EventModel.TYPE_EVENT,
        map
      );
      if (isFirstEvent) {
        isFirstEvent = false;
        firstEvent = car.getChildRef();
      }
    }

    if (enableMailSending) {
      sendEventNotificationMessage(
        eventRoot,
        eventNodeRef,
        firstEvent,
        event,
        MailTemplate.EVENT_REMINDER
      );
      sendEventMeesage(
        eventRoot,
        eventNodeRef,
        firstEvent,
        event,
        MailTemplate.EVENT_CREATE_NOTIFICATION
      );
    }
    return eventNodeRef;
  }

  private List<String> getUsersEmails(NodeRef eventRoot) {
    final List<String> users = new ArrayList<>();

    users.addAll(
      userService.getUsersWithPermission(
        eventRoot,
        EventPermissions.EVEACCESS.toString()
      )
    );
    users.addAll(
      userService.getUsersWithPermission(
        eventRoot,
        EventPermissions.EVEADMIN.toString()
      )
    );

    return users;
  }

  private void validateEvent(NodeRef eventRoot, Event event) {
    ParameterCheck.mandatory(
      "The evenRoot node reference is mandatory param",
      eventRoot
    );
    ParameterCheck.mandatory("The event is mandatory param ", event);

    ParameterCheck.mandatory(
      "The eventRoot node reference is mandatory param",
      eventRoot
    );
    ParameterCheck.mandatory("The meeting is mandatory param ", event);

    ParameterCheck.mandatory(
      "Event Language is mandatory",
      event.getLanguage()
    );
    ParameterCheck.mandatory("Event Title is mandatory", event.getTitle());
    ParameterCheck.mandatory("Event Type is mandatory", event.getEventType());

    ParameterCheck.mandatory("Event Date is mandatory", event.getStartDate());
    ParameterCheck.mandatory(
      "Event occurs is mandatory",
      event.getOccurenceRate()
    );
    ParameterCheck.mandatory(
      "Event start time is mandatory",
      event.getStartTime()
    );
    ParameterCheck.mandatory("Event end time is mandatory", event.getEndTime());
    ParameterCheck.mandatory(
      "Event timezone is mandatory",
      event.getTimeZoneId()
    );

    ParameterCheck.mandatory(
      "Event audience status is mandatory",
      event.getAudienceStatus()
    );

    ParameterCheck.mandatory("Event name is mandatory", event.getName());
    ParameterCheck.mandatory("Event phone is mandatory", event.getPhone());
    ParameterCheck.mandatory("Event email is mandatory", event.getEmail());

    if (
      event.getAudienceStatus() == AudienceStatus.Open &&
      !event.getInvitedUsers().isEmpty()
    ) {
      throw new IllegalStateException(
        "Open event  should not have invated users."
      );
    }

    if (!nodeService.hasAspect(eventRoot, CircabcModel.ASPECT_EVENT_ROOT)) {
      throw new IllegalArgumentException(
        EVENT_ROOT_NODE_REF_ASPECT + CircabcModel.ASPECT_EVENT_ROOT
      );
    }
  }

  private void sendEventNotificationMessage(
    NodeRef eventRoot,
    NodeRef eventDefinition,
    NodeRef eventItem,
    Event event,
    MailTemplate mailTemplate
  ) {
    Map<String, Object> model = buildDefaultTemplateModel(
      eventRoot,
      event,
      eventDefinition,
      eventItem
    );

    MailWrapper bodyReminder =
      getMailPreferencesService().getDefaultMailTemplate(
        eventRoot,
        mailTemplate
      );

    String from = mailService.getNoReplyEmailAddress();
    String to = event.getEmail();

    model.put(EVENT_REF, eventItem);

    boolean html = true;
    boolean result = false;
    try {
      result = mailService.send(
        from,
        to,
        event.getEmail(),
        bodyReminder.getSubject(model),
        bodyReminder.getBody(model),
        html,
        event.getUseBCC()
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }
    logNotification(eventItem, to, result, false);
    logNotification(eventItem, to, result, true);
  }

  private Map<String, Object> buildDefaultTemplateModel(
    NodeRef eventService,
    Appointment appointment,
    NodeRef appointmentDefinition,
    NodeRef appointmentItem
  ) {
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
    final String currentUsername =
      AuthenticationUtil.getFullyAuthenticatedUser();
    NodeRef currentUserRef = null;

    if (
      currentUsername != null &&
      getPersonService().personExists(currentUsername)
    ) {
      currentUserRef = getPersonService().getPerson(currentUsername);
    }

    return currentUserRef;
  }

  private void sendEventMeesage(
    NodeRef eventRoot,
    NodeRef eventDefinition,
    NodeRef eventItem,
    Event event,
    MailTemplate mailTemplate
  ) {
    Map<String, Object> model = buildDefaultTemplateModel(
      eventRoot,
      event,
      eventDefinition,
      eventItem
    );
    MailWrapper bodyReminder =
      getMailPreferencesService().getDefaultMailTemplate(
        eventRoot,
        mailTemplate
      );

    List<String> emails = getAppointmentEmails(eventRoot, event);

    if (emails.isEmpty()) {
      return;
    }

    String from = mailService.getNoReplyEmailAddress();

    model.put(EVENT_REF, eventItem);

    boolean html = true;
    boolean result = false;
    try {
      result = mailService.send(
        from,
        emails,
        event.getEmail(),
        bodyReminder.getSubject(model),
        bodyReminder.getBody(model),
        html,
        event.getUseBCC()
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }
    String to = StringUtils.join(emails.toArray(), ", ");
    logNotification(eventItem, to, result, false);
    logNotification(eventItem, to, result, true);
  }

  private List<String> getAppointmentEmails(
    NodeRef eventRoot,
    Appointment appointment
  ) {
    List<String> emails = new ArrayList<>();
    List<String> users = new ArrayList<>();

    if (appointment.getAudienceStatus() == AudienceStatus.Closed) {
      users = appointment.getInvitedUsers();
    } else if (
      appointment.getAudienceStatus() == AudienceStatus.Open &&
      Boolean.TRUE.equals(appointment.getEnableNotification())
    ) {
      users = getUsersEmails(eventRoot);
    }

    for (String user : users) {
      if (user.contains("@")) {
        emails.add(user);
      } else {
        String email = userService.getUserEmail(user);
        if (!email.isEmpty()) {
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
    MailTemplate mailTemplate
  ) {
    Map<String, Object> model = buildDefaultTemplateModel(
      eventRoot,
      meeting,
      meetingDefinition,
      meetingItem
    );
    MailWrapper bodyReminder =
      getMailPreferencesService().getDefaultMailTemplate(
        eventRoot,
        mailTemplate
      );

    String from = mailService.getNoReplyEmailAddress();
    String to = meeting.getEmail();

    model.put(EVENT_REF, meetingItem);

    boolean html = true;
    boolean result = false;
    try {
      result = mailService.send(
        from,
        to,
        null,
        bodyReminder.getSubject(model),
        bodyReminder.getBody(model),
        html,
        meeting.getUseBCC()
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when sending meeting notification: ", e);
      }
    }
    logNotification(meetingItem, to, result, false);
    logNotification(meetingItem, to, result, true);
  }

  private void sendMeetingRequest(
    NodeRef eventRoot,
    Meeting meeting,
    Meeting oldMeeting,
    UpdateMode mode,
    NodeRef nodeRef,
    AppointmentUpdateInfo updateInfo,
    boolean useBCC
  ) {
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
      result = mailService.sendMeetingRequest(
        from,
        emails,
        null,
        meeting,
        oldMeeting,
        mode,
        useBCC
      );
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error when sending meeting request: ", e);
      }
    }
    String to = StringUtils.join(emails.toArray(), ", ");
    logNotification(nodeRef, to, result, false);
    logNotification(nodeRef, to, result, true);
  }

  private List<String> getAppointmentEmails(
    NodeRef eventRoot,
    Meeting meeting,
    Meeting oldMeeting
  ) {
    List<String> emails = getAppointmentEmails(eventRoot, meeting);
    List<String> oldEmails = getAppointmentEmails(eventRoot, oldMeeting);
    emails.removeAll(oldEmails);
    return emails;
  }

  /**
   * Creates a meeting (and its occurrences) under the given event root, sending the related
   * notifications and meeting requests.
   *
   * <p>Convenience overload equivalent to calling {@link #createMeeting(NodeRef, Meeting, boolean)}
   * with mail sending enabled.
   *
   * @param eventRoot the event root node of the Interest Group; must carry the
   *     {@code CircabcModel.ASPECT_EVENT_ROOT} aspect
   * @param meeting the meeting definition to create
   * @return the node reference of the created meeting definition
   */
  public final NodeRef createMeeting(NodeRef eventRoot, Meeting meeting) {
    return createMeeting(eventRoot, meeting, true);
  }

  /**
   * Creates a meeting under the given event root together with a node for every occurrence produced
   * by its recurrence rule.
   *
   * <p>The meeting sequence is initialised to {@code 0}. A meeting definition node is created, a
   * dates container is (re)used to hold one node per occurrence, and when {@code enableMailSending}
   * is {@code true} a reminder notification and an iCalendar meeting request are sent for the first
   * occurrence.
   *
   * @param eventRoot the event root node of the Interest Group; must carry the
   *     {@code CircabcModel.ASPECT_EVENT_ROOT} aspect
   * @param meeting the meeting definition to create; validated by {@code validateMeeting}
   * @param enableMailSending whether reminder notifications and meeting requests should be sent
   * @return the node reference of the created meeting definition
   * @throws IllegalArgumentException if the event root does not carry the event root aspect
   * @throws IllegalStateException if an open meeting declares invited users
   */
  public NodeRef createMeeting(
    NodeRef eventRoot,
    Meeting meeting,
    boolean enableMailSending
  ) {
    validateMeeting(eventRoot, meeting);

    meeting.setSequence(0);

    PropertyMap properties = meeting.getProperties();

    // Create the property
    final ChildAssociationRef assocRef = nodeService.createNode(
      eventRoot,
      EventModel.ASSOC_EVENT,
      EventModel.TYPE_EVENT_MEETING_DEFINITION,
      EventModel.TYPE_EVENT_MEETING_DEFINITION,
      properties
    );

    NodeRef meetingNodeRef = assocRef.getChildRef();
    meeting.setId(meetingNodeRef.getId());

    // Get the eventsDatesContainer
    NodeRef eventsDatesContainer = getEventsDatesContainer(meetingNodeRef);
    // if container does not exists create it
    if (eventsDatesContainer == null) {
      eventsDatesContainer = createEventDatesContainer(meetingNodeRef);
    }

    List<PropertyMap> eventDatesProperties = meeting.getEventDatesProperties(
      AppointmentType.Meeting
    );

    boolean isFirstMeeting = true;
    NodeRef firstMeeting = null;

    for (PropertyMap map : eventDatesProperties) {
      // Create the property
      ChildAssociationRef car = nodeService.createNode(
        eventsDatesContainer,
        EventModel.ASSOC_EVENT_DATES,
        EventModel.TYPE_EVENT,
        EventModel.TYPE_EVENT,
        map
      );
      if (isFirstMeeting) {
        isFirstMeeting = false;
        firstMeeting = car.getChildRef();
      }
    }

    if (enableMailSending) {
      sendMeetingNotificationMeesage(
        eventRoot,
        meetingNodeRef,
        firstMeeting,
        meeting,
        MailTemplate.MEETING_REMINDER
      );
      sendMeetingRequest(
        eventRoot,
        meeting,
        null,
        null,
        meetingNodeRef,
        null,
        false
      );
    }
    return meetingNodeRef;
  }

  private void validateMeeting(NodeRef eventRoot, Meeting meeting) {
    ParameterCheck.mandatory(
      "The eventRoot node reference is mandatory param",
      eventRoot
    );
    ParameterCheck.mandatory("The meeting is mandatory param ", meeting);

    ParameterCheck.mandatory(
      "Meeting Language is mandatory",
      meeting.getLanguage()
    );
    ParameterCheck.mandatory("Meeting Title is mandatory", meeting.getTitle());
    ParameterCheck.mandatory(
      "Meeting Type is mandatory",
      meeting.getMeetingTypeString()
    );
    ParameterCheck.mandatory(
      "Meeting Date is mandatory",
      meeting.getStartDate()
    );

    ParameterCheck.mandatory(
      "Meeting occurs is mandatory",
      meeting.getOccurenceRate()
    );

    ParameterCheck.mandatory(
      "Meeting start time is mandatory",
      meeting.getStartTime()
    );
    ParameterCheck.mandatory(
      "Meeting end time is mandatory",
      meeting.getEndTime()
    );
    ParameterCheck.mandatory(
      "Meeting timezone is mandatory",
      meeting.getTimeZoneId()
    );

    ParameterCheck.mandatory(
      "Meeting availability is mandatory",
      meeting.getAvailability()
    );
    ParameterCheck.mandatory(
      "Meeting audience status is mandatory",
      meeting.getAudienceStatus()
    );

    ParameterCheck.mandatory("Meeting name is mandatory", meeting.getName());
    ParameterCheck.mandatory("Meeting phone is mandatory", meeting.getPhone());
    ParameterCheck.mandatory("Meeting email is mandatory", meeting.getEmail());

    if (
      meeting.getAudienceStatus() == AudienceStatus.Open &&
      !meeting.getInvitedUsers().isEmpty()
    ) {
      throw new IllegalStateException(
        "Open meeting  should not have invated users."
      );
    }

    if (!nodeService.hasAspect(eventRoot, CircabcModel.ASPECT_EVENT_ROOT)) {
      throw new IllegalArgumentException(
        EVENT_ROOT_NODE_REF_ASPECT + CircabcModel.ASPECT_EVENT_ROOT
      );
    }
  }

  /**
   * Deletes an appointment occurrence, all its occurrences, or its future occurrences depending on
   * the given mode, sending the corresponding cancellation notifications beforehand.
   *
   * <p>For {@link UpdateMode#FuturOccurences} the method returns without doing anything when there
   * are no future occurrences to delete.
   *
   * @param appointmentNodeRef the node reference of a single occurrence (event or meeting)
   * @param mode whether to delete a single occurrence, all occurrences or future occurrences
   */
  public final void deleteAppointment(
    NodeRef appointmentNodeRef,
    UpdateMode mode
  ) {
    Appointment appointment = getAppointmentByNodeRef(appointmentNodeRef);
    NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      appointmentNodeRef
    );
    NodeRef eventRoot = getEventRootFromAppointmentDefinition(
      appointmentDefinition
    );
    Date eventDate = getEventDateForMode(appointmentNodeRef, mode);
    List<NodeRef> futureEvents =
      mode == UpdateMode.FuturOccurences
        ? getFutureAppointmens(appointmentNodeRef)
        : null;

    if (mode == UpdateMode.FuturOccurences && futureEvents.isEmpty()) return;
    if (futureEvents != null && !futureEvents.isEmpty()) {
      eventDate = (Date) nodeService.getProperty(
        futureEvents.get(0),
        EventModel.PROP_EVENT_DATE
      );
    }

    sendDeleteNotifications(
      appointment,
      appointmentNodeRef,
      appointmentDefinition,
      eventRoot,
      eventDate,
      mode
    );
    executeDeleteByMode(appointmentNodeRef, appointment, futureEvents, mode);
  }

  private Date getEventDateForMode(
    NodeRef appointmentNodeRef,
    UpdateMode mode
  ) {
    return mode == UpdateMode.Single
      ? (Date) nodeService.getProperty(
          appointmentNodeRef,
          EventModel.PROP_EVENT_DATE
        )
      : null;
  }

  private void sendDeleteNotifications(
    Appointment appointment,
    NodeRef appointmentNodeRef,
    NodeRef appointmentDefinition,
    NodeRef eventRoot,
    Date eventDate,
    UpdateMode mode
  ) {
    if (appointment instanceof Meeting meeting) {
      Integer sequence = incrementSequence(appointmentNodeRef);
      meeting.setId(appointmentDefinition.getId());
      meeting.setSequence(sequence);
      sendMeetingNotificationMeesage(
        eventRoot,
        appointmentDefinition,
        appointmentNodeRef,
        meeting,
        MailTemplate.MEETING_DELETE_REMINDER
      );
      sendCancelMeetingMessage(eventRoot, meeting, eventDate, mode);
    } else if (appointment instanceof Event event) {
      sendEventNotificationMessage(
        eventRoot,
        appointmentDefinition,
        appointmentNodeRef,
        event,
        MailTemplate.EVENT_DELETE_REMINDER
      );
      sendEventMeesage(
        eventRoot,
        appointmentDefinition,
        appointmentNodeRef,
        event,
        MailTemplate.EVENT_DELETE_NOTIFICATION
      );
    }
  }

  private void executeDeleteByMode(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    List<NodeRef> futureEvents,
    UpdateMode mode
  ) {
    switch (mode) {
      case AllOccurences:
        deleteAllAppointments(appointmentNodeRef);
        break;
      case FuturOccurences:
        if (futureEvents != null) {
          futureEvents.forEach(this::deleteSingleAppointment);
        } else {
          logger.warn("futureEvents is null, no appointments to delete.");
        }
        break;
      case Single:
        if (
          appointment.getOccurenceRate().getMainOccurence() ==
          MainOccurence.OnlyOnce
        ) {
          deleteAllAppointments(appointmentNodeRef);
        } else {
          deleteSingleAppointment(appointmentNodeRef);
        }
        break;
      default:
        break;
    }
  }

  /**
   * Returns all occurrences of an appointment as calendar items.
   *
   * <p>The given node may be a single occurrence (in which case its sibling occurrences are
   * returned) or an event/meeting definition (in which case all occurrences under its dates
   * container are returned).
   *
   * @param appointmentNodeRef the node reference of a single occurrence or an appointment
   *     definition
   * @return the list of occurrences as {@link EventItem}s
   * @throws IllegalArgumentException if {@code appointmentNodeRef} is {@code null} or of an
   *     unexpected node type
   */
  public final List<EventItem> getAllOccurences(
    final NodeRef appointmentNodeRef
  ) {
    if (appointmentNodeRef == null) {
      throw new IllegalArgumentException("appointmentNodeRef is null");
    }

    final NodeRef dateContainer;

    if (isSingleEvent(appointmentNodeRef)) {
      dateContainer = nodeService
        .getPrimaryParent(appointmentNodeRef)
        .getParentRef();
    } else if (
      isMeetingDefinition(appointmentNodeRef) ||
      isEventDefinition(appointmentNodeRef)
    ) {
      dateContainer = getEventsDatesContainer(appointmentNodeRef);
    } else {
      throw new IllegalArgumentException("Bad node type.");
    }

    final List<ChildAssociationRef> singleEventAssoc =
      nodeService.getChildAssocs(dateContainer);
    final List<EventItem> allOccurences = new ArrayList<>(
      singleEventAssoc.size()
    );

    final NodeRef interestGroup = apiToolBox.getCurrentInterestGroup(
      appointmentNodeRef
    );
    final String interestGroupName = (String) nodeService.getProperty(
      interestGroup,
      ContentModel.PROP_NAME
    );
    final String interesGroupTitle = (String) nodeService.getProperty(
      interestGroup,
      ContentModel.PROP_TITLE
    );

    for (final ChildAssociationRef child : singleEventAssoc) {
      allOccurences.add(
        buildEventItem(
          interestGroupName,
          interesGroupTitle,
          child.getChildRef()
        )
      );
    }

    return allOccurences;
  }

  private void sendCancelMeetingMessage(
    NodeRef eventRoot,
    Meeting meeting,
    Date eventDate,
    UpdateMode mode
  ) {
    List<String> to = getAppointmentEmails(eventRoot, meeting);

    if (to.isEmpty()) {
      return;
    }

    String from = mailService.getNoReplyEmailAddress();

    try {
      mailService.cancelMeeting(from, to, null, meeting, eventDate, mode);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error(e.getMessage(), e);
      }
    }
  }

  private List<NodeRef> getAllAppointmens(NodeRef appointment) {
    List<NodeRef> result = new ArrayList<>();
    if (appointment == null || !isSingleEvent(appointment)) {
      throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
    }
    NodeRef appointmentDateContainer = nodeService
      .getPrimaryParent(appointment)
      .getParentRef();
    String luceneQuery = null;

    boolean directStoreAccess = circabcConfig.getEventsDirectStoreAccess();

    if (!directStoreAccess) {
      luceneQuery = buildLuceneQuery(
        null,
        appointmentDateContainer,
        null,
        null
      );
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
    NodeRef appointmentDateContainer = nodeService
      .getPrimaryParent(appointment)
      .getParentRef();
    String luceneQuery = null;

    boolean directStoreAccess = circabcConfig.getEventsDirectStoreAccess();

    if (!directStoreAccess) {
      luceneQuery = buildLuceneQuery(
        null,
        appointmentDateContainer,
        new Date(),
        null
      );
    }

    ResultSet resultSet = null;
    try {
      if (!directStoreAccess) {
        resultSet = executeLuceneQuery(luceneQuery);
      } else {
        resultSet = getEventsDirect(
          null,
          appointmentDateContainer,
          new Date(),
          null
        );
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

  /**
   * Records a user's reply (accepted/rejected) to a meeting invitation.
   *
   * <p>When the node is a meeting definition the status is applied directly to it. Otherwise the
   * status is applied to the single occurrence, to all occurrences (via the definition) or to
   * future occurrences depending on the given mode.
   *
   * @param meetingNodeRef the meeting occurrence or definition node reference
   * @param userName the user whose reply is being recorded
   * @param meetingRequestStatus the reply status to apply
   * @param mode whether the reply applies to a single, all or future occurrences
   */
  public final void setMeetingRequestStatus(
    NodeRef meetingNodeRef,
    String userName,
    MeetingRequestStatus meetingRequestStatus,
    UpdateMode mode
  ) {
    if (isMeetingDefinition(meetingNodeRef)) {
      applyMeetingRequestStatus(meetingNodeRef, userName, meetingRequestStatus);
      return;
    }

    switch (mode) {
      case AllOccurences:
        NodeRef meetingDefinition = getAppointmentDefinitionNodeFromEvent(
          meetingNodeRef
        );
        applyMeetingRequestStatus(
          meetingDefinition,
          userName,
          meetingRequestStatus
        );
        break;
      case FuturOccurences:
        for (NodeRef ref : getFutureAppointmens(meetingNodeRef)) {
          applyMeetingRequestStatus(ref, userName, meetingRequestStatus);
        }
        break;
      case Single:
        applyMeetingRequestStatus(
          meetingNodeRef,
          userName,
          meetingRequestStatus
        );
        break;
      default:
        break;
    }
  }

  private void applyMeetingRequestStatus(
    NodeRef nodeRef,
    String userName,
    MeetingRequestStatus status
  ) {
    if (status == MeetingRequestStatus.Accepted) {
      acceptMeetingRequest(nodeRef, userName);
    } else if (status == MeetingRequestStatus.Rejected) {
      rejectMeetingRequest(nodeRef, userName);
    }
  }

  private NodeRef createEventDatesContainer(NodeRef nodeRef) {
    final ChildAssociationRef assocRef = nodeService.createNode(
      nodeRef,
      EventModel.ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER,
      EventModel.TYPE_EVENT_DATES_CONTAINER,
      EventModel.TYPE_EVENT_DATES_CONTAINER,
      new PropertyMap()
    );

    return assocRef.getChildRef();
  }

  private NodeRef getEventsDatesContainer(NodeRef nodeRef) {
    if (nodeRef == null) {
      throw new IllegalArgumentException("nodeRef can not be null");
    }
    QName type = nodeService.getType(nodeRef);
    if (
      !(EventModel.TYPE_EVENT_DEFINITION.isMatch(type) ||
        EventModel.TYPE_EVENT_MEETING_DEFINITION.isMatch(type))
    ) {
      throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
    }

    NodeRef eventdatesContainer = null;

    final List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
      nodeRef,
      EventModel.ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER,
      RegexQNamePattern.MATCH_ALL
    );

    if (childAssocRefs.size() == 1) {
      final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
      eventdatesContainer = toKeepAssocRef.getChildRef();
    } else if (childAssocRefs.size() > 1) {
      // This is a problem - destroy all but the first
      if (logger.isWarnEnabled()) {
        logger.warn("Too many event containers: " + nodeRef);
      }
      throw new CircabcRuntimeException("Too many event containers");
    }
    return eventdatesContainer;
  }

  private void deleteSingleAppointment(NodeRef eventNodeRef) {
    if (eventNodeRef == null || !isSingleEvent(eventNodeRef)) {
      throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
    }
    nodeService.deleteNode(eventNodeRef);
  }

  private boolean isEventDefinition(NodeRef nodeRef) {
    return EventModel.TYPE_EVENT_DEFINITION.isMatch(
      nodeService.getType(nodeRef)
    );
  }

  private void deleteAllAppointments(NodeRef appointment) {
    if (appointment == null || !isSingleEvent(appointment)) {
      throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
    }

    NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      appointment
    );
    if (appointmentDefinition == null) {
      throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
    }

    boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
    boolean isEventDefinition = isEventDefinition(appointmentDefinition);
    if ((!isMeetingDefinition && !isEventDefinition)) {
      throw new IllegalArgumentException(APPOINTMENT_WRONG_TYPE);
    }
    nodeService.deleteNode(appointmentDefinition);
  }

  private boolean isSingleEvent(NodeRef nodeRef) {
    return EventModel.TYPE_EVENT.isMatch(nodeService.getType(nodeRef));
  }

  private boolean isMeetingDefinition(NodeRef nodeRef) {
    return EventModel.TYPE_EVENT_MEETING_DEFINITION.isMatch(
      nodeService.getType(nodeRef)
    );
  }

  private NodeRef getAppointmentDefinitionNodeFromEvent(NodeRef appointment) {
    ChildAssociationRef parent = nodeService.getPrimaryParent(appointment);
    ChildAssociationRef parentOfParent = nodeService.getPrimaryParent(
      parent.getParentRef()
    );
    return parentOfParent.getParentRef();
  }

  private NodeRef getEventRootFromAppointmentDefinition(
    NodeRef appointmentDefinition
  ) {
    ChildAssociationRef parent = nodeService.getPrimaryParent(
      appointmentDefinition
    );
    return parent.getParentRef();
  }

  /**
   * Returns all event occurrences of an Interest Group that fall within the given calendar month.
   *
   * @param eventRoot the event root node of the Interest Group
   * @param month the month (1-12)
   * @param year the four-digit year
   * @return the occurrences within that month as {@link EventItem}s
   */
  public final List<EventItem> getEventsByMonth(
    NodeRef eventRoot,
    int month,
    int year
  ) {
    DateTime firstDateInMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
    DateTime lastDateInMonth = firstDateInMonth.dayOfMonth().withMaximumValue();
    return getEvents(
      null,
      eventRoot,
      firstDateInMonth.toDate(),
      lastDateInMonth.toDate()
    );
  }

  private List<EventItem> getEvents(
    String userName,
    NodeRef eventRoot,
    Date dateFrom,
    Date dateTo
  ) {
    String luceneQuery = null;
    // get all events for interest group between two dates in calendar

    boolean directStoreAccess = circabcConfig.getEventsDirectStoreAccess();

    if (!directStoreAccess) {
      if (isCurrentUserEventAdmin(eventRoot)) {
        luceneQuery = buildLuceneQuery(null, eventRoot, dateFrom, dateTo);
      } else {
        luceneQuery = buildLuceneQuery(userName, eventRoot, dateFrom, dateTo);
      }
    }

    final NodeRef ig = nodeService.getPrimaryParent(eventRoot).getParentRef();
    final String interesGroupName = (String) nodeService.getProperty(
      ig,
      ContentModel.PROP_NAME
    );
    final String interesGroupTitle = (String) nodeService.getProperty(
      ig,
      ContentModel.PROP_TITLE
    );

    ResultSet resultSet = null;
    final List<EventItem> resultList = new ArrayList<>();
    try {
      resultSet = getResultSet(
        eventRoot,
        userName,
        dateFrom,
        dateTo,
        luceneQuery,
        directStoreAccess
      );
      for (final ResultSetRow row : resultSet) {
        NodeRef nodeRef = row.getNodeRef();
        if (nodeService.exists(nodeRef)) {
          EventItem eventItem = buildEventItem(
            interesGroupName,
            interesGroupTitle,
            nodeRef
          );
          eventItem.setMeetingStatus(getMeetingStatusString(nodeRef, userName));
          resultList.add(eventItem);
        }
      }
    } finally {
      if (resultSet != null) resultSet.close();
    }
    return resultList;
  }

  private ResultSet getResultSet(
    NodeRef eventRoot,
    String userName,
    Date dateFrom,
    Date dateTo,
    String luceneQuery,
    boolean directStoreAccess
  ) {
    if (!directStoreAccess) return executeLuceneQuery(luceneQuery);
    return isCurrentUserEventAdmin(eventRoot)
      ? getEventsDirect(null, eventRoot, dateFrom, dateTo)
      : getEventsDirect(userName, eventRoot, dateFrom, dateTo);
  }

  private String getMeetingStatusString(NodeRef nodeRef, String userName) {
    MeetingRequestStatus status = getMeetingStatus(nodeRef, userName);
    if (status == MeetingRequestStatus.Accepted) {
      return "Accepted";
    }
    return status == MeetingRequestStatus.Rejected ? "Rejected" : "";
  }

  /**
   * Builds a calendar {@link EventItem} from a single occurrence node, completing the missing
   * abstract/location fields from the parent appointment definition when they are not set on the
   * occurrence itself.
   *
   * @param interestGroupName the name of the owning Interest Group
   * @param interestGroupTitle the title of the owning Interest Group
   * @param nodeRef the single occurrence node reference
   * @return the populated {@link EventItem}
   */
  public EventItem buildEventItem(
    final String interestGroupName,
    final String interestGroupTitle,
    NodeRef nodeRef
  ) {
    Date d = (Date) nodeService.getProperty(
      nodeRef,
      EventModel.PROP_EVENT_DATE
    );
    EventItem eventItem = new EventItem();
    eventItem.setEventNodeRef(nodeRef);
    eventItem.setContact(
      (String) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_NAME)
    );
    eventItem.setDate(d);
    eventItem.setInterestGroup(interestGroupName);
    eventItem.setInterestGroupTitle(interestGroupTitle);
    String eventType = (String) nodeService.getProperty(
      nodeRef,
      EventModel.PROP_KIND_OF_EVENT
    );
    eventItem.setEventType(AppointmentType.valueOf(eventType));
    eventItem.setTitle(
      (String) nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_TITLE)
    );
    eventItem.setStartTime(
      new LocalTime(
        nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_START_TIME)
      )
    );
    eventItem.setEndTime(
      new LocalTime(
        nodeService.getProperty(nodeRef, EventModel.PROP_EVENT_END_TIME)
      )
    );
    String theAbstract = (String) nodeService.getProperty(
      nodeRef,
      EventModel.PROP_EVENT_ABSTRACT
    );
    String location = (String) nodeService.getProperty(
      nodeRef,
      EventModel.PROP_EVENT_LOCATION
    );

    // get info from the (parent, parent) to complete the EventItem
    NodeRef eventDefinitionNodeRef = nodeService
      .getPrimaryParent(nodeService.getPrimaryParent(nodeRef).getParentRef())
      .getParentRef();
    eventItem.setOccurrenceRate(
      (String) nodeService.getProperty(
        eventDefinitionNodeRef,
        EventModel.PROP_EVENT_OCCURENCE_RATE
      )
    );
    String appointmentContainerId = eventDefinitionNodeRef.getId();
    eventItem.setAppointmentContainerId(
      appointmentContainerId == null ? "" : appointmentContainerId
    );
    if (theAbstract == null || theAbstract.trim().isEmpty()) {
      theAbstract = (String) nodeService.getProperty(
        eventDefinitionNodeRef,
        EventModel.PROP_EVENT_ABSTRACT
      );
    }
    if (location == null || location.trim().isEmpty()) {
      location = (String) nodeService.getProperty(
        eventDefinitionNodeRef,
        EventModel.PROP_EVENT_LOCATION
      );
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
    final String userName,
    final NodeRef searchNodeRef,
    Date dateFrom,
    Date dateTo
  ) {
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
      .append("ce\\:date")
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
    final String userName,
    final NodeRef searchNodeRef,
    Date dateFrom,
    Date dateTo
  ) {
    List<EventResultSetRow> events = new ArrayList<>();

    collectEvents(searchNodeRef, events, userName, dateFrom, dateTo);

    return new EventResultSet(events);
  }

  /**
   * Recursive function that collects all events according to their dates and
   * invited users. It is
   * used by getEventsDirect to traverse the events tree of the IG/Events node and
   * retrieve all
   * events without using search.
   */
  private void collectEvents(
    NodeRef nodeRef,
    List<EventResultSetRow> events,
    String userName,
    Date dateFrom,
    Date dateTo
  ) {
    if (nodeRef == null || !nodeService.exists(nodeRef)) {
      return;
    }

    for (ChildAssociationRef childRef : nodeService.getChildAssocs(nodeRef)) {
      final NodeRef childNodeRef = childRef.getChildRef();

      if (!nodeService.exists(childNodeRef)) {
        continue;
      }

      if (nodeService.getType(childNodeRef).equals(EventModel.TYPE_EVENT)) {
        processEventNode(childNodeRef, events, userName, dateFrom, dateTo);
      } else {
        collectEvents(childNodeRef, events, userName, dateFrom, dateTo);
      }
    }
  }

  private void processEventNode(
    NodeRef childNodeRef,
    List<EventResultSetRow> events,
    String userName,
    Date dateFrom,
    Date dateTo
  ) {
    Date date = (Date) nodeService.getProperty(
      childNodeRef,
      EventModel.PROP_EVENT_DATE
    );

    if (!isDateInRange(date, dateFrom, dateTo)) {
      return;
    }

    String effectiveUserName =
      userName != null
        ? userName
        : AuthenticationUtil.getFullyAuthenticatedUser();
    if (shouldAddAppointment(childNodeRef, effectiveUserName)) {
      events.add(new EventResultSetRow(childNodeRef));
    }
  }

  private boolean isDateInRange(Date date, Date dateFrom, Date dateTo) {
    boolean dateAfter = dateFrom == null || !date.before(dateFrom);
    boolean dateBefore = dateTo == null || !date.after(dateTo);

    if (dateAfter && dateBefore) {
      return true;
    }

    // Compare for equality without time part
    if (dateFrom != null && dateFrom.equals(dateTo)) {
      LocalDate dateFromLT = new DateTime(dateFrom).toLocalDate();
      LocalDate dateLT = new DateTime(date).toLocalDate();
      return dateFromLT.compareTo(dateLT) == 0;
    }

    return false;
  }

  private boolean shouldAddAppointment(NodeRef childNodeRef, String userName) {
    return AuthenticationUtil.runAs(
      () -> {
        if (
          permissionService.hasPermission(
            childNodeRef,
            EventPermissions.EVEADMIN.toString()
          ) ==
          AccessStatus.ALLOWED
        ) {
          return true;
        }
        if (
          permissionService.hasPermission(
            childNodeRef,
            EventPermissions.EVEACCESS.toString()
          ) !=
          AccessStatus.ALLOWED
        ) {
          return false;
        }
        String audience = (String) nodeService.getProperty(
          childNodeRef,
          EventModel.PROP_EVENT_AUDIENCE
        );
        if (!"Closed".equals(audience)) return true;
        String invitedUsers = (String) nodeService.getProperty(
          childNodeRef,
          EventModel.PROP_EVENT_INVITED_USERS
        );
        return (
          invitedUsers.contains(userName) ||
          (SEPARATOR + SEPARATOR).equals(invitedUsers) ||
          SEPARATOR.equals(invitedUsers)
        );
      },
      userName
    );
  }

  private String getPathFromSpaceRef(NodeRef searchNodeRef, boolean children) {
    final Path path = nodeService.getPath(searchNodeRef);

    final StringBuilder buf = new StringBuilder(64);
    String elementString;
    Path.Element element;
    ChildAssociationRef elementRef;
    Collection<String> prefixes;
    for (int i = 0; i < path.size(); i++) {
      elementString = "";
      element = path.get(i);
      if (element instanceof Path.ChildAssocElement childAssocElement) {
        elementRef = childAssocElement.getRef();
        if (elementRef.getParentRef() != null) {
          prefixes = namespaceService.getPrefixes(
            elementRef.getQName().getNamespaceURI()
          );
          if (!prefixes.isEmpty()) {
            elementString =
              '/' +
              prefixes.iterator().next() +
              ':' +
              ISO9075.encode(elementRef.getQName().getLocalName());
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
    return str.replace(ESCAPE1, ESCAPE2).replace(ESCAPE3, ESCAPE4);
  }

  /**
   * Returns the current and future event occurrences of the given Interest Group.
   *
   * @param eventRoot the event root node of the Interest Group
   * @return the occurrences dated today or later, as {@link EventItem}s
   */
  public final List<EventItem> getCurrentFutureEvents(NodeRef eventRoot) {
    return getEvents(null, eventRoot, new Date(), null);
  }

  /**
   * Returns the past event occurrences of the given Interest Group.
   *
   * @param eventRoot the event root node of the Interest Group
   * @return the occurrences dated before now, as {@link EventItem}s
   */
  public final List<EventItem> getCurrentPreviousEvents(NodeRef eventRoot) {
    return getEvents(null, eventRoot, null, new Date());
  }

  /**
   * Returns the event occurrences of the given Interest Group that take place on a specific date.
   *
   * @param eventRoot the event root node of the Interest Group
   * @param date the date to query
   * @return the occurrences on that date as {@link EventItem}s
   */
  public List<EventItem> getCalendarEventsOnDate(
    NodeRef eventRoot,
    DateValue date
  ) {
    Date dateFrom = AppointmentUtils.convertDateValueToDate(date);
    return getEvents(null, eventRoot, dateFrom, dateFrom);
  }

  /**
   * Reconstructs an {@link Appointment} (an {@link EventImpl} or {@link MeetingImpl}) from a single
   * occurrence node by merging the properties of the parent appointment definition with those of
   * the occurrence.
   *
   * <p>For closed meetings, the invited users' accepted/rejected status is also loaded into the
   * returned appointment's audience.
   *
   * @param eventItemId the node reference of a single occurrence
   * @return the reconstructed appointment
   * @throws IllegalArgumentException if the node is {@code null}, not a single occurrence, or its
   *     definition is missing or of an unexpected type
   */
  public final Appointment getAppointmentByNodeRef(NodeRef eventItemId) {
    if (eventItemId == null || !isSingleEvent(eventItemId)) {
      throw new IllegalArgumentException("eventItemId wrong type or null");
    }

    NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      eventItemId
    );
    if (appointmentDefinition == null) {
      throw new IllegalArgumentException("meetingNodeRef is null");
    }

    boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
    boolean isEventDefinition = isEventDefinition(appointmentDefinition);
    if ((!isMeetingDefinition && !isEventDefinition)) {
      throw new IllegalArgumentException("meetingNodeRef wrong type ");
    }
    Map<QName, Serializable> properties = nodeService.getProperties(
      appointmentDefinition
    );
    Appointment appointment = null;

    if (isMeetingDefinition) {
      appointment = new MeetingImpl();
    } else {
      appointment = new EventImpl();
    }

    Map<QName, Serializable> itemProperties = nodeService.getProperties(
      eventItemId
    );
    properties.putAll(itemProperties);
    appointment.init(properties);

    if (
      isMeetingDefinition &&
      appointment.getAudienceStatus() == AudienceStatus.Closed
    ) {
      final List<String> invitedUsers = getUserListFromProperty(
        eventItemId,
        EventModel.PROP_EVENT_INVITED_USERS
      );
      final List<String> acceptedUsers = getUserListFromProperty(
        eventItemId,
        EventModel.PROP_MEETING_ACCEPTED_USERS
      );
      final List<String> rejectedUsers = getUserListFromProperty(
        eventItemId,
        EventModel.PROP_MEETING_ACCEPTED_USERS
      );
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
    String invitedUsers = (String) nodeService.getProperty(
      meetingNodeRef,
      EventModel.PROP_EVENT_INVITED_USERS
    );
    if (invitedUsers.contains(userNameItem)) {
      acceptUser(meetingNodeRef, userNameItem, userNameItemToAdd);
    } else {
      final String newUserName = getInvitedUserWithSameEmail(
        userName,
        invitedUsers
      );
      if (newUserName != null) {
        final String newUserNameItem = SEPARATOR + newUserName + SEPARATOR;
        final String newUserNameItemToAdd = newUserName + SEPARATOR;
        acceptUser(meetingNodeRef, newUserNameItem, newUserNameItemToAdd);
      } else {
        throw new IllegalStateException(
          "User " + userName + " is not invated for this meeting"
        );
      }
    }
  }

  private String getInvitedUserWithSameEmail(
    String userName,
    String invitedUsers
  ) {
    if (userName.contains("@")) {
      return null;
    }
    final String[] users = invitedUsers.split("\\|");
    final NodeRef personNodeRef = userService.getPerson(userName);
    final String email = (String) nodeService.getProperty(
      personNodeRef,
      ContentModel.PROP_EMAIL
    );
    for (String user : users) {
      if (user.isEmpty()) {
        continue;
      }
      final NodeRef userNodeRef = userService.getPerson(user);
      final String userEmail = (String) nodeService.getProperty(
        userNodeRef,
        ContentModel.PROP_EMAIL
      );
      if (userEmail.equals(email)) {
        return user;
      }
    }
    return null;
  }

  private void acceptUser(
    NodeRef meetingNodeRef,
    final String userNameItem,
    final String userNameItemToAdd
  ) {
    String acceptedUsers = (String) nodeService.getProperty(
      meetingNodeRef,
      EventModel.PROP_MEETING_ACCEPTED_USERS
    );
    if (acceptedUsers == null || acceptedUsers.equalsIgnoreCase("")) {
      acceptedUsers = SEPARATOR;
    }
    if (!acceptedUsers.contains(userNameItem)) {
      acceptedUsers = acceptedUsers.concat(userNameItemToAdd);
      nodeService.setProperty(
        meetingNodeRef,
        EventModel.PROP_MEETING_ACCEPTED_USERS,
        acceptedUsers
      );
    }
    String rejectedUsers = (String) nodeService.getProperty(
      meetingNodeRef,
      EventModel.PROP_MEETING_ACCEPTED_USERS
    );
    if (rejectedUsers != null) {
      nodeService.setProperty(
        meetingNodeRef,
        EventModel.PROP_MEETING_ACCEPTED_USERS,
        rejectedUsers.replace(userNameItemToAdd, "")
      );
    }
  }

  private void rejectMeetingRequest(NodeRef meetingNodeRef, String userName) {
    final String userNameItem = SEPARATOR + userName + SEPARATOR;
    final String userNameItemToAdd = userName + SEPARATOR;
    String invitedUsers = (String) nodeService.getProperty(
      meetingNodeRef,
      EventModel.PROP_EVENT_INVITED_USERS
    );
    if (invitedUsers.contains(userNameItem)) {
      rejectUser(meetingNodeRef, userNameItem, userNameItemToAdd);
    } else {
      final String newUserName = getInvitedUserWithSameEmail(
        userName,
        invitedUsers
      );
      if (newUserName != null) {
        final String newUserNameItem = SEPARATOR + newUserName + SEPARATOR;
        final String newUserNameItemToAdd = newUserName + SEPARATOR;
        rejectUser(meetingNodeRef, newUserNameItem, newUserNameItemToAdd);
      } else {
        throw new IllegalStateException(
          "User " + userName + " is not invated for this meeting"
        );
      }
    }
  }

  private void rejectUser(
    NodeRef meetingNodeRef,
    final String userNameItem,
    final String userNameItemToAdd
  ) {
    String rejectedUsers = (String) nodeService.getProperty(
      meetingNodeRef,
      EventModel.PROP_MEETING_ACCEPTED_USERS
    );
    if (rejectedUsers == null || rejectedUsers.equalsIgnoreCase("")) {
      rejectedUsers = SEPARATOR;
    }
    if (!rejectedUsers.contains(userNameItem)) {
      rejectedUsers = rejectedUsers.concat(userNameItemToAdd);

      nodeService.setProperty(
        meetingNodeRef,
        EventModel.PROP_MEETING_ACCEPTED_USERS,
        rejectedUsers
      );
    }
    String acceptedUsers = (String) nodeService.getProperty(
      meetingNodeRef,
      EventModel.PROP_MEETING_ACCEPTED_USERS
    );
    if (acceptedUsers != null) {
      nodeService.setProperty(
        meetingNodeRef,
        EventModel.PROP_MEETING_ACCEPTED_USERS,
        acceptedUsers.replace(userNameItemToAdd, "")
      );
    }
  }

  /**
   * Computes a user's invitation reply status for a given meeting occurrence.
   *
   * <p>The occurrence node is checked first and, if no status is found there, the parent meeting
   * definition is checked as a fallback.
   *
   * @param eventItemId the node reference of a single occurrence
   * @param userName the user to look up; may be {@code null}
   * @return the user's {@link MeetingRequestStatus}, or {@link MeetingRequestStatus#NotApplicable}
   *     when the user is {@code null}, the node is not a meeting, or the user is not invited
   */
  public MeetingRequestStatus getMeetingStatus(
    NodeRef eventItemId,
    String userName
  ) {
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

  private MeetingRequestStatus checkMeetingStatus(
    NodeRef nodeRef,
    final String userNameItem
  ) {
    MeetingRequestStatus result = MeetingRequestStatus.NotApplicable;
    String invitedUsers = (String) nodeService.getProperty(
      nodeRef,
      EventModel.PROP_EVENT_INVITED_USERS
    );
    if (invitedUsers.contains(userNameItem)) {
      String acceptedUsers = (String) nodeService.getProperty(
        nodeRef,
        EventModel.PROP_MEETING_ACCEPTED_USERS
      );
      if (acceptedUsers != null && acceptedUsers.contains(userNameItem)) {
        result = MeetingRequestStatus.Accepted;
      } else {
        String rejectedUsers = (String) nodeService.getProperty(
          nodeRef,
          EventModel.PROP_MEETING_ACCEPTED_USERS
        );
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
      Serializable eventType = nodeService.getProperty(
        nodeRef,
        EventModel.PROP_KIND_OF_EVENT
      );
      if (eventType != null) {
        result = (AppointmentType.Meeting ==
          AppointmentType.valueOf(eventType.toString()));
      }
    }
    return result;
  }

  /**
   * Returns all event occurrences of the given Interest Group between two dates (inclusive).
   *
   * <p>The lower bound is normalised to the start of the day (00:00) and the upper bound to the end
   * of the day (23:59).
   *
   * @param eventRoot the event root node of the Interest Group
   * @param dateFrom the first day to include
   * @param dateTo the last day to include
   * @return the occurrences within the range as {@link EventItem}s
   */
  public final List<EventItem> getEventsBetweenDates(
    NodeRef eventRoot,
    DateValue dateFrom,
    DateValue dateTo
  ) {
    Date beginDate = setStartTime(
      AppointmentUtils.convertLocalTimeDateValueToDate(
        new LocalTime(0, 0),
        dateFrom
      )
    );
    Date endDate = setEndTime(
      AppointmentUtils.convertLocalTimeDateValueToDate(
        new LocalTime(23, 59),
        dateTo
      )
    );
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
    if (property != null) {
      String userList = property.toString();
      if (userList.length() > 1) {
        userList = userList.substring(1, userList.length() - 1);
        String[] elements = userList.split("\\" + SEPARATOR);
        result = Arrays.asList(elements);
      }
    }
    return result;
  }

  /**
   * Returns all event occurrences between two dates across every event root the given user can
   * access.
   *
   * @param userName the user whose accessible Interest Groups are queried
   * @param dateFrom the first day to include
   * @param dateTo the last day to include
   * @return the occurrences within the range, aggregated over all the user's event roots
   */
  public final List<EventItem> getEventsBetweenDates(
    String userName,
    DateValue dateFrom,
    DateValue dateTo
  ) {
    List<EventItem> result = new ArrayList<>();
    List<NodeRef> eventroots = userService.getEventRootNodes(userName);
    for (NodeRef ref : eventroots) {
      result.addAll(getEventsBetweenDates(ref, dateFrom, dateTo));
    }
    return result;
  }

  private boolean isCurrentUserEventAdmin(NodeRef eventRoot) {
    AccessStatus status = permissionService.hasPermission(
      eventRoot,
      EventPermissions.EVEADMIN.toString()
    );
    return (status == AccessStatus.ALLOWED);
  }

  /**
   * Updates an appointment, sending the related notifications.
   *
   * <p>Convenience overload equivalent to calling
   * {@link #updateAppointment(NodeRef, Appointment, UpdateMode, AppointmentUpdateInfo, Boolean)}
   * with notifications enabled.
   *
   * @param appointmentNodeRef the node reference of a single occurrence
   * @param appointment the new appointment data to apply
   * @param mode whether to update a single, all or future occurrences
   * @param updateInfo the subset of information that changed
   */
  public void updateAppointment(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    UpdateMode mode,
    AppointmentUpdateInfo updateInfo
  ) {
    updateAppointment(appointmentNodeRef, appointment, mode, updateInfo, true);
  }

  /**
   * Updates an appointment for a single occurrence, all occurrences or future occurrences and
   * optionally sends the related notifications and meeting requests.
   *
   * @param appointmentNodeRef the node reference of a single occurrence
   * @param appointment the new appointment data to apply
   * @param mode whether to update a single, all or future occurrences
   * @param updateInfo the subset of information that changed
   * @param sendNotification whether update notifications should be sent
   */
  @Override
  public void updateAppointment(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    UpdateMode mode,
    AppointmentUpdateInfo updateInfo,
    Boolean sendNotification
  ) {
    Appointment oldAppointment = getOldAppointment(appointmentNodeRef, mode);
    List<NodeRef> futureEvents =
      mode == UpdateMode.FuturOccurences
        ? getFutureAppointmens(appointmentNodeRef)
        : null;

    final NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      appointmentNodeRef
    );
    final NodeRef eventRoot = getEventRootFromAppointmentDefinition(
      appointmentDefinition
    );

    applyUpdateByMode(
      appointmentNodeRef,
      appointment,
      updateInfo,
      mode,
      futureEvents,
      oldAppointment
    );

    if (Boolean.TRUE.equals(sendNotification)) {
      sendNotificationsAfterUpdate(
        appointmentNodeRef,
        appointment,
        mode,
        updateInfo,
        oldAppointment,
        appointmentDefinition,
        eventRoot
      );
    }
  }

  private Appointment getOldAppointment(
    NodeRef appointmentNodeRef,
    UpdateMode mode
  ) {
    if (mode == UpdateMode.Single || mode == UpdateMode.AllOccurences) {
      return getAppointmentByNodeRef(appointmentNodeRef);
    }
    if (mode == UpdateMode.FuturOccurences) {
      List<NodeRef> futureEvents = getFutureAppointmens(appointmentNodeRef);
      return futureEvents.isEmpty()
        ? null
        : getAppointmentByNodeRef(futureEvents.get(0));
    }
    return null;
  }

  private void applyUpdateByMode(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    AppointmentUpdateInfo updateInfo,
    UpdateMode mode,
    List<NodeRef> futureEvents,
    Appointment oldAppointment
  ) {
    switch (mode) {
      case AllOccurences:
        updateAllAppointments(appointmentNodeRef, appointment, updateInfo);
        for (NodeRef ref : getAllAppointmens(appointmentNodeRef)) {
          updateAppointment(ref, appointment, updateInfo);
        }
        break;
      case FuturOccurences:
        if (futureEvents != null) {
          for (NodeRef ref : futureEvents) {
            updateAppointment(ref, appointment, updateInfo);
          }
        } else {
          logger.warn("futureEvents is null, no appointments to delete.");
        }
        break;
      case Single:
        updateSingleAppointment(
          appointmentNodeRef,
          appointment,
          updateInfo,
          oldAppointment
        );
        break;
      default:
        break;
    }
  }

  private void updateSingleAppointment(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    AppointmentUpdateInfo updateInfo,
    Appointment oldAppointment
  ) {
    updateAppointment(appointmentNodeRef, appointment, updateInfo);
    if (
      oldAppointment != null &&
      !appointment.getDate().equals(oldAppointment.getDate())
    ) {
      Date javaDate = AppointmentUtils.convertLocalTimeDateValueToDate(
        appointment.getStartTime(),
        appointment.getDate()
      );
      nodeService.setProperty(
        appointmentNodeRef,
        EventModel.PROP_EVENT_DATE,
        javaDate
      );
    }
  }

  /**
   * Sends the appropriate notifications after an appointment update, dispatching to meeting-specific
   * or event-specific notification logic based on the concrete appointment type.
   *
   * @param appointmentNodeRef the node reference of the updated occurrence
   * @param appointment the updated appointment (a {@link Meeting} or an {@link Event})
   * @param mode the update mode that was applied
   * @param updateInfo the subset of information that changed
   * @param oldAppointment the appointment state before the update, used to detect rescheduling
   * @param appointmentDefinition the parent appointment definition node
   * @param eventRoot the owning event root node
   */
  public void sendNotificationsAfterUpdate(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    UpdateMode mode,
    AppointmentUpdateInfo updateInfo,
    Appointment oldAppointment,
    NodeRef appointmentDefinition,
    NodeRef eventRoot
  ) {
    if (appointment instanceof Meeting meeting) {
      sendMeetingUpdateNotifications(
        appointmentNodeRef,
        meeting,
        (Meeting) oldAppointment,
        appointmentDefinition,
        eventRoot,
        mode,
        updateInfo
      );
    } else if (appointment instanceof Event event) {
      sendEventUpdateNotifications(
        eventRoot,
        appointmentDefinition,
        appointmentNodeRef,
        event
      );
    }
  }

  private void sendMeetingUpdateNotifications(
    NodeRef appointmentNodeRef,
    Meeting meeting,
    Meeting oldMeeting,
    NodeRef appointmentDefinition,
    NodeRef eventRoot,
    UpdateMode mode,
    AppointmentUpdateInfo updateInfo
  ) {
    Integer sequence = getSequence(appointmentNodeRef);
    boolean rescheduled =
      !oldMeeting.getDate().equals(meeting.getDate()) ||
      !oldMeeting.getStartTime().equals(meeting.getStartTime()) ||
      !oldMeeting.getEndTime().equals(meeting.getEndTime());
    if (rescheduled) sequence = incrementSequence(appointmentNodeRef);
    meeting.setId(appointmentDefinition.getId());
    meeting.setSequence(sequence);
    sendMeetingNotificationMeesage(
      eventRoot,
      appointmentDefinition,
      appointmentNodeRef,
      meeting,
      MailTemplate.MEETING_REMINDER
    );
    sendMeetingRequest(
      eventRoot,
      meeting,
      oldMeeting,
      mode,
      appointmentNodeRef,
      updateInfo,
      meeting.getUseBCC()
    );
  }

  private void sendEventUpdateNotifications(
    NodeRef eventRoot,
    NodeRef appointmentDefinition,
    NodeRef appointmentNodeRef,
    Event event
  ) {
    sendEventNotificationMessage(
      eventRoot,
      appointmentDefinition,
      appointmentNodeRef,
      event,
      MailTemplate.EVENT_REMINDER
    );
    sendEventMeesage(
      eventRoot,
      appointmentDefinition,
      appointmentNodeRef,
      event,
      MailTemplate.EVENT_UPDATE_NOTIFICATION
    );
  }

  private Integer incrementSequence(NodeRef appointmentNodeRef) {
    NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      appointmentNodeRef
    );
    if (appointmentDefinition == null) {
      throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
    }
    boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
    if ((!isMeetingDefinition)) {
      throw new IllegalArgumentException(APPOINTMENT_WRONG_TYPE);
    }
    Serializable property = nodeService.getProperty(
      appointmentDefinition,
      EventModel.PROP_SEQUENCE
    );
    int sequence = 0;
    if (property != null) {
      sequence = Integer.parseInt(property.toString());
    }
    sequence = sequence + 1;
    nodeService.setProperty(
      appointmentDefinition,
      EventModel.PROP_SEQUENCE,
      sequence
    );

    return sequence;
  }

  private Integer getSequence(NodeRef appointmentNodeRef) {
    NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      appointmentNodeRef
    );
    if (appointmentDefinition == null) {
      throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
    }
    boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
    if ((!isMeetingDefinition)) {
      throw new IllegalArgumentException(APPOINTMENT_WRONG_TYPE);
    }
    Serializable property = nodeService.getProperty(
      appointmentDefinition,
      EventModel.PROP_SEQUENCE
    );
    int sequence = 0;
    if (property != null) {
      sequence = Integer.parseInt(property.toString());
    }
    return sequence;
  }

  private void updateAppointment(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    AppointmentUpdateInfo updateInfo
  ) {
    if (appointmentNodeRef == null || !isSingleEvent(appointmentNodeRef)) {
      throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
    }

    PropertyMap newProperties = appointment.getProperties(updateInfo);
    Map<QName, Serializable> oldProperties = nodeService.getProperties(
      appointmentNodeRef
    );
    oldProperties.putAll(newProperties);
    oldProperties.remove(EventModel.PROP_EVENT_OCCURENCE_RATE);
    oldProperties.remove(EventModel.PROP_SEQUENCE);
    nodeService.setProperties(appointmentNodeRef, oldProperties);
  }

  private void updateAllAppointments(
    NodeRef appointmentNodeRef,
    Appointment appointment,
    AppointmentUpdateInfo updateInfo
  ) {
    if (appointmentNodeRef == null || !isSingleEvent(appointmentNodeRef)) {
      throw new IllegalArgumentException(EVENT_NODE_REF_WRONG_TYPE_OR_NULL);
    }

    NodeRef appointmentDefinition = getAppointmentDefinitionNodeFromEvent(
      appointmentNodeRef
    );
    if (appointmentDefinition == null) {
      throw new IllegalArgumentException(APPOINTMENT_DEFINITION_IS_NULL);
    }

    boolean isMeetingDefinition = isMeetingDefinition(appointmentDefinition);
    boolean isEventDefinition = isEventDefinition(appointmentDefinition);
    if ((!isMeetingDefinition && !isEventDefinition)) {
      throw new IllegalArgumentException(APPOINTMENT_WRONG_TYPE);
    }
    PropertyMap newProperties = appointment.getProperties(updateInfo);
    Map<QName, Serializable> oldProperties = nodeService.getProperties(
      appointmentDefinition
    );
    oldProperties.putAll(newProperties);

    oldProperties.remove(EventModel.PROP_SEQUENCE);
    nodeService.setProperties(appointmentDefinition, oldProperties);
  }

  /**
   * Returns appointment occurrences matching a filter, optionally scoped to a single event root.
   *
   * <p>When {@code eventRoot} is {@code null} the query spans every event root accessible to the
   * given user; otherwise it is limited to that event root. The {@link EventFilter} selects exact
   * occurrences on a date, future occurrences or past occurrences.
   *
   * @param filter the selection filter ({@code Exact}, {@code Future} or {@code Previous})
   * @param eventRoot the event root to query, or {@code null} to query all of the user's event
   *     roots
   * @param userName the user whose accessible occurrences are queried
   * @param date the reference date, used by the {@code Exact} filter
   * @return the matching occurrences as {@link EventItem}s
   */
  public final List<EventItem> getAppointments(
    EventFilter filter,
    NodeRef eventRoot,
    String userName,
    DateValue date
  ) {
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

  private List<EventItem> getCurrentPreviousEvents(
    NodeRef eventRoot,
    String userName
  ) {
    return getEvents(
      userName,
      eventRoot,
      null,
      AppointmentUtils.getEndOfDay(new Date())
    );
  }

  private List<EventItem> getExactEventsOnDate(
    String userName,
    DateValue date
  ) {
    List<EventItem> result = new ArrayList<>();
    List<NodeRef> eventroots = userService.getEventRootNodes(userName);
    for (NodeRef ref : eventroots) {
      result.addAll(getExactEventsOnDate(ref, userName, date));
    }
    return result;
  }

  private List<EventItem> getExactEventsOnDate(
    NodeRef eventRoot,
    String userName,
    DateValue date
  ) {
    Date d = AppointmentUtils.convertDateValueToDate(date);
    Date dateFrom = AppointmentUtils.getStartOfDay(d);
    Date dateTo = AppointmentUtils.getEndOfDay(d);

    return getEvents(userName, eventRoot, dateFrom, dateTo);
  }

  private List<EventItem> getCurrentFutureEvents(
    NodeRef eventRoot,
    String userName
  ) {
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

  /**
   * Resolves the single meeting occurrence node identified by an iCalendar recurrence id under a
   * given meeting definition.
   *
   * @param meetingDefinitionNodeRef the meeting definition node reference
   * @param recurrenceId the iCalendar {@code RECURRENCE-ID} value identifying the occurrence date
   * @return the node reference of the matching occurrence
   */
  public NodeRef getMeetingNodeRef(
    NodeRef meetingDefinitionNodeRef,
    String recurrenceId
  ) {
    Date date = getDateFromRecurrenceId(recurrenceId);
    NodeRef appointmentDateContainer = getEventsDatesContainer(
      meetingDefinitionNodeRef
    );
    return getAppointmentOnDate(appointmentDateContainer, date);
  }

  @SuppressWarnings("deprecation")
  private Date getDateFromRecurrenceId(String recurrenceId) {
    final int beginIndex = recurrenceId.indexOf(':');
    final String dateString = recurrenceId.substring(
      beginIndex + 1,
      beginIndex + 9
    );

    int year = Integer.parseInt(dateString.substring(0, 4));
    int month = Integer.parseInt(dateString.substring(4, 6));
    int day = Integer.parseInt(dateString.substring(6, 8));
    return new Date(year - 1900, month - 1, day);
  }

  private NodeRef getAppointmentOnDate(
    final NodeRef appointmentDateContainer,
    final Date date
  ) {
    String luceneQuery = null;

    boolean directStoreAccess = circabcConfig.getEventsDirectStoreAccess();

    if (!directStoreAccess) {
      luceneQuery = buildLuceneQuery(
        null,
        appointmentDateContainer,
        date,
        date
      );
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
  public final void setMailPreferencesService(
    MailPreferencesService mailPreferencesService
  ) {
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
   * Returns all appointment definitions (events and meetings) directly under the given event root.
   *
   * @param eventRoot the event root node of the Interest Group; must carry the
   *     {@code CircabcModel.ASPECT_EVENT_ROOT} aspect
   * @return the appointment definitions as {@link Appointment}s
   * @throws IllegalArgumentException if the event root does not carry the event root aspect
   */
  public final List<Appointment> getAllAppointments(NodeRef eventRoot) {
    ParameterCheck.mandatory(
      "The evenRoot node reference is mandatory param",
      eventRoot
    );
    if (!nodeService.hasAspect(eventRoot, CircabcModel.ASPECT_EVENT_ROOT)) {
      throw new IllegalArgumentException(
        EVENT_ROOT_NODE_REF_ASPECT + CircabcModel.ASPECT_EVENT_ROOT
      );
    }

    List<Appointment> result = new ArrayList<>();
    List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
      eventRoot
    );
    for (ChildAssociationRef ref : childAssocs) {
      final NodeRef nodeRef = ref.getChildRef();
      if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {
        final Map<QName, Serializable> properties = nodeService.getProperties(
          nodeRef
        );
        Appointment item = null;
        QName nodeType = nodeService.getType(nodeRef);
        if (nodeType.equals(EventModel.TYPE_EVENT_DEFINITION)) {
          item = new EventImpl();
          item.init(properties);
          result.add(item);
        } else if (nodeType.equals(EventModel.TYPE_EVENT_MEETING_DEFINITION)) {
          item = new MeetingImpl();
          item.init(properties);
          result.add(item);
        }
      }
    }
    return result;
  }

  /**
   * Returns the "Events" root node of the Interest Group identified by the given node id.
   *
   * @param id the node id of the Interest Group root
   * @return the event root node of that Interest Group
   * @throws CircabcRuntimeException if the Interest Group or its "Events" root node does not exist
   */
  public NodeRef getIGsEventRoot(String id) {
    // get the IG root and the its child the Event root nodeRef
    NodeRef igRootRef = new NodeRef(
      StoreRef.PROTOCOL_WORKSPACE,
      "SpacesStore",
      id
    );

    if (!nodeService.exists(igRootRef)) {
      throw new CircabcRuntimeException(
        "The IG with id '" + id + DOES_NOT_EXIST
      );
    }

    NodeRef eventRoot = nodeService.getChildByName(
      igRootRef,
      ContentModel.ASSOC_CONTAINS,
      "Events"
    );

    if (eventRoot == null) {
      throw new CircabcRuntimeException(
        "The Event root for IG with id '" + id + DOES_NOT_EXIST
      );
    }
    return eventRoot;
  }

  /**
   * Walks up the node hierarchy from a given event node to find its owning Interest Group root (the
   * ancestor carrying the {@code CircabcModel.ASPECT_IGROOT} aspect).
   *
   * @param eventId the node id of an event (or descendant) node
   * @return the Interest Group root node reference
   * @throws CircabcRuntimeException if the event node does not exist or has no Interest Group root
   *     ancestor
   */
  public NodeRef getIGRoot(String eventId) {
    NodeRef eventNodeRef = new NodeRef(
      StoreRef.PROTOCOL_WORKSPACE,
      "SpacesStore",
      eventId
    );

    if (!nodeService.exists(eventNodeRef)) {
      throw new CircabcRuntimeException(
        "The Event with id '" + eventId + DOES_NOT_EXIST
      );
    }

    NodeRef parentRef = eventNodeRef;

    do {
      parentRef = nodeService.getPrimaryParent(parentRef).getParentRef();
    } while (
      parentRef != null &&
      !nodeService.hasAspect(parentRef, CircabcModel.ASPECT_IGROOT)
    );

    if (nodeService.hasAspect(parentRef, CircabcModel.ASPECT_IGROOT)) {
      return parentRef;
    }

    throw new CircabcRuntimeException(
      "The node with id '" + eventId + "' has no IG root."
    );
  }
}
