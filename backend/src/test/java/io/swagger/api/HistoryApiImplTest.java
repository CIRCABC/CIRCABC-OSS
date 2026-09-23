package io.swagger.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import eu.europa.ec.digit.circabc.rest.service.history.HistoryDaoService;
import io.swagger.model.*;
import io.swagger.model.db.MemberExpirationDAO;
import io.swagger.model.db.UserPropertyHistoryDAO;
import io.swagger.model.db.UserRevocationRequestDAO;
import java.lang.reflect.Field;
import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.Before;
import org.junit.Test;

public class HistoryApiImplTest {

  private HistoryApiImpl historyApi;
  private HistoryDaoService historyDaoService;
  private GroupsApi groupsApi;
  private UsersApi usersApi;
  private ProfilesApi profilesApi;
  private org.alfresco.service.cmr.repository.NodeService nodeService;
  private org.alfresco.service.cmr.security.PermissionService permissionService;
  private PersonService personService;
  private AuthenticationService authenticationService;
  private org.alfresco.service.transaction.TransactionService transactionService;

  @Before
  public void setUp() throws Exception {
    historyApi = new HistoryApiImpl();
    historyDaoService = mock(HistoryDaoService.class);
    groupsApi = mock(GroupsApi.class);
    usersApi = mock(UsersApi.class);
    profilesApi = mock(ProfilesApi.class);
    nodeService = mock(org.alfresco.service.cmr.repository.NodeService.class);
    permissionService = mock(
      org.alfresco.service.cmr.security.PermissionService.class
    );
    personService = mock(PersonService.class);
    authenticationService = mock(AuthenticationService.class);
    transactionService = mock(
      org.alfresco.service.transaction.TransactionService.class
    );

    setField("historyDaoService", historyDaoService);
    setField("groupsApi", groupsApi);
    setField("usersApi", usersApi);
    setField("profilesApi", profilesApi);
    setField("nodeService", nodeService);
    setField("permissionService", permissionService);
    setField("personService", personService);
    setField("authenticationService", authenticationService);
    setField("transactionService", transactionService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = HistoryApiImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(historyApi, value);
  }

  @Test
  public void testLogOldMembership_whenOldMembershipProvided_thenDelegates() {
    OldMembership oldMembership = new OldMembership();
    historyApi.logOldMembership(oldMembership);
    verify(historyDaoService).insertOldMembership(oldMembership);
  }

  @Test
  public void testGetRecoverableProfileInGroup_whenCalled_thenReturnsDaoResult() {
    when(
      historyDaoService.getGroupIdForRecoverableUser("user1", "group1")
    ).thenReturn("profile1");

    String result = historyApi.getRecoverableProfileInGroup("user1", "group1");

    assertEquals("profile1", result);
  }

  @Test
  public void testIsRecoverableFromGroup_whenProfileExists_thenRecoverableTrue() {
    when(
      historyDaoService.getGroupIdForRecoverableUser("user1", "group1")
    ).thenReturn("profile1");
    Profile profile = new Profile();
    when(profilesApi.profilesIdGet("profile1")).thenReturn(profile);

    UserRecoveryOption result = historyApi.isRecoverableFromGroup(
      "user1",
      "group1"
    );

    assertTrue(result.getRecoverable());
    assertEquals(profile, result.getProfile());
  }

  @Test
  public void testIsRecoverableFromGroup_whenNoProfile_thenRecoverableFalse() {
    when(
      historyDaoService.getGroupIdForRecoverableUser("user1", "group1")
    ).thenReturn(null);

    UserRecoveryOption result = historyApi.isRecoverableFromGroup(
      "user1",
      "group1"
    );

    assertFalse(result.getRecoverable());
    assertNull(result.getProfile());
  }

  @Test
  public void testGetRevocations_whenDataExists_thenReturnsPagedResult() {
    when(historyDaoService.countTotalRevocations()).thenReturn(1);
    UserRevocationRequestDAO dao = new UserRevocationRequestDAO();
    dao.setId(1);
    dao.setRequester("admin");
    dao.setRevocationDate(new Date());
    dao.setRequestState(0);
    dao.setAction("revoke");
    dao.setGroupId("group1");
    dao.setUserIds("user1, user2");
    when(historyDaoService.getRevocations(10, 0)).thenReturn(
      Collections.singletonList(dao)
    );

    PagedUserRevocationRequest result = historyApi.getRevocations(10, 0);

    assertEquals(Integer.valueOf(1), result.getTotal());
    assertEquals(1, result.getData().size());
    assertEquals(Integer.valueOf(1), result.getData().get(0).getId());
    assertEquals("admin", result.getData().get(0).getRequester());
    assertEquals(2, result.getData().get(0).getUserIds().size());
    assertEquals("user1", result.getData().get(0).getUserIds().get(0));
    assertEquals("user2", result.getData().get(0).getUserIds().get(1));
  }

  @Test
  public void testGetWaitingRevocations_whenEmpty_thenReturnsEmptyList() {
    when(historyDaoService.getWaitingRevocations()).thenReturn(
      Collections.emptyList()
    );

    List<UserRevocationRequest> result = historyApi.getWaitingRevocations();

    assertTrue(result.isEmpty());
  }

  @Test
  public void testRegisterRevocation_whenRequesterEmpty_thenSetsCurrentUser() {
    when(authenticationService.getCurrentUserName()).thenReturn("currentUser");
    UserRevocationRequest request = new UserRevocationRequest();
    request.setRequester("");

    historyApi.registerRevocation(request);

    assertEquals("currentUser", request.getRequester());
    assertEquals(Integer.valueOf(0), request.getRequestState());
    verify(historyDaoService).insertUserRevocation(request);
  }

  @Test
  public void testRegisterRevocation_whenRequesterNull_thenSetsCurrentUser() {
    when(authenticationService.getCurrentUserName()).thenReturn("currentUser");
    UserRevocationRequest request = new UserRevocationRequest();
    request.setRequester(null);

    historyApi.registerRevocation(request);

    assertEquals("currentUser", request.getRequester());
    verify(historyDaoService).insertUserRevocation(request);
  }

  @Test
  public void testRegisterRevocation_whenRequesterSet_thenKeepsIt() {
    UserRevocationRequest request = new UserRevocationRequest();
    request.setRequester("specificUser");
    request.setRequestState(1);

    historyApi.registerRevocation(request);

    assertEquals("specificUser", request.getRequester());
    assertEquals(Integer.valueOf(1), request.getRequestState());
    verify(historyDaoService).insertUserRevocation(request);
  }

  @Test
  public void testRegisterCleanPermissions_whenCalled_thenRegistersWithDao() {
    when(authenticationService.getCurrentUserName()).thenReturn("admin");
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    List<String> userIds = Arrays.asList("user1", "user2");

    historyApi.registerCleanPermissions(groupRef, userIds);

    verify(historyDaoService).registerCleanPermissions(any());
  }

  @Test
  public void testGetAutoExpiredUsers_whenDataExists_thenReturnsMap() {
    Date expDate = new Date();
    MemberExpirationDAO dao = new MemberExpirationDAO();
    dao.setUserId("user1");
    dao.setExpirationDate(expDate);
    when(historyDaoService.getMemberExpirationByIg("ig1")).thenReturn(
      Collections.singletonList(dao)
    );

    Map<String, Date> result = historyApi.getAutoExpiredUsers("ig1");

    assertEquals(1, result.size());
    assertEquals(expDate, result.get("user1"));
  }

  @Test
  public void testGetAutoExpiredUsers_whenNoData_thenReturnsEmptyMap() {
    when(historyDaoService.getMemberExpirationByIg("ig1")).thenReturn(
      Collections.emptyList()
    );

    Map<String, Date> result = historyApi.getAutoExpiredUsers("ig1");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testRecoverPropertiesFromGroup_whenNodeNotExists_thenDoesNothing() {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    when(nodeService.exists(groupRef)).thenReturn(false);

    historyApi.recoverPropertiesFromGroup("user1", "group-id");

    verify(historyDaoService, never()).selectOldProperties(
      anyString(),
      any(NodeRef.class)
    );
  }

  @Test
  public void testRecoverPropertiesFromGroup_whenPermissionProperty_thenRestores() {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );
    when(nodeService.exists(groupRef)).thenReturn(true);
    when(nodeService.exists(nodeRef)).thenReturn(true);
    when(personService.personExists("user1")).thenReturn(true);

    UserPropertyHistoryDAO prop = new UserPropertyHistoryDAO();
    prop.setNodeId("node-id");
    prop.setTypeName("permission");
    prop.setTypeId(1);
    prop.setOldValue("Contributor");
    prop.setAllowed(true);
    prop.setState(0);
    when(historyDaoService.selectOldProperties("user1", groupRef)).thenReturn(
      Collections.singletonList(prop)
    );

    historyApi.recoverPropertiesFromGroup("user1", "group-id");

    verify(permissionService).setPermission(
      nodeRef,
      "user1",
      "Contributor",
      true
    );
    verify(historyDaoService).markPropertyRestored("user1", "node-id", 1);
  }

  @Test
  public void testCleanMembershipsLogs_whenCalled_thenDelegates() {
    historyApi.cleanMembershipsLogs("group1", "user1");
    verify(historyDaoService).cleanMembershipsLogs("group1", "user1");
  }

  @Test
  public void testUpdateRevocationJobState_whenCalled_thenDelegates() {
    Date start = new Date();
    Date end = new Date();
    historyApi.updateRevocationJobState(1, start, end, 2);
    verify(historyDaoService).updateRevocationJobState(1, start, end, 2);
  }

  @Test
  public void testDeleteExpirationDate_whenCalled_thenDelegates() {
    historyApi.deleteExpirationDate("user1", "group1");
    verify(historyDaoService).deleteMemberExpiration(
      any(MemberExpirationDAO.class)
    );
  }

  @Test
  public void testCancelWaitingCleanPermissions_whenCalled_thenDelegates() {
    NodeRef groupRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    historyApi.cancelWaitingCleanPermissions("user1", groupRef);
    verify(historyDaoService).removeWaitingCleanPermissions("user1", groupRef);
  }
}
