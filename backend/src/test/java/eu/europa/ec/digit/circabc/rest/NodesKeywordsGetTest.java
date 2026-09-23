package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NodesKeywordsGetTest {

  private NodesKeywordsGet nodesKeywordsGet;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String NODE_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    nodesKeywordsGet = new NodesKeywordsGet();
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
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndNoLanguage_thenReturnsKeywords()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);

    List<KeywordDefinition> keywords = new ArrayList<>();
    keywords.add(new KeywordDefinition());
    when(keywordsApi.nodesIdKeywordsGet(NODE_ID)).thenReturn(keywords);

    Map<String, Object> model = nodesKeywordsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(keywords, model.get("keywords"));
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndLanguageSet_thenReturnsKeywords()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);

    List<KeywordDefinition> keywords = new ArrayList<>();
    when(keywordsApi.nodesIdKeywordsGet(NODE_ID)).thenReturn(keywords);

    Map<String, Object> model = nodesKeywordsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(model);
    assertEquals(keywords, model.get("keywords"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(false);

    Map<String, Object> model = nodesKeywordsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(keywordsApi.nodesIdKeywordsGet(NODE_ID)).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, NODE_ID)
      )
    );

    Map<String, Object> model = nodesKeywordsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalServerError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        NODE_ID,
        LibraryPermissions.LIBACCESS
      )
    ).thenReturn(true);
    when(keywordsApi.nodesIdKeywordsGet(NODE_ID)).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = nodesKeywordsGet.executeImpl(
      req,
      status,
      cache
    );

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesKeywordsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodesKeywordsGet, value);
  }
}
