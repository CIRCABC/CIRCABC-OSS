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
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis-backed implementation of {@link AutoUploadConfigurationService}.
 *
 * <p>Persists and retrieves auto-upload {@link Configuration} entries in the CIRCABC audit
 * database. All operations are delegated to a Spring-managed {@link SqlSessionTemplate}, using the
 * MyBatis statements declared under the {@code AutoUploadConfiguration} mapper namespace.
 *
 * <p>The auto-upload feature lets an Interest Group be configured so that documents are uploaded
 * automatically; in the CIRCABC enterprise edition the actual upload is handled by the Oracle
 * Service Bus, while this service manages the associated configuration records.
 *
 * @author beaurpi
 */
public class AutoUploadConfigurationServiceImpl
  implements AutoUploadConfigurationService
{

  /**
   * MyBatis session template used to execute the {@code AutoUploadConfiguration} mapper statements.
   * Injected via {@link #setSqlSessionTemplate(SqlSessionTemplate)}.
   */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Inserts a new auto-upload configuration into the CIRCABC audit database.
   *
   * @param config the configuration to persist
   */
  /* (non-Javadoc)
   * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#registerConfiguration(eu.cec.digit.circabc.repo.config.auto.upload.Configuration)
   */
  @Override
  public void registerConfiguration(Configuration config) {
    sqlSessionTemplate.insert(
      "AutoUploadConfiguration.insert_configuration",
      config
    );
  }

  /**
   * Lists all auto-upload configurations registered for a given Interest Group.
   *
   * @param igName the name of the Interest Group whose configurations are requested
   * @return the list of matching configurations; empty if none are defined
   */
  /* (non-Javadoc)
   * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#listConfigurations(java.lang.String)
   */
  @Override
  public List<Configuration> listConfigurations(String igName) {
    return sqlSessionTemplate.selectList(
      "AutoUploadConfiguration.select_all_configurations",
      igName
    );
  }

  /**
   * Deletes an existing auto-upload configuration from the CIRCABC audit database.
   *
   * @param config the configuration to remove
   */
  /* (non-Javadoc)
   * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#deleteConfiguration(eu.cec.digit.circabc.repo.config.auto.upload.Configuration)
   */
  @Override
  public void deleteConfiguration(Configuration config) {
    sqlSessionTemplate.delete(
      "AutoUploadConfiguration.delete_configuration",
      config
    );
  }

  /**
   * Updates an existing auto-upload configuration in the CIRCABC audit database.
   *
   * @param config the configuration carrying the updated values
   */
  /* (non-Javadoc)
   * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#updateConfiguration(eu.cec.digit.circabc.repo.config.auto.upload.Configuration)
   */
  @Override
  public void updateConfiguration(Configuration config) {
    sqlSessionTemplate.update(
      "AutoUploadConfiguration.update_configuration",
      config
    );
  }

  /**
   * Retrieves a single auto-upload configuration by its numeric identifier.
   *
   * @param idConfig the configuration identifier
   * @return the matching configuration, or {@code null} if none exists for the given id
   */
  @Override
  public Configuration getConfigurationById(Integer idConfig) {
    return (Configuration) sqlSessionTemplate.selectOne(
      "AutoUploadConfiguration.select_configuration_by_id",
      idConfig.toString()
    );
  }

  /**
   * Retrieves the auto-upload configuration associated with a given document node.
   *
   * @param nodeRef the Alfresco node reference of the document
   * @return the matching configuration, or {@code null} if the node has no configuration
   */
  @Override
  public Configuration getConfigurationByNodeRef(NodeRef nodeRef) {
    return (Configuration) sqlSessionTemplate.selectOne(
      "AutoUploadConfiguration.select_configuration_by_file_ref",
      nodeRef.toString()
    );
  }

  /**
   * Retrieves every auto-upload configuration stored in the database, across all Interest Groups.
   *
   * @return the complete list of configurations; empty if none are defined
   */
  @Override
  public List<Configuration> getAllConfigurations() {
    return sqlSessionTemplate.selectList(
      "AutoUploadConfiguration.select_all_configurations_all"
    );
  }

  /** @param sqlSessionTemplate the sqlSessionTemplate to set */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }
}
