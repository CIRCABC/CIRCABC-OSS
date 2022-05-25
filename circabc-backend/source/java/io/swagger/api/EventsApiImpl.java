package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.repo.event.AppointmentUtils;
import eu.cec.digit.circabc.repo.event.EventImpl;
import eu.cec.digit.circabc.repo.event.MeetingImpl;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.event.*;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.user.UserService;
import io.swagger.model.InterestGroup;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.PagedEventItems;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation to manage events and meetings
 *
 * @author schwerr
 */
public class EventsApiImpl implements EventsApi {

    private final Log logger = LogFactory.getLog(EventsApiImpl.class);

    private EventService eventService = null;

    private PersonService personService = null;

    private PermissionService permissionService = null;

    private SearchService searchService = null;

    private UserService userService = null;

    private NodeService nodeService = null;

    private AuthorityService authorityService = null;

    private UsersApi usersApi = null;

    private ApiToolBox apiToolBox;

    private CircabcService circabcService;

    @Override
    public List<EventItem> groupsIdEventsGet(String id, Date dateFrom, Date dateTo, String language) {

        // TODO language ?

        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("'dateFrom' and 'dateTo' cannot be null.");
        }

        NodeRef eventRoot = eventService.getIGsEventRoot(id);

        List<EventItem> eventItems =
                eventService.getEventsBetweenDates(
                        eventRoot,
                        AppointmentUtils.convertDateToDateValue(dateFrom),
                        AppointmentUtils.convertDateToDateValue(dateTo));

        adaptEventItemsWithIGIdAndTimeZone(eventItems);

        sortEventItems("appointmentDate_DESC", eventItems);

