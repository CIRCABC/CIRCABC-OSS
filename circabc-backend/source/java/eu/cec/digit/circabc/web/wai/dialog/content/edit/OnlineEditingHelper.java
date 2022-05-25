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
package eu.cec.digit.circabc.web.wai.dialog.content.edit;

import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.dialog.content.edit.CreateContentBaseDialog.AttachementWrapper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yanick Pignot
 */
public abstract class OnlineEditingHelper {

    private OnlineEditingHelper() {
    }


    /**
     * Generate a list of well know urls to be used in a WYSIWYG
     */
    public static List<Pair<String, String>> generateLinks(final CreateContentBaseDialog dialog,
                                                           final CircabcNavigationBean navigator) {
        final List<Pair<String, String>> links = new ArrayList<>(5);

        links.add(new Pair<>("Europa", "http://www.europa.eu"));

        addLink(links, navigator.getCircabcHomeNode());
        addLink(links, navigator.getCurrentCategory());
        addLink(links, navigator.getCurrentIGRoot());
        addLink(links, navigator.getCurrentIGService());

        if (navigator.isGuest() == false && navigator.getCurrentUser() != null) {
            links.add(new Pair<>("My Profile", WebClientHelper
                    .getGeneratedWaiFullUrl(navigator.getCurrentUser().getPerson(),
                            ExtendedURLMode.HTTP_USERDETAILS)));
        }

        addAttachements(dialog, links);

        return links;
    }

    /**
     * Generate a list of well know urls to be used in a WYSIWYG
     */
    public static List<Pair<String, String>> generateMediafiles(
            final CreateContentBaseDialog dialog) {
        final List<Pair<String, String>> links = new ArrayList<>(5);

        addAttachements(dialog, links);

        return links;
    }


    private static void addAttachements(final CreateContentBaseDialog dialog,
                                        final List<Pair<String, String>> links) {
        final List<AttachementWrapper> wrappers = dialog.getAttachementWrappers();

        if (wrappers != null) {
            for (final AttachementWrapper wrapper : wrappers) {
                addLink(links, wrapper);
            }
        }
    }

    private static void addLink(final List<Pair<String, String>> links,
                                final AttachementWrapper wrapper) {
        if (wrapper != null) {
            final NodeRef attachRef;

            if (wrapper.isCreated()) {
                attachRef = wrapper.getAttachement().getNodeRef();
            } else {
                attachRef = wrapper.getAttachRef();
            }

            links.add(
                    new Pair<>(
                            wrapper.getName(),
                            WebClientHelper.getGeneratedWaiFullUrl(attachRef, ExtendedURLMode.HTTP_DOWNLOAD))
            );
        }
    }

    private static void addLink(final List<Pair<String, String>> links, final NavigableNode node) {
        if (node != null) {
            links.add(
                    new Pair<>(
                            (String) node.get(NavigableNode.BEST_TITLE_RESOLVER_NAME),
                            WebClientHelper.getGeneratedWaiFullUrl(node, ExtendedURLMode.HTTP_WAI_BROWSE))
            );
        }
    }

}
