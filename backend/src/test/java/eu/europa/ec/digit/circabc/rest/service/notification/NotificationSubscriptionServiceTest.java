package eu.europa.ec.digit.circabc.rest.service.notification;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.ProfilesApi;
import io.swagger.model.NotificationStatus;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class NotificationSubscriptionServiceTest {

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
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NotificationSubscriptionServiceImpl.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(service, value);
  }

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

  @Test(expected = IllegalArgumentException.class)
  public void testGetNotifications_whenNullNodeRef_thenThrows() {
    service.getNotifications(null);
  }

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
}
