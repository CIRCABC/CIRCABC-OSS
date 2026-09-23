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

import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.AudienceStatus;
import io.swagger.model.Meeting;
import io.swagger.model.MeetingAvailability;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.alfresco.EventModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * Domain model implementation of a CIRCABC {@link Meeting}, a specialized {@link AppointmentImpl}.
 *
 * <p>A meeting is an appointment enriched with meeting-specific data such as an agenda, an
 * availability status, an organizing entity, a meeting type, an associated library section and an
 * audience (accepted/rejected invitees). This class is responsible for translating those attributes
 * to and from the Alfresco node property representation defined in {@link EventModel}:
 *
 * <ul>
 *   <li>{@link #getProperties()} and {@link #getProperties(AppointmentUpdateInfo)} serialize the
 *       in-memory state into a {@link PropertyMap} to be persisted on the Alfresco node;
 *   <li>{@link #init(Map)} rebuilds the in-memory state from a node's property map.
 * </ul>
 *
 * <p>Accepted and rejected invitees are stored on the node as {@link AppointmentImpl#SEPARATOR
 * "|"}-delimited strings; this class encodes them with {@link #formatUserList(List)} and decodes
 * them with {@link #parseUserList(Serializable, java.util.function.Consumer)}. The class is
 * {@link Serializable} so instances can be carried across the web-script layer.
 */
public class MeetingImpl
  extends AppointmentImpl
  implements Meeting, Serializable
{

  private static final long serialVersionUID = 5849473709093936742L;

  /** Free-text agenda of the meeting. */
  private String agenda;

  /** Availability status of the meeting (for example free/busy) advertised to invitees. */
  private MeetingAvailability availability;

  /** Meeting type identifier, kept as its raw string representation. */
  private String meetingTypeString;

  /** Name of the organizing entity responsible for the meeting. */
  private String organization;

  /** Reference to the library section (folder) associated with the meeting, if any. */
  private NodeRef librarySection;

  /** Invitees who have declined the meeting invitation. */
  private List<String> rejectedUsers;

  /** Invitees who have accepted the meeting invitation. */
  private List<String> acceptedUsers;

  /** Sequence number used to track revisions of the meeting (iCalendar-style SEQUENCE). */
  private Integer sequence;

  /**
   * Returns the meeting agenda.
   *
   * @return the agenda text, or {@code null} if none is set
   */
  public String getAgenda() {
    return agenda;
  }

  /**
   * Sets the meeting agenda.
   *
   * @param value the agenda text to set
   */
  public void setAgenda(String value) {
    agenda = value;
  }

  /**
   * Returns the meeting availability status.
   *
   * @return the {@link MeetingAvailability}, or {@code null} if none is set
   */
  public MeetingAvailability getAvailability() {
    return availability;
  }

  /**
   * Sets the meeting availability status.
   *
   * @param value the {@link MeetingAvailability} to set
   */
  public void setAvailability(MeetingAvailability value) {
    availability = value;
  }

  /**
   * Returns the availability status as an upper-case string.
   *
   * @return the availability rendered in upper case
   * @throws NullPointerException if the availability has not been set
   */
  public String getAvailabilityAsString() {
    return availability.toString().toUpperCase();
  }

  /**
   * Returns the raw meeting type string.
   *
   * @return the meeting type, or {@code null} if none is set
   */
  public String getMeetingTypeString() {
    return meetingTypeString;
  }

  /**
   * Sets the raw meeting type string.
   *
   * @param value the meeting type to set
   */
  public void setMeetingTypeString(String value) {
    meetingTypeString = value;
  }

  /**
   * Returns the organizing entity of the meeting.
   *
   * @return the organization, or {@code null} if none is set
   */
  public String getOrganization() {
    return organization;
  }

  /**
   * Sets the organizing entity of the meeting.
   *
   * @param value the organization to set
   */
  public void setOrganization(String value) {
    organization = value;
  }

  /**
   * Returns the library section associated with the meeting.
   *
   * @return the library section {@link NodeRef}, or {@code null} if none is set
   */
  public NodeRef getLibrarySection() {
    return librarySection;
  }

  /**
   * Sets the library section associated with the meeting.
   *
   * @param value the library section {@link NodeRef} to set
   */
  public void setLibrarySection(NodeRef value) {
    librarySection = value;
  }

  /**
   * Builds the subset of Alfresco node properties relevant to the given update scope, adding
   * meeting-specific properties on top of those produced by the superclass.
   *
   * <p>Only the properties matching {@code updateInfo} are populated: general information (agenda,
   * availability, organization, type), audience (accepted/rejected users) or the relevant space
   * (library section). Values that are {@code null} or empty are omitted.
   *
   * @param updateInfo the scope of the update indicating which group of properties to include
   * @return a {@link PropertyMap} containing the properties for the requested scope
   */
  @Override
  public PropertyMap getProperties(AppointmentUpdateInfo updateInfo) {
    PropertyMap properties = super.getProperties(updateInfo);

    switch (updateInfo) {
      case GeneralInformation:
        if (this.getAgenda() != null) {
          properties.put(EventModel.PROP_MEETING_AGENDA, this.getAgenda());
        }
        if (this.getAvailability() != null) {
          properties.put(
            EventModel.PROP_MEETING_AVAILABILITY,
            this.getAvailability()
          );
        }
        if (this.getOrganization() != null) {
          properties.put(
            EventModel.PROP_MEETING_ORGAINZATION,
            this.getOrganization()
          );
        }
        if (this.getMeetingTypeString() != null) {
          properties.put(
            EventModel.PROP_MEETING_TYPE,
            this.getMeetingTypeString()
          );
        }
        break;
      case Audience:
        String acceptedUsersList = this.getAcceptedUsersList();
        if (acceptedUsersList != null && !acceptedUsersList.isEmpty()) {
          properties.put(
            EventModel.PROP_MEETING_ACCEPTED_USERS,
            acceptedUsersList
          );
        }

        String rejectedUsersList = this.getRejectedUsersList();
        if (rejectedUsersList != null && !rejectedUsersList.isEmpty()) {
          properties.put(
            EventModel.PROP_MEETING_REJECTED_USERS,
            rejectedUsersList
          );
        }
        break;
      case ContactInformation:
        break;
      case RelevantSpace:
        properties.put(
          EventModel.PROP_MEETING_LIBRARY_SECTION,
          this.getLibrarySection()
        );

        break;
      default:
        break;
    }

    return properties;
  }

  /**
   * Builds the full set of Alfresco node properties for the meeting, combining the superclass
   * properties with all meeting-specific properties (sequence, agenda, availability, organization,
   * type, library section and the accepted/rejected user lists).
   *
   * <p>The sequence is always written; the remaining meeting attributes are added only when set.
   *
   * @return a {@link PropertyMap} containing the complete meeting properties
   */
  @Override
  public PropertyMap getProperties() {
    PropertyMap properties = super.getProperties();

    properties.put(EventModel.PROP_SEQUENCE, this.getSequence());

    if (this.getAgenda() != null) {
      properties.put(EventModel.PROP_MEETING_AGENDA, this.getAgenda());
    }
    if (this.getAvailability() != null) {
      properties.put(
        EventModel.PROP_MEETING_AVAILABILITY,
        this.getAvailability()
      );
    }
    if (this.getOrganization() != null) {
      properties.put(
        EventModel.PROP_MEETING_ORGAINZATION,
        this.getOrganization()
      );
    }
    if (this.getMeetingTypeString() != null) {
      properties.put(EventModel.PROP_MEETING_TYPE, this.getMeetingTypeString());
    }
    if (this.getLibrarySection() != null) {
      properties.put(
        EventModel.PROP_MEETING_LIBRARY_SECTION,
        this.getLibrarySection()
      );
    }

    if (this.getAcceptedUsersList() != null) {
      properties.put(
        EventModel.PROP_MEETING_ACCEPTED_USERS,
        this.getAcceptedUsersList()
      );
    }

    if (this.getRejectedUsersList() != null) {
      properties.put(
        EventModel.PROP_MEETING_REJECTED_USERS,
        this.getRejectedUsersList()
      );
    }

    return properties;
  }

  /**
   * Serializes a list of user names into a single {@link AppointmentImpl#SEPARATOR}-delimited
   * string, with a leading and trailing separator (for example {@code |userA|userB|}).
   *
   * @param users the user names to serialize; may be {@code null}
   * @return the delimited string, or an empty string when {@code users} is {@code null}
   */
  private String formatUserList(List<String> users) {
    if (users == null) {
      return "";
    }
    StringBuilder result = new StringBuilder(SEPARATOR);
    for (String user : users) {
      result.append(user);
      result.append(SEPARATOR);
    }
    return result.toString();
  }

  /**
   * Returns the rejected invitees as a {@link AppointmentImpl#SEPARATOR}-delimited string.
   *
   * @return the delimited rejected-users string, or an empty string when none are set
   */
  public String getRejectedUsersList() {
    return formatUserList(rejectedUsers);
  }

  /**
   * Returns the accepted invitees as a {@link AppointmentImpl#SEPARATOR}-delimited string.
   *
   * @return the delimited accepted-users string, or an empty string when none are set
   */
  public String getAcceptedUsersList() {
    return formatUserList(acceptedUsers);
  }

  /**
   * Initializes this meeting from an Alfresco node property map.
   *
   * <p>Delegates common appointment initialization to the superclass, then loads the basic
   * meeting-specific properties and, when the audience is closed, rebuilds the audience map from the
   * stored accepted/rejected user lists.
   *
   * @param properties the node property map to read values from
   */
  @Override
  public void init(Map<QName, Serializable> properties) {
    super.init(properties);
    initBasicProperties(properties);
    initAudienceIfClosed(properties);
  }

  /**
   * Loads the basic meeting attributes (sequence, agenda, availability, organization, type and
   * library section) from the given property map, skipping any property that is not present.
   *
   * @param properties the node property map to read values from
   */
  private void initBasicProperties(Map<QName, Serializable> properties) {
    Serializable localSequence = properties.get(EventModel.PROP_SEQUENCE);
    if (localSequence != null) {
      this.setSequence(Integer.valueOf(localSequence.toString()));
    }
    Serializable localAgenda = properties.get(EventModel.PROP_MEETING_AGENDA);
    if (localAgenda != null) {
      this.setAgenda(localAgenda.toString());
    }
    Serializable localAvailability = properties.get(
      EventModel.PROP_MEETING_AVAILABILITY
    );
    if (localAvailability != null) {
      this.setAvailability(
        MeetingAvailability.valueOf(localAvailability.toString())
      );
    }
    Serializable localOrganization = properties.get(
      EventModel.PROP_MEETING_ORGAINZATION
    );
    if (localOrganization != null) {
      this.setOrganization(localOrganization.toString());
    }
    Serializable type = properties.get(EventModel.PROP_MEETING_TYPE);
    if (type != null) {
      this.setMeetingTypeString(type.toString());
    }
    Serializable library = properties.get(
      EventModel.PROP_MEETING_LIBRARY_SECTION
    );
    if (library != null) {
      this.setLibrarySection((NodeRef) library);
    }
  }

  /**
   * When the meeting audience is {@link AudienceStatus#Closed}, parses the stored accepted and
   * rejected user lists and rebuilds the {@code audience} map, assigning each invited user a
   * {@link MeetingRequestStatus}. Does nothing when the audience is not closed.
   *
   * @param properties the node property map to read the user lists from
   */
  private void initAudienceIfClosed(Map<QName, Serializable> properties) {
    if (this.getAudienceStatus() != AudienceStatus.Closed) {
      return;
    }
    parseUserList(
      properties.get(EventModel.PROP_MEETING_ACCEPTED_USERS),
      this::setAcceptedUsers
    );
    parseUserList(
      properties.get(EventModel.PROP_MEETING_REJECTED_USERS),
      this::setRejectedUsers
    );

    audience = new HashMap<>();
    for (String user : this.getInvitedUsers()) {
      audience.put(user, determineUserStatus(user));
    }
  }

  /**
   * Deserializes a {@link AppointmentImpl#SEPARATOR}-delimited user-list property (with leading and
   * trailing separators) into a list of user names and passes it to the given setter. Does nothing
   * when the property is {@code null} or does not contain any entries.
   *
   * @param property the raw delimited property value; may be {@code null}
   * @param setter the consumer that receives the parsed list of user names
   */
  private void parseUserList(
    Serializable property,
    java.util.function.Consumer<List<String>> setter
  ) {
    if (property == null) return;
    String usersList = property.toString();
    if (usersList.length() > 1) {
      usersList = usersList.substring(1, usersList.length() - 1);
      setter.accept(Arrays.asList(usersList.split("\\" + SEPARATOR)));
    }
  }

  /**
   * Determines the request status of a single invited user by checking the accepted and rejected
   * user lists.
   *
   * @param user the user name to evaluate
   * @return {@link MeetingRequestStatus#Accepted} or {@link MeetingRequestStatus#Rejected} when the
   *     user is found in the corresponding list, otherwise {@link MeetingRequestStatus#Pending}
   */
  private MeetingRequestStatus determineUserStatus(String user) {
    if (this.getAcceptedUsersList().contains("|" + user)) {
      return MeetingRequestStatus.Accepted;
    } else if (this.getRejectedUsersList().contains("|" + user)) {
      return MeetingRequestStatus.Rejected;
    }
    return MeetingRequestStatus.Pending;
  }

  /**
   * Sets the list of invitees who have rejected the meeting.
   *
   * @param value the rejected user names to set
   */
  public void setRejectedUsers(List<String> value) {
    rejectedUsers = value;
  }

  /**
   * Sets the list of invitees who have accepted the meeting.
   *
   * @param value the accepted user names to set
   */
  public void setAcceptedUsers(List<String> value) {
    acceptedUsers = value;
  }

  /**
   * Returns the meeting sequence number.
   *
   * @return the sequence number, or {@code null} if none is set
   */
  public Integer getSequence() {
    return sequence;
  }

  /**
   * Sets the meeting sequence number.
   *
   * @param value the sequence number to set
   */
  public void setSequence(Integer value) {
    sequence = value;
  }
}
