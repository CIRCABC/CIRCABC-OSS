package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.CircabcServiceName;
import io.swagger.model.InterestGroup;
import io.swagger.model.Node;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class NodesApiImplTest {

  private NodesApiImpl nodesApi;

  private NodeService nodeService;
  private NodeService secureNodeService;
  private PermissionService permissionService;
  private OwnableService ownableService;
  private ApiToolBox apiToolBox;
  private LockService lockService;
  private MimetypeService mimetypeService;
  private MultilingualContentService multilingualContentService;
  private CheckOutCheckInService checkOutCheckInService;
  private AuthorityService authorityService;
  private FavouritesService favouritesService;
  private GroupsApi groupsApi;
  private SearchService searchService;

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

    nodesApi = spy(new NodesApiImpl());

    nodeService = mock(NodeService.class);
    secureNodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    ownableService = mock(OwnableService.class);
    apiToolBox = mock(ApiToolBox.class);
    lockService = mock(LockService.class);
    mimetypeService = mock(MimetypeService.class);
    multilingualContentService = mock(MultilingualContentService.class);
    checkOutCheckInService = mock(CheckOutCheckInService.class);
    authorityService = mock(AuthorityService.class);
    favouritesService = mock(FavouritesService.class);
    groupsApi = mock(GroupsApi.class);
    searchService = mock(SearchService.class);

    setField("nodeService", nodeService);
    setField("secureNodeService", secureNodeService);
    setField("permissionService", permissionService);
    setField("ownableService", ownableService);
    setField("apiToolBox", apiToolBox);
    setField("lockService", lockService);
    setField("mimetypeService", mimetypeService);
    setField("multilingualContentService", multilingualContentService);
    setField("checkOutCheckInService", checkOutCheckInService);
    setField("authorityService", authorityService);
    setField("favouritesService", favouritesService);
    setField("groupsApi", groupsApi);
    setField("searchService", searchService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = NodesApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(nodesApi, value);
  }

  // ==================== originalNodeRef ====================

  private static final String OLD_REF = "workspace://SpacesStore/old-uuid-1";

  @Test
  public void testGetOriginalNodeRef_whenPresent_thenReturnsValue() {
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1");
    when(nodeService.exists(ref)).thenReturn(true);
    when(
      nodeService.getProperty(ref, CircabcModel.PROP_ORIGINAL_NODE_REF)
    ).thenReturn(OLD_REF);

    assertEquals(OLD_REF, nodesApi.getOriginalNodeRef("n1"));
  }

  @Test
  public void testGetOriginalNodeRef_whenAbsent_thenReturnsNull() {
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1");
    when(nodeService.exists(ref)).thenReturn(true);
    when(
      nodeService.getProperty(ref, CircabcModel.PROP_ORIGINAL_NODE_REF)
    ).thenReturn(null);

    assertNull(nodesApi.getOriginalNodeRef("n1"));
  }

  @Test(expected = InvalidNodeRefException.class)
  public void testGetOriginalNodeRef_whenNodeMissing_thenThrows() {
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1");
    when(nodeService.exists(ref)).thenReturn(false);
    nodesApi.getOriginalNodeRef("n1");
  }

  @Test
  public void testSetOriginalNodeRef_appliesAspectAndSetsProperty() {
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1");
    when(nodeService.exists(ref)).thenReturn(true);
    Node stub = new Node();
    doReturn(stub).when(nodesApi).getNode(ref);

    Node result = nodesApi.setOriginalNodeRef("n1", OLD_REF);

    assertSame(stub, result);
    verify(nodeService).addAspect(
      eq(ref),
      eq(CircabcModel.ASPECT_MIGRATED),
      anyMap()
    );
    verify(nodeService).setProperty(
      ref,
      CircabcModel.PROP_ORIGINAL_NODE_REF,
      OLD_REF
    );
  }

  @Test
  public void testDeleteOriginalNodeRef_removesAspectWhenPresent() {
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1");
    when(nodeService.exists(ref)).thenReturn(true);
    when(nodeService.hasAspect(ref, CircabcModel.ASPECT_MIGRATED)).thenReturn(
      true
    );

    nodesApi.deleteOriginalNodeRef("n1");

    verify(nodeService).removeAspect(ref, CircabcModel.ASPECT_MIGRATED);
  }

  @Test
  public void testDeleteOriginalNodeRef_whenNoAspect_thenNoRemove() {
    NodeRef ref = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "n1");
    when(nodeService.exists(ref)).thenReturn(true);
    when(nodeService.hasAspect(ref, CircabcModel.ASPECT_MIGRATED)).thenReturn(
      false
    );

    nodesApi.deleteOriginalNodeRef("n1");

    verify(nodeService, never()).removeAspect(
      ref,
      CircabcModel.ASPECT_MIGRATED
    );
  }

  @Test
  public void testResolveByOriginalNodeRef_whenFoundAndReadable_thenReturnsNode() {
    String originalId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    NodeRef found = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-1"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(found));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);
    when(nodeService.exists(found)).thenReturn(true);
    when(
      permissionService.hasPermission(found, PermissionService.READ)
    ).thenReturn(AccessStatus.ALLOWED);
    Node stub = new Node();
    doReturn(stub).when(nodesApi).getNode(found);

    assertSame(stub, nodesApi.resolveByOriginalNodeRef(originalId));
  }

  @Test
  public void testResolveByOriginalNodeRef_whenNotFound_thenReturnsNull() {
    String originalId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.emptyList());
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    assertNull(nodesApi.resolveByOriginalNodeRef(originalId));
  }

  @Test(expected = AccessDeniedException.class)
  public void testResolveByOriginalNodeRef_whenFoundButNoRead_thenThrows() {
    String originalId = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    NodeRef found = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-1"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(found));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);
    when(nodeService.exists(found)).thenReturn(true);
    when(
      permissionService.hasPermission(found, PermissionService.READ)
    ).thenReturn(AccessStatus.DENIED);

    nodesApi.resolveByOriginalNodeRef(originalId);
  }

  @Test
  public void testGetNode_whenFolderNode_thenReturnsPopulatedNode() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "test-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "TestFolder"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)).thenReturn(
      false
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(null);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "TestFolder");
    when(nodeService.getProperties(nodeRef)).thenReturn(props);
    when(ownableService.hasOwner(nodeRef)).thenReturn(false);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(false);
    when(favouritesService.isFavourite("testuser", nodeRef)).thenReturn(false);
    when(
      nodeService.getChildAssocs(nodeRef, Set.of(ContentModel.TYPE_FOLDER))
    ).thenReturn(Collections.emptyList());

    Node result = nodesApi.getNode(nodeRef);

    assertEquals("test-id", result.getId());
    assertEquals("TestFolder", result.getName());
    assertEquals(ContentModel.TYPE_FOLDER.toString(), result.getType());
    assertEquals("parent-id", result.getParentId());
    assertEquals(CircabcServiceName.LIBRARY, result.getService());
    assertFalse(result.getHasSubFolders());
  }

  @Test
  public void testGetNode_whenInformationService_thenServiceSetCorrectly() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "info-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "InfoNode"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(true);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getProperties(nodeRef)).thenReturn(new HashMap<>());
    when(ownableService.hasOwner(nodeRef)).thenReturn(false);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(false);
    when(favouritesService.isFavourite("testuser", nodeRef)).thenReturn(false);
    when(
      nodeService.getChildAssocs(nodeRef, Set.of(ContentModel.TYPE_FOLDER))
    ).thenReturn(Collections.emptyList());

    Node result = nodesApi.getNode(nodeRef);

    assertEquals(CircabcServiceName.INFORMATION, result.getService());
  }

  @Test
  public void testGetNodeById_whenValidId_thenReturnsNode() {
    String id = "some-node-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "SomeNode"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)).thenReturn(
      false
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getProperties(nodeRef)).thenReturn(new HashMap<>());
    when(ownableService.hasOwner(nodeRef)).thenReturn(false);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(false);
    when(favouritesService.isFavourite("testuser", nodeRef)).thenReturn(false);
    when(
      nodeService.getChildAssocs(nodeRef, Set.of(ContentModel.TYPE_FOLDER))
    ).thenReturn(Collections.emptyList());

    Node result = nodesApi.getNodeById(id);

    assertNotNull(result);
    assertEquals(id, result.getId());
  }

  @Test
  public void testGetFileNameExtension_whenHasExtension_thenReturnsExtension() {
    assertEquals("pdf", nodesApi.getFileNameExtension("document.pdf"));
    assertEquals("txt", nodesApi.getFileNameExtension("file.name.txt"));
  }

  @Test
  public void testGetFileNameExtension_whenNoExtension_thenReturnsNull() {
    assertNull(nodesApi.getFileNameExtension("noextension"));
  }

  @Test
  public void testRemoveFileNameExtension_whenHasExtension_thenReturnsNameWithoutExtension() {
    assertEquals("document", nodesApi.removeFileNameExtension("document.pdf"));
    assertEquals(
      "file.name",
      nodesApi.removeFileNameExtension("file.name.txt")
    );
  }

  @Test
  public void testRemoveFileNameExtension_whenNoExtension_thenReturnsOriginal() {
    assertEquals(
      "noextension",
      nodesApi.removeFileNameExtension("noextension")
    );
  }

  @Test
  public void testGenerateUniqueName_whenNameNotTaken_thenReturnsCandidateName() {
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    when(
      secureNodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file.txt"
      )
    ).thenReturn(null);

    String result = nodesApi.generateUniqueName(parent, "file.txt");

    assertEquals("file.txt", result);
  }

  @Test
  public void testGenerateUniqueName_whenNameTaken_thenReturnsIncrementedName() {
    NodeRef parent = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef existingNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing-id"
    );

    when(
      secureNodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file.txt"
      )
    ).thenReturn(existingNode);
    when(
      secureNodeService.getChildByName(
        parent,
        ContentModel.ASSOC_CONTAINS,
        "file (1).txt"
      )
    ).thenReturn(null);

    String result = nodesApi.generateUniqueName(parent, "file.txt");

    assertEquals("file (1).txt", result);
  }

  @Test
  public void testNodesIdOwnershipPut_whenCalled_thenTakesOwnership() {
    String id = "ownership-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "OwnedNode"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)).thenReturn(
      false
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getProperties(nodeRef)).thenReturn(new HashMap<>());
    when(ownableService.hasOwner(nodeRef)).thenReturn(false);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(false);
    when(favouritesService.isFavourite("testuser", nodeRef)).thenReturn(false);
    when(
      nodeService.getChildAssocs(nodeRef, Set.of(ContentModel.TYPE_FOLDER))
    ).thenReturn(Collections.emptyList());

    Node result = nodesApi.nodesIdOwnershipPut(id);

    verify(ownableService).takeOwnership(nodeRef);
    assertNotNull(result);
    assertEquals(id, result.getId());
  }

  @Test
  public void testGetNode_whenForumType_thenChecksForumSubFolders() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "ForumNode"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ForumModel.TYPE_FORUM);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)).thenReturn(
      false
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);
    when(apiToolBox.getCurrentNewsgroupRoot(nodeRef)).thenReturn(null);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getProperties(nodeRef)).thenReturn(new HashMap<>());
    when(ownableService.hasOwner(nodeRef)).thenReturn(false);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(true);
    when(
      nodeService.getChildAssocs(nodeRef, Set.of(ForumModel.TYPE_FORUM))
    ).thenReturn(Collections.emptyList());

    Node result = nodesApi.getNode(nodeRef);

    assertEquals(CircabcServiceName.NEWSGROUPS, result.getService());
    assertFalse(result.getHasSubFolders());
  }

  // --- getNode with content type ---

  @Test
  public void testGetNode_whenContentType_thenPopulatesContentProperties() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "document.pdf"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_CONTENT);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(apiToolBox.getCurrentLibraryRoot(nodeRef)).thenReturn(null);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getProperties(nodeRef)).thenReturn(new HashMap<>());
    when(ownableService.hasOwner(nodeRef)).thenReturn(false);
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());

    org.alfresco.service.cmr.repository.ContentData contentData =
      new org.alfresco.service.cmr.repository.ContentData(
        "content://url",
        "application/pdf",
        1024L,
        "UTF-8"
      );
    when(
      nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT)
    ).thenReturn(contentData);
    when(mimetypeService.getDisplaysByMimetype()).thenReturn(
      java.util.Map.of("application/pdf", "PDF Document")
    );
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)
    ).thenReturn(false);
    when(lockService.getLockType(nodeRef)).thenReturn(null);
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(false);
    when(favouritesService.isFavourite("testuser", nodeRef)).thenReturn(true);
    when(
      nodeService.hasAspect(
        nodeRef,
        io.swagger.model.alfresco.DocumentModel.ASPECT_URLABLE
      )
    ).thenReturn(false);

    Node result = nodesApi.getNode(nodeRef);

    assertEquals("content-id", result.getId());
    assertEquals("document.pdf", result.getName());
    assertEquals("1024", result.getProperties().get("size"));
    assertEquals("application/pdf", result.getProperties().get("mimetype"));
    assertEquals("UTF-8", result.getProperties().get("encoding"));
    assertEquals("false", result.getProperties().get("multilingual"));
    assertEquals("false", result.getProperties().get("locked"));
    assertTrue(result.getFavourite());
  }

  // --- getPathByNode ---

  @Test
  public void testGetPathByNode_whenNotLibraryOrInfo_thenReturnsEmpty() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(
      secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);

    List<Node> result = nodesApi.getPathByNode("node-id");

    assertTrue(result.isEmpty());
  }

  // --- getNode with owner ---

  @Test
  public void testGetNode_whenHasOwner_thenSetsOwnerProperty() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "owned-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    ChildAssociationRef childAssocRef = mock(ChildAssociationRef.class);

    when(nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)).thenReturn(
      "OwnedFolder"
    );
    when(nodeService.getType(nodeRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)).thenReturn(
      false
    );
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(nodeService.getPrimaryParent(nodeRef)).thenReturn(childAssocRef);
    when(childAssocRef.getParentRef()).thenReturn(parentRef);
    when(nodeService.getProperties(nodeRef)).thenReturn(new HashMap<>());
    when(ownableService.hasOwner(nodeRef)).thenReturn(true);
    when(ownableService.getOwner(nodeRef)).thenReturn("admin");
    when(permissionService.getAllSetPermissions(nodeRef)).thenReturn(
      Collections.emptySet()
    );
    when(authorityService.getAuthorities()).thenReturn(new HashSet<>());
    when(
      nodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
    ).thenReturn(false);
    when(favouritesService.isFavourite("testuser", nodeRef)).thenReturn(false);
    when(
      nodeService.getChildAssocs(nodeRef, Set.of(ContentModel.TYPE_FOLDER))
    ).thenReturn(Collections.emptyList());

    Node result = nodesApi.getNode(nodeRef);

    assertEquals("admin", result.getProperties().get("owner"));
  }
}
