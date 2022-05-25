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
package eu.cec.digit.circabc.web;

import eu.cec.digit.circabc.business.api.space.ContainerIcon;
import org.alfresco.web.ui.common.component.UIListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains Common jsf convertion methds
 *
 * @author Yanick Pignot
 */
public abstract class JSFUtils {

    private JSFUtils() {
    }


    /**
     * Convert a list of logo to JSF selectable items
     */
    public static List<UIListItem> convertLogos(final List<ContainerIcon> containerIcons) {
        final List<UIListItem> icons = new ArrayList<>(containerIcons.size());
        UIListItem item;
        for (final ContainerIcon containerIcon : containerIcons) {
            item = new UIListItem();
            item.setValue(containerIcon.getIconName());
            item.setImage(containerIcon.getIconPath());
            icons.add(item);
        }
        return icons;
    }
}
