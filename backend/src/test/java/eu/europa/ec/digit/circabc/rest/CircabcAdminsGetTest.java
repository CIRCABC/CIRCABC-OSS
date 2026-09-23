package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CircabcAdminsGetTest {

  private CircabcAdminsGet circabcAdminsGet;
  private CircabcApi circabcApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    circabcAdminsGet = new CircabcAdminsGet();
    circabcApi = mock(CircabcApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("circabcApi", circabcApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsAdmins()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    List<User> admins = List.of(new User());
    when(circabcApi.getCircabcAdmins()).thenReturn(admins);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(admins, model.get("admins"));
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsAdmins()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(circabcApi.getCircabcAdmins()).thenReturn(Collections.emptyList());

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("admins"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    when(circabcApi.getCircabcAdmins()).thenReturn(Collections.emptyList());

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      CircabcAdminsGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      circabcAdminsGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcAdminsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(circabcAdminsGet, value);
  }
}
