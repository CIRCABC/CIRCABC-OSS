package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.db.LogActivityDAO;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.Arrays;
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

public class AuditActivitiesGetTest {

  private AuditActivitiesGet webscript;
  private LogService logService;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private NodeService nodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    webscript = new AuditActivitiesGet();
    logService = mock(LogService.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );
    nodeService = mock(NodeService.class);

    setField("logService", logService);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );
    setField("nodeService", nodeService);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    Match match = new Match("", templateVars, "");
    when(req.getServiceMatch()).thenReturn(match);
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsActivities()
    throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(123L);

    LogActivityDAO activity = new LogActivityDAO();
    List<LogActivityDAO> activities = Arrays.asList(activity);
    when(logService.getActivitiesById(123L)).thenReturn(activities);

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(activities, model.get("activities"));
  }

  @Test
  public void testExecuteImpl_whenGroupAdmin_thenReturnsActivities()
    throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(TEST_ID)
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isGroupAdmin(TEST_ID)).thenReturn(
      true
    );

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(456L);

    when(logService.getActivitiesById(456L)).thenReturn(
      Collections.emptyList()
    );

    Map<String, Object> model = callExecuteImpl();

    assertNotNull(model);
    assertEquals(Collections.emptyList(), model.get("activities"));
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(
      false
    );
    when(
      currentUserPermissionCheckerService.isCategoryAdmin(TEST_ID)
    ).thenReturn(false);
    when(currentUserPermissionCheckerService.isGroupAdmin(TEST_ID)).thenReturn(
      false
    );

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNodeNotFound_thenReturnsError()
    throws Exception {
    when(currentUserPermissionCheckerService.isCircabcAdmin()).thenReturn(true);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(nodeService.exists(nodeRef)).thenReturn(false);

    Map<String, Object> model = callExecuteImpl();

    assertNull(model);
    assertEquals(Status.STATUS_NOT_ACCEPTABLE, status.getCode());
  }

  private Map<String, Object> callExecuteImpl() throws Exception {
    java.lang.reflect.Method method = org.springframework.extensions.webscripts
      .DeclarativeWebScript.class.getDeclaredMethod(
      "executeImpl",
      WebScriptRequest.class,
      Status.class,
      Cache.class
    );
    method.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Object> result = (Map<String, Object>) method.invoke(
      webscript,
      req,
      status,
      cache
    );
    return result;
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AuditActivitiesGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webscript, value);
  }
}
