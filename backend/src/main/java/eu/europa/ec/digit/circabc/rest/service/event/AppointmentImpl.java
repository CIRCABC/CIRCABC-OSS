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

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateValue;
import io.swagger.model.Appointment;
import io.swagger.model.AppointmentType;
import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.AudienceStatus;
import io.swagger.model.Meeting;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.OccurenceRate;
import io.swagger.model.alfresco.EventModel;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;

/**
 * Default implementation of the {@link Appointment} domain model used by the CIRCABC event/calendar
 * service.
 *
 * <p>An appointment carries the descriptive data of a calendar event (title, dates, times, timezone,
 * location, contact details, audience and recurrence). This class provides the bridge between the
 * Alfresco content model and the domain model:
 *
 * <ul>
 *   <li>{@link #init(Map)} populates the appointment from a map of node properties read from the
 *       Alfresco repository.
 *   <li>{@link #getProperties()} and {@link #getProperties(AppointmentUpdateInfo)} convert the
 *       appointment back into an Alfresco {@link PropertyMap} for persistence.
 *   <li>{@link #getEventDates()} and {@link #getEventDatesProperties(AppointmentType)} expand the
 *       recurrence rate into the individual occurrence dates (using an iCalendar RRULE).
 * </ul>
 *
 * <p>This is a mutable data holder and is not thread-safe.
 */
public class AppointmentImpl implements Appointment {

  /** iCalendar RRULE fragment used to append the occurrence count (";COUNT="). */
  private static final String COUNT = ";COUNT=";
  /** Maximum number of occurrence dates generated for a recurring appointment. */
  public static final int LIMIT_OCCURENCE_GENERATION = 52;
  /** Delimiter used to serialize the list of invited users into a single string property. */
  public static final String SEPARATOR = "|";
  /** Invited users mapped to their meeting request status; populated by {@link #init(Map)}. */
  protected HashMap<String, MeetingRequestStatus> audience;
  private DateValue startDate;
  private DateValue date;
  private String email;
  private AudienceStatus audienceStatus;
  private LocalTime endTime;
  private List<String> invitedUsers = new ArrayList<>();
  private String invitationMessage;
  private String language;
  private String location;
  private String name;
  private OccurenceRate occurenceRate;
  private String phone;
  private LocalTime startTime;
  private String timeZoneId;
  private String title;
  private String url;
  private String eventAbstract;
  private Boolean enableNotification = false;
  private Boolean useBCC = false;
  private String id;
  private String rrule;

  /**
   * @return the audience status (Open or Closed) controlling who may attend the appointment
   */
  public AudienceStatus getAudienceStatus() {
    return audienceStatus;
  }

  /**
   * @param value the audience status to set
   */
  public void setAudienceStatus(AudienceStatus value) {
    audienceStatus = value;
  }

  /**
   * @return the occurrence date of this appointment instance
   */
  public DateValue getDate() {
    return date;
  }

  /**
   * @param value the occurrence date to set
   */
  public void setDate(DateValue value) {
    date = value;
  }

  /**
   * @return the occurrence date converted to a {@link Date}, or {@code null} if unset
   */
  public Date getDateAsDate() {
    return AppointmentUtils.convertDateValueToDate(date);
  }

  /**
   * @param value the occurrence date, as a {@link Date}, to set
   */
  public void setDateAsDate(Date value) {
    date = AppointmentUtils.convertDateToDateValue(value);
  }

  /**
   * @return the start date of the (possibly recurring) appointment
   */
  public DateValue getStartDate() {
    return startDate;
  }

  /**
   * @param value the start date to set
   */
  public void setStartDate(DateValue value) {
    startDate = value;
  }

  /**
   * @return the start date converted to a {@link Date}, or {@code null} if unset
   */
  public Date getStartDateAsDate() {
    return AppointmentUtils.convertDateValueToDate(startDate);
  }

  /**
   * @param value the start date, as a {@link Date}, to set
   */
  public void setStartDateAsDate(Date value) {
    startDate = AppointmentUtils.convertDateToDateValue(value);
  }

  /**
   * @return the organiser contact email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param value the organiser contact email address to set
   */
  public void setEmail(String value) {
    email = value;
  }

  /**
   * @return the appointment end time (time-of-day, without a date)
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * @param value the appointment end time to set
   */
  public void setEndTime(LocalTime value) {
    endTime = value;
  }

