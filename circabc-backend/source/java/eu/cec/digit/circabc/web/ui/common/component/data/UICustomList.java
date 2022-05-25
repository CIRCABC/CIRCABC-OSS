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

import eu.cec.digit.circabc.service.customisation.nav.NavigationPreference;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Pignot Yanick
 */
public class UICustomList extends UIRichList {
    // ------------------------------------------------------------------------------
    // Construction

    private NavigationPreference configuration = null;

    // ------------------------------------------------------------------------------
    // Component implementation

    /**
     * Default constructor
     */
    public UICustomList() {
        super("eu.cec.digit.circabc.faces.CustomListRenderer");
    }

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.Data";
    }

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;
        super.restoreState(context, state);

        this.configuration = (NavigationPreference) values[values.length - 1];
    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    public Object saveState(FacesContext context) {

        final Object values[] = (Object[]) super.saveState(context);

        final List<Object> list = new ArrayList<>(values.length + 1);
        list.addAll(Arrays.asList(values));
        list.add(this.configuration);

        return list.toArray();
    }

    /**
     * @return the configuration
     */
    public final NavigationPreference getConfiguration() {
        if (this.configuration == null) {
            ValueBinding vb = getValueBinding("configuration");
            if (vb != null) {
                this.configuration = (NavigationPreference) vb.getValue(getFacesContext());
            }
        }

        return configuration;
    }

    /**
     * @param configuration the configuration to set
     */
    public final void setConfiguration(NavigationPreference configuration) {
        this.configuration = configuration;
    }

}
