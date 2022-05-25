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

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.CommonUtils;
import eu.cec.digit.circabc.util.PathUtils;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class SimpleDownloadServlet extends BaseServlet {

    private static final String SIMPLE_BROWSER_URL = "/sd/a/{0}/{1}";
    private static final String SIMPLE_DOWNLOAD_URL = "/sd/d/{0}/{1}";

    /**
     *
     */
    private static final long serialVersionUID = -3736113035795055591L;
    private static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
    private static final String DOWNLOAD_FILE = "Alternative download file: ";
    private static final String DOWNLOAD_CONTENT = "Alternative download content";
    private static final String BROWSE_FILE = "Alternative browse file: ";
    private static final String BROWSE_CONTENT = "Alternative browse content";
    private static final String LIBRARY = "Library";
    private static final String INFORMATION = "Information";
    private static final String NEWSGROUP = "Newsgroup";
    private static Log logger = LogFactory.getLog(SimpleDownloadServlet.class);

    public static String generateDownloadURL(NodeRef ref, String name) {
        return generateUrl(SIMPLE_DOWNLOAD_URL, ref, name);
    }

    public static String generateBrowseURL(NodeRef ref, String name) {
        return generateUrl(SIMPLE_BROWSER_URL, ref, name);
    }

    protected static String generateUrl(String pattern, NodeRef ref,
                                        String name) {
        return MessageFormat.format(pattern, ref.getId(),
                URLEncoder.encode(name));
    }

    private static CircabcServiceRegistry getCircabcServiceRegistry(
            final ServletContext sc) {
        final WebApplicationContext wc = WebApplicationContextUtils
                .getRequiredWebApplicationContext(sc);
        return (CircabcServiceRegistry) wc
                .getBean(CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) {
        Principal userPrincipal = null;
        try {
            userPrincipal = req.getUserPrincipal();
            String queryString = req.getQueryString();
            if (AuthenticationStatus.Failure == servletAuthenticate(req, res)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Fail to authenticate : ");
                    if (userPrincipal != null) {
                        logger.warn("User principal name " + userPrincipal.getName());
                    } else {
                        logger.warn("User principal is null ");
                    }

                    logger.warn("Query string  " + queryString);
                }
                if (!res.isCommitted()) {
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
                return;
            }

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during authentification ", e);
            }
        }
        try {
            processDownloadRequest(req, res, userPrincipal);
        } catch (ServletException | IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during procesing download request", e);
            }
        }

    }

    protected void processDownloadRequest(HttpServletRequest req,
                                          HttpServletResponse res, Principal userPrincipal)
            throws ServletException, IOException {

        String uri = req.getRequestURI();

        uri = uri.substring(req.getContextPath().length());
        StringTokenizer t = new StringTokenizer(uri, "/");
        int tokenCount = t.countTokens();
        if (tokenCount != 4) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        t.nextToken();
        String attachToken = t.nextToken();
        boolean attachment = attachToken.equalsIgnoreCase("d");

        ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
        String id = t.nextToken();
        if (id == null || id.length() == 0) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;

        }

        String filename = "";

        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);

        NodeService nodeService = serviceRegistry.getNodeService();
        ContentService contentService = serviceRegistry.getContentService();
        PermissionService permissionService = serviceRegistry.getPermissionService();
        // check that the user has at least READ_CONTENT access - else redirect
        // to the login page to ask for ECAS authentication
        if (userPrincipal == null && permissionService.hasPermission(nodeRef,
                PermissionService.READ_CONTENT) == AccessStatus.DENIED) {

            HttpSession session = req.getSession();

            session.setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL,
                    req.getRequestURI());

            redirectToLoginPage(req, res, getServletContext());

            return;
        }
        // If the user is authenticated in ECAS but it has no rights,
        // send forbidden
        else if (permissionService.hasPermission(nodeRef,
                PermissionService.READ_CONTENT) == AccessStatus.DENIED) {

            final FacesContext fc = FacesHelper.getFacesContext(req, res, getServletContext());

            Utils.addErrorMessage(Application.getMessage(fc,
                    ExternalAccessServlet.MSG_LOGIN_BUT_NOT_ALLOWED));

            String redirectPage = req.getContextPath() + BaseServlet.FACES_SERVLET;

            redirectPage += ExternalAccessServlet.WAI_WELCOME_PAGE;

            // mem the messages to set it in the new created context by the redirection.
            final Iterator<FacesMessage> messages = fc.getMessages();

            if (messages.hasNext()) {
                ErrorsRenderer.addForcedMessage(messages);
            }

            res.sendRedirect(redirectPage);

            return;
        }

        final boolean exists = nodeService.exists(nodeRef);
        if (!exists) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME) != null) {
            filename = serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME)
                    .toString();
        } else {
            // filename is last remaining token
            filename = t.nextToken();
        }

        final QName type = nodeService.getType(nodeRef);

        if (!type.equals(ContentModel.TYPE_CONTENT) &&
                !type.equals(DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT) &&
                !type.equals(ApplicationModel.TYPE_FILELINK)) {
            res.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        // If it's a file link, extract its destination
        if (type.equals(ApplicationModel.TYPE_FILELINK)) {

            nodeRef = (NodeRef) nodeService.getProperty(nodeRef,
                    CircabcModel.PROP_DESTINATION);

            // Check if nodeRef is not null
            if (nodeRef == null) {
                res.sendError(HttpServletResponse.SC_NOT_FOUND, "The link " +
                        "destination is invalid.");
                return;
            }

            // Remove the .url extension because we want the name of the
            // linked document instead of the URL name
            if (filename != null && filename.toLowerCase().endsWith(".url")) {
                filename = filename.substring(0, filename.length() - 4);
            }
        }

        ContentData contentData = null;

        if (type.equals(DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT)) {
            contentData = (ContentData) nodeService.getProperty(
                    nodeRef, DocumentModel.PROP_CONTENT);
        } else {
            contentData = (ContentData) nodeService.getProperty(
                    nodeRef, ContentModel.PROP_CONTENT);
        }

        // Check if the contet data is not null
        if (contentData == null) {
            res.sendError(HttpServletResponse.SC_NO_CONTENT, "The link " +
                    "content is invalid.");
            return;
        }

        // get the content reader
        ContentReader reader = null;

        if (type.equals(DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT)) {
            reader = contentService.getReader(nodeRef, DocumentModel.PROP_CONTENT);
        } else {
            reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        }

        String mimetype = contentData.getMimetype();
        // fall back if unable to resolve mimetype property
        if (mimetype == null || mimetype.length() == 0) {
            mimetype = MIMETYPE_OCTET_STREAM;
        }

        // String filename = (String) nodeService.getProperty(nodeRef,
        // ContentModel.PROP_NAME);

        res.reset();
        res.setContentType(mimetype);

        String headerContentDispo = CommonUtils.
                generateFilenameContentDispositionHeader(attachment, filename, req);
        res.setHeader("Content-Disposition", headerContentDispo);

        res.setHeader("Accept-ranges", "none");
        res.setCharacterEncoding(reader.getEncoding());

        // return the complete entity range
        long size = reader.getSize();
        res.setContentLength((int) size);
        reader.getContent(res.getOutputStream());
        res.flushBuffer();
        setLogRecord(req, res, nodeRef, filename, size, attachment);
    }

    private void setLogRecord(final HttpServletRequest req,
                              final HttpServletResponse res, final NodeRef nodeRef,
                              final String filename, final long size, final boolean attachment) {
        final LogRecord logRecord = new LogRecord();
        try {
            // get the services we need to retrieve the content
            final CircabcServiceRegistry circabcServiceRegistry = getCircabcServiceRegistry(
                    getServletContext());
            final ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
            final NodeService nodeService = serviceRegistry.getNodeService();
            final ManagementService managementService = circabcServiceRegistry
                    .getManagementService();
            final NodeRef currentInterestGroup = managementService
                    .getCurrentInterestGroup(nodeRef);
            final LogService logService = circabcServiceRegistry.getLogService();
            AuthenticationUtil.runAs(
                    new AuthenticationUtil.RunAsWork<Object>() {
                        public Object doWork() {
                            if (currentInterestGroup != null) {
                                final String service;
                                final Set<QName> aspects = nodeService
                                        .getAspects(nodeRef);
                                if (aspects
                                        .contains(CircabcModel.ASPECT_LIBRARY)) {
                                    service = LIBRARY;
                                } else if (aspects
                                        .contains(CircabcModel.ASPECT_INFORMATION)) {
                                    service = INFORMATION;
                                } else if (aspects
                                        .contains(CircabcModel.ASPECT_NEWSGROUP)) {
                                    // for attachements
                                    service = NEWSGROUP;
                                } else {
                                    service = null;
                                }

                                if (service != null) {
                                    final Long igID = (Long) nodeService
                                            .getProperty(currentInterestGroup,
                                                    ContentModel.PROP_NODE_DBID);
                                    final Path nodePath = nodeService
                                            .getPath(nodeRef);
                                    final String circabcPath = PathUtils
                                            .getCircabcPath(nodePath, true);
                                    final User user = AuthenticationHelper
                                            .getUser(getServletContext(), req, res);
                                    final Long documentID = (Long) nodeService
                                            .getProperty(nodeRef,
                                                    ContentModel.PROP_NODE_DBID);

                                    logRecord.setService(service);
                                    if (attachment) {
                                        logRecord.setActivity(DOWNLOAD_CONTENT);
                                        logRecord.setInfo(DOWNLOAD_FILE + filename
                                                + ", size " + String.valueOf(size)
                                                + " bytes.");
                                    } else {
                                        logRecord.setActivity(BROWSE_CONTENT);
                                        logRecord.setInfo(BROWSE_FILE + filename
                                                + ", size " + String.valueOf(size)
                                                + " bytes.");
                                    }
                                    logRecord.setIgID(igID);
                                    logRecord.setDocumentID(documentID);

                                    if (user != null) {
                                        logRecord.setUser(user.getUserName());
                                    } else {
                                        logRecord
                                                .setUser(CircabcConstant.GUEST_AUTHORITY);
                                    }
                                    logRecord.setPath(circabcPath);
                                    logRecord.setOK(true);
                                    logService.log(logRecord);
                                }

                            }
                            return null;
                        }
                    }, AuthenticationUtil.getAdminUserName());

            // else this node is downloaded outside of an interest group. Don't
            // log it
        } catch (final Exception e) {
            logger.error("Error during logging file download ", e);
        }

    }

}
