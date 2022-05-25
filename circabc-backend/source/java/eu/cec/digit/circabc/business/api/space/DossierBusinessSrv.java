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
package eu.cec.digit.circabc.business.api.space;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Business service to manage Dossiers.
 *
 * @author Yanick Pignot
 */
public interface DossierBusinessSrv {

    /**
     * Add a dossier node a given parent. The name will be unique and unique and computed from the
     * title.
     *
     * @param parent      An existing parent
     * @param title       A title (not empty) that will become the title
     * @param description An optional descption of the dossier
     * @param iconName    An optional icon name of the dossier
     */
    NodeRef createDossier(final NodeRef parent, final String title, final String description,
                          final String iconName);


    /**
     * Return the available icons for dossiers.
     *
     * @return All defined icons for dossier. Never null.
     */
    List<ContainerIcon> getDossierIcons();
}
