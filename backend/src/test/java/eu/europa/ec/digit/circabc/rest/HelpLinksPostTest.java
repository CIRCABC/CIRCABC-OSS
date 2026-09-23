package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpLink;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.*;

public class HelpLinksPostTest {

  private HelpLinksPost helpLinksPost;
  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    helpLinksPost = new HelpLinksPost();
    helpApi = mock(HelpApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("helpApi", helpApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenCreatesLink()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(
      "{\"id\":\"link-1\",\"href\":\"http://example.com\"}"
    );
    when(req.getContent()).thenReturn(content);

    HelpLink createdLink = new HelpLink();
    createdLink.setId("link-1");
    when(helpApi.createHelpLink(any(HelpLink.class))).thenReturn(createdLink);

    Map<String, Object> result = helpLinksPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(createdLink, result.get("link"));
    verify(helpApi).createHelpLink(any(HelpLink.class));
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenCreatesLink()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("{\"id\":\"link-1\"}");
    when(req.getContent()).thenReturn(content);

    HelpLink createdLink = new HelpLink();
    when(helpApi.createHelpLink(any(HelpLink.class))).thenReturn(createdLink);

    Map<String, Object> result = helpLinksPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(createdLink, result.get("link"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      false
    );
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );

    Map<String, Object> result = helpLinksPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(helpApi, never()).createHelpLink(any(HelpLink.class));
  }

  @Test
  public void testExecuteImpl_whenIOException_thenReturnsBadRequest()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );

    Content content = mock(Content.class);
    when(content.getContent()).thenThrow(new IOException("read error"));
    when(req.getContent()).thenReturn(content);

    Map<String, Object> result = helpLinksPost.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(currentUserPermissionCheckerService.isAlfrescoAdmin()).thenReturn(
      true
    );
    when(req.getParameter("language")).thenReturn("fr");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("{\"id\":\"link-1\"}");
    when(req.getContent()).thenReturn(content);

    HelpLink createdLink = new HelpLink();
    when(helpApi.createHelpLink(any(HelpLink.class))).thenReturn(createdLink);

    Map<String, Object> result = helpLinksPost.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals(createdLink, result.get("link"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HelpLinksPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(helpLinksPost, value);
  }
}
