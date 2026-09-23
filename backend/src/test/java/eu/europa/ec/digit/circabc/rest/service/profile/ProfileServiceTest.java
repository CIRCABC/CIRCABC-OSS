package eu.europa.ec.digit.circabc.rest.service.profile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ProfileModel;
import io.swagger.model.db.Profile;
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

public class ProfileServiceTest {

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
  public void testGetProfiles_returnsProfileList() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    List<Profile> result = profileService.getProfiles(IG_NODE_REF);

    assertEquals(1, result.size());
    assertEquals("Admin", result.get(0).getName());
    assertEquals("AdminGroup", result.get(0).getAlfrescoGroup());
  }

  @Test
  public void testGetProfile_returnsMatchingProfile() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    Profile result = profileService.getProfile(IG_NODE_REF, "Admin");

    assertNotNull(result);
    assertEquals("Admin", result.getName());
  }

  @Test
  public void testGetProfile_returnsNullWhenNotFound() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    Profile result = profileService.getProfile(IG_NODE_REF, "NonExistent");

    assertNull(result);
  }

  @Test
  public void testGetPersonProfile_returnsProfileNameForUser() {
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
  public void testGetPersonProfile_returnsNullWhenUserNotFound() {
    setupSingleProfile("Admin", "GROUP_AdminGroup", false, false);

    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_AdminGroup",
        false
      )
    ).thenReturn(Collections.emptySet());

    String result = profileService.getPersonProfile(IG_NODE_REF, "unknown");

    assertNull(result);
  }

  @Test
  public void testHasGuestVisibility_returnsTrueWhenVisible() {
    setupSingleProfile("GuestProfile", "guest", false, false);

    Boolean result = profileService.hasGuestVisibility(IG_NODE_REF);

    assertTrue(result);
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

    setupServiceNodes(true);
  }

  private void setupServiceNodes(boolean visible) {
    String[] services = {
      "DIRECTORY",
      "INFORMATION",
      "LIBRARY",
      "EVENT",
      "NEWSGROUP",
      "VISIBILITY",
    };
    String[] perms = {
      "DirAdmin",
      "InfAdmin",
      "LibAdmin",
      "EveAdmin",
      "NwsAdmin",
      visible ? "Visibility" : "NoVisibility",
    };

    List<ChildAssociationRef> assocs = new ArrayList<>();
    for (int i = 0; i < services.length; i++) {
      NodeRef svcNode = new NodeRef(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        "svc-" + i
      );
      ChildAssociationRef assoc = mock(ChildAssociationRef.class);
      when(assoc.getChildRef()).thenReturn(svcNode);
      assocs.add(assoc);

      when(nodeService.getProperty(svcNode, SERVICE_NAME_QNAME)).thenReturn(
        services[i]
      );
      when(nodeService.getProperty(svcNode, PERMISSION_SET_QNAME)).thenReturn(
        new ArrayList<>(Collections.singletonList(perms[i]))
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
