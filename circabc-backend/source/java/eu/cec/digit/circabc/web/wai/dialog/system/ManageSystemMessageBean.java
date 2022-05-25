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
package eu.cec.digit.circabc.web.wai.dialog.system;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Map;

public class ManageSystemMessageBean extends BaseWaiDialog {

    final static Log logger = LogFactory.getLog(ManageSystemMessageBean.class);
    private static final long serialVersionUID = 1L;
    private static final String MSG_OFF = "manage_system_message_off";
    private static final String MSG_ON = "manage_system_message_on";
    private static final String MSG_CHANGED = "manage_system_message_changed";
    private String originalMessage = null;
    private String helpLink = null;
    private boolean originalShowMessage = false;

    //***********************************************************************
    //                                                              OVERRIDES
    //***********************************************************************
    private SystemMessageBean systemMessageBean;
    private boolean showMessage;
    private String message;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

        //update the bean properties with the node

        originalShowMessage = getSystemMessageBean().isShowMessage();
        this.setShowMessage(originalShowMessage);

        originalMessage = getSystemMessageBean().getMessage();
        this.setMessage(originalMessage);

        NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();

        if (this.getNodeService().getProperty(rootRef, CircabcModel.PROP_HELP_LINK) != null) {
            this.helpLink = this.getNodeService().getProperty(rootRef, CircabcModel.PROP_HELP_LINK)
                    .toString();
        }
    }

    //***********************************************************************
    //                                                         PRIVATE HELPER
    //***********************************************************************

    @Override
    protected String finishImpl(FacesContext context, String outcome)
            throws Throwable {
        // update the node
        getSystemMessageBean().updateProperties(
                this.isShowMessage(),
                getSecurityService().getCleanHTML(this.getMessage(), true));

        // inform the user
        String status = "";
        if (originalShowMessage != this.isShowMessage()) {
            if (this.isShowMessage()) {
                // system message has been turned on
                status += translate(MSG_ON);
            } else {
                // system message has been turned off
                status += translate(MSG_OFF);
            }
        }

        if (!status.equals("")) {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO, status);
        }

        NodeRef rootRef = Beans.getWaiNavigator().getCircabcHomeNode().getNodeRef();

        String[] schemes = {"http", "https"};
        UrlValidator uValid = new UrlValidator(schemes);

        if (uValid.isValid(helpLink)) {
            this.getNodeService().setProperty(rootRef, CircabcModel.PROP_HELP_LINK,
                    getSecurityService().getCleanHTML(this.helpLink, false));
        } else {
            Utils.addStatusMessage(FacesMessage.SEVERITY_INFO,
                    translate("customisation_console_invalid_help_link"));
            logger.warn("Invalid help link provided:" + helpLink);
        }

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME;
    }

    public String getPageIconAltText() {
        return null;
    }

    public String getBrowserTitle() {
        return null;
    }

    private SystemMessageBean getSystemMessageBean() {
        if (systemMessageBean == null) {
            @SuppressWarnings("rawtypes")
            Map application = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            systemMessageBean = (SystemMessageBean) application.get("SystemMessageBean");
        }
        return systemMessageBean;
    }

    public boolean isShowMessage() {
        return showMessage;
    }

    public void setShowMessage(boolean showMessage) {
        this.showMessage = showMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the helpLink
     */
    public String getHelpLink() {
        return helpLink;
    }

    /**
     * @param helpLink the helpLink to set
     */
    public void setHelpLink(String helpLink) {
        this.helpLink = helpLink;
    }


}
