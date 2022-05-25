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
package eu.cec.digit.circabc.model;

import org.alfresco.service.namespace.QName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * It is the model for the dynamic properties specification
 *
 * @author Slobodan Filipovic
 */
public interface EventModel extends BaseCircabcModel {

    /**
     * Circabc Event namespace
     */
    String CIRCABC_EVENT_MODEL_1_0_URI = CIRCABC_NAMESPACE
            + "/model/events/1.0";

    /**
     * Circabc event prefix
     */
    String CIRCABC_EVENT_MODEL_PREFIX = "ce";

    QName ASSOC_BASE_EVANT_DATE_CONTAINER_CONTAINER = QName
            .createQName(CIRCABC_EVENT_MODEL_1_0_URI, "baseEvantDateContainer");


    QName ASSOC_EVENT_DATES = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "eventDatesAssociation");


    /**
     * Circabc event root container
     */
    QName TYPE_EVENT_DATES_CONTAINER = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "datesContainer");

    QName TYPE_EVENT = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "event");

    /**
     * Circabc
     */
    QName TYPE_EVENT_MEETING_DEFINITION = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "meetingDefinition");

    /**
     * Circabc
     */
    QName TYPE_EVENT_DEFINITION = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "eventDefinition");

    /**
     * Circabc
     */
    QName ASSOC_EVENT = QName.createQName(
            CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "eventAssociation");

    /**
     * Circabc  index
     */
    QName PROP_EVENT_ABSTRACT = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "abstract");

    /**
     * Circabc
     */
    QName PROP_EVENT_AUDIENCE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "audience");

    /**
     * Circabc
     */


    QName PROP_KIND_OF_EVENT = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "kindOfEvent");


    QName PROP_EVENT_LANGUAGE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "language");

    QName PROP_EVENT_TITLE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "title");
    QName PROP_EVENT_NAME = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "name");
    QName PROP_EVENT_URL = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "url");
    QName PROP_EVENT_EMAIL = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "email");
    QName PROP_EVENT_PHONE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "phone");
    QName PROP_EVENT_OCCURENCE_RATE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "occurenceRate");
    QName PROP_EVENT_DATE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "date");
    QName PROP_EVENT_START_DATE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "startDate");
    QName PROP_EVENT_TIMEZONE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "timezone");
    QName PROP_EVENT_START_TIME = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "startTime");
    QName PROP_EVENT_END_TIME = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "endTime");

    QName PROP_EVENT_INVITED_USERS = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "invitedUsers");

    QName PROP_MEETING_ACCEPTED_USERS = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "acceptUserList");

    QName PROP_MEETING_REJECTED_USERS = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "rejectUserList");

    QName PROP_EVENT_INVITATION_MESSAGE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "invitationMessage");

    QName PROP_EVENT_LOCATION = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "location");


    // event specific properties
    QName PROP_EVENT_PRIORITY = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "eventPriority");

    QName PROP_EVENT_TYPE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "eventType");

    // meeting specific properties

    QName PROP_MEETING_AVAILABILITY = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "availability");

    QName PROP_MEETING_ORGAINZATION = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "orgainzation");

    QName PROP_MEETING_AGENDA = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "agenda");

    QName PROP_MEETING_TYPE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "meetingType");

    QName PROP_MEETING_LIBRARY_SECTION = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "librarySection");

    QName PROP_WEEK_START_DAY = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "weekStartDay");

    QName PROP_SEQUENCE = QName.createQName(
            CIRCABC_EVENT_MODEL_1_0_URI, "sequence");


    /**
     * The possible values of the timezone
     */
    List<String> EVENT_TIME_ZONE_CONSTRAINT_VALUES = Collections.unmodifiableList(Arrays.asList(
            "GMT-12", "GMT-11", "GMT-10", "GMT-9", "GMT-8", "GMT-7", "GMT-6",
            "GMT-5", "GMT-4", "GMT-3", "GMT-2", "GMT-1", "GMT", "GMT+1",
            "GMT+2", "GMT+3", "GMT+4", "GMT+5", "GMT+6", "GMT+7", "GMT+8",
            "GMT+9", "GMT+10", "GMT+11"));

    String[] EVENT_AVAILABILITY_CONSTRAINT_VALUES = {
            "Private", "Public"};

    String[] EVENT_AUDIENCE_CONSTRAINT_VALUES = {"Open",
            "Close"};

    String[] EVENT_PRIORITY_CONSTRAINT_VALUES = {"Low",
            "Medium", "High", "Urgent"};

    String[] EVENT_TYPE_CONSTRAINT_VALUES = {
            "Appointment", "Task", "Other"};

    String[] MEETING_TYPE_CONSTRAINT_VALUES = {
            "FaceToFace", "VirtualMeeting",
            "ElectronicWithConnectixVideoPhone",
            "ElectronicWithEnhancedSeeYouSeeMe",
            "ElectronicWithInternetVideoPhone", "ElectronicWithIntelProshare",
            "ElectronicWithMicrosoftNetMeeting",
            "ElectronicWithNetscapeConference",
            "ElectronicWithNetscapeCooltalk", "ElectronicWithVDOnetVDOPhone",
            "ElectronicWithotherSoftware"};


    List<String> WEEK_START_DAY_CONSTRAINT_VALUES = Collections.unmodifiableList(Arrays.asList(
            "sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "today"));
}
