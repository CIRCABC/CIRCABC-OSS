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
package eu.cec.digit.circabc.web.ui.component;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import java.util.List;

/**
 * Class for setter and getter
 *
 * @author Guillaume
 */
public class UINavigationList extends UICommand {
    // ------------------------------------------------------------------------------
    // Construction

    /**
     * The value (ie the navigation list)
     */
    private List value;

    // ------------------------------------------------------------------------------
    // Component implementation
    /**
     * The separator between the link
     */
    private String separator;
    /**
     * The CSS class for the separator
     */
    private String separatorClass;
    /**
     * The separatorFirst property which indicate if the separator should be put ahead (default
     * behavior)
     */
    private Boolean separatorFirst = null;

    // ------------------------------------------------------------------------------
    // Strongly typed component property accessors
    /**
     * The CSS class to use for general html composant
     */
    private String styleClass = null;
    private String onclick = null;
    private String bannerStyle = "normal";
    private String renderPropertyName = "shortname";

    /**
     * Default Constructor
     */
    public UINavigationList() {
        setRendererType("eu.cec.digit.circabc.faces.NavigationListRenderer");
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.NavigationList";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(final FacesContext context, final Object state) {
        final Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.value = (List) restoreAttachedState(context, values[1]);
        this.separator = (String) values[2];
        this.separatorClass = (String) values[3];
        this.separatorFirst = (Boolean) values[4];
        this.styleClass = (String) values[5];
        this.onclick = (String) values[6];
        this.bannerStyle = (String) values[7];
        this.renderPropertyName = (String) values[8];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(final FacesContext context) {
        final Object values[] = new Object[9];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, this.value);
        values[2] = this.separator;
        values[3] = this.separatorClass;
        values[4] = this.separatorFirst;
        values[5] = this.styleClass;
        values[6] = this.onclick;
        values[7] = this.bannerStyle;
        values[8] = this.renderPropertyName;
        return (values);
    }

    /**
     * Get the value (ie the navigation list)
     *
     * @return The value (ie the navigation list).
     */
    public List getValue() {
        final ValueBinding vb = getValueBinding("value");
        if (vb != null) {
            this.value = (List) vb.getValue(getFacesContext());
        }

        return this.value;
    }

    /**
     * Set the value (ie the navigation list)
     *
     * @param value The value (ie the navigation list)
     */
    public void setValue(final List value) {
        this.value = value;
    }

    /**
     * Get the separator between the link
     *
     * @return The separator between the link.
     */
    public String getSeparator() {
        final ValueBinding vb = getValueBinding("separator");
        if (vb != null) {
            this.separator = (String) vb.getValue(getFacesContext());
        }

        return this.separator;
    }

    /**
     * Set the separator between the link
     *
     * @param separator The separator between the link.
     */
    public void setSeparators(final String separator) {
        this.separator = separator;
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * Get the CSS class for the separator
     *
     * @return The CSS class for the separator.
     */
    public String getSeparatorClass() {
        final ValueBinding vb = getValueBinding("separatorClass");
        if (vb != null) {
            this.separatorClass = (String) vb.getValue(getFacesContext());
        }

        return this.separatorClass;
    }

    /**
     * Set the CSS class for the separator
     *
     * @param separatorClass The CSS class for the separator.
     */
    public void setSeparatorClass(final String separatorClass) {
        this.separatorClass = separatorClass;
    }

    /**
     * Get the separatorFirst property
     *
     * @return the separatorFirst
     */
    public boolean getSeparatorFirst() {
        final ValueBinding vb = getValueBinding("separatorFirst");
        if (vb != null) {
            this.separatorFirst = (Boolean) vb.getValue(getFacesContext());
        }

        if (this.separatorFirst != null) {
            return this.separatorFirst;
        } else {
            // return default
            return true;
        }
    }

    /**
     * Set the separatorFirst property
     *
     * @param separatorFirst Indicate if the separator should be put ahead (default behavior)
     */
    public void setSeparatorFirst(final boolean separatorFirst) {
        this.separatorFirst = separatorFirst;
    }

    /**
     * Get the CSS class to use for general html composant
     *
     * @return The CSS class to use for general html composant
     */
    public String getStyleClass() {
        final ValueBinding vb = getValueBinding("styleClass");
        if (vb != null) {
            this.styleClass = (String) vb.getValue(getFacesContext());
        }

        return this.styleClass;
    }

    /**
     * Set the CSS class to use for general html composant
     *
     * @param styleClass The CSS class to use for general html composant
     */
    public void setStyleClass(final String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Returns the onclick handler
     *
     * @return The onclick handler
     */
    public String getOnclick() {
        final ValueBinding vb = getValueBinding("onclick");
        if (vb != null) {
            this.onclick = (String) vb.getValue(getFacesContext());
        }

        return this.onclick;
    }

    /**
     * Sets the onclick handler
     *
     * @param onclick The onclick handler
     */
    public void setOnclick(final String onclick) {
        this.onclick = onclick;
    }

    public String getBannerStyle() {
        return bannerStyle;
    }

    public void setBannerStyle(String bannerStyle) {
        this.bannerStyle = bannerStyle;
    }

    /**
     * @return the renderPropertyName
     */
    public String getRenderPropertyName() {

        final ValueBinding vb = getValueBinding("renderPropertyName");
        if (vb != null) {
            this.renderPropertyName = (String) vb.getValue(getFacesContext());
        }

        return this.renderPropertyName;

    }

    /**
     * @param renderPropertyName the renderPropertyName to set
     */
    public void setRenderPropertyName(String renderPropertyName) {
        this.renderPropertyName = renderPropertyName;
    }

}
