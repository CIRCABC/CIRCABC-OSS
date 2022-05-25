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

import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.customisation.nav.NavigationConfigService;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;
import eu.cec.digit.circabc.service.customisation.nav.NavigationPreferencesService;
import eu.cec.digit.circabc.service.customisation.nav.ServiceConfig;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

import javax.faces.context.FacesContext;
import javax.jcr.PathNotFoundException;
import java.util.Map;

/**
 * Dialog for navigation preferences editing.
 *
 * @author Yanick Pignot
 */
public class RemoveNavigationPreference extends BaseWaiDialog {

    private static final String PARAM_PREF_TYPE = "prefType";
    private static final String PARAM_PREF_SERVICE = "prefService";

    private static final String MSG_CONFIRMATION = "remove_navigation_dialog_confirmation";
    private static final String MSG_UNDEFINED = "manage_navigation_dialog_undefined";

    /**
     *
     */
    private static final long serialVersionUID = -2811145542680105661L;

    private transient NavigationPreferencesService navigationPreferencesService;
    private transient NavigationConfigService navigationConfigService;

    private NavigationPreference preference;
    private NavigationPreference parentPreference;
    private ServiceConfig globalPreference;


    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory.");
        }

        if (parameters != null) {
            preference = null;
            parentPreference = null;
            globalPreference = null;

            final String serviceParam = parameters.get(PARAM_PREF_SERVICE);
            final String typeParam = parameters.get(PARAM_PREF_TYPE);

            if (serviceParam == null || typeParam == null) {
                throw new IllegalArgumentException(
                        PARAM_PREF_SERVICE + " AND " + PARAM_PREF_TYPE + " are mandatories.");
            }
            final NodeRef nodeRef = getActionNode().getNodeRef();

            preference = getNavigationPreferencesService()
                    .getServicePreference(nodeRef, serviceParam, typeParam);
            globalPreference = preference.getService();

            final ChildAssociationRef parent = getNodeService().getPrimaryParent(nodeRef);
            parentPreference = getNavigationPreferencesService()
                    .getServicePreference(parent.getParentRef(), serviceParam, typeParam);
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        getNavigationPreferencesService()
                .removeServicePreference(getActionNode().getNodeRef(), globalPreference.getName(),
                        globalPreference.getType());

        return outcome;
    }

    public String getConfirmation() {
        String path;
        try {
            final SimplePath simplePath = new SimplePath(getNodeService(),
                    parentPreference.getCustomizedOn());
            path = simplePath.toString();
        } catch (PathNotFoundException e) {
            path = "<i>" + translate(MSG_UNDEFINED) + "</i>";
        }

        return translate(MSG_CONFIRMATION, path);
    }


    @Override
    public String getContainerTitle() {
        return translate("remove_navigation_dialog_title", globalPreference.getName(),
                globalPreference.getType(), getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("remove_navigation_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("remove_navigation_dialog_icon_tooltip");
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
