package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.InterestGroup;
import io.swagger.model.InterestGroupPostModel;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CategoryGroupsPostTest {

  private CategoryGroupsPost categoryGroupsPost;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String VALID_JSON =
    "{\"name\":\"TestGroup\",\"title\":{\"en\":\"Test Title\"},\"description\":{\"en\":\"Test Desc\"},\"contact\":{\"en\":\"contact@test.com\"},\"leaders\":[\"admin\"],\"notify\":true,\"notifyText\":{\"en\":\"Notification\"}}";

  @Before
  public void setUp() throws Exception {
    categoryGroupsPost = new CategoryGroupsPost();
    categoriesApi = mock(CategoriesApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("categoriesApi", categoriesApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryGroupsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryGroupsPost, value);
  }

  private void mockTemplateVars(String categoryId) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", categoryId);
    Match match = new Match("", vars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void mockContent(String json) throws IOException {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsGroup()
    throws Exception {
    String categoryId = "test-category-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);
    mockContent(VALID_JSON);

    InterestGroup expectedGroup = new InterestGroup();
    when(
      categoriesApi.categoriesIdGroupsPost(
        eq(categoryId),
        any(InterestGroupPostModel.class)
      )
    ).thenReturn(expectedGroup);

    Map<String, Object> result = categoryGroupsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(expectedGroup, result.get("group"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsGroup()
    throws Exception {
    String categoryId = "test-category-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn("fr");
    mockContent(VALID_JSON);

    InterestGroup expectedGroup = new InterestGroup();
    when(
      categoriesApi.categoriesIdGroupsPost(
        eq(categoryId),
        any(InterestGroupPostModel.class)
      )
    ).thenReturn(expectedGroup);

    Map<String, Object> result = categoryGroupsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    assertEquals(expectedGroup, result.get("group"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    String categoryId = "restricted-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);
    mockContent(VALID_JSON);

    when(
      currentUserPermissionCheckerService.throwIfNotCategoryAdmin(categoryId)
    ).thenThrow(new AccessDeniedException("denied"));

    Map<String, Object> result = categoryGroupsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    String categoryId = "invalid-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);
    mockContent(VALID_JSON);

    when(
      categoriesApi.categoriesIdGroupsPost(
        eq(categoryId),
        any(InterestGroupPostModel.class)
      )
    ).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, categoryId)
      )
    );

    Map<String, Object> result = categoryGroupsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    String categoryId = "test-category-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = categoryGroupsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    String categoryId = "error-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);
    mockContent(VALID_JSON);

    when(
      categoriesApi.categoriesIdGroupsPost(
        eq(categoryId),
        any(InterestGroupPostModel.class)
      )
    ).thenThrow(new RuntimeException("unexpected"));

    Map<String, Object> result = categoryGroupsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
