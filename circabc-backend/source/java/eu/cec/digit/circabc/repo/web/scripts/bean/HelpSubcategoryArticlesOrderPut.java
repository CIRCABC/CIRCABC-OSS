package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HelpSubcategoryArticlesOrderPut
  extends CircabcDeclarativeWebScript {

  static final Log logger = LogFactory.getLog(
    HelpSubcategoryArticlesOrderPut.class
  );

  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);
    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("Forbidden");
      }

      final String id = templateVars.get("id");
      if (id == null || id.isEmpty()) {
        throw new InvalidArgumentException("Subcategory ID is required");
      }

      final List<String> articleIds = SimpleIdJsonParser.parseListOfId(req);

      helpApi.reorderSubcategoryArticles(id, articleIds);
    } catch (AccessDeniedException e) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Forbidden");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied for reorder subcategory articles", e);
      }
      return null;
    } catch (InvalidArgumentException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage(e.getMsgId());
      status.setRedirect(true);
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Invalid request for reorder subcategory articles: " + e.getMsgId()
        );
      }
      return null;
    } catch (IOException | ParseException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Invalid request body");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(
          "Failed to parse request body for reorder subcategory articles",
          e
        );
      }
      return null;
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error(
          "Internal server error during reorder subcategory articles",
          e
        );
      }
      return null;
    }

    return model;
  }

  public HelpApi getHelpApi() {
    return helpApi;
  }

  public void setHelpApi(HelpApi helpApi) {
    this.helpApi = helpApi;
  }

  public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
    return currentUserPermissionCheckerService;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
