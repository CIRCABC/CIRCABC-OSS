package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AuditIdHistoryGetTest {

  private AuditIdHistoryGet webscript;
  private LogService logService;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

  @Before
  public void setUp() throws Exception {
    webscript = new AuditIdHistoryGet();
    logService = mock(LogService.class);
    nodeService = mock(NodeService.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);

    setField("logService", logService);
    setField("nodeService", nodeService);
    setField("currentUserPermissionCheckerService", permissionCheckerService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);

    when(
      permissionCheckerService.hasAlfrescoReadPermission(TEST_ID)
    ).thenReturn(true);
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
  }

  @Test
  public void testExecuteImpl_whenDefaultPagination_thenReturnsResults() {
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("limit")).thenReturn(null);

    List<LogSearchResultDAO> results = List.of(new LogSearchResultDAO());
    when(logService.countHistory(100L, TEST_ID)).thenReturn(1L);
    when(logService.getHistory(100L, TEST_ID, 0L, 25L)).thenReturn(results);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(results, model.get("logResults"));
    assertEquals(1L, model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenLimitZero_thenReturnsAllResults() {
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("0");

    List<LogSearchResultDAO> results = new ArrayList<>();
    results.add(new LogSearchResultDAO());
    results.add(new LogSearchResultDAO());
    when(logService.getHistory(100L, TEST_ID)).thenReturn(results);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(results, model.get("logResults"));
    assertEquals((long) results.size(), model.get("total"));
  }

  @Test
  public void testExecuteImpl_whenCustomPagination_thenUsesOffset() {
    when(req.getParameter("page")).thenReturn("3");
    when(req.getParameter("limit")).thenReturn("10");

    when(logService.countHistory(100L, TEST_ID)).thenReturn(50L);
    when(logService.getHistory(100L, TEST_ID, 20L, 10L)).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(50L, model.get("total"));
    verify(logService).getHistory(100L, TEST_ID, 20L, 10L);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenPageIsZero_thenThrows() {
    when(req.getParameter("page")).thenReturn("0");
    when(req.getParameter("limit")).thenReturn("10");

    webscript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenPageIsNegative_thenThrows() {
    when(req.getParameter("page")).thenReturn("-1");
    when(req.getParameter("limit")).thenReturn("10");

    webscript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenLimitIsNegative_thenThrows() {
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("-5");

    webscript.executeImpl(req, status, cache);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecuteImpl_whenPageNotNumeric_thenThrows() {
    when(req.getParameter("page")).thenReturn("abc");
    when(req.getParameter("limit")).thenReturn("10");

    webscript.executeImpl(req, status, cache);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(
      permissionCheckerService.hasAlfrescoReadPermission(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNodeNotFound_thenReturnsError() {
    when(req.getParameter("page")).thenReturn("1");
    when(req.getParameter("limit")).thenReturn("10");
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    Map<String, Object> model = webscript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AuditIdHistoryGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
