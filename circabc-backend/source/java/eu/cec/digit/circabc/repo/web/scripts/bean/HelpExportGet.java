package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Webscript to export the complete FAQ structure to JSON format.
 * Restricted to admin users only (isAdmin or isCircabcAdmin).
 *
 * @author FAQ Import/Export Feature
 */
public class HelpExportGet extends AbstractWebScript {

  private static final Log logger = LogFactory.getLog(HelpExportGet.class);
  private static final String CONTENT_TYPE_JSON = "application/json";
  private static final String HEADER_CONTENT_DISPOSITION =
    "Content-Disposition";
  private static final String FILENAME_PREFIX = "faq-export-";
  private static final String FILENAME_EXTENSION = ".json";
  private static final String DATE_FORMAT = "yyyy-MM-dd-HHmmss";

  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  public void execute(final WebScriptRequest req, final WebScriptResponse res)
    throws IOException {
    try {
      // Check admin authorization
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException(
          "Cannot export FAQ, not enough permission. User must be admin."
        );
      }

      // Export FAQ structure
      final String jsonContent = helpApi.exportFaq();

      // Set response headers
      res.setContentType(CONTENT_TYPE_JSON);
      res.setContentEncoding(StandardCharsets.UTF_8.name());

      // Generate filename with timestamp
      final String timestamp = new SimpleDateFormat(DATE_FORMAT).format(
        new Date()
      );
      final String filename = FILENAME_PREFIX + timestamp + FILENAME_EXTENSION;
      res.setHeader(
        HEADER_CONTENT_DISPOSITION,
        "attachment; filename=\"" + filename + "\""
      );

      // Write JSON content to response
      final byte[] jsonBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
      res.getOutputStream().write(jsonBytes);
      res.getOutputStream().flush();

      if (logger.isInfoEnabled()) {
        logger.info("FAQ export completed successfully. File: " + filename);
      }
    } catch (final AccessDeniedException e) {
      res.setStatus(HttpServletResponse.SC_FORBIDDEN);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied for FAQ export", e);
      }
    } catch (final Exception e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      if (logger.isErrorEnabled()) {
        logger.error("FAQ export failed", e);
      }
    }
  }

  public HelpApi getHelpApi() {
    return helpApi;
  }

  public void setHelpApi(final HelpApi helpApi) {
    this.helpApi = helpApi;
  }

  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
  }

  public void setCurrentUserPermissionCheckerService(
    final CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
