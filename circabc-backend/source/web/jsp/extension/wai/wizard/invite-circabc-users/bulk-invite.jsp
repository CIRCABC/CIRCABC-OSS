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
	
	<script type="text/javascript">
	
		function displayLoader()
		{
			document.getElementById("loaderDiv").style.display='block';
		}
	
	</script>
	

		<f:verbatim><div class="formArea"></f:verbatim>
		
		<f:verbatim><div id="loaderDiv" style="display:none; width:100%; height:75px; text-align:center; line-height:25px;">
			Loading users, please wait.
			<br/></f:verbatim>
			<h:graphicImage value="/images/extension/arrows-loader.gif" alt="loading..."/>
		<f:verbatim></div></f:verbatim>


	<f:verbatim><h4></f:verbatim>
	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_select_user_from_group_title }"></h:outputText>
	<f:verbatim></h4></f:verbatim>
	<h:outputLabel value="#{cmsg.bulk_invite_select_category}:" for="category"></h:outputLabel>
	<h:selectOneMenu id="category" value="#{BulkInviteCircabcUsersWizard.selectedCategory }" styleClass="textNormalWidth" onchange="submit()" valueChangeListener="#{BulkInviteCircabcUsersWizard.refreshAvailableInterestGroups }">
		<f:selectItems value="#{BulkInviteCircabcUsersWizard.availableCategories }"/>
	</h:selectOneMenu>
	<f:verbatim><br/>
	<br/></f:verbatim>
	<h:outputLabel value="#{cmsg.bulk_invite_select_interest_group}:" for="ig"></h:outputLabel>
	
	<h:selectManyListbox value="#{BulkInviteCircabcUsersWizard.selectedAvailableInterestGroups }" style="width:30%;height:150px;float:left;">
		<f:selectItems value="#{BulkInviteCircabcUsersWizard.availableInterestGroups }"/>
	</h:selectManyListbox>
	
	<f:verbatim><div style="float:left; padding:15px;"></f:verbatim>
	<h:commandButton value="" actionListener="#{BulkInviteCircabcUsersWizard.addSelection }" image="/images/extension/icons/move-right.png" style="margin-top:10px;" onclick="displayLoader()"></h:commandButton>
	<f:verbatim><br/></f:verbatim>
	<h:commandButton value="" actionListener="#{BulkInviteCircabcUsersWizard.removeSelection }" image="/images/extension/icons/move-left.png" style="margin-top:35px;" onclick="displayLoader()"></h:commandButton>
	<f:verbatim></div></f:verbatim>
	
	<h:selectManyListbox value="#{BulkInviteCircabcUsersWizard.selectedChosedInterestGroups }" style="width:30%;height:150px;float:left;">
		<f:selectItems value="#{BulkInviteCircabcUsersWizard.chosenConvertedGroups }"/>
	</h:selectManyListbox>
	
	<f:verbatim><br style="clear:both;"/></f:verbatim>

	
	<h:graphicImage alt="" value="/images/extension/icons/cursor.png" style="float:right; margin-right:10%; margin-top:50px;" width="100"></h:graphicImage>
	
	<f:verbatim><h4></f:verbatim>
	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_profile_helper_title }"></h:outputText>
	<f:verbatim></h4></f:verbatim>
	<h:selectBooleanCheckbox  style="margin-left:200px;" id="createProfileHelper" value="#{BulkInviteCircabcUsersWizard.createIgProfileHelper }" valueChangeListener="#{BulkInviteCircabcUsersWizard.refreshIgProfileHelper}" onchange="submit()"></h:selectBooleanCheckbox><h:outputText value="#{cmsg.bulk_invite_profile_helper_description }"></h:outputText>
	<f:verbatim><br/><br/></f:verbatim>
	
	<f:verbatim><h4></f:verbatim>
	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_upload_file }"></h:outputText>
	<f:verbatim></h4></f:verbatim>
	
	<h:outputLabel value="#{cmsg.file}:" for="file"></h:outputLabel>
	<t:inputFileUpload id="file" value="#{BulkInviteCircabcUsersWizard.submittedFile}" size="40" storage="file"></t:inputFileUpload>
	<f:verbatim><br/></f:verbatim>
	<h:commandButton style="margin-left:200px;" value="#{cmsg.add_content_upload }" actionListener="#{BulkInviteCircabcUsersWizard.uploadTemplate }" onclick="displayLoader()"></h:commandButton>
	
		<h:commandLink value="#{cmsg.bulk_invite_upload_file_template_link}"
		title="#{cmsg.bulk_invite_upload_file_template_link}"
		action="#{BulkInviteCircabcUsersWizard.downloadTemplate }"/>
		
	<f:verbatim><br/><br/></f:verbatim>
	
	<h:dataTable var="file" value="#{BulkInviteCircabcUsersWizard.uploadedTemplates }" rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader" width="100%" rendered="#{BulkInviteCircabcUsersWizard.uploadedTemplatesSize > 0 }">
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="#{cmsg.file }" />
		  </f:facet> 
		   <h:outputText value="#{file.name}"></h:outputText>
		</h:column>
		
		<h:column>
		  <f:facet name="header">
		  <h:outputText value="#{cmsg.action }" />
		  </f:facet>
		  <circabc:actionLink id="removeFile-link" value="#{cmsg.bulk_invite_remove_template_file}" tooltip="#{cmsg.bulk_invite_remove_template_file}" actionListener="#{BulkInviteCircabcUsersWizard.removeTemplate}">
				<circabc:param id="idFileName" name="fileName" value="#{file.name}" />
		  </circabc:actionLink>
		</h:column>
		
		<f:facet name="empty">
			<h:outputFormat id="empty-template-list" value="#{cmsg.bulk_invite_empty_template_file_list}" />
		</f:facet>
		
	</h:dataTable>
	
	<f:verbatim><h4></f:verbatim>
	<h:outputText styleClass="signup_subrub_title" value="#{cmsg.bulk_invite_profile_backup_title }"></h:outputText></h4>
	<h:commandButton style="margin-left:200px;" value="#{cmsg.bulk_invite_profile_backup_action_title }" actionListener="#{BulkInviteCircabcUsersWizard.saveWorkTemplate }"></h:commandButton>
	<f:verbatim><br/><br/></f:verbatim>
	
	

<f:verbatim></div></f:verbatim>


<script type="text/javascript" language="javascript">
var formTag = document.getElementById("FormPrincipal");
formTag.setAttribute("enctype","multipart/form-data");

</script>