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
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false" %>

<script language="javascript">
    function updateList(){
        document.getElementById("FormPrincipal:submit-change-filter").click();
    }
</script>


<circabc:panel id="contentMainFormManageUserProfile" styleClass="contentMainForm">

	<!--  Action add a new keyword -->
	<circabc:panel id="manage-user-profile-more-action-section" styleClass="wai_dialog_more_action">
		<h:graphicImage value="/images/icons/invite.gif" title="#{cmsg.invite_circabc_user_action_tooltip}" alt="#{cmsg.invite_circabc_user_action_tooltip}" />
		<h:outputText id="manage-user-profile-set-perm-space" value="&nbsp;" escape="false" />
		<circabc:actionLink id="manage-user-profile-act-invite" value="#{cmsg.manage_invited_users_action_invite}" tooltip="#{cmsg.invite_circabc_user_action_tooltip}" action="wai:wizard:inviteCircabcUsers" actionListener="#{WaiWizardManager.setupParameters}" >
			<circabc:param id="manage-user-profile-act-id" name="id" value="#{DialogManager.bean.actionNode.id}" />
		</circabc:actionLink>
	</circabc:panel>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="manage-user-profile-filter-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.uninvite_change_circabc_user_profile_section_0}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:outputText id="manage-user-profile-filter-text" value="#{cmsg.uninvite_change_circabc_user_profile_filter}:&nbsp;" escape="false"/>
	<h:selectOneMenu id="manage-user-profile-filter-select" value="#{DialogManager.bean.selectedProfile}" onchange="updateList()" valueChangeListener="#{ManageUserProfilesDialog.updateList}" immediate="true"  >
		<f:selectItems id="manage-user-profile-filter-options" value="#{DialogManager.bean.profiles}" />	
	</h:selectOneMenu>
	<h:outputText id="directory-home-name-label" value="&nbsp;&nbsp;#{cmsg.members_home_name}&nbsp;" escape="false" />
	<h:inputText id="manage-user-profile-filter-search-text" value="#{DialogManager.bean.searchText}"  valueChangeListener="#{ManageUserProfilesDialog.updateSearchText}" immediate="true" />
	<h:outputText id="directory-home-txt-page-size" value="&nbsp;&nbsp;#{cmsg.members_home_results_per_page}&nbsp;" escape="false" />
	<h:selectOneMenu id="directory-home-sel-page-size" value="#{DirectoryBean.pageSize}" onchange="updateList()" valueChangeListener="#{DirectoryBean.updatePageSize}" immediate="true"  >
		<f:selectItems id="directory-home-page-size-options" value="#{DirectoryBean.pageSizes}" />
	</h:selectOneMenu>
	<h:outputText id="manage-user-profile-filter-spaces-submit" value="&nbsp;&nbsp;" escape="false" />
	<h:commandButton id="submit-change-filter" styleClass="" value="#{cmsg.filter}" action="wai:dialog:close:wai:dialog:manageUserProfilesDialogWai" rendered="true" immediate="true" title="#{cmsg.uninvite_change_circabc_user_profile_apply_filter_submit}" />

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="apply-new-user-profile-section" styleClass="signup_rub_title">
		<h:outputText value="Change Profiles" />	
	</circabc:panel>
		
	<f:verbatim>
		<br />
	</f:verbatim>

	<h:outputText id="manage-user-apply-profile-text" value="#{cmsg.manage_user_apply_profile_text}:&nbsp;" escape="false"/>
	<h:selectOneMenu id="manage-user-apply-profile-select" disabled="#{DialogManager.bean.disableApplyNewProfileButton}" value="#{DialogManager.bean.newProfile}" valueChangeListener="#{ManageUserProfilesDialog.updateNewProfileList}" immediate="true"  >
		<f:selectItems id="manage-user-apply-profile-select-options" value="#{DialogManager.bean.assignableProfileList}" />	
	</h:selectOneMenu>	
	<h:outputText id="apply-new-profile-command-spaces-button" value="&nbsp;&nbsp;" escape="false" />
	<h:commandButton id="apply-new-profile-command-button" styleClass="" disabled="#{DialogManager.bean.disableApplyNewProfileButton}" value="#{cmsg.apply_new_profile_command_button_text}" action="#{DialogManager.bean.applyNewProfile}" rendered="true" immediate="true" title="#{cmsg.apply_new_profile_command_button_tooltip}" />
	
	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="manage-user-profile-main-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.uninvite_change_circabc_user_profile_section_1}"  />
	</circabc:panel>

	
	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:richList id="manage-user-profile-list"
			viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
			pageSize="#{DirectoryBean.pageSize}"
			value="#{DialogManager.bean.filteredUsers}" var="r" initialSortColumn="lastName">

		<%-- Primary column with last name --%>
		<circabc:column id="manage-user-profile-col-lastname" primary="true" >
			<f:facet name="header">
				<circabc:sortLink id="manage-user-profile-link-lastname" label="#{cmsg.members_home_surname}" value="lastName" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<f:facet name="small-icon">
				<h:graphicImage id="manage-user-profile-col-image" url="#{r.icon}" />
			</f:facet>
			<h:outputText id="manage-user-profile-val-lastname" value="#{r.lastName}" />
		</circabc:column>
		
		<%-- column with first name --%>
		<circabc:column id="manage-user-profile-col-firstname" >
			<f:facet name="header">
				<circabc:sortLink id="manage-user-profile-link-firstname" label="#{cmsg.members_home_firstname}" value="firstName" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="manage-user-profile-val-firstname" value="#{r.firstName}" />
		</circabc:column>

		<%-- Email column --%>
		<circabc:column id="manage-user-profile-col-authority" >
			<f:facet name="header">
				<circabc:sortLink id="manage-user-profile-sort-authority" label="#{msg.email}" value="email" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="manage-user-profile-val-authority" value="#{r.email}" />
		</circabc:column>

		<%-- Roles column --%>
		<circabc:column id="manage-user-profile-col-role">
			<f:facet name="header">
				<circabc:sortLink id="manage-user-profile-role-link" label="#{cmsg.profile}" value="userProfile" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="manage-user-profile-role-value" value="#{r.userProfile}" />
		</circabc:column>

		<%-- Actions column --%>
		<circabc:column id="manage-user-profile-col-action" actions="true" >
			<f:facet name="header">
				<h:outputText id="manage-user-profile-actions-link" value="#{cmsg.actions}" />
			</f:facet>
			<a:booleanEvaluator  value="#{r.canBeRemoved == true}" >
				<circabc:actionLink id="manage-user-profile-col-actions-edit" value="#{msg.change_roles}" tooltip="#{msg.change_roles}" image="/images/icons/edituser.gif" showLink="false" action="wai:dialog:editUserProfileDialogWai" actionListener="#{WaiDialogManager.setupParameters}">
					<circabc:param id="manage-user-profile-col-actions-edit-par-id" name="id" value="#{DialogManager.bean.actionNode.id}" />
					<circabc:param id="manage-user-profile-col-actions-edit-par-user" name="userName" value="#{r.authority}" />
					<circabc:param id="manage-user-profile-col-actions-edit-par-imageName" name="imageName" value="edit_#{r.userName}" />
					<circabc:param id="manage-user-profile-col-actions-edit-par-service" name="service" value="Administration" />
					<circabc:param id="manage-user-profile-col-actions-edit-par-activity" name="activity" value="Change profile" />
				</circabc:actionLink>
				<circabc:actionLink id="manage-user-profile-col-actions-remove" value="#{cmsg.remove_membership}" tooltip="#{cmsg.remove_membership}" image="/images/icons/delete_person.gif" showLink="false" action="wai:dialog:removeUserProfileDialogWai" actionListener="#{WaiDialogManager.setupParameters}">
					<circabc:param id="manage-user-profile-col-actions-remove-par-id" name="id" value="#{DialogManager.bean.actionNode.id}" />
					<circabc:param id="manage-user-profile-col-actions-remove-par-user" name="userName" value="#{r.authority}" />
					<circabc:param id="manage-user-profile-col-actions-remove-imageName" name="imageName" value="remove_#{r.userName}" />
					<circabc:param id="manage-user-profile-col-actions-remove-par-profile" name="profile" value="#{r.userProfile}" />
					<circabc:param id="manage-user-profile-col-actions-remove-par-service" name="service" value="Administration" />
					<circabc:param id="manage-user-profile-col-actions-remove-par-activity" name="activity" value="Remove membership" />
				</circabc:actionLink>
			</a:booleanEvaluator>
		</circabc:column>

		<f:facet name="empty">
			<h:outputFormat id="manage-user-profileissions-list-container" value="#{cmsg.no_list_items}"  />
		</f:facet>

		<circabc:dataPager id="data-pager-manage-user-profiles" styleClass="pagerCirca" />
	</circabc:richList>
	
	
	<f:verbatim><br /></f:verbatim>	
	
	<circabc:panel id="panelExport" label="#{cmsg.export_label}" tooltip="#{cmsg.export_label}" styleClass="panelSearchGlobal" styleClassLabel="panelSearchLabel">
		<f:verbatim><br /></f:verbatim>
		<h:selectOneMenu id="export-type" value="#{DialogManager.bean.exportType}" immediate="true"  >
			<f:selectItems id="export-type-options" value="#{DialogManager.bean.exportTypes}" />
		</h:selectOneMenu>
		<h:outputText id="export-space3" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="export-button" action="#{DialogManager.bean.export}" value="#{cmsg.export}" />
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:panel>
	
</circabc:panel>

