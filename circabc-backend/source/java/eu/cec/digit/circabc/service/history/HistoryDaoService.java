/**
 *
 */
package eu.cec.digit.circabc.service.history;

import eu.cec.digit.circabc.repo.app.model.UserPropertyHistoryDAO;
import io.swagger.model.OldMembership;
import io.swagger.model.UserRevocationRequest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;

import java.util.Date;
import java.util.List;

/** @author beaurpi */
public interface HistoryDaoService {

    void insertOldMembership(OldMembership oldMembership);

    String getGroupIdForRecoverableUser(String userId, String groupId);

    void updateStateOldMemebership(String userId, String groupId,String profileId, String alfGroupName, int i);

    void insertOldPermission(
            AccessPermission acp, String userId, NodeRef groupNodeRef, NodeRef nodeRef);

    List<UserPropertyHistoryDAO> selectOldProperties(String userId, NodeRef groupNodeRef);

    void markPropertyRestored(String userId, String nodeId, Integer typeId);

    void insertUserRevocation(UserRevocationRequest request);

    Integer countTotalRevocations();

    List<UserRevocationRequestDAO> getRevocations(Integer limit, Integer page);

    List<UserRevocationRequestDAO> getWaitingRevocations();

    void updateRevocationJobState(Integer id, Date jobStartedOn, Date jobFinishedOn, Integer state);

    void cleanMembershipsLogs(String groupId, String userId);

    List<UserRevocationRequestDAO> getWaitingCleanPermissions();

    void registerCleanPermissions(UserRevocationRequest request);

    void removeWaitingCleanPermissions(String userId, NodeRef groupNodeRef);

    void insertMemberExpiration(MemberExpirationDAO memberExpiration);

    void deleteMemberExpiration(MemberExpirationDAO memberExpiration);

    void updateMemberExpiration(MemberExpirationDAO memberExpiration);

    List<MemberExpirationDAO> getMemberExpirationByIg(String id);

    List<MemberExpirationDAO> getExpiredMembers();

    Integer countUserExpirations(String userId, String id);
}
