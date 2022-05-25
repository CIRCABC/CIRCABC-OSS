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
package eu.cec.digit.circabc.service.config.auto.upload;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * @author beaurpi Service Interface to be able to configure autoupload inside CIRCABC Auto upload
 *     in CIRCABC enterprise is implemented by Oracle Service Bus
 */
public interface AutoUploadConfigurationService {

    /**
     * * Insert one configuration in the audit database of CIRCABC
     *
     * @param config
     */
    void registerConfiguration(Configuration config);

    /**
     * * Get all configurations for one Interest Group
     *
     * @param igName
     * @return
     */
    List<Configuration> listConfigurations(String igName);

    /**
     * * Remove configuration from Database
     *
     * @param config
     */
    void deleteConfiguration(Configuration config);

    /**
     * * Update configuration inside audit database
     *
     * @param config
     */
    void updateConfiguration(Configuration config);

    /**
     * * Get one configuration by ID.
     *
     * @param idConfig
     * @return
     */
    Configuration getConfigurationById(Integer idConfig);

    /**
     * * Get one configuration by document node ref.
     *
     * @param nodeRef
     * @return
     */
    Configuration getConfigurationByNodeRef(NodeRef nodeRef);

    /**
     * * all configurations
     *
     * @return
     */
    List<Configuration> getAllConfigurations();
}