        return eventItems;
    }

    @Override
    public void groupsIdEventsPost(String id, String appointmentBody) {

        LogRecord logRecord = new LogRecord();
        logRecord.setService("Event");

        NodeRef eventRoot = eventService.getIGsEventRoot(id);

        Appointment appointment = parseBodyJSON(appointmentBody, false);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        // create an event or meeting according to the appointment type
        try {
            MLPropertyInterceptor.setMLAware(false);

            if (appointment instanceof Event) {
                logRecord.setActivity("Create event");

                NodeRef nodeRef = eventService.createEvent(eventRoot, (Event) appointment);
                logRecord.setDocumentID(
                        (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
            } else if (appointment instanceof Meeting) {
                logRecord.setActivity("Create meeting");

                /* NodeRef nodeRef = */
                eventService.createMeeting(eventRoot, (Meeting) appointment);
            } else {
                throw new RuntimeException("Invalid appointment type provided. Must be Event or Meeting.");
            }
        } catch (Exception e) {
            logger.error("Exception when creating event or meeting.", e);
            throw e;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.EventsApi#eventsIdDelete(java.lang.String,
     * eu.cec.digit.circabc.service.event.UpdateMode)
     */
    @Override
    public void eventsIdDelete(String id, UpdateMode updateMode) {

        NodeRef appointmentNodeRef = getAppointmentNodeRef(id);

        eventService.deleteAppointment(appointmentNodeRef, updateMode);
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.EventsApi#eventsIdGet(java.lang.String)
     */
    @Override
    public Appointment eventsIdGet(String id) {

        NodeRef appointmentNodeRef = getAppointmentNodeRef(id);

        Appointment appointment = eventService.getAppointmentByNodeRef(appointmentNodeRef);

        String igId = eventService.getIGRoot(id).getId();
        List<String> newInvitedUsers = adaptInvitedUsers(igId, appointment.getInvitedUsers());
        appointment.setInvitedUsers(newInvitedUsers);

        return appointment;
    }

    private List<String> adaptInvitedUsers(String igId, List<String> invitedUsers) {

        List<String> newInvitedUsers = new ArrayList<>();

        Set<String> userIds = circabcService.getUserIds(igId);

        for (String invitedUser : invitedUsers) {
            // check if it is an email
            if (invitedUser.contains("@")) {
                newInvitedUsers.add(invitedUser);
                continue;
            }
            if (userIds.contains(invitedUser)) {
                newInvitedUsers.add(invitedUser);
            } else {
                newInvitedUsers.add(userService.getUserEmail(invitedUser));
            }
        }

        return newInvitedUsers;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.EventsApi#eventsIdPut(java.lang.String, java.lang.String,
     * eu.cec.digit.circabc.service.event.AppointmentUpdateInfo,
     * eu.cec.digit.circabc.service.event.UpdateMode)
     */
    @Override
    public void eventsIdPut(
            String id,
            String appointmentBody,
            AppointmentUpdateInfo appointmentUpdateInfo,
            UpdateMode updateMode) {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(false);

            NodeRef appointmentNodeRef = getAppointmentNodeRef(id);

            Appointment appointment = parseBodyJSON(appointmentBody, true);

            if (!(appointment instanceof Meeting)
                    && appointmentUpdateInfo.equals(AppointmentUpdateInfo.RelevantSpace)) {
                throw new IllegalArgumentException("'RelevantSpace' is only applicable for Meetings.");
            }

            if (AppointmentUpdateInfo.All.equals(appointmentUpdateInfo)) {

                eventService.updateAppointment(
                        appointmentNodeRef, appointment, updateMode, AppointmentUpdateInfo.Audience, false);
                eventService.updateAppointment(
                        appointmentNodeRef,
                        appointment,
                        updateMode,
                        AppointmentUpdateInfo.ContactInformation,
                        false);
                eventService.updateAppointment(
                        appointmentNodeRef,
                        appointment,
                        updateMode,
                        AppointmentUpdateInfo.RelevantSpace,
                        false);
                eventService.updateAppointment(
                        appointmentNodeRef,
                        appointment,
                        updateMode,
                        AppointmentUpdateInfo.GeneralInformation,
                        true);
            } else {
                eventService.updateAppointment(
                        appointmentNodeRef, appointment, updateMode, appointmentUpdateInfo);
            }
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.EventsApi#usersIdEventsGet(java.lang.String, java.util.Date,
     * eu.cec.digit.circabc.service.event.EventFilter)
     */
    @Override
    public List<EventItem> usersIdEventsGet(String userId, Date exactDate, EventFilter filter) {
        List<EventItem> items =
                eventService.getAppointments(
                        filter, null, userId, AppointmentUtils.convertDateToDateValue(exactDate));
        for (EventItem eventItem : items) {
            String audience =
                    (String)
                            nodeService.getProperty(eventItem.getEventNodeRef(), EventModel.PROP_EVENT_AUDIENCE);
            eventItem.setMeetingStatus(audience);

            String timeZone = eventService.getAppointmentByNodeRef(eventItem.getEventNodeRef()).getTimeZoneId();
            eventItem.setTimeZone(timeZone);
            
            NodeRef igNodeRef = eventService.getIGRoot(eventItem.getEventNodeRef().getId());
            eventItem.setInterestGroup(igNodeRef.getId());
        }
        return items;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.EventsApi#usersIdEventsGet(java.lang.String, java.util.Date,
     * java.util.Date)
     */
    @Override
    public List<EventItem> usersIdEventsGet(String userId, Date dateFrom, Date dateTo) {

        if (userId == null) {
            throw new IllegalArgumentException("'userId' cannot be null.");
        }

        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("'dateFrom' and 'dateTo' cannot be null.");
        }

        List<EventItem> eventItems = new ArrayList<>();

        ResultSet resultSet = null;

        try {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            String startDate = simpleDateFormat.format(dateFrom);
            String endDate = simpleDateFormat.format(dateTo);

            String query =
                    "TYPE:\"{http://www.cc.cec/circabc/model/events/1.0}event\" AND @ce\\:date:[\""
                            + startDate
                            + "\" TO \""
                            + endDate
                            + "\"] AND (@ce\\:audience:\"Open\" OR (@ce\\:audience:\"Closed\" AND @ce\\:invitedUsers:*"
                            + userId
                            + "*))";

            if (logger.isDebugEnabled()) {
                logger.debug("Query to search for events: " + query);
            }

            final SearchParameters searchParameters = new SearchParameters();

            searchParameters.setQuery(query);
            searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
            searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            searchParameters.addSort("dateOfEvent", true);

            resultSet =
                    AuthenticationUtil.runAs(
                            new AuthenticationUtil.RunAsWork<ResultSet>() {

                                public ResultSet doWork() {
                                    return searchService.query(searchParameters);
                                }
                            },
                            AuthenticationUtil.getSystemUserName());

            for (final ResultSetRow row : resultSet) {

                NodeRef eventNodeRef = row.getNodeRef();

                NodeRef igNodeRef = eventService.getIGRoot(eventNodeRef.getId());

                String audienceStatus =
                        (String) nodeService.getProperty(eventNodeRef, EventModel.PROP_EVENT_AUDIENCE);

                // check if the userId is part of the IG where the event was defined (for Open events only)
                if ("Open".equals(audienceStatus) && !userInIG(userId, igNodeRef.getId())) {
                    continue;
                }

                EventItem eventItem = eventService.buildEventItem(igNodeRef.getId(), "", eventNodeRef);
                eventItem.setMeetingStatus(audienceStatus);

                String timeZone = eventService.getAppointmentByNodeRef(eventItem.getEventNodeRef()).getTimeZoneId();
                eventItem.setTimeZone(timeZone);
                eventItems.add(eventItem);
            }
        } catch (Exception e) {
            logger.error(e);
            throw e;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }

        return eventItems;
    }

    /**
     * @see io.swagger.api.EventsApi#usersIdEventsPost(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public void usersIdEventsPost(String userId, String meetingId, String action, String updateMode) {

        if (!("Rejected".equals(action) || "Accepted".equals(action))) {
            throw new IllegalArgumentException("Invalid action. Must be 'Accepted' or 'Rejected'");
        }

        if (!("Single".equals(updateMode)
                || "AllOccurences".equals(updateMode)
                || "FuturOccurences".equals(updateMode))) {
            throw new IllegalArgumentException(
                    "Invalid update mode. Must be " + "'Single', 'AllOccurences' or 'FuturOccurences'");
        }

        if (userId == null || userId.isEmpty()) {
            userId = AuthenticationUtil.getFullyAuthenticatedUser();
        }

        NodeRef meetingNodeRef = Converter.createNodeRefFromId(meetingId);

        if (!(nodeService.getType(meetingNodeRef).equals(EventModel.TYPE_EVENT)
                && "Meeting"
                .equals(nodeService.getProperty(meetingNodeRef, EventModel.PROP_KIND_OF_EVENT)))) {
            throw new IllegalArgumentException("Node with id " + meetingId + " is not a meeting.");
        }

        // once we know it's a meeting, apply the action
        eventService.setMeetingRequestStatus(
                meetingNodeRef,
                userId,
                MeetingRequestStatus.valueOf(action),
                UpdateMode.valueOf(updateMode));
    }

    /**
     * @see io.swagger.api.EventsApi#groupsIdEventsListGet(String, String, Date, int, int, String)
     */
    @Override
    public PagedEventItems groupsIdEventsListGet(
            String igId, String filter, Date exactDate, int startItem, int amount, String sort) {

        NodeRef eventRoot = null;

        // if eventRoot is null, list all IGs
        if (igId != null) {

            eventRoot = Converter.createNodeRefFromId(igId);

            eventRoot = nodeService.getChildByName(eventRoot, ContentModel.ASSOC_CONTAINS, "Events");

            if (eventRoot == null) {
                throw new IllegalArgumentException("The event root of IG " + igId + " was not found.");
            }
        }

        String userName = AuthenticationUtil.getFullyAuthenticatedUser();

        List<EventItem> eventItems =
                eventService.getAppointments(
                        EventFilter.valueOf(filter),
                        eventRoot,
                        userName,
                        AppointmentUtils.convertDateToDateValue(exactDate));
        
        adaptEventItemsWithIGIdAndTimeZone(eventItems);

        int resultSize = eventItems.size();

        sortEventItems(sort, eventItems);

        List<EventItem> pagedEventItems;

        if (amount == 0) {
            // limit == 0 means that we want all items
            pagedEventItems = eventItems;
        } else {
            pagedEventItems = new ArrayList<>();

            int endItem = Math.min(startItem + amount, resultSize);

            for (int index = startItem; index < endItem; index++) {
                pagedEventItems.add(eventItems.get(index));
            }
        }

        return new PagedEventItems(pagedEventItems, resultSize);
    }

    private void sortEventItems(final String sort, List<EventItem> eventItems) {

        // sort elements
        if (sort != null && (sort.endsWith("_ASC") || sort.endsWith("_DESC"))) {

            final boolean descending = sort.endsWith("_DESC");

            Comparator<EventItem> comparator =
                    new Comparator<EventItem>() {

                        /** @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) */
                        @Override
                        public int compare(EventItem eventItem1, EventItem eventItem2) {

                            if (sort.startsWith("appointmentDate")) {
                                if (eventItem1.getDate().equals(eventItem2.getDate())) {
                                    return 0;
                                } else if (eventItem1.getDate().after(eventItem2.getDate())) {
                                    return descending ? 1 : -1;
                                } else {
                                    return descending ? -1 : 1;
                                }
                            } else if (sort.startsWith("title")) {
                                if (eventItem1.getTitle().equals(eventItem2.getTitle())) {
                                    return 0;
                                } else if (eventItem1.getTitle().compareToIgnoreCase(eventItem2.getTitle()) > 0) {
                                    return descending ? 1 : -1;
                                } else {
                                    return descending ? -1 : 1;
                                }
                            } else if (sort.startsWith("appointmentType")) {
                                if (eventItem1.getEventType().equals(eventItem2.getEventType())) {
                                    return 0;
                                } else if (eventItem1.getEventType().compareTo(eventItem2.getEventType()) > 0) {
                                    return descending ? 1 : -1;
                                } else {
                                    return descending ? -1 : 1;
                                }
                            } else if (sort.startsWith("contact")) {
                                if (eventItem1.getContact().equals(eventItem2.getContact())) {
                                    return 0;
                                } else if (eventItem1.getContact().compareToIgnoreCase(eventItem2.getContact())
                                        > 0) {
                                    return descending ? 1 : -1;
                                } else {
                                    return descending ? -1 : 1;
                                }
                            } else if (sort.startsWith("meetingStatus")) {
                                if (eventItem1.getMeetingStatus().equals(eventItem2.getMeetingStatus())) {
                                    return 0;
                                } else if (eventItem1
                                        .getMeetingStatus()
                                        .compareToIgnoreCase(eventItem2.getMeetingStatus())
                                        > 0) {
                                    return descending ? 1 : -1;
                                } else {
                                    return descending ? -1 : 1;
                                }
                            }

                            return 0;
                        }
                    };

            Collections.sort(eventItems, comparator);
        }
    }

    /**
     * Checks if the given userId belongs to the given IG
     */
    private boolean userInIG(String userId, String igId) {

        List<InterestGroupProfile> profiles = usersApi.getUserMembership(userId);

        for (InterestGroupProfile profile : profiles) {

            InterestGroup ig = profile.getInterestGroup();

            if (ig.getId().equals(igId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Sets each interest group of the list of events to the id of the interest group and timezone. This id is
     * returned later as part of the events.
     */
    private void adaptEventItemsWithIGIdAndTimeZone(List<EventItem> items) {

        for (EventItem item : items) {
            item.setInterestGroup(eventService.getIGRoot(item.getEventNodeRef().getId()).getId());      
            String timeZone = eventService.getAppointmentByNodeRef(item.getEventNodeRef()).getTimeZoneId();
            item.setTimeZone(timeZone);
            

        }
    }

    /**
     * Gets the nodeRef with the id of the given appointment
     */
    private NodeRef getAppointmentNodeRef(String id) {

        NodeRef appointmentNodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.exists(appointmentNodeRef)) {
            throw new IllegalArgumentException("The Appointment with id '" + id + "' does not exist.");
        }

        return appointmentNodeRef;
    }

    /**
     * Parses the json string retrieved from the client and extracts all fields.
     */
    private Appointment parseBodyJSON(String appointmentBody, boolean forUpdate) {

        if (appointmentBody == null || appointmentBody.isEmpty()) {
            throw new IllegalArgumentException(
                    "The body (appointment data) cannot be empty. It must contain the appointment data to add/update.");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Provided JSON String: " + appointmentBody);
        }

        JSONParser parser = new JSONParser();

        JSONObject json;

        try {
            json = (JSONObject) parser.parse(appointmentBody);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error when parsing the body (appointment data).", e);
        }

        Appointment appointment;

        boolean appointmentTypeEvent;

        try {
            appointmentTypeEvent = (Boolean) json.get("appointmentTypeEvent");
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "'appointmentTypeEvent' must be 'true' for an Event or 'false' for a Meeting.", e);
        }

        // discriminate between events and meetings and set each properties first
        if (appointmentTypeEvent) {

            appointment = new EventImpl();

            ((Event) appointment)
                    .setEventType(EventType.valueOf(getCheckedStringProperty(json, "eventType")));
            ((Event) appointment)
                    .setPriority(EventPriority.valueOf(getCheckedStringProperty(json, "eventPriority")));
        } else {

            appointment = new MeetingImpl();

            Object meetingPublicAvailability = json.get("meetingPublicAvailability");

            if (!(meetingPublicAvailability instanceof Boolean)) {
                throw new IllegalArgumentException(
                        "'meetingPublicAvailability' must be 'true' or 'false'.");
            }

            ((Meeting) appointment)
                    .setAvailability(
                            ((boolean) meetingPublicAvailability)
                                    ? MeetingAvailability.Public
                                    : MeetingAvailability.Private);
            ((Meeting) appointment).setOrganization((String) json.get("meetingOrganisation"));
            ((Meeting) appointment).setAgenda((String) json.get("meetingAgenda"));
            ((Meeting) appointment)
                    .setMeetingTypeString(getCheckedStringProperty(json, "meetingType"));

            String librarySectionString = (String) json.get("meetingLibrarySection");

            if (librarySectionString != null && !librarySectionString.isEmpty()) {

                // check if the node exists
                NodeRef librarySectionNodeRef;

                try {
                    librarySectionNodeRef = Converter.createNodeRefFromId(librarySectionString);
                } catch (MalformedNodeRefException e) {
                    throw new IllegalArgumentException(
                            "'meetingLibrarySection' must be a valid node reference.", e);
                }

                // check if the node is below a Library node
                NodeRef parentNodeRef = librarySectionNodeRef;

                do {
                    if (nodeService.getAspects(parentNodeRef).contains(CircabcModel.ASPECT_LIBRARY_ROOT)) {
                        break;
                    }

                    parentNodeRef = nodeService.getPrimaryParent(parentNodeRef).getParentRef();

                } while (parentNodeRef != null);

                if (parentNodeRef != null
                        && nodeService.getAspects(parentNodeRef).contains(CircabcModel.ASPECT_LIBRARY_ROOT)) {
                    if (!AccessStatus.ALLOWED.equals(
                            permissionService.hasPermission(parentNodeRef, PermissionService.READ))) {
                        throw new IllegalArgumentException(
                                "'meetingLibrarySection' error. The given user has no access to the Library.");
                    }
                    ((Meeting) appointment).setLibrarySection(librarySectionNodeRef);
                } else {
                    throw new IllegalArgumentException(
                            "'meetingLibrarySection' must be a valid node inside the Library.");
                }
            } else {
                ((Meeting) appointment).setLibrarySection(null);
            }
        }

        // set the simple properties
        appointment.setTitle(getCheckedStringProperty(json, "title"));
        appointment.setTimeZoneId(getCheckedStringProperty(json, "timezone"));
        appointment.setEventAbstract(getCheckedNullProperty(json, "abstract"));
        appointment.setInvitationMessage(getCheckedNullProperty(json, "invitationMessage"));
        appointment.setLocation(getCheckedNullProperty(json, "location"));

        // set language and prepare ML to return strings
        String language = getCheckedStringProperty(json, "language");

        Locale locale = new Locale(language);
        I18NUtil.setContentLocale(locale);
        I18NUtil.setLocale(locale);

        appointment.setLanguage(language);

        try {
            appointment.setEnableNotification((Boolean) json.get("enableNotification"));
        } catch (Exception e) {
            throw new IllegalArgumentException("'enableNotification' must be a boolean value.", e);
        }

        try {
            appointment.setUseBCC((Boolean) json.get("useBCC"));
        } catch (Exception e) {
            appointment.setUseBCC(false);
        }

        appointment.setName(getCheckedStringProperty(json, "contactName"));
        appointment.setPhone((String) json.get("contactPhone"));
        appointment.setEmail(getCheckedStringProperty(json, "contactEmail"));
        appointment.setUrl(getCheckedNullProperty(json, "contactURL"));

        // set the complex objects

        // date information
        parseDateInfo(json, appointment);

        // occurrence rate
        parseOccurrenceRate(json, appointment);

        // update existing event node instead of creation
        if (forUpdate) {

            String appointmentId = getCheckedStringProperty(json, "id");

            if (appointmentId.isEmpty()) {
                throw new IllegalArgumentException("The appointment id cannot be empty.");
            }

            NodeRef igRootRef = Converter.createNodeRefFromId(appointmentId);

            if (!nodeService.exists(igRootRef)) {
                throw new IllegalArgumentException(
                        "The appointment with id '" + appointmentId + "' does not exist.");
            }

            appointment.setId(appointmentId);
        }

        // attendants
        parseAttendantsInfo(json, appointment);

        return appointment;
    }

    /**
     * Parses the info about the event attendants (invited users and external emails)
     */
    private void parseAttendantsInfo(JSONObject json, Appointment appointment) {

        JSONObject attendantsInfo = (JSONObject) getCheckedObjectProperty(json, "attendantsInfo");

        boolean audienceStatusOpen;

        try {
            audienceStatusOpen = (Boolean) attendantsInfo.get("audienceStatusOpen");
        } catch (Exception e) {
            throw new IllegalArgumentException("'audienceStatusOpen' must be 'true' or 'false'.", e);
        }

        appointment.setAudienceStatus(audienceStatusOpen ? AudienceStatus.Open : AudienceStatus.Closed);

        if (!audienceStatusOpen) {

            Set<String> attendants = new HashSet<>();

            // add invited users or profiles
            JSONArray invitedUsersOrProfiles = (JSONArray) attendantsInfo.get("invitedUsersOrProfiles");

            for (int i = 0; invitedUsersOrProfiles != null && i < invitedUsersOrProfiles.size(); i++) {

                String authority = (String) invitedUsersOrProfiles.get(i);

                if (authority == null || authority.isEmpty()) {
                    throw new IllegalArgumentException(
                            "'audienceStatusOpen == true', but no user ids are provided.");
                }

                if (!authorityService.authorityExists(authority)) {
                    throw new IllegalArgumentException(
                            "The autority '"
                                    + authority
                                    + "' included in 'invitedUsersOrProfiles' does not exist.");
                }

                if (AuthorityType.getAuthorityType(authority).equals(AuthorityType.GROUP)) {

                    List<String> usersInProfile = apiToolBox.getUsersFromGroup(authority);

                    attendants.addAll(usersInProfile);
                } else {
                    attendants.add(authority);
                }
            }

            // add external emails
            String invitedExternalEmails =
                    getCheckedNullProperty(attendantsInfo, "invitedExternalEmails");

            StringTokenizer tokens = new StringTokenizer(invitedExternalEmails, "\n", false);

            while (tokens.hasMoreTokens()) {

                String email = tokens.nextToken().trim();

                if (email.length() > 0) {

                    String authority = userService.getUserByEmail(email);

                    if (authority == null) {
                        attendants.add(email);
                    } else {

                        if (!personService.personExists(authority)) {
                            userService.createLdapUser(authority,false);
                        }

                        attendants.add(authority);
                    }
                }
            }

            if (attendants.isEmpty()) {
                throw new IllegalArgumentException(
                        "'audienceStatusOpen == true', but no attendants are provided.");
            }

            appointment.setInvitedUsers(new ArrayList<>(attendants));
        }
    }

    /**
     * Parses the info about the date and time.
     */
    private void parseDateInfo(JSONObject json, Appointment appointment) {

        JSONObject dateInfo = (JSONObject) getCheckedObjectProperty(json, "dateInfo");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            appointment.setStartDateAsDate(
                    simpleDateFormat.parse(getCheckedStringProperty(dateInfo, "date")));
        } catch (java.text.ParseException e) {
            throw new IllegalArgumentException("'date' is not well formed. Must be yyyy-MM-dd", e);
        }

        try {
            LocalTime startTime = LocalTime.parse(getCheckedStringProperty(dateInfo, "startTime"));
            appointment.setStartTime(startTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("'startTime' is not well formed. Must be HH:mm", e);
        }

        try {
            LocalTime endTime = LocalTime.parse(getCheckedStringProperty(dateInfo, "endTime"));
            appointment.setEndTime(endTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("'endTime' is not well formed. Must be HH:mm", e);
        }

        appointment.setDateAsDate(appointment.getStartDateAsDate());
    }

    /**
     * Parse the information about the occurrence rate of the event. If it should be only once or
     * repeat, and how many times.
     */
    private void parseOccurrenceRate(JSONObject json, Appointment appointment) {

        JSONObject repeatsInfo = (JSONObject) getCheckedObjectProperty(json, "repeatsInfo");

        OccurenceRate occurenceRate = new OccurenceRate();
        MainOccurence mainOccurence =
                MainOccurence.valueOf(getCheckedStringProperty(repeatsInfo, "mainOccurence"));
        occurenceRate.setMainOccurence(mainOccurence);

        if (mainOccurence.equals(MainOccurence.EveryTimes)) {
            occurenceRate.setEveryTimesOccurence(
                    EveryTimesOccurence.valueOf(
                            getCheckedStringProperty(repeatsInfo, "everyTimesOccurence")));
            occurenceRate.setEvery(getCheckedLongProperty(repeatsInfo, "everyTime").intValue());
        } else if (mainOccurence.equals(MainOccurence.Times)) {
            occurenceRate.setTimesOccurence(
                    TimesOccurence.valueOf(getCheckedStringProperty(repeatsInfo, "timesOccurence")));
        }

        if (!mainOccurence.equals(MainOccurence.OnlyOnce)) {
            occurenceRate.setTimes(getCheckedLongProperty(repeatsInfo, "times").intValue());
        }

        if ((occurenceRate.getEvery() < 2 && occurenceRate.getEvery() > 10)
                || (occurenceRate.getTimes() < 2 && occurenceRate.getTimes() > 10)) {
            throw new IllegalArgumentException("Invalid frequency values. Must be between 2 and 10");
        }

        appointment.setOccurenceRate(occurenceRate);
    }

    /**
     * Check if the given String property exists and throws and exception otherwise
     */
    private String getCheckedStringProperty(JSONObject json, String name) {

        String value;

        try {
            value = (String) json.get(name);
        } catch (Exception e) {
            throw new IllegalArgumentException("'" + name + "' must be a string.", e);
        }

        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Property '" + name + "' is mandatory.");
        }

        return value;
    }

    /**
     * Check if the given Long property exists and throws and exception otherwise
     */
    private Long getCheckedLongProperty(JSONObject json, String name) {

        Object property = json.get(name);

        if (property == null) {
            throw new IllegalArgumentException("Property '" + name + "' is mandatory.");
        }

        long value;

        try {
            value = (long) json.get(name);
        } catch (Exception e) {
            throw new IllegalArgumentException("'" + name + "' must be an integer.", e);
        }

        return value;
    }

    /**
     * Check if the given Object property exists and throws and exception otherwise
     */
    private Object getCheckedObjectProperty(JSONObject json, String name) {

        Object value = json.get(name);

        if (value == null) {
            throw new IllegalArgumentException("Property '" + name + "' is mandatory.");
        }

        return value;
    }

    /**
     * Get a String property from the json by name, and return the empty String if the property is
     * null
     */
    private String getCheckedNullProperty(JSONObject json, String name) {

        String value = (String) json.get(name);

        return value == null ? "" : value;
    }

    /**
     * @param eventService the eventService to set
     */
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    /**
     * @param apiToolBox the apiToolBox to set
     */
    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }
}
