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
import eu.cec.digit.circabc.util.CommonUtils;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.surf.util.URLEncoder;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.*;


/**
 * @author Slobodan Filipovic Change BaseDownloadContentServlet to force response headers as
 * parameters in request so we can debug IE problems
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 URLEncoder and URLDecoder was moved to Spring.
 */
public abstract class IEBaseDownloadContentServlet extends BaseServlet {

    protected static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
    protected static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
    protected static final String URL_DIRECT = "d";
    protected static final String URL_DIRECT_LONG = "direct";
    protected static final String URL_ATTACH = "a";
    protected static final String URL_ATTACH_LONG = "attach";
    protected static final String ARG_PROPERTY = "property";
    protected static final String ARG_PATH = "path";
    private static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final long serialVersionUID = -4558907921887235967L;
    private static final String POWER_POINT_DOCUMENT_MIMETYPE = "application/vnd.powerpoint";
    private static final String POWER_POINT_2007_DOCUMENT_MIMETYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    private static final String MULTIPART_BYTERANGES_BOUNDRY = "<ALF4558907921887235966L>";
    private static final String MULTIPART_BYTERANGES_HEADER =
            "multipart/byteranges; boundary=" + MULTIPART_BYTERANGES_BOUNDRY;
    private static final String MULTIPART_BYTERANGES_BOUNDRY_SEP =
            "--" + MULTIPART_BYTERANGES_BOUNDRY;
    private static final String MULTIPART_BYTERANGES_BOUNDRY_END =
            MULTIPART_BYTERANGES_BOUNDRY_SEP + "--";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_RANGE = "Content-Range";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HEADER_RANGE = "Range";
    private static final String HEADER_ETAG = "ETag";
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String HEADER_LAST_MODIFIED = "Last-Modified";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    /**
     * size of a multi-part byte range output buffer
     */
    private static final int CHUNKSIZE = 64 * 1024;
    protected transient ThreadLocal<Boolean> permissionDenied = new ThreadLocalFalse();

    /**
     * Helper to generate a URL to a content node for downloading content from the server.
     *
     * @param pattern The pattern to use for the URL
     * @param ref     NodeRef of the content node to generate URL for (cannot be null)
     * @param name    File name to return in the URL (cannot be null)
     * @return URL to download the content from the specified node
     */
    protected static String generateUrl(String pattern, NodeRef ref, String name) {
        return MessageFormat.format(pattern, ref.getStoreRef().getProtocol(),
                ref.getStoreRef().getIdentifier(),
                ref.getId(),
                URLEncoder.encode(name));
    }

    /**
     * Gets the logger to use for this request.
     * <p>
     * This will show all debug entries from this class as though they came from the subclass.
     *
     * @return The logger
     */
    protected abstract Log getLogger();

