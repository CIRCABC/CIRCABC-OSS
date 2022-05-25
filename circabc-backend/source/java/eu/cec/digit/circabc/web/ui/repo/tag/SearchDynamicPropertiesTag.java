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
package eu.cec.digit.circabc.web.ui.repo.tag;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

import javax.faces.component.UIComponent;

/**
 * @author yanick Pignot
 */
public class SearchDynamicPropertiesTag extends HtmlComponentTag {

    /**
     * the bean reference
     */
    private String bean;
    /**
     * the variable Map reference
     */
    private String var;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.AdvancedSearch";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        // self rendering component
        return null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setStringProperty(component, "bean", this.bean);
        setStringProperty(component, "var", this.var);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();

        this.bean = null;
        this.var = null;
    }

    /**
     * Set the bean reference
     *
     * @param bean the bean reference
     */
    public void setBean(String bean) {
        this.bean = bean;
    }

    /**
     * Set the var
     *
     * @param var the var
     */
    public void setVar(String var) {
        this.var = var;
    }
}
