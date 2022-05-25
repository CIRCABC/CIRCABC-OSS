package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.CategoriesApi;
import io.swagger.model.Category;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.CategoryJsonParser;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CategoryPut extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(CategoryPut.class);

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
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
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

            if (!this.currentUserPermissionCheckerService.throwIfNotCategoryAdmin(categoryId)) {
                throw new AccessDeniedException("No access on node:" + categoryId);
            }

            Category categoryBody = CategoryJsonParser.parseSimpleJSON(req);

            model.put("category", this.categoriesApi.categoriesIdPut(categoryId, categoryBody));

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
        } catch (IOException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad body");
            status.setRedirect(true);
            return null;
        } catch (ParseException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Parse execption " + req, e);
            }
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad body");
            status.setRedirect(true);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
        return model;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
