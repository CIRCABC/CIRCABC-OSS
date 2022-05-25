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

import eu.cec.digit.circabc.business.api.space.ContainerIcon;
import eu.cec.digit.circabc.business.api.space.SpaceBusinessSrv;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.Date;
import java.util.List;

/**
 * Business service implementation to manage spaces.
 *
 * @author Yanick Pignot
 */
public class SpaceBusinessImpl extends ContainerBaseBusinessService implements SpaceBusinessSrv {
    //--------------
    //-- public methods

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.space.SpaceBusinessSrv#createSpace(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, java.lang.String, java.util.Date)
     */
    public NodeRef createSpace(final NodeRef parent, final String title, final String description,
                               final String iconName, final Date expirationDate) {
        return createSpace(parent, title, description,
                getConfigManager().getIcon(ContentModel.TYPE_FOLDER, iconName), expirationDate);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.space.SpaceBusinessSrv#createSpace(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String, eu.cec.digit.circabc.business.api.space.ContainerIcon, java.util.Date)
     */
    public NodeRef createSpace(final NodeRef parent, final String title, final String description,
                               final ContainerIcon icon, final Date expirationDate) {
        return createContainerImpl(parent, ContentModel.TYPE_FOLDER, title, description, icon,
                expirationDate);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.space.SpaceBusinessSrv#getSpaceIcons()
     */
    public List<ContainerIcon> getSpaceIcons() {
        return getIcons();
    }

    @Override
    protected QName getIconType() {
        return ContentModel.TYPE_FOLDER;
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC
}
