package eu.europa.ec.digit.circabc.rest;

import static eu.europa.ec.digit.circabc.rest.RestConstants.ERROR_OCCURRED;

import io.swagger.api.KeywordsApi;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Alfresco web script endpoint that handles the bulk import of keywords for an Interest Group.
 *
 * <p>Bound to an HTTP {@code POST} request (as implied by the {@code Post} suffix in the class
 * name), this endpoint accepts a multipart form upload containing a legacy Excel ({@code .xls},
 * HSSF) workbook whose rows describe keyword definitions in one or more languages. Each row is
 * expected to provide an index (column 0), a language code (column 1) and the keyword title
 * (column 2). Rows sharing the same index are merged into a single multilingual
 * {@link io.swagger.model.KeywordDefinition}.
 *
 * <p>The target Interest Group is identified by the {@code igId} URL template variable, and an
 * optional {@code language} request parameter controls the content locale used while reading and
 * writing multilingual properties. Imported keywords that match an existing keyword (by an
 * identical title in any language) are merged into that keyword by adding the missing language
 * translations; keywords with no match are created as new keywords.
 *
 * <p>The caller must be a library administrator of the target Interest Group; otherwise the
 * request is rejected with HTTP 403. On success the endpoint returns the full, up-to-date list of
 * the group's keywords under the {@code keywords} model key.
 */
public class GroupsBulkKeywordsPost extends CircabcDeclarativeWebScript {

  /** Logger used to record parsing and processing errors during the import. */
  static final Log logger = LogFactory.getLog(GroupsBulkKeywordsPost.class);

  /** API used to read, create and update the Interest Group's keyword definitions. */
  @Autowired
  private KeywordsApi keywordsApi;

