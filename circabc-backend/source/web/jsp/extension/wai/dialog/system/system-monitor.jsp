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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormSystemMonitorUi" styleClass="contentMainForm">

	<p class="monitorText">
		<h:outputText value="#{cmsg.system_monitor_max_file_upload}"/>
		<h:inputText value="#{WaiDialogManager.bean.maxFileSize}" styleClass="monitorCounter"  />
	</p>
		
	<p class="monitorText">
		<h:outputText value="#{cmsg.system_monitor_import_max_file_upload}"/>
		<h:inputText value="#{WaiDialogManager.bean.importMaxFileSize}" styleClass="monitorCounter"  />
	</p>
	
	<p class="monitorText">
		<h:outputText value="#{cmsg.system_monitor_text1}" />
		<h:outputText value="#{WaiDialogManager.bean.userCount}" styleClass="monitorCounter"  />
		<h:outputText value="#{cmsg.system_monitor_text3}" />
	</p>
	
	<circabc:richList id="userList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" 
		value="#{WaiDialogManager.bean.users}" var="usr" pageSize="100">
		<circabc:column id="userListColUserName">
			<f:facet name="header">
				<h:outputText value="#{cmsg.system_monitor_user_list_col_username}" />
			</f:facet>
			<h:outputText value="#{usr.userName}"/>
		</circabc:column>
		<circabc:column id="userListColFullName">
			<f:facet name="header">
				<h:outputText value="#{cmsg.system_monitor_user_list_col_fullname}" />
			</f:facet>
			<h:outputText value="#{usr.fullName}"/>
		</circabc:column>
		<circabc:column id="userListColEmail">
			<f:facet name="header">
				<h:outputText value="#{cmsg.system_monitor_user_list_col_email}" />
			</f:facet>
			<h:outputText value="#{usr.email}"/>
		</circabc:column>
		<circabc:dataPager id="system-monitor-pager" styleClass="pagerCirca" />
	</circabc:richList>
	
</circabc:panel>
