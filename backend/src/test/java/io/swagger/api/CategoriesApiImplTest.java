package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.group.request.GroupRequestsDaoService;
import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.InterestGroupItem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class CategoriesApiImplTest {

  private static final String TEST_CATEGORY_ID =
    "00000000-0000-0000-0000-000000000001";
  private static final NodeRef TEST_CATEGORY_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_CATEGORY_ID
  );
  private static final String TEST_CIRCABC_ID =
    "00000000-0000-0000-0000-000000000099";
  private static final NodeRef TEST_CIRCABC_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_CIRCABC_ID
  );

  private CategoriesApiImpl categoriesApi;
  private NodeService nodeService;
  private CircabcApi circabcApi;
  private CircabcService circabcService;
  private UsersApi usersApi;
  private AuthorityService authorityService;
  private PersonService personService;
  private CircabcDaoServiceImpl circabcDaoService;
  private GroupRequestsDaoService groupRequestsDaoService;
  private UserService userService;
  private LdapUserService ldapUserService;

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

    categoriesApi = new CategoriesApiImpl();
    nodeService = mock(NodeService.class);
    circabcApi = mock(CircabcApi.class);
    circabcService = mock(CircabcService.class);
    usersApi = mock(UsersApi.class);
    authorityService = mock(AuthorityService.class);
    personService = mock(PersonService.class);
    circabcDaoService = mock(CircabcDaoServiceImpl.class);
    groupRequestsDaoService = mock(GroupRequestsDaoService.class);
    userService = mock(UserService.class);
    ldapUserService = mock(LdapUserService.class);

    setField(categoriesApi, "nodeService", nodeService);
    setField(categoriesApi, "circabcApi", circabcApi);
    setField(categoriesApi, "circabcService", circabcService);
    setField(categoriesApi, "usersApi", usersApi);
    setField(categoriesApi, "authorityService", authorityService);
    setField(categoriesApi, "personService", personService);
    setField(categoriesApi, "circabcDaoService", circabcDaoService);
    setField(categoriesApi, "groupRequestsDaoService", groupRequestsDaoService);
    setField(categoriesApi, "userService", userService);
    setField(categoriesApi, "ldapUserService", ldapUserService);
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  // --- getCategories ---

  @Test
  public void testGetCategories_whenNoChildren_thenReturnsEmptyList() {
    when(circabcApi.getCircabcNodeRef()).thenReturn(TEST_CIRCABC_REF);
    when(nodeService.getChildAssocs(TEST_CIRCABC_REF)).thenReturn(
      Collections.emptyList()
    );

    List<Category> result = categoriesApi.getCategories();

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetCategories_whenChildIsCategory_thenReturnsCategory() {
    when(circabcApi.getCircabcNodeRef()).thenReturn(TEST_CIRCABC_REF);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(TEST_CATEGORY_REF);
    when(nodeService.getChildAssocs(TEST_CIRCABC_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(
      nodeService.hasAspect(TEST_CATEGORY_REF, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(true);
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestCategory");
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Test Category Title");

    List<Category> result = categoriesApi.getCategories();

    assertEquals(1, result.size());
    assertEquals(TEST_CATEGORY_ID, result.get(0).getId());
    assertEquals("TestCategory", result.get(0).getName());
  }

  @Test
  public void testGetCategories_whenChildIsNotCategory_thenSkipped() {
    when(circabcApi.getCircabcNodeRef()).thenReturn(TEST_CIRCABC_REF);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(TEST_CATEGORY_REF);
    when(nodeService.getChildAssocs(TEST_CIRCABC_REF)).thenReturn(
      Collections.singletonList(childAssoc)
    );
    when(
      nodeService.hasAspect(TEST_CATEGORY_REF, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(false);

    List<Category> result = categoriesApi.getCategories();

    assertTrue(result.isEmpty());
  }

  // --- categoriesIdGet ---

  @Test
  public void testCategoriesIdGet_whenCategoryExists_thenReturnsCategory() {
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_NAME)
    ).thenReturn("MyCat");
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_TITLE)
    ).thenReturn("My Category");
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, CircabcModel.PROP_LOGO_REF)
    ).thenReturn(null);
    when(
      nodeService.getProperty(
        TEST_CATEGORY_REF,
        CircabcModel.PROP_SINGLE_CONTACT
      )
    ).thenReturn(null);
    when(
      nodeService.getProperty(
        TEST_CATEGORY_REF,
        CircabcModel.PROP_CONTACT_VERIFIED
      )
    ).thenReturn(null);
    when(
      nodeService.getProperty(
        TEST_CATEGORY_REF,
        CircabcModel.PROP_CONTACT_EMAILS
      )
    ).thenReturn(null);

    Category result = categoriesApi.categoriesIdGet(TEST_CATEGORY_ID);

    assertNotNull(result);
    assertEquals(TEST_CATEGORY_ID, result.getId());
    assertEquals("MyCat", result.getName());
  }

  @Test
  public void testCategoriesIdGet_whenContactEmails_thenParsedCorrectly() {
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_NAME)
    ).thenReturn("Cat");
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_TITLE)
    ).thenReturn(null);
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, CircabcModel.PROP_LOGO_REF)
    ).thenReturn(null);
    when(
      nodeService.getProperty(
        TEST_CATEGORY_REF,
        CircabcModel.PROP_SINGLE_CONTACT
      )
    ).thenReturn("true");
    when(
      nodeService.getProperty(
        TEST_CATEGORY_REF,
        CircabcModel.PROP_CONTACT_VERIFIED
      )
    ).thenReturn("false");
    when(
      nodeService.getProperty(
        TEST_CATEGORY_REF,
        CircabcModel.PROP_CONTACT_EMAILS
      )
    ).thenReturn("a@b.com;c@d.com;");

    Category result = categoriesApi.categoriesIdGet(TEST_CATEGORY_ID);

    assertTrue(result.getUseSingleContact());
    assertFalse(result.getContactVerified());
    assertEquals(2, result.getContactEmails().size());
    assertEquals("a@b.com", result.getContactEmails().get(0));
    assertEquals("c@d.com", result.getContactEmails().get(1));
  }

  // --- categoriesIdPut ---

  @Test
  public void testCategoriesIdPut_whenNullCategoryId_thenReturnsCategory() {
    Category category = new Category();
    Category result = categoriesApi.categoriesIdPut(null, category);
    assertEquals(category, result);
  }

  @Test
  public void testCategoriesIdPut_whenNullCategory_thenReturnsNull() {
    Category result = categoriesApi.categoriesIdPut(TEST_CATEGORY_ID, null);
    assertNull(result);
  }

  @Test
  public void testCategoriesIdPut_whenNameProvided_thenUpdatesName() {
    Category category = new Category();
    category.setName("NewName");
    category.setContactEmails(null);

    categoriesApi.categoriesIdPut(TEST_CATEGORY_ID, category);

    verify(nodeService).setProperty(
      TEST_CATEGORY_REF,
      ContentModel.PROP_NAME,
      "NewName"
    );
  }

  // --- categoriesIdAdminsGet ---

  @Test
  public void testCategoriesIdAdminsGet_whenAdminsExist_thenReturnsUsers() {
    List<String> admins = Arrays.asList("user1", "user2");
    when(circabcService.getCategoryAdmins(TEST_CATEGORY_REF)).thenReturn(
      admins
    );

    User user1 = new User();
    User user2 = new User();
    when(usersApi.usersUserIdGet("user1")).thenReturn(user1);
    when(usersApi.usersUserIdGet("user2")).thenReturn(user2);

    List<User> result = categoriesApi.categoriesIdAdminsGet(TEST_CATEGORY_ID);

    assertEquals(2, result.size());
    assertSame(user1, result.get(0));
    assertSame(user2, result.get(1));
  }

  @Test
  public void testCategoriesIdAdminsGet_whenNoAdmins_thenReturnsEmptyList() {
    when(circabcService.getCategoryAdmins(TEST_CATEGORY_REF)).thenReturn(
      Collections.emptyList()
    );

    List<User> result = categoriesApi.categoriesIdAdminsGet(TEST_CATEGORY_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- getInterestGroupByCategoryId ---

  @Test
  public void testGetInterestGroupByCategoryId_whenNotCategory_thenReturnsEmpty() {
    when(
      nodeService.hasAspect(TEST_CATEGORY_REF, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(false);

    List<InterestGroup> result = categoriesApi.getInterestGroupByCategoryId(
      TEST_CATEGORY_ID
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // --- categoriesIdAdminsDelete ---

  @Test
  public void testCategoriesIdAdminsDelete_whenNullCategoryId_thenNoOp() {
    categoriesApi.categoriesIdAdminsDelete(null, "user1");

    verifyNoInteractions(authorityService);
    verifyNoInteractions(circabcService);
  }

  // --- getInterestGroupByCategoryId (full path) ---

  @Test
  public void testGetInterestGroupByCategoryId_whenIsCategory_thenReturnsIGs() {
    when(
      nodeService.hasAspect(TEST_CATEGORY_REF, CircabcModel.ASPECT_CATEGORY)
    ).thenReturn(true);

    User user = new User();
    user.setUserId("testuser");
    user.setUiLang("en");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(user);

    InterestGroupItem igItem = mock(InterestGroupItem.class);
    when(igItem.getId()).thenReturn("00000000-0000-0000-0000-000000000050");
    when(igItem.getName()).thenReturn("TestIG");
    when(igItem.getBestTitle()).thenReturn("Test IG Title");
    when(igItem.getLogoRef()).thenReturn(null);

    when(
      circabcService.getInterestGroupByCategoryUser(
        TEST_CATEGORY_REF,
        "testuser"
      )
    ).thenReturn(Collections.singletonList(igItem));

    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "00000000-0000-0000-0000-000000000050"
    );
    java.util.Map<String, String> titleMap = new java.util.HashMap<>();
    titleMap.put("en", "Test IG Title");
    when(circabcService.getInterestGroupTitle(igRef)).thenReturn(titleMap);

    List<InterestGroup> result = categoriesApi.getInterestGroupByCategoryId(
      TEST_CATEGORY_ID
    );

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("00000000-0000-0000-0000-000000000050", result.get(0).getId());
  }

  // --- categoriesIdAdminsPost ---

  @Test
  public void testCategoriesIdAdminsPost_whenValidInput_thenAddsAdmin() {
    NodeRef adminProfileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "admin-profile"
    );
    ChildAssociationRef profileAssoc = mock(ChildAssociationRef.class);
    when(profileAssoc.getChildRef()).thenReturn(adminProfileRef);

    QName categoryAdminProfileName = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "CircaCategoryAdmin"
    );
    QName categoryProfileAssoc = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryProfileAssoc"
    );
    when(
      nodeService.getChildAssocs(
        TEST_CATEGORY_REF,
        categoryProfileAssoc,
        categoryAdminProfileName
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    QName categoryProfileGroupName = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryProfileGroupName"
    );
    when(
      nodeService.getProperty(adminProfileRef, categoryProfileGroupName)
    ).thenReturn("GROUP_catadmin");
    when(authorityService.authorityExists("GROUP_catadmin")).thenReturn(true);
    when(personService.personExists("newadmin")).thenReturn(true);

    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-ref"
    );
    when(personService.getPerson("newadmin")).thenReturn(personRef);
    when(
      nodeService.getProperty(TEST_CATEGORY_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(10L);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(20L);
    when(circabcService.isUserExists("newadmin")).thenReturn(true);

    List<String> result = categoriesApi.categoriesIdAdminsPost(
      TEST_CATEGORY_ID,
      Collections.singletonList("newadmin")
    );

    assertEquals(1, result.size());
    assertEquals("newadmin", result.get(0));
    verify(circabcDaoService).insertCategoryAdmin(any());
  }

  @Test
  public void testCategoriesIdAdminsPost_whenNullInputs_thenReturnsEmpty() {
    List<String> result = categoriesApi.categoriesIdAdminsPost(null, null);
    assertTrue(result.isEmpty());
  }

  // --- categoriesIdAdminsDelete (full path) ---

  @Test
  public void testCategoriesIdAdminsDelete_whenAuthorityExists_thenRemoves() {
    NodeRef adminProfileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "admin-profile"
    );
    ChildAssociationRef profileAssoc = mock(ChildAssociationRef.class);
    when(profileAssoc.getChildRef()).thenReturn(adminProfileRef);

    QName categoryAdminProfileName = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "CircaCategoryAdmin"
    );
    QName categoryProfileAssoc = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryProfileAssoc"
    );
    when(
      nodeService.getChildAssocs(
        TEST_CATEGORY_REF,
        categoryProfileAssoc,
        categoryAdminProfileName
      )
    ).thenReturn(Collections.singletonList(profileAssoc));

    QName categoryProfileGroupName = QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "circaCategoryProfileGroupName"
    );
    when(
      nodeService.getProperty(adminProfileRef, categoryProfileGroupName)
    ).thenReturn("GROUP_catadmin");
    when(authorityService.authorityExists("GROUP_catadmin")).thenReturn(true);

    categoriesApi.categoriesIdAdminsDelete(TEST_CATEGORY_ID, "user1");

    verify(circabcService).removeCategoryAdmin(TEST_CATEGORY_REF, "user1");
  }

  // --- categoriesIdGroupRequestsGet ---

  @Test
  public void testCategoriesIdGroupRequestsGet_whenRequestsExist_thenReturnsPaged() {
    GroupCreationRequest req = new GroupCreationRequest();
    req.setId(1);
    when(
      groupRequestsDaoService.getCategoryGroupCreationRequests(
        TEST_CATEGORY_ID,
        10,
        1,
        ""
      )
    ).thenReturn(Collections.singletonList(req));
    when(
      groupRequestsDaoService.getCountCategoryGroupCreationRequests(
        TEST_CATEGORY_ID,
        ""
      )
    ).thenReturn(1);

    PagedGroupCreationRequests result =
      categoriesApi.categoriesIdGroupRequestsGet(TEST_CATEGORY_ID, 10, 1, "");

    assertNotNull(result);
    assertEquals(Long.valueOf(1L), result.getTotal());
    assertEquals(1, result.getData().size());
  }

  @Test
  public void testCategoriesIdGroupRequestsGet_whenNoRequests_thenReturnsEmpty() {
    when(
      groupRequestsDaoService.getCategoryGroupCreationRequests(
        TEST_CATEGORY_ID,
        10,
        1,
        null
      )
    ).thenReturn(Collections.emptyList());
    when(
      groupRequestsDaoService.getCountCategoryGroupCreationRequests(
        TEST_CATEGORY_ID,
        null
      )
    ).thenReturn(0);

    PagedGroupCreationRequests result =
      categoriesApi.categoriesIdGroupRequestsGet(TEST_CATEGORY_ID, 10, 1, null);

    assertNotNull(result);
    assertEquals(Long.valueOf(0L), result.getTotal());
    assertTrue(result.getData().isEmpty());
  }

  // --- categoriesGroupRequestPut ---

  @Test
  public void testCategoriesGroupRequestPut_whenCalled_thenDelegates() {
    GroupCreationRequest body = new GroupCreationRequest();
    body.setId(1);

    GroupCreationRequest existing = new GroupCreationRequest();
    existing.setCategoryRef("cat-1");
    when(
      groupRequestsDaoService.getCategoryGroupCreationRequests("req-1")
    ).thenReturn(existing);

    categoriesApi.categoriesGroupRequestPut("cat-1", "req-1", body);

    verify(groupRequestsDaoService).putCategoryGroupCreationRequest(
      "req-1",
      body
    );
  }

  // --- getInterestGroups ---

  @Test
  public void testGetInterestGroups_whenHasIGs_thenReturnsOnlyIGs() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-ref"
    );
    NodeRef nonIgRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "non-ig-ref"
    );

    ChildAssociationRef assoc1 = mock(ChildAssociationRef.class);
    when(assoc1.getChildRef()).thenReturn(igRef);
    ChildAssociationRef assoc2 = mock(ChildAssociationRef.class);
    when(assoc2.getChildRef()).thenReturn(nonIgRef);

    when(nodeService.getChildAssocs(TEST_CATEGORY_REF)).thenReturn(
      Arrays.asList(assoc1, assoc2)
    );
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(
      nodeService.hasAspect(nonIgRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    List<NodeRef> result = categoriesApi.getInterestGroups(TEST_CATEGORY_REF);

    assertEquals(1, result.size());
    assertEquals(igRef, result.get(0));
  }

  // --- categoriesIdGroupsPost ---

  @Test(expected = IllegalArgumentException.class)
  public void testCategoriesIdGroupsPost_whenNullName_thenThrows()
    throws Exception {
    GroupsApi groupsApi = mock(GroupsApi.class);
    setField(categoriesApi, "groupsApi", groupsApi);

    InterestGroupPostModel ig = new InterestGroupPostModel();
    ig.setName(null);

    categoriesApi.categoriesIdGroupsPost(TEST_CATEGORY_ID, ig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCategoriesIdGroupsPost_whenEmptyName_thenThrows()
    throws Exception {
    GroupsApi groupsApi = mock(GroupsApi.class);
    setField(categoriesApi, "groupsApi", groupsApi);

    InterestGroupPostModel ig = new InterestGroupPostModel();
    ig.setName("");

    categoriesApi.categoriesIdGroupsPost(TEST_CATEGORY_ID, ig);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCategoriesIdGroupsPost_whenNameAlreadyExists_thenThrows()
    throws Exception {
    GroupsApi groupsApi = mock(GroupsApi.class);
    setField(categoriesApi, "groupsApi", groupsApi);

    NodeRef existingRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing"
    );
    when(
      nodeService.getChildByName(
        TEST_CATEGORY_REF,
        ContentModel.ASSOC_CONTAINS,
        "ExistingIG"
      )
    ).thenReturn(existingRef);

    InterestGroupPostModel ig = new InterestGroupPostModel();
    ig.setName("ExistingIG");

    categoriesApi.categoriesIdGroupsPost(TEST_CATEGORY_ID, ig);
  }

  // --- categoriesIdGroupRequestPost ---

  @Test
  public void testCategoriesIdGroupRequestPost_whenValid_thenSavesAndSendsEmail()
    throws Exception {
    EmailApi emailApi = mock(EmailApi.class);
    setField(categoriesApi, "emailApi", emailApi);

    when(circabcService.getCategoryAdmins(TEST_CATEGORY_REF)).thenReturn(
      Collections.singletonList("admin1")
    );
    User adminUser = new User();
    adminUser.setUserId("admin1");
    when(usersApi.usersUserIdGet("admin1")).thenReturn(adminUser);

    EmailDefinition emailDef = new EmailDefinition();
    when(emailApi.prepareEmailForGroupRequest(any(), any())).thenReturn(
      emailDef
    );

    GroupCreationRequest body = new GroupCreationRequest();
    body.setProposedName("NewGroup");

    categoriesApi.categoriesIdGroupRequestPost(TEST_CATEGORY_ID, body);

    verify(groupRequestsDaoService).saveRequest(body);
    verify(emailApi).mailPost(emailDef);
  }

  @Test
  public void testCategoriesIdGroupRequestPost_whenEmailFails_thenStillSaves()
    throws Exception {
    EmailApi emailApi = mock(EmailApi.class);
    setField(categoriesApi, "emailApi", emailApi);

    when(circabcService.getCategoryAdmins(TEST_CATEGORY_REF)).thenThrow(
      new RuntimeException("fail")
    );

    GroupCreationRequest body = new GroupCreationRequest();
    body.setProposedName("NewGroup");

    categoriesApi.categoriesIdGroupRequestPost(TEST_CATEGORY_ID, body);

    verify(groupRequestsDaoService).saveRequest(body);
  }

  // --- getCategoryLogoByCategoryId ---

  @Test
  public void testGetCategoryLogoByCategoryId_whenNoLogoFolder_thenReturnsEmpty()
    throws Exception {
    NodesApi nodesApi = mock(NodesApi.class);
    setField(categoriesApi, "nodesApi", nodesApi);

    when(nodeService.getChildAssocs(TEST_CATEGORY_REF)).thenReturn(
      Collections.emptyList()
    );

    List<Node> result = categoriesApi.getCategoryLogoByCategoryId(
      TEST_CATEGORY_ID
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetCategoryLogoByCategoryId_whenHasLogos_thenReturnsNodes()
    throws Exception {
    NodesApi nodesApi = mock(NodesApi.class);
    NodeService secureNodeService = mock(NodeService.class);
    setField(categoriesApi, "nodesApi", nodesApi);
    setField(categoriesApi, "secureNodeService", secureNodeService);

    NodeRef logoFolderRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-folder"
    );
    ChildAssociationRef logoFolderAssoc = mock(ChildAssociationRef.class);
    when(logoFolderAssoc.getChildRef()).thenReturn(logoFolderRef);
    when(logoFolderAssoc.getTypeQName()).thenReturn(
      CircabcModel.ASSOC_CATEGORY_LOGOS
    );
    when(nodeService.getChildAssocs(TEST_CATEGORY_REF)).thenReturn(
      Collections.singletonList(logoFolderAssoc)
    );

    NodeRef logoRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "logo-1"
    );
    ChildAssociationRef logoAssoc = mock(ChildAssociationRef.class);
    when(logoAssoc.getChildRef()).thenReturn(logoRef);
    when(secureNodeService.getChildAssocs(logoFolderRef)).thenReturn(
      Collections.singletonList(logoAssoc)
    );

    Node logoNode = new Node();
    when(nodesApi.getNode(logoRef)).thenReturn(logoNode);

    List<Node> result = categoriesApi.getCategoryLogoByCategoryId(
      TEST_CATEGORY_ID
    );

    assertEquals(1, result.size());
    assertSame(logoNode, result.get(0));
  }

  // --- postCategoryLogoByCategoryId ---

  @Test(expected = IllegalArgumentException.class)
  public void testPostCategoryLogoByCategoryId_whenInvalidFileType_thenThrows()
    throws Exception {
    categoriesApi.postCategoryLogoByCategoryId(
      TEST_CATEGORY_ID,
      null,
      "malware.exe"
    );
  }
}
