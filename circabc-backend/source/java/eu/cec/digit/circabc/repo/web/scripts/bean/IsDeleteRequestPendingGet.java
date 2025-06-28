package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.CategoriesApi;
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

public class IsDeleteRequestPendingGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(IsDeleteRequestPendingGet.class);

  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String groupId = templateVars.get("groupId");

    boolean mlAware = MLPropertyInterceptor.isMLAware();
    String language = req.getParameter("language");

    if (language == null) {
      MLPropertyInterceptor.setMLAware(true);
    } else {
      Locale locale = new Locale(language);
      I18NUtil.setContentLocale(locale);
      I18NUtil.setLocale(locale);
      MLPropertyInterceptor.setMLAware(false);
    }

    try {
      if (!currentUserPermissionCheckerService.isGroupAdmin(groupId)) {
        throw new AccessDeniedException(
          "Impossible to edit the category group request. Not enough permissions"
        );
      }
      boolean isPending = this.categoriesApi.existsGroupDeleteRequest(groupId);
      model.put("isPending", isPending);
    } catch (AccessDeniedException ade) {
      status.setCode(HttpServletResponse.SC_FORBIDDEN);
      status.setMessage("Access denied");
      status.setRedirect(true);
      return null;
    } catch (InvalidNodeRefException inre) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request");
      status.setRedirect(true);
      return null;
    } finally {
      MLPropertyInterceptor.setMLAware(mlAware);
    }
    return model;
  }

  public void setCurrentUserPermissionCheckerService(
    CurrentUserPermissionCheckerService currentUserPermissionCheckerService
  ) {
    this.currentUserPermissionCheckerService =
      currentUserPermissionCheckerService;
  }

  /**
   * @return the categoriesApi
   */
  public CategoriesApi getCategoriesApi() {
    return this.categoriesApi;
  }

  /**
   * @param categoriesApi the categoriesApi to set
   */
  public void setCategoriesApi(CategoriesApi categoriesApi) {
    this.categoriesApi = categoriesApi;
  }
}
