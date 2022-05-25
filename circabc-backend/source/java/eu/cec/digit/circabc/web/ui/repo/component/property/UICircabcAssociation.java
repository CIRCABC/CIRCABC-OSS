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
package eu.cec.digit.circabc.web.ui.repo.component.property;

import eu.cec.digit.circabc.web.ui.repo.RepoConstants;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.DataDictionary;
import org.alfresco.web.ui.repo.component.property.UIAssociation;
import org.alfresco.web.ui.repo.component.property.UIAssociationEditor;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.jsf.FacesContextUtils;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import java.io.IOException;

public class UICircabcAssociation extends UIAssociation {

    private static Log logger = LogFactory.getLog(UICircabcAssociation.class);
    private String interestGroupNodeRef;

    public UICircabcAssociation() {
        // set the default renderer
        setRendererType("eu.cec.digit.circabc.faces.CircabcAssociationRenderer");
    }

    @SuppressWarnings("unchecked")
    public void restoreState(FacesContext context, Object state) {
        Object values[] = (Object[]) state;

        super.restoreState(context, values[0]);
        this.interestGroupNodeRef = (String) values[1];
    }

    public Object saveState(FacesContext context) {
        Object values[] = new Object[2];
        // standard component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.interestGroupNodeRef;

        return (values);
    }

    public String getFamily() {
        return "eu.cec.digit.circabc.faces.CircabcAssociation";
    }

    protected void generateItem(FacesContext context, UIPropertySheet propSheet)
            throws IOException {
        String associationName = (String) getName();

        // get details of the association
        DataDictionary dd = (DataDictionary) FacesContextUtils
                .getRequiredWebApplicationContext(context).getBean(
                        Application.BEAN_DATA_DICTIONARY);
        AssociationDefinition assocDef = dd.getAssociationDefinition(
                propSheet.getNode(), associationName);

        if (assocDef == null) {
            logger.warn("Failed to find association definition for association '"
                    + associationName + "'");
        } else {
            // we've found the association definition but we also need to check
            // that the association is not a parent child one
            if (assocDef.isChild()) {
                logger.warn("The association named '" + associationName
                        + "' is not an association");
            } else {
                String displayLabel = (String) getDisplayLabel();
                if (displayLabel == null) {
                    // try and get the repository assigned label
                    displayLabel = assocDef.getTitle();

                    // if the label is still null default to the local name of
                    // the property
                    if (displayLabel == null) {
                        displayLabel = assocDef.getName().getLocalName();
                    }
                }

                // generate the label and type specific control
                generateLabel(context, propSheet, displayLabel);
                generateControl(context, propSheet, assocDef);
            }
        }
    }

    public String getInterestGroupNodeRef() {
        ValueBinding vb = getValueBinding("interestGroupNodeRef");
        if (vb != null) {
            this.interestGroupNodeRef = (String) vb.getValue(getFacesContext());
        }

        return this.interestGroupNodeRef;
    }

    public void setInterestGroupNodeRef(String value) {
        this.interestGroupNodeRef = value;
    }

    private void generateControl(FacesContext context,
                                 UIPropertySheet propSheet, AssociationDefinition assocDef) {
        // get the custom component generator (if one)
        String componentGeneratorName = this.getComponentGenerator();

        // use the default component generator if there wasn't an overridden one
        if (componentGeneratorName == null) {
            componentGeneratorName = RepoConstants.CIRCAB_GENERATOR_ASSOCIATION;
        }

        UIAssociationEditor control = (UIAssociationEditor) FacesHelper
                .getComponentGenerator(context, componentGeneratorName)
                .generateAndAdd(context, propSheet, this);
        String igNodeRef = getInterestGroupNodeRef();
        if (control instanceof UICircabcAssociationEditor && igNodeRef != null) {
            ((UICircabcAssociationEditor) control).setIgNodeRef(new NodeRef(
                    igNodeRef));

        }

        if (logger.isDebugEnabled()) {
            logger.debug("Created control " + control + "("
                    + control.getClientId(context) + ") for '"
                    + assocDef.getName().toString()
                    + "' and added it to component " + this);
        }
    }

}
