package io.swagger.model;

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

import com.google.ical.values.DateValue;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.joda.time.LocalTime;

/**
 * Domain model contract for a CIRCABC calendar appointment (event/meeting).
 *
 * <p>An {@code Appointment} describes a single- or recurring-occurrence event managed by the Events
 * service of an Interest Group. It carries scheduling information (start/end date and time, time
 * zone, recurrence rate and RRULE), descriptive metadata (title, abstract, location, URL,
 * language), organiser contact details (name, phone, e-mail) and the invitation/audience data
 * (invited users, invitation message, per-user meeting-request status and notification flags).
 *
 * <p>The interface exposes both native representations (for example {@link DateValue} and {@link
 * LocalTime}) and {@link Date}-based accessors so that the same value can be consumed either as an
 * iCal-style value or as a plain {@code java.util.Date}. It also provides conversion helpers that
 * translate the appointment into Alfresco {@link PropertyMap} instances used when persisting the
 * event node in the repository.
 */
public interface Appointment {
  /**
   * Returns the unique identifier of the appointment.
   *
   * @return the appointment id
   */
  String getId();

  /**
   * Sets the unique identifier of the appointment.
   *
   * @param id the appointment id
   */
  void setId(String id);

  /**
   * Returns the language (locale) associated with the appointment content.
   *
   * @return the language code
   */
  String getLanguage();

  /**
   * Sets the language (locale) associated with the appointment content.
   *
   * @param value the language code
   */
  void setLanguage(String value);

  /**
   * Returns the appointment title.
   *
   * @return the title
   */
  String getTitle();

  /**
   * Sets the appointment title.
   *
   * @param value the title
   */
  void setTitle(String value);

  /**
   * Returns the short abstract/description of the event.
   *
   * @return the event abstract
   */
  String getEventAbstract();

  /**
   * Sets the short abstract/description of the event.
   *
   * @param value the event abstract
   */
  void setEventAbstract(String value);

  /**
   * Returns the reference date of the appointment as an iCal {@link DateValue}.
   *
   * @return the date value
   */
  DateValue getDate();

  /**
   * Returns the reference date of the appointment as a {@link Date}.
   *
   * @return the date
   */
  Date getDateAsDate();

  /**
   * Sets the reference date of the appointment from a {@link Date}.
   *
   * @param value the date
   */
  void setDateAsDate(Date value);

  /**
   * Returns the start date of the appointment as an iCal {@link DateValue}.
   *
   * @return the start date value
   */
  DateValue getStartDate();

  /**
   * Sets the start date of the appointment as an iCal {@link DateValue}.
   *
   * @param value the start date value
   */
  void setStartDate(DateValue value);

  /**
   * Returns the start date of the appointment as a {@link Date}.
   *
   * @return the start date
   */
  Date getStartDateAsDate();

  /**
   * Sets the start date of the appointment from a {@link Date}.
   *
   * @param value the start date
   */
  void setStartDateAsDate(Date value);

  /**
   * Returns the recurrence rate of the appointment.
   *
   * @return the occurrence rate
   */
  OccurenceRate getOccurenceRate();

  /**
   * Sets the recurrence rate of the appointment.
   *
   * @param value the occurrence rate
   */
  void setOccurenceRate(OccurenceRate value);

  /**
   * Returns the start time of the appointment as a {@link LocalTime}.
   *
   * @return the start time
   */
  LocalTime getStartTime();

  /**
   * Sets the start time of the appointment as a {@link LocalTime}.
   *
   * @param value the start time
   */
  void setStartTime(LocalTime value);

  /**
   * Returns the start time of the appointment as a {@link Date}.
   *
   * @return the start time
   */
  Date getStartTimeAsDate();

  /**
   * Sets the start time of the appointment from a {@link Date}.
   *
   * @param value the start time
   */
  void setStartTimeAsDate(Date value);

  /**
   * Returns the end time of the appointment as a {@link LocalTime}.
   *
   * @return the end time
   */
  LocalTime getEndTime();

  /**
   * Sets the end time of the appointment as a {@link LocalTime}.
   *
   * @param value the end time
   */
  void setEndTime(LocalTime value);

  /**
   * Returns the end time of the appointment as a {@link Date}.
   *
   * @return the end time
   */
  Date getEndTimeAsDate();

  /**
   * Sets the end time of the appointment from a {@link Date}.
   *
   * @param value the end time
   */
  void setEndTimeAsDate(Date value);

  /**
   * Returns the identifier of the time zone applied to the appointment times.
   *
   * @return the time zone id
   */
  String getTimeZoneId();

