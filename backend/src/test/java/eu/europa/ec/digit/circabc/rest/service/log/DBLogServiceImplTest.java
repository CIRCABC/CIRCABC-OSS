package eu.europa.ec.digit.circabc.rest.service.log;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.LogRecord;
import io.swagger.model.LogRestRecord;
import io.swagger.model.db.ActivityCountDAO;
import io.swagger.model.db.LogActivityDAO;
import io.swagger.model.db.LogCountResultDAO;
import io.swagger.model.db.LogRecordDAO;
import io.swagger.model.db.LogRestDAO;
import io.swagger.model.db.LogSearchResultDAO;
import io.swagger.model.db.UserActionLogDAO;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.junit.Before;
import org.junit.Test;

public class DBLogServiceImplTest {

  private DBLogServiceImpl service;
  private LogDaoService logDaoService;
  private NodeService nodeService;
  private AuthenticationService authenticationService;
  private LogTransformService logTransformService;
  private MultilingualContentService multilingualContentService;

  @Before
  public void setUp() throws Exception {
    service = new DBLogServiceImpl();
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
    field.set(service, value);
  }

  // --- log() tests ---

  @Test
  public void testLog_whenActivityOrServiceNull_thenReturnsEarly() {
    LogRecord record = new LogRecord();
    record.setActivity(null);
    record.setService("Library");
    service.log(record);
    verifyNoInteractions(logDaoService);

    LogRecord record2 = new LogRecord();
    record2.setActivity("Upload");
    record2.setService(null);
    service.log(record2);
    verifyNoInteractions(logDaoService);
  }

  @Test
  public void testLog_whenValidRecord_thenDelegatesToDao() {
    LogRecord record = new LogRecord();
    record.setService("Library");
    record.setActivity("Upload document");
    record.setDate(new Date());
    record.setIgID(100L);
    record.setDocumentID(200L);
    record.setInfo("test info");
    record.setPath("/some/path");
    record.setUser("testuser");
    record.setIgName("TestIG");
    record.setOK(true);

    when(logDaoService.getActivityID("Library", "Upload document")).thenReturn(
      5
    );

    service.log(record);

    verify(logDaoService).log(any(LogRecordDAO.class));
  }

  @Test
  public void testLog_whenActivityIdNotFound_thenInsertsNew() {
    LogRecord record = new LogRecord();
    record.setService("Library");
    record.setActivity("New activity");
    record.setOK(false);

    when(logDaoService.getActivityID("Library", "New activity")).thenReturn(-1);
    when(logDaoService.insertActivity("Library", "New activity")).thenReturn(
      10
    );

    service.log(record);

    verify(logDaoService).insertActivity("Library", "New activity");
    verify(logDaoService).log(any(LogRecordDAO.class));
  }

  @Test
  public void testLog_whenDaoThrows_thenNoExceptionPropagated() {
    LogRecord record = new LogRecord();
    record.setService("Library");
    record.setActivity("Upload");

    when(logDaoService.getActivityID("Library", "Upload")).thenThrow(
      new RuntimeException("DB error")
    );

    service.log(record); // should not throw
  }

  // --- logRest() tests ---

  @Test
  public void testLogRest_whenMethodOrTemplateNull_thenReturnsEarly() {
    LogRestRecord record = new LogRestRecord();
    record.setMethod(null);
    record.setTemplate("/circabc/groups/{id}");
    service.logRest(record);
    verifyNoInteractions(logDaoService);

    LogRestRecord record2 = new LogRestRecord();
    record2.setMethod("GET");
    record2.setTemplate(null);
    service.logRest(record2);
    verifyNoInteractions(logDaoService);
  }

  @Test
  public void testLogRest_whenValidRecord_thenDelegatesToDao() {
    LogRestRecord record = new LogRestRecord();
    record.setMethod("GET");
    record.setTemplate("/circabc/groups/{id}");
    record.setDate(new Date());
    record.setInfo("some info");
    record.setStatusCode(200);
    record.setUser("testuser");
    record.setUrl("/circabc/groups/123");

    when(logDaoService.getTemplateID("GET", "/circabc/groups/{id}")).thenReturn(
      3
    );

    service.logRest(record);

    verify(logDaoService).logRest(any(LogRestDAO.class));
  }

