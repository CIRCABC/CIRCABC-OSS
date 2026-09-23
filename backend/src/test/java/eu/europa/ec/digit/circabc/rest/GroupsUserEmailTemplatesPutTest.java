package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EmailApi;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsUserEmailTemplatesPutTest {

  private GroupsUserEmailTemplatesPut webscript;
  private EmailApi emailApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsUserEmailTemplatesPut();
    emailApi = mock(EmailApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Field field = GroupsUserEmailTemplatesPut.class.getDeclaredField(
      "emailApi"
    );
    field.setAccessible(true);
    field.set(webscript, emailApi);
  }

  @Test
  public void testExecuteImpl_whenValidParams_thenReturnsId() {
    when(req.getParameter("templateName")).thenReturn("myTemplate");
    when(req.getParameter("templateSubject")).thenReturn("mySubject");
    when(req.getParameter("templateText")).thenReturn("myText");
    when(req.getParameter("overwrite")).thenReturn("true");
    when(
      emailApi.saveUserMailTemplate("myTemplate", "mySubject", "myText", true)
    ).thenReturn("node-123");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("node-123", model.get("id"));
  }

  @Test
  public void testExecuteImpl_whenOverwriteFalse_thenPassesFalse() {
    when(req.getParameter("templateName")).thenReturn("name");
    when(req.getParameter("templateSubject")).thenReturn("subject");
    when(req.getParameter("templateText")).thenReturn("text");
    when(req.getParameter("overwrite")).thenReturn("false");
    when(
      emailApi.saveUserMailTemplate("name", "subject", "text", false)
    ).thenReturn("node-456");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals("node-456", model.get("id"));
  }

  @Test
  public void testExecuteImpl_whenTemplateNameNull_thenReturnsNull() {
    when(req.getParameter("templateName")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenTemplateNameEmpty_thenReturnsNull() {
    when(req.getParameter("templateName")).thenReturn("");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenTemplateSubjectNull_thenReturnsNull() {
    when(req.getParameter("templateName")).thenReturn("name");
    when(req.getParameter("templateSubject")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenTemplateTextEmpty_thenReturnsNull() {
    when(req.getParameter("templateName")).thenReturn("name");
    when(req.getParameter("templateSubject")).thenReturn("subject");
    when(req.getParameter("templateText")).thenReturn("");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("templateName")).thenReturn("name");
    when(req.getParameter("templateSubject")).thenReturn("subject");
    when(req.getParameter("templateText")).thenReturn("text");
    when(req.getParameter("overwrite")).thenReturn("true");
    when(
      emailApi.saveUserMailTemplate("name", "subject", "text", true)
    ).thenThrow(new AccessDeniedException("denied"));

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }
}
