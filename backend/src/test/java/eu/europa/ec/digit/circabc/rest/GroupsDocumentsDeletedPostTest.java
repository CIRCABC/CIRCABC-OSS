package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ArchiveApi;
import io.swagger.model.RestoreNodeMetadata;
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

public class GroupsDocumentsDeletedPostTest {

  private GroupsDocumentsDeletedPost webScript;
  private ArchiveApi archiveApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;

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

    webScript = new GroupsDocumentsDeletedPost();
    archiveApi = mock(ArchiveApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("archiveApi", archiveApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    content = mock(Content.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenSuccess() throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(true);
    when(content.getContent()).thenReturn(
      "{\"archiveNodeId\":\"node-123\",\"targetFolderId\":\"folder-456\"}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(archiveApi).groupsIdDocumentsDeletedPost(
      eq("test-ig-id"),
      any(RestoreNodeMetadata.class)
    );
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(archiveApi);
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(true);
    when(content.getContent()).thenReturn(
      "{\"archiveNodeId\":\"node-123\",\"targetFolderId\":\"folder-456\"}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(archiveApi).groupsIdDocumentsDeletedPost(
      eq("test-ig-id"),
      any(RestoreNodeMetadata.class)
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(true);
    when(content.getContent()).thenReturn("invalid json");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(true);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsDocumentsDeletedPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
