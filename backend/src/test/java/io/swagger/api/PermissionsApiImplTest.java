package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.PermissionDefinition;
import io.swagger.model.PermissionDefinitionPermissionsProfiles;
import io.swagger.model.PermissionDefinitionPermissionsUsers;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;

public class PermissionsApiImplTest {

  private PermissionsApiImpl permissionsApi;
  private PermissionService permissionService;
  private ProfilesApi profilesApi;
  private UsersApi usersApi;
  private ApiToolBox apiToolBox;

  private static final String NODE_ID = "test-node-id";
  private static final String IG_ID = "test-ig-id";
  private static final NodeRef NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    NODE_ID
  );
  private static final NodeRef IG_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    IG_ID
  );

  @Before
  public void setUp() throws Exception {
    permissionsApi = new PermissionsApiImpl();
    permissionService = mock(PermissionService.class);
    profilesApi = mock(ProfilesApi.class);
    usersApi = mock(UsersApi.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("permissionService", permissionService);
    setField("profilesApi", profilesApi);
    setField("usersApi", usersApi);
    setField("apiToolBox", apiToolBox);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = PermissionsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(permissionsApi, value);
  }

  @Test
  public void testGetNodeIdPermissionsGet_whenUserPermission_thenReturnsUserInResult() {
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);

    Profile profile = new Profile();
    profile.setGroupName("GROUP_TestProfile");
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.singletonList(profile)
    );

    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      true
    );

    AccessPermission userPerm = mock(AccessPermission.class);
    when(userPerm.getAuthorityType()).thenReturn(AuthorityType.USER);
    when(userPerm.getPermission()).thenReturn("Read");
    when(userPerm.getAuthority()).thenReturn("john");
    when(userPerm.isInherited()).thenReturn(false);

    Set<AccessPermission> perms = new LinkedHashSet<>();
    perms.add(userPerm);
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(perms);

    User user = new User();
    user.setUserId("john");
    when(usersApi.usersUserIdGet("john")).thenReturn(user);

    PermissionDefinition result = permissionsApi.getNodeIdPermissionsGet(
      NODE_ID
    );

    assertTrue(result.getInherited());
    assertEquals(1, result.getPermissions().getUsers().size());
    assertEquals(
      "john",
      result.getPermissions().getUsers().get(0).getUser().getUserId()
    );
    assertEquals(
      "Read",
      result.getPermissions().getUsers().get(0).getPermission()
    );
    assertTrue(result.getPermissions().getProfiles().isEmpty());
  }

  @Test
  public void testGetNodeIdPermissionsGet_whenGroupPermission_thenReturnsProfileInResult() {
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);

    Profile profile = new Profile();
    profile.setGroupName("GROUP_TestProfile");
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.singletonList(profile)
    );

    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      false
    );

    AccessPermission groupPerm = mock(AccessPermission.class);
    when(groupPerm.getAuthorityType()).thenReturn(AuthorityType.GROUP);
    when(groupPerm.getPermission()).thenReturn("Contributor");
    when(groupPerm.getAuthority()).thenReturn("GROUP_TestProfile");
    when(groupPerm.isInherited()).thenReturn(false);

    Set<AccessPermission> perms = new LinkedHashSet<>();
    perms.add(groupPerm);
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(perms);

    PermissionDefinition result = permissionsApi.getNodeIdPermissionsGet(
      NODE_ID
    );

    assertFalse(result.getInherited());
    assertEquals(1, result.getPermissions().getProfiles().size());
    assertEquals(
      "GROUP_TestProfile",
      result.getPermissions().getProfiles().get(0).getProfile().getGroupName()
    );
    assertEquals(
      "Contributor",
      result.getPermissions().getProfiles().get(0).getPermission()
    );
    assertTrue(result.getPermissions().getUsers().isEmpty());
  }

  @Test
  public void testGetNodeIdPermissionsGet_whenNotificationStatus_thenFiltered() {
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.emptyList()
    );
    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      true
    );

    AccessPermission notifPerm = mock(AccessPermission.class);
    when(notifPerm.getAuthorityType()).thenReturn(AuthorityType.USER);
    when(notifPerm.getPermission()).thenReturn("NotificationStatus");
    when(notifPerm.getAuthority()).thenReturn("john");

    Set<AccessPermission> perms = new LinkedHashSet<>();
    perms.add(notifPerm);
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(perms);

    PermissionDefinition result = permissionsApi.getNodeIdPermissionsGet(
      NODE_ID
    );

    assertTrue(result.getPermissions().getUsers().isEmpty());
    assertTrue(result.getPermissions().getProfiles().isEmpty());
  }

  @Test
  public void testGetNodeIdPermissionsGet_whenGuestAuthority_thenTreatedAsProfile() {
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);

    Profile guestProfile = new Profile();
    guestProfile.setGroupName("guest");
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.singletonList(guestProfile)
    );

    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      true
    );

    AccessPermission guestPerm = mock(AccessPermission.class);
    when(guestPerm.getAuthorityType()).thenReturn(AuthorityType.USER);
    when(guestPerm.getPermission()).thenReturn("Read");
    when(guestPerm.getAuthority()).thenReturn("guest");
    when(guestPerm.isInherited()).thenReturn(false);

    Set<AccessPermission> perms = new LinkedHashSet<>();
    perms.add(guestPerm);
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(perms);

    PermissionDefinition result = permissionsApi.getNodeIdPermissionsGet(
      NODE_ID
    );

    // guest user is excluded from users list
    assertTrue(result.getPermissions().getUsers().isEmpty());
    // guest is added to profiles list
    assertEquals(1, result.getPermissions().getProfiles().size());
    assertEquals(
      "guest",
      result.getPermissions().getProfiles().get(0).getProfile().getGroupName()
    );
  }

  @Test
  public void testNodeIdPermissionsDelete_whenValidInput_thenDeletesCalled() {
    permissionsApi.nodeIdPermissionsDelete(NODE_ID, "john", "Read");

    verify(permissionService).deletePermission(NODE_REF, "john", "Read");
  }

  @Test
  public void testNodeIdPermissionsDelete_whenEmptyAuthority_thenNoDelete() {
    permissionsApi.nodeIdPermissionsDelete(NODE_ID, "", "Read");

    verify(permissionService, never()).deletePermission(
      any(NodeRef.class),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testNodeIdPermissionsDelete_whenEmptyPermission_thenNoDelete() {
    permissionsApi.nodeIdPermissionsDelete(NODE_ID, "john", "");

    verify(permissionService, never()).deletePermission(
      any(NodeRef.class),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testNodeIdPermissionsClear_whenAuthority_thenDeletesNonInheritedNonNotification() {
    AccessPermission perm1 = mock(AccessPermission.class);
    when(perm1.getAuthority()).thenReturn("john");
    when(perm1.isInherited()).thenReturn(false);
    when(perm1.getPermission()).thenReturn("Read");

    AccessPermission perm2 = mock(AccessPermission.class);
    when(perm2.getAuthority()).thenReturn("john");
    when(perm2.isInherited()).thenReturn(true);
    when(perm2.getPermission()).thenReturn("Write");

    AccessPermission perm3 = mock(AccessPermission.class);
    when(perm3.getAuthority()).thenReturn("john");
    when(perm3.isInherited()).thenReturn(false);
    when(perm3.getPermission()).thenReturn("NotificationStatus");

    AccessPermission perm4 = mock(AccessPermission.class);
    when(perm4.getAuthority()).thenReturn("other");
    when(perm4.isInherited()).thenReturn(false);
    when(perm4.getPermission()).thenReturn("Read");

    Set<AccessPermission> perms = new LinkedHashSet<>();
    perms.add(perm1);
    perms.add(perm2);
    perms.add(perm3);
    perms.add(perm4);
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(perms);

    permissionsApi.nodeIdPermissionsClear(NODE_ID, "john");

    // Only perm1 should be deleted (non-inherited, matching authority, not NotificationStatus)
    verify(permissionService).deletePermission(NODE_REF, "john", "Read");
    verify(permissionService, times(1)).deletePermission(
      any(NodeRef.class),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testNodeIdPermissionsClear_whenEmptyAuthority_thenNoDelete() {
    permissionsApi.nodeIdPermissionsClear(NODE_ID, "");

    verify(permissionService, never()).getAllSetPermissions(any(NodeRef.class));
    verify(permissionService, never()).deletePermission(
      any(NodeRef.class),
      anyString(),
      anyString()
    );
  }

  @Test
  public void testNodeIdPermissionsPut_whenInheritanceEnabled_thenSetsInheritTrue() {
    // Setup: old perms have inherited=false, body has inherited=true
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.emptyList()
    );
    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      false
    );
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(
      Collections.emptySet()
    );

    PermissionDefinition body = new PermissionDefinition();
    body.setInherited(true);

    permissionsApi.nodeIdPermissionsPut(NODE_ID, body);

    verify(permissionService).setInheritParentPermissions(NODE_REF, true);
  }

  @Test
  public void testNodeIdPermissionsPut_whenInheritanceDisabled_thenSetsInheritFalse() {
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.emptyList()
    );
    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      true
    );
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(
      Collections.emptySet()
    );

    PermissionDefinition body = new PermissionDefinition();
    body.setInherited(false);

    permissionsApi.nodeIdPermissionsPut(NODE_ID, body);

    verify(permissionService).setInheritParentPermissions(NODE_REF, false);
  }

  @Test
  public void testNodeIdPermissionsPut_whenSameInheritance_thenSetsPermissions() {
    when(apiToolBox.getCurrentInterestGroup(NODE_REF)).thenReturn(IG_REF);
    when(profilesApi.groupsIdProfilesGet(IG_ID, null, false)).thenReturn(
      Collections.emptyList()
    );
    when(permissionService.getInheritParentPermissions(NODE_REF)).thenReturn(
      true
    );
    when(permissionService.getAllSetPermissions(NODE_REF)).thenReturn(
      Collections.emptySet()
    );

    PermissionDefinition body = new PermissionDefinition();
    body.setInherited(true);

    Profile profile = new Profile();
    profile.setGroupName("GROUP_TestProfile");
    PermissionDefinitionPermissionsProfiles pdpp =
      new PermissionDefinitionPermissionsProfiles();
    pdpp.setProfile(profile);
    pdpp.setPermission("Contributor");
    body.getPermissions().getProfiles().add(pdpp);

    User user = new User();
    user.setUserId("john");
    PermissionDefinitionPermissionsUsers pdpu =
      new PermissionDefinitionPermissionsUsers();
    pdpu.setUser(user);
    pdpu.setPermission("Read");
    body.getPermissions().getUsers().add(pdpu);

    permissionsApi.nodeIdPermissionsPut(NODE_ID, body);

    verify(permissionService).setPermission(
      NODE_REF,
      "GROUP_TestProfile",
      "Contributor",
      true
    );
    verify(permissionService).setPermission(NODE_REF, "john", "Read", true);
    verify(permissionService, never()).setInheritParentPermissions(
      any(NodeRef.class),
      anyBoolean()
    );
  }
}
