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


<%@ page isELIgnored="false" %>

<circabc:panel id="contentMainFormDefineOthersNotification" styleClass="contentMainForm">

	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:panel id="define-notif-section-title" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.notification_define_other_dialog_section_title}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<a:genericPicker id="define-notif-picker" showAddButton="false"
		filters="#{DialogManager.bean.filters}"
		queryCallback="#{DialogManager.bean.pickerCallback}"
		width="300" />
	<h:outputText value="" />

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:outputText id="define-notif-select-status" value="#{cmsg.notification_define_other_dialog_select}" />

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<h:selectOneListbox id="define-notif-statuses" size="3" >
		<f:selectItems id="define-notif-statuses-list" value="#{DialogManager.bean.statuses}" />
	</h:selectOneListbox>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<h:commandButton id="define-notif-add-to-list" value="#{msg.add_to_list_button}"
		actionListener="#{DialogManager.bean.addSelection}"
		styleClass="wizardButton" />

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<h:dataTable id="define-notif-data-table" value="#{DefineOthersNotification.userNotificationDataModel}" var="row"
		rowClasses="selectedItemsRow,selectedItemsRowAlt"
		styleClass="selectedItems" headerClass="selectedItemsHeader"
		cellspacing="0" cellpadding="4"
		rendered="#{DefineOthersNotification.userNotificationDataModel.rowCount != 0}">

		<h:column>
			<f:facet name="header">
				<h:outputText id="define-notif-col-1-header" value="#{cmsg.notification_view_other_dialog_col_type}" />
			</f:facet>
			<h:outputText id="define-notif-col-1-value" value="#{row.type}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText id="define-notif-col-2-header" value="#{cmsg.notification_view_other_dialog_col_username}" />
			</f:facet>
			<h:outputText id="define-notif-col-2-value" value="#{row.username}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText id="define-notif-col-3-header" value="#{cmsg.notification_view_other_dialog_col_status}" />
			</f:facet>
			<h:outputText id="define-notif-col-3-value" value="#{row.status}" />
		</h:column>
		<h:column>
			<circabc:actionLink tooltip="#{msg.remove}" id="define-notif-col-4-value"
				actionListener="#{DialogManager.bean.removeSelection}"
				image="/images/icons/delete.gif" value="#{msg.remove}" showLink="false"
				styleClass="pad6Left" />
		</h:column>
	</h:dataTable>

	<a:panel id="no-items"
		rendered="#{DefineOthersNotification.userNotificationDataModel.rowCount == 0}">
		<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
			<tr>
				<td class='selectedItemsRow'>
					<h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
				</td>
			</tr>
		</table>
	</a:panel>


</circabc:panel>