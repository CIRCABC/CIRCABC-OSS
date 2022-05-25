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
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class CircabcDownloadContentServlet extends IEDownloadContentServlet {


    protected static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";

    protected static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";

    private static final String DOWNLOAD_FILE = "Download file: ";

    private static final String DOWNLOAD_CONTENT = "Download content";

    private static final String LIBRARY = "Library";
    private static final String INFORMATION = "Information";
    private static final String NEWSGROUP = "Newsgroup";

    /**
     *
     */
    private static final long serialVersionUID = 8286999650143988187L;

    private static Log logger = LogFactory.getLog(DownloadContentServlet.class);

    public static CircabcServiceRegistry getCircabcServiceRegistry(final ServletContext sc) {
        final WebApplicationContext wc = WebApplicationContextUtils
                .getRequiredWebApplicationContext(sc);
        return (CircabcServiceRegistry) wc.getBean(CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
    }

    @Override
    protected Log getLogger() {
        return logger;
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        // get range so we do not log range request
        String range = req.getHeader("Range");
        if (range == null) {
            final Log logger = getLogger();
            final LogRecord logRecord = logDownloadRequest(req, res);
            try {
                logRecord.setOK(true);
                super.doGet(req, res);
                if (super.isAuthenticationError()) {
                    logRecord.setOK(false);
                    logRecord.addInfo("Authentication error.");
                    authenticationError.remove();
                }
                if (super.isPermissionDenied()) {
                    logRecord.setOK(false);
                    logRecord.addInfo("User does not have permissions to read content.");
                    permissionDenied.remove();
                }

            } catch (final Exception e) {
                logRecord.setOK(false);
                logger.error("Error during file download ", e);

            } finally {
                final CircabcServiceRegistry circabcServiceRegistry = getCircabcServiceRegistry(
                        getServletContext());
                final LogService logService = circabcServiceRegistry.getLogService();
                logService.log(logRecord);

            }

        } else {
            super.doGet(req, res);
        }

    }

    private LogRecord logDownloadRequest(final HttpServletRequest req,
                                         final HttpServletResponse res) {
        final String uri = req.getRequestURI().substring(req.getContextPath().length());
        final StringTokenizer t = new StringTokenizer(uri, "/");
        final int tokenCount = t.countTokens();

        t.nextToken(); // skip servlet name

        // attachment mode (either 'attach' or 'direct')
        /* final String attachToken = */
        t.nextToken();
        //boolean attachment = true; // URL_ATTACH.equals(attachToken) || URL_ATTACH_LONG.equals(attachToken);

        final ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());

        // get or calculate the noderef and filename to download as
        NodeRef nodeRef;
        String filename;

        // do we have a path parameter instead of a NodeRef?
        final String path = req.getParameter(ARG_PATH);
        if (path != null && path.length() != 0) {
            // process the name based path to resolve the NodeRef and the Filename element
            final PathRefInfo pathInfo = resolveNamePath(getServletContext(), path);

            nodeRef = pathInfo.NodeRef;
            filename = pathInfo.Filename;
        } else {
            // a NodeRef must have been specified if no path has been found
            if (tokenCount < 6) {
                throw new IllegalArgumentException(
                        "Download URL did not contain all required args: " + uri);
            }

            // assume 'workspace' or other NodeRef based protocol for remaining URL elements
            final StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
            final String id = URLDecoder.decode(t.nextToken());

            // build noderef from the appropriate URL elements
            nodeRef = new NodeRef(storeRef, id);

            if (tokenCount > 6) {
                // found additional relative path elements i.e. noderefid/images/file.txt
                // this allows a url to reference siblings nodes via a cm:name based relative path
                // solves the issue with opening HTML content containing relative URLs in HREF or IMG tags etc.
                final List<String> paths = new ArrayList<>(tokenCount - 5);
                while (t.hasMoreTokens()) {
                    paths.add(URLDecoder.decode(t.nextToken()));
                }
                filename = paths.get(paths.size() - 1);

                try {
                    final NodeRef parentRef = serviceRegistry.getNodeService().getPrimaryParent(nodeRef)
                            .getParentRef();
                    final FileInfo fileInfo = serviceRegistry.getFileFolderService()
                            .resolveNamePath(parentRef, paths);
                    nodeRef = fileInfo.getNodeRef();
                } catch (final FileNotFoundException e) {
                    throw new AlfrescoRuntimeException(
                            "Unable to find node reference by relative path:" + uri);
                }
            } else {
                // filename is last remaining token
                filename = t.nextToken();
            }
        }

        // get qualified of the property to get content from - default to ContentModel.PROP_CONTENT
        //QName propertyQName = ContentModel.PROP_CONTENT;
        //String property = req.getParameter(ARG_PROPERTY);
        //if (property != null && property.length() != 0)
        //{
        //propertyQName = QName.createQName(property);
        //}

        return setLogRecord(req, res, serviceRegistry, nodeRef, filename);
    }

    private LogRecord setLogRecord(final HttpServletRequest req, final HttpServletResponse res,
                                   final ServiceRegistry serviceRegistry, final NodeRef nodeRef, final String filename) {
        final LogRecord logRecord = new LogRecord();
        try {
            // get the services we need to retrieve the content
            final CircabcServiceRegistry circabcServiceRegistry = getCircabcServiceRegistry(
                    getServletContext());
            final NodeService nodeService = serviceRegistry.getNodeService();
            final ManagementService managementService = circabcServiceRegistry.getManagementService();
            final NodeRef currentInterestGroup = managementService.getCurrentInterestGroup(nodeRef);
            AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {
                public Object doWork() {
                    if (currentInterestGroup != null) {
                        final String service;
                        final Set<QName> aspects = nodeService.getAspects(nodeRef);
                        if (aspects.contains(CircabcModel.ASPECT_LIBRARY)) {
                            service = LIBRARY;
                        } else if (aspects.contains(CircabcModel.ASPECT_INFORMATION)) {
                            service = INFORMATION;
                        } else if (aspects.contains(CircabcModel.ASPECT_NEWSGROUP)) {
                            // for attachements
                            service = NEWSGROUP;
                        } else {
                            service = null;
                        }

                        if (service != null) {
                            final Long igID = (Long) nodeService
                                    .getProperty(currentInterestGroup, ContentModel.PROP_NODE_DBID);
                            final Path nodePath = nodeService.getPath(nodeRef);
                            final String circabcPath = PathUtils.getCircabcPath(nodePath, true);
                            // Migration 3.1 -> 3.4.6 - 05/12/2011
                            // getUser added the ServletContext to its parameters.
                            final User user = AuthenticationHelper.getUser(getServletContext(), req, res);
                            PermissionService permissionService = serviceRegistry.getPermissionService();
                            Long documentID = -1L;
                            if (user != null && !AuthenticationUtil.getGuestUserName().equals(user.getUserName())
                                    &&
                                    permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT)
                                            == AccessStatus.ALLOWED) {
                                // Workaround, because guest may not have permission
                                documentID = (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
                            }

                            logRecord.setService(service);
                            logRecord.setActivity(DOWNLOAD_CONTENT);
                            logRecord.setInfo(DOWNLOAD_FILE + filename);
                            logRecord.setIgID(igID);
                            logRecord.setDocumentID(documentID);

                            if (user != null) {
                                logRecord.setUser(user.getUserName());
                            } else {
                                logRecord.setUser(CircabcConstant.GUEST_AUTHORITY);
                            }
                            logRecord.setPath(circabcPath);
                        }

                    }
                    return null;
                }
                // Migration 3.1 -> 3.4.6 - 12/12/2011 - Changed AuthenticationUtil.getAdminUserName() in Alfresco 3.4
            }, AuthenticationUtil.getSystemUserName());

            // else this node is downloaded outside of an interest group. Don't log it
        } catch (final Exception e) {
            getLogger().error("Error during logging file download ", e);
        }
        return logRecord;
    }


}
