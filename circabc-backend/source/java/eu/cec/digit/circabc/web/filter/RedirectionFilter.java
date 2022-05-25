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
package eu.cec.digit.circabc.web.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handle redirection to the correct url for the current context.
 * In order to decommission old user interface, this filter is used to redirect
 * to the new url.
 */
public class RedirectionFilter implements Filter {

    private static final Log logger = LogFactory.getLog(RedirectionFilter.class);

    @Override
    public void destroy() {
        if (logger.isInfoEnabled()) {
            logger.info("destroy");
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        if (logger.isInfoEnabled()) {
            logger.info("filtering " + httpReq.getRequestURI());
        }
        String requestURI = httpReq.getRequestURI();

        if (requestURI.startsWith("/faces/jsp/extension/session_expired.jsp")
                || requestURI.startsWith("/faces/jsp/extension/wai/ecas/ecaslogin.jsp")) {
            chain.doFilter(req, res);
        } else if (requestURI.startsWith("/faces/jsp/extension/")) {
            httpRes.sendRedirect(httpReq.getContextPath() + "/ui/index.html");
        } else if (requestURI.startsWith("/w/browse/")) {
            httpRes.sendRedirect(
                    httpReq.getContextPath() + "/ui/w/browse/" + requestURI.replaceFirst("/w/browse/", ""));
        } else if (requestURI.startsWith("/d/a/") || requestURI.startsWith("/d/d/")) {
            String[] path = requestURI.split("/");
            if (path.length > 5) {
                httpRes.sendRedirect(httpReq.getContextPath() + "/ui/w/browse/" + path[5]
                        + (requestURI.startsWith("/d/d/") ? "?download=true" : ""));
            }
        } else {
            chain.doFilter(req, res);
        }

    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        if (logger.isInfoEnabled()) {
            logger.info("init");
        }

    }

}