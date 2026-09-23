package eu.europa.ec.digit.circabc.rest.service.app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.exception.UserDeletionException;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.db.UserWithProfile;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class CircabcServiceImplTest {

  private CircabcServiceImpl circabcService;
  private CircabcDaoServiceImpl circabcDaoService;
  private NodeService nodeService;
  private PersonService personService;

  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "test-id"
  );

  @Before
  public void setUp() throws Exception {
    circabcService = new CircabcServiceImpl();
    circabcDaoService = mock(CircabcDaoServiceImpl.class);
    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);

    setField("circabcDaoService", circabcDaoService);
    setField("nodeService", nodeService);
    setField("personService", personService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(circabcService, value);
  }

  // --- isCircabcAdmin ---

  @Test
  public void testIsCircabcAdmin_whenUserIsAdmin_thenReturnsTrue() {
    when(circabcDaoService.getIsCircabcAdmin("admin")).thenReturn(1);
    assertTrue(circabcService.isCircabcAdmin("admin"));
  }

  @Test
  public void testIsCircabcAdmin_whenUserIsNotAdmin_thenReturnsFalse() {
    when(circabcDaoService.getIsCircabcAdmin("user")).thenReturn(0);
    assertFalse(circabcService.isCircabcAdmin("user"));
  }

  // --- isCategoryAdmin ---

  @Test
  public void testIsCategoryAdmin_whenUserIsAdmin_thenReturnsTrue() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(circabcDaoService.getIsCategoryAdmin(100L, "admin")).thenReturn(1);

    assertTrue(circabcService.isCategoryAdmin(TEST_NODE_REF, "admin"));
  }

  @Test
  public void testIsCategoryAdmin_whenUserIsNotAdmin_thenReturnsFalse() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(circabcDaoService.getIsCategoryAdmin(100L, "user")).thenReturn(0);

    assertFalse(circabcService.isCategoryAdmin(TEST_NODE_REF, "user"));
  }

  // --- isUserMember ---

  @Test
  public void testIsUserMember_whenMember_thenReturnsTrue() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(circabcDaoService.getIsMemberOfGroup(200L, "member")).thenReturn(1);

    assertTrue(circabcService.isUserMember(TEST_NODE_REF, "member"));
  }

  @Test
  public void testIsUserMember_whenNotMember_thenReturnsFalse() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(circabcDaoService.getIsMemberOfGroup(200L, "stranger")).thenReturn(0);

    assertFalse(circabcService.isUserMember(TEST_NODE_REF, "stranger"));
  }

  // --- isExternalUser ---

  @Test
  public void testIsExternalUser_whenExternal_thenReturnsTrue() {
    when(circabcDaoService.getisExternalUser("ext")).thenReturn(1);
    assertTrue(circabcService.isExternalUser("ext"));
  }

  @Test
  public void testIsExternalUser_whenInternal_thenReturnsFalse() {
    when(circabcDaoService.getisExternalUser("int")).thenReturn(0);
    assertFalse(circabcService.isExternalUser("int"));
  }

  // --- getUserLocaleID ---

  @Test
  public void testGetUserLocaleID_whenLocaleExists_thenReturnsIt() {
    when(circabcDaoService.selectLocaleIDByUserName("user")).thenReturn(5L);
    assertEquals(Long.valueOf(5L), circabcService.getUserLocaleID("user"));
  }

  @Test
  public void testGetUserLocaleID_whenLocaleNull_thenReturnsDefault() {
    when(circabcDaoService.selectLocaleIDByUserName("user")).thenReturn(null);
    assertEquals(Long.valueOf(1L), circabcService.getUserLocaleID("user"));
  }

  // --- isUserExists ---

  @Test
  public void testIsUserExists_whenUserFound_thenReturnsTrue() {
    when(circabcDaoService.selectUserIDByUserName("user")).thenReturn(42L);
    assertTrue(circabcService.isUserExists("user"));
  }

  @Test
  public void testIsUserExists_whenUserNotFound_thenReturnsFalse() {
    when(circabcDaoService.selectUserIDByUserName("ghost")).thenReturn(0L);
    assertFalse(circabcService.isUserExists("ghost"));
  }

  @Test
  public void testIsUserExists_whenExceptionThrown_thenReturnsFalse() {
    when(circabcDaoService.selectUserIDByUserName("bad")).thenThrow(
      new RuntimeException("db error")
    );
    assertFalse(circabcService.isUserExists("bad"));
  }

  // --- getInterestGroup ---

  @Test
  public void testGetInterestGroup_returnsResult() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(300L);
    InterestGroupResult expected = mock(InterestGroupResult.class);
    when(circabcDaoService.selectIgByID(300L)).thenReturn(expected);

    InterestGroupResult result = circabcService.getInterestGroup(TEST_NODE_REF);
    assertSame(expected, result);
  }

  // --- deleteAll ---

  @Test
  public void testDeleteAll_callsDaoDeleteAll() {
    circabcService.deleteAll();
    verify(circabcDaoService).deleteAll();
  }

  // --- countMembersInIg ---

  @Test
  public void testCountMembersInIg_returnsCount() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(500L);
    when(circabcDaoService.countUsersInIg(500L)).thenReturn(42);

    assertEquals(42, circabcService.countMembersInIg("ig-id"));
  }

  // --- getUserIds ---

  @Test
  public void testGetUserIds_returnsSet() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(600L);
    Set<String> expected = Set.of("user1", "user2");
    when(circabcDaoService.getUserIds(600L)).thenReturn(expected);

    assertEquals(expected, circabcService.getUserIds("ig-id"));
  }

  // --- getFilteredUsers ---

  @Test
  public void testGetFilteredUsers_whenNotIgRoot_thenReturnsEmpty() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    List<UserWithProfile> result = circabcService.getFilteredUsers(
      TEST_NODE_REF,
      1L,
      "group",
      "text"
    );
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetFilteredUsers_whenIgRoot_thenReturnsList() {
    when(
      nodeService.hasAspect(TEST_NODE_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(700L);
    List<UserWithProfile> expected = List.of(mock(UserWithProfile.class));
    when(
      circabcDaoService.selectUsersProfiles(700L, 1L, "group", "text", null)
    ).thenReturn(expected);

    List<UserWithProfile> result = circabcService.getFilteredUsers(
      TEST_NODE_REF,
      1L,
      "group",
      "text"
    );
    assertEquals(expected, result);
  }

  // --- deleteUserFromDatabase ---

  @Test
  public void testDeleteUserFromDatabase_callsDao() {
    circabcService.deleteUserFromDatabase("user1");
    verify(circabcDaoService).deleteUserFromAllTables("user1");
  }

  @Test(expected = UserDeletionException.class)
  public void testDeleteUserFromDatabase_whenDaoThrows_thenThrowsUserDeletionException() {
    doThrow(new RuntimeException("db error"))
      .when(circabcDaoService)
      .deleteUserFromAllTables("bad");

    circabcService.deleteUserFromDatabase("bad");
  }

  // --- updateInterestGroupPublic ---

  @Test
  public void testUpdateInterestGroupPublic_whenNullRef_thenNoOp() {
    circabcService.updateInterestGroupPublic(null, true);
    verifyNoInteractions(circabcDaoService);
  }

  @Test
  public void testUpdateInterestGroupPublic_whenNullFlag_thenNoOp() {
    circabcService.updateInterestGroupPublic(TEST_NODE_REF, null);
    verifyNoInteractions(circabcDaoService);
  }

  @Test
  public void testUpdateInterestGroupPublic_whenValid_thenUpdates() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(800L);

    circabcService.updateInterestGroupPublic(TEST_NODE_REF, true);
    verify(circabcDaoService).updateInterestGroupPublic(800L, true);
  }

  // --- getCategoryAdmins ---

  @Test
  public void testGetCategoryAdmins_returnsList() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(900L);
    List<String> expected = List.of("admin1", "admin2");
    when(circabcDaoService.selectCategoryAdmins(900L)).thenReturn(expected);

    assertEquals(expected, circabcService.getCategoryAdmins(TEST_NODE_REF));
  }

  // --- deletePersonFromGroup ---

  @Test
  public void testDeletePersonFromGroup_deletesAllMemberships() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1000L);
    var pu1 = mock(io.swagger.model.db.ProfileUser.class);
    var pu2 = mock(io.swagger.model.db.ProfileUser.class);
    when(circabcDaoService.getUserProfileInGroup(1000L, "user")).thenReturn(
      List.of(pu1, pu2)
    );

    circabcService.deletePersonFromGroup(TEST_NODE_REF, "user");

    verify(circabcDaoService).deleteUserInGroup(pu1);
    verify(circabcDaoService).deleteUserInGroup(pu2);
  }

  // --- addCircabcAdmin ---

  @Test
  public void testAddCircabcAdmin_insertsAdmin() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("newadmin")).thenReturn(personRef);
    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NODE_DBID, 55L);
    when(nodeService.getProperties(personRef)).thenReturn(props);

    circabcService.addCircabcAdmin("newadmin");

    verify(circabcDaoService).insertCircabcAdmin(argThat(admin -> true));
  }

  // --- isUserDirAdminOrCategoryAdminOrCircabcAdmin ---

  @Test
  public void testIsUserDirAdminOrCategoryAdminOrCircabcAdmin_whenTrue() {
    when(circabcDaoService.selectCountAdminDByUserName("admin")).thenReturn(1L);
    assertTrue(
      circabcService.isUserDirAdminOrCategoryAdminOrCircabcAdmin("admin")
    );
  }

  @Test
  public void testIsUserDirAdminOrCategoryAdminOrCircabcAdmin_whenFalse() {
    when(circabcDaoService.selectCountAdminDByUserName("user")).thenReturn(0L);
    assertFalse(
      circabcService.isUserDirAdminOrCategoryAdminOrCircabcAdmin("user")
    );
  }

  // --- isCategoryAdminOfInterestGroup ---

  @Test
  public void testIsCategoryAdminOfInterestGroup_whenTrue() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      circabcDaoService.getIsCategoryAdminOfInterestGroup(100L, "admin")
    ).thenReturn(1);
    assertTrue(
      circabcService.isCategoryAdminOfInterestGroup(TEST_NODE_REF, "admin")
    );
  }

  @Test
  public void testIsCategoryAdminOfInterestGroup_whenFalse() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      circabcDaoService.getIsCategoryAdminOfInterestGroup(100L, "user")
    ).thenReturn(0);
    assertFalse(
      circabcService.isCategoryAdminOfInterestGroup(TEST_NODE_REF, "user")
    );
  }

  // --- addCategoryNode ---

  @Test
  public void testAddCategoryNode_insertsCategoryWithTitle() {
    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header"
    );
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat"
    );

    when(
      nodeService.getProperty(catRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);
    when(nodeService.getProperty(catRef, ContentModel.PROP_NAME)).thenReturn(
      "catname"
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_TITLE)).thenReturn(
      "Cat Title"
    );
    when(
      nodeService.getProperty(headerRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(1L);

    circabcService.addCategoryNode(headerRef, catRef);

    verify(circabcDaoService).insertCategory(
      any(io.swagger.model.db.Category.class)
    );
  }

  // --- deleteCategory ---

  @Test
  public void testDeleteCategory_callsDaoDelete() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat"
    );
    when(
      circabcDaoService.selectCategoryIDByNodeRef(catRef.toString())
    ).thenReturn(5L);

    circabcService.deleteCategory(catRef);

    verify(circabcDaoService).deleteCategory(5L);
  }

  // --- deleteIntestGroup ---

  @Test
  public void testDeleteIntestGroup_callsDaoDelete() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(42L);

    circabcService.deleteIntestGroup(TEST_NODE_REF);

    verify(circabcDaoService).deleteInterestGroup(42L);
  }

  // --- deleteIntestGroupByID ---

  @Test
  public void testDeleteIntestGroupByID_callsDaoDelete() {
    circabcService.deleteIntestGroupByID(99L);
    verify(circabcDaoService).deleteInterestGroup(99L);
  }

  // --- addPersonToProfile ---

  @Test
  public void testAddPersonToProfile_whenProfileFound_thenInserts() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setAlfrescoGroup("GROUP_mygroup");
    when(
      circabcDaoService.selectProfileByInterestGroupIDProfileName(any())
    ).thenReturn(profile);

    NodeRef personRef2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person"
    );
    when(personService.getPerson("user1")).thenReturn(personRef2);
    Map<QName, Serializable> personProps = new HashMap<>();
    personProps.put(ContentModel.PROP_NODE_DBID, 55L);
    when(nodeService.getProperties(personRef2)).thenReturn(personProps);

    circabcService.addPersonToProfile(TEST_NODE_REF, "user1", "Access");

    verify(circabcDaoService).insertProfileUser(
      any(io.swagger.model.db.ProfileUser.class)
    );
  }

  // --- changePersonProfile ---

  @Test
  public void testChangePersonProfile_whenDeleteSucceeds_thenInserts() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      circabcDaoService.deleteProfileByInterestGroupUserName(100L, "user1")
    ).thenReturn(1);
    when(circabcDaoService.selectUserIDByUserName("user1")).thenReturn(55L);
    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setAlfrescoGroup("GROUP_newgroup");
    when(
      circabcDaoService.selectProfileByInterestGroupIDProfileName(any())
    ).thenReturn(profile);

    circabcService.changePersonProfile(TEST_NODE_REF, "user1", "NewProfile");

    verify(circabcDaoService).insertProfileUser(
      any(io.swagger.model.db.ProfileUser.class)
    );
  }

  // --- deleteProfile ---

  @Test
  public void testDeleteProfile_whenProfileExists_thenDeletes() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setId(77L);
    when(
      circabcDaoService.selectProfileByInterestGroupIDProfileName(any())
    ).thenReturn(profile);

    circabcService.deleteProfile(TEST_NODE_REF, "OldProfile");

    verify(circabcDaoService).deleteProfileTitleTranslationsByID(77L);
    verify(circabcDaoService).deleteProfileByID(77L);
  }

  // --- removeCategoryAdmin ---

  @Test
  public void testRemoveCategoryAdmin_callsDao() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat"
    );
    NodeRef personRef2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person"
    );
    when(
      nodeService.getProperty(catRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);
    when(personService.getPerson("admin")).thenReturn(personRef2);
    Map<org.alfresco.service.namespace.QName, java.io.Serializable> props =
      new HashMap<>();
    props.put(ContentModel.PROP_NODE_DBID, 55L);
    when(nodeService.getProperties(personRef2)).thenReturn(props);

    circabcService.removeCategoryAdmin(catRef, "admin");

    verify(circabcDaoService).deleteCategoryAdmin(
      any(io.swagger.model.db.CategoryAdmin.class)
    );
  }

  // --- getCategories (user) ---

  @Test
  public void testGetCategories_whenDaoReturnsItems_thenReturnsMapped() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-id"
    );
    when(circabcDaoService.selectCategories("user1")).thenReturn(
      List.of(catRef.toString())
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_NAME)).thenReturn(
      "catname"
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_TITLE)).thenReturn(
      "Cat Title"
    );

    var result = circabcService.getCategories("user1");

    assertEquals(1, result.size());
    assertEquals("cat-id", result.get(0).getCategoryNodeId());
  }

  // --- updateInterestGroupPublic with valid params ---

  @Test
  public void testUpdateInterestGroupRegistered_whenValid_thenUpdates() {
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(800L);

    circabcService.updateInterestGroupRegistered(TEST_NODE_REF, true);
    verify(circabcDaoService).updateInterestGroupRegistered(800L, true);
  }

  @Test
  public void testUpdateInterestGroupRegistered_whenNullRef_thenNoOp() {
    circabcService.updateInterestGroupRegistered(null, true);
    verifyNoInteractions(circabcDaoService);
  }

  // --- getInterestGroups ---

  @Test
  public void testGetInterestGroups_whenUserHasMemberships_thenReturnsMapped() {
    io.swagger.model.db.UserIGMembership membership =
      new io.swagger.model.db.UserIGMembership();
    membership.setCatNodeRef("workspace://SpacesStore/cat-1");
    membership.setIgNodeRef("workspace://SpacesStore/ig-1");
    membership.setProfileName("Access");
    membership.setProfileNodeRefId("workspace://SpacesStore/prof-1");
    membership.setProfileId(100L);
    membership.setAlfrescoGroup("GROUP_access");

    when(circabcDaoService.selectInterestGroups("user1")).thenReturn(
      Collections.singletonList(membership)
    );
    when(circabcDaoService.getAllAlfrescoLocale()).thenReturn(
      Collections.singletonMap("en_", 1L)
    );
    when(circabcDaoService.selectProfileTitles(100L)).thenReturn(
      Collections.emptyList()
    );

    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_NAME)).thenReturn(
      "category"
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_TITLE)).thenReturn(
      "Cat Title"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "myig"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "IG Title"
    );

    var result = circabcService.getInterestGroups("user1");

    assertEquals(1, result.size());
    assertEquals("ig-1", result.get(0).getInterestGroupNodeId());
    assertEquals("Myig", result.get(0).getInterestGroup());
    assertEquals("Access", result.get(0).getProfile());
  }

  @Test
  public void testGetInterestGroups_whenDaoThrows_thenReturnsEmpty() {
    when(circabcDaoService.selectInterestGroups("user1")).thenThrow(
      new RuntimeException("db error")
    );

    var result = circabcService.getInterestGroups("user1");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- updateIntestGroupProperties ---

  @Test
  public void testUpdateIntestGroupProperties_whenCalled_thenUpdatesDao() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(42L);
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "MyIG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      "Title"
    );

    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    org.alfresco.service.cmr.repository.ChildAssociationRef parentAssoc = mock(
      org.alfresco.service.cmr.repository.ChildAssociationRef.class
    );
    when(parentAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(igRef)).thenReturn(parentAssoc);
    when(
      nodeService.getProperty(parentRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);

    circabcService.updateIntestGroupProperties(igRef);

    verify(circabcDaoService).updateInterestGroup(any());
  }

  // --- updateCategoryProperties ---

  @Test
  public void testUpdateCategoryProperties_whenCalled_thenUpdatesDao() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    when(
      nodeService.getProperty(catRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);
    when(nodeService.getProperty(catRef, ContentModel.PROP_NAME)).thenReturn(
      "MyCat"
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_TITLE)).thenReturn(
      "Cat Title"
    );

    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header-1"
    );
    org.alfresco.service.cmr.repository.ChildAssociationRef parentAssoc = mock(
      org.alfresco.service.cmr.repository.ChildAssociationRef.class
    );
    when(parentAssoc.getParentRef()).thenReturn(headerRef);
    when(nodeService.getPrimaryParent(catRef)).thenReturn(parentAssoc);
    when(
      nodeService.getProperty(headerRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(5L);

    circabcService.updateCategoryProperties(catRef);

    verify(circabcDaoService).updateCategory(any());
  }

  // --- isExternalUser ---

  @Test
  public void testIsExternalUser_whenNotExternal_thenReturnsFalse() {
    when(circabcDaoService.getisExternalUser("intuser")).thenReturn(0);
    assertFalse(circabcService.isExternalUser("intuser"));
  }

  // --- addCategoryNode ---

  @Test
  public void testAddCategoryNode_whenValid_thenInserts() {
    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "header-1"
    );
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );

    when(
      nodeService.getProperty(catRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);
    when(nodeService.getProperty(catRef, ContentModel.PROP_NAME)).thenReturn(
      "MyCat"
    );
    when(nodeService.getProperty(catRef, ContentModel.PROP_TITLE)).thenReturn(
      "Cat Title"
    );
    when(
      nodeService.getProperty(headerRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(5L);

    // Mock for updateCategoryProperties
    org.alfresco.service.cmr.repository.ChildAssociationRef parentAssoc = mock(
      org.alfresco.service.cmr.repository.ChildAssociationRef.class
    );
    when(parentAssoc.getParentRef()).thenReturn(headerRef);
    when(nodeService.getPrimaryParent(catRef)).thenReturn(parentAssoc);

    circabcService.addCategoryNode(headerRef, catRef);

    verify(circabcDaoService).insertCategory(any());
  }

  // --- addUser ---

  @Test
  public void testAddUser_whenLocalesAvailable_thenInsertsUser() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-1"
    );

    Map<String, Long> locales = new java.util.HashMap<>();
    locales.put("en_", 1L);
    when(circabcDaoService.getAllAlfrescoLocale()).thenReturn(locales);

    Map<org.alfresco.service.namespace.QName, java.io.Serializable> props =
      new java.util.HashMap<>();
    props.put(org.alfresco.model.ContentModel.PROP_NODE_DBID, 42L);
    props.put(org.alfresco.model.ContentModel.PROP_USERNAME, "john");
    props.put(org.alfresco.model.ContentModel.PROP_FIRSTNAME, "John");
    props.put(org.alfresco.model.ContentModel.PROP_LASTNAME, "Doe");
    props.put(org.alfresco.model.ContentModel.PROP_EMAIL, "john@test.com");
    when(nodeService.getProperties(userRef)).thenReturn(props);

    circabcService.addUser(userRef);

    verify(circabcDaoService).insertUser(any());
  }

  @Test
  public void testAddUser_whenLocalesFail_thenDoesNotInsert() {
    NodeRef userRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "user-1"
    );
    when(circabcDaoService.getAllAlfrescoLocale()).thenThrow(
      new RuntimeException("db error")
    );

    circabcService.addUser(userRef);

    verify(circabcDaoService, never()).insertUser(any());
  }

  // --- deleteCategory ---

  @Test
  public void testDeleteCategory_thenDelegatesToDao() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    when(
      circabcDaoService.selectCategoryIDByNodeRef(catRef.toString())
    ).thenReturn(10L);

    circabcService.deleteCategory(catRef);

    verify(circabcDaoService).deleteCategory(10L);
  }

  // --- deleteIntestGroup ---

  @Test
  public void testDeleteIntestGroup_thenDelegatesToDao() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(20L);

    circabcService.deleteIntestGroup(igRef);

    verify(circabcDaoService).deleteInterestGroup(20L);
  }

  // --- deleteIntestGroupByID ---

  @Test
  public void testDeleteIntestGroupByID_thenDelegatesToDao() {
    circabcService.deleteIntestGroupByID(30L);
    verify(circabcDaoService).deleteInterestGroup(30L);
  }

  // --- removeCategoryAdmin ---

  @Test
  public void testRemoveCategoryAdmin_thenDeletesFromDao() {
    NodeRef catRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "cat-1"
    );
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );

    when(
      nodeService.getProperty(catRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);
    when(personService.getPerson("admin1")).thenReturn(personRef);

    Map<QName, java.io.Serializable> personProps = new java.util.HashMap<>();
    personProps.put(ContentModel.PROP_NODE_DBID, 42L);
    when(nodeService.getProperties(personRef)).thenReturn(personProps);

    circabcService.removeCategoryAdmin(catRef, "admin1");

    verify(circabcDaoService).deleteCategoryAdmin(any());
  }

  // --- addPersonToProfile ---

  @Test
  public void testAddPersonToProfile_whenProfileFound_thenInsertsUser() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(20L);

    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setAlfrescoGroup("GROUP_access");
    when(
      circabcDaoService.selectProfileByInterestGroupIDProfileName(any())
    ).thenReturn(profile);

    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    Map<QName, java.io.Serializable> personProps = new java.util.HashMap<>();
    personProps.put(ContentModel.PROP_NODE_DBID, 100L);
    when(nodeService.getProperties(personRef)).thenReturn(personProps);

    circabcService.addPersonToProfile(igRef, "user1", "Access");

    verify(circabcDaoService).insertProfileUser(any());
  }

  @Test
  public void testAddPersonToProfile_whenProfileNotFound_thenDoesNotInsert() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(20L);
    when(
      circabcDaoService.selectProfileByInterestGroupIDProfileName(any())
    ).thenReturn(null);

    circabcService.addPersonToProfile(igRef, "user1", "NonExistent");

    verify(circabcDaoService, never()).insertProfileUser(any());
  }

  // --- deleteProfile ---

  @Test
  public void testDeleteProfile_whenProfileExists_thenDeletesFromDao() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-1"
    );
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(20L);

    io.swagger.model.db.Profile profile = new io.swagger.model.db.Profile();
    profile.setId(50L);
    profile.setAlfrescoGroup("GROUP_test");
    when(
      circabcDaoService.selectProfileByInterestGroupIDProfileName(any())
    ).thenReturn(profile);

    circabcService.deleteProfile(igRef, "TestProfile");

    verify(circabcDaoService).deleteProfileByID(50L);
    verify(circabcDaoService).deleteProfileTitleTranslationsByID(50L);
  }
}
