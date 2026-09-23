package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.api.HeadersApi;
import io.swagger.model.Header;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HeaderPostTest {

  private HeaderPost headerPost;
  private HeadersApi headerApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private CircabcService circabcService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    headerPost = new HeaderPost();
    headerApi = mock(HeadersApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    circabcService = mock(CircabcService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("headerApi", headerApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
    setField("circabcService", circabcService);
  }

  @Test(expected = WebScriptException.class)
  public void testExecuteImpl_whenNoContent_thenBadRequest() {
    when(req.getContent()).thenReturn(null);
    headerPost.executeImpl(req, status, cache);
  }

  @Test(expected = AccessDeniedException.class)
  public void testExecuteImpl_whenNotAdmin_thenAccessDenied() throws Exception {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"name\":\"test\",\"description\":{\"en\":\"desc\"}}"
    );
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);

    headerPost.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsHeader()
    throws Exception {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"name\":\"TestHeader\",\"description\":{\"en\":\"A description\"}}"
    );
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    Header resultHeader = new Header();
    resultHeader.setId("header-id-123");
    resultHeader.setName("TestHeader");
    when(headerApi.postHeader(any(Header.class))).thenReturn(resultHeader);

    Map<String, Object> model = headerPost.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultHeader, model.get("header"));
    verify(circabcService).addHeaderNode(any());
  }

  @Test
  public void testExecuteImpl_whenDuplicateName_thenConflict()
    throws Exception {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn(
      "{\"name\":\"Dup\",\"description\":{\"en\":\"desc\"}}"
    );
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(headerApi.postHeader(any(Header.class))).thenThrow(
      new DuplicateChildNodeNameException(null, null, "Dup", null)
    );

    Map<String, Object> model = headerPost.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_CONFLICT, status.getCode());
  }

  @Test(expected = WebScriptException.class)
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getContent()).thenReturn("not json");
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    headerPost.executeImpl(req, status, cache);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HeaderPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(headerPost, value);
  }
}
