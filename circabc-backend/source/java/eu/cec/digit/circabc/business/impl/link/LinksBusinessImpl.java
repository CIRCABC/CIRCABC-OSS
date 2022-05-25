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
package eu.cec.digit.circabc.business.impl.link;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.business.api.link.InterestGroupItem;
import eu.cec.digit.circabc.business.api.link.LinksBusinessSrv;
import eu.cec.digit.circabc.business.api.link.ShareSpaceItem;
import eu.cec.digit.circabc.business.api.space.ContainerIcon;
import eu.cec.digit.circabc.business.helper.ApplicationConfigManager;
import eu.cec.digit.circabc.business.helper.MetadataManager;
import eu.cec.digit.circabc.business.helper.NodeTypeManager;
import eu.cec.digit.circabc.business.helper.ValidationManager;
import eu.cec.digit.circabc.business.impl.AssertUtils;
import eu.cec.digit.circabc.business.impl.ValidationUtils;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.SharedSpaceModel;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.sharespace.ShareSpaceService;
import eu.cec.digit.circabc.util.PathUtils;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Business service implementation to manage file links, folder links and shared spaces.
 *
 * @author Yanick Pignot
 */
public class LinksBusinessImpl implements LinksBusinessSrv {

    //	 File prefix to use for link nodes
    private static final String LINK_TO_PREFIX = "Link to ";
    //	 File extension to use for link nodes
    private static final String LINK_NODE_EXTENSION = ".url";

    private final Log logger = LogFactory.getLog(LinksBusinessImpl.class);

    private FileFolderService fileFolderService;
    private NodeService nodeService;
    private ShareSpaceService shareSpaceService;

    private ApplicationConfigManager configManager;
    private ValidationManager validationManager;
    private MetadataManager metadataManager;
    private NodeTypeManager nodeTypeManager;

    //--------------
    //-- public methods


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#createLink(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public NodeRef createLink(final NodeRef parent, final NodeRef target) {
        assertLinkCreation(parent, target, null, false);

        final String name = getLinkName(parent, target);
        final String title =
                LINK_TO_PREFIX + getNodeService().getProperty(target, ContentModel.PROP_TITLE);
        final ChildAssociationRef assocRef = getNodeService().getPrimaryParent(target);

        final Map<QName, Serializable> nodeProps = new HashMap<>(2);
        nodeProps.put(ContentModel.PROP_NAME, name);
        nodeProps.put(ContentModel.PROP_LINK_DESTINATION, (Serializable) target);

        final ChildAssociationRef childRef;

        if (getNodeTypeManager().isContent(target)) {
            childRef = getNodeService().createNode(
                    target,
                    ContentModel.ASSOC_CONTAINS,
                    assocRef.getQName(),
                    ApplicationModel.TYPE_FILELINK,
                    nodeProps);

            applyUIFacet(childRef.getChildRef(), title, title, getSpaceLinkIcons().get(0).getIconName());

            if (logger.isDebugEnabled()) {
                logger.debug("Link to content created with name: " + name + " and title" + title);
            }
        } else {
            // create Folder link node
            childRef = getNodeService().createNode(
                    target,
                    ContentModel.ASSOC_CONTAINS,
                    assocRef.getQName(),
                    ApplicationModel.TYPE_FOLDERLINK,
                    nodeProps);

            applyUIFacet(childRef.getChildRef(), title, title, null);

            if (logger.isDebugEnabled()) {
                logger.debug("Link to space created with name: " + name + " and title" + title);
            }
        }

