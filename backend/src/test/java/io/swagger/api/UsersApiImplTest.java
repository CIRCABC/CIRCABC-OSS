package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetails;
import eu.europa.ec.digit.circabc.rest.service.user.UserDetailsService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.model.Category;
import io.swagger.model.InterestGroupProfile;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.User;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserIGMembershipRecord;
import io.swagger.model.alfresco.UserModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class UsersApiImplTest {

  private UsersApiImpl usersApi;
  private UserService userService;
  private NodeService nodeService;
  private PersonService personService;
  private AuthorityService authorityService;
  private MutableAuthenticationService authenticationService;
  private UserDetailsService userDetailsService;
  private CircabcService circabcService;
  private GroupsApi groupsApi;

  @Before
  public void setUp() throws Exception {
    // Initialize AuthenticationUtil
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

    usersApi = new UsersApiImpl();

    userService = mock(UserService.class);
    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);
    authorityService = mock(AuthorityService.class);
    authenticationService = mock(MutableAuthenticationService.class);
    userDetailsService = mock(UserDetailsService.class);
    circabcService = mock(CircabcService.class);
    groupsApi = mock(GroupsApi.class);

    setField("userService", userService);
    setField("nodeService", nodeService);
    setField("personService", personService);
    setField("authorityService", authorityService);
    setField("authenticationService", authenticationService);
    setField("userDetailsService", userDetailsService);
    setField("circabcService", circabcService);
    setField("groupsApi", groupsApi);
    setField("maxUserNumber", 5);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(usersApi, value);
  }

  // --- usersGet tests ---

  @Test
  public void testUsersGet_whenQueryWithoutAt_thenSearchesByDomain() {
    List<SearchResultRecord> records = new ArrayList<>();
    records.add(new SearchResultRecord("user1", "John", "Doe", "john@ec.eu"));
    when(
      userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      )
    ).thenReturn(records);

    List<User> result = usersApi.usersGet("john", false, false);

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0).getUserId());
    assertEquals("John", result.get(0).getFirstname());
    assertEquals("Doe", result.get(0).getLastname());
    assertEquals("john@ec.eu", result.get(0).getEmail());
  }

  @Test
  public void testUsersGet_whenQueryWithAt_thenSearchesByMailDomain() {
    List<SearchResultRecord> records = new ArrayList<>();
    records.add(new SearchResultRecord("user2", "Jane", "Smith", "jane@ec.eu"));
    when(
      userService.getUsersByMailDomain("jane@ec.eu", "allusers", false)
    ).thenReturn(records);

    List<User> result = usersApi.usersGet("jane@ec.eu", false, false);

    assertEquals(1, result.size());
    assertEquals("user2", result.get(0).getUserId());
  }

  @Test
  public void testUsersGet_whenMatchQuery_thenFiltersNonMatchingEmails() {
    List<SearchResultRecord> records = new ArrayList<>();
    records.add(new SearchResultRecord("user1", "John", "Doe", "john@ec.eu"));
    records.add(new SearchResultRecord("user2", "Jane", "Smith", "jane@ec.eu"));
    when(
      userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      )
    ).thenReturn(records);

    List<User> result = usersApi.usersGet("john", false, true);

    // Only "john@ec.eu" matches the query when matchQuery=true (case-insensitive)
    // "john" != "john@ec.eu" so nothing matches
    assertEquals(0, result.size());
  }

  @Test
  public void testUsersGet_whenDuplicateUsernames_thenDeduplicates() {
    List<SearchResultRecord> records = new ArrayList<>();
    records.add(new SearchResultRecord("user1", "John", "Doe", "john@ec.eu"));
    records.add(new SearchResultRecord("user1", "John", "Doe", "john@ec.eu"));
    when(
      userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      )
    ).thenReturn(records);

    List<User> result = usersApi.usersGet("john", false, false);

    assertEquals(1, result.size());
  }

  @Test
  public void testUsersGet_whenMoreThanMaxUsers_thenLimitsResults() {
    List<SearchResultRecord> records = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      records.add(
        new SearchResultRecord(
          "user" + i,
          "First" + i,
          "Last" + i,
          "user" + i + "@ec.eu"
        )
      );
    }
    when(
      userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "user",
        false
      )
    ).thenReturn(records);

    List<User> result = usersApi.usersGet("user", false, false);

    assertEquals(5, result.size());
  }

  // --- usersUserIdGet tests ---

  @Test
  public void testUsersUserIdGet_whenUserExists_thenReturnsPopulatedUser() {
    String userId = "testuser";
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    NodeRef avatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "avatar-id"
    );
    NodeRef defaultAvatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "default-avatar-id"
    );

    when(personService.personExists(userId)).thenReturn(true);
    when(personService.getPerson(userId)).thenReturn(personRef);

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_USERNAME, userId);
    props.put(ContentModel.PROP_FIRSTNAME, "Test");
    props.put(ContentModel.PROP_LASTNAME, "User");
    props.put(ContentModel.PROP_EMAIL, "test@ec.eu");
    props.put(UserModel.PROP_PHONE, "+32123456");
    props.put(UserModel.PROP_VISISBILITY, true);
    props.put(UserModel.PROP_ECAS_USER_NAME, "ecasUser");
    props.put(UserModel.PROP_GLOBAL_NOTIFICATION, true);
    when(nodeService.getProperties(personRef)).thenReturn(props);

    when(userDetailsService.getAvatar(personRef)).thenReturn(avatarRef);
    when(userDetailsService.getDefaultAvatar()).thenReturn(defaultAvatarRef);

    Set<String> authorities = new HashSet<>();
    when(authorityService.getAuthorities()).thenReturn(authorities);
    when(authorityService.hasAdminAuthority()).thenReturn(false);

    User result = usersApi.usersUserIdGet(userId);

    assertEquals(userId, result.getUserId());
    assertEquals("Test", result.getFirstname());
    assertEquals("User", result.getLastname());
    assertEquals("test@ec.eu", result.getEmail());
    assertEquals("+32123456", result.getPhone());
    assertEquals(true, result.getVisibility());
    assertEquals("avatar-id", result.getAvatar());
    assertFalse(result.isDefaultAvatar());
  }

  @Test
  public void testUsersUserIdGet_whenUserDoesNotExist_thenReturnsEmptyUser() {
    when(personService.personExists("unknown")).thenReturn(false);

    User result = usersApi.usersUserIdGet("unknown");

    assertNull(result.getUserId());
    assertNotNull(result.getProperties());
  }

  @Test
  public void testUsersUserIdGet_whenAvatarThrowsException_thenSetsDefaultAvatar() {
    String userId = "testuser";
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );

    when(personService.personExists(userId)).thenReturn(true);
    when(personService.getPerson(userId)).thenReturn(personRef);

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_USERNAME, userId);
    props.put(ContentModel.PROP_FIRSTNAME, "Test");
    props.put(ContentModel.PROP_LASTNAME, "User");
    props.put(ContentModel.PROP_EMAIL, "test@ec.eu");
    when(nodeService.getProperties(personRef)).thenReturn(props);

    when(userDetailsService.getAvatar(personRef)).thenThrow(
      new RuntimeException("avatar error")
    );

    Set<String> authorities = new HashSet<>();
    when(authorityService.getAuthorities()).thenReturn(authorities);
    when(authorityService.hasAdminAuthority()).thenReturn(false);

    User result = usersApi.usersUserIdGet(userId);

    assertTrue(result.isDefaultAvatar());
    assertEquals("", result.getAvatar());
  }

  // --- getUserCategories tests ---

  @Test
  public void testGetUserCategories_whenUserHasCategories_thenReturnsList() {
    String userId = "testuser";
    NodeRef categRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "categ-id"
    );

    List<UserCategoryMembershipRecord> categories = new ArrayList<>();
    categories.add(
      new UserCategoryMembershipRecord("TestCategory", "CatAdmin", "categ-id")
    );
    when(userService.getCategories(userId)).thenReturn(categories);
    when(nodeService.getProperty(categRef, ContentModel.PROP_NAME)).thenReturn(
      "TestCategory"
    );
    when(nodeService.getProperty(categRef, ContentModel.PROP_TITLE)).thenReturn(
      "Test Category Title"
    );

    List<Category> result = usersApi.getUserCategories(userId);

    assertEquals(1, result.size());
    assertEquals("categ-id", result.get(0).getId());
    assertEquals("TestCategory", result.get(0).getName());
  }

  @Test
  public void testGetUserCategories_whenEmptyUserId_thenReturnsEmptyList() {
    List<Category> result = usersApi.getUserCategories("");

    assertEquals(0, result.size());
    verifyNoInteractions(userService);
  }

  // --- usersUserIdDelete tests ---

  @Test
  public void testUsersUserIdDelete_whenUserExists_thenDeletesUser() {
    String userId = "deleteMe";

    when(personService.personExists(userId)).thenReturn(true);
    when(authenticationService.authenticationExists(userId)).thenReturn(true);

    usersApi.usersUserIdDelete(userId);

    verify(personService).deletePerson(userId);
    verify(authenticationService).deleteAuthentication(userId);
    verify(circabcService).deleteUserFromDatabase(userId);
  }

  @Test
  public void testUsersUserIdDelete_whenUserDoesNotExist_thenSkipsDeletion() {
    String userId = "ghost";

    when(personService.personExists(userId)).thenReturn(false);
    when(authenticationService.authenticationExists(userId)).thenReturn(false);

    usersApi.usersUserIdDelete(userId);

    verify(personService, never()).deletePerson(userId);
    verify(authenticationService, never()).deleteAuthentication(userId);
    verify(circabcService).deleteUserFromDatabase(userId);
  }

  // --- removeAvatar tests ---

  @Test
  public void testRemoveAvatar_whenCalled_thenDelegatesToService() {
    String userId = "testuser";
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson(userId)).thenReturn(personRef);

    usersApi.removeAvatar(userId);

    verify(userDetailsService).removeAvatar(personRef);
  }

  // --- usersPost tests ---

  @Test
  public void testUsersPost_whenValidUser_thenCreatesAuthAndPerson() {
    User user = new User();
    user.setUserId("newuser");
    user.setFirstname("New");
    user.setLastname("User");
    user.setEmail("new@ec.eu");
    user.setPhone("+32000");
    Map<String, String> props = new HashMap<>();
    props.put("title", "Mr");
    props.put("fax", "+32111");
    props.put("urlAddress", "http://example.com");
    props.put("postalAddress", "Brussels");
    props.put("description", "Test user");
    props.put("companyId", "DIGIT");
    props.put("password", "secret123");
    user.setProperties(props);

    usersApi.usersPost(user);

    verify(authenticationService).createAuthentication(
      eq("newuser"),
      eq("secret123".toCharArray())
    );
    verify(personService).createPerson(anyMap());
  }

  // --- getUserMembership tests ---

  @Test
  public void testGetUserMembership_whenEmptyUserId_thenReturnsEmptyList() {
    List<InterestGroupProfile> result = usersApi.getUserMembership("");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetUserMembership_whenUserHasMemberships_thenReturnsList() {
    String userId = "testuser";
    List<UserIGMembershipRecord> memberships = new ArrayList<>();
    UserIGMembershipRecord record = new UserIGMembershipRecord(
      "ig-node-id",
      "TestIG",
      "cat-node-id",
      "TestCategory",
      "Member",
      "TestCategory",
      "TestIG",
      "Member Title",
      "GROUP_TestIG_Member",
      "profile-node-id"
    );
    memberships.add(record);
    when(userService.getInterestGroups(userId)).thenReturn(memberships);

    io.swagger.model.InterestGroup ig = new io.swagger.model.InterestGroup();
    ig.setId("ig-node-id");
    when(groupsApi.getInterestGroup("ig-node-id", true)).thenReturn(ig);

    List<InterestGroupProfile> result = usersApi.getUserMembership(userId);

    assertEquals(1, result.size());
    assertEquals("ig-node-id", result.get(0).getInterestGroup().getId());
    assertEquals("profile-node-id", result.get(0).getProfile().getId());
    assertEquals("Member", result.get(0).getProfile().getName());
  }

  // --- usersUserIdPut tests ---

  @Test
  public void testUsersUserIdPut_whenUpdatingFields_thenReturnsUpdatedUser()
    throws Exception {
    String userId = "testuser";
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    NodeRef avatarRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "avatar-id"
    );

    org.alfresco.repo.configuration.ConfigurableService configurableService =
      mock(org.alfresco.repo.configuration.ConfigurableService.class);
    setField("configurableService", configurableService);

    when(personService.getPerson(userId)).thenReturn(personRef);

    org.alfresco.service.cmr.security.PersonService.PersonInfo personInfo =
      mock(org.alfresco.service.cmr.security.PersonService.PersonInfo.class);
    when(personInfo.getUserName()).thenReturn(userId);
    when(personInfo.getFirstName()).thenReturn("Test");
    when(personInfo.getLastName()).thenReturn("User");
    when(personService.getPerson(personRef)).thenReturn(personInfo);

    UserDetails userDetails = new UserDetails();
    userDetails.setEmail("test@ec.eu");
    userDetails.setPhone("+32123");
    userDetails.setVisibility(true);
    userDetails.setUserInterfaceLanguage("en");
    userDetails.setContentFilterLanguage(java.util.Locale.ENGLISH);
    userDetails.setAvatar(avatarRef);
    when(userDetailsService.getUserDetails(personRef)).thenReturn(userDetails);
    when(configurableService.getConfigurationFolder(personRef)).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "config")
    );

    User body = new User();
    body.setEmail("updated@ec.eu");
    body.setFirstname("Updated");

    User result = usersApi.usersUserIdPut(userId, body);

    assertEquals("updated@ec.eu", result.getEmail());
    assertEquals("Updated", result.getFirstname());
    verify(userDetailsService).updateUserDetails(
      eq(personRef),
      any(UserDetails.class)
    );
    verify(circabcService).updateUser(personRef);
  }
}
