package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.PagedEmails;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppDistributionMailsGetTest {

  private AppDistributionMailsGet webscript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppDistributionMailsGet();
    appMessageApi = mock(AppMessageApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("appMessageApi", appMessageApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppDistributionMailsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenAdmin_thenReturnsEmails() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("search")).thenReturn(null);

    PagedEmails pagedEmails = new PagedEmails();
    pagedEmails.setData(Collections.emptyList());
    pagedEmails.setTotal(0);
    when(appMessageApi.getAppDistributionEmails(1, 10, null)).thenReturn(
      pagedEmails
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(pagedEmails, model.get("emails"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsEmails() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("2");
    when(req.getParameter("limit")).thenReturn("25");
    when(req.getParameter("search")).thenReturn("test");

    PagedEmails pagedEmails = new PagedEmails();
    pagedEmails.setData(Collections.emptyList());
    pagedEmails.setTotal(5);
    when(appMessageApi.getAppDistributionEmails(2, 25, "test")).thenReturn(
      pagedEmails
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(5L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("search")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullPageAndLimit_thenUsesDefaults() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("search")).thenReturn(null);

    PagedEmails pagedEmails = new PagedEmails();
    pagedEmails.setData(Collections.emptyList());
    pagedEmails.setTotal(0);
    when(appMessageApi.getAppDistributionEmails(1, 25, null)).thenReturn(
      pagedEmails
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(appMessageApi).getAppDistributionEmails(1, 25, null);
  }

  @Test
  public void testExecuteImpl_whenInvalidPage_thenDefaultsToZero() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("abc");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("search")).thenReturn(null);

    PagedEmails pagedEmails = new PagedEmails();
    pagedEmails.setData(Collections.emptyList());
    pagedEmails.setTotal(0);
    when(appMessageApi.getAppDistributionEmails(0, 10, null)).thenReturn(
      pagedEmails
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(appMessageApi).getAppDistributionEmails(0, 10, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenInvalidLimit_thenThrows() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("abc");
    when(req.getParameter("search")).thenReturn(null);

    webscript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenServiceThrows_thenInternalError() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(req.getParameter("search")).thenReturn(null);
    when(appMessageApi.getAppDistributionEmails(1, 10, null)).thenThrow(
      new RuntimeException("DB error")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
