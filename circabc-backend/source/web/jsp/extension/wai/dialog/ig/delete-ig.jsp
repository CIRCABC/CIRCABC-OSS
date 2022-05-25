<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? – as soon they
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
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormDeleteIgNodeDetails" styleClass="contentMainForm">

	

	<circabc:displayer rendered="#{WaiDialogManager.bean.profilesExportedUsedByIGIsEmpty == false}">
		<h:outputText id="delete-ig-node-stop" value="#{cmsg.cannot_delete_ig_whit_exported_profiles_who_are_used}" styleClass="mainSubTitle"/>
		<h:outputText id="delete-ig-node-stop2" value="<br/><br/>" escape="false"/>
		<h:dataTable id="profilesTable" value="#{WaiDialogManager.bean.profiles}" var="row"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" width="100%">
			<h:column id="profileName">
				<f:facet name="header">
					<h:outputText value="#{cmsg.exported_profiles_of_current_ig}" />
				</f:facet>
				<h:outputText value="#{row.profileName}" title="#{row.profileName}" />
			</h:column>
			<h:column id="igWhoImportProfile">
				<f:facet name="header">
					<h:outputText value="#{cmsg.exported_profile_imported_in_ig}" />
				</f:facet>
				<h:dataTable id="igsTable" value="#{row.igs}" var="ig"
					width="100%">
					<h:column id="igName">
						<h:outputText value="#{ig}" title="#{ig}" />
					</h:column>
				</h:dataTable>
			</h:column>
		</h:dataTable>
	</circabc:displayer>

	<circabc:displayer rendered="#{WaiDialogManager.bean.sharedSpacesIsEmpty == false}">
		<h:outputText id="delete-ig-node-stop3" value="#{cmsg.cannot_delete_ig_with_shared_spaces}" styleClass="mainSubTitle"/>
		<h:outputText id="delete-ig-node-stop4" value="<br/><br/>" escape="false"/>
		<h:dataTable id="invitedInterestGroupTable" value="#{WaiDialogManager.bean.sharedSpaces}" var="row"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" width="100%">
			<h:column id="sharedSpacePath">
				<f:facet name="header">
					<h:outputText value="#{cmsg.shared_space_path}" />
				</f:facet>
				<h:outputText value="#{row.path}" title="#{row.path}" />
			</h:column>
		</h:dataTable>
	</circabc:displayer>

	<circabc:displayer rendered="#{WaiDialogManager.bean.deletionAllowed == true}">
		<h:outputText id="delete-ig-node-confirmation" value="#{WaiDialogManager.bean.confirmMessage}" styleClass="mainSubTitle" />
		<f:verbatim><br /></f:verbatim>
		<h:outputText id="delete-log-text" value="#{cmsg.delete_log}" styleClass="mainSubTitle"  />
		<h:selectBooleanCheckbox id="delete-log" value="#{WaiDialogManager.bean.deleteLog}" />
		<f:verbatim><br /></f:verbatim>
		<h:outputText id="purge-data-text" value="#{cmsg.purge_data}" styleClass="mainSubTitle" />
		<h:selectBooleanCheckbox id="purge-data" value="#{WaiDialogManager.bean.purgeData}" />
	</circabc:displayer>
</circabc:panel>


