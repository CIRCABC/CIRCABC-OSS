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
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import java.util.*;
import java.util.Map.Entry;


/**
 * Bean that backs the "Edit Selection Property" WAI Dialog.
 *
 * @author Slobodan Filipovic
 */
public class EditSelectionPropertyDialog extends BaseWaiDialog {

    /**
     * Public JSF Bean name
     */
    public static final String BEAN_NAME = "EditPropertyDialog";
    private static final String UPDATE_TEXT = "update-text";
    private static final String ADD_TEXT = "add-text";
    private static final String LIST_OF_VALID_VALUES = "list-of-valid-values";
    /**
     *
     */
    private static final long serialVersionUID = -3366182849266786067L;
    private static final Log logger = LogFactory.getLog(EditSelectionPropertyDialog.class);

    private DynamicProperty propertyToEdit = null;

    private DynamicPropertyService propertiesService;

    private String validValues;

    private List<String> oldListOfValidValues;
    private List<String> listOfValidValues;

    private boolean updateExistingProperties;
    private Set<String> deletedValues;
    private Map<String, String> updatedValues;
    private String addText;
    private String updateText;
    private String currentValue;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            deletedValues = new HashSet<>();
            updatedValues = new HashMap<>();
            updateExistingProperties = true;

            final String propertyAsString = parameters.get(ManagePropertiesDialog.PROPERTY_PARAMETER);
            propertyToEdit = null;

            if (propertyAsString == null) {
                throw new IllegalArgumentException(
                        "Impossible to edit the property if the property parameters is not seted: "
                                + ManagePropertiesDialog.PROPERTY_PARAMETER);
            }

            propertyToEdit = this.getPropertiesService()
                    .getDynamicPropertyByID(new NodeRef(propertyAsString));
            this.listOfValidValues = propertyToEdit.getListOfValidValues();
            this.oldListOfValidValues = propertyToEdit.getListOfValidValues();
            if (this.listOfValidValues.size() > 0) {
                currentValue = (String) this.listOfValidValues.get(0);
            }

            addText = "";
            updateText = "";

        }
    }

    @Override
    protected String finishImpl(FacesContext context, String outcome) throws Exception {
        validValues = calculateValidValues();
        this.getPropertiesService()
                .updateDynamicPropertyValidValues(propertyToEdit, validValues, updateExistingProperties,
                        deletedValues, updatedValues);
        return outcome;
    }

    private String calculateValidValues() {
        final StringBuilder result = new StringBuilder("");
        for (String item : listOfValidValues) {
            result
                    .append(item)
                    .append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);

        }
        return result.toString();
    }

    public String getPropertyName() {
        return this.propertyToEdit.getName();
    }

    public String getBrowserTitle() {
        return translate("edit_selection_list_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_selection_list_dialog_icon_tooltip");
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


    public List<SelectItem> getListOfValidValues() {
        List<SelectItem> items = new ArrayList<>();
        for (String item : listOfValidValues) {
            items.add(new SelectItem(item));
        }
        return items;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public void select(final ActionEvent event) {
        final String selectValue = getUISelectOneValue(event, LIST_OF_VALID_VALUES);
        addText = selectValue;
        updateText = selectValue;
    }

    public void add(ActionEvent event) {
        final String inputValue = getUIInputValue(event, ADD_TEXT);
        if (!listOfValidValues.contains(inputValue)) {
            listOfValidValues.add(inputValue);
        }


    }


    private String getUIInputValue(ActionEvent event, String id) {
        final UIInput input = (UIInput) event.getComponent().findComponent(id);
        final String inputValue = ((String) input.getValue()).trim();
        return inputValue;
    }

    public void update(ActionEvent event) {
        final String oldValue = getUISelectOneValue(event, LIST_OF_VALID_VALUES);
        final String newValue = getUIInputValue(event, UPDATE_TEXT);

        final int index = listOfValidValues.indexOf(oldValue);
        if (index >= 0) {
            listOfValidValues.set(index, newValue);
            if (oldListOfValidValues.contains(oldValue)) {
                updatedValues.put(oldValue, newValue);
            } else if (updatedValues.containsValue(oldValue)) {
                String originalOldValue = null;
                for (Entry<String, String> item : updatedValues.entrySet()) {
                    if (item.getValue().equals(oldValue)) {
                        originalOldValue = item.getKey();
                    }
                }
                if (originalOldValue != null) {
                    updatedValues.put(originalOldValue, newValue);
                }

            }

        }


    }

    public void remove(ActionEvent event) {

        final String selectValue = getUISelectOneValue(event, LIST_OF_VALID_VALUES);

        final int index = listOfValidValues.indexOf(selectValue);
        if (index >= 0) {
            listOfValidValues.remove(index);
            if (oldListOfValidValues.contains(selectValue)) {
                deletedValues.add(selectValue);
            }
        }


    }

    public void up(ActionEvent event) {
        final String selectValue = getUISelectOneValue(event, LIST_OF_VALID_VALUES);
        int index = listOfValidValues.indexOf(selectValue);
        if (index > 0) {
            Collections.swap(listOfValidValues, index, index - 1);
        }
    }

    public void down(ActionEvent event) {
        final String selectValue = getUISelectOneValue(event, LIST_OF_VALID_VALUES);
        final int index = listOfValidValues.indexOf(selectValue);
        final int size = listOfValidValues.size();
        if (index < size - 1) {
            Collections.swap(listOfValidValues, index, index + 1);
        }
    }

    private String getUISelectOneValue(ActionEvent event, String id) {
        final UISelectOne select = (UISelectOne) event.getComponent().findComponent(id);
        final String selectValue = (String) select.getValue();
        return selectValue;
    }

    public String getAddText() {
        return addText;
    }

    public void setAddText(String addText) {
        this.addText = addText;
    }

    public String getUpdateText() {
        return updateText;
    }

    public void setUpdateText(String updateText) {
        this.updateText = updateText;
    }

    public boolean isUpdateExistingProperties() {
        return updateExistingProperties;
    }

    public void setUpdateExistingProperties(boolean updateExistingProperties) {
        this.updateExistingProperties = updateExistingProperties;
    }

}
