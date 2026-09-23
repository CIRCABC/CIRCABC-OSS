package eu.europa.ec.digit.circabc.rest.service.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.LogRecord;
import io.swagger.model.LogRestRecord;
import io.swagger.model.db.LogActivityDAO;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.model.db.LogSearchResultDAO;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;

public class LogServiceTest {

  private DBLogServiceImpl logService;
  private LogDaoService logDaoService;
  private NodeService nodeService;
  private AuthenticationService authenticationService;
  private LogTransformService logTransformService;
  private MultilingualContentService multilingualContentService;

  @Before
  public void setUp() throws Exception {
    logService = new DBLogServiceImpl();
    logDaoService = mock(LogDaoService.class);
    nodeService = mock(NodeService.class);
    authenticationService = mock(AuthenticationService.class);
    logTransformService = mock(LogTransformService.class);
    multilingualContentService = mock(MultilingualContentService.class);

    setField("logDaoService", logDaoService);
    setField("nodeService", nodeService);
    setField("authenticationService", authenticationService);
    setField("logTransformService", logTransformService);
    setField("multilingualContentService", multilingualContentService);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = DBLogServiceImpl.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(logService, value);
  }

  // --- log() tests ---

  @Test
  public void testLog_whenValidRecord_thenDelegatesToDao() {
    LogRecord record = new LogRecord();
    record.setService("Library");
    record.setActivity("Upload document");
    record.setUser("testuser");
    record.setIgID(100L);
    record.setDocumentID(200L);
    record.setDate(new Date());
    record.setOK(true);

    when(logDaoService.getActivityID("Library", "Upload document")).thenReturn(
      5
    );

    logService.log(record);

    verify(logDaoService).log(any(LogRecordDAO.class));
  }

  @Test
  public void testLog_whenActivityNotFound_thenInsertsNewActivity() {
    LogRecord record = new LogRecord();
    record.setService("Library");
    record.setActivity("New action");
    record.setUser("testuser");

    when(logDaoService.getActivityID("Library", "New action")).thenReturn(-1);
    when(logDaoService.insertActivity("Library", "New action")).thenReturn(10);

    logService.log(record);

    verify(logDaoService).insertActivity("Library", "New action");
    verify(logDaoService).log(any(LogRecordDAO.class));
  }

  @Test
  public void testLog_whenActivityIsNull_thenDoesNothing() {
    LogRecord record = new LogRecord();
    record.setService("Library");
    record.setActivity(null);

    logService.log(record);

    verifyNoInteractions(logDaoService);
  }

  @Test
  public void testLog_whenServiceIsNull_thenDoesNothing() {
    LogRecord record = new LogRecord();
    record.setService(null);
    record.setActivity("Upload document");

    logService.log(record);

    verifyNoInteractions(logDaoService);
  }

  // --- logRest() tests ---

  @Test
  public void testLogRest_whenValidRecord_thenDelegatesToDao() {
    LogRestRecord record = new LogRestRecord();
    record.setMethod("GET");
    record.setTemplate("/circabc/groups/{id}");
    record.setUser("testuser");
    record.setDate(new Date());
    record.setStatusCode(200);

    when(logDaoService.getTemplateID("GET", "/circabc/groups/{id}")).thenReturn(
      3
    );

    logService.logRest(record);

    verify(logDaoService).logRest(any(LogRestDAO.class));
  }

  @Test
  public void testLogRest_whenMethodIsNull_thenDoesNothing() {
    LogRestRecord record = new LogRestRecord();
    record.setMethod(null);
    record.setTemplate("/circabc/groups/{id}");

    logService.logRest(record);

    verifyNoInteractions(logDaoService);
  }

  @Test
  public void testLogRest_whenInfoExceeds4000Chars_thenTruncates() {
    LogRestRecord record = new LogRestRecord();
    record.setMethod("POST");
    record.setTemplate("/circabc/nodes/{id}");
    record.setInfo("x".repeat(5000));
    record.setStatusCode(200);

    when(logDaoService.getTemplateID("POST", "/circabc/nodes/{id}")).thenReturn(
      1
    );

    logService.logRest(record);

    verify(logDaoService).logRest(
      argThat(dao ->
        dao.getInfo().equals("{ \"error\": \"value too big to be recorded\"}")
      )
    );
  }

  // --- search() tests ---

  @Test
  public void testSearch_whenValid_thenReturnResults() {
    Date from = new Date();
    Date to = new Date();
    List<LogSearchResultDAO> expected = Arrays.asList(new LogSearchResultDAO());
    when(
      logDaoService.search(1L, "user", "Library", "Upload", from, to)
    ).thenReturn(expected);

    List<LogSearchResultDAO> result = logService.search(
      1L,
      "user",
      "Library",
      "Upload",
      from,
      to
    );

    assertEquals(expected, result);
  }

