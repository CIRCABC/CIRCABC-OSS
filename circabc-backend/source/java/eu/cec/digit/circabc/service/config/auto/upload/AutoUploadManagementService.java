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
import org.alfresco.service.cmr.rule.Rule;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/** @author beaurpi */
public interface AutoUploadManagementService {

    /**
     * * Insert one configuration in the audit database of CIRCABC
     *
     * @param config
     * @throws SQLException
     */
    void registerConfiguration(Configuration config) throws SQLException;

    /**
     * * Get all configurations for one Interest Group
     *
     * @param igName
     * @return
     * @throws SQLException
     */
    List<Configuration> listConfigurations(String igName) throws SQLException;

    /**
     * * Remove configuration from Database
     *
     * @param config
     * @throws SQLException
     */
    void deleteConfiguration(Configuration config) throws SQLException;

    /**
     * * Update configuration inside audit database
     *
     * @param config
     * @throws SQLException
     */
    void updateConfiguration(Configuration config) throws SQLException;

    /**
     * * Get one configuration by ID.
     *
     * @param idConfig
     * @return
     * @throws SQLException
     */
    Configuration getConfigurationById(Integer idConfig) throws SQLException;

    /**
     * *
     *
     * @param nodeRef
     * @return
     * @throws SQLException
     */
    Configuration getConfigurationByNodeRef(NodeRef nodeRef) throws SQLException;

    /**
     * * Configure auto extract rule & action for given sapce noderef
     *
     * @param spaceRef
     * @return
     */
    Rule buildDefaultExtractRule(NodeRef spaceRef);

    /**
     * * save & build auto extract rule & action for the given space noderef
     *
     * @param spaceRef
     */
    void addAutoExtractRuleToSpace(NodeRef spaceRef);

    /**
     * * remove the auto extract rule applied to the space
     *
     * @param spaceRef
     */
    void removeAutoExtractRule(NodeRef spaceRef);

    /**
     * * get all configurations
     *
     * @return
     * @throws SQLException
     */
    List<Configuration> listAllConfigurations() throws SQLException;

    /**
     * * call this method to update content of node when new ftp file has been detected
     *
     * @param fileRef
     * @param file
     */
    void updateContent(NodeRef fileRef, File file);

    /**
     * * log result of auto upload
     *
     * @param conf
     * @param result
     * @param jobResultInfo
     */
    void logJobResult(Configuration conf, AutoUploadJobResult result, String jobResultInfo);

    /**
     * * send email for job if configured
     *
     * @param conf
     * @param result
     * @throws Exception
     */
    void sendJobNofitication(Configuration conf, AutoUploadJobResult result);

    /**
     * * use circabc importer executor to extract zip
     *
     * @param fileRef
     */
    void extractZip(NodeRef fileRef);

    /**
     * *
     *
     * @param fileRef
     * @return
     */
    boolean documentExists(NodeRef fileRef);

    /**
     * * create content for configuration without fileNoderef -> it will update with filenodeRef just
     * after
     *
     * @param fileRef
     * @param tmpFile
     * @return
     */
    NodeRef createContent(NodeRef fileRef, File tmpFile, NodeRef destinationFolder, String fileName);

    Integer lockJobFile(Long idConfiguration);

    Integer unlockJobFile(Long idConfiguration);
}
