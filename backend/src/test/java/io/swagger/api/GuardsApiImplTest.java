package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.EventPermissions;
import io.swagger.model.permissions.InformationPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.*;
import org.junit.Before;
import org.junit.Test;

public class GuardsApiImplTest {

  private GuardsApiImpl guardsApi;
  private PermissionService permissionService;
  private AuthenticationService authenticationService;
  private AuthorityService authorityService;
  private NodeService nodeService;

  private static final String TEST_ID = "test-node-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );

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

    guardsApi = new GuardsApiImpl();
    permissionService = mock(PermissionService.class);
    authenticationService = mock(AuthenticationService.class);
    authorityService = mock(AuthorityService.class);
    nodeService = mock(NodeService.class);

    setField("permissionService", permissionService);
    setField("authenticationService", authenticationService);
    setField("authorityService", authorityService);
    setField("nodeService", nodeService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GuardsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(guardsApi, value);
  }

  // --- guardsGroupIdGet ---

  @Test
  public void testGuardsGroupIdGet_whenPublic_thenGranted() {
    Set<AccessPermission> permissions = new HashSet<>();
    AccessPermission guestPerm = mock(AccessPermission.class);
    when(guestPerm.getAuthority()).thenReturn("guest");
    when(guestPerm.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    when(guestPerm.getPermission()).thenReturn("Visibility");
    permissions.add(guestPerm);

    when(permissionService.getAllSetPermissions(TEST_NODE_REF)).thenReturn(
      permissions
    );

    GuardAuthorization result = guardsApi.guardsGroupIdGet(TEST_ID);

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsGroupIdGet_whenNotPublicNotRegistered_andUserHasRead_thenGranted() {
    Set<AccessPermission> permissions = new HashSet<>();
    when(permissionService.getAllSetPermissions(TEST_NODE_REF)).thenReturn(
      permissions
    );
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(permissionService.hasPermission(TEST_NODE_REF, "Read")).thenReturn(
      AccessStatus.ALLOWED
    );

    GuardAuthorization result = guardsApi.guardsGroupIdGet(TEST_ID);

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsGroupIdGet_whenGuestUser_andNotPublic_thenNotGranted() {
    Set<AccessPermission> permissions = new HashSet<>();
    when(permissionService.getAllSetPermissions(TEST_NODE_REF)).thenReturn(
      permissions
    );
    when(authenticationService.getCurrentUserName()).thenReturn("guest");

    GuardAuthorization result = guardsApi.guardsGroupIdGet(TEST_ID);

    assertFalse(result.getGranted());
  }

  // --- guardsAccessIdGet ---

  @Test(expected = NonExistingNodeException.class)
  public void testGuardsAccessIdGet_whenNodeNotExists_thenThrows()
    throws NonExistingNodeException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    guardsApi.guardsAccessIdGet(TEST_ID);
  }

  @Test
  public void testGuardsAccessIdGet_whenLibraryAspectAndAllowed_thenGranted()
    throws NonExistingNodeException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(
      permissionService.hasPermission(
        TEST_NODE_REF,
        LibraryPermissions.LIBACCESS.toString()
      )
    ).thenReturn(AccessStatus.ALLOWED);

    GuardAuthorization result = guardsApi.guardsAccessIdGet(TEST_ID);

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsAccessIdGet_whenNoMatchingAspect_thenNotGranted()
    throws NonExistingNodeException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    when(nodeService.hasAspect(eq(TEST_NODE_REF), any())).thenReturn(false);

    GuardAuthorization result = guardsApi.guardsAccessIdGet(TEST_ID);

    assertFalse(result.getGranted());
  }

  // --- guardsEditionIdGet ---

  @Test(expected = NonExistingNodeException.class)
  public void testGuardsEditionIdGet_whenNodeNotExists_thenThrows()
    throws NonExistingNodeException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(false);

    guardsApi.guardsEditionIdGet(TEST_ID);
  }

