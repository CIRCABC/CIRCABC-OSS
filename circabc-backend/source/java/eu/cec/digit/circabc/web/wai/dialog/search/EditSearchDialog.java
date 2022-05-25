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
package eu.cec.digit.circabc.web.wai.dialog.search;

import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;

import javax.faces.context.FacesContext;
import java.util.Map;

public class EditSearchDialog extends org.alfresco.web.bean.search.EditSearchDialog implements
        WaiDialog {


    /**
     *
     */
    private static final long serialVersionUID = 3586954733856359034L;

    @Override
    public void init(Map<String, String> parameters) {
        // TODO Auto-generated method stub
        super.init(parameters);

        properties.setSearchDescription(null);
        properties.setSearchName(null);
        properties.setEditSearchName(null);

        // load previously selected search for overwrite
        try {
            NodeRef searchRef = new NodeRef(Repository.getStoreRef(), properties.getSavedSearch());
            Node searchNode = new Node(searchRef);
            if (getNodeService().exists(searchRef) && searchNode.hasPermission(PermissionService.WRITE)) {
                Node node = new Node(searchRef);
                properties.setSearchName(node.getName());
                properties.setEditSearchName(properties.getSearchName());
                properties.setSearchDescription(
                        (String) node.getProperties().get(ContentModel.PROP_DESCRIPTION.toString()));
            } else {
                // unable to overwrite existing saved search
                properties.setSavedSearch(null);
            }
        } catch (Throwable err) {
            // unable to overwrite existing saved search for some other reason
            properties.setSavedSearch(null);
        }
    }


    @Override
    public boolean getFinishButtonDisabled() {
        return false;
    }

    public ActionsListWrapper getActionList() {
        return null;
    }

    public String getBrowserTitle() {
        return Application
                .getMessage(FacesContext.getCurrentInstance(), "save_search_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return Application
                .getMessage(FacesContext.getCurrentInstance(), "save_search_dialog_icon_tooltip");
    }

    public boolean isCancelButtonVisible() {
        return true;
    }

    public boolean isFormProvided() {
        return false;
    }


}
	 
