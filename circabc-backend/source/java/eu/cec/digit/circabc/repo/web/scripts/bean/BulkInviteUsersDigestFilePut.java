package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.user.BulkImportUserData;
import io.swagger.api.UsersApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkInviteUsersDigestFilePut extends CircabcDeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(BulkInviteUsersDigestFilePut.class);

    private UsersApi usersApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        MLPropertyInterceptor.setMLAware(false);

        try {

            String igId = req.getParameter("igId");

            if ((igId == null) || igId.trim().isEmpty()) {
                throw new IllegalArgumentException("'igId' cannot be empty.");
            }

            if (!this.currentUserPermissionCheckerService.hasAlfrescoReadPermission(igId)) {
                throw new AccessDeniedException("No access on node: " + igId);
            }

            FormData form = (FormData) req.parseContent();

            if (form == null) {
                throw new IllegalArgumentException("Not a multipart request.");
            }

            if (form.getFields().length != 1) {
                throw new IllegalArgumentException("Wrong number of parameters.");
            }

            InputStream inputStream = null;
            String fileName = null;

            for (FormData.FormField field : form.getFields()) {

                if (field.getName().equals("fileData") && field.getIsFile()) {
                    inputStream = field.getInputStream();
                    fileName = field.getFilename();
                } else {
                    throw new IllegalArgumentException("Incorrect file parameter.");
                }
            }

            List<BulkImportUserData> userData =
                    this.usersApi.bulkInviteUsersDigestFile(igId, inputStream, fileName);

            model.put("userData", userData);

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
        } catch (Exception e) {
            status.setCode(HttpServletResponse.SC_NOT_ACCEPTABLE);
            status.setMessage(e.getMessage());
            status.setException(e);
            status.setRedirect(true);
            return null;
        } finally {
            MLPropertyInterceptor.setMLAware(mlAware);
        }

        return model;
    }

    /**
     * @param usersApi the usersApi to set
     */
    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
