package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.permissions.LibraryPermissions;
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

public class NodesKeywordsDeleteTest {

  private NodesKeywordsDelete nodesKeywordsDelete;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-node-id";
  private static final String KEYWORD_ID = "test-keyword-id";

  @Before
  public void setUp() throws Exception {
    nodesKeywordsDelete = new NodesKeywordsDelete();
    keywordsApi = mock(KeywordsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("keywordsApi", keywordsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", NODE_ID);
    templateVars.put("keywordId", KEYWORD_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenUserHasPermission_thenDeletesKeyword() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBEDITONLY
      )
    ).thenReturn(true);

    Map<String, Object> result = nodesKeywordsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(keywordsApi).nodesIdKeywordsKeywordIdDelete(NODE_ID, KEYWORD_ID);
  }

  @Test
  public void testExecuteImpl_whenUserLacksPermission_thenReturnsForbidden() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBEDITONLY
      )
    ).thenReturn(false);

    Map<String, Object> result = nodesKeywordsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(keywordsApi, never()).nodesIdKeywordsKeywordIdDelete(
      anyString(),
      anyString()
    );
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBEDITONLY
      )
    ).thenReturn(true);
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      NODE_ID
    );
    doThrow(new InvalidNodeRefException("invalid", nodeRef))
      .when(keywordsApi)
      .nodesIdKeywordsKeywordIdDelete(NODE_ID, KEYWORD_ID);

    Map<String, Object> result = nodesKeywordsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError() {
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBEDITONLY
      )
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(keywordsApi)
      .nodesIdKeywordsKeywordIdDelete(NODE_ID, KEYWORD_ID);

    Map<String, Object> result = nodesKeywordsDelete.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesKeywordsDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodesKeywordsDelete, value);
  }
}
