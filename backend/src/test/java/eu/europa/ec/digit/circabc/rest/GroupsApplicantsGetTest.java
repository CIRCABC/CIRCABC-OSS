package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.Applicant;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsApplicantsGetTest {

  private GroupsApplicantsGet webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsApplicantsGet();
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
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndNoLanguage_thenReturnsApplicants() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);

    Applicant applicant = new Applicant();
    List<Applicant> applicants = Collections.singletonList(applicant);
    when(groupsApi.groupsIdMembersApplicantsGet("test-ig-id")).thenReturn(
      applicants
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(applicants, result.get("applicants"));
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndLanguageSet_thenReturnsApplicants() {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);

    List<Applicant> applicants = Collections.emptyList();
    when(groupsApi.groupsIdMembersApplicantsGet("test-ig-id")).thenReturn(
      applicants
    );

    Map<String, Object> result = callExecuteImpl();

    assertNotNull(result);
    assertEquals(applicants, result.get("applicants"));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(false);

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfDirectoryPermission(
        "test-ig-id",
        DirectoryPermissions.DIRMANAGEMEMBERS
      )
    ).thenReturn(true);
    when(groupsApi.groupsIdMembersApplicantsGet("test-ig-id")).thenThrow(
      new org.alfresco.service.cmr.repository.InvalidNodeRefException(
        "invalid",
        new org.alfresco.service.cmr.repository.NodeRef(
          "workspace://SpacesStore/test-id"
        )
      )
    );

    Map<String, Object> result = callExecuteImpl();

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() {
    try {
      java.lang.reflect.Method method =
        org.springframework.extensions.webscripts
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
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getCause() instanceof RuntimeException re) {
        throw re;
      }
      throw new RuntimeException(e.getCause());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsApplicantsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
