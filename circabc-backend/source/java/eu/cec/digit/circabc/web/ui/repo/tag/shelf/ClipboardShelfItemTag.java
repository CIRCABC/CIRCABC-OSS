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
package eu.cec.digit.circabc.web.ui.repo.tag.shelf;

import eu.cec.digit.circabc.web.ui.repo.component.shelf.UIClipboardShelfItem;
import org.springframework.extensions.webscripts.ui.common.tag.BaseComponentTag;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;

/**
 * @author Guillaume
 * @author Stephane Clinckart
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 BaseComponentTag was moved to Spring Surf. Class
 * implementation didn't change between Alfresco versions.
 */
public class ClipboardShelfItemTag extends BaseComponentTag {

    /**
     * the pasteActionListener
     */
    private String pasteActionListener;
    /**
     * the downloadAllActionListener
     */
    private String downloadAllActionListener;
    /**
     * the style class
     */
    private String styleClass;
    /**
     * the clipboard collections reference
     */
    private String collections;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.ClipboardShelfItem";
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
    protected void setProperties(final UIComponent component) {
        super.setProperties(component);

        setStringBindingProperty(component, "collections", this.collections);
        setStringProperty(component, "styleClass", this.styleClass);
        if (isValueReference(this.pasteActionListener)) {
            final MethodBinding vb = getFacesContext().getApplication()
                    .createMethodBinding(this.pasteActionListener, ACTION_CLASS_ARGS);
            ((UIClipboardShelfItem) component).setPasteActionListener(vb);
        } else {
            throw new FacesException("paste Action listener method binding incorrectly specified: "
                    + this.pasteActionListener);
        }

        if (isValueReference(this.downloadAllActionListener)) {
            final MethodBinding vb = getFacesContext().getApplication()
                    .createMethodBinding(this.downloadAllActionListener, ACTION_CLASS_ARGS);
            ((UIClipboardShelfItem) component).setDownloadAllActionListener(vb);
        } else {
            throw new FacesException("DownloadAll Action listener method binding incorrectly specified: "
                    + this.downloadAllActionListener);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();

        this.collections = null;
        this.styleClass = null;
        this.pasteActionListener = null;
    }

    /**
     * Set the clipboard collections to show
     *
     * @param collections the clipboard collections to show
     */
    public void setCollections(final String collections) {
        this.collections = collections;
    }

    /**
     * The CSS class to use
     *
     * @param styleClass The CSS class to use
     */
    public void setStyleClass(final String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * Set the pasteActionListener
     *
     * @param pasteActionListener the pasteActionListener
     */
    public void setPasteActionListener(final String pasteActionListener) {
        this.pasteActionListener = pasteActionListener;
    }

    /**
     * Set the pasteActionListener
     *
     * @param pasteActionListener the pasteActionListener
     */
    public void setDownloadAllActionListener(final String downloadAllActionListener) {
        this.downloadAllActionListener = downloadAllActionListener;
    }
}
