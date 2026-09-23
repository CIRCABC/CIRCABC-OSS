package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.AppMessageApi;
import io.swagger.model.db.DistributionEmailDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AppDistributionMailsPostTest {

  private AppDistributionMailsPost webscript;
  private AppMessageApi appMessageApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new AppDistributionMailsPost();
    appMessageApi = mock(AppMessageApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("appMessageApi", appMessageApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AppDistributionMailsPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }

  private void mockRequestBody(String json) throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(json);
    when(req.getContent()).thenReturn(content);
  }

  @Test
  public void testExecuteImpl_whenSingleEmailAndUserMatches_thenSuccess()
    throws Exception {
    mockRequestBody("[{\"emailAddress\":\"test@example.com\"}]");
    when(
      permissionCheckerService.isCurrentUserEmailEqualTo("test@example.com")
    ).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(appMessageApi).addAppDistributionPostEmails(anyList());
  }

  @Test
  public void testExecuteImpl_whenSingleEmailAndAdmin_thenSuccess()
    throws Exception {
    mockRequestBody("[{\"emailAddress\":\"other@example.com\"}]");
    when(
      permissionCheckerService.isCurrentUserEmailEqualTo("other@example.com")
    ).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(appMessageApi).addAppDistributionPostEmails(anyList());
  }

  @Test
  public void testExecuteImpl_whenSingleEmailAndNotAdminNotOwner_thenForbidden()
    throws Exception {
    mockRequestBody("[{\"emailAddress\":\"other@example.com\"}]");
    when(
      permissionCheckerService.isCurrentUserEmailEqualTo("other@example.com")
    ).thenReturn(false);
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenMultipleEmailsAndAdmin_thenSuccess()
    throws Exception {
    mockRequestBody(
      "[{\"emailAddress\":\"a@example.com\"},{\"emailAddress\":\"b@example.com\"}]"
    );
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(appMessageApi).addAppDistributionPostEmails(anyList());
  }

  @Test
  public void testExecuteImpl_whenMultipleEmailsAndNotAdmin_thenForbidden()
    throws Exception {
    mockRequestBody(
      "[{\"emailAddress\":\"a@example.com\"},{\"emailAddress\":\"b@example.com\"}]"
    );
    when(permissionCheckerService.isCircabcAdmin()).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenEmptyList_thenBadRequest() throws Exception {
    mockRequestBody("[]");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenBadRequest()
    throws Exception {
    mockRequestBody("not valid json");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenIOException_thenBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new java.io.IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }
}
