package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HelpApi;
import io.swagger.exception.ValidationException;
import io.swagger.model.ImportResult;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * Webscript to import FAQ structure from a JSON file.
 * Restricted to admin users only (isAdmin or isCircabcAdmin).
 *
 * @author FAQ Import/Export Feature
 */
public class HelpImportPost extends CircabcDeclarativeWebScript {

  private static final Log logger = LogFactory.getLog(HelpImportPost.class);
  private static final String PARAM_FILE = "file";
  private static final int SC_UNPROCESSABLE_ENTITY = 422;

  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    final WebScriptRequest req,
    final Status status,
    final Cache cache
  ) {
    final Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      // Check admin authorization
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException(
          "Cannot import FAQ, not enough permission. User must be admin."
        );
      }

      // Extract multipart file from request using Alfresco FormData
      final FormData form = (FormData) req.parseContent();
      if (form == null) {
        status.setCode(HttpServletResponse.SC_BAD_REQUEST);
        status.setMessage("Not a multipart request");
        status.setRedirect(true);
        return null;
      }

      InputStream fileInputStream = null;
      String fileName = null;
      for (final FormData.FormField field : form.getFields()) {
        if (field.getName().equals(PARAM_FILE) && field.getIsFile()) {
          fileInputStream = field.getInputStream();
          fileName = field.getFilename();
          break;
        }
      }

      if (fileInputStream == null) {
        status.setCode(HttpServletResponse.SC_BAD_REQUEST);
        status.setMessage("No file provided or file is empty");
        status.setRedirect(true);
        if (logger.isWarnEnabled()) {
          logger.warn("FAQ import rejected - no file provided");
        }
        return null;
      }

      // Import FAQ structure
      final ImportResult result = helpApi.importFaq(fileInputStream, fileName);

      // Build success response
      final JSONObject jsonResult = new JSONObject();
      jsonResult.put("message", result.getMessage());
      jsonResult.put("categoriesProcessed", result.getCategoriesProcessed());
      jsonResult.put(
        "subcategoriesProcessed",
        result.getSubcategoriesProcessed()
      );
      jsonResult.put("articlesProcessed", result.getArticlesProcessed());

      model.put("result", jsonResult.toJSONString());

      if (logger.isInfoEnabled()) {
        logger.info(
          String.format(
            "FAQ import completed: %d sections, %d subsections, %d articles",
            result.getCategoriesProcessed(),
            result.getSubcategoriesProcessed(),
            result.getArticlesProcessed()
          )
        );
      }

      return model;
    } catch (final AccessDeniedException e) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied for FAQ import", e);
      }
      return null;
    } catch (final ValidationException e) {
      // Validation errors - return 422 Unprocessable Entity with error details
      status.setCode(SC_UNPROCESSABLE_ENTITY);
      status.setMessage("Validation failed");
      status.setRedirect(true);

      final JSONObject errorResponse = new JSONObject();
      errorResponse.put("message", e.getMessage());
      errorResponse.put("errors", e.getValidationErrors());

      model.put("error", errorResponse.toJSONString());

      if (logger.isWarnEnabled()) {
        logger.warn("FAQ import validation failed: " + e.getMessage());
      }

      return model;
    } catch (final IllegalArgumentException e) {
      // Bad request - invalid file size or format
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage(e.getMessage());
      status.setRedirect(true);
      if (logger.isWarnEnabled()) {
        logger.warn("FAQ import rejected: " + e.getMessage());
      }
      return null;
    } catch (final Exception e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("FAQ import failed", e);
      }
      return null;
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
