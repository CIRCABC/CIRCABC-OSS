package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Version;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentVersionGetTest {

  private ContentVersionGet webScript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ContentVersionGet();
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

    Map<String, String> templateVars = Map.of(
      "id",
      "test-node-id",
      "versionId",
      "1.0"
    );
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndNoLanguage_thenReturnsVersion()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    Version version = new Version();
    when(
      contentApi.contentIdVersionsVersionIdGet("test-node-id", "1.0", null)
    ).thenReturn(version);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(version, result.get("version"));
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndLanguageSet_thenReturnsVersion()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    Version version = new Version();
    when(
      contentApi.contentIdVersionsVersionIdGet("test-node-id", "1.0", "fr")
    ).thenReturn(version);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(version, result.get("version"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-node-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(
      contentApi.contentIdVersionsVersionIdGet("test-node-id", "1.0", null)
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentVersionGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