  /**
   * @return the end time as a {@link Date}, combined with the occurrence date when available,
   *     otherwise the time alone
   */
  public Date getEndTimeAsDate() {
    if (date != null) {
      return AppointmentUtils.convertLocalTimeDateValueToDate(endTime, date);
    } else {
      return AppointmentUtils.convertLocalTimeToDate(endTime);
    }
  }

  /**
   * @param value the end time, as a {@link Date}, to set
   */
  public void setEndTimeAsDate(Date value) {
    endTime = AppointmentUtils.convertDateToLocalTime(value);
  }

  /**
   * @return the message sent to invited users
   */
  public String getInvitationMessage() {
    return invitationMessage;
  }

  /**
   * @param value the invitation message to set
   */
  public void setInvitationMessage(String value) {
    invitationMessage = value;
  }

  /**
   * @return the language of the appointment
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param value the language to set
   */
  public void setLanguage(String value) {
    language = value;
  }

  /**
   * @return the location of the appointment
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param value the location to set
   */
  public void setLocation(String value) {
    location = value;
  }

  /**
   * @return the name of the appointment
   */
  public String getName() {
    return name;
  }

  /**
   * @param value the name to set
   */
  public void setName(String value) {
    name = value;
  }

  /**
   * @return the occurrence rate describing how the appointment repeats
   */
  public OccurenceRate getOccurenceRate() {
    return occurenceRate;
  }

  /**
   * @param value the occurrence rate to set
   */
  public void setOccurenceRate(OccurenceRate value) {
    occurenceRate = value;
  }

  /**
   * @return the organiser contact phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * @param value the organiser contact phone number to set
   */
  public void setPhone(String value) {
    phone = value;
  }

  /**
   * @return the appointment start time (time-of-day, without a date)
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * @param value the appointment start time to set
   */
  public void setStartTime(LocalTime value) {
    startTime = value;
  }

  /**
   * @return the start time as a {@link Date}, combined with the occurrence date when available,
   *     otherwise the time alone
   */
  public Date getStartTimeAsDate() {
    if (date != null) {
      return AppointmentUtils.convertLocalTimeDateValueToDate(startTime, date);
    } else {
      return AppointmentUtils.convertLocalTimeToDate(startTime);
    }
  }

  /**
   * @param value the start time, as a {@link Date}, to set
   */
  public void setStartTimeAsDate(Date value) {
    startTime = AppointmentUtils.convertDateToLocalTime(value);
  }

  /**
   * @return the identifier of the timezone in which the times are expressed
   */
  public String getTimeZoneId() {
    return timeZoneId;
  }

  /**
   * @param value the timezone identifier to set
   */
  public void setTimeZoneId(String value) {
    timeZoneId = value;
  }

  /**
   * @return the title of the appointment
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param value the title to set
   */
  public void setTitle(String value) {
    title = value;
  }

  /**
   * @return the URL associated with the appointment
   */
  public String getUrl() {
    return url;
  }

  /**
   * @param value the URL to set
   */
  public void setUrl(String value) {
    url = value;
  }

  /**
   * @return the abstract/summary text describing the appointment
   */
  public String getEventAbstract() {
    return eventAbstract;
  }

  /**
   * @param value the abstract/summary text to set
   */
  public void setEventAbstract(String value) {
    eventAbstract = value;
  }

  /**
   * @return the list of invited user identifiers
   */
  public List<String> getInvitedUsers() {
    return invitedUsers;
  }

  /**
   * @param value the list of invited user identifiers to set
   */
  public void setInvitedUsers(List<String> value) {
    invitedUsers = value;
  }

