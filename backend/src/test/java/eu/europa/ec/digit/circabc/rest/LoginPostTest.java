package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class LoginPostTest {

  private LoginPost loginPost;
  private MutableAuthenticationService authenticationService;

  @Before
  public void setUp() throws Exception {
    loginPost = new LoginPost();
    authenticationService = mock(MutableAuthenticationService.class);

    setField("authenticationService", authenticationService);

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
  }

  @Test
  public void testExecuteImpl_whenValidCredentials_thenReturnsTicket()
    throws Exception {
    WebScriptRequest req = mockRequest("user1", "pass1");
    when(authenticationService.getCurrentTicket()).thenReturn("TICKET_123");

    Map<String, Object> model = loginPost.executeImpl(
      req,
      new Status(),
      new Cache()
    );

    assertNotNull(model);
    assertEquals("user1", model.get("username"));
    assertEquals("TICKET_123", model.get("ticket"));
    verify(authenticationService).authenticate("user1", "pass1".toCharArray());
  }

  @Test
  public void testExecuteImpl_whenAuthFails_thenForbidden_andDoesNotReEnable()
    throws Exception {
    WebScriptRequest req = mockRequest("user1", "pass1");

    doThrow(
      new org.alfresco.repo.security.authentication.AuthenticationException(
        "fail"
      )
    )
      .when(authenticationService)
      .authenticate("user1", "pass1".toCharArray());

    Status status = new Status();
    Map<String, Object> model = loginPost.executeImpl(req, status, new Cache());

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(authenticationService, never()).setAuthenticationEnabled(
      anyString(),
      anyBoolean()
    );
  }

  @Test
  public void testExecuteImpl_whenAuthFails_andUserEnabled_thenReturnsForbidden()
    throws Exception {
    WebScriptRequest req = mockRequest("user1", "wrong");
    doThrow(
      new org.alfresco.repo.security.authentication.AuthenticationException(
        "fail"
      )
    )
      .when(authenticationService)
      .authenticate("user1", "wrong".toCharArray());
    when(authenticationService.getAuthenticationEnabled("user1")).thenReturn(
      true
    );

    Status status = new Status();
    Map<String, Object> model = loginPost.executeImpl(req, status, new Cache());

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test(expected = WebScriptException.class)
  public void testExecuteImpl_whenNoBody_thenThrowsBadRequest()
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    when(req.getContent()).thenReturn(null);

    loginPost.executeImpl(req, new Status(), new Cache());
  }

  @Test(expected = WebScriptException.class)
  public void testExecuteImpl_whenEmptyUsername_thenThrowsBadRequest()
    throws Exception {
    WebScriptRequest req = mockRequest("", "pass1");

    loginPost.executeImpl(req, new Status(), new Cache());
  }

  private WebScriptRequest mockRequest(String username, String password)
    throws Exception {
    JSONObject json = new JSONObject();
    json.put("username", username);
    json.put("password", password);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json.toString());

    WebScriptRequest req = mock(WebScriptRequest.class);
    when(req.getContent()).thenReturn(content);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LoginPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(loginPost, value);
  }
}
