/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.history;

import io.swagger.model.OldMembership;
import io.swagger.model.UserRevocationRequest;
import io.swagger.model.db.MemberExpirationDAO;
import io.swagger.model.db.UserPropertyHistoryDAO;
import io.swagger.model.db.UserRevocationRequestDAO;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;

/**
 * Data-access abstraction for CIRCABC history and membership-lifecycle records.
 *
 * <p>Implementations of this interface persist and query the historical trail of
 * user memberships, permissions and profile properties, together with the
 * bookkeeping required for user-revocation processing, permission cleanup and
 * membership-expiration handling. It is the persistence boundary used by the
 * history-related services and scheduled jobs; it contains no business rules and
 * simply reads from and writes to the underlying store.
 *
 * @author beaurpi
 */
public interface HistoryDaoService {
  /**
   * Persists a snapshot of a membership that has been removed, so it can later be
   * recovered or audited.
   *
   * @param oldMembership the membership record to store
   */
  void insertOldMembership(OldMembership oldMembership);

  /**
   * Resolves the group identifier under which a previously removed user can be
   * recovered.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the group to look up
   * @return the recoverable group id associated with the user, or {@code null} if
   *     none is found
   */
  String getGroupIdForRecoverableUser(String userId, String groupId);

  /**
   * Updates the state flag of a stored (old) membership record.
   *
   * @param userId the identifier of the user
   * @param groupId the identifier of the CIRCABC group
   * @param profileId the identifier of the profile the membership refers to
   * @param alfGroupName the underlying Alfresco group name
   * @param i the new state value to set on the record
   */
  void updateStateOldMemebership(
    String userId,
    String groupId,
    String profileId,
    String alfGroupName,
    int i
  );

  /**
   * Stores a snapshot of an access permission that was granted to a user on a
   * node, so it can later be restored or audited.
   *
   * @param acp the access permission being recorded
   * @param userId the identifier of the user holding the permission
   * @param groupNodeRef the reference of the group node the permission relates to
   * @param nodeRef the reference of the node the permission applies to
   */
  void insertOldPermission(
    AccessPermission acp,
    String userId,
    NodeRef groupNodeRef,
    NodeRef nodeRef
  );

  /**
   * Retrieves the historical profile properties previously recorded for a user
   * within the scope of a given group.
   *
   * @param userId the identifier of the user
   * @param groupNodeRef the reference of the group node scoping the lookup
   * @return the list of stored property-history records, possibly empty
   */
  List<UserPropertyHistoryDAO> selectOldProperties(
    String userId,
    NodeRef groupNodeRef
  );

  /**
   * Marks a previously stored user property as restored.
   *
   * @param userId the identifier of the user
   * @param nodeId the identifier of the node the property belongs to
   * @param typeId the identifier of the property type that was restored
   */
  void markPropertyRestored(String userId, String nodeId, Integer typeId);

  /**
   * Records a new user-revocation request for later processing.
   *
   * @param request the revocation request to persist
   */
  void insertUserRevocation(UserRevocationRequest request);

  /**
   * Counts the total number of revocation requests stored.
   *
   * @return the total count of revocation requests
   */
  Integer countTotalRevocations();

  /**
   * Retrieves a paginated list of revocation requests.
   *
   * @param limit the maximum number of records per page
   * @param page the zero-based (or one-based, per implementation) page index
   * @return the list of revocation requests for the requested page
   */
  List<UserRevocationRequestDAO> getRevocations(Integer limit, Integer page);

  /**
   * Retrieves the revocation requests that are still waiting to be processed.
   *
   * @return the list of pending revocation requests, possibly empty
   */
  List<UserRevocationRequestDAO> getWaitingRevocations();

  /**
   * Updates the processing state and job timestamps of a revocation request.
   *
   * @param id the identifier of the revocation request
   * @param jobStartedOn the time at which the processing job started
   * @param jobFinishedOn the time at which the processing job finished
   * @param state the new processing state value
   */
  void updateRevocationJobState(
    Integer id,
    Date jobStartedOn,
    Date jobFinishedOn,
    Integer state
  );

  /**
   * Removes the stored membership-history logs for a user within a group.
   *
   * @param groupId the identifier of the group
   * @param userId the identifier of the user whose logs are cleaned
   */
  void cleanMembershipsLogs(String groupId, String userId);

  /**
   * Retrieves the permission-cleanup requests that are still waiting to be
   * processed.
   *
   * @return the list of pending permission-cleanup requests, possibly empty
   */
  List<UserRevocationRequestDAO> getWaitingCleanPermissions();

  /**
   * Registers a request to clean up permissions for a user.
   *
   * @param request the request describing the permissions to clean
   */
  void registerCleanPermissions(UserRevocationRequest request);

  /**
   * Removes a pending permission-cleanup request for a user on a group node.
   *
   * @param userId the identifier of the user
   * @param groupNodeRef the reference of the group node the request relates to
   */
  void removeWaitingCleanPermissions(String userId, NodeRef groupNodeRef);

  /**
   * Persists a membership-expiration record.
   *
   * @param memberExpiration the expiration record to store
   */
  void insertMemberExpiration(MemberExpirationDAO memberExpiration);

  /**
   * Deletes a stored membership-expiration record.
   *
   * @param memberExpiration the expiration record to remove
   */
  void deleteMemberExpiration(MemberExpirationDAO memberExpiration);

  /**
   * Updates an existing membership-expiration record.
   *
   * @param memberExpiration the expiration record carrying the updated values
   */
  void updateMemberExpiration(MemberExpirationDAO memberExpiration);

  /**
   * Retrieves the membership-expiration records for a given Interest Group.
   *
   * @param id the identifier of the Interest Group
   * @return the list of expiration records for the group, possibly empty
   */
  List<MemberExpirationDAO> getMemberExpirationByIg(String id);

  /**
   * Retrieves all membership-expiration records whose expiration date has passed.
   *
   * @return the list of expired membership records, possibly empty
   */
  List<MemberExpirationDAO> getExpiredMembers();

  /**
   * Counts the membership-expiration entries recorded for a user within an
   * Interest Group.
   *
   * @param userId the identifier of the user
   * @param id the identifier of the Interest Group
   * @return the number of matching expiration entries
   */
  Integer countUserExpirations(String userId, String id);
}
