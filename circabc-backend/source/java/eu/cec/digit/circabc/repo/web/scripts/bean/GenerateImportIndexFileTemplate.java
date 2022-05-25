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

/**
 * Webscript entry to generate the index file template for the import operation
 *
 * @author schwerr
 */
public class GenerateImportIndexFileTemplate extends AbstractWebScript {

    static final Log logger = LogFactory.getLog(GenerateImportIndexFileTemplate.class);

    private GroupsApi groupsApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
     * org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);

            this.groupsApi.generateImportIndexFileTemplate(res);

        } catch (Exception e) {
            logger.error("Could not generate index file.", e);
            throw new IOException("Could not generate index file.", e);
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
