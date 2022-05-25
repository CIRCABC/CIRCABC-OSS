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
package eu.cec.digit.circabc.web.ui.repo.tag.evaluator;

import eu.cec.digit.circabc.web.ui.repo.component.evaluator.PermissionEvaluator;
import org.alfresco.web.ui.common.tag.evaluator.GenericEvaluatorTag;

import javax.faces.component.UIComponent;
import javax.servlet.jsp.JspException;

/**
 * @author Clinckart Stephane
 */
public class PermissionEvaluatorTag extends GenericEvaluatorTag {

    /**
     * the allow permissions
     */
    private String allow;
    /**
     * the deny permissions
     */
    private String deny;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.PermissionEvaluator";
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
        if (component instanceof PermissionEvaluator) {
            if (((PermissionEvaluator) component).evaluate() == true) {
                return EVAL_BODY_INCLUDE;
            } else {
                return SKIP_BODY;
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringProperty(component, "allow", this.allow);
        setStringProperty(component, "deny", this.deny);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.allow = null;
        this.deny = null;
    }

    /**
     * Set the allow permissions
     *
     * @param allow the allow permissions
     */
    public void setAllow(String allow) {
        this.allow = allow;
    }

    /**
     * Set the deny permissions
     *
     * @param deny the deny permissions
     */
    public void setDeny(String deny) {
        this.deny = deny;
    }
}
