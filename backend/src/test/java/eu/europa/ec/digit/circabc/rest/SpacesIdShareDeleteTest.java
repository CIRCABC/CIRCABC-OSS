package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesIdShareDeleteTest {

  private SpacesIdShareDelete webScript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private ApiToolBox apiToolBox;
  private NodeService unsecureNodeService;

  @Before
  public void setUp() throws Exception {
    webScript = new SpacesIdShareDelete();
    spacesApi = mock(SpacesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    apiToolBox = mock(ApiToolBox.class);
    unsecureNodeService = mock(NodeService.class);

    setField("spacesApi", spacesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
    setField(CircabcDeclarativeWebScript.class, "apiToolBox", apiToolBox);
    setField(
      CircabcDeclarativeWebScript.class,
      "unsecureNodeService",
      unsecureNodeService
    );

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
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenDeletesShare()
    throws Exception {
    String spaceId = "test-space-id";
    String sharedIGId = "test-ig-id";
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      spaceId
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    WebScriptRequest req = mockRequest(spaceId, sharedIGId);
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.hasAlfrescoDeletePermission(spaceId)).thenReturn(
      true
    );
    when(apiToolBox.getCurrentInterestGroup(spaceRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(spaceRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(spaceRef)).thenReturn(1L);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(spacesApi).deleteShare(spaceId, sharedIGId);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    String spaceId = "test-space-id";

    WebScriptRequest req = mockRequest(spaceId, "test-ig-id");
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.hasAlfrescoDeletePermission(spaceId)).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(spacesApi, never()).deleteShare(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenDeleteThrowsException_thenReturnsNotAcceptable()
    throws Exception {
    String spaceId = "test-space-id";
    String sharedIGId = "test-ig-id";
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      spaceId
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    WebScriptRequest req = mockRequest(spaceId, sharedIGId);
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.hasAlfrescoDeletePermission(spaceId)).thenReturn(
      true
    );
    when(apiToolBox.getCurrentInterestGroup(spaceRef)).thenReturn(parentRef);
    when(apiToolBox.getCircabcPath(spaceRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(spaceRef)).thenReturn(1L);
    doThrow(new RuntimeException("delete failed"))
      .when(spacesApi)
      .deleteShare(spaceId, sharedIGId);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private WebScriptRequest mockRequest(String spaceId, String sharedIGId) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", spaceId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("sharedIGId")).thenReturn(sharedIGId);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    setField(SpacesIdShareDelete.class, fieldName, value);
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
