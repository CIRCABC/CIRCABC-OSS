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
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>

<%@ page isELIgnored="false"%>

<%-- redirect to the web application's appropriate start page --%>
<%
	// ensure construction of the FacesContext before attemping a service call
	FacesContext fc = FacesHelper.getFacesContext(request, response, application);
	response.sendRedirect(request.getContextPath() + "/faces/jsp/extension/welcome.jsp");
%>
