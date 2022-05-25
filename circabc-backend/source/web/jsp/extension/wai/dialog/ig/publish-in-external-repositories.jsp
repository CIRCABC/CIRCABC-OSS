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
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="publishInExternalRepository" styleClass="contentMainForm">
	
	<f:verbatim><br/><br/>
	
	<div style="padding:5px" class="infoPanel">
		<img src="${currentContextPath}/images/extension/icons/warning.png" alt="Warning" style="float:left; margin-right:96px; width:48px;"></f:verbatim>
		<span style="font-weight:bold; line-height:48px; vertical-align:center;"><h:outputText value="#{cmsg.publish_in_external_repositories_dialog_warning}" /></span>
		<div style="clear:left;"></div>
		<f:verbatim>
	</div>

	<br/><br/>
	<fieldset class="formArea">
	
	</f:verbatim>
	<legend><h:outputText value="#{cmsg.publish_in_external_repositories_dialog_configure}"></h:outputText></legend><br/>
	
	<h:outputLabel value="#{cmsg.publish_in_external_repositories_dialog_subject}" for="subject-mess" />
	<h:inputText styleClass="textNormalWidth" id="subject-mess" value="#{PublishInExternalRepositoriesDialog.subject}" /><br/>
	
	<h:outputLabel value="#{cmsg.publish_in_external_repositories_dialog_comment}" for="comment-mess" />
	<h:inputText styleClass="textLongWidth" id="comment-mess" value="#{PublishInExternalRepositoriesDialog.comment}" /><br/>
	
	<h:outputLabel value="#{cmsg.publish_in_external_repositories_dialog_mail_type}" for="list-available-columns" />
	<h:selectOneMenu id="list-available-columns" title="#{cmsg.publish_in_external_repositories_dialog_mail_type}" value="#{PublishInExternalRepositoriesDialog.selectedMailType}" 
						onchange="submit()" valueChangeListener="#{PublishInExternalRepositoriesDialog.changedMailType}">
		<f:selectItems id="values-available-columns" value="#{PublishInExternalRepositoriesDialog.mailTypes}" />
	</h:selectOneMenu>
	<br/><br/>
	
	<%-- Senders --%>
	
	<h:outputLabel value="#{PublishInExternalRepositoriesDialog.sendersSelectText}" for="sendersPicker" />
	<a:genericPicker id="sendersPicker" showFilter="false" 
			showAddButton="false" multiSelect="false" 
			queryCallback="#{PublishInExternalRepositoriesDialog.pickerSendersCallback}" />
	<br/>
	
	<h:outputLabel value="" />
	<h:commandButton id="AddToListIS" actionListener="#{PublishInExternalRepositoriesDialog.addSelectedSenders}" value="#{msg.add_to_list_button}" /><br/><br/>
	
  	<h:panelGroup>
		
		<h:dataTable value="#{PublishInExternalRepositoriesDialog.selectedSendersDataModel}" var="row"
			rowClasses="selectedItemsRow,selectedItemsRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4"
			rendered="#{PublishInExternalRepositoriesDialog.selectedSendersDataModel.rowCount != 0}" 
			style="margin-left: 200px">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msg.name}" />
				</f:facet>
				<h:outputText value="#{row.authority}" />
			</h:column>
			<h:column>
				<circabc:actionLink actionListener="#{PublishInExternalRepositoriesDialog.removeSelectedSender}"
					image="/images/icons/delete.gif" tooltip="#{msg.remove}" value="#{msg.remove}" showLink="false"
					styleClass="pad6Left" />
			</h:column>
		</h:dataTable>
		<a:panel id="no-itemsIS"
			rendered="#{PublishInExternalRepositoriesDialog.selectedSendersDataModel.rowCount == 0}">
			<br/>
			<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
				<tr>
					<td colspan='2' class='selectedItemsHeader'>
					<h:outputLabel id="no-items-nameIS" value="" /></td>
				</tr>
				<tr>
					<td class='selectedItemsRow'>
						<h:outputText id="no-items-msgIS" value="#{msg.no_selected_items}" />
					</td>
				</tr>
			</table>
		</a:panel>
		
  	</h:panelGroup>
	
	<br/>
	<br/>
	
	<%-- Recipients --%>
	
	<h:outputLabel value="#{PublishInExternalRepositoriesDialog.recipientsSelectText}" for="recipientsPicker" />	
	<a:genericPicker id="recipientsPicker" showFilter="false" 
			showAddButton="false" multiSelect="false" 
			queryCallback="#{PublishInExternalRepositoriesDialog.pickerRecipientsCallback}" />
	<br/>
	
	<h:outputLabel value="" />
	<h:commandButton id="AddToList" actionListener="#{PublishInExternalRepositoriesDialog.addSelectedRecipients}" value="#{msg.add_to_list_button}" /><br/><br/>
	
  	<h:panelGroup>
		
		<h:dataTable value="#{PublishInExternalRepositoriesDialog.selectedRecipientsDataModel}" var="row"
			rowClasses="selectedItemsRow,selectedItemsRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4"
			rendered="#{PublishInExternalRepositoriesDialog.selectedRecipientsDataModel.rowCount != 0}" 
			style="margin-left: 200px">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msg.name}" />
				</f:facet>
				<h:outputText value="#{row.authority}" />
			</h:column>
			<h:column>
				<circabc:actionLink actionListener="#{PublishInExternalRepositoriesDialog.removeSelectedRecipient}"
					image="/images/icons/delete.gif" tooltip="#{msg.remove}" value="#{msg.remove}" showLink="false"
					styleClass="pad6Left" />
			</h:column>
		</h:dataTable>
		<a:panel id="no-items"
			rendered="#{PublishInExternalRepositoriesDialog.selectedRecipientsDataModel.rowCount == 0}">
			<br/>
			<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
				<tr>
					<td colspan='2' class='selectedItemsHeader'>
					<h:outputLabel id="no-items-name" value="" /></td>
				</tr>
				<tr>
					<td class='selectedItemsRow'>
						<h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
					</td>
				</tr>
			</table>
		</a:panel>
		
  	</h:panelGroup>
  	
	<f:verbatim></fieldset></f:verbatim>
	
</circabc:panel>
