package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NodesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodeOriginalNodeRefGetTest {

  private NodeOriginalNodeRefGet webScript;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodeOriginalNodeRefGet();
    nodesApi = mock(NodesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodeOriginalNodeRefGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenMigrated_thenReturnsRef() {
    mockTemplateVars("n1");
    when(nodesApi.getOriginalNodeRef("n1")).thenReturn(
      "workspace://SpacesStore/old-1"
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("n1", model.get("id"));
    assertEquals("workspace://SpacesStore/old-1", model.get("originalNodeRef"));
  }

  @Test
  public void testExecuteImpl_whenNotMigrated_thenEmptyString() {
    mockTemplateVars("n1");
    when(nodesApi.getOriginalNodeRef("n1")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("", model.get("originalNodeRef"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    mockTemplateVars("n1");
    doThrow(new AccessDeniedException("no"))
      .when(permissionChecker)
      .throwIfNotCircabcAdmin();

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }
}
