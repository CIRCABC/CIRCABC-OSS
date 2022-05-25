<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or � ? as soon they
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<circabc:panel id="contentMainFormDeleteUserProfile" styleClass="contentMainForm">

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:outputText id="delete-user-profile-tabulation" value="&nbsp;" escape="false"/>
	<h:outputText id="delete-user-profile-confirmation" value="#{WaiDialogManager.bean.confirmationMessage}" styleClass="mainSubTitle" />
		
	<f:verbatim>
		<br /><br />
	</f:verbatim>	
	<h:outputText value="&nbsp;&nbsp;#{cmsg.send_email_to_dir_admins}" escape="false" rendered="#{WaiDialogManager.bean.interestGroup}" />
	<h:selectOneRadio id="SendNotificationDirAdminRadio" value="#{WaiDialogManager.bean.notifyDirAdmins}" rendered="#{WaiDialogManager.bean.interestGroup}" >
		<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
		<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
	</h:selectOneRadio>

	<f:verbatim>
		<br />
	</f:verbatim>

</circabc:panel>