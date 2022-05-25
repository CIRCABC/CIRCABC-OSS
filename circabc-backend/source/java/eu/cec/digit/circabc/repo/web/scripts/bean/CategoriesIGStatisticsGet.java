package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.CategoriesApi;
import io.swagger.model.PagedStatisticsContents;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class CategoriesIGStatisticsGet extends CircabcDeclarativeWebScript {

    static final Log logger = LogFactory.getLog(CategoriesIGStatisticsGet.class);

    private CategoriesApi categoriesApi;

    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {
            if (!this.currentUserPermissionCheckerService.isCircabcAdmin()) {
                this.currentUserPermissionCheckerService.throwIfNotCategoryAdmin(id);
            }

            MLPropertyInterceptor.setMLAware(false);

            String pageStr = req.getParameter("page");
            int page = 0;
            page = getPage(pageStr, page);

            String limitStr = req.getParameter("limit");
            int limit = 0;
            if ((limitStr != null) && !limitStr.isEmpty()) {
                limit = getLimit(limitStr, limit);
            }

            if (page <= 0) {
                throw new IllegalArgumentException("Values for 'page' must be > 0");
            }

            if (limit < 0) {
                throw new IllegalArgumentException("Values for 'limit' must be >= 0");
            }

            MLPropertyInterceptor.setMLAware(false);

            int startRecord = (page - 1) * limit;

            PagedStatisticsContents contents =
                    this.categoriesApi.getIGStatisticsContents(id, startRecord, limit);

            model.put("data", contents.getData());
            model.put("total", contents.getTotal());
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

    private int getLimit(String limitStr, int limit) {
        try {
            limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'limit': " + limit, e);
        }
        return limit;
    }

    private int getPage(String pageStr, int page) {
        try {
            page = (((pageStr == null) || pageStr.isEmpty()) ? 1 : Integer.parseInt(pageStr));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Wrong numeric value for 'page': " + page, e);
        }
        return page;
    }

    /**
     * @param categoriesApi the categoriesApi to set
     */
    public void setCategoriesApi(CategoriesApi categoriesApi) {
        this.categoriesApi = categoriesApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
