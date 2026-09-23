package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.AudienceStatus;
import io.swagger.model.MeetingAvailability;
import io.swagger.model.MeetingRequestStatus;
import io.swagger.model.alfresco.EventModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.junit.Before;
import org.junit.Test;

public class MeetingImplTest {

  private MeetingImpl meeting;

  @Before
  public void setUp() {
    meeting = new MeetingImpl();
  }

  @Test
  public void testGetSetAgenda() {
    assertNull(meeting.getAgenda());
    meeting.setAgenda("Discuss budget");
    assertEquals("Discuss budget", meeting.getAgenda());
  }

  @Test
  public void testGetSetAvailability() {
    assertNull(meeting.getAvailability());
    meeting.setAvailability(MeetingAvailability.Public);
    assertEquals(MeetingAvailability.Public, meeting.getAvailability());
  }

  @Test
  public void testGetAvailabilityAsString() {
    meeting.setAvailability(MeetingAvailability.Private);
    assertEquals("PRIVATE", meeting.getAvailabilityAsString());
  }

  @Test
  public void testGetSetMeetingTypeString() {
    assertNull(meeting.getMeetingTypeString());
    meeting.setMeetingTypeString("FaceToFace");
    assertEquals("FaceToFace", meeting.getMeetingTypeString());
  }

  @Test
  public void testGetSetOrganization() {
    assertNull(meeting.getOrganization());
    meeting.setOrganization("EC");
    assertEquals("EC", meeting.getOrganization());
  }

