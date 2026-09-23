package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.TopicsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class TopicsUpdatePutTest {

  private TopicsUpdatePut topicsUpdatePut;
  private TopicsApi topicsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Before
  public void setUp() throws Exception {
    topicsUpdatePut = new TopicsUpdatePut();
    topicsApi = mock(TopicsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField(TopicsUpdatePut.class, "topicsApi", topicsApi);
    setField(
      TopicsUpdatePut.class,
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

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
  public void testExecuteImpl_whenValidRequest_thenUpdatesTopic()
    throws Exception {
    String nodeId = "test-node-id";
    String json =
      "{\"name\":\"topic1\",\"title\":{\"en\":\"Title\"},\"description\":{\"en\":\"Desc\"}}";
    WebScriptRequest req = mockRequest(nodeId, json);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeId)
    ).thenReturn(true);

    Map<String, Object> result = topicsUpdatePut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(topicsApi).updateTopic(eq(nodeId), any(Node.class));
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId, "{}");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeId)
    ).thenReturn(false);

    Map<String, Object> result = topicsUpdatePut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(topicsApi, never()).updateTopic(anyString(), any(Node.class));
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsInternalError()
    throws Exception {
    String nodeId = "test-node-id";
    WebScriptRequest req = mockRequest(nodeId, "not valid json");
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeId)
    ).thenReturn(true);

    Map<String, Object> result = topicsUpdatePut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
    verify(topicsApi, never()).updateTopic(anyString(), any(Node.class));
  }

  @Test
  public void testExecuteImpl_whenUpdateThrowsRuntimeException_thenReturnsInternalError()
    throws Exception {
    String nodeId = "test-node-id";
    String json = "{\"name\":\"topic1\"}";
    WebScriptRequest req = mockRequest(nodeId, json);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeId)
    ).thenReturn(true);
    doThrow(new RuntimeException("unexpected"))
      .when(topicsApi)
      .updateTopic(anyString(), any(Node.class));

    Map<String, Object> result = topicsUpdatePut.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPropertiesProvided_thenParsesProperties()
    throws Exception {
    String nodeId = "test-node-id";
    String json =
      "{\"name\":\"topic1\",\"properties\":{\"security_ranking\":\"PUBLIC\",\"expiration_date\":\"2026-12-31\"}}";
    WebScriptRequest req = mockRequest(nodeId, json);
    Status status = new Status();
    Cache cache = new Cache();

    when(
      currentUserPermissionCheckerService.hasAlfrescoWritePermission(nodeId)
    ).thenReturn(true);

    Map<String, Object> result = topicsUpdatePut.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(topicsApi).updateTopic(eq(nodeId), any(Node.class));
  }

  private WebScriptRequest mockRequest(String nodeId, String body)
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", nodeId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(body);
    when(req.getContent()).thenReturn(content);

    return req;
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(topicsUpdatePut, value);
  }
}
