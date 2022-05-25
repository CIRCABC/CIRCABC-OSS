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
package eu.cec.digit.circabc.business.impl.space;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.space.ContainerIcon;
import eu.cec.digit.circabc.business.helper.ApplicationConfigManager;
import eu.cec.digit.circabc.business.helper.MetadataManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.AssertUtils;
import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ContainerBaseBusinessService {

    private final Log logger = LogFactory.getLog(ContainerBaseBusinessService.class);

    private FileFolderService fileFolderService;
    private NodeService nodeService;

    private MetadataManager metadataManager;
    private ValidationManager validationManager;
    private ApplicationConfigManager configManager;

    /*package*/ ContainerBaseBusinessService() {
    }


    protected NodeRef createContainerImpl(final NodeRef parent, final QName type, final String title,
                                          final String description, final ContainerIcon icon, final Date expirationDate) {
        final BusinessStackError stack = new BusinessStackError();

        getValidationManager().validateNodeRef(parent, stack);
        getValidationManager().validateSpace(parent, stack);
        getValidationManager().validateExpirationDate(expirationDate, stack);
        getValidationManager().validateTitle(title, stack);
        getValidationManager().validateName(title, stack);

        stack.finish(logger);

        //generate a valid and unique name
        final String validName = getMetadataManager().getValidUniqueName(parent, title);

        final FileInfo fileInfo = getFileFolderService().create(parent, validName, type);
        final NodeRef nodeRef = fileInfo.getNodeRef();

        if (logger.isDebugEnabled()) {
            logger.debug("Created " + type.getLocalName() + " node with name: " + validName);
        }

        // apply the uifacets aspect - icon, title and description props
        final Map<QName, Serializable> uiFacetsProps = new HashMap<>(5);
        uiFacetsProps.put(ApplicationModel.PROP_ICON, geDefaultIcon(icon));
        uiFacetsProps.put(ContentModel.PROP_TITLE, title);
        uiFacetsProps.put(ContentModel.PROP_DESCRIPTION, description);
        getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, uiFacetsProps);

        if (logger.isDebugEnabled()) {
            logger.debug("Added uifacets aspect with properties: " + uiFacetsProps);
        }

        if (expirationDate != null) {
            // TODO create an ExpirableAspect
            getNodeService().setProperty(nodeRef, DocumentModel.PROP_EXPIRATION_DATE, expirationDate);

            if (logger.isDebugEnabled()) {
                logger.debug("Added expiration property (Without associated aspect): " + expirationDate);
            }
        }

        return nodeRef;
    }

    protected List<ContainerIcon> getIcons() {
        final List<ContainerIcon> icons = getConfigManager().getIcons(getIconType());
        AssertUtils.notEmpty(icons);

        return icons;
    }

    protected abstract QName getIconType();

    //--------------
    //-- private helpers

    private String geDefaultIcon(final ContainerIcon icon) {
        if (icon == null) {
            return getIcons().get(0).getIconName();
        } else {
            return icon.getIconName();
        }
    }

    //--------------
    //-- IOC

    /**
     * @return the fileFolderService
     */
    protected final FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public final void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

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

}
