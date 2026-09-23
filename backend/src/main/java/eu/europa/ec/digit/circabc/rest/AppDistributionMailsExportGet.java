package eu.europa.ec.digit.circabc.rest;

import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.io.OutputStream;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Alfresco web script endpoint that exports the application-wide distribution
 * mailing list as a downloadable Excel (.xls) file.
 *
 * <p>The class name follows the {@code <Entity><Method>} convention: the
 * trailing {@code Get} indicates this endpoint is bound to the HTTP
 * {@code GET} method. When invoked, it streams a spreadsheet named
 * {@code distribution-list.xls} back to the client as a file attachment.
 *
 * <p>Access is restricted to administrators: the request is rejected with an
 * HTTP {@code 403 Forbidden} status unless the current user is either an
 * Alfresco administrator or a CIRCABC administrator. The endpoint takes no
 * request parameters; the distribution list is resolved server-side through
 * {@link AppMessageApi#getdistributionListAsExcel()}.
 *
 * <p>Unlike most CIRCABC endpoints, this class extends {@link AbstractWebScript}
 * directly (rather than a declarative web script) because it writes a binary
 * response stream instead of rendering a FreeMarker/JSON template.
 *
 * @author beaurpi
 */
public class AppDistributionMailsExportGet extends AbstractWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(
    AppDistributionMailsExportGet.class
  );

  /**
   * API used to build the distribution list workbook that is streamed to the
   * client.
   */
  @Autowired
  private AppMessageApi appMessageApi;

  /**
   * Service used to verify that the current user holds the administrator
   * privileges required to perform the export.
   */
  @Autowired
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  /**
   * Handles the incoming GET request by generating and streaming the
   * distribution list as an Excel attachment.
   *
   * <p>The method first checks that the current user is an Alfresco or CIRCABC
   * administrator. If so, it sets the appropriate {@code Content-Disposition}
   * and {@code Content-Type} response headers, builds the workbook via
   * {@link AppMessageApi#getdistributionListAsExcel()} and writes it to the
   * response output stream. Errors are handled internally by setting the
   * corresponding HTTP status code:
   * <ul>
   *   <li>{@code 403 Forbidden} when the user lacks administrator rights;</li>
   *   <li>{@code 500 Internal Server Error} for I/O or unexpected failures.</li>
   * </ul>
   * The output stream is always closed in a {@code finally} block.
   *
   * @param req the incoming web script request (no parameters are read)
   * @param res the web script response the Excel workbook is written to
   * @throws IOException if closing the response output stream fails
   */
  @Override
  public void execute(WebScriptRequest req, WebScriptResponse res)
    throws IOException {
    OutputStream outStream = null;
    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("");
      }

      res.setHeader(
        "Content-Disposition",
        "attachment;filename=distribution-list.xls"
      );
      res.setContentType("application/vnd.ms-excel;charset=UTF-8");

      Workbook workbook = appMessageApi.getdistributionListAsExcel();
      outStream = res.getOutputStream();
      workbook.write(outStream);
    } catch (AccessDeniedException ade) {
      logger.error("Access denied to export distribution mails", ade);
      res.setStatus(Status.STATUS_FORBIDDEN);
    } catch (IOException e) {
      logger.error("Error generating Excel export", e);
      res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error("Unexpected error exporting distribution mails", e);
      res.setStatus(Status.STATUS_INTERNAL_SERVER_ERROR);
    } finally {
      if (outStream != null) {
        outStream.close();
      }
    }
  }
}
