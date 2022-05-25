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
import eu.cec.digit.circabc.service.customisation.logo.LogoDefinition;
import eu.cec.digit.circabc.service.customisation.logo.LogoPreferencesService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.jcr.PathNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Dialog for navigation preferences editing.
 *
 * @author Yanick Pignot
 */
public class RemoveLogoPreference extends BaseWaiDialog {

    private static final Log logger = LogFactory.getLog(RemoveLogoPreference.class);

    private static final String PARAM_LOGONAME = "logoName";

    private static final String MSG_ERROR_DELETE = "manage_iglogo_dialog_delete_error";
    private static final String MSG_ERROR_LOCATION_DELETE = "manage_iglogo_dialog_delete_error_location";
    private static final String MSG_ERROR_NAME_DELETE = "manage_iglogo_dialog_delete_error_name";

    private static final String MSG_UNDEFINED = "manage_navigation_dialog_undefined";
    private static final String MSG_LOGO_DELETED = "manage_iglogo_dialog_delete_success";
    private static final String MSG_CONFIRMATION = "remove_iglogo_dialog_confirmation";


    /**
     *
     */
    private static final long serialVersionUID = -2899966542680105661L;

    private transient LogoPreferencesService logoPreferencesService;
    private String logoName;


    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        if (getActionNode() == null) {
            throw new IllegalArgumentException("The node id parameter is mandatory.");
        }

        if (parameters != null) {
            this.logoName = parameters.get(PARAM_LOGONAME);

            if (logoName == null || logoName.trim().length() < 1) {
                throw new IllegalArgumentException("The logo name parameter is mandatory.");
            }
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Throwable {
        try {
            // test if the logo is defined at this location. It's impossible to delete a logo
            // defined on any parent.
            final NodeRef currentLocationRef = getActionNode().getNodeRef();
            final List<LogoDefinition> logos = getLogoPreferencesService()
                    .getAllLogos(currentLocationRef);

            LogoDefinition logoByName = null;

            for (final LogoDefinition logo : logos) {
                if (logo.getName().equals(this.logoName)) {
                    logoByName = logo;

                    if (currentLocationRef.equals(logo.getDefinedOn())) {
                        // all is ok
                        break;
                    }
                    //else searh another logo with the same name
                }
            }

            if (logoByName == null) {
                Utils.addErrorMessage(translate(MSG_ERROR_NAME_DELETE, this.logoName));

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "No logo with the name " + this.logoName + " found on " + getActionNode().getName());
                }

                return null;
            } else if (currentLocationRef.equals(logoByName.getDefinedOn()) == false) {
                Utils.addErrorMessage(
                        translate(MSG_ERROR_LOCATION_DELETE, getPath(logoByName.getDefinedOn())));

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Delete a logo defined on a parent location is not allowed. Parent: " + logoByName
                                    .getDefinedOn() + " instand of " + currentLocationRef);
                }

                return null;
            } else {
                getLogoPreferencesService().removeLogo(currentLocationRef, this.logoName);

                Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                        translate(MSG_LOGO_DELETED, this.logoName));

                return outcome;
            }
        } catch (final Throwable t) {
            Utils.addErrorMessage(translate(MSG_ERROR_DELETE, t.getMessage()));

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Unexpected error during removing logo on " + getPath(getActionNode().getNodeRef()), t);
            }

            return null;
        }
    }

    private String getPath(final NodeRef ref) {
        String path;
        try {
            final SimplePath simplePath = new SimplePath(getNodeService(), ref);
            path = simplePath.toString();
        } catch (PathNotFoundException e) {
            path = "<i>" + translate(MSG_UNDEFINED) + "</i>";
        }

        return path;
    }

    public String getConfirmation() {
        return translate(MSG_CONFIRMATION, this.logoName, getPath(getActionNode().getNodeRef()));
    }


    @Override
    public String getContainerTitle() {
        return translate("remove_iglogo_dialog_title", this.logoName, getActionNode().getName());
    }

    public String getBrowserTitle() {
        return translate("remove_iglogo_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("remove_iglogo_dialog_icon_tooltip");
    }


    /**
     * @return the logoPreferencesService
     */
    protected final LogoPreferencesService getLogoPreferencesService() {
        if (this.logoPreferencesService == null) {
            this.logoPreferencesService = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getLogoPreferencesService();
        }
        return logoPreferencesService;
    }


    /**
     * @param logoPreferencesService the logoPreferencesService to set
     */
    public final void setLogoPreferencesService(LogoPreferencesService logoPreferencesService) {
        this.logoPreferencesService = logoPreferencesService;
    }

}
