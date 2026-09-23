package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.UsersApi;
import io.swagger.model.User;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UsersRetrievalPostTest {

  private UsersRetrievalPost webScript;
  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService permissionChecker;
  private WebScriptRequest req;
  private Status status;
  private Cache cache;

  @Before
  public void setUp() throws Exception {
    webScript = new UsersRetrievalPost();
    usersApi = mock(UsersApi.class);
    permissionChecker = mock(CurrentUserPermissionCheckerService.class);
    req = mock(WebScriptRequest.class);
    status = new Status();
    cache = new Cache();

    setField("usersApi", usersApi);
    setField("currentUserPermissionCheckerService", permissionChecker);
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = UsersRetrievalPost.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  private void mockExcelRequest(byte[] excelBytes) throws IOException {
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getInputStream()).thenReturn(
      new ByteArrayInputStream(excelBytes)
    );
  }

  private byte[] createExcel(String[][] data) throws IOException {
    try (Workbook wb = new XSSFWorkbook()) {
      Sheet sheet = wb.createSheet();
      for (int i = 0; i < data.length; i++) {
        Row row = sheet.createRow(i);
        for (int j = 0; j < data[i].length; j++) {
          row.createCell(j).setCellValue(data[i][j]);
        }
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      wb.write(out);
      return out.toByteArray();
    }
  }

  @Test
  public void testExecuteImpl_whenCircabcAdmin_thenReturnsUsers()
    throws IOException {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);

    byte[] excel = createExcel(
      new String[][] { { "userid", "email" }, { "user1", "user1@example.com" } }
    );
    mockExcelRequest(excel);

    List<User> retrievedUsers = new ArrayList<>();
    User u = new User();
    u.setUserId("user1");
    u.setEmail("user1@example.com");
    retrievedUsers.add(u);
    when(usersApi.retrieveUserList(anyList())).thenReturn(retrievedUsers);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(retrievedUsers, model.get("users"));
    verify(usersApi).retrieveUserList(anyList());
  }

  @Test
  public void testExecuteImpl_whenAlfrescoAdmin_thenReturnsUsers()
    throws IOException {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(true);

    byte[] excel = createExcel(
      new String[][] { { "userid", "email" }, { "user2", "user2@example.com" } }
    );
    mockExcelRequest(excel);

    List<User> retrievedUsers = new ArrayList<>();
    when(usersApi.retrieveUserList(anyList())).thenReturn(retrievedUsers);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNotNull(model);
    assertEquals(retrievedUsers, model.get("users"));
  }

  @Test
  public void testExecuteImpl_whenNotAdmin_thenReturnsForbidden() {
    when(permissionChecker.isCircabcAdmin()).thenReturn(false);
    when(permissionChecker.isAlfrescoAdmin()).thenReturn(false);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_FORBIDDEN, status.getCode());
    verifyNoInteractions(usersApi);
  }

  @Test
  public void testExecuteImpl_whenInvalidFile_thenReturnsInternalServerError()
    throws IOException {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);

    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getInputStream()).thenReturn(
      new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 })
    );

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }

  @Test
  public void testExecuteImpl_whenNullContent_thenReturnsInternalServerError() {
    when(permissionChecker.isCircabcAdmin()).thenReturn(true);
    when(req.getContent()).thenReturn(null);

    Map<String, Object> model = webScript.executeImpl(req, status, cache);

    assertNull(model);
    assertEquals(Status.STATUS_INTERNAL_SERVER_ERROR, status.getCode());
  }
}
