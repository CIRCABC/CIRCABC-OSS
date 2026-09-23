package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
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

public class UserCategoriesGetTest {

  private UserCategoriesGet webScript;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new UserCategoriesGet();
    usersApi = mock(UsersApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenCurrentUser_thenReturnsCategories() {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    List<Object> categories = List.of("cat1", "cat2");
    when(usersApi.getUserCategories("testuser")).thenReturn((List) categories);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(categories, result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsCategories() {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);
    when(usersApi.getUserCategories("testuser")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Collections.emptyList(), result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsCategories() {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(usersApi.getUserCategories("testuser")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(Collections.emptyList(), result.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "bad"
    );
    when(usersApi.getUserCategories("testuser")).thenThrow(
      new InvalidNodeRefException("invalid", nodeRef)
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullUserId_thenReturnsEmptyModel() {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", null);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertFalse(result.containsKey("categories"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(usersApi.getUserCategories("testuser")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    when(permissionChecker.isCurrentUserEqualTo("testuser")).thenReturn(true);
    when(usersApi.getUserCategories("testuser")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserCategoriesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
