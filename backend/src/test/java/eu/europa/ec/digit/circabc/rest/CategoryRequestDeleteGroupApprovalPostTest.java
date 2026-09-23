package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupDeletionRequestApproval;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryRequestDeleteGroupApprovalPostTest {

  private CategoryRequestDeleteGroupApprovalPost webScript;
  private CategoriesApi categoriesApi;
  private AuthenticationService authenticationService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;
  private Content content;
  private Map<String, String> templateVars;

  @Before
  public void setUp() throws Exception {
    webScript = new CategoryRequestDeleteGroupApprovalPost();
    categoriesApi = mock(CategoriesApi.class);
    authenticationService = mock(AuthenticationService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
    content = mock(Content.class);

    setField("categoriesApi", categoriesApi);
    setField("authenticationService", authenticationService);
    setField("currentUserPermissionCheckerService", permissionChecker);

    templateVars = new HashMap<>();
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getContent()).thenReturn(content);
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryRequestDeleteGroupApprovalPost.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenCategoryAdmin_thenSuccess() throws Exception {
    templateVars.put("id", "cat-123");
    when(permissionChecker.isCategoryAdmin("cat-123")).thenReturn(true);
    when(content.getContent()).thenReturn(
      "{\"id\":1,\"agreement\":1,\"argument\":\"approved\"}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(categoriesApi).categoriesIdGroupRequestDeleteApprovalPost(
      eq("cat-123"),
      any(GroupDeletionRequestApproval.class),
      eq("admin")
    );
  }

  @Test
  public void testExecuteImpl_whenNotCategoryAdmin_thenStatus403()
    throws Exception {
    templateVars.put("id", "cat-456");
    when(permissionChecker.isCategoryAdmin("cat-456")).thenReturn(false);

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(categoriesApi);
  }

  @Test
  public void testExecuteImpl_whenIOException_thenStatus400() throws Exception {
    templateVars.put("id", "cat-789");
    when(permissionChecker.isCategoryAdmin("cat-789")).thenReturn(true);
    when(content.getContent()).thenThrow(new IOException("read error"));

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSuccess() throws Exception {
    templateVars.put("id", "cat-lang");
    when(req.getParameter("language")).thenReturn("fr");
    when(permissionChecker.isCategoryAdmin("cat-lang")).thenReturn(true);
    when(content.getContent()).thenReturn(
      "{\"id\":2,\"agreement\":0,\"argument\":\"rejected\"}"
    );

    Map<String, Object> result = webScript.executeImpl(req, status, cache);

    assertNotNull(result);
    verify(categoriesApi).categoriesIdGroupRequestDeleteApprovalPost(
      eq("cat-lang"),
      any(GroupDeletionRequestApproval.class),
      eq("admin")
    );
  }
}
