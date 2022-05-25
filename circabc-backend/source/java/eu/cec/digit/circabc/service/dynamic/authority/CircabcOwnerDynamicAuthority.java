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
import eu.cec.digit.circabc.service.app.CircabcService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.DynamicAuthority;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import static eu.cec.digit.circabc.model.CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI;

/**
 * The Circabc owner dynamic authority similar like Alfresco original, but if user is not member of
 * ig , or category admin , or circabc admin permission is denied
 *
 * @author andyh
 */
public class CircabcOwnerDynamicAuthority implements DynamicAuthority, InitializingBean {

    private static final Log logger = LogFactory.getLog(CircabcOwnerDynamicAuthority.class);
    private ServiceRegistry serviceRegistry = null;
    private OwnableService ownableService;
    private NodeService nodeService;
    private MultilingualContentService multilingualContentService;
    private AuthorityService authorityService;
    private CircabcService circabcService;

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    public void afterPropertiesSet() throws Exception {
        if (ownableService == null) {
            throw new IllegalArgumentException("There must be an ownable service");
        }
    }

    public boolean hasAuthority(final NodeRef nodeRef, final String userName) {
        return AuthenticationUtil.runAs(
                new RunAsWork<Boolean>() {

                    public Boolean doWork() throws Exception {
                        // TODO Auto-generated method stub
                        final String owner = ownableService.getOwner(nodeRef);
                        final boolean isOwner = EqualsHelper.nullSafeEquals(owner, userName);
                        boolean isMangedByCircabc =
                                nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_MANAGEMENT);
                        if (isMangedByCircabc) {
                            return isOwner && isInvitedUser(nodeRef, userName);
                        } else {
                            return isOwner;
                        }
                    }

                    private boolean isInvitedUser(NodeRef nodeRef, String userName) {
                        try {
                            final NodeRef container = getFirstContainer(nodeRef);
                            if (container == null) {
                                if (logger.isErrorEnabled()) {
                                    logger.error(
                                            "Error when call is invited user for user: "
                                                    + userName
                                                    + " and nodeRef "
                                                    + nodeRef.toString()
                                                    + " container is null");
                                }
                                return false;
                            }
                            if (nodeService.hasAspect(container, CircabcModel.ASPECT_IGROOT)
                                    && circabcService.readFromDatabase()) {
                                return circabcService.isUserMember(container, userName);
                            }
                            Set<String> invitedUsers = getInvitedUsers(container);
                            return invitedUsers.contains(userName);
                        } catch (Exception e) {
                            if (logger.isErrorEnabled()) {
                                logger.error(
                                        "Error when call is invited user for user: "
                                                + userName
                                                + " and nodeRef "
                                                + nodeRef.toString(),
                                        e);
                            }
                            return false;
                        }
                    }

                    private Set<String> getInvitedUsers(NodeRef container) {
                        Set<String> result = Collections.emptySet();
                        if (container == null) {
                            return result;
                        }
                        QName invitedUsersGroupQName = null;
                        if (nodeService.hasAspect(container, CircabcModel.ASPECT_IGROOT)) {
                            invitedUsersGroupQName =
                                    QName.createQName(
                                            CIRCABC_CONTENT_MODEL_1_0_URI, "circaIGRoot" + "InvitedUsersGroup");
                        } else if (nodeService.hasAspect(container, CircabcModel.ASPECT_CATEGORY)) {
                            invitedUsersGroupQName =
                                    QName.createQName(
                                            CIRCABC_CONTENT_MODEL_1_0_URI, "circaCategory" + "InvitedUsersGroup");
                        } else if (nodeService.hasAspect(container, CircabcModel.ASPECT_CIRCABC_ROOT)) {
                            invitedUsersGroupQName =
                                    QName.createQName(CIRCABC_CONTENT_MODEL_1_0_URI, "circaBC" + "InvitedUsersGroup");
                        }

                        if (invitedUsersGroupQName != null) {
                            Serializable property = nodeService.getProperty(container, invitedUsersGroupQName);
                            if (property != null) {
                                final String invitedUsersGroupName = (String) property;
                                final String prefixedUserGroupName =
                                        authorityService.getName(AuthorityType.GROUP, invitedUsersGroupName);
                                if (authorityService.authorityExists(prefixedUserGroupName)) {
                                    try {
                                        result =
                                                authorityService.getContainedAuthorities(
                                                        AuthorityType.USER, prefixedUserGroupName, false);
                                    } catch (InvalidNodeRefException e) {
                                        if (logger.isErrorEnabled()) {
                                            logger.error("Try to check authority for a deleted node / profile.", e);
                                        }
                                    }
                                } else {
                                    if (logger.isErrorEnabled()) {
                                        logger.error("Authority does bot exists: " + prefixedUserGroupName);
                                    }
                                }

                            } else {
                                if (logger.isErrorEnabled()) {
                                    logger.error(
                                            "Property "
                                                    + invitedUsersGroupQName
                                                    + " is null, for node "
                                                    + container.toString());
                                }
                            }
                        }
                        return result;
                    }

                    private NodeRef getFirstContainer(NodeRef nodeRef) {
                        NodeRef tempNodeRef = nodeRef;
                        for (; ; ) {
                            if (tempNodeRef == null) {
                                break;
                            }

                            if (nodeService
                                    .getType(tempNodeRef)
                                    .equals(ContentModel.TYPE_MULTILINGUAL_CONTAINER)) {
                                tempNodeRef = getMultilingualContentService().getPivotTranslation(tempNodeRef);
                            }

                            if (nodeService.hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)
                                    || nodeService.hasAspect(tempNodeRef, CircabcModel.ASPECT_CATEGORY)
                                    || nodeService.hasAspect(tempNodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)) {
                                break;
                            } else {
                                tempNodeRef = nodeService.getPrimaryParent(tempNodeRef).getParentRef();
                            }
                        }
                        return tempNodeRef;
                    }
                },
                AuthenticationUtil.getSystemUserName());
    }

    public String getAuthority() {
        return PermissionService.OWNER_AUTHORITY;
    }

    public Set<PermissionReference> requiredFor() {
        return null;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public MultilingualContentService getMultilingualContentService() {
        if (multilingualContentService == null) {
            multilingualContentService = serviceRegistry.getMultilingualContentService();
        }
        return multilingualContentService;
    }

    /**
     * @param serviceRegistry the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
