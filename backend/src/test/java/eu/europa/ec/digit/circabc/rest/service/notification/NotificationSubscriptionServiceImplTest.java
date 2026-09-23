package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.ProfilesApi;
import io.swagger.model.NotifiableUser;
import io.swagger.model.NotificationStatus;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.UserModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class NotificationSubscriptionServiceImplTest {

  private NotificationSubscriptionServiceImpl service;
  private PermissionService permissionService;
  private NodeService nodeService;
  private PersonService personService;
  private ApiToolBox apiToolBox;
  private ProfilesApi profilesApi;
  private BehaviourFilter policyBehaviourFilter;
  private UserService userService;
  private AuthorityService authorityService;

  private NodeRef nodeRef;
  private NodeRef igNodeRef;
  private NodeRef parentNodeRef;

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

    service = new NotificationSubscriptionServiceImpl();
    permissionService = mock(PermissionService.class);
    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);
    apiToolBox = mock(ApiToolBox.class);
    profilesApi = mock(ProfilesApi.class);
    policyBehaviourFilter = mock(BehaviourFilter.class);
    userService = mock(UserService.class);
    authorityService = mock(AuthorityService.class);

    setField("permissionService", permissionService);
    setField("nodeService", nodeService);
    setField("personService", personService);
    setField("apiToolBox", apiToolBox);
    setField("profilesApi", profilesApi);
    setField("policyBehaviourFilter", policyBehaviourFilter);
    setField("userService", userService);
    setField("authorityService", authorityService);

    nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "node-id");
    igNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");
    parentNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NotificationSubscriptionServiceImpl.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(service, value);
  }

  // --- getNotifications ---

  @Test
  public void testGetNotifications_whenNoPermissions_thenReturnsEmpty() {
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    Set<AuthorityNotification> result = service.getNotifications(nodeRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetNotifications_whenHasNotificationPermission_thenReturnsNotification() {
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);

    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);

    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    Set<AuthorityNotification> result = service.getNotifications(nodeRef);

    assertEquals(1, result.size());
    AuthorityNotification notif = result.iterator().next();
    assertEquals("user1", notif.getAuthority());
    assertEquals(NotificationStatus.SUBSCRIBED, notif.getNotificationStatus());
  }

  @Test
  public void testGetNotifications_whenDenied_thenUnsubscribed() {
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.DENIED);

    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);

    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    Set<AuthorityNotification> result = service.getNotifications(nodeRef);

    assertEquals(1, result.size());
    assertEquals(
      NotificationStatus.UNSUBSCRIBED,
      result.iterator().next().getNotificationStatus()
    );
  }

  @Test
  public void testGetNotifications_whenInheritParent_thenIncludesParentNotifications() {
    AccessPermission parentPerm = mock(AccessPermission.class);
    when(parentPerm.getPermission()).thenReturn("NotificationStatus");
    when(parentPerm.getAuthority()).thenReturn("user1");
    when(parentPerm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);

    Set<AccessPermission> parentPerms = new HashSet<>();
    parentPerms.add(parentPerm);

    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      true
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parentNodeRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(permissionService.getAllSetPermissions(parentNodeRef)).thenReturn(
      parentPerms
    );

    Set<AuthorityNotification> result = service.getNotifications(nodeRef);

    assertEquals(1, result.size());
    AuthorityNotification notif = result.iterator().next();
    assertEquals("user1", notif.getAuthority());
    assertTrue(notif.getInherited());
  }

  @Test
  public void testGetNotifications_whenNonNotificationPermission_thenFiltered() {
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getPermission()).thenReturn("Read");
    when(perm.getAuthority()).thenReturn("user1");

    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);

    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    Set<AuthorityNotification> result = service.getNotifications(nodeRef);

    assertTrue(result.isEmpty());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNotifications_whenNullNodeRef_thenThrows() {
    service.getNotifications(null);
  }

  // --- setNotificationStatus ---

  @Test
  public void testSetNotificationStatus_whenSubscribed_thenSetsPermissionAllowed() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );

    service.setNotificationStatus(
      nodeRef,
      "user1",
      NotificationStatus.SUBSCRIBED
    );

    verify(permissionService).setPermission(
      nodeRef,
      "user1",
      "NotificationStatus",
      true
    );
  }

  @Test
  public void testSetNotificationStatus_whenUnsubscribed_thenSetsPermissionDenied() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );

    service.setNotificationStatus(
      nodeRef,
      "user1",
      NotificationStatus.UNSUBSCRIBED
    );

    verify(permissionService).setPermission(
      nodeRef,
      "user1",
      "NotificationStatus",
      false
    );
  }

  @Test
  public void testSetNotificationStatus_whenInheritedNotIgRoot_thenDeletesPermission() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );

    // Existing permission to delete
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);

    service.setNotificationStatus(
      nodeRef,
      "user1",
      NotificationStatus.INHERITED
    );

    verify(permissionService).deletePermission(
      nodeRef,
      "user1",
      "NotificationStatus"
    );
  }

  @Test(expected = IllegalStateException.class)
  public void testSetNotificationStatus_whenInheritedOnIgRoot_thenThrows() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    service.setNotificationStatus(
      nodeRef,
      "user1",
      NotificationStatus.INHERITED
    );
  }

  @Test(expected = IllegalStateException.class)
  public void testSetNotificationStatus_whenInvalidAuthorityType_thenThrows() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);

    service.setNotificationStatus(
      nodeRef,
      "ROLE_EVERYONE",
      NotificationStatus.SUBSCRIBED
    );
  }

  @Test
  public void testSetNotificationStatus_whenGroupAuthority_thenAllowed() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );

    service.setNotificationStatus(
      nodeRef,
      "GROUP_myprofile",
      NotificationStatus.SUBSCRIBED
    );

    verify(permissionService).setPermission(
      nodeRef,
      "GROUP_myprofile",
      "NotificationStatus",
      true
    );
  }

  // --- getAuthorityNotificationStatus ---

  @Test
  public void testGetAuthorityNotificationStatus_whenPermissionAllowed_thenSubscribed() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);

    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);

    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);

    AuthorityNotification result = service.getAuthorityNotificationStatus(
      nodeRef,
      "user1"
    );

    assertEquals(NotificationStatus.SUBSCRIBED, result.getNotificationStatus());
    assertEquals("user1", result.getAuthority());
  }

  @Test
  public void testGetAuthorityNotificationStatus_whenNoPermission_thenDefaultUnsubscribedAtIg() {
    when(nodeService.exists(igNodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(igNodeRef)).thenReturn(igNodeRef);
    when(permissionService.getAllSetPermissions(igNodeRef)).thenReturn(
      Collections.emptySet()
    );

    AuthorityNotification result = service.getAuthorityNotificationStatus(
      igNodeRef,
      "user1"
    );

    assertEquals(
      NotificationStatus.UNSUBSCRIBED,
      result.getNotificationStatus()
    );
  }

  @Test
  public void testGetAuthorityNotificationStatus_whenNoPermissionBelowIg_thenInheritsFromParent() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(igNodeRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);

    // IG level has a subscription
    AccessPermission igPerm = mock(AccessPermission.class);
    when(igPerm.getAuthority()).thenReturn("user1");
    when(igPerm.getPermission()).thenReturn("NotificationStatus");
    when(igPerm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    Set<AccessPermission> igPerms = new HashSet<>();
    igPerms.add(igPerm);
    when(permissionService.getAllSetPermissions(igNodeRef)).thenReturn(igPerms);

    AuthorityNotification result = service.getAuthorityNotificationStatus(
      nodeRef,
      "user1"
    );

    assertEquals(NotificationStatus.SUBSCRIBED, result.getNotificationStatus());
  }

  @Test(expected = InvalidNodeRefException.class)
  public void testGetAuthorityNotificationStatus_whenNodeNotExists_thenThrows() {
    when(nodeService.exists(nodeRef)).thenReturn(false);

    service.getAuthorityNotificationStatus(nodeRef, "user1");
  }

  @Test(expected = InvalidNodeRefException.class)
  public void testGetAuthorityNotificationStatus_whenNotUnderIg_thenThrows() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(null);

    service.getAuthorityNotificationStatus(nodeRef, "user1");
  }

  // --- removeNotification ---

  @Test
  public void testRemoveNotification_whenPermissionExists_thenDeletes() {
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);

    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);

    service.removeNotification(nodeRef, "user1");

    verify(permissionService).deletePermission(
      nodeRef,
      "user1",
      "NotificationStatus"
    );
  }

  @Test
  public void testRemoveNotification_whenNoPermission_thenNoDelete() {
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );

    service.removeNotification(nodeRef, "user1");

    verify(permissionService, never()).deletePermission(
      any(NodeRef.class),
      anyString(),
      anyString()
    );
  }

  // --- getUserNotificationReport ---

  @Test
  public void testGetUserNotificationReport_whenNotUserAuthority_thenReturnsNull() {
    UserNotificationReport result = service.getUserNotificationReport(
      nodeRef,
      "GROUP_somegroup"
    );

    assertNull(result);
  }

  @Test
  public void testGetUserNotificationReport_whenPersonNotExists_thenReturnsNull() {
    when(personService.personExists("unknownuser")).thenReturn(false);

    UserNotificationReport result = service.getUserNotificationReport(
      nodeRef,
      "unknownuser"
    );

    assertNull(result);
  }

  @Test
  public void testGetUserNotificationReport_whenNoIg_thenReturnsNull() {
    when(personService.personExists("user1")).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(null);

    UserNotificationReport result = service.getUserNotificationReport(
      nodeRef,
      "user1"
    );

    assertNull(result);
  }

  @Test
  public void testGetUserNotificationReport_whenUserSubscribed_thenNotifiable() {
    when(personService.personExists("user1")).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(profilesApi.getInvitedUsers(igNodeRef)).thenReturn(Set.of("user1"));
    when(profilesApi.getPersonProfileGroupName(igNodeRef, "user1")).thenReturn(
      "GROUP_profile1"
    );

    // User has subscription on the node
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);

    // Person has global notification enabled
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    Map<QName, java.io.Serializable> props = new HashMap<>();
    props.put(UserModel.PROP_GLOBAL_NOTIFICATION, Boolean.TRUE);
    when(
      nodeService.getProperty(personRef, UserModel.PROP_GLOBAL_NOTIFICATION)
    ).thenReturn(Boolean.TRUE);

    UserNotificationReport result = service.getUserNotificationReport(
      nodeRef,
      "user1"
    );

    assertNotNull(result);
    assertTrue(result.isUserNotifiable());
    assertEquals(
      NotificationStatus.SUBSCRIBED,
      result.getUserNotificationStatus()
    );
  }

  @Test
  public void testGetUserNotificationReport_whenGlobalNotifDisabled_thenNotNotifiable() {
    when(personService.personExists("user1")).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);
    when(profilesApi.getInvitedUsers(igNodeRef)).thenReturn(Set.of("user1"));
    when(profilesApi.getPersonProfileGroupName(igNodeRef, "user1")).thenReturn(
      "GROUP_profile1"
    );

    // User has subscription
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);

    // Person has global notification disabled
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, UserModel.PROP_GLOBAL_NOTIFICATION)
    ).thenReturn(Boolean.FALSE);

    UserNotificationReport result = service.getUserNotificationReport(
      nodeRef,
      "user1"
    );

    assertNotNull(result);
    assertFalse(result.isUserNotifiable());
  }

  // --- getNotifiableUsers ---

  @Test
  public void testGetNotifiableUsers_whenUserSubscribedAndGlobalEnabled_thenReturned() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);

    // Node-level permission: user subscribed
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    // Node equals IG (no parent traversal needed)
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(igNodeRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(permissionService.getAllSetPermissions(igNodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(permissionService.getInheritParentPermissions(igNodeRef)).thenReturn(
      false
    );

    // Person setup
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    Map<QName, java.io.Serializable> props = new HashMap<>();
    props.put(UserModel.PROP_GLOBAL_NOTIFICATION, Boolean.TRUE);
    when(nodeService.getProperties(personRef)).thenReturn(props);

    // Not multilingual
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ).thenReturn(false);

    // Has read permission
    when(
      permissionService.hasPermission(
        nodeRef,
        PermissionService.READ_PROPERTIES
      )
    ).thenReturn(AccessStatus.ALLOWED);

    Set<NotifiableUser> result = service.getNotifiableUsers(nodeRef);

    assertEquals(1, result.size());
  }

  @Test
  public void testGetNotifiableUsers_whenUserUnsubscribed_thenNotReturned() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);

    // Node-level permission: user unsubscribed (DENIED)
    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.DENIED);
    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(igNodeRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(permissionService.getAllSetPermissions(igNodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(permissionService.getInheritParentPermissions(igNodeRef)).thenReturn(
      false
    );

    Set<NotifiableUser> result = service.getNotifiableUsers(nodeRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetNotifiableUsers_whenGlobalNotifDisabled_thenNotReturned() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igNodeRef);

    AccessPermission perm = mock(AccessPermission.class);
    when(perm.getPermission()).thenReturn("NotificationStatus");
    when(perm.getAuthority()).thenReturn("user1");
    when(perm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    Set<AccessPermission> perms = new HashSet<>();
    perms.add(perm);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(perms);
    when(permissionService.getInheritParentPermissions(nodeRef)).thenReturn(
      false
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(igNodeRef);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);
    when(permissionService.getAllSetPermissions(igNodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(permissionService.getInheritParentPermissions(igNodeRef)).thenReturn(
      false
    );

    // Person with global notification disabled
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    Map<QName, java.io.Serializable> props = new HashMap<>();
    props.put(UserModel.PROP_GLOBAL_NOTIFICATION, Boolean.FALSE);
    when(nodeService.getProperties(personRef)).thenReturn(props);
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ).thenReturn(false);
    when(
      permissionService.hasPermission(
        nodeRef,
        PermissionService.READ_PROPERTIES
      )
    ).thenReturn(AccessStatus.ALLOWED);

    Set<NotifiableUser> result = service.getNotifiableUsers(nodeRef);

    assertTrue(result.isEmpty());
  }

  @Test(expected = InvalidNodeRefException.class)
  public void testGetNotifiableUsers_whenNodeNotExists_thenThrows() {
    when(nodeService.exists(nodeRef)).thenReturn(false);

    service.getNotifiableUsers(nodeRef);
  }

  @Test(expected = InvalidNodeRefException.class)
  public void testGetNotifiableUsers_whenNotUnderIg_thenThrows() {
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(null);

    service.getNotifiableUsers(nodeRef);
  }
}