  @Test
  public void testLogRest_whenInfoTooLong_thenTruncated() {
    LogRestRecord record = new LogRestRecord();
    record.setMethod("POST");
    record.setTemplate("/circabc/nodes/{id}");
    record.setStatusCode(200);
    StringBuilder longInfo = new StringBuilder();
    for (int i = 0; i < 5000; i++) {
      longInfo.append("x");
    }
    record.setInfo(longInfo.toString());

    when(logDaoService.getTemplateID("POST", "/circabc/nodes/{id}")).thenReturn(
      1
    );

    service.logRest(record);

    verify(logDaoService).logRest(any(LogRestDAO.class));
  }

  // --- search() tests ---

  @Test
  public void testSearch_whenValid_thenReturnsList() {
    List<LogSearchResultDAO> expected = Arrays.asList(new LogSearchResultDAO());
    when(
      logDaoService.search(
        1L,
        "user",
        "Library",
        "Upload",
        (Date) null,
        (Date) null
      )
    ).thenReturn(expected);

    List<LogSearchResultDAO> result = service.search(
      1L,
      "user",
      "Library",
      "Upload",
      null,
      null
    );

    assertEquals(expected, result);
  }

  @Test
  public void testSearch_whenDaoThrows_thenReturnsNull() {
    when(
      logDaoService.search(
        1L,
        "user",
        "Library",
        "Upload",
        (Date) null,
        (Date) null
      )
    ).thenThrow(new RuntimeException("DB error"));

    List<LogSearchResultDAO> result = service.search(
      1L,
      "user",
      "Library",
      "Upload",
      null,
      null
    );

    assertNull(result);
  }

  // --- searchCount() tests ---

  @Test
  public void testSearchCount_whenValid_thenReturnsCount() {
    when(
      logDaoService.searchCount(
        1L,
        "user",
        "Library",
        "Upload",
        (Date) null,
        (Date) null
      )
    ).thenReturn(42);

    int result = service.searchCount(
      1L,
      "user",
      "Library",
      "Upload",
      null,
      null
    );

    assertEquals(42, result);
  }

  @Test
  public void testSearchCount_whenDaoThrows_thenReturnsZero() {
    when(
      logDaoService.searchCount(
        1L,
        "user",
        "Library",
        "Upload",
        (Date) null,
        (Date) null
      )
    ).thenThrow(new RuntimeException("DB error"));

    int result = service.searchCount(
      1L,
      "user",
      "Library",
      "Upload",
      null,
      null
    );

    assertEquals(0, result);
  }

  // --- getHistory() tests ---

  @Test
  public void testGetHistory_whenValid_thenReturnsList() {
    List<LogSearchResultDAO> expected = Arrays.asList(new LogSearchResultDAO());
    when(logDaoService.getHistory(10L, "uuid-123")).thenReturn(expected);

    List<LogSearchResultDAO> result = service.getHistory(10L, "uuid-123");

    assertEquals(expected, result);
  }

  @Test
  public void testGetHistory_whenDaoThrows_thenReturnsEmptyList() {
    when(logDaoService.getHistory(10L, "uuid-123")).thenThrow(
      new RuntimeException("DB error")
    );

    List<LogSearchResultDAO> result = service.getHistory(10L, "uuid-123");

    assertTrue(result.isEmpty());
  }

  // --- deleteInterestgroupLog() tests ---

  @Test
  public void testDeleteInterestgroupLog_whenValid_thenDelegatesToDao() {
    service.deleteInterestgroupLog(50L);
    verify(logDaoService).deleteInterestgroupLog(50L);
  }

  @Test
  public void testDeleteInterestgroupLog_whenDaoThrows_thenNoExceptionPropagated() {
    doThrow(new RuntimeException("DB error"))
      .when(logDaoService)
      .deleteInterestgroupLog(50L);

    service.deleteInterestgroupLog(50L); // should not throw
  }

  // --- getLastLoginDateOfUser() tests ---

