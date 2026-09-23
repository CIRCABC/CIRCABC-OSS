package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.UserActionLogDAO;
import io.swagger.model.db.UserNewsFeedRequest;
import io.swagger.util.Converter;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link DashboardApi} backing the user dashboard REST endpoints.
 *
 * <p>This service assembles the activity data shown on a user's personal dashboard by reading raw
 * action-log records through {@link LogService} and resolving them against the Alfresco repository:
 *
 * <ul>
 *   <li>recent documents downloaded by the user;
 *   <li>recent documents uploaded by the user;
 *   <li>a personalized news feed aggregating recent activity (uploads, updates and comments) across
 *       the Interest Groups the user is a member of.
 * </ul>
 *
 * <p>For every log entry the corresponding node is validated (it must still exist, not be archived,
 * belong to a Library or Newsgroup, and be readable by the current user) before being exposed. Raw
 * action labels persisted in the logs are normalized to a small set of dashboard categories
 * (upload, translate, update, comment) via {@link #categorizeAction(String)}.
 *
 * @author beaurpi
 */
public class DashboardApiImpl implements DashboardApi {

  /** Normalized dashboard category for commenting/replying actions. */
  private static final String COMMENT = "comment";
  /** Raw log label for creating a forum post; normalized to {@link #COMMENT}. */
  private static final String CREATE_POST = "create post";
  /** Raw log label for replying to a post; normalized to {@link #COMMENT}. */
  private static final String ADD_REPLY = "add reply";
  /** Normalized dashboard category for document update actions. */
  private static final String UPDATE = "update";
  /** Normalized dashboard category for translation actions. */
  private static final String TRANSLATE = "translate";
  /** Raw log label for a check-in operation; normalized to {@link #UPDATE}. */
  private static final String CHECKIN = "perform checkin";
  /** Raw log label for updating content; normalized to {@link #UPDATE}. */
  private static final String UPDATE_CONTENT = "update content";
  /** Raw log label for updating a document; normalized to {@link #UPDATE}. */
  private static final String UPDATE_DOCUMENT = "update document";
  /** Raw log label for updating content properties; normalized to {@link #UPDATE}. */
  private static final String UPDATE_CONTENT_PROPERTIES =
    "update content properties";
  /** Raw log label variant for a document update; normalized to {@link #UPDATE}. */
  private static final String UPDATEDOCUMENT = "updatedocument";
  /** Normalized dashboard category for upload actions. */
  private static final String UPLOAD = "upload";
  /** Raw log label for adding a translation; normalized to {@link #TRANSLATE}. */
  private static final String ADD_TRANSLATION = "add translation";
  /** Raw log label for adding a content translation; normalized to {@link #TRANSLATE}. */
  private static final String ADD_CONTENT_TRANSLATION =
    "add content translation";
  /** Raw log label for uploading a document; normalized to {@link #UPLOAD}. */
  private static final String UPLOAD_DOCUMENT = "upload document";
  /** Raw log label for updating a checked-out working copy; normalized to {@link #UPDATE}. */
  private static final String UPDATE_LOCK_DOCUMENT =
    "update checked out working copy (no checking in)";

  /** Logger for this service. */
  private static final Log logger = LogFactory.getLog(DashboardApiImpl.class);

  /** Provides access to the persisted user action logs backing the dashboard. */
  @Autowired
  private LogService logService;

  /** Alfresco service used to resolve nodes and read their properties/aspects. */
  @Autowired
  private NodeService nodeService;

  /** Checks whether the current user is allowed to read a given node. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /** Converts {@link NodeRef}s into the API {@code Node} representation exposed to clients. */
  @Autowired
  private NodesApi nodesApi;

  /** Provides user membership information used to scope the news feed to the user's groups. */
  @Autowired
  private UsersApi usersApi;

  /**
   * {@inheritDoc}
   *
   * <p>Reads the user's most recent download log entries (capped at 10), keeping only Library
   * documents that still exist, are not archived and are readable by the current user. Each
   * surviving entry is mapped to a {@link UserActionLog} with action {@code "download"}; entries
   * that fail to resolve are logged and skipped.
   *
   * @param userId the identifier of the user whose download history is requested
   * @return the list of {@link UserActionLog} entries describing the user's recent downloads;
   *     never {@code null}
   */
  @Override
  public List<UserActionLog> usersUserIdDashboardDownloadsGet(String userId) {
    List<UserActionLog> result = new ArrayList<>();

    List<UserActionLogDAO> recentUserDownloads =
      logService.getRecentUserDownloads(userId, 10);
    for (UserActionLogDAO action : recentUserDownloads) {
      NodeRef igRef = nodeService.getNodeRef(Long.parseLong(action.getIgId()));
      NodeRef nodeRef = nodeService.getNodeRef(
        Long.parseLong(action.getDocumentId())
      );
      if (
        nodeRef != null &&
        !nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) &&
        nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY) &&
        igRef != null &&
        nodeService.exists(igRef) &&
        nodeService.exists(nodeRef) &&
        currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          nodeRef.getId()
        )
      ) {
        UserActionLog userActionLog = new UserActionLog();
        try {
          userActionLog.setAction("download");
          userActionLog.setActionDate(
            Converter.convertDateToString(action.getLogDate())
          );
          userActionLog.setNode(nodesApi.getNode(nodeRef));
          userActionLog.setIgNode(igRef.getId());
        } catch (Exception e) {
          if (logger.isErrorEnabled()) {
            logger.error(
              "Exception in usersUserIdDashboardDownloadsGet for action" +
                action.toString(),
              e
            );
          }
          continue;
        }
        result.add(userActionLog);
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads the user's most recent upload log entries (capped at 10), keeping only Library
   * documents that still exist, are not archived and are readable by the current user. Each
   * surviving entry is mapped to a {@link UserActionLog} with action {@link #UPLOAD}; entries that
   * fail to resolve are logged and skipped.
   *
   * @param userId the identifier of the user whose upload history is requested
   * @return the list of {@link UserActionLog} entries describing the user's recent uploads; never
   *     {@code null}
   */
  @Override
  public List<UserActionLog> usersUserIdDashboardUploadsGet(String userId) {
    List<UserActionLog> result = new ArrayList<>();

    List<UserActionLogDAO> recentUserUploads = logService.getRecentUserUploads(
      userId,
      10
    );

    for (UserActionLogDAO action : recentUserUploads) {
      NodeRef nodeRef = nodeService.getNodeRef(
        Long.parseLong(action.getDocumentId())
      );
      NodeRef igRef = nodeService.getNodeRef(Long.parseLong(action.getIgId()));
      if (
        nodeRef != null &&
        !nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) &&
        nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY) &&
        igRef != null &&
        nodeService.exists(igRef) &&
        nodeService.exists(nodeRef) &&
        currentUserPermissionCheckerService.hasAlfrescoReadPermission(
          nodeRef.getId()
        )
      ) {
        UserActionLog userActionLog = new UserActionLog();
        try {
          userActionLog.setAction(UPLOAD);
          userActionLog.setActionDate(
            Converter.convertDateToString(action.getLogDate())
          );
          userActionLog.setNode(nodesApi.getNode(nodeRef));
          userActionLog.setIgNode(igRef.getId());
        } catch (Exception e) {
          if (logger.isErrorEnabled()) {
            logger.error(
              "Exception in usersUserIdDashboardUploadsGet for action" +
                action.toString(),
              e
            );
          }
          continue;
        }
        result.add(userActionLog);
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Determines the Interest Groups the user belongs to, resolves the requested time window and,
   * when both the group set and the set of dashboard-relevant activity types are non-empty,
   * aggregates the matching activity into per-group feeds together with global upload/update/comment
   * counters.
   *
   * @param userId the identifier of the user whose news feed is requested
   * @param when the requested time window; one of the {@link UserNewsFeed.WhenEnum} string values
   *     ({@code TODAY}, {@code WEEK}, {@code PREVIOUSWEEK}), or any other value to leave the window
   *     unconstrained
   * @return the populated {@link UserNewsFeed} for the user; never {@code null}
   */
  @Override
  public UserNewsFeed usersUserIdDashboardNewsfeedGet(
    String userId,
    String when
  ) {
    UserNewsFeed result = new UserNewsFeed();

    List<Long> groupIds = collectGroupIds(userId);

    UserNewsFeed.WhenEnum whenEnum = resolveWhenEnum(when);
    result.setWhen(whenEnum);

    List<Long> activityIds = logService.getUserDashboardActivityIds();
    if (!groupIds.isEmpty() && !activityIds.isEmpty()) {
      populateNewsFeed(result, groupIds, activityIds, whenEnum);
    }

    return result;
  }

  /**
   * Collects the database identifiers of the Interest Groups the given user is a member of.
   *
   * @param userId the identifier of the user whose memberships are inspected
   * @return the list of {@code cm:node-dbid} values for the user's Interest Groups
   */
  private List<Long> collectGroupIds(String userId) {
    List<Long> groupIds = new ArrayList<>();
    for (InterestGroupProfile membership : usersApi.getUserMembership(userId)) {
      NodeRef groupRef = Converter.createNodeRefFromId(
        membership.getInterestGroup().getId()
      );
      groupIds.add(
        (Long) nodeService.getProperty(groupRef, ContentModel.PROP_NODE_DBID)
      );
    }
    return groupIds;
  }

  /**
   * Queries the dashboard activity for the given groups and time window, then aggregates it into the
   * supplied {@link UserNewsFeed}: it counts uploads, updates and comments and groups the individual
   * action logs into per-Interest-Group feeds. Invalid or unreadable activity nodes are skipped, and
   * comment nodes are resolved to their owning document before being exposed.
   *
   * @param result the news feed to populate (mutated in place)
   * @param groupIds the database identifiers of the Interest Groups to include
   * @param activityIds the identifiers of the activity types considered dashboard-relevant
   * @param whenEnum the resolved time window, or {@code null} for no time constraint
   */
  private void populateNewsFeed(
    UserNewsFeed result,
    List<Long> groupIds,
    List<Long> activityIds,
    UserNewsFeed.WhenEnum whenEnum
  ) {
    UserNewsFeedRequest request = new UserNewsFeedRequest();
    request.setIgIds(groupIds);
    request.setActivityIds(activityIds);
    if (whenEnum != null) {
      request.setWhen(whenEnum.toString());
    }

    long uploads = 0L;
    long updates = 0L;
    long comments = 0L;
    Map<String, InterestGroupFeed> groupFeeds = new HashMap<>();

    for (UserActionLogDAO activity : logService.getUserDashboardActivities(
      request
    )) {
      NodeRef nodeRef = nodeService.getNodeRef(
        Long.parseLong(activity.getDocumentId())
      );
      NodeRef igRef = nodeService.getNodeRef(
        Long.parseLong(activity.getIgId())
      );

      if (!isValidActivityNode(nodeRef, igRef, activity)) {
        continue;
      }

      UserActionLog log = buildActionLog(activity, nodeRef, igRef);
      String category = log.getAction();

      if (UPLOAD.equals(category) || TRANSLATE.equals(category)) {
        uploads += 1;
      } else if (UPDATE.equals(category)) {
        updates += 1;
      } else if (COMMENT.equals(category)) {
        comments += 1;
        nodeRef = resolveCommentNode(nodeRef);
        log.setNode(nodesApi.getNode(nodeRef));
      }

      buildOrUpdateGroupFeed(groupFeeds, activity, igRef, log);
    }

    result.setUploads(uploads);
    result.setUpdates(updates);
    result.setComments(comments);
    result.getGroupFeeds().addAll(groupFeeds.values());
  }

  /**
   * Builds a {@link UserActionLog} from a raw activity record, resolving the associated node and
   * Interest Group and normalizing the raw action label to a dashboard category.
   *
   * @param activity the raw action-log record
   * @param nodeRef the resolved node the activity refers to
   * @param igRef the resolved Interest Group the activity belongs to
   * @return the populated {@link UserActionLog}
   */
  private UserActionLog buildActionLog(
    UserActionLogDAO activity,
    NodeRef nodeRef,
    NodeRef igRef
  ) {
    UserActionLog log = new UserActionLog();
    log.setIgNode(igRef.getId());
    log.setNode(nodesApi.getNode(nodeRef));
    log.setActionDate(Converter.convertDateToString(activity.getLogDate()));
    log.setUsername(activity.getUsername());
    log.setAction(categorizeAction(activity.getAction().toLowerCase()));
    return log;
  }

  /**
   * Resolves the document a comment belongs to. For comments on Library content the node is walked
   * two levels up its primary parent chain to reach the commented document; other nodes are returned
   * unchanged.
   *
   * @param nodeRef the comment node
   * @return the node representing the commented document
   */
  private NodeRef resolveCommentNode(NodeRef nodeRef) {
    if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
      nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
      nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
    }
    return nodeRef;
  }

  /**
   * Maps the {@code when} request parameter to the corresponding {@link UserNewsFeed.WhenEnum}.
   *
   * @param when the raw time-window parameter
   * @return the matching {@link UserNewsFeed.WhenEnum} ({@code WEEK}, {@code PREVIOUSWEEK} or
   *     {@code TODAY}), or {@code null} if the value does not match any known window
   */
  private UserNewsFeed.WhenEnum resolveWhenEnum(String when) {
    if (UserNewsFeed.WhenEnum.WEEK.toString().equals(when)) {
      return UserNewsFeed.WhenEnum.WEEK;
    } else if (UserNewsFeed.WhenEnum.PREVIOUSWEEK.toString().equals(when)) {
      return UserNewsFeed.WhenEnum.PREVIOUSWEEK;
    } else if (UserNewsFeed.WhenEnum.TODAY.toString().equals(when)) {
      return UserNewsFeed.WhenEnum.TODAY;
    }
    return null;
  }

  /**
   * Determines whether an activity's node may be shown on the dashboard. The node and its Interest
   * Group must both be non-null and still exist, the node must not be archived, must carry the
   * Library or Newsgroup aspect, and must be readable by the current user.
   *
   * @param nodeRef the resolved activity node, possibly {@code null}
   * @param igRef the resolved Interest Group node, possibly {@code null}
   * @param activity the raw action-log record providing the document identifier for the permission
   *     check
   * @return {@code true} if the activity is valid and readable, {@code false} otherwise
   */
  private boolean isValidActivityNode(
    NodeRef nodeRef,
    NodeRef igRef,
    UserActionLogDAO activity
  ) {
    return (
      nodeRef != null &&
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        activity.getDocumentId()
      ) &&
      !nodeRef.getStoreRef().equals(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE) &&
      igRef != null &&
      nodeService.exists(igRef) &&
      nodeService.exists(nodeRef) &&
      currentUserPermissionCheckerService.hasAlfrescoReadPermission(
        nodeRef.getId()
      ) &&
      (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY) ||
        nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP))
    );
  }

  /**
   * Normalizes a raw (lower-cased) action label from the logs into one of the dashboard categories
   * ({@link #UPLOAD}, {@link #TRANSLATE}, {@link #UPDATE} or {@link #COMMENT}).
   *
   * @param action the raw, lower-cased action label
   * @return the normalized dashboard category, or the original label if it maps to no known category
   */
  private String categorizeAction(String action) {
    if (action.equals(UPLOAD_DOCUMENT)) {
      return UPLOAD;
    }
    if (
      action.equals(ADD_TRANSLATION) || action.equals(ADD_CONTENT_TRANSLATION)
    ) {
      return TRANSLATE;
    }
    if (
      action.equals(UPDATE_DOCUMENT) ||
      action.equals(UPDATE_CONTENT_PROPERTIES) ||
      action.equals(UPDATE_CONTENT) ||
      action.equals(CHECKIN) ||
      action.equals(UPDATEDOCUMENT) ||
      action.equals(UPDATE_LOCK_DOCUMENT)
    ) {
      return UPDATE;
    }
    if (action.equals(CREATE_POST) || action.equals(ADD_REPLY)) {
      return COMMENT;
    }
    return action;
  }

  /**
   * Adds the given action log to the feed of its Interest Group, creating the
   * {@link InterestGroupFeed} (with its id, name and localized title) the first time the group is
   * encountered.
   *
   * @param groupFeeds the map of Interest Group id to its accumulated feed (mutated in place)
   * @param activity the raw action-log record providing the Interest Group id key
   * @param igRef the resolved Interest Group node
   * @param log the action log entry to append to the group's feed
   */
  private void buildOrUpdateGroupFeed(
    Map<String, InterestGroupFeed> groupFeeds,
    UserActionLogDAO activity,
    NodeRef igRef,
    UserActionLog log
  ) {
    if (groupFeeds.containsKey(activity.getIgId())) {
      groupFeeds.get(activity.getIgId()).getFeed().add(log);
    } else {
      InterestGroupFeed feed = new InterestGroupFeed();
      feed.setId(igRef.getId());
      feed.setName(
        nodeService.getProperty(igRef, ContentModel.PROP_NAME).toString()
      );
      Object title = nodeService.getProperty(igRef, ContentModel.PROP_TITLE);
      feed.setTitle(
        title != null
          ? Converter.toI18NProperty((MLText) title)
          : new I18nProperty()
      );
      feed.getFeed().add(log);
      groupFeeds.put(activity.getIgId(), feed);
    }
  }
}