  @Test
  public void testGuardsEditionIdGet_whenUserHasWritePermission_thenGranted()
    throws NonExistingNodeException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    Set<AccessPermission> permissions = new HashSet<>();
    when(permissionService.getAllSetPermissions(TEST_NODE_REF)).thenReturn(
      permissions
    );
    when(permissionService.hasPermission(TEST_NODE_REF, "Write")).thenReturn(
      AccessStatus.ALLOWED
    );

    GuardAuthorization result = guardsApi.guardsEditionIdGet(TEST_ID);

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsEditionIdGet_whenUserHasNoPermission_thenNotGranted()
    throws NonExistingNodeException {
    when(nodeService.exists(TEST_NODE_REF)).thenReturn(true);
    Set<AccessPermission> permissions = new HashSet<>();
    when(permissionService.getAllSetPermissions(TEST_NODE_REF)).thenReturn(
      permissions
    );
    when(permissionService.hasPermission(TEST_NODE_REF, "Write")).thenReturn(
      AccessStatus.DENIED
    );

    GuardAuthorization result = guardsApi.guardsEditionIdGet(TEST_ID);

    assertFalse(result.getGranted());
  }

  // --- guardsGroupIdServiceNameGet ---

  @Test
  public void testGuardsGroupIdServiceNameGet_whenMembersAndHasPermission_thenGranted() {
    for (DirectoryPermissions dp : DirectoryPermissions.values()) {
      if (dp.equals(DirectoryPermissions.DIRNOACCESS)) continue;
      when(
        permissionService.hasPermission(TEST_NODE_REF, dp.toString())
      ).thenReturn(
        dp.equals(DirectoryPermissions.DIRACCESS)
          ? AccessStatus.ALLOWED
          : AccessStatus.DENIED
      );
    }

    GuardAuthorization result = guardsApi.guardsGroupIdServiceNameGet(
      TEST_ID,
      "members"
    );

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsGroupIdServiceNameGet_whenApplicantsAndManageMembers_thenGranted() {
    when(
      permissionService.hasPermission(
        TEST_NODE_REF,
        DirectoryPermissions.DIRMANAGEMEMBERS.toString()
      )
    ).thenReturn(AccessStatus.ALLOWED);
    // Stub the other permission check to avoid NPE
    when(
      permissionService.hasPermission(
        TEST_NODE_REF,
        DirectoryPermissions.DIRADMIN.toString()
      )
    ).thenReturn(AccessStatus.DENIED);

    GuardAuthorization result = guardsApi.guardsGroupIdServiceNameGet(
      TEST_ID,
      "applicants"
    );

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsGroupIdServiceNameGet_whenInformationAndConsumer_thenGranted() {
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );
    when(
      nodeService.getChildByName(
        TEST_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(infRef);
    when(permissionService.hasPermission(infRef, "Consumer")).thenReturn(
      AccessStatus.ALLOWED
    );

    GuardAuthorization result = guardsApi.guardsGroupIdServiceNameGet(
      TEST_ID,
      "information"
    );

    assertTrue(result.getGranted());
  }

  // --- guardsGroupIdMembersAdminGet ---

  @Test
  public void testGuardsGroupIdMembersAdminGet_whenIgRootAndDirAdmin_thenGranted() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      permissionService.hasPermission(
        TEST_NODE_REF,
        DirectoryPermissions.DIRADMIN.toString()
      )
    ).thenReturn(AccessStatus.ALLOWED);

    GuardAuthorization result = guardsApi.guardsGroupIdMembersAdminGet(TEST_ID);

    assertTrue(result.getGranted());
  }

  @Test
  public void testGuardsGroupIdMembersAdminGet_whenNotIgRoot_thenNotGranted() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    GuardAuthorization result = guardsApi.guardsGroupIdMembersAdminGet(TEST_ID);

    assertFalse(result.getGranted());
  }
}
