package io.swagger.api;

import static io.swagger.util.ares.TokenUtils.generateToken;

import eu.europa.ec.digit.circabc.rest.service.ares.AresBridgeDaoService;
import eu.europa.ec.digit.circabc.rest.service.external.repositories.ExternalRepositoriesManagementService;
import io.swagger.model.RepositoryConfiguration;
import io.swagger.model.db.AresBridgeDAO;
import io.swagger.util.Converter;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Default implementation of {@link AresBridgeApi} providing integration with the
 * ARES Bridge external repository.
 *
 * <p>ARES Bridge is an external document management system that CIRCABC content can be
 * pushed to. This service concentrates the operations required for that integration:
 *
 * <ul>
 *   <li>Generating and validating the authentication tokens used to sign requests
 *       exchanged with the ARES Bridge system (see {@code io.swagger.util.ares.TokenUtils}).</li>
 *   <li>Managing the external repository configurations attached to a CIRCABC node,
 *       delegating to {@link ExternalRepositoriesManagementService}.</li>
 *   <li>Persisting and querying the transaction log of requests sent to ARES Bridge,
 *       delegating to {@link AresBridgeDaoService}.</li>
 * </ul>
 *
 * <p>Connection and credential settings (API key, secret, application name and the
 * various URLs) are injected from configuration properties via {@link Value}.
 */
public class AresBridgeApiImpl implements AresBridgeApi {

  /** Identifier of the ARES Bridge repository type; used as a marker in authorization headers. */
  public static final String ARES_BRIDGE = "AresBridge";

  /** Application name registered with ARES Bridge, injected from the {@code ab.application.name} property. */
  @Value("${ab.application.name}")
  private String applicationName;

  /** API key used to identify this application to ARES Bridge, injected from the {@code ab.apiKey} property. */
  @Value("${ab.apiKey}")
  private String apiKey;

  /** Shared secret used to sign and validate ARES Bridge tokens, injected from the {@code ab.secret} property. */
  @Value("${ab.secret}")
  private String secret;

  /** Base URL of the ARES Bridge system, injected from the {@code ab.base.url} property. */
  @Value("${ab.base.url}")
  private String baseURL;

  /** Service (API) URL of the ARES Bridge system, injected from the {@code ab.service.url} property. */
  @Value("${ab.service.url}")
  private String serviceURL;

  /** User interface URL of the ARES Bridge system, injected from the {@code ab.ui.url} property. */
  @Value("${ab.ui.url}")
  private String uiURL;

  /** Service managing the external repository configurations bound to CIRCABC nodes. */
  @Autowired
  private ExternalRepositoriesManagementService externalRepositoriesManagementService;

  /** Service persisting and retrieving the ARES Bridge transaction log entries. */
  @Autowired
  private AresBridgeDaoService aresBridgeDaoService;

  /** Logger for this class. */
  private static final Log logger = LogFactory.getLog(AresBridgeApiImpl.class);

  /**
   * Generates an authentication token (ticket) used to sign a request to ARES Bridge,
   * derived from the configured API key and secret.
   *
   * @param requestDate the request date used as part of the token computation
   * @param httpVerb the HTTP method (e.g. {@code GET}, {@code POST}) the token is issued for
   * @param path the request path the token is issued for
   * @return the generated token string
   */
  @Override
  public String getTicket(String requestDate, String httpVerb, String path) {
    return generateToken(this.apiKey, this.secret, requestDate, httpVerb, path);
  }

  /**
   * Returns the external repository configurations attached to the given CIRCABC node.
   *
   * @param id the CIRCABC node identifier whose configured repositories are requested
   * @return the collection of {@link RepositoryConfiguration} configured on the node
   */
  @Override
  public Collection<RepositoryConfiguration> getExternalRepositories(
    String id
  ) {
    String parentNodeId = Converter.createNodeRefFromId(id).toString();
    return this.externalRepositoriesManagementService.getConfiguredRepositories(
      parentNodeId
    );
  }

  /**
   * Adds a new external repository configuration with the given name to the given CIRCABC node.
   *
   * @param id the CIRCABC node identifier to attach the repository to
   * @param name the name of the repository configuration to create
   */
  @Override
  public void addExternalRepositories(String id, String name) {
    String parentNodeId = Converter.createNodeRefFromId(id).toString();
    RepositoryConfiguration configuration = new RepositoryConfiguration();
    configuration.setName(name);
    this.externalRepositoriesManagementService.addRepository(
      parentNodeId,
      configuration
    );
  }

