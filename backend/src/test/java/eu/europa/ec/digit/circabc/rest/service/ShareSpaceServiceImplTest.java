package eu.europa.ec.digit.circabc.rest.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.report.ReportDaoService;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.SharedSpaceModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;

public class ShareSpaceServiceImplTest {

  private ShareSpaceServiceImpl service;
  private NodeService nodeService;
  private PermissionService permissionService;
  private DictionaryService dictionaryService;
  private SearchService searchService;
  private NamespaceService namespaceService;
  private ReportDaoService reportDaoService;
  private ApiToolBox apiToolBox;

  private NodeRef shareSpace;
  private NodeRef interestGroup;
  private NodeRef categoryRef;

  @Before
  public void setUp() throws Exception {
    service = new ShareSpaceServiceImpl();
    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    dictionaryService = mock(DictionaryService.class);
    searchService = mock(SearchService.class);
    namespaceService = mock(NamespaceService.class);
    reportDaoService = mock(ReportDaoService.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("dictionaryService", dictionaryService);
    setField("searchService", searchService);
    setField("namespaceService", namespaceService);
    setField("reportDaoService", reportDaoService);
    setField("apiToolBox", apiToolBox);

    shareSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "share-space-id"
    );
    interestGroup = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    categoryRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "category-id"
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ShareSpaceServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  // --- getInvitedInterestGroups ---

  @Test
  public void testGetInvitedInterestGroups_whenNoContainer_thenReturnsEmptyList() {
    when(
      nodeService.getChildAssocs(
        shareSpace,
        SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.emptyList());

    List<Pair<NodeRef, String>> result = service.getInvitedInterestGroups(
      shareSpace
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetInvitedInterestGroups_whenInvitedIgsExist_thenReturnsPairs() {
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerRef);

    ChildAssociationRef igAssoc = mock(ChildAssociationRef.class);
    when(igAssoc.getChildRef()).thenReturn(childRef);

    when(
      nodeService.getChildAssocs(
        shareSpace,
        SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(containerAssoc));
    when(
      nodeService.getChildAssocs(
        containerRef,
        SharedSpaceModel.ASSOC_ITEREST_GROUP,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(igAssoc));
    when(
      nodeService.getProperty(
        childRef,
        SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF
      )
    ).thenReturn(interestGroup);
    when(nodeService.exists(interestGroup)).thenReturn(true);
    when(
      nodeService.getProperty(childRef, SharedSpaceModel.PROP_PERMISSION)
    ).thenReturn("LibAccess");

    List<Pair<NodeRef, String>> result = service.getInvitedInterestGroups(
      shareSpace
    );

    assertEquals(1, result.size());
    assertEquals(interestGroup, result.get(0).getFirst());
    assertEquals("LibAccess", result.get(0).getSecond());
  }

  @Test
  public void testGetInvitedInterestGroups_whenIgNodeRefNull_thenSkipped() {
    NodeRef containerRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "container-id"
    );
    NodeRef childRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "child-id"
    );

    ChildAssociationRef containerAssoc = mock(ChildAssociationRef.class);
    when(containerAssoc.getChildRef()).thenReturn(containerRef);

    ChildAssociationRef igAssoc = mock(ChildAssociationRef.class);
    when(igAssoc.getChildRef()).thenReturn(childRef);

    when(
      nodeService.getChildAssocs(
        shareSpace,
        SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(containerAssoc));
    when(
      nodeService.getChildAssocs(
        containerRef,
        SharedSpaceModel.ASSOC_ITEREST_GROUP,
        RegexQNamePattern.MATCH_ALL
      )
    ).thenReturn(Collections.singletonList(igAssoc));
    when(
      nodeService.getProperty(
        childRef,
        SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF
      )
    ).thenReturn(null);

    List<Pair<NodeRef, String>> result = service.getInvitedInterestGroups(
      shareSpace
    );

    assertTrue(result.isEmpty());
  }

  // --- linkSharedSpace ---

  @Test
  public void testLinkSharedSpace_whenNameIsUnique_thenCreatesLink() {
    NodeRef currentSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "current-space"
    );
    String name = "MyLink";
    NodeRef createdNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "created-node"
    );

    // checkExists returns empty (name is unique)
    when(
      searchService.selectNodes(
        eq(currentSpace),
        anyString(),
        any(),
        eq(namespaceService),
        eq(false)
      )
    ).thenReturn(Collections.emptyList());

    QName qname = QName.createQName(
      NamespaceService.CONTENT_MODEL_1_0_URI,
      "test"
    );
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getQName()).thenReturn(qname);
    when(nodeService.getPrimaryParent(shareSpace)).thenReturn(parentAssoc);

    ChildAssociationRef createdAssoc = mock(ChildAssociationRef.class);
    when(createdAssoc.getChildRef()).thenReturn(createdNode);
    when(
      nodeService.createNode(
        eq(currentSpace),
        eq(ContentModel.ASSOC_CONTAINS),
        eq(qname),
        eq(ApplicationModel.TYPE_FOLDERLINK),
        any()
      )
    ).thenReturn(createdAssoc);

    NodeRef result = service.linkSharedSpace(
      currentSpace,
      shareSpace,
      name,
      "Title",
      "Desc"
    );

    assertEquals(createdNode, result);
  }

  @Test(expected = IllegalStateException.class)
  public void testLinkSharedSpace_whenNameExists_thenThrows() {
    NodeRef currentSpace = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "current-space"
    );
    NodeRef existingNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "existing"
    );

