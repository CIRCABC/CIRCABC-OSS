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
 * @author Guillaume
 */
public class LockIconTag extends HtmlComponentTag {

    /**
     * the lockedOwnerTooltip
     */
    private String lockedOwnerTooltip;
    /**
     * the lockedUserTooltip
     */
    private String lockedUserTooltip;
    /**
     * the lockImage
     */
    private String lockImage;
    /**
     * the lockOwnerImage
     */
    private String lockOwnerImage;
    /**
     * the value
     */
    private String value;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.LockIcon";
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
        setStringProperty(component, "lockImage", this.lockImage);
        setStringProperty(component, "lockOwnerImage", this.lockOwnerImage);
        setStringProperty(component, "lockedOwnerTooltip", this.lockedOwnerTooltip);
        setStringProperty(component, "lockedUserTooltip", this.lockedUserTooltip);
        setStringBindingProperty(component, "value", this.value);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();
        this.lockImage = null;
        this.lockOwnerImage = null;
        this.lockedOwnerTooltip = null;
        this.lockedUserTooltip = null;
        this.value = null;
    }

    /**
     * Set the lockImage
     *
     * @param lockImage the lockImage
     */
    public void setLockImage(String lockImage) {
        this.lockImage = lockImage;
    }

    /**
     * Set the lockOwnerImage
     *
     * @param lockOwnerImage the lockOwnerImage
     */
    public void setLockOwnerImage(String lockOwnerImage) {
        this.lockOwnerImage = lockOwnerImage;
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
     * Set the lockedOwnerTooltip
     *
     * @param lockedOwnerTooltip the lockedOwnerTooltip
     */
    public void setLockedOwnerTooltip(String lockedOwnerTooltip) {
        this.lockedOwnerTooltip = lockedOwnerTooltip;
    }

    /**
     * Set the lockedUserTooltip
     *
     * @param lockedUserTooltip the lockedUserTooltip
     */
    public void setLockedUserTooltip(String lockedUserTooltip) {
        this.lockedUserTooltip = lockedUserTooltip;
    }
}
