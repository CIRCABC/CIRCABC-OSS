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

		<circabc:panel id="edit-profile-first-section" styleClass="signup_rub_title">
			<h:outputText value="1.&nbsp;#{cmsg.edit_access_profile_dialog_section_1}" escape="false" />
		</circabc:panel>

		<f:verbatim>
			<br />
		</f:verbatim>

		<circabc:displayer id="render-title-edit" rendered="#{DialogManager.bean.titleEditable == true}">
			<h:selectOneMenu id="edit-profile-language" value="#{DialogManager.bean.language}" >
					<f:selectItems id="edit-profile-languages" value="#{DialogManager.bean.languages}" />
				</h:selectOneMenu>
				<h:inputText id="edit-profile-value" value="#{DialogManager.bean.value}" />

				<h:commandButton id="AddToList" value="#{msg.add_to_list_button}"
					actionListener="#{DialogManager.bean.addSelection}"
					styleClass="wizardButton" />

				<f:verbatim>
					<br /><br />
				</f:verbatim>

				<h:dataTable value="#{DialogManager.bean.translationDataModel}" var="row"
					rowClasses="selectedItemsRow,selectedItemsRowAlt"
					styleClass="selectedItems" headerClass="selectedItemsHeader"
					cellspacing="0" cellpadding="4"
					rendered="#{DialogManager.bean.translationDataModel.rowCount != 0}">
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msg.language}" />
						</f:facet>
						<h:outputText id="col-lang" value="#{row.key}" />
					</h:column>
					<h:column>
						<f:facet name="header">
							<h:outputText id="col-name" value="#{msg.name}" />
						</f:facet>
						<h:outputText value="#{row.value}" />
					</h:column>
					<h:column>
						<h:commandButton id="RemoveFromList" actionListener="#{DialogManager.bean.removeSelection}"
							image="/images/icons/delete.gif" styleClass="pad6Left" />
					</h:column>
				</h:dataTable>
				<a:panel id="no-items"
					rendered="#{DialogManager.bean.translationDataModel.rowCount == 0}">
					<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
						<tr>
							<td class='selectedItemsRow'>
								<h:outputText id="no-items-msg" value="#{cmsg.edit_access_profile_dialog_empty_titles}" />
							</td>
						</tr>
					</table>
				</a:panel>
		</circabc:displayer>

		<circabc:displayer id="render-notitle-edit" rendered="#{DialogManager.bean.titleEditable == false}">
				<h:outputText id="notitle-edit-explain" value="#{cmsg.edit_access_profile_dialog_section_not_editable}"  styleClass="noItem" />
		</circabc:displayer>


		<f:verbatim>
			<br /><br />
		</f:verbatim>

		<circabc:panel id="edit-profile-second-section" styleClass="signup_rub_title">
			<h:outputText value="2.&nbsp;#{cmsg.edit_access_profile_dialog_section_2}" escape="false" />
		</circabc:panel>

		<f:verbatim>
			<br />
		</f:verbatim>

		<h:dataTable value="#{DialogManager.bean.permissionDataModel}" var="row"
			rowClasses="selectedItemsRow,selectedItemsRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4"
			rendered="#{DialogManager.bean.permissionDataModel.rowCount != 0}">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{cmsg.service}" />
				</f:facet>
				<h:outputText id="col-service" value="#{row.serviceLabel}" />
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText id="col-perm" value="#{cmsg.permission}" />
				</f:facet>
				<h:selectOneMenu id="SelectPermission" value="#{row.permissionValue}" disabled="#{row.locked}">
						<f:selectItems value="#{row.permissions}" />
				</h:selectOneMenu>
			</h:column>
		</h:dataTable>

		<f:verbatim>
			<br /><br />
		</f:verbatim>

	</circabc:panel>

</r:permissionEvaluator>



