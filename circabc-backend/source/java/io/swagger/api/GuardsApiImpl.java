package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.app.CircabcDaoServiceImpl;
import eu.cec.digit.circabc.service.profile.permissions.*;
import io.swagger.exception.NonExistingNodeException;
import io.swagger.model.GuardAuthorization;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;

import java.util.Set;

/**
 * @author beaurpi
 */
public class GuardsApiImpl implements GuardsApi {

    private static final String INFORMATION = "information";
    private static final String MEMBERS = "members";
    private static final String APPLICANTS = "applicants";
    private static final String GUEST = "guest";
    private CircabcDaoServiceImpl circabcDaoService;
    private PermissionService permissionService;
    private AuthenticationService authenticationService;
    private AuthorityService authorityService;
    private NodeService nodeService;
    private GroupsApi groupsApi;

    @Override
    public GuardAuthorization guardsGroupIdGet(String id) {

        NodeRef igRef = Converter.createNodeRefFromId(id);

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);

        final Set<AccessPermission> allSetPermissions = permissionService.getAllSetPermissions(igRef);
        boolean isPublic = isAuthorityVisible(allSetPermissions, GUEST);
        boolean isRegistered = isAuthorityVisible(allSetPermissions, "GROUP_EVERYONE");

        if (isPublic) {
            result.setGranted(true);
        } else if (isRegistered && !authenticationService.getCurrentUserName().equals(GUEST)) {
            result.setGranted(true);
        } else if (!isRegistered) {
            String username = authenticationService.getCurrentUserName();

            if (username != null && !username.equals(GUEST)) {
                result.setGranted(
                        permissionService.hasPermission(igRef, "Read").equals(AccessStatus.ALLOWED));
            }
        }

