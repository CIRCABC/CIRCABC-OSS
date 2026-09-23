package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Version;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class ContentVersionsGetTest {

  private ContentVersionsGet webScript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new ContentVersionsGet();
    contentApi = mock(ContentApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("contentApi", contentApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsVersions()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId, null);
    Status status = new Status();
    List<Version> versions = Collections.singletonList(new Version());

    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        nodeId,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(contentApi.contentIdVersionsGet(nodeId, null)).thenReturn(versions);

    Map<String, Object> model = webScript.executeImpl(req, status, new Cache());

    assertNotNull(model);
    assertEquals(versions, model.get("versions"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenPassesLanguage()
    throws Exception {
    String nodeId = "test-node-id";
    String language = "fr";
    WebScriptRequest req = mockRequest(nodeId, language);
    Status status = new Status();
    List<Version> versions = Collections.singletonList(new Version());

    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        nodeId,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(contentApi.contentIdVersionsGet(nodeId, language)).thenReturn(
      versions
    );

    Map<String, Object> model = webScript.executeImpl(req, status, new Cache());

    assertNotNull(model);
    assertEquals(versions, model.get("versions"));
    verify(contentApi).contentIdVersionsGet(nodeId, language);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId, null);
    Status status = new Status();

    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        nodeId,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, new Cache());

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    String nodeId = "invalid-id";
    WebScriptRequest req = mockRequest(nodeId, null);
    Status status = new Status();

    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        nodeId,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(contentApi.contentIdVersionsGet(nodeId, null)).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId)
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, new Cache());

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private WebScriptRequest mockRequest(String nodeId, String language) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", nodeId);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(language);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentVersionsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
