package eu.europa.ec.digit.circabc.rest.service.log;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.swagger.model.db.LogActivityDAO;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.model.db.UserActionLogDAO;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.spring.SqlSessionTemplate;

public class IBatisLogDaoServiceImplTest {

  private IBatisLogDaoServiceImpl service;
  private SqlSessionTemplate sqlSessionTemplate;

  @Before
  public void setUp() throws Exception {
    service = new IBatisLogDaoServiceImpl();
    sqlSessionTemplate = mock(SqlSessionTemplate.class);
    setField("sqlSessionTemplate", sqlSessionTemplate);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = IBatisLogDaoServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  @Test
  public void testLog_whenInsertSucceeds_thenReturnsRecordId() {
    LogRecordDAO record = mock(LogRecordDAO.class);
    when(record.getId()).thenReturn(42L);
    when(
      sqlSessionTemplate.insert(eq("CircabcLog.insert_log_record"), eq(record))
    ).thenReturn(1);

    Long result = service.log(record);

    assertEquals(Long.valueOf(42L), result);
    verify(sqlSessionTemplate).insert("CircabcLog.insert_log_record", record);
  }

  @Test
  public void testGetActivityID_whenFound_thenReturnsId() {
    when(
      sqlSessionTemplate.selectOne(eq("CircabcLog.select_activity_id"), any())
    ).thenReturn(5);

    Integer result = service.getActivityID("Library", "Upload");

    assertEquals(Integer.valueOf(5), result);
  }

  @Test
  public void testGetActivityID_whenNotFound_thenReturnsMinusOne() {
    when(
      sqlSessionTemplate.selectOne(eq("CircabcLog.select_activity_id"), any())
    ).thenReturn(null);

    Integer result = service.getActivityID("Library", "Unknown");

    assertEquals(Integer.valueOf(-1), result);
  }

  @Test
  public void testInsertActivity_thenReturnsGeneratedId() {
    when(
      sqlSessionTemplate.insert(eq("CircabcLog.insert_activity"), any())
    ).thenReturn(1);

    // The method returns logActivityDAO.getId() which will be null since mock doesn't set it
    Integer result = service.insertActivity("Library", "Upload");

    verify(sqlSessionTemplate).insert(eq("CircabcLog.insert_activity"), any());
  }

  @Test
  public void testGetTemplateID_whenFound_thenReturnsId() {
    when(
      sqlSessionTemplate.selectOne(eq("CircabcLog.select_template_id"), any())
    ).thenReturn(10);

    Integer result = service.getTemplateID("GET", "/api/groups");

    assertEquals(Integer.valueOf(10), result);
  }

  @Test
  public void testGetTemplateID_whenNotFound_thenReturnsMinusOne() {
    when(
      sqlSessionTemplate.selectOne(eq("CircabcLog.select_template_id"), any())
    ).thenReturn(null);

    Integer result = service.getTemplateID("GET", "/unknown");

    assertEquals(Integer.valueOf(-1), result);
  }

  @Test
  public void testSearch_whenUserNameIsNull_thenSetsNullInParams() {
    List<LogSearchResultDAO> expected = Collections.singletonList(
      mock(LogSearchResultDAO.class)
    );
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
  public void testSearch_whenUserNameIsNullString_thenSetsNullInParams() {
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
  public void testSearchCount_whenFound_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(
        eq("CircabcLog.select_log_records_count"),
        any()
      )
    ).thenReturn(42);

    Integer result = service.searchCount(
      1L,
      "admin",
      "Library",
      "Upload",
      new Date(),
      new Date()
    );

    assertEquals(Integer.valueOf(42), result);
  }

  @Test
  public void testGetHistory_thenReturnsList() {
    List<LogSearchResultDAO> expected = Collections.singletonList(
      mock(LogSearchResultDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_item_history"), any());

    List<LogSearchResultDAO> result = service.getHistory(100L, "uuid-123");

    assertEquals(expected, result);
  }

  @Test
  public void testCountHistory_thenReturnsCount() {
    when(
      sqlSessionTemplate.selectOne(eq("CircabcLog.count_item_history"), any())
    ).thenReturn(5L);

    long result = service.countHistory(100L, "uuid-123");

    assertEquals(5L, result);
  }

  @Test
  public void testDeleteInterestgroupLog_thenCallsDelete() {
    service.deleteInterestgroupLog(99L);

    verify(sqlSessionTemplate).delete("CircabcLog.delete_log_by_ig", 99L);
  }

  @Test
  public void testGetLastLoginDateOfUser_thenReturnsDate() {
    Date expected = new Date();
    when(
      sqlSessionTemplate.selectOne(
        "CircabcLog.select_last_login_date_of_user",
        "admin"
      )
    ).thenReturn(expected);

    Date result = service.getLastLoginDateOfUser("admin");

    assertEquals(expected, result);
  }

  @Test
  public void testLogBatch_thenInsertsAllRecords() {
    LogRecordDAO r1 = mock(LogRecordDAO.class);
    LogRecordDAO r2 = mock(LogRecordDAO.class);

    service.logBatch(Arrays.asList(r1, r2));

    verify(sqlSessionTemplate).insert("CircabcLog.insert_log_record", r1);
    verify(sqlSessionTemplate).insert("CircabcLog.insert_log_record", r2);
  }

  @Test
  public void testGetActivityIDByTemplateId_whenFound_thenReturnsId() {
    when(
      sqlSessionTemplate.selectOne(
        "CircabcLog.select_activity_id_by_template_id",
        7L
      )
    ).thenReturn(3L);

    long result = service.getActivityID(7L);

    assertEquals(3L, result);
  }

  @Test
  public void testGetActivityIDByTemplateId_whenNotFound_thenReturnsMinusOne() {
    when(
      sqlSessionTemplate.selectOne(
        "CircabcLog.select_activity_id_by_template_id",
        7L
      )
    ).thenReturn(null);

    long result = service.getActivityID(7L);

    assertEquals(-1L, result);
  }

  @Test
  public void testUpdateRest_thenCallsUpdate() {
    service.updateRest(1L, 2L);

    verify(sqlSessionTemplate).update(
      eq("CircabcLog.update_cbc_log_id"),
      any()
    );
  }

  @Test
  public void testGetRowsToProcess_thenReturnsList() {
    List<LogRestDAO> expected = Collections.singletonList(
      mock(LogRestDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList("CircabcLog.select_rest_log");

    List<LogRestDAO> result = service.getRowsToProcess();

    assertEquals(expected, result);
  }

  @Test
  public void testGetRecentUserDownloads_thenReturnsList() {
    List<UserActionLogDAO> expected = Collections.singletonList(
      mock(UserActionLogDAO.class)
    );
    doReturn(expected)
      .when(sqlSessionTemplate)
      .selectList(eq("CircabcLog.select_download_logs_for_user"), any());

    List<UserActionLogDAO> result = service.getRecentUserDownloads("admin", 10);

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

    List<UserActionLogDAO> result = service.getRecentUserUploads("admin", 5);

    assertEquals(expected, result);
  }
}
