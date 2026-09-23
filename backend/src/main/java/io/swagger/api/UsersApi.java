package io.swagger.api;

import io.swagger.model.*;
import io.swagger.model.db.IGData;
import java.io.InputStream;
import java.util.List;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Business-logic contract for user management operations exposed by the CIRCABC REST layer.
 *
 * <p>Implementations of this interface encapsulate the logic behind the {@code /users} REST
 * endpoints: retrieving, creating, updating and deleting Alfresco user accounts, resolving a
 * user's interest-group memberships and categories, managing user avatars and preferences, and
 * driving the bulk-invitation workflow used to import members into interest groups.
 *
 * <p>The interface is typically injected (via {@code @Autowired}) into the corresponding
 * webscript endpoint classes, which adapt HTTP requests to these operations and render the
 * results through FreeMarker templates.
 */
public interface UsersApi {
  /**
   * Retrieves the list of users / Alfresco accounts, optionally restricted by a search query.
   *
   * @param query the search term used to look up users
   * @param filter whether the result set should be filtered according to the current context
   * @param matchQuery whether the query must be matched exactly rather than as a partial match
   * @return the list of matching {@link User} accounts
   */
  List<User> usersGet(String query, boolean filter, boolean matchQuery);

  /**
   * Creates a new user.
   *
   * @param body the {@link User} details of the account to create
   */
  void usersPost(User body);

  /**
   * Retrieves a single user by identifier.
   *
   * @param userId the identifier of the user to retrieve
   * @return the matching {@link User}
   */
  User usersUserIdGet(String userId);

  /**
   * Retrieves user details from the user database (LDAP-backed source).
   *
   * @param userId the identifier of the user to look up
   * @return the {@link User} details resolved from the user db
   */
  User usersUserIdGetFromLdap(String userId);

  /**
   * Updates an existing user.
   *
   * @param userId the identifier of the user to update
   * @param body the new {@link User} details to apply
   * @return the updated {@link User}
   */
  User usersUserIdPut(String userId, User body);

  /**
   * Retrieves the interest-group memberships of a user.
   *
   * @param userId the identifier of the user whose memberships are requested
   * @return the list of {@link InterestGroupProfile} entries describing the user's memberships
   */
  List<InterestGroupProfile> getUserMembership(String userId);

  /**
   * Retrieves the interest-group memberships of a user, optionally in a reduced (light) form.
   *
   * @param userId the identifier of the user whose memberships are requested
   * @param lightMode when {@code true}, returns a lightweight representation with fewer details
   * @return the list of {@link InterestGroupProfile} entries describing the user's memberships
   */
  List<InterestGroupProfile> getUserMembership(
    String userId,
    Boolean lightMode
  );

  /**
   * Retrieves the categories associated with a user.
   *
   * @param userId the identifier of the user whose categories are requested
   * @return the list of {@link Category} entries the user is associated with
   */
  List<Category> getUserCategories(String userId);

  /**
   * Deletes the avatar of a user.
   *
   * <p>Backs {@code DELETE /users/{userId}/avatar}.
   *
   * @param userId the identifier of the user whose avatar should be removed
   */
  // Delete the users avatar
  // DELETE /users/{userId}/avatar
  void removeAvatar(String userId);

  /**
   * Updates (uploads) the avatar of a user.
   *
   * <p>Backs {@code PUT /users/{userId}/avatar}.
   *
   * @param userId the identifier of the user whose avatar should be updated
   * @param imageInputStream the input stream containing the avatar image data
   * @param fileName the original file name of the uploaded avatar image
   */
  // Update the users avatar
  // PUT /users/{userId}/avatar
  void updateAvatar(
    String userId,
    InputStream imageInputStream,
    String fileName
  );

  /**
   * Writes the bulk-invitation template (e.g. a spreadsheet skeleton) directly to the response.
   *
   * @param response the {@link WebScriptResponse} to which the template content is streamed
   */
  void writeBulkInviteTemplate(WebScriptResponse response);

  /**
   * Retrieves the categories available to a user when initiating a bulk invitation.
   *
   * @param username the login name of the user performing the bulk invitation
   * @return the list of {@link Category} entries eligible for bulk invitation
   */
  List<Category> getBulkInviteCategories(String username);

  /**
   * Retrieves the interest groups eligible as sources for a bulk invitation.
   *
   * @param categoryId current category
   * @param currentIgId current interest group
   * @return if current user is category admin return all interest groups except the current one
   *         else return only the interest groups in category where current user has Directory access except the current one
   *         rights
   */
  List<IGData> getBulkInviteIGs(String categoryId, String currentIgId);

  /**
   * Retrieves the members of the given source interest groups that can be bulk-invited into a
   * destination interest group.
   *
   * @param igIds the identifiers of the source interest groups to read members from
   * @param destinationIGId the identifier of the interest group the members would be invited into
   * @return the list of {@link BulkImportUserData} candidates for the bulk invitation
   */
  List<BulkImportUserData> getBulkInviteMembers(
    List<String> igIds,
    String destinationIGId
  );

  /**
   * Performs a bulk invitation of users into an interest group.
   *
   * @param bulkInviteDataJson the JSON payload describing the users to invite and their profiles
   * @param igId the identifier of the target interest group
   * @param createNewProfiles whether missing user profiles should be created during the invitation
   * @param notifyUsers whether the invited users should be notified (e.g. by email)
   */
  void bulkInviteUsers(
    String bulkInviteDataJson,
    String igId,
    boolean createNewProfiles,
    boolean notifyUsers
  );

  /**
   * Parses an uploaded bulk-invitation file and produces the digest of users it contains for a
   * target interest group.
   *
   * @param igId the identifier of the target interest group
   * @param inputStream the input stream of the uploaded bulk-invitation file
   * @param fileName the original file name of the uploaded file
   * @return the list of {@link BulkImportUserData} parsed from the file
   */
  List<BulkImportUserData> bulkInviteUsersDigestFile(
    String igId,
    InputStream inputStream,
    String fileName
  );

  /**
   * Resolves and enriches a list of users, returning their full account details.
   *
   * @param users the users to resolve (typically identified by a partial representation)
   * @return the list of fully resolved {@link User} accounts
   */
  List<User> retrieveUserList(List<User> users);

  /**
   * Saves the preference configuration for a user.
   *
   * @param username the login name of the user whose preference is being saved
   * @param preference the serialized preference configuration to persist
   */
  void saveUserPreferenceConfiguration(String username, String preference);

  /**
   * Retrieves the preference configuration for a user.
   *
   * @param username the login name of the user whose preferences are requested
   * @return the {@link PreferenceConfiguration} for the user
   */
  PreferenceConfiguration getUserPreference(String username);

  /**
   * Deletes a user from Alfresco and the CIRCABC {@code cbc_} database tables.
   *
   * @param userId the identifier of the user to delete
   */
  void usersUserIdDelete(String userId);
}
