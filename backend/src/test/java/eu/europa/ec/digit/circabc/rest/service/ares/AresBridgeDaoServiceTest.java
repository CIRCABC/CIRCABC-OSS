package eu.europa.ec.digit.circabc.rest.service.ares;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.db.AresBridgeDAO;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class AresBridgeDaoServiceTest {

  private AresBridgeDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() {
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service = new AresBridgeDaoServiceImpl();
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  @Test
  public void testUpdateResponse() {
    service.updateResponse("tx-123", "SAVE");

    verify(sqlSessionTemplate).update(
      eq("AresBridge.update_response"),
      argThat(
        (Map<String, Object> params) ->
          "tx-123".equals(params.get("transactionId")) &&
          "SAVE".equals(params.get("requestType"))
      )
    );
  }

  @Test
  public void testSaveResponse() {
    service.saveResponse("tx-456", "REGISTER", "doc-1", "SN-1", "RN-1");

    verify(sqlSessionTemplate).insert(
      eq("AresBridge.insert_response"),
      argThat(
        (Map<String, Object> params) ->
          "tx-456".equals(params.get("transactionId")) &&
          "REGISTER".equals(params.get("requestType")) &&
          "doc-1".equals(params.get("documentId")) &&
          "SN-1".equals(params.get("saveNumber")) &&
          "RN-1".equals(params.get("registrationNumber"))
      )
    );
  }

  @Test
  public void testSaveRequest() {
    service.saveRequest("group-1", "tx-789", "node-1", "1.0", "file.pdf");

    verify(sqlSessionTemplate).insert(
      eq("AresBridge.insert_request"),
      argThat(
        (Map<String, Object> params) ->
          "group-1".equals(params.get("groupId")) &&
          "tx-789".equals(params.get("transactionId")) &&
          "node-1".equals(params.get("nodeId")) &&
          "1.0".equals(params.get("versionLabel")) &&
          "file.pdf".equals(params.get("name"))
      )
    );
  }

  @Test
  public void testGetResponses() {
    AresBridgeDAO dao = new AresBridgeDAO();
    dao.setTransactionId("tx-1");
    List<AresBridgeDAO> expected = Arrays.asList(dao);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("AresBridge.select_responses");

    List<AresBridgeDAO> result = service.getResponses();

    assertEquals(expected, result);
  }

  @Test
  public void testGetResponses_empty() {
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList("AresBridge.select_responses");

    List<AresBridgeDAO> result = service.getResponses();

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetResponsesByNodeId() {
    AresBridgeDAO dao = new AresBridgeDAO();
    dao.setNodeId("node-42");
    List<AresBridgeDAO> expected = Arrays.asList(dao);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("AresBridge.select_responses_by_node_id"), any(Map.class));

    List<AresBridgeDAO> result = service.getResponsesByNodeId("node-42");

    assertEquals(expected, result);
  }

  @Test
  public void testGetResponsesByGroupId() {
    AresBridgeDAO dao = new AresBridgeDAO();
    List<AresBridgeDAO> expected = Arrays.asList(dao);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(
        eq("AresBridge.select_responses_by_group_id"),
        any(Map.class)
      );

    List<AresBridgeDAO> result = service.getResponsesByGroupId("group-99");

    assertEquals(expected, result);
  }
}