    when(
      searchService.selectNodes(
        eq(currentSpace),
        anyString(),
        any(),
        eq(namespaceService),
        eq(false)
      )
    ).thenReturn(Collections.singletonList(existingNode));

    service.linkSharedSpace(
      currentSpace,
      shareSpace,
      "ExistingName",
      null,
      null
    );
  }

  // --- getAllSharedSpaceInInterestGroup ---

  @Test
  public void testGetAllSharedSpaceInInterestGroup_whenResultsExist_thenReturnsNodeRefs() {
    NodeRef resultNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result-node"
    );
    when(apiToolBox.getPathFromSpaceRef(interestGroup, true)).thenReturn(
      "/app:company_home/cm:test"
    );

    ResultSet resultSet = mock(ResultSet.class);
    ResultSetRow row = mock(ResultSetRow.class);
    when(row.getNodeRef()).thenReturn(resultNode);

    @SuppressWarnings("unchecked")
    Iterator<ResultSetRow> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true, false);
    when(iterator.next()).thenReturn(row);
    when(resultSet.iterator()).thenReturn(iterator);

    when(searchService.query(any())).thenReturn(resultSet);

    List<NodeRef> result = service.getAllSharedSpaceInInterestGroup(
      interestGroup
    );

    assertEquals(1, result.size());
    assertEquals(resultNode, result.get(0));
    verify(resultSet).close();
  }

  @Test
  public void testGetAllSharedSpaceInInterestGroup_whenNoResults_thenReturnsEmptyList() {
    when(apiToolBox.getPathFromSpaceRef(interestGroup, true)).thenReturn(
      "/app:company_home/cm:test"
    );

    ResultSet resultSet = mock(ResultSet.class);
    @SuppressWarnings("unchecked")
    Iterator<ResultSetRow> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(false);
    when(resultSet.iterator()).thenReturn(iterator);

    when(searchService.query(any())).thenReturn(resultSet);

    List<NodeRef> result = service.getAllSharedSpaceInInterestGroup(
      interestGroup
    );

    assertTrue(result.isEmpty());
    verify(resultSet).close();
  }

  // --- inviteInterestGroup ---

  @Test(expected = IllegalStateException.class)
  public void testInviteInterestGroup_whenNotIgRoot_thenThrows() {
    when(
      nodeService.hasAspect(interestGroup, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    service.inviteInterestGroup(shareSpace, interestGroup, "LibAccess");
  }

  @Test(expected = IllegalStateException.class)
  public void testInviteInterestGroup_whenSameIg_thenThrows() {
    when(
      nodeService.hasAspect(interestGroup, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(shareSpace)).thenReturn(
      interestGroup
    );

    service.inviteInterestGroup(shareSpace, interestGroup, "LibAccess");
  }

  @Test(expected = IllegalStateException.class)
  public void testInviteInterestGroup_whenDifferentCategory_thenThrows() {
    NodeRef otherCategory = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "other-cat"
    );
    when(
      nodeService.hasAspect(interestGroup, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(apiToolBox.getCurrentInterestGroup(shareSpace)).thenReturn(
      new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "other-ig")
    );
    when(apiToolBox.getCurrentCategory(shareSpace)).thenReturn(categoryRef);
    when(apiToolBox.getCurrentCategory(interestGroup)).thenReturn(
      otherCategory
    );

    service.inviteInterestGroup(shareSpace, interestGroup, "LibAccess");
  }

  // --- unInviteInterestGroup ---

  @Test(expected = IllegalStateException.class)
  public void testUnInviteInterestGroup_whenMissingAspect_thenThrows() {
    when(
      nodeService.hasAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE)
    ).thenReturn(false);

    service.unInviteInterestGroup(shareSpace, interestGroup);
  }

  // --- getAvailableShareSpaces ---

  @Test
  public void testGetAvailableShareSpaces_whenSharedSpacesExist_thenReturnsFiltered() {
    NodeRef space = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef sharedNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "shared-node"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-ref"
    );
    NodeRef grandParentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "grandparent-ref"
    );

    when(apiToolBox.getCurrentInterestGroup(space)).thenReturn(interestGroup);
    when(reportDaoService.getAvailibleShareSpaces(interestGroup)).thenReturn(
      Collections.singletonList(sharedNode)
    );

    ChildAssociationRef primaryParent = mock(ChildAssociationRef.class);
    when(primaryParent.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(sharedNode)).thenReturn(primaryParent);

    ChildAssociationRef parentOfParent = mock(ChildAssociationRef.class);
    when(parentOfParent.getParentRef()).thenReturn(grandParentRef);
    when(nodeService.getPrimaryParent(parentRef)).thenReturn(parentOfParent);

    when(
      nodeService.hasAspect(grandParentRef, CircabcModel.ASPECT_SHARED_SPACE)
    ).thenReturn(true);

    List<NodeRef> result = service.getAvailableShareSpaces(space);

    assertEquals(1, result.size());
    assertEquals(grandParentRef, result.get(0));
  }

  @Test
  public void testGetAvailableShareSpaces_whenNotSharedSpace_thenFiltered() {
    NodeRef space = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "space-id"
    );
    NodeRef sharedNode = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "shared-node"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-ref"
    );
    NodeRef grandParentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "grandparent-ref"
    );

    when(apiToolBox.getCurrentInterestGroup(space)).thenReturn(interestGroup);
    when(reportDaoService.getAvailibleShareSpaces(interestGroup)).thenReturn(
      Collections.singletonList(sharedNode)
    );

    ChildAssociationRef primaryParent = mock(ChildAssociationRef.class);
    when(primaryParent.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(sharedNode)).thenReturn(primaryParent);

    ChildAssociationRef parentOfParent = mock(ChildAssociationRef.class);
    when(parentOfParent.getParentRef()).thenReturn(grandParentRef);
    when(nodeService.getPrimaryParent(parentRef)).thenReturn(parentOfParent);

    when(
      nodeService.hasAspect(grandParentRef, CircabcModel.ASPECT_SHARED_SPACE)
    ).thenReturn(false);

    List<NodeRef> result = service.getAvailableShareSpaces(space);

    assertTrue(result.isEmpty());
  }
}
