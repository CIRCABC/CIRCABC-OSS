package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.EmailApi;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Webscript entry to save the current email template in the current user's email template space
 *
 * @author schwerr
 */
public class GroupsUserEmailTemplatesDelete extends DeclarativeWebScript {

    private EmailApi emailApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        try {

            String ids = req.getParameter("templateIds");

            if (ids == null || "".equals(ids)) {
                throw new IllegalArgumentException("'templateIds' cannot be empty. It's mandatory.");
            }

            this.emailApi.deleteUserMailTemplates(ids);

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
        }

        return model;
    }

    /**
     * @return the emailApi
     */
    public EmailApi getEmailApi() {
        return this.emailApi;
    }

    /**
     * @param emailApi the emailApi to set
     */
    public void setEmailApi(EmailApi emailApi) {
        this.emailApi = emailApi;
    }
}
