package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.db.UserActionLogDAO;
import io.swagger.model.db.UserNewsFeedRequest;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.Test;

public class DashboardApiImplTest {

  private DashboardApiImpl dashboardApi;
  private LogService logService;
  private NodeService nodeService;
  private CurrentUserPermissionCheckerService permissionChecker;
  private NodesApi nodesApi;
  private UsersApi usersApi;

  @Before
  public void setUp() throws Exception {
    dashboardApi = new DashboardApiImpl();
    logService = mock(LogService.class);
    nodeService = mock(NodeService.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    nodesApi = mock(NodesApi.class);
    usersApi = mock(UsersApi.class);

    setField(dashboardApi, "logService", logService);
    setField(dashboardApi, "nodeService", nodeService);
    setField(
      dashboardApi,
      "currentUserPermissionCheckerService",
      permissionChecker
    );
    setField(dashboardApi, "nodesApi", nodesApi);
    setField(dashboardApi, "usersApi", usersApi);
  }

  private void setField(Object target, String fieldName, Object value)
    throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  public void testDownloadsGet_whenValidNode_thenReturnsActionLog() {
    String userId = "testUser";
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    UserActionLogDAO dao = new UserActionLogDAO();
    dao.setDocumentId("100");
    dao.setIgId("200");
    dao.setLogDate(new Date());
    dao.setAction("download");

    when(logService.getRecentUserDownloads(userId, 10)).thenReturn(
      Collections.singletonList(dao)
    );
    when(nodeService.getNodeRef(100L)).thenReturn(nodeRef);
    when(nodeService.getNodeRef(200L)).thenReturn(igRef);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(permissionChecker.hasAlfrescoReadPermission("doc-id")).thenReturn(
      true
    );
    when(nodesApi.getNode(nodeRef)).thenReturn(new Node());

    List<UserActionLog> result = dashboardApi.usersUserIdDashboardDownloadsGet(
      userId
    );

    assertEquals(1, result.size());
    assertEquals("download", result.get(0).getAction());
    assertEquals("ig-id", result.get(0).getIgNode());
  }

  @Test
  public void testDownloadsGet_whenNodeInArchive_thenReturnsEmpty() {
    String userId = "testUser";
    NodeRef archivedRef = new NodeRef(
      StoreRef.STORE_REF_ARCHIVE_SPACESSTORE,
      "archived-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    UserActionLogDAO dao = new UserActionLogDAO();
    dao.setDocumentId("100");
    dao.setIgId("200");
    dao.setLogDate(new Date());

    when(logService.getRecentUserDownloads(userId, 10)).thenReturn(
      Collections.singletonList(dao)
    );
    when(nodeService.getNodeRef(100L)).thenReturn(archivedRef);
    when(nodeService.getNodeRef(200L)).thenReturn(igRef);

    List<UserActionLog> result = dashboardApi.usersUserIdDashboardDownloadsGet(
      userId
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testDownloadsGet_whenNodeRefNull_thenReturnsEmpty() {
    String userId = "testUser";

    UserActionLogDAO dao = new UserActionLogDAO();
    dao.setDocumentId("100");
    dao.setIgId("200");
    dao.setLogDate(new Date());

    when(logService.getRecentUserDownloads(userId, 10)).thenReturn(
      Collections.singletonList(dao)
    );
    when(nodeService.getNodeRef(100L)).thenReturn(null);

    List<UserActionLog> result = dashboardApi.usersUserIdDashboardDownloadsGet(
      userId
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testDownloadsGet_whenNoPermission_thenReturnsEmpty() {
    String userId = "testUser";
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    UserActionLogDAO dao = new UserActionLogDAO();
    dao.setDocumentId("100");
    dao.setIgId("200");
    dao.setLogDate(new Date());

    when(logService.getRecentUserDownloads(userId, 10)).thenReturn(
      Collections.singletonList(dao)
    );
    when(nodeService.getNodeRef(100L)).thenReturn(nodeRef);
    when(nodeService.getNodeRef(200L)).thenReturn(igRef);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(permissionChecker.hasAlfrescoReadPermission("doc-id")).thenReturn(
      false
    );

    List<UserActionLog> result = dashboardApi.usersUserIdDashboardDownloadsGet(
      userId
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testUploadsGet_whenValidNode_thenReturnsActionLog() {
    String userId = "testUser";
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );

    UserActionLogDAO dao = new UserActionLogDAO();
    dao.setDocumentId("100");
    dao.setIgId("200");
    dao.setLogDate(new Date());
    dao.setAction("upload");

    when(logService.getRecentUserUploads(userId, 10)).thenReturn(
      Collections.singletonList(dao)
    );
    when(nodeService.getNodeRef(100L)).thenReturn(nodeRef);
    when(nodeService.getNodeRef(200L)).thenReturn(igRef);
    when(
      nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
    ).thenReturn(true);
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(permissionChecker.hasAlfrescoReadPermission("doc-id")).thenReturn(
      true
    );
    when(nodesApi.getNode(nodeRef)).thenReturn(new Node());

    List<UserActionLog> result = dashboardApi.usersUserIdDashboardUploadsGet(
      userId
    );

    assertEquals(1, result.size());
    assertEquals("upload", result.get(0).getAction());
    assertEquals("ig-id", result.get(0).getIgNode());
  }

  @Test
  public void testUploadsGet_whenNoUploads_thenReturnsEmpty() {
    when(logService.getRecentUserUploads("user1", 10)).thenReturn(
      Collections.emptyList()
    );

    List<UserActionLog> result = dashboardApi.usersUserIdDashboardUploadsGet(
      "user1"
    );

    assertTrue(result.isEmpty());
  }

  @Test
  public void testNewsfeedGet_whenNoMemberships_thenReturnsEmptyFeed() {
    String userId = "testUser";
    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.emptyList()
    );
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.singletonList(1L)
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "today"
    );

    assertNotNull(result);
    assertEquals(UserNewsFeed.WhenEnum.TODAY, result.getWhen());
    assertTrue(result.getGroupFeeds().isEmpty());
  }

  @Test
  public void testNewsfeedGet_whenWeek_thenSetsWhenEnum() {
    String userId = "testUser";
    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.emptyList()
    );
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.emptyList()
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "week"
    );

    assertEquals(UserNewsFeed.WhenEnum.WEEK, result.getWhen());
  }

  @Test
  public void testNewsfeedGet_whenPreviousWeek_thenSetsWhenEnum() {
    String userId = "testUser";
    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.emptyList()
    );
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.emptyList()
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "previousWeek"
    );

    assertEquals(UserNewsFeed.WhenEnum.PREVIOUSWEEK, result.getWhen());
  }

  @Test
  public void testNewsfeedGet_whenInvalidWhen_thenWhenIsNull() {
    String userId = "testUser";
    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.emptyList()
    );
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.emptyList()
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "invalid"
    );

