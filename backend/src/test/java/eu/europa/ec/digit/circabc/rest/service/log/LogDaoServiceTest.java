package eu.europa.ec.digit.circabc.rest.service.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.db.ActivityCountDAO;
import io.swagger.model.db.LogActivityDAO;
import io.swagger.model.db.LogCountResultDAO;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.model.db.UserActionLogDAO;
import io.swagger.model.db.UserNewsFeedRequest;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class LogDaoServiceTest {

  private LogDaoService service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    IBatisLogDaoServiceImpl impl = new IBatisLogDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    Field field = IBatisLogDaoServiceImpl.class.getDeclaredField(
      "sqlSessionTemplate"
    );
    field.setAccessible(true);
    field.set(impl, sqlSessionTemplate);
    service = impl;
  }

  @Test
  public void testLog_whenInsertSucceeds_thenReturnsRecordId() {
    LogRecordDAO record = mock(LogRecordDAO.class);
    when(record.getId()).thenReturn(7L);
    when(
      sqlSessionTemplate.insert("CircabcLog.insert_log_record", record)
    ).thenReturn(1);

    Long result = service.log(record);

    assertEquals(Long.valueOf(7L), result);
  }

  @Test
  public void testLog_whenInsertReturnsZero_thenStillReturnsRecordId() {
    LogRecordDAO record = mock(LogRecordDAO.class);
    when(record.getId()).thenReturn(99L);
    when(
      sqlSessionTemplate.insert("CircabcLog.insert_log_record", record)
    ).thenReturn(0);

    Long result = service.log(record);

    assertEquals(Long.valueOf(99L), result);
  }

  @Test
  public void testGetActivityID_whenFound_thenReturnsId() {
    doReturn(12)
      .when(sqlSessionTemplate)
      .selectOne(eq("CircabcLog.select_activity_id"), any());

    Integer result = service.getActivityID("Newsgroup", "Post");

    assertEquals(Integer.valueOf(12), result);
  }

  @Test
  public void testGetActivityID_whenNotFound_thenReturnsMinusOne() {
    doReturn(null)
      .when(sqlSessionTemplate)
      .selectOne(eq("CircabcLog.select_activity_id"), any());

    Integer result = service.getActivityID("Unknown", "Action");

    assertEquals(Integer.valueOf(-1), result);
  }

  @Test
  public void testInsertActivity_thenCallsInsert() {
    when(
      sqlSessionTemplate.insert(eq("CircabcLog.insert_activity"), any())
    ).thenReturn(1);

    service.insertActivity("Library", "Upload");

    verify(sqlSessionTemplate).insert(eq("CircabcLog.insert_activity"), any());
  }

  @Test
  public void testLogRest_thenReturnsInsertResult() {
    LogRestDAO dao = mock(LogRestDAO.class);
    when(
      sqlSessionTemplate.insert("CircabcLog.insert_log_rest", dao)
    ).thenReturn(1);

    Long result = service.logRest(dao);

    assertEquals(Long.valueOf(1L), result);
  }

  @Test
  public void testGetVisitedRestLogs_thenReturnsList() {
    List<String> expected = Arrays.asList("/api/groups", "/api/users");
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_rest_log_by_id_user"), any());

    List<String> result = service.getVisitedRestLogs(5, "admin");

    assertEquals(expected, result);
  }

  @Test
  public void testGetTemplateID_whenFound_thenReturnsId() {
    doReturn(20)
      .when(sqlSessionTemplate)
      .selectOne(eq("CircabcLog.select_template_id"), any());

    Integer result = service.getTemplateID("POST", "/api/nodes");

    assertEquals(Integer.valueOf(20), result);
  }

  @Test
  public void testGetTemplateID_whenNotFound_thenReturnsMinusOne() {
    doReturn(null)
      .when(sqlSessionTemplate)
      .selectOne(eq("CircabcLog.select_template_id"), any());

    Integer result = service.getTemplateID("DELETE", "/api/missing");

    assertEquals(Integer.valueOf(-1), result);
  }

  @Test
  public void testSelectLogActivities_thenReturnsList() {
    List<LogActivityDAO> expected = Collections.singletonList(
      mock(LogActivityDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_log_activity");

    List<LogActivityDAO> result = service.selectLogActivities();

    assertEquals(expected, result);
  }

  @Test
  public void testSelectLogActivitiesById_thenReturnsList() {
    List<LogActivityDAO> expected = Collections.singletonList(
      mock(LogActivityDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_log_activity_by_id"), any());

    List<LogActivityDAO> result = service.selectLogActivitiesById(3L);

    assertEquals(expected, result);
  }

  @Test
  public void testSearch_whenUserNameIsNullString_thenSetsNullUserName() {
    List<LogSearchResultDAO> expected = Collections.emptyList();
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_log_records"), any());

    List<LogSearchResultDAO> result = service.search(
      1L,
      "null",
      "Library",
      "Upload",
      new Date(),
      new Date()
    );

    assertEquals(expected, result);
  }

  @Test
  public void testSearch_whenUserNameIsRegularValue_thenSetsUserName() {
    List<LogSearchResultDAO> expected = Collections.singletonList(
      mock(LogSearchResultDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_log_records"), any());

    List<LogSearchResultDAO> result = service.search(
      2L,
      "john",
      "Events",
      "Create",
      new Date(),
      new Date()
    );

    assertEquals(expected, result);
  }

  @Test
  public void testSearch_whenUserNameIsNull_thenSetsNull() {
    List<LogSearchResultDAO> expected = Collections.emptyList();
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_log_records"), any());

    List<LogSearchResultDAO> result = service.search(
      1L,
      null,
      "Library",
      "Upload",
      new Date(),
      new Date()
    );

    assertEquals(expected, result);
  }

  @Test
  public void testSearchCount_thenReturnsCount() {
    doReturn(15)
      .when(sqlSessionTemplate)
      .selectOne(eq("CircabcLog.select_log_records_count"), any());

    Integer result = service.searchCount(
      1L,
      "admin",
      "Library",
      "Download",
      new Date(),
      new Date()
    );

    assertEquals(Integer.valueOf(15), result);
  }

  @Test
  public void testSearchPage_whenUserIsNullString_thenSetsNullUserName() {
    List<LogSearchResultDAO> expected = Collections.singletonList(
      mock(LogSearchResultDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_log_records_page"), any());

    LogSearchCriteria criteria = new LogSearchCriteria(
      1L,
      "null",
      "Library",
      "Upload",
      new Date(),
      new Date(),
      0,
      10
    );

    List<LogSearchResultDAO> result = service.searchPage(criteria);

    assertEquals(expected, result);
  }

  @Test
  public void testSearchPage_whenUserIsRegularValue_thenSetsUserName() {
    List<LogSearchResultDAO> expected = Collections.emptyList();
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_log_records_page"), any());

    LogSearchCriteria criteria = new LogSearchCriteria(
      2L,
      "admin",
      "Events",
      "Create",
      new Date(),
      new Date(),
      5,
      20
    );

    List<LogSearchResultDAO> result = service.searchPage(criteria);

    assertEquals(expected, result);
  }

  @Test
  public void testGetHistory_twoArgs_thenReturnsList() {
    List<LogSearchResultDAO> expected = Collections.singletonList(
      mock(LogSearchResultDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_item_history"), any());

    List<LogSearchResultDAO> result = service.getHistory(50L, "abc-def");

    assertEquals(expected, result);
  }

  @Test
  public void testCountHistory_thenReturnsCount() {
    doReturn(8L)
      .when(sqlSessionTemplate)
      .selectOne(eq("CircabcLog.count_item_history"), any());

    long result = service.countHistory(50L, "abc-def");

    assertEquals(8L, result);
  }

  @Test
  public void testGetHistory_withPagination_thenReturnsList() {
    List<LogSearchResultDAO> expected = Collections.singletonList(
      mock(LogSearchResultDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_item_history_pagination"), any());

    List<LogSearchResultDAO> result = service.getHistory(50L, "abc-def", 0, 10);

    assertEquals(expected, result);
  }

  @Test
  public void testDeleteInterestgroupLog_thenCallsDelete() {
    service.deleteInterestgroupLog(42L);

    verify(sqlSessionTemplate).delete("CircabcLog.delete_log_by_ig", 42L);
  }

  @Test
  public void testGetLastLoginDateOfUser_whenFound_thenReturnsDate() {
    Date expected = new Date();
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectOne("CircabcLog.select_last_login_date_of_user", "user1");

    Date result = service.getLastLoginDateOfUser("user1");

    assertEquals(expected, result);
  }

  @Test
  public void testGetLastLoginDateOfUser_whenNotFound_thenReturnsNull() {
    doReturn(null)
      .when(sqlSessionTemplate)
      .selectOne("CircabcLog.select_last_login_date_of_user", "unknown");

    Date result = service.getLastLoginDateOfUser("unknown");

    assertNull(result);
  }

  @Test
  public void testGetNumberOfActionsYesterdayPerHour_thenReturnsList() {
    List<LogCountResultDAO> expected = Collections.singletonList(
      mock(LogCountResultDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_count_actions_per_hour_yesterday");

    List<LogCountResultDAO> result =
      service.getNumberOfActionsYesterdayPerHour();

    assertEquals(expected, result);
  }

  @Test
  public void testGetListOfActivityCountForInterestGroup_thenReturnsList() {
    List<ActivityCountDAO> expected = Collections.singletonList(
      mock(ActivityCountDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_activity_of_interest_group", 10L);

    List<ActivityCountDAO> result =
      service.getListOfActivityCountForInterestGroup(10L);

    assertEquals(expected, result);
  }

  @Test
  public void testGetLastAccessLogOnInterestGroup_thenReturnsDate() {
    Date expected = new Date();
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectOne("CircabcLog.select_last_log_for_ig", 5L);

    Date result = service.getLastAccessLogOnInterestGroup(5L);

    assertEquals(expected, result);
  }

  @Test
  public void testGetLastUpdateLogOnInterestGroup_thenReturnsDate() {
    Date expected = new Date();
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectOne("CircabcLog.select_last_update_log_for_ig", 5L);

    Date result = service.getLastUpdateLogOnInterestGroup(5L);

    assertEquals(expected, result);
  }

  @Test
  public void testGetUserDashboardActivityIds_thenReturnsList() {
    List<Long> expected = Arrays.asList(1L, 2L, 3L);
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_activity_id_for_news_feed");

    List<Long> result = service.getUserDashboardActivityIds();

    assertEquals(expected, result);
  }

  @Test
  public void testGetUserDashboardActivities_thenReturnsList() {
    UserNewsFeedRequest request = mock(UserNewsFeedRequest.class);
    List<UserActionLogDAO> expected = Collections.singletonList(
      mock(UserActionLogDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_group_news_feed_uploads", request);

    List<UserActionLogDAO> result = service.getUserDashboardActivities(request);

    assertEquals(expected, result);
  }

  @Test
  public void testGetActivityIDByTemplateId_whenFound_thenReturnsId() {
    doReturn(4L)
      .when(sqlSessionTemplate)
      .selectOne("CircabcLog.select_activity_id_by_template_id", 11L);

    long result = service.getActivityID(11L);

    assertEquals(4L, result);
  }

  @Test
  public void testGetActivityIDByTemplateId_whenNotFound_thenReturnsMinusOne() {
    doReturn(null)
      .when(sqlSessionTemplate)
      .selectOne("CircabcLog.select_activity_id_by_template_id", 99L);

    long result = service.getActivityID(99L);

    assertEquals(-1L, result);
  }

  @Test
  public void testUpdateRest_thenCallsUpdate() {
    service.updateRest(10L, 20L);

    verify(sqlSessionTemplate).update(
      eq("CircabcLog.update_cbc_log_id"),
      any()
    );
  }

  @Test
  public void testLogBatch_whenEmptyList_thenNoInserts() {
    service.logBatch(Collections.emptyList());

    verify(sqlSessionTemplate, never()).insert(anyString(), any());
  }

  @Test
  public void testLogBatch_whenMultipleRecords_thenInsertsAll() {
    LogRecordDAO r1 = mock(LogRecordDAO.class);
    LogRecordDAO r2 = mock(LogRecordDAO.class);
    LogRecordDAO r3 = mock(LogRecordDAO.class);

    service.logBatch(Arrays.asList(r1, r2, r3));

    verify(sqlSessionTemplate).insert("CircabcLog.insert_log_record", r1);
    verify(sqlSessionTemplate).insert("CircabcLog.insert_log_record", r2);
    verify(sqlSessionTemplate).insert("CircabcLog.insert_log_record", r3);
  }

  @Test
  public void testGetRecentUserDownloads_thenReturnsList() {
    List<UserActionLogDAO> expected = Collections.singletonList(
      mock(UserActionLogDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_download_logs_for_user"), any());

    List<UserActionLogDAO> result = service.getRecentUserDownloads("user1", 5);

    assertEquals(expected, result);
  }

  @Test
  public void testGetRecentUserUploads_thenReturnsList() {
    List<UserActionLogDAO> expected = Collections.singletonList(
      mock(UserActionLogDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_upload_logs_for_user"), any());

    List<UserActionLogDAO> result = service.getRecentUserUploads("user1", 3);

    assertEquals(expected, result);
  }

  @Test
  public void testGetRowsToProcess_thenReturnsList() {
    List<LogRestDAO> expected = Arrays.asList(
      mock(LogRestDAO.class),
      mock(LogRestDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_rest_log");

    List<LogRestDAO> result = service.getRowsToProcess();

    assertEquals(expected, result);
    assertEquals(2, result.size());
  }
}
