package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceClosed;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceOpen;
import eu.cec.digit.circabc.migration.entities.generated.properties.Availability;
import eu.cec.digit.circabc.migration.entities.generated.properties.ContactInformation;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventPriority;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventType;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurenceType;
import eu.cec.digit.circabc.migration.entities.generated.properties.MeetingType;
import eu.cec.digit.circabc.migration.entities.generated.properties.SingleDate;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimeZone;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurenceType;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.EventModel;
import eu.cec.digit.circabc.service.event.MainOccurence;
import eu.cec.digit.circabc.service.event.OccurenceRate;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * CalendarReader implementation that reads events/meetings from the CIRCABC Alfresco repository.
 * Reads event/meeting definitions directly under the Events service root and maps
 * their properties to the migration Appointment model.
 */
public class AlfrescoCalendarReader implements CalendarReader {

    private static final Log logger = LogFactory.getLog(AlfrescoCalendarReader.class);

    private NodeService nodeService;

    @Override
    public List<Appointment> getAllAppointments(final Events events) throws ExportationException {
        final String path = ElementsHelper.getExportationPath(events);
        if (path == null) {
            return Collections.emptyList();
        }
        try {
            final NodeRef eventsRef = new NodeRef(path);
            if (!nodeService.exists(eventsRef)) {
                return Collections.emptyList();
            }

            final List<ChildAssociationRef> children = nodeService.getChildAssocs(eventsRef);
            final List<Appointment> appointments = new ArrayList<>();

            if (logger.isDebugEnabled()) {
                logger.debug("Events root " + path + " has " + children.size() + " child associations");
            }

            for (final ChildAssociationRef child : children) {
                final NodeRef childRef = child.getChildRef();
                final QName type = nodeService.getType(childRef);

                if (logger.isDebugEnabled()) {
                    logger.debug("  Child: " + childRef + " type=" + type);
                }

                if (EventModel.TYPE_EVENT_DEFINITION.equals(type)) {
                    final Event event = buildEvent(childRef);
                    if (event != null) {
                        appointments.add(event);
                    }
                } else if (EventModel.TYPE_EVENT_MEETING_DEFINITION.equals(type)) {
                    final Meeting meeting = buildMeeting(childRef);
                    if (meeting != null) {
                        appointments.add(meeting);
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug(appointments.size() + " appointments exported from " + path);
            }
            return appointments;
        } catch (Exception e) {
            throw new ExportationException("Error reading events from " + path, e);
        }
    }

    private Event buildEvent(final NodeRef definitionRef) {
        try {
            final Map<QName, Serializable> props = nodeService.getProperties(definitionRef);
            final Event event = new Event();

            // Pre-set required fields with defaults before mapping
            event.setStartTime(getDefaultTime());
            event.setEndTime(getDefaultTime());
            event.setStartDate(new Date());
            event.setSingleDate(new SingleDate());

            mapCommonProperties(event, props);

            // Event-specific properties
            final Serializable eventType = props.get(EventModel.PROP_EVENT_TYPE);
            if (eventType != null) {
                try {
                    event.setType(EventType.fromValue(eventType.toString()));
                } catch (Exception e) {
                    event.setType(EventType.TASK);
                }
            }

            final Serializable priority = props.get(EventModel.PROP_EVENT_PRIORITY);
            if (priority != null) {
                try {
                    event.setPriority(EventPriority.fromValue(priority.toString()));
                } catch (Exception e) {
                    event.setPriority(EventPriority.LOW);
                }
            }

            return event;
        } catch (Exception e) {
            logger.warn("Error building event from " + definitionRef, e);
            return null;
        }
    }

    private Meeting buildMeeting(final NodeRef definitionRef) {
        try {
            final Map<QName, Serializable> props = nodeService.getProperties(definitionRef);
            final Meeting meeting = new Meeting();

            // Pre-set required fields with defaults before mapping
            meeting.setStartTime(getDefaultTime());
            meeting.setEndTime(getDefaultTime());
            meeting.setStartDate(new Date());
            meeting.setSingleDate(new SingleDate());

            mapCommonProperties(meeting, props);

            // Meeting-specific properties
            final Serializable meetingType = props.get(EventModel.PROP_MEETING_TYPE);
            if (meetingType != null) {
                try {
                    meeting.setType(MeetingType.fromValue(meetingType.toString()));
                } catch (Exception e) {
                    meeting.setType(MeetingType.FACE_TO_FACE);
                }
            }

            final Serializable availability = props.get(EventModel.PROP_MEETING_AVAILABILITY);
            if (availability != null) {
                try {
                    meeting.setAvailability(Availability.fromValue(availability.toString()));
                } catch (Exception e) {
                    meeting.setAvailability(Availability.PRIVATE);
                }
            }

            final Serializable organization = props.get(EventModel.PROP_MEETING_ORGAINZATION);
            if (organization != null) {
                meeting.setOrganization(organization.toString());
            }

            final Serializable agenda = props.get(EventModel.PROP_MEETING_AGENDA);
            if (agenda != null) {
                meeting.setAgenda(agenda.toString());
            }

            final Serializable librarySection = props.get(EventModel.PROP_MEETING_LIBRARY_SECTION);
            if (librarySection != null && !librarySection.toString().isEmpty()) {
                final String libSectionStr = librarySection.toString();
                if (libSectionStr.startsWith("workspace://") || libSectionStr.startsWith("archive://")) {
                    // Resolve nodeRef to CIRCABC path
                    try {
                        final NodeRef libSectionRef = new NodeRef(libSectionStr);
                        if (nodeService.exists(libSectionRef)) {
                            final String circabcPath = buildCircabcPath(libSectionRef);
                            if (circabcPath != null) {
                                meeting.setLibrarySection(circabcPath);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Could not resolve librarySection nodeRef: " + libSectionStr, e);
                    }
                } else {
                    // Already a path
                    meeting.setLibrarySection(libSectionStr);
                }
            }

            return meeting;
        } catch (Exception e) {
            logger.warn("Error building meeting from " + definitionRef, e);
            return null;
        }
    }

    private void mapCommonProperties(final Appointment appointment, final Map<QName, Serializable> props) {
        // Title
        final Serializable title = props.get(EventModel.PROP_EVENT_TITLE);
        if (title != null) {
            appointment.setAppointmentTitle(title.toString());
        }

        // Language
        final Serializable language = props.get(EventModel.PROP_EVENT_LANGUAGE);
        if (language != null) {
            appointment.setLanguage(new Locale(language.toString()));
        }

        // Abstract
        final Serializable eventAbstract = props.get(EventModel.PROP_EVENT_ABSTRACT);
        if (eventAbstract != null) {
            appointment.setAbstract(eventAbstract.toString());
        }

        // Date
        final Serializable date = props.get(EventModel.PROP_EVENT_DATE);
        if (date instanceof Date) {
            appointment.setDate((Date) date);
        }

        // Start date (required by schema)
        final Serializable startDate = props.get(EventModel.PROP_EVENT_START_DATE);
        if (startDate instanceof Date) {
            appointment.setStartDate((Date) startDate);
        } else {
            // startDate is required - use date or current date as fallback
            appointment.setStartDate(appointment.getDate() != null ? appointment.getDate() : new Date());
        }

        // Start time (stored as String "HH:mm" or "HH:mm:ss.SSS" in Alfresco)
        final Serializable startTime = props.get(EventModel.PROP_EVENT_START_TIME);
        if (logger.isDebugEnabled()) {
            logger.debug("  PROP_EVENT_START_TIME raw value: " + startTime + " (class: " + (startTime != null ? startTime.getClass().getName() : "null") + ")");
        }
        if (startTime != null && startTime.toString().trim().length() > 0) {
            final Date parsedStart = parseTime(startTime.toString());
            if (parsedStart != null) {
                appointment.setStartTime(parsedStart);
            }
        }

        // End time
        final Serializable endTime = props.get(EventModel.PROP_EVENT_END_TIME);
        if (logger.isDebugEnabled()) {
            logger.debug("  PROP_EVENT_END_TIME raw value: " + endTime + " (class: " + (endTime != null ? endTime.getClass().getName() : "null") + ")");
        }
        if (endTime != null && endTime.toString().trim().length() > 0) {
            final Date parsedEnd = parseTime(endTime.toString());
            if (parsedEnd != null) {
                appointment.setEndTime(parsedEnd);
            }
        }

        // Timezone
        final Serializable timezone = props.get(EventModel.PROP_EVENT_TIMEZONE);
        if (timezone != null) {
            try {
                appointment.setTimeZoneId(TimeZone.fromValue(timezone.toString()));
            } catch (Exception e) {
                appointment.setTimeZoneId(TimeZone.VALUE_13);
            }
        }

        // Location
        final Serializable location = props.get(EventModel.PROP_EVENT_LOCATION);
        if (location != null) {
            appointment.setLocation(location.toString());
        }

        // Occurrence rate (choice: singleDate OR timesOccurence OR everyTimesOccurence)
        // Exactly one must be set per schema - required element
        final Serializable occurenceRate = props.get(EventModel.PROP_EVENT_OCCURENCE_RATE);
        mapOccurrenceRate(appointment, occurenceRate);

        // Audience
        final Serializable audience = props.get(EventModel.PROP_EVENT_AUDIENCE);
        if (audience != null) {
            final String audienceStr = audience.toString();
            if ("Open".equalsIgnoreCase(audienceStr)) {
                appointment.setAudienceOpen(new AudienceOpen());
            } else if ("Closed".equalsIgnoreCase(audienceStr) || "Close".equalsIgnoreCase(audienceStr)) {
                final AudienceClosed closed = new AudienceClosed();
                // Get invited users
                final Serializable invitedUsers = props.get(EventModel.PROP_EVENT_INVITED_USERS);
                if (invitedUsers != null) {
                    final String usersStr = invitedUsers.toString();
                    if (usersStr.length() > 2) {
                        // Format: |user1|user2|user3|
                        final String[] users = usersStr.split("\\|");
                        for (String user : users) {
                            if (user != null && !user.trim().isEmpty()) {
                                closed.getInvitedUsers().add(user.trim());
                            }
                        }
                    }
                }
                appointment.setAudienceClosed(closed);
            }
        }

        // Contact information
        final Serializable email = props.get(EventModel.PROP_EVENT_EMAIL);
        final Serializable phone = props.get(EventModel.PROP_EVENT_PHONE);
        final Serializable url = props.get(EventModel.PROP_EVENT_URL);
        final Serializable contactName = props.get(EventModel.PROP_EVENT_NAME);
        final ContactInformation contact = new ContactInformation();
        contact.setName(contactName != null ? contactName.toString() : "");
        contact.setEmail(email != null ? email.toString() : "");
        contact.setPhone(phone != null ? phone.toString() : "");
        if (url != null) { contact.setUrl(url.toString()); }
        appointment.setContact(contact);
    }

    /**
     * Maps the CIRCABC occurrence rate to the migration schema's occurrence choice.
     *
     * <p>In CIRCABC the occurrence rate is persisted (via {@link OccurenceRate#toString()})
     * as a pipe-delimited string:
     * {@code mainOccurence|timesOccurence|everyTimesOccurence|times|every}
     * (e.g. {@code OnlyOnce|null|null|-1|-1}, {@code Times|Weekly|null|5|-1},
     * {@code EveryTimes|null|weeks|10|2}).
     *
     * <p>The {@code appointmentProperties} group in PropertiesSchema.xsd requires exactly one
     * of {@code singleDate}, {@code timesOccurence} or {@code everyTimesOccurence}. This method
     * sets the matching one so that recurring events are exported with their recurrence intact
     * instead of being flattened to a single date. Anything that cannot be mapped falls back to
     * {@code singleDate} so the export stays schema-valid.
     */
    private void mapOccurrenceRate(final Appointment appointment, final Serializable occurenceRate) {
        // The occurrence is an xs:choice: exactly one of singleDate / timesOccurence /
        // everyTimesOccurence may be present. buildEvent()/buildMeeting() pre-set a default
        // singleDate, so clear all three first and then set exactly one below.
        appointment.setSingleDate(null);
        appointment.setTimesOccurence(null);
        appointment.setEveryTimesOccurence(null);

        if (occurenceRate == null || occurenceRate.toString().trim().isEmpty()) {
            appointment.setSingleDate(new SingleDate());
            return;
        }

        final OccurenceRate rate;
        try {
            rate = new OccurenceRate(occurenceRate.toString());
        } catch (Exception e) {
            logger.warn("Could not parse occurrence rate '" + occurenceRate
                    + "', exporting as single date", e);
            appointment.setSingleDate(new SingleDate());
            return;
        }

        final MainOccurence main = rate.getMainOccurence();
        try {
            if (main == MainOccurence.Times && rate.getTimesOccurence() != null
                    && isValidForTimes(rate.getTimes())) {
                final TimesOccurence times = new TimesOccurence();
                // Source and target enums share identical string values.
                times.setType(TimesOccurenceType.fromValue(rate.getTimesOccurence().name()));
                times.setForTimes(rate.getTimes());
                appointment.setTimesOccurence(times);
                return;
            } else if (main == MainOccurence.EveryTimes && rate.getEveryTimesOccurence() != null
                    && isValidForTimes(rate.getTimes()) && rate.getEvery() >= 1) {
                final EveryTimesOccurence every = new EveryTimesOccurence();
                every.setEvery(rate.getEvery());
                every.setType(EveryTimesOccurenceType.fromValue(rate.getEveryTimesOccurence().name()));
                every.setForTimes(rate.getTimes());
                appointment.setEveryTimesOccurence(every);
                return;
            }
        } catch (Exception e) {
            logger.warn("Could not map occurrence rate '" + occurenceRate
                    + "', exporting as single date", e);
        }

        // OnlyOnce, or anything that could not be safely mapped.
        appointment.setSingleDate(new SingleDate());
    }

    /**
     * The schema constrains {@code forTimes} to the inclusive range [1, 52]
     * (see timesOccurence/everyTimesOccurence in PropertiesSchema.xsd).
     */
    private boolean isValidForTimes(final int times) {
        return times >= 1 && times <= 52;
    }

    /**
     * Parse a time string (HH:mm or HH:mm:ss or HH:mm:ss.SSS) into a Date with only time component.
     */
    private Date parseTime(final String timeStr) {
        try {
            final String cleaned = timeStr.trim();
            final String[] parts = cleaned.split("[:.]");
            final java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.YEAR, 1970);
            cal.set(java.util.Calendar.MONTH, 0);
            cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            cal.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
            cal.set(java.util.Calendar.MINUTE, parts.length > 1 ? Integer.parseInt(parts[1]) : 0);
            cal.set(java.util.Calendar.SECOND, parts.length > 2 ? Integer.parseInt(parts[2]) : 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            return cal.getTime();
        } catch (Exception e) {
            logger.warn("Failed to parse time: '" + timeStr + "', using default", e);
            return null;
        }
    }

    /**
     * Returns a default time (00:00:00) as a Date object. Never returns null.
     */
    private Date getDefaultTime() {
        final java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, 1970);
        cal.set(java.util.Calendar.MONTH, 0);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Build the CIRCABC path for a node by walking up the parent chain.
     * Returns a path like /Circabc/CategoryHeader/Category/IG/Service/Folder/...
     * The "Circabc" prefix is the virtual root used by the import tree (class simple name).
     * The node with ASPECT_CIRCABC_ROOT is the CategoryHeader (e.g., "CircaBC").
     */
    private String buildCircabcPath(final NodeRef nodeRef) {
        final List<String> parts = new ArrayList<>();
        NodeRef current = nodeRef;

        // Walk up to the CIRCABC root (max 20 levels to prevent infinite loop)
        for (int i = 0; i < 20 && current != null; i++) {
            final String name = (String) nodeService.getProperty(current, ContentModel.PROP_NAME);
            if (name == null) break;

            parts.add(0, name);

            // Stop at the node with CIRCABC root aspect (CategoryHeader level)
            // and prepend "Circabc" as the virtual root above it
            if (nodeService.hasAspect(current, CircabcModel.ASPECT_CIRCABC_ROOT)) {
                parts.add(0, "Circabc");
                break;
            }

            final org.alfresco.service.cmr.repository.ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(current);
            if (parentAssoc == null) break;
            current = parentAssoc.getParentRef();
        }

        if (parts.isEmpty() || !"Circabc".equals(parts.get(0))) {
            // Couldn't resolve to a valid CIRCABC path
            return null;
        }

        return "/" + String.join("/", parts);
    }

    // --- Setters for Spring injection ---

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
