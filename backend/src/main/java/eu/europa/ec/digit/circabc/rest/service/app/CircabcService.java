package eu.europa.ec.digit.circabc.rest.service.app;

import io.swagger.model.Profile;
import io.swagger.model.UserCategoryMembershipRecord;
import io.swagger.model.UserIGMembershipRecord;
import io.swagger.model.db.IGData;
import io.swagger.model.db.InterestGroupItem;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.db.UserWithProfile;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Core business service for CIRCABC's organizational and membership domain.
 *
 * <p>This service centralizes the operations that manage the CIRCABC hierarchy
 * (Headers &rarr; Categories &rarr; Interest Groups) together with the users,
 * profiles and memberships attached to those structures. It typically keeps the
 * relational database projection of this hierarchy in sync with the underlying
 * Alfresco repository, so many methods both mutate Alfresco nodes and update the
 * corresponding database records.
 *
 * <p>Alfresco nodes are referenced through {@link NodeRef}, while users are
 * identified by their user name ({@code String}). Implementations are expected
 * to be invoked from the REST/web-script layer and from scheduled jobs.
 */
public interface CircabcService {
  /**
   * Removes category-administrator rights from a user for the given category.
   *
   * @param categoryNodeRef the {@link NodeRef} of the category
   * @param userName the user name of the administrator to remove
   */
  void removeCategoryAdmin(NodeRef categoryNodeRef, String userName);

  /**
   * Assigns a user to a membership profile within an interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name of the person to add
   * @param profileName the name of the profile to assign the user to
   */
  void addPersonToProfile(
    NodeRef igNodeRef,
    String userName,
    String profileName
  );

  /**
   * Returns the profile name a user currently holds in an interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name to look up
   * @return the profile name, or {@code null} if the user has no profile in the group
   */
  String getPersonProfile(NodeRef igNodeRef, String userName);

  /**
   * Returns the Alfresco group name backing the user's profile in an interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name to look up
   * @return the Alfresco group name associated with the user's profile
   */
  String getPersonProfileGroupName(NodeRef igNodeRef, String userName);

  /**
   * Moves a user from their current profile to a different profile within an
   * interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name whose profile is changing
   * @param profileName the name of the target profile
   */
  void changePersonProfile(
    NodeRef igNodeRef,
    String userName,
    String profileName
  );

  /**
   * Deletes a membership profile from an interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param profileName the name of the profile to delete
   */
  void deleteProfile(NodeRef igNodeRef, String profileName);

  /**
   * Recomputes and updates the "can apply for membership" flag of an interest
   * group based on its current configuration.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   */
  void updateCanApplyForMemberhip(NodeRef igNodeRef);

  /**
   * Updates the definition of a membership profile within an interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param profileName the name of the profile to update
   * @param profile the new profile definition
   */
  void updateProfile(NodeRef igNodeRef, String profileName, Profile profile);

  /**
   * Registers a category node under a header, creating its database projection.
   *
   * @param headerNodeRef the {@link NodeRef} of the parent header
   * @param categoryNodeRef the {@link NodeRef} of the category to add
   */
  void addCategoryNode(NodeRef headerNodeRef, NodeRef categoryNodeRef);

  /**
   * Registers a user identified by their person node in the CIRCABC database.
   *
   * @param userNodeRef the {@link NodeRef} of the person node to add
   */
  void addUser(NodeRef userNodeRef);

  /**
   * Synchronizes the stored properties of an interest group with its current
   * Alfresco node state.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   */
  void updateIntestGroupProperties(NodeRef igNodeRef);

  /**
   * Synchronizes the stored properties of a category with its current Alfresco
   * node state.
   *
   * @param catNodeRef the {@link NodeRef} of the category
   */
  void updateCategoryProperties(NodeRef catNodeRef);

  /**
   * Deletes a category and its database projection.
   *
   * @param catNodeRef the {@link NodeRef} of the category to delete
   */
  void deleteCategory(NodeRef catNodeRef);

  /**
   * Deletes an interest group identified by its Alfresco node.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group to delete
   */
  void deleteIntestGroup(NodeRef igNodeRef);

  /**
   * Deletes an interest group identified by its database identifier.
   *
   * @param interestGroupID the database identifier of the interest group to delete
   */
  void deleteIntestGroupByID(Long interestGroupID);

  /**
   * Registers a header node, creating its database projection.
   *
   * @param headerNodeRef the {@link NodeRef} of the header to add
   */
  void addHeaderNode(NodeRef headerNodeRef);

  /**
   * Deletes a header and its database projection.
   *
   * @param nodeRef the {@link NodeRef} of the header to delete
   */
  void deleteHeader(NodeRef nodeRef);

  /**
   * Lists the interest groups of a category that are visible to a given user.
   *
   * @param categoryRef the {@link NodeRef} of the category
   * @param userName the user name for which visibility is evaluated
   * @return the list of matching {@link InterestGroupItem} entries
   */
  List<InterestGroupItem> getInterestGroupByCategoryUser(
    NodeRef categoryRef,
    String userName
  );