        return result;
    }

    private boolean isAuthorityVisible(Set<AccessPermission> allSetPermissions, String authority) {
        for (AccessPermission accessPermission : allSetPermissions) {
            if (accessPermission.getAuthority().equals(authority)
                    && accessPermission.getAccessStatus().equals(AccessStatus.ALLOWED)) {
                if (accessPermission.getPermission().equals("NoVisibility")) {
                    return false;
                } else if (accessPermission.getPermission().equals("Visibility")) {
                    return true;
                }
            }
        }
        return false;
    }

    public CircabcDaoServiceImpl getCircabcDaoService() {
        return circabcDaoService;
    }

    public void setCircabcDaoService(CircabcDaoServiceImpl circabcDaoService) {
        this.circabcDaoService = circabcDaoService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    @Override
    public GuardAuthorization guardsAccessIdGet(String id) throws NonExistingNodeException {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.exists(nodeRef)) {
            throw new NonExistingNodeException("the node is not existing" + nodeRef.toString());
        }

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
            if (permissionService
                    .hasPermission(nodeRef, LibraryPermissions.LIBACCESS.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)) {
            if (permissionService
                    .hasPermission(nodeRef, InformationPermissions.INFACCESS.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {
            if (permissionService
                    .hasPermission(nodeRef, NewsGroupPermissions.NWSACCESS.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {
            if (permissionService
                    .hasPermission(nodeRef, EventPermissions.EVEACCESS.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        }

        return result;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public GuardAuthorization guardsEditionIdGet(String id) throws NonExistingNodeException {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.exists(nodeRef)) {
            throw new NonExistingNodeException("the node is not existing" + nodeRef.toString());
        }

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);
        String userName = AuthenticationUtil.getRunAsUser();
        Set<String> authorities = authorityService.getAuthorities();
        for (AccessPermission ac : permissionService.getAllSetPermissions(nodeRef)) {
            if (ac.getAuthorityType() == AuthorityType.USER) {
                if (ac.getAuthority().equals(userName)) {
                    if (ac.getPermission().equals(LibraryPermissions.LIBMANAGEOWN.toString())
                            || ac.getPermission().equals(LibraryPermissions.LIBFULLEDIT.toString())
                            || ac.getPermission().equals(LibraryPermissions.LIBEDITONLY.toString())
                            || ac.getPermission().equals(LibraryPermissions.LIBADMIN.toString())
                            && ac.getAccessStatus().name().equals("ALLOWED")) {
                        result.setGranted(true);
                    }
                }
            } else if (ac.getAuthorityType() == AuthorityType.GROUP) {
                if (authorities.contains(ac.getAuthority())) {
                    result.setGranted(true);
                }
            }
        }

        if (permissionService.hasPermission(nodeRef, "Write").equals(AccessStatus.ALLOWED)) {
            result.setGranted(true);
        }

        return result;
    }

    @Override
    public GuardAuthorization guardsGroupIdServiceNameGet(String groupIp, String serviceName) {
        NodeRef igNodeRef = Converter.createNodeRefFromId(groupIp);

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);

        if (MEMBERS.equals(serviceName)) {
            for (DirectoryPermissions dirPermDef : DirectoryPermissions.values()) {
                if (!dirPermDef.equals(DirectoryPermissions.DIRNOACCESS)) {
                    if (permissionService
                            .hasPermission(igNodeRef, dirPermDef.toString())
                            .equals(AccessStatus.ALLOWED)) {

                        result.setGranted(true);
                        break;
                    }
                }
            }
        }

        if (APPLICANTS.equals(serviceName)) {
            if (permissionService
                    .hasPermission(igNodeRef, DirectoryPermissions.DIRMANAGEMEMBERS.toString())
                    .equals(AccessStatus.ALLOWED)
                    || permissionService
                    .hasPermission(igNodeRef, DirectoryPermissions.DIRADMIN.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        }

        if (INFORMATION.equals(serviceName)) {
            NodeRef infRef =
                    nodeService.getChildByName(igNodeRef, ContentModel.ASSOC_CONTAINS, "Information");
            if (permissionService.hasPermission(infRef, "Consumer").equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        }

        return result;
    }

    @Override
    public GuardAuthorization guardsAdministrationIdGet(String nodeId) {

        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            NodeRef infRef =
                    nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "Information");
            NodeRef libRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "Library");
            NodeRef nwsRef =
                    nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "Newsgroups");
            NodeRef evtRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, "Events");

            if (permissionService
                    .hasPermission(libRef, LibraryPermissions.LIBADMIN.toString())
                    .equals(AccessStatus.ALLOWED)
                    && permissionService
                    .hasPermission(infRef, InformationPermissions.INFADMIN.toString())
                    .equals(AccessStatus.ALLOWED)
                    && permissionService
                    .hasPermission(evtRef, EventPermissions.EVEADMIN.toString())
                    .equals(AccessStatus.ALLOWED)
                    && permissionService
                    .hasPermission(nwsRef, NewsGroupPermissions.NWSADMIN.toString())
                    .equals(AccessStatus.ALLOWED)
                    && permissionService
                    .hasPermission(nodeRef, DirectoryPermissions.DIRADMIN.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)) {

            result.setGranted(
                    permissionService
                            .hasPermission(nodeRef, InformationPermissions.INFADMIN.toString())
                            .equals(AccessStatus.ALLOWED));
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {

            result.setGranted(
                    permissionService
                            .hasPermission(nodeRef, LibraryPermissions.LIBADMIN.toString())
                            .equals(AccessStatus.ALLOWED));
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {

            result.setGranted(
                    permissionService
                            .hasPermission(nodeRef, NewsGroupPermissions.NWSADMIN.toString())
                            .equals(AccessStatus.ALLOWED));
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {

            result.setGranted(
                    permissionService
                            .hasPermission(nodeRef, EventPermissions.EVEADMIN.toString())
                            .equals(AccessStatus.ALLOWED));
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CATEGORY)) {
            result.setGranted(
                    permissionService
                            .hasPermission(nodeRef, CategoryPermissions.CIRCACATEGORYADMIN.toString())
                            .equals(AccessStatus.ALLOWED)
                            || permissionService
                            .hasPermission(nodeRef, CategoryPermissions.CIRCACATEGORYMANAGEMEMBERS.toString())
                            .equals(AccessStatus.ALLOWED));
        }

        return result;
    }

    @Override
    public GuardAuthorization guardsGroupIdMembersAdminGet(String groupIp) {
        NodeRef nodeRef = Converter.createNodeRefFromId(groupIp);

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);

        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT)) {
            if (permissionService
                    .hasPermission(nodeRef, DirectoryPermissions.DIRADMIN.toString())
                    .equals(AccessStatus.ALLOWED)) {
                result.setGranted(true);
            }
        }

        return result;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }
}
