package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class KeywordsDeleteTest {

  private KeywordsDelete keywordsDelete;
  private KeywordsApi keywordsApi;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String KEYWORD_ID = "keyword-node-id";
  private static final String CONTAINER_ID = "container-node-id";
  private static final String IG_ID = "ig-node-id";

  @Before
  public void setUp() throws Exception {
    keywordsDelete = new KeywordsDelete();
    keywordsApi = mock(KeywordsApi.class);
    nodeService = mock(NodeService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("keywordsApi", keywordsApi);
    setField("nodeService", nodeService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("keywordId", KEYWORD_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserIsAdmin_thenDeletesKeyword() {
    setupNodeHierarchy();
    when(currentUserPermissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(
      true
    );

    Map<String, Object> result = keywordsDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("message"));
    verify(keywordsApi).keywordsKeywordIdDelete(KEYWORD_ID);
  }

  @Test
  public void testExecuteImpl_whenUserIsNotAdmin_thenReturnsForbidden() {
    setupNodeHierarchy();
    when(currentUserPermissionCheckerService.isGroupAdmin(IG_ID)).thenReturn(
      false
    );

    Map<String, Object> result = keywordsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(keywordsApi, never()).keywordsKeywordIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    NodeRef keywordRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      KEYWORD_ID
    );
    when(nodeService.getPrimaryParent(keywordRef)).thenThrow(
      new InvalidNodeRefException("invalid", keywordRef)
    );

    Map<String, Object> result = keywordsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError() {
    NodeRef keywordRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      KEYWORD_ID
    );
    when(nodeService.getPrimaryParent(keywordRef)).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> result = keywordsDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    assertEquals("unexpected", status.getMessage());
  }

  private void setupNodeHierarchy() {
    NodeRef keywordRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      KEYWORD_ID
    );
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      CONTAINER_ID
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      IG_ID
    );

    ChildAssociationRef keywordAssoc = mock(ChildAssociationRef.class);
    when(keywordAssoc.getParentRef()).thenReturn(containerRef);
    when(nodeService.getPrimaryParent(keywordRef)).thenReturn(keywordAssoc);

    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getParentRef()).thenReturn(igRef);
    when(nodeService.getPrimaryParent(containerRef)).thenReturn(containerAssoc);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = KeywordsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(keywordsDelete, value);
  }
}
