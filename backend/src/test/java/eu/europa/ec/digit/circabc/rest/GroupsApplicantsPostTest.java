package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
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

public class GroupsApplicantsPostTest {

  private GroupsApplicantsPost webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsApplicantsPost();
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
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-ig-id"
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocaleAndCheckPermission()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-ig-id"
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidBody_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-ig-id"
      )
    ).thenReturn(true);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new IOException("test error"));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsApplicantsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, field.getType().cast(value));
  }
}
