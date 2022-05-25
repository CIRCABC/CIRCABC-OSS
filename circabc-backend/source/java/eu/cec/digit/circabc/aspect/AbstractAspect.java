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
package eu.cec.digit.circabc.aspect;

import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * @author Clinckart Stephane
 */
public abstract class AbstractAspect {

    private static final Log logger = LogFactory.getLog(AbstractAspect.class);
    /**
     * AuthorityService
     */
    protected AuthorityService authorityService;

    // IOC parameters
    /**
     * NodeService
     */
    protected NodeService nodeService;
    /**
     * PermissionService
     */
    protected PermissionService permissionService;
    /**
     * The policy component
     */
    protected PolicyComponent policyComponent;
    /**
     * ProfileManagerServiceFactory
     */
    private ProfileManagerServiceFactory profileManagerServiceFactory;

    /**
     * Spring initialise method used to register the policy behaviours
     */
    abstract public void initialise();

    /**
     * Called when the Aspect is added.
     *
     * @param nodeRef         the node reference
     * @param aspectTypeQName qualified name of the aspect
     */

    abstract public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName);

    /**
     * @return the comparator that should be used to determine the nodeType
     */
    abstract public ComparatorType getComparator();

    /**
     * @return the the qname that must be applied on any node to match this aspect imlementation
     */
    abstract public QName getComparatorQName();

    /**
     * Return true if the current implementation aspect is setted at the given node
     */
    public final boolean isNodeFromType(final Node node) {
        final ComparatorType type = getComparator();
        final QName qname = getComparatorQName();

        if (type.equals(ComparatorType.ASPECT)) {
            return node.hasAspect(qname);
        } else if (type.equals(ComparatorType.TYPE)) {
            return node.getType().isMatch(qname);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Return true if the current implementation aspect is setted at the given nodeRef
     */
    public final boolean isNodeFromType(final NodeRef nodeRef) {
        final ComparatorType type = getComparator();
        final QName qname = getComparatorQName();

        if (type.equals(ComparatorType.ASPECT)) {
            return nodeService.hasAspect(nodeRef, qname);
        } else if (type.equals(ComparatorType.TYPE)) {
            return nodeService.getType(nodeRef).isMatch(qname);
        } else if (type.equals(ComparatorType.NOT_ASPECT)) {
            return !nodeService.hasAspect(nodeRef, qname);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Called before a node is deleted. the job of this method is to remove all hidden groups
     * associated to the node
     *
     * @param nodeRef the node reference
     */
    public void beforeDeleteNode(final NodeRef nodeRef) {
        final ProfileManagerService profileManagerService = profileManagerServiceFactory
                .getProfileManagerService(nodeRef);

        final CircabcRootProfileManagerService circabcRootProfileManagerService = profileManagerServiceFactory
                .getCircabcRootProfileManagerService();

        if (profileManagerService != null) {
            // first get the list of associated groups
            final Map<String, Profile> profiles = profileManagerService.getProfileMap(nodeRef);

            String profileGroupName;
            for (final Profile profile : profiles.values()) {
                if (!profile.isImported()) {
                    // get associated group name
                    profileGroupName = profile.getPrefixedAlfrescoGroupName();

                    try {
                        if ((circabcRootProfileManagerService.getPrefixedAllCircaUsersGroupName() != null &&
                                circabcRootProfileManagerService.getPrefixedAllCircaUsersGroupName()
                                        .equals(profileGroupName)) ||
                                CircabcConstant.GUEST_AUTHORITY.equals(profileGroupName)) {
                            if (logger.isTraceEnabled()) {
                                logger.trace("Don't Delete Authority:" + profileGroupName);
                            }
                        } else {
                            if (authorityService.authorityExists(profileGroupName)) {
                                if (logger.isWarnEnabled()) {
                                    logger.warn("Delete Authority:" + profileGroupName);
                                }
                                authorityService.deleteAuthority(profileGroupName);
                            } else {
                                if (logger.isTraceEnabled()) {
                                    logger.trace("Tried to delete a non existing Authority (allready deleted):"
                                            + profileGroupName);
                                }
                            }
                        }
                    } catch (final Exception e) {
                        if (logger.isWarnEnabled()) {
                            // should not happen
                            logger.warn("Authority should always exist", e);
                        }
                    }
                } else {
                    if (logger.isWarnEnabled()) {
                        logger
                                .warn("Don't delete the Authority linked to a profile Imported from another node");
                    }
                }
            }

            // get group name of the group containing all the users
            // group name is not prefixed by GROUP_
            String authorityName = authorityService.getName(
                    AuthorityType.GROUP, profileManagerService
                            .getMasterInvitedGroupName(nodeRef));

            if (authorityService.authorityExists(authorityName)) {
                authorityService.deleteAuthority(authorityName, true);
            }

        } else {
            //This should append only if whe are on a node that has an optionnal permission defined (like the library node)
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "There is no redefinition of herited permission defined on this node:" + nodeRef);
            }
        }

    }

    /**
     * Setter method for authorityService
     *
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * IOC injections
     */

    /**
     * Getter method for ProfileManagerServiceFactory
     */
    public final ProfileManagerServiceFactory getProfileManagerServiceFactory() {
        return this.profileManagerServiceFactory;
    }

    /**
     * Setter method for ProfileManagerServiceFactory
     */
    public final void setProfileManagerServiceFactory(
            ProfileManagerServiceFactory profileManagerServiceFactory) {
        this.profileManagerServiceFactory = profileManagerServiceFactory;
    }

    /**
     * @param policyComponent
     */
    public final void setPolicyComponent(PolicyComponent policyComponent) {
        this.policyComponent = policyComponent;
    }

    /**
     * @param nodeService
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Setter method for permissionService
     *
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Determine the diffent way to compare a node
     */
    public enum ComparatorType {
        ASPECT,
        NOT_ASPECT,
        TYPE
    }
}
