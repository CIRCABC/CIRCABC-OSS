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

import eu.cec.digit.circabc.CircabcConfig;
import eu.cec.digit.circabc.aspect.DisableNotificationThreadLocal;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.web.auth.TicketCredentials;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.repo.webdav.WebDAV;
import org.alfresco.repo.webdav.auth.AuthenticationFilter;
import org.alfresco.repo.webdav.auth.WebDAVUser;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

/**
 * WebDAV Authentication Filter Class
 *
 * @author Migration 3.1 -> 3.4.6 - 05/12/2011 Class was converted to its 3.4.x version:
 * org.alfresco.repo.webdav.auth.AuthenticationFilter
 */
public class CircabcWebdavAuthenticationFilter extends AuthenticationFilter implements
        DependencyInjectedFilter {

    // Authenticated user session object name
    public static final String AUTHENTICATION_USER = "_alfDAVAuthTicket";
    // WebDAV user-agent header must start with Mozilla for a compatible Mozilla browser
    public static final String MOZILLA_WEB_BROWSER = "Mozilla";
    // Init parameter names
    public static final String KEY_STORE = "store";
    public static final String KEY_ROOT_PATH = "rootPath";
    // Debug logging
    private static Log logger = LogFactory.getLog(CircabcWebdavAuthenticationFilter.class);
    // Allow an authentication ticket to be passed as part of a request to bypass authentication
    private SearchService searchService = null;
    private NamespaceService namespaceService = null;
    private PermissionService permissionService = null;
    private FileFolderService fileFolderService = null;
    private NodeRef rootNodeRef = null;

    private String store = null;
    private String rootPath = null;

    private String ecasLoginPage = null, loginPage = null;
    private String webdavRootPath = "/webdav";

    private String guestDisabledForLibrary = "false";

    /**
     * Initialize the filter
     */
    public void init() throws ServletException {
        // Initialize the root node
        rootNodeRef = getRootNode();
    }

    /**
     * Run the authentication filter
     *
     * @param req   ServletRequest
     * @param resp  ServletResponse
     * @param chain FilterChain
     */
    public void doFilter(ServletContext context, ServletRequest req,
                         ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        // disable notification for webdav
        DisableNotificationThreadLocal disableNotificationThreadLocal = new DisableNotificationThreadLocal();
        disableNotificationThreadLocal.set(true);

        // Assume it's an HTTP request
        final HttpServletRequest httpReq = (HttpServletRequest) req;
        final HttpServletResponse httpResp = (HttpServletResponse) resp;

        String webdavUrl = httpReq.getRequestURI();

        String authHdr = httpReq.getHeader("Authorization");

        if (authHdr != null && authHdr.length() > 5 && authHdr.substring(0, 5)
                .equalsIgnoreCase("BASIC")) {
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
                    this.authenticationService.validate(ticket);
                    this.authenticationListener.userAuthenticated(new TicketCredentials(ticket));
                    chain.doFilter(req, resp);
                    return;
                }

            } else {
                super.doFilter(context, req, resp, chain);
                return;
            }
        }

        String ticketdav = req.getParameter(ARG_TICKET);

        if (ticketdav != null && ticketdav.length() > 0) {
            this.authenticationService.validate(ticketdav);
            this.authenticationListener.userAuthenticated(new TicketCredentials(ticketdav));
            chain.doFilter(req, resp);
            return;
        }

        int posTK = webdavUrl.indexOf(NavigableNode.NEW_TICKET_PREFIX);

        // If the request URI includes a ticket parameter, wrap the request into
        // a new one that returns that ticket as a parameter when passed to
        // the superclass
        if (posTK != -1) {

            // Set notifications for Edit in Office
            disableNotificationThreadLocal.set(false);

            String firstPart = webdavUrl.substring(0, posTK);

            String secondPart = webdavUrl.substring(posTK +
                    NavigableNode.NEW_TICKET_PREFIX.length());

            int posEndTicket = secondPart.indexOf("/");

            final String ticket = "TICKET_" + secondPart.substring(0, posEndTicket);

            final String newRequestURI = firstPart + secondPart.substring(posEndTicket);

            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpReq) {

                /**
                 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
                 */
                @Override
                public String getParameter(String name) {
                    if ("ticket".equals(name)) {
                        return ticket;
                    }
                    return super.getParameter(name);
                }

                /**
                 * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
                 */
                @Override
                public String getRequestURI() {
                    return newRequestURI;
                }
            };

            // Set response header to avoid caching in IE
            httpResp.setHeader("Cache-Control", "no-cache, no-store");
            httpResp.setHeader("Pragma", "no-cache");

            super.doFilter(context, wrapper, resp, chain);
            disableNotificationThreadLocal.remove();
            return;
        }

        String agent = httpReq.getHeader(WebDAV.HEADER_USER_AGENT);

        // If it is not a web browser, redirect to original Alfresco WebDAV filter
        if (agent != null && !agent.trim().startsWith(MOZILLA_WEB_BROWSER)) {
            super.doFilter(context, req, resp, chain);
            disableNotificationThreadLocal.remove();
            return;
        }

        HttpSession session = httpReq.getSession();

        // Get the user details object from the session
        SessionUser user = checkHttpUser(context, httpReq, httpResp);
        //SessionUser user = getSessionUser(context, httpReq, httpResp, false);

        if (user == null) {

            String pathInfo = httpReq.getPathInfo();
            pathInfo = pathInfo == null ? "" : pathInfo;

            String guest = AuthenticationUtil.getGuestUserName();

            // If guest is allowed, just continue authenticating guest
            if (userAllowedOnPath(pathInfo, guest,
                    new String[]{PermissionService.READ_CONTENT})) {

                // Authenticate the user
                authenticationService.authenticateAsGuest();

                // Setup User object and Home space ID etc.
                user = createUserEnvironment(httpReq.getSession(),
                        authenticationService.getCurrentUserName(),
                        authenticationService.getCurrentTicket(), false);
            }
            // User guest is not allowed, so redirect to the ecasLogin page to
            // authenticate through ECAS
            else {

                String contextPath = httpReq.getContextPath();

                // Set the URL of the webdav resource to access. The system will
                // redirect to this URL later, after authenticating through ECAS
                session.setAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL,
                        contextPath + webdavRootPath + pathInfo);

                FacesContext facesContext = FacesContext.getCurrentInstance();

                // The services called after needs to have a configured
                // FacesContext.
                if (facesContext == null) {
                    FacesHelper.getFacesContext(req, resp, context);
                    facesContext = FacesContext.getCurrentInstance();
                }

                if (CircabcConfig.ENT) {
                    // Redirect to ECAS authentication
                    httpResp.sendRedirect(contextPath + ecasLoginPage);
                } else {
                    httpResp.sendRedirect(contextPath + loginPage);
                }

                facesContext.responseComplete();
                disableNotificationThreadLocal.remove();
                return;
            }
        }

        // Chain other filters
        chain.doFilter(req, resp);
        disableNotificationThreadLocal.remove();
    }

    /**
     * Checks if the given user has allowed access to the given path.
     *
     * @param path            The path to check.
     * @param userName        The user to check against.
     * @param permissionNames Names of the permissions to check.
     */
    private boolean userAllowedOnPath(String path, String userName,
                                      final String[] permissionNames) {

        if (userName == null || userName.isEmpty()) {
            return false;
        }

        List<String> pathElements = Arrays.asList(StringUtils.split(
                (path == null) ? "" : path, '/'));
        NodeRef nodeRef = null;

        if (pathElements.isEmpty()) {
            nodeRef = rootNodeRef;
        } else {
            try {
                nodeRef = fileFolderService.resolveNamePath(rootNodeRef,
                        pathElements).getNodeRef();
            } catch (FileNotFoundException e) {
                // Path not found, nothing to check permissions for
                return false;
            }
        }

        if (nodeRef != null) {

            final NodeRef finalNodeRef = nodeRef;

            if (userName.equals(AuthenticationUtil.getGuestUserName()) && this.guestDisabledForLibrary.equals("true")) {
                if (hasLibararyOrNewsGroupAspect(finalNodeRef)) {
                    return false;
                }
            }

            return AuthenticationUtil.runAs(
                    new AuthenticationUtil.RunAsWork<Boolean>() {

                        public Boolean doWork() {

                            boolean allowed = true;

                            for (int idx = 0; idx < permissionNames.length && allowed;
                                 idx++) {

                                AccessStatus status = permissionService.hasPermission(
                                        finalNodeRef, permissionNames[idx]);

                                allowed &= status == AccessStatus.ALLOWED;
                            }

                            return allowed;
                        }

                    }, userName);
        }

        return false;
    }

    private boolean hasLibararyOrNewsGroupAspect(NodeRef nodeRef) {
        final NodeRef finalNodeRef = nodeRef;
        return AuthenticationUtil.runAs(
                new AuthenticationUtil.RunAsWork<Boolean>() {

                    public Boolean doWork() {

                        boolean result = true;

                        result = nodeService.hasAspect(finalNodeRef, CircabcModel.ASPECT_LIBRARY) || nodeService.hasAspect(finalNodeRef, CircabcModel.ASPECT_NEWSGROUP);

                        return result;
                    }

                }, AuthenticationUtil.SYSTEM_USER_NAME);

    }

    private WebDAVUser checkHttpUser(final ServletContext servletContext,
                                     final HttpServletRequest httpReq, final HttpServletResponse httpResp) {

//        SessionUser user = getSessionUser(context, httpReq, httpResp, false);

        // Get the user details object from the session
        AuthenticationStatus status = null;

        try {
            status = AuthenticationHelper.authenticate(servletContext, httpReq, httpResp, false, false);
        } catch (final IOException e) {
            logger.error("Unable to retrieve already authenticated user.", e);
            return null;
        }

        // ECAS component stores the user principal
        final Principal userPrincipal = httpReq.getUserPrincipal();

        if ((userPrincipal != null) || (status == AuthenticationStatus.Success)) {
            UserTransaction tx = null;
            try {
                // Validate the ticket
                final String ticket = authenticationService.getCurrentTicket();
                authenticationService.validate(ticket);

                // Need to create the User instance if not already available
                final String currentUsername = authenticationService.getCurrentUserName();

                // Start a transaction
                tx = transactionService.getUserTransaction();
                tx.begin();

                final NodeRef personRef = personService.getPerson(currentUsername);
                final NodeRef homeRef = (NodeRef) nodeService
                        .getProperty(personRef, ContentModel.PROP_HOMEFOLDER);

                // Check that the home space node exists - else Login cannot proceed
                if (nodeService.exists(homeRef) == false) {
                    throw new InvalidNodeRefException(homeRef);
                }

                final WebDAVUser webDavUser = new WebDAVUser(currentUsername,
                        authenticationService.getCurrentTicket(), homeRef);
                tx.commit();
                tx = null;

                // Store the User object in the Session - the authentication servlet will then proceed
                httpReq.getSession().setAttribute(AUTHENTICATION_USER, webDavUser);
                return webDavUser;
            } catch (final Throwable e) {
                // Clear the user object to signal authentication failure

                //user = null;
            } finally {
                try {
                    if (tx != null) {
                        tx.rollback();
                    }
                } catch (final Exception tex) {
                }
            }
        }
        //userPrincipal == null) && status != AuthenticationStatus.Success
        // or Exception
        return null;
    }

    /**
     * Cleanup filter resources
     */
    public void destroy() {
        // Nothing to do
    }

    public final List<String> splitAllPaths(final String path) {
        if (path == null || path.length() == 0) {
            return Collections.emptyList();
        }
        // split the path
        final StringTokenizer token = new StringTokenizer(path, "/");
        final List<String> results = new ArrayList<>(10);
        while (token.hasMoreTokens()) {
            results.add(token.nextToken());
        }
        return results;
    }

    /**
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @return the loginPage
     */
    public String getLoginPage() {
        return loginPage;
    }

    /**
     * @param loginPage the loginPage to set
     */
    public void setLoginPage(String loginPage) {
        this.loginPage = loginPage;
    }

    /**
     * @return the ecasLoginPage
     */
    public String getEcasLoginPage() {
        return ecasLoginPage;
    }

    /**
     * @param ecasLoginPage the ecasLoginPage to set
     */
    public void setEcasLoginPage(String ecasLoginPage) {
        this.ecasLoginPage = ecasLoginPage;
    }

    /**
     * @param webdavRootPath the webdavRootPath to set
     */
    public void setWebdavRootPath(String webdavRootPath) {
        this.webdavRootPath = webdavRootPath;
    }

    /**
     * @param store the store to set
     */
    public void setStore(String store) {
        this.store = store;
    }

    /**
     * @param rootPath the rootPath to set
     */
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * @see org.alfresco.repo.webdav.auth.BaseAuthenticationFilter#getLogger()
     */
    protected Log getLogger() {
        return logger;
    }

    /**
     * Migration 3.1 -> 3.4.6 - 05/12/2011 This method was originally provided by the WebdavServlet of
     * Alfresco 3.1 but in 3.4.x does not exist anymore. So, this implementation was taken from
     * Alfresco 3.1 source code and added here to provide the same functionality.
     */
    public NodeRef getRootNode() throws ServletException {

        RunAsWork<NodeRef> runAsWork = new RunAsWork<NodeRef>() {

            @Override
            public NodeRef doWork() throws Exception {

                // Initialize the root node
                NodeRef m_rootNodeRef = null;

                // Wrap the initialization in a transaction
                UserTransaction tx = transactionService.getUserTransaction(true);

                try {
                    // Start the transaction

                    if (tx != null) {
                        tx.begin();
                    }

                    // Get the store
                    if (store == null) {
                        throw new ServletException("Device missing init value: " + KEY_STORE);
                    }
                    StoreRef storeRef = new StoreRef(store);

                    // Connect to the repo and ensure that the store exists

                    if (!nodeService.exists(storeRef)) {
                        throw new ServletException(
                                "Store not created prior to application startup: " + storeRef);
                    }
                    NodeRef storeRootNodeRef = nodeService.getRootNode(storeRef);

                    // Check the root path
                    if (rootPath == null) {
                        throw new ServletException("Device missing init value: " + KEY_ROOT_PATH);
                    }

                    // Find the root node for this device
                    List<NodeRef> nodeRefs = searchService.selectNodes(storeRootNodeRef,
                            rootPath, null, namespaceService, false);

                    if (nodeRefs.size() > 1) {
                        throw new ServletException("Multiple possible roots for device: \n" +
                                "   root path: " + rootPath + "\n" +
                                "   results: " + nodeRefs);
                    } else if (nodeRefs.size() == 0) {
                        // nothing found
                        throw new ServletException("No root found for device: \n" +
                                "   root path: " + rootPath);
                    } else {
                        // we found a node
                        m_rootNodeRef = nodeRefs.get(0);
                    }

                    // Commit the transaction
                    tx.commit();
                } catch (Exception ex) {
                    logger.error("Error during getting root node", ex);
                }

                return m_rootNodeRef;
            }
        };

        // Use the system user as the authenticated context for the filesystem initialization
        return AuthenticationUtil.runAs(runAsWork, AuthenticationUtil.SYSTEM_USER_NAME);
    }

    public String getGuestDisabledForLibrary() {
        return guestDisabledForLibrary;
    }

    public void setGuestDisabledForLibrary(String guestDisabledForLibrary) {
        this.guestDisabledForLibrary = guestDisabledForLibrary;
    }
}
