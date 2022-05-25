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

import java.util.Date;
import java.util.List;

/**
 * Business service to manage spaces.
 *
 * @author Yanick Pignot
 */
public interface SpaceBusinessSrv {

    /**
     * Add a space node a given parent. The name will be unique and unique and computed from the
     * title.
     *
     * @param parent         An existing parent
     * @param title          A title (not empty) that will become the title
     * @param description    An optional desciption of the space
     * @param iconName       An optional icon name of the space
     * @param expirationDate An optional date for the space expiration (The date may be null but not
     *                       earlier than today).
     */
    NodeRef createSpace(final NodeRef parent, final String title, final String description,
                        final String iconName, final Date expirationDate);


    /**
     * Add a space node a given parent. The name will be unique and unique and computed from the
     * title.
     *
     * @param parent         An existing parent
     * @param title          A title (not empty) that will become the title
     * @param description    An optional descption of the space
     * @param icon           An optional icon of the space
     * @param expirationDate An optional date for the space expiration (The date may be null but not
     *                       earlier than today).
     */
    NodeRef createSpace(final NodeRef parent, final String title, final String description,
                        final ContainerIcon icon, final Date expirationDate);

    /**
     * Return the available icons for spaces.
     *
     * @return All defined icons for spaces. Never null.
     */
    List<ContainerIcon> getSpaceIcons();

}
