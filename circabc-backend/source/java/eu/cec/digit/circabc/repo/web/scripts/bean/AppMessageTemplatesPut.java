package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AppMessageApi;
import io.swagger.model.AppMessage;
import io.swagger.util.CurrentUserPermissionCheckerService;
import io.swagger.util.parsers.AppMessageJsonParser;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author beaurpi
 */
public class AppMessageTemplatesPut extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(AppMessageTemplatesPut.class);

    private AppMessageApi appMessageApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        try {

            if (!(currentUserPermissionCheckerService.isAlfrescoAdmin()
                    || currentUserPermissionCheckerService.isCircabcAdmin())) {
                throw new AccessDeniedException("Not enough permissions");
            }

            AppMessage template = AppMessageJsonParser.parse(req);
            appMessageApi.updateAppMessageTemplate(template);

            String notifyStr = req.getParameter("notification");
            if (!"".equals(notifyStr) && Boolean.parseBoolean(notifyStr) && template.getEnabled()) {
                appMessageApi.notifyTemplate(template);
            }

        } catch (AccessDeniedException ade) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied");
            status.setRedirect(true);
            return null;
        } catch (IOException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Problem with object");
            status.setRedirect(true);
            return null;
        } catch (ParseException e) {
            status.setCode(HttpServletResponse.SC_BAD_REQUEST);
            status.setMessage("Bad JSON format");
            status.setRedirect(true);
            return null;
        }

        return model;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @return the appMessageApi
     */
    public AppMessageApi getAppMessageApi() {
        return appMessageApi;
    }

    /**
     * @param appMessageApi the appMessageApi to set
     */
    public void setAppMessageApi(AppMessageApi appMessageApi) {
        this.appMessageApi = appMessageApi;
    }
}
