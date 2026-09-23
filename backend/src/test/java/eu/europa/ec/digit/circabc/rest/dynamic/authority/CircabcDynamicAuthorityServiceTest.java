package eu.europa.ec.digit.circabc.rest.dynamic.authority;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.junit.Before;
import org.junit.Test;

public class CircabcDynamicAuthorityServiceTest {

  private CircabcDynamicAuthorityServiceImpl service;
  private NodeService nodeService;
  private MultilingualContentService multilingualContentService;
  private CircabcDynamicAuthorityDAO circabcDAO;
  private NamespaceService namespaceService;

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

    service = new CircabcDynamicAuthorityServiceImpl();
    nodeService = mock(NodeService.class);
    multilingualContentService = mock(MultilingualContentService.class);
    circabcDAO = mock(CircabcDynamicAuthorityDAO.class);
    namespaceService = mock(NamespaceService.class);

    setField("nodeService", nodeService);
    setField("multilingualContentService", multilingualContentService);
    setField("circabcDAO", circabcDAO);
    setField("namespaceService", namespaceService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = CircabcDynamicAuthorityServiceImpl.class.getDeclaredField(
      fieldName
    );
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testIsGroupMember_whenPermissionsExist_thenTrue() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    CircabcPermission perm = new CircabcPermission();
    perm.setLibraryPermission("LibAccess");
    when(circabcDAO.getGroupPermission(group.toString(), "user1")).thenReturn(
      Arrays.asList(perm)
    );

    assertTrue(service.isGroupMember(group, "user1"));
  }

  @Test
  public void testIsGroupMember_whenNoPermissions_thenFalse() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.getGroupPermission(group.toString(), "user1")).thenReturn(
      Collections.emptyList()
    );

    assertFalse(service.isGroupMember(group, "user1"));
  }

  @Test
  public void testIsGroupMember_whenNull_thenFalse() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.getGroupPermission(group.toString(), "user1")).thenReturn(
      null
    );

    assertFalse(service.isGroupMember(group, "user1"));
  }

  @Test
  public void testIsCategoryAdmin_whenTrue_thenTrue() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.isCategoryAdmin(group.toString(), "admin")).thenReturn(
      true
    );

    assertTrue(service.isCategoryAdmin(group, "admin"));
  }

  @Test
  public void testIsCategoryAdmin_whenFalse_thenFalse() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.isCategoryAdmin(group.toString(), "user1")).thenReturn(
      false
    );

    assertFalse(service.isCategoryAdmin(group, "user1"));
  }

  @Test
  public void testIsAdmin_whenCategoryAdmin_thenTrue() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.isCategoryAdmin(group.toString(), "user1")).thenReturn(
      true
    );

    assertTrue(service.isAdmin(group, "user1", CircabcServiceType.LIBRARY));
  }

  @Test
  public void testIsAdmin_whenLibAdmin_thenTrue() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.isCategoryAdmin(group.toString(), "user1")).thenReturn(
      false
    );
    CircabcPermission perm = new CircabcPermission();
    perm.setLibraryPermission("LibAdmin");
    perm.setNewsGroupPermission("NwsAccess");
    perm.setInformationPermission("InfAccess");
    when(circabcDAO.getGroupPermission(group.toString(), "user1")).thenReturn(
      Arrays.asList(perm)
    );

    assertTrue(service.isAdmin(group, "user1", CircabcServiceType.LIBRARY));
  }

  @Test
  public void testIsAdmin_whenNwsAdmin_thenTrue() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.isCategoryAdmin(group.toString(), "user1")).thenReturn(
      false
    );
    CircabcPermission perm = new CircabcPermission();
    perm.setLibraryPermission("LibAccess");
    perm.setNewsGroupPermission("NwsAdmin");
    perm.setInformationPermission("InfAccess");
    when(circabcDAO.getGroupPermission(group.toString(), "user1")).thenReturn(
      Arrays.asList(perm)
    );

    assertTrue(service.isAdmin(group, "user1", CircabcServiceType.NEWSGROUP));
  }

  @Test
  public void testIsAdmin_whenNoAdminPermission_thenFalse() {
    NodeRef group = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(circabcDAO.isCategoryAdmin(group.toString(), "user1")).thenReturn(
      false
    );
    CircabcPermission perm = new CircabcPermission();
    perm.setLibraryPermission("LibAccess");
    perm.setNewsGroupPermission("NwsAccess");
    perm.setInformationPermission("InfAccess");
    when(circabcDAO.getGroupPermission(group.toString(), "user1")).thenReturn(
      Arrays.asList(perm)
    );

    assertFalse(service.isAdmin(group, "user1", CircabcServiceType.LIBRARY));
  }

  @Test
  public void testIsCircabcNode_whenHasManagementAspect_thenTrue() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT)
    ).thenReturn(true);

    assertTrue(service.isCircabcNode(nodeRef));
  }

  @Test
  public void testIsCircabcNode_whenNoAspectAndGetPathThrows_thenFalse() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT)
    ).thenReturn(false);
    when(nodeService.getPath(nodeRef)).thenThrow(
      new RuntimeException("node not found")
    );

    assertFalse(service.isCircabcNode(nodeRef));
  }

  @Test
  public void testFindServiceType_whenLibrary_thenLibrary() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);

    assertEquals(CircabcServiceType.LIBRARY, service.findServiceType(nodeRef));
  }

  @Test
  public void testFindServiceType_whenNewsgroup_thenNewsgroup() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);

    assertEquals(
      CircabcServiceType.NEWSGROUP,
      service.findServiceType(nodeRef)
    );
  }

  @Test
  public void testFindServiceType_whenNoAspect_thenUnknown() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);

    assertEquals(CircabcServiceType.UNKNOWN, service.findServiceType(nodeRef));
  }

  @Test
  public void testFindGroup_whenNodeIsIgRoot_thenReturnsNode() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    assertEquals(nodeRef, service.findGroup(nodeRef));
  }

  @Test
  public void testFindGroup_whenParentIsIgRoot_thenReturnsParent() {
    NodeRef child = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child"
    );
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent"
    );

    when(nodeService.getType(child)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.hasAspect(child, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(parent);
    when(nodeService.getPrimaryParent(child)).thenReturn(childAssoc);

    when(nodeService.getType(parent)).thenReturn(ContentModel.TYPE_FOLDER);
    when(nodeService.hasAspect(parent, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    assertEquals(parent, service.findGroup(child));
  }

  @Test
  public void testFindGroup_whenNoIgRoot_thenReturnsNull() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      false
    );
    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getParentRef()).thenReturn(null);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssoc);

    assertNull(service.findGroup(nodeRef));
  }
}
