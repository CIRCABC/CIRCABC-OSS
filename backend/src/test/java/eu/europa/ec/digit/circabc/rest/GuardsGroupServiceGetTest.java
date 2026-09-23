package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GuardsApi;
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
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GuardsGroupServiceGetTest {

  private GuardsGroupServiceGet guardsGroupServiceGet;
  private GuardsApi guardsApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    guardsGroupServiceGet = new GuardsGroupServiceGet();
    guardsApi = mock(GuardsApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("guardsApi", guardsApi);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-group-id");
    templateVars.put("name", "library");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GuardsGroupServiceGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(guardsGroupServiceGet, value);
  }

  @Test
  public void testExecuteImpl_whenGranted_thenReturnsResult() throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    GuardAuthorization auth = new GuardAuthorization();
    auth.setGranted(true);
    when(
      guardsApi.guardsGroupIdServiceNameGet("test-group-id", "library")
    ).thenReturn(auth);

    Map<String, Object> result = guardsGroupServiceGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    GuardAuthorization resultAuth = (GuardAuthorization) result.get("result");
    assertTrue(resultAuth.getGranted());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    GuardAuthorization auth = new GuardAuthorization();
    auth.setGranted(true);
    when(
      guardsApi.guardsGroupIdServiceNameGet("test-group-id", "library")
    ).thenReturn(auth);

    Map<String, Object> result = guardsGroupServiceGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    GuardAuthorization resultAuth = (GuardAuthorization) result.get("result");
    assertTrue(resultAuth.getGranted());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsGrantedFalse()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      guardsApi.guardsGroupIdServiceNameGet("test-group-id", "library")
    ).thenThrow(new AccessDeniedException("denied"));

    Map<String, Object> result = guardsGroupServiceGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    GuardAuthorization resultAuth = (GuardAuthorization) result.get("result");
    assertFalse(resultAuth.getGranted());
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
      guardsApi.guardsGroupIdServiceNameGet("test-group-id", "library")
    ).thenThrow(new InvalidNodeRefException("invalid", nodeRef));

    Map<String, Object> result = guardsGroupServiceGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
