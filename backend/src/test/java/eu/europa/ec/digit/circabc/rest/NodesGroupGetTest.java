package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.NodesApi;
import io.swagger.model.InterestGroup;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
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

public class NodesGroupGetTest {

  private NodesGroupGet webScript;
  private NodesApi nodesApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new NodesGroupGet();
    nodesApi = mock(NodesApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("nodesApi", nodesApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsGroup() {
    InterestGroup group = new InterestGroup();
    when(
      permissionCheckerService.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    when(nodesApi.nodesIdGroupGet("test-node-id")).thenReturn(group);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(group, result.get("g"));
  }

  @Test
  public void testExecuteImpl_whenLanguageSpecified_thenReturnsGroup() {
    when(req.getParameter("language")).thenReturn("fr");
    InterestGroup group = new InterestGroup();
    when(
      permissionCheckerService.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    when(nodesApi.nodesIdGroupGet("test-node-id")).thenReturn(group);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(group, result.get("g"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden() {
    when(
      permissionCheckerService.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      permissionCheckerService.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    when(nodesApi.nodesIdGroupGet("test-node-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError() {
    when(
      permissionCheckerService.hasAlfrescoReadPermission("test-node-id")
    ).thenReturn(true);
    when(nodesApi.nodesIdGroupGet("test-node-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesGroupGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
