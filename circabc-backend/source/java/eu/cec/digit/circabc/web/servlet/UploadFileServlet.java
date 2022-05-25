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

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.ErrorBean;
import org.alfresco.web.bean.FileUploadBean;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.config.ConfigService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servlet that takes a file uploaded via a browser and represents it as an UploadFileBean in the
 * session
 *
 * @author gavinc
 */
public class UploadFileServlet extends BaseServlet {

    private static final long serialVersionUID = -5482538466491052876L;
    private static final Log logger = LogFactory.getLog(UploadFileServlet.class);
    private static String validJavaScript = "javascript:window.parent.upload_complete_helper('',{error: '${_UPLOAD_ERROR}', fileTypeImage: '${_FILE_TYPE_IMAGE}'})";
    private static Set<String> circabcValidPages = new HashSet<>();

    static {
        circabcValidPages.add("/jsp/extension/wai/dialog/actions/import.jsp");
        circabcValidPages.add("/jsp/extension/wai/dialog/admin/uploadCircaIndexFile.jsp");
        circabcValidPages.add("/jsp/extension/wai/dialog/coci/checkin-file.jsp");
        circabcValidPages.add("/jsp/extension/wai/dialog/coci/update-file.jsp");
        circabcValidPages.add("/jsp/extension/wai/dialog/content/bulk-upload.jsp");
        circabcValidPages.add("/jsp/extension/wai/dialog/ml/add-translation.jsp");
        circabcValidPages.add("/jsp/extension/wai/dialog/users/update-avatar.jsp");
    }

    private ConfigService configService;
    private ServletConfig servletConfig;
    private UploadFileServletConfig config;

    public static void resetUploadedFiles(final HttpSession session) {
        session.removeAttribute(CircabcUploadedFile.FILE_UPLOAD_BEAN_NAME);
    }

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init(final ServletConfig sc) throws ServletException {
        super.init(sc);
        this.servletConfig = sc;
        final WebApplicationContext ctx = WebApplicationContextUtils
                .getRequiredWebApplicationContext(sc.getServletContext());
        this.configService = (ConfigService) ctx.getBean("webClientConfigService");
        this.config = (UploadFileServletConfig) ctx.getBean("fileUploadLimit");

    }

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        String uploadId = null;
        String returnPage = null;
        String submitCallback = null;

        final RequestContext requestContext = new ServletRequestContext(request);
        boolean isMultipart = ServletFileUpload.isMultipartContent(requestContext);

        try {
            // check content size
            final int maxSizeInBytes = this.config.getMaxSizeInMegaBytes() * 1024 * 1024;
            if (request.getContentLength() > maxSizeInBytes) {
                throw new CircabcRuntimeException(
                        "File is too big to be uploded actual size of request is:  " +
                                String.valueOf(request.getContentLength()) +
                                " maximum allowed size is: " + String.valueOf(maxSizeInBytes) + " bytes.");
            }

            final AuthenticationStatus status = servletAuthenticate(request, response);
            if (status == AuthenticationStatus.Failure) {
                return;
            }

            if (!isMultipart) {
                throw new AlfrescoRuntimeException(
                        "This servlet can only be used to handle file upload requests, make" +
                                "sure you have set the enctype attribute on your form to multipart/form-data");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Uploading servlet servicing...");
            }

            final HttpSession session = request.getSession();
            final ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());

            // ensure that the encoding is handled correctly
            upload.setHeaderEncoding("UTF-8");

            final List<FileItem> fileItems = upload.parseRequest(request);

