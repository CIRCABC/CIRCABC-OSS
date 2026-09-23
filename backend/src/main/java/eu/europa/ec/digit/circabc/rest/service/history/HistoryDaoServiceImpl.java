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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * MyBatis-backed implementation of {@link HistoryDaoService}.
 *
 * <p>This class is the persistence boundary for CIRCABC history and
 * membership-lifecycle data. It executes the named SQL statements declared under
 * the {@code History} MyBatis namespace through a {@link SqlSessionTemplate},
 * mapping the supplied domain objects to statement parameters and returning the
 * corresponding DAO records. It performs no business logic beyond the read/write
 * routing (for example, choosing between insert and update depending on an
 * existence count) required to keep the historical records consistent.
 *
 * @author beaurpi
 */
public class HistoryDaoServiceImpl implements HistoryDaoService {

  /** MyBatis parameter key for the membership-expiration date. */
  private static final String EXPIRATION_DATE = "expirationDate";

  /** MyBatis parameter key for the state flag of a history record. */
  private static final String STATE = "state";

  /** MyBatis parameter key for the underlying Alfresco group name. */
  private static final String ALF_GROUP_NAME = "alfGroupName";

  /** MyBatis parameter key for the profile identifier. */
  private static final String PROFILE_ID = "profileId";

  /** MyBatis parameter key for the group identifier. */
  private static final String GROUP_ID = "groupId";

  /** MyBatis parameter key for the user identifier. */
  private static final String USER_ID = "userId";

  /** Logger for this service. */
  private static final Log logger = LogFactory.getLog(
    HistoryDaoServiceImpl.class
  );

