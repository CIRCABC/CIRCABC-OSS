package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.RecentDiscussion;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsDashboardDiscussionsGetTest {

  private GroupsDashboardDiscussionsGet webscript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsDashboardDiscussionsGet();
    groupsApi = mock(GroupsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = Map.of("igId", "test-ig-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenLanguageNull_thenReturnsDiscussions()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.canAccessInterestGroup("test-ig-id")
    ).thenReturn(true);

    List<RecentDiscussion> discussions = List.of(new RecentDiscussion());
    when(groupsApi.getGroupRecentDiscussions("test-ig-id")).thenReturn(
      discussions
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(discussions, model.get("discussions"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsDiscussions()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      permissionCheckerService.canAccessInterestGroup("test-ig-id")
    ).thenReturn(true);

    List<RecentDiscussion> discussions = Collections.emptyList();
    when(groupsApi.getGroupRecentDiscussions("test-ig-id")).thenReturn(
      discussions
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(discussions, model.get("discussions"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.canAccessInterestGroup("test-ig-id")
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionCheckerService.canAccessInterestGroup("test-ig-id")
    ).thenReturn(true);
    when(groupsApi.getGroupRecentDiscussions("test-ig-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsDashboardDiscussionsGet.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
