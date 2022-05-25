package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.log.UserActionLogDAO;
import eu.cec.digit.circabc.repo.log.UserNewsFeedRequest;
import eu.cec.digit.circabc.service.log.LogService;
import io.swagger.model.*;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author beaurpi
 */
public class DashboardApiImpl implements DashboardApi {

    private static final String COMMENT = "comment";
    private static final String CREATE_POST = "create post";
    private static final String ADD_REPLY = "add reply";
    private static final String UPDATE = "update";
    private static final String TRANSLATE = "translate";
    private static final String CHECKIN = "perform checkin";
    private static final String UPDATE_CONTENT = "update content";
    private static final String UPDATE_DOCUMENT = "update document";
    private static final String UPDATE_CONTENT_PROPERTIES = "update content properties";
    private static final String UPDATEDOCUMENT = "updatedocument";
    private static final String UPLOAD = "upload";
    private static final String ADD_TRANSLATION = "add translation";
    private static final String ADD_CONTENT_TRANSLATION = "add content translation";
    private static final String UPLOAD_DOCUMENT = "upload document";
    private static final String UPDATE_LOCK_DOCUMENT =
            "update checked out working copy (no checking in)";

    private static final Log logger = LogFactory.getLog(DashboardApiImpl.class);
    private LogService logService;
    private NodeService nodeService;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
    private NodesApi nodesApi;
    private UsersApi usersApi;

