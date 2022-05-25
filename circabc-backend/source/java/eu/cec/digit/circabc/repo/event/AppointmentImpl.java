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

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateValue;
import eu.cec.digit.circabc.service.event.*;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

import static eu.cec.digit.circabc.model.EventModel.*;

public class AppointmentImpl implements Appointment {

    public static final int LIMIT_OCCURENCE_GENERATION = 52;
    public static final String separator = "|";
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

    public AudienceStatus getAudienceStatus() {
        return audienceStatus;
    }

    public void setAudienceStatus(AudienceStatus value) {

        audienceStatus = value;
    }

    public DateValue getDate() {

        return date;
    }

    public void setDate(DateValue value) {

        date = value;
    }

    public Date getDateAsDate() {

        return AppointmentUtils.convertDateValueToDate(date);
    }

    public void setDateAsDate(Date value) {

        date = AppointmentUtils.convertDateToDateValue(value);
    }

    public DateValue getStartDate() {

        return startDate;
    }

    public void setStartDate(DateValue value) {

        startDate = value;
    }

    public Date getStartDateAsDate() {

        return AppointmentUtils.convertDateValueToDate(startDate);
    }

    public void setStartDateAsDate(Date value) {

        startDate = AppointmentUtils.convertDateToDateValue(value);
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String value) {

        email = value;
    }

    public LocalTime getEndTime() {

        return endTime;
    }

    public void setEndTime(LocalTime value) {

        endTime = value;
    }

    public Date getEndTimeAsDate() {

        if (date != null) {
            return AppointmentUtils.convertLocalTimeDateValueToDate(endTime, date);
        } else {
            return AppointmentUtils.convertLocalTimeToDate(endTime);
        }
    }

    public void setEndTimeAsDate(Date value) {

        endTime = AppointmentUtils.convertDateToLocalTime(value);
    }

    public String getInvitationMessage() {

        return invitationMessage;
    }

    public void setInvitationMessage(String value) {

        invitationMessage = value;
    }

    public String getLanguage() {

        return language;
    }

    public void setLanguage(String value) {

        language = value;
    }

    public String getLocation() {

        return location;
    }

    public void setLocation(String value) {

        location = value;
    }

    public String getName() {

        return name;
    }

    public void setName(String value) {

        name = value;
    }

    public OccurenceRate getOccurenceRate() {

        return occurenceRate;
    }

    public void setOccurenceRate(OccurenceRate value) {

        occurenceRate = value;
    }

    public String getPhone() {

        return phone;
    }

    public void setPhone(String value) {

        phone = value;
    }

    public LocalTime getStartTime() {

        return startTime;
    }

    public void setStartTime(LocalTime value) {

        startTime = value;
    }

    public Date getStartTimeAsDate() {
        if (date != null) {
            return AppointmentUtils.convertLocalTimeDateValueToDate(startTime, date);
        } else {
            return AppointmentUtils.convertLocalTimeToDate(startTime);
        }
    }

    public void setStartTimeAsDate(Date value) {

        startTime = AppointmentUtils.convertDateToLocalTime(value);
    }

    public String getTimeZoneId() {

        return timeZoneId;
    }

    public void setTimeZoneId(String value) {

        timeZoneId = value;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String value) {

        title = value;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String value) {

        url = value;
    }

    public String getEventAbstract() {

        return eventAbstract;
    }

    public void setEventAbstract(String value) {

        eventAbstract = value;
    }

