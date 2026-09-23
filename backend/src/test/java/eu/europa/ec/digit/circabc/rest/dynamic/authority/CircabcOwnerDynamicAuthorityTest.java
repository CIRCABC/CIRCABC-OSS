package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.junit.Before;
import org.junit.Test;

public class CircabcOwnerDynamicAuthorityTest {

  private CircabcOwnerDynamicAuthority authority;
  private OwnableService ownableService;
  private CircabcDynamicAuthorityService circabcService;

  @Before
  public void setUp() throws Exception {
    authority = new CircabcOwnerDynamicAuthority();
    ownableService = mock(OwnableService.class);
    circabcService = mock(CircabcDynamicAuthorityService.class);

    Field ownableField = CircabcOwnerDynamicAuthority.class.getDeclaredField(
      "ownableService"
    );
    ownableField.setAccessible(true);
    ownableField.set(authority, ownableService);

    Field circabcField = CircabcOwnerDynamicAuthority.class.getDeclaredField(
      "circabcService"
    );
    circabcField.setAccessible(true);
    circabcField.set(authority, circabcService);

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
  }

  @Test
  public void testGetAuthority_returnsOwnerAuthority() {
    assertEquals(PermissionService.OWNER_AUTHORITY, authority.getAuthority());
  }

  @Test
  public void testRequiredFor_returnsNull() {
    assertNull(authority.requiredFor());
  }

  @Test
  public void testHasAuthority_whenNotCircabcNode_andUserIsOwner_returnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(false);
    when(ownableService.getOwner(nodeRef)).thenReturn("user1");

    assertTrue(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenNotCircabcNode_andUserIsNotOwner_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(false);
    when(ownableService.getOwner(nodeRef)).thenReturn("otheruser");

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenCircabcNode_andNoGroup_andUserIsOwner_returnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findGroup(nodeRef)).thenReturn(null);
    when(ownableService.getOwner(nodeRef)).thenReturn("user1");

    assertTrue(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenCircabcNode_andNoGroup_andUserIsNotOwner_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findGroup(nodeRef)).thenReturn(null);
    when(ownableService.getOwner(nodeRef)).thenReturn("otheruser");

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenUserIsCategoryAdmin_returnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findGroup(nodeRef)).thenReturn(groupRef);
    when(circabcService.isCategoryAdmin(groupRef, "user1")).thenReturn(true);

    assertTrue(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenGroupMember_andIsOwner_returnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findGroup(nodeRef)).thenReturn(groupRef);
    when(circabcService.isCategoryAdmin(groupRef, "user1")).thenReturn(false);
    when(circabcService.isGroupMember(groupRef, "user1")).thenReturn(true);
    when(ownableService.getOwner(nodeRef)).thenReturn("user1");

    assertTrue(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenGroupMember_andNotOwner_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findGroup(nodeRef)).thenReturn(groupRef);
    when(circabcService.isCategoryAdmin(groupRef, "user1")).thenReturn(false);
    when(circabcService.isGroupMember(groupRef, "user1")).thenReturn(true);
    when(ownableService.getOwner(nodeRef)).thenReturn("otheruser");

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenNotGroupMember_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findGroup(nodeRef)).thenReturn(groupRef);
    when(circabcService.isCategoryAdmin(groupRef, "user1")).thenReturn(false);
    when(circabcService.isGroupMember(groupRef, "user1")).thenReturn(false);

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInit_whenOwnableServiceIsNull_throwsException()
    throws Exception {
    CircabcOwnerDynamicAuthority auth = new CircabcOwnerDynamicAuthority();
    Field circabcField = CircabcOwnerDynamicAuthority.class.getDeclaredField(
      "circabcService"
    );
    circabcField.setAccessible(true);
    circabcField.set(auth, mock(CircabcDynamicAuthorityService.class));
    auth.init();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInit_whenCircabcServiceIsNull_throwsException()
    throws Exception {
    CircabcOwnerDynamicAuthority auth = new CircabcOwnerDynamicAuthority();
    Field ownableField = CircabcOwnerDynamicAuthority.class.getDeclaredField(
      "ownableService"
    );
    ownableField.setAccessible(true);
    ownableField.set(auth, mock(OwnableService.class));
    auth.init();
  }
}
