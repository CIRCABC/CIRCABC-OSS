package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.LinksBusinessSrv;
import eu.europa.ec.digit.circabc.rest.service.ShareSpaceService;
import eu.europa.ec.digit.circabc.rest.service.helper.MetadataManager;
import io.swagger.model.I18nProperty;
import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.model.PagedShares;
import io.swagger.model.Share;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class SpacesApiImplTest {

  private SpacesApiImpl spacesApi;
  private NodeService secureNodeService;
  private NodeService unsecureNodeService;
  private FileFolderService fileFolderService;
  private NodesApi nodesApi;
  private LinksBusinessSrv linksBusinessSrv;
  private ApiToolBox apiToolBox;
  private PermissionService permissionService;
  private DictionaryService dictionaryService;
  private ShareSpaceService shareSpaceService;
  private MetadataManager metadataManager;

  @Before
  public void setUp() throws Exception {
    spacesApi = new SpacesApiImpl();

    secureNodeService = mock(NodeService.class);
    unsecureNodeService = mock(NodeService.class);
    fileFolderService = mock(FileFolderService.class);
    nodesApi = mock(NodesApi.class);
    linksBusinessSrv = mock(LinksBusinessSrv.class);
    apiToolBox = mock(ApiToolBox.class);
    permissionService = mock(PermissionService.class);
    dictionaryService = mock(DictionaryService.class);
    shareSpaceService = mock(ShareSpaceService.class);
    metadataManager = mock(MetadataManager.class);

    setField("secureNodeService", secureNodeService);
    setField("unsecureNodeService", unsecureNodeService);
    setField("fileFolderService", fileFolderService);
    setField("nodesApi", nodesApi);
    setField("linksBusinessSrv", linksBusinessSrv);
    setField("apiToolBox", apiToolBox);
    setField("permissionService", permissionService);
    setField("dictionaryService", dictionaryService);
    setField("shareSpaceService", shareSpaceService);
    setField("metadataManager", metadataManager);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SpacesApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(spacesApi, value);
  }

  @Test
  public void testSpaceGetChildren_whenFolderOnly_thenReturnsOnlyFolders() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    when(
      secureNodeService.hasAspect(spaceRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(childRef);
    when(fileFolderService.listFolders(spaceRef)).thenReturn(
      Collections.singletonList(fileInfo)
    );

    when(
      secureNodeService.hasAspect(
        childRef,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      )
    ).thenReturn(false);

    Node node = new Node();
    node.setId("child-id");
    when(nodesApi.getNode(childRef)).thenReturn(node);

    List<Node> result = spacesApi.spaceGetChildren("space-id", true);

    assertEquals(1, result.size());
    assertEquals("child-id", result.get(0).getId());
    verify(fileFolderService).listFolders(spaceRef);
  }

  @Test
  public void testSpaceGetChildren_whenNotLibraryOrInformation_thenReturnsEmpty() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );

    when(
      secureNodeService.hasAspect(spaceRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(
      secureNodeService.hasAspect(spaceRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);

    List<Node> result = spacesApi.spaceGetChildren("space-id", false);

    assertTrue(result.isEmpty());
    verifyNoInteractions(fileFolderService);
  }

  @Test
  public void testSpacesIdSpacesPost_whenParentIsFolder_thenCreatesSpace() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "new-child"
    );

    when(secureNodeService.getType(parentRef)).thenReturn(
      ContentModel.TYPE_FOLDER
    );

    ChildAssociationRef childAssoc = mock(ChildAssociationRef.class);
    when(childAssoc.getChildRef()).thenReturn(childRef);
    when(
      secureNodeService.createNode(
        eq(parentRef),
        eq(ContentModel.ASSOC_CONTAINS),
        any(QName.class),
        eq(ContentModel.TYPE_FOLDER)
      )
    ).thenReturn(childAssoc);

    Node expectedNode = new Node();
    expectedNode.setId("new-child");
    when(nodesApi.getNode(childRef)).thenReturn(expectedNode);

    Node body = new Node();
    body.setName("Test Folder");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setProperties(new HashMap<>());

    Node result = spacesApi.spacesIdSpacesPost("parent-id", body);

    assertEquals("new-child", result.getId());
    verify(secureNodeService).setProperty(
      childRef,
      ContentModel.PROP_NAME,
      "Test Folder"
    );
  }

  @Test(expected = InvalidTypeException.class)
  public void testSpacesIdSpacesPost_whenParentNotFolder_thenThrows() {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    when(secureNodeService.getType(parentRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );

    Node body = new Node();
    body.setName("Test");

    spacesApi.spacesIdSpacesPost("parent-id", body);
  }

  @Test
  public void testSpaceDelete_whenTypeIsFolder_thenDeletesNode() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    NodeRef igRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-root"
    );

    when(secureNodeService.getType(nodeRef)).thenReturn(
      ContentModel.TYPE_FOLDER
    );
    when(apiToolBox.getCurrentInterestGroup(nodeRef)).thenReturn(igRoot);

    spacesApi.spaceDelete("folder-id");

    verify(secureNodeService).setProperty(
      nodeRef,
      CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED,
      "ig-root"
    );
    verify(secureNodeService).deleteNode(nodeRef);
  }

  @Test(expected = InvalidTypeException.class)
  public void testSpaceDelete_whenTypeIsNotFolder_thenThrows() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "content-id"
    );

    when(secureNodeService.getType(nodeRef)).thenReturn(
      ContentModel.TYPE_CONTENT
    );

    spacesApi.spaceDelete("content-id");
  }

  @Test
  public void testDeleteShare_whenCalled_thenDelegatesToLinksBusinessSrv() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    spacesApi.deleteShare("space-id", "ig-id");

    verify(linksBusinessSrv).removeSharing(spaceRef, igRef);
  }

  @Test
  public void testGetFolderSize_whenEmptyFolder_thenReturnsZero() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "empty-folder"
    );

    when(
      secureNodeService.hasAspect(spaceRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(fileFolderService.list(spaceRef)).thenReturn(Collections.emptyList());

    int size = spacesApi.getFolderSize("empty-folder");

    assertEquals(0, size);
  }

  @Test
  public void testSpaceGetChildren_whenAllItems_thenReturnsList() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    when(
      secureNodeService.hasAspect(spaceRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(childRef);
    when(fileFolderService.list(spaceRef)).thenReturn(
      Collections.singletonList(fileInfo)
    );

    when(
      secureNodeService.hasAspect(
        childRef,
        ContentModel.ASPECT_MULTILINGUAL_DOCUMENT
      )
    ).thenReturn(false);

    Node node = new Node();
    node.setId("child-id");
    when(nodesApi.getNode(childRef)).thenReturn(node);

    List<Node> result = spacesApi.spaceGetChildren("space-id", false);

    assertEquals(1, result.size());
    verify(fileFolderService).list(spaceRef);
  }

  @Test
  public void testCreateSharedSpaceLink_whenCalled_thenDelegatesToService() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );

    when(
      unsecureNodeService.getProperty(spaceRef, ContentModel.PROP_NAME)
    ).thenReturn("SharedFolder");
    when(
      metadataManager.getValidUniqueName(parentRef, "Link to SharedFolder.url")
    ).thenReturn("Link to SharedFolder.url");

    spacesApi.createSharedSpaceLink("space-id", "parent-id", "title", "desc");

    verify(shareSpaceService).linkSharedSpace(
      parentRef,
      spaceRef,
      "Link to SharedFolder.url",
      "title",
      "desc"
    );
  }

  // --- spacesIdPut tests ---

  @Test
  public void testSpacesIdPut_whenValidBody_thenUpdatesProperties()
    throws Exception {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );

    FileFolderService fileFolderSvc = fileFolderService;

    Node body = new Node();
    body.setName("Updated Folder");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setProperties(new HashMap<>());

    spacesApi.spacesIdPut("folder-id", body);

    verify(fileFolderSvc).rename(nodeRef, "Updated Folder");
    verify(secureNodeService).setProperty(
      eq(nodeRef),
      eq(ContentModel.PROP_TITLE),
      any()
    );
    verify(secureNodeService).setProperty(
      eq(nodeRef),
      eq(ContentModel.PROP_DESCRIPTION),
      any()
    );
  }

  // --- spacesIdUrlPost tests ---

  @Test
  public void testSpacesIdUrlPost_whenLibraryAspect_thenCreatesUrlNode()
    throws Exception {
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef createdRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "url-node-id"
    );

    NodesApi nodesApiMock = nodesApi;

    when(
      secureNodeService.hasAspect(parentRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(
      secureNodeService.getChildByName(
        eq(parentRef),
        eq(ContentModel.ASSOC_CONTAINS),
        anyString()
      )
    ).thenReturn(null);

    FileInfo fileInfo = mock(FileInfo.class);
    when(fileInfo.getNodeRef()).thenReturn(createdRef);
    when(
      fileFolderService.create(
        eq(parentRef),
        anyString(),
        eq(ContentModel.TYPE_CONTENT)
      )
    ).thenReturn(fileInfo);

    Node expectedNode = new Node();
    expectedNode.setId("url-node-id");
    when(nodesApiMock.getNode(createdRef)).thenReturn(expectedNode);
    when(
      nodesApiMock.generateUniqueName(eq(parentRef), anyString())
    ).thenReturn("MyLink.html");

    // Re-inject nodesApi since we need generateUniqueName to work
    java.lang.reflect.Field f = SpacesApiImpl.class.getDeclaredField(
      "nodesApi"
    );
    f.setAccessible(true);
    f.set(spacesApi, nodesApiMock);

    Node body = new Node();
    body.setName("MyLink");
    Map<String, String> props = new HashMap<>();
    props.put("url", "https://example.com");
    body.setProperties(props);

    Node result = spacesApi.spacesIdUrlPost("parent-id", body);

    assertEquals("url-node-id", result.getId());
    verify(secureNodeService).addAspect(
      eq(createdRef),
      eq(io.swagger.model.alfresco.DocumentModel.ASPECT_URLABLE),
      any()
    );
    verify(secureNodeService).setProperty(
      createdRef,
      io.swagger.model.alfresco.DocumentModel.PROP_URL,
      "https://example.com"
    );
  }

  // --- getInvitedInterestGroups tests ---

  @Test
  public void testGetInvitedInterestGroups_whenNoShares_thenReturnsEmpty() {
    NodeRef spaceRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );

    when(
      secureNodeService.getChildAssocs(
        eq(spaceRef),
        eq(
          io.swagger.model.alfresco.SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER
        ),
        any()
      )
    ).thenReturn(Collections.emptyList());

    PagedShares result = spacesApi.getInvitedInterestGroups("space-id", 0, 10);

    assertEquals(0, result.getTotal());
    assertTrue(result.getData().isEmpty());
  }
}
