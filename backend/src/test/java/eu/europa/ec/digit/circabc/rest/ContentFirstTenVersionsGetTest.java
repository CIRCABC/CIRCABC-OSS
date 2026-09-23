package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.ContentApi;
import io.swagger.model.Version;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentFirstTenVersionsGetTest {

  private static final String TEST_ID = "test-node-id";

  private ContentFirstTenVersionsGet webscript;
  private ContentApi contentApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new ContentFirstTenVersionsGet();

    contentApi = mock(ContentApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("contentApi", contentApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenReturnsVersions()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);

    List<Version> versions = List.of(new Version(), new Version());
    when(contentApi.contentIdFirstVersionsGet(TEST_ID)).thenReturn(versions);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(versions, model.get("versions"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      permissionCheckerService.hasAnyOfLibraryPermission(
        TEST_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);

    when(contentApi.contentIdFirstVersionsGet(TEST_ID)).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID)
      )
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      ContentFirstTenVersionsGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webscript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentFirstTenVersionsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