  /**
   * Sets the identifier of the time zone applied to the appointment times.
   *
   * @param value the time zone id
   */
  void setTimeZoneId(String value);

  /**
   * Returns the location where the event takes place.
   *
   * @return the location
   */
  String getLocation();

  /**
   * Sets the location where the event takes place.
   *
   * @param value the location
   */
  void setLocation(String value);

  /**
   * Returns the list of users invited to the appointment.
   *
   * @return the invited users
   */
  List<String> getInvitedUsers();

  /**
   * Sets the list of users invited to the appointment.
   *
   * @param value the invited users
   */
  void setInvitedUsers(List<String> value);

  /**
   * Returns the invited users rendered as a single delimited string.
   *
   * @return the invited users list as a string
   */
  String getInvitedUsersList();

  /**
   * Returns the message sent along with the invitation.
   *
   * @return the invitation message
   */
  String getInvitationMessage();

  /**
   * Sets the message sent along with the invitation.
   *
   * @param value the invitation message
   */
  void setInvitationMessage(String value);

  /**
   * Returns the audience visibility/status of the appointment.
   *
   * @return the audience status
   */
  AudienceStatus getAudienceStatus();

  /**
   * Sets the audience visibility/status of the appointment.
   *
   * @param value the audience status
   */
  void setAudienceStatus(AudienceStatus value);

  /**
   * Returns the technical name of the appointment node.
   *
   * @return the name
   */
  String getName();

  /**
   * Sets the technical name of the appointment node.
   *
   * @param value the name
   */
  void setName(String value);

  /**
   * Returns the organiser contact phone number.
   *
   * @return the phone number
   */
  String getPhone();

  /**
   * Sets the organiser contact phone number.
   *
   * @param value the phone number
   */
  void setPhone(String value);

  /**
   * Returns the iCal recurrence rule (RRULE) describing repeated occurrences.
   *
   * @return the RRULE string
   */
  String getRRule();

  /**
   * Returns the organiser contact e-mail address.
   *
   * @return the e-mail address
   */
  String getEmail();

  /**
   * Sets the organiser contact e-mail address.
   *
   * @param value the e-mail address
   */
  void setEmail(String value);

  /**
   * Returns the URL associated with the event.
   *
   * @return the URL
   */
  String getUrl();

  /**
   * Sets the URL associated with the event.
   *
   * @param value the URL
   */
  void setUrl(String value);

  /**
   * Returns whether notifications are enabled for this appointment.
   *
   * @return {@code true} if notifications are enabled, otherwise {@code false}
   */
  Boolean getEnableNotification();

  /**
   * Sets whether notifications are enabled for this appointment.
   *
   * @param value {@code true} to enable notifications, otherwise {@code false}
   */
  void setEnableNotification(Boolean value);

  /**
   * Returns whether invitation e-mails should be sent using blind carbon copy (BCC).
   *
   * @return {@code true} if BCC should be used, otherwise {@code false}
   */
  boolean getUseBCC();

  /**
   * Sets whether invitation e-mails should be sent using blind carbon copy (BCC).
   *
   * @param value {@code true} to use BCC, otherwise {@code false}
   */
  void setUseBCC(boolean value);

  /**
   * Builds the Alfresco {@link PropertyMap} representation of this appointment for persistence.
   *
   * @return the property map for the appointment
   */
  PropertyMap getProperties();

  /**
   * Builds the Alfresco {@link PropertyMap} representation of this appointment, taking the supplied
   * update information into account.
   *
   * @param updateInfo the update context influencing which properties are produced
   * @return the property map for the appointment
   */
  PropertyMap getProperties(AppointmentUpdateInfo updateInfo);

  /**
   * Builds the per-occurrence Alfresco property maps for the event dates of the given appointment
   * type.
   *
   * @param appointmentType the type of appointment determining how event dates are expanded
   * @return the list of property maps, one per event date/occurrence
   */
  List<PropertyMap> getEventDatesProperties(AppointmentType appointmentType);

  /**
   * Initialises this appointment from an Alfresco node property map.
   *
   * @param properties the node properties keyed by {@link QName}
   */
  void init(Map<QName, Serializable> properties);

  /**
   * Returns the audience of the appointment as a mapping of user to meeting-request status.
   *
   * @return the audience map keyed by user
   */
  HashMap<String, MeetingRequestStatus> getAudience();

  /**
   * Adds (or updates) an audience member with the given meeting-request status.
   *
   * @param user the user identifier
   * @param status the meeting-request status for the user
   */
  void addAudience(String user, MeetingRequestStatus status);
}
