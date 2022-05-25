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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<div class="formArea">

	<h:panelGroup rendered="#{BulkInviteCircabcUsersWizard.ecasDepartmentNumberEnabled == false}">

	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_department_number_disabled_title }"></h:outputText>
	
	<h:outputText escape="false" value="<br/><br/>#{cmsg.bulk_invite_department_number_disabled }"></h:outputText>

	</h:panelGroup>
	
	<h:panelGroup rendered="#{BulkInviteCircabcUsersWizard.ecasDepartmentNumberEnabled == true}">

	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_department_number_title }"></h:outputText>

	</h:panelGroup>
	
</div>
