package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.app.CircabcDaoServiceImpl;
import eu.europa.ec.digit.circabc.rest.service.app.CircabcService;
import eu.europa.ec.digit.circabc.rest.service.customization.logo.DefaultLogoConfigurationImpl;
import eu.europa.ec.digit.circabc.rest.service.customization.logo.LogoPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.iam.SynchronizationService;
import eu.europa.ec.digit.circabc.rest.service.log.LogService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailPreferencesService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import eu.europa.ec.digit.circabc.rest.service.mail.MailWrapper;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsParameter;
import eu.europa.ec.digit.circabc.rest.service.statistic.ig.IgStatisticsService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.config.CircabcConfig;
import io.swagger.model.*;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.ModerationModel;
import io.swagger.model.db.InterestGroupResult;
import io.swagger.model.db.UserWithProfile;
import io.swagger.util.ApiToolBox;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class GroupsApiImplTest {

  private static final String TEST_IG_ID =
    "00000000-0000-0000-0000-000000000001";
  private static final NodeRef TEST_IG_REF = new NodeRef(
    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
    TEST_IG_ID
  );

  private GroupsApiImpl groupsApi;
  private NodeService nodeService;
  private PermissionService permissionService;
  private CircabcService circabcService;
  private LogoPreferencesService logoPreferencesService;
  private AuthenticationService authenticationService;
  private LogService logService;
  private CircabcDaoServiceImpl circabcDaoService;
  private AuthorityService authorityService;

  @Before
  public void setUp() throws Exception {
    Field initialized = AuthenticationUtil.class.getDeclaredField(
      "initialized"
    );
    initialized.setAccessible(true);
    initialized.set(null, true);
    Field guest = AuthenticationUtil.class.getDeclaredField(
      "defaultGuestUserName"
    );
    guest.setAccessible(true);
    guest.set(null, "guest");
    AuthenticationUtil.setFullyAuthenticatedUser("testuser");

    groupsApi = new GroupsApiImpl();
    nodeService = mock(NodeService.class);
    permissionService = mock(PermissionService.class);
    circabcService = mock(CircabcService.class);
    logoPreferencesService = mock(LogoPreferencesService.class);
    authenticationService = mock(AuthenticationService.class);
    logService = mock(LogService.class);
    circabcDaoService = mock(CircabcDaoServiceImpl.class);
    authorityService = mock(AuthorityService.class);

    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("circabcService", circabcService);
    setField("logoPreferencesService", logoPreferencesService);
    setField("authenticationService", authenticationService);
    setField("logService", logService);
    setField("circabcDaoService", circabcDaoService);
    setField("authorityService", authorityService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(groupsApi, value);
  }

  // --- getInterestGroup ---

  @Test
  public void testGetInterestGroup_whenNotIG_thenReturnsNull() {
    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    InterestGroup result = groupsApi.getInterestGroup(TEST_IG_ID);

    assertNull(result);
  }

  @Test
  public void testGetInterestGroup_whenIsIG_thenReturnsInterestGroup()
    throws Exception {
    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestIG");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Test Title");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("Test Description");
    when(
      nodeService.getProperty(
        TEST_IG_REF,
        CircabcModel.PROP_CONTACT_INFORMATION
      )
    ).thenReturn("Contact");

    InterestGroupResult igResult = new InterestGroupResult();
    igResult.setIsPublic(true);
    igResult.setIsRegistered(false);
    igResult.setIsApplyForMembership(true);
    when(circabcService.getInterestGroup(TEST_IG_REF)).thenReturn(igResult);

    DefaultLogoConfigurationImpl logoConfig = mock(
      DefaultLogoConfigurationImpl.class
    );
    when(logoConfig.isLogoDisplayedOnMainPage()).thenReturn(false);
    when(
      logoPreferencesService.getOrCreateConfiguraton(TEST_IG_REF, false)
    ).thenReturn(logoConfig);

    // Mock service child nodes for permission checks
    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib-id"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "inf-id"
    );
    NodeRef eventRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "event-id"
    );
    NodeRef newsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "news-id"
    );
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Library"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(infRef);
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(eventRef);
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(newsRef);

    // All permissions denied
    when(
      permissionService.hasPermission(any(NodeRef.class), anyString())
    ).thenReturn(AccessStatus.DENIED);

    when(nodeService.getChildAssocs(TEST_IG_REF)).thenReturn(
      Collections.emptyList()
    );

    InterestGroup result = groupsApi.getInterestGroup(TEST_IG_ID);

    assertNotNull(result);
    assertEquals(TEST_IG_ID, result.getId());
    assertEquals("TestIG", result.getName());
    assertTrue(result.getIsPublic());
    assertFalse(result.getIsRegistered());
    assertTrue(result.getAllowApply());
  }

  @Test
  public void testGetInterestGroup_whenLightMode_thenSkipsDetails() {
    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("LightIG");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Light Title");

    InterestGroup result = groupsApi.getInterestGroup(TEST_IG_ID, true);

    assertNotNull(result);
    assertEquals("LightIG", result.getName());
    // In light mode, description is not populated
    verify(nodeService, never()).getProperty(
      TEST_IG_REF,
      ContentModel.PROP_DESCRIPTION
    );
  }

  // --- groupsIdMembersApplicantsGet ---

  @Test
  public void testGroupsIdMembersApplicantsGet_whenNoApplicants_thenReturnsEmptyList() {
    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn(null);

    List<Applicant> result = groupsApi.groupsIdMembersApplicantsGet(TEST_IG_ID);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGroupsIdMembersApplicantsGet_whenHasApplicants_thenReturnsSortedList()
    throws Exception {
    UsersApi usersApi = mock(UsersApi.class);
    setField("usersApi", usersApi);

    io.swagger.model.alfresco.Applicant app1 =
      new io.swagger.model.alfresco.Applicant(
        "user1",
        new Date(1000000),
        "Please add me"
      );

    io.swagger.model.alfresco.Applicant app2 =
      new io.swagger.model.alfresco.Applicant(
        "user2",
        new Date(2000000),
        "I want to join"
      );

    Map<String, io.swagger.model.alfresco.Applicant> applicantMap =
      new HashMap<>();
    applicantMap.put("user1", app1);
    applicantMap.put("user2", app2);

    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn((java.io.Serializable) applicantMap);

    io.swagger.model.User mockUser1 = new io.swagger.model.User();
    mockUser1.setUserId("user1");
    io.swagger.model.User mockUser2 = new io.swagger.model.User();
    mockUser2.setUserId("user2");
    when(usersApi.usersUserIdGet("user1")).thenReturn(mockUser1);
    when(usersApi.usersUserIdGet("user2")).thenReturn(mockUser2);

    List<Applicant> result = groupsApi.groupsIdMembersApplicantsGet(TEST_IG_ID);

    assertEquals(2, result.size());
    // Sorted by submitted date descending (most recent first)
    assertEquals("user2", result.get(0).getUser().getUserId());
    assertEquals("user1", result.get(1).getUser().getUserId());
  }

  // --- countMembersInIg ---

  @Test
  public void testCountMembersInIg_whenCalled_thenDelegatesToService() {
    when(circabcService.countMembersInIg("some-ig-id")).thenReturn(42);

    int count = groupsApi.countMembersInIg("some-ig-id");

    assertEquals(42, count);
    verify(circabcService).countMembersInIg("some-ig-id");
  }

  // --- getVisitedGroups ---

  @Test(expected = IllegalAccessError.class)
  public void testGetVisitedGroups_whenNoUsername_thenThrows() {
    when(authenticationService.getCurrentUserName()).thenReturn(null);

    groupsApi.getVisitedGroups(5);
  }

  @Test
  public void testGetVisitedGroups_whenNoLogs_thenReturnsEmptyList() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");
    when(logService.getVisitedIGRestLogs("testuser")).thenReturn(
      Collections.emptyList()
    );

    List<InterestGroup> result = groupsApi.getVisitedGroups(5);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetVisitedGroups_whenNodeDoesNotExist_thenSkipsIt() {
    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    List<String> logs = new ArrayList<>();
    logs.add(TEST_IG_ID);
    when(logService.getVisitedIGRestLogs("testuser")).thenReturn(logs);
    when(nodeService.exists(TEST_IG_REF)).thenReturn(false);

    List<InterestGroup> result = groupsApi.getVisitedGroups(5);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetVisitedGroups_whenAmountLimits_thenRespectsLimit() {
    String id2 = "00000000-0000-0000-0000-000000000002";
    NodeRef ref2 = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id2);

    when(authenticationService.getCurrentUserName()).thenReturn("testuser");

    List<String> logs = new ArrayList<>();
    logs.add(TEST_IG_ID);
    logs.add(id2);
    when(logService.getVisitedIGRestLogs("testuser")).thenReturn(logs);

    when(nodeService.exists(TEST_IG_REF)).thenReturn(true);
    when(nodeService.exists(ref2)).thenReturn(true);
    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(nodeService.hasAspect(ref2, CircabcModel.ASPECT_IGROOT)).thenReturn(
      true
    );
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("IG1");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Title1");
    when(nodeService.getProperty(ref2, ContentModel.PROP_NAME)).thenReturn(
      "IG2"
    );
    when(nodeService.getProperty(ref2, ContentModel.PROP_TITLE)).thenReturn(
      "Title2"
    );

    // getInterestGroupDetails calls populateInterestGroupDetails which needs more mocks
    // Use light mode indirectly - the method calls getInterestGroupDetails(ref, false)
    // We need to mock enough for it not to fail
    mockInterestGroupDetailsMinimal(TEST_IG_REF);
    mockInterestGroupDetailsMinimal(ref2);

    List<InterestGroup> result = groupsApi.getVisitedGroups(1);

    assertEquals(1, result.size());
  }

  // --- updateIgToBeDeleted ---

  @Test
  public void testUpdateIgToBeDeleted_whenCalled_thenDelegatesToDao() {
    groupsApi.updateIgToBeDeleted(123L, true);

    verify(circabcDaoService).updateIgToBeDeleted(123L, true);
  }

  // --- groupsIdMembersUserIdExpirationDelete ---

  @Test
  public void testGroupsIdMembersUserIdExpirationDelete_whenCalled_thenDelegates()
    throws Exception {
    HistoryApi historyApi = mock(HistoryApi.class);
    setField("historyApi", historyApi);

    groupsApi.groupsIdMembersUserIdExpirationDelete("group1", "user1");

    verify(historyApi).deleteExpirationDate("user1", "group1");
  }

  // --- groupsIdMembersUserIdExpirationPut ---

  @Test
  public void testGroupsIdMembersUserIdExpirationPut_whenCalled_thenDelegates()
    throws Exception {
    HistoryApi historyApi = mock(HistoryApi.class);
    setField("historyApi", historyApi);

    Date expDate = new Date();
    groupsApi.groupsIdMembersUserIdExpirationPut("group1", "user1", expDate);

    verify(historyApi).updateExpirationDate("group1", "user1", expDate);
  }

  // --- getGroupDashboard ---

  @Test
  public void testGetGroupDashboard_whenNotIG_thenReturnsNull() {
    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    GroupDashboard result = groupsApi.getGroupDashboard(TEST_IG_ID);

    assertNull(result);
  }

  @Test
  public void testGetGroupDashboard_whenIsIG_thenReturnsDashboard()
    throws Exception {
    SearchService searchService = mock(SearchService.class);
    NodesApi nodesApi = mock(NodesApi.class);
    ContentService contentService = mock(ContentService.class);
    ApiToolBox apiToolBox = mock(ApiToolBox.class);
    setField("searchService", searchService);
    setField("nodesApi", nodesApi);
    setField("contentService", contentService);
    setField("apiToolBox", apiToolBox);

    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestIG");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Title");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("Desc");
    when(
      nodeService.getProperty(
        TEST_IG_REF,
        CircabcModel.PROP_CONTACT_INFORMATION
      )
    ).thenReturn("Contact");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_CREATED)
    ).thenReturn(new Date());
    when(circabcService.getInterestGroup(TEST_IG_REF)).thenReturn(null);
    when(
      logoPreferencesService.getOrCreateConfiguraton(TEST_IG_REF, false)
    ).thenReturn(null);

    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "inf"
    );
    NodeRef evtRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "evt"
    );
    NodeRef nwsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "nws"
    );
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Library"
      )
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(infRef);
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Events"
      )
    ).thenReturn(evtRef);
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(nwsRef);
    when(
      permissionService.hasPermission(any(NodeRef.class), anyString())
    ).thenReturn(AccessStatus.DENIED);
    when(nodeService.getChildAssocs(TEST_IG_REF)).thenReturn(
      Collections.emptyList()
    );

    // Mock search returning one content node
    ResultSet rs = mock(ResultSet.class);
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc1"
    );
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(docRef));
    when(searchService.query(any())).thenReturn(rs);
    when(nodeService.exists(docRef)).thenReturn(true);
    when(nodeService.getType(docRef)).thenReturn(ContentModel.TYPE_CONTENT);

    Date now = new Date();
    when(nodeService.getProperty(docRef, ContentModel.PROP_CREATED)).thenReturn(
      now
    );
    when(
      nodeService.getProperty(docRef, ContentModel.PROP_MODIFIED)
    ).thenReturn(now);

    Node mockNode = new Node();
    mockNode.setProperties(new HashMap<>());
    when(nodesApi.getNode(docRef)).thenReturn(mockNode);

    GroupDashboard result = groupsApi.getGroupDashboard(TEST_IG_ID);

    assertNotNull(result);
    assertNotNull(result.getGroup());
    assertNotNull(result.getEntries());
    assertEquals(1, result.getEntries().size());
    assertEquals(1, result.getEntries().get(0).getNews().size());
    assertEquals(
      "create",
      result.getEntries().get(0).getNews().get(0).getType()
    );
  }

  @Test
  public void testGetGroupDashboard_whenModifiedDiffersFromCreated_thenTypeIsUpdate()
    throws Exception {
    SearchService searchService = mock(SearchService.class);
    NodesApi nodesApi = mock(NodesApi.class);
    ContentService contentService = mock(ContentService.class);
    ApiToolBox apiToolBox = mock(ApiToolBox.class);
    setField("searchService", searchService);
    setField("nodesApi", nodesApi);
    setField("contentService", contentService);
    setField("apiToolBox", apiToolBox);

    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("IG");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_TITLE)
    ).thenReturn("T");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("D");
    when(
      nodeService.getProperty(
        TEST_IG_REF,
        CircabcModel.PROP_CONTACT_INFORMATION
      )
    ).thenReturn("C");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_CREATED)
    ).thenReturn(new Date());
    when(circabcService.getInterestGroup(TEST_IG_REF)).thenReturn(null);
    when(
      logoPreferencesService.getOrCreateConfiguraton(TEST_IG_REF, false)
    ).thenReturn(null);

    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "lib2"
    );
    when(
      nodeService.getChildByName(
        eq(TEST_IG_REF),
        eq(ContentModel.ASSOC_CONTAINS),
        anyString()
      )
    ).thenReturn(libRef);
    when(
      permissionService.hasPermission(any(NodeRef.class), anyString())
    ).thenReturn(AccessStatus.DENIED);
    when(nodeService.getChildAssocs(TEST_IG_REF)).thenReturn(
      Collections.emptyList()
    );

    ResultSet rs = mock(ResultSet.class);
    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "doc2"
    );
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(docRef));
    when(searchService.query(any())).thenReturn(rs);
    when(nodeService.exists(docRef)).thenReturn(true);
    when(nodeService.getType(docRef)).thenReturn(ContentModel.TYPE_CONTENT);

    Date created = new Date(1000000);
    Date modified = new Date(2000000);
    when(nodeService.getProperty(docRef, ContentModel.PROP_CREATED)).thenReturn(
      created
    );
    when(
      nodeService.getProperty(docRef, ContentModel.PROP_MODIFIED)
    ).thenReturn(modified);

    Node mockNode = new Node();
    mockNode.setProperties(new HashMap<>());
    when(nodesApi.getNode(docRef)).thenReturn(mockNode);

    GroupDashboard result = groupsApi.getGroupDashboard(TEST_IG_ID);

    assertEquals(
      "update",
      result.getEntries().get(0).getNews().get(0).getType()
    );
  }

  // --- getGroupRecentDiscussions ---

  @Test
  public void testGetGroupRecentDiscussions_whenNotIG_thenReturnsEmpty()
    throws Exception {
    SearchService searchService = mock(SearchService.class);
    setField("searchService", searchService);

    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(false);

    List<RecentDiscussion> result = groupsApi.getGroupRecentDiscussions(
      TEST_IG_ID
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetGroupRecentDiscussions_whenIsIG_thenReturnsDiscussions()
    throws Exception {
    SearchService searchService = mock(SearchService.class);
    NodesApi nodesApi = mock(NodesApi.class);
    ContentService contentService = mock(ContentService.class);
    ApiToolBox apiToolBox = mock(ApiToolBox.class);
    setField("searchService", searchService);
    setField("nodesApi", nodesApi);
    setField("contentService", contentService);
    setField("apiToolBox", apiToolBox);

    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);

    NodeRef forumRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "forum"
    );
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(forumRef);
    when(apiToolBox.getPathFromSpaceRef(forumRef, true)).thenReturn(
      "/app:company_home/forum"
    );

    ResultSet rs = mock(ResultSet.class);
    NodeRef postRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "post1"
    );
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(postRef));
    when(searchService.query(any())).thenReturn(rs);

    NodeRef topicRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "topic1"
    );
    ChildAssociationRef parentAssoc = mock(ChildAssociationRef.class);
    when(parentAssoc.getParentRef()).thenReturn(topicRef);
    when(nodeService.getPrimaryParent(postRef)).thenReturn(parentAssoc);
    when(
      nodeService.hasAspect(postRef, ModerationModel.ASPECT_WAITING_APPROVAL)
    ).thenReturn(false);

    Node postNode = new Node();
    postNode.setProperties(new HashMap<>());
    Node topicNode = new Node();
    when(nodesApi.getNode(postRef)).thenReturn(postNode);
    when(nodesApi.getNode(topicRef)).thenReturn(topicNode);

    ContentReader reader = mock(ContentReader.class);
    when(reader.getContentString()).thenReturn("<p>Hello world</p>");
    when(
      contentService.getReader(postRef, ContentModel.PROP_CONTENT)
    ).thenReturn(reader);

    List<RecentDiscussion> result = groupsApi.getGroupRecentDiscussions(
      TEST_IG_ID
    );

    assertEquals(1, result.size());
    assertNotNull(result.get(0).getPost());
    assertNotNull(result.get(0).getTopic());
  }

  // --- groupsIdMembersGet (paged) ---

  @Test
  public void testGroupsIdMembersGet_paged_whenUsersExist_thenReturnsPaged()
    throws Exception {
    UsersApi usersApi = mock(UsersApi.class);
    HistoryApi historyApi = mock(HistoryApi.class);
    setField("usersApi", usersApi);
    setField("historyApi", historyApi);

    Map<String, Long> localeMap = new HashMap<>();
    localeMap.put("en_", 1L);
    when(circabcDaoService.getAllAlfrescoLocale()).thenReturn(localeMap);

    UserWithProfile uwp = new UserWithProfile();
    uwp.setUserName("user1");
    uwp.setProfileId(100L);
    uwp.setProfileName("Access");
    uwp.setAlfrescoGroup("access_group");
    uwp.setDirectoryPermission("DirAccess");
    uwp.setLibraryPermission("LibAccess");
    uwp.setEventPermission("EveAccess");
    uwp.setNewsgroupPermission("NwsAccess");
    uwp.setInformationPermission("InfAccess");

    List<UserWithProfile> users = new ArrayList<>();
    users.add(uwp);
    when(
      circabcService.getFilteredUsers(
        eq(TEST_IG_REF),
        anyLong(),
        anyString(),
        anyString(),
        any()
      )
    ).thenReturn(users);

    NodeRef profileRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "prof-id"
    );
    when(nodeService.getNodeRef(100L)).thenReturn(profileRef);

    Map<String, String> titleMap = new HashMap<>();
    titleMap.put("en", "Access Profile");
    when(circabcService.getProfileTitle(profileRef)).thenReturn(titleMap);

    when(historyApi.getAutoExpiredUsers(TEST_IG_ID)).thenReturn(
      Collections.emptyMap()
    );

    User mockUser = new User();
    mockUser.setUserId("user1");
    when(usersApi.usersUserIdGet("user1")).thenReturn(mockUser);

    PagedUserProfile result = groupsApi.groupsIdMembersGet(
      TEST_IG_ID,
      null,
      "en",
      10,
      1,
      null,
      (String) null
    );

    assertNotNull(result);
    assertEquals(Integer.valueOf(1), result.getTotal());
    assertEquals(1, result.getData().size());
    assertEquals("user1", result.getData().get(0).getUser().getUserId());
  }

  // --- groupsIdMembersPost ---

  @Test
  public void testGroupsIdMembersPost_whenNotENT_thenInvitesMember()
    throws Exception {
    UsersApi usersApi = mock(UsersApi.class);
    HistoryApi historyApi = mock(HistoryApi.class);
    CircabcConfig circabcConfig = mock(CircabcConfig.class);
    PersonService personService = mock(PersonService.class);
    SynchronizationService synchronizationService = mock(
      SynchronizationService.class
    );
    setField("usersApi", usersApi);
    setField("historyApi", historyApi);
    setField("circabcConfig", circabcConfig);
    setField("personService", personService);
    setField("synchronizationService", synchronizationService);

    when(circabcConfig.isENT()).thenReturn(false);
    when(personService.personExists("newuser")).thenReturn(true);
    when(usersApi.getUserMembership("newuser")).thenReturn(
      Collections.emptyList()
    );
    when(circabcService.isUserExists("newuser")).thenReturn(true);

    User user = new User();
    user.setUserId("newuser");
    Profile profile = new Profile();
    profile.setGroupName("GROUP_access");
    profile.setName("Access");
    profile.setId("prof-id");
    UserProfile up = new UserProfile();
    up.setUser(user);
    up.setProfile(profile);

    MembershipPostDefinition body = new MembershipPostDefinition();
    body.setMemberships(Collections.singletonList(up));
    body.setUserNotifications(false);
    body.setAdminNotifications(false);

    MembershipPostDefinition result = groupsApi.groupsIdMembersPost(
      TEST_IG_REF,
      body
    );

    assertNotNull(result);
    assertEquals(1, result.getMemberships().size());
    verify(authorityService).addAuthority("GROUP_access", "newuser");
  }

  // --- groupsIdMembersPut ---

  @Test
  public void testGroupsIdMembersPut_whenUserIsMember_thenChangesProfile()
    throws Exception {
    UsersApi usersApi = mock(UsersApi.class);
    HistoryApi historyApi = mock(HistoryApi.class);
    PersonService personService = mock(PersonService.class);
    CircabcConfig circabcConfig = mock(CircabcConfig.class);
    setField("usersApi", usersApi);
    setField("historyApi", historyApi);
    setField("personService", personService);
    setField("circabcConfig", circabcConfig);

    when(personService.personExists("user1")).thenReturn(true);

    // isAlreadyMember check
    InterestGroupProfile igProfile = new InterestGroupProfile();
    InterestGroup ig = new InterestGroup();
    ig.setId(TEST_IG_ID);
    igProfile.setInterestGroup(ig);
    Profile existingProfile = new Profile();
    existingProfile.setGroupName("GROUP_old_profile");
    igProfile.setProfile(existingProfile);
    when(usersApi.getUserMembership("user1")).thenReturn(
      Collections.singletonList(igProfile)
    );
    when(usersApi.getUserMembership("user1", false)).thenReturn(
      Collections.singletonList(igProfile)
    );

    User user = new User();
    user.setUserId("user1");
    Profile newProfile = new Profile();
    newProfile.setGroupName("GROUP_new_profile");
    newProfile.setName("NewProfile");
    UserProfile up = new UserProfile();
    up.setUser(user);
    up.setProfile(newProfile);

    MembershipPostDefinition body = new MembershipPostDefinition();
    body.setMemberships(Collections.singletonList(up));
    body.setUserNotifications(false);
    body.setAdminNotifications(false);

    MembershipPostDefinition result = groupsApi.groupsIdMembersPut(
      TEST_IG_REF,
      body
    );

    assertNotNull(result);
    assertEquals(1, result.getMemberships().size());
    verify(circabcService).changePersonProfile(
      TEST_IG_REF,
      "user1",
      "NewProfile"
    );
  }

  // --- groupsIdMembersApplicantsPut ---

  @Test
  public void testGroupsIdMembersApplicantsPut_whenCleanAction_thenRemovesApplicant()
    throws Exception {
    Map<String, io.swagger.model.alfresco.Applicant> applicantMap =
      new HashMap<>();
    applicantMap.put(
      "user1",
      new io.swagger.model.alfresco.Applicant("user1", new Date(), "msg")
    );
    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn((java.io.Serializable) applicantMap);

    ApplicantAction body = new ApplicantAction();
    body.setAction("clean");
    body.setUsername("user1");
    body.setMessage("");

    groupsApi.groupsIdMembersApplicantsPut(TEST_IG_ID, body);

    verify(nodeService).setProperty(
      eq(TEST_IG_REF),
      eq(CircabcModel.PROP_APPLICANTS),
      any()
    );
  }

  // --- groupsIdMembersApplicantsPost ---

  @Test
  public void testGroupsIdMembersApplicantsPost_whenSubmitNew_thenAddsApplicant()
    throws Exception {
    ProfilesApi profilesApi = mock(ProfilesApi.class);
    PersonService personService = mock(PersonService.class);
    setField("profilesApi", profilesApi);
    setField("personService", personService);

    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn(null);
    when(profilesApi.groupsIdProfilesGet(TEST_IG_ID, null, false)).thenReturn(
      Collections.emptyList()
    );

    ApplicantAction body = new ApplicantAction();
    body.setAction("submitNew");
    body.setUsername("applicant1");
    body.setMessage("Please add me");

    groupsApi.groupsIdMembersApplicantsPost(TEST_IG_ID, body);

    verify(nodeService).setProperty(
      eq(TEST_IG_REF),
      eq(CircabcModel.PROP_APPLICANTS),
      any()
    );
  }

  // --- getIGSummaryStatistics ---

  @Test
  public void testGetIGSummaryStatistics_whenCalculateFalseAndNoFile_thenReturnsEmpty()
    throws Exception {
    ContentService contentService = mock(ContentService.class);
    setField("contentService", contentService);

    // readSummaryContent: no child node found -> returns null -> method returns empty list
    when(
      nodeService.getChildByName(
        TEST_IG_REF,
        ContentModel.ASSOC_CONTAINS,
        "statistics.json"
      )
    ).thenReturn(null);

    List<StatData> result = groupsApi.getIGSummaryStatistics(
      TEST_IG_ID,
      false,
      false
    );

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  private void mockInterestGroupDetailsMinimal(NodeRef igRef) {
    when(
      nodeService.getProperty(igRef, ContentModel.PROP_DESCRIPTION)
    ).thenReturn("desc");
    when(
      nodeService.getProperty(igRef, CircabcModel.PROP_CONTACT_INFORMATION)
    ).thenReturn("contact");
    when(circabcService.getInterestGroup(igRef)).thenReturn(null);
    try {
      when(
        logoPreferencesService.getOrCreateConfiguraton(igRef, false)
      ).thenReturn(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    NodeRef libRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igRef.getId() + "-lib"
    );
    NodeRef infRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igRef.getId() + "-inf"
    );
    NodeRef eventRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igRef.getId() + "-evt"
    );
    NodeRef newsRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      igRef.getId() + "-nws"
    );
    when(
      nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Library")
    ).thenReturn(libRef);
    when(
      nodeService.getChildByName(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        "Information"
      )
    ).thenReturn(infRef);
    when(
      nodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, "Events")
    ).thenReturn(eventRef);
    when(
      nodeService.getChildByName(
        igRef,
        ContentModel.ASSOC_CONTAINS,
        "Newsgroups"
      )
    ).thenReturn(newsRef);
    when(
      permissionService.hasPermission(any(NodeRef.class), anyString())
    ).thenReturn(AccessStatus.DENIED);
    when(nodeService.getChildAssocs(igRef)).thenReturn(Collections.emptyList());
  }

  // --- groupsIdDelete ---

  @Test
  public void testGroupsIdDelete_whenPurgeDataTrue_thenAddsAspectAndDeletes()
    throws Exception {
    RuleService ruleService = mock(RuleService.class);
    NodeArchiveService nodeArchiveService = mock(NodeArchiveService.class);
    setField("ruleService", ruleService);
    setField("nodeArchiveService", nodeArchiveService);

    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(100L);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestIG");
    when(nodeService.getPath(TEST_IG_REF)).thenReturn(
      new org.alfresco.service.cmr.repository.Path()
    );
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
    when(nodeArchiveService.getArchivedNode(TEST_IG_REF)).thenReturn(null);

    groupsApi.groupsIdDelete(TEST_IG_ID, true, false);

    verify(nodeService).addAspect(
      TEST_IG_REF,
      ContentModel.ASPECT_TEMPORARY,
      null
    );
    verify(nodeService).deleteNode(TEST_IG_REF);
    verify(ruleService).disableRules();
    verify(ruleService).enableRules();
  }

  @Test
  public void testGroupsIdDelete_whenPurgeLogTrue_thenDeletesLogs()
    throws Exception {
    RuleService ruleService = mock(RuleService.class);
    NodeArchiveService nodeArchiveService = mock(NodeArchiveService.class);
    setField("ruleService", ruleService);
    setField("nodeArchiveService", nodeArchiveService);

    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(200L);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestIG");
    when(nodeService.getPath(TEST_IG_REF)).thenReturn(
      new org.alfresco.service.cmr.repository.Path()
    );
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
    when(nodeArchiveService.getArchivedNode(TEST_IG_REF)).thenReturn(null);

    groupsApi.groupsIdDelete(TEST_IG_ID, false, true);

    verify(logService).deleteInterestgroupLog(200L);
  }

  @Test
  public void testGroupsIdDelete_whenExceptionThrown_thenLogsFailure()
    throws Exception {
    RuleService ruleService = mock(RuleService.class);
    NodeArchiveService nodeArchiveService = mock(NodeArchiveService.class);
    setField("ruleService", ruleService);
    setField("nodeArchiveService", nodeArchiveService);

    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NODE_DBID)
    ).thenReturn(300L);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("TestIG");
    when(nodeService.getPath(TEST_IG_REF)).thenReturn(
      new org.alfresco.service.cmr.repository.Path()
    );
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
    doThrow(new RuntimeException("fail"))
      .when(nodeService)
      .deleteNode(TEST_IG_REF);

    groupsApi.groupsIdDelete(TEST_IG_ID, false, false);

    verify(ruleService).enableRules();
    verify(logService).log(any());
  }

  // --- groupsIdPut ---

  @Test
  public void testGroupsIdPut_whenNameProvided_thenUpdatesName()
    throws Exception {
    ProfilesApi profilesApi = mock(ProfilesApi.class);
    setField("profilesApi", profilesApi);

    InterestGroup body = new InterestGroup();
    body.setName("NewName");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setContact(new I18nProperty());
    body.setAllowApply(false);
    body.setIsPublic(null);
    body.setIsRegistered(null);

    groupsApi.groupsIdPut(TEST_IG_ID, body);

    verify(nodeService).setProperty(
      TEST_IG_REF,
      ContentModel.PROP_NAME,
      "NewName"
    );
  }

  @Test
  public void testGroupsIdPut_whenPublicTrue_thenUpdatesVisibility()
    throws Exception {
    ProfilesApi profilesApi = mock(ProfilesApi.class);
    setField("profilesApi", profilesApi);

    Profile guestProfile = new Profile();
    guestProfile.setId("00000000-0000-0000-0000-000000000010");
    guestProfile.setPermissions(new HashMap<>());
    when(
      profilesApi.groupsIdProfilesGet(TEST_IG_ID, "guest", false)
    ).thenReturn(Collections.singletonList(guestProfile));

    InterestGroup body = new InterestGroup();
    body.setName("");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setContact(new I18nProperty());
    body.setAllowApply(true);
    body.setIsPublic(true);
    body.setIsRegistered(null);

    groupsApi.groupsIdPut(TEST_IG_ID, body);

    verify(circabcService).updateInterestGroupPublic(TEST_IG_REF, true);
    verify(permissionService).setPermission(
      TEST_IG_REF,
      "guest",
      "Visibility",
      true
    );
  }

  @Test
  public void testGroupsIdPut_whenPublicTrueAndRegisteredFalse_thenForcesRegisteredTrue()
    throws Exception {
    ProfilesApi profilesApi = mock(ProfilesApi.class);
    setField("profilesApi", profilesApi);

    Profile guestProfile = new Profile();
    guestProfile.setId("00000000-0000-0000-0000-000000000010");
    guestProfile.setPermissions(new HashMap<>());
    when(
      profilesApi.groupsIdProfilesGet(eq(TEST_IG_ID), anyString(), eq(false))
    ).thenReturn(Collections.singletonList(guestProfile));

    InterestGroup body = new InterestGroup();
    body.setName("");
    body.setTitle(new I18nProperty());
    body.setDescription(new I18nProperty());
    body.setContact(new I18nProperty());
    body.setAllowApply(false);
    body.setIsPublic(true);
    body.setIsRegistered(false);

    groupsApi.groupsIdPut(TEST_IG_ID, body);

    // When public=true and registered=false, registered should be forced to true
    verify(circabcService).updateInterestGroupRegistered(TEST_IG_REF, true);
  }

  // --- groupsIdMembersUserIdDelete ---

  @Test
  public void testGroupsIdMembersUserIdDelete_whenUserIsMember_thenRemovesMember()
    throws Exception {
    HistoryApi historyApi = mock(HistoryApi.class);
    CircabcConfig circabcConfig = mock(CircabcConfig.class);
    setField("historyApi", historyApi);
    setField("circabcConfig", circabcConfig);

    when(circabcService.isUserMember(TEST_IG_REF, "user1")).thenReturn(true);
    when(circabcConfig.isENT()).thenReturn(false);

    io.swagger.model.db.Profile dbProfile = new io.swagger.model.db.Profile();
    dbProfile.setName("Access");
    dbProfile.setAlfrescoGroup("profile1");
    when(
      circabcDaoService.selectProfileByInterestGroupNodeRefUserName(
        TEST_IG_REF.toString(),
        "user1"
      )
    ).thenReturn(dbProfile);

    groupsApi.groupsIdMembersUserIdDelete(TEST_IG_ID, "user1");

    verify(circabcService).deletePersonFromGroup(TEST_IG_REF, "user1");
    verify(historyApi).registerCleanPermissions(TEST_IG_REF, "user1");
    verify(historyApi).deleteExpirationDate("user1", TEST_IG_ID);
  }

  @Test
  public void testGroupsIdMembersUserIdDelete_whenUserNotMember_thenSkipsRemoval()
    throws Exception {
    HistoryApi historyApi = mock(HistoryApi.class);
    CircabcConfig circabcConfig = mock(CircabcConfig.class);
    setField("historyApi", historyApi);
    setField("circabcConfig", circabcConfig);

    when(circabcService.isUserMember(TEST_IG_REF, "user1")).thenReturn(false);
    when(circabcConfig.isENT()).thenReturn(false);

    groupsApi.groupsIdMembersUserIdDelete(TEST_IG_ID, "user1");

    verify(circabcService, never()).deletePersonFromGroup(any(), anyString());
  }

  // --- getGroupDashboard (additional) ---

  @Test
  public void testGetGroupDashboard_whenNodeDoesNotExist_thenSkipsIt()
    throws Exception {
    SearchService searchService = mock(SearchService.class);
    NodesApi nodesApi = mock(NodesApi.class);
    ApiToolBox apiToolBox = mock(ApiToolBox.class);
    setField("searchService", searchService);
    setField("nodesApi", nodesApi);
    setField("apiToolBox", apiToolBox);

    when(
      nodeService.hasAspect(TEST_IG_REF, CircabcModel.ASPECT_IGROOT)
    ).thenReturn(true);
    mockInterestGroupDetailsMinimal(TEST_IG_REF);
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_NAME)
    ).thenReturn("IG");
    when(
      nodeService.getProperty(TEST_IG_REF, ContentModel.PROP_TITLE)
    ).thenReturn("Title");
    when(apiToolBox.getPathFromSpaceRef(eq(TEST_IG_REF), eq(true))).thenReturn(
      "/path"
    );

    NodeRef docRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "gone"
    );
    ResultSet rs = mock(ResultSet.class);
    when(rs.getNodeRefs()).thenReturn(Collections.singletonList(docRef));
    when(searchService.query(any(SearchParameters.class))).thenReturn(rs);
    when(nodeService.exists(docRef)).thenReturn(false);

    GroupDashboard result = groupsApi.getGroupDashboard(TEST_IG_ID);

    assertTrue(result.getEntries().get(0).getNews().isEmpty());
  }

  // --- groupsIdMembersGet paged (additional) ---

  @Test
  public void testGroupsIdMembersGet_paged_whenPageExceedsSize_thenResetsStart()
    throws Exception {
    HistoryApi historyApi = mock(HistoryApi.class);
    setField("historyApi", historyApi);

    when(
      circabcService.getFilteredUsers(
        eq(TEST_IG_REF),
        anyLong(),
        eq(""),
        eq(""),
        isNull()
      )
    ).thenReturn(Collections.emptyList());
    when(circabcDaoService.getAllAlfrescoLocale()).thenReturn(
      Collections.singletonMap("en_", 1L)
    );
    when(historyApi.getAutoExpiredUsers(TEST_IG_ID)).thenReturn(
      Collections.emptyMap()
    );

    PagedUserProfile result = groupsApi.groupsIdMembersGet(
      TEST_IG_ID,
      null,
      "en",
      10,
      999,
      null,
      null
    );

    assertEquals(0, result.getTotal().intValue());
  }

  // --- groupsIdMembersApplicantsPut ---

  @Test
  public void testGroupsIdMembersApplicantsPut_whenClean_thenRemovesFromMap()
    throws Exception {
    MailPreferencesService mailPreferencesService = mock(
      MailPreferencesService.class
    );
    setField("mailPreferencesService", mailPreferencesService);

    Map<String, io.swagger.model.alfresco.Applicant> applicantMap =
      new HashMap<>();
    applicantMap.put(
      "user1",
      new io.swagger.model.alfresco.Applicant("user1", new Date(), "msg")
    );

    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn((java.io.Serializable) applicantMap);

    ApplicantAction body = new ApplicantAction();
    body.setAction("clean");
    body.setUsername("user1");

    groupsApi.groupsIdMembersApplicantsPut(TEST_IG_ID, body);

    assertFalse(applicantMap.containsKey("user1"));
    verify(nodeService).setProperty(
      eq(TEST_IG_REF),
      eq(CircabcModel.PROP_APPLICANTS),
      any()
    );
  }

  @Test
  public void testGroupsIdMembersApplicantsPut_whenDeclineWithMessage_thenSendsEmail()
    throws Exception {
    MailPreferencesService mailPreferencesService = mock(
      MailPreferencesService.class
    );
    MailService mailService = mock(MailService.class);
    UserService userService = mock(UserService.class);
    CircabcApi circabcApi = mock(CircabcApi.class);
    PersonService personService = mock(PersonService.class);
    setField("mailPreferencesService", mailPreferencesService);
    setField("mailService", mailService);
    setField("userService", userService);
    setField("circabcApi", circabcApi);
    setField("personService", personService);

    Map<String, io.swagger.model.alfresco.Applicant> applicantMap =
      new HashMap<>();
    applicantMap.put(
      "user1",
      new io.swagger.model.alfresco.Applicant("user1", new Date(), "msg")
    );

    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn((java.io.Serializable) applicantMap);

    NodeRef personRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "person-1"
    );
    when(personService.getPerson("user1")).thenReturn(personRef);
    when(
      nodeService.getProperty(personRef, ContentModel.PROP_EMAIL)
    ).thenReturn("user1@test.com");
    when(
      mailPreferencesService.buildDefaultModel(
        eq(TEST_IG_REF),
        eq(personRef),
        isNull()
      )
    ).thenReturn(new HashMap<>());

    NodeRef circabcRoot = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "root"
    );
    when(circabcApi.getCircabcNodeRef()).thenReturn(circabcRoot);

    MailWrapper mailWrapper = mock(MailWrapper.class);
    when(mailWrapper.getSubject(any(), any())).thenReturn("Subject");
    when(mailWrapper.getBody(any(), any())).thenReturn("Body");
    when(
      mailPreferencesService.getDefaultMailTemplate(eq(circabcRoot), any())
    ).thenReturn(mailWrapper);
    when(
      userService.getPreference(
        eq(personRef),
        any(org.alfresco.service.namespace.QName.class)
      )
    ).thenReturn(null);
    when(mailService.getNoReplyEmailAddress()).thenReturn("noreply@test.com");

    ApplicantAction body = new ApplicantAction();
    body.setAction("decline");
    body.setUsername("user1");
    body.setMessage("Sorry, declined");

    groupsApi.groupsIdMembersApplicantsPut(TEST_IG_ID, body);

    verify(mailService).send(
      eq("noreply@test.com"),
      eq("user1@test.com"),
      isNull(),
      eq("Subject"),
      eq("Body"),
      eq(true),
      eq(false)
    );
  }

  // --- groupsIdMembersApplicantsPost ---

  @Test
  public void testGroupsIdMembersApplicantsPost_whenSubmitNew_thenAddsToMap()
    throws Exception {
    ProfilesApi profilesApi = mock(ProfilesApi.class);
    PersonService personService = mock(PersonService.class);
    setField("profilesApi", profilesApi);
    setField("personService", personService);

    when(
      nodeService.getProperty(TEST_IG_REF, CircabcModel.PROP_APPLICANTS)
    ).thenReturn(null);
    when(
      profilesApi.groupsIdProfilesGet(eq(TEST_IG_ID), isNull(), eq(false))
    ).thenReturn(Collections.emptyList());

    ApplicantAction body = new ApplicantAction();
    body.setAction("submitNew");
    body.setUsername("newuser");
    body.setMessage("Please add me");

    groupsApi.groupsIdMembersApplicantsPost(TEST_IG_ID, body);

    verify(nodeService).setProperty(
      eq(TEST_IG_REF),
      eq(CircabcModel.PROP_APPLICANTS),
      any()
    );
  }

  @Test
  public void testGroupsIdMembersApplicantsPost_whenNotSubmitNew_thenDoesNothing()
    throws Exception {
    ApplicantAction body = new ApplicantAction();
    body.setAction("other");
    body.setUsername("user1");

    groupsApi.groupsIdMembersApplicantsPost(TEST_IG_ID, body);

    verify(nodeService, never()).setProperty(
      eq(TEST_IG_REF),
      eq(CircabcModel.PROP_APPLICANTS),
      any()
    );
  }
}
