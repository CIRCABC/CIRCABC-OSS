/**
 *
 */
package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.history.HistoryDaoService;
import io.swagger.model.*;
import io.swagger.model.db.MemberExpirationDAO;
import io.swagger.model.db.UserPropertyHistoryDAO;
import io.swagger.model.db.UserRevocationRequestDAO;
import io.swagger.util.Converter;
import jakarta.transaction.*;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link HistoryApi}.
 *
 * <p>This service centralises the historical bookkeeping performed by CIRCABC
 * when memberships and permissions change over time. It is responsible for:
 *
 * <ul>
 *   <li>Logging former memberships when a user is removed from an Interest
 *       Group, so the membership can later be recovered.</li>
 *   <li>Capturing and removing per-node permissions for revoked users
 *       ("clean permission" processing), and restoring them on recovery.</li>
 *   <li>Recording, querying and updating user revocation requests processed by
 *       the scheduled revocation jobs.</li>
 *   <li>Managing membership expiration dates (adding, updating, deleting and
 *       querying expired members).</li>
 * </ul>
 *
 * <p>Persistence is delegated to {@link HistoryDaoService}, while membership,
 * user and profile operations are delegated to the corresponding CIRCABC API
 * beans. Alfresco {@code NodeService} and {@code PermissionService} are used to
 * traverse the repository and manipulate node permissions.
 *
 * @author beaurpi
 */
public class HistoryApiImpl implements HistoryApi {

  /** Logger used to report failures while cleaning permissions. */
  private static final Log logger = LogFactory.getLog(HistoryApiImpl.class);

  /** API used to resolve Interest Group members and to (re)create memberships. */
  @Autowired
  private GroupsApi groupsApi;

  /** API used to resolve user details by user id. */
  @Autowired
  private UsersApi usersApi;

  /** API used to resolve profile details by profile id. */
  @Autowired
  private ProfilesApi profilesApi;

  /** Alfresco service used to traverse nodes and inspect their types. */
  @Autowired
  private NodeService nodeService;

  /** Alfresco service used to read, set and delete node permissions. */
  @Autowired
  private PermissionService permissionService;

  /** DAO service that persists and retrieves history records. */
  @Autowired
  private HistoryDaoService historyDaoService;

  /** Alfresco service used to check whether a person node exists. */
  @Autowired
  private PersonService personService;

  /** Alfresco service used to resolve the current authenticated user. */
  @Autowired
  private AuthenticationService authenticationService;

  /** Alfresco service used to run permission cleanup in isolated transactions. */
  @Autowired
  private TransactionService transactionService;

  /**
   * Persists a fully populated former-membership record.
   *
   * @param oldMembership the former membership to store
   */
  @Override
  public void logOldMembership(OldMembership oldMembership) {
    historyDaoService.insertOldMembership(oldMembership);
  }

  /**
   * Builds and persists a former-membership record for a user in a group.
   *
   * <p>The user's current membership is resolved from the Interest Group; if a
   * single matching membership is found, its profile information is captured
   * and stored with state {@code 0} (recoverable). If no unique membership is
   * found, nothing is logged.
   *
   * @param userId the id of the user whose membership is being logged
   * @param igId the id of the Interest Group the user belonged to
   */
  @Override
  public void logOldMembership(String userId, String igId) {
    OldMembership oldMembership = new OldMembership();
    PagedUserProfile users = groupsApi.groupsIdMembersGet(
      igId,
      null,
      "en",
      null,
      null,
      null,
      userId
    );

    if (users.getTotal() == 1) {
      oldMembership.setUserId(userId);
      oldMembership.setGroupId(igId);
      oldMembership.setProfileId(users.getData().get(0).getProfile().getId());
      oldMembership.setAlfGroupName(
        users.getData().get(0).getProfile().getGroupName()
      );
      oldMembership.setState(0);
      oldMembership.setStateDate(new Date());
      logOldMembership(oldMembership);
    }
  }

