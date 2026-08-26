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

public class HelpCategoriesOrderPut extends CircabcDeclarativeWebScript {

  static final Log logger = LogFactory.getLog(HelpCategoriesOrderPut.class);

  private HelpApi helpApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    try {
      if (
        !(currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          currentUserPermissionCheckerService.isCircabcAdmin())
      ) {
        throw new AccessDeniedException("Forbidden");
      }

      final List<String> categoryIds = SimpleIdJsonParser.parseListOfId(req);

      helpApi.reorderCategories(categoryIds);
    } catch (AccessDeniedException e) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Forbidden");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Access denied for reorder categories", e);
      }
      return null;
    } catch (InvalidArgumentException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage(e.getMsgId());
      status.setRedirect(true);
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid request for reorder categories: " + e.getMsgId());
      }
      return null;
    } catch (IOException | ParseException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Invalid request body");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Failed to parse request body for reorder categories", e);
      }
      return null;
    } catch (Exception e) {
      status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      if (logger.isErrorEnabled()) {
        logger.error("Internal server error during reorder categories", e);
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
