package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AutoUploadApi;
import io.swagger.model.Configuration;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AutoUploadsGetTest {

  private AutoUploadsGet autoUploadsGet;
  private AutoUploadApi autoUploadApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    autoUploadsGet = new AutoUploadsGet();
    autoUploadApi = mock(AutoUploadApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("autoUploadApi", autoUploadApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    templateVars = new HashMap<>();
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AutoUploadsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(autoUploadsGet, value);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsPagedResults() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    List<Configuration> data = Collections.singletonList(new Configuration());
    PagedAutoUploadConfiguration paged = new PagedAutoUploadConfiguration(
      data,
      1L
    );
    when(autoUploadApi.getAutoUploadEntries("ig-123", 0, 10)).thenReturn(paged);

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(data, model.get("autouploads"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNoPageParam_thenDefaultsToPageOne() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("5");

    List<Configuration> data = Collections.emptyList();
    PagedAutoUploadConfiguration paged = new PagedAutoUploadConfiguration(
      data,
      0L
    );
    when(autoUploadApi.getAutoUploadEntries("ig-123", 0, 5)).thenReturn(paged);

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(data, model.get("autouploads"));
    assertEquals(0L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      false
    );

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidPageValue_thenNotAcceptable() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("page")).thenReturn("abc");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageZero_thenNotAcceptable() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNegativeLimit_thenNotAcceptable() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("-1");

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNoLimitParam_thenDefaultsToZero() {
    templateVars.put("id", "ig-123");
    when(currentUserPermissionCheckerService.isGroupAdmin("ig-123")).thenReturn(
      true
    );
    when(req.getParameter("page")).thenReturn("2");
    when(req.getParameter("limit")).thenReturn(null);

    List<Configuration> data = Collections.emptyList();
    PagedAutoUploadConfiguration paged = new PagedAutoUploadConfiguration(
      data,
      0L
    );
    when(autoUploadApi.getAutoUploadEntries("ig-123", 0, 0)).thenReturn(paged);

    Map<String, Object> model = autoUploadsGet.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(autoUploadApi).getAutoUploadEntries("ig-123", 0, 0);
  }
}
