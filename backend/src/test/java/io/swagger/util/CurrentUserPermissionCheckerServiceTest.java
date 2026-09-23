package io.swagger.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.permissions.DirectoryPermissions;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.model.permissions.NewsGroupPermissions;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class CurrentUserPermissionCheckerServiceTest {

  private CurrentUserPermissionCheckerService service;
  private PermissionService permissionService;
  private NodeService nodeService;
  private AuthorityService authorityService;
  private CircabcService circabcService;
  private CircabcConfig circabcConfig;
  private LdapUserService ldapUserService;
  private PersonService personService;

  private static final String TEST_USER = "testuser";
  private static final String TEST_NODE_ID = "test-node-id";

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
    AuthenticationUtil.setFullyAuthenticatedUser(TEST_USER);
    AuthenticationUtil.setRunAsUser(TEST_USER);

    service = new CurrentUserPermissionCheckerService();

    permissionService = mock(PermissionService.class);
    nodeService = mock(NodeService.class);
    authorityService = mock(AuthorityService.class);
    circabcService = mock(CircabcService.class);
    circabcConfig = mock(CircabcConfig.class);
    ldapUserService = mock(LdapUserService.class);
    personService = mock(PersonService.class);

    setField("permissionService", permissionService);
    setField("nodeService", nodeService);
    setField("authorityService", authorityService);
    setField("circabcService", circabcService);
    setField("circabcConfig", circabcConfig);
    setField("ldapUserService", ldapUserService);
    setField("personService", personService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CurrentUserPermissionCheckerService.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(service, value);
  }

  // --- isCircabcAdmin ---

  @Test
  public void testIsCircabcAdmin_whenUserIsAdmin_thenReturnsTrue() {
    when(circabcService.isCircabcAdmin(TEST_USER)).thenReturn(true);
    assertTrue(service.isCircabcAdmin());
  }

  @Test
  public void testIsCircabcAdmin_whenUserIsNotAdmin_thenReturnsFalse() {
    when(circabcService.isCircabcAdmin(TEST_USER)).thenReturn(false);
    assertFalse(service.isCircabcAdmin());
  }

  // --- throwIfNotCircabcAdmin ---

  @Test
  public void testThrowIfNotCircabcAdmin_whenAdmin_thenReturnsTrue() {
    when(circabcService.isCircabcAdmin(TEST_USER)).thenReturn(true);
    assertTrue(service.throwIfNotCircabcAdmin());
  }

  @Test(expected = AccessDeniedException.class)
  public void testThrowIfNotCircabcAdmin_whenNotAdmin_thenThrows() {
    when(circabcService.isCircabcAdmin(TEST_USER)).thenReturn(false);
    service.throwIfNotCircabcAdmin();
  }

  // --- isCategoryAdmin ---

  @Test
  public void testIsCategoryAdmin_whenAdmin_thenReturnsTrue() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    when(circabcService.isCategoryAdmin(catRef, TEST_USER)).thenReturn(true);
    assertTrue(service.isCategoryAdmin("cat-id"));
  }

  @Test
  public void testIsCategoryAdmin_whenNotAdmin_thenReturnsFalse() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    when(circabcService.isCategoryAdmin(catRef, TEST_USER)).thenReturn(false);
    assertFalse(service.isCategoryAdmin("cat-id"));
  }

  // --- hasAlfrescoReadPermission ---

  @Test
  public void testHasAlfrescoReadPermission_whenAllowed_thenReturnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    assertTrue(service.hasAlfrescoReadPermission(TEST_NODE_ID));
  }

  @Test
  public void testHasAlfrescoReadPermission_whenDenied_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.DENIED);
    assertFalse(service.hasAlfrescoReadPermission(TEST_NODE_ID));
  }

  // --- hasAnyOfAlfrescoPermission ---

  @Test
  public void testHasAnyOfAlfrescoPermission_whenFirstMatches_thenReturnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    assertTrue(
      service.hasAnyOfAlfrescoPermission(
        TEST_NODE_ID,
        PermissionService.READ,
        PermissionService.WRITE
      )
    );
  }

  @Test
  public void testHasAnyOfAlfrescoPermission_whenNoneMatch_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      permissionService.hasPermission(nodeRef, PermissionService.READ)
    ).thenReturn(AccessStatus.DENIED);
    when(
      permissionService.hasPermission(nodeRef, PermissionService.WRITE)
    ).thenReturn(AccessStatus.DENIED);
    assertFalse(
      service.hasAnyOfAlfrescoPermission(
        TEST_NODE_ID,
        PermissionService.READ,
        PermissionService.WRITE
      )
    );
  }

  // --- isGuest ---

  @Test
  public void testIsGuest_whenGuestAuthority_thenReturnsTrue() {
    when(authorityService.isGuestAuthority(TEST_USER)).thenReturn(true);
    assertTrue(service.isGuest());
  }

  @Test
  public void testIsGuest_whenNotGuest_thenReturnsFalse() {
    when(authorityService.isGuestAuthority(TEST_USER)).thenReturn(false);
    assertFalse(service.isGuest());
  }

  // --- isCurrentUserEqualTo ---

  @Test
  public void testIsCurrentUserEqualTo_whenSameUser_thenReturnsTrue() {
    assertTrue(service.isCurrentUserEqualTo(TEST_USER));
  }

  @Test
  public void testIsCurrentUserEqualTo_whenDifferentUser_thenReturnsFalse() {
    assertFalse(service.isCurrentUserEqualTo("otheruser"));
  }

  // --- canAccessInterestGroup ---

  @Test
  public void testCanAccessInterestGroup_whenGuestAndPublic_thenReturnsTrue() {
    AuthenticationUtil.setFullyAuthenticatedUser("guest");
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    InterestGroupResult igResult = mock(InterestGroupResult.class);
    when(circabcService.getInterestGroup(igRef)).thenReturn(igResult);
    when(igResult.getIsPublic()).thenReturn(true);
    assertTrue(service.canAccessInterestGroup("ig-id"));
  }

  @Test
  public void testCanAccessInterestGroup_whenRegisteredIG_thenReturnsTrue() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    InterestGroupResult igResult = mock(InterestGroupResult.class);
    when(circabcService.getInterestGroup(igRef)).thenReturn(igResult);
    when(igResult.getIsRegistered()).thenReturn(true);
    assertTrue(service.canAccessInterestGroup("ig-id"));
  }

  @Test
  public void testCanAccessInterestGroup_whenMember_thenReturnsTrue() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    InterestGroupResult igResult = mock(InterestGroupResult.class);
    when(circabcService.getInterestGroup(igRef)).thenReturn(igResult);
    when(igResult.getIsRegistered()).thenReturn(false);
    when(circabcService.isUserMember(igRef, TEST_USER)).thenReturn(true);
    assertTrue(service.canAccessInterestGroup("ig-id"));
  }

  @Test
  public void testCanAccessInterestGroup_whenNotMemberNotCatAdmin_thenReturnsFalse() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    InterestGroupResult igResult = mock(InterestGroupResult.class);
    when(circabcService.getInterestGroup(igRef)).thenReturn(igResult);
    when(igResult.getIsRegistered()).thenReturn(false);
    when(circabcService.isUserMember(igRef, TEST_USER)).thenReturn(false);
    when(
      circabcService.isCategoryAdminOfInterestGroup(igRef, TEST_USER)
    ).thenReturn(false);
    assertFalse(service.canAccessInterestGroup("ig-id"));
  }

  // --- isInterestGroupLibAdmin ---

  @Test
  public void testIsInterestGroupLibAdmin_whenHasLibAdmin_thenReturnsTrue() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-id"
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(
      nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Library")
    ).thenReturn(libRef);
    when(
      permissionService.hasPermission(
        libRef,
        LibraryPermissions.LIBADMIN.toString()
      )
    ).thenReturn(AccessStatus.ALLOWED);
    assertTrue(service.isInterestGroupLibAdmin("ig-id"));
  }

  @Test
  public void testIsInterestGroupLibAdmin_whenNotIgRoot_thenReturnsFalse() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );
    assertFalse(service.isInterestGroupLibAdmin("ig-id"));
  }

  // --- isWorkingCopyOwner ---

  @Test
  public void testIsWorkingCopyOwner_whenOwner_thenReturnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER)
    ).thenReturn(TEST_USER);
    assertTrue(service.isWorkingCopyOwner(TEST_NODE_ID));
  }

  @Test
  public void testIsWorkingCopyOwner_whenNotWorkingCopy_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);
    assertFalse(service.isWorkingCopyOwner(TEST_NODE_ID));
  }

  @Test
  public void testIsWorkingCopyOwner_whenDifferentOwner_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      TEST_NODE_ID
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(true);
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER)
    ).thenReturn("otheruser");
    assertFalse(service.isWorkingCopyOwner(TEST_NODE_ID));
  }

  // --- isExternalUser (no-arg) ---

  @Test
  public void testIsExternalUser_whenLdapDisabled_thenReturnsFalse() {
    when(circabcConfig.isUseLDAP()).thenReturn(false);
    assertFalse(service.isExternalUser());
  }

  @Test
  public void testIsExternalUser_whenExternalDomain_thenReturnsTrue() {
    when(circabcConfig.isUseLDAP()).thenReturn(true);
    when(authorityService.isGuestAuthority(TEST_USER)).thenReturn(false);
    CircabcUserDataBean userBean = mock(CircabcUserDataBean.class);
    when(ldapUserService.getLDAPUserDataByUid(TEST_USER)).thenReturn(userBean);
    when(userBean.getDomain()).thenReturn("external");
    assertTrue(service.isExternalUser());
  }

  @Test
  public void testIsExternalUser_whenInternalDomain_thenReturnsFalse() {
    when(circabcConfig.isUseLDAP()).thenReturn(true);
    when(authorityService.isGuestAuthority(TEST_USER)).thenReturn(false);
    CircabcUserDataBean userBean = mock(CircabcUserDataBean.class);
    when(ldapUserService.getLDAPUserDataByUid(TEST_USER)).thenReturn(userBean);
    when(userBean.getDomain()).thenReturn("internal");
    assertFalse(service.isExternalUser());
  }

  // --- isCurrentUserEmailEqualTo ---

  @Test
  public void testIsCurrentUserEmailEqualTo_whenMatches_thenReturnsTrue() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson(TEST_USER)).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("test@example.com");
    assertTrue(service.isCurrentUserEmailEqualTo("test@example.com"));
  }

  @Test
  public void testIsCurrentUserEmailEqualTo_whenDifferent_thenReturnsFalse() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson(TEST_USER)).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("test@example.com");
    assertFalse(service.isCurrentUserEmailEqualTo("other@example.com"));
  }

  // --- throwIfNotCategoryAdmin ---

  @Test
  public void testThrowIfNotCategoryAdmin_whenAdmin_thenReturnsTrue() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    when(nodeService.exists(catRef)).thenReturn(true);
    when(circabcService.isCategoryAdmin(catRef, "testuser")).thenReturn(true);
    assertTrue(service.throwIfNotCategoryAdmin("cat-1"));
  }

  @Test(expected = AccessDeniedException.class)
  public void testThrowIfNotCategoryAdmin_whenNotAdmin_thenThrows() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    when(nodeService.exists(catRef)).thenReturn(true);
    when(circabcService.isCategoryAdmin(catRef, "testuser")).thenReturn(false);
    when(circabcService.isCircabcAdmin("testuser")).thenReturn(false);
    service.throwIfNotCategoryAdmin("cat-1");
  }

  // --- throwIfCanNotAccessInterestGroup ---

  @Test(expected = AccessDeniedException.class)
  public void testThrowIfCanNotAccessInterestGroup_whenNoAccess_thenThrows() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(nodeService.exists(igRef)).thenReturn(true);
    when(circabcService.isUserMember(igRef, "testuser")).thenReturn(false);
    when(
      circabcService.isCategoryAdminOfInterestGroup(igRef, "testuser")
    ).thenReturn(false);
    when(circabcService.isCircabcAdmin("testuser")).thenReturn(false);
    when(authorityService.isGuestAuthority("testuser")).thenReturn(false);
    InterestGroupResult igResult = mock(InterestGroupResult.class);
    when(igResult.getIsPublic()).thenReturn(false);
    when(igResult.getIsRegistered()).thenReturn(false);
    when(circabcService.getInterestGroup(igRef)).thenReturn(igResult);
    service.throwIfCanNotAccessInterestGroup("ig-1");
  }

  // --- throwIfNotCurrentUser ---

  @Test
  public void testThrowIfNotCurrentUser_whenSameUser_thenReturnsTrue() {
    assertTrue(service.throwIfNotCurrentUser("testuser"));
  }

  @Test(expected = AccessDeniedException.class)
  public void testThrowIfNotCurrentUser_whenDifferentUser_thenThrows() {
    service.throwIfNotCurrentUser("otheruser");
  }

  // --- hasAlfrescoWritePermission ---

  @Test
  public void testHasAlfrescoWritePermission_whenAllowed_thenReturnsTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    when(
      permissionService.hasPermission(nodeRef, PermissionService.WRITE)
    ).thenReturn(AccessStatus.ALLOWED);
    assertTrue(service.hasAlfrescoWritePermission("node-1"));
  }

  @Test
  public void testHasAlfrescoWritePermission_whenDenied_thenReturnsFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-1"
    );
    when(
      permissionService.hasPermission(nodeRef, PermissionService.WRITE)
    ).thenReturn(AccessStatus.DENIED);
    assertFalse(service.hasAlfrescoWritePermission("node-1"));
  }

  // --- isAlfrescoAdmin ---

  @Test
  public void testIsAlfrescoAdmin_whenAdmin_thenReturnsTrue() {
    when(authorityService.isAdminAuthority("testuser")).thenReturn(true);
    assertTrue(service.isAlfrescoAdmin());
  }

  @Test
  public void testIsAlfrescoAdmin_whenNotAdmin_thenReturnsFalse() {
    when(authorityService.isAdminAuthority("testuser")).thenReturn(false);
    assertFalse(service.isAlfrescoAdmin());
  }
}
