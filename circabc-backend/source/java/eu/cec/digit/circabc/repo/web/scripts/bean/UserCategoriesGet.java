package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author beaurpi
 */
public class UserCategoriesGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(UserCategoriesGet.class);

  private UsersApi usersApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

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

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    try {
      String userId = templateVars.get("userId");
      if (userId != null) {
        if (
          this.currentUserPermissionCheckerService.isCurrentUserEqualTo(
              userId
            ) ||
          this.currentUserPermissionCheckerService.isAlfrescoAdmin() ||
          this.currentUserPermissionCheckerService.isCircabcAdmin()
        ) {
          model.put("categories", this.usersApi.getUserCategories(userId));
        } else {
          throw new AccessDeniedException("Operation is not allowed");
        }
      }
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }

    return model;
  }

  /**
   * @return the usersApi
   */
  public UsersApi getUsersApi() {
    return this.usersApi;
  }

  /**
   * @param usersApi the usersApi to set
   */
  public void setUsersApi(UsersApi usersApi) {
    this.usersApi = usersApi;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }
}
