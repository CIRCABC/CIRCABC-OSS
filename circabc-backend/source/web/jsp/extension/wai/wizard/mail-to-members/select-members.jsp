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

<%@ page isELIgnored="false"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormMailToMembersStep1" styleClass="contentMainForm">

	<circabc:panel id="select-members-section-1" styleClass="signup_rub_title" >
		<h:outputText id="select-members-mess" value="&nbsp;#{msg.general_properties}" escape="false" />
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>

	<h:panelGrid id="select-members-grid" columns="2" rendered="#{WizardManager.bean.searchResultLimitExceeded}" width="100%" style="background-color:#FFFFCC;border: 1px solid #CCCCCC;padding:5px;" >
	    <h:graphicImage id="select-members-warn" value="/images/icons/warning.gif" alt="#{msg.warning}" />
	    <h:outputFormat id="select-members-hits" value="#{cmsg.invite_user_max_hits}">
			<circabc:param value="#{WizardManager.bean.maxSearchResult}" />
		</h:outputFormat>
	</h:panelGrid>

	<h:selectBooleanCheckbox value="#{WizardManager.bean.onlineMembersOnly}"  />
	<h:outputText value="#{cmsg.mail_online_members_only}"  style="font-style: italic;"/>

	<a:genericPicker id="picker" showAddButton="false"
		filters="#{WizardManager.bean.filters}"
		queryCallback="#{WizardManager.bean.pickerSendMailToMembersCallback}"
		width="300" />

	<f:verbatim><br /></f:verbatim>

	<h:commandButton id="mail_to-members-add-to-list-button"  value="#{msg.add_to_list_button}"
		actionListener="#{WizardManager.bean.addSelection}"
		styleClass="wizardButton" />
	<f:verbatim><br /><br /></f:verbatim>

	<h:dataTable id="select-members-data-table" value="#{WizardManager.bean.selectedUsersDataModel}" var="row"
		rowClasses="selectedItemsRow,selectedItemsRowAlt"
		styleClass="selectedItems" headerClass="selectedItemsHeader"
		cellspacing="0" cellpadding="4"
		rendered="#{WizardManager.bean.selectedUsersDataModel.rowCount != 0}">
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msg.name}" />
			</f:facet>
			<h:outputText id="select-members-label" value="#{row.label}" />
		</h:column>
		<h:column>
			<circabc:actionLink  id="select-members-actio" actionListener="#{WizardManager.bean.removeSelection}"
				image="/images/icons/delete.gif" tooltip="#{cmsg.remove}" value="#{msg.remove}" showLink="false"
				styleClass="pad6Left" />
		</h:column>
	</h:dataTable>
	<a:panel id="no-items" rendered="#{WizardManager.bean.selectedUsersDataModel.rowCount == 0}">
		<h:outputText id="select-members-notitem" value="#{msg.name}: #{msg.no_selected_items}" />
	</a:panel>

</circabc:panel>

