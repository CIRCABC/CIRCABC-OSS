package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.StatData;
import io.swagger.model.db.ActivityCountDAO;
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

public class GroupsSummaryGetTest {

  private GroupsSummaryGet webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsSummaryGet();
    groupsApi = mock(GroupsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("groupsApi", groupsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenStatistics_thenReturnsStatistics() {
    when(req.getServicePath()).thenReturn("/groups/test-group-id/statistics");
    when(req.getParameter("calculate")).thenReturn("true");
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);

    List<StatData> stats = Collections.singletonList(new StatData());
    when(
      groupsApi.getIGSummaryStatistics("test-group-id", true, false)
    ).thenReturn(stats);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(stats, result.get("statistics"));
  }

  @Test
  public void testExecuteImpl_whenTimeline_thenReturnsTimeline() {
    when(req.getServicePath()).thenReturn("/groups/test-group-id/timeline");
    when(req.getParameter("calculate")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);

    List<ActivityCountDAO> timeline = Collections.singletonList(
      new ActivityCountDAO()
    );
    when(groupsApi.getIGSummaryTimeline("test-group-id")).thenReturn(timeline);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(timeline, result.get("timeline"));
  }

  @Test
  public void testExecuteImpl_whenStructure_thenReturnsStructure() {
    when(req.getServicePath()).thenReturn("/groups/test-group-id/structure");
    when(req.getParameter("calculate")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);

    String structure = "structure-data";
    when(groupsApi.getIGSummaryStructure("test-group-id")).thenReturn(
      structure
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(structure, result.get("structure"));
  }

  @Test
  public void testExecuteImpl_whenNotGroupAdmin_thenReturnsForbidden() {
    when(req.getServicePath()).thenReturn("/groups/test-group-id/statistics");
    when(req.getParameter("calculate")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getServicePath()).thenReturn("/groups/test-group-id/statistics");
    when(req.getParameter("calculate")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(
      groupsApi.getIGSummaryStatistics("test-group-id", false, false)
    ).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "invalid",
        new org.alfresco.service.cmr.repository.NodeRef(
          "workspace://SpacesStore/test-group-id"
        )
      )
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() {
    try {
      java.lang.reflect.Method method =
        org.springframework.extensions.webscripts
          .DeclarativeWebScript.class.getDeclaredMethod(
          "executeImpl",
          WebScriptRequest.class,
          Status.class,
          Cache.class
        );
      method.setAccessible(true);
      @SuppressWarnings("unchecked")
      Map<String, Object> result = (Map<String, Object>) method.invoke(
        webScript,
        req,
        status,
        cache
      );
      return result;
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(e.getCause());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsSummaryGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
