/**
 *
 */
package io.swagger.api;

import eu.cec.digit.circabc.repo.app.model.UserPropertyHistoryDAO;
import eu.cec.digit.circabc.service.history.HistoryDaoService;
import eu.cec.digit.circabc.service.history.MemberExpirationDAO;
import eu.cec.digit.circabc.service.history.UserRevocationRequestDAO;
import io.swagger.model.*;
import io.swagger.util.Converter;
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

import javax.transaction.*;
import java.util.*;

/** @author beaurpi */
public class HistoryApiImpl implements HistoryApi {

    private static final Log logger = LogFactory.getLog(HistoryApiImpl.class);

    private GroupsApi groupsApi;
    private UsersApi usersApi;
    private ProfilesApi profilesApi;
    private NodeService nodeService;
    private PermissionService permissionService;
    private HistoryDaoService historyDaoService;
    private PersonService personService;
    private AuthenticationService authenticationService;
    private TransactionService transactionService;

    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    /** @return the profilesApi */
    public ProfilesApi getProfilesApi() {
        return profilesApi;
    }

    /** @param profilesApi the profilesApi to set */
    public void setProfilesApi(ProfilesApi profilesApi) {
        this.profilesApi = profilesApi;
    }

    public HistoryDaoService getHistoryDaoService() {
        return historyDaoService;
    }

