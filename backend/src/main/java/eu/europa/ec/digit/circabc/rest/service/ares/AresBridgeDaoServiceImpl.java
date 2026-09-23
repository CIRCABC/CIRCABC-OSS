package eu.europa.ec.digit.circabc.rest.service.ares;

import io.swagger.model.db.AresBridgeDAO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis-backed implementation of {@link AresBridgeDaoService}.
 *
 * <p>This class persists and queries ARES bridge records through a configured
 * {@link SqlSessionTemplate}, delegating to the mapped statements declared under
 * the {@code AresBridge} MyBatis namespace (for example
 * {@code AresBridge.insert_request}, {@code AresBridge.insert_response} and
 * {@code AresBridge.select_responses}). Each operation builds a parameter map
 * whose keys match the placeholders expected by the corresponding SQL mapping.
 *
 * <p>The {@link SqlSessionTemplate} is injected via its setter (Spring bean
 * configuration), keeping the higher level ARES bridge logic agnostic of the
 * underlying database access details.
 */
public class AresBridgeDaoServiceImpl implements AresBridgeDaoService {

  /** Parameter-map key used to bind the ARES transaction identifier in SQL mappings. */
  private static final String TRANSACTION_ID = "transactionId";

  /** Template used to execute the mapped MyBatis statements against the database. */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Updates a previously persisted ARES bridge record, identified by its
   * transaction, to reflect the given request type.
   *
   * <p>Executes the {@code AresBridge.update_response} mapped statement.
   *
   * @param transactionId the unique identifier of the ARES transaction to update
   * @param requestType the request type to associate with the transaction
   */
  @Override
  public void updateResponse(String transactionId, String requestType) {
    Map<String, Object> params = new HashMap<>();

    params.put(TRANSACTION_ID, transactionId);
    params.put("requestType", requestType);

    sqlSessionTemplate.update("AresBridge.update_response", params);
  }

  /**
   * Persists an ARES response received for a previously submitted request.
   *
   * <p>Executes the {@code AresBridge.insert_response} mapped statement.
   *
   * @param transactionId the unique identifier of the ARES transaction the
   *     response relates to
   * @param requestType the type of request the response corresponds to
   * @param documentId the identifier of the document registered in ARES
   * @param saveNumber the ARES save number assigned to the document
   * @param registrationNumber the ARES registration number assigned to the document
   */
  @Override
  public void saveResponse(
    String transactionId,
    String requestType,
    String documentId,
    String saveNumber,
    String registrationNumber
  ) {
    Map<String, Object> params = new HashMap<>();

    params.put(TRANSACTION_ID, transactionId);
    params.put("requestType", requestType);
    params.put("documentId", documentId);
    params.put("saveNumber", saveNumber);
    params.put("registrationNumber", registrationNumber);

    sqlSessionTemplate.insert("AresBridge.insert_response", params);
  }

  /**
   * Persists an outgoing ARES registration request so that its lifecycle can be
   * tracked and later correlated with the corresponding response.
   *
   * <p>Executes the {@code AresBridge.insert_request} mapped statement.
   *
   * @param groupId the identifier of the interest group the request originates from
   * @param transactionId the unique identifier assigned to the ARES transaction
   * @param nodeId the identifier of the Alfresco node (document) being registered
   * @param versionLabel the version label of the node being registered
   * @param name the name of the document being registered
   */
  @Override
  public void saveRequest(
    String groupId,
    String transactionId,
    String nodeId,
    String versionLabel,
    String name
  ) {
    Map<String, Object> params = new HashMap<>();

    params.put("groupId", groupId);
    params.put(TRANSACTION_ID, transactionId);
    params.put("nodeId", nodeId);
    params.put("versionLabel", versionLabel);
    params.put("name", name);

    sqlSessionTemplate.insert("AresBridge.insert_request", params);
  }

  /**
   * Retrieves all persisted ARES bridge records.
   *
   * <p>Executes the {@code AresBridge.select_responses} mapped statement.
   *
   * @return the list of all {@link AresBridgeDAO} records; never {@code null}
   */
  @Override
  public List<AresBridgeDAO> getResponses() {
    return sqlSessionTemplate.selectList("AresBridge.select_responses");
  }

  /**
   * Retrieves the ARES bridge records associated with a given Alfresco node.
   *
   * <p>Executes the {@code AresBridge.select_responses_by_node_id} mapped statement.
   *
   * @param nodeId the identifier of the Alfresco node (document) to filter by
   * @return the list of matching {@link AresBridgeDAO} records; never {@code null}
   */
  @Override
  public List<AresBridgeDAO> getResponsesByNodeId(String nodeId) {
    Map<String, Object> params = new HashMap<>();
    params.put("nodeId", nodeId);
    return sqlSessionTemplate.selectList(
      "AresBridge.select_responses_by_node_id",
      params
    );
  }

  /**
   * Retrieves the ARES bridge records associated with a given interest group.
   *
   * <p>Executes the {@code AresBridge.select_responses_by_group_id} mapped statement.
   *
   * @param groupId the identifier of the interest group to filter by
   * @return the list of matching {@link AresBridgeDAO} records; never {@code null}
   */
  @Override
  public List<AresBridgeDAO> getResponsesByGroupId(String groupId) {
    Map<String, Object> params = new HashMap<>();

    params.put("groupId", groupId);

    return sqlSessionTemplate.selectList(
      "AresBridge.select_responses_by_group_id",
      params
    );
  }

  /**
   * Returns the MyBatis session template used to execute the mapped statements.
   *
   * @return the configured {@link SqlSessionTemplate}, or {@code null} if not yet set
   */
  public SqlSessionTemplate getSqlSessionTemplate() {
    return sqlSessionTemplate;
  }

  /**
   * Injects the MyBatis session template used to execute the mapped statements.
   *
   * @param sqlSessionTemplate the {@link SqlSessionTemplate} to use for database access
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }
}