  /**
   * Returns the interest-group memberships of a user.
   *
   * @param userName the user name to query
   * @return the list of {@link UserIGMembershipRecord} for the user
   */
  List<UserIGMembershipRecord> getInterestGroups(String userName);

  /**
   * Returns the category memberships of a user.
   *
   * @param userName the user name to query
   * @return the list of {@link UserCategoryMembershipRecord} for the user
   */
  List<UserCategoryMembershipRecord> getCategories(String userName);

  /**
   * Synchronizes the stored properties of a user with their current Alfresco
   * person node state.
   *
   * @param userNodeRef the {@link NodeRef} of the person node to update
   */
  void updateUser(NodeRef userNodeRef);

  /**
   * Re-synchronizes an interest group's database projection with the Alfresco
   * repository, rebuilding derived data as needed.
   *
   * @param nodeRef the {@link NodeRef} of the interest group to resync
   */
  void resyncInterestGroup(NodeRef nodeRef);

  /**
   * Returns the identifier of a user's preferred locale.
   *
   * @param userName the user name to query
   * @return the locale identifier, or {@code null} if none is set
   */
  Long getUserLocaleID(String userName);

  /**
   * Searches the members of an interest group using a single free-text term.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param localeID the locale identifier used to resolve localized values
   * @param alfrescoGroup the Alfresco group (profile) to restrict the search to
   * @param searchText the free-text term matched against user attributes
   * @return the list of matching {@link UserWithProfile} entries
   */
  List<UserWithProfile> getFilteredUsers(
    NodeRef igNodeRef,
    long localeID,
    String alfrescoGroup,
    String searchText
  );

  /**
   * Searches the members of an interest group using a free-text term, with an
   * explicit sort order.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param localeID the locale identifier used to resolve localized values
   * @param alfrescoGroup the Alfresco group (profile) to restrict the search to
   * @param searchText the free-text term matched against user attributes
   * @param order the sort order to apply to the results
   * @return the list of matching {@link UserWithProfile} entries
   */
  List<UserWithProfile> getFilteredUsers(
    NodeRef igNodeRef,
    long localeID,
    String alfrescoGroup,
    String searchText,
    String order
  );

  /**
   * Searches the members of an interest group using individual field filters,
   * with an explicit sort order.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param localeID the locale identifier used to resolve localized values
   * @param alfrescoGroup the Alfresco group (profile) to restrict the search to
   * @param firstName the first-name filter
   * @param lastName the last-name filter
   * @param email the email filter
   * @param order the sort order to apply to the results
   * @return the list of matching {@link UserWithProfile} entries
   */
  List<UserWithProfile> getFilteredUsers(
    NodeRef igNodeRef,
    long localeID,
    String alfrescoGroup,
    String firstName,
    String lastName,
    String email,
    String order
  );

  /**
   * Indicates whether a user is a member of an interest group.
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name to check
   * @return {@code true} if the user is a member, {@code false} otherwise
   */
  boolean isUserMember(NodeRef igNodeRef, String userName);

  /**
   * Indicates whether a user is an administrator of a category.
   *
   * @param categoryNodeRef the {@link NodeRef} of the category
   * @param userName the user name to check
   * @return {@code true} if the user administers the category, {@code false} otherwise
   */
  boolean isCategoryAdmin(NodeRef categoryNodeRef, String userName);

  /**
   * Indicates whether a user is an administrator of the category owning the
   * given interest group.
   *
   * @param interestGroupNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name to check
   * @return {@code true} if the user administers the parent category, {@code false} otherwise
   */
  boolean isCategoryAdminOfInterestGroup(
    NodeRef interestGroupNodeRef,
    String userName
  );

  /**
   * Indicates whether a user has global CIRCABC administrator rights.
   *
   * @param userName the user name to check
   * @return {@code true} if the user is a CIRCABC administrator, {@code false} otherwise
   */
  boolean isCircabcAdmin(String userName);

  /**
   * Grants global CIRCABC administrator rights to a user.
   *
   * @param userName the user name to promote
   */
  void addCircabcAdmin(String userName);

  /**
   * Revokes global CIRCABC administrator rights from a user.
   *
   * @param userName the user name to demote
   */
  void removeCircabcAdmin(String userName);

  /**
   * Retrieves the full detail projection of an interest group.
   *
   * @param interestGroupNodeRef the {@link NodeRef} of the interest group
   * @return the {@link InterestGroupResult} describing the interest group
   */
  InterestGroupResult getInterestGroup(NodeRef interestGroupNodeRef);

  /**
   * Returns the user names of the administrators of a category.
   *
   * @param categoryNodeRef the {@link NodeRef} of the category
   * @return the list of administrator user names
   */
  List<String> getCategoryAdmins(NodeRef categoryNodeRef);

  /**
   * Removes a user from an interest group entirely (all profiles/groups).
   *
   * @param igNodeRef the {@link NodeRef} of the interest group
   * @param userName the user name to remove
   */
  void deletePersonFromGroup(NodeRef igNodeRef, String userName);

