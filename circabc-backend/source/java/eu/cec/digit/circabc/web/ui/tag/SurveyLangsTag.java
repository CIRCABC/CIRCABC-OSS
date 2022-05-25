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
 * Displays the list of translations for a survey.
 *
 * @author Matthieu Sprunck
 * @author Guillaume
 */
public class SurveyLangsTag extends HtmlComponentTag {

    // ------------------------------------------------------------------------------
    // Component methods

    /**
     * The variable's name of the attribute value
     */
    public static final String ATTR_VALUE = "value";
    /**
     * The variable's name of the attribute value
     */
    public static final String ATTR_WAI = "wai";
    /**
     * The value
     */
    private String value;
    /**
     * The wai status
     */
    private String wai;

    // ------------------------------------------------------------------------------
    // Bean implementation

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    @Override
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.SurveyLangs";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    @Override
    public String getRendererType() {
        return null;
    }

    // ------------------------------------------------------------------------------
    // Data

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    @Override
    public void release() {
        super.release();
        this.value = null;
        this.wai = null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, ATTR_VALUE, this.value);
        setBooleanProperty(component, ATTR_WAI, this.wai);
    }

    /**
     * Setter method for value
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Setter method for wai status
     *
     * @param wai the wai status to set
     */
    public void setWai(String wai) {
        this.wai = wai;
    }
}
