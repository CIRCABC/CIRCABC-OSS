package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import io.swagger.model.Category;
import io.swagger.model.Header;
import io.swagger.model.I18nProperty;
import io.swagger.model.User;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.junit.Before;
import org.junit.Test;

public class HeadersApiImplTest {

  private HeadersApiImpl headersApi;
  private NodeService nodeService;
  private NodeService secureNodeService;
  private CircabcService circabcService;
  private CircabcDaoServiceImpl circabcDaoServiceImpl;
  private UsersApi usersApi;
  private CategoryService categoryService;

  private static final String TEST_ID = "test-header-id";
  private static final NodeRef TEST_NODE_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_ID
  );
  private static final NodeRef CIRCABC_ROOT = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    "circabc-root-id"
  );

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

    headersApi = new HeadersApiImpl();
    nodeService = mock(NodeService.class);
    secureNodeService = mock(NodeService.class);
    circabcService = mock(CircabcService.class);
    circabcDaoServiceImpl = mock(CircabcDaoServiceImpl.class);
    usersApi = mock(UsersApi.class);
    categoryService = mock(CategoryService.class);

    // Use setters for non-autowired fields
    headersApi.setNodeService(nodeService);
    headersApi.setSecureNodeService(secureNodeService);

    // Use reflection for @Autowired fields
    setField("circabcService", circabcService);
    setField("circabcDaoServiceImpl", circabcDaoServiceImpl);
    setField("usersApi", usersApi);
    setField("categoryService", categoryService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HeadersApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(headersApi, value);
  }

  private void setupCircabcCategoryRoot() throws Exception {
    setField("circabcCategoryRoot", CIRCABC_ROOT);
  }

  private void mockIsHeader(NodeRef nodeRef) {
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(CIRCABC_ROOT);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(parentAssoc);
  }

  @Test
  public void testGetHeader_whenValidHeader_thenReturnsHeader()
    throws Exception {
    setupCircabcCategoryRoot();
    mockIsHeader(TEST_NODE_REF);

    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestHeader");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("A description");
    when(
      nodeService.getProperty(TEST_NODE_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);

    User user = new User();
    user.setUiLang("en");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(user);
    when(circabcService.getUserLocaleID("testuser")).thenReturn(1L);
    when(
      circabcDaoServiceImpl.selectCategoriesByHeaderLocale(100L, 1L)
    ).thenReturn(new ArrayList<>());

    Header result = headersApi.getHeader(TEST_ID);

    assertNotNull(result);
    assertEquals(TEST_ID, result.getId());
    assertEquals("TestHeader", result.getName());
    assertNotNull(result.getDescription());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetHeader_whenNotAHeader_thenThrowsException()
    throws Exception {
    setupCircabcCategoryRoot();

    NodeRef otherParent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-parent"
    );
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(otherParent);
    when(nodeService.getPrimaryParent(TEST_NODE_REF)).thenReturn(parentAssoc);

    headersApi.getHeader(TEST_ID);
  }

  @Test
  public void testGetHeaders_whenHeadersExist_thenReturnsList()
    throws Exception {
    io.swagger.model.db.Header dbHeader = new io.swagger.model.db.Header();
    dbHeader.setNodeRef("workspace://SpacesStore/header-1");
    dbHeader.setName("Header1");
    dbHeader.setDescription("Desc1");

    List<io.swagger.model.db.Header> dbHeaders = new ArrayList<>();
    dbHeaders.add(dbHeader);
    when(circabcDaoServiceImpl.selectHeaders()).thenReturn(dbHeaders);

    List<Header> result = headersApi.getHeaders(null, null);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Header1", result.get(0).getName());
  }

  @Test
  public void testGetHeaders_whenExceptionOccurs_thenReturnsEmptyList()
    throws Exception {
    when(circabcDaoServiceImpl.selectHeaders()).thenThrow(
      new RuntimeException("DB error")
    );

    List<Header> result = headersApi.getHeaders(null, null);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testPostHeader_whenValid_thenCreatesHeader() throws Exception {
    // Setup circabcCategoryRoot via initCircabcCategoryRoot
    setupCircabcCategoryRoot();

    Header body = new Header();
    body.setName("NewHeader");
    I18nProperty desc = new I18nProperty("en", "New description");
    body.setDescription(desc);

    NodeRef createdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-header-id"
    );
    when(categoryService.createCategory(CIRCABC_ROOT, "NewHeader")).thenReturn(
      createdRef
    );

    Header result = headersApi.postHeader(body);

    assertNotNull(result);
    assertEquals("new-header-id", result.getId());
    assertEquals("NewHeader", result.getName());
    assertEquals(desc, result.getDescription());
    verify(secureNodeService).setProperty(
      eq(createdRef),
      eq(ContentModel.PROP_DESCRIPTION),
      any()
    );
  }

  @Test
  public void testPutHeader_whenValidHeader_thenUpdatesHeader()
    throws Exception {
    setupCircabcCategoryRoot();
    mockIsHeader(TEST_NODE_REF);

    Header body = new Header();
    body.setName("UpdatedName");
    I18nProperty desc = new I18nProperty("en", "Updated desc");
    body.setDescription(desc);

    Header result = headersApi.putHeader(TEST_ID, body);

    assertNotNull(result);
    assertEquals(TEST_ID, result.getId());
    assertEquals("UpdatedName", result.getName());
    assertEquals(desc, result.getDescription());
    verify(secureNodeService).setProperty(
      eq(TEST_NODE_REF),
      eq(ContentModel.PROP_DESCRIPTION),
      any()
    );
    verify(secureNodeService).setProperty(
      TEST_NODE_REF,
      ContentModel.PROP_NAME,
      "UpdatedName"
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPutHeader_whenNotAHeader_thenThrowsException()
    throws Exception {
    setupCircabcCategoryRoot();

    NodeRef otherParent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-parent"
    );
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(otherParent);
    when(nodeService.getPrimaryParent(TEST_NODE_REF)).thenReturn(parentAssoc);

    headersApi.putHeader(TEST_ID, new Header());
  }

  @Test
  public void testDeleteHeader_whenEmptyHeader_thenDeletes() throws Exception {
    setupCircabcCategoryRoot();
    mockIsHeader(TEST_NODE_REF);

    when(
      categoryService.getChildren(TEST_NODE_REF, Mode.MEMBERS, Depth.IMMEDIATE)
    ).thenReturn(Collections.emptyList());

    headersApi.deleteHeader(TEST_ID);

    verify(categoryService).deleteCategory(TEST_NODE_REF);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteHeader_whenHeaderNotEmpty_thenThrowsException()
    throws Exception {
    setupCircabcCategoryRoot();
    mockIsHeader(TEST_NODE_REF);

    ChildAssociationRef childRef = mock(ChildAssociationRef.class);
    when(
      categoryService.getChildren(TEST_NODE_REF, Mode.MEMBERS, Depth.IMMEDIATE)
    ).thenReturn(List.of(childRef));

    headersApi.deleteHeader(TEST_ID);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteHeader_whenNotAHeader_thenThrowsException()
    throws Exception {
    setupCircabcCategoryRoot();

    NodeRef otherParent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-parent"
    );
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(otherParent);
    when(nodeService.getPrimaryParent(TEST_NODE_REF)).thenReturn(parentAssoc);

    headersApi.deleteHeader(TEST_ID);
  }

  @Test
  public void testGetHeaderByCategory_whenCategoryHasHeader_thenReturnsHeader()
    throws Exception {
    setupCircabcCategoryRoot();

    String categoryId = "category-id";
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      categoryId
    );
    NodeRef headerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "linked-header-id"
    );

    List<NodeRef> categories = new ArrayList<>();
    categories.add(headerRef);
    when(
      nodeService.getProperty(categoryRef, ContentModel.PROP_CATEGORIES)
    ).thenReturn((java.io.Serializable) categories);

    // Mock getHeader for the linked header
    mockIsHeader(headerRef);
    when(nodeService.getProperty(headerRef, ContentModel.PROP_NAME)).thenReturn(
      "LinkedHeader"
    );
    when(
      nodeService.getProperty(headerRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("desc");
    when(
      nodeService.getProperty(headerRef, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);

    User user = new User();
    user.setUiLang("en");
    when(usersApi.usersUserIdGet("testuser")).thenReturn(user);
    when(circabcService.getUserLocaleID("testuser")).thenReturn(1L);
    when(
      circabcDaoServiceImpl.selectCategoriesByHeaderLocale(200L, 1L)
    ).thenReturn(new ArrayList<>());

    Header result = headersApi.getHeaderByCategory(categoryId);

    assertNotNull(result);
    assertEquals("linked-header-id", result.getId());
    assertEquals("LinkedHeader", result.getName());
  }

  @Test
  public void testGetHeaderByCategory_whenNoCategoriesProperty_thenReturnsNull()
    throws Exception {
    String categoryId = "category-id";
    NodeRef categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      categoryId
    );

    when(
      nodeService.getProperty(categoryRef, ContentModel.PROP_CATEGORIES)
    ).thenReturn(null);

    Header result = headersApi.getHeaderByCategory(categoryId);

    assertNull(result);
  }
}