    public void setHistoryDaoService(HistoryDaoService historyDaoService) {
        this.historyDaoService = historyDaoService;
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    /** @return the nodeService */
    public NodeService getNodeService() {
        return nodeService;
    }

    /** @param nodeService the nodeService to set */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /** @return the permissionService */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /** @param permissionService the permissionService to set */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /** @param personService the personService to set */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /** @param authenticationService the authenticationService to set */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /** @return the transactionService */
    public TransactionService getTransactionService() {
        return transactionService;
    }

    /** @param transactionService the transactionService to set */
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void logOldMembership(OldMembership oldMembership) {
        historyDaoService.insertOldMembership(oldMembership);
    }

    @Override
    public void logOldMembership(String userId, String igId) {
        OldMembership oldMembership = new OldMembership();
        PagedUserProfile users =
                groupsApi.groupsIdMembersGet(igId, null, "en", null, null, null, userId);

        if (users.getTotal() == 1) {
            oldMembership.setUserId(userId);
            oldMembership.setGroupId(igId);
            oldMembership.setProfileId(users.getData().get(0).getProfile().getId());
            oldMembership.setAlfGroupName(users.getData().get(0).getProfile().getGroupName());
            oldMembership.setState(0);
            oldMembership.setStateDate(new Date());
            logOldMembership(oldMembership);
        }
    }

    @Override
    public String getRecoverableProfileInGroup(String userId, String groupId) {
        return historyDaoService.getGroupIdForRecoverableUser(userId, groupId);
    }

    @Override
    public UserRecoveryOption isRecoverableFromGroup(String userId, String groupId) {
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

    @Override
    public void recoverMembershipFromGroup(String userId, String groupId, String profileId) {
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
        historyDaoService.updateStateOldMemebership(userId, groupId,profile.getId(),profile.getGroupName(), 1);
        recoverPropertiesFromGroup(userId, groupId);
    }

    @Override
    public void cleanAndLogPermissions(NodeRef groupNodeRef, String userId) {
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        cleanAndLogPermissions(groupNodeRef, userIds);
    }

    @Override
    public void cleanAndLogPermissions(NodeRef groupNodeRef, Set<String> userIds) {
        cleanAndLogPermissions(groupNodeRef, userIds, false);
    }

    @Override
    public void cleanAndLogPermissions(
            NodeRef groupNodeRef, Set<String> userIds, Boolean useMiniTransaction) {
        LinkedList<NodeRef> queue = new LinkedList<>();
        queue.addLast(groupNodeRef);
        while (!queue.isEmpty() && userIds != null && !userIds.isEmpty()) {
            NodeRef nodeRef = queue.removeFirst();
            // prepare queue for next nodes
            List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
            for (ChildAssociationRef childAssoc : children) {
                NodeRef childRef = childAssoc.getChildRef();
                QName type = nodeService.getType(childRef);
                if (ContentModel.TYPE_FOLDER.equals(type)
                        || ContentModel.TYPE_CONTENT.equals(type)
                        || ForumModel.TYPE_FORUM.equals(type)
                        || ForumModel.TYPE_TOPIC.equals(type)
                        || ForumModel.TYPE_POST.equals(type)) {
                    queue.addLast(childRef);
                }
            }
            Set<AccessPermission> perms = permissionService.getAllSetPermissions(nodeRef);
            for (AccessPermission acp : perms) {
                if (userIds.contains(acp.getAuthority()) && !acp.isInherited()) {
                    try {
                        if (!useMiniTransaction) {
                            historyDaoService.insertOldPermission(acp, acp.getAuthority(), groupNodeRef, nodeRef);
                            permissionService.deletePermission(nodeRef, acp.getAuthority(), acp.getPermission());
                        } else {
                            UserTransaction trx = transactionService.getNonPropagatingUserTransaction(false);
                            trx.begin();
                            historyDaoService.insertOldPermission(acp, acp.getAuthority(), groupNodeRef, nodeRef);
                            permissionService.deletePermission(nodeRef, acp.getAuthority(), acp.getPermission());
                            trx.commit();
                        }
                    } catch (SecurityException e) {
                        logger.error(
                                "Security Exception when processing clean permission of" + acp.getAuthority(), e);
                    } catch (IllegalStateException e) {
                        logger.error(
                                "IllegalStateException when processing clean permission of" + acp.getAuthority(),
                                e);
                    } catch (NotSupportedException e) {
                        logger.error(
                                "NotSupportedException when processing clean permission of" + acp.getAuthority(),
                                e);
                    } catch (SystemException e) {
                        logger.error(
                                "SystemException when processing clean permission of" + acp.getAuthority(), e);
                    } catch (RollbackException e) {
                        logger.error(
                                "RollbackException when processing clean permission of" + acp.getAuthority(), e);
                    } catch (HeuristicMixedException e) {
                        logger.error(
                                "HeuristicMixedException when processing clean permission of" + acp.getAuthority(),
                                e);
                    } catch (HeuristicRollbackException e) {
                        logger.error(
                                "HeuristicRollbackException when processing clean permission of"
                                        + acp.getAuthority(),
                                e);
                    }
                }
            }
        }
    }

    @Override
    public void recoverPropertiesFromGroup(String userId, String groupId) {
        NodeRef groupNodeRef = Converter.createNodeRefFromId(groupId);

        if (nodeService.exists(groupNodeRef) && personService.personExists(userId)) {

            historyDaoService.removeWaitingCleanPermissions(userId, groupNodeRef);
            List<UserPropertyHistoryDAO> propertiesToRecover =
                    historyDaoService.selectOldProperties(userId, groupNodeRef);
            for (UserPropertyHistoryDAO property : propertiesToRecover) {
                NodeRef node = Converter.createNodeRefFromId(property.getNodeId());
                if (property.getState() == 0 && nodeService.exists(node)) {
                    if ("permission".equals(property.getTypeName())
                            || "notification".equals(property.getTypeName())) {
                        permissionService.setPermission(
                                node, userId, property.getOldValue(), property.getAllowed());
                        historyDaoService.markPropertyRestored(
                                userId, property.getNodeId(), property.getTypeId());
                    }
                }
            }
        }
    }

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

    @Override
    public PagedUserRevocationRequest getRevocations(Integer limit, Integer page) {
        Integer total = historyDaoService.countTotalRevocations();
        List<UserRevocationRequestDAO> data = historyDaoService.getRevocations(limit, page);
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

    @Override
    public List<UserRevocationRequest> getWaitingRevocations() {
        List<UserRevocationRequestDAO> data = historyDaoService.getWaitingRevocations();
        List<UserRevocationRequest> newData = new ArrayList<>();

        for (UserRevocationRequestDAO request : data) {
            UserRevocationRequest req = convertRevocationRequest(request);
            newData.add(req);
        }
        return newData;
    }

    private UserRevocationRequest convertRevocationRequest(UserRevocationRequestDAO request) {
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

    @Override
    public void updateRevocationJobState(
            Integer id, Date jobStartedOn, Date jobFinishedOn, Integer state) {
        historyDaoService.updateRevocationJobState(id, jobStartedOn, jobFinishedOn, state);
    }

    @Override
    public void cleanMembershipsLogs(String groupId, String userId) {
        historyDaoService.cleanMembershipsLogs(groupId, userId);
    }

    @Override
    public List<UserRevocationRequest> getWaitingCleanPermission() {
        List<UserRevocationRequestDAO> data = historyDaoService.getWaitingCleanPermissions();
        List<UserRevocationRequest> newData = new ArrayList<>();

        for (UserRevocationRequestDAO request : data) {
            UserRevocationRequest req = convertRevocationRequest(request);
            newData.add(req);
        }
        return newData;
    }

    @Override
    public void registerCleanPermissions(NodeRef groupNodeRef, String userId) {
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);
        this.registerCleanPermissions(groupNodeRef, userIds);
    }

    @Override
    public void registerCleanPermissions(NodeRef groupNodeRef, List<String> userIds) {
        UserRevocationRequest request = new UserRevocationRequest();
        request.setAction("clean-permission");
        request.setGroupId(groupNodeRef.getId());
        request.setUserIds(userIds);
        request.setRequester(authenticationService.getCurrentUserName());
        request.setRevocationDate(new DateTime());
        request.setRequestState(0);
        historyDaoService.registerCleanPermissions(request);
    }

    @Override
    public void cancelWaitingCleanPermissions(String userId, NodeRef groupNodeRef) {
        historyDaoService.removeWaitingCleanPermissions(userId, groupNodeRef);
    }

    @Override
    public void addExpirationDate(
            String userId, String groupId, String profileId, String alfrescoGroup, Date expirationDate) {
        MemberExpirationDAO memberExpiration = new MemberExpirationDAO();
        memberExpiration.setUserId(userId);
        memberExpiration.setGroupId(groupId);
        memberExpiration.setProfileId(profileId);
        memberExpiration.setAlfrescoGroup(alfrescoGroup);
        memberExpiration.setExpirationDate(expirationDate);

        historyDaoService.insertMemberExpiration(memberExpiration);
    }

    @Override
    public void deleteExpirationDate(String userId, String groupId) {
        MemberExpirationDAO memberExpiration = new MemberExpirationDAO();
        memberExpiration.setUserId(userId);
        memberExpiration.setGroupId(groupId);
        historyDaoService.deleteMemberExpiration(memberExpiration);
    }

    @Override
    public Map<String, Date> getAutoExpiredUsers(String id) {
        HashMap<String, Date> result = new HashMap<>();
        List<MemberExpirationDAO> list = historyDaoService.getMemberExpirationByIg(id);
        for (MemberExpirationDAO item : list) {
            result.put(item.getUserId(), item.getExpirationDate());
        }
        return result;
    }

    @Override
    public List<MemberExpirationDAO> getExpiredUsers() {
        return historyDaoService.getExpiredMembers();
    }

    @Override
    public void registerExpiration(List<UserMembershipsExpirationRequest> request) {
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

    private boolean hasExpirationSet(String userId, String id) {
        Integer number = historyDaoService.countUserExpirations(userId, id);
        return number > 0;
    }

    @Override
    public void updateExpirationDate(String groupId, String userId, Date expirationDate) {
        MemberExpirationDAO memberExpiration = new MemberExpirationDAO();
        memberExpiration.setUserId(userId);
        memberExpiration.setGroupId(groupId);
        memberExpiration.setExpirationDate(expirationDate);
        historyDaoService.updateMemberExpiration(memberExpiration);
    }
}
