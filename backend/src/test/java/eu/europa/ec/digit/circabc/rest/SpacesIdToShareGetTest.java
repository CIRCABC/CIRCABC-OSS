package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.ShareIGsAndPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesIdToShareGetTest {

  private SpacesIdToShareGet webScript;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new SpacesIdToShareGet();
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
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-space-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsModel()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);

    ShareIGsAndPermissions expected = new ShareIGsAndPermissions(
      new ArrayList<>(),
      new ArrayList<>()
    );
    when(spacesApi.getShareIGsAndPermissions("test-space-id")).thenReturn(
      expected
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertSame(expected, model.get("igsAndPermissions"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-space-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);

    when(spacesApi.getShareIGsAndPermissions("test-space-id")).thenThrow(
      new RuntimeException("something went wrong")
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("something went wrong", status.getMessage());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webScript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesIdToShareGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
