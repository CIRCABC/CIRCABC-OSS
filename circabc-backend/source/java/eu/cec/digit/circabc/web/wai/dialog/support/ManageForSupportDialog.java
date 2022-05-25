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
package eu.cec.digit.circabc.web.wai.dialog.support;

import eu.cec.digit.circabc.service.support.SupportService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.util.Map;


/**
 * Bean that backs the "Manage support" WAI page.
 *
 * @author Stephane Clinckart
 */
public class ManageForSupportDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "ManageForSupportDialog";
    /**
     * The constant for the 'keyword' parameter
     */
    public static final String KEYWORD_PARAMETER = "keyword";
    protected static final String NULL_VALUE = "null";
    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(ManageForSupportDialog.class);
    private static final long serialVersionUID = 6521387670111634025L;
    private transient SupportService supportService;

    @Override
    public void init(final Map<String, String> parameters) {
        super.init(parameters);

    }

    public void generateError(final ActionEvent event) {
        // int x = 5 / 0;
        //final UIInput input = (UIInput) event.getComponent().findComponent(COMPONENT_EDIT_PROFILE_VALUE);
        //final UISelectOne select = (UISelectOne) event.getComponent().findComponent(COMPONENT_EDIT_PROFILE_LANGUAGE);

        //final String selLang = (String) select.getValue();
        //final String selValue = ((String) input.getValue()).trim();
    }


    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        // nothing to do
        return null;
    }

    @Override
    public void restored() {

    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    public String getBrowserTitle() {
        return translate("manage_for_support_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("manage_for_support_dialog_icon_tooltip");
    }

    public Node getInterestGroup() {
        return getActionNode();
    }

    /**
     * /**
     *
     * @return the supportService
     */
    protected final SupportService getSupportService() {
        if (supportService == null) {
            supportService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getSupportService();
        }
        return supportService;
    }

    /**
     * @param supportService the supportService to set
     */
    public final void setSupportService(final SupportService supportService) {
        this.supportService = supportService;
    }
}
