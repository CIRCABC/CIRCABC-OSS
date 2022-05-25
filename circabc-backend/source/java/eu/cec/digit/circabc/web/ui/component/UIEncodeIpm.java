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

import eu.cec.digit.circabc.web.client.HttpClientHelper;
import eu.cec.digit.circabc.web.ui.tag.SurveyLangsTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Component to encode an IPM survey.
 *
 * @author Matthieu Sprunck
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 SelfRenderingComponent was moved to Spring. This class seems
 * to be developed for CircaBC
 */
public class UIEncodeIpm extends SelfRenderingComponent {

    // ------------------------------------------------------------------------------
    // Component implementation

    private static final Log logger = LogFactory.getLog(UIEncodeIpm.class);
    /**
     * The url
     */
    private String value;

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.EncodeIpm";
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("encodeBegin() for <ci:encode/> Id: " + getId());
        }

        // Get the url
        String url = getValue();

        // Get the current session (don't create a new session)
        HttpSession session = (HttpSession) context.getExternalContext()
                .getSession(false);
        // Call the IPM server
        InputStream in = HttpClientHelper.call(session, url);

        // Write the IPM content
        if (in != null) {
            ResponseWriter out = context.getResponseWriter();
            Reader inr = new InputStreamReader(in, "UTF-8");
            try {
                HttpClientHelper.copy(inr, out);
            } finally {
                inr.close();
                in.close();
            }
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#getRendersChildren()
     */
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * Get the value (for this component the value is an object used as the DataModel)
     *
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public String getValue() {
        ValueBinding vb = getValueBinding(SurveyLangsTag.ATTR_VALUE);
        if (vb != null) {
            this.value = (String) vb.getValue(getFacesContext());
        }
        return this.value;
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
        if (logger.isDebugEnabled()) {
            logger.debug("value setted " + value);
        }
    }
}
