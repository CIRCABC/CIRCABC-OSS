package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.UsersApi;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;

/**
 * Webscript entry to download the bulk invite template
 *
 * @author schwerr
 */
public class BulkInviteDownloadTemplate extends AbstractWebScript {

    static final Log logger = LogFactory.getLog(BulkInviteDownloadTemplate.class);

    private UsersApi usersApi;

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest,
     * org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        try {

            MLPropertyInterceptor.setMLAware(true);

            this.usersApi.writeBulkInviteTemplate(res);

        } catch (Exception e) {
            logger.error("Could not export members.", e);
            throw new IOException("Could not export members.", e);
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }
    }

    /**
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }
}
