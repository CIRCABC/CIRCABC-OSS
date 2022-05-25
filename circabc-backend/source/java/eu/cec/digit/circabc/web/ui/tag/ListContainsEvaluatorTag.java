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

import org.alfresco.web.ui.common.tag.evaluator.GenericEvaluatorTag;

import javax.faces.component.UIComponent;

/**
 * Tag to check if a list contains an object.
 *
 * @author sprunma
 */
public class ListContainsEvaluatorTag extends GenericEvaluatorTag {

    public static final String ATTR_LIST = "list";
    /**
     * list
     */
    private String list;

    @Override
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.ListContainsEvaluator";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, "list", this.list);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.list = null;
    }

    /**
     * Set the list
     *
     * @param list the list value binding
     */
    public void setList(String list) {
        this.list = list;
    }
}
