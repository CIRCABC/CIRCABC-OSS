package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
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

public class DynPropsGetTest {

  private DynPropsGet dynPropsGet;
  private DynamicPropertiesApi dynamicPropertiesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    dynPropsGet = new DynPropsGet();
    dynamicPropertiesApi = mock(DynamicPropertiesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("dynamicPropertiesApi", dynamicPropertiesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-dp-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermission_thenReturnsDynamicProperty()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-dp-id"
      )
    ).thenReturn(true);
    Object dpResult = new Object();
    when(dynamicPropertiesApi.dynpropsIdGet("test-dp-id")).thenReturn(null);

    Map<String, Object> result = invokeExecuteImpl();

    assertNotNull(result);
    assertNull(result.get("dp"));
    verify(dynamicPropertiesApi).dynpropsIdGet("test-dp-id");
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-dp-id"
      )
    ).thenReturn(false);

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-dp-id"
      )
    ).thenReturn(true);
    when(dynamicPropertiesApi.dynpropsIdGet("test-dp-id")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-dp-id")
      )
    );

    Map<String, Object> result = invokeExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method = DynPropsGet.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      dynPropsGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DynPropsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(dynPropsGet, value);
  }
}
