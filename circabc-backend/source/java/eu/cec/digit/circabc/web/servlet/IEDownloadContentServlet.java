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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Slobodan Filipovic
 * <p>
 * This is copy of org.alfresco.web.app.servlet.DownloadContentServlet in order to resolve problem
 * when user download file  with IE please see jira issue https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-1854
 * more details When migrate to new version of alfresco please compare this file with original
 * alfresco file
 */

public abstract class IEDownloadContentServlet extends IEBaseDownloadContentServlet {

    /**
     *
     */
    private static final long serialVersionUID = 2741577203046319378L;
    private static final String DOWNLOAD_URL = "/d/" + URL_ATTACH + "/{0}/{1}/{2}/{3}";
    private static final String BROWSER_URL = "/d/" + URL_DIRECT + "/{0}/{1}/{2}/{3}";
    private static Log logger = LogFactory.getLog(IEDownloadContentServlet.class);
    protected transient ThreadLocal<Boolean> authenticationError = new ThreadLocalFalse();

    /**
     * Helper to generate a URL to a content node for downloading content from the server. The content
     * is supplied as an HTTP1.1 attachment to the response. This generally means a browser should
     * prompt the user to save the content to specified location.
     *
     * @param ref  NodeRef of the content node to generate URL for (cannot be null)
     * @param name File name to return in the URL (cannot be null)
     * @return URL to download the content from the specified node
     */
    public static String generateDownloadURL(NodeRef ref, String name) {
        return generateUrl(DOWNLOAD_URL, ref, name);
    }

    /**
     * Helper to generate a URL to a content node for downloading content from the server. The content
     * is supplied directly in the reponse. This generally means a browser will attempt to open the
     * content directly if possible, else it will prompt to save the file.
     *
     * @param ref  NodeRef of the content node to generate URL for (cannot be null)
     * @param name File name to return in the URL (cannot be null)
     * @return URL to download the content from the specified node
     */
    public static String generateBrowserURL(NodeRef ref, String name) {
        return generateUrl(BROWSER_URL, ref, name);
    }

    protected ThreadLocal<Boolean> initauthenticationErrorThreadLocal() {
        return new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };
    }

    @Override
    protected Log getLogger() {
        return logger;
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doHead(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            String queryString = req.getQueryString();
            logger.debug("Authenticating (HEAD) request to URL: " + req.getRequestURI() +
                    ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
        }
        if (authenticationError == null) {
            authenticationError = initauthenticationErrorThreadLocal();
        }
        authenticationError.set(false);
        AuthenticationStatus status = servletAuthenticate(req, res);
        if (status == AuthenticationStatus.Failure) {
            authenticationError.set(true);
            return;
        }
        processDownloadRequest(req, res, true, false);

    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (logger.isDebugEnabled()) {
            String queryString = req.getQueryString();
            logger.debug("Authenticating (GET) request to URL: " + req.getRequestURI() +
                    ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
        }

        authenticationError.set(false);
        AuthenticationStatus status = servletAuthenticate(req, res, false);
        if (status == AuthenticationStatus.Failure) {

            status = AuthenticationHelper.authenticate(getServletContext(), req, res, true);

            if (status == AuthenticationStatus.Failure) {

                authenticationError.set(true);

                return;
            }
        }

        processDownloadRequest(req, res, true, true);
    }

    protected boolean isAuthenticationError() {
        return authenticationError.get();
    }

}
