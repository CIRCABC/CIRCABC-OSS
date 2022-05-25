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

<circabc:panel id="contentMainFormManageNavCustomization" styleClass="contentMainForm">

	<!--  Action add a new keyword -->
	<circabc:panel id="edit-nav-more-action" styleClass="wai_dialog_more_action">
		<h:graphicImage value="/images/icons/arrow_down.gif" alt="#{cmsg.edit_navigation_dialog_action_load_default}" />
		<h:outputText id="edit-nav-space" value="&nbsp;" escape="false" />
		<circabc:actionLink id="edit-nav-action-load-link" value="#{cmsg.edit_navigation_dialog_action_load_default}" tooltip="#{cmsg.edit_navigation_dialog_action_load_default_tooltip}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.loadDefault}" />
		<f:verbatim><br /></f:verbatim>
		<h:graphicImage value="/images/icons/undo_checkout.gif" alt="#{cmsg.edit_navigation_dialog_action_revert}" />
		<h:outputText id="edit-nav-revert-space" value="&nbsp;" escape="false" />
		<circabc:actionLink id="edit-revert-link" value="#{cmsg.edit_navigation_dialog_action_revert}" tooltip="#{cmsg.edit_navigation_dialog_action_revert_tooltip}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.revert}" />
	</circabc:panel>

	<f:verbatim>
		<br /><br /><br />
	</f:verbatim>
	
	<circabc:panel id="edit-navigation-breadcrumb_display" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_navigation_dialog_section_breadcrumb_title}" />
	</circabc:panel>

	<h:outputLabel for="banner_customisation_title" value="#{cmsg.category_customise_dialog_manage_navigation_list_title}" ></h:outputLabel>
		<h:selectOneRadio id="breadcrumb_customisation_title" value="#{DialogManager.bean.selectedRenderChoice}"> <!--  -->
			<f:selectItems value="#{DialogManager.bean.selectRenderChoices}"/>
		</h:selectOneRadio>
		<h:outputText value="#{cmsg.category_customise_dialog_manage_navigation_list_description }"></h:outputText>

	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:panel id="edit-navigation-mandatoryCol-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_navigation_dialog_section_mandatory_col}" />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:selectOneRadio  value="#{DialogManager.bean.primaryColumn}" layout="pageDirection" rendered="#{DialogManager.bean.nameAndTitleKeys}" >
		<f:selectItem itemValue="PRIMARY_COL_NAME"  itemLabel="#{cmsg.edit_navigation_dialog_primary_name}" />
		<f:selectItem itemValue="PRIMARY_COL_TITLE" itemLabel="#{cmsg.edit_navigation_dialog_primary_title}" />
		<f:selectItem itemValue="PRIMARY_COL_NAME_TITLE"  itemLabel="#{cmsg.edit_navigation_dialog_primary_name_title}" />
		<f:selectItem itemValue="PRIMARY_COL_TITLE_NAME"  itemLabel="#{cmsg.edit_navigation_dialog_primary_title_name}" />
	</h:selectOneRadio>

	<h:selectOneRadio  value="#{DialogManager.bean.primaryColumn}" layout="pageDirection" rendered="#{DialogManager.bean.bestTitleKey}" >
		<f:selectItem itemValue="PRIMARY_BEST_TITLE"  itemLabel="#{cmsg.edit_navigation_dialog_primary_besttitle}" />
	</h:selectOneRadio>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="edit-navigation-otherCol-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_navigation_dialog_section_other_col}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:panel id="manage-nav--col-warning" styleClass="infoPanel" styleClassLabel="infoContent" >
		<h:graphicImage id="manage-nav-image-col-warning" value="/images/extension/icons/info.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
		<h:outputText id="manage-nav-text-col-warning" value="&nbsp;&nbsp;#{cmsg.edit_navigation_dialog_mandatory_col_info}<br /><br />" escape="false" />
		<%-- 	To uncomment if limits will be configured
		<h:outputFormat value="#{cmsg.edit_navigation_dialog_select_columns_between}" style="font-style: italic;">
			<f:param value="#{DialogManager.bean.minColumnSize}"/>
			<f:param value="#{DialogManager.bean.maxColumnSize}"/>
		</h:outputFormat>
		--%>
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:panelGrid columns="4" cellpadding="3" cellspacing="3"  border="0" >
		<h:selectManyListbox id="list-available-columns" title="#{cmsg.edit_navigation_dialog_list_title}" value="#{DialogManager.bean.highlightedAvailableColumns}" >
			<f:selectItems id="values-available-columns" value="#{DialogManager.bean.availableColumns}"  />
		</h:selectManyListbox>
		<h:panelGrid columns="1" cellpadding="0" cellspacing="0"  border="0" >
            <h:commandButton image="/images/icons/approve.gif" title="#{cmsg.edit_navigation_dialog_list_move_right}" value="#{cmsg.edit_navigation_dialog_list_move_right}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.selectColumn}" />
            <h:commandButton image="/images/icons/reject.gif" title="#{cmsg.edit_navigation_dialog_list_move_left}" value="#{cmsg.edit_navigation_dialog_list_move_left}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.deselectColumn}" />
		</h:panelGrid>

		<h:selectManyListbox id="list-selected-columns" title="#{cmsg.edit_navigation_dialog_list_title}" value="#{DialogManager.bean.highlightedSelectedColumns}">
			<f:selectItems id="values-selected-columns" value="#{DialogManager.bean.selectedColumns}" />
		</h:selectManyListbox>
		<h:panelGrid columns="1" cellpadding="0" cellspacing="0"  border="0" >
            <h:commandButton image="/images/icons/arrow_up.gif" title="#{cmsg.edit_navigation_dialog_list_move_up}" value="#{cmsg.edit_navigation_dialog_list_move_up}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.moveUpColumn}" />
            <h:commandButton image="/images/icons/arrow_down.gif" title="#{cmsg.edit_navigation_dialog_list_move_down}" value="#{cmsg.edit_navigation_dialog_list_move_down}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.moveDownColumn}" />
		</h:panelGrid>
	</h:panelGrid>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="edit-navigation-actions-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_navigation_dialog_section_actions}" />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:panel id="manage-nav--act-warning" styleClass="infoPanel" styleClassLabel="infoContent" >
		<h:graphicImage id="manage-nav-image-act-warning" value="/images/extension/icons/info.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
		<h:outputText id="manage-nav-text-act-warning" value="&nbsp;&nbsp;#{cmsg.edit_navigation_dialog_mandatory_action_info}<br /><br />" escape="false" />
		<%-- 	To uncomment if limits will be configured
		<h:outputFormat value="#{cmsg.edit_navigation_dialog_select_actions_between}" style="font-style: italic;">
			<f:param value="#{DialogManager.bean.minActionSize}"/>
			<f:param value="#{DialogManager.bean.maxActionSize}"/>
		</h:outputFormat>
		--%>
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:panelGrid columns="4" cellpadding="3" cellspacing="3"  border="0" >
		<h:selectManyListbox id="list-available-actions" title="#{cmsg.edit_navigation_dialog_list_title}"  value="#{DialogManager.bean.highlightedAvailableActions}">
			<f:selectItems id="values-available-actions" value="#{DialogManager.bean.availableActions}"  />
		</h:selectManyListbox>
		<h:panelGrid columns="1" cellpadding="0" cellspacing="0"  border="0" >
            <h:commandButton image="/images/icons/approve.gif" title="#{cmsg.edit_navigation_dialog_list_move_right}" value="#{cmsg.edit_navigation_dialog_list_move_right}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.selectAction}" />
            <h:commandButton image="/images/icons/reject.gif" title="#{cmsg.edit_navigation_dialog_list_move_left}" value="#{cmsg.edit_navigation_dialog_list_move_left}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.deselectAction}" />
		</h:panelGrid>

		<h:selectManyListbox id="list-selected-actions" title="#{cmsg.edit_navigation_dialog_list_title}" value="#{DialogManager.bean.highlightedSelectedActions}">
			<f:selectItems id="values-selected-actions" value="#{DialogManager.bean.selectedActions}" />
		</h:selectManyListbox>
		<h:panelGrid columns="1" cellpadding="0" cellspacing="0"  border="0" >
            <h:commandButton image="/images/icons/arrow_up.gif" title="#{cmsg.edit_navigation_dialog_list_move_up}" value="#{cmsg.edit_navigation_dialog_list_move_up}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.moveUpAction}" />
            <h:commandButton image="/images/icons/arrow_down.gif" title="#{cmsg.edit_navigation_dialog_list_move_down}" value="#{cmsg.edit_navigation_dialog_list_move_down}" action="wai:dialog:close:wai:dialog:editNavigationPreferenceDialog" actionListener="#{WaiDialogManager.bean.moveDownAction}" />
		</h:panelGrid>
	</h:panelGrid>

	<h:panelGrid rendered="#{DialogManager.bean.contentNode && NavigationBean.enterpriseEnabled}" columns="3" cellpadding="3" cellspacing="3" border="0">
		<h:outputText value="#{cmsg.edit_navigation_dialog_default_action_file}:" />
		<h:selectOneMenu value="#{DialogManager.bean.previewAction}" >
			<f:selectItem itemLabel="#{cmsg.option_preview}" itemValue="preview" />
			<f:selectItems value="#{DialogManager.bean.selectedPreviewActions}" />
		</h:selectOneMenu>
		<h:outputText value="" />
	</h:panelGrid>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="edit-navigation-misc-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_navigation_dialog_section_misc}" />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">

		<h:outputText value="#{cmsg.edit_navigation_dialog_sort_column}:"  />
		<h:selectOneMenu value="#{DialogManager.bean.sortColumn}" >
			<f:selectItem itemLabel="#{cmsg.edit_navigation_dialog_default}" itemValue="__default_"/>
			<f:selectItems value="#{DialogManager.bean.selectedColumns}" />
		</h:selectOneMenu>
		<h:outputText value=""  />

		<h:outputText value="#{cmsg.edit_navigation_dialog_sort_descending}:"  />
		<h:selectBooleanCheckbox value="#{DialogManager.bean.sortDescending}" />
		<h:outputText value=""  />

		<h:outputText value="#{cmsg.edit_navigation_dialog_list_size}:"  />
		<h:selectOneMenu value="#{DialogManager.bean.listSize}" >
			<f:selectItems value="#{DialogManager.bean.listSizes}" />
		</h:selectOneMenu>
		<h:outputText value="" />
	</h:panelGrid>
	
	<%-- Render options for posts only if we are in topic preferences --%>
	<f:verbatim rendered="#{DialogManager.bean.topicNode}">
		<br /><br />
	</f:verbatim>
	
	<circabc:panel id="edit-navigation-posts-options" rendered="#{DialogManager.bean.topicNode}" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_navigation_dialog_posts_options}" />
	</circabc:panel>
	
	<f:verbatim rendered="#{DialogManager.bean.topicNode}">
		<br />
	</f:verbatim>
	
	<h:panelGrid rendered="#{DialogManager.bean.topicNode}" columns="3" cellpadding="3" cellspacing="3" border="0">
		<h:outputText value="#{cmsg.edit_navigation_dialog_sort_posts_descending}:" />
		<h:selectBooleanCheckbox value="#{DialogManager.bean.sortPostsDescending}" />
		<h:outputText value="" />
	</h:panelGrid>
	
</circabc:panel>