  @Test
  public void testGetSetLibrarySection() {
    assertNull(meeting.getLibrarySection());
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-id"
    );
    meeting.setLibrarySection(ref);
    assertEquals(ref, meeting.getLibrarySection());
  }

  @Test
  public void testGetSetSequence() {
    assertNull(meeting.getSequence());
    meeting.setSequence(3);
    assertEquals(Integer.valueOf(3), meeting.getSequence());
  }

  @Test
  public void testFormatUserList_whenUsersSet_thenReturnsSeparatedString() {
    meeting.setAcceptedUsers(Arrays.asList("userA", "userB"));
    assertEquals("|userA|userB|", meeting.getAcceptedUsersList());
  }

  @Test
  public void testFormatUserList_whenNull_thenReturnsEmpty() {
    assertEquals("", meeting.getAcceptedUsersList());
    assertEquals("", meeting.getRejectedUsersList());
  }

  @Test
  public void testGetProperties_whenAllFieldsSet_thenIncludesMeetingProperties() {
    meeting.setAgenda("Agenda");
    meeting.setAvailability(MeetingAvailability.Public);
    meeting.setOrganization("DG DIGIT");
    meeting.setMeetingTypeString("Virtual");
    meeting.setSequence(1);
    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-1"
    );
    meeting.setLibrarySection(libRef);
    meeting.setAcceptedUsers(Arrays.asList("user1"));
    meeting.setRejectedUsers(Arrays.asList("user2"));

    PropertyMap properties = meeting.getProperties();

    assertEquals("Agenda", properties.get(EventModel.PROP_MEETING_AGENDA));
    assertEquals(
      MeetingAvailability.Public,
      properties.get(EventModel.PROP_MEETING_AVAILABILITY)
    );
    assertEquals(
      "DG DIGIT",
      properties.get(EventModel.PROP_MEETING_ORGAINZATION)
    );
    assertEquals("Virtual", properties.get(EventModel.PROP_MEETING_TYPE));
    assertEquals(
      libRef,
      properties.get(EventModel.PROP_MEETING_LIBRARY_SECTION)
    );
    assertEquals(Integer.valueOf(1), properties.get(EventModel.PROP_SEQUENCE));
    assertEquals(
      "|user1|",
      properties.get(EventModel.PROP_MEETING_ACCEPTED_USERS)
    );
    assertEquals(
      "|user2|",
      properties.get(EventModel.PROP_MEETING_REJECTED_USERS)
    );
  }

  @Test
  public void testGetProperties_whenFieldsNull_thenExcludesMeetingProperties() {
    meeting.setSequence(null);

    PropertyMap properties = meeting.getProperties();

    assertNull(properties.get(EventModel.PROP_MEETING_AGENDA));
    assertNull(properties.get(EventModel.PROP_MEETING_AVAILABILITY));
    assertNull(properties.get(EventModel.PROP_MEETING_ORGAINZATION));
    assertNull(properties.get(EventModel.PROP_MEETING_TYPE));
    assertNull(properties.get(EventModel.PROP_MEETING_LIBRARY_SECTION));
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenGeneralInformation_thenIncludesMeetingFields() {
    meeting.setAgenda("Test Agenda");
    meeting.setAvailability(MeetingAvailability.Private);
    meeting.setOrganization("Org");
    meeting.setMeetingTypeString("Phone");

    PropertyMap properties = meeting.getProperties(
      AppointmentUpdateInfo.GeneralInformation
    );

    assertEquals("Test Agenda", properties.get(EventModel.PROP_MEETING_AGENDA));
    assertEquals(
      MeetingAvailability.Private,
      properties.get(EventModel.PROP_MEETING_AVAILABILITY)
    );
    assertEquals("Org", properties.get(EventModel.PROP_MEETING_ORGAINZATION));
    assertEquals("Phone", properties.get(EventModel.PROP_MEETING_TYPE));
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenAudience_thenIncludesUserLists() {
    meeting.setAcceptedUsers(Arrays.asList("alice", "bob"));
    meeting.setRejectedUsers(Arrays.asList("charlie"));

    PropertyMap properties = meeting.getProperties(
      AppointmentUpdateInfo.Audience
    );

    assertEquals(
      "|alice|bob|",
      properties.get(EventModel.PROP_MEETING_ACCEPTED_USERS)
    );
    assertEquals(
      "|charlie|",
      properties.get(EventModel.PROP_MEETING_REJECTED_USERS)
    );
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenRelevantSpace_thenIncludesLibrarySection() {
    NodeRef ref = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-1"
    );
    meeting.setLibrarySection(ref);

    PropertyMap properties = meeting.getProperties(
      AppointmentUpdateInfo.RelevantSpace
    );

    assertEquals(ref, properties.get(EventModel.PROP_MEETING_LIBRARY_SECTION));
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenContactInformation_thenNoMeetingFields() {
    meeting.setAgenda("Agenda");
    meeting.setOrganization("Org");

    PropertyMap properties = meeting.getProperties(
      AppointmentUpdateInfo.ContactInformation
    );

    assertNull(properties.get(EventModel.PROP_MEETING_AGENDA));
    assertNull(properties.get(EventModel.PROP_MEETING_ORGAINZATION));
  }

  @Test
  public void testInit_whenPropertiesContainMeetingFields_thenSetsFields() {
    Map<QName, Serializable> properties = buildMinimalInitProperties();
    properties.put(EventModel.PROP_SEQUENCE, "5");
    properties.put(EventModel.PROP_MEETING_AGENDA, "Init Agenda");
    properties.put(EventModel.PROP_MEETING_AVAILABILITY, "Public");
    properties.put(EventModel.PROP_MEETING_ORGAINZATION, "Init Org");
    properties.put(EventModel.PROP_MEETING_TYPE, "Virtual");
    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "init-lib"
    );
    properties.put(EventModel.PROP_MEETING_LIBRARY_SECTION, libRef);

    meeting.init(properties);

    assertEquals(Integer.valueOf(5), meeting.getSequence());
    assertEquals("Init Agenda", meeting.getAgenda());
    assertEquals(MeetingAvailability.Public, meeting.getAvailability());
    assertEquals("Init Org", meeting.getOrganization());
    assertEquals("Virtual", meeting.getMeetingTypeString());
    assertEquals(libRef, meeting.getLibrarySection());
  }

  @Test
  public void testInit_whenAudienceClosed_thenParsesAcceptedAndRejectedUsers() {
    Map<QName, Serializable> properties = buildMinimalInitProperties();
    properties.put(EventModel.PROP_EVENT_AUDIENCE, "Closed");
    properties.put(EventModel.PROP_EVENT_INVITED_USERS, "|alice|bob|charlie|");
    properties.put(EventModel.PROP_MEETING_ACCEPTED_USERS, "|alice|bob|");
    properties.put(EventModel.PROP_MEETING_REJECTED_USERS, "|charlie|");

    meeting.init(properties);

    assertEquals(AudienceStatus.Closed, meeting.getAudienceStatus());
    assertNotNull(meeting.getAudience());
    assertEquals(
      MeetingRequestStatus.Accepted,
      meeting.getAudience().get("alice")
    );
    assertEquals(
      MeetingRequestStatus.Accepted,
      meeting.getAudience().get("bob")
    );
    assertEquals(
      MeetingRequestStatus.Rejected,
      meeting.getAudience().get("charlie")
    );
  }

  @Test
  public void testInit_whenAudienceOpen_thenDoesNotParseUserLists() {
    Map<QName, Serializable> properties = buildMinimalInitProperties();
    properties.put(EventModel.PROP_EVENT_AUDIENCE, "Open");
    properties.put(EventModel.PROP_MEETING_ACCEPTED_USERS, "|alice|");

    meeting.init(properties);

    assertEquals(AudienceStatus.Open, meeting.getAudienceStatus());
    assertTrue(meeting.getAudience().isEmpty());
  }

  @Test
  public void testInit_whenMeetingFieldsAbsent_thenFieldsRemainNull() {
    Map<QName, Serializable> properties = buildMinimalInitProperties();

    meeting.init(properties);

    assertNull(meeting.getSequence());
    assertNull(meeting.getAgenda());
    assertNull(meeting.getAvailability());
    assertNull(meeting.getOrganization());
    assertNull(meeting.getMeetingTypeString());
    assertNull(meeting.getLibrarySection());
  }

  private Map<QName, Serializable> buildMinimalInitProperties() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(EventModel.PROP_EVENT_TITLE, "Title");
    props.put(EventModel.PROP_EVENT_AUDIENCE, "Open");
    props.put(EventModel.PROP_EVENT_START_DATE, new java.util.Date());
    props.put(EventModel.PROP_EVENT_START_TIME, "10:00");
    props.put(EventModel.PROP_EVENT_END_TIME, "11:00");
    props.put(EventModel.PROP_EVENT_ABSTRACT, "Abstract");
    props.put(EventModel.PROP_EVENT_EMAIL, "test@test.com");
    props.put(EventModel.PROP_EVENT_INVITATION_MESSAGE, "Welcome");
    props.put(EventModel.PROP_EVENT_LANGUAGE, "en");
    props.put(EventModel.PROP_EVENT_LOCATION, "Brussels");
    props.put(EventModel.PROP_EVENT_NAME, "Name");
    props.put(EventModel.PROP_EVENT_PHONE, "123456");
    props.put(EventModel.PROP_EVENT_URL, "http://example.com");
    props.put(EventModel.PROP_EVENT_TIMEZONE, "Europe/Brussels");
    props.put(EventModel.PROP_EVENT_OCCURENCE_RATE, "OnlyOnce|null|null|1|1");
    return props;
  }
}
