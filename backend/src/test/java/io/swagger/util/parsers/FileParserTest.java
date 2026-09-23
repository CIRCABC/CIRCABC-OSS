package io.swagger.util.parsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.model.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class FileParserTest {

  private WebScriptRequest mockRequest(byte[] excelBytes) throws IOException {
    WebScriptRequest req = mock(WebScriptRequest.class);
    Content content = mock(Content.class);
    when(req.getContent()).thenReturn(content);
    when(content.getInputStream()).thenReturn(
      new ByteArrayInputStream(excelBytes)
    );
    return req;
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
  public void testParseExcelUserListPartial_whenValidData_thenUsersReturned()
    throws IOException {
    String[][] data = {
      { "userid", "email" },
      { "user1", "user1@example.com" },
      { "user2", "user2@example.com" },
    };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertEquals(2, result.size());
    assertEquals("user1", result.get(0).getUserId());
    assertEquals("user1@example.com", result.get(0).getEmail());
    assertEquals("user2", result.get(1).getUserId());
    assertEquals("user2@example.com", result.get(1).getEmail());
  }

  @Test
  public void testParseExcelUserListPartial_whenOnlyUserIdColumn_thenUsersWithoutEmail()
    throws IOException {
    String[][] data = { { "userid" }, { "user1" } };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0).getUserId());
  }

  @Test
  public void testParseExcelUserListPartial_whenOnlyEmailColumn_thenUsersWithoutUserId()
    throws IOException {
    String[][] data = { { "email" }, { "valid@example.com" } };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertEquals(1, result.size());
    assertEquals("valid@example.com", result.get(0).getEmail());
  }

  @Test
  public void testParseExcelUserListPartial_whenDuplicateUserId_thenNoDuplicates()
    throws IOException {
    String[][] data = {
      { "userid", "email" },
      { "user1", "a@example.com" },
      { "user1", "b@example.com" },
    };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertEquals(1, result.size());
  }

  @Test
  public void testParseExcelUserListPartial_whenDuplicateEmail_thenNoDuplicates()
    throws IOException {
    String[][] data = {
      { "userid", "email" },
      { "user1", "same@example.com" },
      { "user2", "same@example.com" },
    };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertEquals(1, result.size());
  }

  @Test
  public void testParseExcelUserListPartial_whenEmptyRows_thenSkipped()
    throws IOException {
    String[][] data = {
      { "userid", "email" },
      { "", "" },
      { "user1", "user1@example.com" },
    };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0).getUserId());
  }

  @Test
  public void testParseExcelUserListPartial_whenHeaderOnly_thenEmptyList()
    throws IOException {
    String[][] data = { { "userid", "email" } };
    byte[] excel = createExcel(data);
    WebScriptRequest req = mockRequest(excel);

    List<User> result = FileParser.parseExcelUserListPartial(req);

    assertTrue(result.isEmpty());
  }

  @Test
  public void testConstructor_whenInstantiated_thenThrowsException()
    throws Exception {
    java.lang.reflect.Constructor<FileParser> constructor =
      FileParser.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected IllegalStateException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
    }
  }
}
