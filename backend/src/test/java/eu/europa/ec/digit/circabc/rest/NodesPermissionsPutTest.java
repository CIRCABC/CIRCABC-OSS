package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.PermissionsApi;
import io.swagger.model.PermissionDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesPermissionsPutTest {

  private NodesPermissionsPut nodesPermissionsPut;
  private PermissionsApi permissionsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    nodesPermissionsPut = new NodesPermissionsPut();
    permissionsApi = mock(PermissionsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("permissionsApi", permissionsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", NODE_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenLibAdmin_thenReturnsDefinition()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"inherited\":true,\"permissions\":{\"profiles\":[],\"users\":[]}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    PermissionDefinition result = new PermissionDefinition();
    when(
      permissionsApi.nodeIdPermissionsPut(
        eq(NODE_ID),
        any(PermissionDefinition.class)
      )
    ).thenReturn(result);

    Map<String, Object> model = nodesPermissionsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(result, model.get("definition"));
  }

  @Test
  public void testExecuteImpl_whenNwsAdmin_thenReturnsDefinition()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        NODE_ID,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"inherited\":false,\"permissions\":{\"profiles\":[],\"users\":[]}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    PermissionDefinition result = new PermissionDefinition();
    when(
      permissionsApi.nodeIdPermissionsPut(
        eq(NODE_ID),
        any(PermissionDefinition.class)
      )
    ).thenReturn(result);

    Map<String, Object> model = nodesPermissionsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(result, model.get("definition"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        NODE_ID,
        NewsGroupPermissions.NWSADMIN
      )
    ).thenReturn(false);

    Map<String, Object> model = nodesPermissionsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = nodesPermissionsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBADMIN
      )
    ).thenReturn(true);

    String jsonBody =
      "{\"inherited\":true,\"permissions\":{\"profiles\":[],\"users\":[]}}";
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(jsonBody);
    when(req.getContent()).thenReturn(content);

    PermissionDefinition result = new PermissionDefinition();
    when(
      permissionsApi.nodeIdPermissionsPut(
        eq(NODE_ID),
        any(PermissionDefinition.class)
      )
    ).thenReturn(result);

    Map<String, Object> model = nodesPermissionsPut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(result, model.get("definition"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesPermissionsPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodesPermissionsPut, value);
  }
}
