package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.model.CircabcUserDataBean;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TicketGetTest {

  private TicketGet ticketGet;
  private TicketComponent ticketComponent;
  private PersonService personService;
  private UserService userService;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private LogService logService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    ticketGet = new TicketGet();
    ticketComponent = mock(TicketComponent.class);
    personService = mock(PersonService.class);
    userService = mock(UserService.class);
    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);
    logService = mock(LogService.class);

    setField(TicketGet.class, "ticketComponent", ticketComponent);
    setField(TicketGet.class, "personService", personService);
    setField(TicketGet.class, "userService", userService);
    setField(TicketGet.class, "nodeService", nodeService);
    setField(TicketGet.class, "circabcApi", circabcApi);
    setField(CircabcDeclarativeWebScript.class, "logService", logService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = Map.of("id", "johndoe");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenExistingUser_thenReturnsTicket()
    throws Exception {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    NodeRef circabcRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "circabc-id"
    );
    CircabcUserDataBean ldapUser = mock(CircabcUserDataBean.class);

    when(personService.personExists("johndoe")).thenReturn(true);
    when(personService.getPerson("johndoe")).thenReturn(personRef);
    when(userService.getLDAPUserDataByUid("johndoe")).thenReturn(ldapUser);
    when(ldapUser.getLastModificationDetailsTime()).thenReturn(null);
    when(userService.getAuthenticationEnabled("johndoe")).thenReturn(true);
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRef);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      nodeService.getProperty(circabcRef, ContentModel.PROP_NAME)
    ).thenReturn("CircaBC");
    when(ticketComponent.getCurrentTicket("johndoe", true)).thenReturn(
      "TICKET_abc123"
    );

    Map<String, Object> result = ticketGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("TICKET_abc123", result.get("ticket"));
    verify(logService).log(any());
  }

  @Test
  public void testExecuteImpl_whenNewUser_thenCreatesUserAndReturnsTicket()
    throws Exception {
    NodeRef guestHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "guest-home"
    );
    CircabcUserDataBean ldapUser = mock(CircabcUserDataBean.class);

    when(personService.personExists("johndoe")).thenReturn(false);
    when(userService.getLDAPUserDataByUid("johndoe")).thenReturn(ldapUser);
    when(circabcApi.getGuestHomeNodeRef()).thenReturn(guestHome);
    when(circabcApi.getCircabcNodeRef()).thenReturn(null);
    when(ticketComponent.getCurrentTicket("johndoe", true)).thenReturn(
      "TICKET_new"
    );

    Map<String, Object> result = ticketGet.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("TICKET_new", result.get("ticket"));
    verify(userService).createUser(any(CircabcUserDataBean.class), eq(true));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(personService.personExists("johndoe")).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> result = ticketGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError()
    throws Exception {
    when(personService.personExists("johndoe")).thenThrow(
      new RuntimeException("boom")
    );

    Map<String, Object> result = ticketGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(ticketGet, value);
  }
}
