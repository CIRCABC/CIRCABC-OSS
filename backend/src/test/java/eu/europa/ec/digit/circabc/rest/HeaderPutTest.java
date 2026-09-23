package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HeadersApi;
import io.swagger.model.Header;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HeaderPutTest {

  private HeaderPut headerPut;
  private HeadersApi headerApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    headerPut = new HeaderPut();
    headerApi = mock(HeadersApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("headerApi", headerApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test(expected = WebScriptException.class)
  public void testExecuteImpl_whenNoContent_thenThrowsBadRequest() {
    WebScriptRequest req = mock(WebScriptRequest.class);
    when(req.getContent()).thenReturn(null);

    headerPut.executeImpl(req, new Status(), new Cache());
  }

  @Test(expected = AccessDeniedException.class)
  public void testExecuteImpl_whenNotAdmin_thenThrowsAccessDenied()
    throws Exception {
    WebScriptRequest req = mockRequestWithBody(
      "test-id",
      "{\"name\":\"Test\",\"description\":{\"en\":\"desc\"}}"
    );
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);

    headerPut.executeImpl(req, new Status(), new Cache());
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenUpdatesHeader()
    throws Exception {
    String json = "{\"name\":\"NewName\",\"description\":{\"en\":\"English\"}}";
    WebScriptRequest req = mockRequestWithBody("header-id-1", json);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    Header returnedHeader = new Header();
    returnedHeader.setName("NewName");
    when(headerApi.putHeader(eq("header-id-1"), any(Header.class))).thenReturn(
      returnedHeader
    );

    Map<String, Object> model = headerPut.executeImpl(
      req,
      new Status(),
      new Cache()
    );

    assertNotNull(model);
    assertEquals(returnedHeader, model.get("header"));
    verify(headerApi).putHeader(eq("header-id-1"), any(Header.class));
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenUpdatesHeader()
    throws Exception {
    String json =
      "{\"name\":\"AnotherName\",\"description\":{\"fr\":\"French\"}}";
    WebScriptRequest req = mockRequestWithBody("header-id-2", json);
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    Header returnedHeader = new Header();
    returnedHeader.setName("AnotherName");
    when(headerApi.putHeader(eq("header-id-2"), any(Header.class))).thenReturn(
      returnedHeader
    );

    Map<String, Object> model = headerPut.executeImpl(
      req,
      new Status(),
      new Cache()
    );

    assertNotNull(model);
    assertEquals(returnedHeader, model.get("header"));
  }

  @Test
  public void testExecuteImpl_whenDuplicateName_thenReturnsNullWithConflict()
    throws Exception {
    String json = "{\"name\":\"Duplicate\",\"description\":{\"en\":\"desc\"}}";
    WebScriptRequest req = mockRequestWithBody("header-id-3", json);
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(headerApi.putHeader(eq("header-id-3"), any(Header.class))).thenThrow(
      new DuplicateChildNodeNameException(
        new org.alfresco.service.cmr.repository.NodeRef(
          StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
          "test"
        ),
        null,
        "Duplicate",
        null
      )
    );

    Status status = new Status();
    Map<String, Object> model = headerPut.executeImpl(req, status, new Cache());

    assertNull(model);
    assertEquals(Status.STATUS_CONFLICT, status.getCode());
  }

  @Test(expected = WebScriptException.class)
  public void testExecuteImpl_whenInvalidJson_thenThrowsBadRequest()
    throws Exception {
    WebScriptRequest req = mockRequestWithBody("header-id", "not valid json");
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    headerPut.executeImpl(req, new Status(), new Cache());
  }

  private WebScriptRequest mockRequestWithBody(String id, String body)
    throws Exception {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(body);
    when(req.getContent()).thenReturn(content);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", id);
    Match match = new Match("header", templateVars, "/circabc/headers/" + id);
    when(req.getServiceMatch()).thenReturn(match);

    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HeaderPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(headerPut, value);
  }
}