  @Test
  public void testSearch_whenDaoThrows_thenReturnsNull() {
    when(
      logDaoService.search(
        anyLong(),
        anyString(),
        anyString(),
        anyString(),
        any(Date.class),
        any(Date.class)
      )
    ).thenThrow(new RuntimeException("DB error"));

    List<LogSearchResultDAO> result = logService.search(
      1L,
      "user",
      "Library",
      "Upload",
      new Date(),
      new Date()
    );

    assertNull(result);
  }

  // --- logBatch() tests ---

  @Test
  public void testLogBatch_whenValidRecords_thenDelegatesToDao() {
    LogRecord r1 = new LogRecord();
    r1.setService("Library");
    r1.setActivity("Upload document");
    r1.setUser("user1");

    LogRecord r2 = new LogRecord();
    r2.setService("Newsgroup");
    r2.setActivity("Create topic");
    r2.setUser("user2");

    when(logDaoService.getActivityID("Library", "Upload document")).thenReturn(
      1
    );
    when(logDaoService.getActivityID("Newsgroup", "Create topic")).thenReturn(
      2
    );

    logService.logBatch(Arrays.asList(r1, r2));

    verify(logDaoService).logBatch(argThat(list -> list.size() == 2));
  }

  @Test
  public void testLogBatch_whenRecordHasNullActivity_thenSkipsIt() {
    LogRecord valid = new LogRecord();
    valid.setService("Library");
    valid.setActivity("Upload document");

    LogRecord invalid = new LogRecord();
    invalid.setService("Library");
    invalid.setActivity(null);

    when(logDaoService.getActivityID("Library", "Upload document")).thenReturn(
      1
    );

    logService.logBatch(Arrays.asList(valid, invalid));

    verify(logDaoService).logBatch(argThat(list -> list.size() == 1));
  }

  // --- processRestLog() tests ---

  @Test
  public void testProcessRestLog_whenRowsExist_thenTransformsAndLogs() {
    LogRestDAO row = new LogRestDAO();
    row.setId(10L);
    LogRecordDAO transformed = new LogRecordDAO();

    when(logDaoService.getRowsToProcess()).thenReturn(
      Collections.singletonList(row)
    );
    when(logTransformService.transform(row)).thenReturn(transformed);
    when(logDaoService.log(transformed)).thenReturn(99L);

    logService.processRestLog();

    verify(logDaoService).log(transformed);
    verify(logDaoService).updateRest(10L, 99L);
  }

  @Test
  public void testProcessRestLog_whenTransformThrows_thenMarksError() {
    LogRestDAO row = new LogRestDAO();
    row.setId(5L);

    when(logDaoService.getRowsToProcess()).thenReturn(
      Collections.singletonList(row)
    );
    when(logTransformService.transform(row)).thenThrow(
      new RuntimeException("transform error")
    );

    logService.processRestLog();

    verify(logDaoService).updateRest(5L, -1L);
  }

  // --- getVisitedIGRestLogs() tests ---

  @Test
  public void testGetVisitedIGRestLogs_whenTemplateExists_thenReturnsLogs() {
    when(logDaoService.getTemplateID("GET", "/circabc/groups/{id}")).thenReturn(
      7
    );
    when(logDaoService.getVisitedRestLogs(7, "testuser")).thenReturn(
      Arrays.asList("ig1", "ig2")
    );

    List<String> result = logService.getVisitedIGRestLogs("testuser");

    assertEquals(2, result.size());
    assertEquals("ig1", result.get(0));
  }

  @Test
  public void testGetVisitedIGRestLogs_whenTemplateNotFound_thenReturnsEmpty() {
    when(logDaoService.getTemplateID("GET", "/circabc/groups/{id}")).thenReturn(
      -1
    );

    List<String> result = logService.getVisitedIGRestLogs("testuser");

    assertTrue(result.isEmpty());
    verify(logDaoService, never()).getVisitedRestLogs(anyInt(), anyString());
  }

  // --- getLastLoginDateOfUser() tests ---

  @Test
  public void testGetLastLoginDateOfUser_whenFound_thenReturnsDate() {
    Date expected = new Date();
    when(logDaoService.getLastLoginDateOfUser("user1")).thenReturn(expected);

    Date result = logService.getLastLoginDateOfUser("user1");

    assertEquals(expected, result);
  }

  @Test
  public void testGetLastLoginDateOfUser_whenDaoThrows_thenReturnsNull() {
    when(logDaoService.getLastLoginDateOfUser("user1")).thenThrow(
      new RuntimeException("DB error")
    );

    Date result = logService.getLastLoginDateOfUser("user1");

    assertNull(result);
  }

  // --- deleteInterestgroupLog() tests ---

  @Test
  public void testDeleteInterestgroupLog_whenValid_thenDelegatesToDao() {
    logService.deleteInterestgroupLog(42L);

    verify(logDaoService).deleteInterestgroupLog(42L);
  }
}
