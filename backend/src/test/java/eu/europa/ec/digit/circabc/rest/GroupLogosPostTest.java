package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.Node;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class GroupLogosPostTest {

  private GroupLogosPost webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupLogosPost();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  @Test
  public void testExecuteImpl_whenValidUpload_thenReturnsLogos()
    throws Exception {
    String groupId = "test-group-id";
    WebScriptRequest req = mockRequest(groupId, null);
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getFilename()).thenReturn("logo.png");
    InputStream inputStream = new ByteArrayInputStream(new byte[] { 1, 2 });
    when(field.getInputStream()).thenReturn(inputStream);
    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });

    Node node = new Node();
    when(
      groupsApi.postGroupLogoByGroupId(eq(groupId), any(), eq("logo.png"))
    ).thenReturn(node);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    @SuppressWarnings("unchecked")
    List<Node> logos = (List<Node>) model.get("logos");
    assertEquals(1, logos.size());
    assertSame(node, logos.get(0));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden()
    throws Exception {
    String groupId = "test-group-id";
    WebScriptRequest req = mockRequest(groupId, null);
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenThrows() throws Exception {
    String groupId = "test-group-id";
    WebScriptRequest req = mockRequest(groupId, null);
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    try {
      webScript.executeImpl(req, status, cache);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Not a multipart request.", e.getMessage());
    }
  }

  @Test
  public void testExecuteImpl_whenNullForm_thenThrows() throws Exception {
    String groupId = "test-group-id";
    WebScriptRequest req = mockRequest(groupId, null);
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    try {
      webScript.executeImpl(req, status, cache);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertEquals("Not a multipart request.", e.getMessage());
    }
  }

  @Test
  public void testExecuteImpl_whenLanguageSet_thenSetsLocale()
    throws Exception {
    String groupId = "test-group-id";
    WebScriptRequest req = mockRequest(groupId, "fr");
    Status status = new Status();
    Cache cache = new Cache();

    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);
    when(formData.getFields()).thenReturn(new FormData.FormField[] {});

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    @SuppressWarnings("unchecked")
    List<Node> logos = (List<Node>) model.get("logos");
    assertTrue(logos.isEmpty());
  }

  private WebScriptRequest mockRequest(String groupId, String language) {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", groupId);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
    return req;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupLogosPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }
}
