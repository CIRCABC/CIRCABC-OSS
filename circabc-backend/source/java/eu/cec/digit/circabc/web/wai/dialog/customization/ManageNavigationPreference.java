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
package eu.cec.digit.circabc.web.wai.dialog.customization;

import eu.cec.digit.circabc.service.customisation.nav.NavigationConfigService;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.ServiceConfig;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.faces.context.FacesContext;
import java.util.*;

/**
 * Dialog for navigation preferences managing.
 *
 * @author Yanick Pignot
 */
public class ManageNavigationPreference extends BaseWaiDialog {

    /**
     *
     */
    private static final long serialVersionUID = -2811145542680105661L;

    private transient NavigationPreferencesService navigationPreferencesService;
    private transient NavigationConfigService navigationConfigService;

    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory");
        }
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        // nothing to do
        return outcome;
    }

    public List<NavigationPrefWrapper> getPreferences() {
        final NodeRef currentNodeRef = getActionNode().getNodeRef();

        final Collection<ServiceConfig> globalPreferences = getNavigationConfigService()
                .getAllServiceConfig();
        final List<NavigationPrefWrapper> wrappers = new ArrayList<>(globalPreferences.size());
        NavigationPreference preferences;

        /* Display all navigations cutomization for Category or Ig. But display only the service related ones below */
        final String serviceFilter;

        final NavigableNodeType type = AspectResolver.resolveType(getActionNode());

        if (type == null) {
            throw new IllegalStateException(
                    "Impossible to manage customization preferences for a non circabc node!");
        } else if (NavigableNodeType.CIRCABC_ROOT.equals(type)) {
            throw new IllegalStateException(
                    "Impossible to manage circabc customization preferences via this page!");
        } else if (NavigableNodeType.CATEGORY.equals(type) || NavigableNodeType.IG_ROOT.equals(type)) {
            // means all services
            serviceFilter = null;
        } else if (type.isStrictlyUnder(NavigableNodeType.LIBRARY)) {
            serviceFilter = NavigationPreferencesService.LIBRARY_SERVICE;
        } else if (type.isStrictlyUnder(NavigableNodeType.NEWSGROUP)) {
            serviceFilter = NavigationPreferencesService.NEWSGROUP_SERVICE;
        } else if (type.isStrictlyUnder(NavigableNodeType.INFORMATION)) {
            serviceFilter = NavigationPreferencesService.INFORMATION_SERVICE;
        } else {
            // nothing to configure here

            return Collections.emptyList();
        }

        for (final ServiceConfig service : globalPreferences) {
            if (serviceFilter == null || serviceFilter.equals(service.getName())) {
                preferences = getNavigationPreferencesService().getServicePreference(
                        currentNodeRef, service.getName(), service.getType());

                wrappers.add(new NavigationPrefWrapper(
                        preferences,
                        getNodeService(),
                        getActionNode()
                ));
            }
        }

        return wrappers;
    }

    @Override
    public String getContainerTitle() {
        return translate("manage_navigation_dialog_title", getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("manage_navigation_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_navigation_dialog_icon_tooltip");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    /**
     * @return the navigationPreferencesService
     */
    protected final NavigationPreferencesService getNavigationPreferencesService() {
        if (navigationPreferencesService == null) {
            navigationPreferencesService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNavigationPreferencesService();
        }
        return navigationPreferencesService;
    }


    /**
     * @param navigationPreferencesService the navigationPreferencesService to set
     */
    public final void setNavigationPreferencesService(
            NavigationPreferencesService navigationPreferencesService) {
        this.navigationPreferencesService = navigationPreferencesService;
    }


    /**
     * @return the navigationConfigService
     */
    protected final NavigationConfigService getNavigationConfigService() {
        if (navigationConfigService == null) {
            navigationConfigService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getNavigationConfigService();
        }
        return navigationConfigService;
    }


    /**
     * @param navigationConfigService the navigationConfigService to set
     */
    public final void setNavigationConfigService(NavigationConfigService navigationConfigService) {
        this.navigationConfigService = navigationConfigService;
    }

}
