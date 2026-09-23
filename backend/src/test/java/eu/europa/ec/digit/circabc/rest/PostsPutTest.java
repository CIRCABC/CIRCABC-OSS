package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class PostsPutTest {

  private PostsPut postsPut;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;

  @Before
  public void setUp() throws Exception {
    postsPut = new PostsPut();
    topicsApi = mock(TopicsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    nodeService = mock(NodeService.class);

    setField("topicsApi", topicsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("nodeService", nodeService);

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
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    WebScriptRequest req = mockRequest("post-id");
    Status status = new Status();

    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(false);

    Map<String, Object> result = postsPut.executeImpl(req, status, new Cache());

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullFormData_thenReturnsBadRequest() {
    WebScriptRequest req = mockRequest("post-id");
    Status status = new Status();

    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = postsPut.executeImpl(req, status, new Cache());

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyFormFields_thenReturnsError() {
    WebScriptRequest req = mockRequest("post-id");
    Status status = new Status();
    FormData formData = mock(FormData.class);

    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);
    when(formData.getFields()).thenReturn(new FormData.FormField[0]);

    Map<String, Object> result = postsPut.executeImpl(req, status, new Cache());

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenHasNewsGroupPermission_thenProceeds() {
    WebScriptRequest req = mockRequest("post-id");
    Status status = new Status();
    FormData formData = mock(FormData.class);
    FormData.FormField field = mock(FormData.FormField.class);

    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);
    when(req.getParameter("notify")).thenReturn("false");
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(field.getName()).thenReturn("unexpected");

    Map<String, Object> result = postsPut.executeImpl(req, status, new Cache());

    // unexpected field triggers IllegalArgumentException -> internal server error
    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenHasLibraryPermission_thenProceeds() {
    WebScriptRequest req = mockRequest("post-id");
    Status status = new Status();
    FormData formData = mock(FormData.class);
    FormData.FormField field = mock(FormData.FormField.class);

    when(
      currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(false);
    when(
      currentUserPermissionCheckerService.hasAnyOfLibraryPermission(
        eq("post-id"),
        any(),
        any(),
        any()
      )
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);
    when(req.getParameter("notify")).thenReturn("false");
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });
    when(field.getName()).thenReturn("post");
    when(field.getIsFile()).thenReturn(false);
    when(field.getValue()).thenReturn(
      "{\"name\":\"test\",\"properties\":{\"text\":\"hello\"}}"
    );

    Node node = new Node();
    node.setId("post-id");
    when(
      topicsApi.postsIdPut(
        eq("post-id"),
        any(),
        anyList(),
        anyList(),
        anyList()
      )
    ).thenReturn(node);

    Map<String, Object> result = postsPut.executeImpl(req, status, new Cache());

    assertNotNull(result);
    assertEquals(node, result.get("post"));
  }

  private WebScriptRequest mockRequest(String id) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    java.util.Map<String, String> vars = new java.util.HashMap<>();
    vars.put("id", id);
    String template = "/circabc/posts/{id}";
    Match match = new Match(template, vars, template.replace("{id}", id));
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("notify")).thenReturn(null);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PostsPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(postsPut, value);
  }
}
