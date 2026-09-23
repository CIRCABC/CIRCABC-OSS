package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesIdCreateSharePostTest {

  private SpacesIdCreateSharePost webScript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Before
  public void setUp() throws Exception {
    webScript = new SpacesIdCreateSharePost();
    spacesApi = mock(SpacesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("spacesApi", spacesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsOk()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "space-id",
      "parent-id",
      "My Title",
      "My Desc"
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        "parent-id"
      )
    ).thenReturn(true);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("ok", model.get("message"));
    verify(spacesApi).createSharedSpaceLink(
      "space-id",
      "parent-id",
      "My Title",
      "My Desc"
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "space-id",
      "parent-id",
      "Title",
      "Desc"
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        "parent-id"
      )
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    WebScriptRequest req = mockRequest(
      "space-id",
      "parent-id",
      "Title",
      "Desc"
    );
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoAddChildrenPermission(
        "parent-id"
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("Something went wrong"))
      .when(spacesApi)
      .createSharedSpaceLink("space-id", "parent-id", "Title", "Desc");

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("Something went wrong", status.getMessage());
  }

  private WebScriptRequest mockRequest(
    String spaceId,
    String parentId,
    String title,
    String description
  ) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", spaceId);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("parentId")).thenReturn(parentId);
    when(req.getParameter("title")).thenReturn(title);
    when(req.getParameter("description")).thenReturn(description);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesIdCreateSharePost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
