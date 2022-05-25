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
package eu.cec.digit.circabc.web.wai.dialog.properties;

import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.Map;


/**
 * Bean that backs the "Delete Property" WAI Dialog.
 *
 * @author Slobodan Filipovic
 */
public class DeletePropertyDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "DeletePropertyDialog";
    private static final long serialVersionUID = 5393940370953035374L;
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(DeletePropertyDialog.class);

    private static final String MSG_ERROR_NODE_LOCKED = "delete_property_dialog_error_node_locked";

    private transient DynamicPropertyService propertiesService;
    private DynamicProperty propertyToDelete = null;


    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            final String propertyAsString = parameters.get(ManagePropertiesDialog.PROPERTY_PARAMETER);
            propertyToDelete = null;

            if (propertyAsString == null) {
                throw new IllegalArgumentException(
                        "Impossible to delete the property if the property parameters is not seted: "
                                + ManagePropertiesDialog.PROPERTY_PARAMETER);
            }

            propertyToDelete = this.getPropertiesService()
                    .getDynamicPropertyByID(new NodeRef(propertyAsString));
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        try {
            String info = MessageFormat
                    .format("the property {0} has been deleted", propertyToDelete.getName());
            logRecord.setInfo(info);
            this.getPropertiesService().deleteDynamicProperty(propertyToDelete);
            return outcome;

        } catch (NodeLockedException e) {
            Utils.addErrorMessage(translate(MSG_ERROR_NODE_LOCKED));

            if (logger.isDebugEnabled()) {
                logger.debug("Impossible to delete a dynamic property since documents are locked");
            }

            return null;
        }


    }

    public String getPropertyName() {
        return this.propertyToDelete.getName();
    }

    public String getBrowserTitle() {
        return translate("delete_property_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("delete_property_dialog_icon_tooltip");
    }

    /**
     * @return the propertiesService
     */
    protected final DynamicPropertyService getPropertiesService() {
        if (propertiesService == null) {
            propertiesService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getDynamicPropertieService();
        }
        return propertiesService;
    }

    /**
     * @param propertiesService the propertiesService to set
     */
    public final void setDynamicPropertyService(DynamicPropertyService dynamicPropertyService) {
        this.propertiesService = dynamicPropertyService;
    }
}
