package io.swagger.api;

import io.swagger.model.PermissionDefinition;
import io.swagger.model.PermissionDefinitionPermissionsProfiles;
import io.swagger.model.PermissionDefinitionPermissionsUsers;
import io.swagger.model.Profile;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;

import java.util.List;
import java.util.Set;

/**
 * @author beaurpi
 */
public class PermissionsApiImpl implements PermissionsApi {

    public static final String GUEST = "guest";
    private static final String NOTIFICATION_STATUS = "NotificationStatus";
    private PermissionService permissionService;
    private NodeService nodeService;
    private ProfilesApi profilesApi;
    private UsersApi usersApi;
    private ApiToolBox apiToolBox;

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.PermissionsApi#getNodeIdPermissionsGet(java.lang.String)
     */
    @Override
    public PermissionDefinition getNodeIdPermissionsGet(String id) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        NodeRef igRef = apiToolBox.getCurrentInterestGroup(nodeRef);
        List<Profile> groupProfiles = profilesApi.groupsIdProfilesGet(igRef.getId(), null, false);

        PermissionDefinition result = new PermissionDefinition();
        result.setInherited(permissionService.getInheritParentPermissions(nodeRef));

        Set<AccessPermission> perms = permissionService.getAllSetPermissions(nodeRef);
        for (AccessPermission perm : perms) {
            if (perm.getAuthorityType().equals(AuthorityType.USER)
                    && !perm.getPermission().equals(NOTIFICATION_STATUS)
                    && !perm.getAuthority().equals(GUEST)) {
                result.getPermissions().getUsers().add(getUserPermission(perm));
            } else if ((perm.getAuthorityType().equals(AuthorityType.GROUP)
                    || perm.getAuthorityType().equals(AuthorityType.EVERYONE)
                    || perm.getAuthority().equals(GUEST))
                    && !perm.getPermission().equals(NOTIFICATION_STATUS)
                    && !perm.getAuthority().contains("CircaCategoryAdmin")) {
                // necessary because some nodes can have permissions referring
                // non existing profile
                PermissionDefinitionPermissionsProfiles tmp = getProfilePermission(perm, groupProfiles);
                if (tmp.getProfile() != null) {
                    result.getPermissions().getProfiles().add(tmp);
                }
            } else if (perm.getAuthority().equals(GUEST)
                    && !perm.getAuthority().contains("CircaCategoryAdmin")) {
                // necessary because some nodes can have permissions referring
                // non existing profile
                PermissionDefinitionPermissionsProfiles tmp = getProfilePermission(perm, groupProfiles);
                if (tmp.getProfile() != null) {
                    result.getPermissions().getProfiles().add(tmp);
                }
            }
        }

        return result;
    }

    private PermissionDefinitionPermissionsUsers getUserPermission(AccessPermission perm) {
        PermissionDefinitionPermissionsUsers result = new PermissionDefinitionPermissionsUsers();
        result.setUser(usersApi.usersUserIdGet(perm.getAuthority()));
        result.setPermission(perm.getPermission());
        result.setInherited(perm.isInherited());
        return result;
    }

    private PermissionDefinitionPermissionsProfiles getProfilePermission(
            AccessPermission perm, List<Profile> groupProfiles) {

        PermissionDefinitionPermissionsProfiles result = new PermissionDefinitionPermissionsProfiles();
        result.setProfile(getProfile(perm, groupProfiles));
        result.setPermission(perm.getPermission());
        result.setInherited(perm.isInherited());
        return result;
    }

    private Profile getProfile(AccessPermission perm, List<Profile> groupProfiles) {
        Profile result = null;
        for (Profile p : groupProfiles) {
            if (perm.getAuthority().equals(p.getGroupName())) {
                result = p;
            }
        }
        return result;
    }

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the profilesApi
     */
    public ProfilesApi getProfilesApi() {
        return profilesApi;
    }

    /**
     * @param profilesApi the profilesApi to set
     */
    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @Override
    public PermissionDefinition nodeIdPermissionsPut(String id, PermissionDefinition body) {

        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        PermissionDefinition oldPerms = getNodeIdPermissionsGet(id);
        if (!oldPerms.getInherited() && body.getInherited()) {
            permissionService.setInheritParentPermissions(nodeRef, true);
            return getNodeIdPermissionsGet(id);
        } else if (oldPerms.getInherited() && !body.getInherited()) {
            permissionService.setInheritParentPermissions(nodeRef, false);
        } else {
            for (PermissionDefinitionPermissionsProfiles pdpp : body.getPermissions().getProfiles()) {
                this.permissionService.setPermission(
                        nodeRef, pdpp.getProfile().getGroupName(), pdpp.getPermission(), true);
            }

            for (PermissionDefinitionPermissionsUsers pdpu : body.getPermissions().getUsers()) {
                this.permissionService.setPermission(
                        nodeRef, pdpu.getUser().getUserId(), pdpu.getPermission(), true);
            }
        }
        return getNodeIdPermissionsGet(id);
    }

    @Override
    public void nodeIdPermissionsDelete(String id, String authority, String permission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (id != null && !"".equals(authority) && !"".equals(permission)) {
            permissionService.deletePermission(nodeRef, authority, permission);
        }
    }

    @Override
    public void nodeIdPermissionsClear(String id, String authority) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (id != null && !"".equals(authority)) {
            Set<AccessPermission> allPerms = permissionService.getAllSetPermissions(nodeRef);
            for (AccessPermission perm : allPerms) {
                if (perm.getAuthority().equals(authority)
                        && !perm.isInherited()
                        && !perm.getPermission().equals(NOTIFICATION_STATUS)) {
                    permissionService.deletePermission(nodeRef, perm.getAuthority(), perm.getPermission());
                }
            }
        }
    }

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }
}
