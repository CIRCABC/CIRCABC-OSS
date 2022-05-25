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

import javax.faces.component.UIComponentBase;
import javax.faces.el.ValueBinding;

/**
 * @author Guillaume
 */
public class UIDisplayer extends UIComponentBase {
    // ------------------------------------------------------------------------------
    // Component Impl

    /**
     * Default constructor
     */
    public UIDisplayer() {
        setRendererType(null);
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Controls";
    }

    /**
     * Force the get of the value
     */
    @Override
    public boolean isRendered() {
        ValueBinding vb = getValueBinding("rendered");
        Boolean v = vb != null ? (Boolean) vb.getValue(getFacesContext()) : null;
        return v != null ? v : true;
    }
}
