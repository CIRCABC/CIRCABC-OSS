/**
 *
 */
package eu.cec.digit.circabc.service.history;

import eu.cec.digit.circabc.repo.app.model.UserPropertyHistoryDAO;
import io.swagger.model.OldMembership;
import io.swagger.model.UserRevocationRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author beaurpi */
public class HistoryDaoServiceImpl implements HistoryDaoService {

    private static final Log logger = LogFactory.getLog(HistoryDaoServiceImpl.class);

    private SqlSessionTemplate sqlSessionTemplate = null;

    @Override
    public void insertOldMembership(OldMembership oldMembership) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", oldMembership.getUserId());
        props.put("groupId", oldMembership.getGroupId());
        props.put("profileId", oldMembership.getProfileId());
        props.put("alfGroupName", oldMembership.getAlfGroupName());
        props.put("state", oldMembership.getState());

        Integer count =
                (Integer) sqlSessionTemplate.selectOne("History.count_membership_history", props);
        if (count == 0) {
            sqlSessionTemplate.insert("History.insert_membership_history", props);
        } else {
            sqlSessionTemplate.update("History.update_membership_history_in_group", props);
        }
    }

    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Override
    public String getGroupIdForRecoverableUser(String userId, String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", userId);
        props.put("groupId", groupId);

        List<String> result =
                (List<String>)
                        sqlSessionTemplate.selectList("History.get_membership_history_in_group", props);
        if (result.size() > 0) {
            return result.get(0);
        }

        return null;
    }

    @Override
    public void updateStateOldMemebership(String userId, String groupId,String profileId,String alfGroupName, int i) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", userId);
        props.put("groupId", groupId);
        props.put("profileId", profileId);
        props.put("alfGroupName", alfGroupName);
        props.put("state", i);
        sqlSessionTemplate.update("History.update_membership_history_in_group", props);
    }

    @Override
    public void insertOldPermission(
            AccessPermission acp, String userId, NodeRef groupNodeRef, NodeRef nodeRef) {

        Map<String, Object> props = new HashMap<>();
        props.put("userId", userId);
        props.put("groupId", groupNodeRef.getId());
        props.put("nodeId", nodeRef.getId());
        props.put("oldValue", acp.getPermission());
        props.put("state", 0);
        Boolean isPerm = !acp.getPermission().contains("NotificationStatus");
        props.put(
                "typeId",
                (Integer)
                        sqlSessionTemplate.selectOne(
                                isPerm
                                        ? "History.select_permission_type_id"
                                        : "History.select_notification_type_id"));
        props.put("allowed", acp.getAccessStatus().equals(AccessStatus.ALLOWED));

        Integer count = (Integer) sqlSessionTemplate.selectOne("History.count_property_history", props);
        if (count == 0) {
            sqlSessionTemplate.insert("History.insert_property_history", props);
        } else {
            sqlSessionTemplate.update("History.update_property_history", props);
        }
    }

    @Override
    public List<UserPropertyHistoryDAO> selectOldProperties(String userId, NodeRef groupNodeRef) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", userId);
        props.put("groupId", groupNodeRef.getId());

        return (List<UserPropertyHistoryDAO>)
                sqlSessionTemplate.selectList("History.select_group_property_history", props);
    }

    @Override
    public void markPropertyRestored(String userId, String nodeId, Integer typeId) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", userId);
        props.put("nodeId", nodeId);
        props.put("typeId", typeId);

        sqlSessionTemplate.update("History.mark_restore_property_history", props);
    }

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
            sqlSessionTemplate.insert("History.insert_user_revocation_request", props);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public Integer countTotalRevocations() {
        return (Integer) sqlSessionTemplate.selectOne("History.count_user_revocation_request");
    }

    @Override
    public List<UserRevocationRequestDAO> getRevocations(Integer limit, Integer page) {
        Map<String, Object> props = new HashMap<>();
        props.put("limit", limit);
        // 0 based system
        if (page > 0) {
            props.put("page", page - 1);
        }
        // for mysql
        props.put("limitMin", (page - 1) * limit);

        return (List<UserRevocationRequestDAO>)
                sqlSessionTemplate.selectList("History.select_user_revocation_request", props);
    }

    @Override
    public List<UserRevocationRequestDAO> getWaitingRevocations() {
        return (List<UserRevocationRequestDAO>)
                sqlSessionTemplate.selectList("History.select_waiting_revocation_request");
    }

    @Override
    public void updateRevocationJobState(
            Integer id, Date jobStartedOn, Date jobFinishedOn, Integer state) {

        Map<String, Object> props = new HashMap<>();
        props.put("id", id);
        props.put("jobStarted", jobStartedOn);
        props.put("jobEnded", jobFinishedOn);
        props.put("state", state);

        sqlSessionTemplate.update("History.update_user_revocation_request", props);
    }

    @Override
    public void cleanMembershipsLogs(String groupId, String userId) {
        Map<String, Object> props = new HashMap<>();
        props.put("groupId", groupId);
        props.put("userId", userId);
        sqlSessionTemplate.update("History.clean_user_memberhistory", props);
        sqlSessionTemplate.update("History.clean_user_propertyhistory", props);
    }

    @Override
    public List<UserRevocationRequestDAO> getWaitingCleanPermissions() {
        return (List<UserRevocationRequestDAO>)
                sqlSessionTemplate.selectList("History.select_waiting_clean_permission_request");
    }

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
        props.put("groupId", request.getGroupId());
        try {
            sqlSessionTemplate.insert("History.insert_clean_permission_request", props);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void removeWaitingCleanPermissions(String userId, NodeRef groupNodeRef) {
        Map<String, String> props = new HashMap<>();
        props.put("userId", userId);
        props.put("groupId", groupNodeRef.getId());

        sqlSessionTemplate.delete("History.delete_waiting_clean_permission_request", props);
    }

    @Override
    public void insertMemberExpiration(MemberExpirationDAO memberExpiration) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", memberExpiration.getUserId());
        props.put("groupId", memberExpiration.getGroupId());
        props.put("profileId", memberExpiration.getProfileId());
        props.put("alfGroupName", memberExpiration.getAlfrescoGroup());
        props.put("expirationDate", memberExpiration.getExpirationDate());

        sqlSessionTemplate.insert("History.insert_membership_expiration", props);
    }

    @Override
    public void deleteMemberExpiration(MemberExpirationDAO memberExpiration) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", memberExpiration.getUserId());
        props.put("groupId", memberExpiration.getGroupId());
        props.put("profileId", memberExpiration.getProfileId());
        props.put("alfGroupName", memberExpiration.getAlfrescoGroup());
        props.put("expirationDate", memberExpiration.getExpirationDate());

        sqlSessionTemplate.delete("History.delete_membership_expiration", props);
    }

    @Override
    public void updateMemberExpiration(MemberExpirationDAO memberExpiration) {
        Map<String, Object> props = new HashMap<>();
        props.put("userId", memberExpiration.getUserId());
        props.put("groupId", memberExpiration.getGroupId());
        props.put("profileId", memberExpiration.getProfileId());
        props.put("alfGroupName", memberExpiration.getAlfrescoGroup());
        props.put("expirationDate", memberExpiration.getExpirationDate());

        sqlSessionTemplate.update("History.update_membership_expiration", props);
    }

    @Override
    public List<MemberExpirationDAO> getMemberExpirationByIg(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put("groupId", groupId);

        return (List<MemberExpirationDAO>)
                sqlSessionTemplate.selectList("History.select_member_expiration_by_group_id", props);
    }

    @Override
    public List<MemberExpirationDAO> getExpiredMembers() {
        return (List<MemberExpirationDAO>)
                sqlSessionTemplate.selectList("History.select_expired_members");
    }

    @Override
    public Integer countUserExpirations(String userId, String groupId) {
        Map<String, String> props = new HashMap<>();
        props.put("userId", userId);
        props.put("groupId", groupId);
        return (Integer) sqlSessionTemplate.selectOne("History.count_expiration_for_user_group", props);
    }
}
