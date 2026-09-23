package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.NotificationService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.api.SpacesApi;
import io.swagger.model.NotifiableUser;
import io.swagger.util.ApiToolBox;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class SpaceDeleteTest {

  private SpaceDelete spaceDelete;
  private SpacesApi spacesApi;
  private NotificationService notificationService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private NotificationSubscriptionService notificationSubscriptionService;
  private NodeService unsecureNodeService;
  private ApiToolBox apiToolBox;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  private static final String TEST_ID = "test-space-id";

  @Before
  public void setUp() throws Exception {
    spaceDelete = new SpaceDelete();
    spacesApi = mock(SpacesApi.class);
    notificationService = mock(NotificationService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );
    unsecureNodeService = mock(NodeService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField(SpaceDelete.class, "spacesApi", spacesApi);
    setField(SpaceDelete.class, "notificationService", notificationService);
    setField(
      SpaceDelete.class,
      "currentUserPermissionCheckerService",
      permissionChecker
    );
    setField(
      SpaceDelete.class,
      "notificationSubscriptionService",
      notificationSubscriptionService
    );
    setField(
      CircabcDeclarativeWebScript.class,
      "unsecureNodeService",
      unsecureNodeService
    );
    setField(CircabcDeclarativeWebScript.class, "apiToolBox", apiToolBox);

    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();
  }

  @Test
  public void testExecuteImpl_whenValidIdAndPermitted_thenDeletesSpace() {
    setupRequest(TEST_ID, null, null);
    setupPermission(true);
    setupRecordBeforeDelete();

    Set<NotifiableUser> users = new HashSet<>();
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(users);

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("result"));
    verify(spacesApi).spaceDelete(TEST_ID);
  }

  @Test
  public void testExecuteImpl_whenNotifyFalse_thenSkipsNotification() {
    setupRequest(TEST_ID, null, "false");
    setupPermission(true);
    setupRecordBeforeDelete();

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("result"));
    verifyNoInteractions(notificationSubscriptionService);
    verifyNoInteractions(notificationService);
  }

  @Test
  public void testExecuteImpl_whenAccessDenied_thenReturnsForbidden() {
    setupRequest(TEST_ID, null, null);
    setupPermission(false);

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(new HashSet<>());

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verify(spacesApi, never()).spaceDelete(anyString());
  }

  @Test
  public void testExecuteImpl_whenInvalidNodeRef_thenReturnsBadRequest() {
    setupRequest(TEST_ID, null, null);
    setupPermission(true);
    setupRecordBeforeDelete();

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(new HashSet<>());
    doThrow(new InvalidNodeRefException(nodeRef))
      .when(spacesApi)
      .spaceDelete(TEST_ID);

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenInvalidType_thenReturnsBadRequest() {
    setupRequest(TEST_ID, null, null);
    setupPermission(true);
    setupRecordBeforeDelete();

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(new HashSet<>());
    doThrow(new InvalidTypeException("bad type", null))
      .when(spacesApi)
      .spaceDelete(TEST_ID);

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_BAD_REQUEST, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenUnexpectedException_thenReturnsInternalError() {
    setupRequest(TEST_ID, null, null);
    setupPermission(true);
    setupRecordBeforeDelete();

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(new HashSet<>());
    doThrow(new RuntimeException("unexpected"))
      .when(spacesApi)
      .spaceDelete(TEST_ID);

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNull(result);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenLanguageProvided_thenSetsLocale() {
    setupRequest(TEST_ID, "fr", null);
    setupPermission(true);
    setupRecordBeforeDelete();

    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(nodeRef)
    ).thenReturn(new HashSet<>());

    Map<String, Object> result = spaceDelete.executeImpl(req, status, cache);

    assertNotNull(result);
    assertEquals("ok", result.get("result"));
    verify(spacesApi).spaceDelete(TEST_ID);
  }

  private void setupRequest(String id, String language, String notify) {
    Map<String, String> templateVars = new HashMap<>();
    if (id != null) {
      templateVars.put("id", id);
    }
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
    when(req.getParameter("language")).thenReturn(language);
    when(req.getParameter("notify")).thenReturn(notify);
  }

  private void setupPermission(boolean hasPermission) {
    when(permissionChecker.hasAlfrescoDeletePermission(TEST_ID)).thenReturn(
      hasPermission
    );
  }

  private void setupRecordBeforeDelete() {
    NodeRef deletedRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_ID
    );
    when(apiToolBox.getCurrentInterestGroup(deletedRef)).thenReturn(null);
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef assoc = mock(ChildAssociationRef.class);
    when(assoc.getParentRef()).thenReturn(parentRef);
    when(unsecureNodeService.getPrimaryParent(deletedRef)).thenReturn(assoc);
    when(apiToolBox.getCircabcPath(deletedRef, true)).thenReturn("/test/path");
    when(apiToolBox.getDatabaseID(deletedRef)).thenReturn(123L);
  }

  private void setField(Class<?> clazz, String fieldName, Object value)
    throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(spaceDelete, value);
  }
}
