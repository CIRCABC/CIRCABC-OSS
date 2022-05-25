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
 * Display a survey of IPM to encode.
 *
 * @author Matthieu Sprunck
 */
public class EncodeIpmTag extends HtmlComponentTag {

    //  ------------------------------------------------------------------------------
    // Component methods

    public static final String ATTR_VALUE = "value";
    public static final String ATTR_SURVEY = "survey";
    public static final String ATTR_LANG = "lang";
    /**
     * The value
     */
    private String value;

    //  ------------------------------------------------------------------------------
    // Bean implementation
    /**
     * The survey's short name
     */
    private String survey;
    /**
     * The selected lang
     */
    private String lang;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.EncodeIpm";
    }

    //  ------------------------------------------------------------------------------
    // Data

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return null;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.value = null;
        this.survey = null;
        this.lang = null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringProperty(component, ATTR_VALUE, this.value);
        setStringProperty(component, ATTR_SURVEY, this.survey);
        setStringProperty(component, ATTR_LANG, this.lang);
    }

    /**
     * Setter method for survey
     *
     * @param param the param to set
     */
    public void setSurvey(String survey) {
        this.survey = survey;
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
     * Setter method for lang
     *
     * @param lang the lang to set
     */
    public void setLang(String lang) {
        this.lang = lang;
    }
}
