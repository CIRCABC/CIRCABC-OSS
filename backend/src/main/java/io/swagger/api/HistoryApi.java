/**
 *
 */
package io.swagger.api;

import io.swagger.model.*;
import io.swagger.model.db.MemberExpirationDAO;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service contract for tracking and managing the historical lifecycle of
 * Interest Group (IG) memberships and permissions in CIRCABC.
 *
 * <p>Implementations of this interface are responsible for the persistence and
 * business logic behind membership history, including:
 *
 * <ul>
 *   <li><b>Membership logging</b> &mdash; recording former ("old") memberships
 *       when a user leaves or is removed from a group, so that the previous
 *       profile can later be recovered.
 *   <li><b>Recovery</b> &mdash; determining whether a user's former membership,
 *       profile or node properties in a group can be restored, and performing
 *       that restoration.
 *   <li><b>Permission clean-up</b> &mdash; revoking and logging the permissions
 *       a user held on a group's node hierarchy, either immediately or as a
 *       deferred (job-driven) operation.
 *   <li><b>Revocations</b> &mdash; registering, querying and updating the state
 *       of user revocation requests that are processed asynchronously.
 *   <li><b>Membership expiration</b> &mdash; scheduling, updating and removing
 *       expiration dates for memberships, and reporting users whose membership
 *       has expired.
 * </ul>
 *
 * @author beaurpi
 */
public interface HistoryApi {
  /**
   * Records the user's current membership in the given Interest Group as a
   * former ("old") membership, resolving the profile from the current state.
   *
   * @param userId the identifier of the user whose membership is being logged
   * @param igId the identifier of the Interest Group the membership belongs to
   */
  void logOldMembership(String userId, String igId);

  /**
   * Records the supplied former membership entry as-is.
   *
   * @param oldMembership the fully populated former membership to persist
   */
  void logOldMembership(OldMembership oldMembership);

  /**
   * Returns the profile a user previously held in a group, if a recoverable
   * former membership exists.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the group
   * @return the recoverable profile identifier, or {@code null} if none exists
   */
  String getRecoverableProfileInGroup(String userId, String groupId);

  /**
   * Determines whether a user's former membership in a group can be recovered.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the group
   * @return a {@link UserRecoveryOption} describing whether and how the
   *     membership can be recovered
   */
  UserRecoveryOption isRecoverableFromGroup(String userId, String groupId);

  /**
   * Restores a user's membership in a group under the specified profile.
   *
   * @param userId the identifier of the user to re-add
   * @param groupId the identifier of the group
   * @param profileId the profile to assign to the recovered membership
   */
  void recoverMembershipFromGroup(
    String userId,
    String groupId,
    String profileId
  );

  /**
   * Revokes and logs the permissions held by a single user on the given group
   * node hierarchy.
   *
   * @param groupNodeRef the node reference of the group
   * @param userId the identifier of the user whose permissions are cleaned
   */
  void cleanAndLogPermissions(NodeRef groupNodeRef, String userId);

  /**
   * Revokes and logs the permissions held by several users on the given group
   * node hierarchy.
   *
   * @param groupNodeRef the node reference of the group
   * @param userIds the identifiers of the users whose permissions are cleaned
   */
  void cleanAndLogPermissions(NodeRef groupNodeRef, Set<String> userIds);

  /**
   * Revokes and logs the permissions held by several users on the given group
   * node hierarchy, optionally splitting the work into mini-transactions.
   *
   * @param groupNodeRef the node reference of the group
   * @param userIds the identifiers of the users whose permissions are cleaned
   * @param useMiniTransaction {@code true} to process the clean-up in smaller
   *     batched transactions, {@code false} to run it in a single transaction
   */
  void cleanAndLogPermissions(
    NodeRef groupNodeRef,
    Set<String> userIds,
    Boolean useMiniTransaction
  );

  /**
   * Restores the node properties a user previously held in a group from the
   * logged history.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the group
   */
  void recoverPropertiesFromGroup(String userId, String groupId);

