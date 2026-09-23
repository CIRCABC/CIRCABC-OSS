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

public class GroupsUserEmailTemplatesDeleteTest {

  private GroupsUserEmailTemplatesDelete webscript;
  private EmailApi emailApi;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new GroupsUserEmailTemplatesDelete();
    emailApi = mock(EmailApi.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Field field = GroupsUserEmailTemplatesDelete.class.getDeclaredField(
      "emailApi"
    );
    field.setAccessible(true);
    field.set(webscript, emailApi);
  }

  @Test
  public void testExecuteImpl_whenValidTemplateIds_thenSuccess() {
    when(req.getParameter("templateIds")).thenReturn("id1,id2");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(emailApi).deleteUserMailTemplates("id1,id2");
  }

  @Test
  public void testExecuteImpl_whenTemplateIdsNull_thenReturnsNull() {
    when(req.getParameter("templateIds")).thenReturn(null);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    verifyNoInteractions(emailApi);
  }

  @Test
  public void testExecuteImpl_whenTemplateIdsEmpty_thenReturnsNull() {
    when(req.getParameter("templateIds")).thenReturn("");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertTrue(status.getMessage().contains("'templateIds' cannot be empty"));
    verifyNoInteractions(emailApi);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenForbidden() {
    when(req.getParameter("templateIds")).thenReturn("id1");
    doThrow(new AccessDeniedException("denied"))
      .when(emailApi)
      .deleteUserMailTemplates("id1");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    assertEquals("Access denied", status.getMessage());
  }

  @Test
  public void testExecuteImpl_whenRuntimeException_thenNotAcceptable() {
    when(req.getParameter("templateIds")).thenReturn("id1");
    doThrow(new RuntimeException("something failed"))
      .when(emailApi)
      .deleteUserMailTemplates("id1");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
    assertEquals("something failed", status.getMessage());
  }
}
