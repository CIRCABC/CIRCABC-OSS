package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.model.User;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class CircabcApiImplTest {

  private CircabcApiImpl circabcApi;

  private NodeService nodeService;
  private NodeLocatorService nodeLocatorService;
  private AuthorityService authorityService;
  private PersonService personService;
  private CircabcService circabcService;
  private UserService userService;
  private FileFolderService fileFolderService;
  private CategoryService categoryService;

  private static final NodeRef COMPANY_HOME = new NodeRef(
    "workspace://SpacesStore/company-home-id"
  );
  private static final NodeRef CIRCABC_NODE = new NodeRef(
    "workspace://SpacesStore/circabc-node-id"
  );

  @Before
  public void setUp() throws Exception {
    circabcApi = new CircabcApiImpl();

    nodeService = mock(NodeService.class);
    nodeLocatorService = mock(NodeLocatorService.class);
    authorityService = mock(AuthorityService.class);
    personService = mock(PersonService.class);
    circabcService = mock(CircabcService.class);
    userService = mock(UserService.class);
    fileFolderService = mock(FileFolderService.class);
    categoryService = mock(CategoryService.class);

    setField("nodeService", nodeService);
    setField("nodeLocatorService", nodeLocatorService);
    setField("authorityService", authorityService);
    setField("personService", personService);
    setField("circabcService", circabcService);
    setField("userService", userService);
    setField("fileFolderService", fileFolderService);
    setField("categoryService", categoryService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(circabcApi, value);
  }

  @Test
  public void testGetCircabcAdmins_whenAdminGroupHasUsers_thenReturnsUserList()
    throws Exception {
    setField("circabcNodeRef", CIRCABC_NODE);
    setField("circabcAdminGroup", "GROUP_CircaBCAdmin");
    setField("circabcInvitedUsersGroup", "GROUP_CircaBCInvited");

    when(nodeService.exists(CIRCABC_NODE)).thenReturn(true);
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCMaster");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCSubs");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCInvited");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCAdmin");

    Set<String> authorities = new HashSet<>();
    authorities.add("user1");

    when(
      authorityService.getContainedAuthorities(
        AuthorityType.USER,
        "GROUP_CircaBCAdmin",
        true
      )
    ).thenReturn(authorities);

    NodeRef personRef = new NodeRef("workspace://SpacesStore/person-1");
    when(personService.getPerson("user1")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_USERNAME)
    ).thenReturn("user1");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME)
    ).thenReturn("John");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME)
    ).thenReturn("Doe");
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("john@example.com");

    List<User> admins = circabcApi.getCircabcAdmins();

    assertEquals(1, admins.size());
    assertEquals("user1", admins.get(0).getUserId());
    assertEquals("John", admins.get(0).getFirstname());
    assertEquals("Doe", admins.get(0).getLastname());
    assertEquals("john@example.com", admins.get(0).getEmail());
  }

  @Test
  public void testGetCircabcAdmins_whenAdminGroupIsNull_thenReturnsEmptyList()
    throws Exception {
    setField("circabcNodeRef", CIRCABC_NODE);
    setField("circabcAdminGroup", null);
    setField("circabcInvitedUsersGroup", "GROUP_CircaBCInvited");

    when(nodeService.exists(CIRCABC_NODE)).thenReturn(true);
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCMaster");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCSubs");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCInvited");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY
      )
    ).thenReturn(null);

    List<User> admins = circabcApi.getCircabcAdmins();

    assertTrue(admins.isEmpty());
  }

  @Test
  public void testCircabcAdminsUserIdDelete_whenUserIsAdmin_thenRemovesAuthority()
    throws Exception {
    setField("circabcNodeRef", CIRCABC_NODE);
    setField("circabcAdminGroup", "GROUP_CircaBCAdmin");
    setField("circabcInvitedUsersGroup", "GROUP_CircaBCInvited");

    when(nodeService.exists(CIRCABC_NODE)).thenReturn(true);
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCMaster");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCSubs");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCInvited");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCAdmin");
    when(circabcService.isCircabcAdmin("user1")).thenReturn(true);

    circabcApi.circabcAdminsUserIdDelete("user1");

    verify(authorityService).removeAuthority("GROUP_CircaBCAdmin", "user1");
    verify(circabcService).removeCircabcAdmin("user1");
  }

  @Test
  public void testCircabcAdminsUserIdDelete_whenUserIsNotAdmin_thenOnlyRemovesAuthority()
    throws Exception {
    setField("circabcNodeRef", CIRCABC_NODE);
    setField("circabcAdminGroup", "GROUP_CircaBCAdmin");
    setField("circabcInvitedUsersGroup", "GROUP_CircaBCInvited");

    when(nodeService.exists(CIRCABC_NODE)).thenReturn(true);
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCMaster");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCSubs");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCInvited");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCAdmin");
    when(circabcService.isCircabcAdmin("user1")).thenReturn(false);

    circabcApi.circabcAdminsUserIdDelete("user1");

    verify(authorityService).removeAuthority("GROUP_CircaBCAdmin", "user1");
    verify(circabcService, never()).removeCircabcAdmin("user1");
  }

  @Test
  public void testGetCompanyHomeNodeRef_returnsFirstChildOfRoot() {
    NodeRef rootNode = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef expectedCompanyHome = new NodeRef(
      "workspace://SpacesStore/company-home"
    );

    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(rootNode);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(expectedCompanyHome);

    List<ChildAssociationRef> children = new ArrayList<>();
    children.add(childAssoc);
    when(nodeService.getChildAssocs(rootNode)).thenReturn(children);

    NodeRef result = circabcApi.getCompanyHomeNodeRef();

    assertEquals(expectedCompanyHome, result);
  }

  @Test
  public void testGetGuestHomeNodeRef_returnsGuestHomeChild() {
    NodeRef rootNode = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef companyHome = new NodeRef("workspace://SpacesStore/company-home");
    NodeRef guestHome = new NodeRef("workspace://SpacesStore/guest-home");

    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(rootNode);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(companyHome);
    List<ChildAssociationRef> children = new ArrayList<>();
    children.add(childAssoc);
    when(nodeService.getChildAssocs(rootNode)).thenReturn(children);

    when(
      nodeService.getChildByName(
        companyHome,
        ContentModel.ASSOC_CONTAINS,
        "Guest Home"
      )
    ).thenReturn(guestHome);

    NodeRef result = circabcApi.getGuestHomeNodeRef();

    assertEquals(guestHome, result);
  }

  @Test
  public void testGetCircabcNodeRef_whenAlreadySet_returnsCachedValue()
    throws Exception {
    setField("circabcNodeRef", CIRCABC_NODE);

    NodeRef result = circabcApi.getCircabcNodeRef();

    assertEquals(CIRCABC_NODE, result);
    verifyNoInteractions(nodeLocatorService);
  }

  @Test
  public void testGetCircabcNodeRef_whenNull_looksUpByName() throws Exception {
    when(nodeLocatorService.getNode("companyhome", null, null)).thenReturn(
      COMPANY_HOME
    );

    NodeRef circabcChild = new NodeRef("workspace://SpacesStore/circabc-found");
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(circabcChild);

    List<ChildAssociationRef> children = new ArrayList<>();
    children.add(childAssoc);
    when(nodeService.getChildAssocs(COMPANY_HOME)).thenReturn(children);
    when(
      nodeService.getProperty(circabcChild, ContentModel.PROP_NAME)
    ).thenReturn("CircaBC");

    NodeRef result = circabcApi.getCircabcNodeRef();

    assertEquals(circabcChild, result);
  }

  @Test
  public void testGetCircabcDictionaryNodeRef_whenExists_returnsExisting()
    throws Exception {
    NodeRef rootNode = new NodeRef("workspace://SpacesStore/root-id");
    NodeRef companyHome = new NodeRef("workspace://SpacesStore/company-home");
    NodeRef dataDictionary = new NodeRef(
      "workspace://SpacesStore/data-dictionary"
    );
    NodeRef circabcDict = new NodeRef(
      "workspace://SpacesStore/circabc-dictionary"
    );

    when(
      nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
    ).thenReturn(rootNode);

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(companyHome);
    List<ChildAssociationRef> children = new ArrayList<>();
    children.add(childAssoc);
    when(nodeService.getChildAssocs(rootNode)).thenReturn(children);

    when(
      nodeService.getChildByName(
        companyHome,
        ContentModel.ASSOC_CONTAINS,
        "Data Dictionary"
      )
    ).thenReturn(dataDictionary);
    when(
      nodeService.getChildByName(
        dataDictionary,
        ContentModel.ASSOC_CONTAINS,
        "CircaBC"
      )
    ).thenReturn(circabcDict);

    NodeRef result = circabcApi.getCircabcDictionaryNodeRef();

    assertEquals(circabcDict, result);
    verifyNoInteractions(fileFolderService);
  }

  @Test
  public void testGetInvitedUsersGroupName_returnsGroupName() throws Exception {
    setField("circabcNodeRef", CIRCABC_NODE);
    setField("circabcInvitedUsersGroup", "GROUP_CircaBCInvited");

    when(nodeService.exists(CIRCABC_NODE)).thenReturn(true);
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_MASTER_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCMaster");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_SUBS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCSubs");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_INVITED_USERS_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCInvited");
    when(
      nodeService.getProperty(
        CIRCABC_NODE,
        CircabcModel.CIRCA_BC_ADMIN_GROUP_PROPERTY
      )
    ).thenReturn("CircaBCAdmin");

    String result = circabcApi.getInvitedUsersGroupName();

    assertNotNull(result);
    assertEquals("GROUP_CircaBCInvited", result);
  }

  @Test
  public void testGetRootCategoryHeader_whenFound_returnsNodeRef() {
    NodeRef headerNode = new NodeRef(
      "workspace://SpacesStore/circabc-header-id"
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(headerNode);

    List<ChildAssociationRef> rootCategories = new ArrayList<>();
    rootCategories.add(childAssoc);

    when(
      categoryService.getCategories(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        ContentModel.ASPECT_GEN_CLASSIFIABLE,
        CategoryService.Depth.IMMEDIATE
      )
    ).thenReturn(rootCategories);
    when(
      nodeService.getProperty(headerNode, ContentModel.PROP_NAME)
    ).thenReturn("CircaBCHeader");

    NodeRef result = circabcApi.getRootCategoryHeader();

    assertEquals(headerNode, result);
  }

  @Test
  public void testGetRootCategoryHeader_whenNotFound_returnsNull() {
    NodeRef otherNode = new NodeRef("workspace://SpacesStore/other-id");
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(otherNode);

    List<ChildAssociationRef> rootCategories = new ArrayList<>();
    rootCategories.add(childAssoc);

    when(
      categoryService.getCategories(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
        ContentModel.ASPECT_GEN_CLASSIFIABLE,
        CategoryService.Depth.IMMEDIATE
      )
    ).thenReturn(rootCategories);
    when(nodeService.getProperty(otherNode, ContentModel.PROP_NAME)).thenReturn(
      "SomethingElse"
    );

    NodeRef result = circabcApi.getRootCategoryHeader();

    assertNull(result);
  }

  @Test
  public void testGetRootCategoryHeader_whenCached_returnsCachedValue()
    throws Exception {
    NodeRef cachedRef = new NodeRef("workspace://SpacesStore/cached-header");
    setField("circabcCategoryRoot", cachedRef);

    NodeRef result = circabcApi.getRootCategoryHeader();

    assertEquals(cachedRef, result);
    verifyNoInteractions(categoryService);
  }
}
