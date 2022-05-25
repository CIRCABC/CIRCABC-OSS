package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import io.swagger.api.AutoUploadApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class AutoUploadGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(AutoUploadGet.class);

    private AutoUploadApi autoUploadApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    private static int retrieveDayFrequencyFromCron(String dateRestriction) {

        String[] strings = dateRestriction.split(" ");
        int result;

        if (strings[5].equals("*")) {
            result = -1;
        } else {
            result = Integer.parseInt(strings[5]);
        }

        return result;
    }

    private static int retrieveHourFrequencyFromCron(String dateRestriction) {

        String[] strings = dateRestriction.split(" ");
        int result;

        if (strings[2].equals("*")) {
            result = -1;
        } else {
            result = Integer.parseInt(strings[2]);
        }

        return result;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        MLPropertyInterceptor.setMLAware(false);

        try {

            Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
            String igId = templateVars.get("id");

            if (!currentUserPermissionCheckerService.isGroupAdmin(igId)) {
                throw new AccessDeniedException("No access on IG: " + igId);
            }

            String nodeId = req.getParameter("nodeId");

            Configuration configuration = this.autoUploadApi.getAutoUploadEntry(nodeId);

            model.put("autoupload", configuration);

            if (configuration != null) {
                model.put("dayChoice", retrieveDayFrequencyFromCron(configuration.getDateRestriction()));
                model.put("hourChoice", retrieveHourFrequencyFromCron(configuration.getDateRestriction()));
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
