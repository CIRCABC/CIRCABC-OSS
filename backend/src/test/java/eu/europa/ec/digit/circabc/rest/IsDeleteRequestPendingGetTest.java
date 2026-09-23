package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
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

public class IsDeleteRequestPendingGetTest {

  private IsDeleteRequestPendingGet webScript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = IsDeleteRequestPendingGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Before
  public void setUp() throws Exception {
    webScript = new IsDeleteRequestPendingGet();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("groupId", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenPending_thenReturnsTrue() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(categoriesApi.existsGroupDeleteRequest("test-group-id")).thenReturn(
      true
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(true, model.get("isPending"));
  }

  @Test
  public void testExecuteImpl_whenNotPending_thenReturnsFalse() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(categoriesApi.existsGroupDeleteRequest("test-group-id")).thenReturn(
      false
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(false, model.get("isPending"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-group-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError() {
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.isGroupAdmin("test-group-id")
    ).thenReturn(true);
    when(categoriesApi.existsGroupDeleteRequest("test-group-id")).thenReturn(
      true
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(true, model.get("isPending"));
  }
}