  /**
   * Populates this appointment from a map of Alfresco node properties.
   *
   * <p>Every property is read defensively: several event/meeting properties are optional at the
   * content-model level (for example {@code ce:invitationMessage}), so a single missing optional
   * property must not fail the whole call with a {@link NullPointerException}. When the audience is
   * {@link AudienceStatus#Closed}, the serialized invited-users property is parsed and each user is
   * added to the {@link #audience} map with a default status (Pending for meetings, otherwise
   * NotApplicable).
   *
   * @param properties the node properties, keyed by {@link QName}, read from the repository
   */
  public void init(Map<QName, Serializable> properties) {
    // Read every property defensively. Several event/meeting properties are
    // optional at the content-model level (e.g. ce:invitationMessage, which the
    // migration/import format does not carry and which is not written when null
    // via getProperties()). The list endpoint loads the full appointment just to
    // read the timezone, so a single missing optional property must not blow up
    // the whole call with a NullPointerException.
    this.setTitle(stringOrEmpty(properties.get(EventModel.PROP_EVENT_TITLE)));

    Serializable audienceProperty = properties.get(
      EventModel.PROP_EVENT_AUDIENCE
    );
    this.setAudienceStatus(
      audienceProperty != null
        ? AudienceStatus.valueOf(audienceProperty.toString())
        : AudienceStatus.Open
    );

    Serializable dateProperty = properties.get(EventModel.PROP_EVENT_DATE);
    if (dateProperty != null) {
      this.setDate(
        AppointmentUtils.convertDateToDateValue((Date) dateProperty)
      );
    }
    Serializable startDateProperty = properties.get(
      EventModel.PROP_EVENT_START_DATE
    );
    if (startDateProperty != null) {
      this.setStartDate(
        AppointmentUtils.convertDateToDateValue((Date) startDateProperty)
      );
    }

    Serializable startTimeProperty = properties.get(
      EventModel.PROP_EVENT_START_TIME
    );
    if (startTimeProperty != null) {
      this.setStartTime(new LocalTime(startTimeProperty.toString()));
    }
    Serializable endTimeProperty = properties.get(
      EventModel.PROP_EVENT_END_TIME
    );
    if (endTimeProperty != null) {
      this.setEndTime(new LocalTime(endTimeProperty.toString()));
    }
    this.setEventAbstract(
      stringOrEmpty(properties.get(EventModel.PROP_EVENT_ABSTRACT))
    );

    this.setEmail(stringOrEmpty(properties.get(EventModel.PROP_EVENT_EMAIL)));
    this.setInvitationMessage(
      stringOrEmpty(properties.get(EventModel.PROP_EVENT_INVITATION_MESSAGE))
    );
    this.setLanguage(
      stringOrEmpty(properties.get(EventModel.PROP_EVENT_LANGUAGE))
    );
    this.setLocation(
      stringOrEmpty(properties.get(EventModel.PROP_EVENT_LOCATION))
    );
    this.setName(stringOrEmpty(properties.get(EventModel.PROP_EVENT_NAME)));

    this.setPhone(stringOrEmpty(properties.get(EventModel.PROP_EVENT_PHONE)));
    this.setUrl(stringOrEmpty(properties.get(EventModel.PROP_EVENT_URL)));
    this.setTimeZoneId(
      stringOrEmpty(properties.get(EventModel.PROP_EVENT_TIMEZONE))
    );
    Serializable occurenceRateProperty = properties.get(
      EventModel.PROP_EVENT_OCCURENCE_RATE
    );
    if (occurenceRateProperty != null) {
      this.setOccurenceRate(
        new OccurenceRate(occurenceRateProperty.toString())
      );
    }
    this.audience = new HashMap<>();
    final MeetingRequestStatus defaultSatus = (this instanceof Meeting)
      ? MeetingRequestStatus.Pending
      : MeetingRequestStatus.NotApplicable;
    if (this.getAudienceStatus() == AudienceStatus.Closed) {
      Serializable invitedUsersProperty = properties.get(
        EventModel.PROP_EVENT_INVITED_USERS
      );
      String invitedUsersList =
        invitedUsersProperty != null ? invitedUsersProperty.toString() : "";
      if (invitedUsersList.length() > 1) {
        invitedUsersList = invitedUsersList.substring(
          1,
          invitedUsersList.length() - 1
        );
        String[] elements = invitedUsersList.split("\\" + SEPARATOR);
        this.setInvitedUsers(Arrays.asList(elements));
        for (String user : this.getInvitedUsers()) {
          audience.put(user, defaultSatus);
        }
      }
    }
  }

  /**
   * Returns the string form of a node property, or an empty string when the
   * property is absent. Guards the read path against optional properties that
   * were never persisted (e.g. imported appointments without an invitation
   * message), which would otherwise cause a NullPointerException.
   */
  private static String stringOrEmpty(Serializable value) {
    return value == null ? "" : value.toString();
  }

