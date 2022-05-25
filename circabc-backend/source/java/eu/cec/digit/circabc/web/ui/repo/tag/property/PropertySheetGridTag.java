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
package eu.cec.digit.circabc.web.ui.repo.tag.property;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.component.UIComponent;

/**
 * @author patrice.coppens@trasys.lu
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. Class
 * implementation didn't change between Alfresco versions.
 */
public class PropertySheetGridTag extends BaseComponentTag {

    private String value;
    private String var;
    private String columns;
    private String externalConfig;
    private String configArea;
    private String readOnly;
    private String mode;
    private String validationEnabled;
    private String labelStyleClass;
    private String cellpadding;
    private String cellspacing;
    private String finishButtonId;
    private String nextButtonId;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.PropertySheet";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return "javax.faces.Grid";
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @param var The var to set.
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @param columns The columns to set.
     */
    public void setColumns(String columns) {
        this.columns = columns;
    }

    /**
     * @param externalConfig The externalConfig to set.
     */
    public void setExternalConfig(String externalConfig) {
        this.externalConfig = externalConfig;
    }

    /**
     * @param configArea Sets the named config area to use
     */
    public void setConfigArea(String configArea) {
        this.configArea = configArea;
    }

    /**
     * @param mode The mode, either "edit" or "view"
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @param readOnly The readOnly to set.
     */
    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @param validationEnabled The validationEnabled to set.
     */
    public void setValidationEnabled(String validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    /**
     * @param labelStyleClass Sets the style class for the label column
     */
    public void setLabelStyleClass(String labelStyleClass) {
        this.labelStyleClass = labelStyleClass;
    }

    /**
     * @param cellpadding Sets the cellpadding for the grid
     */
    public void setCellpadding(String cellpadding) {
        this.cellpadding = cellpadding;
    }

    /**
     * @param cellspacing Sets the cellspacing for the grid
     */
    public void setCellspacing(String cellspacing) {
        this.cellspacing = cellspacing;
    }

    /**
     * @param nextButtonId Sets the next button id
     */
    public void setNextButtonId(String nextButtonId) {
        this.nextButtonId = nextButtonId;
    }

    /**
     * @param finishButtonId Sets the finish button id
     */
    public void setFinishButtonId(String finishButtonId) {
        this.finishButtonId = finishButtonId;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setStringProperty(component, "value", this.value);
        setStringProperty(component, "mode", this.mode);
        setStringProperty(component, "configArea", this.configArea);
        setStringStaticProperty(component, "var", this.var);
        setIntProperty(component, "columns", this.columns);
        setStringStaticProperty(component, "labelStyleClass", this.labelStyleClass);
        setBooleanProperty(component, "externalConfig", this.externalConfig);
        setBooleanProperty(component, "readOnly", this.readOnly);
        setBooleanProperty(component, "validationEnabled", this.validationEnabled);
        setStringStaticProperty(component, "cellpadding", this.cellpadding);
        setStringStaticProperty(component, "cellspacing", this.cellspacing);
        setStringStaticProperty(component, "finishButtonId", this.finishButtonId);
        setStringStaticProperty(component, "nextButtonId", this.nextButtonId);
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#release()
     */
    public void release() {
        this.value = null;
        this.var = null;
        this.columns = null;
        this.externalConfig = null;
        this.configArea = null;
        this.readOnly = null;
        this.mode = null;
        this.validationEnabled = null;
        this.labelStyleClass = null;
        this.cellpadding = null;
        this.cellspacing = null;
        this.finishButtonId = null;
        this.nextButtonId = null;

        super.release();
    }
}