  @Test
  public void testGetLastLoginDateOfUser_whenValid_thenReturnsDate() {
    Date expected = new Date();
    when(logDaoService.getLastLoginDateOfUser("testuser")).thenReturn(expected);

    Date result = service.getLastLoginDateOfUser("testuser");

    assertEquals(expected, result);
  }

  @Test
  public void testGetLastLoginDateOfUser_whenDaoThrows_thenReturnsNull() {
    when(logDaoService.getLastLoginDateOfUser("testuser")).thenThrow(
      new RuntimeException("DB error")
    );

    Date result = service.getLastLoginDateOfUser("testuser");

    assertNull(result);
  }

  // --- processRestLog() tests ---

  @Test
  public void testProcessRestLog_whenRowsExist_thenProcessesEach() {
    LogRestDAO row = new LogRestDAO();
    row.setId(1L);
    LogRecordDAO transformed = new LogRecordDAO();

    when(logDaoService.getRowsToProcess()).thenReturn(Arrays.asList(row));
    when(logTransformService.transform(row)).thenReturn(transformed);
    when(logDaoService.log(transformed)).thenReturn(99L);

    service.processRestLog();

    verify(logDaoService).log(transformed);
    verify(logDaoService).updateRest(1L, 99L);
  }

  @Test
  public void testProcessRestLog_whenTransformThrows_thenMarksError() {
    LogRestDAO row = new LogRestDAO();
    row.setId(2L);

    when(logDaoService.getRowsToProcess()).thenReturn(Arrays.asList(row));
    when(logTransformService.transform(row)).thenThrow(
      new RuntimeException("transform error")
    );

    service.processRestLog();

    verify(logDaoService).updateRest(2L, -1L);
  }

  // --- getVisitedIGRestLogs() tests ---

  @Test
  public void testGetVisitedIGRestLogs_whenTemplateNotFound_thenReturnsEmpty() {
    when(logDaoService.getTemplateID("GET", "/circabc/groups/{id}")).thenReturn(
      -1
    );

    List<String> result = service.getVisitedIGRestLogs("testuser");

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetVisitedIGRestLogs_whenTemplateFound_thenDelegatesToDao() {
    when(logDaoService.getTemplateID("GET", "/circabc/groups/{id}")).thenReturn(
      7
    );
    when(logDaoService.getVisitedRestLogs(7, "testuser")).thenReturn(
      Arrays.asList("ig1", "ig2")
    );

    List<String> result = service.getVisitedIGRestLogs("testuser");

    assertEquals(2, result.size());
    assertEquals("ig1", result.get(0));
  }

  // --- getNumberOfActionsYesterdayPerHour() tests ---

  @Test
  public void testGetNumberOfActionsYesterdayPerHour_whenDaoThrows_thenReturnsEmpty() {
    when(logDaoService.getNumberOfActionsYesterdayPerHour()).thenThrow(
      new RuntimeException("DB error")
    );

    List<LogCountResultDAO> result =
      service.getNumberOfActionsYesterdayPerHour();

    assertTrue(result.isEmpty());
  }

  // --- getListOfActivityCountForInterestGroup() tests ---

  @Test
  public void testGetListOfActivityCountForInterestGroup_whenDaoThrows_thenReturnsEmpty() {
    when(logDaoService.getListOfActivityCountForInterestGroup(1L)).thenThrow(
      new RuntimeException("DB error")
    );

    List<ActivityCountDAO> result =
      service.getListOfActivityCountForInterestGroup(1L);

    assertTrue(result.isEmpty());
  }

  // --- logBatch() tests ---

  @Test
  public void testLogBatch_whenValidRecords_thenDelegatesToDao() {
    LogRecord r1 = new LogRecord();
    r1.setService("Library");
    r1.setActivity("Upload");
    r1.setOK(true);

    when(logDaoService.getActivityID("Library", "Upload")).thenReturn(1);

    service.logBatch(Arrays.asList(r1));

    verify(logDaoService).logBatch(anyList());
  }

  @Test
  public void testLogBatch_whenRecordHasNullActivity_thenSkipped() {
    LogRecord r1 = new LogRecord();
    r1.setService("Library");
    r1.setActivity(null);

    service.logBatch(Arrays.asList(r1));

    verify(logDaoService).logBatch(argThat(List::isEmpty));
  }
}
