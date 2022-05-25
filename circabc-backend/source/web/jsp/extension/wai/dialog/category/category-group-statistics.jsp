<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? � as soon they
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
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page buffer="32kb" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainCategoryStats"
	styleClass="contentMainForm">

	<circabc:actionLink value="#{cmsg.category_group_stats_launch_action}" tooltip="" actionListener="#{CategoryGroupStatisticsBean.generateCategoryIgList}"></circabc:actionLink>
	<br/><br/>
	<h:outputText value="#{cmsg.category_group_stats_launch_action_description }"></h:outputText>
	
	<hr/>
	
	<circabc:panel id="panelContainer" label="#{cmsg.library_panel_content_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.library_panel_container_tooltip}">
			
			<h:dataTable id="reports" styleClass="recordSet" value="#{CategoryGroupStatisticsBean.reports}" var="file" >
				<%-- Primary column for details view mode --%>
				<h:column id="content-title">
					<f:facet name="header">
						<h:outputText id="ig-home-wnew-col-name-content-title" value="#{cmsg.title}"/>
					</f:facet>
					<circabc:actionLink id="ig-home-wnew-content-icon"  value="#{file.name}" href="#{file.downloadUrl}" target="new" tooltip="#{cmsg.igroot_home_whats_new_link_tooltip}"/>
				</h:column>
				
				<h:column id="content-size">
					<f:facet name="header">
						<h:outputText id="ig-home-wnew-col-name-content-size" value="#{cmsg.size}"/>
					</f:facet>
					<h:outputText id="size" value="#{file.sizeAsString}"/>
					
				</h:column>
				
				<h:column id="content-modified">
					<f:facet name="header">
						<h:outputText id="ig-home-wnew-col-name-content-modified" value="#{cmsg.modified}"/>
					</f:facet>
					<h:outputText id="modified" value="#{file.fileInfo.modifiedDate}"/>
					
				</h:column>
				
				<%-- component to display if the list is empty - normally not seen --%>
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-whatsnews" value="#{cmsg.no_list_items}"/>
				</f:facet>
			</h:dataTable>
		</circabc:panel>	


</circabc:panel>