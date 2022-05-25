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
package eu.cec.digit.circabc.web.ui.repo.tag.property;

import org.alfresco.web.ui.repo.tag.property.AssociationTag;

import javax.faces.component.UIComponent;

public class CircabcAssociationTag extends AssociationTag {

    private String interestGroupNodeRef;

    public String getRendererType() {
        return "eu.cec.digit.circabc.faces.CircabcAssociationRenderer";
    }

    public String getComponentType() {
        return "eu.cec.digit.circabc.faces.CircabcAssociation";
    }

    /**
     * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
     */
    protected void setProperties(UIComponent component) {
        super.setProperties(component);
        setStringBindingProperty(component, "interestGroupNodeRef", this.interestGroupNodeRef);

    }


    /**
     * @see javax.faces.webapp.UIComponentTag#release()
     */
    public void release() {
        this.interestGroupNodeRef = null;
        super.release();
    }

    public void setInterestGroupNodeRef(String value) {
        this.interestGroupNodeRef = value;
    }
}
