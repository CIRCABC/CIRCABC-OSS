package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.DynamicPropertiesApi;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsDynPropsPostTest {

  private static final String IG_ID = "test-ig-id";
  private static final String VALID_JSON =
    "{\"id\":\"1\",\"title\":{\"en\":\"Test\"},\"propertyType\":\"TEXT_FIELD\"}";

  private GroupsDynPropsPost webscript;
  private DynamicPropertiesApi dynamicPropertiesApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsDynPropsPost();
    dynamicPropertiesApi = mock(DynamicPropertiesApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("dynamicPropertiesApi", dynamicPropertiesApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new java.util.HashMap<>();
    templateVars.put("igId", IG_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenAdmin_thenReturnsDynamicProperty()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(VALID_JSON);

    DynamicPropertyDefinition result = new DynamicPropertyDefinition();
    result.setId("1");
    when(
      dynamicPropertiesApi.groupsIdDynpropsPost(
        eq(IG_ID),
        any(DynamicPropertyDefinition.class)
      )
    ).thenReturn(result);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(result, model.get("dp"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenParseException_thenReturnsBadRequest()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("{\"invalid\": true}");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(permissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsDynPropsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
