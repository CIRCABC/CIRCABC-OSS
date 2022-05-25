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

<circabc:panel id="contentMainFormManageNotification" styleClass="contentMainForm">

	<!--  Display a message that infor that only SUSCRIBED and USUSCRIBED will be displayed -->
	<circabc:panel id="manage-notif-panel-warning" styleClass="infoPanel" styleClassLabel="infoContent" >
		<h:graphicImage id="manage-notif-image-warning" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
		<h:outputText id="manage-notif-text-warning-spaces" value="&nbsp;&nbsp;" escape="false" />
		<h:outputText id="manage-notif-text-warning-for-ig" value="#{cmsg.notification_view_other_dialog_forig_info}" escape="false" rendered="#{ManageNotificationDialog.currentNodeInterestGroup == true}" />
		<h:outputText id="manage-notif-text-warning-not-ig" value="#{cmsg.notification_view_other_dialog_notig_info}"  rendered="#{ManageNotificationDialog.currentNodeInterestGroup == false}" />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<!--  Action define a new Notification status for any authority -->
	<circabc:panel id="manage-notif-define-authority-section" styleClass="wai_dialog_more_action">
		<h:graphicImage value="/images/icons/add_user.gif" alt="#{cmsg.notification_define_other_action_tooltip}" />
		<h:outputText id="manage-notif-define-new-space" value="&nbsp;" escape="false" />
		<circabc:actionLink id="manage-notif-act-define" value="#{cmsg.notification_define_other_action_title}" tooltip="#{cmsg.notification_define_other_action_tooltip}" action="wai:dialog:defineOthersNotificationWai" actionListener="#{WaiDialogManager.setupParameters}" >
			<circabc:param id="manage-notif-act-id" name="id" value="#{DialogManager.bean.currentNode.id}" />
			<circabc:param id="manage-notif-act-service" name="service" value="Administration" />
			<circabc:param id="manage-notif-act-activity" name="activity" value="Define notification status" />
		</circabc:actionLink>
	</circabc:panel>


	<f:verbatim>
		<br /><br />
	</f:verbatim>
	
	<circabc:panel id="panelNotificationStatus" label="#{cmsg.notification_view_other_status_panel_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.notification_view_other_status_panel_tooltip}">
		<!--  Display the Notification status -->
		<circabc:richList id="manage-notif-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{DialogManager.bean.notifications}" var="n" initialSortColumn="inheritedString" pageSize="#{BrowseBean.listElementNumber}">
	
			<circabc:column id="manage-notif-list-type">
				<f:facet name="header">
					<circabc:sortLink id="manage-notif-list-type-sorter" label="#{cmsg.notification_view_other_dialog_col_type}" value="type" tooltipAscending="#{cmsg.notification_view_other_type_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_type_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-list-col-type" value="#{n.type}"/>
			</circabc:column>
	
			<circabc:column id="manage-notif-list-username">
				<f:facet name="header">
					<circabc:sortLink id="manage-notif-list-username-sorter" label="#{cmsg.notification_view_other_dialog_col_username}" value="username" tooltipAscending="#{cmsg.notification_view_other_name_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_name_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-list-col-username" value="#{n.username}"/>
			</circabc:column>
	
			<circabc:column id="manage-notif-list-status">
				<f:facet name="header">
					<circabc:sortLink id="manage-notif-list-status-sorter" label="#{cmsg.notification_view_other_dialog_col_status}" value="status" tooltipAscending="#{cmsg.notification_view_other_status_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_status_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-list-col-status" value="#{n.status}"/>
			</circabc:column>
			<circabc:column id="manage-notif-list-inherited">
				<f:facet name="header">
					<circabc:sortLink id="manage-notif-list-inherited-sorter" label="#{cmsg.notification_view_other_dialog_col_inherited}" value="inheritedString" tooltipAscending="#{cmsg.notification_view_other_inherited_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_inherited_sort_desc}"/>
				</f:facet>
				<h:selectBooleanCheckbox id="manage-notif-list-col-inherited-value" readonly="true" value="#{n.inherited}"/>
			</circabc:column>
			<circabc:column id="manage-notif-list-actions-col">
				<f:facet name="header">
					<h:outputText id="manage-notif-list-cont-act" value="#{cmsg.actions}"  />
				</f:facet>
				<circabc:actionLink noDisplay="#{n.inherited == true}" image="/images/icons/edit_group.gif" id="manage-notif-act-modif-authority" tooltip="#{cmsg.notification_edit_other_action_tooltip}" value="#{cmsg.notification_edit_other_action_tooltip}" showLink="false" action="wai:dialog:editAuthorityNotificationDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="param-notif-profile-authority" name="authority" value="#{n.authority}" />
					<circabc:param id="param-notif-profile-status" name="status" value="#{n.statusValueToString}" />
					<circabc:param id="param-notif-profile-node-id" name="id" value="#{n.nodeId}" />
					<circabc:param id="param-notif-profile-node-display" name="displayName" value="#{n.username}" />
					<circabc:param id="param-notif-profile-node-id-imageName" name="imageName" value="edit_notification_#{n.username}" />
					<circabc:param id="param-notif-profile-service" name="service" value="Administration" />
					<circabc:param id="param-notif-profile-activity" name="activity" value="Change notification status" />
				</circabc:actionLink>
				<circabc:actionLink noDisplay="#{n.inherited == true}" image="/images/icons/delete_group.gif" id="manage-notif-act-delete-authority" tooltip="#{cmsg.notification_edit_other_action_tooltip}" value="#{cmsg.notification_edit_other_action_tooltip}" showLink="false" action="wai:dialog:editAuthorityNotificationDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
		 	 	 	<circabc:param id="param-notif-profile-authority_remove" name="authority" value="#{n.authority}" />
		 	 	 	<circabc:param id="param-notif-profile-status_remove" name="status" value="#{n.statusValueToString}" />
		 	 	 	<circabc:param id="param-notif-profile-node-id_remove" name="id" value="#{n.nodeId}" />
		 	 	 	<circabc:param id="param-notif-profile-node-display_remove" name="displayName" value="#{n.username}" />
		 	 	 	<circabc:param id="param-notif-profile-node-id-imageName_remove" name="imageName" value="edit_notification_#{n.username}" />
		 	 	 	<circabc:param id="param-notif-profile-service_remove" name="service" value="Administration" />
		 	 	 	<circabc:param id="param-notif-profile-activity_remove" name="activity" value="Delete notification status" />
		 	 	 	<circabc:param id="param-notif-profile-delete" name="deleteAuthority" value="true" />
	 	 	 	</circabc:actionLink>
			</circabc:column>
	
			<f:facet name="empty">
				<h:outputFormat id="manage-notif-list-container" value="#{cmsg.notification_view_other_no_list_items}"  />
			</f:facet>
			<circabc:dataPager id="manage-notifications-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>	
	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="panelNotificationUsers" label="#{cmsg.notification_view_other_user_panel_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.notification_view_other_user_panel_tooltip}">
		<!--  Display the users that will be notified status -->
		<circabc:richList id="manage-notif-user-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{DialogManager.bean.users}" var="u" initialSortColumn="userName" pageSize="#{BrowseBean.listElementNumber}">
			<circabc:column id="manage-notif-user-list-user-name">
				<f:facet name="header">
					<circabc:sortLink id="col-user-name" label="#{cmsg.members_home_username}" value="userName" tooltipAscending="#{cmsg.notification_view_other_name_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_name_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-user-list-col-user-name" value="#{u.userName}"/>
			</circabc:column>
			<circabc:column id="col-name-surname">
				<f:facet name="header">
					<circabc:sortLink id="col-surname" label="#{cmsg.members_home_surname}" value="lastName" tooltipAscending="#{cmsg.notification_view_other_name_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_name_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-user-list-col-last-name" value="#{u.lastName}"/>
			</circabc:column>
			<circabc:column id="col-first">
				<f:facet name="header">
					<circabc:sortLink id="col-name-first-name" label="#{cmsg.members_home_firstname}" value="firstName" tooltipAscending="#{cmsg.notification_view_other_name_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_name_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-user-list-col-first-name" value="#{u.firstName}"/>
			</circabc:column>
			<circabc:column id="col-email">
				<f:facet name="header">
					<circabc:sortLink id="col-name-email" label="#{cmsg.members_home_email}" value="firstName" tooltipAscending="#{cmsg.notification_view_other_name_sort_asc}" tooltipDescending="#{cmsg.notification_view_other_name_sort_desc}"/>
				</f:facet>
				<h:outputText id="manage-notif-user-list-col-email" value="#{u.emailAddress}"/>
			</circabc:column>
			<f:facet name="empty">
				<h:outputFormat id="manage-notif-user-list-emptu" value="#{cmsg.notification_view_other_no_list_items}"  />
			</f:facet>
			<circabc:dataPager id="manage-notified-user-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>
</circabc:panel>