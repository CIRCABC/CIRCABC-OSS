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
package eu.cec.digit.circabc.web.app.servlet;

import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Migration 3.1 -> 3.4.6 - 06/12/2011 New constant LoginOutcomeBean.PARAM_REDIRECT_URL added.
 */
public class AuthenticationFilter extends
        org.alfresco.web.app.servlet.AuthenticationFilter {

    /**
     * forcing guess access is available on most servlets
     */
    public static final String ARG_GUEST = CircabcConstant.GUEST_AUTHORITY;
    /**
     * an existing Ticket can be passed to most servlet for non-session based authentication
     */
    public static final String ARG_TICKET = "ticket";
    public static final String MSG_AUTH_AS_GUEST = "accessing_as_guest";
    private static final Log logger = LogFactory
            .getLog(AuthenticationFilter.class);
    /**
     * list of valid JSPs for redirect after a clean login
     */
    // TODO: make this list configurable
    private static final Set<String> circabcValidRedirectJSPs = new HashSet<>();

    static {
        circabcValidRedirectJSPs.add("/jsp/extension/welcome.jsp");
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
//	public void init(final FilterConfig config) throws ServletException {
//		this.context = config.getServletContext();
//	}

    private String loginPage = null;

    /**
     * Redirect to the Login page - saving the current URL which can be redirected back later once the
     * user has successfully completed the authentication process.
     */
    public static void redirectToLoginPage(final HttpServletRequest req,
                                           final HttpServletResponse res, final ServletContext sc)
            throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info("**** redirectToLoginPage is Called ****");
        }
        // authentication failed - so end servlet execution and redirect to
        // login page
        res.sendRedirect(req.getContextPath() + BaseServlet.FACES_SERVLET
                + Application.getLoginPage(sc));

        // save the full requested URL so the login page knows where to redirect
        // too later
        final String uri = req.getRequestURI();
        String url = uri;
        if (req.getQueryString() != null && req.getQueryString().length() != 0) {
            url += "?" + req.getQueryString();
        }
        if (uri.contains(req.getContextPath() + BaseServlet.FACES_SERVLET)) {
            // if we find a JSF servlet reference in the URI then we need to
            // check if the rest of the
            // JSP specified is valid for a redirect operation after Login has
            // occured.
            int jspIndex = uri.indexOf(BaseServlet.FACES_SERVLET)
                    + BaseServlet.FACES_SERVLET.length();

            if (uri.length() > jspIndex
                    && (BaseServlet.validRedirectJSP(uri.substring(jspIndex)) || circabcValidRedirectJSPs
                    .contains(uri.substring(jspIndex)))) {
                req.getSession()
                        .setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL, url);
            }
        } else {
            req.getSession().setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL, url);
        }
    }

    /**
     * Migration 3.1 -> 3.4.6 - 06/12/2011 Added the method inherited from DependencyInjectedFilter
     *
     * @see org.alfresco.web.app.servlet.AuthenticationFilter#doFilter(javax.servlet.ServletContext,
     * javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletContext context, ServletRequest req,
                         ServletResponse res, FilterChain chain) throws IOException,
            ServletException {

        if (logger.isInfoEnabled()) {
            logger.info("**** AuthenticationFilter Filter Called ****");
        }
        final HttpServletRequest httpReq = (HttpServletRequest) req;
        final HttpServletResponse httpRes = (HttpServletResponse) res;

        httpReq.setCharacterEncoding("UTF-8");
        httpRes.setContentType("text/html; charset=" + "UTF-8");

        // allow the login page to proceed
        if (!httpReq.getRequestURI().endsWith(getLoginPage(context))) {

            AuthenticationStatus status = null;

            // see if a ticket or a force Guest parameter has been supplied
            final String ticket = req.getParameter(ARG_TICKET);
            boolean wasNotGuest = false;

            if (ticket != null && ticket.length() != 0) {
                status = AuthenticationHelper.authenticate(context,
                        httpReq, httpRes, ticket);
            } else {
                final Cookie cookie = AuthenticationHelper
                        .getAuthCookie(httpReq);

                if (cookie != null && cookie.getValue() != null) {
                    wasNotGuest = !cookie.getValue().equals(
                            CircabcConstant.GUEST_AUTHORITY);
                }

                boolean forceGuest = false;
                final String guest = req
                        .getParameter(AuthenticationFilter.ARG_GUEST);
                if (guest != null) {
                    forceGuest = Boolean.parseBoolean(guest);
                }

                status = AuthenticationHelper.authenticate(context,
                        httpReq, httpRes, forceGuest);
            }

            if (status == AuthenticationStatus.Failure) {
                // authentication failed - now need to display the login page to
                // the user, if asked to
                status = AuthenticationHelper.authenticate(context,
                        httpReq, httpRes, true);

                if (wasNotGuest) {
                    final String authAsGuest = Application.getBundle(
                            httpReq.getSession()).getString(MSG_AUTH_AS_GUEST);
                    ErrorsRenderer.addForcedMessage(new FacesMessage(
                            authAsGuest));
                }
            }
        }

        // continue filter chaining
        chain.doFilter(req, res);
    }

//	private ServletContext context;

    /**
     * @return The login page url
     */
    private String getLoginPage(ServletContext context) {
        if (this.loginPage == null) {
            this.loginPage = Application.getLoginPage(context);
        }

        return this.loginPage;
    }
}
