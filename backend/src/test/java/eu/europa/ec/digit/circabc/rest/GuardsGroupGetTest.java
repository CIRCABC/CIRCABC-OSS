package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.GuardAuthorization;
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

public class GuardsGroupGetTest {

  private GuardsGroupGet guardsGroupGet;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    guardsGroupGet = new GuardsGroupGet();
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GuardsGroupGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(guardsGroupGet, value);
  }

  @Test
  public void testExecuteImpl_whenGranted_thenReturnsTrue() throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.canAccessInterestGroup(
        "test-group-id"
      )
    ).thenReturn(true);

    Map<String, Object> result = guardsGroupGet.executeImpl(req, status, cache);

    assertNotNull(result);
    GuardAuthorization auth = (GuardAuthorization) result.get("result");
    assertTrue(auth.getGranted());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.canAccessInterestGroup(
        "test-group-id"
      )
    ).thenReturn(true);

    Map<String, Object> result = guardsGroupGet.executeImpl(req, status, cache);

    assertNotNull(result);
    GuardAuthorization auth = (GuardAuthorization) result.get("result");
    assertTrue(auth.getGranted());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsGrantedFalse()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.canAccessInterestGroup(
        "test-group-id"
      )
    ).thenThrow(new AccessDeniedException("denied"));

    Map<String, Object> result = guardsGroupGet.executeImpl(req, status, cache);

    assertNotNull(result);
    GuardAuthorization auth = (GuardAuthorization) result.get("result");
    assertFalse(auth.getGranted());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-group-id"
    );
    when(
      currentUserPermissionCheckerService.canAccessInterestGroup(
        "test-group-id"
      )
    ).thenThrow(new InvalidNodeRefException("invalid", nodeRef));

    Map<String, Object> result = guardsGroupGet.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
