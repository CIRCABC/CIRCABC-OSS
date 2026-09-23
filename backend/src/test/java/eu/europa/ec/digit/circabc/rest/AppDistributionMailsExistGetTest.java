package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.db.DistributionEmailDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppDistributionMailsExistGetTest {

  private AppDistributionMailsExistGet webscript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppDistributionMailsExistGet();
    appMessageApi = mock(AppMessageApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("appMessageApi", appMessageApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsEmail() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    DistributionEmailDAO dao = new DistributionEmailDAO();
    when(appMessageApi.getSubscribedDistributionEmail("testuser")).thenReturn(
      dao
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(dao, result.get("email"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsEmail() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    DistributionEmailDAO dao = new DistributionEmailDAO();
    when(appMessageApi.getSubscribedDistributionEmail("testuser")).thenReturn(
      dao
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(dao, result.get("email"));
  }

  @Test
  public void testExecuteImpl_whenCurrentUser_thenReturnsEmail() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    DistributionEmailDAO dao = new DistributionEmailDAO();
    when(appMessageApi.getSubscribedDistributionEmail("testuser")).thenReturn(
      dao
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(dao, result.get("email"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsServerError() {
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(appMessageApi.getSubscribedDistributionEmail("testuser")).thenThrow(
      new RuntimeException("db error")
    );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppDistributionMailsExistGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
