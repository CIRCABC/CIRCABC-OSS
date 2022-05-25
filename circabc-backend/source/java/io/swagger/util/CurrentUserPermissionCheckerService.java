package io.swagger.util;

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.app.model.InterestGroupResult;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.permissions.*;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import io.swagger.model.GuardAuthorization;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
import org.apache.commons.lang.NotImplementedException;

import java.util.Set;

public class CurrentUserPermissionCheckerService {

    private PermissionService permissionService;
    private NodeService nodeService;
    private AuthorityService authorityService;
    private CircabcService circabcService;
    private LdapUserService ldapUserService;

    public boolean isExternalUser() {
        if (CircabcConfig.OSS) {
            return false;
        }
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        CircabcUserDataBean user = ldapUserService.getLDAPUserDataByUid(userName);
        return user.getDomain().equalsIgnoreCase("external");
    }

    /**
     * if current user is not CIRCABC admin it will throw AccessDenied exception
     *
     * @return true if current user is CIRCABC admin
     */
    public boolean throwIfNotCircabcAdmin() {
        return throwAccessDeniedIfFalse(isCircabcAdmin(), "Not Circabc admin");
    }

    /**
     * if current user is not CIRCABC admin it will throw AccessDenied exception
     *
     * @param categoryId id of category
     * @return true if current user is category admin
     */
    public boolean throwIfNotCategoryAdmin(String categoryId) {
        return throwAccessDeniedIfFalse(isCategoryAdmin(categoryId), "Not Category admin");
    }

    /**
     * Throw AccessDenied exception if current user does not have rights to access interest group
     *
     * @param interestGroupId id of interest group
     * @return tru if user has rights otherwise throw exception
     */
    public boolean throwIfCanNotAccessInterestGroup(String interestGroupId) {
        return throwAccessDeniedIfFalse(
                canAccessInterestGroup(interestGroupId), "Can not access interest group");
    }

    /**
     * Throw exception if current userName is not equal with passed userName This method is useful to
     * check if user that is passed in url is equal to authenticated user
     *
     * @param userName userName that we want to check
     * @return true if exception is not thrown
     */
    public boolean throwIfNotCurrentUser(String userName) {
        return throwAccessDeniedIfFalse(
                isCurrentUserEqualTo(userName), userName + "is not current user");
    }

    private Boolean throwAccessDeniedIfFalse(Boolean value, String message) {
        if (Boolean.FALSE.equals(value)) {
            throw new AccessDeniedException(message);
        }
        return true;
    }

