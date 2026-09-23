package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
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

public class GroupLogosDeleteTest {

  private GroupLogosDelete groupLogosDelete;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    groupLogosDelete = new GroupLogosDelete();
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
    templateVars.put("id", "group-id");
    templateVars.put("logoId", "logo-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenAdminDeletesLogo_thenSuccess()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);

    Map<String, Object> result = groupLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(groupsApi).deleteLogo("group-id", "logo-id");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(false);

    Map<String, Object> result = groupLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(groupsApi, never()).deleteLogo(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);

    Map<String, Object> result = groupLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(groupsApi).deleteLogo("group-id", "logo-id");
  }

  @Test
  public void testExecuteImpl_whenGroupIdNull_thenDoesNotCallDelete()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);
    templateVars.put("logoId", "logo-id");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    when(currentUserPermissionCheckerService.isGroupAdmin(null)).thenReturn(
      true
    );

    Map<String, Object> result = groupLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(groupsApi, never()).deleteLogo(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id")
    ).thenReturn(true);
    doThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "invalid",
        null
      )
    )
      .when(groupsApi)
      .deleteLogo("group-id", "logo-id");

    Map<String, Object> result = groupLogosDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupLogosDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupLogosDelete, value);
  }
}
