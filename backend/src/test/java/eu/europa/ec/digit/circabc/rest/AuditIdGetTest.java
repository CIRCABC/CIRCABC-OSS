package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
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
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class AuditIdGetTest {

  private AuditIdGet auditIdGet;
  private LogService logService;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService permissionChecker;
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
    auditIdGet = new AuditIdGet();
    logService = mock(LogService.class);
    nodeService = mock(NodeService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);

    setField("logService", logService);
    setField("nodeService", nodeService);
    setField("currentUserPermissionCheckerService", permissionChecker);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    org.springframework.extensions.webscripts.Match match =
      new org.springframework.extensions.webscripts.Match(
        null,
        templateVars,
        null
      );
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsResults()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);

    when(req.getParameter("userId")).thenReturn("");
    when(req.getParameter("service")).thenReturn("");
    when(req.getParameter("activity")).thenReturn("");
    when(req.getParameter("from")).thenReturn("2024-01-01T00:00:00.000Z");
    when(req.getParameter("to")).thenReturn("2024-12-31T23:59:59.000Z");

    List<LogSearchResultDAO> expected = Collections.singletonList(
      new LogSearchResultDAO()
    );
    when(
      logService.search(
        eq(100L),
        isNull(),
        isNull(),
        isNull(),
        any(Date.class),
        any(Date.class)
      )
    ).thenReturn(expected);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(expected, model.get("logResults"));
  }

  @Test
  public void testExecuteImpl_whenGroupAdmin_thenReturnsResults()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCategoryAdmin(TEST_ID)).thenReturn(false);
    when(permissionChecker.isGroupAdmin(TEST_ID)).thenReturn(true);
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);

    when(req.getParameter("userId")).thenReturn("john");
    when(req.getParameter("service")).thenReturn("Library");
    when(req.getParameter("activity")).thenReturn("Download");
    when(req.getParameter("from")).thenReturn("2024-01-01T00:00:00.000Z");
    when(req.getParameter("to")).thenReturn("2024-12-31T23:59:59.000Z");

    List<LogSearchResultDAO> expected = Collections.emptyList();
    when(
      logService.search(
        eq(200L),
        eq("john"),
        eq("Library"),
        eq("Download"),
        any(Date.class),
        any(Date.class)
      )
    ).thenReturn(expected);

    Map<String, Object> model = invokeExecuteImpl();

    assertNotNull(model);
    assertEquals(expected, model.get("logResults"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isCategoryAdmin(TEST_ID)).thenReturn(false);
    when(permissionChecker.isGroupAdmin(TEST_ID)).thenReturn(false);

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNodeNotExists_thenReturnsNotAcceptable()
    throws Exception {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    Map<String, Object> model = invokeExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private Map<String, Object> invokeExecuteImpl() throws Exception {
    java.lang.reflect.Method method =
      AuditIdGet.class.getSuperclass().getDeclaredMethod(
        "executeImpl",
        WebScriptRequest.class,
        Status.class,
        Cache.class
      );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      auditIdGet,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AuditIdGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(auditIdGet, value);
  }
}
