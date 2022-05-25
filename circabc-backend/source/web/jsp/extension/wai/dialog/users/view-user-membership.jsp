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

<f:verbatim>
	<br />
</f:verbatim>

<circabc:panel id="messagePanel" rendered="#{!ViewUserMembershipDialog.allowedToView}">
	<h:outputText value="#{cmsg.view_user_membership_not_allowed}"   />
</circabc:panel>

<circabc:panel id="view-user-membership-title" styleClass="signup_rub_title" tooltip="#{cmsg.view_user_membership_header_tooltip}"
	rendered="#{ViewUserMembershipDialog.allowedToView}">
	<h:outputText value="#{cmsg.view_user_membership_header}"   />
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<circabc:richList id="categoryRoles" viewMode="circa" 
	styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" 
	value="#{WaiDialogManager.bean.categoryRoles}" var="u" pageSize="#{BrowseBean.listElementNumber}"
	rendered="#{DialogManager.bean.myProfile}">
	<%-- category column --%>
	<circabc:column id="category-category">
		<f:facet name="header">
	         <h:outputText id="category-category-header" value="#{cmsg.category_column_header}" styleClass="header"/>
		</f:facet>
		<circabc:actionLink value="#{u.category}" tooltip="#{u.category}" actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();">
				<circabc:param name="id" value="#{u.categoryNodeId}" />
		</circabc:actionLink>
	</circabc:column>
	<circabc:column id="category-profile">
		<f:facet name="header">
	         <h:outputText id="category-profile-header" value="#{cmsg.profile_column_header}" styleClass="header"/>
		</f:facet>
		<h:outputText id="category-profile-text" value="#{u.profile}"/>
	</circabc:column>
	<circabc:column id="category-action-column">
		<circabc:actionLink id="view-user-membership-category-col-actions-remove" value="#{cmsg.remove_membership}" 
		tooltip="#{cmsg.remove_membership}" image="/images/icons/delete_person.gif" showLink="false" 
		action="wai:dialog:removeUserProfileDialogWai" actionListener="#{WaiDialogManager.setupParameters}">
				<circabc:param id="view-user-membership-category-col-actions-remove-par-id" name="id" value="#{u.categoryNodeId}" />
				<circabc:param id="view-user-membership-category-col-actions-remove-par-user" name="userName" value="#{DialogManager.bean.userName}" />
				<circabc:param id="view-user-membership-category-col-actions-remove-par-fullname" name="fullName" value="#{DialogManager.bean.userFullName}" />
				<circabc:param id="view-user-membership-category-col-actions-remove-par-profile" name="profile" value="#{u.profile}" />
				<circabc:param id="view-user-membership-category-col-actions-remove-par-interest-group-name" name="categoryName" value="#{u.category}" />
				<circabc:param id="view-user-membership-category-col-actions-remove-par-service" name="service" value="Administration" />
				<circabc:param id="view-user-membership-category-col-actions-remove-par-activity" name="activity" value="Remove membership" />
		</circabc:actionLink>
	</circabc:column>
	<f:facet name="empty">
		<h:outputFormat id="no-items-category-results" value="#{cmsg.view_user_membership_no_list_items_category}" />
	</f:facet>
	<circabc:dataPager id="view-membership-category-pager" styleClass="pagerCirca" />
</circabc:richList>

<circabc:richList id="igRoles" viewMode="circa" 
	styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" 
	value="#{WaiDialogManager.bean.igRoles}" var="u" pageSize="#{BrowseBean.listElementNumber}"
	rendered="#{ViewUserMembershipDialog.allowedToView}">
	<%-- category column --%>
	<circabc:column id="category-column">
		<f:facet name="header">
	         <h:outputText id="category-column-header" value="#{cmsg.category_column_header}" styleClass="header"/>
		</f:facet>
		<circabc:actionLink value="#{u.categoryTitle}" tooltip="#{u.categoryTitle}" actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();">
				<circabc:param name="id" value="#{u.categoryNodeId}" />
		</circabc:actionLink>
	</circabc:column>
	<circabc:column id="interest-group-column">
		<f:facet name="header">
	         <h:outputText id="interest-group-column-header" value="#{cmsg.interest_group_column_header}" styleClass="header"/>
		</f:facet>
		<h:outputText id="interest-group-text" value=""/>
		<circabc:actionLink value="#{u.interestGroupTitle}" tooltip="#{u.interestGroupTitle}" actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();">
				<circabc:param name="id" value="#{u.interestGroupNodeId}" />
		</circabc:actionLink>
	</circabc:column>
	<circabc:column id="profile-column">
		<f:facet name="header">
	         <h:outputText id="profile-column-header" value="#{cmsg.profile_column_header}" styleClass="header"/>
		</f:facet>
		<h:outputText id="profile-text" value="#{u.profileTitle}"/>
	</circabc:column>
	<circabc:column id="action-column">
		<circabc:actionLink id="view-user-membership-col-actions-remove" value="#{cmsg.remove_membership}" 
		tooltip="#{cmsg.remove_membership}" image="/images/icons/delete_person.gif" 
		showLink="false" action="wai:dialog:removeUserProfileDialogWai" actionListener="#{WaiDialogManager.setupParameters}"
		rendered="#{DialogManager.bean.myProfile && !u.imported}">
			<circabc:param id="view-user-membership-col-actions-remove-par-id" name="id" value="#{u.interestGroupNodeId}" />
			<circabc:param id="view-user-membership-col-actions-remove-par-user" name="userName" value="#{DialogManager.bean.userName}" />
			<circabc:param id="view-user-membership-col-actions-remove-par-fullname" name="fullName" value="#{DialogManager.bean.userFullName}" />
			<circabc:param id="view-user-membership-col-actions-remove-imageName" name="imageName" value="remove_#{u.interestGroup}" />
			<circabc:param id="view-user-membership-col-actions-remove-par-profile" name="profile" value="#{u.profile}" />
			<circabc:param id="view-user-membership-col-actions-remove-par-interest-group-name" name="interestGroupName" value="#{u.interestGroup}" />
			<circabc:param id="view-user-membership-col-actions-remove-par-service" name="service" value="Administration" />
			<circabc:param id="view-user-membership-col-actions-remove-par-activity" name="activity" value="Remove membership" />
		</circabc:actionLink>
	</circabc:column>
	<f:facet name="empty">
		<h:outputFormat id="no-items-interest-group-results" value="#{cmsg.view_user_membership_no_list_items_ig}" />
	</f:facet>
	<circabc:dataPager id="view-membership-ig-pager" styleClass="pagerCirca" />
</circabc:richList>
