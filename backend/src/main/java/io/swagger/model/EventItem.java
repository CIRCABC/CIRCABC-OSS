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

import java.util.Date;
import org.alfresco.service.cmr.repository.NodeRef;
import org.joda.time.LocalTime;

/**
 * Domain model representing a single calendar event (appointment) within an Interest Group.
 *
 * <p>An {@code EventItem} bundles the identifying reference of the underlying Alfresco node
 * together with the descriptive properties of the event (title, date, times, location, contact,
 * meeting status, etc.) and the Interest Group it belongs to. It is used to carry event data
 * between the service layer and the REST/notification layers (for example when rendering calendar
 * responses or building event notification messages).
 *
 * <p>Some properties are inherited from the parent appointment container (occurrence rate,
 * location, description, container id and time zone) and are grouped accordingly.
 */
public class EventItem {

  /** Reference to the Alfresco repository node backing this event. */
  private NodeRef eventNodeRef;

  /** Identifier (short name) of the Interest Group that owns this event. */
  private String interestGroup;

  /** Human-readable title of the owning Interest Group; falls back to {@link #interestGroup}. */
  private String interestGroupTitle;

  /** Title of the event. */
  private String title;

  /** Date on which the event takes place. */
  private Date date;

  /** Contact information (e.g. organiser) for the event. */
  private String contact;

  /** Meeting status of the event (e.g. confirmed, tentative, cancelled). */
  private String meetingStatus;

  /** Type of appointment (see {@link AppointmentType}). */
  private AppointmentType eventType;

  /** Time of day at which the event starts. */
  private LocalTime startTime;

  /** Time of day at which the event ends. */
  private LocalTime endTime;

  // parent, parent properties
  /** Recurrence/occurrence rate inherited from the parent appointment container. */
  private String occurrenceRate;

  /** Physical or virtual location of the event. */
  private String location;

  /** Free-text description of the event. */
  private String description;

  /** Identifier of the parent appointment container this event belongs to. */
  private String appointmentContainerId;

  /** Time zone associated with the event's date and times. */
  private String timeZone;

  /**
   * @return the eventNodeRef
   */
  public NodeRef getEventNodeRef() {
    return eventNodeRef;
  }

  /**
   * @return the timeZone associated with this event
   */
  public String getTimeZone() {
    return timeZone;
  }

  /**
   * @param timeZone the timeZone to set
   */
  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  /**
   * @param eventNodeRef the eventNodeRef to set
   */
  public void setEventNodeRef(NodeRef eventNodeRef) {
    this.eventNodeRef = eventNodeRef;
  }

  /**
   * @return the interestGroup
   */
  public String getInterestGroup() {
    return interestGroup;
  }

  /**
   * @param interestGroup the interestGroup to set
   */
  public void setInterestGroup(String interestGroup) {
    this.interestGroup = interestGroup;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }

  /**
   * @return the contact
   */
  public String getContact() {
    return contact;
  }

  /**
   * @param contact the contact to set
   */
  public void setContact(String contact) {
    this.contact = contact;
  }

  /**
   * @return the status
   */
  public String getMeetingStatus() {
    return meetingStatus;
  }

  /**
   * @param status the status to set
   */
  public void setMeetingStatus(String status) {
    this.meetingStatus = status;
  }

  /**
   * @return the eventType
   */
  public AppointmentType getEventType() {
    return eventType;
  }

  /**
   * @param eventType the eventType to set
   */
  public void setEventType(AppointmentType eventType) {
    this.eventType = eventType;
  }

  /**
   * @return the startTime
   */
  public LocalTime getStartTime() {
    return startTime;
  }

  /**
   * @param startTime the startTime to set
   */
  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  /**
   * @return the endTime
   */
  public LocalTime getEndTime() {
    return endTime;
  }

  /**
   * @param endTime the endTime to set
   */
  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  /**
   * @return the interestGroupTitle
   */
  public final String getInterestGroupTitle() {
    return (interestGroupTitle == null || interestGroupTitle.isEmpty())
      ? interestGroup
      : interestGroupTitle;
  }

  /**
   * @param interestGroupTitle the interestGroupTitle to set
   */
  public final void setInterestGroupTitle(String interestGroupTitle) {
    this.interestGroupTitle = interestGroupTitle;
  }

  /**
   * Returns the Interest Group identifier truncated to a maximum of 50 characters.
   *
   * @return the interest group identifier, shortened to its first 50 characters when longer
   */
  public String getInterestGroup50() {
    if (interestGroup.length() > 50) {
      return interestGroup.substring(0, 50);
    } else {
      return interestGroup;
    }
  }

  /**
   * Returns the event title truncated to a maximum of 50 characters.
   *
   * @return the title, shortened to its first 50 characters when longer
   */
  public String getTitle50() {
    if (title.length() > 50) {
      return title.substring(0, 50);
    } else {
      return title;
    }
  }

  /**
   * @return the occurrenceRate
   */
  public String getOccurrenceRate() {
    return occurrenceRate;
  }

  /**
   * @param occurrenceRate the occurrenceRate to set
   */
  public void setOccurrenceRate(String occurrenceRate) {
    this.occurrenceRate = occurrenceRate;
  }

  /**
   * @return the location
   */
  public String getLocation() {
    return location;
  }

  /**
   * @param location the location to set
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return the appointmentContainerId
   */
  public String getAppointmentContainerId() {
    return appointmentContainerId;
  }

  /**
   * @param appointmentContainerId the appointmentContainerId to set
   */
  public void setAppointmentContainerId(String appointmentContainerId) {
    this.appointmentContainerId = appointmentContainerId;
  }
}