  /**
   * Removes the named external repository configuration from the given CIRCABC node.
   *
   * @param id the CIRCABC node identifier the repository is attached to
   * @param name the name of the repository configuration to remove
   */
  @Override
  public void deleteExternalRepository(String id, String name) {
    String parentNodeId = Converter.createNodeRefFromId(id).toString();
    this.externalRepositoriesManagementService.removeRepository(
      parentNodeId,
      name
    );
  }

  /**
   * Returns the identifiers of the external repository types available for configuration.
   *
   * @return a collection containing the supported repository type identifiers
   *         (currently only {@link #ARES_BRIDGE})
   */
  @Override
  public Collection<String> getAvailableExternalRepositories() {
    Collection<String> result = new ArrayList<>(1);
    result.add(ARES_BRIDGE);
    return result;
  }

  /**
   * Validates an incoming ARES Bridge {@code Authorization} header for a {@code POST} request.
   *
   * <p>The header is expected to contain both the {@link #ARES_BRIDGE} marker and the
   * configured API key; the remaining part is treated as the token and validated against
   * the configured API key and secret. An invalid or malformed header is logged and rejected.
   *
   * @param dateHeader the request date header used in token validation
   * @param authorizationHeader the raw authorization header to validate
   * @param path the request path the token was issued for
   * @return {@code true} if the header is well-formed and the token is valid; {@code false} otherwise
   */
  @Override
  public boolean validateAuthorizationHeader(
    String dateHeader,
    String authorizationHeader,
    String path
  ) {
    if (
      authorizationHeader.contains(ARES_BRIDGE) &&
      authorizationHeader.contains(apiKey)
    ) {
      String ticket = authorizationHeader
        .replace(ARES_BRIDGE, "")
        .replace(apiKey, "")
        .replace(":", "")
        .trim();
      return io.swagger.util.ares.TokenUtils.validateToken(
        ticket,
        apiKey,
        secret,
        dateHeader,
        "POST",
        path
      );
    } else {
      if (logger.isErrorEnabled()) {
        logger.error("Invalid authorization header ");
      }
      return false;
    }
  }

  /**
   * Persists a transaction record describing a request sent to ARES Bridge.
   *
   * @param groupId the identifier of the interest group the transaction relates to
   * @param repositoryId the identifier of the external repository (currently unused in persistence)
   * @param transactionId the ARES Bridge transaction identifier
   * @param nodeId the CIRCABC node identifier involved in the transaction
   * @param versionLabel the version label of the node at the time of the transaction
   * @param name the name associated with the transaction
   */
  @Override
  public void saveTransaction(
    String groupId,
    String repositoryId,
    String transactionId,
    String nodeId,
    String versionLabel,
    String name
  ) {
    this.aresBridgeDaoService.saveRequest(
      groupId,
      transactionId,
      nodeId,
      versionLabel,
      name
    );
  }

  /**
   * Validates an ARES Bridge token against the configured API key and secret for the given
   * request date, path and HTTP method.
   *
   * @param dateHeader the request date header used in token validation
   * @param token the token to validate
   * @param path the request path the token was issued for
   * @param method the HTTP method the token was issued for
   * @return {@code true} if the token is valid; {@code false} otherwise
   */
  @Override
  public boolean validateToken(
    String dateHeader,
    String token,
    String path,
    String method
  ) {
    return io.swagger.util.ares.TokenUtils.validateToken(
      token,
      apiKey,
      secret,
      dateHeader,
      method,
      path
    );
  }

  /**
   * Returns the ARES Bridge transaction log entries recorded for the given node.
   *
   * @param nodeId the CIRCABC node identifier whose log entries are requested
   * @param name a name qualifier (currently unused in the query)
   * @return the collection of {@link AresBridgeDAO} entries associated with the node
   */
  @Override
  public Collection<AresBridgeDAO> nodeLog(String nodeId, String name) {
    return aresBridgeDaoService.getResponsesByNodeId(nodeId);
  }

  /**
   * Returns the ARES Bridge transaction log entries recorded for the given interest group.
   *
   * @param groupId the interest group identifier whose log entries are requested
   * @param name a name qualifier (currently unused in the query)
   * @return the collection of {@link AresBridgeDAO} entries associated with the group
   */
  @Override
  public Collection<AresBridgeDAO> groupLog(String groupId, String name) {
    return aresBridgeDaoService.getResponsesByGroupId(groupId);
  }
}