        return childRef.getChildRef();
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#createSharedSpaceLink(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.lang.String)
     */
    public NodeRef createSharedSpaceLink(final NodeRef parent, final NodeRef targetSharedSpace,
                                         final String title, final String description) {
        assertLinkCreation(parent, targetSharedSpace, title, true);

        final String name = getLinkName(parent, targetSharedSpace);

        final NodeRef link = getShareSpaceService().linkSharedSpace(parent, targetSharedSpace, name);

        applyUIFacet(link, title, name, getSpaceLinkIcons().get(0).getIconName());

        if (logger.isDebugEnabled()) {
            logger.debug("Link to share space created with name: " + name + " and title" + title);
        }

        return link;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#applySharing(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void applySharing(final NodeRef sharedSpace, final NodeRef interestGroup,
                             final LibraryPermissions libraryPermission) {
        AssertUtils.notNull(libraryPermission);

        assertInvitation(sharedSpace, interestGroup);

        getShareSpaceService()
                .inviteInterestGroup(sharedSpace, interestGroup, libraryPermission.toString());
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#removeSharing(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void removeSharing(final NodeRef sharedSpace, final NodeRef interestGroup) {
        assertInvitation(sharedSpace, interestGroup);

        getShareSpaceService().unInviteInterestGroup(sharedSpace, interestGroup);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#getAvailableSharedSpaces(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ShareSpaceItem> getAvailableSharedSpaces(final NodeRef nodeRef) {
        ValidationUtils.assertNodeRef(nodeRef, getValidationManager(), logger);

        final List<NodeRef> allSharedSpaces = getShareSpaceService().getAvailableShareSpaces(nodeRef);

        if (allSharedSpaces == null || allSharedSpaces.size() < 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("No shared space available for space: " + nodeRef);
            }

            return Collections.emptyList();
        } else {
            return buildSharedSpaceItems(allSharedSpaces);
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#findSharedSpaces(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ShareSpaceItem> findSharedSpaces(final NodeRef nodeRef) {
        ValidationUtils.assertNodeRef(nodeRef, getValidationManager(), logger);

        final List<NodeRef> allSharedSpaces = getShareSpaceService()
                .getAllSharedSpaceInInterestGroup(nodeRef);

        if (allSharedSpaces == null || allSharedSpaces.size() < 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("No shared space available under the node: " + nodeRef);
            }

            return Collections.emptyList();
        } else {
            return buildSharedSpaceItems(allSharedSpaces);
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#findInterestGroupForSharing(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<InterestGroupItem> getInterestGroupForSharing(final NodeRef nodeRef) {
        ValidationUtils.assertLibrarySpace(nodeRef, getValidationManager(), logger);

        // TODO if we use not proxied service in business layer, remove this method of the Shared space service. It is bussiness logic.
        final List<NodeRef> interestGroups = getShareSpaceService().getAvailableInterestGroups(nodeRef);

        if (interestGroups == null || interestGroups.size() < 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("All interest group of the category have been invited: " + nodeRef);
            }

            return Collections.emptyList();
        } else {
            final List<InterestGroupItem> items = new ArrayList<>(interestGroups.size());
            for (NodeRef ref : interestGroups) {
                items.add(buildInterestGroupItem(ref, null));
            }

            return items;
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#getInvitationsForSharing(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<InterestGroupItem> getInvitationsForSharing(final NodeRef nodeRef) {
        ValidationUtils.assertLibrarySpace(nodeRef, getValidationManager(), logger);

        final List<Pair<NodeRef, String>> interestGroups = getShareSpaceService()
                .getInvitedInterestGroups(nodeRef);

        if (interestGroups == null || interestGroups.size() < 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("No invitation for space sharing found for: " + nodeRef);
            }

            return Collections.emptyList();
        } else {
            final List<InterestGroupItem> items = new ArrayList<>(interestGroups.size());
            for (Pair<NodeRef, String> pair : interestGroups) {
                items.add(buildInterestGroupItem(pair.getFirst(), pair.getSecond()));
            }

            return items;
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.link.LinksBusinessSrv#getSpaceLinkIcons()
     */
    public List<ContainerIcon> getSpaceLinkIcons() {
        final List<ContainerIcon> icons = getConfigManager().getIcons(ApplicationModel.TYPE_FOLDERLINK);
        AssertUtils.notEmpty(icons);

        return icons;
    }

    //--------------
    //-- private helpers

    private List<ShareSpaceItem> buildSharedSpaceItems(final List<NodeRef> allSharedSpaces) {
        final List<ShareSpaceItem> wrappers = new ArrayList<>(allSharedSpaces.size());

        Path path;
        for (final NodeRef ref : allSharedSpaces) {
            if (checkIfOneInvitedInterestGroupExists(ref)) {
                path = getNodeService().getPath(ref);
                wrappers.add(new ShareSpaceItem(
                        ref,
                        PathUtils.getCategoryPath(path, true)
                ));
            }
        }

        return wrappers;
    }

    private boolean checkIfOneInvitedInterestGroupExists(NodeRef shareSpace) {
        boolean result = false;
        if (getNodeService().hasAspect(shareSpace, CircabcModel.ASPECT_SHARED_SPACE)) {
            List<ChildAssociationRef> childAssocs = getNodeService()
                    .getChildAssocs(shareSpace, SharedSpaceModel.ASSOC_SHARE_SPACE_CONTAINER,
                            RegexQNamePattern.MATCH_ALL);
            final ChildAssociationRef assocRef;
            if (childAssocs.size() > 0) {
                assocRef = childAssocs.get(0);
                NodeRef container = assocRef.getChildRef();
                List<ChildAssociationRef> igChildAssocs = getNodeService()
                        .getChildAssocs(container, SharedSpaceModel.ASSOC_ITEREST_GROUP,
                                RegexQNamePattern.MATCH_ALL);
                for (ChildAssociationRef ref : igChildAssocs) {
                    NodeRef childRef = ref.getChildRef();
                    NodeRef igNodeRef = (NodeRef) getNodeService()
                            .getProperty(childRef, SharedSpaceModel.PROP_INTEREST_GROUP_NODE_REF);
                    if ((igNodeRef != null) && getNodeService().exists(igNodeRef)) {
                        result = true;
                        break;
                    }
                }
            }


        }
        return result;
    }

    private void applyUIFacet(final NodeRef ref, final String title, final String desc,
                              final String icon) {
        final Map<QName, Serializable> titledProps = new HashMap<>(3, 1.0f);
        titledProps.put(ContentModel.PROP_TITLE, title);
        titledProps.put(ContentModel.PROP_DESCRIPTION, desc);

        if (icon != null) {
            titledProps.put(ApplicationModel.PROP_ICON, icon);
        }

        getNodeService().addAspect(ref, ApplicationModel.ASPECT_UIFACETS, titledProps);
    }

    private void assertLinkCreation(final NodeRef parent, final NodeRef target, final String title,
                                    final boolean isSharedSpace) {
        final BusinessStackError stack = new BusinessStackError();

        getValidationManager().validateNodeRef(parent, stack);
        getValidationManager().validateCanAddChild(parent, stack);

        if (isSharedSpace) {
            getValidationManager().validateSharedSpace(target, stack);
            getValidationManager().validateTitle(title, stack);
        } else {
            getValidationManager().validateSpace(parent, stack);
            getValidationManager().validateNodeRef(target, stack);
        }

        stack.finish(logger);
    }

    private void assertInvitation(final NodeRef sharedSpace, final NodeRef interestGroup) {
        BusinessStackError stack;

        try {
            ValidationUtils.assertLibrarySpace(sharedSpace, getValidationManager(), logger);
            stack = new BusinessStackError();
        } catch (final BusinessStackError error) {
            stack = error;
        }

        getValidationManager().validateInterstGroup(interestGroup, stack);

        // if the service is well used, we don't have to test
        //		if the IG is already invited or not
        //		if the IG is not equals to the shared space IG
        //		if the IG is in the same IG that the shared space

        stack.finish(logger);
    }

    private InterestGroupItem buildInterestGroupItem(final NodeRef interestGroup,
                                                     final String permission) {
        final Map<QName, Serializable> props = getNodeService().getProperties(interestGroup);

        return new InterestGroupItem(
                interestGroup,
                (String) props.get(ContentModel.PROP_NAME),
                permission,
                getMetadataManager().computeTitle(props)
        );
    }

    private String getLinkName(final NodeRef parent, final NodeRef target) {
        final String targetName = (String) getNodeService().getProperty(target, ContentModel.PROP_NAME);

        return getMetadataManager()
                .getValidUniqueName(parent, LINK_TO_PREFIX + targetName + LINK_NODE_EXTENSION);
    }

    //--------------
    //-- IOC


    /**
     * @return the fileFolderService
     */
    protected final FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public final void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @return the shareSpaceService
     */
    protected final ShareSpaceService getShareSpaceService() {
        return shareSpaceService;
    }


    /**
     * @param shareSpaceService the shareSpaceService to set
     */
    public final void setShareSpaceService(ShareSpaceService shareSpaceService) {
        this.shareSpaceService = shareSpaceService;
    }

    /**
     * @return the configManager
     */
    public final ApplicationConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * @param configManager the configManager to set
     */
    public final void setConfigManager(ApplicationConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * @return the validationManager
     */
    protected final ValidationManager getValidationManager() {
        return validationManager;
    }

    /**
     * @param validationManager the validationManager to set
     */
    public final void setValidationManager(ValidationManager validationManager) {
        this.validationManager = validationManager;
    }

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
     * @return the metadataManager
     */
    protected final MetadataManager getMetadataManager() {
        return metadataManager;
    }

    /**
     * @param metadataManager the metadataManager to set
     */
    public final void setMetadataManager(MetadataManager metadataManager) {
        this.metadataManager = metadataManager;
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

}
