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



<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="add-property-first-section" styleClass="signup_rub_title">
	<h:outputText value="1.&nbsp;#{cmsg.define_description_desc}" escape="false" />
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<h:selectOneMenu id="define-property-language" value="#{DialogManager.bean.language}">
	<f:selectItems value="#{DialogManager.bean.languages}" />
</h:selectOneMenu>

<h:inputText id="define-property-text" value="#{DialogManager.bean.text}" />
<h:commandButton id="AddDescription" value="#{cmsg.edit_property_dialog_add_desc_button}" actionListener ="#{DialogManager.bean.addDescription}" />

<f:verbatim>
	<br /><br /><br />
</f:verbatim>

<circabc:panel id="add_new_property_dialog_section_add" styleClass="signup_rub_title">
	<h:outputText value="2.&nbsp;#{cmsg.edit_property_dialog_section_title}" escape="false" />
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>


<h:dataTable value="#{DialogManager.bean.translationDataModel}" var="row"
		rowClasses="selectedItemsRow,selectedItemsRowAlt"
		styleClass="selectedItems" headerClass="selectedItemsHeader"
		cellspacing="0" cellpadding="4"
		rendered="#{DialogManager.bean.translationDataModel.rowCount != 0}">
		<h:column>
			<f:facet name="header">
				<h:outputText id="edit-dyn-prop-col-1-header" value="#{cmsg.define_property_dialog_language}" />
			</f:facet>
			<h:outputText id="edit-dyn-prop-col-1-value" value="#{row.language}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText id="edit-dyn-prop-col-2-header" value="#{cmsg.define_property_dialog_translation}" />
			</f:facet>
			<h:outputText id="edit-dyn-prop-col-2-value" value="#{row.value}" />
		</h:column>
		<h:column>
			<circabc:actionLink id="define-dyn-prop-col-3-value"
				tooltip="#{cmsg.remove}"
				actionListener="#{DialogManager.bean.removeSelection}"
				image="/images/icons/delete.gif" value="#{msg.remove}" showLink="false"
				styleClass="pad6Left" />
		</h:column>
	</h:dataTable>

	<a:panel id="no-items"
		rendered="#{DialogManager.bean.translationDataModel.rowCount == 0}">
		<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
			<tr>
				<td class='selectedItemsRow'>
					<h:outputText id="edit-dyn-prop-no-items-msg" value="#{cmsg.define_property_dialog_no_list_items}" />
				</td>
			</tr>
		</table>
	</a:panel>
