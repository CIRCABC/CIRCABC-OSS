package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.notification.AuthorityNotification;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationManagerService;
import eu.europa.ec.digit.circabc.rest.service.notification.NotificationSubscriptionService;
import io.swagger.model.*;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class NotificationsApiImplTest {

  private NotificationsApiImpl notificationsApi;
  private PermissionService permissionService;
  private PersonService personService;
  private NodeService nodeService;
  private NotificationManagerService notificationManagerService;
  private NotificationSubscriptionService notificationSubscriptionService;
  private BehaviourFilter policyBehaviourFilter;
  private ProfilesApi profilesApi;
  private UsersApi usersApi;
  private ApiToolBox apiToolBox;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

  @Before
  public void setUp() throws Exception {
    notificationsApi = new NotificationsApiImpl();

    permissionService = mock(PermissionService.class);
    personService = mock(PersonService.class);
    nodeService = mock(NodeService.class);
    notificationManagerService = mock(NotificationManagerService.class);
    notificationSubscriptionService = mock(
      NotificationSubscriptionService.class
    );
    policyBehaviourFilter = mock(BehaviourFilter.class);
    profilesApi = mock(ProfilesApi.class);
    usersApi = mock(UsersApi.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("permissionService", permissionService);
    setField("personService", personService);
    setField("nodeService", nodeService);
    setField("notificationManagerService", notificationManagerService);
    setField(
      "notificationSubscriptionService",
      notificationSubscriptionService
    );
    setField("policyBehaviourFilter", policyBehaviourFilter);
    setField("profilesApi", profilesApi);
    setField("usersApi", usersApi);
    setField("apiToolBox", apiToolBox);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NotificationsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(notificationsApi, value);
  }

  @Test
  public void testNodesIdNotificationsAuthorityPut_whenValueOn_thenSetsPermissionAllowed() {
    notificationsApi.nodesIdNotificationsAuthorityPut(TEST_ID, "user1", "on");

    verify(policyBehaviourFilter).disableBehaviour(
      TEST_NODE_REF,
      ContentModel.ASPECT_AUDITABLE
    );
    verify(permissionService).setPermission(
      TEST_NODE_REF,
      "user1",
      "NotificationStatus",
      true
    );
    verify(policyBehaviourFilter).enableBehaviour(
      TEST_NODE_REF,
      ContentModel.ASPECT_AUDITABLE
    );
  }

  @Test
  public void testNodesIdNotificationsAuthorityPut_whenValueOff_thenSetsPermissionDenied() {
    notificationsApi.nodesIdNotificationsAuthorityPut(TEST_ID, "user1", "off");

    verify(permissionService).setPermission(
      TEST_NODE_REF,
      "user1",
      "NotificationStatus",
      false
    );
  }

  @Test
  public void testNodesIdNotificationsAuthorityPut_whenValueOther_andNoInherit_thenDeletesPermission() {
    when(
      permissionService.getInheritParentPermissions(TEST_NODE_REF)
    ).thenReturn(false);

    notificationsApi.nodesIdNotificationsAuthorityPut(
      TEST_ID,
      "user1",
      "inherit"
    );

    verify(permissionService).deletePermission(
      TEST_NODE_REF,
      "user1",
      "NotificationStatus"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNodesIdNotificationsAuthorityPut_whenNullId_thenThrows() {
    notificationsApi.nodesIdNotificationsAuthorityPut(null, "user1", "on");
  }

  @Test
  public void testGetPasteNotificationsState_whenCalled_thenReturnsBothFlags() {
    when(
      notificationManagerService.isPasteNotificationEnabled(TEST_NODE_REF)
    ).thenReturn(true);
    when(
      notificationManagerService.isPasteAllNotificationEnabled(TEST_NODE_REF)
    ).thenReturn(false);

    PasteNotificationsState result =
      notificationsApi.getPasteNotificationsState(TEST_ID);

    assertTrue(result.isPasteEnabled());
    assertFalse(result.isPasteAllEnabled());
  }

  @Test
  public void testSetPasteNotificationsState_whenCalled_thenDelegatesToService() {
    notificationsApi.setPasteNotificationsState(TEST_ID, true, false);

    verify(notificationManagerService).setPasteNotificationEnabled(
      TEST_NODE_REF,
      true
    );
    verify(notificationManagerService).setPasteAllNotificationEnabled(
      TEST_NODE_REF,
      false
    );
  }

  @Test
  public void testNodesIdNotificationsAuthorityDelete_whenValidArgs_thenDeletesPermission() {
    notificationsApi.nodesIdNotificationsAuthorityDelete(TEST_ID, "user1");

    verify(policyBehaviourFilter).disableBehaviour(
      TEST_NODE_REF,
      ContentModel.ASPECT_AUDITABLE
    );
    verify(permissionService).deletePermission(
      TEST_NODE_REF,
      "user1",
      "NotificationStatus"
    );
    verify(policyBehaviourFilter).enableBehaviour(
      TEST_NODE_REF,
      ContentModel.ASPECT_AUDITABLE
    );
  }

  @Test
  public void testNodesIdNotificationsAuthorityDelete_whenNullAuthority_thenNoInteraction() {
    notificationsApi.nodesIdNotificationsAuthorityDelete(TEST_ID, null);

    verify(permissionService, never()).deletePermission(
      any(NodeRef.class),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testRemoveNotification_whenCalled_thenDelegatesToSubscriptionService() {
    notificationsApi.removeNotification(TEST_ID, "user1");

    verify(notificationSubscriptionService).removeNotification(
      TEST_NODE_REF,
      "user1"
    );
  }

  @Test
  public void testSetNotificationStatus_whenCalled_thenDelegatesToSubscriptionService() {
    notificationsApi.setNotificationStatus(
      TEST_ID,
      "user1",
      NotificationStatus.SUBSCRIBED
    );

    verify(notificationSubscriptionService).setNotificationStatus(
      TEST_NODE_REF,
      "user1",
      NotificationStatus.SUBSCRIBED
    );
  }

  @Test
  public void testIsUsersubscribedForNotification_whenUserExists_thenReturnsTrue() {
    NotifiableUser user = mock(NotifiableUser.class);
    when(user.getUserName()).thenReturn("user1");
    Set<NotifiableUser> users = new HashSet<>(Collections.singletonList(user));
    when(
      notificationSubscriptionService.getNotifiableUsers(TEST_NODE_REF)
    ).thenReturn(users);

    boolean result = notificationsApi.isUsersubscribedForNotification(
      TEST_ID,
      "user1"
    );

    assertTrue(result);
  }

  @Test
  public void testIsUsersubscribedForNotification_whenUserNotFound_thenReturnsFalse() {
    NotifiableUser user = mock(NotifiableUser.class);
    when(user.getUserName()).thenReturn("other");
    Set<NotifiableUser> users = new HashSet<>(Collections.singletonList(user));
    when(
      notificationSubscriptionService.getNotifiableUsers(TEST_NODE_REF)
    ).thenReturn(users);

    boolean result = notificationsApi.isUsersubscribedForNotification(
      TEST_ID,
      "user1"
    );

    assertFalse(result);
  }

  @Test
  public void testIsUsersubscribedForNotification_whenEmptySet_thenReturnsFalse() {
    when(
      notificationSubscriptionService.getNotifiableUsers(TEST_NODE_REF)
    ).thenReturn(Collections.emptySet());

    boolean result = notificationsApi.isUsersubscribedForNotification(
      TEST_ID,
      "user1"
    );

    assertFalse(result);
  }

  @Test
  public void testGetNotifiableUsers_whenAmountZero_thenReturnsAll() {
    NotifiableUser user1 = mock(NotifiableUser.class);
    NotifiableUser user2 = mock(NotifiableUser.class);
    Set<NotifiableUser> users = new LinkedHashSet<>(
      Arrays.asList(user1, user2)
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(TEST_NODE_REF)
    ).thenReturn(users);

    PagedNotificationSubscribedUsers result =
      notificationsApi.getNotifiableUsers(TEST_ID, 0, 0);

    assertEquals(2, result.getTotal());
    assertEquals(2, result.getData().size());
  }

  @Test
  public void testGetNotifiableUsers_whenPaginated_thenReturnsSubset() {
    NotifiableUser user1 = mock(NotifiableUser.class);
    NotifiableUser user2 = mock(NotifiableUser.class);
    NotifiableUser user3 = mock(NotifiableUser.class);
    Set<NotifiableUser> users = new LinkedHashSet<>(
      Arrays.asList(user1, user2, user3)
    );
    when(
      notificationSubscriptionService.getNotifiableUsers(TEST_NODE_REF)
    ).thenReturn(users);

    PagedNotificationSubscribedUsers result =
      notificationsApi.getNotifiableUsers(TEST_ID, 0, 2);

    assertEquals(3, result.getTotal());
    assertEquals(2, result.getData().size());
  }

  @Test
  public void testGetNotifications_whenAmountZero_thenReturnsAll() {
    AuthorityNotification notification = mock(AuthorityNotification.class);
    when(notification.getAuthorityType()).thenReturn(AuthorityType.USER);
    when(notification.getAuthority()).thenReturn("user1");
    when(notification.getNotificationStatus()).thenReturn(
      NotificationStatus.SUBSCRIBED
    );
    when(notification.getInherited()).thenReturn(false);

    Set<AuthorityNotification> notifications = new LinkedHashSet<>(
      Collections.singletonList(notification)
    );
    when(
      notificationSubscriptionService.getNotifications(TEST_NODE_REF)
    ).thenReturn(notifications);

    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    Map<org.alfresco.service.namespace.QName, java.io.Serializable> props =
      new HashMap<>();
    props.put(ContentModel.PROP_USERNAME, "user1");
    when(nodeService.getProperties(personRef)).thenReturn(props);

    PagedNotificationConfigurations result = notificationsApi.getNotifications(
      TEST_ID,
      0,
      0,
      "en"
    );

    assertEquals(1, result.getTotal());
    assertEquals(1, result.getData().size());
    assertNotNull(result.getData().get(0));
  }
}
