package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AresBridgeApi;
import io.swagger.api.AresBridgeApiImpl;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ExternalRepositoryTransactionPostTest {

  private ExternalRepositoryTransactionPost webScript;
  private AresBridgeApi aresBridgeApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private ApiToolBox apiToolBox;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new ExternalRepositoryTransactionPost();
    aresBridgeApi = mock(AresBridgeApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    apiToolBox = mock(ApiToolBox.class);

    setField("aresBridgeApi", aresBridgeApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setParentField("apiToolBox", apiToolBox);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenUserIsGuest_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", AresBridgeApiImpl.ARES_BRIDGE);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(true);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(aresBridgeApi);
  }

  @Test
  public void testExecuteImpl_whenUserIsExternal_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", AresBridgeApiImpl.ARES_BRIDGE);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(true);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(aresBridgeApi);
  }

  @Test
  public void testExecuteImpl_whenIdIsNull_thenSkipsProcessing()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", null);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(aresBridgeApi);
  }

  @Test
  public void testExecuteImpl_whenIdIsNotAresBridge_thenSkipsSave()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "OtherRepo");

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"transactionId\":\"tx1\",\"nodes\":[{\"id\":\"node-id\",\"name\":\"doc.pdf\",\"properties\":{\"versionLabel\":\"1.0\"}}]}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verifyNoInteractions(aresBridgeApi);
  }

  @Test
  public void testExecuteImpl_whenValidAresBridgeTransaction_thenSavesTransaction()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", AresBridgeApiImpl.ARES_BRIDGE);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"transactionId\":\"tx1\",\"nodes\":[{\"id\":\"node-id\",\"name\":\"doc.pdf\",\"properties\":{\"versionLabel\":\"1.0\"}}]}"
    );

    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(aresBridgeApi).saveTransaction(
      "ig-id",
      AresBridgeApiImpl.ARES_BRIDGE,
      "tx1",
      "node-id",
      "1.0",
      "doc.pdf"
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", AresBridgeApiImpl.ARES_BRIDGE);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not valid json");

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", AresBridgeApiImpl.ARES_BRIDGE);

    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(currentUserPermissionCheckerService.isGuest()).thenReturn(false);
    when(currentUserPermissionCheckerService.isExternalUser()).thenReturn(
      false
    );

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExternalRepositoryTransactionPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void setParentField(String fieldName, Object value) throws Exception {
    Field field = CircabcDeclarativeWebScript.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