    public List<String> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<String> value) {
        invitedUsers = value;
    }

    public void init(Map<QName, Serializable> properties) {

        this.setTitle(properties.get(PROP_EVENT_TITLE).toString());

        this.setAudienceStatus(AudienceStatus.valueOf(properties.get(PROP_EVENT_AUDIENCE).toString()));

        Serializable dateProperty = properties.get(PROP_EVENT_DATE);
        if (dateProperty != null) {
            this.setDate(AppointmentUtils.convertDateToDateValue((Date) dateProperty));
        }
        this.setStartDate(
                AppointmentUtils.convertDateToDateValue((Date) properties.get(PROP_EVENT_START_DATE)));

        this.setStartDate(
                AppointmentUtils.convertDateToDateValue((Date) properties.get(PROP_EVENT_START_DATE)));

        this.setStartTime(new LocalTime(properties.get(PROP_EVENT_START_TIME).toString()));
        this.setEndTime(new LocalTime(properties.get(PROP_EVENT_END_TIME).toString()));
        this.setEventAbstract(properties.get(PROP_EVENT_ABSTRACT).toString());

        this.setEmail(properties.get(PROP_EVENT_EMAIL).toString());
        this.setInvitationMessage(properties.get(PROP_EVENT_INVITATION_MESSAGE).toString());
        this.setLanguage(properties.get(PROP_EVENT_LANGUAGE).toString());
        this.setLocation(properties.get(PROP_EVENT_LOCATION).toString());
        this.setName(properties.get(PROP_EVENT_NAME).toString());

        this.setPhone(properties.get(PROP_EVENT_PHONE).toString());
        this.setUrl(properties.get(PROP_EVENT_URL).toString());
        this.setTimeZoneId(properties.get(PROP_EVENT_TIMEZONE).toString());
        this.setOccurenceRate(new OccurenceRate(properties.get(PROP_EVENT_OCCURENCE_RATE).toString()));
        this.audience = new HashMap<>();
        final MeetingRequestStatus defaultSatus =
                (this instanceof Meeting)
                        ? MeetingRequestStatus.Pending
                        : MeetingRequestStatus.NotApplicable;
        if (this.getAudienceStatus() == AudienceStatus.Closed) {
            String invitedUsersList = properties.get(PROP_EVENT_INVITED_USERS).toString();
            if (invitedUsersList.length() > 1) {
                invitedUsersList = invitedUsersList.substring(1, invitedUsersList.length() - 1);
                String[] elements = invitedUsersList.split("\\" + separator);
                this.setInvitedUsers(Arrays.asList(elements));
                for (String user : this.getInvitedUsers()) {
                    audience.put(user, defaultSatus);
                }
            }
        }
    }

    public PropertyMap getProperties() {
        PropertyMap properties = new PropertyMap(15);
        if (this.getTitle() != null) {
            properties.put(PROP_EVENT_TITLE, this.getTitle());
        }
        if (this.getAudienceStatus() != null) {
            properties.put(PROP_EVENT_AUDIENCE, this.getAudienceStatus().toString());
        }
        if (this.getStartDate() != null) {
            Date javaDate = AppointmentUtils.convertDateValueToDate(this.getStartDate());
            properties.put(PROP_EVENT_START_DATE, javaDate);
        }

        if (this.getStartTime() != null) {
            properties.put(PROP_EVENT_START_TIME, this.getStartTime().toString());
        }
        if (this.getEndTime() != null) {
            properties.put(PROP_EVENT_END_TIME, this.getEndTime().toString());
        }
        if (this.getEventAbstract() != null) {
            properties.put(PROP_EVENT_ABSTRACT, this.getEventAbstract());
        }

        if (this.getEmail() != null) {
            properties.put(PROP_EVENT_EMAIL, this.getEmail());
        }
        if (this.getInvitationMessage() != null) {
            properties.put(PROP_EVENT_INVITATION_MESSAGE, this.getInvitationMessage());
        }
        if (this.getLanguage() != null) {
            properties.put(PROP_EVENT_LANGUAGE, this.getLanguage());
        }
        if (this.getLocation() != null) {
            properties.put(PROP_EVENT_LOCATION, this.getLocation());
        }
        if (this.getName() != null) {
            properties.put(PROP_EVENT_NAME, this.getName());
        }

        if (this.getPhone() != null) {
            properties.put(PROP_EVENT_PHONE, this.getPhone());
        }
        if (this.getUrl() != null) {
            properties.put(PROP_EVENT_URL, this.getUrl());
        }
        if (this.getTimeZoneId() != null) {
            properties.put(PROP_EVENT_TIMEZONE, this.getTimeZoneId());
        }
        if (this.getOccurenceRate() != null) {
            properties.put(PROP_EVENT_OCCURENCE_RATE, this.getOccurenceRate().toString());
        }
        if (this.getInvitedUsersList() != null) {
            properties.put(PROP_EVENT_INVITED_USERS, this.getInvitedUsersList());
        }
        return properties;
    }

    public List<PropertyMap> getEventDatesProperties(AppointmentType appointmentType) {
        final List<DateValue> eventDates = getEventDates();
        final List<PropertyMap> listOfProperties = new ArrayList<>(eventDates.size());
        String invitedUserList = this.getInvitedUsersList();
        if (this.audienceStatus == AudienceStatus.Open) {
            invitedUserList = separator + separator;
        }

        for (DateValue value : eventDates) {
            PropertyMap properties = new PropertyMap(6);
            Date javaDate = AppointmentUtils.convertLocalTimeDateValueToDate(this.startTime, value);
            properties.put(PROP_EVENT_DATE, javaDate);
            properties.put(PROP_EVENT_INVITED_USERS, invitedUserList);
            properties.put(PROP_KIND_OF_EVENT, appointmentType.toString());
            properties.put(PROP_EVENT_START_TIME, this.startTime.toString());
            properties.put(PROP_EVENT_END_TIME, this.endTime.toString());
            properties.put(PROP_EVENT_TITLE, this.title);
            properties.put(PROP_EVENT_NAME, this.name);
            properties.put(PROP_EVENT_AUDIENCE, this.audienceStatus);
            listOfProperties.add(properties);
        }
        return listOfProperties;
    }

    public List<DateValue> getEventDates() {

        rrule = getRRuleString();
        List<DateValue> result = new ArrayList<>();
        int limit = LIMIT_OCCURENCE_GENERATION;
        try {
            RecurrenceIterator ri =
                    RecurrenceIteratorFactory.createRecurrenceIterator(
                            rrule, startDate, TimeZone.getTimeZone(timeZoneId));

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
        String result = null;
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
                        result = "RRULE:FREQ=DAILY;INTERVAL=14;COUNT=" + occurenceRate.getTimes();
                        break;
                    case MondayToFriday:
                        result = "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;COUNT=" + occurenceRate.getTimes();
                        break;
                    case MondayWednseyFriday:
                        result = "RRULE:FREQ=WEEKLY;BYDAY=MO,WE,FR;COUNT=" + occurenceRate.getTimes();
                        break;
                    case TuesdayThursday:
                        result = "RRULE:FREQ=WEEKLY;BYDAY=TU,TH;COUNT=" + occurenceRate.getTimes();
                        break;
                    case MonthlyByDate:
                        result =
                                "RRULE:FREQ=MONTHLY;COUNT="
                                        + occurenceRate.getTimes()
                                        + ";BYMONTHDAY="
                                        + dayOfMonth;
                        break;
                    case MonthlyByWeekday:
                        result =
                                "RRULE:FREQ=MONTHLY;COUNT="
                                        + occurenceRate.getTimes()
                                        + ";BYDAY=1"
                                        + getWeekDay2Letters(dayOfWeek);
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
                                "RRULE:FREQ=DAILY;INTERVAL="
                                        + occurenceRate.getEvery()
                                        + ";COUNT="
                                        + occurenceRate.getTimes();
                        break;
                    case weeks:
                        result =
                                "RRULE:FREQ=WEEKLY;INTERVAL="
                                        + occurenceRate.getEvery()
                                        + ";COUNT="
                                        + occurenceRate.getTimes();
                        break;
                    case months:
                        result =
                                "RRULE:FREQ=MONTHLY;INTERVAL="
                                        + occurenceRate.getEvery()
                                        + ";COUNT="
                                        + occurenceRate.getTimes();
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

    public String getInvitedUsersList() {

        if (invitedUsers == null) {
            return separator;
        }

        StringBuilder result = new StringBuilder(separator);
        for (String user : invitedUsers) {
            result.append(user);
            result.append(separator);
        }
        return result.toString();
    }

    public Boolean getEnableNotification() {
        return enableNotification;
    }

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

    public HashMap<String, MeetingRequestStatus> getAudience() {
        return audience;
    }

    public void addAudience(String user, MeetingRequestStatus status) {
        audience.put(user, status);
    }

    public PropertyMap getProperties(AppointmentUpdateInfo updateInfo) {

        PropertyMap properties = new PropertyMap();

        switch (updateInfo) {
            case GeneralInformation:
                if (this.getLanguage() != null) {
                    properties.put(PROP_EVENT_LANGUAGE, this.getLanguage());
                }
                if (this.getTitle() != null) {
                    properties.put(PROP_EVENT_TITLE, this.getTitle());
                }

                if (this.getStartDate() != null) {
                    Date javaDate = AppointmentUtils.convertDateValueToDate(this.getStartDate());
                    properties.put(PROP_EVENT_START_DATE, javaDate);
                }

                if (this.getStartTime() != null) {
                    properties.put(PROP_EVENT_START_TIME, this.getStartTime().toString());
                }
                if (this.getEndTime() != null) {
                    properties.put(PROP_EVENT_END_TIME, this.getEndTime().toString());
                }

                if (this.getTimeZoneId() != null) {
                    properties.put(PROP_EVENT_TIMEZONE, this.getTimeZoneId());
                }
                if (this.getOccurenceRate() != null) {
                    properties.put(PROP_EVENT_OCCURENCE_RATE, this.getOccurenceRate().toString());
                }
                if (this.getEventAbstract() != null) {
                    properties.put(PROP_EVENT_ABSTRACT, this.getEventAbstract());
                }

                if (this.getInvitationMessage() != null) {
                    properties.put(PROP_EVENT_INVITATION_MESSAGE, this.getInvitationMessage());
                }

                if (this.getLocation() != null) {
                    properties.put(PROP_EVENT_LOCATION, this.getLocation());
                }

                break;

            case Audience:
                if (this.getAudienceStatus() != null) {
                    properties.put(PROP_EVENT_AUDIENCE, this.getAudienceStatus());
                }
                if (this.getInvitedUsersList() != null) {
                    properties.put(PROP_EVENT_INVITED_USERS, this.getInvitedUsersList());
                }

                break;
            case ContactInformation:
                if (this.getEmail() != null) {
                    properties.put(PROP_EVENT_EMAIL, this.getEmail());
                }

                if (this.getName() != null) {
                    properties.put(PROP_EVENT_NAME, this.getName());
                }

                if (this.getPhone() != null) {
                    properties.put(PROP_EVENT_PHONE, this.getPhone());
                }
                if (this.getUrl() != null) {
                    properties.put(PROP_EVENT_URL, this.getUrl());
                }

                break;
            case RelevantSpace:
                // done in meeting only meeting has relevenat space
                break;
            default:
                break;
        }

        return properties;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRRule() {
        if (rrule == null) {
            rrule = getRRuleString();
        }
        return rrule.replace("RRULE:", "");
    }
}
