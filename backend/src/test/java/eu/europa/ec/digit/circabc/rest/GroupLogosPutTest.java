package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.exception.CustomizationException;
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
import org.springframework.extensions.webscripts.WebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupLogosPutTest {

  private GroupLogosPut groupLogosPut;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    groupLogosPut = new GroupLogosPut();
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
    templateVars.put("id", "group-id-1");
    templateVars.put("logoId", "logo-id-1");

    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenAdminAndValidGroup_thenPutsSelectedLogo()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id-1")
    ).thenReturn(true);

    Map<String, Object> result = groupLogosPut.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(groupsApi).putSelectedLogo("group-id-1", "logo-id-1");
  }

  @Test
  public void testExecuteImpl_whenAdminWithLanguage_thenPutsSelectedLogo()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id-1")
    ).thenReturn(true);

    Map<String, Object> result = groupLogosPut.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(groupsApi).putSelectedLogo("group-id-1", "logo-id-1");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id-1")
    ).thenReturn(false);

    Map<String, Object> result = groupLogosPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
    assertTrue(status.getRedirect());
    verify(groupsApi, never()).putSelectedLogo(anyString(), anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id-1")
    ).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    )
      .when(groupsApi)
      .putSelectedLogo("group-id-1", "logo-id-1");

    Map<String, Object> result = groupLogosPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request", status.getMessage());
    assertTrue(status.getRedirect());
  }

  @Test
  public void testExecuteImpl_whenCustomizationException_thenReturnsInternalError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.isGroupAdmin("group-id-1")
    ).thenReturn(true);
    doThrow(new CustomizationException("error"))
      .when(groupsApi)
      .putSelectedLogo("group-id-1", "logo-id-1");

    Map<String, Object> result = groupLogosPut.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("Internal error", status.getMessage());
    assertTrue(status.getRedirect());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupLogosPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupLogosPut, value);
  }
}