  /**
   * Returns the localized titles of an interest group.
   *
   * @param igRef the {@link NodeRef} of the interest group
   * @return a map of locale code to title
   */
  Map<String, String> getInterestGroupTitle(NodeRef igRef);

  /**
   * Returns the localized titles of a profile.
   *
   * @param profileId the {@link NodeRef} of the profile
   * @return a map of locale code to title
   */
  Map<String, String> getProfileTitle(NodeRef profileId);

  /**
   * Returns the localized titles of a category.
   *
   * @param categRef the {@link NodeRef} of the category
   * @return a map of locale code to title
   */
  Map<String, String> getCategoryTitle(NodeRef categRef);

  /**
   * Indicates whether a user account exists.
   *
   * @param userName the user name to check
   * @return {@code true} if the user exists, {@code false} otherwise
   */
  boolean isUserExists(String userName);

  /**
   * Enables or disables public (guest) access to an interest group.
   *
   * @param igRef the {@link NodeRef} of the interest group
   * @param isGuest {@code true} to allow guest access, {@code false} to disallow it
   */
  void updateInterestGroupPublic(NodeRef igRef, Boolean isGuest);

  /**
   * Enables or disables access for registered users on an interest group.
   *
   * @param igRef the {@link NodeRef} of the interest group
   * @param isRegistered {@code true} to allow registered-user access, {@code false} to disallow it
   */
  void updateInterestGroupRegistered(NodeRef igRef, Boolean isRegistered);

  /**
   * Enables or disables the ability to apply for membership on an interest group.
   *
   * @param igRef the {@link NodeRef} of the interest group
   * @param applicationAllowed {@code true} to allow membership applications, {@code false} to disallow them
   */
  void updateInterestGroupApplication(
    NodeRef igRef,
    Boolean applicationAllowed
  );

  /**
   * Deletes all CIRCABC database records managed by this service.
   *
   * <p>This is a destructive, bulk operation intended for full re-initialization.
   */
  void deleteAll();

  /**
   * Counts the members of an interest group.
   *
   * @param id the identifier of the interest group
   * @return the number of members in the interest group
   */
  int countMembersInIg(String id);

  /**
   * Returns the set of user identifiers that belong to an interest group.
   *
   * @param interestGroupId the identifier of the interest group
   * @return the set of user identifiers
   */
  Set<String> getUserIds(String interestGroupId);

  /**
   * Registers a user identified by their user name in the CIRCABC database.
   *
   * @param userName the user name to add
   */
  void addUser(String userName);

  /**
   * Indicates whether a user holds any administrative role, i.e. directory
   * administrator, category administrator or CIRCABC administrator.
   *
   * @param userName the user name to check
   * @return {@code true} if the user holds any of these roles, {@code false} otherwise
   */
  boolean isUserDirAdminOrCategoryAdminOrCircabcAdmin(String userName);

  /**
   * Indicates whether a user is an external (non-internal) user.
   *
   * @param userName the user name to check
   * @return {@code true} if the user is external, {@code false} otherwise
   */
  boolean isExternalUser(String userName);

  /**
   * Lists the interest groups of a category, excluding the current one.
   *
   * @param categoryNodeRef the {@link NodeRef} of the category
   * @param currentIgNodeRef the {@link NodeRef} of the interest group to exclude
   * @param username the user name for which the listing is evaluated
   * @return the list of matching {@link IGData} entries
   */
  List<IGData> getCategoryIGsExceptCurrent(
    NodeRef categoryNodeRef,
    NodeRef currentIgNodeRef,
    String username
  );

  /**
   * Lists the interest groups of a category (excluding the current one) that
   * the given user can access through the directory.
   *
   * @param categoryNodeRef the {@link NodeRef} of the category
   * @param currentIgNodeRef the {@link NodeRef} of the interest group to exclude
   * @param username the user name for which directory access is evaluated
   * @return the list of matching {@link IGData} entries
   */
  List<IGData> getCategoryIGsExceptCurrentWithDirectoryAccess(
    NodeRef categoryNodeRef,
    NodeRef currentIgNodeRef,
    String username
  );

  /**
   * Returns the email addresses of a category's administrators.
   *
   * @param categoryRef the {@link NodeRef} of the category
   * @return the list of administrator email addresses
   */
  List<String> getCategoryAdminEmails(NodeRef categoryRef);

  /**
   * Returns the email addresses of an interest group's administrators.
   *
   * @param interestGroupRef the {@link NodeRef} of the interest group
   * @return the list of administrator email addresses
   */
  List<String> getInterestGroupAdminEmails(NodeRef interestGroupRef);

  /**
   * Returns all category nodes known to CIRCABC.
   *
   * @return the list of category {@link NodeRef}s
   */
  List<NodeRef> getCategories();

  /**
   * Deletes a user's record from the CIRCABC database.
   *
   * @param userName the user name to delete
   */
  void deleteUserFromDatabase(String userName);
}
