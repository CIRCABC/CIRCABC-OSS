package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.GroupConfiguration;
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

public class GroupConfigurationGetTest {

  private GroupConfigurationGet webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupConfigurationGet();
    groupsApi = mock(GroupsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("groupsApi", groupsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenValidGroupAndPermission_thenReturnsConfiguration()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-group-id"
      )
    ).thenReturn(true);
    GroupConfiguration config = new GroupConfiguration();
    when(groupsApi.getInterestGroupConfiguration("test-group-id")).thenReturn(
      config
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(config, result.get("configuration"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsConfiguration()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-group-id"
      )
    ).thenReturn(true);
    GroupConfiguration config = new GroupConfiguration();
    when(groupsApi.getInterestGroupConfiguration("test-group-id")).thenReturn(
      config
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(config, result.get("configuration"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-group-id"
      )
    ).thenReturn(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-group-id"
      )
    ).thenReturn(true);
    when(groupsApi.getInterestGroupConfiguration("test-group-id")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-group-id")
      )
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenGroupIdNull_thenReturnsEmptyModel()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertFalse(result.containsKey("configuration"));
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
    Field field = GroupConfigurationGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
