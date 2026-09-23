package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.Category;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class BulkInviteCategoriesGetTest {

  private BulkInviteCategoriesGet webScript;
  private UsersApi usersApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new BulkInviteCategoriesGet();
    usersApi = mock(UsersApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = BulkInviteCategoriesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Test
  public void testExecuteImpl_whenValidUsername_thenReturnsCategories()
    throws Exception {
    when(req.getParameter("username")).thenReturn("testuser");
    List<Category> categories = Arrays.asList(new Category(), new Category());
    when(usersApi.getBulkInviteCategories("testuser")).thenReturn(categories);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(categories, model.get("categories"));
  }

  @Test
  public void testExecuteImpl_whenEmptyCategories_thenReturnsEmptyList()
    throws Exception {
    when(req.getParameter("username")).thenReturn("testuser");
    when(usersApi.getBulkInviteCategories("testuser")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("categories"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenUsernameNull_thenThrowsIllegalArgument() {
    when(req.getParameter("username")).thenReturn(null);

    webScript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenUsernameEmpty_thenThrowsIllegalArgument() {
    when(req.getParameter("username")).thenReturn("   ");

    webScript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("username")).thenReturn("testuser");
    when(usersApi.getBulkInviteCategories("testuser")).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("username")).thenReturn("testuser");
    when(usersApi.getBulkInviteCategories("testuser")).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsServerError()
    throws Exception {
    when(req.getParameter("username")).thenReturn("testuser");
    when(usersApi.getBulkInviteCategories("testuser")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
