package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.Node;
import io.swagger.model.PagedNodes;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.Iterator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Before;
import org.junit.Test;

public class ExpiredApiImplTest {

  private ExpiredApiImpl expiredApi;
  private SearchService internalSearchService;
  private NodesApi nodesApi;
  private ApiToolBox apiToolBox;

  @Before
  public void setUp() throws Exception {
    expiredApi = new ExpiredApiImpl();

    internalSearchService = mock(SearchService.class);
    nodesApi = mock(NodesApi.class);
    apiToolBox = mock(ApiToolBox.class);

    setField("internalSearchService", internalSearchService);
    setField("nodesApi", nodesApi);
    setField("apiToolBox", apiToolBox);

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
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = ExpiredApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(expiredApi, value);
  }

  @Test
  public void testGroupsIdDocumentsExpiredGet_whenResultsExist_thenReturnsPagedNodes() {
    String id = "test-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef expiredRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "expired-id"
    );

    ResultSet resultSet = mock(ResultSet.class);
    ResultSetRow row = mock(ResultSetRow.class);
    @SuppressWarnings("unchecked")
    Iterator<ResultSetRow> iterator = mock(Iterator.class);

    when(apiToolBox.getPathFromSpaceRef(eq(nodeRef), eq(true))).thenReturn(
      "/app:company_home/test"
    );
    when(
      internalSearchService.query(
        eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
        eq(SearchService.LANGUAGE_LUCENE),
        anyString()
      )
    ).thenReturn(resultSet);
    when(resultSet.getNumberFound()).thenReturn(1L);
    when(resultSet.length()).thenReturn(1);
    when(resultSet.iterator()).thenReturn(iterator);
    when(iterator.hasNext()).thenReturn(true, false);
    when(iterator.next()).thenReturn(row);
    when(row.getNodeRef()).thenReturn(expiredRef);

    Node node = new Node();
    node.setId("expired-id");
    when(nodesApi.getNode(expiredRef)).thenReturn(node);

    PagedNodes result = expiredApi.groupsIdDocumentsExpiredGet(
      id,
      10,
      1,
      "asc"
    );

    assertNotNull(result);
    assertEquals(Long.valueOf(1L), result.getTotal());
    assertEquals(1, result.getData().size());
    assertEquals("expired-id", result.getData().get(0).getId());
  }

  @Test
  public void testGroupsIdDocumentsExpiredGet_whenNoResults_thenReturnsEmptyPagedNodes() {
    String id = "test-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

    ResultSet resultSet = mock(ResultSet.class);

    when(apiToolBox.getPathFromSpaceRef(eq(nodeRef), eq(true))).thenReturn(
      "/app:company_home/test"
    );
    when(
      internalSearchService.query(
        eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
        eq(SearchService.LANGUAGE_LUCENE),
        anyString()
      )
    ).thenReturn(resultSet);
    when(resultSet.getNumberFound()).thenReturn(0L);
    when(resultSet.length()).thenReturn(0);

    PagedNodes result = expiredApi.groupsIdDocumentsExpiredGet(
      id,
      10,
      1,
      "asc"
    );

    assertNotNull(result);
    assertEquals(Long.valueOf(0L), result.getTotal());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void testGroupsIdDocumentsExpiredGet_whenMultipleResults_thenReturnsAll() {
    String id = "test-id";
    NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
    NodeRef expiredRef1 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "expired-1"
    );
    NodeRef expiredRef2 = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "expired-2"
    );

    ResultSet resultSet = mock(ResultSet.class);
    ResultSetRow row1 = mock(ResultSetRow.class);
    ResultSetRow row2 = mock(ResultSetRow.class);
    @SuppressWarnings("unchecked")
    Iterator<ResultSetRow> iterator = mock(Iterator.class);

    when(apiToolBox.getPathFromSpaceRef(eq(nodeRef), eq(true))).thenReturn(
      "/app:company_home/test"
    );
    when(
      internalSearchService.query(
        eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE),
        eq(SearchService.LANGUAGE_LUCENE),
        anyString()
      )
    ).thenReturn(resultSet);
    when(resultSet.getNumberFound()).thenReturn(2L);
    when(resultSet.length()).thenReturn(2);
    when(resultSet.iterator()).thenReturn(iterator);
    when(iterator.hasNext()).thenReturn(true, true, false);
    when(iterator.next()).thenReturn(row1, row2);
    when(row1.getNodeRef()).thenReturn(expiredRef1);
    when(row2.getNodeRef()).thenReturn(expiredRef2);

    Node node1 = new Node();
    node1.setId("expired-1");
    Node node2 = new Node();
    node2.setId("expired-2");
    when(nodesApi.getNode(expiredRef1)).thenReturn(node1);
    when(nodesApi.getNode(expiredRef2)).thenReturn(node2);

    PagedNodes result = expiredApi.groupsIdDocumentsExpiredGet(
      id,
      10,
      1,
      "asc"
    );

    assertNotNull(result);
    assertEquals(Long.valueOf(2L), result.getTotal());
    assertEquals(2, result.getData().size());
  }
}
