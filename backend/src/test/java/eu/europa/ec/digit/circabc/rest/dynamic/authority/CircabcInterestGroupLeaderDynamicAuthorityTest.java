package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class CircabcInterestGroupLeaderDynamicAuthorityTest {

  private CircabcInterestGroupLeaderDynamicAuthority authority;
  private CircabcDynamicAuthorityService circabcService;

  @Before
  public void setUp() throws Exception {
    authority = new CircabcInterestGroupLeaderDynamicAuthority();
    circabcService = mock(CircabcDynamicAuthorityService.class);
    Field field =
      CircabcInterestGroupLeaderDynamicAuthority.class.getDeclaredField(
        "circabcService"
      );
    field.setAccessible(true);
    field.set(authority, circabcService);
  }

  @Test
  public void testGetAuthority_returnsCorrectRole() {
    assertEquals("ROLE_CIRCABC_LEADER", authority.getAuthority());
  }

  @Test
  public void testRequiredFor_returnsNull() {
    assertNull(authority.requiredFor());
  }

  @Test
  public void testHasAuthority_whenNotCircabcNode_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(false);

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenUnknownServiceType_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findServiceType(nodeRef)).thenReturn(
      CircabcServiceType.UNKNOWN
    );

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenNoGroup_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findServiceType(nodeRef)).thenReturn(
      CircabcServiceType.LIBRARY
    );
    when(circabcService.findGroup(nodeRef)).thenReturn(null);

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenUserIsAdmin_returnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findServiceType(nodeRef)).thenReturn(
      CircabcServiceType.LIBRARY
    );
    when(circabcService.findGroup(nodeRef)).thenReturn(groupRef);
    when(
      circabcService.isAdmin(groupRef, "user1", CircabcServiceType.LIBRARY)
    ).thenReturn(true);

    assertTrue(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test
  public void testHasAuthority_whenUserIsNotAdmin_returnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcService.isCircabcNode(nodeRef)).thenReturn(true);
    when(circabcService.findServiceType(nodeRef)).thenReturn(
      CircabcServiceType.LIBRARY
    );
    when(circabcService.findGroup(nodeRef)).thenReturn(groupRef);
    when(
      circabcService.isAdmin(groupRef, "user1", CircabcServiceType.LIBRARY)
    ).thenReturn(false);

    assertFalse(authority.hasAuthority(nodeRef, "user1"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInit_whenServiceIsNull_throwsException() throws Exception {
    CircabcInterestGroupLeaderDynamicAuthority auth =
      new CircabcInterestGroupLeaderDynamicAuthority();
    auth.init();
  }
}
