/**
 *
 */
package io.swagger.api;

import eu.cec.digit.circabc.service.history.MemberExpirationDAO;
import io.swagger.model.*;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author beaurpi */
public interface HistoryApi {

    void logOldMembership(String userId, String igId);

    void logOldMembership(OldMembership oldMembership);

    String getRecoverableProfileInGroup(String userId, String groupId);

    UserRecoveryOption isRecoverableFromGroup(String userId, String groupId);

    void recoverMembershipFromGroup(String userId, String groupId, String profileId);

    void cleanAndLogPermissions(NodeRef groupNodeRef, String userId);

    void cleanAndLogPermissions(NodeRef groupNodeRef, Set<String> userIds);

    void cleanAndLogPermissions(
            NodeRef groupNodeRef, Set<String> userIds, Boolean useMiniTransaction);

    void recoverPropertiesFromGroup(String userId, String groupId);

    void registerRevocation(UserRevocationRequest request);

    PagedUserRevocationRequest getRevocations(Integer limit, Integer page);

    List<UserRevocationRequest> getWaitingRevocations();

    void updateRevocationJobState(Integer id, Date jobStartedOn, Date jobFinishedOn, Integer state);

    void cleanMembershipsLogs(String groupId, String userId);

    List<UserRevocationRequest> getWaitingCleanPermission();

    void registerCleanPermissions(NodeRef groupNodeRef, String userId);

    void registerCleanPermissions(NodeRef groupNodeRef, List<String> userIds);

    void cancelWaitingCleanPermissions(String userId, NodeRef groupNodeRef);

    void addExpirationDate(
            String userId, String groupId, String profileId, String alfrescoGroup, Date expirationDate);

    void deleteExpirationDate(String userId, String groupId);

    Map<String, Date> getAutoExpiredUsers(String id);

    List<MemberExpirationDAO> getExpiredUsers();

    void registerExpiration(List<UserMembershipsExpirationRequest> request);

    void updateExpirationDate(String groupId, String userId, Date expirationDate);
}
