package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.SpacesApi;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpacesFolderSizeGetTest {

  private SpacesFolderSizeGet spacesFolderSizeGet;
  private SpacesApi spacesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    spacesFolderSizeGet = new SpacesFolderSizeGet();
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
    templateVars.put("id", "test-folder-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenReturnsFolderSize()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-folder-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(spacesApi.getFolderSize("test-folder-id")).thenReturn(42);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertEquals(42, result.get("result"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-folder-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        "test-folder-id",
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(spacesApi.getFolderSize("test-folder-id")).thenThrow(
      new RuntimeException("something went wrong")
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("something went wrong", status.getMessage());
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    Method method = SpacesFolderSizeGet.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      spacesFolderSizeGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesFolderSizeGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(spacesFolderSizeGet, value);
  }
}