  /**
   * Returns the profile id under which a user can be recovered in a group.
   *
   * @param userId the id of the user to check
   * @param groupId the id of the group to check
   * @return the recoverable profile id, or {@code null} if the user is not
   *     recoverable in that group
   */
  @Override
  public String getRecoverableProfileInGroup(String userId, String groupId) {
    return historyDaoService.getGroupIdForRecoverableUser(userId, groupId);
  }

  /**
   * Determines whether a former member can be recovered into a group.
   *
   * @param userId the id of the user to check
   * @param groupId the id of the group to check
   * @return a {@link UserRecoveryOption} indicating recoverability and, when
   *     recoverable, the associated profile
   */
  @Override
  public UserRecoveryOption isRecoverableFromGroup(
    String userId,
    String groupId
  ) {
    String profileId = getRecoverableProfileInGroup(userId, groupId);
    UserRecoveryOption result = new UserRecoveryOption();
    if (profileId != null) {
      Profile profile = profilesApi.profilesIdGet(profileId);
      if (profile != null) {
        result.setRecoverable(true);
        result.setProfile(profile);
      }
    }
    return result;
  }

  /**
   * Recreates a previously logged membership and restores its saved state.
   *
   * <p>Re-adds the user to the group under the given profile (without admin or
   * user notifications), marks the corresponding former-membership record as
   * recovered, and restores any previously cleaned properties/permissions.
   *
   * @param userId the id of the user to recover
   * @param groupId the id of the group to recover the membership into
   * @param profileId the id of the profile to assign to the recovered member
   */
  @Override
  public void recoverMembershipFromGroup(
    String userId,
    String groupId,
    String profileId
  ) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(groupId);
    MembershipPostDefinition body = new MembershipPostDefinition();
    UserProfile membershipsItem = new UserProfile();
    membershipsItem.setUser(usersApi.usersUserIdGet(userId));
    Profile profile = profilesApi.profilesIdGet(profileId);
    membershipsItem.setProfile(profile);
    body.addMembershipsItem(membershipsItem);
    body.setAdminNotifications(false);
    body.setUserNotifications(false);
    groupsApi.groupsIdMembersPost(groupNodeRef, body);
    historyDaoService.updateStateOldMemebership(
      userId,
      groupId,
      profile.getId(),
      profile.getGroupName(),
      1
    );
    recoverPropertiesFromGroup(userId, groupId);
  }

  /**
   * Logs and removes the group's non-inherited permissions for a single user.
   *
   * @param groupNodeRef the root node of the group subtree to process
   * @param userId the id of the user whose permissions are cleaned
   */
  @Override
  public void cleanAndLogPermissions(NodeRef groupNodeRef, String userId) {
    Set<String> userIds = new HashSet<>();
    userIds.add(userId);
    cleanAndLogPermissions(groupNodeRef, userIds);
  }

  /**
   * Logs and removes the group's non-inherited permissions for several users.
   *
   * <p>Runs without mini-transactions (see
   * {@link #cleanAndLogPermissions(NodeRef, Set, Boolean)}).
   *
   * @param groupNodeRef the root node of the group subtree to process
   * @param userIds the ids of the users whose permissions are cleaned
   */
  @Override
  public void cleanAndLogPermissions(
    NodeRef groupNodeRef,
    Set<String> userIds
  ) {
    cleanAndLogPermissions(groupNodeRef, userIds, false);
  }

  /**
   * Traverses the group subtree, logging then removing non-inherited
   * permissions granted directly to the given users.
   *
   * <p>The traversal is breadth-first over the traversable content and forum
   * types. For each visited node, the directly set permissions belonging to the
   * target users are archived via {@link HistoryDaoService} and then deleted.
   *
   * @param groupNodeRef the root node of the group subtree to process
   * @param userIds the ids of the users whose permissions are cleaned
   * @param useMiniTransaction when {@code true}, each permission is archived and
   *     deleted inside its own non-propagating transaction; when {@code false}
   *     the operations run in the ambient transaction
   */
  @Override
  public void cleanAndLogPermissions(
    NodeRef groupNodeRef,
    Set<String> userIds,
    Boolean useMiniTransaction
  ) {
    LinkedList<NodeRef> queue = new LinkedList<>();
    queue.addLast(groupNodeRef);
    while (!queue.isEmpty() && userIds != null && !userIds.isEmpty()) {
      NodeRef nodeRef = queue.removeFirst();
      if (!nodeService.exists(nodeRef)) {
        continue;
      }
      enqueueChildren(queue, nodeRef);
      processNodePermissions(
        nodeRef,
        groupNodeRef,
        userIds,
        useMiniTransaction
      );
    }
  }

  /**
   * Enqueues the traversable children of a node for permission processing.
   *
   * @param queue the traversal queue to append children to
   * @param nodeRef the node whose children are inspected
   */
  private void enqueueChildren(LinkedList<NodeRef> queue, NodeRef nodeRef) {
    for (ChildAssociationRef childAssoc : nodeService.getChildAssocs(nodeRef)) {
      QName type = nodeService.getType(childAssoc.getChildRef());
      if (TRAVERSABLE_TYPES.contains(type)) {
        queue.addLast(childAssoc.getChildRef());
      }
    }
  }

  /** Node types traversed when cleaning permissions across a group subtree. */
  private static final Set<QName> TRAVERSABLE_TYPES = Set.of(
    ContentModel.TYPE_FOLDER,
    ContentModel.TYPE_CONTENT,
    ForumModel.TYPE_FORUMS,
    ForumModel.TYPE_FORUM,
    ForumModel.TYPE_TOPIC,
    ForumModel.TYPE_POST
  );

  /**
   * Archives then deletes the target users' directly set permissions on a node.
   *
   * <p>Inherited permissions and permissions not belonging to the target users
   * are skipped. Any transaction or security error is logged and does not abort
   * the overall traversal.
   *
   * @param nodeRef the node whose permissions are processed
   * @param groupNodeRef the group root node, stored with each archived record
   * @param userIds the ids of the users whose permissions are cleaned
   * @param useMiniTransaction whether to wrap each archive/delete pair in its
   *     own non-propagating transaction
   */
  private void processNodePermissions(
    NodeRef nodeRef,
    NodeRef groupNodeRef,
    Set<String> userIds,
    Boolean useMiniTransaction
  ) {
    for (AccessPermission acp : permissionService.getAllSetPermissions(
      nodeRef
    )) {
      if (!userIds.contains(acp.getAuthority()) || acp.isInherited()) {
        continue;
      }
      try {
        if (Boolean.FALSE.equals(useMiniTransaction)) {
          historyDaoService.insertOldPermission(
            acp,
            acp.getAuthority(),
            groupNodeRef,
            nodeRef
          );
          permissionService.deletePermission(
            nodeRef,
            acp.getAuthority(),
            acp.getPermission()
          );
        } else {
          UserTransaction trx =
            transactionService.getNonPropagatingUserTransaction(false);
          trx.begin();
          historyDaoService.insertOldPermission(
            acp,
            acp.getAuthority(),
            groupNodeRef,
            nodeRef
          );
          permissionService.deletePermission(
            nodeRef,
            acp.getAuthority(),
            acp.getPermission()
          );
          trx.commit();
        }
      } catch (
        SecurityException
        | IllegalStateException
        | NotSupportedException
        | SystemException
        | RollbackException
        | HeuristicMixedException
        | HeuristicRollbackException e
      ) {
        logger.error(
          "Exception when processing clean permission of " + acp.getAuthority(),
          e
        );
      }
    }
  }

  /**
   * Restores previously cleaned permissions and notifications for a user.
   *
   * <p>Cancels any pending clean-permission request, then re-applies each
   * archived permission/notification property (in state {@code 0}) on nodes
   * that still exist, marking each as restored.
   *
   * @param userId the id of the user whose properties are restored
   * @param groupId the id of the group whose archived properties are restored
   */
  @Override
  public void recoverPropertiesFromGroup(String userId, String groupId) {
    NodeRef groupNodeRef = Converter.createNodeRefFromId(groupId);

    if (
      nodeService.exists(groupNodeRef) && personService.personExists(userId)
    ) {
      historyDaoService.removeWaitingCleanPermissions(userId, groupNodeRef);
      List<UserPropertyHistoryDAO> propertiesToRecover =
        historyDaoService.selectOldProperties(userId, groupNodeRef);
      for (UserPropertyHistoryDAO property : propertiesToRecover) {
        NodeRef node = Converter.createNodeRefFromId(property.getNodeId());
        if (
          property.getState() == 0 &&
          nodeService.exists(node) &&
          ("permission".equals(property.getTypeName()) ||
            "notification".equals(property.getTypeName()))
        ) {
          permissionService.setPermission(
            node,
            userId,
            property.getOldValue(),
            property.getAllowed()
          );
          historyDaoService.markPropertyRestored(
            userId,
            property.getNodeId(),
            property.getTypeId()
          );
        }
      }
    }
  }

  /**
   * Persists a user revocation request.
   *
   * <p>If no requester is set, the current authenticated user is used; if no
   * request state is set, it defaults to {@code 0} (pending).
   *
   * @param request the revocation request to store
   */
  @Override
  public void registerRevocation(UserRevocationRequest request) {
    if ("".equals(request.getRequester()) || request.getRequester() == null) {
      request.setRequester(authenticationService.getCurrentUserName());
    }

    if (request.getRequestState() == null) {
      request.setRequestState(0);
    }

    historyDaoService.insertUserRevocation(request);
  }

  /**
   * Returns a paginated list of revocation requests.
   *
   * @param limit the maximum number of requests per page
   * @param page the zero-based (or DAO-defined) page index
   * @return a {@link PagedUserRevocationRequest} with the page data and the
   *     total number of revocations
   */
  @Override
  public PagedUserRevocationRequest getRevocations(
    Integer limit,
    Integer page
  ) {
    Integer total = historyDaoService.countTotalRevocations();
    List<UserRevocationRequestDAO> data = historyDaoService.getRevocations(
      limit,
      page
    );
    List<UserRevocationRequest> newData = new ArrayList<>();
    for (UserRevocationRequestDAO request : data) {
      UserRevocationRequest req = convertRevocationRequest(request);
      newData.add(req);
    }

    PagedUserRevocationRequest result = new PagedUserRevocationRequest();
    result.setData(newData);
    result.setTotal(total);

    return result;
  }

  /**
   * Returns the revocation requests that are still waiting to be processed.
   *
   * @return the list of pending revocation requests
   */
  @Override
  public List<UserRevocationRequest> getWaitingRevocations() {
    List<UserRevocationRequestDAO> data =
      historyDaoService.getWaitingRevocations();
    List<UserRevocationRequest> newData = new ArrayList<>();

    for (UserRevocationRequestDAO request : data) {
      UserRevocationRequest req = convertRevocationRequest(request);
      newData.add(req);
    }
    return newData;
  }

  /**
   * Maps a persistence {@link UserRevocationRequestDAO} to its API model.
   *
   * <p>Converts date fields to {@link DateTime} and splits the comma-separated
   * user id string into a trimmed list.
   *
   * @param request the DAO record to convert
   * @return the corresponding {@link UserRevocationRequest} API model
   */
  private UserRevocationRequest convertRevocationRequest(
    UserRevocationRequestDAO request
  ) {
    UserRevocationRequest req = new UserRevocationRequest();
    req.setId(request.getId());
    req.setRequester(request.getRequester());
    req.setRevocationDate(new DateTime(request.getRevocationDate()));
    req.setRequestState(request.getRequestState());
    req.setAction(request.getAction());
    req.setGroupId(request.getGroupId());
    if (request.getJobStarted() != null) {
      req.setJobStarted(new DateTime(request.getJobStarted()));
    }
    if (request.getJobEnded() != null) {
      req.setJobEnded(new DateTime(request.getJobEnded()));
    }

    String[] users = request.getUserIds().split(",");
    for (String user : users) {
      req.getUserIds().add(user.trim());
    }
    return req;
  }

  /**
   * Updates the job execution state of a revocation request.
   *
   * @param id the id of the revocation request to update
   * @param jobStartedOn the timestamp when the job started
   * @param jobFinishedOn the timestamp when the job finished
   * @param state the new request state
   */
  @Override
  public void updateRevocationJobState(
    Integer id,
    Date jobStartedOn,
    Date jobFinishedOn,
    Integer state
  ) {
    historyDaoService.updateRevocationJobState(
      id,
      jobStartedOn,
      jobFinishedOn,
      state
    );
  }

  /**
   * Removes the former-membership log entries for a user in a group.
   *
   * @param groupId the id of the group
   * @param userId the id of the user
   */
  @Override
  public void cleanMembershipsLogs(String groupId, String userId) {
    historyDaoService.cleanMembershipsLogs(groupId, userId);
  }

  /**
   * Returns the pending clean-permission requests.
   *
   * @return the list of revocation requests awaiting permission cleanup
   */
  @Override
  public List<UserRevocationRequest> getWaitingCleanPermission() {
    List<UserRevocationRequestDAO> data =
      historyDaoService.getWaitingCleanPermissions();
    List<UserRevocationRequest> newData = new ArrayList<>();

    for (UserRevocationRequestDAO request : data) {
      UserRevocationRequest req = convertRevocationRequest(request);
      newData.add(req);
    }
    return newData;
  }

  /**
   * Registers a clean-permission request for a single user.
   *
   * @param groupNodeRef the group root node the request applies to
   * @param userId the id of the user whose permissions should be cleaned
   */
  @Override
  public void registerCleanPermissions(NodeRef groupNodeRef, String userId) {
    List<String> userIds = new ArrayList<>();
    userIds.add(userId);
    this.registerCleanPermissions(groupNodeRef, userIds);
  }

  /**
   * Registers a clean-permission request for several users.
   *
   * <p>The request is created with action {@code "clean-permission"}, the
   * current authenticated user as requester, the current date and state
   * {@code 0} (pending).
   *
   * @param groupNodeRef the group root node the request applies to
   * @param userIds the ids of the users whose permissions should be cleaned
   */
  @Override
  public void registerCleanPermissions(
    NodeRef groupNodeRef,
    List<String> userIds
  ) {
    UserRevocationRequest request = new UserRevocationRequest();
    request.setAction("clean-permission");
    request.setGroupId(groupNodeRef.getId());
    request.setUserIds(userIds);
    request.setRequester(authenticationService.getCurrentUserName());
    request.setRevocationDate(new DateTime());
    request.setRequestState(0);
    historyDaoService.registerCleanPermissions(request);
  }

  /**
   * Cancels a pending clean-permission request for a user in a group.
   *
   * @param userId the id of the user
   * @param groupNodeRef the group root node the request applies to
   */
  @Override
  public void cancelWaitingCleanPermissions(
    String userId,
    NodeRef groupNodeRef
  ) {
    historyDaoService.removeWaitingCleanPermissions(userId, groupNodeRef);
  }

  /**
   * Records a membership expiration date for a user in a group.
   *
   * @param userId the id of the expiring user
   * @param groupId the id of the group
   * @param profileId the id of the user's profile in the group
   * @param alfrescoGroup the underlying Alfresco group name
   * @param expirationDate the date on which the membership expires
   */
  @Override
  public void addExpirationDate(
    String userId,
    String groupId,
    String profileId,
    String alfrescoGroup,
    Date expirationDate
  ) {
    MemberExpirationDAO memberExpiration = new MemberExpirationDAO();
    memberExpiration.setUserId(userId);
    memberExpiration.setGroupId(groupId);
    memberExpiration.setProfileId(profileId);
    memberExpiration.setAlfrescoGroup(alfrescoGroup);
    memberExpiration.setExpirationDate(expirationDate);

    historyDaoService.insertMemberExpiration(memberExpiration);
  }

  /**
   * Removes the membership expiration date for a user in a group.
   *
   * @param userId the id of the user
   * @param groupId the id of the group
   */
  @Override
  public void deleteExpirationDate(String userId, String groupId) {
    MemberExpirationDAO memberExpiration = new MemberExpirationDAO();
    memberExpiration.setUserId(userId);
    memberExpiration.setGroupId(groupId);
    historyDaoService.deleteMemberExpiration(memberExpiration);
  }

  /**
   * Returns the users with a scheduled expiration in the given Interest Group.
   *
   * @param id the id of the Interest Group
   * @return a map of user id to expiration date
   */
  @Override
  public Map<String, Date> getAutoExpiredUsers(String id) {
    HashMap<String, Date> result = new HashMap<>();
    List<MemberExpirationDAO> list = historyDaoService.getMemberExpirationByIg(
      id
    );
    for (MemberExpirationDAO item : list) {
      result.put(item.getUserId(), item.getExpirationDate());
    }
    return result;
  }

  /**
   * Returns all memberships whose expiration date has passed.
   *
   * @return the list of expired member records
   */
  @Override
  public List<MemberExpirationDAO> getExpiredUsers() {
    return historyDaoService.getExpiredMembers();
  }

  /**
   * Registers or updates expiration dates for a batch of user memberships.
   *
   * <p>For each user and each of their Interest Group profiles, an existing
   * expiration is updated when present, otherwise a new one is inserted.
   *
   * @param request the per-user membership expiration requests to apply
   */
  @Override
  public void registerExpiration(
    List<UserMembershipsExpirationRequest> request
  ) {
    for (UserMembershipsExpirationRequest item : request) {
      String userId = item.getUserId();

      for (InterestGroupProfile groupProfile : item.getMemberships()) {
        MemberExpirationDAO expiration = new MemberExpirationDAO();
        expiration.setAlfrescoGroup(groupProfile.getProfile().getGroupName());
        expiration.setExpirationDate(item.getExpirationDate());
        expiration.setGroupId(groupProfile.getInterestGroup().getId());
        expiration.setProfileId(groupProfile.getProfile().getId());
        expiration.setUserId(userId);
        if (hasExpirationSet(userId, groupProfile.getInterestGroup().getId())) {
          historyDaoService.updateMemberExpiration(expiration);
        } else {
          historyDaoService.insertMemberExpiration(expiration);
        }
      }
    }
  }

  /**
   * Indicates whether a user already has an expiration set in a group.
   *
   * @param userId the id of the user
   * @param id the id of the group
   * @return {@code true} if at least one expiration record exists
   */
  private boolean hasExpirationSet(String userId, String id) {
    Integer number = historyDaoService.countUserExpirations(userId, id);
    return number > 0;
  }

  /**
   * Updates the expiration date of an existing membership.
   *
   * @param groupId the id of the group
   * @param userId the id of the user
   * @param expirationDate the new expiration date
   */
  @Override
  public void updateExpirationDate(
    String groupId,
    String userId,
    Date expirationDate
  ) {
    MemberExpirationDAO memberExpiration = new MemberExpirationDAO();
    memberExpiration.setUserId(userId);
    memberExpiration.setGroupId(groupId);
    memberExpiration.setExpirationDate(expirationDate);
    historyDaoService.updateMemberExpiration(memberExpiration);
  }
}
