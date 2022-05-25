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
package eu.cec.digit.circabc.web.ui.common.component;

import eu.cec.digit.circabc.web.ui.common.UtilsCircabc;
import eu.cec.digit.circabc.web.ui.common.WebResourcesCircabc;
import org.alfresco.web.ui.common.Utils;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import java.io.IOException;

/**
 * @author Guillaume
 */
public class UIPanel extends UICommand {
    // ------------------------------------------------------------------------------
    // Component Impl

    // component settings
    private String label = null;
    private String styleClassLabel = null;
    private String tooltip = null;
    private String facetsId = null;
    // component state
    private boolean hasLabel = false;

    /**
     * Default constructor
     */
    public UIPanel() {
        setRendererType(null);
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    @Override
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Controls";
    }

    // ------------------------------------------------------------------------------
    // Strongly typed component property accessors

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();
        // Determine if we have a component on the header
        UIComponent titleComponent = getTitleComponent();

        // Determine whether we have a label
        String label = getLabel();
        if (label != null || titleComponent != null) {
            label = Utils.encode(label);
            this.hasLabel = true;
        }

        // output first part of the panel
        out.write("<div");
        Utils.outputAttribute(out, getAttributes().get("id") + "Global", "id");
        Utils.outputAttribute(out, getAttributes().get("styleClass"), "class");
        out.write(">");

        if (this.hasLabel) {
            // start the containing div if we have any adornments
            out.write("<div");
            Utils.outputAttribute(out, getAttributes().get("id") + "Header", "id");
            Utils.outputAttribute(out, this.styleClassLabel, "class");
            out.write(">");
            if (this.tooltip != null) {
                out.write(UtilsCircabc
                        .buildImageTag(context, WebResourcesCircabc.PUCE_PANEL, this.tooltip, this.tooltip,
                                this.styleClassLabel));
            } else {
                out.write(UtilsCircabc.buildImageTag(context, WebResourcesCircabc.PUCE_PANEL, ".", ".",
                        this.styleClassLabel));
            }
            out.write(label);

//        render the title component if supplied
            if (titleComponent != null) {
                out.write("&nbsp;&nbsp;");
                Utils.encodeRecursive(context, titleComponent);
            }

            out.write("</div>");
        }

    }

    /**
     * Return the UI Component to be displayed on the right of the panel title area
     *
     * @return UIComponent
     */
    public UIComponent getTitleComponent() {
        UIComponent titleComponent = null;

        // attempt to find a component with the specified ID
        String facetsId = getFacetsId();
        if (facetsId != null) {
            UIForm parent = Utils.getParentForm(FacesContext.getCurrentInstance(), this);
            UIComponent facetsComponent = parent.findComponent(facetsId);
            if (facetsComponent != null) {
                // get the 'title' facet from the component
                titleComponent = facetsComponent.getFacet("title");
            }
        }

        return titleComponent;
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        if (!isRendered()) {
            return;
        }

        ResponseWriter out = context.getResponseWriter();
        out.write("</div>");
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    @Override
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.label = (String) values[1];
        this.styleClassLabel = (String) values[2];
        this.tooltip = (String) values[3];
        this.facetsId = (String) values[4];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    @Override
    public Object saveState(FacesContext context) {
        Object values[] = new Object[]{
                super.saveState(context),
                this.label,
                this.styleClassLabel,
                this.tooltip,
                this.facetsId};
        return values;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        ValueBinding vb = getValueBinding("label");
        if (vb != null) {
            this.label = (String) vb.getValue(getFacesContext());
        }

        return this.label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Get the styleClass for the label to use
     *
     * @return the styleClass for the label
     */
    public String getStyleClassLabel() {
        ValueBinding vb = getValueBinding("styleClassLabel");
        if (vb != null) {
            this.styleClassLabel = (String) vb.getValue(getFacesContext());
        }

        return this.styleClassLabel;
    }

    // ------------------------------------------------------------------------------
    // Private members

    /**
     * Set the styleClassLabel to use
     *
     * @param styleClassLabel the styleClassLabel component Id
     */
    public void setStyleClassLabel(String styleClassLabel) {
        this.styleClassLabel = styleClassLabel;
    }

    /**
     * Get the tooltip for the image to use
     *
     * @return String the tooltip for the image
     */
    public String getTooltip() {
        ValueBinding vb = getValueBinding("tooltip");
        if (vb != null) {
            this.tooltip = (String) vb.getValue(getFacesContext());
        }

        return this.tooltip;
    }

    /**
     * Set the tooltip to use for the image
     *
     * @param tooltip for the image to set
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Get the facets component Id to use
     *
     * @return the facets component Id
     */
    public String getFacetsId() {
        ValueBinding vb = getValueBinding("facets");
        if (vb != null) {
            this.facetsId = (String) vb.getValue(getFacesContext());
        }

        return this.facetsId;
    }

    /**
     * Set the facets component Id to use
     *
     * @param facets the facets component Id
     */
    public void setFacetsId(String facets) {
        this.facetsId = facets;
    }

}
