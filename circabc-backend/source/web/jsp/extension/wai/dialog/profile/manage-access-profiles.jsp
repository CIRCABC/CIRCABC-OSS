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
<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<r:permissionEvaluator value="#{DialogManager.bean.actionNode}" allow="DirAdmin">

<circabc:panel id="contentMainFormEditProfiles" styleClass="contentMainForm">

	<!--  Actions  -->
	<h:panelGrid columns="2" cellpadding="2" cellspacing="1" border="0" id="edit-profiles-view-user-profile-section" styleClass="wai_dialog_more_action">
			<h:graphicImage value="/images/icons/users.gif" title="#{cmsg.uninvite_change_circabc_user_icon_tooltip}" alt="#{cmsg.uninvite_change_circabc_user_icon_tooltip}" />
			<circabc:actionLink id="edit-profiles-more-action-action" value="#{cmsg.uninvite_change_circabc_user_profile}" tooltip="#{cmsg.uninvite_change_circabc_user_profile_tooltip}" action="wai:dialog:manageUserProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
				<circabc:param id="edit-profiles-more-action-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
			</circabc:actionLink>
			<h:graphicImage value="/images/icons/add_group.gif" title="#{cmsg.create_access_profile_dialog_icon_tooltip}" alt="#{cmsg.create_access_profile_dialog_icon_tooltip}" />
			<circabc:actionLink id="add-profile-more-action-action" value="#{cmsg.create_access_profile_dialog_page_action}" tooltip="#{cmsg.create_access_profile_dialog_page_action_tooltip}" action="wai:dialog:createAccessProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
				<circabc:param id="add-profile-more-action-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
	            <circabc:param id="add-profile-service" name="service" value="Administration" />
		        <circabc:param id="add-profile-activity" name="activity" value="Add a new profile" />
			</circabc:actionLink>
	</h:panelGrid>


	<f:verbatim><br /><br /><br /></f:verbatim>

	<!--
			Import a profile Section
	 -->
	<circabc:panel id="dit-profiles-first-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_circabc_ig_profiles_section_1}"  />
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>

	<h:outputText id="import-text" value="#{cmsg.import_profile_text}:&nbsp;" escape="false"/>

	<h:selectOneMenu id="import-text-value" value="#{ManageAccessProfilesDialog.exportedProfile}"   >
		<f:selectItem  id="import-empty" itemValue="__SELECT_ONE__" itemLabel="#{cmsg.import_profile_select_one}" />
		<f:selectItems id="import-text-values" value="#{ManageAccessProfilesDialog.exportedProfiles}" />
	</h:selectOneMenu>
	<h:outputText id="import-text-submit" value="&nbsp;&nbsp;" escape="false" />
	<h:commandButton id="submit-import-profile" styleClass="" value="#{cmsg.import_profile_button}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" rendered="true" title="#{cmsg.import_profile_button}" actionListener="#{ManageAccessProfilesDialog.importProfile}" onclick="showWaitProgress();" />

	<f:verbatim><br /><br /></f:verbatim>

	<!--
			Public/Registered  Access Control
	 -->

	<circabc:panel id="dit-profiles-fourd-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_circabc_ig_profiles_section_4}"  />
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">

		<h:outputFormat id="activate-guest" value="#{cmsg.public_access_profile_status}" >
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
		</h:outputFormat>

		<h:outputText id="guest-enable" value="#{cmsg.access_profile_enable_action}" rendered="#{ManageAccessProfilesDialog.guestActivated == true}" styleClass="textBolder" />
		<h:outputText id="guest-disable" value="#{cmsg.access_profile_disable_action}" rendered="#{ManageAccessProfilesDialog.guestActivated == false}" styleClass="textBolder"/>

		<h:commandButton id="submit-deact-guest" image="/images/icons/abort_submission.gif" value="#{cmsg.access_profile_disable_action}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" immediate="true" title="#{cmsg.public_access_profile_disable_tooltip}" actionListener="#{ManageAccessProfilesDialog.activateGuest}"  rendered="#{ManageAccessProfilesDialog.guestActivated == true}"  onclick="showWaitProgress();">
			<circabc:param id="param-deact-guest" name="activate" value="false" />
		</h:commandButton>
		<h:commandButton id="submit-act-guest" image="/images/icons/promote_submission.gif" value="#{cmsg.access_profile_disable_action}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" immediate="true" title="#{cmsg.public_access_profile_enable_tooltip}" actionListener="#{ManageAccessProfilesDialog.activateGuest}" rendered="#{ManageAccessProfilesDialog.guestActivated == false}" onclick="showWaitProgress();">
			<circabc:param id="param-act-guest" name="activate" value="true" />
		</h:commandButton>

		<h:outputFormat id="activate-registred" value="#{cmsg.registred_access_profile_status}" >
				<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
		</h:outputFormat>

		<h:outputText id="registred-enable" value="#{cmsg.access_profile_enable_action}" rendered="#{ManageAccessProfilesDialog.registredActivated == true}" styleClass="textBolder" />
		<h:outputText id="registred-disable" value="#{cmsg.access_profile_disable_action}" rendered="#{ManageAccessProfilesDialog.registredActivated == false}" styleClass="textBolder" />

		<h:commandButton id="submit-deact-registred" image="/images/icons/abort_submission.gif" value="#{cmsg.access_profile_enable_action}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" immediate="true" title="#{cmsg.registred_access_profile_disable_tooltip}" actionListener="#{ManageAccessProfilesDialog.activateRegistered}" rendered="#{ManageAccessProfilesDialog.registredActivated == true}" onclick="showWaitProgress();">
			<circabc:param id="param-deact-registred" name="activate" value="false" />
		</h:commandButton>
		<h:commandButton id="submit-act-registred" image="/images/icons/promote_submission.gif" value="#{cmsg.access_profile_disable_action}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" immediate="true" title="#{cmsg.registred_access_profile_enable_tooltip}" actionListener="#{ManageAccessProfilesDialog.activateRegistered}" rendered="#{ManageAccessProfilesDialog.registredActivated == false}" onclick="showWaitProgress();">
			<circabc:param id="param-act-registred" name="activate" value="true" />
		</h:commandButton>
		
		
		<h:outputFormat id="activate-registred-allow-apply" value="#{cmsg.registred_access_profile_status_allow_apply}" rendered="#{ManageAccessProfilesDialog.registredActivated == true}"  >
				<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
		</h:outputFormat>

		<h:outputText id="registred-enable-apply" value="#{cmsg.access_profile_enable_action}" rendered="#{ManageAccessProfilesDialog.registredActivated == true && ManageAccessProfilesDialog.registredActivatedApply == true}" styleClass="textBolder" />
		<h:outputText id="registred-disable-apply" value="#{cmsg.access_profile_disable_action}" rendered="#{ManageAccessProfilesDialog.registredActivated == true && ManageAccessProfilesDialog.registredActivatedApply == false}" styleClass="textBolder" />

		<h:commandButton id="submit-deact-registred-apply" image="/images/icons/abort_submission.gif" value="#{cmsg.access_profile_enable_action}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" immediate="true" title="#{cmsg.registred_access_profile_disable_tooltip}" actionListener="#{ManageAccessProfilesDialog.activateRegisteredApply}" rendered="#{ManageAccessProfilesDialog.registredActivated == true && ManageAccessProfilesDialog.registredActivatedApply == true}" onclick="showWaitProgress();">
			<circabc:param id="param-deact-registred-apply" name="activate" value="false" />
		</h:commandButton>
		<h:commandButton id="submit-act-registred-apply" image="/images/icons/promote_submission.gif" value="#{cmsg.access_profile_disable_action}" action="wai:dialog:close:wai:dialog:manageAccessProfilesDialogWai" immediate="true" title="#{cmsg.registred_access_profile_enable_tooltip}" actionListener="#{ManageAccessProfilesDialog.activateRegisteredApply}" rendered="#{ManageAccessProfilesDialog.registredActivated == true && ManageAccessProfilesDialog.registredActivatedApply == false}" onclick="showWaitProgress();">
			<circabc:param id="param-act-registred-apply" name="activate" value="true" />
		</h:commandButton>
	</h:panelGrid>

	<f:verbatim><br /><br /></f:verbatim>

	<!--
			List of defined profiles
	 -->
	<circabc:panel id="dit-profiles-second-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_circabc_ig_profiles_section_2}"  />
	</circabc:panel>
	<f:verbatim><br /></f:verbatim>

	<h:dataTable id="AccessProfilesTable" value="#{ManageAccessProfilesDialog.profilesDataModel}" var="row"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" width="100%"
			rendered="#{ManageAccessProfilesDialog.profilesDataModel.rowCount != 0}">

			<h:column id="ColumnInternalName">
				<f:facet name="header">
					<h:outputText value="#{cmsg.profile_internal_name}" />
				</f:facet>

				<circabc:actionLink id="view-select-profile-link" value="#{row.displayTitle}" tooltip="#{cmsg.view_persons_in_profile_action_tooltip}" action="wai:dialog:manageUserProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}"  styleClass="underlinedTextBolder" onclick="showWaitProgress();">
					<circabc:param id="view-select-profile-link-profile-auth" name="profileName" value="#{row.autority}" />
					<circabc:param id="view-select-profile-link-profile-id"  name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
				</circabc:actionLink>
			</h:column>

			<h:column id="ColumnInformation" >
				<f:facet name="header">
					<h:outputText value="#{cmsg.information_role}" />
				</f:facet>
				<h:outputText value="#{row.infPermission}" title="#{row.infPermissionTooltip}"/>
			</h:column>

			<h:column id="ColumnLibrary">
				<f:facet name="header">
					<h:outputText value="#{cmsg.library_role}" />
				</f:facet>
				<h:outputText value="#{row.libPermission}" title="#{row.libPermissionTooltip}"/>
			</h:column>
			<h:column id="ColumnDirectory">
				<f:facet name="header">
					<h:outputText value="#{cmsg.directory_role}" />
				</f:facet>
				<h:outputText value="#{row.dirPermission}" title="#{row.dirPermissionTooltip}"/>
			</h:column>
			<h:column id="ColumnEvent">
				<f:facet name="header">
					<h:outputText value="#{cmsg.event_role}" />
				</f:facet>
				<h:outputText value="#{row.evePermission}"  title="#{row.evePermissionTooltip}"/>
			</h:column>
			<h:column id="ColumnNewsgroup">
				<f:facet name="header">
					<h:outputText value="#{cmsg.newsGroup_role}" />
				</f:facet>
				<h:outputText value="#{row.newPermission}" title="#{row.newPermissionTooltip}"/>
			</h:column>
			<h:column id="ActionsOnProfile">
				<f:facet name="header">
					<h:outputText value="#{cmsg.actions}" />
				</f:facet>

				<a:booleanEvaluator value="#{row.specialProfile == false}">
					<circabc:actionLink id="ViewUserProfile" action="wai:dialog:manageUserProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" onclick="showWaitProgress();"
							image="/images/icons/users.gif" value="#{cmsg.view_persons_in_profile_action_title}" tooltip="#{cmsg.view_persons_in_profile_action_tooltip}" showLink="false" styleClass="pad6Left" >
							<circabc:param id="ViewUserProfile-param-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
							<circabc:param id="ViewUserProfile-param-autority" name="profileName" value="#{row.autority}" />
					</circabc:actionLink>
					<f:verbatim>&nbsp;</f:verbatim>
				</a:booleanEvaluator>

				<a:booleanEvaluator value="#{row.editActionAvailable}">
					<circabc:actionLink id="EditProfile" action="wai:dialog:editAccessProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" onclick="showWaitProgress();"
							image="/images/icons/edit_group.gif" value="#{cmsg.profile_edit_title}" tooltip="#{cmsg.profile_edit_tooltip}" 
							showLink="false" styleClass="pad6Left">
							<circabc:param id="EditProfile-param-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
							<circabc:param id="EditProfile-param-autority" name="profileName" value="#{row.name}" />
							<circabc:param id="EditProfile-param-service" name="service" value="Administration" />
			        		<circabc:param id="EditProfile-param-activity" name="activity" value="Change permission on profile" />
					</circabc:actionLink>
					<f:verbatim>&nbsp;</f:verbatim>
				</a:booleanEvaluator>

				<a:booleanEvaluator value="#{row.unexportedActionAvailable}">
					<circabc:actionLink id="UnexportProfile" actionListener="#{ManageAccessProfilesDialog.unexportProfile}" onclick="showWaitProgress();"
						image="/images/icons/blog_remove.png" value="#{cmsg.profile_unexport_title}" tooltip="#{cmsg.profile_unexport_tooltip}" showLink="false" styleClass="pad6Left" />
					<f:verbatim>&nbsp;</f:verbatim>
				</a:booleanEvaluator>

				<a:booleanEvaluator value="#{row.exportedActionAvailable}">
					<circabc:actionLink id="ExportProfile" actionListener="#{ManageAccessProfilesDialog.exportProfile}" onclick="showWaitProgress();"
						image="/images/icons/blog_post.png" value="#{cmsg.profile_export_title}" tooltip="#{cmsg.profile_export_tooltip}" showLink="false" styleClass="pad6Left" />
					<f:verbatim>&nbsp;</f:verbatim>
				</a:booleanEvaluator>

				<a:booleanEvaluator value="#{row.deleteActionAvailable}">
					<circabc:actionLink id="DeleteProfile" action="wai:dialog:deleteAccessProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" onclick="showWaitProgress();"
							image="/images/icons/delete.gif" value="#{cmsg.profile_remove_title}" tooltip="#{cmsg.profile_remove_tooltip}" showLink="false" styleClass="pad6Left" >
							<circabc:param id="DeleteProfile-param-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
							<circabc:param id="DeleteProfile-param-autority" name="profileName" value="#{row.name}" />
							<circabc:param id="DeleteProfile-param-prof-title" name="profileDisplayTitle" value="#{row.displayTitle}" />
							<circabc:param id="DeleteProfile-service" name="service" value="Administration" />
					        <circabc:param id="DeleteProfile-activity" name="activity" value="Delete a profile" />
					</circabc:actionLink>
					<f:verbatim>&nbsp;</f:verbatim>
				</a:booleanEvaluator>
				<f:verbatim>&nbsp;</f:verbatim>
			</h:column>

	</h:dataTable>

	<h:outputText id="no-profile" value="#{cmsg.edit_circabc_ig_profiles_section_2_no_profile}"  rendered="#{ManageAccessProfilesDialog.profilesDataModel.rowCount == 0}" styleClass="noItem" />

	<f:verbatim><br /><br /></f:verbatim>

	<!--
			List of imported profiles
	 -->
	<circabc:panel id="dit-profiles-third-section" styleClass="signup_rub_title" >
		<h:outputText value="#{cmsg.edit_circabc_ig_profiles_section_3}"  />
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>

	<h:dataTable id="ImportedAccessProfilesTable" value="#{ManageAccessProfilesDialog.importedProfilesDataModel}" var="row"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" width="100%"
			rendered="#{ManageAccessProfilesDialog.importedProfilesDataModel.rowCount != 0}">

			<h:column id="ImportedColumnInternalName"  >
				<f:facet name="header">
					<h:outputText value="#{cmsg.profile_internal_name}" />
				</f:facet>

				<circabc:actionLink id="edit-select-profile-link" value="#{row.displayTitle}" tooltip="#{cmsg.view_persons_in_profile_action_tooltip}" action="wai:dialog:manageUserProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}"  styleClass="underlinedTextBolder" onclick="showWaitProgress();">
					<circabc:param id="edit-select-profile-link-profile-auth" name="profileName" value="#{row.autority}" />
					<circabc:param id="edit-select-profile-link-profile-id"  name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
				</circabc:actionLink>
			</h:column>

			<h:column id="ImportedColumnInformation" >
				<f:facet name="header">
					<h:outputText value="#{cmsg.information_role}" />
				</f:facet>
				<h:outputText value="#{row.infPermission}" title="#{row.infPermissionTooltip}"/>
			</h:column>

			<h:column id="ImportedColumnLibrary">
				<f:facet name="header">
					<h:outputText value="#{cmsg.library_role}" />
				</f:facet>
				<h:outputText value="#{row.libPermission}" title="#{row.libPermissionTooltip}"/>
			</h:column>
			<h:column id="ImportedColumnDirectory">
				<f:facet name="header">
					<h:outputText value="#{cmsg.directory_role}" />
				</f:facet>
				<h:outputText value="#{row.dirPermission}" title="#{row.dirPermissionTooltip}"/>
			</h:column>
			<h:column id="ImportedColumnEvent">
				<f:facet name="header">
					<h:outputText value="#{cmsg.event_role}" />
				</f:facet>
				<h:outputText value="#{row.evePermission}" title="#{row.evePermissionTooltip}"/>
			</h:column>
			<h:column id="ImportedColumnNewsgroup">
				<f:facet name="header">
					<h:outputText value="#{cmsg.newsGroup_role}" />
				</f:facet>
				<h:outputText value="#{row.newPermission}" title="#{row.newPermissionTooltip}"/>
			</h:column>
			<h:column id="ImportedActionsOnProfile">
				<f:facet name="header">
					<h:outputText value="#{cmsg.actions}" />
				</f:facet>

				<a:booleanEvaluator value="#{row.specialProfile == false}">
					<circabc:actionLink id="ViewUserImportedProfile" action="wai:dialog:manageUserProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" onclick="showWaitProgress();"
							image="/images/icons/users.gif" value="#{cmsg.view_persons_in_profile_action_title}" tooltip="#{cmsg.view_persons_in_profile_action_tooltip}" showLink="false" styleClass="pad6Left" >
							<circabc:param id="ViewUserImportedProfile-param-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
							<circabc:param id="ViewUserImportedProfile-param-autority" name="profileName" value="#{row.autority}" />
					</circabc:actionLink>
					<f:verbatim>&nbsp;</f:verbatim>
				</a:booleanEvaluator>

				<circabc:actionLink id="EditImportedProfile" action="wai:dialog:editAccessProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" onclick="showWaitProgress();"
						image="/images/icons/edit_group.gif" value="#{cmsg.profile_edit_title}" tooltip="#{cmsg.profile_edit_tooltip}" showLink="false" styleClass="pad6Left">
						<circabc:param id="EditImportedProfile-param-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
						<circabc:param id="EditImportedProfile-param-autority" name="profileName" value="#{row.name}" />
						<circabc:param id="EditImportedProfile-param-service" name="service" value="Administration" />
		        		<circabc:param id="EditImportedProfile-param-activity" name="activity" value="Change permission on imported profile" />
				</circabc:actionLink>
				<f:verbatim>&nbsp;</f:verbatim>

				<circabc:actionLink id="DeleteImportedProfile" action="wai:dialog:deleteAccessProfilesDialogWai" actionListener="#{WaiDialogManager.setupParameters}" onclick="showWaitProgress();"
						image="/images/icons/delete.gif" value="#{cmsg.profile_remove_title}" tooltip="#{cmsg.profile_remove_tooltip}" showLink="false" styleClass="pad6Left">
						<circabc:param id="DeleteImportedProfile-param-id" name="id" value="#{ManageAccessProfilesDialog.actionNode.id}" />
						<circabc:param id="DeleteImportedProfile-param-autority" name="profileName" value="#{row.name}" />
						<circabc:param id="DeleteImportedProfile-param-prof-title" name="profileDisplayTitle" value="#{row.displayTitle}" />
						<circabc:param id="DeleteImportedProfile-param-prof-import" name="profileImported" value="true" />
						<circabc:param id="DeleteImportedProfile-param-service" name="service" value="Administration" />
					    <circabc:param id="DeleteImportedProfile-param-activity" name="activity" value="Remove imported profile" />
				</circabc:actionLink>
				<f:verbatim>&nbsp;</f:verbatim>
			</h:column>
	</h:dataTable>

	<h:outputText id="no-imported" value="#{cmsg.edit_circabc_ig_profiles_section_3_no_imported}"  rendered="#{ManageAccessProfilesDialog.importedProfilesDataModel.rowCount == 0}" styleClass="noItem" />


</circabc:panel>

</r:permissionEvaluator>



