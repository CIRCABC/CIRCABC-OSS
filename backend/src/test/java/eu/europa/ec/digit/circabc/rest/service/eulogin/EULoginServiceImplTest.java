package eu.europa.ec.digit.circabc.rest.service.eulogin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.CircabcApi;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.alfresco.UserModel;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.Date;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class EULoginServiceImplTest {

  private EULoginServiceImpl service;
  private CircabcConfig circabcConfig;
  private TicketComponent ticketComponent;
  private PersonService personService;
  private UserService userService;
  private NodeService nodeService;
  private CircabcApi circabcApi;

  @Before
  public void setUp() throws Exception {
    service = new EULoginServiceImpl();

    circabcConfig = mock(CircabcConfig.class);
    ticketComponent = mock(TicketComponent.class);
    personService = mock(PersonService.class);
    userService = mock(UserService.class);
    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);

    setField("circabcConfig", circabcConfig);
    setField("ticketComponent", ticketComponent);
    setField("personService", personService);
    setField("userService", userService);
    setField("nodeService", nodeService);
    setField("circabcApi", circabcApi);

    when(circabcConfig.getCasBaseUrl()).thenReturn("https://ecas.test.eu/cas");
    when(circabcConfig.getCasServiceUrl()).thenReturn(
      "http://localhost:8080/alfresco/service/circabc/eulogin"
    );
    when(circabcConfig.getCasFrontendRedirectUrl()).thenReturn(
      "http://localhost:4200/ui/welcome"
    );
    when(circabcConfig.getCasCookiePath()).thenReturn("/");
  }

  // --- Config getters ---

  @Test
  public void testGetCasBaseUrl() {
    assertEquals("https://ecas.test.eu/cas", service.getCasBaseUrl());
  }

  @Test
  public void testGetCasServiceUrl() {
    assertEquals(
      "http://localhost:8080/alfresco/service/circabc/eulogin",
      service.getCasServiceUrl()
    );
  }

  @Test
  public void testGetFrontendRedirectUrl() {
    assertEquals(
      "http://localhost:4200/ui/welcome",
      service.getFrontendRedirectUrl()
    );
  }

  @Test
  public void testGetCookiePath() {
    assertEquals("/", service.getCookiePath());
  }

  // --- buildServiceUrl ---

  @Test
  public void testBuildServiceUrl_withNullRoute_returnsBaseServiceUrl() {
    String result = service.buildServiceUrl(null);
    assertEquals(
      "http://localhost:8080/alfresco/service/circabc/eulogin",
      result
    );
  }

  @Test
  public void testBuildServiceUrl_withEmptyRoute_returnsBaseServiceUrl() {
    String result = service.buildServiceUrl("");
    assertEquals(
      "http://localhost:8080/alfresco/service/circabc/eulogin",
      result
    );
  }

  @Test
  public void testBuildServiceUrl_withRoute_appendsEncodedRoute() {
    String result = service.buildServiceUrl("/group/123/library");
    assertEquals(
      "http://localhost:8080/alfresco/service/circabc/eulogin?route=%2Fgroup%2F123%2Flibrary",
      result
    );
  }

  // --- buildLoginRedirectUrl ---

  @Test
  public void testBuildLoginRedirectUrl_withNullRoute() {
    String result = service.buildLoginRedirectUrl(null);
    assertTrue(result.startsWith("https://ecas.test.eu/cas/login?service="));
    assertTrue(result.contains("circabc%2Feulogin"));
  }

  @Test
  public void testBuildLoginRedirectUrl_withRoute() {
    String result = service.buildLoginRedirectUrl("/dashboard");
    assertTrue(result.startsWith("https://ecas.test.eu/cas/login?service="));
    assertTrue(result.contains("route"));
    assertTrue(result.contains("dashboard"));
  }

  // --- buildRedirectTarget ---

  @Test
  public void testBuildRedirectTarget_withNullRoute_returnsFrontendUrl() {
    String result = service.buildRedirectTarget(null);
    assertEquals("http://localhost:4200/ui/welcome", result);
  }

  @Test
  public void testBuildRedirectTarget_withEmptyRoute_returnsFrontendUrl() {
    String result = service.buildRedirectTarget("");
    assertEquals("http://localhost:4200/ui/welcome", result);
  }

  @Test
  public void testBuildRedirectTarget_withSlashRoute_returnsFrontendUrl() {
    String result = service.buildRedirectTarget("/");
    assertEquals("http://localhost:4200/ui/welcome", result);
  }

  @Test
  public void testBuildRedirectTarget_withRoute_replacesWelcomePath() {
    String result = service.buildRedirectTarget("/group/abc/library");
    assertEquals("http://localhost:4200/group/abc/library", result);
  }

  // --- generateAlfrescoTicket ---

  @Test
  public void testGenerateAlfrescoTicket_delegatesToTicketComponent() {
    when(ticketComponent.getCurrentTicket("user1", true)).thenReturn(
      "TICKET_abc123"
    );

    String ticket = service.generateAlfrescoTicket("user1");

    assertEquals("TICKET_abc123", ticket);
    verify(ticketComponent).getCurrentTicket("user1", true);
  }

  // --- setCookies ---

  @Test
  public void testSetCookies_setsThreeCookies() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

    service.setCookies(response, "jdoe", "TICKET_xyz", "/dashboard");

    verify(response, times(3)).addCookie(cookieCaptor.capture());

    Cookie usernameCookie = cookieCaptor.getAllValues().get(0);
    assertEquals("username", usernameCookie.getName());
    assertEquals("jdoe", usernameCookie.getValue());
    assertEquals("/", usernameCookie.getPath());

    Cookie ticketCookie = cookieCaptor.getAllValues().get(1);
    assertEquals("ticket", ticketCookie.getName());
    assertEquals("TICKET_xyz", ticketCookie.getValue());

    Cookie routeCookie = cookieCaptor.getAllValues().get(2);
    assertEquals("route", routeCookie.getName());
    assertEquals("/dashboard", routeCookie.getValue());
  }

  @Test
  public void testSetCookies_withNullRoute_setsSlashAsDefault() {
    HttpServletResponse response = mock(HttpServletResponse.class);
    ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

    service.setCookies(response, "jdoe", "TICKET_xyz", null);

    verify(response, times(3)).addCookie(cookieCaptor.capture());

    Cookie routeCookie = cookieCaptor.getAllValues().get(2);
    assertEquals("route", routeCookie.getName());
    assertEquals("/", routeCookie.getValue());
  }

  // --- ensureUserExists (new user) ---

  @Test
  public void testEnsureUserExists_whenNewUser_createsFromLdap() {
    NodeRef guestHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "guest-home"
    );
    CircabcUserDataBean ldapUser = new CircabcUserDataBean();
    ldapUser.setFirstName("John");
    ldapUser.setLastName("Doe");

    when(personService.personExists("jdoe")).thenReturn(false);
    when(userService.getLDAPUserDataByUid("jdoe")).thenReturn(ldapUser);
    when(circabcApi.getGuestHomeNodeRef()).thenReturn(guestHome);

    EULoginUserDetails assertionDetails = new EULoginUserDetails("jdoe");
    service.ensureUserExists("jdoe", assertionDetails);

    ArgumentCaptor<CircabcUserDataBean> captor = ArgumentCaptor.forClass(
      CircabcUserDataBean.class
    );
    verify(userService).createUser(captor.capture(), eq(true));
    verify(personService, never()).getPerson(anyString());

    CircabcUserDataBean created = captor.getValue();
    assertEquals("", created.getCompanyId());
    assertEquals("", created.getURL());
    assertEquals(Boolean.FALSE, created.getVisibility());
    assertEquals(Boolean.TRUE, created.getGlobalNotification());
    assertNotNull(created.getLastLoginTime());
    assertNotNull(created.getLastModificationDetailsTime());
    assertNotNull(created.getCreationDate());
  }

  @Test
  public void testEnsureUserExists_whenNewUserAndLdapUnavailable_usesAssertionFallback() {
    NodeRef guestHome = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "guest-home"
    );

    when(personService.personExists("jdoe")).thenReturn(false);
    when(userService.getLDAPUserDataByUid("jdoe")).thenReturn(null);
    when(circabcApi.getGuestHomeNodeRef()).thenReturn(guestHome);

    EULoginUserDetails assertionDetails = new EULoginUserDetails("jdoe");
    assertionDetails.setEmail("john.doe@ec.europa.eu");
    assertionDetails.setFirstName("John");
    assertionDetails.setLastName("Doe");
    assertionDetails.setDomain("eu.europa.ec");
    assertionDetails.setMoniker("jdoe-moniker");

    service.ensureUserExists("jdoe", assertionDetails);

    ArgumentCaptor<CircabcUserDataBean> captor = ArgumentCaptor.forClass(
      CircabcUserDataBean.class
    );
    verify(userService).createUser(captor.capture(), eq(true));

    CircabcUserDataBean created = captor.getValue();
    assertEquals("jdoe", created.getUserName());
    assertEquals("john.doe@ec.europa.eu", created.getEmail());
    assertEquals("John", created.getFirstName());
    assertEquals("Doe", created.getLastName());
    assertEquals("eu.europa.ec", created.getDomain());
    assertEquals("jdoe-moniker", created.getEcasUserName());
    assertEquals(guestHome, created.getHomeSpaceNodeRef());

    // Verify fields initialized for proper user storage
    assertEquals("", created.getCompanyId());
    assertEquals("", created.getURL());
    assertEquals(Boolean.FALSE, created.getVisibility());
    assertEquals(Boolean.TRUE, created.getGlobalNotification());
    assertNotNull(created.getLastLoginTime());
    assertNotNull(created.getLastModificationDetailsTime());
    assertNotNull(created.getCreationDate());
  }

  // --- ensureUserExists (existing user, no LDAP update needed) ---

  @Test
  public void testEnsureUserExists_whenExistingUser_updatesLastLogin() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );
    CircabcUserDataBean ldapUser = new CircabcUserDataBean();

    when(personService.personExists("jdoe")).thenReturn(true);
    when(personService.getPerson("jdoe")).thenReturn(personRef);
    when(userService.getLDAPUserDataByUid("jdoe")).thenReturn(ldapUser);
    when(userService.getAuthenticationEnabled("jdoe")).thenReturn(true);

    EULoginUserDetails assertionDetails = new EULoginUserDetails("jdoe");
    service.ensureUserExists("jdoe", assertionDetails);

    verify(nodeService).setProperty(
      eq(personRef),
      eq(UserModel.PROP_LAST_LOGIN_TIME),
      any(Date.class)
    );
    verify(userService, never()).createUser(any(), anyBoolean());
  }

  // --- ensureUserExists (existing user, LDAP data newer) ---

  @Test
  public void testEnsureUserExists_whenLdapDataNewer_updatesUser() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );

    Date oldDate = new Date(1000000L);
    Date newDate = new Date(2000000L);

    CircabcUserDataBean ldapUser = mock(CircabcUserDataBean.class);
    when(ldapUser.getLastModificationDetailsTime()).thenReturn(newDate);

    CircabcUserDataBean repoUser = mock(CircabcUserDataBean.class);

    when(personService.personExists("jdoe")).thenReturn(true);
    when(personService.getPerson("jdoe")).thenReturn(personRef);
    when(userService.getLDAPUserDataByUid("jdoe")).thenReturn(ldapUser);
    when(userService.getAuthenticationEnabled("jdoe")).thenReturn(true);
    when(
      nodeService.getProperty(
        personRef,
        UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME
      )
    ).thenReturn(oldDate);
    when(userService.getCircabcUserDataBean("jdoe")).thenReturn(repoUser);

    EULoginUserDetails assertionDetails = new EULoginUserDetails("jdoe");
    service.ensureUserExists("jdoe", assertionDetails);

    verify(repoUser).copyLdapProperties(ldapUser);
    verify(userService).updateUser(repoUser);
  }

  // --- ensureUserExists (existing user, disabled auth) ---

  @Test(
    expected = org.alfresco.repo.security.authentication
      .AuthenticationException.class
  )
  public void testEnsureUserExists_whenAuthDisabled_thenDenies() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );
    CircabcUserDataBean ldapUser = new CircabcUserDataBean();

    when(personService.personExists("jdoe")).thenReturn(true);
    when(personService.getPerson("jdoe")).thenReturn(personRef);
    when(userService.getLDAPUserDataByUid("jdoe")).thenReturn(ldapUser);
    when(userService.getAuthenticationEnabled("jdoe")).thenReturn(false);

    EULoginUserDetails assertionDetails = new EULoginUserDetails("jdoe");
    service.ensureUserExists("jdoe", assertionDetails);
  }

  // --- Helper ---

  private void setField(String fieldName, Object value) throws Exception {
    Field field = EULoginServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
