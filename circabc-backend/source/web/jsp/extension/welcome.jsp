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
<%@ page import="org.apache.commons.logging.Log"%>
<%@ page import="org.apache.commons.logging.LogFactory"%>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.service.cmr.repository.NodeRef" %>
<%@ page import="org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback"%>
<%@ page import="org.alfresco.repo.transaction.RetryingTransactionHelper"%>
<%@ page import="org.alfresco.service.transaction.TransactionService" %>
<%@ page import="org.alfresco.service.cmr.security.AuthenticationService" %>
<%@ page import="org.alfresco.service.cmr.security.PersonService" %>
<%@ page import="org.alfresco.web.app.servlet.AuthenticationHelper" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.bean.repository.User" %>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="eu.cec.digit.circabc.service.cmr.security.CircabcConstant" %>
<%@ page import="eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean"%>
<%@ page import="eu.cec.digit.circabc.web.wai.bean.navigation.WelcomeBean"%>
<%@ page import="eu.cec.digit.circabc.web.Services"%>

<%@ page isELIgnored="false"%>

<%-- redirect to the web application's appropriate start page --%>
<%
	final WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());

	// ensure construction of the FacesContext before attemping a service call
	final FacesContext fc = FacesHelper.getFacesContext(request, response, application);
	final HttpSession finalSession = session ;

	TransactionService transactionService = Services.getAlfrescoServiceRegistry(context.getServletContext()).getTransactionService();
	final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();

	final RetryingTransactionHelper.RetryingTransactionCallback<Object> callback1 = new RetryingTransactionCallback<Object>()
	{
		public Object execute() throws Throwable
		{

			User user = (User)finalSession.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
			if (user == null || finalSession.isNew())
			{
				AuthenticationService authService = (AuthenticationService)context.getBean("AuthenticationService");
			    authService.authenticateAsGuest();
		   		PersonService personService = (PersonService)context.getBean("personService");
		   		NodeRef guestRef = personService.getPerson(CircabcConstant.GUEST_AUTHORITY);
		   		user = new User(authService.getCurrentUserName(), authService.getCurrentTicket(), guestRef);
		   		finalSession.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);

			}
			return null;
		}
	};
	try
	{
		txnHelper.doInTransaction(callback1, false, true);

	}
	catch (Throwable e)
	{
		final Log logger = LogFactory.getLog(WelcomeBean.class);
		if(logger.isErrorEnabled())
			logger.error("Error during the display of the welcome page: ", e);
	}

	final CircabcBrowseBean browseBean = (CircabcBrowseBean) FacesHelper.getManagedBean(fc, CircabcBrowseBean.BEAN_NAME);

	final RetryingTransactionHelper.RetryingTransactionCallback<Object> callback2 = new RetryingTransactionCallback<Object>()
	{
		public Object execute() throws Throwable
		{
			try
			{
				browseBean.clickCircabcHome(null);
			}
			catch(net.sf.acegisecurity.AuthenticationCredentialsNotFoundException ex)
			{
				User user = (User)finalSession.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
				// if the a secure context is not found, create an new one with the Guest Authority...
				AuthenticationService authService = (AuthenticationService)context.getBean("AuthenticationService");
			    authService.authenticateAsGuest();
		   		PersonService personService = (PersonService)context.getBean("personService");
		   		NodeRef guestRef = personService.getPerson("guest");
		   		user = new User(authService.getCurrentUserName(), authService.getCurrentTicket(), guestRef);
		   		finalSession.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
			}
			return null;
		}
	};
	try
	{
		// Migration 3.1 -> 3.4.6 - 15/12/2011 - Changed read-only to read-write for new 3.4.6 nesting that is read-write
		txnHelper.doInTransaction(callback2, false);

	}
	catch (Throwable e)
	{
		LogFactory.getLog(WelcomeBean.class).error("Error during the display of the welcome page: ", e);
	}

	response.sendRedirect(request.getContextPath() + "/faces/jsp/extension/wai/navigation/container.jsp");
%>

