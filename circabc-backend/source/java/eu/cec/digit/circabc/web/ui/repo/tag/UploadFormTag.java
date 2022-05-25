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
 * This is to handle the upload form required by the uploadServlet
 *
 * @author Guillaume
 */
public class UploadFormTag extends HtmlComponentTag {

    /**
     * The return page url
     */
    private String returnPage = null;
    /**
     * What to do after comit (require callback with parameter of type 'FileBean)
     */
    private String submitCallback = null;

    /**
     * @see javax.faces.webapp.UIComponentTag#getComponentType()
     */
    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.UploadForm";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#getRendererType()
     */
    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.UploadFormRenderer";

    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(final UIComponent component) {
        super.setProperties(component);

        setStringProperty(component, "returnPage", this.returnPage);
        setStringProperty(component, "submitCallback", this.submitCallback);
    }

    /**
     * @see org.alfresco.web.ui.common.tag.HtmlComponentTag#release()
     */
    public void release() {
        super.release();

        this.returnPage = null;
    }

    /**
     * Set the return page url
     *
     * @param returnPage the returnPage
     */
    public void setReturnPage(final String returnPage) {
        this.returnPage = returnPage;
    }

    /**
     * @return the submitCallback
     */
    public final String getSubmitCallback() {
        return submitCallback;
    }

    /**
     * @param submitCallback the submitCallback to set
     */
    public final void setSubmitCallback(String submitCallback) {
        this.submitCallback = submitCallback;
    }

}
