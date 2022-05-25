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

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;

/**
 * @author Guillaume
 */
public class SimpleSearchTag extends HtmlComponentTag {

    /**
     * The label of the Box
     */
    private String label;
    /**
     * The alt text of the image at the left of the label
     */
    private String labelAltText;
    /**
     * The label of the button
     */
    private String button;
    /**
     * The alt text of the image for the search action
     */
    private String buttonAltText;
    /**
     * the actionListener
     */
    private String actionListener;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.SimpleSearch";
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

        setActionListenerProperty((UICommand) component, this.actionListener);
        setStringProperty(component, "label", this.label);
        setStringProperty(component, "labelAltText", this.labelAltText);
        setStringProperty(component, "button", this.button);
        setStringProperty(component, "buttonAltText", this.buttonAltText);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();

        this.actionListener = null;
        this.label = null;
        this.labelAltText = null;
        this.button = null;
        this.buttonAltText = null;
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
     * Set the label of the Box
     *
     * @param label the label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Set the alt text of the image at the left of the label
     *
     * @param labelAltText the labelAltText
     */
    public void setLabelAltText(String labelAltText) {
        this.labelAltText = labelAltText;
    }

    /**
     * Set the button of the Box
     *
     * @param button the button
     */
    public void setButton(String button) {
        this.button = button;
    }

    /**
     * Set the alt text of the button for the search action
     *
     * @param buttonAltText the buttonAltText
     */
    public void setButtonAltText(String buttonAltText) {
        this.buttonAltText = buttonAltText;
    }
}
