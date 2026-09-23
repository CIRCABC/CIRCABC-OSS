package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.InterestGroup;
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

public class GroupGetTest {

  private GroupGet webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupGet();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidGroupNoLanguage_thenReturnsModel() {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(groupId)).thenReturn(true);
    InterestGroup ig = new InterestGroup();
    when(groupsApi.getInterestGroup(groupId)).thenReturn(ig);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(groupId, model.get("id"));
    assertSame(ig, model.get("g"));
    verify(groupsApi).getInterestGroup(groupId);
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsModel() {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.hasAlfrescoReadPermission(groupId)).thenReturn(true);
    InterestGroup ig = new InterestGroup();
    when(groupsApi.getInterestGroup(groupId)).thenReturn(ig);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(groupId, model.get("id"));
    assertSame(ig, model.get("g"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(groupId)).thenReturn(
      false
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    String groupId = "invalid-id";
    mockTemplateVars(groupId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.hasAlfrescoReadPermission(groupId)).thenReturn(true);
    when(groupsApi.getInterestGroup(groupId)).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "invalid",
        new org.alfresco.service.cmr.repository.NodeRef(
          org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
          "invalid-id"
        )
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenReturnsEmptyModel() {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", null);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(model.isEmpty());
    verifyNoInteractions(groupsApi);
  }
}
