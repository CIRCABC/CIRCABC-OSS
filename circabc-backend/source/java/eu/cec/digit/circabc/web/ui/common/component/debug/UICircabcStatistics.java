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
package eu.cec.digit.circabc.web.ui.common.component.debug;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.ui.common.component.debug.BaseDebugComponent;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Component which displays the Circabc Statistics properties
 *
 * @author Yanick Pignot
 */
public class UICircabcStatistics extends BaseDebugComponent {

    /**
     * @see javax.faces.component.UIComponent#getFamily()
     */
    public String getFamily() {
        return "eu.cec.digit.circabc.faces.debug.CircabcStatistics";
    }

    /**
     * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
     */
    @SuppressWarnings("unchecked")
    public Map getDebugData() {
        final Map properties = new LinkedHashMap();

        final FacesContext fc = FacesContext.getCurrentInstance();
        final ManagementService managementService = Services.getCircabcServiceRegistry(fc)
                .getManagementService();

        final CircabcRootProfileManagerService circabcProfileManger = Services
                .getCircabcServiceRegistry(fc).getCircabcRootProfileManagerService();
        final CategoryProfileManagerService categoryProfileManger = Services
                .getCircabcServiceRegistry(fc).getCategoryProfileManagerService();
        final IGRootProfileManagerService igrootProfileManger = Services.getCircabcServiceRegistry(fc)
                .getIGRootProfileManagerService();

        final NodeService nodeService = Services.getAlfrescoServiceRegistry(fc).getNodeService();

        final NodeRef circabc = managementService.getCircabcNodeRef();
        final List<NodeRef> categoryHeaders = managementService.getExistingCategoryHeaders();
        final List<NodeRef> categories = managementService.getCategories();

        properties.put("Circabc - name", nodeService.getProperty(circabc, ContentModel.PROP_NAME));
        properties
                .put("Circabc - created", nodeService.getProperty(circabc, ContentModel.PROP_CREATED));
        properties.put("Circabc - admins", circabcProfileManger
                .getPersonInProfile(circabc, CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN));
        properties.put("Circabc - users", circabcProfileManger.getInvitedUsers(circabc));

        for (final NodeRef catHeader : categoryHeaders) {
            final Serializable name = nodeService.getProperty(catHeader, ContentModel.PROP_NAME);

            properties.put("Header (" + name + ") - nodeRef", catHeader);
            properties.put("Header (" + name + ") - created",
                    nodeService.getProperty(catHeader, ContentModel.PROP_CREATED));
        }

        for (final NodeRef category : categories) {
            final Serializable catName = nodeService.getProperty(category, ContentModel.PROP_NAME);

            properties.put("Category (" + catName + ") - nodeRef", category);
            properties.put("Category (" + catName + ") - created",
                    nodeService.getProperty(category, ContentModel.PROP_CREATED));
            properties.put("Category (" + catName + ") - admins", categoryProfileManger
                    .getPersonInProfile(category,
                            CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN));
            properties.put("Category (" + catName + ") - users",
                    categoryProfileManger.getInvitedUsers(category));

            final List<NodeRef> interestGroups = getInterestGroups(category, nodeService);

            for (final NodeRef igroot : interestGroups) {
                final Serializable igName = nodeService.getProperty(igroot, ContentModel.PROP_NAME);

                properties.put("IG (" + catName + "." + igName + ") - nodeRef", igroot);
                properties.put("IG (" + catName + "." + igName + ") - created",
                        nodeService.getProperty(igroot, ContentModel.PROP_CREATED));
                properties.put("IG (" + catName + "." + igName + ") - guest visibility",
                        managementService.hasGuestVisibility(igroot));
                properties.put("IG (" + catName + "." + igName + ") - registred visibility",
                        managementService.hasAllCircabcUsersVisibility(igroot));
                properties.put("IG (" + catName + "." + igName + ") - admins", igrootProfileManger
                        .getPersonInProfile(igroot, IGRootProfileManagerService.Profiles.IGLEADER));
                properties.put("IG (" + catName + "." + igName + ") - users",
                        igrootProfileManger.getInvitedUsers(igroot));
            }

        }

        return properties;
    }


    public List<NodeRef> getInterestGroups(NodeRef category, NodeService nodeService) {
        final List<ChildAssociationRef> assocs = nodeService.getChildAssocs(category);

        final List<NodeRef> interestGroupsNodes = new ArrayList<>(assocs.size());

        NodeRef ref = null;

        for (final ChildAssociationRef assoc : assocs) {
            ref = assoc.getChildRef();

            // Secure the list of ig. No other kind of spaces can be returned
            if (nodeService.hasAspect(ref, CircabcModel.ASPECT_IGROOT)) {
                interestGroupsNodes.add(ref);
            }
        }

        return interestGroupsNodes;
    }

}
