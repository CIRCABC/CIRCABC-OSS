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
package eu.cec.digit.circabc.repo.config.auto.upload;

import eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.List;

/** @author beaurpi */
public class AutoUploadConfigurationServiceImpl implements AutoUploadConfigurationService {

    private SqlSessionTemplate sqlSessionTemplate = null;

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#registerConfiguration(eu.cec.digit.circabc.repo.config.auto.upload.Configuration)
     */
    @Override
    public void registerConfiguration(Configuration config) {
        sqlSessionTemplate.insert("AutoUploadConfiguration.insert_configuration", config);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#listConfigurations(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Configuration> listConfigurations(String igName) {

        return (List<Configuration>)
                sqlSessionTemplate.selectList("AutoUploadConfiguration.select_all_configurations", igName);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#deleteConfiguration(eu.cec.digit.circabc.repo.config.auto.upload.Configuration)
     */
    @Override
    public void deleteConfiguration(Configuration config) {

        sqlSessionTemplate.delete("AutoUploadConfiguration.delete_configuration", config);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.config.auto.upload.AutoUploadConfigurationService#updateConfiguration(eu.cec.digit.circabc.repo.config.auto.upload.Configuration)
     */
    @Override
    public void updateConfiguration(Configuration config) {

        sqlSessionTemplate.update("AutoUploadConfiguration.update_configuration", config);
    }

    @Override
    public Configuration getConfigurationById(Integer idConfig) {

        return (Configuration)
                sqlSessionTemplate.selectOne(
                        "AutoUploadConfiguration.select_configuration_by_id", idConfig.toString());
    }

    @Override
    public Configuration getConfigurationByNodeRef(NodeRef nodeRef) {

        return (Configuration)
                sqlSessionTemplate.selectOne(
                        "AutoUploadConfiguration.select_configuration_by_file_ref", nodeRef.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Configuration> getAllConfigurations() {

        return (List<Configuration>)
                sqlSessionTemplate.selectList("AutoUploadConfiguration.select_all_configurations_all");
    }

    /** @param sqlSessionTemplate the sqlSessionTemplate to set */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }
}
