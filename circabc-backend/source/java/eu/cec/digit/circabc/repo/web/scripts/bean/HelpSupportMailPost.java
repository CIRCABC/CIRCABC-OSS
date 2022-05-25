package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.HelpApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * @author beaurpi
 */
public class HelpSupportMailPost extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(HelpSupportMailPost.class);

    private HelpApi helpApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

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

            if (this.currentUserPermissionCheckerService.isGuest()) {
                throw new AccessDeniedException("Contact support not allowed for guest");
            }

            FormData form = (FormData) req.parseContent();

            if ((form == null) || !form.getIsMultiPart()) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            List<File> attachementsFiles = new ArrayList<>();
            String reason = "";
            String name = "";
            String email = "";
            String subject = "";
            String content = "";

            for (FormData.FormField field : form.getFields()) {
                if ("reason".equals(field.getName())) {
                    reason = field.getValue();
                } else if ("name".equals(field.getName())) {
                    name = field.getValue();
                } else if ("email".equals(field.getName())) {
                    email = field.getValue();
                } else if ("subject".equals(field.getName())) {
                    subject = field.getValue();
                } else if ("content".equals(field.getName())) {
                    content = field.getValue();
                } else if (field.getIsFile()) {
                    if (field.getInputStream() != null) {
                        File attahhement =
                                TempFileProvider.createTempFile(
                                        field.getInputStream(), field.getFilename(), "cbctmp");
                        attachementsFiles.add(attahhement);
                    }
                }
            }

            // no security check - it is accessible to everyone
            helpApi.contactSupport(reason, name, email, subject, content, attachementsFiles);

        } catch (AccessDeniedException e) {
            status.setCode(HttpServletResponse.SC_FORBIDDEN);
            status.setMessage("Access denied for guest");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } catch (IllegalArgumentException e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Internal server error - bad arguments");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }

            return null;
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setMessage("Internal server error during sending email");
            status.setRedirect(true);
            if (logger.isErrorEnabled()) {
                logger.error(e);
            }
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
