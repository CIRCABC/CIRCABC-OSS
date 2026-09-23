package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HistoryApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HistoryMembershipsRevocationGetTest {

  private HistoryMembershipsRevocationGet webscript;
  private CurrentUserPermissionCheckerService permissionChecker;
  private HistoryApi historyApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new HistoryMembershipsRevocationGet();
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    historyApi = mock(HistoryApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("historyApi", historyApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HistoryMembershipsRevocationGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsRevocations() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("page")).thenReturn("2");

    Object expected = new Object();
    when(historyApi.getRevocations(10, 2)).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(historyApi).getRevocations(10, 2);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsRevocations() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(historyApi).getRevocations(25, 0);
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsServerError() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("limit")).thenReturn("5");
    when(req.getParameter("page")).thenReturn("1");
    when(historyApi.getRevocations(5, 1)).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("DB error", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenInvalidLimit_thenReturnsServerError() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("limit")).thenReturn("abc");
    when(req.getParameter("page")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
