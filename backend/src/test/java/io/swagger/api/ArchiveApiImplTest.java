package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.ArchiveNode;
import io.swagger.model.Node;
import io.swagger.model.PagedArchiveNodes;
import io.swagger.model.RestoreNodeMetadata;
import io.swagger.model.alfresco.CircabcModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Before;
import org.junit.Test;

public class ArchiveApiImplTest {

  private ArchiveApiImpl archiveApi;
  private NodeArchiveService nodeArchiveService;
  private SearchService internalSearchService;
  private NodesApi nodesApi;
  private NodeService nodeService;

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

    archiveApi = new ArchiveApiImpl();

    nodeArchiveService = mock(NodeArchiveService.class);
    internalSearchService = mock(SearchService.class);
    nodesApi = mock(NodesApi.class);
    nodeService = mock(NodeService.class);

    setField("nodeArchiveService", nodeArchiveService);
    setField("internalSearchService", internalSearchService);
    setField("nodesApi", nodesApi);
    setField("nodeService", nodeService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ArchiveApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(archiveApi, value);
  }

  @Test
  public void testGroupsIdDocumentsDeletedGet_whenResultsExist_thenReturnsPagedNodes() {
    String groupId = "group-id-1";
    NodeRef archiveRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "archive-root"
    );
    when(
      nodeArchiveService.getStoreArchiveNode(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
      )
    ).thenReturn(archiveRoot);

    NodeRef archivedNodeRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "node-1"
    );
    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getNumberFound()).thenReturn(1L);

    ResultSetRow row = mock(ResultSetRow.class);
    when(row.getNodeRef()).thenReturn(archivedNodeRef);

    @SuppressWarnings("unchecked")
    Iterator<ResultSetRow> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(true, false);
    when(iterator.next()).thenReturn(row);
    when(resultSet.iterator()).thenReturn(iterator);

    when(internalSearchService.query(any())).thenReturn(resultSet);

    Node node = new Node();
    node.setId("node-1");
    node.setName("test-doc.pdf");
    when(nodesApi.getNode(archivedNodeRef)).thenReturn(node);
    when(
      nodeService.getProperty(archivedNodeRef, ContentModel.PROP_ARCHIVED_BY)
    ).thenReturn("admin");
    when(
      nodeService.getProperty(archivedNodeRef, ContentModel.PROP_ARCHIVED_DATE)
    ).thenReturn(new Date());

    PagedArchiveNodes result = archiveApi.groupsIdDocumentsDeletedGet(
      groupId,
      10,
      1,
      ""
    );

    assertNotNull(result);
    assertEquals(Long.valueOf(1L), Long.valueOf(result.getTotal()));
    assertEquals(1, result.getData().size());
    assertEquals("admin", result.getData().get(0).getDeletedBy());
  }

  @Test
  public void testGroupsIdDocumentsDeletedGet_whenNoResults_thenReturnsEmptyList() {
    String groupId = "group-id-1";
    NodeRef archiveRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "archive-root"
    );
    when(
      nodeArchiveService.getStoreArchiveNode(
        StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
      )
    ).thenReturn(archiveRoot);

    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getNumberFound()).thenReturn(0L);

    @SuppressWarnings("unchecked")
    Iterator<ResultSetRow> iterator = mock(Iterator.class);
    when(iterator.hasNext()).thenReturn(false);
    when(resultSet.iterator()).thenReturn(iterator);

    when(internalSearchService.query(any())).thenReturn(resultSet);

    PagedArchiveNodes result = archiveApi.groupsIdDocumentsDeletedGet(
      groupId,
      10,
      1,
      ""
    );

    assertNotNull(result);
    assertEquals(Long.valueOf(0L), Long.valueOf(result.getTotal()));
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void testGroupsIdDocumentsDeletedPost_whenTargetFolderProvided_thenRestoresToTarget() {
    RestoreNodeMetadata metadata = new RestoreNodeMetadata();
    metadata.setArchiveNodeId("archived-node-id");
    metadata.setTargetFolderId("target-folder-id");

    NodeRef expectedArchiveRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "archived-node-id"
    );
    NodeRef expectedTargetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "target-folder-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-1"
    );
    when(
      nodeService.getProperty(
        expectedArchiveRef,
        CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED
      )
    ).thenReturn("group-1");
    when(nodeService.exists(expectedTargetRef)).thenReturn(true);
    when(
      nodeService.hasAspect(expectedTargetRef, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);
    ChildAssociationRef targetParentAssoc = mock(ChildAssociationRef.class);
    when(targetParentAssoc.getParentRef()).thenReturn(igRef);
    when(nodeService.getPrimaryParent(expectedTargetRef)).thenReturn(
      targetParentAssoc
    );
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );

    archiveApi.groupsIdDocumentsDeletedPost("group-1", metadata);

    verify(nodeArchiveService).restoreArchivedNode(
      expectedArchiveRef,
      expectedTargetRef,
      null,
      null
    );
  }

  @Test
  public void testGroupsIdDocumentsDeletedPost_whenNoTargetFolder_thenRestoresToOriginal() {
    RestoreNodeMetadata metadata = new RestoreNodeMetadata();
    metadata.setArchiveNodeId("archived-node-id");
    metadata.setTargetFolderId("");

    NodeRef expectedArchiveRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "archived-node-id"
    );
    when(
      nodeService.getProperty(
        expectedArchiveRef,
        CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED
      )
    ).thenReturn("group-1");

    archiveApi.groupsIdDocumentsDeletedPost("group-1", metadata);

    verify(nodeArchiveService).restoreArchivedNode(expectedArchiveRef);
  }

  @Test
  public void testGroupsIdDocumentsDeletedPost_whenEmptyArchiveNodeId_thenDoesNothing() {
    RestoreNodeMetadata metadata = new RestoreNodeMetadata();
    metadata.setArchiveNodeId("");
    metadata.setTargetFolderId("");

    archiveApi.groupsIdDocumentsDeletedPost("group-1", metadata);

    verifyNoInteractions(nodeArchiveService);
  }

  @Test
  public void testGroupsIdDocumentsDeletedNodeIdDelete_thenPurgesNode() {
    NodeRef expectedRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "node-to-purge"
    );
    when(
      nodeService.getProperty(
        expectedRef,
        CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED
      )
    ).thenReturn("group-1");

    archiveApi.groupsIdDocumentsDeletedNodeIdDelete("group-1", "node-to-purge");

    verify(nodeArchiveService).purgeArchivedNode(expectedRef);
  }
}
