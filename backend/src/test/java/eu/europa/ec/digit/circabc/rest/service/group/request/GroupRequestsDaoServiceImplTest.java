package eu.europa.ec.digit.circabc.rest.service.group.request;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.GroupCreationRequest;
import io.swagger.model.GroupDeletionRequest;
import io.swagger.model.I18nProperty;
import io.swagger.model.User;
import io.swagger.model.db.GroupCreationRequestDAO;
import io.swagger.model.db.GroupDeletionRequestDAO;
import io.swagger.model.db.KeyValueString;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class GroupRequestsDaoServiceImplTest {

  private GroupRequestsDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;
  private UsersApi usersApi;

  @Before
  public void setUp() throws Exception {
    service = new GroupRequestsDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    usersApi = mock(UsersApi.class);

    service.setSqlSessionTemplate(sqlSessionTemplate);
    setField("usersApi", usersApi);
  }

  @Test
  public void testGetCountCategoryGroupCreationRequests_whenCalled_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_count_category_group_requests"),
        anyMap()
      )
    ).thenReturn(5);

    Integer result = service.getCountCategoryGroupCreationRequests(
      "catRef1",
      "test"
    );

    assertEquals(Integer.valueOf(5), result);
  }

  @Test
  public void testGetCategoryGroupCreationRequests_whenResultsExist_thenConvertsToRequests() {
    GroupCreationRequestDAO dao = new GroupCreationRequestDAO();
    dao.setId(1);
    dao.setProposedName("TestGroup");
    dao.setFromUsername("user1");
    dao.setJustification("justification");
    dao.setRequestDate(new Date());
    dao.setAgreementDate(new Date());
    dao.setCategoryReference("catRef");
    dao.setAgreement(0);

    when(
      sqlSessionTemplate.selectList(
        eq("GroupRequests.select_category_group_requests"),
        anyMap()
      )
    ).thenReturn(Collections.singletonList(dao));
    when(
      sqlSessionTemplate.selectList(
        eq("select_category_group_requests_titles"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());
    when(
      sqlSessionTemplate.selectList(
        eq("select_category_group_requests_descriptions"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());

    User user = new User();
    user.setUserId("user1");
    when(usersApi.usersUserIdGet("user1")).thenReturn(user);

    List<GroupCreationRequest> result =
      service.getCategoryGroupCreationRequests("catRef", 10, 1, "");

    assertEquals(1, result.size());
    assertEquals("TestGroup", result.get(0).getProposedName());
    assertEquals(user, result.get(0).getFrom());
  }

  @Test
  public void testGetCategoryGroupCreationRequests_whenEmpty_thenReturnsEmptyList() {
    when(
      sqlSessionTemplate.selectList(
        eq("GroupRequests.select_category_group_requests"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());

    List<GroupCreationRequest> result =
      service.getCategoryGroupCreationRequests("catRef", 10, 1, "");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetCategoryGroupCreationRequests_whenHasLeadersAndReviewer_thenPopulatesThem() {
    GroupCreationRequestDAO dao = new GroupCreationRequestDAO();
    dao.setId(2);
    dao.setProposedName("Group2");
    dao.setFromUsername("user1");
    dao.setRequestDate(new Date());
    dao.setAgreementDate(new Date());
    dao.setCategoryReference("catRef");
    dao.setAgreement(1);
    dao.setLeaders("leader1;leader2");
    dao.setReviewer("reviewer1");

    when(
      sqlSessionTemplate.selectList(
        eq("GroupRequests.select_category_group_requests"),
        anyMap()
      )
    ).thenReturn(Collections.singletonList(dao));
    when(
      sqlSessionTemplate.selectList(
        eq("select_category_group_requests_titles"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());
    when(
      sqlSessionTemplate.selectList(
        eq("select_category_group_requests_descriptions"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());

    User user1 = new User();
    user1.setUserId("user1");
    User leader1 = new User();
    leader1.setUserId("leader1");
    User leader2 = new User();
    leader2.setUserId("leader2");
    User reviewer = new User();
    reviewer.setUserId("reviewer1");

    when(usersApi.usersUserIdGet("user1")).thenReturn(user1);
    when(usersApi.usersUserIdGet("leader1")).thenReturn(leader1);
    when(usersApi.usersUserIdGet("leader2")).thenReturn(leader2);
    when(usersApi.usersUserIdGet("reviewer1")).thenReturn(reviewer);

    List<GroupCreationRequest> result =
      service.getCategoryGroupCreationRequests("catRef", 10, 1, "");

    assertEquals(2, result.get(0).getLeaders().size());
    assertEquals(reviewer, result.get(0).getReviewer());
  }

  @Test
  public void testExistsGroupDeleteRequest_whenExists_thenReturnsTrue() {
    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_count_group_delete_requests"),
        anyMap()
      )
    ).thenReturn(1);

    assertTrue(service.existsGroupDeleteRequest("group1"));
  }

  @Test
  public void testExistsGroupDeleteRequest_whenNotExists_thenReturnsFalse() {
    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_count_group_delete_requests"),
        anyMap()
      )
    ).thenReturn(0);

    assertFalse(service.existsGroupDeleteRequest("group1"));
  }

  @Test
  public void testUpdateGroupCreationRequestApproval_whenCalled_thenUpdates() {
    service.updateGroupCreationRequestApproval("admin", 1L, 1, "approved");

    verify(sqlSessionTemplate).update(
      eq("update_category_group_request_approval"),
      anyMap()
    );
  }

  @Test
  public void testGetCategoryGroupCreationRequestById_whenExists_thenReturnsRequest() {
    GroupCreationRequestDAO dao = new GroupCreationRequestDAO();
    dao.setId(5);
    dao.setProposedName("MyGroup");
    dao.setFromUsername("user1");
    dao.setRequestDate(new Date());
    dao.setAgreementDate(new Date());
    dao.setCategoryReference("catRef");

    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_category_group_request"),
        anyMap()
      )
    ).thenReturn(dao);
    when(
      sqlSessionTemplate.selectList(
        eq("select_category_group_requests_titles"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());
    when(
      sqlSessionTemplate.selectList(
        eq("select_category_group_requests_descriptions"),
        anyMap()
      )
    ).thenReturn(Collections.emptyList());

    User user = new User();
    user.setUserId("user1");
    when(usersApi.usersUserIdGet("user1")).thenReturn(user);

    GroupCreationRequest result = service.getCategoryGroupCreationRequests("5");

    assertNotNull(result);
    assertEquals(Integer.valueOf(5), result.getId());
    assertEquals("MyGroup", result.getProposedName());
  }

  @Test
  public void testGetCategoryGroupCreationRequestById_whenNotExists_thenReturnsNull() {
    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_category_group_request"),
        anyMap()
      )
    ).thenReturn(null);

    GroupCreationRequest result = service.getCategoryGroupCreationRequests(
      "99"
    );

    assertNull(result);
  }

  @Test
  public void testGetCountCategoryGroupDeletionRequests_whenCalled_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_count_category_group_delete_requests"),
        anyMap()
      )
    ).thenReturn(3);

    Long result = service.getCountCategoryGroupDeletionRequests(
      "catRef",
      "filter"
    );

    assertEquals(Long.valueOf(3), result);
  }

  @Test
  public void testDeleteRequestDeletion_whenCalled_thenDeletes() {
    service.deleteRequestDeletion("group1");

    verify(sqlSessionTemplate).delete(
      eq("GroupRequests.delete_group_request_delete"),
      anyMap()
    );
  }

  @Test
  public void testGetCategoryGroupDeletionRequests_whenResultsExist_thenConverts() {
    GroupDeletionRequestDAO dao = new GroupDeletionRequestDAO();
    dao.setId(10);
    dao.setFromUsername("user1");
    dao.setRequestDate(new Date());
    dao.setCategoryRef("catRef");
    dao.setAgreementDate(new Date());
    dao.setAgreement(0);
    dao.setGroupId("grp1");
    dao.setTitle("Title");
    dao.setName("Name");
    dao.setDescription("Desc");

    when(
      sqlSessionTemplate.selectList(
        eq("GroupRequests.select_category_group_delete_requests"),
        anyMap()
      )
    ).thenReturn(Collections.singletonList(dao));

    User user = new User();
    user.setUserId("user1");
    when(usersApi.usersUserIdGet("user1")).thenReturn(user);

    List<GroupDeletionRequest> result =
      service.getCategoryGroupDeletionRequests("catRef", 10, 1, null);

    assertEquals(1, result.size());
    assertEquals(Long.valueOf(10), result.get(0).getId());
    assertEquals("Title", result.get(0).getTitle());
    assertEquals("grp1", result.get(0).getGroupId());
  }

  @Test
  public void testGetCategoryGroupDeletionRequestById_whenNotExists_thenReturnsNull() {
    when(
      sqlSessionTemplate.selectOne(
        eq("GroupRequests.select_group_request_delete_by_id"),
        anyMap()
      )
    ).thenReturn(null);

    GroupDeletionRequest result = service.getCategoryGroupDeletionRequests(
      "99"
    );

    assertNull(result);
  }

  @Test
  public void testUpdateRequestDeletion_whenCalled_thenUpdates() {
    GroupDeletionRequest body = new GroupDeletionRequest();
    body.setId(1L);
    body.setAgreement(1);

    service.updateRequestDeletion(body, "reviewer1");

    verify(sqlSessionTemplate).update(
      eq("GroupRequests.update_category_group_request_deletion"),
      anyMap()
    );
  }

  @Test
  public void testUpdateRequestDeletion_whenRejected_thenIncludesRejectedMessage() {
    GroupDeletionRequest body = new GroupDeletionRequest();
    body.setId(1L);
    body.setAgreement(-1);
    body.setRejectedMessage("Not valid");

    service.updateRequestDeletion(body, "reviewer1");

    verify(sqlSessionTemplate).update(
      eq("GroupRequests.update_category_group_request_deletion"),
      argThat((Map<String, Object> map) ->
        "Not valid".equals(map.get("rejected_message"))
      )
    );
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupRequestsDaoServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
