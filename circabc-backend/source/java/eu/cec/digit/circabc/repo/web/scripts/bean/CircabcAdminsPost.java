package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.CircabcApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.SimpleIdJsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class CircabcAdminsPost extends CircabcDeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CircabcAdminsPost.class);

  private CircabcApi circabcApi;

  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  public void setCircabcApi(CircabcApi circabcApi) {
    this.circabcApi = circabcApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    String language = req.getParameter("language");
    boolean mlAware = MLPropertyInterceptor.isMLAware();

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = new Locale(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }
    try {
      boolean isAllowed =
        currentUserPermissionCheckerService.isCircabcAdmin() ||
        currentUserPermissionCheckerService.isAlfrescoAdmin();
      if (!isAllowed) {
        throw new AccessDeniedException(
          "The user don't have enough permissions"
        );
      }

      List<String> userIds = SimpleIdJsonParser.parseListOfId(req);
      this.circabcApi.circabcAdminsPost(userIds);
    } catch (AccessDeniedException ade) {
      logger.error(ade);
      status.setCode(Status.STATUS_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException | ParseException | IOException inre) {
      logger.error(inre);
      status.setCode(Status.STATUS_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    } catch (Exception e) {
      logger.error(e);
      status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR);
      status.setMessage("Internal server error");
      status.setRedirect(true);
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }
}
