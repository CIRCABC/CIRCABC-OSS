package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Node;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentMachineTranslationPostTest {

  private ContentMachineTranslationPost contentMachineTranslationPost;
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

    contentMachineTranslationPost = new ContentMachineTranslationPost();
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
    templateVars.put("language", "fr");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("notify")).thenReturn("false");
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsTranslationSet()
    throws Exception {
    Node translationNode = new Node();
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      contentApi.requestMachineTranslation("node-123", "fr", false)
    ).thenReturn(translationNode);

    Map<String, Object> result = contentMachineTranslationPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(translationNode, result.get("translationSet"));
  }

  @Test
  public void testExecuteImpl_whenNotifyTrue_thenPassesNotifyTrue()
    throws Exception {
    when(req.getParameter("notify")).thenReturn("true");
    Node translationNode = new Node();
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      contentApi.requestMachineTranslation("node-123", "fr", true)
    ).thenReturn(translationNode);

    Map<String, Object> result = contentMachineTranslationPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(contentApi).requestMachineTranslation("node-123", "fr", true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenLanguageNull_thenThrowsIllegalArgument()
    throws Exception {
    templateVars.put("language", null);

    contentMachineTranslationPost.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenLanguageEmpty_thenThrowsIllegalArgument()
    throws Exception {
    templateVars.put("language", "");

    contentMachineTranslationPost.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenLanguageIsNullString_thenThrowsIllegalArgument()
    throws Exception {
    templateVars.put("language", "null");

    contentMachineTranslationPost.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(false);

    Map<String, Object> result = contentMachineTranslationPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      contentApi.requestMachineTranslation("node-123", "fr", false)
    ).thenThrow(new InvalidNodeRefException("bad ref", null));

    Map<String, Object> result = contentMachineTranslationPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidAspect_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "node-123",
        LibraryPermissions.LIBMANAGEOWN
      )
    ).thenReturn(true);
    when(
      contentApi.requestMachineTranslation("node-123", "fr", false)
    ).thenThrow(new InvalidAspectException("bad aspect", null));

    Map<String, Object> result = contentMachineTranslationPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentMachineTranslationPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(contentMachineTranslationPost, value);
  }
}
