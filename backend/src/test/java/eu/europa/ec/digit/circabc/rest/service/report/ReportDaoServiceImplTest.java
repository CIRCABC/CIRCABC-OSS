package eu.europa.ec.digit.circabc.rest.service.report;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class ReportDaoServiceImplTest {

  private ReportDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() {
    service = new ReportDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  private void stubInitQueries() {
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcReporting.select_qname_id_by_local_name__and_uri"),
        anyMap()
      )
    ).thenReturn(1);
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcReporting.select_store_id_by_protocol_protocol"),
        anyMap()
      )
    ).thenReturn(10);
  }

  @Test
  public void testQueryDbForNumberOfDocuments_whenCalled_thenReturnsCount() {
    stubInitQueries();
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcReporting.select_count_documents"),
        any()
      )
    ).thenReturn(42);

    Integer result = service.queryDbForNumberOfDocuments();

    assertEquals(Integer.valueOf(42), result);
  }

  @Test
  public void testQueryDbForNumberOfDocuments_whenNull_thenReturnsNull() {
    stubInitQueries();
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcReporting.select_count_documents"),
        any()
      )
    ).thenReturn(null);

    Integer result = service.queryDbForNumberOfDocuments();

    assertNull(result);
  }

  @Test
  public void testGetAvailibleShareSpaces_whenResults_thenReturnsNodeRefs() {
    stubInitQueries();
    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    List<String> dbResults = Arrays.asList(
      "workspace://SpacesStore/node-1",
      "workspace://SpacesStore/node-2"
    );
    doReturn(dbResults)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcReporting.select_shared_spaces_by_ig"), any());

    List<NodeRef> result = service.getAvailibleShareSpaces(igNodeRef);

    assertEquals(2, result.size());
    assertEquals(new NodeRef("workspace://SpacesStore/node-1"), result.get(0));
    assertEquals(new NodeRef("workspace://SpacesStore/node-2"), result.get(1));
  }

  @Test
  public void testGetAvailibleShareSpaces_whenEmpty_thenReturnsEmptyList() {
    stubInitQueries();
    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcReporting.select_shared_spaces_by_ig"), any());

    List<NodeRef> result = service.getAvailibleShareSpaces(igNodeRef);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetAvailibleShareSpaces_whenIdsNull_thenReturnsEmptyList()
    throws Exception {
    // Simulate init() returning null for typeQnameId/propQnameId/storeID
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcReporting.select_qname_id_by_local_name__and_uri"),
        anyMap()
      )
    ).thenReturn(null);
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcReporting.select_store_id_by_protocol_protocol"),
        anyMap()
      )
    ).thenReturn(null);

    NodeRef igNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    // Force init to run by calling the method (idContent is null initially)
    // After init, typeQnameId/propQnameId/storeID will be null, triggering the early return
    List<NodeRef> result = service.getAvailibleShareSpaces(igNodeRef);

    assertTrue(result.isEmpty());
  }
}
