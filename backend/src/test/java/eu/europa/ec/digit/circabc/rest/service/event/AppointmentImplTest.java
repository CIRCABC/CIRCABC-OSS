package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import io.swagger.model.*;
import io.swagger.model.alfresco.EventModel;
import java.io.Serializable;
import java.util.*;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class AppointmentImplTest {

  private AppointmentImpl appointment;

  @Before
  public void setUp() {
    appointment = new AppointmentImpl();
  }

  @Test
  public void testGetProperties_whenAllFieldsSet_thenReturnsPopulatedMap() {
    appointment.setTitle("Test Event");
    appointment.setEventAbstract("Abstract");
    appointment.setEmail("test@example.com");
    appointment.setInvitationMessage("Please join");
    appointment.setLanguage("EN");
    appointment.setLocation("Brussels");
    appointment.setName("John");
    appointment.setPhone("+32123456");
    appointment.setUrl("http://example.com");
    appointment.setTimeZoneId("Europe/Brussels");
    appointment.setAudienceStatus(AudienceStatus.Open);
    appointment.setStartDate(new DateValueImpl(2026, 5, 4));
    appointment.setStartTime(new LocalTime(10, 0));
    appointment.setEndTime(new LocalTime(11, 0));
    appointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));
    appointment.setInvitedUsers(Arrays.asList("user1", "user2"));

    PropertyMap props = appointment.getProperties();

    assertEquals("Test Event", props.get(EventModel.PROP_EVENT_TITLE));
    assertEquals("Abstract", props.get(EventModel.PROP_EVENT_ABSTRACT));
    assertEquals("test@example.com", props.get(EventModel.PROP_EVENT_EMAIL));
    assertEquals("EN", props.get(EventModel.PROP_EVENT_LANGUAGE));
    assertEquals("Brussels", props.get(EventModel.PROP_EVENT_LOCATION));
    assertEquals("John", props.get(EventModel.PROP_EVENT_NAME));
    assertEquals("+32123456", props.get(EventModel.PROP_EVENT_PHONE));
    assertEquals("http://example.com", props.get(EventModel.PROP_EVENT_URL));
    assertEquals("Europe/Brussels", props.get(EventModel.PROP_EVENT_TIMEZONE));
    assertEquals("Open", props.get(EventModel.PROP_EVENT_AUDIENCE));
    assertEquals("10:00:00.000", props.get(EventModel.PROP_EVENT_START_TIME));
    assertEquals("11:00:00.000", props.get(EventModel.PROP_EVENT_END_TIME));
    assertNotNull(props.get(EventModel.PROP_EVENT_START_DATE));
    assertNotNull(props.get(EventModel.PROP_EVENT_OCCURENCE_RATE));
  }

  @Test
  public void testGetProperties_whenNullFields_thenSkipsNullValues() {
    PropertyMap props = appointment.getProperties();

    assertNull(props.get(EventModel.PROP_EVENT_TITLE));
    assertNull(props.get(EventModel.PROP_EVENT_AUDIENCE));
    assertNull(props.get(EventModel.PROP_EVENT_START_DATE));
  }

  @Test
  public void testGetEventDates_whenOnlyOnce_thenReturnsSingleDate() {
    appointment.setStartDate(new DateValueImpl(2026, 5, 4));
    appointment.setTimeZoneId("Europe/Brussels");
    appointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));

    List<DateValue> dates = appointment.getEventDates();

    assertEquals(1, dates.size());
    assertEquals(2026, dates.get(0).year());
    assertEquals(5, dates.get(0).month());
    assertEquals(4, dates.get(0).day());
  }

  @Test
  public void testGetEventDates_whenTimesDaily_thenReturnsMultipleDates() {
    appointment.setStartDate(new DateValueImpl(2026, 5, 4));
    appointment.setTimeZoneId("Europe/Brussels");
    appointment.setOccurenceRate(
      new OccurenceRate(MainOccurence.Times, TimesOccurence.Daily, 3)
    );

    List<DateValue> dates = appointment.getEventDates();

    assertEquals(3, dates.size());
  }

  @Test(expected = AlfrescoRuntimeException.class)
  public void testGetEventDates_whenOccurenceRateNull_thenThrows() {
    appointment.setStartDate(new DateValueImpl(2026, 5, 4));
    appointment.setTimeZoneId("Europe/Brussels");
    appointment.setOccurenceRate(null);

    appointment.getEventDates();
  }

  @Test(expected = AlfrescoRuntimeException.class)
  public void testGetEventDates_whenTimezoneNull_thenThrows() {
    appointment.setStartDate(new DateValueImpl(2026, 5, 4));
    appointment.setTimeZoneId(null);
    appointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));

    appointment.getEventDates();
  }

  @Test(expected = AlfrescoRuntimeException.class)
  public void testGetEventDates_whenStartDateNull_thenThrows() {
    appointment.setStartDate(null);
    appointment.setTimeZoneId("Europe/Brussels");
    appointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));

    appointment.getEventDates();
  }

  @Test
  public void testGetInvitedUsersList_whenUsersExist_thenFormatsWithSeparator() {
    appointment.setInvitedUsers(Arrays.asList("user1", "user2"));

    String result = appointment.getInvitedUsersList();

    assertEquals("|user1|user2|", result);
  }

  @Test
  public void testGetInvitedUsersList_whenNull_thenReturnsSeparator() {
    appointment.setInvitedUsers(null);

    String result = appointment.getInvitedUsersList();

    assertEquals("|", result);
  }

  @Test
  public void testGetInvitedUsersList_whenEmpty_thenReturnsSeparatorOnly() {
    appointment.setInvitedUsers(Collections.emptyList());

    String result = appointment.getInvitedUsersList();

    assertEquals("|", result);
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenContactInfo_thenReturnsContactFields() {
    appointment.setEmail("test@example.com");
    appointment.setName("John");
    appointment.setPhone("+32123456");
    appointment.setUrl("http://example.com");

    PropertyMap props = appointment.getProperties(
      AppointmentUpdateInfo.ContactInformation
    );

    assertEquals("test@example.com", props.get(EventModel.PROP_EVENT_EMAIL));
    assertEquals("John", props.get(EventModel.PROP_EVENT_NAME));
    assertEquals("+32123456", props.get(EventModel.PROP_EVENT_PHONE));
    assertEquals("http://example.com", props.get(EventModel.PROP_EVENT_URL));
    assertNull(props.get(EventModel.PROP_EVENT_TITLE));
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenAudience_thenReturnsAudienceFields() {
    appointment.setAudienceStatus(AudienceStatus.Closed);
    appointment.setInvitedUsers(Arrays.asList("user1"));

    PropertyMap props = appointment.getProperties(
      AppointmentUpdateInfo.Audience
    );

    assertEquals(
      AudienceStatus.Closed,
      props.get(EventModel.PROP_EVENT_AUDIENCE)
    );
    assertNotNull(props.get(EventModel.PROP_EVENT_INVITED_USERS));
  }

  @Test
  public void testGetRRule_whenOnlyOnce_thenReturnsFreqDaily() {
    appointment.setStartDate(new DateValueImpl(2026, 5, 4));
    appointment.setTimeZoneId("Europe/Brussels");
    appointment.setOccurenceRate(new OccurenceRate(MainOccurence.OnlyOnce));

    String rrule = appointment.getRRule();

    assertEquals("FREQ=DAILY;COUNT=1", rrule);
  }

  @Test
  public void testAddAudience_whenCalled_thenAddsToMap() {
    appointment.setAudienceStatus(AudienceStatus.Closed);
    appointment.setInvitedUsers(Arrays.asList("user1"));
    // Initialize audience via init-like setup
    Map<QName, Serializable> properties = createMinimalProperties();
    appointment.init(properties);

    appointment.addAudience("newUser", MeetingRequestStatus.Accepted);

    HashMap<String, MeetingRequestStatus> audience = appointment.getAudience();
    assertEquals(MeetingRequestStatus.Accepted, audience.get("newUser"));
  }

  @Test
  public void testInit_whenClosedAudience_thenParsesInvitedUsers() {
    Map<QName, Serializable> properties = createMinimalProperties();

    appointment.init(properties);

    assertEquals("Test Event", appointment.getTitle());
    assertEquals(AudienceStatus.Closed, appointment.getAudienceStatus());
    assertNotNull(appointment.getAudience());
    assertTrue(appointment.getAudience().containsKey("user1"));
    assertTrue(appointment.getAudience().containsKey("user2"));
  }

  @Test
  public void testInit_whenInvitationMessageMissing_thenDefaultsToEmptyAndReadsTimeZone() {
    // Regression: reproduces the imported-appointment case. The migration/export
    // format carries no invitation message, so ce:invitationMessage is never
    // persisted. The events-list endpoint loads the full appointment just to
    // read the timezone; init() must not throw a NullPointerException when the
    // property is absent.
    Map<QName, Serializable> properties = createMinimalProperties();
    properties.remove(EventModel.PROP_EVENT_INVITATION_MESSAGE);

    appointment.init(properties);

    assertEquals("", appointment.getInvitationMessage());
    assertEquals("Europe/Brussels", appointment.getTimeZoneId());
    assertEquals("Test Event", appointment.getTitle());
  }

  @Test
  public void testInit_whenOptionalTextPropertiesMissing_thenDefaultToEmpty() {
    // All optional text properties absent must still not throw; they default to
    // "" while the mandatory-ish fields (title, timezone) are read normally.
    Map<QName, Serializable> properties = createMinimalProperties();
    properties.remove(EventModel.PROP_EVENT_INVITATION_MESSAGE);
    properties.remove(EventModel.PROP_EVENT_ABSTRACT);
    properties.remove(EventModel.PROP_EVENT_EMAIL);
    properties.remove(EventModel.PROP_EVENT_LOCATION);
    properties.remove(EventModel.PROP_EVENT_NAME);
    properties.remove(EventModel.PROP_EVENT_PHONE);
    properties.remove(EventModel.PROP_EVENT_URL);
    properties.remove(EventModel.PROP_EVENT_LANGUAGE);

    appointment.init(properties);

    assertEquals("", appointment.getInvitationMessage());
    assertEquals("", appointment.getEventAbstract());
    assertEquals("", appointment.getEmail());
    assertEquals("", appointment.getLocation());
    assertEquals("", appointment.getName());
    assertEquals("", appointment.getPhone());
    assertEquals("", appointment.getUrl());
    assertEquals("", appointment.getLanguage());
    assertEquals("Test Event", appointment.getTitle());
    assertEquals("Europe/Brussels", appointment.getTimeZoneId());
  }

  private Map<QName, Serializable> createMinimalProperties() {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(EventModel.PROP_EVENT_TITLE, "Test Event");
    props.put(EventModel.PROP_EVENT_AUDIENCE, "Closed");
    props.put(EventModel.PROP_EVENT_DATE, new Date());
    props.put(EventModel.PROP_EVENT_START_DATE, new Date());
    props.put(EventModel.PROP_EVENT_START_TIME, "10:00:00.000");
    props.put(EventModel.PROP_EVENT_END_TIME, "11:00:00.000");
    props.put(EventModel.PROP_EVENT_ABSTRACT, "Abstract");
    props.put(EventModel.PROP_EVENT_EMAIL, "test@example.com");
    props.put(EventModel.PROP_EVENT_INVITATION_MESSAGE, "Join us");
    props.put(EventModel.PROP_EVENT_LANGUAGE, "EN");
    props.put(EventModel.PROP_EVENT_LOCATION, "Brussels");
    props.put(EventModel.PROP_EVENT_NAME, "John");
    props.put(EventModel.PROP_EVENT_PHONE, "+32123456");
    props.put(EventModel.PROP_EVENT_URL, "http://example.com");
    props.put(EventModel.PROP_EVENT_TIMEZONE, "Europe/Brussels");
    props.put(EventModel.PROP_EVENT_OCCURENCE_RATE, "OnlyOnce|null|null|-1|-1");
    props.put(EventModel.PROP_EVENT_INVITED_USERS, "|user1|user2|");
    return props;
  }
}
