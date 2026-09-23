package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.ContentApi;
import io.swagger.model.NotifiableUser;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ContentDeleteTest {

  private ContentDelete contentDelete;
  private ContentApi contentApi;
  private NotificationService notificationService;
  private CurrentUserPermissionCheckerService permissionCheckerService;
  private NotificationSubscriptionService notificationSubscriptionService;
  private ApiToolBox apiToolBox;
  private NodeService unsecureNodeService;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-node-id";

  @Before
  public void setUp() throws Exception {
    contentDelete = new ContentDelete();
    contentApi = mock(ContentApi.class);
    notificationService = mock(NotificationService.class);
    permissionCheckerService = mock(CurrentUserPermissionCheckerService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );
    apiToolBox = mock(ApiToolBox.class);
    unsecureNodeService = mock(NodeService.class);

    setField("contentApi", contentApi);
    setField("notificationService", notificationService);
    setField("currentUserPermissionCheckerService", permissionCheckerService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );
    setParentField("apiToolBox", apiToolBox);
    setParentField("unsecureNodeService", unsecureNodeService);

    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(apiToolBox.getCurrentInterestGroup(any(NodeRef.class))).thenReturn(
      parentRef
    );
    when(apiToolBox.getCircabcPath(any(NodeRef.class), eq(true))).thenReturn(
      "/some/path"
    );
    when(apiToolBox.getDatabaseID(any(NodeRef.class))).thenReturn(1L);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenValidRequestWithNotify_thenDeletesAndNotifies()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn("true");
    when(
      permissionCheckerService.hasAlfrescoDeletePermission(TEST_ID)
    ).thenReturn(true);

    Set<NotifiableUser> users = new HashSet<>();
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(users);

    Map<String, Object> result = contentDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("result"));
    verify(notificationService).notifyNewFiles(
      eq(nodeRef),
      anyList(),
      eq(users),
      any()
    );
    verify(contentApi).contentIdDelete(TEST_ID);
  }

  @Test
  public void testExecuteImpl_whenNotifyFalse_thenDeletesWithoutNotification()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn("false");
    when(
      permissionCheckerService.hasAlfrescoDeletePermission(TEST_ID)
    ).thenReturn(true);

    Map<String, Object> result = contentDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("result"));
    verify(notificationService, never()).notifyNewFiles(
      any(),
      anyList(),
      anySet(),
      any()
    );
    verify(contentApi).contentIdDelete(TEST_ID);
  }

  @Test
  public void testExecuteImpl_whenNoPermission_thenReturnsForbidden()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn(null);
    when(
      permissionCheckerService.hasAlfrescoDeletePermission(TEST_ID)
    ).thenReturn(false);

    Map<String, Object> result = contentDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(contentApi, never()).contentIdDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn("false");
    when(
      permissionCheckerService.hasAlfrescoDeletePermission(TEST_ID)
    ).thenReturn(true);
    doThrow(
      new InvalidNodeRefException(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, TEST_ID)
      )
    )
      .when(contentApi)
      .contentIdDelete(TEST_ID);

    Map<String, Object> result = contentDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidType_thenReturnsBadRequest()
    throws Exception {
    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("id", TEST_ID);
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(null);
    when(req.getParameter("notify")).thenReturn("false");
    when(
      permissionCheckerService.hasAlfrescoDeletePermission(TEST_ID)
    ).thenReturn(true);
    doThrow(
      new InvalidTypeException(
        "bad type",
        QName.createQName(
          "http://www.alfresco.org/model/content/1.0",
          "content"
        )
      )
    )
      .when(contentApi)
      .contentIdDelete(TEST_ID);

    Map<String, Object> result = contentDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ContentDelete.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(contentDelete, value);
  }

  private void setParentField(String fieldName, Object value) throws Exception {
    Field field = CircabcDeclarativeWebScript.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(contentDelete, value);
  }
}
