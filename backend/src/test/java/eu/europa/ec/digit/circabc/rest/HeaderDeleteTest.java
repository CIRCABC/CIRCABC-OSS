package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.api.HeadersApi;
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

public class HeaderDeleteTest {

  private HeaderDelete headerDelete;
  private HeadersApi headerApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private CircabcService circabcService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    headerDelete = new HeaderDelete();
    headerApi = mock(HeadersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    circabcService = mock(CircabcService.class);

    setField("headerApi", headerApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("circabcService", circabcService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenDeletesHeader() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> result = headerDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("test-id", result.get("id"));
    verify(headerApi).deleteHeader("test-id");
    verify(circabcService).deleteHeader(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id")
    );
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenDeletesHeader() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    Map<String, Object> result = headerDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("test-id", result.get("id"));
    verify(headerApi).deleteHeader("test-id");
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = headerDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
    assertTrue(status.getRedirect());
    verify(headerApi, never()).deleteHeader(anyString());
  }

  @Test
  public void testExecuteImpl_whenIllegalArgument_thenBadRequest() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    doThrow(new IllegalArgumentException("not empty"))
      .when(headerApi)
      .deleteHeader("test-id");

    Map<String, Object> result = headerDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Header is not empty", status.getMessage());
    assertTrue(status.getRedirect());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenBadRequest() {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-id")
      )
    )
      .when(headerApi)
      .deleteHeader("test-id");

    Map<String, Object> result = headerDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Invalid id", status.getMessage());
    assertTrue(status.getRedirect());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HeaderDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(headerDelete, value);
  }
}
