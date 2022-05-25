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
package eu.cec.digit.circabc.business.impl.nav;

import eu.cec.digit.circabc.business.api.nav.InterestGroupAccessMode;
import eu.cec.digit.circabc.business.api.nav.InterestGroupServices;
import eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv;
import eu.cec.digit.circabc.business.helper.NodeTypeManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.ValidationUtils;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Business service implementation that helps the navigation througt circabc spaces and contents.
 *
 * @author Yanick Pignot
 */
public class NavigationBusinessImpl implements NavigationBusinessSrv {

    private final Log logger = LogFactory.getLog(NavigationBusinessImpl.class);

    private ManagementService managementService;
    private NodeService nodeService;
    private TenantService tenantService;

    private NodeTypeManager nodeTypeManager;
    private ValidationManager validationManager;

    //--------------
    //-- public methods

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getNodePath(org.alfresco.service.cmr.repository.NodeRef)
     */
    public String getNodePathString(final NodeRef nodeRef) {
        ValidationUtils.assertNodeRef(nodeRef, validationManager, logger);

        try {
            return managementService.getNodePath(nodeRef).toString();
        } catch (final Throwable circabcError) {
            // don't worry, the node in not under circabc!
            if (logger.isDebugEnabled()) {
                logger.debug("The path of noderef (" + nodeRef
                                + ")is probably not managed by circabc and return error: " + circabcError.getMessage(),
                        circabcError);
            }

            try {
                return nodeService.getPath(nodeRef).toString();
            } catch (Throwable alfrescoError) {
                //don't worry, the node in not under circabc!
                if (logger.isErrorEnabled()) {
                    logger.error(
                            "Impossible to retreive the path of noderef (" + nodeRef + "): " + alfrescoError
                                    .getMessage(), alfrescoError);
                }

                return nodeRef.toString();
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getCircabcRoot()
     */
    public NodeRef getCircabcRoot() {
        return managementService.getCircabcNodeRef();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getHeaders()
     */
    public List<NodeRef> getHeaders() {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getCategories()
     */
    public List<NodeRef> getCategories() {
        final List<ChildAssociationRef> childs = nodeService
                .getChildAssocs(getCircabcRoot(), ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        final List<NodeRef> categories;

        if (childs == null || childs.size() == 0) {
            categories = Collections.emptyList();
        } else {
            categories = new ArrayList<>(childs.size());

            NodeRef childRef;
            for (final ChildAssociationRef child : childs) {
                childRef = child.getChildRef();

                if (nodeTypeManager.isCategory(childRef)) {
                    categories.add(childRef);
                }
            }
        }

        return categories;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getCategories(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> getCategories(final NodeRef categoryHeader) {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getCategory(java.lang.String)
     */
    public NodeRef getCategory(String categoryName) {
        final NodeRef childByName = nodeService
                .getChildByName(getCircabcRoot(), ContentModel.ASSOC_CONTAINS, categoryName);

        if (childByName == null || nodeTypeManager.isCategory(childByName) == false) {
            return null;
        } else {
            return childByName;
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getCategoriesByHeader()
     */
    public Map<NodeRef, List<NodeRef>> getCategoriesByHeader() {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getInterestGroups(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<NodeRef> getInterestGroups(final NodeRef category) {
        final List<NodeRef> igs;

        if (category == null || nodeTypeManager.isCategory(category) == false) {
            // not usefull to throw exception here.
            igs = Collections.emptyList();
        } else {
            final List<ChildAssociationRef> childs = nodeService
                    .getChildAssocs(category, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

            if (childs == null || childs.size() == 0) {
                igs = Collections.emptyList();
            } else {
                igs = new ArrayList<>(childs.size());

                NodeRef childRef;
                for (final ChildAssociationRef child : childs) {
                    childRef = child.getChildRef();

                    if (nodeTypeManager.isInterestGroup(childRef)) {
                        igs.add(childRef);
                    }
                }
            }
        }

        return igs;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getInterestGroupsByMode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<InterestGroupAccessMode, List<NodeRef>> getInterestGroupsByMode(
            final NodeRef category) {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.nav.NavigationBusinessSrv#getInterestGroupServices(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<InterestGroupServices, NodeRef> getInterestGroupServices(final NodeRef interestGroup) {
        throw new NotImplementedException();
    }

    public boolean isContainer(final NodeRef ref) {
        return getNodeTypeManager().isContainer(ref);
    }

    public boolean isContent(final NodeRef ref) {
        return getNodeTypeManager().isContent(ref);
    }

    //--------------
    //-- private helpers

    //--------------
    //-- IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the tenantService
     */
    protected final TenantService getTenantService() {
        return tenantService;
    }

    /**
     * @param tenantService the tenantService to set
     */
    public final void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }


    /**
     * @return the nodeTypeManager
     */
    protected final NodeTypeManager getNodeTypeManager() {
        return nodeTypeManager;
    }

    /**
     * @param nodeTypeManager the nodeTypeManager to set
     */
    public final void setNodeTypeManager(NodeTypeManager nodeTypeManager) {
        this.nodeTypeManager = nodeTypeManager;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param validationManager the validationManager to set
     */
    public final void setValidationManager(ValidationManager validationManager) {
        this.validationManager = validationManager;
    }
}
