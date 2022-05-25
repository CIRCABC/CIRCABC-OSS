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
package eu.cec.digit.circabc.action;

import org.alfresco.repo.action.executer.ImporterActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.handlers.BaseActionHandler;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;


public class CircabcImportHandler extends BaseActionHandler {

    protected static final String IMPORT_ENCODING = "UTF-8";
    private static final long serialVersionUID = -3011384175544764038L;

    public String getJSPPath() {
        return getJSPPath(ImporterActionExecuter.NAME);
    }

    public void prepareForSave(Map<String, Serializable> actionProps,
                               Map<String, Serializable> repoProps) {
        // add the encoding
        repoProps.put(ImporterActionExecuter.PARAM_ENCODING, IMPORT_ENCODING);

        // add the destination for the import
        NodeRef destNodeRef = (NodeRef) actionProps.get(PROP_DESTINATION);
        repoProps.put(ImporterActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
    }

    public void prepareForEdit(Map<String, Serializable> actionProps,
                               Map<String, Serializable> repoProps) {
        NodeRef destNodeRef = (NodeRef) repoProps.get(
                ImporterActionExecuter.PARAM_DESTINATION_FOLDER);
        actionProps.put(PROP_DESTINATION, destNodeRef);
    }

    public String generateSummary(FacesContext context, IWizardBean wizard,
                                  Map<String, Serializable> actionProps) {
        NodeRef space = (NodeRef) actionProps.get(PROP_DESTINATION);
        String spaceName = Repository.getNameForNode(
                Repository.getServiceRegistry(context).getNodeService(), space);

        return MessageFormat.format(Application.getMessage(context, "action_import"),
                spaceName);
    }
}
