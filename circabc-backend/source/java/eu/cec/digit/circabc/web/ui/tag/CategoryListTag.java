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

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

import javax.faces.component.UIComponent;

/**
 * @author Stephane Clinckart
 */
public class CategoryListTag extends HtmlComponentTag {

    /**
     * The value (ie the navigation list)
     */
    private String value;
    private String chooseHeader;
    /**
     * Define if the Categories List has to be displayed or not.
     */
    private String displayCategories;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.CategoryList";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(final UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, "value", this.value);
        setStringBindingProperty(component, "chooseHeader", this.chooseHeader);
        setBooleanProperty(component, "displayCategories", this.displayCategories);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();
        this.value = null;
        this.chooseHeader = null;
        this.displayCategories = null;
    }

    /**
     * Set the value (ie the navigation list)
     *
     * @param value the value (ie the navigation list)
     */
    public void setValue(final String value) {
        this.value = value;
    }

    public void setChooseHeader(final String chooseHeader) {
        this.chooseHeader = chooseHeader;
    }

    public void setDisplayCategories(final String displayCategories) {
        this.displayCategories = displayCategories;
    }

}
