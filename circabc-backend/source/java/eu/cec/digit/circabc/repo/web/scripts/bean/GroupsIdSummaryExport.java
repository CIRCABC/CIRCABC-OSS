package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.GroupsApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

/**
 * Webscript entry to export the summary of an IG in different formats: CSV, XLS, or XML
 *
 * @author schwerr
 */
public class GroupsIdSummaryExport extends AbstractWebScript {

    static final Log logger = LogFactory.getLog(GroupsIdSummaryExport.class);

    private GroupsApi groupsApi;
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

            if (!this.currentUserPermissionCheckerService.isGroupAdmin(id)) {
                throw new AccessDeniedException(
                        "Impossible to get the summary of the Interest Group, not enough permissions");
            }

            MLPropertyInterceptor.setMLAware(true);

            String format = req.getParameter("format");

            if ((format == null)
                    || !("csv".equalsIgnoreCase(format)
                    || "xls".equalsIgnoreCase(format)
                    || "xml".equalsIgnoreCase(format))) {
                throw new IllegalArgumentException("Export 'format' must be CSV, XML or XLS");
            }

            String type = req.getParameter("type");

            if ((type == null)
                    || !("statistics".equalsIgnoreCase(type) || "timeline".equalsIgnoreCase(type))) {
                throw new IllegalArgumentException("Export 'type' must be statistics or timeline");
            }

            this.groupsApi.exportSummary(id, format.toLowerCase(), type.toLowerCase(), res);

        } catch (Exception e) {
            logger.error("Could not export members.", e);
            throw new IOException("Could not export members.", e);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    /**
     * @param groupsApi the groupsApi to set
     */
    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
