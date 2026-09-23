package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
import org.springframework.extensions.webscripts.servlet.FormData;

public class GroupsBulkKeywordsPostTest {

  private GroupsBulkKeywordsPost webScript;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsBulkKeywordsPost();
    keywordsApi = mock(KeywordsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("keywordsApi", keywordsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsBulkKeywordsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String igId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("igId", igId);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isInterestGroupLibAdmin(igId)).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenFormIsNull_thenThrowsException() {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isInterestGroupLibAdmin(igId)).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenNotMultipart_thenThrowsException() {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isInterestGroupLibAdmin(igId)).thenReturn(true);
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenMultipartNoFile_thenReturnsKeywords() {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isInterestGroupLibAdmin(igId)).thenReturn(true);
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(formData.getFields()).thenReturn(new FormData.FormField[0]);
    when(req.parseContent()).thenReturn(formData);

    List<KeywordDefinition> keywords = new ArrayList<>();
    when(keywordsApi.groupsIdKeywordsGet(igId)).thenReturn(keywords);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(keywords, model.get("keywords"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.isInterestGroupLibAdmin(igId)).thenReturn(true);
    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(formData.getFields()).thenReturn(new FormData.FormField[0]);
    when(req.parseContent()).thenReturn(formData);

    List<KeywordDefinition> keywords = new ArrayList<>();
    when(keywordsApi.groupsIdKeywordsGet(igId)).thenReturn(keywords);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertSame(keywords, model.get("keywords"));
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    String igId = "test-ig-id";
    mockTemplateVars(igId);
    when(req.getParameter("language")).thenReturn(null);
    when(permissionChecker.isInterestGroupLibAdmin(igId)).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
