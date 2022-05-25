package eu.cec.digit.circabc.repo.security;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.module.circabc.CircabcComponent;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * Implementation of the permission service that intercepts the permission checks. It is used to
 * check if a root is locked for access. Fast useful way when exporting Categories and IGs, but
 * performance killer
 *
 * @author schwerr
 */
public class CircabcPermissionServiceImpl extends PermissionServiceImpl {

    private AuthenticationService authenticationService = null;
    private NodeService nodeService2 = null;
    private CircabcServiceRegistry circabcServiceRegistry = null;

    private NodeRef circabcRoot = null;

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#hasPermission(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.repo.security.permissions.PermissionReference)
     */
    @Override
    public AccessStatus hasPermission(NodeRef passedNodeRef, PermissionReference permIn) {
        if (lockedForAccess(passedNodeRef)) {
            return AccessStatus.DENIED;
        }
        return super.hasPermission(passedNodeRef, permIn);
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#getInheritParentPermissions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean getInheritParentPermissions(NodeRef nodeRef) {
        if (lockedForAccess(nodeRef)) {
            return false;
        }
        return super.getInheritParentPermissions(nodeRef);
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#hasPermission(org.alfresco.service.cmr.repository.NodeRef,
     * java.lang.String)
     */
    @Override
    public AccessStatus hasPermission(NodeRef nodeRef, String perm) {
        if (lockedForAccess(nodeRef)) {
            return AccessStatus.DENIED;
        }
        return super.hasPermission(nodeRef, perm);
    }

    /**
     * @see org.alfresco.repo.security.permissions.impl.PermissionServiceImpl#hasReadPermission(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public AccessStatus hasReadPermission(NodeRef nodeRef) {
        if (lockedForAccess(nodeRef)) {
            return AccessStatus.DENIED;
        }
        return super.hasReadPermission(nodeRef);
    }

    /**
     * Check if the current node's ultimate parent is an IG and has the locked for access aspect
     * applied on its root node.
     */
    private boolean lockedForAccess(final NodeRef nodeRef) {

        // Feature can be enabled/disabled by configuration as well
        if (!Boolean.parseBoolean(
                CircabcConfiguration.getProperty(
                        "migration.permission.service.locked.for.access.enabled"))) {
            return false;
        }

        // "admin" and "system" always have access without checks
        if (!CircabcComponent.isInitialized()
                || AuthenticationUtil.getAdminUserName().equals(authenticationService.getCurrentUserName())
                || AuthenticationUtil.getSystemUserName()
                .equals(authenticationService.getCurrentUserName())) {
            return false;
        }

        if (circabcRoot == null) {
            circabcRoot = circabcServiceRegistry.getManagementService().getCircabcNodeRef();
        }

        // Check if the aspect is set on the root node. If set, then the
        // freezing subsystem is enabled
        if (!nodeService2.hasAspect(circabcRoot, CircabcModel.ASPECT_LOCKED_FOR_ACCESS_ENABLED)) {
            return false;
        }

        return AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Boolean>() {

                    /**
                     * @see org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork#doWork()
                     */
                    @Override
                    public Boolean doWork() throws Exception {
                        return ultimateAncestorLockedForAccess(nodeRef);
                    }
                },
                AuthenticationUtil.getSystemUserName());
    }

    /**
     * Checks until it finds a root that is locked for access (has the aspect)
     */
    private boolean ultimateAncestorLockedForAccess(NodeRef nodeRef) {

        // Iterate up until the *possible* locked IG or Category root node is
        // found
        while (nodeRef != null
                && nodeService2.exists(nodeRef)
                && !nodeService2.hasAspect(nodeRef, CircabcModel.ASPECT_LOCKED_FOR_ACCESS)
                && !(nodeService2.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)
                || nodeService2.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY))) {

            nodeRef = nodeService2.getPrimaryParent(nodeRef).getParentRef();
        }

        // Check if the ultimate ancestor (parent, grand-parent, ...) that is
        // an IG or Category is locked for access
        return nodeRef != null
                && nodeService2.exists(nodeRef)
                && nodeService2.hasAspect(nodeRef, CircabcModel.ASPECT_LOCKED_FOR_ACCESS)
                && (nodeService2.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)
                || nodeService2.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY));
    }

    /**
     * Sets the value of the authenticationService
     *
     * @param authenticationService the authenticationService to set.
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Sets the value of the nodeService2
     *
     * @param nodeService2 the nodeService2 to set.
     */
    public void setNodeService2(NodeService nodeService2) {
        this.nodeService2 = nodeService2;
    }

    /**
     * Sets the value of the circabcServiceRegistry
     *
     * @param circabcServiceRegistry the circabcServiceRegistry to set.
     */
    public void setCircabcServiceRegistry(CircabcServiceRegistry circabcServiceRegistry) {
        this.circabcServiceRegistry = circabcServiceRegistry;
    }
}
