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
package io.swagger.model.alfresco;

import static io.swagger.model.alfresco.BaseCircabcModel.CIRCABC_NAMESPACE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.service.namespace.QName;

/**
 * Content-model constants for the CIRCABC "events" model.
 *
 * <p>This is a non-instantiable utility holder that centralises the Alfresco
 * {@link QName} identifiers (types, associations and properties) and the
 * allowed constraint value lists used to describe events, meetings and their
 * scheduling dates within the repository. Other components reference these
 * constants instead of hard-coding namespace strings, ensuring a single
 * source of truth for the events model definition.
 *
 * @author Slobodan Filipovic
 */
public final class EventModel {

  /**
   * Private constructor to prevent instantiation of this constants holder.
   */
  private EventModel() {}

  /**
   * Circabc Event namespace
   */
  public static final String CIRCABC_EVENT_MODEL_1_0_URI =
    CIRCABC_NAMESPACE + "/model/events/1.0";

  /**
   * Circabc event prefix
   */
  public static final String CIRCABC_EVENT_MODEL_PREFIX = "ce";

  /**
   * Association from an event/meeting to its base date container node.
   */
  public static final QName ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER =
    QName.createQName(CIRCABC_EVENT_MODEL_1_0_URI, "baseEvantDateContainer");

  /**
   * Association linking a date container to its individual event date nodes.
   */
  public static final QName ASSOC_EVENT_DATES = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "eventDatesAssociation"
  );

  /**
   * Content type for the root container that holds the dates of an event.
   */
  public static final QName TYPE_EVENT_DATES_CONTAINER = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "datesContainer"
  );

  /**
   * Content type representing a single calendar event.
   */
  public static final QName TYPE_EVENT = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "event"
  );

  /**
   * Content type describing the definition (template) of a meeting.
   */
  public static final QName TYPE_EVENT_MEETING_DEFINITION = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "meetingDefinition"
  );

  /**
   * Content type describing the definition (template) of an event.
   */
  public static final QName TYPE_EVENT_DEFINITION = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "eventDefinition"
  );

  /**
   * Association, defined in the CIRCABC content model, that links a node to
   * its event.
   */
  public static final QName ASSOC_EVENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "eventAssociation"
  );

  /**
   * Property holding the short abstract/summary of an event.
   */
  public static final QName PROP_EVENT_ABSTRACT = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "abstract"
  );

  /**
   * Property holding the audience of an event (see
   * {@link #EVENT_AUDIENCE_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_EVENT_AUDIENCE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "audience"
  );

  /**
   * Property holding the kind/category of the event.
   */
  public static final QName PROP_KIND_OF_EVENT = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "kindOfEvent"
  );

  /**
   * Property holding the language of the event.
   */
  public static final QName PROP_EVENT_LANGUAGE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "language"
  );

  /**
   * Property holding the display title of the event.
   */
  public static final QName PROP_EVENT_TITLE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "title"
  );

  /**
   * Property holding the internal name of the event.
   */
  public static final QName PROP_EVENT_NAME = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "name"
  );

  /**
   * Property holding a URL associated with the event.
   */
  public static final QName PROP_EVENT_URL = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "url"
  );

  /**
   * Property holding the contact e-mail address for the event.
   */
  public static final QName PROP_EVENT_EMAIL = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "email"
  );

  /**
   * Property holding the contact phone number for the event.
   */
  public static final QName PROP_EVENT_PHONE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "phone"
  );

  /**
   * Property holding the recurrence/occurrence rate of the event.
   */
  public static final QName PROP_EVENT_OCCURENCE_RATE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "occurenceRate"
  );

  /**
   * Property holding the date of the event.
   */
  public static final QName PROP_EVENT_DATE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "date"
  );

  /**
   * Property holding the start date of the event.
   */
  public static final QName PROP_EVENT_START_DATE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "startDate"
  );

  /**
   * Property holding the timezone of the event (see
   * {@link #EVENT_TIME_ZONE_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_EVENT_TIMEZONE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "timezone"
  );

  /**
   * Property holding the start time of the event.
   */
  public static final QName PROP_EVENT_START_TIME = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "startTime"
  );

  /**
   * Property holding the end time of the event.
   */
  public static final QName PROP_EVENT_END_TIME = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "endTime"
  );

  /**
   * Property holding the list of users invited to the event/meeting.
   */
  public static final QName PROP_EVENT_INVITED_USERS = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "invitedUsers"
  );

  /**
   * Property holding the list of users who accepted the meeting invitation.
   */
  public static final QName PROP_MEETING_ACCEPTED_USERS = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "acceptUserList"
  );

  /**
   * Property holding the list of users who rejected the meeting invitation.
   */
  public static final QName PROP_MEETING_REJECTED_USERS = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "rejectUserList"
  );

  /**
   * Property holding the message sent with the event/meeting invitation.
   */
  public static final QName PROP_EVENT_INVITATION_MESSAGE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "invitationMessage"
  );

  /**
   * Property holding the location where the event takes place.
   */
  public static final QName PROP_EVENT_LOCATION = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "location"
  );

  // event specific properties

  /**
   * Event-specific property holding the priority (see
   * {@link #EVENT_PRIORITY_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_EVENT_PRIORITY = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "eventPriority"
  );

  /**
   * Event-specific property holding the type (see
   * {@link #EVENT_TYPE_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_EVENT_TYPE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "eventType"
  );

  // meeting specific properties

  /**
   * Meeting-specific property holding the availability/visibility (see
   * {@link #EVENT_AVAILABILITY_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_MEETING_AVAILABILITY = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "availability"
  );

  /**
   * Meeting-specific property holding the organising body of the meeting.
   */
  public static final QName PROP_MEETING_ORGAINZATION = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "orgainzation"
  );

  /**
   * Meeting-specific property holding the meeting agenda.
   */
  public static final QName PROP_MEETING_AGENDA = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "agenda"
  );

  /**
   * Meeting-specific property holding the meeting type (see
   * {@link #MEETING_TYPE_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_MEETING_TYPE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "meetingType"
  );

  /**
   * Meeting-specific property referencing the related library section.
   */
  public static final QName PROP_MEETING_LIBRARY_SECTION = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "librarySection"
  );

  /**
   * Property holding the first day of the week for the calendar (see
   * {@link #WEEK_START_DAY_CONSTRAINT_VALUES}).
   */
  public static final QName PROP_WEEK_START_DAY = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "weekStartDay"
  );

  /**
   * Property holding the sequence number used to order events.
   */
  public static final QName PROP_SEQUENCE = QName.createQName(
    CIRCABC_EVENT_MODEL_1_0_URI,
    "sequence"
  );

  /**
   * The possible values of the timezone
   */
  public static final List<String> EVENT_TIME_ZONE_CONSTRAINT_VALUES =
    Collections.unmodifiableList(
      Arrays.asList(
        "GMT-12",
        "GMT-11",
        "GMT-10",
        "GMT-9",
        "GMT-8",
        "GMT-7",
        "GMT-6",
        "GMT-5",
        "GMT-4",
        "GMT-3",
        "GMT-2",
        "GMT-1",
        "GMT",
        "GMT+1",
        "GMT+2",
        "GMT+3",
        "GMT+4",
        "GMT+5",
        "GMT+6",
        "GMT+7",
        "GMT+8",
        "GMT+9",
        "GMT+10",
        "GMT+11"
      )
    );

  /**
   * Allowed values for the meeting availability property
   * ({@link #PROP_MEETING_AVAILABILITY}).
   */
  protected static final String[] EVENT_AVAILABILITY_CONSTRAINT_VALUES = {
    "Private",
    "Public",
  };

  /**
   * Allowed values for the event audience property
   * ({@link #PROP_EVENT_AUDIENCE}).
   */
  protected static final String[] EVENT_AUDIENCE_CONSTRAINT_VALUES = {
    "Open",
    "Close",
  };

  /**
   * Allowed values for the event priority property
   * ({@link #PROP_EVENT_PRIORITY}).
   */
  protected static final String[] EVENT_PRIORITY_CONSTRAINT_VALUES = {
    "Low",
    "Medium",
    "High",
    "Urgent",
  };

  /**
   * Allowed values for the event type property
   * ({@link #PROP_EVENT_TYPE}).
   */
  protected static final String[] EVENT_TYPE_CONSTRAINT_VALUES = {
    "Appointment",
    "Task",
    "Other",
  };

  /**
   * Allowed values for the meeting type property
   * ({@link #PROP_MEETING_TYPE}).
   */
  protected static final String[] MEETING_TYPE_CONSTRAINT_VALUES = {
    "FaceToFace",
    "VirtualMeeting",
    "ElectronicWithConnectixVideoPhone",
    "ElectronicWithEnhancedSeeYouSeeMe",
    "ElectronicWithInternetVideoPhone",
    "ElectronicWithIntelProshare",
    "ElectronicWithMicrosoftNetMeeting",
    "ElectronicWithNetscapeConference",
    "ElectronicWithNetscapeCooltalk",
    "ElectronicWithVDOnetVDOPhone",
    "ElectronicWithotherSoftware",
  };

  /**
   * The possible values of the week start day
   * ({@link #PROP_WEEK_START_DAY}).
   */
  public static final List<String> WEEK_START_DAY_CONSTRAINT_VALUES =
    Collections.unmodifiableList(
      Arrays.asList(
        "sunday",
        "monday",
        "tuesday",
        "wednesday",
        "thursday",
        "friday",
        "saturday",
        "today"
      )
    );
}
