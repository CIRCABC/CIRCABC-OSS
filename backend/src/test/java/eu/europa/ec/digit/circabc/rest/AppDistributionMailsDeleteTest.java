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

public class AppDistributionMailsDeleteTest {

  private AppDistributionMailsDelete webScript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    webScript = new AppDistributionMailsDelete();
    appMessageApi = mock(AppMessageApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("appMessageApi", appMessageApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    templateVars = new HashMap<>();
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppDistributionMailsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenOwnerDeletes_thenSuccess() throws Exception {
    templateVars.put("id", "42");

    DistributionEmailDAO dao = new DistributionEmailDAO();
    dao.setId(42);
    dao.setEmailAddress("user@example.com");
    when(appMessageApi.getSubscribedDistributionEmailById(42)).thenReturn(dao);
    when(
      permissionChecker.isCurrentUserEmailEqualTo("user@example.com")
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).removeAppDistributionPostEmails(42);
  }

  @Test
  public void testExecuteImpl_whenAdmin_thenSuccess() throws Exception {
    templateVars.put("id", "10");

    DistributionEmailDAO dao = new DistributionEmailDAO();
    dao.setId(10);
    dao.setEmailAddress("other@example.com");
    when(appMessageApi.getSubscribedDistributionEmailById(10)).thenReturn(dao);
    when(
      permissionChecker.isCurrentUserEmailEqualTo("other@example.com")
    ).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(appMessageApi).removeAppDistributionPostEmails(10);
  }

  @Test
  public void testExecuteImpl_whenNotFound_thenStatus404() throws Exception {
    templateVars.put("id", "99");
    when(appMessageApi.getSubscribedDistributionEmailById(99)).thenReturn(null);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_FOUND, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenForbidden_thenStatus403() throws Exception {
    templateVars.put("id", "5");

    DistributionEmailDAO dao = new DistributionEmailDAO();
    dao.setEmailAddress("other@example.com");
    when(appMessageApi.getSubscribedDistributionEmailById(5)).thenReturn(dao);
    when(
      permissionChecker.isCurrentUserEmailEqualTo("other@example.com")
    ).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidId_thenStatus400() throws Exception {
    templateVars.put("id", "notanumber");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
