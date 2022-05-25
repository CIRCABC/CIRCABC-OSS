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
package eu.cec.digit.circabc.service.profile;

import eu.cec.digit.circabc.repo.applicant.Applicant;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service provides operations to manage information profiles
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface InformationProfileManagerService extends ProfileManagerService {

    String SERVICE_NAME = "informationProfileManagerService";
    String PROXIED_SERVICE_NAME = "InformationProfileManagerService";

    /**
     * Add a user to the list of applicants with additional details and wait the approval of an admin.
     *
     * @param rootNode  the root node to which the user wants to apply to begin a member
     * @param userID    the applicant user username
     * @param message   the message posted by the applicant
     * @param firstName the first name of the applicant
     * @param lastName  the last name of the applicant
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "rootNode",
            "userID",
            "message",
            "firstName",
            "lastName"
    })
    void addApplicantPerson(
            final NodeRef rootNode,
            final String userID,
            final String message,
            final String firstName,
            final String lastName);

    /**
     * Add person to a profile in the rootNode. (Where Profile are stored) An exception is thrown if
     * the person is already member of the rootNode. When a person is added, a profile must be
     * specidied.
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "userID", "profileName"})
    void addPersonToProfile(final NodeRef nodeRef, final String userID, final String profileName)
            throws ProfileException;

    /**
     * Add a new profile called profileName, on node. Role for services are given with profVal.
     * Exception are thrown when profileName already exist * When a permission doesn't exist for a
     * given service.
     *
     * @param profileName         profile name
     * @param nodeRef             root node
     * @param servicesPermissions HashMap containing the coples (service1,permission in
     *                            service),(service2, permission in service)
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "nodeRef",
            "profileName",
            "servicesPermissions"
    })
    void addProfile(
            final NodeRef nodeRef,
            final String profileName,
            final String authorityGroupName,
            final Map<String, Set<String>> servicesPermissions)
            throws ProfileException;

    /**
     * @param nodeRef
     * @param profileName
     * @param newDescriptions
     */
    void addProfileDescriptions(
            final NodeRef nodeRef, final String profileName, final MLText newDescriptions);

    /**
     * @param nodeRef
     * @param profileName
     * @param newTitles
     */
    void addProfileTitles(final NodeRef nodeRef, final String profileName, final MLText newTitles);

    /**
     * Move a person. The person must already be in a profile in the group. Profile must exist in the
     * rootNode. (Where Profile are stored)
     *
     * @param userID      userID of the moved user
     * @param profileName profile name the user is moved to
     * @rootNodeRef root of the Profile
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "userID", "profileName"})
    void changePersonProfile(final NodeRef nodeRef, final String userID, final String profileName)
            throws ProfileException;

    @NotAuditable
    String createInvitedUsersGroup(final NodeRef nodeRef);

    @NotAuditable
    String createMasterGroup(final NodeRef nodeRef);

    @NotAuditable
    String createSubsGroup(final NodeRef nodeRef);

    /**
     * Delete an existing profile. It throws an exception if profileName doesn't exist or if profile
     * still contain users,
     *
     * @param profileName profile name being deleted
     * @param rootNodeRef rootNode the profile belongs to
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "profileName", "cleanPermission"})
    void deleteProfile(final NodeRef nodeRef, final String profileName, final Boolean cleanPermission)
            throws ProfileException;

    /**
     * @param export export or not
     */
    void exportProfile(final NodeRef nodeRef, final String profileName, boolean export);

    /**
     * Return a Set with users that are applicant to this node
     */
    @NotAuditable
    Map<String, Applicant> getApplicantUsers(final NodeRef rootNode);

    /**
     * Return the name of the ApplicantUsersMap associated to the nodeRef.
     */
    @NotAuditable
    String getApplicantUsersMapName(final NodeRef nodeRef);

    /**
     * return the CircaHome node of the given node
     */
    @NotAuditable
    NodeRef getCircaHome(final NodeRef nodeRef);

    /**
     * return the root node of the given node who has aspect defined by the QName
     */
    @NotAuditable
    NodeRef getCurrentAspectRoot(final NodeRef nodeRef);

    /**
     * Get all exported profile (recusively)
     */
    List<Profile> getExportedProfiles(final NodeRef nodeRef);

    /**
     * Return a Set with users that are invited to this node
     */
    @NotAuditable
    Set<String> getInvitedUsers(final NodeRef rootNode);

    @NotAuditable
    Map<String, Profile> getInvitedUsersProfiles(final NodeRef rootNode);

    /**
     * Return all user that are invited in a subService
     */
    @NotAuditable
    Set<String> getAllSubUsers(final NodeRef nodeRef);

    /**
     * Return the name of the InvitedUsersGroup associated to the nodeRef. The group name is not
     * prefixed
     */
    @NotAuditable
    String getInvitedUsersGroupName(final NodeRef nodeRef);

    @NotAuditable
    QName getInvitedUsersGroupQName();

    @NotAuditable
    QName getMasterGroupQName();

    /**
     * Return the name of the MasterInvitedGroup associated to the nodeRef. The group name is not
     * prefixed
     */
    @NotAuditable
    String getMasterInvitedGroupName(final NodeRef nodeRef);

    /**
     * Return a Set with all users that are binded this node
     */
    @NotAuditable
    Set<String> getMasterUsers(final NodeRef rootNode);

    /**
     * Return the person set belonging to that profile
     */
    @NotAuditable
    Set<String> getPersonInProfile(final NodeRef nodeRef, final String profileName);

    /**
     * Get the profile name the person belongs to. Return null if the person doesn't belong to the
     * rootNodeRef.
     *
     * @param userID user id of the person
     * @return profile name profile name
     */
    @NotAuditable
    String getPersonProfile(final NodeRef nodeRef, final String userID);

    /**
     * Fing the Profile associated
     */
    @NotAuditable
    Profile getProfile(final NodeRef nodeRef, final String profileName);

    @NotAuditable
    Profile getProfileFromGroup(final NodeRef nodeRef, final String groupId);

    /**
     * Get Profile binded to the current Node
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef"})
    Map<String, Profile> getProfileMap(final NodeRef nodeRef);

    /**
     * Return the list of Profiles defined on the current node
     */
    @NotAuditable
    List<Profile> getProfiles(final NodeRef nodeRef);

    /**
     * Fing the Profiles recursivly
     */
    @NotAuditable
    List<Profile> getProfilesRecursivly(final NodeRef nodeRef);

    /**
     * @param nodeRef
     * @param profileName
     * @return
     */
    List<NodeRef> getRefInExportedProfile(final NodeRef nodeRef, final String profileName);

    @NotAuditable
    String getRegistredGroupName(final NodeRef nodeRef);

    /**
     * Return a Set with users that are registred to this node
     */
    @NotAuditable
    Set<String> getRegistredUsers(final NodeRef rootNode);

    /**
     * @return the services handled by this node
     */
    @NotAuditable
    Set<String> getServices();

    @NotAuditable
    String getSubsGroupName(final NodeRef nodeRef);

    /**
     * Due to a bug of CGLib that will be corrected in version 1.2 the heritance of interface doesn't
     * work correctly. @TODO Remove duplicate declaration when new version will be used.
     */

    @NotAuditable
    QName getSubsGroupQName();

    @NotAuditable
    boolean hasApplicantFeature();

    @NotAuditable
    boolean hasMasterParentGroup();

    @NotAuditable
    boolean hasParentSubsGroup();

    /**
     * @param toIgNoderef
     * @param fromIgNoderef
     * @param fromProfileName
     */
    void importProfile(
            final NodeRef toIgNoderef,
            final NodeRef fromIgNoderef,
            final String fromProfileName,
            final Map<String, Set<String>> servicesPermissions);

    /**
     * @param nodeRef
     * @param profileName
     * @return
     */
    boolean isProfileDeletable(final NodeRef nodeRef, final String profileName);

    /**
     * Remove a user of the list of applicants of the givent root node
     *
     * @param rootNode the root node from which the user is reject for being a membership
     * @param userName the rejected user username
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"rootNode", "userName"})
    void removeApplicantPerson(final NodeRef rootNode, final String userName);

    /**
     * Rename an existing profile, an exception is thrown when oldName or if oldname already exist. An
     * exception is also thrown if rootNodeRef has not specified aspect
     *
     * @param nodeRef        root node of the profile
     * @param oldProfileName old profile name
     * @param newProfileName new profile name
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "oldProfileName", "newProfileName"})
    void renameProfile(NodeRef nodeRef, final String oldProfileName, final String newProfileName)
            throws ProfileException;

    /**
     * RemovePerson from the interest group. rootNodeRef must be the root (where Profile are stored).
     * user must belong to the rootNodeRef. Clear all specific permission given on any root of the
     * rootNodeRef for that person (recursive)
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "userID", "cleanPermission"})
    void uninvitePerson(final NodeRef nodeRef, final String userID, final Boolean cleanPermission)
            throws ProfileException;

    /**
     * Change the profile. HashMap contain the new profile definition. Exception is also thrown if
     * rootNodeRef is not the rootNode. (Where Profile are stored) Exception is generated when the
     * profile value doesn't exist
     *
     * @param profileName name of the profile to be changed
     * @param profVal     profile values
     * @param rootNodeRef root og the Profile
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "profileName", "profVal"})
    Profile updateProfile(
            final NodeRef nodeRef,
            final String profileName,
            final Map<String, Set<String>> profVal,
            final boolean updateProfileMap)
            throws ProfileException;

    // @NotAuditable
    // public void setAuthenticationService(final AuthenticationService authenticationService);

    // @NotAuditable
    // public void setAuthorityService(final AuthorityService authService);

    // @NotAuditable
    // public void setFileFolderService(final FileFolderService fileFolderService);

    // @NotAuditable
    // public void setNodeService(final NodeService nodeService);

    // @NotAuditable
    // public void setOwnableService(final OwnableService ownableService);

    // @NotAuditable
    // public void setPermissionService(final PermissionService permissionService);

    // @NotAuditable
    // public void setServiceRegistry(final ServiceRegistry serviceRegistry);

    interface Profiles extends IGRootProfileManagerService.Profiles {
        // Nothing new in this place at moment
    }

    interface Roles extends IGRootProfileManagerService.Roles {
        // Nothing new in this place at moment
    }
}
