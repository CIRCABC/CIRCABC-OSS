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
package eu.cec.digit.circabc.web.wai.dialog.users;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.wai.dialog.WaiDialog;
import eu.cec.digit.circabc.web.wai.manager.ActionsListWrapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.search.AdvancedSearchDialog;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewUserSavedSearchDialog extends AdvancedSearchDialog implements WaiDialog {


    private static final long serialVersionUID = -939753458567279359L;


    private static final String SAVED_SEARCHES_USER = "user";
    private static final String SAVED_SEARCHES_GLOBAL = "global";


    private String savedSearchMode;


    private NodeRef actionNodeRef;


    private ManagementService managementService;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        String id = parameters.get("id");
        if (id != null) {
            actionNodeRef = new NodeRef(Repository.getStoreRef(), id);
        } else {
            actionNodeRef = null;
        }
        savedSearchMode = parameters.get("mode");
    }

    @Override
    public String getPageIconAltText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrowserTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCancelButtonLabel() {
        return WebClientHelper.translate("close");
    }

    public void setSavedSearches() {

    }

    public List<SelectItem> getSavedSearches() {

        List<SelectItem> savedSearches = new ArrayList<>();

        properties.setSavedSearchMode(savedSearchMode);
        FacesContext fc = FacesContext.getCurrentInstance();
        ServiceRegistry services = Repository.getServiceRegistry(fc);

        // get the searches list from the current user or global searches location
        NodeRef searchesRef = null;

        if (SAVED_SEARCHES_USER.equals(properties.getSavedSearchMode()) == true) {
            searchesRef = getUserSearchesRef();
        } else if (SAVED_SEARCHES_GLOBAL.equals(properties.getSavedSearchMode()) == true) {
            searchesRef = getGlobalSearchesRef();
        }

        // read the content nodes under the folder
        if (searchesRef != null) {
            DictionaryService dd = services.getDictionaryService();

            List<ChildAssociationRef> childRefs = getNodeService().getChildAssocs(
                    searchesRef,
                    ContentModel.ASSOC_CONTAINS,
                    RegexQNamePattern.MATCH_ALL);

            savedSearches = new ArrayList<>(childRefs.size() + 1);
            if (childRefs.size() != 0) {
                String currentIG = null;

                if (actionNodeRef != null) {
                    final NodeRef igNodeRef = getManagementService().getCurrentInterestGroup(actionNodeRef);
                    if (igNodeRef != null) {
                        currentIG = igNodeRef.toString();
                    }
                }
                for (ChildAssociationRef ref : childRefs) {
                    Node childNode = new Node(ref.getChildRef());
                    if (dd.isSubClass(childNode.getType(), ContentModel.TYPE_CONTENT)) {
                        Map<String, Object> props = childNode.getProperties();
                        String locationSearch = (String) getNodeService()
                                .getProperty(childNode.getNodeRef(), CircabcModel.PROP_LOCATION);
                        if (currentIG == null || currentIG.equals(locationSearch)) {
                            String desc = (String) props.get(ContentModel.PROP_DESCRIPTION.toString());
                            savedSearches.add(new SelectItem(childNode.getId(), childNode.getName(), desc));
                        }
                    }
                }

            }
        }

        return savedSearches;
    }


    @Override
    public ActionsListWrapper getActionList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCancelButtonVisible() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isFormProvided() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void restored() {
        properties.getCachedSavedSearches().put(null);
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }


}
