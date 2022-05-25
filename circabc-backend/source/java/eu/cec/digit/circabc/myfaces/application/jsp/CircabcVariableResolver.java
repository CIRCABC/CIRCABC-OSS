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
package eu.cec.digit.circabc.myfaces.application.jsp;

import org.alfresco.web.app.AlfrescoVariableResolver;
import org.alfresco.web.app.Application;

import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;
import java.util.List;

public class CircabcVariableResolver extends AlfrescoVariableResolver {

    public CircabcVariableResolver(VariableResolver originalVariableResolver) {
        super(originalVariableResolver);
        // TODO Auto-generated constructor stub
    }


    protected List<String> getDialogContainers(FacesContext context) {
        if ((this.dialogContainers == null) || (Application
                .isDynamicConfig(FacesContext.getCurrentInstance()))) {
            this.dialogContainers = super.getDialogContainers(context);
            this.dialogContainers.add("/jsp/extension/wai/dialog/container.jsp");
        }

        return this.dialogContainers;
    }

    protected List<String> getWizardContainers(FacesContext context) {
        if ((this.wizardContainers == null) || (Application
                .isDynamicConfig(FacesContext.getCurrentInstance()))) {
            this.wizardContainers = super.getWizardContainers(context);
            this.wizardContainers.add("/jsp/extension/wai/wizard/container.jsp");
        }

        return this.wizardContainers;
    }
}
