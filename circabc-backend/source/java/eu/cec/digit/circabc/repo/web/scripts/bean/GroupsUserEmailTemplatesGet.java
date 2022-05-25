package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.EmailApi;
import io.swagger.model.MailTemplateDefinition;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Webscript entry to get the list of mail templates created by the current user
 *
 * @author schwerr
 */
public class GroupsUserEmailTemplatesGet extends DeclarativeWebScript {

    private EmailApi emailApi;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        try {

            List<MailTemplateDefinition> templates = this.emailApi.getUserMailTemplates();

            model.put("templates", templates);

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
