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
package eu.cec.digit.circabc.web.ui.tag;

import eu.cec.digit.circabc.web.ui.component.UIDisplayer;
import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.component.UIComponent;
import javax.servlet.jsp.JspException;

/**
 * @author Guillaume
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. This class seems
 * to be developed for CircaBC
 */
public class DisplayerTag extends BaseComponentTag {

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.Displayer";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        // the component is self renderering
        return null;
    }

    /**
     * Override this to allow the displayer component to control whether child components are rendered
     * by the JSP tag framework. This is a nasty solution as it requires a reference to the
     * UIDisplayer instance and also specific knowledge of the component type that is created by the
     * framework for this tag.
     * <p>
     * The reason for this solution is to allow any child content (including HTML tags) to be
     * displayed inside the UIDisplayer component without having to resort to the awful JSF Component
     * getRendersChildren() mechanism - as this would force the use of the verbatim tags for ALL
     * non-JSF child content!
     */
    protected int getDoStartValue() throws JspException {
        UIComponent component = getComponentInstance();
        if (component instanceof UIDisplayer) {
            if (component.isRendered() == true) {
                return EVAL_BODY_INCLUDE;
            } else {
                return SKIP_BODY;
            }
        }
        return EVAL_BODY_INCLUDE;
    }
}
