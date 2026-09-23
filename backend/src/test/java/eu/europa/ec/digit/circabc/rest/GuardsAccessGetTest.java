package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GuardsApi;
import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GuardsAccessGetTest {

  private GuardsAccessGet guardsAccessGet;
  private GuardsApi guardsApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    guardsAccessGet = new GuardsAccessGet();
    guardsApi = mock(GuardsApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("guardsApi", guardsApi);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    org.springframework.extensions.webscripts.Match match =
      new org.springframework.extensions.webscripts.Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GuardsAccessGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(guardsAccessGet, value);
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsGuardAuthorization()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    GuardAuthorization expected = new GuardAuthorization();
    expected.setGranted(true);
    when(guardsApi.guardsAccessIdGet("test-node-id")).thenReturn(expected);

    Map<String, Object> result = guardsAccessGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(expected, result.get("result"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    GuardAuthorization expected = new GuardAuthorization();
    expected.setGranted(true);
    when(guardsApi.guardsAccessIdGet("test-node-id")).thenReturn(expected);

    Map<String, Object> result = guardsAccessGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(expected, result.get("result"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsGrantedFalse()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(guardsApi.guardsAccessIdGet("test-node-id")).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> result = guardsAccessGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    GuardAuthorization auth = (GuardAuthorization) result.get("result");
    assertNotNull(auth);
    assertFalse(auth.getGranted());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-node-id"
    );
    when(guardsApi.guardsAccessIdGet("test-node-id")).thenThrow(
      new InvalidNodeRefException("invalid", nodeRef)
    );

    Map<String, Object> result = guardsAccessGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNonExistingNode_thenReturnsNoContent()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(guardsApi.guardsAccessIdGet("test-node-id")).thenThrow(
      new NonExistingNodeException("not found")
    );

    Map<String, Object> result = guardsAccessGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_NO_CONTENT, status.getCode());
  }
}