    /**
     * Processes the download request using the current context i.e. no authentication checks are
     * made, it is presumed they have already been done.
     *
     * @param req             The HTTP request
     * @param res             The HTTP response
     * @param redirectToLogin Flag to determine whether to redirect to the login page if the user does
     *                        not have the correct permissions
     */
    protected void processDownloadRequest(HttpServletRequest req, HttpServletResponse res,
                                          boolean redirectToLogin, boolean transmitContent)
            throws ServletException, IOException {

        Log logger = getLogger();
        String uri = req.getRequestURI();

        if (logger.isDebugEnabled()) {
            String queryString = req.getQueryString();
            logger.debug("Processing URL: " + uri +
                    ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
        }

        uri = uri.substring(req.getContextPath().length());
        StringTokenizer t = new StringTokenizer(uri, "/");
        int tokenCount = t.countTokens();

        t.nextToken();    // skip servlet name

        // attachment mode (either 'attach' or 'direct')
        String attachToken = t.nextToken();
        boolean attachment = URL_ATTACH.equals(attachToken) || URL_ATTACH_LONG.equals(attachToken);

        ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());

        // get or calculate the noderef and filename to download as
        NodeRef nodeRef;
        String filename = "";

        // do we have a path parameter instead of a NodeRef?
        String path = req.getParameter(ARG_PATH);
        if (path != null && path.length() != 0) {
            // process the name based path to resolve the NodeRef and the Filename element
            PathRefInfo pathInfo = resolveNamePath(getServletContext(), path);

            nodeRef = pathInfo.NodeRef;
            filename = pathInfo.Filename;
        } else {
            // a NodeRef must have been specified if no path has been found
            if (tokenCount < 6) {
                throw new IllegalArgumentException(
                        "Download URL did not contain all required args: " + uri);
            }

            // assume 'workspace' or other NodeRef based protocol for remaining URL elements
            StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
            String id = URLDecoder.decode(t.nextToken());

            // build noderef from the appropriate URL elements
            nodeRef = new NodeRef(storeRef, id);
        }

        // get the services we need to retrieve the content
        NodeService nodeService = serviceRegistry.getNodeService();
        ContentService contentService = serviceRegistry.getContentService();
        PermissionService permissionService = serviceRegistry.getPermissionService();

        Principal userPrincipal = req.getUserPrincipal();

        try {
            if (permissionDenied == null) {
                permissionDenied = new ThreadLocalFalse();
            }

            permissionDenied.set(true);

            // check that the user has at least READ_CONTENT access - else redirect to the login page
            if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT)
                    == AccessStatus.DENIED && userPrincipal == null) {

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "User does not have permissions to read content for NodeRef: " + nodeRef.toString());
                }

                if (redirectToLogin) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Redirecting to login page...");
                    }

                    HttpSession session = req.getSession();

                    session.setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL,
                            req.getRequestURI());

                    redirectToLoginPage(req, res, getServletContext());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Returning 403 Forbidden error...");
                    }

                    res.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }
            // If the user has no access, show error
            else if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT)
                    == AccessStatus.DENIED && userPrincipal != null) {

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

            if (tokenCount > 6) {
                // found additional relative path elements i.e. noderefid/images/file.txt
                // this allows a url to reference siblings nodes via a cm:name based relative path
                // solves the issue with opening HTML content containing relative URLs in HREF or IMG tags etc.
                List<String> paths = new ArrayList<>(tokenCount - 5);
                while (t.hasMoreTokens()) {
                    paths.add(URLDecoder.decode(t.nextToken()));
                }
                filename = paths.get(paths.size() - 1);

                try {
                    NodeRef parentRef = serviceRegistry.getNodeService().getPrimaryParent(nodeRef)
                            .getParentRef();
                    FileInfo fileInfo = serviceRegistry.getFileFolderService()
                            .resolveNamePath(parentRef, paths);
                    nodeRef = fileInfo.getNodeRef();
                } catch (FileNotFoundException e) {
                    throw new AlfrescoRuntimeException(
                            "Unable to find node reference by relative path:" + uri);
                }
            } else {
                if (serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME) != null) {
                    filename = serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME)
                            .toString();
                } else {
                    // filename is last remaining token
                    filename = t.nextToken();
                }
            }

            // get qualified of the property to get content from - default to ContentModel.PROP_CONTENT
            QName propertyQName = ContentModel.PROP_CONTENT;
            String property = req.getParameter(ARG_PROPERTY);
            if (property != null && property.length() != 0) {
                propertyQName = QName.createQName(property);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Found NodeRef: " + nodeRef);
                logger.debug("Will use filename: " + filename);
                logger.debug("For property: " + propertyQName);
                logger.debug("With attachment mode: " + attachment);
            }

            res.reset();
            // check If-Modified-Since header and set Last-Modified header as appropriate
            Date modified = (Date) nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
            if (modified != null) {
                long modifiedSince = req.getDateHeader(HEADER_IF_MODIFIED_SINCE);
                if (modifiedSince > 0L) {
                    // round the date to the ignore millisecond value which is not supplied by header
                    long modDate = (modified.getTime() / 1000L) * 1000L;
                    if (modDate <= modifiedSince) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Returning 304 Not Modified.");
                        }
                        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                }
                setDateHeader(res, HEADER_LAST_MODIFIED, modified.getTime(), req);
                setHeader(res, HEADER_CACHE_CONTROL, "must-revalidate", req);
                setHeader(res, HEADER_ETAG, "\"" + Long.toString(modified.getTime()) + "\"", req);
            }

            // set header based on filename - will force a Save As from the browse if it doesn't recognise it
            // this is better than the default response of the browser trying to display the contents
            String headerContentDispo = CommonUtils
                    .generateFilenameContentDispositionHeader(attachment, filename, req);
            res.setHeader("Content-Disposition", headerContentDispo);

            // get the content reader
            ContentReader reader = contentService.getReader(nodeRef, propertyQName);

            //
            if (reader == null || !reader.exists()) {
                if (propertyQName.equals(ContentModel.PROP_CONTENT)) {
                    propertyQName = CircabcModel.PROP_CONTENT;
                } else if (propertyQName.equals(CircabcModel.PROP_CONTENT)) {
                    propertyQName = ContentModel.PROP_CONTENT;
                }
                reader = contentService.getReader(nodeRef, propertyQName);
            }

            // ensure that it is safe to use
            reader = FileContentReader.getSafeContentReader(
                    reader,
                    Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                    nodeRef, reader);

            String mimetype = reader.getMimetype();
            // fall back if unable to resolve mimetype property
            if (mimetype == null || mimetype.length() == 0) {
                MimetypeService mimetypeMap = serviceRegistry.getMimetypeService();
                mimetype = MIMETYPE_OCTET_STREAM;
                int extIndex = filename.lastIndexOf('.');
                if (extIndex != -1) {
                    String ext = filename.substring(extIndex + 1);
                    String mt = mimetypeMap.getMimetypesByExtension().get(ext);
                    if (mt != null) {
                        mimetype = mt;
                    }
                }
            }

            // explicitly set the content disposition header if the content is powerpoint
            if (!attachment && (mimetype.equals(POWER_POINT_2007_DOCUMENT_MIMETYPE) ||
                    mimetype.equals(POWER_POINT_DOCUMENT_MIMETYPE))) {
                setHeader(res, HEADER_CONTENT_DISPOSITION, "attachment", req);
            }

            // get the content and stream directly to the response output stream
            // assuming the repo is capable of streaming in chunks, this should allow large files
            // to be streamed directly to the browser response stream.
            setHeader(res, HEADER_ACCEPT_RANGES, "bytes", req);

            // for a GET request, transmit the content else just the headers are sent
            if (transmitContent) {
                try {
                    boolean processedRange = false;
                    String range = req.getHeader(HEADER_CONTENT_RANGE);
                    if (range == null) {
                        range = req.getHeader(HEADER_RANGE);
                    }
                    if (range != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found content range header: " + range);
                        }

                        // ensure the range header is starts with "bytes=" and process the range(s)
                        if (range.length() > 6) {
                            processedRange = processRange(res, reader, range.substring(6), nodeRef, propertyQName,
                                    mimetype, req);
                        }
                    }
                    if (processedRange == false) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Sending complete file content...");
                        }

                        // set mimetype for the content and the character encoding for the stream
                        res.setContentType(mimetype);
                        res.setCharacterEncoding(reader.getEncoding());

                        // return the complete entity range
                        long size = reader.getSize();
                        setHeader(res, HEADER_CONTENT_RANGE,
                                "bytes 0-" + Long.toString(size - 1L) + "/" + Long.toString(size), req);
                        setHeader(res, HEADER_CONTENT_LENGTH, Long.toString(size), req);
                        reader.getContent(res.getOutputStream());
                    }
                } catch (SocketException e1) {
                    // the client cut the connection - our mission was accomplished apart from a little error message
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
                    }
                } catch (ContentIOException e2) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Failed stream read:\n\tnode: " + nodeRef + " due to: " + e2.getMessage());
                    }
                } catch (Throwable err) {
                    if (err.getCause() instanceof SocketException) {
                        // the client cut the connection - our mission was accomplished apart from a little error message
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "Client aborted stream read:\n\tnode: " + nodeRef + "\n\tcontent: " + reader);
                        }
                    } else {
                        throw err;
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("HEAD request processed - no content sent.");
                }
                res.getOutputStream().close();
            }
        } catch (Throwable err) {
            throw new AlfrescoRuntimeException(
                    "Error during download content servlet processing: " + err.getMessage(), err);
        }

        permissionDenied.set(false);
    }

    private void setDateHeader(HttpServletResponse res, String key, long time,
                               HttpServletRequest req) {
        String parameter = req.getParameter(key);
        if (parameter != null && parameter.length() != 0) {
            if (!parameter.equalsIgnoreCase("no")) {
                res.setDateHeader(key, Long.valueOf(parameter));
            }
        } else {
            res.setDateHeader(key, time);
        }

    }

    /**
     * Process a range header - handles single and multiple range requests.
     */
    private boolean processRange(HttpServletResponse res, ContentReader reader, String range,
                                 NodeRef ref, QName property, String mimetype, HttpServletRequest req)
            throws IOException {
        // test for multiple byte ranges present in header
        if (range.indexOf(',') == -1) {
            return processSingleRange(res, reader, range, mimetype, req);
        } else {
            return processMultiRange(res, range, ref, property, mimetype, req);
        }
    }

    /**
     * Process a single range request.
     *
     * @param res      HttpServletResponse
     * @param reader   ContentReader to retrieve content
     * @param range    Range header value
     * @param mimetype Content mimetype
     * @return true if processed range, false otherwise
     */
    private boolean processSingleRange(HttpServletResponse res, ContentReader reader, String range,
                                       String mimetype, HttpServletRequest req)
            throws IOException {
        // return the specific set of bytes as requested in the content-range header

	      /* Examples of byte-content-range-spec values, assuming that the entity contains total of 1234 bytes:
	            The first 500 bytes:
	             bytes 0-499/1234
	            The second 500 bytes:
	             bytes 500-999/1234
	            All except for the first 500 bytes:
	             bytes 500-1233/1234 */
	      /* 'Range' header example:
	             bytes=10485760-20971519 */

        boolean processedRange = false;
        Range r = null;
        try {
            r = Range.constructRange(range, mimetype, reader.getSize());
        } catch (IllegalArgumentException err) {
            if (getLogger().isDebugEnabled()) {
                getLogger()
                        .debug("Failed to parse range header - returning 416 status code: " + err.getMessage());
            }

            res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            setHeader(res, HEADER_CONTENT_RANGE, "\"*\"", req);
            res.getOutputStream().close();
            return true;
        }

        // set Partial Content status and range headers
        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        res.setContentType(mimetype);
        String contentRange =
                "bytes " + Long.toString(r.start) + "-" + Long.toString(r.end) + "/" + Long
                        .toString(reader.getSize());
        setHeader(res, HEADER_CONTENT_RANGE, contentRange, req);
        setHeader(res, HEADER_CONTENT_LENGTH, Long.toString((r.end - r.start) + 1L), req);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Processing: Content-Range: " + contentRange);
        }

        InputStream is = null;
        try {
            // output the binary data for the range
            ServletOutputStream os = res.getOutputStream();
            is = reader.getContentInputStream();

            streamRangeBytes(r, is, os, 0L);

            os.close();
            processedRange = true;
        } catch (IOException err) {
            if (getLogger().isDebugEnabled()) {
                getLogger()
                        .debug("Unable to process single range due to IO Exception: " + err.getMessage());
            }
            throw err;
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return processedRange;
    }

    /**
     * Process multiple ranges.
     *
     * @param res      HttpServletResponse
     * @param range    Range header value
     * @param ref      NodeRef to the content for streaming
     * @param property Content Property for the content
     * @param mimetype Mimetype of the content
     * @return true if processed range, false otherwise
     */
    private boolean processMultiRange(HttpServletResponse res, String range, NodeRef ref,
                                      QName property, String mimetype, HttpServletRequest req)
            throws IOException {
        final Log logger = getLogger();

        // return the sets of bytes as requested in the content-range header
        // the response will be formatted as multipart/byteranges media type message

	      /* Examples of byte-ranges-specifier values (assuming an entity-body of length 10000):

	      - The first 500 bytes (byte offsets 0-499, inclusive):  bytes=0-499
	      - The second 500 bytes (byte offsets 500-999, inclusive):
	        bytes=500-999
	      - The final 500 bytes (byte offsets 9500-9999, inclusive):
	        bytes=-500
	      - Or bytes=9500-
	      - The first and last bytes only (bytes 0 and 9999):  bytes=0-0,-1
	      - Several legal but not canonical specifications of byte offsets 500-999, inclusive:
	         bytes=500-600,601-999
	         bytes=500-700,601-999 */

        boolean processedRange = false;

        // get the content reader
        ContentService contentService = getServiceRegistry(getServletContext()).getContentService();
        ContentReader reader = contentService.getReader(ref, property);

        final List<Range> ranges = new ArrayList<>(8);
        long entityLength = reader.getSize();
        for (StringTokenizer t = new StringTokenizer(range, ", "); t.hasMoreTokens(); /**/) {
            try {
                ranges.add(Range.constructRange(t.nextToken(), mimetype, entityLength));
            } catch (IllegalArgumentException err) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(
                            "Failed to parse range header - returning 416 status code: " + err.getMessage());
                }

                res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                setHeader(res, HEADER_CONTENT_RANGE, "\"*\"", req);
                res.getOutputStream().close();
                return true;
            }
        }

        if (ranges.size() != 0) {
            // merge byte ranges if possible
            if (ranges.size() > 1) {
                for (int i = 0; i < ranges.size() - 1; i++) {
                    Range first = ranges.get(i);
                    Range second = ranges.get(i + 1);
                    if (first.end + 1 == second.start) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Merging byte range: " + first + " with " + second);
                        }

                        // merge second range into first
                        first.end = second.end;
                        // delete second range
                        ranges.remove(i + 1);
                        // reset loop index
                        i--;
                    }
                }
            }

            // calculate response content length
            long length = MULTIPART_BYTERANGES_BOUNDRY_END.length() + 2;
            for (Range r : ranges) {
                length += r.getLength();
            }

            // output headers as we have at least one range to process
            res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            setHeader(res, HEADER_CONTENT_TYPE, MULTIPART_BYTERANGES_HEADER, req);
            setHeader(res, HEADER_CONTENT_LENGTH, Long.toString(length), req);

            ServletOutputStream os = res.getOutputStream();

            InputStream is = null;
            try {
                for (Range r : ranges) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing: " + r.getContentRange());
                    }

                    try {
                        // output the header bytes for the range
                        r.outputHeader(os);

                        // output the binary data for the range
                        // need a new reader for each new InputStream
                        is = contentService.getReader(ref, property).getContentInputStream();
                        streamRangeBytes(r, is, os, 0L);
                        is.close();
                        is = null;

                        // section marker and flush stream
                        os.println();
                        os.flush();
                    } catch (IOException err) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug(
                                    "Unable to process multiple range due to IO Exception: " + err.getMessage());
                        }
                        throw err;
                    }
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            // end marker
            os.println(MULTIPART_BYTERANGES_BOUNDRY_END);
            os.close();
            processedRange = true;
        }

        return processedRange;
    }

    /**
     * Stream a range of bytes from the given InputStream to the ServletOutputStream
     *
     * @param r      Byte Range to process
     * @param is     InputStream
     * @param os     ServletOutputStream
     * @param offset Assumed InputStream position - to calculate skip bytes from
     * @return current InputStream position - so the stream can be reused if required
     */
    private void streamRangeBytes(final Range r, final InputStream is, final ServletOutputStream os,
                                  long offset)
            throws IOException {
        final Log logger = getLogger();
        final boolean trace = logger.isTraceEnabled();

        // TODO: investigate using getFileChannel() on ContentReader

        if (r.start != 0L) {
            long skipped = offset;
            while (skipped < r.start) {
                skipped += is.skip(r.start - skipped);
            }
        }
        long span = (r.end - r.start) + 1L;
        long bytesLeft = span;
        int read = 0;
        byte[] buf = new byte[((int) bytesLeft) < CHUNKSIZE ? (int) bytesLeft : CHUNKSIZE];
        while ((read = is.read(buf)) > 0 && bytesLeft != 0L) {
            os.write(buf, 0, read);

            bytesLeft -= (long) read;

            if (bytesLeft != 0L) {
                int resize = ((int) bytesLeft) < CHUNKSIZE ? (int) bytesLeft : CHUNKSIZE;
                if (resize != buf.length) {
                    buf = new byte[resize];
                }
            }
            if (trace) {
                logger.trace("...wrote " + read + " bytes, with " + bytesLeft + " to go...");
            }
        }
    }

    protected boolean isPermissionDenied() {
        return permissionDenied.get();
    }

    /**
     * @param res   Server response
     * @param key   Header key to set
     * @param value Header value to set
     * @param req   Server request
     */
    private void setHeader(HttpServletResponse res, String key, String value,
                           HttpServletRequest req) {
        String parameter = req.getParameter(key);
        if (parameter != null && parameter.length() != 0) {
            parameter = URLDecoder.decode(parameter);
            if (!parameter.equalsIgnoreCase("no")) {
                res.setHeader(key, parameter);
            }
        } else {
            res.setHeader(key, value);
        }
    }

    protected static final class ThreadLocalFalse extends ThreadLocal<Boolean> {

        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    }

    /**
     * Representation of a single byte range.
     */
    private static class Range implements Comparable<Range> {

        private long start;
        private long end;
        private long entityLength;
        private String contentType;
        private String contentRange;

        /**
         * Constructor
         *
         * @param contentType  Mimetype of the range content
         * @param start        Start position in the parent entity
         * @param end          End position in the parent entity
         * @param entityLength Length of the parent entity
         */
        Range(String contentType, long start, long end, long entityLength) {
            this.contentType = HEADER_CONTENT_TYPE + ": " + contentType;
            this.start = start;
            this.end = end;
            this.entityLength = entityLength;
        }

        /**
         * Factory method to construct a byte range from a range header value.
         *
         * @param range        Range header value
         * @param contentType  Mimetype of the range
         * @param entityLength Length of the parent entity
         * @return Range
         * @throws IllegalArgumentException for an invalid range
         */
        static Range constructRange(String range, String contentType, long entityLength) {
            if (range == null) {
                throw new IllegalArgumentException("Range argument is mandatory");
            }

            // strip total if present - it does not give us anything useful
            if (range.indexOf('/') != -1) {
                range = range.substring(0, range.indexOf('/'));
            }

            // find the separator
            int separator = range.indexOf('-');
            if (separator == -1) {
                throw new IllegalArgumentException("Invalid range: " + range);
            }

            try {
                // split range and parse values
                long start = 0L;
                if (separator != 0) {
                    start = Long.parseLong(range.substring(0, separator));
                }
                long end = entityLength - 1L;
                if (separator != range.length() - 1) {
                    end = Long.parseLong(range.substring(separator + 1));
                }

                // return object to represent the byte-range
                return new Range(contentType, start, end, entityLength);
            } catch (NumberFormatException err) {
                throw new IllegalArgumentException("Unable to parse range value: " + range);
            }
        }

        /**
         * Output the header bytes for a multi-part byte range header
         */
        void outputHeader(ServletOutputStream os) throws IOException {
            // output multi-part boundry separator
            os.println(MULTIPART_BYTERANGES_BOUNDRY_SEP);
            // output content type and range size sub-header for this part
            os.println(this.contentType);
            os.println(getContentRange());
            os.println();
        }

        /**
         * @return the length in bytes of the byte range content including the header bytes
         */
        int getLength() {
            // length in bytes of range plus it's header plus section marker and line feed bytes
            return MULTIPART_BYTERANGES_BOUNDRY_SEP.length() + 2 +
                    this.contentType.length() + 2 +
                    getContentRange().length() + 4 + (int) (this.end - this.start + 1L) + 2;
        }

        /**
         * @return the Content-Range header string value for this byte range
         */
        private String getContentRange() {
            if (this.contentRange == null) {
                this.contentRange = "Content-Range: bytes " + Long.toString(this.start) + "-" +
                        Long.toString(this.end) + "/" + Long.toString(this.entityLength);
            }
            return this.contentRange;
        }

        @Override
        public String toString() {
            return this.start + "-" + this.end;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Range o) {
            return this.start < o.start ? 1 : -1;
        }
    }
}
