package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserActionLog;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

@SuppressWarnings("unchecked")
public class UsersDashboardUploadsGetTest {

  private UsersDashboardUploadsGet webscript;
  private DashboardApi dashboardApi;
  private AuthenticationService authenticationService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new UsersDashboardUploadsGet();
    dashboardApi = mock(DashboardApi.class);
    authenticationService = mock(AuthenticationService.class);

    setField("dashboardApi", dashboardApi);
    setField("authenticationService", authenticationService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
  }

  @Test
  public void testExecuteImpl_whenValidUser_thenReturnsUploads()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);

    List<UserActionLog> uploads = new ArrayList<>();
    uploads.add(new UserActionLog());
    when(dashboardApi.usersUserIdDashboardUploadsGet("testuser")).thenReturn(
      uploads
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(uploads, model.get("uploads"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsUploads()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");

    List<UserActionLog> uploads = new ArrayList<>();
    when(dashboardApi.usersUserIdDashboardUploadsGet("testuser")).thenReturn(
      uploads
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(uploads, model.get("uploads"));
  }

  @Test
  public void testExecuteImpl_whenDifferentUser_thenForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(authenticationService.getCurrentUserName()).thenReturn("otheruser");

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(dashboardApi.usersUserIdDashboardUploadsGet("testuser")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    return (Map<String, Object>) method.invoke(webscript, req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersDashboardUploadsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
