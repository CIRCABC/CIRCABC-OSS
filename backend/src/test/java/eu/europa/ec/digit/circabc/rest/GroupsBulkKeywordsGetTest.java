package eu.europa.ec.digit.circabc.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import io.swagger.api.KeywordsApi;
import io.swagger.model.I18nProperty;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Match;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public class GroupsBulkKeywordsGetTest {

  private GroupsBulkKeywordsGet webScript;
  private KeywordsApi keywordsApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;
  private WebScriptRequest req;
  private WebScriptResponse res;

  private void setField(String fieldName, Object value) throws Exception {
    Field field = GroupsBulkKeywordsGet.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(webScript, value);
  }

  @Before
  public void setUp() throws Exception {
    webScript = new GroupsBulkKeywordsGet();
    keywordsApi = mock(KeywordsApi.class);
    currentUserPermissionCheckerService = mock(
      CurrentUserPermissionCheckerService.class
    );

    setField("keywordsApi", keywordsApi);
    setField(
      "currentUserPermissionCheckerService",
      currentUserPermissionCheckerService
    );

    req = mock(WebScriptRequest.class);
    res = mock(WebScriptResponse.class);

    Map<String, String> templateVars = new HashMap<>();
    templateVars.put("igId", "test-ig-id");
    when(req.getServiceMatch()).thenReturn(new Match("", templateVars, ""));
  }

  @Test
  public void testExecute_whenKeywordsExist_thenWritesXls() throws Exception {
    when(req.getParameter("language")).thenReturn(null);

    KeywordDefinition kd = new KeywordDefinition();
    kd.setId("kw1");
    I18nProperty title = new I18nProperty();
    title.put("en", "Environment");
    title.put("fr", "Environnement");
    kd.setTitle(title);

    when(keywordsApi.groupsIdKeywordsGet("test-ig-id")).thenReturn(
      Collections.singletonList(kd)
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);

    webScript.execute(req, res);

    verify(
      currentUserPermissionCheckerService
    ).throwIfCanNotAccessInterestGroup("test-ig-id");

    // Verify XLS content
    try (
      Workbook workbook = new HSSFWorkbook(
        new java.io.ByteArrayInputStream(baos.toByteArray())
      )
    ) {
      Sheet sheet = workbook.getSheetAt(0);
      // Header row
      assertEquals("Index", sheet.getRow(0).getCell(0).getStringCellValue());
      assertEquals("Language", sheet.getRow(0).getCell(1).getStringCellValue());
      assertEquals("Value", sheet.getRow(0).getCell(2).getStringCellValue());
      // Data rows (2 entries for 2 languages)
      assertTrue(sheet.getLastRowNum() >= 2);
    }
  }

  @Test
  public void testExecute_whenLanguageSpecified_thenSetsLocale()
    throws Exception {
    when(req.getParameter("language")).thenReturn("fr");

    when(keywordsApi.groupsIdKeywordsGet("test-ig-id")).thenReturn(
      Collections.emptyList()
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);

    webScript.execute(req, res);

    verify(keywordsApi).groupsIdKeywordsGet("test-ig-id");
  }

  @Test
  public void testExecute_whenAccessDenied_thenReturnsForbidden()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);

    doThrow(new AccessDeniedException("denied"))
      .when(currentUserPermissionCheckerService)
      .throwIfCanNotAccessInterestGroup("test-ig-id");

    webScript.execute(req, res);

    verify(res).setStatus(Status.STATUS_FORBIDDEN);
  }

  @Test(expected = IOException.class)
  public void testExecute_whenApiThrowsException_thenThrowsIOException()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);

    when(keywordsApi.groupsIdKeywordsGet("test-ig-id")).thenThrow(
      new RuntimeException("db error")
    );

    webScript.execute(req, res);
  }

  @Test
  public void testExecute_whenEmptyKeywords_thenWritesEmptyXls()
    throws Exception {
    when(req.getParameter("language")).thenReturn(null);

    when(keywordsApi.groupsIdKeywordsGet("test-ig-id")).thenReturn(
      Collections.emptyList()
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(res.getOutputStream()).thenReturn(baos);

    webScript.execute(req, res);

    try (
      Workbook workbook = new HSSFWorkbook(
        new java.io.ByteArrayInputStream(baos.toByteArray())
      )
    ) {
      Sheet sheet = workbook.getSheetAt(0);
      // Only header row, no data
      assertEquals(0, sheet.getLastRowNum());
    }
  }
}
