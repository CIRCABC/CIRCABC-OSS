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

import eu.cec.digit.circabc.service.event.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.cec.digit.circabc.model.EventModel.*;

public class MeetingImpl extends AppointmentImpl implements Meeting, Serializable {

    private static final long serialVersionUID = 5849473709093936742L;

    private String agenda;

    private MeetingAvailability availability;

    private MeetingType meetingType;

    private String meetingTypeString;

    private String organization;

    private NodeRef librarySection;

    private List<String> rejectedUsers;

    private List<String> acceptedUsers;

    private Integer sequence;

    public String getAgenda() {

        return agenda;
    }

    public void setAgenda(String value) {
        agenda = value;
    }

    public MeetingAvailability getAvailability() {

        return availability;
    }

    public void setAvailability(MeetingAvailability value) {
        availability = value;
    }

    public String getAvailabilityAsString() {

        return availability.toString().toUpperCase();
    }

    @Deprecated
    public MeetingType getMeetingType() {

        return meetingType;
    }

    @Deprecated
    public void setMeetingType(MeetingType value) {
        meetingType = value;
        meetingTypeString = value.toString();
        
    }

    public String getMeetingTypeString() {

        return meetingTypeString;
    }

    public void setMeetingTypeString(String value) {
        meetingTypeString = value;
        try {
        meetingType=  MeetingType.valueOf(value);
        } catch (IllegalArgumentException e) {
            meetingType = null;
        }
    }
    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String value) {
        organization = value;
    }

    public NodeRef getLibrarySection() {

        return librarySection;
    }

    public void setLibrarySection(NodeRef value) {

        librarySection = value;
    }

    @Override
    public PropertyMap getProperties(AppointmentUpdateInfo updateInfo) {
        PropertyMap properties = super.getProperties(updateInfo);

        switch (updateInfo) {
            case GeneralInformation:
                if (this.getAgenda() != null) {
                    properties.put(PROP_MEETING_AGENDA, this.getAgenda());
                }
                if (this.getAvailability() != null) {
                    properties.put(PROP_MEETING_AVAILABILITY, this.getAvailability());
                }
                if (this.getOrganization() != null) {
                    properties.put(PROP_MEETING_ORGAINZATION, this.getOrganization());
                }
                if (this.getMeetingTypeString() != null) {
                    properties.put(PROP_MEETING_TYPE, this.getMeetingTypeString());
                }
                break;
            case Audience:
                String acceptedUsersList = this.getAcceptedUsersList();
                if (acceptedUsersList != null && !acceptedUsersList.isEmpty()) {
                    properties.put(PROP_MEETING_ACCEPTED_USERS, acceptedUsersList);
                }

                String rejectedUsersList = this.getRejectedUsersList();
                if (rejectedUsersList != null && !rejectedUsersList.isEmpty()) {
                    properties.put(PROP_MEETING_REJECTED_USERS, rejectedUsersList);
                }
                break;

            case ContactInformation:
                break;
            case RelevantSpace:
                //			if (this.getLibrarySection() != null)
                //			{
                properties.put(PROP_MEETING_LIBRARY_SECTION, this.getLibrarySection());
                //			}

                break;

            default:
                break;
        }

        return properties;
    }

    @Override
    public PropertyMap getProperties() {
        PropertyMap properties = super.getProperties();

        properties.put(PROP_SEQUENCE, this.getSequence());

        if (this.getAgenda() != null) {
            properties.put(PROP_MEETING_AGENDA, this.getAgenda());
        }
        if (this.getAvailability() != null) {
            properties.put(PROP_MEETING_AVAILABILITY, this.getAvailability());
        }
        if (this.getOrganization() != null) {
            properties.put(PROP_MEETING_ORGAINZATION, this.getOrganization());
        }
        if (this.getMeetingTypeString() != null) {
            properties.put(PROP_MEETING_TYPE, this.getMeetingTypeString());
        }
        if (this.getLibrarySection() != null) {
            properties.put(PROP_MEETING_LIBRARY_SECTION, this.getLibrarySection());
        }

        if (this.getAcceptedUsersList() != null) {
            properties.put(PROP_MEETING_ACCEPTED_USERS, this.getAcceptedUsersList());
        }

        if (this.getRejectedUsersList() != null) {
            properties.put(PROP_MEETING_REJECTED_USERS, this.getRejectedUsersList());
        }

        return properties;
    }

    public String getRejectedUsersList() {
        if (rejectedUsers == null) {
            return "";
        }

        StringBuilder result = new StringBuilder(separator);
        for (String user : rejectedUsers) {
            result.append(user);
            result.append(separator);
        }
        return result.toString();
    }

    public String getAcceptedUsersList() {
        if (acceptedUsers == null) {
            return "";
        }

        StringBuilder result = new StringBuilder(separator);
        for (String user : acceptedUsers) {
            result.append(user);
            result.append(separator);
        }
        return result.toString();
    }

    @Override
    public void init(Map<QName, Serializable> properties) {
        super.init(properties);

        Serializable sequence = properties.get(PROP_SEQUENCE);
        if (sequence != null) {
            this.setSequence(Integer.valueOf(sequence.toString()));
        }

        Serializable agenda = properties.get(PROP_MEETING_AGENDA);
        if (agenda != null) {
            this.setAgenda(agenda.toString());
        }
        Serializable availability = properties.get(PROP_MEETING_AVAILABILITY);
        if (availability != null) {
            this.setAvailability(MeetingAvailability.valueOf(availability.toString()));
        }
        Serializable organization = properties.get(PROP_MEETING_ORGAINZATION);
        if (organization != null) {
            this.setOrganization(organization.toString());
        }
        Serializable type = properties.get(PROP_MEETING_TYPE);
        if (type != null) {
            this.setMeetingTypeString(type.toString());
        }
        Serializable library = properties.get(PROP_MEETING_LIBRARY_SECTION);
        if (library != null) {
            this.setLibrarySection((NodeRef) library);
        }

        if (this.getAudienceStatus() == AudienceStatus.Closed) {
            Serializable propertyAcceptedUsers = properties.get(PROP_MEETING_ACCEPTED_USERS);
            if (propertyAcceptedUsers != null) {
                String acceptedUsersList = propertyAcceptedUsers.toString();
                if (acceptedUsersList.length() > 1) {
                    acceptedUsersList = acceptedUsersList.substring(1, acceptedUsersList.length() - 1);
                    String[] acceptedElements = acceptedUsersList.split("\\" + separator);
                    this.setAcceptedUsers(Arrays.asList(acceptedElements));
                }
            }

            Serializable propertyRejectedUsers = properties.get(PROP_MEETING_REJECTED_USERS);
            if (propertyRejectedUsers != null) {
                String rejectedUsersList = propertyRejectedUsers.toString();
                if (rejectedUsersList.length() > 1) {
                    rejectedUsersList = rejectedUsersList.substring(1, rejectedUsersList.length() - 1);
                    String[] rejectedElements = rejectedUsersList.split("\\" + separator);
                    this.setRejectedUsers(Arrays.asList(rejectedElements));
                }
            }

            audience = new HashMap<>();
            for (String user : this.getInvitedUsers()) {
                if (this.getAcceptedUsersList().contains("|" + user)) {
                    audience.put(user, MeetingRequestStatus.Accepted);
                } else if (this.getRejectedUsersList().contains("|" + user)) {
                    audience.put(user, MeetingRequestStatus.Rejected);
                } else {
                    audience.put(user, MeetingRequestStatus.Pending);
                }
            }
        }
    }

    public void setRejectedUsers(List<String> value) {
        rejectedUsers = value;
    }

    public void setAcceptedUsers(List<String> value) {
        acceptedUsers = value;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer value) {
        sequence = value;
    }
}
