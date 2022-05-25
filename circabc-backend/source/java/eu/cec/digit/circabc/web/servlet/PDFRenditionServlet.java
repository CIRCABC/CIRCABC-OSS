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

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionHelper;
import eu.cec.digit.circabc.web.Services;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;

/**
 * Calculates the rendition or gets the PDF document URL to display during the preview
 *
 * @author schwerr
 */
public class PDFRenditionServlet extends BaseServlet {

    public static final String WEB_INF_CLASSES_SUFFIX = "WEB-INF/classes/";
    private static final Log logger = LogFactory.getLog(PDFRenditionServlet.class);
    private static final long serialVersionUID = -3169831645378335785L;
    private AuthenticationService authenticationService = null;
    private ContentService contentService = null;
    private TransactionService transactionService = null;
    private PermissionService permissionService = null;

    private CircabcRenditionHelper circabcRenditionHelper = null;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {

        super.init();

        authenticationService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getAuthenticationService();
        contentService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getContentService();
        transactionService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getTransactionService();
        permissionService = Services.getAlfrescoServiceRegistry(getServletContext()).
                getPermissionService();
        circabcRenditionHelper = (CircabcRenditionHelper)
                Services.getCircabcServiceRegistry(getServletContext()).
                        getService(CircabcServiceRegistry.CIRCABC_RENDITION_HELPER);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Authenticate the current user
//		try {
//			String ticket = authenticationService.getCurrentTicket();
//			authenticationService.validate(ticket);
//		} 
//		catch (AuthenticationException e) {
//			logger.error("Error authenticating user.", e);
//			resp.getWriter().write("");
//			return;
//		}

        AuthenticationStatus status = servletAuthenticate(req, resp);

        if (status == AuthenticationStatus.Guest || status == AuthenticationStatus.Failure) {

            status = AuthenticationHelper.authenticate(getServletContext(), req,
                    resp, false);

            if (status == AuthenticationStatus.Failure) {
                logger.error("Error authenticating user: Failure");
                resp.getWriter().write("");
                return;
            }
        }

        // Check if we need the URL or the actual content
        String response = req.getParameter("response");

        // Get the document id to retrieve translations for
        String documentId = req.getParameter("documentId");

        NodeRef currentNodeRef = new NodeRef(documentId);

        // Check permission to read the node
        if (permissionService.hasPermission(currentNodeRef,
                PermissionService.READ_CONTENT) == AccessStatus.DENIED) {
            logger.error("User '" + authenticationService.getCurrentUserName() +
                    "' has no access to the content of document: " + documentId);
            resp.getWriter().write("");
            return;
        }

        // Set response header to avoid caching in IE
        resp.setHeader("Cache-Control", "no-cache, no-store");
        resp.setHeader("Pragma", "no-cache");

        try {

            File tempFile = getDocumentTempFile(currentNodeRef, req);

            if (tempFile == null) {
                resp.getWriter().write("");
                return;
            }

            resp.setHeader("Content-Length", Long.toString(tempFile.length()));

            if ("content".equals(response)) {

                InputStream inputStream = new FileInputStream(tempFile);

                OutputStream outputStream = resp.getOutputStream();

                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();
                outputStream.close();
            } else {
                resp.getWriter().write(req.getContextPath() + "/" +
                        tempFile.getName());
            }
        } catch (MimeTypeException e) {
            logger.error("MimeType error.");
            resp.getWriter().write(e.getMessage());
            return;
        }
    }

    /**
     * Returns the temp file of the document to display. Since the viewer displays only PDFs, if the
     * current document is in PDF format, the plain URL must be taken. If the document is not PDF, a
     * PDF rendition is rendered and its temp file is returned.
     */
    private File getDocumentTempFile(NodeRef currentNodeRef,
                                     HttpServletRequest req) throws IOException, MimeTypeException {

        // Document
        URL url = this.getClass().getResource("/");
        String path = url.getPath();

        if (path != null && path.endsWith(WEB_INF_CLASSES_SUFFIX)) {
            path = path.substring(0, path.length() -
                    WEB_INF_CLASSES_SUFFIX.length());
        } else if (path == null) {
            return null;
        }

        ContentReader reader = getContentReader(currentNodeRef);

        if (reader == null) {
            logger.error("Reader is null when accessing " +
                    "the content node. Unexpected error.");
            return null;
        }

        boolean convertible = true;
        String extension = ".pdf";

        // URL or image
        if (reader.getMimetype().startsWith("image") ||
                reader.getMimetype().startsWith("text/html")) {
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType mimeType = allTypes.forName(reader.getMimetype());
            extension = mimeType.getExtension();
            convertible = false;
        }

        final File tempFile = File.createTempFile("InlineView", extension,
                new File(path));

        boolean success = fillTempFile(tempFile, currentNodeRef, convertible);

        if (success) {
            tempFile.deleteOnExit();
            return tempFile;
        } else {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Could not delete temp file name: " +
                            tempFile.getName() + " path: " + tempFile.getPath());
                }
            }
            return null;
        }
    }

    /**
     * Fills the temp file with the rendition or PDF node content.
     */
    protected boolean fillTempFile(final File tempFile,
                                   final NodeRef currentNodeRef, final boolean convertible) {

        final RetryingTransactionHelper txnHelper = transactionService
                .getRetryingTransactionHelper();

        final RetryingTransactionCallback<Boolean> callback =
                new RetryingTransactionCallback<Boolean>() {

                    public Boolean execute() throws Throwable {

                        RunAsWork<Boolean> work = new RunAsWork<Boolean>() {

                            @Override
                            public Boolean doWork() throws Exception {

                                NodeRef renditionNodeRef = null;

                                // URL or image
                                if (!convertible) {
                                    // URL or image
                                    renditionNodeRef = currentNodeRef;
                                } else {
                                    // Rendition pdf
                                    renditionNodeRef = circabcRenditionHelper.
                                            getRenditionNodeRef(currentNodeRef);
                                }

                                if (renditionNodeRef == null) {
                                    // Conversion error
                                    return false;
                                }

                                if (tempFile != null) {

                                    ContentReader reader = getContentReader(renditionNodeRef);

                                    if (reader == null) {
                                        logger.error("Reader is null when accessing " +
                                                "the rendition. Unexpected error.");
                                        return false;
                                    }

                                    reader.getContent(tempFile);
                                }

                                return true;
                            }
                        };

                        return AuthenticationUtil.runAs(work,
                                AuthenticationUtil.SYSTEM_USER_NAME);
                    }
                };

        return txnHelper.doInTransaction(callback, false, true);
    }

    private ContentReader getContentReader(NodeRef contentNodeRef) {

        ContentReader reader = contentService.getReader(
                contentNodeRef, ContentModel.PROP_CONTENT);

        // try with hidden content
        if (reader == null) {
            reader = contentService.getReader(
                    contentNodeRef, DocumentModel.PROP_CONTENT);
        }

        return reader;
    }
}
