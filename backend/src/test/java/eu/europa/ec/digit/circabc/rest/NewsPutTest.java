package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.InformationApi;
import io.swagger.model.News;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class NewsPutTest {

  private NewsPut newsPut;
  private InformationApi informationApi;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final String VALID_JSON =
    "{\"content\":\"Hello\",\"size\":5,\"layout\":\"normal\",\"pattern\":\"iframe\",\"title\":{\"en\":\"Title\"}}";

  @Before
  public void setUp() throws Exception {
    newsPut = new NewsPut();
    informationApi = mock(InformationApi.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("informationApi", informationApi);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
    when(req.getParameter("language")).thenReturn(null);
  }

  @Test
  public void testExecuteImpl_whenPermissionGranted_thenUpdatesSuccessfully()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    News resultNews = new News();
    when(informationApi.newsIdPut(eq(TEST_ID), any(News.class))).thenReturn(
      resultNews
    );

    Map<String, Object> model = newsPut.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNews, model.get("newsInfo"));
    verify(informationApi).newsIdPut(eq(TEST_ID), any(News.class));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> model = newsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidJson_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn("not valid json");
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    Map<String, Object> model = newsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    when(informationApi.newsIdPut(eq(TEST_ID), any(News.class))).thenThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID)
      )
    );

    Map<String, Object> model = newsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError()
    throws Exception {
    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    when(informationApi.newsIdPut(eq(TEST_ID), any(News.class))).thenThrow(
      new RuntimeException("unexpected")
    );

    Map<String, Object> model = newsPut.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");

    Content content = mock(Content.class);
    when(content.getContent()).thenReturn(VALID_JSON);
    when(req.getContent()).thenReturn(content);

    when(
      permissionCheckerService.hasAlfrescoWritePermission(TEST_ID)
    ).thenReturn(true);

    News resultNews = new News();
    when(informationApi.newsIdPut(eq(TEST_ID), any(News.class))).thenReturn(
      resultNews
    );

    Map<String, Object> model = newsPut.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(resultNews, model.get("newsInfo"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NewsPut.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(newsPut, value);
  }
}
