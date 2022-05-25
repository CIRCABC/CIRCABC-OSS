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

<circabc:panel id="contentMainFormManageShareSpace" styleClass="contentMainForm">

	<!--  Action to invite new ig to share space -->
	<circabc:panel id="manage-share-space-add-ig-section" styleClass="wai_dialog_more_action" >
		<h:graphicImage value="/images/extension/icons/new_rule_small.gif" alt="#{cmsg.add_new_ig_dialog_action_tooltip}" />
		<h:outputText id="manage-share-space-add-ig" value="&nbsp;" escape="false" />
		<circabc:actionLink id="manage-share-space-act-add" tooltip="#{cmsg.add_new_ig_dialog_action_tooltip}" value="#{cmsg.add_new_ig_dialog_action_title}" showLink="false" action="wai:wizard:sharingSpace" actionListener="#{WaiWizardManager.setupParameters}" >
			<circabc:param id="id" name="id" value="#{DialogManager.bean.actionNode.nodeRef.id}" />
			<circabc:param id="manage-share-space-service" name="service" value="Library" />
			<circabc:param id="manage-share-space-activity" name="activity" value="Add shared space" />
		</circabc:actionLink>
	</circabc:panel>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<!--  Display the dynamic properties -->
	<circabc:richList id="manage-share-space-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{DialogManager.bean.invitedInterestGroups}" var="ig" initialSortColumn="name" pageSize="#{BrowseBean.listElementNumber}">
		<circabc:column id="manage-share-space-list-name">
			<f:facet name="header">
				<h:outputText id="manage-share-space-list-header-name" value="#{cmsg.interest_group_name}"  />
			</f:facet>
			<h:outputText id="manage-share-space-list-col-name" value="#{ig.igTitle}"/>
		</circabc:column>
		<circabc:column id="manage-share-space-list-type">
			<f:facet name="header">
				<h:outputText id="manage-share-space-list-header-type" value="#{cmsg.interest_group_permission}"  />
			</f:facet>
			<h:outputText id="manage-share-space-list-col-type" value="#{ig.permissionTitle}"/>
		</circabc:column>
		<circabc:column id="manage-share-space-list-actions-col">
			<f:facet name="header">
				<h:outputText id="manage-share-space-list-container-action" value="#{cmsg.actions}"  />
			</f:facet>
			<circabc:actionLink image="/images/icons/delete.gif" id="manage-share-space-delete-ig" tooltip="#{cmsg.delete_share_space_ig_action_tooltip}" value="#{cmsg.delete_share_space_ig_action_tooltip}" showLink="false" action="wai:dialog:removeIGShareSpaceDialog" actionListener="#{WaiDialogManager.setupParameters}" >
				<circabc:param id="param-delete-ig-id" name="interestGroupID" value="#{ig.id}" />
				<circabc:param id="param-delete-share-space-id" name="shareSpaceID" value="#{DialogManager.bean.actionNode.nodeRef}" />
				<circabc:param id="param-delete-ig-name" name="interestGroupName" value="#{ig.igTitle}" />
				<circabc:param id="param-delete-ig-imageName" name="imageName" value="remove_#{ig.name}" />
				<circabc:param id="param-delete-share-space-service" name="service" value="Library" />
				<circabc:param id="param-delete-share-space-activity" name="activity" value="Delete Shared Space" />
			</circabc:actionLink>
		</circabc:column>
		<f:facet name="empty">
			<h:outputFormat id="manage-interest-group-list-container" value="#{cmsg.manage_share_space_dialog_no_list_items}"  />
		</f:facet>
		<circabc:dataPager id="manage-share-space-pager" styleClass="pagerCirca" />
	</circabc:richList>
</circabc:panel>