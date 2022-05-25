package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HelpApi;
import io.swagger.model.HelpCategory;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.HelpJsonParser;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
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

/**
 * @author beaurpi
 */
public class HelpCategoryPut extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(HelpCategoryPut.class);

    private HelpApi helpApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

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

            if (!(currentUserPermissionCheckerService.isAlfrescoAdmin()
                    || currentUserPermissionCheckerService.isCircabcAdmin())) {
                throw new AccessDeniedException("Cannot create help category, not enough permission");
            }

            String id = templateVars.get("id");

            if ("".equals(id)) {
                throw new InvalidArgumentException();
            }

            HelpCategory category = HelpJsonParser.parseCategory(req);
            model.put("category", helpApi.updateHelpCategory(id, category));

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (IOException | ParseException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Internal error");
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
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
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
