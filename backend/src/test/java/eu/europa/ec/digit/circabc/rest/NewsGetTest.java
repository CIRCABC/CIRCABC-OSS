package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.InformationApi;
import io.swagger.model.News;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NewsGetTest {

  private NewsGet newsGet;
  private InformationApi informationApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    newsGet = new NewsGet();
    informationApi = mock(InformationApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("informationApi", informationApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "test-node-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenHasPermissionAndNoLanguage_thenReturnsNewsInfo()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);
    News newsInfo = new News();
    when(informationApi.newsIdGet("test-node-id")).thenReturn(newsInfo);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertSame(newsInfo, model.get("newsInfo"));
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenReturnsNewsInfo()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);
    News newsInfo = new News();
    when(informationApi.newsIdGet("test-node-id")).thenReturn(newsInfo);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertSame(newsInfo, model.get("newsInfo"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(false);

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);
    when(informationApi.newsIdGet("test-node-id")).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "test-node-id")
      )
    );

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);
    when(
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        "test-node-id"
      )
    ).thenReturn(true);
    when(informationApi.newsIdGet("test-node-id")).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method = NewsGet.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      newsGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NewsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(newsGet, value);
  }
}
