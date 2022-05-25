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
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<f:verbatim><div class="formArea"></f:verbatim>

			<f:verbatim><h4></f:verbatim>
			<h:outputText value="#{cmsg.bulk_invite_bulk_tools_title }"></h:outputText>
			<f:verbatim></h4></f:verbatim>
			
			<f:verbatim><div style="float:right;margin-right:15px;"></f:verbatim>
			
			 <h:outputLabel value="#{cmsg.bulk_invite_user_notify_title}"></h:outputLabel>
			 <h:selectBooleanCheckbox value="#{BulkInviteCircabcUsersWizard.notifyInvitations }"></h:selectBooleanCheckbox>
			
			<f:verbatim></div></f:verbatim>
	
			<h:selectOneMenu id="profileSelection" value="#{BulkInviteCircabcUsersWizard.selectedProfile }" >
		   		<f:selectItems value="#{BulkInviteCircabcUsersWizard.igProfilesAvailable }"/>
		   </h:selectOneMenu>
	
		   <h:outputLabel value="#{cmsg.bulk_invite_bulk_change_profile_title}"></h:outputLabel>
		   <h:commandButton value="#{cmsg.bulk_invite_bulk_update_title}" actionListener="#{BulkInviteCircabcUsersWizard.updateProfilesForSelectedUsers }"></h:commandButton>
		   <h:outputText value="#{cmsg.bulk_invite_bulk_selecte_users}"></h:outputText>
		   
		   <f:verbatim><br/></f:verbatim>
	
			<h:outputLabel value=""></h:outputLabel>
			<h:commandButton value="#{cmsg.bulk_invite_bulk_remove_title}" actionListener="#{BulkInviteCircabcUsersWizard.removeSelectedUsers }"></h:commandButton>
			<h:outputText value="#{cmsg.bulk_invite_bulk_selecte_users}"></h:outputText>
			
			<f:verbatim><br/></f:verbatim>
			
			
			<f:verbatim><h4></f:verbatim>
			<h:outputText value="#{cmsg.bulk_invite_big_table_title }"></h:outputText>
			<f:verbatim></h4></f:verbatim>
			
			<f:verbatim><div style="float:right; text-align:right;margin-right:15px;"></f:verbatim>
				<h:outputText value="#{cmsg.bulk_invite_user_status_ok }" style="margin-right:5px;"> </h:outputText>
				<h:graphicImage alt="Ok" id="okInvitation" title="#{cmsg.bulk_invite_user_status_ok_tooltip }" value="/images/extension/icons/BulletGreen.png"></h:graphicImage>
				<f:verbatim><br/></f:verbatim>
				<h:outputText value="#{cmsg.bulk_invite_user_status_already_member }" style="margin-right:5px;"> </h:outputText>
				<h:graphicImage alt="alreadyMember" id="alreadyMemberInvitation" title="#{cmsg.bulk_invite_user_status_already_invited_tooltip }" value="/images/extension/icons/BulletOrange.png" ></h:graphicImage>
				<f:verbatim><br/></f:verbatim>
				<h:outputText value="#{cmsg.bulk_invite_user_status_not_ok }" style="margin-right:5px;"> </h:outputText>
				<h:graphicImage alt="not Ok" id="notOkInvitation" title="#{cmsg.bulk_invite_user_status_not_ok_tooltip }" value="/images/extension/icons/BulletRed.png" ></h:graphicImage>
			<f:verbatim></div></f:verbatim>
			
			
			<circabc:actionLink value="" tooltip="" image="/images/extension/icons/checked.png" rendered="#{BulkInviteCircabcUsersWizard.selectedAllUsers == true}" actionListener="#{BulkInviteCircabcUsersWizard.unselectUser }">
		   		<circabc:param value="true" name="all"></circabc:param>
		   </circabc:actionLink>
		   
		   <circabc:actionLink value="" tooltip="" image="/images/extension/icons/unchecked.png" rendered="#{BulkInviteCircabcUsersWizard.selectedAllUsers == false}" actionListener="#{BulkInviteCircabcUsersWizard.selectUser }">
		   		<circabc:param value="true" name="all"></circabc:param>
		   </circabc:actionLink>
		   
		   <h:outputText value="#{cmsg.bulk_invite_select_unselect_label }" style="margin-left:5px;"> </h:outputText>
		   
		  <f:verbatim> <br/><br/></f:verbatim>
	
	<h:dataTable var="row" value="#{BulkInviteCircabcUsersWizard.model  }" rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader" width="100%">
	
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="" />
		  </f:facet> 
		   <circabc:actionLink value="" tooltip="" image="/images/extension/icons/checked.png" rendered="#{row.selected == true}" actionListener="#{BulkInviteCircabcUsersWizard.unselectUser }">
		   	<circabc:param value="#{row.user.email}" name="mail"></circabc:param>
		   </circabc:actionLink>
		   
		   <circabc:actionLink value="" tooltip="" image="/images/extension/icons/unchecked.png" rendered="#{row.selected == false}" actionListener="#{BulkInviteCircabcUsersWizard.selectUser }">
		   	<circabc:param value="#{row.user.email}" name="mail"></circabc:param>
		   </circabc:actionLink>
		</h:column>
	
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Ig" />
		  </f:facet> 
		   <h:outputText value="#{row.igName}" rendered="#{row.igName != null }"></h:outputText>
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Group" />
		  </f:facet> 
		   <h:outputText value="#{row.departmentNumber}" rendered="#{row.departmentNumber != null }"></h:outputText>
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="File" />
		  </f:facet> 
		   <h:outputText value="#{row.fromFile}" rendered="#{row.fromFile != null }"></h:outputText>
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Username" />
		  </f:facet> 
		   <h:outputText value="#{row.user.ecasUserName}"></h:outputText>
		    <h:outputText value="#{row.expectedUsername}" rendered="#{row.user == null}"></h:outputText>
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Email" />
		  </f:facet> 
		   <h:outputText value="#{row.user.email}"></h:outputText>
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Profile" />
		  </f:facet> 
		   <h:outputText value="#{row.profile}"></h:outputText>		   
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Status" />
		  </f:facet> 
		  <h:graphicImage alt="Ok" id="okInvitationTable" title="#{cmsg.bulk_invite_user_status_ok_tooltip }" value="/images/extension/icons/BulletGreen.png" rendered="#{row.status == 'ok'}"></h:graphicImage>
		  <h:graphicImage alt="Error" id="errorInvitationTable" title="#{cmsg.bulk_invite_user_status_not_ok_tooltip }" value="/images/extension/icons/BulletRed.png" rendered="#{row.status == 'ignore' || row.status == 'nok'}"></h:graphicImage>
		  <h:graphicImage alt="Already Member" id="alreadyMemberInvitationTable" title="#{cmsg.bulk_invite_user_status_already_invited_tooltip }" value="/images/extension/icons/BulletOrange.png" rendered="#{row.status == 'member'}"></h:graphicImage>
		</h:column>
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="Actions" />
		  </f:facet> 
		   
		   <circabc:actionLink value="" tooltip="#{cmsg.event_delete_meeting_action_title }" image="/images/extension/help/delete.gif" actionListener="#{BulkInviteCircabcUsersWizard.removeUserSelection }">
		   		<circabc:param value="#{row.user.email}" name="mail"></circabc:param>
		   		<circabc:param value="#{row.expectedUsername}" name="username"></circabc:param>
		   </circabc:actionLink>
		</h:column>
	
	</h:dataTable>
	
	<f:verbatim><h4></f:verbatim>
	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_profile_backup_title }"></h:outputText>
	<f:verbatim></h4></f:verbatim>
	<h:commandButton style="margin-left:200px;" value="#{cmsg.bulk_invite_profile_backup_action_title }" actionListener="#{BulkInviteCircabcUsersWizard.saveWorkTemplate }"></h:commandButton>
	<f:verbatim><br/><br/></f:verbatim>
	
	<f:verbatim></div></f:verbatim>