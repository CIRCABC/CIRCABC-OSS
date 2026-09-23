package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.profile.ProfileService;
import io.swagger.model.I18nProperty;
import io.swagger.model.Profile;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ProfileModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class ProfilesApiImplTest {

  private ProfilesApiImpl profilesApi;

  private NodeService nodeService;
  private PermissionService permissionService;
  private AuthorityService authorityService;
  private ApiToolBox apiToolBox;
  private CircabcService circabcService;
  private ProfileService profileService;

  private static final String GROUP_ID = "test-group-id";
  private static final String PROFILE_ID = "test-profile-id";
  private static final NodeRef GROUP_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    GROUP_ID
  );
  private static final NodeRef PROFILE_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    PROFILE_ID
  );

  private static final QName PROFILE_ASSOC_QNAME = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "circaIGRootProfileAssoc"
  );

  private static final QName DIRECTORY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "DIRECTORY"
  );
  private static final QName LIBRARY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "LIBRARY"
  );
  private static final QName NEWSGROUP = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "NEWSGROUP"
  );
  private static final QName EVENT = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "EVENT"
  );
  private static final QName INFORMATION = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "INFORMATION"
  );
  private static final QName VISIBILITY = QName.createQName(
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
    "VISIBILITY"
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

    profilesApi = new ProfilesApiImpl();

    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    authorityService = mock(AuthorityService.class);
    apiToolBox = mock(ApiToolBox.class);
    circabcService = mock(CircabcService.class);
    profileService = mock(ProfileService.class);

    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("authorityService", authorityService);
    setField("apiToolBox", apiToolBox);
    setField("circabcService", circabcService);
    setField("profileService", profileService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ProfilesApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(profilesApi, value);
  }

  @Test
  public void testGroupsIdProfilesGet_whenProfilesExist_thenReturnsAll() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    ChildAssociationRef profileAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );

    when(
      nodeService.getChildAssocs(
        GROUP_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    Map<QName, Serializable> props = buildProfileProperties(
      "Admin",
      "GROUP_admin"
    );
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    List<Profile> result = profilesApi.groupsIdProfilesGet(GROUP_ID, "", false);

    assertEquals(1, result.size());
    assertEquals("Admin", result.get(0).getName());
    assertEquals("GROUP_admin", result.get(0).getGroupName());
  }

  @Test
  public void testGroupsIdProfilesGet_whenSearchQueryMatches_thenFiltersResults() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    ChildAssociationRef profileAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );

    when(
      nodeService.getChildAssocs(
        GROUP_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    Map<QName, Serializable> props = buildProfileProperties(
      "Admin",
      "GROUP_admin"
    );
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    List<Profile> result = profilesApi.groupsIdProfilesGet(
      GROUP_ID,
      "NonExistent",
      false
    );

    assertEquals(0, result.size());
  }

  @Test
  public void testGroupsIdProfilesGet_whenNonEmptyProfilesTrue_thenFiltersEmpty() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    ChildAssociationRef profileAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );

    when(
      nodeService.getChildAssocs(
        GROUP_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    Map<QName, Serializable> props = buildProfileProperties(
      "Admin",
      "GROUP_admin"
    );
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    when(apiToolBox.getUsersFromGroup("GROUP_admin")).thenReturn(
      Collections.emptyList()
    );

    List<Profile> result = profilesApi.groupsIdProfilesGet(GROUP_ID, "", true);

    assertEquals(0, result.size());
  }

  @Test
  public void testGroupsIdProfilesGet_whenNonEmptyProfilesTrue_thenReturnsNonEmpty() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    ChildAssociationRef profileAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );

    when(
      nodeService.getChildAssocs(
        GROUP_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    Map<QName, Serializable> props = buildProfileProperties(
      "Admin",
      "GROUP_admin"
    );
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    when(apiToolBox.getUsersFromGroup("GROUP_admin")).thenReturn(
      Collections.singletonList("user1")
    );

    List<Profile> result = profilesApi.groupsIdProfilesGet(GROUP_ID, "", true);

    assertEquals(1, result.size());
  }

  @Test
  public void testProfilesIdGet_whenValidId_thenReturnsProfile() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    Map<QName, Serializable> props = buildProfileProperties(
      "Reader",
      "GROUP_reader"
    );
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    Profile result = profilesApi.profilesIdGet(PROFILE_ID);

    assertEquals("Reader", result.getName());
    assertEquals(PROFILE_ID, result.getId());
    assertEquals("GROUP_reader", result.getGroupName());
  }

  @Test
  public void testProfilesIdDelete_whenNotImported_thenDeletesAuthorityAndNode() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    Map<QName, Serializable> props = buildProfileProperties(
      "TestProfile",
      "GROUP_test"
    );
    props.put(ProfileModel.PROP_PROFILE_IMPORTED, false);
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );
    when(nodeService.getPrimaryParent(PROFILE_NODE_REF)).thenReturn(
      parentAssoc
    );

    NodeRef result = profilesApi.profilesIdDelete(PROFILE_NODE_REF);

    assertEquals(GROUP_NODE_REF, result);
    verify(nodeService).deleteNode(PROFILE_NODE_REF);
    verify(circabcService).deleteProfile(GROUP_NODE_REF, "TestProfile");
  }

  @Test
  public void testProfilesIdDelete_whenImported_thenSkipsAuthorityDeletion() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    Map<QName, Serializable> props = buildProfileProperties(
      "ImportedProfile",
      "GROUP_imported"
    );
    props.put(ProfileModel.PROP_PROFILE_IMPORTED, true);
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );
    when(nodeService.getPrimaryParent(PROFILE_NODE_REF)).thenReturn(
      parentAssoc
    );

    NodeRef result = profilesApi.profilesIdDelete(PROFILE_NODE_REF);

    assertEquals(GROUP_NODE_REF, result);
    verify(nodeService).deleteNode(PROFILE_NODE_REF);
    verify(authorityService, never()).deleteAuthority(anyString());
  }

  @Test
  public void testGetInvitedUsers_whenAuthorityExists_thenReturnsUsers() {
    QName invitedUsersGroupProp = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaIGRootInvitedUsersGroup"
    );
    when(
      nodeService.getProperty(GROUP_NODE_REF, invitedUsersGroupProp)
    ).thenReturn("someGroup");
    when(authorityService.getName(AuthorityType.GROUP, "someGroup")).thenReturn(
      "GROUP_someGroup"
    );
    when(authorityService.authorityExists("GROUP_someGroup")).thenReturn(true);

    Set<String> users = new HashSet<>(Arrays.asList("user1", "user2"));
    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_someGroup",
        false
      )
    ).thenReturn(users);

    Set<String> result = profilesApi.getInvitedUsers(GROUP_NODE_REF);

    assertEquals(2, result.size());
    assertTrue(result.contains("user1"));
    assertTrue(result.contains("user2"));
  }

  @Test
  public void testGetInvitedUsers_whenAuthorityNotExists_thenReturnsEmpty() {
    QName invitedUsersGroupProp = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaIGRootInvitedUsersGroup"
    );
    when(
      nodeService.getProperty(GROUP_NODE_REF, invitedUsersGroupProp)
    ).thenReturn("someGroup");
    when(authorityService.getName(AuthorityType.GROUP, "someGroup")).thenReturn(
      "GROUP_someGroup"
    );
    when(authorityService.authorityExists("GROUP_someGroup")).thenReturn(false);

    Set<String> result = profilesApi.getInvitedUsers(GROUP_NODE_REF);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetPersonProfile_whenCalled_thenDelegatesToProfileService() {
    when(profileService.getPersonProfile(GROUP_NODE_REF, "user1")).thenReturn(
      "Admin"
    );

    String result = profilesApi.getPersonProfile(GROUP_NODE_REF, "user1");

    assertEquals("Admin", result);
    verify(profileService).getPersonProfile(GROUP_NODE_REF, "user1");
  }

  @Test
  public void testGetPersonProfileGroupName_whenCalled_thenDelegatesToCircabcService() {
    when(
      circabcService.getPersonProfileGroupName(GROUP_NODE_REF, "user1")
    ).thenReturn("GROUP_admin");

    String result = profilesApi.getPersonProfileGroupName(
      GROUP_NODE_REF,
      "user1"
    );

    assertEquals("GROUP_admin", result);
    verify(circabcService).getPersonProfileGroupName(GROUP_NODE_REF, "user1");
  }

  // --- profilesIdPut ---

  @Test
  public void testProfilesIdPut_whenValidBody_thenUpdatesProfile() {
    NodeRef serviceChildRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-child-id"
    );
    Map<QName, Serializable> props = buildProfileProperties(
      "Access",
      "GROUP_access"
    );
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);
    mockServiceChildAssocs(PROFILE_NODE_REF, serviceChildRef);

    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      PROFILE_NODE_REF
    );
    when(nodeService.getPrimaryParent(PROFILE_NODE_REF)).thenReturn(
      parentAssoc
    );

    // Mock the service child for updateServicePermission
    ChildAssociationRef serviceAssoc = new ChildAssociationRef(
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      PROFILE_NODE_REF,
      DIRECTORY,
      serviceChildRef
    );

    // Mock child by name for applyServicePermissions
    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-ref"
    );
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Library"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(libRef);

    I18nProperty title = new I18nProperty();
    title.put("en", "Updated Title");

    Profile body = new Profile();
    body.setTitle(title);
    body.setImported(false);
    body.setExported(false);
    body.setGroupName("GROUP_access");
    body.getPermissions().put("members", "DirAccess");
    body.getPermissions().put("library", "LibAccess");
    body.getPermissions().put("newsgroups", "NwsAccess");
    body.getPermissions().put("events", "EveAccess");
    body.getPermissions().put("information", "InfAccess");
    body.getPermissions().put("visibility", "Visibility");

    Profile result = profilesApi.profilesIdPut(PROFILE_NODE_REF, body);

    assertNotNull(result);
    verify(nodeService).setProperty(
      eq(PROFILE_NODE_REF),
      eq(ContentModel.PROP_TITLE),
      any()
    );
    verify(circabcService).updateProfile(
      eq(GROUP_NODE_REF),
      eq("Access"),
      any(Profile.class)
    );
  }

  // --- groupsIdProfilesPost ---

  @Test
  public void testGroupsIdProfilesPost_whenValidBody_thenCreatesProfile() {
    when(
      nodeService.hasAspect(GROUP_NODE_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    // No existing profiles
    when(
      nodeService.getChildAssocs(
        GROUP_NODE_REF,
        PROFILE_ASSOC_QNAME,
        org.alfresco.service.namespace.RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    // Mock the invited user group property
    when(
      nodeService.getProperty(
        GROUP_NODE_REF,
        CircabcModel.PROP_IG_ROOT_INVITED_USER_GROUP
      )
    ).thenReturn("invitedGroup");

    // Mock createNode for profile and all services
    NodeRef newProfileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-profile"
    );
    ChildAssociationRef profileAssocRef = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      newProfileRef
    );
    when(
      nodeService.createNode(
        eq(GROUP_NODE_REF),
        any(QName.class),
        any(QName.class),
        any(QName.class)
      )
    ).thenReturn(profileAssocRef);

    NodeRef serviceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "svc-ref"
    );
    ChildAssociationRef svcAssocRef = new ChildAssociationRef(
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      newProfileRef,
      DIRECTORY,
      serviceRef
    );
    when(
      nodeService.createNode(
        eq(newProfileRef),
        any(QName.class),
        any(QName.class),
        any(QName.class)
      )
    ).thenReturn(svcAssocRef);

    // Mock authorityService for group creation
    when(authorityService.createAuthority(any(), anyString())).thenReturn(
      "GROUP_newprofile"
    );

    // Mock getProfile for the return value
    Map<QName, Serializable> newProps = buildProfileProperties(
      "circaIGRoot0",
      "GROUP_newprofile"
    );
    when(nodeService.getProperties(newProfileRef)).thenReturn(newProps);
    mockServiceChildAssocs(newProfileRef, serviceRef);

    // Mock getPrimaryParent for applyServicePermissions
    ChildAssociationRef parentAssoc = new ChildAssociationRef(
      PROFILE_ASSOC_QNAME,
      GROUP_NODE_REF,
      PROFILE_ASSOC_QNAME,
      newProfileRef
    );
    when(nodeService.getPrimaryParent(newProfileRef)).thenReturn(parentAssoc);

    // Mock child by name for applyServicePermissions
    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-ref"
    );
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Library"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        GROUP_NODE_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(libRef);

    I18nProperty title = new I18nProperty();
    title.put("en", "New Profile");

    Profile body = new Profile();
    body.setTitle(title);
    body.getPermissions().put("members", "DirAccess");
    body.getPermissions().put("library", "LibAccess");
    body.getPermissions().put("newsgroups", "NwsAccess");
    body.getPermissions().put("events", "EveAccess");
    body.getPermissions().put("information", "InfAccess");
    body.getPermissions().put("visibility", "Visibility");

    Profile result = profilesApi.groupsIdProfilesPost(GROUP_NODE_REF, body);

    assertNotNull(result);
    verify(circabcService).updateProfile(
      eq(GROUP_NODE_REF),
      anyString(),
      any(Profile.class)
    );
  }

  // --- Helper methods ---

  private Map<QName, Serializable> buildProfileProperties(
    String name,
    String groupName
  ) {
    Map<QName, Serializable> props = new HashMap<>();
    props.put(CircabcModel.PROP_IG_ROOT_PROFILE_NAME, name);
    props.put(ContentModel.PROP_TITLE, name);
    props.put(CircabcModel.PROP_IG_ROOT_PROFILE_GROUP_NAME, groupName);
    props.put(ProfileModel.PROP_PROFILE_IMPORTED, false);
    props.put(ProfileModel.PROP_PROFILE_EXPORTED, false);
    return props;
  }

  private void mockServiceChildAssocs(
    NodeRef profileRef,
    NodeRef serviceChildRef
  ) {
    ChildAssociationRef serviceAssoc = new ChildAssociationRef(
      CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
      profileRef,
      DIRECTORY,
      serviceChildRef
    );

    when(
      nodeService.getChildAssocs(
        profileRef,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        DIRECTORY
      )
    ).thenReturn(Collections.singletonList(serviceAssoc));
    when(
      nodeService.getChildAssocs(
        profileRef,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        LIBRARY
      )
    ).thenReturn(Collections.singletonList(serviceAssoc));
    when(
      nodeService.getChildAssocs(
        profileRef,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        NEWSGROUP
      )
    ).thenReturn(Collections.singletonList(serviceAssoc));
    when(
      nodeService.getChildAssocs(
        profileRef,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        EVENT
      )
    ).thenReturn(Collections.singletonList(serviceAssoc));
    when(
      nodeService.getChildAssocs(
        profileRef,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        INFORMATION
      )
    ).thenReturn(Collections.singletonList(serviceAssoc));
    when(
      nodeService.getChildAssocs(
        profileRef,
        CircabcModel.PROP_IG_ROOT_SERVICE_ASSOC,
        VISIBILITY
      )
    ).thenReturn(Collections.singletonList(serviceAssoc));

    ArrayList<String> perms = new ArrayList<>();
    perms.add("DirAccess");
    when(
      nodeService.getProperty(
        serviceChildRef,
        CircabcModel.PROP_IG_ROOT_PERMISSION_SET
      )
    ).thenReturn(perms);

    when(
      nodeService.getTargetAssocs(
        profileRef,
        ProfileModel.ASSOC_PROFILE_IMPORTED_TO
      )
    ).thenReturn(Collections.emptyList());
  }
}
