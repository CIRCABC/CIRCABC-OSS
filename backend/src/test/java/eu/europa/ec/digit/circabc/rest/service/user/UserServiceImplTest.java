package eu.europa.ec.digit.circabc.rest.service.user;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.SearchResultRecord;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserIGMembershipRecord;
import io.swagger.model.alfresco.UserModel;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class UserServiceImplTest {

  private UserServiceImpl userService;
  private NodeService nodeService;
  private PersonService personService;
  private SearchService searchService;
  private AuthorityService authorityService;
  private PermissionService permissionService;
  private MutableAuthenticationService authenticationService;
  private CircabcService circabcService;
  private LdapUserService ldapUserService;

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

    userService = new UserServiceImpl();

    nodeService = mock(NodeService.class);
    personService = mock(PersonService.class);
    searchService = mock(SearchService.class);
    authorityService = mock(AuthorityService.class);
    permissionService = mock(PermissionService.class);
    authenticationService = mock(MutableAuthenticationService.class);
    circabcService = mock(CircabcService.class);
    ldapUserService = mock(LdapUserService.class);

    setField("nodeService", nodeService);
    setField("personService", personService);
    setField("searchService", searchService);
    setField("authorityService", authorityService);
    setField("permissionService", permissionService);
    setField("authenticationService", authenticationService);
    setField("circabcService", circabcService);
    setField("ldapUserService", ldapUserService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UserServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(userService, value);
  }

  // --- getPerson ---

  @Test
  public void testGetPerson_whenValidUser_thenReturnsNodeRef() {
    NodeRef expected = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(expected);

    NodeRef result = userService.getPerson("john");

    assertEquals(expected, result);
  }

  // --- getUserEmail ---

  @Test
  public void testGetUserEmail_whenUserExists_thenReturnsEmail() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("john@ec.europa.eu");

    String email = userService.getUserEmail("john");

    assertEquals("john@ec.europa.eu", email);
  }

  // --- getUserFullName ---

  @Test
  public void testGetUserFullName_whenBothNames_thenReturnsConcatenated() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");

    String fullName = userService.getUserFullName("john");

    assertEquals("John Doe", fullName);
  }

  @Test
  public void testGetUserFullName_whenFirstNameNull_thenReturnsLastNameOnly() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");

    String fullName = userService.getUserFullName("john");

    assertEquals("Doe", fullName);
  }

  @Test
  public void testGetUserFullName_whenBothNull_thenReturnsEmpty() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn(null);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn(null);

    String fullName = userService.getUserFullName("john");

    assertEquals("", fullName);
  }

  // --- getUserDomain ---

  @Test
  public void testGetUserDomain_whenDomainExists_thenReturnsDomain() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);
    when(nodeService.getProperty(personRef, UserModel.PROP_DOMAIN)).thenReturn(
      "ec.europa.eu"
    );

    String domain = userService.getUserDomain("john");

    assertEquals("ec.europa.eu", domain);
  }

  @Test
  public void testGetUserDomain_whenDomainNull_thenReturnsNull() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);
    when(nodeService.getProperty(personRef, UserModel.PROP_DOMAIN)).thenReturn(
      null
    );

    String domain = userService.getUserDomain("john");

    assertNull(domain);
  }

  // --- getUserNameByEmail ---

  @Test
  public void testGetUserNameByEmail_whenFound_thenReturnsUsername() {
    ResultSet rs = mock(ResultSet.class);
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);
    when(rs.length()).thenReturn(1);

    NodeRef resultNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "found-id"
    );
    org.alfresco.service.cmr.search.ResultSetRow row = mock(
      org.alfresco.service.cmr.search.ResultSetRow.class
    );
    when(rs.getRow(0)).thenReturn(row);
    when(row.getNodeRef()).thenReturn(resultNode);
    when(
      nodeService.getProperty(resultNode, ContentModel.PROP_USERNAME)
    ).thenReturn("john");

    String result = userService.getUserNameByEmail("john@ec.europa.eu");

    assertEquals("john", result);
    verify(rs).close();
  }

  @Test
  public void testGetUserNameByEmail_whenNotFound_thenReturnsNull() {
    ResultSet rs = mock(ResultSet.class);
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);
    when(rs.length()).thenReturn(0);

    String result = userService.getUserNameByEmail("nobody@ec.europa.eu");

    assertNull(result);
    verify(rs).close();
  }

  // --- getUsersWithPermission ---

  @Test
  public void testGetUsersWithPermission_whenUserHasPermission_thenReturnsUser() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    AccessPermission ap = mock(AccessPermission.class);
    when(ap.getPermission()).thenReturn("Read");
    when(ap.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    when(ap.getAuthority()).thenReturn("john");

    Set<AccessPermission> permissions = new HashSet<>();
    permissions.add(ap);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      permissions
    );

    Set<String> result = userService.getUsersWithPermission(nodeRef, "Read");

    assertTrue(result.contains("john"));
    assertEquals(1, result.size());
  }

  @Test
  public void testGetUsersWithPermission_whenGroupAuthority_thenExpandsGroup() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    AccessPermission ap = mock(AccessPermission.class);
    when(ap.getPermission()).thenReturn("Read");
    when(ap.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);
    when(ap.getAuthority()).thenReturn("GROUP_TestGroup");

    Set<AccessPermission> permissions = new HashSet<>();
    permissions.add(ap);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      permissions
    );
    when(authorityService.authorityExists("GROUP_TestGroup")).thenReturn(true);

    Set<String> groupMembers = new HashSet<>(Arrays.asList("alice", "bob"));
    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_TestGroup",
        false
      )
    ).thenReturn(groupMembers);

    Set<String> result = userService.getUsersWithPermission(nodeRef, "Read");

    assertTrue(result.contains("alice"));
    assertTrue(result.contains("bob"));
    assertEquals(2, result.size());
  }

  @Test
  public void testGetUsersWithPermission_whenDenied_thenExcludesUser() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    AccessPermission ap = mock(AccessPermission.class);
    when(ap.getPermission()).thenReturn("Read");
    when(ap.getAccessStatus()).thenReturn(AccessStatus.DENIED);
    when(ap.getAuthority()).thenReturn("john");

    Set<AccessPermission> permissions = new HashSet<>();
    permissions.add(ap);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      permissions
    );

    Set<String> result = userService.getUsersWithPermission(nodeRef, "Read");

    assertTrue(result.isEmpty());
  }

  // --- setPassword ---

  @Test
  public void testSetPassword_whenCalled_thenDelegatesToAuthService() {
    char[] password = "newPass123".toCharArray();

    userService.setPassword("john", password);

    verify(authenticationService).setAuthentication("john", password);
  }

  // --- setAuthenticationEnabled / getAuthenticationEnabled ---

  @Test
  public void testSetAuthenticationEnabled_whenCalled_thenDelegates() {
    userService.setAuthenticationEnabled("john", true);

    verify(authenticationService).setAuthenticationEnabled("john", true);
  }

  @Test
  public void testGetAuthenticationEnabled_whenEnabled_thenReturnsTrue() {
    when(authenticationService.getAuthenticationEnabled("john")).thenReturn(
      true
    );

    assertTrue(userService.getAuthenticationEnabled("john"));
  }

  // --- getCategories ---

  @Test
  public void testGetCategories_whenCalled_thenReturnsSortedList() {
    List<UserCategoryMembershipRecord> categories = new ArrayList<>();
    when(circabcService.getCategories("john")).thenReturn(categories);

    List<UserCategoryMembershipRecord> result = userService.getCategories(
      "john"
    );

    assertSame(categories, result);
    verify(circabcService).getCategories("john");
  }

  // --- getInterestGroups ---

  @Test
  public void testGetInterestGroups_whenCalled_thenReturnsSortedList() {
    List<UserIGMembershipRecord> igs = new ArrayList<>();
    when(circabcService.getInterestGroups("john")).thenReturn(igs);

    List<UserIGMembershipRecord> result = userService.getInterestGroups("john");

    assertSame(igs, result);
    verify(circabcService).getInterestGroups("john");
  }

  // --- generateSecurePassword ---

  @Test
  public void testGenerateSecurePassword_whenCalled_thenReturnsCorrectLength() {
    String password = UserServiceImpl.generateSecurePassword(50);

    assertEquals(50, password.length());
  }

  @Test
  public void testGenerateSecurePassword_whenZeroLength_thenReturnsEmpty() {
    String password = UserServiceImpl.generateSecurePassword(0);

    assertEquals("", password);
  }

  // --- getLDAPUserDataByUid ---

  @Test
  public void testGetLDAPUserDataByUid_whenCalled_thenDelegates() {
    CircabcUserDataBean expected = new CircabcUserDataBean();
    when(ldapUserService.getLDAPUserDataByUid("john")).thenReturn(expected);

    CircabcUserDataBean result = userService.getLDAPUserDataByUid("john");

    assertSame(expected, result);
  }

  // --- getUsersByDomainFirstNameLastNameEmail ---

  @Test
  public void testGetUsersByDomainFirstNameLastNameEmail_whenNoLdap_thenReturnsLdapResults()
    throws Exception {
    io.swagger.config.CircabcConfig circabcConfig = mock(
      io.swagger.config.CircabcConfig.class
    );
    setField("circabcConfig", circabcConfig);
    when(circabcConfig.isUseLDAP()).thenReturn(false);

    List<SearchResultRecord> ldapResults = new ArrayList<>();
    ldapResults.add(
      new SearchResultRecord("user1", "John", "Doe", "john@ec.eu")
    );
    when(
      ldapUserService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      )
    ).thenReturn(ldapResults);

    List<SearchResultRecord> result =
      userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      );

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0).getUserName());
  }

  @Test
  public void testGetUsersByDomainFirstNameLastNameEmail_whenUseLdapAndNotFilter_thenMergesResults()
    throws Exception {
    io.swagger.config.CircabcConfig circabcConfig = mock(
      io.swagger.config.CircabcConfig.class
    );
    LdapUserService luceneUserService = mock(LdapUserService.class);
    setField("circabcConfig", circabcConfig);
    setField("luceneUserService", luceneUserService);
    when(circabcConfig.isUseLDAP()).thenReturn(true);

    List<SearchResultRecord> ldapResults = new ArrayList<>();
    ldapResults.add(
      new SearchResultRecord("user1", "John", "Doe", "john@ec.eu")
    );
    when(
      ldapUserService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      )
    ).thenReturn(ldapResults);

    List<SearchResultRecord> luceneResults = new ArrayList<>();
    luceneResults.add(
      new SearchResultRecord("user2", "Jane", "Smith", "jane@ec.eu")
    );
    when(
      luceneUserService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      )
    ).thenReturn(luceneResults);

    List<SearchResultRecord> result =
      userService.getUsersByDomainFirstNameLastNameEmail(
        "allusers",
        "john",
        false
      );

    assertEquals(2, result.size());
  }

  // --- getUsersByMailDomain ---

  @Test
  public void testGetUsersByMailDomain_whenNoLdap_thenReturnsLdapResults()
    throws Exception {
    io.swagger.config.CircabcConfig circabcConfig = mock(
      io.swagger.config.CircabcConfig.class
    );
    setField("circabcConfig", circabcConfig);
    when(circabcConfig.isUseLDAP()).thenReturn(false);

    List<SearchResultRecord> ldapResults = new ArrayList<>();
    ldapResults.add(
      new SearchResultRecord("user1", "John", "Doe", "john@ec.eu")
    );
    when(
      ldapUserService.getUsersByMailDomain("john@ec.eu", "allusers", false)
    ).thenReturn(ldapResults);

    List<SearchResultRecord> result = userService.getUsersByMailDomain(
      "john@ec.eu",
      "allusers",
      false
    );

    assertEquals(1, result.size());
  }

  // --- getCircabcUserDataBean ---

  @Test
  public void testGetCircabcUserDataBean_whenUserExists_thenReturnsBean() {
    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-id"
    );
    when(personService.getPerson("john")).thenReturn(personRef);

    Map<QName, java.io.Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_FIRSTNAME, "John");
    props.put(ContentModel.PROP_LASTNAME, "Doe");
    props.put(ContentModel.PROP_USERNAME, "john");
    props.put(ContentModel.PROP_EMAIL, "john@ec.eu");
    props.put(UserModel.PROP_PHONE, "+32123");
    props.put(UserModel.PROP_VISISBILITY, Boolean.TRUE);
    when(nodeService.getProperties(personRef)).thenReturn(props);

    CircabcUserDataBean result = userService.getCircabcUserDataBean("john");

    assertEquals("John", result.getFirstName());
    assertEquals("Doe", result.getLastName());
    assertEquals("john", result.getUserName());
    assertEquals("john@ec.eu", result.getEmail());
    assertEquals("+32123", result.getPhone());
    assertTrue(result.getVisibility());
  }
}
