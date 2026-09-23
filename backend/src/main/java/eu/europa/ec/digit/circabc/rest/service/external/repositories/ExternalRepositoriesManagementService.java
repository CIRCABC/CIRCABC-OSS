/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.external.repositories;

import io.swagger.model.RepositoryConfiguration;
import java.util.Collection;

/**
 * Service contract for managing the publication of CIRCABC content to external repositories.
 *
 * <p>Implementations are responsible for maintaining the set of external repositories configured
 * for a given container node, publishing document metadata to those repositories, and tracking
 * whether a specific document has already been published.
 *
 * @author schwerr
 */
public interface ExternalRepositoriesManagementService {
  /**
   * Publishes (or records the publication of) a document's metadata to the named external
   * repository.
   *
   * @param repositoryName the name of the target external repository
   * @param nodeId the identifier of the CIRCABC node (document) being published
   * @param documentId the identifier assigned to the document in the external repository
   * @param saveNumber the save/version number associated with the publication
   * @param registartionNumber the registration number assigned to the published document
   * @param requestType the type of publishing request being performed
   * @param transactionId the identifier of the transaction under which the publication occurs
   */
  void saveExternalMetadata(
    String repositoryName,
    String nodeId,
    String documentId,
    String saveNumber,
    String registartionNumber,
    String requestType,
    String transactionId
  );

  /**
   * Returns all external repositories configured for the given parent (container) node.
   *
   * @param parentNodeId the identifier of the parent node whose configured repositories are
   *     requested
   * @return the collection of {@link RepositoryConfiguration} entries configured for the node; may
   *     be empty if none are configured
   */
  Collection<RepositoryConfiguration> getConfiguredRepositories(
    String parentNodeId
  );

  /**
   * Adds an external repository to the configuration of the given parent (container) node.
   *
   * @param parentNodeId the identifier of the parent node to which the repository is added
   * @param configuration the {@link RepositoryConfiguration} describing the repository to add
   */
  void addRepository(
    String parentNodeId,
    RepositoryConfiguration configuration
  );

  /**
   * Removes the named external repository from the configuration of the given parent (container)
   * node.
   *
   * @param parentNodeId the identifier of the parent node from which the repository is removed
   * @param repositoryName the name of the repository configuration to remove
   */
  void removeRepository(String parentNodeId, String repositoryName);

  /**
   * Indicates whether the given document has already been published to the named external
   * repository.
   *
   * @param repositoryName the name of the external repository to check against
   * @param nodeId the identifier of the CIRCABC node (document) to check
   * @return {@code true} if the document was already published to the repository, {@code false}
   *     otherwise
   */
  boolean wasPublishedTo(String repositoryName, String nodeId);
}
