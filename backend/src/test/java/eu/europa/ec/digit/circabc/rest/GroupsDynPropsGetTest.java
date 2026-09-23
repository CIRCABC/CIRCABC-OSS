package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.model.DynamicPropertyDefinition;
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

public class GroupsDynPropsGetTest {

  private GroupsDynPropsGet webscript;
  private DynamicPropertiesApi dynamicPropertiesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsDynPropsGet();
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
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenReturnsDynProperties() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(true);
    List<DynamicPropertyDefinition> dynProps = Collections.singletonList(
      new DynamicPropertyDefinition()
    );
    when(dynamicPropertiesApi.groupsIdDynpropsGet("test-ig-id")).thenReturn(
      dynProps
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(dynProps, model.get("dynproperties"));
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-ig-id")
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsDynPropsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
