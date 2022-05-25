package eu.cec.digit.circabc.repo.web.scripts.bean;

import io.swagger.api.AppMessageApi;
import io.swagger.util.CurrentUserPermissionCheckerService;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author beaurpi
 */
public class AppDistributionMailsExportGet extends AbstractWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(AppDistributionMailsExportGet.class);

    private AppMessageApi appMessageApi;
    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
        OutputStream outStream = null;
        try {

            if (!(currentUserPermissionCheckerService.isAlfrescoAdmin()
                    || currentUserPermissionCheckerService.isCircabcAdmin())) {
                throw new AccessDeniedException("");
            }

            res.setHeader("Content-Disposition", "attachment;filename=distribution-list.xls");
            res.setContentType("application/vnd.ms-excel;charset=UTF-8");

            Workbook workbook = appMessageApi.getdistributionListAsExcel();
            outStream = res.getOutputStream();
            workbook.write(outStream);

        } catch (AccessDeniedException ade) {
            res.setStatus(HttpStatus.FORBIDDEN_403);
        } catch (IOException e) {
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
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

    /**
     * @return the currentUserPermissionCheckerService
     */
    public CurrentUserPermissionCheckerService getCurrentUserPermissionCheckerService() {
        return currentUserPermissionCheckerService;
    }

    /**
     * @param currentUserPermissionCheckerService the currentUserPermissionCheckerService to set
     */
    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
