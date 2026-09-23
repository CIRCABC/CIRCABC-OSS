package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import io.swagger.model.AppointmentUpdateInfo;
import io.swagger.model.EventPriority;
import io.swagger.model.EventType;
import io.swagger.model.alfresco.EventModel;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.junit.Before;
import org.junit.Test;

public class EventImplTest {

  private EventImpl event;

  @Before
  public void setUp() {
    event = new EventImpl();
  }

  @Test
  public void testGetSetEventType() {
    assertNull(event.getEventType());
    event.setEventType(EventType.Task);
    assertEquals(EventType.Task, event.getEventType());
  }

  @Test
  public void testGetSetPriority() {
    assertNull(event.getPriority());
    event.setPriority(EventPriority.High);
    assertEquals(EventPriority.High, event.getPriority());
  }

  @Test
  public void testGetProperties_whenPriorityAndTypeSet_thenIncludesEventProperties() {
    event.setPriority(EventPriority.Urgent);
    event.setEventType(EventType.Appointment);
    event.setTitle("Test Event");

    PropertyMap properties = event.getProperties();

    assertEquals(
      EventPriority.Urgent,
      properties.get(EventModel.PROP_EVENT_PRIORITY)
    );
    assertEquals(
      EventType.Appointment,
      properties.get(EventModel.PROP_EVENT_TYPE)
    );
  }

  @Test
  public void testGetProperties_whenPriorityAndTypeNull_thenExcludesEventProperties() {
    event.setTitle("Test Event");

    PropertyMap properties = event.getProperties();

    assertNull(properties.get(EventModel.PROP_EVENT_PRIORITY));
    assertNull(properties.get(EventModel.PROP_EVENT_TYPE));
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenGeneralInformation_thenIncludesEventProperties() {
    event.setPriority(EventPriority.Medium);
    event.setEventType(EventType.Other);

    PropertyMap properties = event.getProperties(
      AppointmentUpdateInfo.GeneralInformation
    );

    assertEquals(
      EventPriority.Medium,
      properties.get(EventModel.PROP_EVENT_PRIORITY)
    );
    assertEquals(EventType.Other, properties.get(EventModel.PROP_EVENT_TYPE));
  }

  @Test
  public void testGetPropertiesWithUpdateInfo_whenNotGeneralInformation_thenExcludesEventProperties() {
    event.setPriority(EventPriority.Low);
    event.setEventType(EventType.Task);

    PropertyMap properties = event.getProperties(
      AppointmentUpdateInfo.ContactInformation
    );

    assertNull(properties.get(EventModel.PROP_EVENT_PRIORITY));
    assertNull(properties.get(EventModel.PROP_EVENT_TYPE));
  }

  @Test
  public void testInit_whenPropertiesContainPriorityAndType_thenSetsFields() {
    Map<QName, Serializable> properties = buildMinimalInitProperties();
    properties.put(EventModel.PROP_EVENT_PRIORITY, "High");
    properties.put(EventModel.PROP_EVENT_TYPE, "Task");

    event.init(properties);

    assertEquals(EventPriority.High, event.getPriority());
    assertEquals(EventType.Task, event.getEventType());
  }

  @Test
  public void testInit_whenPropertiesLackPriorityAndType_thenFieldsRemainNull() {
    Map<QName, Serializable> properties = buildMinimalInitProperties();

    event.init(properties);

    assertNull(event.getPriority());
    assertNull(event.getEventType());
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
