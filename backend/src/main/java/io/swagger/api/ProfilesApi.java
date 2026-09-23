package io.swagger.api;

import io.swagger.model.Profile;
import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Business-logic contract for managing CIRCABC Interest Group (IG) <em>profiles</em>.
 *
 * <p>A profile is a named role within an Interest Group that bundles together the permission set
 * granted on each of the IG services (directory/members, library, newsgroups, events, information)
 * as well as the visibility permission. Each profile is backed by an Alfresco authority (group)
 * whose members inherit the profile's permissions on the IG's service nodes.
 *
 * <p>Implementations of this interface (see {@code ProfilesApiImpl}) are injected into the CIRCABC
 * REST webscript endpoints and encapsulate the interaction with the Alfresco repository
 * ({@code NodeService}, {@code PermissionService}, {@code AuthorityService}) required to create,
 * read, update, delete, import and synchronize profiles.
 *
 * @author beaurpi
 */
public interface ProfilesApi {
  /**
   * Lists the profiles defined for the given Interest Group, optionally filtered by a search query
   * and/or restricted to profiles that currently contain at least one member.
   *
   * @param id the identifier of the Interest Group node whose profiles are to be listed
   * @param searchQuery an optional case-insensitive substring matched against each profile's name
   *     and localized titles; when {@code null} or empty no name/title filtering is applied
   * @param nonEmptyProfiles when {@code true}, profiles whose backing group has no members are
   *     excluded from the result
   * @return the list of matching {@link Profile} instances (never {@code null}; may be empty)
   */
  List<Profile> groupsIdProfilesGet(
    String id,
    String searchQuery,
    boolean nonEmptyProfiles
  );

  /**
   * Creates a new profile within the given Interest Group and synchronizes it into the CIRCABC
   * database.
   *
   * @param nodeRef the node reference of the Interest Group in which the profile is created
   * @param body the profile definition to create (name, titles and per-service permissions)
   * @return the created {@link Profile}, including its generated identifier and backing group name
   */
  Profile groupsIdProfilesPost(NodeRef nodeRef, Profile body);

  /**
   * Creates a new profile without triggering the CIRCABC synchronization.
   *
   * <p>noSync for the CIRCABC method Interceptor in the case of a IG Creation. Sync should be done
   * at the end of the IG creation. not during Profile creation
   *
   * @param nodeRef the node reference of the Interest Group in which the profile is created
   * @param body the profile definition to create (name, titles and per-service permissions)
   * @return the created {@link Profile}, including its generated identifier and backing group name
   */
  Profile groupsIdProfilesPostNoSync(NodeRef nodeRef, Profile body);

  /**
   * Deletes the given profile and removes it from the CIRCABC database.
   *
   * <p>pass the node ref of the profile to be deleted and return the noderef of the IG to be
   * resynchronized in the DB
   *
   * @param profileRef the node reference of the profile to delete
   * @return the IGRef, i.e. the node reference of the parent Interest Group to be resynchronized
   */
  NodeRef profilesIdDelete(NodeRef profileRef);

  /**
   * Updates an existing profile: its titles, imported/exported flags and per-service permissions,
   * re-applying the corresponding Alfresco permissions and synchronizing the change into the
   * CIRCABC database.
   *
   * @param profileRef the node reference of the profile to update
   * @param body the profile definition carrying the new titles, flags and permissions
   * @return the updated {@link Profile} as re-read from the repository
   */
  Profile profilesIdPut(NodeRef profileRef, Profile body);

  /**
   * Imports an existing profile from a source Interest Group into the target Interest Group,
   * creating a linked, read-only copy with all service permissions initialized to "no access".
   *
   * @param nodeRef the node reference of the target Interest Group receiving the imported profile
   * @param body the source profile to import; its {@code id} identifies the profile to copy
   * @return the newly created imported {@link Profile}
   */
  Profile groupsIdImportedProfilesPost(NodeRef nodeRef, Profile body);

  /**
   * Retrieves a single profile by its identifier.
   *
   * @param profileId the identifier of the profile node to retrieve
   * @return the corresponding {@link Profile}
   */
  Profile profilesIdGet(String profileId);

  /**
   * Returns the set of users invited into the given Interest Group, resolved from the IG's invited
   * users group.
   *
   * @param igNoderef the node reference of the Interest Group
   * @return the set of user authorities invited to the IG (never {@code null}; may be empty)
   */
  Set<String> getInvitedUsers(NodeRef igNoderef);

  /**
   * Resolves the profile a given user is assigned to within a node's Interest Group.
   *
   * @param nodeRef the node reference used to determine the Interest Group context
   * @param userAutority the authority (user name) whose profile is being resolved
   * @return the name of the profile assigned to the user
   */
  String getPersonProfile(NodeRef nodeRef, String userAutority);

  /**
   * Resolves the backing group name of the profile a given user is assigned to within a node's
   * Interest Group.
   *
   * @param nodeRef the node reference used to determine the Interest Group context
   * @param userAutority the authority (user name) whose profile group name is being resolved
   * @return the Alfresco group name backing the user's profile
   */
  String getPersonProfileGroupName(NodeRef nodeRef, String userAutority);
}
