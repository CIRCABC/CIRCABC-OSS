package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class GroupsKeywordsGetTest {

  private GroupsKeywordsGet webScript;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsKeywordsGet();
    keywordsApi = mock(KeywordsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("keywordsApi", keywordsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecuteImpl_whenNoLanguage_thenReturnsKeywordsMLAware() {
    when(req.getParameter("language")).thenReturn(null);
    KeywordDefinition kw = new KeywordDefinition();
    kw.setId("kw1");
    List<KeywordDefinition> keywords = Collections.singletonList(kw);
    when(keywordsApi.groupsIdKeywordsGet("test-ig-id")).thenReturn(keywords);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(keywords, result.get("keywords"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsKeywords() {
    when(req.getParameter("language")).thenReturn("fr");
    KeywordDefinition kw = new KeywordDefinition();
    kw.setId("kw-fr");
    List<KeywordDefinition> keywords = Collections.singletonList(kw);
    when(keywordsApi.groupsIdKeywordsGet("test-ig-id")).thenReturn(keywords);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(keywords, result.get("keywords"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("language")).thenReturn(null);
    when(
      permissionChecker.throwIfCanNotAccessInterestGroup("test-ig-id")
    ).thenThrow(new AccessDeniedException("denied"));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    when(req.getParameter("language")).thenReturn(null);
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "bad-id"
    );
    when(
      permissionChecker.throwIfCanNotAccessInterestGroup("test-ig-id")
    ).thenThrow(new InvalidNodeRefException("invalid", nodeRef));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
    assertEquals("Bad request", status.getMessage());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsKeywordsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
