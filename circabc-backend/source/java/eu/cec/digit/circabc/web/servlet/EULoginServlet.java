package eu.cec.digit.circabc.web.servlet;

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.ecas.client.jaas.DetailedUser;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.servlet.BaseServlet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;

public class EULoginServlet extends BaseServlet {

    private UserService userService;
    private PersonService personService;
    private NodeService nodeService;

    private ManagementService managementService;
    private LogService logService;

    @Override
    public void init() throws ServletException {

        super.init();

        personService = Services.getAlfrescoServiceRegistry(getServletContext()).getPersonService();

        nodeService = Services.getAlfrescoServiceRegistry(getServletContext()).getNodeService();

        logService = (LogService) Services.getCircabcServiceRegistry(getServletContext())
                .getService(CircabcServiceRegistry.NON_SECURED_LOG_SERVICE);

        managementService = (ManagementService) Services.getCircabcServiceRegistry(getServletContext())
                .getService(CircabcServiceRegistry.MANAGEMENT_SERVICE);

        userService = (UserService) Services.getCircabcServiceRegistry(getServletContext())
                .getService(CircabcServiceRegistry.CIRCABC_USER_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        final LogRecord logRecord = new LogRecord();

        final Principal userPrincipal = request.getUserPrincipal();
        String userName = userPrincipal.getName();
        logRecord.setService("Directory");
        logRecord.setActivity("Login");
        logRecord.setUser(userName);
        try {
            AuthenticationUtil.setRunAsUserSystem();
            if (!personService.personExists(userName)) {
                final CircabcUserDataBean user = new CircabcUserDataBean();
                user.setUserName(userName);

                final CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(userName);
                user.copyLdapProperties(ldapUserDetail);
                if (ldapUserDetail == null) {
                    if (userPrincipal instanceof DetailedUser) {
                        DetailedUser detailedUser = (DetailedUser) userPrincipal;
                        user.copyDetailedUserProperties(detailedUser);
                    }
                }
                user.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
                userService.createUser(user, true);
            } else {
                final CircabcUserDataBean ldapUserDetail = userService.getLDAPUserDataByUid(userName);
                if (ldapUserDetail != null) {
                    Date ldapTime = ldapUserDetail.getLastModificationDetailsTime();
                    if (ldapTime != null) {
                        final NodeRef nodeRef = personService.getPerson(userName);
                        Date repoTime = (Date) nodeService
                                .getProperty(nodeRef, UserModel.PROP_LAST_MODIFICATION_DETAILS_TIME);
                        if (repoTime == null || ldapTime.after(repoTime)) {
                            final CircabcUserDataBean repoUser = userService.getCircabcUserDataBean(userName);
                            repoUser.copyLdapProperties(ldapUserDetail);
                            userService.updateUser(repoUser);
                        }

                    }
                }
                if (!userService.getAuthenticationEnabled(userName)) {
                    userService.setAuthenticationEnabled(userName, true);
                }

            }
        } finally {
            AuthenticationUtil.setRunAsUser(userName);
        }

        try {
            AuthenticationUtil.setRunAsUserSystem();
            final NodeRef circabcNodeRef = managementService.getCircabcNodeRef();
            logRecord
                    .setIgID((Long) nodeService.getProperty(circabcNodeRef, ContentModel.PROP_NODE_DBID));
            logRecord.setIgName((String) nodeService.getProperty(circabcNodeRef, ContentModel.PROP_NAME));
        } finally {
            AuthenticationUtil.setRunAsUser(userName);
        }

        final String ticket = userService.getCurrentTicket(userName);

        logRecord.setOK(true);
        logService.log(logRecord);

        String route = request.getParameter("route");
        if (route == null) {
            route = "";
        }

        setCookie(response, "username", userName);

        setCookie(response, "ticket", ticket);

        setCookie(response, "route", route);

        response.sendRedirect(request.getContextPath() + "/ui/welcome");
    }

    private void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(120);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}