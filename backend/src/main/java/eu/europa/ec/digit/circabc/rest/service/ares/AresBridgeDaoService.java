package eu.europa.ec.digit.circabc.rest.service.ares;

import io.swagger.model.db.AresBridgeDAO;
import java.util.List;

/**
 * Data access service for the ARES bridge integration.
 *
 * <p>ARES is the European Commission's document registration system. This service
 * abstracts the persistence layer used to track the exchange of requests and
 * responses between CIRCABC and ARES. It is responsible for recording outgoing
 * registration requests, storing the corresponding responses (including the
 * assigned save and registration numbers) and querying previously persisted
 * {@link AresBridgeDAO} records.
 *
 * <p>Implementations of this interface encapsulate the underlying storage details
 * (for example database access via Alfresco/Liquibase managed tables) so that
 * higher level ARES bridge logic can remain storage agnostic.
 */
public interface AresBridgeDaoService {
  /**
   * Updates a previously persisted ARES bridge record, identified by its
   * transaction, to reflect the given request type.
   *
   * @param transactionId the unique identifier of the ARES transaction to update
   * @param requestType the request type to associate with the transaction
   */
  void updateResponse(String transactionId, String requestType);

  /**
   * Persists an ARES response received for a previously submitted request.
   *
   * @param transactionId the unique identifier of the ARES transaction the
   *     response relates to
   * @param requestType the type of request the response corresponds to
   * @param documentId the identifier of the document registered in ARES
   * @param saveNumber the ARES save number assigned to the document
   * @param registrationNumber the ARES registration number assigned to the document
   */
  void saveResponse(
    String transactionId,
    String requestType,
    String documentId,
    String saveNumber,
    String registrationNumber
  );

  /**
   * Persists an outgoing ARES registration request so that its lifecycle can be
   * tracked and later correlated with the corresponding response.
   *
   * @param groupId the identifier of the interest group the request originates from
   * @param transactionId the unique identifier assigned to the ARES transaction
   * @param nodeId the identifier of the Alfresco node (document) being registered
   * @param versionLabel the version label of the node being registered
   * @param name the name of the document being registered
   */
  void saveRequest(
    String groupId,
    String transactionId,
    String nodeId,
    String versionLabel,
    String name
  );

  /**
   * Retrieves all persisted ARES bridge records.
   *
   * @return the list of all {@link AresBridgeDAO} records; never {@code null}
   */
  List<AresBridgeDAO> getResponses();

  /**
   * Retrieves the ARES bridge records associated with a given Alfresco node.
   *
   * @param nodeId the identifier of the Alfresco node (document) to filter by
   * @return the list of matching {@link AresBridgeDAO} records; never {@code null}
   */
  List<AresBridgeDAO> getResponsesByNodeId(String nodeId);

  /**
   * Retrieves the ARES bridge records associated with a given interest group.
   *
   * @param groupId the identifier of the interest group to filter by
   * @return the list of matching {@link AresBridgeDAO} records; never {@code null}
   */
  List<AresBridgeDAO> getResponsesByGroupId(String groupId);
}
