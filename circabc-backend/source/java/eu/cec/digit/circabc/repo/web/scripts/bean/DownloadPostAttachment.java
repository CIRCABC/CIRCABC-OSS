package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import io.swagger.api.TopicsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.Map;

/**
 * Webscript entry to download the forum attachment given by its id (hidden content)
 *
 * @author schwerr
 */
public class DownloadPostAttachment extends AbstractWebScript {

    static final Log logger = LogFactory.getLog(DownloadPostAttachment.class);

    private TopicsApi topicsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
     * org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String id = templateVars.get("id");

        try {

            if (!this.currentUserPermissionCheckerService.hasAnyOfNewsGroupPermission(
                    id, NewsGroupPermissions.NWSACCESS)) {
                throw new AccessDeniedException(
                        "Impossible to download attachment, not enough permissions");
            }

            MLPropertyInterceptor.setMLAware(true);

            try (OutputStream outStream = res.getOutputStream()) {
                topicsApi.getAttachment(id, outStream);
            }
        } catch (Exception e) {
            logger.error("Could not download attachment.", e);
            throw new IOException("Could not download attachment.", e);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }

    /**
     * @param topicsApi the topicsApi to set
     */
    public void setTopicsApi(TopicsApi topicsApi) {
        this.topicsApi = topicsApi;
    }
}
