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
package eu.cec.digit.circabc.web.wai.generator;

import eu.cec.digit.circabc.web.ui.repo.RepoConstants;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.generator.AssociationGenerator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


public class CircabcAssociationGenerator extends AssociationGenerator {

    @SuppressWarnings("unchecked")
    public UIComponent generate(FacesContext context, String id) {
        UIComponent component = context.getApplication().
                createComponent(RepoConstants.CIRCAB_FACES_ASSOC_EDITOR);
        FacesHelper.setupComponentId(context, component, id);

        // set the size of the list (if provided)
        if (this.optionsSize != null) {
            component.getAttributes().put("availableOptionsSize", this.optionsSize);
        }

        return component;
    }
}
