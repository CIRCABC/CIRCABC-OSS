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
import java.util.HashMap;
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

public class LinksBusinessSrvTest {

  private LinksBusinessImpl linksBusinessImpl;
  private NodeService nodeService;
  private ShareSpaceService shareSpaceService;
  private ApiToolBox apiToolBox;
  private MetadataManager metadataManager;
  private NodeTypeManager nodeTypeManager;

  private NodeRef parent;
  private NodeRef target;

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

    parent = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "parent-id");
    target = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "target-id");
  }

  @Test
  public void testCreateLink_whenTargetIsContent_thenCreatesFileLink() {
    QName assocQName = QName.createQName("test", "assoc");
    ChildAssociationRef primaryParent = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      parent,
      assocQName,
      target
    );
    NodeRef createdNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "link-id"
    );
    ChildAssociationRef createdAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      target,
      assocQName,
      createdNode
    );

    when(nodeService.getProperty(target, ContentModel.PROP_NAME)).thenReturn(
      "doc.pdf"
    );
    when(
      metadataManager.getValidUniqueName(parent, "Link to doc.pdf.url")
    ).thenReturn("Link to doc.pdf.url");
    when(nodeService.getPrimaryParent(target)).thenReturn(primaryParent);
    when(nodeTypeManager.isContent(target)).thenReturn(true);
    when(
      nodeService.createNode(
        eq(target),
        eq(ContentModel.ASSOC_CONTAINS),
        eq(assocQName),
        eq(ApplicationModel.TYPE_FILELINK),
        anyMap()
      )
    ).thenReturn(createdAssoc);

    NodeRef result = linksBusinessImpl.createLink(parent, target);

    assertEquals(createdNode, result);
    verify(nodeService).createNode(
      eq(target),
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
      parent,
      assocQName,
      target
    );
    NodeRef createdNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-link-id"
    );
    ChildAssociationRef createdAssoc = new ChildAssociationRef(
      ContentModel.ASSOC_CONTAINS,
      target,
      assocQName,
      createdNode
    );

    when(nodeService.getProperty(target, ContentModel.PROP_NAME)).thenReturn(
      "myFolder"
    );
    when(
      metadataManager.getValidUniqueName(parent, "Link to myFolder.url")
    ).thenReturn("Link to myFolder.url");
    when(nodeService.getPrimaryParent(target)).thenReturn(primaryParent);
    when(nodeTypeManager.isContent(target)).thenReturn(false);
    when(
      nodeService.createNode(
        eq(target),
        eq(ContentModel.ASSOC_CONTAINS),
        eq(assocQName),
        eq(ApplicationModel.TYPE_FOLDERLINK),
        anyMap()
      )
    ).thenReturn(createdAssoc);

    NodeRef result = linksBusinessImpl.createLink(parent, target);

    assertEquals(createdNode, result);
    verify(nodeService).createNode(
      eq(target),
      eq(ContentModel.ASSOC_CONTAINS),
      eq(assocQName),
      eq(ApplicationModel.TYPE_FOLDERLINK),
      anyMap()
    );
  }

  @Test
  public void testCreateSharedSpaceLink_delegatesToShareSpaceService() {
    NodeRef expectedLink = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "shared-link-id"
    );

    when(nodeService.getProperty(target, ContentModel.PROP_NAME)).thenReturn(
      "sharedFolder"
    );
    when(
      metadataManager.getValidUniqueName(parent, "Link to sharedFolder.url")
    ).thenReturn("Link to sharedFolder.url");
    when(
      shareSpaceService.linkSharedSpace(
        parent,
        target,
        "Link to sharedFolder.url",
        "My Title",
        "My Desc"
      )
    ).thenReturn(expectedLink);

    NodeRef result = linksBusinessImpl.createSharedSpaceLink(
      parent,
      target,
      "My Title",
      "My Desc"
    );

    assertEquals(expectedLink, result);
  }

  @Test
  public void testApplySharing_delegatesToShareSpaceService() {
    NodeRef shareSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef ig = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");

    linksBusinessImpl.applySharing(shareSpace, ig, LibraryPermissions.LIBADMIN);

    verify(shareSpaceService).inviteInterestGroup(shareSpace, ig, "LibAdmin");
  }

  @Test
  public void testRemoveSharing_delegatesToShareSpaceService() {
    NodeRef shareSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef ig = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id");

    linksBusinessImpl.removeSharing(shareSpace, ig);

    verify(shareSpaceService).unInviteInterestGroup(shareSpace, ig);
  }

  @Test
  public void testGetAvailableSharedSpaces_whenNull_thenReturnsEmptyList() {
    when(shareSpaceService.getAvailableShareSpaces(parent)).thenReturn(null);

    List<ShareSpaceItem> result = linksBusinessImpl.getAvailableSharedSpaces(
      parent
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetAvailableSharedSpaces_whenEmpty_thenReturnsEmptyList() {
    when(shareSpaceService.getAvailableShareSpaces(parent)).thenReturn(
      Collections.emptyList()
    );

    List<ShareSpaceItem> result = linksBusinessImpl.getAvailableSharedSpaces(
      parent
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testFindSharedSpaces_whenNull_thenReturnsEmptyList() {
    when(shareSpaceService.getAllSharedSpaceInInterestGroup(parent)).thenReturn(
      null
    );

    List<ShareSpaceItem> result = linksBusinessImpl.findSharedSpaces(parent);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetInterestGroupForSharing_whenNull_thenReturnsEmptyList() {
    when(shareSpaceService.getAvailableInterestGroups(parent)).thenReturn(null);

    List<InterestGroupLinkItem> result =
      linksBusinessImpl.getInterestGroupForSharing(parent);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetInterestGroupForSharing_whenHasResults_thenReturnsMappedItems() {
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    when(shareSpaceService.getAvailableInterestGroups(parent)).thenReturn(
      Arrays.asList(igRef)
    );

    Map<QName, Serializable> props = new HashMap<>();
    props.put(ContentModel.PROP_NAME, "TestIG");
    when(nodeService.getProperties(igRef)).thenReturn(props);
    when(metadataManager.computeTitle(props)).thenReturn("Test Interest Group");

    List<InterestGroupLinkItem> result =
      linksBusinessImpl.getInterestGroupForSharing(parent);

    assertEquals(1, result.size());
    assertEquals("TestIG", result.get(0).getName());
    assertEquals("Test Interest Group", result.get(0).getTitle());
    assertNull(result.get(0).getPermission());
    assertEquals(igRef, result.get(0).getNodeRef());
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = LinksBusinessImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(linksBusinessImpl, value);
  }
}
