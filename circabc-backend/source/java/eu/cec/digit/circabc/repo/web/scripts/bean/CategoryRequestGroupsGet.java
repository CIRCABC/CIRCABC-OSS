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

public class CategoryRequestGroupsGet extends DeclarativeWebScript {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(CategoryRequestGroupsGet.class);

  private CategoriesApi categoriesApi;
  private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

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

  @Override
  protected Map<String, Object> executeImpl(
    WebScriptRequest req,
    Status status,
    Cache cache
  ) {
    Map<String, Object> model = new HashMap<>(7, 1.0f);

    Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
    String categoryId = templateVars.get("id");

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
      // verification if the user is category admin

      if (!currentUserPermissionCheckerService.isCategoryAdmin(categoryId)) {
        throw new AccessDeniedException(
          "Impossible to list the category group requests. Not enough permissions"
        );
      }

      String limit = req.getParameter("limit");
      int limitInt = 10;
      if (limit != null) {
        limitInt = Integer.parseInt(limit);
      }

      String page = req.getParameter("page");
      int pageInt = 0;
      if (page != null) {
        pageInt = Integer.parseInt(page);
      }

      String filter = req.getParameter("filter");

      model.put(
        "requests",
        this.categoriesApi.categoriesIdGroupRequestsGet(
            categoryId,
            limitInt,
            pageInt,
            filter
          )
      );
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
    } catch (NumberFormatException e) {
      status.setCode(HttpServletResponse.SC_BAD_REQUEST);
      status.setMessage("Bad request query parameters");
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
}
