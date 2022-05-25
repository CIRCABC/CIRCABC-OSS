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

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

/**
 * @author Guillaume
 */
public class NavigationListTag extends HtmlComponentTag {

    private String bannerStyle;

    private String renderPropertyName;
    /**
     * The actionListener
     */
    private String actionListener;
    /**
     * The value (ie the navigation list)
     */
    private String value;
    /**
     * The separator between the link
     */
    private String separator;
    /**
     * The CSS class for the separator
     */
    private String separatorClass;
    /**
     * the separatorFirst boolean
     */
    private String separatorFirst;
    /**
     * the onclick String
     */
    private String onclick;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.NavigationList";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.NavigationListRenderer";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setStringProperty(component, "value", this.value);
        setStringProperty(component, "separator", this.separator);
        setStringProperty(component, "separatorClass", this.separatorClass);
        setBooleanProperty(component, "separatorFirst", this.separatorFirst);
        setStringProperty(component, "onclick", this.onclick);
        setStringProperty(component, "bannerStyle", this.bannerStyle);
        setStringProperty(component, "renderPropertyName", this.renderPropertyName);
        setActionListenerProperty((UICommand) component, this.actionListener);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();

        this.value = null;
        this.separator = null;
        this.separatorClass = null;
        this.separatorFirst = null;
        this.actionListener = null;
        this.onclick = null;
        this.bannerStyle = null;
        this.renderPropertyName = null;
    }

    /**
     * Set the actionListener
     *
     * @param actionListener the actionListener
     */
    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    /**
     * Set the value (ie the navigation list)
     *
     * @param value the value (ie the navigation list)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the separator between the link
     *
     * @param separator the separator to put between the link
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * Set the style to use for the separator
     *
     * @param separatorClass the separatorClass style to use for the separator
     */
    public void setSeparatorClass(String separatorClass) {
        this.separatorClass = separatorClass;
    }

    /**
     * Set the property to show the separator ahead
     *
     * @param separatorFirst False to hide the separator ahead
     */
    public void setSeparatorFirst(String separatorFirst) {
        this.separatorFirst = separatorFirst;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public String getBannerStyle() {
        return bannerStyle;
    }

    public void setBannerStyle(String bannerStyle) {
        this.bannerStyle = bannerStyle;
    }

    /**
     * @return the renderPropertyName
     */
    public String getRenderPropertyName() {
        return renderPropertyName;
    }

    /**
     * @param renderPropertyName the renderPropertyName to set
     */
    public void setRenderPropertyName(String renderPropertyName) {
        this.renderPropertyName = renderPropertyName;
    }

}
