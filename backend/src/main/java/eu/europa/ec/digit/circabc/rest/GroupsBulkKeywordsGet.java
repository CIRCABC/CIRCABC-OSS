package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.KeywordsApi;
import io.swagger.model.I18nProperty;
import io.swagger.model.KeywordDefinition;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * REST webscript endpoint that exports all keywords defined for an Interest Group
 * as a binary Excel (XLS) spreadsheet.
 *
 * <p>The class name implies an HTTP {@code GET} operation. The endpoint resolves the
 * target Interest Group from the {@code igId} URL template variable, verifies that the
 * current user is allowed to access that Interest Group, retrieves its keyword
 * definitions and streams them back to the caller as an HSSF workbook.
 *
 * <p>Key inputs:
 * <ul>
 *   <li>{@code igId} URL template variable identifying the Interest Group whose
 *       keywords are exported.</li>
 *   <li>{@code language} optional request parameter. When provided, the keyword
 *       titles are localised to that language (multilingual awareness disabled);
 *       when absent, the export is multilingual-aware and includes every available
 *       language for each keyword.</li>
 * </ul>
 *
 * <p>If the current user lacks access to the Interest Group the response status is set
 * to {@link Status#STATUS_FORBIDDEN}; any other failure results in an
 * {@link IOException}.
 *
 * <p>Unlike most CIRCABC endpoints this webscript extends {@link AbstractWebScript}
 * directly (rather than rendering a FreeMarker template) because it produces a binary
 * spreadsheet stream instead of a JSON payload.
 */
public class GroupsBulkKeywordsGet extends AbstractWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(GroupsBulkKeywordsGet.class);

  /**
   * API used to retrieve the keyword definitions associated with an Interest Group.
   */
  @Autowired
  private KeywordsApi keywordsApi;

  /**
   * Service used to verify that the current user is allowed to access the requested
   * Interest Group before its keywords are exported.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Writes the supplied keyword definitions into an HSSF (XLS) workbook and streams the
   * result to the given output stream.
   *
   * <p>Each keyword contributes one row per localised title value, with the columns
   * {@code Index}, {@code Language} and {@code Value}. When the maximum number of rows
   * supported by a sheet is reached, a new sheet is created to continue the export.
   *
   * @param data the keyword definitions to export; each definition may carry titles in
   *             several languages
   * @param outStream the stream the generated workbook is written to
   * @throws IOException if the workbook cannot be written to the output stream
   */
  private static void writeXLS(
    List<KeywordDefinition> data,
    OutputStream outStream
  ) throws IOException {
    try (Workbook workbook = new HSSFWorkbook()) {
      int index = 1;
      int indexSheet = 1;
      int iRow = 1;

      Sheet sheet = workbook.createSheet("Keywords" + indexSheet);
      Row titleRow = sheet.createRow(0);
      titleRow.createCell(0).setCellValue("Index");
      titleRow.createCell(1).setCellValue("Language");
      titleRow.createCell(2).setCellValue("Value");

      for (KeywordDefinition keyword : data) {
        I18nProperty title = keyword.getTitle();

        for (Map.Entry<String, String> entry : title.entrySet()) {
          Row row = sheet.createRow(iRow);
          row.createCell(0).setCellValue(index);
          row.createCell(1).setCellValue(entry.getKey());
          row.createCell(2).setCellValue(entry.getValue());
          iRow++;
        }

        if (iRow > 1048576) {
          sheet = workbook.createSheet("Keywords" + indexSheet);
          Row titleRow2 = sheet.createRow(0);
          titleRow2.createCell(0).setCellValue("Index");
          titleRow2.createCell(1).setCellValue("Language");
          titleRow2.createCell(2).setCellValue("Value");
          iRow = 1;
        }

        index++;
      }

      workbook.write(outStream);
    }
  }

  /**
   * Handles the incoming request: configures multilingual behaviour according to the
   * optional {@code language} parameter, resolves the Interest Group from the
   * {@code igId} URL template variable, checks the current user's access rights,
   * retrieves the group's keyword definitions and streams them back as an XLS workbook.
   *
   * <p>On {@link AccessDeniedException} the response status is set to
   * {@link Status#STATUS_FORBIDDEN}. The previous multilingual-awareness setting is
   * always restored before returning.
   *
   * @param req the web script request; supplies the {@code igId} template variable and
   *            the optional {@code language} parameter
   * @param res the web script response the generated workbook is written to
   * @throws IOException if the keywords cannot be exported or written to the response
   * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
   * org.springframework.extensions.webscripts.WebScriptResponse)
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = Locale.of(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String id = templateVars.get("igId");

    try {
      this.currentUserPermissionCheckerService.throwIfCanNotAccessInterestGroup(
        id
      );
      List<KeywordDefinition> list = this.keywordsApi.groupsIdKeywordsGet(id);
      writeXLS(list, res.getOutputStream());
    } catch (AccessDeniedException ade) {
      res.setStatus(Status.STATUS_FORBIDDEN);
    } catch (Exception e) {
      throw new IOException("Could not export keywords.", e);
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
  }
}
