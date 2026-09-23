package io.swagger.api;

import io.swagger.model.*;
import io.swagger.model.db.AresBridgeDAO;
import java.util.Collection;

/**
 * Business operations for integrating CIRCABC with the ARES Bridge external repository.
 *
 * <p>This service abstracts the interaction between CIRCABC and the ARES Bridge, a
 * document exchange system used by the European Commission. It covers three concerns:
 *
 * <ul>
 *   <li><b>Authentication tokens</b> &mdash; generating signed tickets and validating
 *       incoming ARES Bridge authorization headers/tokens against the configured API
 *       key and secret.
 *   <li><b>External repository configuration</b> &mdash; listing, adding and removing
 *       repository configurations attached to a given CIRCABC node, as well as
 *       exposing the set of available external repository types.
 *   <li><b>Transaction logging</b> &mdash; persisting exchange transactions and
 *       retrieving the recorded log entries by node or by group.
 * </ul>
 *
 * <p>Implementations are wired into the Spring context and typically injected into the
 * corresponding webscript endpoints.
 */
public interface AresBridgeApi {
  /**
   * Generates a signed authentication ticket (token) for an outgoing ARES Bridge
   * request, using the configured API key and secret.
   *
   * @param requestDate the request date, as used in the signature computation
   * @param httpVerb the HTTP method of the request the ticket is generated for
   * @param path the request path the ticket is generated for
   * @return the generated authentication token
   */
  String getTicket(String requestDate, String httpVerb, String path);

  /**
   * Returns the external repository configurations attached to the given node.
   *
   * @param id the identifier of the parent node whose configured repositories are
   *     requested
   * @return the collection of repository configurations associated with the node
   */
  Collection<RepositoryConfiguration> getExternalRepositories(String id);

  /**
   * Adds a new external repository configuration with the given name to the specified
   * node.
   *
   * @param id the identifier of the parent node the repository is attached to
   * @param name the name of the repository configuration to add
   */
  void addExternalRepositories(String id, String name);

  /**
   * Removes the named external repository configuration from the specified node.
   *
   * @param id the identifier of the parent node the repository is attached to
   * @param name the name of the repository configuration to remove
   */
  void deleteExternalRepository(String id, String name);

  /**
   * Returns the set of external repository types available for configuration.
   *
   * @return the collection of available external repository identifiers
   */
  Collection<String> getAvailableExternalRepositories();

  /**
   * Validates an incoming ARES Bridge {@code Authorization} header by extracting the
   * embedded ticket and verifying it against the configured API key and secret.
   *
   * @param dateHeader the request date header used in the signature verification
   * @param authorizationHeader the raw authorization header value to validate
   * @param path the request path used in the signature verification
   * @return {@code true} if the header contains a valid ticket, {@code false} otherwise
   */
  boolean validateAuthorizationHeader(
    String dateHeader,
    String authorizationHeader,
    String path
  );

  /**
   * Validates a raw ARES Bridge token against the configured API key and secret.
   *
   * @param dateHeader the request date header used in the signature verification
   * @param token the token to validate
   * @param path the request path used in the signature verification
   * @param method the HTTP method used in the signature verification
   * @return {@code true} if the token is valid, {@code false} otherwise
   */
  boolean validateToken(
    String dateHeader,
    String token,
    String path,
    String method
  );

  /**
   * Persists an ARES Bridge exchange transaction for later auditing.
   *
   * @param groupId the identifier of the group the transaction belongs to
   * @param repositoryId the identifier of the external repository involved
   * @param transactionId the unique identifier of the transaction
   * @param nodeId the identifier of the node involved in the transaction
   * @param versionLabel the version label of the node at the time of the transaction
   * @param name the name associated with the transaction
   */
  void saveTransaction(
    String groupId,
    String repositoryId,
    String transactionId,
    String nodeId,
    String versionLabel,
    String name
  );

  /**
   * Retrieves the ARES Bridge transaction log entries recorded for a given node.
   *
   * @param nodeId the identifier of the node whose log entries are requested
   * @param name the name qualifier for the lookup
   * @return the collection of matching transaction log entries
   */
  Collection<AresBridgeDAO> nodeLog(String nodeId, String name);

  /**
   * Retrieves the ARES Bridge transaction log entries recorded for a given group.
   *
   * @param groupId the identifier of the group whose log entries are requested
   * @param name the name qualifier for the lookup
   * @return the collection of matching transaction log entries
   */
  Collection<AresBridgeDAO> groupLog(String groupId, String name);
}