  /** Service used to verify that the current user is a library administrator of the group. */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Processes the bulk keyword import request.
   *
   * <p>Resolves the target Interest Group from the {@code igId} template variable, verifies the
   * caller's permissions, extracts the uploaded workbook from the multipart form, parses it into
   * keyword definitions and imports them (creating new keywords or merging translations into
   * existing ones). The resulting keyword list is placed in the model under the {@code keywords}
   * key.
   *
   * @param req the web script request; provides the {@code igId} template variable, the optional
   *     {@code language} parameter and the multipart form containing the uploaded file
   * @param status the response status, updated with an error code, message and redirect flag when
   *     the request cannot be processed
   * @param cache the cache directives for the response (unused)
   * @return a model map containing the {@code keywords} entry with the group's current keywords,
   *     or {@code null} when an error has been reported through {@code status}
   */
  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    boolean mlAware = MLPropertyInterceptor.isMLAware();
    setupLocale(req.getParameter("language"));

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      validatePermission(id);
      InputStream fileInputStream = extractFileFromForm(req);
      if (fileInputStream != null) {
        Map<Integer, KeywordDefinition> data = prepareImportedData(
          fileInputStream
        );
        importKeywords(id, data);
      }
      model.put("keywords", keywordsApi.groupsIdKeywordsGet(id));
    } catch (AccessDeniedException ade) {
      return handleError(status, Status.STATUS_FORBIDDEN, "Access denied", ade);
    } catch (InvalidNodeRefException inre) {
      return handleError(
        status,
        Status.STATUS_BAD_REQUEST,
        "Bad request",
        inre
      );
    } catch (IOException e) {
      return handleError(
        status,
        Status.STATUS_INTERNAL_SERVER_ERROR,
        "Error with parsing the imported file",
        e
      );
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  private void setupLocale(String language) {
    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
  }

  private void validatePermission(String id) {
    if (!currentUserPermissionCheckerService.isInterestGroupLibAdmin(id)) {
      throw new AccessDeniedException(
        "Impossible to import keywords, not enough permissions"
      );
    }
  }

  private InputStream extractFileFromForm(WebScriptRequest req) {
    FormData form = (FormData) req.parseContent();
    if (form == null || !form.getIsMultiPart()) {
      throw new IllegalArgumentException("Not a multipart request.");
    }
    for (FormData.FormField field : form.getFields()) {
      if (field.getIsFile()) {
        return field.getInputStream();
      }
    }
    return null;
  }

  private static Map<Integer, KeywordDefinition> prepareImportedData(
    InputStream fileInputStream
  ) throws IOException {
    Map<Integer, KeywordDefinition> result = new HashMap<>();
    try (HSSFWorkbook wb = new HSSFWorkbook(fileInputStream)) {
      for (int iSheet = 0; iSheet < wb.getNumberOfSheets(); iSheet++) {
        processSheet(wb.getSheetAt(iSheet), result);
      }
    }
    return result;
  }

  private static void processSheet(
    Sheet sheet,
    Map<Integer, KeywordDefinition> result
  ) {
    for (int iRow = 1; iRow <= sheet.getLastRowNum(); iRow++) {
      Row row = sheet.getRow(iRow);
      if (row != null && row.getCell(0) != null) {
        processRow(row, result);
      }
    }
  }

  private static void processRow(
    Row row,
    Map<Integer, KeywordDefinition> result
  ) {
    Integer index = parseIndex(row.getCell(0));
    if (index == null) return;

    KeywordDefinition keywordEntry = result.getOrDefault(
      index,
      new KeywordDefinition()
    );
    String lang = row.getCell(1) != null ? row.getCell(1).toString() : "en";
    String value = row.getCell(2) != null ? row.getCell(2).toString() : "";

    if (!keywordEntry.getTitle().containsKey(lang)) {
      keywordEntry.getTitle().put(lang, value);
    }
    result.put(index, keywordEntry);
  }

  private static Integer parseIndex(Cell cell) {
    try {
      String cellValue =
        cell.getCellType() != CellType.NUMERIC
          ? cell.getStringCellValue()
          : Double.toString(cell.getNumericCellValue());
      return (int) Double.parseDouble(cellValue);
    } catch (NumberFormatException e) {
      logger.error(ERROR_OCCURRED, e);
      return null;
    }
  }

  private void importKeywords(
    String id,
    Map<Integer, KeywordDefinition> impData
  ) {
    List<KeywordDefinition> igKeywords = keywordsApi.groupsIdKeywordsGet(id);
    for (KeywordDefinition keywordEntry : impData.values()) {
      KeywordDefinition existing = findMatchingKeyword(
        keywordEntry,
        igKeywords
      );
      if (existing != null) {
        updateKeyword(keywordEntry, existing);
      } else {
        keywordsApi.groupsIdKeywordsPost(id, keywordEntry);
      }
    }
  }

  private KeywordDefinition findMatchingKeyword(
    KeywordDefinition entry,
    List<KeywordDefinition> igKeywords
  ) {
    for (String key : entry.getTitle().keySet()) {
      for (KeywordDefinition igKey : igKeywords) {
        if (
          igKey.getTitle().containsKey(key) &&
          igKey.getTitle().get(key).equals(entry.getTitle().get(key))
        ) {
          return igKey;
        }
      }
    }
    return null;
  }

  private void updateKeyword(
    KeywordDefinition source,
    KeywordDefinition target
  ) {
    boolean updated = false;
    for (String key : source.getTitle().keySet()) {
      if (!target.getTitle().containsKey(key)) {
        target.getTitle().put(key, source.getTitle().get(key));
        updated = true;
      }
    }
    if (updated) {
      keywordsApi.keywordsKeywordIdPut(target.getId(), target);
    }
  }

  private Map<String, Object> handleError(
    Status status,
    int code,
    String message,
    Exception e
  ) {
    logger.error(ERROR_OCCURRED, e);
    status.setCode(code);
    status.setMessage(message);
    status.setRedirect(true);
    return null; // NOSONAR
  }
}
