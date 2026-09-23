package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.GroupConfiguration;
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

public class GroupConfigurationPutTest {

  private GroupConfigurationPut webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupConfigurationPut();
    groupsApi = mock(GroupsApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

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
  public void testExecuteImpl_whenGroupAdminUpdatesConfiguration_thenReturnsConfiguration()
    throws Exception {
    String groupId = "test-group-id";
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    when(permissionCheckerService.isGroupAdmin(groupId)).thenReturn(true);

    GroupConfiguration resultConfig = new GroupConfiguration();
    when(
      groupsApi.putInterestGroupConfiguration(eq(groupId), any())
    ).thenReturn(resultConfig);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"newsgroups\":{\"enableFlagNewTopic\":true}}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultConfig, model.get("configuration"));
    verify(groupsApi).putInterestGroupConfiguration(eq(groupId), any());
  }

  @Test
  public void testExecuteImpl_whenNotGroupAdmin_thenReturnsForbidden()
    throws Exception {
    String groupId = "test-group-id";
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    when(permissionCheckerService.isGroupAdmin(groupId)).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenGroupIdIsNull_thenReturnsEmptyModel()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertTrue(model.isEmpty());
    verifyNoInteractions(groupsApi);
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    String groupId = "test-group-id";
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn("fr");

    when(permissionCheckerService.isGroupAdmin(groupId)).thenReturn(true);

    GroupConfiguration resultConfig = new GroupConfiguration();
    when(
      groupsApi.putInterestGroupConfiguration(eq(groupId), any())
    ).thenReturn(resultConfig);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("{\"newsgroups\":{}}");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultConfig, model.get("configuration"));
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    String groupId = "test-group-id";
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    when(permissionCheckerService.isGroupAdmin(groupId)).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("invalid json");
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupConfigurationPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
