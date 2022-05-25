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

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

import javax.faces.component.UIComponent;

/**
 * @author Guillaume
 */
public class SortLinkTag extends HtmlComponentTag {
    // ------------------------------------------------------------------------------
    // Component methods

    /**
     * the label
     */
    private String label;
    /**
     * the value
     */
    private String value;
    /**
     * the sorting mode
     */
    private String mode;
    /**
     * the tooltip for ascending order
     */
    private String tooltipAscending;
    /**
     * the tooltip for descending ordere
     */
    private String tooltipDescending;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.SortLink";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return null;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.mode = null;
        this.value = null;
        this.label = null;
        this.tooltipAscending = null;
        this.tooltipDescending = null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringProperty(component, "value", this.value);
        setStringProperty(component, "label", this.label);
        setStringProperty(component, "mode", this.mode);
        setStringProperty(component, "tooltipAscending", this.tooltipAscending);
        setStringProperty(component, "tooltipDescending", this.tooltipDescending);
    }

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the sorting mode (see IDataContainer constants)
     *
     * @param mode the sort mode
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Set the label
     *
     * @param label the label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Set the tooltip for ascending order
     *
     * @param tooltipAscending the tooltipAscending
     */
    public void setTooltipAscending(String tooltipAscending) {
        this.tooltipAscending = tooltipAscending;
    }

    /**
     * Set the tooltip for descending order
     *
     * @param tooltipDescending the tooltipDescending
     */
    public void setTooltipDescending(String tooltipDescending) {
        this.tooltipDescending = tooltipDescending;
    }
}
