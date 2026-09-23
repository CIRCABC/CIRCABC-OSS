package eu.europa.ec.digit.circabc.rest.service.event;

import static org.junit.Assert.*;

import io.swagger.model.*;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventServiceTest {

  private EventService eventService;

  @Before
  public void setUp() {
    eventService = Mockito.mock(EventService.class);
  }

  @Test
  public void testCreateEvent() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );
    Event event = Mockito.mock(Event.class);
    NodeRef expected = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-event"
    );

    Mockito.when(eventService.createEvent(eventRoot, event)).thenReturn(
      expected
    );

    NodeRef result = eventService.createEvent(eventRoot, event);
    assertEquals(expected, result);
    Mockito.verify(eventService).createEvent(eventRoot, event);
  }

  @Test
  public void testGetAllAppointments() {
    NodeRef eventRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-root"
    );

    Mockito.when(eventService.getAllAppointments(eventRoot)).thenReturn(
      List.of()
    );

    List<Appointment> result = eventService.getAllAppointments(eventRoot);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testDeleteAppointment() {
    NodeRef appointmentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "appt-1"
    );

    eventService.deleteAppointment(appointmentRef, UpdateMode.Single);

    Mockito.verify(eventService).deleteAppointment(
      appointmentRef,
      UpdateMode.Single
    );
  }

  @Test
  public void testGetIGsEventRoot() {
    NodeRef expected = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-event-root"
    );
    Mockito.when(eventService.getIGsEventRoot("ig-1")).thenReturn(expected);

    NodeRef result = eventService.getIGsEventRoot("ig-1");
    assertEquals(expected, result);
  }
}
