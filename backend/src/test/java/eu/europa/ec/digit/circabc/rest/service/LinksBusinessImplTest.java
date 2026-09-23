package eu.europa.ec.digit.circabc.rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.helper.MetadataManager;
import eu.europa.ec.digit.circabc.rest.service.helper.NodeTypeManager;
import io.swagger.model.ShareSpaceItem;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.SharedSpaceModel;
import io.swagger.model.db.InterestGroupLinkItem;
import io.swagger.model.permissions.LibraryPermissions;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.junit.Before;
import org.junit.Test;

public class LinksBusinessImplTest {

  private LinksBusinessImpl linksBusinessImpl;
  private NodeService nodeService;
  private ShareSpaceService shareSpaceService;
  private ApiToolBox apiToolBox;
  private MetadataManager metadataManager;
  private NodeTypeManager nodeTypeManager;

  private NodeRef parentRef;
  private NodeRef targetRef;
  private NodeRef childRef;

  @Before
  public void setUp() throws Exception {
    linksBusinessImpl = new LinksBusinessImpl();
    nodeService = mock(NodeService.class);
    shareSpaceService = mock(ShareSpaceService.class);
    apiToolBox = mock(ApiToolBox.class);
    metadataManager = mock(MetadataManager.class);
    nodeTypeManager = mock(NodeTypeManager.class);

    setField("nodeService", nodeService);
    setField("shareSpaceService", shareSpaceService);
    setField("apiToolBox", apiToolBox);
    setField("metadataManager", metadataManager);
    setField("nodeTypeManager", nodeTypeManager);

    parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    targetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "target-id"
    );
    childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LinksBusinessImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(linksBusinessImpl, value);
  }

  @Test
  public void testCreateLink_whenTargetIsContent_thenCreatesFileLink() {
    QName assocQName = QName.createQName("test", "assoc");
    ChildAssociationRef primaryParent = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      assocQName,
      targetRef
    );
    ChildAssociationRef createdAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      targetRef,
      assocQName,
      childRef
    );

    when(nodeService.getProperty(targetRef, ContentModel.PROP_NAME)).thenReturn(
      "doc.txt"
    );
    when(
      metadataManager.getValidUniqueName(parentRef, "Link to doc.txt.url")
    ).thenReturn("Link to doc.txt.url");
    when(nodeService.getPrimaryParent(targetRef)).thenReturn(primaryParent);
    when(nodeTypeManager.isContent(targetRef)).thenReturn(true);
    when(
      nodeService.createNode(
        eq(targetRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq(assocQName),
        eq(ApplicationModel.TYPE_FILELINK),
        anyMap()
      )
    ).thenReturn(createdAssoc);

    NodeRef result = linksBusinessImpl.createLink(parentRef, targetRef);

    assertEquals(childRef, result);
    verify(nodeService).createNode(
      eq(targetRef),
      eq(ContentModel.ASSOC_CONTAINS),
      eq(assocQName),
      eq(ApplicationModel.TYPE_FILELINK),
      anyMap()
    );
  }

  @Test
  public void testCreateLink_whenTargetIsFolder_thenCreatesFolderLink() {
    QName assocQName = QName.createQName("test", "assoc");
    ChildAssociationRef primaryParent = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parentRef,
      assocQName,
      targetRef
    );
    ChildAssociationRef createdAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      targetRef,
      assocQName,
      childRef
    );

    when(nodeService.getProperty(targetRef, ContentModel.PROP_NAME)).thenReturn(
      "folder"
    );
    when(
      metadataManager.getValidUniqueName(parentRef, "Link to folder.url")
    ).thenReturn("Link to folder.url");
    when(nodeService.getPrimaryParent(targetRef)).thenReturn(primaryParent);
    when(nodeTypeManager.isContent(targetRef)).thenReturn(false);
    when(
      nodeService.createNode(
        eq(targetRef),
        eq(ContentModel.ASSOC_CONTAINS),
        eq(assocQName),
        eq(ApplicationModel.TYPE_FOLDERLINK),
        anyMap()
      )
    ).thenReturn(createdAssoc);

    NodeRef result = linksBusinessImpl.createLink(parentRef, targetRef);

    assertEquals(childRef, result);
    verify(nodeService).createNode(
      eq(targetRef),
      eq(ContentModel.ASSOC_CONTAINS),
      eq(assocQName),
      eq(ApplicationModel.TYPE_FOLDERLINK),
      anyMap()
    );
  }

  @Test
  public void testCreateSharedSpaceLink_whenCalled_thenDelegatesToShareSpaceService() {
    NodeRef expectedResult = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "link-id"
    );

    when(nodeService.getProperty(targetRef, ContentModel.PROP_NAME)).thenReturn(
      "shared"
    );
    when(
      metadataManager.getValidUniqueName(parentRef, "Link to shared.url")
    ).thenReturn("Link to shared.url");
    when(
      shareSpaceService.linkSharedSpace(
        parentRef,
        targetRef,
        "Link to shared.url",
        "title",
        "desc"
      )
    ).thenReturn(expectedResult);

    NodeRef result = linksBusinessImpl.createSharedSpaceLink(
      parentRef,
      targetRef,
      "title",
      "desc"
    );

    assertEquals(expectedResult, result);
  }

  @Test
  public void testGetAvailableSharedSpaces_whenNullList_thenReturnsEmptyList() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(shareSpaceService.getAvailableShareSpaces(nodeRef)).thenReturn(null);

    List<ShareSpaceItem> result = linksBusinessImpl.getAvailableSharedSpaces(
      nodeRef
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetAvailableSharedSpaces_whenEmptyList_thenReturnsEmptyList() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(shareSpaceService.getAvailableShareSpaces(nodeRef)).thenReturn(
      Collections.emptyList()
    );

    List<ShareSpaceItem> result = linksBusinessImpl.getAvailableSharedSpaces(
      nodeRef
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetInterestGroupForSharing_whenNullList_thenReturnsEmptyList() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(shareSpaceService.getAvailableInterestGroups(nodeRef)).thenReturn(
      null
    );

    List<InterestGroupLinkItem> result =
      linksBusinessImpl.getInterestGroupForSharing(nodeRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetInterestGroupForSharing_whenGroupsExist_thenReturnsItems() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    Map<QName, Serializable> props = Map.of(
      ContentModel.PROP_NAME,
      "ig-name",
      ContentModel.PROP_TITLE,
      "IG Title"
    );

    when(shareSpaceService.getAvailableInterestGroups(nodeRef)).thenReturn(
      Collections.singletonList(igRef)
    );
    when(nodeService.getProperties(igRef)).thenReturn(props);
    when(metadataManager.computeTitle(props)).thenReturn("IG Title");

    List<InterestGroupLinkItem> result =
      linksBusinessImpl.getInterestGroupForSharing(nodeRef);

    assertEquals(1, result.size());
    assertEquals(igRef, result.get(0).getNodeRef());
    assertEquals("ig-name", result.get(0).getName());
    assertEquals("IG Title", result.get(0).getTitle());
    assertNull(result.get(0).getPermission());
  }

  @Test
  public void testApplySharing_whenCalled_thenDelegatesToShareSpaceService() {
    NodeRef shareSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "share-id"
    );
    NodeRef ig = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");

    linksBusinessImpl.applySharing(
      shareSpace,
      ig,
      LibraryPermissions.LIBMANAGEOWN
    );

    verify(shareSpaceService).inviteInterestGroup(
      shareSpace,
      ig,
      "LibManageOwn"
    );
  }

  @Test
  public void testRemoveSharing_whenCalled_thenDelegatesToShareSpaceService() {
    NodeRef shareSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "share-id"
    );
    NodeRef ig = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");

    linksBusinessImpl.removeSharing(shareSpace, ig);

    verify(shareSpaceService).unInviteInterestGroup(shareSpace, ig);
  }

  @Test
  public void testFindSharedSpaces_whenNullList_thenReturnsEmptyList() {
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(
      shareSpaceService.getAllSharedSpaceInInterestGroup(nodeRef)
    ).thenReturn(null);

    List<ShareSpaceItem> result = linksBusinessImpl.findSharedSpaces(nodeRef);

    assertTrue(result.isEmpty());
  }
}
