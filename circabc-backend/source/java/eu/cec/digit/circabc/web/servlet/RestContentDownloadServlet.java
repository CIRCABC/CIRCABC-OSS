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
import eu.cec.digit.circabc.util.CommonUtils;
import eu.cec.digit.circabc.util.PathUtils;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.BaseServlet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.*;

/**
 * @author Slobodan Filipovic Change BaseDownloadContentServlet to force response headers as
 * parameters in request so we can debug IE problems
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 URLEncoder and URLDecoder was moved to Spring.
 */
public class RestContentDownloadServlet extends BaseServlet {

    protected static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
    protected static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
    protected static final String ARG_PROPERTY = "property";
    protected static final String ARG_PATH = "path";
    private static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_RANGE = "Content-Range";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_ACCEPT_RANGES = "Accept-Ranges";
    private static final String HEADER_RANGE = "Range";
    private static final String HEADER_ETAG = "ETag";
    private static final String HEADER_CACHE_CONTROL = "Cache-Control";
    private static final String HEADER_LAST_MODIFIED = "Last-Modified";
    private static final String MULTIPART_BYTERANGES_BOUNDRY = "<ALF4558907921887235966L>";
    private static final String MULTIPART_BYTERANGES_HEADER = "multipart/byteranges; boundary="
            + MULTIPART_BYTERANGES_BOUNDRY;
    private static final String MULTIPART_BYTERANGES_BOUNDRY_SEP =
            "--" + MULTIPART_BYTERANGES_BOUNDRY;
    private static final String MULTIPART_BYTERANGES_BOUNDRY_END =
            MULTIPART_BYTERANGES_BOUNDRY_SEP + "--";
    /**
     * size of a multi-part byte range output buffer
     */
    private static final int CHUNKSIZE = 64 * 1024;
    private static final String LIBRARY = "Library";
    private static final String INFORMATION = "Information";
    private static final String NEWSGROUP = "Newsgroup";
    private static final String DOWNLOAD_FILE = "Download file: ";
    private static final String DOWNLOAD_CONTENT = "Download content";
    /**
     *
     */
    private static final long serialVersionUID = -208568714802217306L;
    private static Log logger = LogFactory.getLog(RestContentDownloadServlet.class);
    private List<QName> validContentTypes;

    public static CircabcServiceRegistry getCircabcServiceRegistry(final ServletContext sc) {
        final WebApplicationContext wc = WebApplicationContextUtils
                .getRequiredWebApplicationContext(sc);
        return (CircabcServiceRegistry) wc.getBean(CircabcServiceRegistry.CIRCABC_SERVICE_REGISTRY);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        validContentTypes = new ArrayList<QName>();
        validContentTypes.add(ContentModel.TYPE_CONTENT);
        validContentTypes.add(CircabcModel.TYPE_CUSTOMIZATION_CONTENT);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Prepare the nodeRef
        String uri = req.getRequestURI();
        String[] uriParts = uri.split("/");
        if ((uriParts.length != 4 && uriParts.length != 5)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request URL malformed");
            throw new ServletException("Request URL malformed");
        }
        Boolean hasGroupAspect = false;

        String uid = uriParts[uriParts.length - 1];
        if ("".equals(uid) || uid == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing  arg UID");
            throw new ServletException("Missing  arg UID");
        }

        servletAuthenticate(req, resp, false);

        final AuthenticationService authenticationService = getServiceRegistry(getServletContext())
                .getAuthenticationService();

        String authHdr = req.getHeader("Authorization");

        if (authHdr != null && authHdr.length() > 5 && authHdr.substring(0, 5)
                .equalsIgnoreCase("BASIC")) {
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

        NodeRef contentRef = Converter.createNodeRefFromId(uid);

        final NodeService nodeService = getServiceRegistry(getServletContext()).getNodeService();

        if (!nodeService.exists(contentRef)) {
            contentRef = Converter.createVersionNodeRefFromId(uid);
            if (!nodeService.exists(contentRef)) {
                contentRef = Converter.createArchiveNodeRefFromId(uid);
                if (!nodeService.exists(contentRef)) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Content unavailable");
                    throw new ServletException("Content unavailable");
                }
            }
        }

        hasGroupAspect =
                nodeService.hasAspect(contentRef, CircabcModel.ASPECT_INFORMATION) || nodeService
                        .hasAspect(contentRef, CircabcModel.ASPECT_LIBRARY);

        if (!validContentTypes.contains(nodeService.getType(contentRef))) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid content type");
            throw new ServletException("Invalid content type");
        }

