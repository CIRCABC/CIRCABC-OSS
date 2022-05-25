<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? as soon they
 will be approved by the European Commission - subsequent
 versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the
 Licence.
 You may obtain a copy of the Licence at:

 https://joinup.ec.europa.eu/software/page/eupl

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.
--%>
<%@page import="eu.cec.digit.circabc.service.cmr.security.CircabcConstant"%>
<%@page import="org.apache.commons.logging.LogFactory"%>


<%@ page import="org.apache.commons.logging.LogFactory"%>
<%@ page import="eu.cec.digit.circabc.web.wai.bean.LoginBean"%>
<%@ page import="eu.cec.digit.circabc.web.Services"%>
<%@ page import="org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback"%>
<%@ page import="org.alfresco.repo.transaction.RetryingTransactionHelper"%>
<%@ page import="eu.cec.digit.circabc.web.app.CircabcNavigationHandler"%>
<%@ page import="eu.cec.digit.circabc.service.cmr.security.CircabcConstant"%>
<%@page import="org.apache.commons.logging.LogFactory"%>
<%@ page import="org.alfresco.web.app.servlet.AuthenticationHelper"%>
<%@ page import="javax.servlet.http.Cookie"%>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.service.cmr.security.AuthenticationService" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>

<%@ page import="org.alfresco.service.transaction.TransactionService" %>
<%@ page import="org.alfresco.service.cmr.security.PermissionService" %>
<%@ page import="org.alfresco.service.cmr.security.PersonService" %>
<%@ page import="org.alfresco.service.cmr.repository.NodeRef" %>
<%@ page import="org.alfresco.web.bean.repository.User" %>


<%@ page isELIgnored="false"%>

<%
	// ensure construction of the FacesContext before attemping a service call
	final FacesContext fc = FacesHelper.getFacesContext(request, response, application);
	fc.getViewRoot().setViewId(CircabcNavigationHandler.WAI_NAVIGATION_CONTAINER_PAGE);
%>

<%
	final WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());
	final HttpSession finalSession = session ;
	final Cookie authCookie = AuthenticationHelper.getAuthCookie(request);
	// remove the cookie
	if (authCookie != null)
	{
    	authCookie.setMaxAge(0);
        response.addCookie(authCookie);
	}

	// expired ticket
        final AuthenticationService unpAuth = (AuthenticationService)context.getBean("authenticationService");
        unpAuth.invalidateTicket(unpAuth.getCurrentTicket());
        unpAuth.clearCurrentSecurityContext();

        final TransactionService transactionService = Services.getAlfrescoServiceRegistry(context.getServletContext()).getTransactionService();

	final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
	final RetryingTransactionHelper.RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
	{
		public Object execute() throws Throwable
		{
			final AuthenticationService authService = (AuthenticationService)context.getBean("AuthenticationService");
			authService.authenticateAsGuest();

			final PersonService personService = (PersonService)context.getBean("personService");
	        final NodeRef guestRef = personService.getPerson(CircabcConstant.GUEST_AUTHORITY);

	        final User user = new User(authService.getCurrentUserName(), authService.getCurrentTicket(), guestRef);
	        finalSession.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);

	        finalSession.removeAttribute(AuthenticationHelper.SESSION_INVALIDATED);

			return null;
		}
	};
	try
	{
		txnHelper.doInTransaction(callback, false,true);

	}
	catch (final Throwable e)
	{
		LogFactory.getLog(LoginBean.class).error("Error during a logout: ", e);
	}

%>

<%-- redirect to the web application's appropriate start page --%>
<%
	final String redirectUrl = request.getContextPath() + "/faces/jsp/extension/welcome.jsp";

	session.invalidate();

	response.sendRedirect(redirectUrl);
%>