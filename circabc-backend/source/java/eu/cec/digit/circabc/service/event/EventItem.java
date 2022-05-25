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

import org.alfresco.service.cmr.repository.NodeRef;
import org.joda.time.LocalTime;

import java.util.Date;

public class EventItem {

    private NodeRef eventNodeRef;
    private String interestGroup;
    private String interestGroupTitle;
    private String title;
    private Date date;
    private String contact;
    private String meetingStatus;
    private AppointmentType eventType;
    private LocalTime startTime;
    private LocalTime endTime;

    // parent, parent properties
    private String occurrenceRate;
    private String location;
    private String description;
    private String appointmentContainerId;
    private String timeZone;

    /**
     * @return the eventNodeRef
     */
    public NodeRef getEventNodeRef() {
        return eventNodeRef;
    }


    public String getTimeZone() {
        return timeZone;
    }


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
        return (interestGroupTitle == null || interestGroupTitle.length() < 1)
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
     * @return the interestGroup50
     */
    public String getInterestGroup50() {
        if (interestGroup.length() > 50) {
            return interestGroup.substring(0, 50);
        } else {
            return interestGroup;
        }
    }

    /**
     * @return the title
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
