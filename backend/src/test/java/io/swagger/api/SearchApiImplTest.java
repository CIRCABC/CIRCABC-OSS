package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.Node;
import io.swagger.model.PagedSearchNodes;
import io.swagger.model.SearchNode;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

public class SearchApiImplTest {

  private static final String[] EMPTY_DYN_PROPS = new String[20];

  private SearchApiImpl searchApi;
  private SearchService searchService;
  private org.alfresco.service.cmr.repository.NodeService nodeService;
  private NodesApi nodesApi;
  private ApiToolBox apiToolBox;
  private DynamicPropertyService dynamicPropertyService;

  @Before
  public void setUp() throws Exception {
    searchApi = new SearchApiImpl();
    searchService = mock(SearchService.class);
    nodeService = mock(org.alfresco.service.cmr.repository.NodeService.class);
    nodesApi = mock(NodesApi.class);
    apiToolBox = mock(ApiToolBox.class);
    dynamicPropertyService = mock(DynamicPropertyService.class);

    setField("searchService", searchService);
    setField("nodeService", nodeService);
    setField("nodesApi", nodesApi);
    setField("apiToolBox", apiToolBox);
    setField("dynamicPropertyService", dynamicPropertyService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = SearchApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(searchApi, value);
  }

  @Test(expected = EmptyQueryStringException.class)
  public void testSearchGet_whenQueryIsNull_thenThrowsException()
    throws Exception {
    searchApi.searchGet(
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );
  }

  @Test(expected = EmptyQueryStringException.class)
  public void testSearchGet_whenQueryIsEmpty_thenThrowsException()
    throws Exception {
    searchApi.searchGet(
      "",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );
  }

  @Test
  public void testSearchGet_whenValidQuery_thenReturnsResults()
    throws Exception {
    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "result-id"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(List.of(resultRef));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    Node node = new Node();
    node.setId("result-id");
    node.setName("test-doc");
    when(nodesApi.getNode(resultRef)).thenReturn(node);

    QName type = ContentModel.TYPE_CONTENT;
    when(nodeService.getType(resultRef)).thenReturn(type);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_EVENT)
    ).thenReturn(false);

    PagedSearchNodes result = searchApi.searchGet(
      "test",
      null,
      null,
      1,
      10,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );

    assertNotNull(result);
    assertEquals(1L, result.getTotal().longValue());
    assertEquals(1, result.getData().size());
    assertEquals("result-id", result.getData().get(0).getId());
    assertEquals("file", result.getData().get(0).getResultType());
  }

  @Test
  public void testSearchGet_whenNoResults_thenReturnsEmptyList()
    throws Exception {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.emptyList());
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    PagedSearchNodes result = searchApi.searchGet(
      "nonexistent",
      null,
      null,
      1,
      10,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );

    assertNotNull(result);
    assertEquals(0L, result.getTotal().longValue());
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void testSearchGet_whenLibraryFolder_thenResultTypeIsFolder()
    throws Exception {
    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "folder-id"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(List.of(resultRef));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    Node node = new Node();
    node.setId("folder-id");
    node.setName("test-folder");
    when(nodesApi.getNode(resultRef)).thenReturn(node);

    when(nodeService.getType(resultRef)).thenReturn(ContentModel.TYPE_FOLDER);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_EVENT)
    ).thenReturn(false);

    PagedSearchNodes result = searchApi.searchGet(
      "folder",
      null,
      null,
      1,
      10,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );

    assertEquals("folder", result.getData().get(0).getResultType());
  }

  @Test
  public void testSearchGet_whenNewsgroupTopic_thenResultTypeIsTopic()
    throws Exception {
    NodeRef resultRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "topic-id"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(List.of(resultRef));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    Node node = new Node();
    node.setId("topic-id");
    node.setName("test-topic");
    when(nodesApi.getNode(resultRef)).thenReturn(node);

    when(nodeService.getType(resultRef)).thenReturn(ForumModel.TYPE_TOPIC);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_NEWSGROUP)
    ).thenReturn(true);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_INFORMATION)
    ).thenReturn(false);
    when(
      nodeService.hasAspect(resultRef, CircabcModel.ASPECT_EVENT)
    ).thenReturn(false);

    PagedSearchNodes result = searchApi.searchGet(
      "topic",
      null,
      null,
      1,
      10,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );

    assertEquals("topic", result.getData().get(0).getResultType());
    assertEquals("topic-id", result.getData().get(0).getTargetNode());
  }

  @Test
  public void testSearchGet_whenNodeIdProvided_thenPathFilterApplied()
    throws Exception {
    String nodeId = "node-123";
    NodeRef targetRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      nodeId
    );
    when(apiToolBox.getPathFromSpaceRef(eq(targetRef), eq(true))).thenReturn(
      "/app:company_home/test/path//*"
    );

    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.emptyList());
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);

    PagedSearchNodes result = searchApi.searchGet(
      "test",
      nodeId,
      null,
      1,
      10,
      null,
      null,
      null,
      (Date) null,
      (Date) null,
      (Date) null,
      (Date) null,
      null,
      null,
      null,
      null,
      EMPTY_DYN_PROPS,
      null,
      false
    );

    assertNotNull(result);
    verify(apiToolBox).getPathFromSpaceRef(eq(targetRef), eq(true));
  }
}
