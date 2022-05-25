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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainGlobalStatisticsWai" styleClass="contentMainForm" >

		<h:outputText value="#{ cmsg.global_statistics_dialog_list_of_files}"></h:outputText>
		<h:commandButton value="Run global statistics" actionListener="#{GlobalStatisticsDialog.fireme }" ></h:commandButton>
		<br/>
		<h:outputText value="#{ cmsg.global_statistics_dialog_list_of_files}"></h:outputText>
		<h:commandButton value="Force cleaning" actionListener="#{GlobalStatisticsDialog.fireme2 }" ></h:commandButton>
		<br/>
		<h:outputText value="Generate Detailed Ig List"></h:outputText>
		<h:commandButton value="Generate" actionListener="#{GlobalStatisticsDialog.generateDetailedIgList }" ></h:commandButton>
		<br/>
		<br/><br/>
		<h:outputText value="#{ cmsg.global_statistics_dialog_search_user_last_login}"></h:outputText>
		<h:commandButton value="Search" actionListener="#{GlobalStatisticsDialog.searchLastLogin }"></h:commandButton>
		<br/>
		<h:outputText value="#{ cmsg.global_statistics_dialog_search_userid}"></h:outputText>
		<h:inputText  value="#{GlobalStatisticsDialog.userid}"></h:inputText>
		<br/>
		<h:outputText value="#{ cmsg.global_statistics_dialog_search_result}"></h:outputText>
		<h:outputText value="#{GlobalStatisticsDialog.result}" style="font-size:120%; font-weight:bold;"></h:outputText>
		<br/><br/>
		
		<circabc:panel id="panelContainer" label="#{cmsg.library_panel_content_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.library_panel_container_tooltip}">
			
			<h:dataTable id="reports" styleClass="recordSet" value="#{GlobalStatisticsDialog.reports}" var="wn" >
				<%-- Primary column for details view mode --%>
				<h:column id="content-title">
					<f:facet name="header">
						<h:outputText id="ig-home-wnew-col-name-content-title" value="#{cmsg.title}"/>
					</f:facet>
					<circabc:actionLink id="ig-home-wnew-content-icon"  value="#{wn.name}" href="#{wn.downloadUrl}" target="new" tooltip="#{cmsg.igroot_home_whats_new_link_tooltip}"/>
				</h:column>
				
				<h:column id="content-size">
					<f:facet name="header">
						<h:outputText id="ig-home-wnew-col-name-content-size" value="#{cmsg.size}"/>
					</f:facet>
					<h:outputText id="size" value="#{wn.sizeAsString}"/>
					
				</h:column>
				
				<h:column id="content-modified">
					<f:facet name="header">
						<h:outputText id="ig-home-wnew-col-name-content-modified" value="#{cmsg.modified}"/>
					</f:facet>
					<h:outputText id="modified" value="#{wn.fileInfo.modifiedDate}"/>
					
				</h:column>
				
				<%-- component to display if the list is empty - normally not seen --%>
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-whatsnews" value="#{cmsg.no_list_items}"/>
				</f:facet>
			</h:dataTable>
		</circabc:panel>
	


</circabc:panel>