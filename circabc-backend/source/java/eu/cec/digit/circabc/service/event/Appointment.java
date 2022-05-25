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

import com.google.ical.values.DateValue;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Appointment {

    String getId();

    void setId(String id);

    String getLanguage();

    void setLanguage(String value);

    String getTitle();

    void setTitle(String value);

    String getEventAbstract();

    void setEventAbstract(String value);

    DateValue getDate();

    Date getDateAsDate();

    void setDateAsDate(Date value);

    DateValue getStartDate();

    void setStartDate(DateValue value);

    Date getStartDateAsDate();

    void setStartDateAsDate(Date value);

    OccurenceRate getOccurenceRate();

    void setOccurenceRate(OccurenceRate value);

    LocalTime getStartTime();

    void setStartTime(LocalTime value);

    Date getStartTimeAsDate();

    void setStartTimeAsDate(Date value);

    LocalTime getEndTime();

    void setEndTime(LocalTime value);

    Date getEndTimeAsDate();

    void setEndTimeAsDate(Date value);

    String getTimeZoneId();

    void setTimeZoneId(String value);

    String getLocation();

    void setLocation(String value);

    List<String> getInvitedUsers();

    void setInvitedUsers(List<String> value);

    String getInvitedUsersList();

    String getInvitationMessage();

    void setInvitationMessage(String value);

    AudienceStatus getAudienceStatus();

    void setAudienceStatus(AudienceStatus value);

    String getName();

    void setName(String value);

    String getPhone();

    void setPhone(String value);

    String getRRule();

    String getEmail();

    void setEmail(String value);

    String getUrl();

    void setUrl(String value);

    Boolean getEnableNotification();

    void setEnableNotification(Boolean value);

    boolean getUseBCC();

    void setUseBCC(boolean value);

    PropertyMap getProperties();

    PropertyMap getProperties(AppointmentUpdateInfo updateInfo);

    List<PropertyMap> getEventDatesProperties(AppointmentType appointmentType);

    void init(Map<QName, Serializable> properties);

    HashMap<String, MeetingRequestStatus> getAudience();

    void addAudience(String user, MeetingRequestStatus status);
}