    @Override
    public List<UserActionLog> usersUserIdDashboardDownloadsGet(String userId) {

        List<UserActionLog> result = new ArrayList<>();

        List<UserActionLogDAO> recentUserDownloads = logService.getRecentUserDownloads(userId, 10);
        for (UserActionLogDAO action : recentUserDownloads) {
            NodeRef igRef = nodeService.getNodeRef(Long.parseLong(action.getIgId()));
            NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(action.getDocumentId()));
            if (nodeRef != null
                    && !nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE)
                    && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
                    && igRef != null
                    && nodeService.exists(igRef)
                    && nodeService.exists(nodeRef)
                    && currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeRef.getId())) {
                UserActionLog userActionLog = new UserActionLog();
                try {
                    userActionLog.setAction("download");
                    userActionLog.setActionDate(Converter.convertDateToString(action.getLogDate()));
                    userActionLog.setNode(nodesApi.getNode(nodeRef));
                    userActionLog.setIgNode(igRef.getId());
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(
                                "Exception in usersUserIdDashboardDownloadsGet for action" + action.toString(), e);
                    }
                    continue;
                }
                result.add(userActionLog);
            }
        }

        return result;
    }

    public LogService getLogService() {
        return logService;
    }

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the nodesApi
     */
    public NodesApi getNodesApi() {
        return nodesApi;
    }

    /**
     * @param nodesApi the nodesApi to set
     */
    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    @Override
    public List<UserActionLog> usersUserIdDashboardUploadsGet(String userId) {

        List<UserActionLog> result = new ArrayList<>();

        List<UserActionLogDAO> recentUserUploads = logService.getRecentUserUploads(userId, 10);

        for (UserActionLogDAO action : recentUserUploads) {
            NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(action.getDocumentId()));
            NodeRef igRef = nodeService.getNodeRef(Long.parseLong(action.getIgId()));
            if (nodeRef != null
                    && !nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE)
                    && nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
                    && igRef != null
                    && nodeService.exists(igRef)
                    && nodeService.exists(nodeRef)
                    && currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeRef.getId())) {
                UserActionLog userActionLog = new UserActionLog();
                try {
                    userActionLog.setAction(UPLOAD);
                    userActionLog.setActionDate(Converter.convertDateToString(action.getLogDate()));
                    userActionLog.setNode(nodesApi.getNode(nodeRef));
                    userActionLog.setIgNode(igRef.getId());
                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(
                                "Exception in usersUserIdDashboardUploadsGet for action" + action.toString(), e);
                    }
                    continue;
                }
                result.add(userActionLog);
            }
        }

        return result;
    }

    @Override
    public UserNewsFeed usersUserIdDashboardNewsfeedGet(String userId, String when) {

        UserNewsFeed result = new UserNewsFeed();

        List<InterestGroupProfile> memberships = usersApi.getUserMembership(userId);
        List<Long> groupIds = new ArrayList<>();

        for (InterestGroupProfile membership : memberships) {
            NodeRef groupRef = Converter.createNodeRefFromId(membership.getInterestGroup().getId());
            Long id = (Long) nodeService.getProperty(groupRef, ContentModel.PROP_NODE_DBID);
            groupIds.add(id);
        }

        if (UserNewsFeed.WhenEnum.WEEK.toString().equals(when)) {
            result.setWhen(UserNewsFeed.WhenEnum.WEEK);
        } else if (UserNewsFeed.WhenEnum.PREVIOUSWEEK.toString().equals(when)) {
            result.setWhen(UserNewsFeed.WhenEnum.PREVIOUSWEEK);
        }
        if (UserNewsFeed.WhenEnum.TODAY.toString().equals(when)) {
            result.setWhen(UserNewsFeed.WhenEnum.TODAY);
        }
        List<Long> activityIds = logService.getUserDashboardActivityIds();
        if (!groupIds.isEmpty() && !activityIds.isEmpty()) {
            UserNewsFeedRequest request = new UserNewsFeedRequest();
            request.setIgIds(groupIds);
            request.setActivityIds(activityIds);

            if (UserNewsFeed.WhenEnum.WEEK.toString().equals(when)) {
                request.setWhen(UserNewsFeed.WhenEnum.WEEK.toString());
            } else if (UserNewsFeed.WhenEnum.PREVIOUSWEEK.toString().equals(when)) {
                request.setWhen(UserNewsFeed.WhenEnum.PREVIOUSWEEK.toString());
            }
            if (UserNewsFeed.WhenEnum.TODAY.toString().equals(when)) {
                request.setWhen(UserNewsFeed.WhenEnum.TODAY.toString());
            }

            long uploads = 0L;
            long updates = 0L;
            long comments = 0L;

            Map<String, InterestGroupFeed> groupFeeds = new HashMap<>();

            List<UserActionLogDAO> activities = logService.getUserDashboardActivities(request);
            for (UserActionLogDAO activity : activities) {

                NodeRef nodeRef = nodeService.getNodeRef(Long.parseLong(activity.getDocumentId()));
                NodeRef igRef = nodeService.getNodeRef(Long.parseLong(activity.getIgId()));

                if (nodeRef != null
                        && currentUserPermissionCheckerService.hasAlfrescoReadPermission(
                        activity.getDocumentId())
                        && !nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE)
                        && igRef != null
                        && nodeService.exists(igRef)
                        && nodeService.exists(nodeRef)
                        && currentUserPermissionCheckerService.hasAlfrescoReadPermission(nodeRef.getId())
                        && (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
                        || nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP))) {
                    UserActionLog log = new UserActionLog();
                    log.setIgNode(igRef.getId());
                    log.setNode(nodesApi.getNode(nodeRef));
                    log.setActionDate(Converter.convertDateToString(activity.getLogDate()));
                    log.setUsername(activity.getUsername());

                    String action = activity.getAction().toLowerCase();

                    if (action.equals(UPLOAD_DOCUMENT)) {
                        log.setAction(UPLOAD);
                        uploads += 1;
                    }

                    if (action.equals(ADD_TRANSLATION) || action.equals(ADD_CONTENT_TRANSLATION)) {
                        log.setAction(TRANSLATE);
                        uploads += 1;
                    }

                    if (action.equals(UPDATE_DOCUMENT)
                            || action.equals(UPDATE_CONTENT_PROPERTIES)
                            || action.equals(UPDATE_CONTENT)
                            || action.equals(CHECKIN)
                            || action.equals(UPDATEDOCUMENT)
                            || action.equals(UPDATE_LOCK_DOCUMENT)) {
                        log.setAction(UPDATE);
                        updates += 1;
                    }

                    if (action.equals(CREATE_POST) || action.equals(ADD_REPLY)) {
                        log.setAction(COMMENT);
                        comments += 1;

                        // special case that handles comments on a library document
                        if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
                            nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef(); // topic
                            nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef(); // discussion
                            log.setNode(nodesApi.getNode(nodeRef));
                        }
                    }

                    if (groupFeeds.containsKey(activity.getIgId())) {
                        groupFeeds.get(activity.getIgId()).getFeed().add(log);
                    } else {
                        InterestGroupFeed feed = new InterestGroupFeed();
                        feed.setId(igRef.getId());
                        feed.setName(nodeService.getProperty(igRef, ContentModel.PROP_NAME).toString());

                        if (nodeService.getProperty(igRef, ContentModel.PROP_TITLE) != null) {
                            feed.setTitle(
                                    Converter.toI18NProperty(
                                            (MLText) nodeService.getProperty(igRef, ContentModel.PROP_TITLE)));
                        } else {
                            feed.setTitle(new I18nProperty());
                        }

                        feed.getFeed().add(log);
                        groupFeeds.put(activity.getIgId(), feed);
                    }
                }
            }

            result.setUploads(uploads);
            result.setUpdates(updates);
            result.setComments(comments);
            result.getGroupFeeds().addAll(groupFeeds.values());
        }

        return result;
    }

    /**
     * @return the usersApi
     */
    public UsersApi getUsersApi() {
        return usersApi;
    }

    /**
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
