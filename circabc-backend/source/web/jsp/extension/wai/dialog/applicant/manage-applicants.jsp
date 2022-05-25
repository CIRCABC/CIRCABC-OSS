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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />

<circabc:panel id="contentMainFormManageApplicants" styleClass="contentMainForm">

	<circabc:displayer id="displayer-manage-applicants--info" rendered="#{DialogManager.bean.hasInformationMessage == true}">
		<circabc:panel id="manage-applicants--info" styleClass="infoPanel" styleClassLabel="infoContent" >
			<h:graphicImage id="manage-applicants-image-info" value="/images/icons/info_icon.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
			<h:outputText id="manage-applicants-text-info-spaces" value="&nbsp;&nbsp;" escape="false" />
			<h:outputText value="#{DialogManager.bean.informationMessage}" />
		</circabc:panel>
		<f:verbatim>
			<br /><br />
		</f:verbatim>
	</circabc:displayer>

	<circabc:displayer id="displayer-manage-applicant-actions-for-all" rendered="#{DialogManager.bean.listEmpty == false}">
		<circabc:panel id="manage-applicant-actions-for-all" styleClass="wai_dialog_more_action" >
			<%-- Refuse all users --%>
		    <circabc:actionLink image="/images/icons/delete_person.gif" id="refuse-all-application" tooltip="#{cmsg.refuse_all_applicants_action_tooltip}" value="#{cmsg.refuse_all_applicants_action_tooltip}" showLink="true" action="wai:dialog:refuseApplicantDialogWai" actionListener="#{RefuseApplicantDialog.refuseApplicantAction}" >
		    	 <circabc:param id="param-refuse-all-node-id" name="id" value="#{DialogManager.bean.currentSpace.id}" />
	             <circabc:param id="param-refuse-all-username" name="userName" value="" />
	             <circabc:param id="param-refuse-service" name="service" value="Administration" />
	             <circabc:param id="param-refuse-activity" name="activity" value="Refuse All Applicants" />
	    	</circabc:actionLink>
		    <h:outputText id="manage-appli-space-1" value=" " />
		    <%-- Invite all users --%>
		    <circabc:actionLink image="/images/icons/invite.gif" id="invite-all-applicant" tooltip="#{cmsg.invite_all_circabc_user_action_tooltip}" value="#{cmsg.invite_all_circabc_user_action_tooltip}" showLink="true" action="wai:wizard:inviteCircabcUsers" actionListener="#{WaiWizardManager.setupParameters}" >
		     	<circabc:param id="param-invite-all-node-id" name="id" value="#{DialogManager.bean.currentSpace.id}" />
		        <circabc:param id="param-invite-all-username" name="users" value="" />
		        <circabc:param id="param-invite-all-filter"   name="filter" value="#{DialogManager.bean.applicantFilterIndex}" />
		        <circabc:param id="param-invite-service" name="service" value="Administration" />
		        <circabc:param id="param-invite-activity" name="activity" value="Invite All Applicants" />
		    </circabc:actionLink>
		</circabc:panel>
		<f:verbatim>
			<br /><br />
		</f:verbatim>
	</circabc:displayer>


	<circabc:richList id="manage-applicants-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{DialogManager.bean.applicants}" var="r" initialSortColumn="date" pageSize="#{BrowseBean.listElementNumber}">
		<%-- Icon column --%>
	    <circabc:column id="manage-applicants-list-icon">
			<f:facet name="header">
				<h:outputText id="manage-applicants-list-header-icon" value="&nbsp;" escape="false" />
			</f:facet>
			<h:graphicImage url="/images/icons/person.gif" />
		</circabc:column>
		<%-- name column --%>
		<circabc:column id="manage-applicants-list-name">
			<f:facet name="header">
				<circabc:sortLink id="manage-applicants-list-sorter" label="#{msg.name}" value="displayName" tooltipAscending="#{cmsg.manage_applicants_dialog_sort_user_asc}" tooltipDescending="#{cmsg.manage_applicants_dialog_sort_user_desc}"/>
			</f:facet>
			<h:outputText id="manage-applicants-list-col-name" value="#{r.displayName}"/>
		</circabc:column>
		<%-- Date column --%>
		<circabc:column id="manage-applicants-list-date">
			<f:facet name="header">
				<circabc:sortLink id="manage-applicants-list-date-sorter" label="#{msg.date}" value="date" tooltipAscending="#{cmsg.manage_applicants_dialog_sort_date_asc}" tooltipDescending="#{cmsg.manage_applicants_dialog_sort_date_desc}"/>
			</f:facet>
			<h:outputText id="manage-applicants-list-col-date" value="#{r.date}">
				<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
			</h:outputText>
		</circabc:column>

	    <%-- message column --%>
	    <circabc:column id="manage-applicants-list-message">
			<f:facet name="header">
		         <h:outputText id="applicant-message-title" value="#{msg.message}" styleClass="header"/>
			</f:facet>
	       <h:outputText id="applicant-message-txt" value="#{r.message}" />
		</circabc:column>

	  	<%-- view actions --%>
	    <circabc:column id="applicant-actions">
	        <f:facet name="header">
	            <a:outputText id="applicant-actions-title" value="#{msg.actions}"/>
	        </f:facet>

	        <%-- View user detail action --%>
	        <circabc:actionLink image="/images/icons/user_console.gif" id="view-user-details" tooltip="#{cmsg.view_user_details_action_tooltip}" value="#{cmsg.view_user_details_action_tooltip}" showLink="false" action="wai:dialog:viewUserDetailsWai" actionListener="#{WaiDialogManager.setupParameters}" >
	            <circabc:param id="param-details-username" name="id" value="#{r.userName}" />
		        <circabc:param id="param-image-name-imageName-detail" name="imageName" value="view_applicant_detail_#{r.userName}" />
	        </circabc:actionLink>
	    	<h:outputText id="manage-appli-space-2" value=" " />

	        <%-- Refuse user detail action --%>
	        <circabc:actionLink image="/images/icons/delete_person.gif" id="refuse-application" tooltip="#{cmsg.refuse_applicants_action_tooltip}" value="#{cmsg.refuse_applicants_action_tooltip}" showLink="false" action="wai:dialog:refuseApplicantDialogWai" actionListener="#{RefuseApplicantDialog.refuseApplicantAction}" >
	        	<circabc:param id="param-refuse-node-id" name="id" value="#{DialogManager.bean.currentSpace.id}" />
	            <circabc:param id="param-refuse-username" name="userName" value="#{r.userName}" />
		        <circabc:param id="param-image-name-imageName-refuse" name="imageName" value="refuse_applicant_#{r.userName}" />
		        <circabc:param id="param-refuse-application-service" name="service" value="Administration" />
		        <circabc:param id="param-refuse-application-activity" name="activity" value="Refuse user" />
	        </circabc:actionLink>
	   			<h:outputText id="manage-appli-space-3" value=" " />

	        <%-- Invite user detail action --%>
	        <circabc:actionLink image="/images/icons/invite.gif" id="invite-applicant" tooltip="#{cmsg.invite_circabc_user_action_tooltip}" value="#{cmsg.invite_circabc_user_action_tooltip}" showLink="false" action="wai:wizard:inviteCircabcUsers" actionListener="#{WaiWizardManager.setupParameters}" >
	          	<circabc:param id="param-invite-node-id" name="id" value="#{DialogManager.bean.currentSpace.id}" />
	            <circabc:param id="param-invite-username" name="users" value="#{r.email}" />
	            <circabc:param id="param-invite-filter"   name="filter" value="0" />
		        <circabc:param id="param-image-name-imageName-accept" name="imageName" value="invite_applicant_#{r.userName}" />
		        <circabc:param id="param-invite-applicant-service" name="service" value="Administration" />
		        <circabc:param id="param-invite-applicant-activity" name="activity" value="Invite user" />
	        </circabc:actionLink>
	    </circabc:column>
		<f:facet name="empty">
			<h:outputFormat id="manage-apllicants-list-container" value="#{cmsg.no_applicant_found}"/>
		</f:facet>
		<circabc:dataPager id="manage-applicants-pager" styleClass="pagerCirca" />
	</circabc:richList>

</circabc:panel>
