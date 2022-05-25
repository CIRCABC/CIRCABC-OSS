/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.dynamic.authority;

import eu.cec.digit.circabc.model.CircabcModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.repo.security.permissions.impl.PermissionsDaoComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

public abstract class AbstractTransgressCutHeritance implements DynamicAuthority, InitializingBean {

    /**
     * The logger
     */
    protected static final Log logger = LogFactory.getLog(AbstractTransgressCutHeritance.class);
    protected final List<String> permissions = new ArrayList<>();
    protected String roleToGrant;
    protected String circabcService;
    protected String permissionToCheck;
    protected Map<QName, QName> mandatoryAspects = new HashMap<>();
    protected NodeService nodeService;
    protected PermissionsDaoComponent permissionsDaoComponent;
    protected AuthorityService authorityService;
    protected ModelDAO modelDAO;
    protected PermissionReference transgressCutInheritancePermRef = null;
    protected Set<PermissionReference> permissionReference = null;

    public AbstractTransgressCutHeritance() {
        super();
    }

    public String getAuthority() {
        return roleToGrant;
    }

    public void afterPropertiesSet() throws Exception {
        if (nodeService == null) {
            throw new IllegalStateException("The NodeService service must be set");
        }

        if (permissionsDaoComponent == null) {
            throw new IllegalStateException("The permissions Dao Component must be set");
        }

        if (authorityService == null) {
            throw new IllegalStateException("The authority Service must be set");
        }

        if (modelDAO == null) {
            throw new IllegalStateException("The model DAO must be set");
        }

        if (mandatoryAspects.size() == 0) {
            throw new IllegalStateException("The mandatory Aspects must be set");
        }

        if (roleToGrant == null) {
            throw new IllegalStateException("The roleToGrant must be set");
        }

        if (permissionToCheck == null) {
            throw new IllegalStateException("The permissionToCheck must be set");
        }

        transgressCutInheritancePermRef =
                modelDAO.getPermissionReference(CircabcModel.ASPECT_CIRCABC_MANAGEMENT, permissionToCheck);
    }

    public boolean hasAuthority(final NodeRef nodeRef, final String userName) {

        return AuthenticationUtil.runAs(
                new RunAsWork<Boolean>() {

                    public Boolean doWork() throws Exception {
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "***  "
                                            + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                                            + " ("
                                            + nodeRef.getId()
                                            + ") "
                                            + "   ***  ");
                        }

                        QName mandatoryAspect = null;
                        for (final QName mandatoryAspectKey : mandatoryAspects.keySet()) {
                            if (nodeService.hasAspect(nodeRef, mandatoryAspectKey)) {
                                mandatoryAspect = mandatoryAspectKey;
                                break;
                            }
                        }

                        if (mandatoryAspect == null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "The node doesn't have one of the mandatory haspects:" + mandatoryAspects);
                            }
                            return false;
                        }

                        return isAdmin(
                                nodeRef,
                                userName,
                                authorityService.getContainingAuthorities(AuthorityType.GROUP, userName, true),
                                modelDAO.getGrantingPermissions(transgressCutInheritancePermRef),
                                mandatoryAspect);
                    }

                    public boolean isAdmin(
                            final NodeRef nodeRef,
                            final String userName,
                            final Set<String> containingGroupAuthorities,
                            final Set<PermissionReference> grantingPermissions,
                            final QName mandatoryAspect) {
                        if (nodeRef == null) {
                            return false;
                        } else {
                            final NodePermissionEntry permissions =
                                    permissionsDaoComponent.getPermissions(nodeRef);

                            boolean adminOnService = false;

                            for (final PermissionEntry permissionEntry : permissions.getPermissionEntries()) {
                                if (permissionEntry.getAuthority().equals(userName)
                                        || containingGroupAuthorities.contains(permissionEntry.getAuthority())) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "("
                                                        + circabcService
                                                        + ")"
                                                        + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)
                                                        + " --> "
                                                        + permissionEntry.getPermissionReference().getName()
                                                        + " from   "
                                                        + permissionEntry.getPosition()
                                                        + " for "
                                                        + permissionEntry.getAuthority());
                                    }

                                    adminOnService =
                                            grantingPermissions.contains(permissionEntry.getPermissionReference());
                                }

                                if (adminOnService) {
                                    break;
                                }
                            }
                            boolean hasTopAspect = false;

                            if (nodeService.hasAspect(nodeRef, mandatoryAspects.get(mandatoryAspect))) {
                                hasTopAspect = true;
                            }

                            if (adminOnService || hasTopAspect) {
                                return adminOnService;
                            } else {
                                // recursive Test: Check the parent
                                return isAdmin(
                                        nodeService.getPrimaryParent(nodeRef).getParentRef(),
                                        userName,
                                        containingGroupAuthorities,
                                        grantingPermissions,
                                        mandatoryAspect);
                            }
                        }
                    }
                },
                AuthenticationUtil.getSystemUserName());
    }

    public Set<PermissionReference> requiredFor() {
        if (permissionReference == null) {
            permissionReference = new HashSet<>();
            for (final String permission : this.permissions) {
                permissionReference.addAll(
                        modelDAO.getGranteePermissions(
                                modelDAO.getPermissionReference(
                                        CircabcModel.ASPECT_CIRCABC_MANAGEMENT, permission)));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("(" + circabcService + ")" + " requiredFor:" + permissionReference);
            }
        }
        return permissionReference;
        // return null;
    }

    /**
     * List of permissions where the dynamic authority has competence for
     *
     * @param permissions list of permissions
     */
    public abstract void setRequiredFor(final List<String> permissions);

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Set the permissions dao component
     */
    public void setPermissionsDaoComponent(final PermissionsDaoComponent permissionsDaoComponent) {
        this.permissionsDaoComponent = permissionsDaoComponent;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public final void setAuthorityService(final AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param modelDAO the modelDAO to set
     */
    public final void setModelDAO(final ModelDAO modelDAO) {
        this.modelDAO = modelDAO;
    }

    public void setPermissionToCheck(final String permissionToCheck) {
        this.permissionToCheck = permissionToCheck;
    }

    public final void setRoleToGrant(final String roleToGrant) {
        this.roleToGrant = roleToGrant;
    }

    public abstract void setCircabcService(final String circabcService);

    public abstract void setMandatoryAspects(final Map<String, String> mandatoryAspects);
}
