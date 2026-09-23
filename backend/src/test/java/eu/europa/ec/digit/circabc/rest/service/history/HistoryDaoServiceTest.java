package eu.europa.ec.digit.circabc.rest.service.history;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.OldMembership;
import io.swagger.model.UserRevocationRequest;
import io.swagger.model.db.MemberExpirationDAO;
import io.swagger.model.db.UserPropertyHistoryDAO;
import io.swagger.model.db.UserRevocationRequestDAO;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class HistoryDaoServiceTest {

  private HistoryDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() {
    service = new HistoryDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    service.setSqlSessionTemplate(sqlSessionTemplate);
  }

  @Test
  public void testInsertOldMembership_whenCountIsZero_thenInserts() {
    OldMembership membership = new OldMembership();
    membership.setUserId("user1");
    membership.setGroupId("group1");
    membership.setProfileId("profile1");
    membership.setAlfGroupName("alfGroup1");
    membership.setState(0);

    when(
      sqlSessionTemplate.selectOne(
        eq("History.count_membership_history"),
        anyMap()
      )
    ).thenReturn(0);

    service.insertOldMembership(membership);

    verify(sqlSessionTemplate).insert(
      eq("History.insert_membership_history"),
      anyMap()
    );
    verify(sqlSessionTemplate, never()).update(
      eq("History.update_membership_history_in_group"),
      anyMap()
    );
  }

  @Test
  public void testInsertOldMembership_whenCountIsNonZero_thenUpdates() {
    OldMembership membership = new OldMembership();
    membership.setUserId("user1");
    membership.setGroupId("group1");
    membership.setProfileId("profile1");
    membership.setAlfGroupName("alfGroup1");
    membership.setState(1);

    when(
      sqlSessionTemplate.selectOne(
        eq("History.count_membership_history"),
        anyMap()
      )
    ).thenReturn(1);

    service.insertOldMembership(membership);

    verify(sqlSessionTemplate, never()).insert(
      eq("History.insert_membership_history"),
      anyMap()
    );
    verify(sqlSessionTemplate).update(
      eq("History.update_membership_history_in_group"),
      anyMap()
    );
  }

  @Test
  public void testGetGroupIdForRecoverableUser_whenResultExists_thenReturnsFirst() {
    when(
      sqlSessionTemplate.selectList(
        eq("History.get_membership_history_in_group"),
        anyMap()
      )
    ).thenReturn(Arrays.asList("profile1", "profile2"));

    String result = service.getGroupIdForRecoverableUser("user1", "group1");

    assertEquals("profile1", result);
  }

  @Test
  public void testGetGroupIdForRecoverableUser_whenResultEmpty_thenReturnsNull() {
    when(
      sqlSessionTemplate.selectList(
        eq("History.get_membership_history_in_group"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());

    String result = service.getGroupIdForRecoverableUser("user1", "group1");

    assertNull(result);
  }

  @Test
  public void testInsertOldPermission_whenPermissionAndCountZero_thenInserts() {
    AccessPermission acp = mock(AccessPermission.class);
    when(acp.getPermission()).thenReturn("Read");
    when(acp.getAccessStatus()).thenReturn(AccessStatus.ALLOWED);

    NodeRef groupNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );

    when(
      sqlSessionTemplate.selectOne("History.select_permission_type_id")
    ).thenReturn(1);
    when(
      sqlSessionTemplate.selectOne(
        eq("History.count_property_history"),
        anyMap()
      )
    ).thenReturn(0);

    service.insertOldPermission(acp, "user1", groupNodeRef, nodeRef);

    verify(sqlSessionTemplate).insert(
      eq("History.insert_property_history"),
      anyMap()
    );
    verify(sqlSessionTemplate, never()).update(
      eq("History.update_property_history"),
      anyMap()
    );
  }

  @Test
  public void testInsertOldPermission_whenNotificationAndCountNonZero_thenUpdates() {
    AccessPermission acp = mock(AccessPermission.class);
    when(acp.getPermission()).thenReturn("NotificationStatus_email");
    when(acp.getAccessStatus()).thenReturn(AccessStatus.DENIED);

    NodeRef groupNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    NodeRef nodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "node-id"
    );

    when(
      sqlSessionTemplate.selectOne("History.select_notification_type_id")
    ).thenReturn(2);
    when(
      sqlSessionTemplate.selectOne(
        eq("History.count_property_history"),
        anyMap()
      )
    ).thenReturn(1);

    service.insertOldPermission(acp, "user1", groupNodeRef, nodeRef);

    verify(sqlSessionTemplate, never()).insert(
      eq("History.insert_property_history"),
      anyMap()
    );
    verify(sqlSessionTemplate).update(
      eq("History.update_property_history"),
      anyMap()
    );
  }

  @Test
  public void testGetRevocations_whenPageGreaterThanZero_thenCalculatesOffset() {
    List<UserRevocationRequestDAO> expected = Collections.singletonList(
      new UserRevocationRequestDAO()
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("History.select_user_revocation_request"), anyMap());

    List<UserRevocationRequestDAO> result = service.getRevocations(10, 2);

    assertEquals(expected, result);
  }

  @Test
  public void testCountTotalRevocations() {
    when(
      sqlSessionTemplate.selectOne("History.count_user_revocation_request")
    ).thenReturn(5);

    Integer count = service.countTotalRevocations();

    assertEquals(Integer.valueOf(5), count);
  }

  @Test
  public void testCleanMembershipsLogs_callsBothUpdates() {
    service.cleanMembershipsLogs("group1", "user1");

    verify(sqlSessionTemplate).update(
      eq("History.clean_user_memberhistory"),
      anyMap()
    );
    verify(sqlSessionTemplate).update(
      eq("History.clean_user_propertyhistory"),
      anyMap()
    );
  }

  @Test
  public void testSelectOldProperties_returnsListFromTemplate() {
    NodeRef groupNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );
    List<UserPropertyHistoryDAO> expected = Collections.singletonList(
      new UserPropertyHistoryDAO()
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("History.select_group_property_history"), anyMap());

    List<UserPropertyHistoryDAO> result = service.selectOldProperties(
      "user1",
      groupNodeRef
    );

    assertEquals(expected, result);
  }

  @Test
  public void testInsertUserRevocation_whenExceptionThrown_thenDoesNotPropagate() {
    UserRevocationRequest request = new UserRevocationRequest();
    request.setRequester("admin");
    request.setRevocationDate(new DateTime());
    request.setUserIds(Arrays.asList("user1", "user2"));

    when(
      sqlSessionTemplate.insert(
        eq("History.insert_user_revocation_request"),
        anyMap()
      )
    ).thenThrow(new RuntimeException("DB error"));

    service.insertUserRevocation(request);
  }

  @Test
  public void testRemoveWaitingCleanPermissions() {
    NodeRef groupNodeRef = new NodeRef(
      StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
      "group-id"
    );

    service.removeWaitingCleanPermissions("user1", groupNodeRef);

    verify(sqlSessionTemplate).delete(
      eq("History.delete_waiting_clean_permission_request"),
      anyMap()
    );
  }

  @Test
  public void testInsertMemberExpiration() {
    MemberExpirationDAO dao = new MemberExpirationDAO();
    dao.setUserId("user1");
    dao.setGroupId("group1");
    dao.setProfileId("profile1");
    dao.setAlfrescoGroup("alfGroup1");
    dao.setExpirationDate(new Date());

    service.insertMemberExpiration(dao);

    verify(sqlSessionTemplate).insert(
      eq("History.insert_membership_expiration"),
      anyMap()
    );
  }

  @Test
  public void testCountUserExpirations() {
    when(
      sqlSessionTemplate.selectOne(
        eq("History.count_expiration_for_user_group"),
        anyMap()
      )
    ).thenReturn(3);

    Integer count = service.countUserExpirations("user1", "group1");

    assertEquals(Integer.valueOf(3), count);
  }

  @Test
  public void testUpdateRevocationJobState() {
    Date started = new Date();
    Date finished = new Date();

    service.updateRevocationJobState(1, started, finished, 2);

    verify(sqlSessionTemplate).update(
      eq("History.update_user_revocation_request"),
      anyMap()
    );
  }
}
