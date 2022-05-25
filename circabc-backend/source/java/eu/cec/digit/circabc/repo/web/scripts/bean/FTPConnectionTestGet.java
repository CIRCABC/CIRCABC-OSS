package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.service.ftp.SimpleFtpClient;
import eu.cec.digit.circabc.service.ftp.SimpleFtpClientImpl;
import io.swagger.util.CurrentUserPermissionCheckerService;
import it.sauronsoftware.ftp4j.*;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FTPConnectionTestGet extends DeclarativeWebScript {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(FTPConnectionTestGet.class);

    private CurrentUserPermissionCheckerService currentUserPermissionCheckerService;

    public static int testConnection(
            String host, int port, String username, String password, String path) {

        SimpleFtpClient ftpTest = new SimpleFtpClientImpl();

        int testResult = -2;

        try {

            ftpTest.initParameters(host, port, username, password, path);
            testResult = 1;

            if (ftpTest.getFileName() != null
                    && !ftpTest.getFileName().isEmpty()
                    && !ftpTest.fileExists(ftpTest.getFileName())) {
                testResult = 0;
            }

        } catch (NumberFormatException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, port number is wrong", e);
            }
        } catch (IllegalStateException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, illegal state", e);
            }
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, i/o exception", e);
            }
        } catch (FTPIllegalReplyException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, illegal reply from FTP server", e);
            }
        } catch (FTPException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection, ftp issue -> wrong path ?", e);
            }
            testResult = -1;
        } catch (FTPDataTransferException | FTPListParseException | FTPAbortedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Problem during testing FTP connection", e);
            }
        }

        return testResult;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        Map<String, Object> model = new HashMap<>(7, 1.0f);

        boolean mlAware = MLPropertyInterceptor.isMLAware();

        MLPropertyInterceptor.setMLAware(false);

        try {

            String host = req.getParameter("host");
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String path = req.getParameter("filePath");

            String portString = req.getParameter("port");
            int port = 0;
            port = getPort(portString, port);

            int connStatus = testConnection(host, port, username, password, path);

            model.put("result", connStatus);
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

    private int getPort(String portString, int port) {
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Invalid port number: " + portString);
            }
        }
        return port;
    }

    public void setCurrentUserPermissionCheckerService(
            CurrentUserPermissionCheckerService currentUserPermissionCheckerService) {
        this.currentUserPermissionCheckerService = currentUserPermissionCheckerService;
    }
}
