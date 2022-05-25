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
package eu.cec.digit.circabc.web.ui.common.tag.data;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.component.UIComponent;


/**
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. This class seems
 * to be developed for CircaBC
 */
public class CustomListTag extends BaseComponentTag {
    // ------------------------------------------------------------------------------
    // Component methods

    /**
     * the header row CSS Class
     */
    private String headerStyleClass;
    /**
     * the row CSS Class
     */
    private String rowStyleClass;
    /**
     * the alternate row CSS Class
     */
    private String altRowStyleClass;
    /**
     * the styleClass
     */
    private String styleClass;

    // ------------------------------------------------------------------------------
    // Bean implementation
    /**
     * the value
     */
    private String value;
    /**
     * the navigation preference
     */
    private String configuration;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.CustomList";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.CustomListRenderer";
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.value = null;
        this.styleClass = null;
        this.rowStyleClass = null;
        this.altRowStyleClass = null;
        this.headerStyleClass = null;
        this.configuration = null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, "value", this.value);
        setStringProperty(component, "styleClass", this.styleClass);
        setStringProperty(component, "rowStyleClass", this.rowStyleClass);
        setStringProperty(component, "altRowStyleClass", this.altRowStyleClass);
        setStringProperty(component, "headerStyleClass", this.headerStyleClass);
        setStringBindingProperty(component, "configuration", this.configuration);
    }

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

// ------------------------------------------------------------------------------
    // Private data

    /**
     * Set the styleClass
     *
     * @param styleClass the styleClass
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Set the the row CSS Class
     *
     * @param rowStyleClass the the row CSS Class
     */
    public void setRowStyleClass(String rowStyleClass) {
        this.rowStyleClass = rowStyleClass;
    }

    /**
     * Set the alternate row CSS Class
     *
     * @param altRowStyleClass the alternate row CSS Class
     */
    public void setAltRowStyleClass(String altRowStyleClass) {
        this.altRowStyleClass = altRowStyleClass;
    }

    /**
     * Set the header row CSS Class
     *
     * @param headerStyleClass the header row CSS Class
     */
    public void setHeaderStyleClass(String headerStyleClass) {
        this.headerStyleClass = headerStyleClass;
    }

    /**
     * @return the configuration
     */
    public final String getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public final void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
}
