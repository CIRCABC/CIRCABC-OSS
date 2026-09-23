package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DashboardApi;
import io.swagger.model.UserNewsFeed;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersDashboardNewsFeedGetTest {

  private UsersDashboardNewsFeedGet webScript;
  private DashboardApi dashboardApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
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

    webScript = new UsersDashboardNewsFeedGet();
    dashboardApi = mock(DashboardApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("dashboardApi", dashboardApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsFeed() {
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("when")).thenReturn("today");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    UserNewsFeed feed = new UserNewsFeed();
    when(
      dashboardApi.usersUserIdDashboardNewsfeedGet("testuser", "today")
    ).thenReturn(feed);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(feed, model.get("feed"));
  }

  @Test
  public void testExecuteImpl_whenNullLanguage_thenMLAwareTrue() {
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("when")).thenReturn("week");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    UserNewsFeed feed = new UserNewsFeed();
    when(
      dashboardApi.usersUserIdDashboardNewsfeedGet("testuser", "week")
    ).thenReturn(feed);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(feed, model.get("feed"));
  }

  @Test
  public void testExecuteImpl_whenNullWhen_thenDefaultsToToday() {
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("when")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    UserNewsFeed feed = new UserNewsFeed();
    when(
      dashboardApi.usersUserIdDashboardNewsfeedGet("testuser", "today")
    ).thenReturn(feed);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(feed, model.get("feed"));
  }

  @Test
  public void testExecuteImpl_whenEmptyWhen_thenDefaultsToToday() {
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("when")).thenReturn("");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    UserNewsFeed feed = new UserNewsFeed();
    when(
      dashboardApi.usersUserIdDashboardNewsfeedGet("testuser", "today")
    ).thenReturn(feed);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(feed, model.get("feed"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("when")).thenReturn("today");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersDashboardNewsFeedGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
