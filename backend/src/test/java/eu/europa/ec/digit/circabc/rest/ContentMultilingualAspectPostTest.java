package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.MultilingualAspectMetadata;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentMultilingualAspectPostTest {

  private ContentMultilingualAspectPost webscript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Map<String, String> templateVars;

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

    webscript = new ContentMultilingualAspectPost();
    contentApi = mock(ContentApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("contentApi", contentApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    templateVars = new HashMap<>();
    templateVars.put("id", "node-123");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndValidBody_thenSuccess()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("node-123"),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"pivotLang\":\"en\",\"author\":\"admin\"}"
    );
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(contentApi).contentIdMultilingualAspectPost(
      eq("node-123"),
      any(MultilingualAspectMetadata.class)
    );
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("node-123"),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class)
      )
    ).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("node-123"),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"pivotLang\":\"en\",\"author\":\"admin\"}"
    );
    when(req.getContent()).thenReturn(content);

    doThrow(new InvalidNodeRefException("bad", null))
      .when(contentApi)
      .contentIdMultilingualAspectPost(
        eq("node-123"),
        any(MultilingualAspectMetadata.class)
      );

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("node-123"),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class),
        any(LibraryPermissions.class)
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = webscript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentMultilingualAspectPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