            final CircabcUploadedFile bean = new CircabcUploadedFile();
            for (final FileItem item : fileItems) {
                if (item.isFormField()) {
                    final String fieldName = item.getFieldName().toLowerCase();
                    if (fieldName.equals("return-page")) {
                        returnPage = item.getString();
                    } else if (fieldName.equals("upload-id")) {
                        uploadId = item.getString();
                    } else if (fieldName.equals("submit-callback")) {
                        submitCallback = item.getString();
                    } else if (fieldName.length() > 0 && fieldName.equals("submitfile") == false) {
                        // add other submited values in the session
                        bean.addSubmitedProperty(fieldName, item.getString());
                    }

                } else {
                    String filename = item.getName();
                    if (filename != null && filename.length() != 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Processing uploaded file: " + filename);
                        }
                        // ADB-41: Ignore non-existent files i.e. 0 byte streams.
                        if (allowZeroByteFiles() == true || item.getSize() > 0) {
                            // workaround a bug in IE where the full path is returned
                            // IE is only available for Windows so only check for the Windows path separator
                            filename = FilenameUtils.getName(filename);
                            final File tempFile = TempFileProvider.createTempFile("alfresco", ".upload");
                            item.write(tempFile);
                            bean.setFile(tempFile);
                            bean.setFileName(filename);
                            bean.setFilePath(tempFile.getAbsolutePath());
                            if (logger.isDebugEnabled()) {
                                logger.debug("Temp file: " + tempFile.getAbsolutePath() +
                                        " size " + tempFile.length() +
                                        " bytes created from upload filename: " + filename);
                            }
                        } else {
                            if (logger.isWarnEnabled()) {
                                logger.warn(
                                        "Ignored file '" + filename + "' as there was no content, this is either " +
                                                "caused by uploading an empty file or a file path that does not exist on the client.");
                            }
                        }
                    }
                }
            }

            final Serializable attributeValue;
            final String attributeKey;

            if (submitCallback == null) {
                // it is an alfresco call.. use alfresco wrapper.
                attributeKey = FileUploadBean.getKey(uploadId);

                final FileUploadBean alfrescoWrapper = new FileUploadBean();
                alfrescoWrapper.setFile(bean.getFile());
                alfrescoWrapper.setFileName(bean.getFileName());
                alfrescoWrapper.setFilePath(bean.getFilePath());

                attributeValue = alfrescoWrapper;
            } else {
                // it is a circabc call.. use circabc wrapper.
                attributeKey = CircabcUploadedFile.getKey(uploadId);
                attributeValue = bean;
            }

            // examine the appropriate session to try and find the User object
            if (Application.inPortalServer() == false) {
                session.setAttribute(attributeKey, attributeValue);
            } else {
                // naff solution as we need to enumerate all session keys until we find the one that
                // should match our User objects - this is weak but we don't know how the underlying
                // Portal vendor has decided to encode the objects in the session
                final Enumeration<String> enumNames = session.getAttributeNames();
                while (enumNames.hasMoreElements()) {
                    final String name = (String) enumNames.nextElement();
                    // find an Alfresco value we know must be there...
                    if (name.startsWith("javax.portlet.p") && name
                            .endsWith(AuthenticationHelper.AUTHENTICATION_USER)) {
                        final String key = name
                                .substring(0, name.lastIndexOf(AuthenticationHelper.AUTHENTICATION_USER));
                        session.setAttribute(key + attributeKey, attributeValue);
                        break;
                    }
                }
            }

            if (submitCallback != null) {
                final FacesContext ctx = FacesHelper
                        .getFacesContext(request, response, servletConfig.getServletContext());

                final MethodBinding callback = ctx.getApplication()
                        .createMethodBinding("#{" + submitCallback + "}",
                                new Class[]{CircabcUploadedFile.class});
                callback.invoke(ctx, new Object[]{bean});
            }

            if (bean.getFile() == null && uploadId != null && logger.isWarnEnabled()) {
                logger.warn("no file uploaded for upload id: " + uploadId);
            }

            if (returnPage == null || returnPage.length() == 0) {
                throw new AlfrescoRuntimeException("return-page parameter has not been supplied");
            }

            if (returnPage.startsWith("javascript:")) {
                if (returnPage.equalsIgnoreCase(validJavaScript)) {
                    returnPage = returnPage.substring("javascript:".length());
                    // finally redirect
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending back javascript response " + returnPage);
                    }
                    response.setContentType(MimetypeMap.MIMETYPE_HTML);
                    response.setCharacterEncoding("UTF-8");
                    final PrintWriter out = response.getWriter();
                    out.println("<html><body><script type=\"text/javascript\">");
                    out.println(returnPage);
                    out.println("</script></body></html>");
                    out.close();
                } else {
                    if (logger.isErrorEnabled()) {
                        logger.error("Inalid return page" + returnPage);
                    }
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                // finally redirect
                if (logger.isDebugEnabled()) {
                    logger.debug("redirecting to: " + returnPage);
                }

                String cleanReturnPage = returnPage.replace(request.getContextPath() + "/" + "faces", "");

                if (circabcValidPages.contains(cleanReturnPage)) {
                    response.sendRedirect(returnPage);
                } else {
                    handleJSONObject(returnPage, request, response);

//        		if (logger.isErrorEnabled()){
//        			logger.error("Inalid return page" + returnPage );
//        		}
//            	response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

            }
        } catch (final Throwable error) {
            if (logger.isErrorEnabled()) {
                logger.error("Error", error);
            }
            Application.handleServletError(getServletContext(), (HttpServletRequest) request,
                    (HttpServletResponse) response, error, logger, returnPage);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("upload complete");
        }
    }

    /**
     * Added for file upload in Alfresco 4.2 Taken from org.alfresco.web.app.servlet.UploadFileServlet
     */
    private void handleJSONObject(String returnPage, final HttpServletRequest request,
                                  final HttpServletResponse response) {

        try {
            JSONObject json;
            try {
                json = new JSONObject(returnPage);

                if (json.has("id") && json.has("args")) {
                    // finally redirect
                    if (logger.isDebugEnabled()) {
                        logger.debug("Sending back javascript response " + returnPage);
                    }
                    response.setContentType(MimetypeMap.MIMETYPE_HTML);
                    response.setCharacterEncoding("utf-8");
                    // work-around for WebKit protection against embedded javascript on POST body response
                    response.setHeader("X-XSS-Protection", "0");
                    final PrintWriter out = response.getWriter();
                    out.println("<html><body><script type=\"text/javascript\">");

                    out.println("window.parent.upload_complete_helper(");
                    out.println("'" + json.getString("id") + "'");
                    out.println(", ");
                    out.println(json.getJSONObject("args"));
                    out.println(");");

                    out.println("</script></body></html>");
                    out.close();
                }
            } catch (JSONException e) {
                // finally redirect
                if (logger.isDebugEnabled()) {
                    logger.debug("redirecting to: " + returnPage);
                }

                response.sendRedirect(returnPage);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("upload complete");
            }
        } catch (Throwable error) {
            handleUploadException(request, response, error, returnPage);
        }
    }

    private void handleUploadException(HttpServletRequest request, HttpServletResponse response,
                                       Throwable error, String returnPage) {
        try {
            HttpSession session = request.getSession(true);
            ErrorBean errorBean = (ErrorBean) session.getAttribute(ErrorBean.ERROR_BEAN_NAME);
            if (errorBean == null) {
                errorBean = new ErrorBean();
                session.setAttribute(ErrorBean.ERROR_BEAN_NAME, errorBean);
            }
            errorBean.setLastError(error);
            errorBean.setReturnPage(returnPage);
        } catch (Throwable e) {
            logger.error("Error while handling upload Exception", e);
        }
        try {
            String errorPage = Application.getErrorPage(getServletContext());

            if (logger.isDebugEnabled()) {
                logger.debug("An error has occurred. Sending back response for redirecting to error page: "
                        + errorPage);
            }

            response.setContentType(MimetypeMap.MIMETYPE_HTML);
            response.setCharacterEncoding("utf-8");
            final PrintWriter out = response.getWriter();
            out.println("<html><body><script type=\"text/javascript\">");
            out.println(
                    "window.parent.location.replace(\" " + request.getContextPath() + errorPage + "\")");
            out.println("</script></body></html> ");
            out.close();
        } catch (Exception e) {
            logger.error("Error while handling upload Exception", e);
        }
    }

    private boolean allowZeroByteFiles() {
        final ClientConfigElement clientConfig = (ClientConfigElement) configService.getGlobalConfig()
                .getConfigElement(
                        ClientConfigElement.CONFIG_ELEMENT_ID);
        return clientConfig.isZeroByteFileUploads();
    }

}
