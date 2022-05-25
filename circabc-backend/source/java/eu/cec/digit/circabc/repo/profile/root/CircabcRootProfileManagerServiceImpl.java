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
package eu.cec.digit.circabc.repo.profile.root;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.PermissionModel;
import eu.cec.digit.circabc.repo.profile.ProfileManagerServiceImpl;
import eu.cec.digit.circabc.service.profile.*;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class CircabcRootProfileManagerServiceImpl extends ProfileManagerServiceImpl
        implements CircabcRootProfileManagerService {

    public static final String PROFILE_PREFIX = "circaBC";
    /**
     * A logger for the class
     */
    private static final Log logger = LogFactory.getLog(CircabcRootProfileManagerServiceImpl.class);
    private static final Set<String> services = new HashSet<>();

    public CircabcRootProfileManagerServiceImpl() {
        services.add(CircabcServices.CIRCABC.toString());
        // Special case
        // services.add(CircaServices.LIBRARY.toString());
    }

    public String getProfilePrefix() {
        return PROFILE_PREFIX;
    }

    /**
     * @return the services
     */
    public final Set<String> getServices() {
        return services;
    }

    public List<QName> getQNameServiceRoles() {
        final List<QName> list = new ArrayList<>();
        // list.add(CircaIGRootAspect.LIBRARY_ROLE);
        list.add(PermissionModel.CIRCABC_PERMISSION);
        // list.add(AbstractAspect.CircaAbstractInterface.DIRECTORY_PERMISSION);
        return list;
    }

    public Map<QName, String> getServiceRolesEnum() {
        final Map<QName, String> map = new HashMap<>();
        // map.put(CircaIGRootAspect.LIBRARY_ROLE, CircaServices.LIBRARY);
        map.put(PermissionModel.CIRCABC_PERMISSION, CircabcServices.CIRCABC.toString());
        // map.put(AbstractAspect.CircaAbstractInterface.DIRECTORY_PERMISSION,
        // CircaServices.DIRECTORY);
        return map;
    }

    @Override
    public QName getNodeAspect() {
        return getNodeRootAspect();
    }

    @Override
    public QName getNodeRootAspect() {
        return CircabcModel.ASPECT_CIRCABC_ROOT;
    }

    public boolean hasParentSubsGroup() {
        return false;
    }

    public boolean hasMasterParentGroup() {
        return false;
    }

    public boolean hasApplicantFeature() {
        return false;
    }

    public void clearAlfrescoAdminPermissions(final NodeRef nodeRef) {
        permissionService.clearPermission(nodeRef, PermissionService.ADMINISTRATOR_AUTHORITY);
        final String key = nodeRef.toString();
        profileMapCache.remove(key);
    }

    public void setAlfrescoAdminPermissions(
            final NodeRef nodeRef, final Set<String> permissions, final boolean access) {
        if (logger.isTraceEnabled()) {
            logger.trace("setPermissions on node:" + nodeRef + "");
        }
        for (final String permission : permissions) {
            permissionService.setPermission(
                    nodeRef, PermissionService.ADMINISTRATOR_AUTHORITY, permission, access);
            if (logger.isTraceEnabled()) {
                logger.trace(
                        "setPermission: "
                                + PermissionService.ADMINISTRATOR_AUTHORITY
                                + " "
                                + permission
                                + " "
                                + access);
            }
        }
        final String key = nodeRef.toString();
        profileMapCache.remove(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.profile.ProfileManagerServiceImpl#addApplicantPerson(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void addApplicantPerson(
            final NodeRef nodeRef,
            final String userID,
            final String message,
            final String firstName,
            final String lastName)
            throws ProfileException {
        throw new IllegalStateException(
                "This method is not yet implemented for the aspect " + getNodeAspect());
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.profile.ProfileManagerServiceImpl#removeApplicantPerson(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    @Override
    public void removeApplicantPerson(final NodeRef nodeRef, final String userID)
            throws ProfileException {
        throw new IllegalStateException(
                "This method is not yet implemented for the aspect " + getNodeAspect());
    }

    /**
     * Create a new Global GROUP that will contain the Domains group
     *
     * <p>Migration 3.1 -> 3.4.6 - 09/12/2011 createAuthority() method changed for version 3.4
     */
    public String createAllCircaUsersGroup() {

        // create the group has a root authority
        //		final String allUserGroupName = authorityService.createAuthority(AuthorityType.GROUP,
        // parentGroup, ProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME, null);
        final String allUserGroupName =
                authorityService.createAuthority(
                        AuthorityType.GROUP,
                        ProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME,
                        ProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME,
                        authorityService.getDefaultZones());
        return allUserGroupName;
    }

    /**
     * Create a new Global GROUP that will contain the Domains group
     *
     * <p>Migration 3.1 -> 3.4.6 - 09/12/2011 createAuthority() method changed for version 3.4
     */
    public String createCircaSupportGroup() {

        // create the group has a root authority
        //		final String supportGroupName = authorityService.createAuthority(AuthorityType.GROUP,
        // parentGroup, ProfileManagerService.SUPPORT_GROUP_NAME, null);
        final String supportGroupName =
                authorityService.createAuthority(
                        AuthorityType.GROUP,
                        ProfileManagerService.SUPPORT_GROUP_NAME,
                        ProfileManagerService.SUPPORT_GROUP_NAME,
                        authorityService.getDefaultZones());
        return supportGroupName;
    }

    /**
     * Return the AllCircaUSers group name without prefixe (GROUP_)
     */
    public String getAllCircaUsersGroupName() {
        return ProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME;
    }

    /**
     * Return the prefixed (GROUP_) AllCircaUSers group name
     */
    public String getPrefixedAllCircaUsersGroupName() {
        return getPrefixedAlfrescoGroupName(getAllCircaUsersGroupName());
    }

    /**
     * utility that returns the name of the group prefixed (GROUP_)
     */
    private String getPrefixedAlfrescoGroupName(final String groupName) {
        final String prefixedGroupName = authorityService.getName(AuthorityType.GROUP, groupName);
        return prefixedGroupName;
    }

    public List<Profile> getProfilesRecursivly(final NodeRef nodeRef) {
        final List<Profile> profiles = new ArrayList<>();
        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_CIRCABC_ROOT)) {
            NodeRef categoryNodeRef;
            profiles.addAll(getProfiles(nodeRef));
            for (final ChildAssociationRef childs : nodeService.getChildAssocs(nodeRef)) {
                categoryNodeRef = childs.getChildRef();
                if (nodeService.hasAspect(categoryNodeRef, CircabcModel.ASPECT_CATEGORY)) {
                    profiles.addAll(getProfiles(categoryNodeRef));
                }
            }
        }
        return profiles;
    }
}
