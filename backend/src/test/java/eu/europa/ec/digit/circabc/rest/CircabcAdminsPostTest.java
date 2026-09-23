package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CircabcApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CircabcAdminsPostTest {

  private CircabcAdminsPost circabcAdminsPost;
  private CircabcApi circabcApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    circabcAdminsPost = new CircabcAdminsPost();
    circabcApi = mock(CircabcApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("circabcApi", circabcApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcAdminsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(circabcAdminsPost, value);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenSuccess() throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("[\"user1\",\"user2\"]");

    Map<String, Object> result = circabcAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(circabcApi).circabcAdminsPost(anyList());
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenSuccess() throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn("en");

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("[\"user1\"]");

    Map<String, Object> result = circabcAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(circabcApi).circabcAdminsPost(anyList());
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenForbidden() throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(req.getParameter("language")).thenReturn(null);

    Map<String, Object> result = circabcAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(circabcApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("invalid json");

    Map<String, Object> result = circabcAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenApiThrowsException_thenInternalError()
    throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("[\"user1\"]");
    doThrow(new RuntimeException("unexpected"))
      .when(circabcApi)
      .circabcAdminsPost(anyList());

    Map<String, Object> result = circabcAdminsPost.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
