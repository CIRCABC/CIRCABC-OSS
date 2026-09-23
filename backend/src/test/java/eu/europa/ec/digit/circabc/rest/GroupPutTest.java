package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.InterestGroup;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupPutTest {

  private GroupPut groupPut;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    groupPut = new GroupPut();
    groupsApi = mock(GroupsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("groupsApi", groupsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
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

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsOk()
    throws Exception {
    mockTemplateVars("group-id-123");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        "group-id-123"
      )
    ).thenReturn(true);

    String json =
      "{\"name\":\"Test\",\"id\":\"group-id-123\",\"isPublic\":true,\"isRegistered\":false,\"allowApply\":true,\"title\":{},\"description\":{},\"contact\":{}}";
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(groupsApi).groupsIdPut(eq("group-id-123"), any(InterestGroup.class));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    mockTemplateVars("group-id-123");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        "group-id-123"
      )
    ).thenReturn(false);

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenGroupIdNull_thenReturnsEmptyModel()
    throws Exception {
    mockTemplateVars(null);
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertTrue(result.isEmpty());
    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    mockTemplateVars("group-id-123");
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        "group-id-123"
      )
    ).thenReturn(true);

    String json =
      "{\"name\":\"Test\",\"id\":\"group-id-123\",\"isPublic\":true,\"isRegistered\":false,\"allowApply\":true,\"title\":{},\"description\":{},\"contact\":{}}";
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(json);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    mockTemplateVars("group-id-123");
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(
        "group-id-123"
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  private Map<String, Object> invokeExecuteImpl() {
    return groupPut.executeImpl(req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupPut, value);
  }
}
