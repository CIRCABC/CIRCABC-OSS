/*******************************************************************************
 * Copyright 2006 European Community
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.servlet;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.rendition.CircabcRenditionService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.util.PathUtils;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.bean.FileUploadBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author Slobodan Filipovic Change BaseDownloadContentServlet to force response headers as
 * parameters in request so we can debug IE problems
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 URLEncoder and URLDecoder was moved to Spring.
 */
public class RestContentUploadServlet extends BaseServlet {

    private static final String LIBRARY = "Library";
    private static final String INFORMATION = "Information";
    private static final String NEWSGROUP = "Newsgroup";
    private static final String UPLOAD_FILE = "Upload file: ";
    private static final String UPLOAD_CONTENT = "Upload document";
    /**
     *
     */
    private static final long serialVersionUID = -208568714802217306L;
    private static Log logger = LogFactory.getLog(RestContentUploadServlet.class);
    private CircabcRenditionService circabcRenditionService = null;

    public static CircabcServiceRegistry getCircabcServiceRegistry(final ServletContext sc) {
        final WebApplicationContext wc =
                WebApplicationContextUtils.getRequiredWebApplicationContext(sc);
        return (CircabcServiceRegistry) wc.getBean(CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Prepare the nodeRef
        String uri = req.getRequestURI();
        String[] uriParts = uri.split("/");
        if ((uriParts.length != 4 && uriParts.length != 5)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request URL malformed");
            throw new ServletException("Request URL malformed");
        }

        String uid = uriParts[uriParts.length - 1];
        if ("".equals(uid) || uid == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing  arg UID");
            throw new ServletException("Missing  arg UID");
        }

        servletAuthenticate(req, resp, false);

        final AuthenticationService authenticationService =
                getServiceRegistry(getServletContext()).getAuthenticationService();

        String authHdr = req.getHeader("Authorization");

        if (authHdr != null && authHdr.length() > 5
                && authHdr.substring(0, 5).equalsIgnoreCase("BASIC")) {
            if (logger.isDebugEnabled()) {
                logger.debug("Basic authentication details present in the header.");
            }
            byte[] decodedBytes = Base64.decodeBase64(authHdr.substring(5).getBytes());
            String basicAuth = new String(decodedBytes);
            int pos = basicAuth.indexOf(":");
            if (pos == -1) {
                String ticket = basicAuth;
                authenticationService.validate(ticket);
            }
        }

        NodeRef parentRef = Converter.createNodeRefFromId(uid);

        final PermissionService permissionService =
                getServiceRegistry(getServletContext()).getPermissionService();

        final NodeService nodeService = getServiceRegistry(getServletContext()).getNodeService();

        circabcRenditionService =
                getCircabcServiceRegistry(getServletContext()).getCircabcRenditionService();

        if (!nodeService.exists(parentRef)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Content unavailable");
            return;
        }

        if (permissionService.hasPermission(parentRef, PermissionService.CREATE_CHILDREN)
                .equals(AccessStatus.DENIED)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied - cannot create file");
            return;
        }

        if (!(nodeService.getType(parentRef).equals(ContentModel.TYPE_FOLDER)
                || nodeService.getType(parentRef).equals(CircabcModel.TYPE_INFORMATION_NEWS))) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Destination is not folder type");
            return;
        }

        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

        // ensure that the encoding is handled correctly
        upload.setHeaderEncoding("UTF-8");

        List<FileItem> fileItems;
        List<Map<String, String>> uploadedFiles = new ArrayList<>();

        try {
            fileItems = upload.parseRequest(req);

            FileUploadBean bean = new FileUploadBean();
            for (FileItem item : fileItems) {
                if (!item.isFormField()) {
                    String filename = item.getName();
                    if (filename != null && filename.length() != 0) {
                        Map<String, String> res = handleUpload(bean, item, parentRef);
                        uploadedFiles.add(res);
                        NodeRef fileRef = Converter.createNodeRefFromId(res.get("id"));
                        if (fileRef != null) {
                            circabcRenditionService.addRequest(fileRef);
                        }

                    }
                }
            }
        } catch (FileUploadException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during content upload", e);
            }
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during content upload", e);
            }
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Content-Type", "application/json");
        writeJsonRespone(resp, uploadedFiles);

    }

    private void writeJsonRespone(HttpServletResponse resp, List<Map<String, String>> uploadedFiles)
            throws IOException {

        Integer i = 0;
        for (Map<String, String> file : uploadedFiles) {
            resp.getWriter().append("{");
            resp.getWriter().append("\"nodeRef\": \"workspace://SpacesStore/" + file.get("id") + "\"");
            resp.getWriter().append(",");
            resp.getWriter().append("\"fileName\": \"" + file.get("fileName") + "\"");
            resp.getWriter().append(",");
            resp.getWriter().append("\"status\":");
            resp.getWriter().append("{");
            resp.getWriter().append("\"code\": 200");
            resp.getWriter().append(",");
            resp.getWriter().append("\"name\": \"ok\"");
            resp.getWriter().append("}");
            resp.getWriter().append("}");
            i += 1;
            if (i < uploadedFiles.size()) {
                resp.getWriter().append(",");
            }
        }

    }

    /**
     * @param bean
     * @param item
     * @return
     * @throws Exception
     */
    private Map<String, String> handleUpload(FileUploadBean bean, final FileItem item,
                                             final NodeRef parentRef) throws Exception {
        final String filename = FilenameUtils.getName(item.getName());
        ;
        final String originalName = filename;

        final LogService logService = getCircabcServiceRegistry(getServletContext()).getLogService();

        if (logger.isDebugEnabled()) {
            logger.debug("Processing uploaded file: " + filename);
        }

        // ADB-41: Ignore non-existent files i.e. 0 byte
        // streams.
        final TransactionService transactionService =
                getServiceRegistry(getServletContext()).getTransactionService();
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        final RetryingTransactionCallback<Map<String, String>> callback =
                new RetryingTransactionCallback<Map<String, String>>() {

                    public Map<String, String> execute() throws Throwable {

                        RunAsWork<Map<String, String>> work = new RunAsWork<Map<String, String>>() {

                            @Override
                            public Map<String, String> doWork() throws Exception {

                                Map<String, String> result = new HashMap<>();

                                // ADB-41: Ignore non-existent files i.e. 0 byte
                                // streams.
                                if (item.getSize() > 0) {

                                    final NodeService nodeService =
                                            getServiceRegistry(getServletContext()).getNodeService();
                                    final BehaviourFilter policyBehaviourFilter =
                                            getCircabcServiceRegistry(getServletContext()).getBehaviourFilter();

                                    boolean wasEnabled = !policyBehaviourFilter.isEnabled();

                                    try {
                                        String newfilename = generateUniqueFilename(parentRef, filename);

                                        QName associationNameQName =
                                                QName.createQName(ContentModel.PROP_NAME.getNamespaceURI(), newfilename);

                                        Map<QName, Serializable> props = new HashMap<>();
                                        props.put(ContentModel.PROP_NAME, newfilename);

                                        if (nodeService.getType(parentRef).equals(CircabcModel.TYPE_INFORMATION_NEWS)) {
                                            policyBehaviourFilter
                                                    .disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
                                        }

                                        NodeRef nodeRef =
                                                nodeService
                                                        .createNode(parentRef, ContentModel.ASSOC_CONTAINS,
                                                                associationNameQName, ContentModel.TYPE_CONTENT, props)
                                                        .getChildRef();

                                        result.put("id", nodeRef.getId());
                                        result.put("fileName", newfilename);

                                        final ContentService contentService =
                                                getServiceRegistry(getServletContext()).getContentService();
                                        final ContentWriter writer =
                                                contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

                                        writer.setMimetype(guessMimetype(filename));
                                        writer.putContent(item.getInputStream());

                                        final LogRecord logRecord = logUploadRequest(nodeRef, originalName);

                                        logRecord.setOK(true);

                                        logService.log(logRecord);
                                    } finally {
                                        if (wasEnabled && nodeService.getType(parentRef)
                                                .equals(CircabcModel.TYPE_INFORMATION_NEWS)) {
                                            policyBehaviourFilter
                                                    .enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
                                        }
                                    }


                                } else {
                                    if (logger.isWarnEnabled()) {
                                        logger.warn("Ignored file '" + filename
                                                + "' as there was no content, this is either "
                                                + "caused by uploading an empty file or a file path that does not exist on the client.");
                                    }
                                }

                                return result;
                            }
                        };

                        return AuthenticationUtil.runAs(work, AuthenticationUtil.SYSTEM_USER_NAME);
                    }
                };

        return txnHelper.doInTransaction(callback, false, true);
    }

    private String generateUniqueFilename(NodeRef parentRef, String filename) {
        final NodeService nodeService = getServiceRegistry(getServletContext()).getNodeService();

        Integer counter = 1;
        while (nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, filename) != null) {
            String cleanName = filename.trim();

            if (cleanName.contains(".")) {
                String leftSideName = cleanName.substring(0, cleanName.lastIndexOf("."));
                String rightSideName = cleanName.substring(cleanName.lastIndexOf("."));

                if (cleanName.matches(".*\\([0-9]*\\)\\..*")) {
                    leftSideName = cleanName.substring(0, cleanName.lastIndexOf("("));
                    filename = MessageFormat.format("{0}({1}){2}", leftSideName, counter, rightSideName);
                } else {
                    filename = MessageFormat.format("{0}({1}){2}", leftSideName, counter, rightSideName);
                }

            } else {
                filename = MessageFormat.format("{0}({1})", cleanName, counter);
            }

            counter += 1;
        }

        return filename;
    }

    public String guessMimetype(final String fileName) {
        Assert.notNull(fileName);

        // fall back to binary mimetype if no match found
        String mimetype = MimetypeMap.MIMETYPE_BINARY;
        final int extIndex = fileName.lastIndexOf('.');
        if (extIndex != -1) {
            String ext = fileName.substring(extIndex + 1).toLowerCase();
            final MimetypeService mimetypeService =
                    getServiceRegistry(getServletContext()).getMimetypeService();
            String mt = mimetypeService.getMimetypesByExtension().get(ext);
            if (mt != null) {
                mimetype = mt;
            }
        }

        return mimetype;
    }

    private LogRecord logUploadRequest(NodeRef nodeRef, String originalFileName) {

        final LogRecord logRecord = new LogRecord();

        try {
            // get the services we need to retrieve the content
            final CircabcServiceRegistry circabcServiceRegistry =
                    getCircabcServiceRegistry(getServletContext());
            final NodeService nodeService = getServiceRegistry(getServletContext()).getNodeService();
            final ManagementService managementService = circabcServiceRegistry.getManagementService();
            final NodeRef currentInterestGroup = managementService.getCurrentInterestGroup(nodeRef);
            final AuthenticationService authenticationService =
                    getServiceRegistry(getServletContext()).getAuthenticationService();

            if (currentInterestGroup != null) {
                final String service;
                final Set<QName> aspects =
                        nodeService.getAspects(nodeService.getPrimaryParent(nodeRef).getParentRef());
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
                    final Long igID =
                            (Long) nodeService.getProperty(currentInterestGroup, ContentModel.PROP_NODE_DBID);
                    final Path nodePath = nodeService.getPath(nodeRef);
                    final String circabcPath = PathUtils.getCircabcPath(nodePath, true);

                    Long documentID = (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
                    String user = authenticationService.getCurrentUserName();

                    logRecord.setService(service);
                    logRecord.setActivity(UPLOAD_CONTENT);
                    // add info in case the file has been rename, because
                    // another child is already existing
                    logRecord.setInfo(UPLOAD_FILE + " with original name" + originalFileName);
                    logRecord.setIgID(igID);
                    logRecord.setDocumentID(documentID);

                    if (user != null) {
                        logRecord.setUser(user);
                    } else {
                        logRecord.setUser(CircabcConstant.GUEST_AUTHORITY);
                    }
                    logRecord.setPath(circabcPath);
                }

            }

        } catch (final Exception e) {
            logger.error("Error during logging file download ", e);
        }

        return logRecord;
    }

}
