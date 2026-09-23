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
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GuardsGroupMembersAdminGetTest {

  private GuardsGroupMembersAdminGet webScript;
  private GuardsApi guardsApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GuardsGroupMembersAdminGet();
    guardsApi = mock(GuardsApi.class);

    Field field = GuardsGroupMembersAdminGet.class.getDeclaredField(
      "guardsApi"
    );
    field.setAccessible(true);
    field.set(webScript, guardsApi);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-group-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenSuccess_thenReturnsGuardAuthorization()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);

    GuardAuthorization expected = new GuardAuthorization();
    expected.setGranted(true);
    when(guardsApi.guardsGroupIdMembersAdminGet("test-group-id")).thenReturn(
      expected
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(expected, model.get("result"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");

    GuardAuthorization expected = new GuardAuthorization();
    expected.setGranted(true);
    when(guardsApi.guardsGroupIdMembersAdminGet("test-group-id")).thenReturn(
      expected
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(expected, model.get("result"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsGrantedFalse()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(guardsApi.guardsGroupIdMembersAdminGet("test-group-id")).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    GuardAuthorization result = (GuardAuthorization) model.get("result");
    assertNotNull(result);
    assertFalse(result.getGranted());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(guardsApi.guardsGroupIdMembersAdminGet("test-group-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-group-id")
      )
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      DeclarativeWebScript.class.getDeclaredMethod(
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
}
