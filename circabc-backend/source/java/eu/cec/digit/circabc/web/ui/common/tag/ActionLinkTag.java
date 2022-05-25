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
package eu.cec.digit.circabc.web.ui.common.tag;

import org.alfresco.web.ui.common.tag.HtmlComponentTag;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

/**
 * @author Guillaume
 */
public class ActionLinkTag extends HtmlComponentTag {

    /**
     * the target
     */
    private String target;
    /**
     * the value (text to display)
     */
    private String value;
    /**
     * the href link
     */
    private String href;
    /**
     * the action
     */
    private String action;
    /**
     * the actionListener
     */
    private String actionListener;
    /**
     * the image
     */
    private String image;
    /**
     * the showLink boolean
     */
    private String showLink;
    /**
     * the anchor name
     */
    private String anchor;
    /**
     * the immediate flag
     */
    private String immediate;
    /**
     * the noDisplay flag
     */
    private String noDisplay;
    /**
     * the padding flag
     */
    private String padding;
    /**
     * the onclick flag
     */
    private String onclick;
    /**
     * the escape flag
     */
    private String escape;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    @Override
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.ActionLink";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    @Override
    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.ActionLinkRenderer";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setActionProperty((UICommand) component, this.action);
        setActionListenerProperty((UICommand) component, this.actionListener);
        setStringProperty(component, "image", this.image);
        setBooleanProperty(component, "showLink", this.showLink);
        setStringProperty(component, "href", this.href);
        setStringProperty(component, "value", this.value);
        setStringProperty(component, "target", this.target);
        setStringProperty(component, "anchor", this.anchor);
        setBooleanProperty(component, "immediate", this.immediate);
        setBooleanProperty(component, "noDisplay", this.noDisplay);
        setIntProperty(component, "padding", this.padding);
        setStringProperty(component, "onclick", this.onclick);
        setBooleanProperty(component, "escape", this.escape);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    @Override
    public void release() {
        super.release();
        this.value = null;
        this.href = null;
        this.action = null;
        this.actionListener = null;
        this.image = null;
        this.showLink = null;
        this.anchor = null;
        this.target = null;
        this.immediate = null;
        this.noDisplay = null;
        this.padding = null;
        this.onclick = null;
        this.escape = null;
    }

    /**
     * Set the value
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Set the href to use instead of a JSF action
     *
     * @param href the href
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * Set the action
     *
     * @param action the action
     */
    public void setAction(String action) {
        this.action = action;
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
     * Set the anchor
     *
     * @param anchor the anchor
     */
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * Set the image
     *
     * @param image the image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Set the showLink
     *
     * @param showLink the showLink
     */
    public void setShowLink(String showLink) {
        this.showLink = showLink;
    }

    /**
     * Set the target
     *
     * @param target the target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Set the immediate
     *
     * @param immediate immediate
     */
    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    /**
     * Set the noDisplay
     *
     * @param noDisplay noDisplay
     */
    public void setNoDisplay(String noDisplay) {
        this.noDisplay = noDisplay;
    }

    /**
     * @return the padding
     */
    public final String getPadding() {
        return padding;
    }

    /**
     * @param padding the padding to set
     */
    public final void setPadding(String padding) {
        this.padding = padding;
    }

    /**
     * @return the onclick
     */
    public final String getOnclick() {
        return onclick;
    }

    /**
     * @param onclick the onclick to set
     */
    public final void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    /**
     * @return the escape
     */
    public String getEscape() {
        return escape;
    }

    /**
     * @param escape the escape to set
     */
    public void setEscape(String escape) {
        this.escape = escape;
    }

}
