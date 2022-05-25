/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.impl.props;

import eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv;
import eu.cec.digit.circabc.business.api.props.PropertyItem;
import eu.cec.digit.circabc.business.helper.AlfrescoObjectsManager;
import eu.cec.digit.circabc.business.helper.ApplicationConfigManager;
import eu.cec.digit.circabc.business.helper.MetadataManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.ValidationUtils;
import org.alfresco.service.cmr.ml.ContentFilterLanguagesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Business service implementation manage properties.
 *
 * @author Yanick Pignot
 */
public class PropertiesBusinessImpl implements PropertiesBusinessSrv {

    private final Log logger = LogFactory.getLog(PropertiesBusinessImpl.class);

    private NodeService nodeService;
    private ContentFilterLanguagesService contentFilterLanguagesService;

    private MetadataManager metadataManager;
    private AlfrescoObjectsManager objectManager;
    private ApplicationConfigManager configManager;
    private ValidationManager validationManager;

    //--------------
    //-- public methods

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv#computeValidName(java.lang.String)
     */
    public String computeValidName(final String name) {
        return getMetadataManager().getValidName(name);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv#computeValidUniqueName(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public String computeValidUniqueName(final NodeRef parent, final String name) {
        return getMetadataManager().getValidUniqueName(parent, name);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv#computeLanguageTranslation(java.util.Locale)
     */
    public String computeLanguageTranslation(final Locale locale) {
        if (locale == null) {
            return "";
        } else {
            return contentFilterLanguagesService.getLabelByCode(locale.getLanguage());
        }
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv#getProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<String, PropertyItem> getProperties(final NodeRef nodeRef) {
        ValidationUtils.assertNodeRef(nodeRef, getValidationManager(), logger);

        // get the properties defined on the node
        final Map<QName, Serializable> nodeProps = getNodeService().getProperties(nodeRef);
        // get property configuration that specify the target client behaviour.
        final Map<String, ItemConfig> propsConfig = getConfigManager().getPropertiesDefinition(nodeRef);
        // convert the map to use circabc business style prop key (QNAme -> String)
        final Map<String, Serializable> stringKeyedProps = new HashMap<>(nodeProps.size());

        for (final Map.Entry<QName, Serializable> prop : nodeProps.entrySet()) {
            stringKeyedProps.put(
                    getObjectManager().asString(prop.getKey()),
                    prop.getValue());
        }

        // build the requested map (the order is important!)
        final Map<String, PropertyItem> propertyItems = new LinkedHashMap<>();

        String propName;
        // first, add configured properties
        for (final Map.Entry<String, ItemConfig> prop : propsConfig.entrySet()) {
            propName = prop.getKey();
            propertyItems.put(propName,
                    new ConfigurableProperty(propName, stringKeyedProps.remove(propName), prop.getValue()));
        }

        // second, add non-configured properties
        for (final Map.Entry<String, Serializable> prop : stringKeyedProps.entrySet()) {
            propName = prop.getKey();
            propertyItems.put(propName, new ConfigurableProperty(propName, prop.getValue()));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Property found for the node " + nodeRef + ": " + propertyItems.keySet());
        }

        return propertyItems;
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the metadataManager
     */
    protected final MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * @param metadataManager the metadataManager to set
     */
    public final void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    /**
     * @return the objectManager
     */
    public final AlfrescoObjectsManager getObjectManager() {
        return objectManager;
    }

    /**
     * @param objectManager the objectManager to set
     */
    public final void setObjectManager(AlfrescoObjectsManager objectManager) {
        this.objectManager = objectManager;
    }

    /**
     * @return the configManager
     */
    protected final ApplicationConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * @param configManager the configManager to set
     */
    public final void setConfigManager(ApplicationConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * @return the validationManager
     */
    protected final ValidationManager getValidationManager() {
        return validationManager;
    }

    /**
     * @param validationManager the validationManager to set
     */
    public final void setValidationManager(ValidationManager validationManager) {
        this.validationManager = validationManager;
    }

    public final void setContentFilterLanguagesService(
            ContentFilterLanguagesService contentFilterLanguagesService) {
        this.contentFilterLanguagesService = contentFilterLanguagesService;
    }
}