  /** Spring-managed MyBatis session template used to run the SQL statements. */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * {@inheritDoc}
   *
   * <p>Inserts a new membership-history row when none exists for the given
   * user/group combination, otherwise updates the existing row's group state.
   */
  @Override
  public void insertOldMembership(OldMembership oldMembership) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, oldMembership.getUserId());
    props.put(GROUP_ID, oldMembership.getGroupId());
    props.put(PROFILE_ID, oldMembership.getProfileId());
    props.put(ALF_GROUP_NAME, oldMembership.getAlfGroupName());
    props.put(STATE, oldMembership.getState());

    Integer count = (Integer) sqlSessionTemplate.selectOne(
      "History.count_membership_history",
      props
    );
    if (count == 0) {
      sqlSessionTemplate.insert("History.insert_membership_history", props);
    } else {
      sqlSessionTemplate.update(
        "History.update_membership_history_in_group",
        props
      );
    }
  }

  /**
   * Returns the MyBatis session template used by this service.
   *
   * @return the configured {@link SqlSessionTemplate}, or {@code null} if not set
   */
  public SqlSessionTemplate getSqlSessionTemplate() {
    return sqlSessionTemplate;
  }

  /**
   * Sets the MyBatis session template used to execute the SQL statements.
   *
   * @param sqlSessionTemplate the session template to use
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /** {@inheritDoc} */
  @Override
  public String getGroupIdForRecoverableUser(String userId, String groupId) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put(GROUP_ID, groupId);

    List<String> result = sqlSessionTemplate.selectList(
      "History.get_membership_history_in_group",
      props
    );
    if (!result.isEmpty()) {
      return result.get(0);
    }

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void updateStateOldMemebership(
    String userId,
    String groupId,
    String profileId,
    String alfGroupName,
    int i
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put(GROUP_ID, groupId);
    props.put(PROFILE_ID, profileId);
    props.put(ALF_GROUP_NAME, alfGroupName);
    props.put(STATE, i);
    sqlSessionTemplate.update(
      "History.update_membership_history_in_group",
      props
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>The MyBatis property type is resolved as a notification type when the
   * permission name contains {@code "NotificationStatus"}, and as a plain
   * permission type otherwise. A new row is inserted only when no matching record
   * already exists; otherwise the existing record is updated.
   */
  @Override
  public void insertOldPermission(
    AccessPermission acp,
    String userId,
    NodeRef groupNodeRef,
    NodeRef nodeRef
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put(GROUP_ID, groupNodeRef.getId());
    props.put("nodeId", nodeRef.getId());
    props.put("oldValue", acp.getPermission());
    props.put(STATE, 0);
    boolean isPerm = !acp.getPermission().contains("NotificationStatus");
    props.put(
      "typeId",
      sqlSessionTemplate.selectOne(
        isPerm
          ? "History.select_permission_type_id"
          : "History.select_notification_type_id"
      )
    );
    props.put("allowed", acp.getAccessStatus().equals(AccessStatus.ALLOWED));

    Integer count = (Integer) sqlSessionTemplate.selectOne(
      "History.count_property_history",
      props
    );
    if (count == 0) {
      sqlSessionTemplate.insert("History.insert_property_history", props);
    } else {
      sqlSessionTemplate.update("History.update_property_history", props);
    }
  }

  /** {@inheritDoc} */
  @Override
  public List<UserPropertyHistoryDAO> selectOldProperties(
    String userId,
    NodeRef groupNodeRef
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put(GROUP_ID, groupNodeRef.getId());

    return sqlSessionTemplate.selectList(
      "History.select_group_property_history",
      props
    );
  }

  /** {@inheritDoc} */
  @Override
  public void markPropertyRestored(
    String userId,
    String nodeId,
    Integer typeId
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put("nodeId", nodeId);
    props.put("typeId", typeId);

    sqlSessionTemplate.update("History.mark_restore_property_history", props);
  }

  /**
   * {@inheritDoc}
   *
   * <p>The request's user identifiers are flattened into a comma-separated string
   * before being persisted. Any failure is logged and swallowed rather than
   * propagated.
   */
  @Override
  public void insertUserRevocation(UserRevocationRequest request) {
    Map<String, Object> props = new HashMap<>();
    props.put("requesterUserId", request.getRequester());
    props.put("revocationDate", request.getRevocationDate().toDate());
    String users = request.getUserIds().toString();
    users = users.replace("[", "");
    users = users.replace("]", "");
    users = users.replace(" ", "");
    props.put("users", users);
    try {
      sqlSessionTemplate.insert(
        "History.insert_user_revocation_request",
        props
      );
    } catch (Exception e) {
      logger.error("Error inserting user revocation request", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Integer countTotalRevocations() {
    return (Integer) sqlSessionTemplate.selectOne(
      "History.count_user_revocation_request"
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>The one-based {@code page} argument is translated into the zero-based
   * offsets expected by the underlying paging statement.
   */
  @Override
  public List<UserRevocationRequestDAO> getRevocations(
    Integer limit,
    Integer page
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("limit", limit);
    // 0 based system
    if (page > 0) {
      props.put("page", page - 1);
    }
    // for mysql
    props.put("limitMin", (page - 1) * limit);

    return sqlSessionTemplate.selectList(
      "History.select_user_revocation_request",
      props
    );
  }

  /** {@inheritDoc} */
  @Override
  public List<UserRevocationRequestDAO> getWaitingRevocations() {
    return sqlSessionTemplate.selectList(
      "History.select_waiting_revocation_request"
    );
  }

  /** {@inheritDoc} */
  @Override
  public void updateRevocationJobState(
    Integer id,
    Date jobStartedOn,
    Date jobFinishedOn,
    Integer state
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", id);
    props.put("jobStarted", jobStartedOn);
    props.put("jobEnded", jobFinishedOn);
    props.put(STATE, state);

    sqlSessionTemplate.update("History.update_user_revocation_request", props);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Removes both the membership-history and property-history logs for the
   * given user/group pair.
   */
  @Override
  public void cleanMembershipsLogs(String groupId, String userId) {
    Map<String, Object> props = new HashMap<>();
    props.put(GROUP_ID, groupId);
    props.put(USER_ID, userId);
    sqlSessionTemplate.update("History.clean_user_memberhistory", props);
    sqlSessionTemplate.update("History.clean_user_propertyhistory", props);
  }

  /** {@inheritDoc} */
  @Override
  public List<UserRevocationRequestDAO> getWaitingCleanPermissions() {
    return sqlSessionTemplate.selectList(
      "History.select_waiting_clean_permission_request"
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>The request's user identifiers are flattened into a comma-separated string
   * before being persisted. Any failure is logged as a warning rather than
   * propagated.
   */
  @Override
  public void registerCleanPermissions(UserRevocationRequest request) {
    Map<String, Object> props = new HashMap<>();
    props.put("requesterUserId", request.getRequester());
    props.put("revocationDate", request.getRevocationDate().toDate());
    String users = request.getUserIds().toString();
    users = users.replace("[", "");
    users = users.replace("]", "");
    users = users.replace(" ", "");
    props.put("users", users);
    props.put(GROUP_ID, request.getGroupId());
    try {
      sqlSessionTemplate.insert(
        "History.insert_clean_permission_request",
        props
      );
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Error inserting clean permission request for user " +
            request.getRequester() +
            " and group " +
            request.getGroupId(),
          e
        );
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void removeWaitingCleanPermissions(
    String userId,
    NodeRef groupNodeRef
  ) {
    Map<String, String> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put(GROUP_ID, groupNodeRef.getId());

    sqlSessionTemplate.delete(
      "History.delete_waiting_clean_permission_request",
      props
    );
  }

  /** {@inheritDoc} */
  @Override
  public void insertMemberExpiration(MemberExpirationDAO memberExpiration) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, memberExpiration.getUserId());
    props.put(GROUP_ID, memberExpiration.getGroupId());
    props.put(PROFILE_ID, memberExpiration.getProfileId());
    props.put(ALF_GROUP_NAME, memberExpiration.getAlfrescoGroup());
    props.put(EXPIRATION_DATE, memberExpiration.getExpirationDate());

    sqlSessionTemplate.insert("History.insert_membership_expiration", props);
  }

  /** {@inheritDoc} */
  @Override
  public void deleteMemberExpiration(MemberExpirationDAO memberExpiration) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, memberExpiration.getUserId());
    props.put(GROUP_ID, memberExpiration.getGroupId());
    props.put(PROFILE_ID, memberExpiration.getProfileId());
    props.put(ALF_GROUP_NAME, memberExpiration.getAlfrescoGroup());
    props.put(EXPIRATION_DATE, memberExpiration.getExpirationDate());

    sqlSessionTemplate.delete("History.delete_membership_expiration", props);
  }

  /** {@inheritDoc} */
  @Override
  public void updateMemberExpiration(MemberExpirationDAO memberExpiration) {
    Map<String, Object> props = new HashMap<>();
    props.put(USER_ID, memberExpiration.getUserId());
    props.put(GROUP_ID, memberExpiration.getGroupId());
    props.put(PROFILE_ID, memberExpiration.getProfileId());
    props.put(ALF_GROUP_NAME, memberExpiration.getAlfrescoGroup());
    props.put(EXPIRATION_DATE, memberExpiration.getExpirationDate());

    sqlSessionTemplate.update("History.update_membership_expiration", props);
  }

  /** {@inheritDoc} */
  @Override
  public List<MemberExpirationDAO> getMemberExpirationByIg(String groupId) {
    Map<String, Object> props = new HashMap<>();
    props.put(GROUP_ID, groupId);

    return sqlSessionTemplate.selectList(
      "History.select_member_expiration_by_group_id",
      props
    );
  }

  /** {@inheritDoc} */
  @Override
  public List<MemberExpirationDAO> getExpiredMembers() {
    return sqlSessionTemplate.selectList("History.select_expired_members");
  }

  /** {@inheritDoc} */
  @Override
  public Integer countUserExpirations(String userId, String groupId) {
    Map<String, String> props = new HashMap<>();
    props.put(USER_ID, userId);
    props.put(GROUP_ID, groupId);
    return (Integer) sqlSessionTemplate.selectOne(
      "History.count_expiration_for_user_group",
      props
    );
  }
}
