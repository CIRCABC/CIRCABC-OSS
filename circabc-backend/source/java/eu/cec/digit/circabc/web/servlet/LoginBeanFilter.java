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

import eu.cec.digit.circabc.model.UserModel;
import eu.cec.digit.circabc.service.log.LogRecord;
import eu.cec.digit.circabc.service.log.LogService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import eu.cec.digit.ecas.client.jaas.DetailedUser;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.LoginOutcomeBean;
import org.alfresco.web.bean.repository.User;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;

public class LoginBeanFilter implements InitializingBean, DependencyInjectedFilter {

    private NodeService nodeService = null;
    private PersonService personService = null;


    private UserService userService = null;
    private ManagementService managementService = null;
    private LogService logService = null;

    private String dispatchUrl = null;

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.dispatchUrl == null) {
            throw new ServletException("Property 'dispatchUrl' not found.");
        }
    }

    /**
     * @see org.alfresco.repo.web.filter.beans.DependencyInjectedFilter#doFilter(javax.servlet.ServletContext,
     * javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletContext context, ServletRequest request,
                         ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        final LogRecord logRecord = new LogRecord();

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        final Principal userPrincipal = httpRequest.getUserPrincipal();
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
                
                if (!userService.getAuthenticationEnabled(userName)){
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

        session.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED);
        final String ticket = userService.getCurrentTicket(userName);
        final NodeRef nodeRef = personService.getPerson(userName);
        final User user = new User(userName, ticket, nodeRef);

        session.removeAttribute(AuthenticationHelper.AUTHENTICATION_USER);
        session.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);

        final String redirectURL = (String) session.getAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL);

        if (redirectURL != null) {
            // remove redirect URL from session
            session.removeAttribute(LoginOutcomeBean.PARAM_REDIRECT_URL);
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendRedirect(redirectURL);
            return;
        }

        logRecord.setOK(true);
        logService.log(logRecord);
        RequestDispatcher dispatcher = request.getRequestDispatcher(dispatchUrl);
        dispatcher.forward(request, response);
    }

    /**
     * @param dispatchUrl the dispatchUrl to set
     */
    public void setDispatchUrl(String dispatchUrl) {
        this.dispatchUrl = dispatchUrl;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @param userService the userService to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @param managementService the managementService to set
     */
    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @param logService the logService to set
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }
}
