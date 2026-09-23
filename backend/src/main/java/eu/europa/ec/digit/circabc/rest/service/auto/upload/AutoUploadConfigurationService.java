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
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service interface for managing auto-upload configurations in CIRCABC.
 *
 * <p>Auto-upload lets documents be automatically ingested into an Interest Group's content
 * structure. This interface defines the CRUD operations used to persist and retrieve those
 * configurations from the CIRCABC audit database. In the CIRCABC enterprise edition the auto-upload
 * process itself is carried out by the Oracle Service Bus.
 *
 * @author beaurpi
 */
public interface AutoUploadConfigurationService {
  /**
   * Inserts a single auto-upload configuration into the CIRCABC audit database.
   *
   * @param config the configuration to persist
   */
  void registerConfiguration(Configuration config);

  /**
   * Retrieves all auto-upload configurations defined for a given Interest Group.
   *
   * @param igName the name of the Interest Group whose configurations should be listed
   * @return the list of configurations belonging to the Interest Group; empty if none exist
   */
  List<Configuration> listConfigurations(String igName);

  /**
   * Removes an auto-upload configuration from the audit database.
   *
   * @param config the configuration to delete
   */
  void deleteConfiguration(Configuration config);

  /**
   * Updates an existing auto-upload configuration in the audit database.
   *
   * @param config the configuration holding the updated values to persist
   */
  void updateConfiguration(Configuration config);

  /**
   * Retrieves a single auto-upload configuration by its identifier.
   *
   * @param idConfig the unique identifier of the configuration
   * @return the matching configuration, or {@code null} if none exists for the given identifier
   */
  Configuration getConfigurationById(Integer idConfig);

  /**
   * Retrieves a single auto-upload configuration associated with a document node.
   *
   * @param nodeRef the node reference of the document whose configuration should be returned
   * @return the matching configuration, or {@code null} if none is associated with the node
   */
  Configuration getConfigurationByNodeRef(NodeRef nodeRef);

  /**
   * Retrieves every auto-upload configuration stored in the audit database.
   *
   * @return the list of all configurations; empty if none exist
   */
  List<Configuration> getAllConfigurations();
}
