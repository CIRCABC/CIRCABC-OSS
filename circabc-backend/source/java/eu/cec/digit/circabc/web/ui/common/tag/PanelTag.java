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
package eu.cec.digit.circabc.web.ui.common.tag;

import eu.cec.digit.circabc.web.ui.common.component.UIPanel;
import org.alfresco.web.ui.common.tag.HtmlComponentTag;

import javax.faces.component.UIComponent;
import javax.servlet.jsp.JspException;

/**
 * @author Guillaume
 */
public class PanelTag extends HtmlComponentTag {

    /**
     * the facets component Id
     */
    private String facetsId;
    /**
     * the label
     */
    private String label;
    /**
     * the style class of the label
     */
    private String styleClassLabel;
    /**
     * the tooltip of the image
     */
    private String tooltip;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.Panel";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        // the component is self renderering
        return null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setStringProperty(component, "label", this.label);
        setStringProperty(component, "styleClassLabel", this.styleClassLabel);
        setStringProperty(component, "tooltip", this.tooltip);
        setStringProperty(component, "facetsId", this.facetsId);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.label = null;
        this.styleClassLabel = null;
        this.tooltip = null;
    }

    /**
     * Override this to allow the panel component to control whether child components are rendered by
     * the JSP tag framework. This is a nasty solution as it requires a reference to the UIPanel
     * instance and also specific knowledge of the component type that is created by the framework for
     * this tag.
     * <p>
     * The reason for this solution is to allow any child content (including HTML tags) to be
     * displayed inside the UIPanel component without having to resort to the awful JSF Component
     * getRendersChildren() mechanism - as this would force the use of the verbatim tags for ALL
     * non-JSF child content!
     */
    protected int getDoStartValue() throws JspException {
        UIComponent component = getComponentInstance();
        if (component instanceof UIPanel) {
            if (component.isRendered() == true) {
                return EVAL_BODY_INCLUDE;
            } else {
                return SKIP_BODY;
            }
        }
        return EVAL_BODY_INCLUDE;
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
     * Set the styleClassLabel
     *
     * @param styleClassLabel the styleClassLabel
     */
    public void setStyleClassLabel(String styleClassLabel) {
        this.styleClassLabel = styleClassLabel;
    }

    /**
     * Set the tooltip
     *
     * @param tooltip the tooltip
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Set the facetsId
     *
     * @param facetsId the facetsId
     */
    public void setFacetsId(String facetsId) {
        this.facetsId = facetsId;
    }
}
