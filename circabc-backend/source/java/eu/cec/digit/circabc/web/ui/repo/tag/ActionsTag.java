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
 * Tag for eu.cec.digit.circabc.web.ui.repo.component.UIActions.
 *
 * @author patrice.coppens@trasys.lu
 */
public class ActionsTag extends HtmlComponentTag {

    /**
     * the context object
     */
    private String context;
    /**
     * the value (id of the action group config to use)
     */
    private String value;
    /**
     * the showLink boolean
     */
    private String showLink;
    /**
     * the vertical boolean
     */
    private String vertical;
    /**
     * the list boolean
     */
    private String list;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.Actions";
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
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setBooleanProperty(component, "showLink", this.showLink);
        setStringProperty(component, "value", this.value);
        setStringBindingProperty(component, "context", this.context);
        setBooleanProperty(component, "vertical", this.vertical);
        setBooleanProperty(component, "list", this.list);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();
        this.value = null;
        this.showLink = null;
        this.context = null;
        this.vertical = null;
        this.list = null;
    }

    /**
     * Set the value (id of the action group config to use).
     *
     * @param value the value (id of the action group config to use)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the showLink.
     *
     * @param showLink the showLink
     */
    public void setShowLink(String showLink) {
        this.showLink = showLink;
    }

    /**
     * Set the context object.
     *
     * @param context the context object
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @param vertical the vertical to set
     */
    public void setVertical(String vertical) {
        this.vertical = vertical;
    }


    /**
     * @param list the list to set
     */
    public void setList(String list) {
        this.list = list;
    }


}
