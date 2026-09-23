package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.FavouritesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersFavouritesDeleteTest {

  private UsersFavouritesDelete webScript;
  private FavouritesApi favouritesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

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

    webScript = new UsersFavouritesDelete();
    favouritesApi = mock(FavouritesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("favouritesApi", favouritesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersFavouritesDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String userId, String nodeId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", userId);
    vars.put("nodeId", nodeId);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenDeletesFavourite() {
    mockTemplateVars("testuser", "node-123");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(favouritesApi).usersUserIdFavouritesNodeIdDelete(
      "testuser",
      "node-123"
    );
  }

  @Test
  public void testExecuteImpl_whenDifferentUser_thenForbidden() {
    mockTemplateVars("otheruser", "node-123");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("otheruser")
    ).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(favouritesApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    mockTemplateVars("testuser", "bad-node");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-node")
      )
    )
      .when(favouritesApi)
      .usersUserIdFavouritesNodeIdDelete("testuser", "bad-node");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullUserId_thenNoDelete() {
    mockTemplateVars(null, "node-123");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo(null)
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(favouritesApi);
  }

  @Test
  public void testExecuteImpl_whenNullNodeId_thenNoDelete() {
    mockTemplateVars("testuser", null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(favouritesApi);
  }
}
