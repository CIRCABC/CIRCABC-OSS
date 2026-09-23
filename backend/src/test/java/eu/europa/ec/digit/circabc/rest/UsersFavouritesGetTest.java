package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.FavouritesApi;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

public class UsersFavouritesGetTest {

  private UsersFavouritesGet webScript;
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

    webScript = new UsersFavouritesGet();
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
    Field field = UsersFavouritesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String userId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("userId", userId);
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsFavourites() {
    mockTemplateVars("testuser");
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    List<Node> nodes = Arrays.asList(new Node(), new Node());
    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(nodes);
    pagedNodes.setTotal(2L);
    when(favouritesApi.usersUserIdFavouritesGet("testuser", 0, 25)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(nodes, result.get("data"));
    assertEquals(2L, result.get("total"));
  }

  @Test
  public void testExecuteImpl_whenPageAndLimitProvided_thenUsesParameters() {
    mockTemplateVars("testuser");
    when(req.getParameter("language")).thenReturn("fr");
    when(req.getParameter("page")).thenReturn("3");
    when(req.getParameter("limit")).thenReturn("10");
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(List.of());
    pagedNodes.setTotal(0L);
    when(favouritesApi.usersUserIdFavouritesGet("testuser", 2, 10)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(favouritesApi).usersUserIdFavouritesGet("testuser", 2, 10);
  }

  @Test
  public void testExecuteImpl_whenPageIsZero_thenPageRemainsZero() {
    mockTemplateVars("testuser");
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    PagedNodes pagedNodes = new PagedNodes();
    pagedNodes.setData(List.of());
    pagedNodes.setTotal(0L);
    when(favouritesApi.usersUserIdFavouritesGet("testuser", 0, 25)).thenReturn(
      pagedNodes
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(favouritesApi).usersUserIdFavouritesGet("testuser", 0, 25);
  }

  @Test
  public void testExecuteImpl_whenDifferentUser_thenForbidden() {
    mockTemplateVars("otheruser");
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
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
    mockTemplateVars("testuser");
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(favouritesApi.usersUserIdFavouritesGet("testuser", 0, 25)).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-node")
      )
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