    /**
     * Check if current user is guest
     *
     * @return true if user is alfresco guest false otherwise
     */
    public boolean isGuest() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        return authorityService.isGuestAuthority(userName);
    }

    /**
     * Check if current user name is equal to passed parameter
     *
     * @param userName user name to check
     * @return true if userNames are equal otherwise false
     */
    public boolean isCurrentUserEqualTo(String userName) {
        return AuthenticationUtil.getFullyAuthenticatedUser().equals(userName);
    }

    /**
     * Check if current user has rights to access interest group
     *
     * @param interestGroupId interest group id (uuid of node ref)
     */
    public boolean canAccessInterestGroup(String interestGroupId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(interestGroupId);
        return canUserAccessInterestGroup(nodeRef);
    }

    /**
     * ,Check if current user is category admin
     *
     * @param categoryId id of category
     * @return true is current user is category admin otherwise false
     */
    public boolean isCategoryAdmin(String categoryId) {
        NodeRef categoryRef = Converter.createNodeRefFromId(categoryId);
        return isCategoryAdmin(categoryRef);
    }

    private boolean isCategoryAdmin(NodeRef categoryNodeRef) {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (circabcService.readFromDatabase()) {
            return circabcService.isCategoryAdmin(categoryNodeRef, userName);
        } else {
            throw new NotImplementedException();
        }
    }

    /**
     * Check if current user is circabc admin
     *
     * @return true if current user is circabc admin otherwise false
     */
    public boolean isCircabcAdmin() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (circabcService.readFromDatabase()) {
            return circabcService.isCircabcAdmin(userName);
        } else {
            throw new NotImplementedException();
        }
    }

    /**
     * Check if current user is alfresco admin
     *
     * @return true if current user is alfresco admin
     */
    public boolean isAlfrescoAdmin() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        return authorityService.isAdminAuthority(userName);
    }

    public boolean isCurrentUserDirAdminOrCategoryAdminOrCircabcAdmin() {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (circabcService.readFromDatabase()) {
            return circabcService.isUserDirAdminOrCategoryAdminOrCircabcAdmin(userName);
        } else {
            throw new NotImplementedException();
        }
    }

    private boolean canUserAccessInterestGroup(NodeRef interestGroupNodeRef) {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        if (circabcService.readFromDatabase()) {
            InterestGroupResult interestGroup = circabcService.getInterestGroup(interestGroupNodeRef);
            if (userName.equals("guest")) {
                return interestGroup.getIsPublic();
            } else if (Boolean.TRUE.equals(interestGroup.getIsRegistered())) {
                return true;
            } else if (circabcService.isUserMember(interestGroupNodeRef, userName)) {
                return true;
            } else {
                return circabcService.isCategoryAdminOfInterestGroup(interestGroupNodeRef, userName);
            }
        } else {
            throw new NotImplementedException();
        }
    }

    /**
     * Check if current user has any of alfresco permissions
     *
     * @param nodeId              id of node to check
     * @param alfrescoPermissions alfresco permissions to check for example
     *                            Read,Write,Delete,AddChildren
     * @return true if user has any of Alfresco permission on given node otherwise false
     */
    public boolean hasAnyOfAlfrescoPermission(String nodeId, String... alfrescoPermissions) {
        boolean result = false;
        for (String alfrescoPermission : alfrescoPermissions) {
            result = hasAlfrescoPermission(nodeId, alfrescoPermission);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Return tru if current user has read permission on node
     */
    public boolean hasAlfrescoReadPermission(String nodeId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, PermissionService.READ)
                .equals(AccessStatus.ALLOWED);
    }

    /**
     * Return true if current user has write permission on node
     */
    public boolean hasAlfrescoWritePermission(String nodeId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, PermissionService.WRITE)
                .equals(AccessStatus.ALLOWED);
    }

    /**
     * Return tru if current user has delete permission on node
     */
    public boolean hasAlfrescoDeletePermission(String nodeId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, PermissionService.DELETE)
                .equals(AccessStatus.ALLOWED);
    }

    /**
     * Return tru if current user has add children permission on node
     */
    public boolean hasAlfrescoAddChildrenPermission(String nodeId) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, PermissionService.ADD_CHILDREN)
                .equals(AccessStatus.ALLOWED);
    }

    /**
     * Check if current user has any of library permissions
     *
     * @param nodeId             id of node to check
     * @param libraryPermissions library permissions to check for example
     *                           Read,Write,Delete,AddChildren
     * @return true if user has any of library permission on given node otherwise false
     */
    public boolean hasAnyOfLibraryPermission(
            String nodeId, LibraryPermissions... libraryPermissions) {
        boolean result = false;
        for (LibraryPermissions libraryPermission : libraryPermissions) {
            result = hasLibraryPermission(nodeId, libraryPermission);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if the current id is a post of a document located in the Library
     *
     * @param id node id
     */
    public boolean isDocumentPost(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        return ForumModel.TYPE_POST.equals(nodeService.getType(nodeRef))
                && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY);
    }

    /**
     * Check if current user has any of newsgroups permissions
     *
     * @param nodeId               id of node to check
     * @param newsGroupPermissions newsgroups permissions to check for example
     *                             Read,Write,Delete,AddChildren
     * @return true if user has any of library permission on given node otherwise false
     */
    public boolean hasAnyOfNewsGroupPermission(
            String nodeId, NewsGroupPermissions... newsGroupPermissions) {
        boolean result = false;
        for (NewsGroupPermissions newsGroupPermission : newsGroupPermissions) {
            result = hasNewsGroupPermission(nodeId, newsGroupPermission);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if current user has any of information permissions
     *
     * @param nodeId                 id of node to check
     * @param informationPermissions information permissions to check f
     * @return true if user has any of information permission on given node otherwise false
     */
    public boolean hasAnyOfInformationPermission(
            String nodeId, InformationPermissions... informationPermissions) {
        boolean result = false;
        for (InformationPermissions informationPermission : informationPermissions) {
            result = hasInformationPermission(nodeId, informationPermission);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if current user has any of event permissions
     *
     * @param nodeId           id of node to check
     * @param eventPermissions event permissions to check
     * @return true if user has any of event permission on given node otherwise false
     */
    public boolean hasAnyOfEventPermission(String nodeId, EventPermissions... eventPermissions) {
        boolean result = false;
        for (EventPermissions eventPermission : eventPermissions) {
            result = hasEventPermission(nodeId, eventPermission);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Check if current user has any of directory permissions
     *
     * @param nodeId               id of node to check
     * @param directoryPermissions directory permissions to check
     * @return true if user has any of directory permission on given node otherwise false
     */
    public boolean hasAnyOfDirectoryPermission(
            String nodeId, DirectoryPermissions... directoryPermissions) {
        boolean result = false;
        for (DirectoryPermissions directoryPermission : directoryPermissions) {
            result = hasDirectoryPermission(nodeId, directoryPermission);
            if (result) {
                break;
            }
        }
        return result;
    }

    private boolean hasLibraryPermission(String nodeId, LibraryPermissions libraryPermission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, libraryPermission.toString())
                .equals(AccessStatus.ALLOWED);
    }

    private boolean hasInformationPermission(
            String nodeId, InformationPermissions informationPermission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, informationPermission.toString())
                .equals(AccessStatus.ALLOWED);
    }

    private boolean hasEventPermission(String nodeId, EventPermissions eventPermission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, eventPermission.toString())
                .equals(AccessStatus.ALLOWED);
    }

    private boolean hasNewsGroupPermission(String nodeId, NewsGroupPermissions newsGroupPermission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, newsGroupPermission.toString())
                .equals(AccessStatus.ALLOWED);
    }

    private boolean hasDirectoryPermission(String nodeId, DirectoryPermissions directoryPermission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, directoryPermission.toString())
                .equals(AccessStatus.ALLOWED);
    }

    private boolean hasAlfrescoPermission(String nodeId, String alfrescoPermission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);
        return permissionService
                .hasPermission(nodeRef, alfrescoPermission)
                .equals(AccessStatus.ALLOWED);
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public boolean isInterestGroupLibAdmin(String igId) {
        NodeRef igRef = Converter.createNodeRefFromId(igId);

        if (nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)) {
            NodeRef libRef = nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Library");
            return libRef != null
                    && hasAnyOfLibraryPermission(libRef.getId(), LibraryPermissions.LIBADMIN);
        }

        return false;
    }

    public boolean isInterestGroupNewsAdmin(String igId) {
        NodeRef igRef = Converter.createNodeRefFromId(igId);

        if (nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)) {
            NodeRef newsRef =
                    nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Newsgroups");
            return newsRef != null
                    && hasAnyOfNewsGroupPermission(newsRef.getId(), NewsGroupPermissions.NWSADMIN);
        }

        return false;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public boolean isInterestGroupDirAdmin(String igId) {
        NodeRef igRef = Converter.createNodeRefFromId(igId);

        if (nodeService.hasAspect(igRef, CircabcModel.ASPECT_IGROOT)) {
            return hasAnyOfDirectoryPermission(igRef.getId(), DirectoryPermissions.DIRADMIN);
        }

        return false;
    }

    public boolean  isWorkingCopyOwner(String id) {
        boolean result = false;
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        boolean isWorkingCopy = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
        if (isWorkingCopy) {
            String workingCopyOwner = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_WORKING_COPY_OWNER);
            result = workingCopyOwner.equals(AuthenticationUtil.getRunAsUser());
        }
        return result;
    }
    

    public boolean hasAlfCancelCheckoutPermission(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        return permissionService
                .hasPermission(nodeRef, PermissionService.CANCEL_CHECK_OUT)
                .equals(AccessStatus.ALLOWED);
    }

    public boolean hasAlfCheckinPermission(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        return permissionService
                .hasPermission(nodeRef, PermissionService.CHECK_IN)
                .equals(AccessStatus.ALLOWED);
    }

    public boolean hasAlfCheckoutPermission(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        return permissionService
                .hasPermission(nodeRef, PermissionService.CHECK_OUT)
                .equals(AccessStatus.ALLOWED);
    }

    public boolean isGroupAdmin(String id) {
        NodeRef igRef = Converter.createNodeRefFromId(id);

        if (nodeService.exists(igRef)) {
            return !(hasAnyOfDirectoryPermission(igRef.getId(), DirectoryPermissions.DIRADMIN)
                    && hasAnyOfLibraryPermission(igRef.getId(), LibraryPermissions.LIBADMIN)
                    && hasAnyOfEventPermission(igRef.getId(), EventPermissions.EVEADMIN)
                    && hasAnyOfInformationPermission(igRef.getId(), InformationPermissions.INFADMIN)
                    && hasAnyOfNewsGroupPermission(igRef.getId(), NewsGroupPermissions.NWSADMIN));
        }

        return false;
    }

    public boolean hasTakeOwnershipPermission(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        return permissionService
                .hasPermission(nodeRef, PermissionService.TAKE_OWNERSHIP)
                .equals(AccessStatus.ALLOWED);
    }

    public boolean verifyMemberPermission(String id, String permission) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.exists(nodeRef)) {
            return false;
        }

        GuardAuthorization result = new GuardAuthorization();
        result.setGranted(false);
        String userName = AuthenticationUtil.getRunAsUser();
        Set<String> authorities = authorityService.getAuthorities();
        for (AccessPermission ac : permissionService.getAllSetPermissions(nodeRef)) {
            if (ac.getAuthorityType() == AuthorityType.USER) {
                if (ac.getAuthority().equals(userName)
                        && ac.getPermission().equals(permission)
                        && ac.getAccessStatus().name().equals("ALLOWED")) {
                    return true;
                }
            } else if (ac.getAuthorityType() == AuthorityType.GROUP
                    && authorities.contains(ac.getAuthority())
                    && ac.getPermission().equals(permission)) {
                return true;
            }
        }
        return (permissionService.hasPermission(nodeRef, "Write").equals(AccessStatus.ALLOWED));
    }

    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }

    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }
}