        // check If-Modified-Since header and set Last-Modified header as
        // appropriate
        Date modified = (Date) nodeService.getProperty(contentRef, ContentModel.PROP_MODIFIED);
        if (modified != null) {
            long modifiedSince = 0L;
            try {
                modifiedSince = req.getDateHeader(HEADER_IF_MODIFIED_SINCE);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Error when get Date Header."/* ,e*/);
                }
            }

            if (modifiedSince > 0L) {
                // round the date to the ignore millisecond value which is not
                // supplied by header
                long modDate = (modified.getTime() / 1000L) * 1000L;
                if (modDate <= modifiedSince) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Returning 304 Not Modified.");
                    }
                    resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }
            resp.setHeader(HEADER_LAST_MODIFIED, String.valueOf(modified.getTime()));
            resp.setHeader(HEADER_CACHE_CONTROL, "must-revalidate");
            resp.setHeader(HEADER_ETAG, "\"" + Long.toString(modified.getTime()) + "\"");
        }

        String filename = nodeService.getProperty(contentRef, ContentModel.PROP_NAME).toString();
        String headerContentDispo = CommonUtils
                .generateFilenameContentDispositionHeader(true, filename, req);
        resp.setHeader("Content-Disposition", headerContentDispo);

        final ContentService contentService = getServiceRegistry(getServletContext())
                .getContentService();
        ContentReader reader = contentService.getReader(contentRef, ContentModel.PROP_CONTENT);
        if (reader == null) {
            reader = contentService.getReader(contentRef, CircabcModel.PROP_CONTENT);
        }

        // ensure that it is safe to use
        reader = FileContentReader.getSafeContentReader(reader,
                Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING), contentRef, reader);

        String mimetype = reader.getMimetype();
        // fall back if unable to resolve mimetype property
        if (mimetype == null || mimetype.length() == 0) {
            MimetypeService mimetypeMap = getServiceRegistry(getServletContext()).getMimetypeService();
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

        // get the content and stream directly to the response output stream
        // assuming the repo is capable of streaming in chunks, this should
        // allow large files
        // to be streamed directly to the browser response stream.
        resp.setHeader(HEADER_ACCEPT_RANGES, "bytes");

        final LogRecord logRecord = logDownloadRequest(contentRef);
        final CircabcServiceRegistry circabcServiceRegistry = getCircabcServiceRegistry(
                getServletContext());
        final LogService logService = circabcServiceRegistry.getLogService();

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

                // ensure the range header is starts with "bytes=" and process
                // the range(s)
                if (range.length() > 6) {
                    processedRange = processRange(resp, reader, range.substring(6), contentRef,
                            ContentModel.PROP_CONTENT, mimetype, req);
                }
            }
            if (processedRange == false) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending complete file content...");
                }

                // set mimetype for the content and the character encoding for
                // the stream
                resp.setContentType(mimetype);
                resp.setCharacterEncoding(reader.getEncoding());

                // return the complete entity range
                long size = reader.getSize();
                resp.setHeader(HEADER_CONTENT_RANGE,
                        "bytes 0-" + Long.toString(size - 1L) + "/" + Long.toString(size));
                resp.setHeader(HEADER_CONTENT_LENGTH, Long.toString(size));
                reader.getContent(resp.getOutputStream());
            }

            logRecord.setOK(true);

        } catch (SocketException e1) {
            if (logger.isErrorEnabled()) {
                logger
                        .error("Client aborted stream read:\n\tnode: " + contentRef + "\n\tcontent: " + reader);
            }
            logRecord.setOK(false);
        } catch (ContentIOException e2) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed stream read:\n\tnode: " + contentRef + " due to: " + e2.getMessage());
            }
            logRecord.setOK(false);
        } catch (Throwable err) {
            if (logger.isErrorEnabled()) {
                logger.error("Download error:\n\tnode: " + contentRef + "\n\tcontent: " + reader);
            }
            logRecord.setOK(false);
        }

        if (hasGroupAspect) {
            logService.log(logRecord);
        }
    }

    /**
     * Process a range header - handles single and multiple range requests.
     */
    private boolean processRange(HttpServletResponse res, ContentReader reader, String range,
                                 NodeRef ref,
                                 QName property, String mimetype, HttpServletRequest req) throws IOException {
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
                                       String mimetype,
                                       HttpServletRequest req) throws IOException {
        // return the specific set of bytes as requested in the content-range
        // header

        /*
         * Examples of byte-content-range-spec values, assuming that the entity
         * contains total of 1234 bytes: The first 500 bytes: bytes 0-499/1234
         * The second 500 bytes: bytes 500-999/1234 All except for the first 500
         * bytes: bytes 500-1233/1234
         */
        /*
         * 'Range' header example: bytes=10485760-20971519
         */

        boolean processedRange = false;
        Range r = null;
        try {
            r = Range.constructRange(range, mimetype, reader.getSize());
        } catch (IllegalArgumentException err) {
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Failed to parse range header - returning 416 status code: " + err.getMessage());
            }

            res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            res.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
            res.getOutputStream().close();
            return true;
        }

        // set Partial Content status and range headers
        res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        res.setContentType(mimetype);
        String contentRange =
                "bytes " + Long.toString(r.getStart()) + "-" + Long.toString(r.getEnd()) + "/"
                        + Long.toString(reader.getSize());
        res.setHeader(HEADER_CONTENT_RANGE, contentRange);
        res.setHeader(HEADER_CONTENT_LENGTH, Long.toString((r.getEnd() - r.getStart()) + 1L));

        if (logger.isDebugEnabled()) {
            logger.debug("Processing: Content-Range: " + contentRange);
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
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to process single range due to IO Exception: " + err.getMessage());
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
                                      QName property,
                                      String mimetype, HttpServletRequest req) throws IOException {

        // return the sets of bytes as requested in the content-range header
        // the response will be formatted as multipart/byteranges media type
        // message

        /*
         * Examples of byte-ranges-specifier values (assuming an entity-body of
         * length 10000):
         *
         * - The first 500 bytes (byte offsets 0-499, inclusive): bytes=0-499 -
         * The second 500 bytes (byte offsets 500-999, inclusive): bytes=500-999
         * - The final 500 bytes (byte offsets 9500-9999, inclusive): bytes=-500
         * - Or bytes=9500- - The first and last bytes only (bytes 0 and 9999):
         * bytes=0-0,-1 - Several legal but not canonical specifications of byte
         * offsets 500-999, inclusive: bytes=500-600,601-999
         * bytes=500-700,601-999
         */

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
                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Failed to parse range header - returning 416 status code: " + err.getMessage());
                }

                res.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                res.setHeader(HEADER_CONTENT_RANGE, "\"*\"");
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
                    if (first.getEnd() + 1 == second.getStart()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Merging byte range: " + first + " with " + second);
                        }

                        // merge second range into first
                        first.setEnd(second.getEnd());
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
            res.setHeader(HEADER_CONTENT_TYPE, MULTIPART_BYTERANGES_HEADER);
            res.setHeader(HEADER_CONTENT_LENGTH, Long.toString(length));

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
                        if (logger.isDebugEnabled()) {
                            logger.debug(
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
        final boolean trace = logger.isTraceEnabled();

        // TODO: investigate using getFileChannel() on ContentReader

        if (r.getStart() != 0L) {
            long skipped = offset;
            while (skipped < r.getStart()) {
                skipped += is.skip(r.getStart() - skipped);
            }
        }
        long span = (r.getEnd() - r.getStart()) + 1L;
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

    private LogRecord logDownloadRequest(NodeRef nodeRef) {

        final ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
        String filename = serviceRegistry.getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME)
                .toString();
        return setLogRecord(serviceRegistry, nodeRef, filename);
    }

    private LogRecord setLogRecord(final ServiceRegistry serviceRegistry, final NodeRef nodeRef,
                                   final String filename) {
        final LogRecord logRecord = new LogRecord();
        try {
            // get the services we need to retrieve the content
            final CircabcServiceRegistry circabcServiceRegistry = getCircabcServiceRegistry(
                    getServletContext());
            final NodeService nodeService = serviceRegistry.getNodeService();
            final ManagementService managementService = circabcServiceRegistry.getManagementService();
            final NodeRef currentInterestGroup = managementService.getCurrentInterestGroup(nodeRef);
            final AuthenticationService authenticationService = serviceRegistry
                    .getAuthenticationService();

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

                    Long documentID = (Long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
                    String user = authenticationService.getCurrentUserName();

                    logRecord.setService(service);
                    logRecord.setActivity(DOWNLOAD_CONTENT);
                    logRecord.setInfo(DOWNLOAD_FILE + filename);
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
