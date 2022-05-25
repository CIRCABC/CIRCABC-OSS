package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AutoUploadApi;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to retrieve a list of autoupload configuration
 *
 * @author schwerr
 */
public class AutoUploadsGet extends DeclarativeWebScript {

    private AutoUploadApi autoUploadApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String igId = templateVars.get("id");
        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
                throw new AccessDeniedException("No access on IG: " + igId);
            }

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

            PagedAutoUploadConfiguration pagedItems =
                    this.autoUploadApi.getAutoUploadEntries(igId, startRecord, limit);

            model.put("autouploads", pagedItems.getData());
            model.put("total", pagedItems.getTotal());
        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage(e.getMessage());
            status.setException(e);
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
     * @param autoUploadApi the autoUploadApi to set
     */
    public void setAutoUploadApi(AutoUploadApi autoUploadApi) {
        this.autoUploadApi = autoUploadApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
