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

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.app.servlet.AuthenticationFilter;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.ui.common.renderer.ErrorsRenderer;
import eu.cec.digit.circabc.web.wai.app.WaiApplication;
import eu.cec.digit.circabc.web.wai.bean.LoginBean;
import eu.cec.digit.circabc.web.wai.dialog.users.ViewUserDetailsDialog;
import eu.cec.digit.circabc.web.wai.manager.DialogManager;
import net.sf.acegisecurity.Authentication;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.app.AlfrescoNavigationHandler;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.alfresco.web.config.DialogsConfigElement;
import org.alfresco.web.config.DialogsConfigElement.DialogConfig;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.*;

/**
 * Extension of the Alfresco external access servlet to redirect user to the wai browsing.
 *
 * @author Yanick Pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring.
 */
public class ExternalAccessServlet extends org.alfresco.web.app.servlet.ExternalAccessServlet {

    public static final String OUTCOME_USER_DETAILS = "userdetails";
    public static final String MSG_BAD_URL = "browse_url_invalid";
    public static final String MSG_GUEST_NOT_ALLOWED = "browse_guest_not_allow";
    public static final String MSG_LOGIN_BUT_NOT_ALLOWED = "browse_login_but_not_allow";
    public static final String WAI_WELCOME_PAGE = "/jsp/extension/welcome.jsp";
    private static final long serialVersionUID = -5229018031448247913L;
    private static final Log logger = LogFactory.getLog(ExternalAccessServlet.class);
    private static final String WAI_EXTERNAL_URL = "/w/{0}";
    private static final String WAI_EXTERNAL_URL_ARGS = "/w/{0}/{1}";

    /**
     * Generate a URL to the External Access Servlet. Allows access to JSF views (via an "outcome" ID)
     * from external URLs.
     *
     * @return URL
     */
    public static String generateRelativeWaiExternalURL(final String outcome, final String args) {
        if (args == null) {
            return MessageFormat.format(WAI_EXTERNAL_URL, outcome);
        } else {
            return MessageFormat.format(WAI_EXTERNAL_URL_ARGS, outcome, args);
        }
    }

    /**
     * Generate a URL to the External Access Servlet and perefix that with the server address. Allows
     * access to JSF views (via an "outcome" ID) from external URLs.
     *
     * @return URL
     */
    public static String generateFullWaiExternalURL(final String outcome, final String args) {
        return getServerContext() + generateRelativeWaiExternalURL(outcome, args);
    }

    /**
     * Generate a URL to the External Access Servlet and perefix that with the server address. Allows
     * access to JSF views (via an "outcome" ID) from external URLs.
     *
     * @return URL
     */
    public static String generateFullExternalURL(final String outcome, final String args) {
        return getServerContext() + generateExternalURL(outcome, args);
    }