  /**
   * Converts the full appointment into an Alfresco {@link PropertyMap} for persistence. Only
   * non-null values are included.
   *
   * @return a property map containing all populated appointment fields
   */
  public PropertyMap getProperties() {
    PropertyMap properties = new PropertyMap(15);
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_TITLE,
      this.getTitle()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_ABSTRACT,
      this.getEventAbstract()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_EMAIL,
      this.getEmail()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_INVITATION_MESSAGE,
      this.getInvitationMessage()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_LANGUAGE,
      this.getLanguage()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_LOCATION,
      this.getLocation()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_NAME,
      this.getName()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_PHONE,
      this.getPhone()
    );
    addPropertyIfNotNull(properties, EventModel.PROP_EVENT_URL, this.getUrl());
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_TIMEZONE,
      this.getTimeZoneId()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_INVITED_USERS,
      this.getInvitedUsersList()
    );

    if (this.getAudienceStatus() != null) {
      properties.put(
        EventModel.PROP_EVENT_AUDIENCE,
        this.getAudienceStatus().toString()
      );
    }
    if (this.getStartDate() != null) {
      properties.put(
        EventModel.PROP_EVENT_START_DATE,
        AppointmentUtils.convertDateValueToDate(this.getStartDate())
      );
    }
    if (this.getStartTime() != null) {
      properties.put(
        EventModel.PROP_EVENT_START_TIME,
        this.getStartTime().toString()
      );
    }
    if (this.getEndTime() != null) {
      properties.put(
        EventModel.PROP_EVENT_END_TIME,
        this.getEndTime().toString()
      );
    }
    if (this.getOccurenceRate() != null) {
      properties.put(
        EventModel.PROP_EVENT_OCCURENCE_RATE,
        this.getOccurenceRate().toString()
      );
    }
    return properties;
  }

  private void addPropertyIfNotNull(
    PropertyMap properties,
    QName key,
    Object value
  ) {
    if (value != null) {
      properties.put(key, (java.io.Serializable) value);
    }
  }

  /**
   * Expands the appointment's recurrence rate into one {@link PropertyMap} per occurrence date, each
   * describing a single concrete event to be created in the repository.
   *
   * <p>When the audience is {@link AudienceStatus#Open} the invited-users list is replaced with an
   * empty marker so that no specific users are recorded.
   *
   * @param appointmentType the kind of event to stamp on each generated occurrence
   * @return a list of property maps, one for each generated occurrence date
   */
  public List<PropertyMap> getEventDatesProperties(
    AppointmentType appointmentType
  ) {
    final List<DateValue> eventDates = getEventDates();
    final List<PropertyMap> listOfProperties = new ArrayList<>(
      eventDates.size()
    );
    String invitedUserList = this.getInvitedUsersList();
    if (this.audienceStatus == AudienceStatus.Open) {
      invitedUserList = SEPARATOR + SEPARATOR;
    }

    for (DateValue value : eventDates) {
      PropertyMap properties = new PropertyMap(6);
      Date javaDate = AppointmentUtils.convertLocalTimeDateValueToDate(
        this.startTime,
        value
      );
      properties.put(EventModel.PROP_EVENT_DATE, javaDate);
      properties.put(EventModel.PROP_EVENT_INVITED_USERS, invitedUserList);
      properties.put(EventModel.PROP_KIND_OF_EVENT, appointmentType.toString());
      properties.put(
        EventModel.PROP_EVENT_START_TIME,
        this.startTime.toString()
      );
      properties.put(EventModel.PROP_EVENT_END_TIME, this.endTime.toString());
      properties.put(EventModel.PROP_EVENT_TITLE, this.title);
      properties.put(EventModel.PROP_EVENT_NAME, this.name);
      properties.put(EventModel.PROP_EVENT_AUDIENCE, this.audienceStatus);
      listOfProperties.add(properties);
    }
    return listOfProperties;
  }

  /**
   * Computes the concrete occurrence dates for this appointment by building an iCalendar RRULE from
   * the occurrence rate and iterating it, capped at {@link #LIMIT_OCCURENCE_GENERATION} entries.
   *
   * @return the list of occurrence dates
   * @throws AlfrescoRuntimeException if the generated RRULE cannot be parsed
   */
  public List<DateValue> getEventDates() {
    rrule = getRRuleString();
    List<DateValue> result = new ArrayList<>();
    int limit = LIMIT_OCCURENCE_GENERATION;
    try {
      RecurrenceIterator ri =
        RecurrenceIteratorFactory.createRecurrenceIterator(
          rrule,
          startDate,
          TimeZone.getTimeZone(timeZoneId)
        );

      while (ri.hasNext() && --limit >= 0) {
        result.add(ri.next());
      }
    } catch (ParseException e) {
      throw new AlfrescoRuntimeException("Unable to parse rrule: " + rrule);
    }
    return result;
  }

  private String getRRuleString() {
    if (this.occurenceRate == null) {
      throw new AlfrescoRuntimeException("Occurence rate should not be null");
    }
    if (this.timeZoneId == null) {
      throw new AlfrescoRuntimeException("Timezone id should not be null");
    }
    if (this.startDate == null) {
      throw new AlfrescoRuntimeException("Start date should not be null");
    }

    DateTime d = new DateTime();
    d = d.withDate(startDate.year(), startDate.month(), startDate.day());
    int dayOfWeek = d.get(DateTimeFieldType.dayOfWeek());
    int dayOfMonth = d.get(DateTimeFieldType.dayOfMonth());
    String result = "";
    switch (occurenceRate.getMainOccurence()) {
      case OnlyOnce:
        result = "RRULE:FREQ=DAILY;COUNT=1";
        break;
      case Times:
        switch (occurenceRate.getTimesOccurence()) {
          case Daily:
            result = "RRULE:FREQ=DAILY;COUNT=" + occurenceRate.getTimes();
            break;
          case Weekly:
            result = "RRULE:FREQ=WEEKLY;COUNT=" + occurenceRate.getTimes();
            break;
          case EveryTwoWeeks:
            result =
              "RRULE:FREQ=DAILY;INTERVAL=14;COUNT=" + occurenceRate.getTimes();
            break;
          case MondayToFriday:
            result =
              "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;COUNT=" +
              occurenceRate.getTimes();
            break;
          case MondayWednseyFriday:
            result =
              "RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR;COUNT=" +
              occurenceRate.getTimes();
            break;
          case TuesdayThursday:
            result =
              "RRULE:FREQ=WEEKLY;BYDAY=TU,TH;COUNT=" + occurenceRate.getTimes();
            break;
          case MonthlyByDate:
            result =
              "RRULE:FREQ=MONTHLY;COUNT=" +
              occurenceRate.getTimes() +
              ";BYMONTHDAY=" +
              dayOfMonth;
            break;
          case MonthlyByWeekday:
            result =
              "RRULE:FREQ=MONTHLY;COUNT=" +
              occurenceRate.getTimes() +
              ";BYDAY=1" +
              getWeekDay2Letters(dayOfWeek);
            break;
          case Yearly:
            result = "RRULE:FREQ=YEARLY;COUNT=" + occurenceRate.getTimes();
            break;
          default:
            break;
        }

        break;
      case EveryTimes:
        switch (occurenceRate.getEveryTimesOccurence()) {
          case days:
            result =
              "RRULE:FREQ=DAILY;INTERVAL=" +
              occurenceRate.getEvery() +
              COUNT +
              occurenceRate.getTimes();
            break;
          case weeks:
            result =
              "RRULE:FREQ=WEEKLY;INTERVAL=" +
              occurenceRate.getEvery() +
              COUNT +
              occurenceRate.getTimes();
            break;
          case months:
            result =
              "RRULE:FREQ=MONTHLY;INTERVAL=" +
              occurenceRate.getEvery() +
              COUNT +
              occurenceRate.getTimes();
            break;
          default:
            break;
        }

        break;
      default:
        break;
    }
    return result;
  }

  private String getWeekDay2Letters(int dayOfWeek) {
    String result = null;
    switch (dayOfWeek) {
      case 1:
        result = "MO";
        break;
      case 2:
        result = "TU";
        break;
      case 3:
        result = "WE";
        break;
      case 4:
        result = "TH";
        break;
      case 5:
        result = "FR";
        break;
      case 6:
        result = "SA";
        break;
      case 7:
        result = "SU";
        break;
      default:
        break;
    }
    return result;
  }

  /**
   * Serializes the invited users into a single {@link #SEPARATOR}-delimited string, with a leading
   * and trailing separator (for example {@code |userA|userB|}). Returns just the separator when no
   * users are set.
   *
   * @return the serialized invited-users string
   */
  public String getInvitedUsersList() {
    if (invitedUsers == null) {
      return SEPARATOR;
    }

    StringBuilder result = new StringBuilder(SEPARATOR);
    for (String user : invitedUsers) {
      result.append(user);
      result.append(SEPARATOR);
    }
    return result.toString();
  }

  /**
   * @return whether email notifications are enabled for this appointment
   */
  public Boolean getEnableNotification() {
    return enableNotification;
  }

  /**
   * @param value whether email notifications should be enabled
   */
  public void setEnableNotification(Boolean value) {
    enableNotification = value;
  }

  /**
   * @return the useBCC
   */
  public boolean getUseBCC() {
    return useBCC;
  }

  /**
   * @param useBCC the useBCC to set
   */
  public void setUseBCC(boolean useBCC) {
    this.useBCC = useBCC;
  }

  /**
   * @return the audience map of invited users to their meeting request status
   */
  public HashMap<String, MeetingRequestStatus> getAudience() {
    return audience;
  }

  /**
   * Records or updates the meeting request status for a single user in the audience map.
   *
   * @param user the user identifier
   * @param status the meeting request status to associate with the user
   */
  public void addAudience(String user, MeetingRequestStatus status) {
    audience.put(user, status);
  }

  /**
   * Builds a {@link PropertyMap} containing only the subset of properties relevant to the given
   * update operation.
   *
   * @param updateInfo the section of the appointment being updated (general information, audience,
   *     contact information or relevant space)
   * @return a property map limited to the fields for the requested section (empty for
   *     {@code RelevantSpace})
   */
  public PropertyMap getProperties(AppointmentUpdateInfo updateInfo) {
    PropertyMap properties = new PropertyMap();

    switch (updateInfo) {
      case GeneralInformation:
        addGeneralInfoProperties(properties);
        break;
      case Audience:
        addAudienceProperties(properties);
        break;
      case ContactInformation:
        addContactInfoProperties(properties);
        break;
      case RelevantSpace:
      default:
        break;
    }
    return properties;
  }

  private void addGeneralInfoProperties(PropertyMap properties) {
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_LANGUAGE,
      this.getLanguage()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_TITLE,
      this.getTitle()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_TIMEZONE,
      this.getTimeZoneId()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_ABSTRACT,
      this.getEventAbstract()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_INVITATION_MESSAGE,
      this.getInvitationMessage()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_LOCATION,
      this.getLocation()
    );

    if (this.getStartDate() != null) {
      properties.put(
        EventModel.PROP_EVENT_START_DATE,
        AppointmentUtils.convertDateValueToDate(this.getStartDate())
      );
    }
    if (this.getStartTime() != null) {
      properties.put(
        EventModel.PROP_EVENT_START_TIME,
        this.getStartTime().toString()
      );
    }
    if (this.getEndTime() != null) {
      properties.put(
        EventModel.PROP_EVENT_END_TIME,
        this.getEndTime().toString()
      );
    }
    if (this.getOccurenceRate() != null) {
      properties.put(
        EventModel.PROP_EVENT_OCCURENCE_RATE,
        this.getOccurenceRate().toString()
      );
    }
  }

  private void addAudienceProperties(PropertyMap properties) {
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_AUDIENCE,
      this.getAudienceStatus()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_INVITED_USERS,
      this.getInvitedUsersList()
    );
  }

  private void addContactInfoProperties(PropertyMap properties) {
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_EMAIL,
      this.getEmail()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_NAME,
      this.getName()
    );
    addPropertyIfNotNull(
      properties,
      EventModel.PROP_EVENT_PHONE,
      this.getPhone()
    );
    addPropertyIfNotNull(properties, EventModel.PROP_EVENT_URL, this.getUrl());
  }

  /**
   * @return the appointment identifier
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the appointment identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the iCalendar recurrence rule for this appointment, computing and caching it on first
   * access, with the {@code RRULE:} prefix stripped.
   *
   * @return the recurrence rule string without the {@code RRULE:} prefix
   * @throws AlfrescoRuntimeException if the occurrence rate, timezone or start date is missing
   */
  public String getRRule() {
    if (rrule == null) {
      rrule = getRRuleString();
    }
    return rrule.replace("RRULE:", "");
  }
}
