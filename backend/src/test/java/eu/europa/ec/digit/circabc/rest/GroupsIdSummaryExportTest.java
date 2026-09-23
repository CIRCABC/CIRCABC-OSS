package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GroupsIdSummaryExportTest {

  private GroupsIdSummaryExport webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private WebScriptResponse res;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsIdSummaryExport();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsIdSummaryExport.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("id", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  @Test
  public void testExecute_whenValidCsvStatistics_thenCallsExportSummary()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn("csv");
    when(req.getParameter("type")).thenReturn("statistics");

    webScript.execute(req, res);

    verify(groupsApi).exportSummary(groupId, "csv", "statistics", res);
  }

  @Test
  public void testExecute_whenXlsTimeline_thenCallsExportSummary()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn("xls");
    when(req.getParameter("type")).thenReturn("timeline");

    webScript.execute(req, res);

    verify(groupsApi).exportSummary(groupId, "xls", "timeline", res);
  }

  @Test
  public void testExecute_whenXmlFormat_thenCallsExportSummary()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn("XML");
    when(req.getParameter("type")).thenReturn("STATISTICS");

    webScript.execute(req, res);

    verify(groupsApi).exportSummary(groupId, "xml", "statistics", res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenNotGroupAdmin_thenThrowsIOException()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(false);

    webScript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenFormatNull_thenThrowsIOException()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn(null);

    webScript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenFormatInvalid_thenThrowsIOException()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn("pdf");

    webScript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenTypeNull_thenThrowsIOException()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn("csv");
    when(req.getParameter("type")).thenReturn(null);

    webScript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenTypeInvalid_thenThrowsIOException()
    throws IOException {
    String groupId = "test-group-id";
    mockTemplateVars(groupId);
    when(permissionChecker.isGroupAdmin(groupId)).thenReturn(true);
    when(req.getParameter("format")).thenReturn("csv");
    when(req.getParameter("type")).thenReturn("invalid");

    webScript.execute(req, res);
  }
}
