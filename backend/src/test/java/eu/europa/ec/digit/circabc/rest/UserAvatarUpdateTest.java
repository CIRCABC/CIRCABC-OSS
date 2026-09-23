package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

public class UserAvatarUpdateTest {

  private UserAvatarUpdate userAvatarUpdate;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    userAvatarUpdate = new UserAvatarUpdate();
    usersApi = mock(UsersApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("userId", "testuser");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserAvatarUpdate.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userAvatarUpdate, value);
  }

  @Test
  public void testExecuteImpl_whenValidFileUpload_thenSuccess()
    throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getFilename()).thenReturn("avatar.png");
    InputStream inputStream = mock(InputStream.class);
    when(field.getInputStream()).thenReturn(inputStream);

    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });

    Map<String, Object> result = userAvatarUpdate.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(usersApi).updateAvatar("testuser", inputStream, "avatar.png");
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenForbidden() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = userAvatarUpdate.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAdminUser_thenSuccess() throws Exception {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(true);
    when(field.getFilename()).thenReturn("avatar.png");
    InputStream inputStream = mock(InputStream.class);
    when(field.getInputStream()).thenReturn(inputStream);

    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });

    Map<String, Object> result = userAvatarUpdate.executeImpl(
      req,
      status,
      cache
    );

    assertNotNull(result);
    verify(usersApi).updateAvatar("testuser", inputStream, "avatar.png");
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenError() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(false);
    when(req.parseContent()).thenReturn(formData);

    Map<String, Object> result = userAvatarUpdate.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullFormData_thenError() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = userAvatarUpdate.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNoFileField_thenError() {
    when(
      currentUserPermissionCheckerService.isCurrentUserEqualTo("testuser")
    ).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);

    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getIsFile()).thenReturn(false);

    when(formData.getFields()).thenReturn(new FormData.FormField[] { field });

    Map<String, Object> result = userAvatarUpdate.executeImpl(
      req,
      status,
      cache
    );

    assertNull(result);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }
}
