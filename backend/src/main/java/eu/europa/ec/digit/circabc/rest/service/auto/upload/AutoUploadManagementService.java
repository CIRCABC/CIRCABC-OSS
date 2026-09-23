/**
 * Copyright 2006 European Community
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
 */
/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.auto.upload;

import io.swagger.model.Configuration;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;

/**
 * Service contract for managing CIRCABC's auto-upload (FTP drop) feature.
 *
 * <p>Auto-upload configurations describe how files that are dropped into a monitored (FTP) location
 * should be ingested into an Interest Group space of the Alfresco repository. This service is
 * responsible for the full lifecycle of those configurations and for the repository-side operations
 * triggered when a new file is detected:
 *
 * <ul>
 *   <li>persisting, updating, retrieving and deleting configurations in the CIRCABC audit database;
 *   <li>installing and removing the Alfresco rule/action that automatically extracts uploaded
 *       archives on a target space;
 *   <li>creating and updating repository content from detected files, and extracting ZIP archives;
 *   <li>recording the outcome of an auto-upload job and optionally notifying users by e-mail;
 *   <li>locking and unlocking a configuration while its job runs to prevent concurrent processing.
 * </ul>
 *
 * @author beaurpi
 */
public interface AutoUploadManagementService {
  /**
   * Inserts a single auto-upload configuration into the CIRCABC audit database.
   *
   * @param config the configuration to persist
   * @throws SQLException if the configuration cannot be stored
   */
  void registerConfiguration(Configuration config) throws SQLException;

  /**
   * Retrieves all auto-upload configurations defined for a given Interest Group.
   *
   * @param igName the name of the Interest Group
   * @return the list of configurations belonging to the Interest Group (never {@code null})
   * @throws SQLException if the configurations cannot be read
   */
  List<Configuration> listConfigurations(String igName) throws SQLException;

  /**
   * Removes an auto-upload configuration from the database.
   *
   * @param config the configuration to delete
   * @throws SQLException if the configuration cannot be removed
   */
  void deleteConfiguration(Configuration config) throws SQLException;

  /**
   * Updates an existing auto-upload configuration in the audit database.
   *
   * @param config the configuration carrying the new values to persist
   * @throws SQLException if the configuration cannot be updated
   */
  void updateConfiguration(Configuration config) throws SQLException;

  /**
   * Retrieves a single auto-upload configuration by its database identifier.
   *
   * @param idConfig the identifier of the configuration
   * @return the matching configuration, or {@code null} if none exists
   * @throws SQLException if the configuration cannot be read
   */
  Configuration getConfigurationById(Integer idConfig) throws SQLException;

  /**
   * Retrieves the auto-upload configuration associated with a given repository node.
   *
   * @param nodeRef the reference of the node the configuration is bound to
   * @return the matching configuration, or {@code null} if none exists
   * @throws SQLException if the configuration cannot be read
   */
  Configuration getConfigurationByNodeRef(NodeRef nodeRef) throws SQLException;

  /**
   * Builds the default Alfresco rule and its associated auto-extract action for the given space,
   * without persisting it on the space.
   *
   * @param spaceRef the reference of the space the rule targets
   * @return the constructed extract rule
   */
  Rule buildDefaultExtractRule(NodeRef spaceRef);

  /**
   * Builds the default auto-extract rule and action and saves it on the given space so that
   * uploaded archives are extracted automatically.
   *
   * @param spaceRef the reference of the space the rule is applied to
   */
  void addAutoExtractRuleToSpace(NodeRef spaceRef);

  /**
   * Removes the auto-extract rule previously applied to the given space.
   *
   * @param spaceRef the reference of the space the rule is removed from
   */
  void removeAutoExtractRule(NodeRef spaceRef);

  /**
   * Retrieves every auto-upload configuration stored in the database, regardless of Interest Group.
   *
   * @return the complete list of configurations (never {@code null})
   * @throws SQLException if the configurations cannot be read
   */
  List<Configuration> listAllConfigurations() throws SQLException;

  /**
   * Updates the content of an existing repository node with the given file. This is invoked when a
   * new file has been detected on the FTP drop location for an already existing document.
   *
   * @param fileRef the reference of the repository node to update
   * @param file the newly detected file whose content replaces the node content
   */
  void updateContent(NodeRef fileRef, File file);

  /**
   * Records the result of an auto-upload job in the audit trail.
   *
   * @param conf the configuration the job was run for
   * @param result the outcome of the job
   * @param jobResultInfo additional human-readable details about the job result
   */
  void logJobResult(
    Configuration conf,
    AutoUploadJobResult result,
    String jobResultInfo
  );

  /**
   * Sends an e-mail notification about a job's outcome when the configuration requests it.
   *
   * @param conf the configuration the job was run for
   * @param result the outcome of the job to report
   */
  void sendJobNofitication(Configuration conf, AutoUploadJobResult result);

  /**
   * Extracts the archive held by the given node using the CIRCABC importer executor.
   *
   * @param fileRef the reference of the ZIP node to extract
   */
  void extractZip(NodeRef fileRef);

  /**
   * Indicates whether the document referenced by the given node still exists in the repository.
   *
   * @param fileRef the reference of the document to check
   * @return {@code true} if the document exists, {@code false} otherwise
   */
  boolean documentExists(NodeRef fileRef);

  /**
   * Creates repository content for a configuration that does not yet carry a file node reference.
   * The created node is returned so the caller can update the configuration with its reference
   * immediately afterwards.
   *
   * @param fileRef the reference associated with the configuration, if any
   * @param tmpFile the temporary file holding the content to store
   * @param destinationFolder the folder the new content node is created in
   * @param fileName the name to give to the created content node
   * @return the reference of the newly created content node
   */
  NodeRef createContent(
    NodeRef fileRef,
    File tmpFile,
    NodeRef destinationFolder,
    String fileName
  );

  /**
   * Locks the job of the given configuration to prevent concurrent auto-upload processing.
   *
   * @param idConfiguration the identifier of the configuration whose job is locked
   * @return the number of affected rows, or a status indicator of the lock operation
   */
  Integer lockJobFile(Long idConfiguration);

  /**
   * Releases the lock previously acquired on the job of the given configuration.
   *
   * @param idConfiguration the identifier of the configuration whose job is unlocked
   * @return the number of affected rows, or a status indicator of the unlock operation
   */
  Integer unlockJobFile(Long idConfiguration);
}
