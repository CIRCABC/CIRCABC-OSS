package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.GroupCreationRequest;
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

public class CategoryRequestGroupPostTest {

  private CategoryRequestGroupPost categoryRequestGroupPost;
  private CategoriesApi categoriesApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String VALID_JSON =
    "{\"id\":1,\"proposedName\":\"TestGroup\",\"categoryRef\":\"cat-ref\",\"justification\":\"Testing\",\"proposedTitle\":{\"en\":\"Title\"},\"proposedDescription\":{\"en\":\"Desc\"},\"leaders\":[{\"userId\":\"admin\",\"firstname\":\"Admin\",\"lastname\":\"User\",\"email\":\"admin@test.com\"}]}";

  @Before
  public void setUp() throws Exception {
    categoryRequestGroupPost = new CategoryRequestGroupPost();
    categoriesApi = mock(CategoriesApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("categoriesApi", categoriesApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoryRequestGroupPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(categoryRequestGroupPost, value);
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
  public void testExecuteImpl_whenValidRequest_thenReturnsModel()
    throws Exception {
    String categoryId = "test-category-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);
    mockContent(VALID_JSON);

    Map<String, Object> result = categoryRequestGroupPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdGroupRequestPost(
      eq(categoryId),
      any(GroupCreationRequest.class)
    );
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsModel()
    throws Exception {
    String categoryId = "test-category-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn("fr");
    mockContent(VALID_JSON);

    Map<String, Object> result = categoryRequestGroupPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(categoriesApi).categoriesIdGroupRequestPost(
      eq(categoryId),
      any(GroupCreationRequest.class)
    );
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    String categoryId = "restricted-id";
    mockTemplateVars(categoryId);
    when(req.getParameter("language")).thenReturn(null);
    mockContent(VALID_JSON);

    doThrow(new AccessDeniedException("denied"))
      .when(categoriesApi)
      .categoriesIdGroupRequestPost(
        eq(categoryId),
        any(GroupCreationRequest.class)
      );

    Map<String, Object> result = categoryRequestGroupPost.executeImpl(
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

    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, categoryId)
      )
    )
      .when(categoriesApi)
      .categoriesIdGroupRequestPost(
        eq(categoryId),
        any(GroupCreationRequest.class)
      );

    Map<String, Object> result = categoryRequestGroupPost.executeImpl(
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

    Map<String, Object> result = categoryRequestGroupPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