  /**
   * Registers a new user revocation request for later (asynchronous)
   * processing.
   *
   * @param request the revocation request to register
   */
  void registerRevocation(UserRevocationRequest request);

  /**
   * Returns a paginated list of registered revocation requests.
   *
   * @param limit the maximum number of requests to return per page
   * @param page the zero- or one-based page index (as defined by the
   *     implementation)
   * @return a paged view of the matching revocation requests
   */
  PagedUserRevocationRequest getRevocations(Integer limit, Integer page);

  /**
   * Returns the revocation requests that are still awaiting processing.
   *
   * @return the list of pending revocation requests
   */
  List<UserRevocationRequest> getWaitingRevocations();

  /**
   * Updates the processing state and job timing information of a revocation
   * request.
   *
   * @param id the identifier of the revocation request to update
   * @param jobStartedOn the timestamp at which the processing job started
   * @param jobFinishedOn the timestamp at which the processing job finished
   * @param state the new state code of the revocation request
   */
  void updateRevocationJobState(
    Integer id,
    Date jobStartedOn,
    Date jobFinishedOn,
    Integer state
  );

  /**
   * Removes the logged membership history entries for a user in a group.
   *
   * @param groupId the identifier of the group
   * @param userId the identifier of the user
   */
  void cleanMembershipsLogs(String groupId, String userId);

  /**
   * Returns the requests that are still awaiting a permission clean-up
   * operation.
   *
   * @return the list of pending clean-permission requests
   */
  List<UserRevocationRequest> getWaitingCleanPermission();

  /**
   * Registers a deferred permission clean-up request for a single user on the
   * given group node.
   *
   * @param groupNodeRef the node reference of the group
   * @param userId the identifier of the user whose permissions will be cleaned
   */
  void registerCleanPermissions(NodeRef groupNodeRef, String userId);

  /**
   * Registers a deferred permission clean-up request for several users on the
   * given group node.
   *
   * @param groupNodeRef the node reference of the group
   * @param userIds the identifiers of the users whose permissions will be
   *     cleaned
   */
  void registerCleanPermissions(NodeRef groupNodeRef, List<String> userIds);

  /**
   * Cancels a pending permission clean-up request previously registered for a
   * user on the given group node.
   *
   * @param userId the identifier of the user
   * @param groupNodeRef the node reference of the group
   */
  void cancelWaitingCleanPermissions(String userId, NodeRef groupNodeRef);

  /**
   * Sets or replaces the expiration date of a user's membership in a group.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the group
   * @param profileId the profile associated with the membership
   * @param alfrescoGroup the underlying Alfresco group name backing the
   *     membership
   * @param expirationDate the date on which the membership should expire
   */
  void addExpirationDate(
    String userId,
    String groupId,
    String profileId,
    String alfrescoGroup,
    Date expirationDate
  );

  /**
   * Removes any expiration date set for a user's membership in a group.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the group
   */
  void deleteExpirationDate(String userId, String groupId);

  /**
   * Returns the users whose membership was automatically expired for the given
   * entity, mapped to the date on which each expiration occurred.
   *
   * @param id the identifier of the group (or related entity) to query
   * @return a map of user identifier to expiration date
   */
  Map<String, Date> getAutoExpiredUsers(String id);

  /**
   * Returns all membership expiration records whose expiration date has been
   * reached.
   *
   * @return the list of expired membership records
   */
  List<MemberExpirationDAO> getExpiredUsers();

  /**
   * Registers one or more membership expiration requests for later processing.
   *
   * @param request the list of membership expiration requests to register
   */
  void registerExpiration(List<UserMembershipsExpirationRequest> request);

  /**
   * Updates the expiration date of an existing membership.
   *
   * @param groupId the identifier of the group
   * @param userId the identifier of the user
   * @param expirationDate the new expiration date to apply
   */
  void updateExpirationDate(String groupId, String userId, Date expirationDate);
}
