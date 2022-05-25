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
package eu.cec.digit.circabc.web.ui.common.tag.data;

import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.component.UIComponent;


/**
 * @author Guillaume
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. Class
 * implementation didn't change between Alfresco versions.
 */
public class ColumnTag extends BaseComponentTag {

    /**
     * the actions
     */
    private String actions;
    /**
     * the styleClass
     */
    private String styleClass;
    /**
     * the primary
     */
    private String primary;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.ListColumn";
    }

    // ------------------------------------------------------------------------------
    // Tag properties

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        // the component is renderer by the parent
        return null;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.primary = null;
        this.actions = null;
        this.styleClass = null;
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setBooleanProperty(component, "primary", this.primary);
        setBooleanProperty(component, "actions", this.actions);
        setStringProperty(component, "styleClass", this.styleClass);
    }

    /**
     * Set if this is the primary column
     *
     * @param primary the primary if "true", otherwise false
     */
    public void setPrimary(String primary) {
        this.primary = primary;
    }

    /**
     * Set the styleClass
     *
     * @param styleClass the styleClass
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Set if this is the actions column
     *
     * @param actions the actions if "true", otherwise false
     */
    public void setActions(String actions) {
        this.actions = actions;
    }
}
