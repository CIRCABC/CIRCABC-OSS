package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.EmailApi;
import io.swagger.model.MailTemplateDefinition;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class GroupsUserEmailTemplatesGetTest {

  private GroupsUserEmailTemplatesGet webscript;
  private EmailApi emailApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsUserEmailTemplatesGet();
    emailApi = mock(EmailApi.class);
    req = mock(WebScriptRequest.class);
    status = mock(Status.class);
    cache = mock(Cache.class);

    Field field = GroupsUserEmailTemplatesGet.class.getDeclaredField(
      "emailApi"
    );
    field.setAccessible(true);
    field.set(webscript, emailApi);
  }

  @Test
  public void testExecuteImpl_whenTemplatesExist_thenReturnsTemplatesInModel()
    throws Exception {
    MailTemplateDefinition t1 = new MailTemplateDefinition(
      "1",
      "t1",
      "s1",
      "b1"
    );
    MailTemplateDefinition t2 = new MailTemplateDefinition(
      "2",
      "t2",
      "s2",
      "b2"
    );
    List<MailTemplateDefinition> templates = Arrays.asList(t1, t2);
    when(emailApi.getUserMailTemplates()).thenReturn(templates);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(templates, model.get("templates"));
  }

  @Test
  public void testExecuteImpl_whenNoTemplates_thenReturnsEmptyList()
    throws Exception {
    when(emailApi.getUserMailTemplates()).thenReturn(Collections.emptyList());

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("templates"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(emailApi.getUserMailTemplates()).thenThrow(
      new AccessDeniedException("denied")
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    verify(status).setCode(Status.STATUS_FORBIDDEN);
    verify(status).setMessage("Access denied");
    verify(status).setRedirect(true);
  }

  @Test
  public void testExecuteImpl_whenExceptionThrown_thenReturnsNotAcceptable()
    throws Exception {
    RuntimeException ex = new RuntimeException("something failed");
    when(emailApi.getUserMailTemplates()).thenThrow(ex);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    verify(status).setCode(Status.STATUS_NOT_ACCEPTABLE);
    verify(status).setMessage("something failed");
    verify(status).setException(ex);
    verify(status).setRedirect(true);
  }
}
