package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.ShareSpaceItem;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesIdAvailableSharesGetTest {

  private SpacesIdAvailableSharesGet webScript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new SpacesIdAvailableSharesGet();
    spacesApi = mock(SpacesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("spacesApi", spacesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = mock(Cache.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsShares()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-space-id"
      )
    ).thenReturn(true);

    ShareSpaceItem item = new ShareSpaceItem(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "share-id"),
      "/test/path"
    );
    List<ShareSpaceItem> shares = Arrays.asList(item);
    when(spacesApi.getAvailableSharedSpaces("test-space-id")).thenReturn(
      shares
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(shares, model.get("shares"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-space-id"
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenReturnsNotAcceptable()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-space-id"
      )
    ).thenReturn(true);
    when(spacesApi.getAvailableSharedSpaces("test-space-id")).thenThrow(
      new RuntimeException("service error")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("service error", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenEmptyShares_thenReturnsEmptyList()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-space-id"
      )
    ).thenReturn(true);
    when(spacesApi.getAvailableSharedSpaces("test-space-id")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("shares"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesIdAvailableSharesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
