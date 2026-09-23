package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.CategoriesApi;
import io.swagger.model.PagedStatisticsContents;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
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

public class CategoriesIGStatisticsGetTest {

  private CategoriesIGStatisticsGet webscript;
  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webscript = new CategoriesIGStatisticsGet();
    categoriesApi = mock(CategoriesApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("categoriesApi", categoriesApi);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", "cat-id");
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CategoriesIGStatisticsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }

  @Test
  public void testExecuteImpl_whenValidRequest_thenReturnsData()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    PagedStatisticsContents contents = new PagedStatisticsContents(
      Collections.emptyList(),
      5L
    );
    when(categoriesApi.getIGStatisticsContents("cat-id", 0, 10)).thenReturn(
      contents
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("data"));
    assertEquals(5L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenNoPageParam_thenDefaultsToPage1()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn("10");

    PagedStatisticsContents contents = new PagedStatisticsContents(
      Collections.emptyList(),
      0L
    );
    when(categoriesApi.getIGStatisticsContents("cat-id", 0, 10)).thenReturn(
      contents
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    verify(categoriesApi).getIGStatisticsContents("cat-id", 0, 10);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    doThrow(new AccessDeniedException("denied"))
      .when(permissionChecker)
      .throwIfNotCategoryAdmin("cat-id");

    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");

    when(categoriesApi.getIGStatisticsContents("cat-id", 0, 10)).thenThrow(
      new InvalidNodeRefException(
        "invalid",
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "bad-id")
      )
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenPageZero_thenReturnsError() throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNegativeLimit_thenReturnsError()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("-1");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidPageFormat_thenReturnsError()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getParameter("page")).thenReturn("abc");
    when(req.getParameter("limit")).thenReturn("10");

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
