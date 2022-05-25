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
import eu.cec.digit.circabc.business.api.space.DossierBusinessSrv;
import eu.cec.digit.circabc.model.DossierModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.List;

/**
 * Business service implementation to manage Dossiers.
 *
 * @author Yanick Pignot
 */
public class DossierBusinessImpl extends ContainerBaseBusinessService implements
        DossierBusinessSrv {

    //--------------
    //-- public methods

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.space.DossierBusinessSrv#createDossier(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public NodeRef createDossier(final NodeRef parent, final String title, final String description,
                                 final String iconName) {
        return createContainerImpl(parent, DossierModel.TYPE_DOSSIER_SPACE, title, description,
                getConfigManager().getIcon(DossierModel.TYPE_DOSSIER_SPACE, iconName), null);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.space.DossierBusinessSrv#getDossierIcons()
     */
    public List<ContainerIcon> getDossierIcons() {
        return getIcons();
    }

    @Override
    protected QName getIconType() {
        return DossierModel.TYPE_DOSSIER_SPACE;
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC

}
