package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.GroupsApi;
import io.swagger.model.I18nProperty;
import io.swagger.model.PagedUserProfile;
import io.swagger.model.Profile;
import io.swagger.model.User;
import io.swagger.model.UserProfile;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GroupsIdMembersExportTest {

  private GroupsIdMembersExport webScript;
  private GroupsApi groupsApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private WebScriptResponse res;

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsIdMembersExport();
    groupsApi = mock(GroupsApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    setField("groupsApi", groupsApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsIdMembersExport.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockTemplateVars(String id) {
    Map<String, String> vars = new HashMap<>();
    vars.put("igId", id);
    when(req.getServiceMatch()).thenReturn(new Match("", vars, ""));
  }

  private PagedUserProfile createPagedResult() {
    User user = new User("user1", "John", "Doe", "john@test.com");
    Map<String, String> props = new HashMap<>();
    props.put("title", "Mr");
    props.put("organisation", "EC");
    props.put("postalAddress", "Brussels");
    user.setProperties(props);

    Profile profile = new Profile();
    profile.setName("ADMIN");
    I18nProperty title = new I18nProperty();
    title.put("en", "Administrator");
    profile.setTitle(title);

    UserProfile up = new UserProfile();
    up.setUser(user);
    up.setProfile(profile);

    PagedUserProfile result = new PagedUserProfile();
    result.setData(List.of(up));
    result.setTotal(1);
    return result;
  }

  private void mockStandardRequest(String groupId, String format) {
    mockTemplateVars(groupId);
    when(
      permissionChecker.hasAnyOfDirectoryPermission(eq(groupId), any())
    ).thenReturn(true);
    when(req.getParameter("format")).thenReturn(format);
    when(req.getParameter("language")).thenReturn("en");
    when(req.getParameter("limit")).thenReturn(null);
    when(req.getParameter("page")).thenReturn(null);
    when(req.getParameter("order")).thenReturn(null);
    when(req.getParameter("firstName")).thenReturn(null);
    when(req.getParameter("lastName")).thenReturn(null);
    when(req.getParameter("email")).thenReturn(null);
    when(req.getParameter("profile")).thenReturn(null);
  }

  @Test
  public void testExecute_whenCsvFormat_thenExportsCsv() throws Exception {
    String groupId = "test-ig-id";
    mockStandardRequest(groupId, "csv");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);
    when(
      groupsApi.groupsIdMembersGet(
        eq(groupId),
        anyList(),
        eq("en"),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )
    ).thenReturn(createPagedResult());

    webScript.execute(req, res);

    verify(res).setContentType("text/csv;charset=UTF-8");
    String csv = baos.toString("UTF-8");
    assertTrue(csv.contains("Username,Title,First Name"));
    assertTrue(csv.contains("user1"));
    assertTrue(csv.contains("John"));
    assertTrue(csv.contains("Doe"));
  }

  @Test
  public void testExecute_whenXlsFormat_thenExportsXls() throws Exception {
    String groupId = "test-ig-id";
    mockStandardRequest(groupId, "xls");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);
    when(
      groupsApi.groupsIdMembersGet(
        eq(groupId),
        anyList(),
        eq("en"),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )
    ).thenReturn(createPagedResult());

    webScript.execute(req, res);

    verify(res).setContentType("application/vnd.ms-excel;charset=UTF-8");
    assertTrue(baos.size() > 0);
  }

  @Test
  public void testExecute_whenXmlFormat_thenExportsXml() throws Exception {
    String groupId = "test-ig-id";
    mockStandardRequest(groupId, "xml");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);
    when(
      groupsApi.groupsIdMembersGet(
        eq(groupId),
        anyList(),
        eq("en"),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )
    ).thenReturn(createPagedResult());

    webScript.execute(req, res);

    verify(res).setContentType("text/xml;charset=UTF-8");
    String xml = baos.toString("UTF-8");
    assertTrue(xml.contains("<members>"));
    assertTrue(xml.contains("username=\"user1\""));
  }

  @Test(expected = IOException.class)
  public void testExecute_whenNoPermission_thenThrowsIOException()
    throws Exception {
    String groupId = "test-ig-id";
    mockTemplateVars(groupId);
    when(req.getParameter("language")).thenReturn("en");
    when(
      permissionChecker.hasAnyOfDirectoryPermission(eq(groupId), any())
    ).thenReturn(false);

    webScript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenInvalidFormat_thenThrowsIOException()
    throws Exception {
    String groupId = "test-ig-id";
    mockStandardRequest(groupId, "pdf");

    webScript.execute(req, res);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenNullFormat_thenThrowsIOException()
    throws Exception {
    String groupId = "test-ig-id";
    mockTemplateVars(groupId);
    when(req.getParameter("language")).thenReturn("en");
    when(
      permissionChecker.hasAnyOfDirectoryPermission(eq(groupId), any())
    ).thenReturn(true);
    when(req.getParameter("format")).thenReturn(null);

    webScript.execute(req, res);
  }
}