    public static String getServerContext() {
        return CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);
    }

    /**
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    protected void service(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {
        String uri = req.getRequestURI();

        Principal userPrincipal = req.getUserPrincipal();

        if (logger.isDebugEnabled()) {
            logger.debug("Processing UR: " + uri + (req.getQueryString() != null
                    ? ("?" + req.getQueryString()) : ""));
        }

        String userName = AuthenticationUtil.getFullyAuthenticatedUser();

        if (userPrincipal == null && (userName == null || userName.equals(
                AuthenticationUtil.getGuestUserName()))) {

            // Authenticate guest if the principal is null (no user logged in)
            final AuthenticationStatus status = servletAuthenticate(req, res);

            if (AuthenticationStatus.Failure.equals(status)) {
                return;
            }
        }
        // If no user is logged in, authenticate to the principal that's
        // logged into ECAS
        else if (userPrincipal != null && (userName == null || userName.equals(
                AuthenticationUtil.getGuestUserName()))) {

            // First clear security context to avoid conflicts
            AuthenticationUtil.clearCurrentSecurityContext();

            Authentication authentication = AuthenticationUtil.
                    setFullyAuthenticatedUser(userPrincipal.getName());

            // If we couldn't authenticate, redirect to the login page
            if (!authentication.isAuthenticated()) {
                redirectToLogin(req, res);
                return;
            }
        }

        setNoCacheHeaders(res);

        uri = uri.substring(req.getContextPath().length());
        StringTokenizer t = new StringTokenizer(uri, "/");
        final int tokenCount = t.countTokens();
        if (tokenCount < 2) {
            throw new IllegalArgumentException(
                    "Externally addressable URL did not contain all required args: " + uri);
        }

        t.nextToken();    // skip servlet name

        final String outcome = t.nextToken();

        // get rest of the tokens arguments
        final String[] args = new String[tokenCount - 2];
        for (int i = 0; i < tokenCount - 2; i++) {
            args[i] = t.nextToken();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("External outcome found: " + outcome);
        }

        // we almost always need this bean reference
        final FacesContext fc = FacesHelper.getFacesContext(req, res, getServletContext());

        final Stack<String> stack = CircabcNavigationHandler.getViewStack(fc);
        if (stack != null) {
            stack.clear();
        }

        final PermissionService permissionService = Services
                .getAlfrescoServiceRegistry(getServletContext()).getPermissionService();
        final NodeService nodeService = Services.getAlfrescoServiceRegistry(getServletContext())
                .getNodeService();
        final CircabcNavigationBean navigator = Beans.getWaiNavigator();
        final CircabcBrowseBean browser = Beans.getWaiBrowseBean();
        //NavigationHandler navigationHandler = fc.getApplication().getNavigationHandler();

        // setup is required for certain outcome requests
        if (OUTCOME_DOCDETAILS.equals(outcome) || OUTCOME_BROWSE.equals(outcome) || OUTCOME_USER_DETAILS
                .equals(outcome)) {
            NodeRef nodeRef = null;
            final boolean isDialog = OUTCOME_USER_DETAILS.equals(outcome);

            if (args != null && args.length == 1) {
                final StoreRef spaceStore = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
                nodeRef = new NodeRef(spaceStore, args[0]);
            } else {
                Utils.addErrorMessage(Application.getMessage(fc, MSG_BAD_URL));
            }

            if (!isDialog && nodeRef == null) {
                nodeRef = getCurrentNode(navigator);
            }

            String redirectPage = req.getContextPath() + BaseServlet.FACES_SERVLET;

            if (isDialog) {
                // set the external container session flag so that a plain container gets used
                fc.getExternalContext().getSessionMap().put(
                        AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION, Boolean.TRUE);

                redirectPage += CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE;
                fc.getViewRoot().setViewId(CircabcNavigationHandler.WAI_DIALOG_CONTAINER_PAGE);

                // init the return location being to the current node
                CircabcNavigationHandler.getViewStack(fc).push(new CircabcNavigationHandler.StackElement(
                        getCurrentNode(navigator).getId(),
                        CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE
                ));

                // init and launch dialog
                final DialogManager dialogManager = WaiApplication.getDialogManager();
                final Map<String, String> parameters = Collections
                        .singletonMap("id", nodeRef == null ? "" : nodeRef.getId());
                ((ViewUserDetailsDialog) Beans.getBean(ViewUserDetailsDialog.BEAN_NAME)).init(parameters);
                dialogManager.setCurrentDialog(getDialogConfig(fc, ViewUserDetailsDialog.DIALOG_NAME));

            }

            // Only for outcome "browse". Checks if we have basic read access on
            // the resource, which means that if the resource already has basic
            // read access for the current user, it is displayed (even if more
            // rights would grant more access)
            // If we have no access, ECAS authentication is requested and
            // afterwards, redirected. If we still have no access after
            // being authenticated, it assumes that we are not granted access.
            // It also checks the user principal that is filled by ECAS
            // authentication to know if we must authenticate
            else if (OUTCOME_BROWSE.equals(outcome) && userPrincipal == null &&
                    (userName == null || userName.equals(
                            AuthenticationUtil.getGuestUserName())) &&
                    permissionService.hasPermission(nodeRef,
                            PermissionService.READ) == AccessStatus.DENIED) {

                redirectToLogin(req, res);

                return;
            } else if (OUTCOME_BROWSE.equals(outcome) && userName != null &&
                    !userName.equals(AuthenticationUtil.getGuestUserName()) &&
                    permissionService.hasPermission(nodeRef,
                            PermissionService.READ) == AccessStatus.DENIED) {

                Utils.addErrorMessage(Application.getMessage(fc,
                        MSG_LOGIN_BUT_NOT_ALLOWED));

                redirectPage += WAI_WELCOME_PAGE;
            } else if (permissionService.hasPermission(nodeRef,
                    PermissionService.READ) == AccessStatus.ALLOWED) {

                redirectPage = grantAccess(fc, browser, nodeRef, redirectPage);

                // Category needs to redirect after login to force a refresh
                if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef,
                        CircabcModel.ASPECT_CATEGORY) &&
                        userPrincipal == null) {

                    HttpSession session = req.getSession();

                    session.setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL,
                            req.getRequestURI());
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("The Guest user have permissions to READ NodeRef: " + nodeRef.toString());
                }

                if (isGuest()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The user is not connected. Redirect him to the login page.");
                    }

                    navigator.setCurrentNodeId(nodeRef.getId());

                    Utils.addErrorMessage(Application.getMessage(fc, MSG_GUEST_NOT_ALLOWED));

                    redirectPage += LoginBean.WAI_LOGIN_PAGE;
                }
            }

            // mem the messages to set it in the new created context by the redirection.
            final Iterator<FacesMessage> messages = fc.getMessages();
            if (messages.hasNext()) {
                ErrorsRenderer.addForcedMessage(messages);
            }

            res.sendRedirect(redirectPage);
        } else {
            ErrorsRenderer.addForcedMessage(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, Application.getMessage(fc, MSG_BAD_URL),
                            Application.getMessage(fc, MSG_BAD_URL)));
            res.sendRedirect(req.getContextPath() + BaseServlet.FACES_SERVLET
                    + CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private String grantAccess(final FacesContext fc,
                               final CircabcBrowseBean browser, NodeRef nodeRef,
                               String redirectPage) {

        // set the external container session flag so that a plain container gets used
        fc.getExternalContext().getSessionMap().put(
                AlfrescoNavigationHandler.EXTERNAL_CONTAINER_SESSION, Boolean.TRUE);
        // the user is autenticated. Let the browse bean to manage the access...
        browser.clickWai(nodeRef);

        redirectPage += CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE;

        fc.getViewRoot().setViewId(CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE);

        return redirectPage;
    }

    private void redirectToLogin(final HttpServletRequest req,
                                 final HttpServletResponse res) throws IOException {

        HttpSession session = req.getSession();

        session.setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL,
                req.getRequestURI());

        redirectToLoginPage(req, res, getServletContext());
    }

    /**
     * @param navigator
     * @return
     */
    private NodeRef getCurrentNode(final CircabcNavigationBean navigator) {
        NodeRef nodeRef;
        // simply refresh the browsing...
        if (navigator.getCurrentNodeId() != null) {
            // go to the last visted page
            nodeRef = navigator.getCurrentNode().getNodeRef();
        } else {
            // go to the welcome page
            nodeRef = navigator.getCircabcHomeNode().getNodeRef();
        }
        return nodeRef;
    }

    public AuthenticationStatus servletAuthenticate(HttpServletRequest req, HttpServletResponse res,
                                                    boolean redirectToLoginPage) throws IOException {
        AuthenticationStatus status;

        // see if a ticket or a force Guest parameter has been supplied
        String ticket = req.getParameter(AuthenticationFilter.ARG_TICKET);
        boolean wasNotGuest = false;

        if (ticket != null && ticket.length() != 0) {
            status = AuthenticationHelper.authenticate(getServletContext(), req, res, ticket);
        } else {
            final Cookie cookie = AuthenticationHelper.getAuthCookie(req);

            if (cookie != null && cookie.getValue() != null) {
                wasNotGuest = !cookie.getValue().equals(CircabcConstant.GUEST_AUTHORITY);
            }

            boolean forceGuest = cookie == null;
            String guest = req.getParameter(AuthenticationFilter.ARG_GUEST);
            if (guest != null) {
                forceGuest = Boolean.parseBoolean(guest);
            }

            status = AuthenticationHelper.authenticate(getServletContext(), req, res, forceGuest);
        }

        if (status == AuthenticationStatus.Failure && redirectToLoginPage) {
            // authentication failed - now need to display the login page to the user, if asked to
            status = AuthenticationHelper.authenticate(getServletContext(), req, res, true);

            if (wasNotGuest) {
                final String authAsGuest = Application.getBundle(req.getSession())
                        .getString(AuthenticationFilter.MSG_AUTH_AS_GUEST);
                ErrorsRenderer.addForcedMessage(new FacesMessage(authAsGuest));
            }
        }

        return status;
    }

    protected boolean isGuest() {
        return AuthenticationUtil.getGuestUserName()
                .equals(Beans.getWaiNavigator().getCurrentUser().getUserName());
    }

    protected DialogConfig getDialogConfig(FacesContext context, String name) {
        DialogConfig dialogConfig = null;
        final ConfigService configSvc = Application.getConfigService(context);

        final Config config = configSvc.getGlobalConfig();

        if (config != null) {
            DialogsConfigElement dialogsCfg = (DialogsConfigElement) config
                    .getConfigElement(DialogsConfigElement.CONFIG_ELEMENT_ID);
            if (dialogsCfg != null) {
                dialogConfig = dialogsCfg.getDialog(name);
            }
        }
        return dialogConfig;
    }

}