    assertNull(result.getWhen());
  }

  @Test
  public void testNewsfeedGet_whenActivitiesExist_thenPopulatesCounts() {
    String userId = "testUser";
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );

    InterestGroupProfile membership = new InterestGroupProfile();
    InterestGroup ig = new InterestGroup();
    ig.setId("ig-id");
    membership.setInterestGroup(ig);

    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.singletonList(membership)
    );
    when(
      nodeService.getProperty(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id"),
        ContentModel.PROP_NODE_DBID
      )
    ).thenReturn(300L);
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.singletonList(1L)
    );

    UserActionLogDAO activity = new UserActionLogDAO();
    activity.setDocumentId("100");
    activity.setIgId("300");
    activity.setAction("upload document");
    activity.setLogDate(new Date());
    activity.setUsername("someUser");

    when(
      logService.getUserDashboardActivities(any(UserNewsFeedRequest.class))
    ).thenReturn(Collections.singletonList(activity));
    when(nodeService.getNodeRef(100L)).thenReturn(docRef);
    when(nodeService.getNodeRef(300L)).thenReturn(igRef);
    when(permissionChecker.hasAlfrescoReadPermission("100")).thenReturn(true);
    when(permissionChecker.hasAlfrescoReadPermission("doc-id")).thenReturn(
      true
    );
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.exists(docRef)).thenReturn(true);
    when(nodeService.hasAspect(docRef, CircabcModel.ASPECT_LIBRARY)).thenReturn(
      true
    );
    when(nodesApi.getNode(docRef)).thenReturn(new Node());
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "Test IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "today"
    );

    assertEquals(Long.valueOf(1L), result.getUploads());
    assertEquals(Long.valueOf(0L), result.getUpdates());
    assertEquals(Long.valueOf(0L), result.getComments());
    assertEquals(1, result.getGroupFeeds().size());
    assertEquals("ig-id", result.getGroupFeeds().get(0).getId());
  }

  @Test
  public void testNewsfeedGet_whenUpdateAction_thenCountsAsUpdate() {
    String userId = "testUser";
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );

    InterestGroupProfile membership = new InterestGroupProfile();
    InterestGroup ig = new InterestGroup();
    ig.setId("ig-id");
    membership.setInterestGroup(ig);

    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.singletonList(membership)
    );
    when(
      nodeService.getProperty(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id"),
        ContentModel.PROP_NODE_DBID
      )
    ).thenReturn(300L);
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.singletonList(1L)
    );

    UserActionLogDAO activity = new UserActionLogDAO();
    activity.setDocumentId("100");
    activity.setIgId("300");
    activity.setAction("perform checkin");
    activity.setLogDate(new Date());
    activity.setUsername("someUser");

    when(
      logService.getUserDashboardActivities(any(UserNewsFeedRequest.class))
    ).thenReturn(Collections.singletonList(activity));
    when(nodeService.getNodeRef(100L)).thenReturn(docRef);
    when(nodeService.getNodeRef(300L)).thenReturn(igRef);
    when(permissionChecker.hasAlfrescoReadPermission("100")).thenReturn(true);
    when(permissionChecker.hasAlfrescoReadPermission("doc-id")).thenReturn(
      true
    );
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.exists(docRef)).thenReturn(true);
    when(nodeService.hasAspect(docRef, CircabcModel.ASPECT_LIBRARY)).thenReturn(
      true
    );
    when(nodesApi.getNode(docRef)).thenReturn(new Node());
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "Test IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "today"
    );

    assertEquals(Long.valueOf(0L), result.getUploads());
    assertEquals(Long.valueOf(1L), result.getUpdates());
    assertEquals(Long.valueOf(0L), result.getComments());
  }

  @Test
  public void testNewsfeedGet_whenCommentAction_thenCountsAsComment() {
    String userId = "testUser";
    NodeRef igRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "ig-id"
    );
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc-id"
    );
    NodeRef parentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "parent-id"
    );
    NodeRef grandParentRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "grandparent-id"
    );

    InterestGroupProfile membership = new InterestGroupProfile();
    InterestGroup ig = new InterestGroup();
    ig.setId("ig-id");
    membership.setInterestGroup(ig);

    when(usersApi.getUserMembership(userId)).thenReturn(
      Collections.singletonList(membership)
    );
    when(
      nodeService.getProperty(
        new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ig-id"),
        ContentModel.PROP_NODE_DBID
      )
    ).thenReturn(300L);
    when(logService.getUserDashboardActivityIds()).thenReturn(
      Collections.singletonList(1L)
    );

    UserActionLogDAO activity = new UserActionLogDAO();
    activity.setDocumentId("100");
    activity.setIgId("300");
    activity.setAction("create post");
    activity.setLogDate(new Date());
    activity.setUsername("someUser");

    when(
      logService.getUserDashboardActivities(any(UserNewsFeedRequest.class))
    ).thenReturn(Collections.singletonList(activity));
    when(nodeService.getNodeRef(100L)).thenReturn(docRef);
    when(nodeService.getNodeRef(300L)).thenReturn(igRef);
    when(permissionChecker.hasAlfrescoReadPermission("100")).thenReturn(true);
    when(permissionChecker.hasAlfrescoReadPermission("doc-id")).thenReturn(
      true
    );
    when(nodeService.exists(igRef)).thenReturn(true);
    when(nodeService.exists(docRef)).thenReturn(true);
    when(nodeService.hasAspect(docRef, CircabcModel.ASPECT_LIBRARY)).thenReturn(
      true
    );
    when(nodesApi.getNode(docRef)).thenReturn(new Node());

    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(parentRef);
    when(nodeService.getPrimaryParent(docRef)).thenReturn(parentAssoc);

    ChildAssociationRef grandParentAssoc = mock(ChildAssociationRef.class);
    when(grandParentAssoc.getParentRef()).thenReturn(grandParentRef);
    when(nodeService.getPrimaryParent(parentRef)).thenReturn(grandParentAssoc);

    when(nodesApi.getNode(grandParentRef)).thenReturn(new Node());
    when(nodeService.getProperty(igRef, ContentModel.PROP_NAME)).thenReturn(
      "Test IG"
    );
    when(nodeService.getProperty(igRef, ContentModel.PROP_TITLE)).thenReturn(
      null
    );

    UserNewsFeed result = dashboardApi.usersUserIdDashboardNewsfeedGet(
      userId,
      "today"
    );

    assertEquals(Long.valueOf(0L), result.getUploads());
    assertEquals(Long.valueOf(0L), result.getUpdates());
    assertEquals(Long.valueOf(1L), result.getComments());
  }
}
