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
import eu.cec.digit.circabc.web.wai.wizard.property.DefineNewPropertyWizard;
import eu.cec.digit.circabc.web.wai.wizard.property.DynPropertyWrapper;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;


/**
 * Bean that backs the "Edit Property" WAI Dialog.
 *
 * @author Slobodan Filipovic
 */
public class EditPropertyDialog extends DefineNewPropertyWizard {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "EditPropertyDialog";
    /**
     *
     */
    private static final long serialVersionUID = -3366182849266786067L;
    private static final Log logger = LogFactory.getLog(EditPropertyDialog.class);

    private DynamicProperty propertyToEdit = null;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            final String propertyAsString = parameters.get(ManagePropertiesDialog.PROPERTY_PARAMETER);
            propertyToEdit = null;

            if (propertyAsString == null) {
                throw new IllegalArgumentException(
                        "Impossible to edit the property if the property parameters is not seted: "
                                + ManagePropertiesDialog.PROPERTY_PARAMETER);
            }

            propertyToEdit = this.getPropertiesService()
                    .getDynamicPropertyByID(new NodeRef(propertyAsString));

            propertyTranslations = new ArrayList<>(30);

            for (Map.Entry<Locale, String> entry : propertyToEdit.getLabel().entrySet()) {
                propertyTranslations.add(new DynPropertyWrapper(entry.getValue(), entry.getKey()));
            }
        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        final MLText labels = new MLText();

        for (final DynPropertyWrapper wrapper : propertyTranslations) {
            labels.addValue(wrapper.getLocale(), wrapper.getValue());
        }

        String info = MessageFormat
                .format("the property {0} has been edited - description {1}", propertyToEdit.getName(),
                        labels.toString());
        logRecord.setInfo(info);

        this.getPropertiesService().updateDynamicPropertyLabel(propertyToEdit, labels);

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Property " + propertyToEdit.getName() + " successfully updated under the interest group "
                            + getActionNode().getName() + ". New value: " + propertyToEdit.toString());
        }

        return outcome;
    }

    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    public String getPropertyName() {
        return this.propertyToEdit.getName();
    }

    public String getBrowserTitle() {
        return translate("edit_property_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_property_dialog_icon_tooltip");
    }


}
