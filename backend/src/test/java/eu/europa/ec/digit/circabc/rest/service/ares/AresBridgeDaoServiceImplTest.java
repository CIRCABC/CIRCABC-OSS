package eu.europa.ec.digit.circabc.rest.service.ares;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.db.AresBridgeDAO;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class AresBridgeDaoServiceImplTest {

  private AresBridgeDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    service = new AresBridgeDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    setField("sqlSessionTemplate", sqlSessionTemplate);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = AresBridgeDaoServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testUpdateResponse_whenCalled_thenDelegatesUpdate() {
    service.updateResponse("tx-123", "SAVE");

    verify(sqlSessionTemplate).update(
      eq("AresBridge.update_response"),
      argThat(arg -> {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) arg;
        return (
          "tx-123".equals(params.get("transactionId")) &&
          "SAVE".equals(params.get("requestType"))
        );
      })
    );
  }

  @Test
  public void testSaveResponse_whenCalled_thenInsertsWithAllParams() {
    service.saveResponse("tx-456", "REGISTER", "doc-1", "SN-1", "RN-1");

    verify(sqlSessionTemplate).insert(
      eq("AresBridge.insert_response"),
      argThat(arg -> {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) arg;
        return (
          "tx-456".equals(params.get("transactionId")) &&
          "REGISTER".equals(params.get("requestType")) &&
          "doc-1".equals(params.get("documentId")) &&
          "SN-1".equals(params.get("saveNumber")) &&
          "RN-1".equals(params.get("registrationNumber"))
        );
      })
    );
  }

  @Test
  public void testSaveRequest_whenCalled_thenInsertsWithAllParams() {
    service.saveRequest("group-1", "tx-789", "node-1", "1.0", "file.pdf");

    verify(sqlSessionTemplate).insert(
      eq("AresBridge.insert_request"),
      argThat(arg -> {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) arg;
        return (
          "group-1".equals(params.get("groupId")) &&
          "tx-789".equals(params.get("transactionId")) &&
          "node-1".equals(params.get("nodeId")) &&
          "1.0".equals(params.get("versionLabel")) &&
          "file.pdf".equals(params.get("name"))
        );
      })
    );
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetResponses_whenCalled_thenReturnsListFromTemplate() {
    AresBridgeDAO dao = new AresBridgeDAO();
    List<AresBridgeDAO> expected = Arrays.asList(dao);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("AresBridge.select_responses");

    List<AresBridgeDAO> result = service.getResponses();

    assertEquals(expected, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetResponses_whenEmpty_thenReturnsEmptyList() {
    doReturn(Collections.emptyList())
      .when(sqlSessionTemplate)
      .selectList("AresBridge.select_responses");

    List<AresBridgeDAO> result = service.getResponses();

    assertTrue(result.isEmpty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetResponsesByNodeId_whenCalled_thenPassesNodeIdParam() {
    AresBridgeDAO dao = new AresBridgeDAO();
    List<AresBridgeDAO> expected = Arrays.asList(dao);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(
        eq("AresBridge.select_responses_by_node_id"),
        argThat(arg -> {
          Map<String, Object> params = (Map<String, Object>) arg;
          return "node-42".equals(params.get("nodeId"));
        })
      );

    List<AresBridgeDAO> result = service.getResponsesByNodeId("node-42");

    assertEquals(expected, result);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetResponsesByGroupId_whenCalled_thenPassesGroupIdParam() {
    AresBridgeDAO dao = new AresBridgeDAO();
    List<AresBridgeDAO> expected = Arrays.asList(dao);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(
        eq("AresBridge.select_responses_by_group_id"),
        argThat(arg -> {
          Map<String, Object> params = (Map<String, Object>) arg;
          return "group-99".equals(params.get("groupId"));
        })
      );

    List<AresBridgeDAO> result = service.getResponsesByGroupId("group-99");

    assertEquals(expected, result);
  }

  @Test
  public void testGetSetSqlSessionTemplate() {
    SqlSessionTemplate another = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(another);
    assertEquals(another, service.getSqlSessionTemplate());
  }
}
