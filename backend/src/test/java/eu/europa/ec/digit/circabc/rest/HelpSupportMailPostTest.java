package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CaptchaApi;
import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

public class HelpSupportMailPostTest {

  private HelpSupportMailPost webscript;
  private HelpApi helpApi;
  private CaptchaApi captchaApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;

  @Before
  public void setUp() throws Exception {
    webscript = new HelpSupportMailPost();
    helpApi = mock(HelpApi.class);
    captchaApi = mock(CaptchaApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();

    setField("helpApi", helpApi);
    setField("captchaApi", captchaApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpSupportMailPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenValidForm_thenCallsContactSupport()
    throws Exception {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(req.getParameter("language")).thenReturn("en");

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);

    FormData.FormField reasonField = mockField("reason", "bug");
    FormData.FormField nameField = mockField("name", "John");
    FormData.FormField emailField = mockField("email", "john@test.com");
    FormData.FormField subjectField = mockField("subject", "Help");
    FormData.FormField contentField = mockField("content", "Details");

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] {
        reasonField,
        nameField,
        emailField,
        subjectField,
        contentField,
      }
    );

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNotNull(result);
    verify(helpApi).contactSupport(
      eq("bug"),
      eq("John"),
      eq("john@test.com"),
      eq("Help"),
      eq("Details"),
      any()
    );
  }

  @Test
  public void testExecuteImpl_whenGuestWithValidCaptcha_thenSucceeds()
    throws Exception {
    when(permissionChecker.isGuest()).thenReturn(true);
    when(req.getParameter("language")).thenReturn(null);
    when(req.getHeader("X-EU-CAPTCHA-TOKEN")).thenReturn("token");
    when(req.getHeader("X-EU-CAPTCHA-ID")).thenReturn("id");
    when(req.getHeader("X-EU-CAPTCHA-TEXT")).thenReturn("answer");
    when(captchaApi.validate("token", "id", "answer")).thenReturn(true);

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);
    when(formData.getFields()).thenReturn(new FormData.FormField[] {});

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNotNull(result);
    verify(captchaApi).validate("token", "id", "answer");
  }

  @Test
  public void testExecuteImpl_whenGuestMissingCaptcha_thenForbidden() {
    when(permissionChecker.isGuest()).thenReturn(true);
    when(req.getParameter("language")).thenReturn("en");
    when(req.getHeader("X-EU-CAPTCHA-TOKEN")).thenReturn(null);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenGuestInvalidCaptcha_thenForbidden() {
    when(permissionChecker.isGuest()).thenReturn(true);
    when(req.getParameter("language")).thenReturn("en");
    when(req.getHeader("X-EU-CAPTCHA-TOKEN")).thenReturn("token");
    when(req.getHeader("X-EU-CAPTCHA-ID")).thenReturn("id");
    when(req.getHeader("X-EU-CAPTCHA-TEXT")).thenReturn("wrong");
    when(captchaApi.validate("token", "id", "wrong")).thenReturn(false);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNotMultipart_thenInternalServerError() {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(req.getParameter("language")).thenReturn("en");
    when(req.parseContent()).thenReturn(null);

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenFileAttachment_thenIncludedInCall()
    throws Exception {
    when(permissionChecker.isGuest()).thenReturn(false);
    when(req.getParameter("language")).thenReturn("en");

    FormData formData = mock(FormData.class);
    when(formData.getIsMultiPart()).thenReturn(true);
    when(req.parseContent()).thenReturn(formData);

    FormData.FormField fileField = mock(FormData.FormField.class);
    when(fileField.getName()).thenReturn("file1");
    when(fileField.getIsFile()).thenReturn(true);
    when(fileField.getFilename()).thenReturn("test.pdf");
    when(fileField.getInputStream()).thenReturn(
      new ByteArrayInputStream("data".getBytes())
    );

    when(formData.getFields()).thenReturn(
      new FormData.FormField[] { fileField }
    );

    Map<String, Object> result = webscript.executeImpl(req, status, null);

    assertNotNull(result);
    verify(helpApi).contactSupport(
      eq(""),
      eq(""),
      eq(""),
      eq(""),
      eq(""),
      any()
    );
  }

  private FormData.FormField mockField(String name, String value) {
    FormData.FormField field = mock(FormData.FormField.class);
    when(field.getName()).thenReturn(name);
    when(field.getValue()).thenReturn(value);
    when(field.getIsFile()).thenReturn(false);
    return field;
  }
}
