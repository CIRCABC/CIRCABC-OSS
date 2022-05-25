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

import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.User;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet filter responsible for redirecting to the login page for the Web Client if the user does
 * not have a valid ticket.
 * <p>
 * The current ticker is validated for each page request and the login page is shown if the ticker
 * has expired.
 * <p>
 * <p>
 * It overrides the AuthenticationFilter by letting ecaslogin.jsp pass the filter
 *
 * @author atadian
 */
public class EcasAuthenticationFilter extends
        eu.cec.digit.circabc.web.app.servlet.AuthenticationFilter {

    private static final String FACES_JSP_EXTENSION_SESSION_EXPIRED_JSP = "/faces/jsp/extension/session_expired.jsp";

    private static final String SESSION_EXPIRED_JSP = "session_expired.jsp";
    /**
     * The logger class*
     */
    private static final Log logger = LogFactory
            .getLog(EcasAuthenticationFilter.class);
    /**
     * The ecas login page URL *
     */
    private String ecasLoginPage = null;
    private boolean enableGuestSessionTimeout = false;
    private int guestSessionTimeoutInMinutes = 10;

    /** The filter config to read some parameters of the filter* */
//     private FilterConfig filterConfig;
    // default value shoud be same like session-timeout  in web.xml
    private int originalSessionTimeoutInMinutes = 30;

    /**
     * Migration 3.1 -> 3.4.6 - 06/12/2011 Added the method inherited from DependencyInjectedFilter
     *
     * @see eu.cec.digit.circabc.web.app.servlet.AuthenticationFilter#doFilter(javax.servlet.ServletContext,
     * javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletContext context, ServletRequest req,
                         ServletResponse res, FilterChain chain) throws IOException,
            ServletException {

        final HttpServletRequest httpReq = (HttpServletRequest) req;
        final HttpServletResponse httpRes = (HttpServletResponse) res;
        final String requestURI = httpReq.getRequestURI();

        // The services called after need to have a configured FacesContext.
        // Ensure its construction.
        if (FacesContext.getCurrentInstance() == null) {
            FacesHelper.getFacesContext(req, res, context);
        }

        // allow session expire page
        if (StringUtils.contains(requestURI, SESSION_EXPIRED_JSP)) {
            chain.doFilter(req, res);
            return;
        } else {
            // only get session do not create
            final HttpSession session = httpReq.getSession(false);
            if (session == null) {
                final boolean isSessionInvalid =
                        (httpReq.getRequestedSessionId() != null) && !httpReq.isRequestedSessionIdValid();
                if (isSessionInvalid) {
                    httpRes.sendRedirect(httpReq.getContextPath() + FACES_JSP_EXTENSION_SESSION_EXPIRED_JSP);
                    return;
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("filtering " + requestURI);
        }

        // Allow the Alfresco Login page to continue (login through Alfresco interface)
        // Allow the ECAS Login page to continue (login through ECAS)
        if (requestURI.endsWith("login.jsp") || requestURI.endsWith(ecasLoginPage)) {
            // continue filter chaining
            chain.doFilter(req, res);
            AuthenticationUtil.clearCurrentSecurityContext();
        }
        // Showing CIRCABC interface
        else {

            final boolean forceGuest =
                    AuthenticationHelper.getUser((ServletContext) context, httpReq, httpRes) == null;

            final AuthenticationStatus status =
                    AuthenticationHelper.authenticate(context, httpReq, httpRes, forceGuest, true);

            if (status == AuthenticationStatus.Success || status == AuthenticationStatus.Guest) {
                // continue filter chaining
                chain.doFilter(req, res);
            } else {
                // authentication failed - so end servlet execution and redirect to login page
                // also save the requested URL so the login page knows where to redirect to later
                AuthenticationHelper.authenticate(context, httpReq, httpRes, true);

                final String authAsGuest = Application.getBundle(httpReq.getSession())
                        .getString(MSG_AUTH_AS_GUEST);
                ErrorsRenderer.addForcedMessage(new FacesMessage(authAsGuest));
                chain.doFilter(req, res);
            }
        }

        if (!enableGuestSessionTimeout) {
            return;
        }

        // Sets the session timeout according to the original value for
        // logged users and sessionTimeoutInMinutes for the guest user
        User user = null;

        try {
            user = AuthenticationHelper.getUser(context, httpReq, httpRes);
        } catch (IllegalStateException e) {
            // In this case the session got invalid because we have logged out
            // from the Alfresco interface (session does not exist anymore)
            logger.info("Logged out - Session invalidated.");
            return;
        }

        HttpSession session = httpReq.getSession(false);

        if (user != null && AuthenticationUtil.getGuestUserName().
                equals(user.getUserName()) && session != null) {
            session.setMaxInactiveInterval(60 * guestSessionTimeoutInMinutes);
        } else if (user != null && !AuthenticationUtil.getGuestUserName().
                equals(user.getUserName()) && session != null) {
            session.setMaxInactiveInterval(60 * originalSessionTimeoutInMinutes);
        } else if (user == null && session != null &&
                !requestURI.endsWith("login.jsp")) {
            session.invalidate();
        }
    }

    /**
     * Migration 3.1 -> 3.4.6 - 06/12/2011 Added this setter because the ecasLoginPage cannot be a
     * filter parameter anymore, since Alfresco changed the authentication structure by subsystems.
     *
     * @param ecasLoginPage the ecasLoginPage to set
     */
    public void setEcasLoginPage(String ecasLoginPage) {
        this.ecasLoginPage = ecasLoginPage;
    }

    /**
     * @param guestSessionTimeoutInMinutes the guestSessionTimeoutInMinutes to set
     */
    public void setGuestSessionTimeoutInMinutes(int guestSessionTimeoutInMinutes) {
        this.guestSessionTimeoutInMinutes = guestSessionTimeoutInMinutes;
    }

    /**
     * @param enableGuestSessionTimeout the enableGuestSessionTimeout to set
     */
    public void setEnableGuestSessionTimeout(boolean enableGuestSessionTimeout) {
        this.enableGuestSessionTimeout = enableGuestSessionTimeout;
    }

    public int getOriginalSessionTimeoutInMinutes() {
        return originalSessionTimeoutInMinutes;
    }

    public void setOriginalSessionTimeoutInMinutes(
            int originalSessionTimeoutInMinutes) {
        this.originalSessionTimeoutInMinutes = originalSessionTimeoutInMinutes;
    }


}
