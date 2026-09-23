package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.LogRestRecord;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Description;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CircabcDeclarativeWebScriptTest {

  private CircabcDeclarativeWebScript webScript;
  private LogService logService;
  private ApiToolBox apiToolBox;
  private NodeService unsecureNodeService;

  @Before
  public void setUp() throws Exception {
    webScript = new CircabcDeclarativeWebScript();
    logService = mock(LogService.class);
    apiToolBox = mock(ApiToolBox.class);
    unsecureNodeService = mock(NodeService.class);

    setField("logService", logService);
    setField("apiToolBox", apiToolBox);
    setField("unsecureNodeService", unsecureNodeService);

    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");
  }

  @Test
  public void testExecuteFinallyImpl_whenPostRequest_thenLogsRecord() {
    setDescription("POST", "/circabc/nodes/{id}");
    WebScriptRequest req = mockRequest("/circabc/nodes/{id}", "abc123");
    Status status = new Status();

    webScript.executeFinallyImpl(req, status, new Cache(), new HashMap<>());

    ArgumentCaptor<LogRestRecord> captor = ArgumentCaptor.forClass(
      LogRestRecord.class
    );
    verify(logService).logRest(captor.capture());
    LogRestRecord record = captor.getValue();
    assertEquals("POST", record.getMethod());
    assertEquals("testuser", record.getUser());
    assertEquals("/circabc/nodes/{id}", record.getTemplate());
    assertEquals(Integer.valueOf(200), record.getStatusCode());
  }

  @Test
  public void testExecuteFinallyImpl_whenGetGroupByGuest_thenSkipsLog() {
    AuthenticationUtil.setFullyAuthenticatedUser("guest");
    setDescription("GET", "/circabc/groups/{id}");
    WebScriptRequest req = mockRequest("/circabc/groups/{id}", "group1");
    Status status = new Status();

    webScript.executeFinallyImpl(req, status, new Cache(), new HashMap<>());

    verify(logService, never()).logRest(any());
  }

  @Test
  public void testExecuteFinallyImpl_whenGetGroupWithLogFalse_thenSkipsLog() {
    setDescription("GET", "/circabc/groups/{id}");
    WebScriptRequest req = mockRequest("/circabc/groups/{id}", "group1");
    when(req.getParameter("log")).thenReturn("false");
    Status status = new Status();

    webScript.executeFinallyImpl(req, status, new Cache(), new HashMap<>());

    verify(logService, never()).logRest(any());
  }

  @Test
  public void testExecuteFinallyImpl_whenGetGroupWithLogTrue_thenLogs() {
    setDescription("GET", "/circabc/groups/{id}");
    WebScriptRequest req = mockRequest("/circabc/groups/{id}", "group1");
    when(req.getParameter("log")).thenReturn("true");
    Status status = new Status();

    webScript.executeFinallyImpl(req, status, new Cache(), new HashMap<>());

    verify(logService).logRest(any());
  }

  @Test
  public void testRecordBeforeDelete_whenNodeHasInterestGroup_thenSetsParent() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef deletedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "deleted-id"
    );
    when(apiToolBox.getCurrentInterestGroup(deletedRef)).thenReturn(igRef);
    when(apiToolBox.getCircabcPath(deletedRef, true)).thenReturn("/some/path");
    when(apiToolBox.getDatabaseID(deletedRef)).thenReturn(42L);

    webScript.recordBeforeDelete("deleted-id");

    assertEquals(igRef, webScript.nodeParent);
    assertEquals("/some/path", webScript.nodePath);
    assertEquals(42L, webScript.nodeID);
  }

  @Test
  public void testRecordBeforeDelete_whenNoInterestGroup_thenUsesPrimaryParent() {
    NodeRef deletedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "deleted-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(deletedRef)).thenReturn(null);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentRef);
    when(unsecureNodeService.getPrimaryParent(deletedRef)).thenReturn(
      childAssoc
    );
    when(apiToolBox.getCircabcPath(deletedRef, true)).thenReturn("/other/path");
    when(apiToolBox.getDatabaseID(deletedRef)).thenReturn(99L);

    webScript.recordBeforeDelete("deleted-id");

    assertEquals(parentRef, webScript.nodeParent);
    assertEquals("/other/path", webScript.nodePath);
    assertEquals(99L, webScript.nodeID);
  }

  @Test
  public void testExecuteFinallyImpl_whenUrlExceedsMaxSize_thenTruncates() {
    setDescription("POST", "/circabc/long/{id}");
    WebScriptRequest req = mockRequest("/circabc/long/{id}", "val");
    String longUrl = "x".repeat(600);
    when(req.getURL()).thenReturn(longUrl);
    Status status = new Status();

    webScript.executeFinallyImpl(req, status, new Cache(), new HashMap<>());

    ArgumentCaptor<LogRestRecord> captor = ArgumentCaptor.forClass(
      LogRestRecord.class
    );
    verify(logService).logRest(captor.capture());
    assertTrue(captor.getValue().getUrl().length() <= 510);
  }

  private void setDescription(String method, String template) {
    try {
      Description desc = mock(Description.class);
      when(desc.getMethod()).thenReturn(method);
      Field descField = org.springframework.extensions.webscripts
        .AbstractWebScript.class.getDeclaredField("description");
      descField.setAccessible(true);
      descField.set(webScript, desc);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private WebScriptRequest mockRequest(String template, String idValue) {
    WebScriptRequest req = mock(WebScriptRequest.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", idValue);

    Match match = new Match(
      template,
      templateVars,
      template.replace("{id}", idValue)
    );
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getURL()).thenReturn(template.replace("{id}", idValue));
    when(req.getParameterNames()).thenReturn(new String[0]);
    when(req.getContent()).thenReturn(null);
    when(req.getParameter("log")).thenReturn(null);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcDeclarativeWebScript.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
