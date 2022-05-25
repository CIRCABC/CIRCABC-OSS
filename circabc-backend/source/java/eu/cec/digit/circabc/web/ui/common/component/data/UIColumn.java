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
package eu.cec.digit.circabc.web.ui.common.component.data;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

/**
 * @author Guillaume
 */
public class UIColumn extends UIComponentBase {
    // ------------------------------------------------------------------------------
    // Component implementation

    private boolean primary = false;
    private boolean actions = false;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Data";
    }

    /**
     * Return the UI Component to be used as the header for this column
     *
     * @return UIComponent
     */
    public UIComponent getHeader() {
        return getFacet("header");
    }

    /**
     * Return the UI Component to be used as the footer for this column
     *
     * @return UIComponent
     */
    public UIComponent getFooter() {
        return getFacet("footer");
    }

    /**
     * Return the UI Component to be used as the large icon for this column
     *
     * @return UIComponent
     */
    public UIComponent getLargeIcon() {
        return getFacet("large-icon");
    }

    /**
     * Return the UI Component to be used as the small icon for this column
     *
     * @return UIComponent
     */
    public UIComponent getSmallIcon() {
        return getFacet("small-icon");
    }

    // ------------------------------------------------------------------------------
    // Strongly typed component property accessors

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.primary = (Boolean) values[1];
        this.actions = (Boolean) values[2];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {
        Object values[] = new Object[3];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = (this.primary ? Boolean.TRUE : Boolean.FALSE);
        values[2] = (this.actions ? Boolean.TRUE : Boolean.FALSE);
        return (values);
    }

    /**
     * @return true if this is the primary column
     */
    public boolean getPrimary() {
        ValueBinding vb = getValueBinding("primary");
        if (vb != null) {
            this.primary = (Boolean) vb.getValue(getFacesContext());
        }
        return this.primary;
    }

    /**
     * @param primary True if this is the primary column, false otherwise
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * @return true if this is the column containing actions for the current row
     */
    public boolean getActions() {
        ValueBinding vb = getValueBinding("actions");
        if (vb != null) {
            this.actions = (Boolean) vb.getValue(getFacesContext());
        }
        return this.actions;
    }

    /**
     * @param actions True if this is the column containing actions for the current row
     */
    public void setActions(boolean actions) {
        this.actions = actions;
    }
}
