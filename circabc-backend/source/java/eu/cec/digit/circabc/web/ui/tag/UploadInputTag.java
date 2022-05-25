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

import javax.faces.component.UIComponent;

/**
 * @author Yanick Pignot
 */
public class UploadInputTag extends org.alfresco.web.ui.common.tag.UploadInputTag {

    public static final String COMPONENT_TYPE = "eu.cec.digit.circabc.faces.UploadInput";

    private String onSubmit;

    @Override
    public String getComponentType() {
        return UploadInputTag.COMPONENT_TYPE;
    }

    @Override
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        component.getAttributes().put("onSubmit", this.onSubmit);
    }

    @Override
    public void release() {
        super.release();
        this.onSubmit = null;
    }

    public void setOnSubmit(String onSubmit) {
        this.onSubmit = onSubmit;
    }

}
