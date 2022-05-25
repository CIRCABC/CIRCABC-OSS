/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.servlet;

import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.web.Beans;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;


public class BulkDownloadServlet extends BaseServlet {

    private static final long serialVersionUID = 4976532808002012214L;
    private static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
    private static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
    private static Log logger = LogFactory.getLog(BulkDownloadServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            final String queryString = req.getQueryString();
            if (logger.isDebugEnabled()) {
                logger.debug("Authenticating request to URL: " + req.getRequestURI() +
                        ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
            }
        }

        final AuthenticationStatus status = servletAuthenticate(req, res);
        if (status == AuthenticationStatus.Failure) {
            return;
        }

        final HttpSession session = req.getSession();
        final String bulkDownloadFile = (String) session
                .getAttribute(CircabcConstant.BULK_DOWNLOAD_FILE);
        if (bulkDownloadFile != null) {
            processDownloadRequest(req, res, true, bulkDownloadFile);

            //refresh the clipboard
            final Node currentNode = Beans.getWaiNavigator().getCurrentNode();
            Beans.getWaiBrowseBean().clickWai(currentNode.getNodeRef());
            Beans.getWaiBrowseBean().applyWaiBrowsing(currentNode.getId(), true);
            final FacesContext context = FacesContext.getCurrentInstance();
            context.renderResponse();
            session.removeAttribute(CircabcConstant.BULK_DOWNLOAD_FILE);
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("Piracy warning non authorized access to BulkDownloadServlet.");
            }
            return;
        }
    }

    /**
     * Processes the download request using the current context i.e. no authentication checks are
     * made, it is presumed they have already been done.
     *
     * @param req             The HTTP request
     * @param res             The HTTP response
     * @param redirectToLogin Flag to determine whether to redirect to the login page if the user does
     *                        not have the correct permissions
     */
    protected void processDownloadRequest(final HttpServletRequest req, final HttpServletResponse res,
                                          final boolean redirectToLogin, String zipFilePath) {

        if (!zipFilePath.startsWith("/")) {
            zipFilePath = "/" + zipFilePath;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Temporary compressed File: " + zipFilePath);
        }
        if (zipFilePath.contains("..") || !zipFilePath.contains("bulk") || !zipFilePath
                .endsWith(".zip")) {
            if (logger.isErrorEnabled()) {
                logger.error("Piracy warning : zipFilePath  " + zipFilePath + " is invalid.");
            }
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        final File zipFile = new File(zipFilePath);
        final ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());

        try {
            final String protocol = req.getProtocol();
            if (protocol.endsWith("/1.0")) {
                res.setHeader("Pragma", "no-cache"); //HTTP 1.0
            } else {
                res.setHeader("Cache-Control", "no-cache"); //HTTP 1.1
            }

            res.setDateHeader("Expires", 0); //prevents caching at the proxy server

            // set header based on filename - will force a Save As from the browse if it doesn't recognise it
            // this is better than the default response of the browser trying to display the contents
            res.setHeader("Content-Disposition", "attachment; filename=download.zip");

            res.setContentType("application/x-download");

            // ensure that it is safe to use
            final FileContentReader fileContentReader = new FileContentReader(zipFile);

            final ContentReader reader = FileContentReader.getSafeContentReader(
                    fileContentReader.getReader(),
                    Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                    zipFile, fileContentReader.getReader());

            String mimetype = reader.getMimetype();
            // fall back if unable to resolve mimetype property
            if (mimetype == null || mimetype.length() == 0) {
                final MimetypeService mimetypeMap = serviceRegistry.getMimetypeService();
                mimetype = MIMETYPE_OCTET_STREAM;
                int extIndex = zipFilePath.lastIndexOf('.');
                if (extIndex != -1) {
                    final String ext = zipFilePath.substring(extIndex + 1);
                    final String mt = mimetypeMap.getMimetypesByExtension().get(ext);
                    if (mt != null) {
                        mimetype = mt;
                    }
                }
            }
            // set mimetype for the content and the character encoding for the stream
            res.setContentType(mimetype);
            res.setCharacterEncoding(reader.getEncoding());

            // disable content streaming

            res.setHeader("Accept-Ranges", "none");
            try {
                // As per the spec:
                //  If the server ignores a byte-range-spec because it is syntactically
                //  invalid, the server SHOULD treat the request as if the invalid Range
                //  header field did not exist.
                final long size = reader.getSize();
                res.setHeader("Content-Length", Long.toString(size));
                reader.getContent(res.getOutputStream());
            } catch (final SocketException | ContentIOException e1) {
                // the client cut the connection - our mission was accomplished apart from a little error message
                if (logger.isInfoEnabled()) {
                    logger.info(
                            "Client aborted stream read:\n\tnode: " + zipFilePath + "\n\tcontent: " + reader);
                }
            }
        } catch (final Throwable err) {
            throw new AlfrescoRuntimeException(
                    "Error during download content servlet processing: " + err.getMessage(), err);
        }
    }
}
