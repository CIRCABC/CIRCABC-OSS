package eu.europa.ec.digit.circabc.rest.service.profile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ProfileModel;
import io.swagger.model.db.Profile;
import io.swagger.model.db.UserWithProfile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class ProfileServiceImplTest {

  private ProfileServiceImpl profileService;
  private NodeService nodeService;
  private AuthorityService authorityService;
  private CircabcService circabcService;
  private CircabcDaoServiceImpl circabcDaoService;

  private static final String CIRCA_IG_ROOT = "circaIGRoot";
  private static final String CIRCABC_URI =
    CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI;

  private static final NodeRef IG_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "ig-id"
  );
  private static final NodeRef PROFILE_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "profile-id"
  );

  private static final QName PROFILE_ASSOC_QNAME = QName.createQName(
    CIRCABC_URI,
    CIRCA_IG_ROOT + "ProfileAssoc"
  );
  private static final QName PROFILE_NAME_QNAME = QName.createQName(
    CIRCABC_URI,
    CIRCA_IG_ROOT + "ProfileName"
  );
  private static final QName PROFILE_GROUP_NAME_QNAME = QName.createQName(
    CIRCABC_URI,
    CIRCA_IG_ROOT + "ProfileGroupName"
  );
  private static final QName SERVICE_ASSOC_QNAME = QName.createQName(
    CIRCABC_URI,
    CIRCA_IG_ROOT + "ServiceAssoc"
  );
  private static final QName SERVICE_NAME_QNAME = QName.createQName(
    CIRCABC_URI,
    CIRCA_IG_ROOT + "ServiceName"
  );
  private static final QName PERMISSION_SET_QNAME = QName.createQName(
    CIRCABC_URI,
    CIRCA_IG_ROOT + "PermissionSet"
  );

  @Before
  public void setUp() throws Exception {
    profileService = new ProfileServiceImpl();
    nodeService = mock(NodeService.class);
    authorityService = mock(AuthorityService.class);
    circabcService = mock(CircabcService.class);
    circabcDaoService = mock(CircabcDaoServiceImpl.class);

    setField("nodeService", nodeService);
    setField("authorityService", authorityService);
    setField("circabcService", circabcService);
    setField("circabcDaoService", circabcDaoService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ProfileServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(profileService, value);
  }

  @Test
  public void testGetProfiles_whenNoProfiles_thenReturnsEmptyList() {
    when(
      nodeService.getChildAssocs(
        IG_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    List<Profile> result = profileService.getProfiles(IG_NODE_REF);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetProfiles_whenProfileExists_thenReturnsProfile() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    List<Profile> result = profileService.getProfiles(IG_NODE_REF);

    assertEquals(1, result.size());
    assertEquals("Admin", result.get(0).getName());
    assertEquals("AdminGroup", result.get(0).getAlfrescoGroup());
    assertEquals(PROFILE_NODE_REF.toString(), result.get(0).getNodeRef());
  }

  @Test
  public void testGetProfile_whenProfileFound_thenReturnsIt() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    Profile result = profileService.getProfile(IG_NODE_REF, "Admin");

    assertNotNull(result);
    assertEquals("Admin", result.getName());
  }

  @Test
  public void testGetProfile_whenProfileNotFound_thenReturnsNull() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    Profile result = profileService.getProfile(IG_NODE_REF, "NonExistent");

    assertNull(result);
  }

  @Test
  public void testGetProfile_whenGuestProfile_thenAlfrescoGroupIsGuest() {
    setupSingleProfile("GuestProfile", "guest", false, false);

    Profile result = profileService.getProfile(IG_NODE_REF, "GuestProfile");

    assertNotNull(result);
    assertEquals("guest", result.getAlfrescoGroup());
  }

  @Test
  public void testGetPersonInProfile_whenUserInProfile_thenReturnsUser() {
    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    when(assocRef.getChildRef()).thenReturn(PROFILE_NODE_REF);
    when(
      nodeService.getChildAssocs(
        IG_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(assocRef));

    when(
      nodeService.getProperty(PROFILE_NODE_REF, PROFILE_NAME_QNAME)
    ).thenReturn("Admin");
    when(
      nodeService.getProperty(PROFILE_NODE_REF, PROFILE_GROUP_NAME_QNAME)
    ).thenReturn("GROUP_AdminGroup");
    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_AdminGroup",
        false
      )
    ).thenReturn(new HashSet<>(Arrays.asList("user1", "user2")));

    Set<String> result = profileService.getPersonInProfile(
      IG_NODE_REF,
      "Admin"
    );

    assertEquals(2, result.size());
    assertTrue(result.contains("user1"));
    assertTrue(result.contains("user2"));
  }

  @Test
  public void testGetPersonInProfile_whenProfileNotFound_thenReturnsEmpty() {
    ChildAssociationRef assocRef = mock(ChildAssociationRef.class);
    when(assocRef.getChildRef()).thenReturn(PROFILE_NODE_REF);
    when(
      nodeService.getChildAssocs(
        IG_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(assocRef));

    when(
      nodeService.getProperty(PROFILE_NODE_REF, PROFILE_NAME_QNAME)
    ).thenReturn("Admin");

    Set<String> result = profileService.getPersonInProfile(
      IG_NODE_REF,
      "Other"
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetPersonProfile_whenUserInGroup_thenReturnsProfileName() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_AdminGroup",
        false
      )
    ).thenReturn(new HashSet<>(Collections.singletonList("testuser")));

    String result = profileService.getPersonProfile(IG_NODE_REF, "testuser");

    assertEquals("Admin", result);
  }

  @Test
  public void testGetPersonProfile_whenUserNotInAnyGroup_thenReturnsNull() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_AdminGroup",
        false
      )
    ).thenReturn(Collections.emptySet());

    String result = profileService.getPersonProfile(IG_NODE_REF, "unknownuser");

    assertNull(result);
  }

  @Test
  public void testGetPersonProfile_whenGuestProfile_thenSkipsIt() {
    setupSingleProfile("GuestProfile", "guest", false, false);

    String result = profileService.getPersonProfile(IG_NODE_REF, "testuser");

    assertNull(result);
    verify(authorityService, never()).getContainedAuthorities(
      any(),
      anyString(),
      anyBoolean()
    );
  }

  @Test
  public void testGetInvitedUsersProfiles_whenUsersExist_thenReturnsMap() {
    Map<String, Long> locales = new HashMap<>();
    locales.put("en_", 1L);
    when(circabcDaoService.getAllAlfrescoLocale()).thenReturn(locales);

    UserWithProfile user = mock(UserWithProfile.class);
    when(user.getUserName()).thenReturn("john");
    when(user.getProfileId()).thenReturn(10L);
    when(user.getAlfrescoGroup()).thenReturn("AdminGroup");
    when(user.getProfileName()).thenReturn("Admin");
    when(user.getProfileTitle()).thenReturn("Administrator");
    when(user.getDirectoryPermission()).thenReturn("DirAdmin");
    when(user.getInformationPermission()).thenReturn("InfAdmin");
    when(user.getLibraryPermission()).thenReturn("LibAdmin");
    when(user.getNewsgroupPermission()).thenReturn("NwsAdmin");
    when(user.getEventPermission()).thenReturn("EveAdmin");
    when(user.isExported()).thenReturn(false);
    when(user.isImported()).thenReturn(false);
    when(user.isVisible()).thenReturn(true);
    when(user.getProfileNodeRef()).thenReturn("noderef1");
    when(user.getProfileIgFromNodeRef()).thenReturn(null);

    when(
      circabcService.getFilteredUsers(IG_NODE_REF, 1L, "", "", "")
    ).thenReturn(Collections.singletonList(user));
    when(
      nodeService.getProperty(IG_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);

    Map<String, Profile> result = profileService.getInvitedUsersProfiles(
      IG_NODE_REF
    );

    assertEquals(1, result.size());
    assertTrue(result.containsKey("john"));
    assertEquals("Admin", result.get("john").getName());
  }

  @Test
  public void testHasGuestVisibility_whenGuestVisible_thenReturnsTrue() {
    setupSingleProfile("GuestProfile", "guest", false, false);

    Boolean result = profileService.hasGuestVisibility(IG_NODE_REF);

    assertTrue(result);
  }

  @Test
  public void testHasAllCircabcUsersVisibility_whenNotVisible_thenReturnsFalse() {
    // Setup a profile with GROUP_EVERYONE that is NOT visible
    ChildAssociationRef profileAssoc = mock(ChildAssociationRef.class);
    when(profileAssoc.getChildRef()).thenReturn(PROFILE_NODE_REF);
    when(
      nodeService.getChildAssocs(
        IG_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NODE_DBID, 1L);
    props.put(PROFILE_NAME_QNAME, "Everyone");
    props.put(PROFILE_GROUP_NAME_QNAME, "GROUP_GROUP_EVERYONE");
    props.put(ProfileModel.PROP_PROFILE_IMPORTED, false);
    props.put(ProfileModel.PROP_PROFILE_EXPORTED, false);
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);

    setupServiceNodes(null, false);

    Boolean result = profileService.hasAllCircabcUsersVisibility(IG_NODE_REF);

    assertFalse(result);
  }

  // --- Helper methods ---

  private void setupSingleProfile(
    String profileName,
    String groupName,
    boolean imported,
    boolean exported
  ) {
    ChildAssociationRef profileAssoc = mock(ChildAssociationRef.class);
    when(profileAssoc.getChildRef()).thenReturn(PROFILE_NODE_REF);
    when(
      nodeService.getChildAssocs(
        IG_NODE_REF,
        PROFILE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NODE_DBID, 1L);
    props.put(PROFILE_NAME_QNAME, profileName);
    props.put(PROFILE_GROUP_NAME_QNAME, groupName);
    props.put(ProfileModel.PROP_PROFILE_IMPORTED, imported);
    props.put(ProfileModel.PROP_PROFILE_EXPORTED, exported);
    when(nodeService.getProperties(PROFILE_NODE_REF)).thenReturn(props);

    // Setup service nodes
    NodeRef serviceNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "service-node"
    );
    setupServiceNodes(serviceNode, true);
  }

  private void setupServiceNodes(NodeRef ignored, boolean visible) {
    String[] serviceNames = {
      "DIRECTORY",
      "INFORMATION",
      "LIBRARY",
      "EVENT",
      "NEWSGROUP",
      "VISIBILITY",
    };
    String[] permissions = {
      "DirAdmin",
      "InfAdmin",
      "LibAdmin",
      "EveAdmin",
      "NwsAdmin",
      visible ? "Visibility" : "NoVisibility",
    };

    List<ChildAssociationRef> assocs = new ArrayList<>();
    for (int i = 0; i < serviceNames.length; i++) {
      NodeRef svcNode = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        "svc-" + i
      );
      ChildAssociationRef assoc = mock(ChildAssociationRef.class);
      when(assoc.getChildRef()).thenReturn(svcNode);
      assocs.add(assoc);

      when(nodeService.getProperty(svcNode, SERVICE_NAME_QNAME)).thenReturn(
        serviceNames[i]
      );
      when(nodeService.getProperty(svcNode, PERMISSION_SET_QNAME)).thenReturn(
        new ArrayList<>(Collections.singletonList(permissions[i]))
      );
    }

    when(
      nodeService.getChildAssocs(
        PROFILE_NODE_REF,
        SERVICE_ASSOC_QNAME,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(assocs);
  }
}
