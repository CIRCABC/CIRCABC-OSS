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
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This service provides operations to manage profiles
 *
 * @author Clinckart Stephane
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface ProfileManagerService {

    String ALL_CIRCA_USERS_PROFILE_NAME = "EVERYONE";
    String SUPPORT_GROUP_NAME = "CIRCABC_SUPPORT";

    @NotAuditable
    String getRegistredGroupName(final NodeRef nodeRef);

    @NotAuditable
    boolean hasApplicantFeature();

    @NotAuditable
    boolean hasMasterParentGroup();

    @NotAuditable
    boolean hasParentSubsGroup();

    @NotAuditable
    void setOwnableService(final OwnableService ownableService);

    @NotAuditable
    void setNodeService(final NodeService nodeService);

    @NotAuditable
    void setFileFolderService(final FileFolderService fileFolderService);

    @NotAuditable
    void setAuthorityService(final AuthorityService authService);

    @NotAuditable
    void setPermissionService(final PermissionService permissionService);

    @NotAuditable
    void setProfileManagerServiceFactory(
            final ProfileManagerServiceFactory profileManagerServiceFactory);

    @NotAuditable
    void setProfileMapCache(final SimpleCache<String, Map<String, Profile>> profileMapCache);

    @NotAuditable
    ServiceRegistry getServiceRegistry();

    @NotAuditable
    void setServiceRegistry(final ServiceRegistry serviceRegistry);

    @NotAuditable
    void setManagementService(final ManagementService managementService);

    /**
     * Setter for the authentification Service
     *
     * @param authenticationService The AuthenticationService to set.
     */
    @NotAuditable
    void setAuthenticationService(final MutableAuthenticationService authenticationService);

    /**
     * @return the services handled by this node
     */
    @NotAuditable
    Set<String> getServices();

    /**
     * Return a Set with all users that are binded this node
     */
    @NotAuditable
    Set<String> getMasterUsers(final NodeRef rootNode);

    /**
     * Return a Set with users that are invited to this node
     */
    @NotAuditable
    Set<String> getInvitedUsers(final NodeRef rootNode);

    /**
     * Return a Map with users that are invited to this node and their Profiles
     */
    @NotAuditable
    Map<String, Profile> getInvitedUsersProfiles(final NodeRef rootNode);

    /**
     * Return all user that are invited in a subService
     */
    @NotAuditable
    Set<String> getAllSubUsers(final NodeRef nodeRef);

    /**
     * Return a Set with users that are registred to this node
     */
    @NotAuditable
    Set<String> getRegistredUsers(final NodeRef rootNode);

    /**
     * Return a Set with users that are applicant to this node
     */
    @NotAuditable
    Map<String, Applicant> getApplicantUsers(final NodeRef rootNode);

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
     * Add a user to the list of applicants with additional details and wait the approval of an admin.
     *
     * @param rootNode  the root node to which the user wants to apply to begin a member
     * @param userID    the applicant user username
     * @param message   the message posted by the applicant
     * @param firstName the first name of the applicant
     * @param lastName  the last name of the applicant
     * @param date      the date of the application
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
            final String lastName,
            final Date date);

    /**
     * Remove a user of the list of applicants of the givent root node
     *
     * @param rootNode the root node from which the user is reject for being a membership
     * @param userName the rejected user username
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"rootNode", "userName"})
    void removeApplicantPerson(final NodeRef rootNode, final String userName);

    /**
     * Add a new profile called profileName, on node. Role for services are given with profVal.
     * Exception are thrown when profileName already exist * When a permission doesn't exist for a
     * given service.
     *
     * @param profileName profile name
     * @param nodeRef     root node
     * @param profVal     HashMap containing the coples (service1,permission in service),(service2,
     *                    permission in service)
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "profileName", "profVal"})
    void addProfile(
            final NodeRef nodeRef, final String profileName, final Map<String, Set<String>> profVal)
            throws ProfileException;

    /**
     * Add a new profile called profileName, on node. Role for services are given with profVal.
     * Exception are thrown when profileName already exist * When a permission doesn't exist for a
     * given service.
     *
     * @param profileName        profile name
     * @param authorityGroupName alfresco group binded to the profile (optional)
     * @param nodeRef            root node
     * @param title              profile title (multilingual)
     * @param description        profile description (multilingual)
     * @param profVal            HashMap containing the coples (service1,permission in service),(service2,
     *                           permission in service)
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "nodeRef",
            "profileName",
            "title",
            "description",
            "profVal"
    })
    void addProfile(
            final NodeRef nodeRef,
            final String profileName,
            final String authorityGroupName,
            final MLText title,
            final MLText description,
            final Map<String, Set<String>> servicesPermissions)
            throws ProfileException;

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
     * Rename an existing profile, an exception is thrown when oldName or if oldname already exist. An
     * exception is also thrown if rootNodeRef has not specified aspect
     *
     * @param nodeRef        root node of the profile
     * @param oldProfileName old profile name
     * @param newProfileName new profile name
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "oldProfileName", "newProfileName"})
    void renameProfile(
            final NodeRef nodeRef, final String oldProfileName, final String newProfileName)
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
            boolean updateProfileMap)
            throws ProfileException;

    /**
     * Add person to a profile in the rootNode. (Where Profile are stored) An exception is thrown if
     * the person is already member of the rootNode. When a person is added, a profile must be
     * specidied.
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "userID", "profileName"})
    void addPersonToProfile(final NodeRef nodeRef, final String userID, final String profileName)
            throws ProfileException;

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

    /**
     * RemovePerson from the interest group. rootNodeRef must be the root (where Profile are stored).
     * user must belong to the rootNodeRef. Clear all specific permission given on any root of the
     * rootNodeRef for that person (recursive)
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"nodeRef", "userID", "cleanPermission"})
    void uninvitePerson(final NodeRef nodeRef, final String userID, final Boolean cleanPermission)
            throws ProfileException;

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
     * Return the profile map. The keys in the first map are the profile names for the rootNodeRef
     * (rows of the array). Then the sub element contain the permission per service.
     *
     * @return HashMap<String ( Profile ), HashMap < String ( Service ), List < String>
     * (Permission)>>
     */
    @NotAuditable
    Map<String, Profile> getProfileMap(final NodeRef nodeRef);

    /**
     * Return the person set belonging to that profile
     */
    @NotAuditable
    Set<String> getPersonInProfile(final NodeRef nodeRef, final String profileName);

    /**
     * return the root node of the given node who has aspect defined by the QName
     */
    @NotAuditable
    NodeRef getCurrentAspectRoot(final NodeRef nodeRef);

    /**
     * return the CircaHome node of the given node
     */
    @NotAuditable
    NodeRef getCircaHome(final NodeRef nodeRef);

    /**
     * Fing the Profile to the profile definition
     */
    @NotAuditable
    Profile getProfile(final NodeRef nodeRef, final String profileName);

    /**
     * @param nodeRef
     * @param profileName
     * @return
     */
    @NotAuditable
    Profile getProfileNoCache(final NodeRef nodeRef, final String profileName);

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
     * Return the name of the MasterInvitedGroup associated to the nodeRef. The group name is not
     * prefixed
     */
    @NotAuditable
    String getMasterInvitedGroupName(final NodeRef nodeRef);

    /**
     * Return the name of the InvitedUsersGroup associated to the nodeRef. The group name is not
     * prefixed
     */
    @NotAuditable
    String getInvitedUsersGroupName(final NodeRef nodeRef);

    /**
     * Return the name of the ApplicantUsersMap associated to the nodeRef.
     */
    @NotAuditable
    String getApplicantUsersMapName(final NodeRef nodeRef);

    @NotAuditable
    String createMasterGroup(final NodeRef nodeRef);

    @NotAuditable
    String createInvitedUsersGroup(final NodeRef nodeRef);

    @NotAuditable
    String createSubsGroup(final NodeRef nodeRef);

    @NotAuditable
    void updateProfileMap(final NodeRef nodeRef, final Profile newProfile);

    @NotAuditable
    QName getMasterGroupQName();

    @NotAuditable
    QName getInvitedUsersGroupQName();

    @NotAuditable
    QName getSubsGroupQName();

    @NotAuditable
    String getSubsGroupName(final NodeRef nodeRef);

    @NotAuditable
    Profile getProfileFromGroup(final NodeRef nodeRef, final String groupId);

    /**
     * Get all exported profiles (recusively)
     */
    List<Profile> getExportedProfiles(final NodeRef nodeRef);

    /**
     * @param toIgNoderef
     * @param fromIgNoderef
     * @param fromProfileName
     * @param servicesPermissions
     * @return
     */
    void importProfile(
            final NodeRef toIgNoderef,
            final NodeRef fromIgNoderef,
            final String fromProfileName,
            final Map<String, Set<String>> servicesPermissions);

    /**
     * @param export export or not
     */
    void exportProfile(final NodeRef nodeRef, final String profileName, boolean export);

    /**
     * @param nodeRef
     * @param profileName
     * @param newTitles
     */
    void addProfileTitles(final NodeRef nodeRef, final String profileName, final MLText newTitles);

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
     * @return
     */
    boolean isProfileDeletable(final NodeRef nodeRef, final String profileName);

    /**
     * @param nodeRef
     * @param profileName
     * @return
     */
    List<NodeRef> getRefInExportedProfile(final NodeRef nodeRef, final String profileName);
}
