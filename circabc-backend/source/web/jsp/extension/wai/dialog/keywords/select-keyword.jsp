<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? � as soon they
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

<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<script language="javascript">
    function updateList(){
        document.getElementById("FormPrincipal:submit-change-lang").click();
    }
</script>

<circabc:panel id="contentMainFormSetDocumentKeyword" styleClass="contentMainForm">

<!--  New keyword panel -->
	<circabc:panel id="add-keywords-first-section" styleClass="signup_rub_title">
		<h:outputText value="1.&nbsp;#{cmsg.set_document_keyword_dialog_section_select}" escape="false" />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:outputText id="set-keywords-lang-text" value="#{cmsg.manage_keyword_dialog_language_filter}:&nbsp;" escape="false"/>
	<h:selectOneMenu id="set-keywords-language" value="#{DialogManager.bean.selectedLanguage}" onchange="updateList()" valueChangeListener="#{SelectKeywordsForSearchDialog.updateList}" immediate="true"  >
		<f:selectItems id="set-keywords-languages" value="#{DialogManager.bean.languages}" />
	</h:selectOneMenu>
	<h:outputText id="set-keywords-lang-text-spaces-submit" value="&nbsp;&nbsp;" escape="false" />
	<h:commandButton id="submit-change-lang" styleClass="" value="filter" action="#{DialogManager.bean.getDialogCloseAndLaunchAction}" rendered="true" immediate="true" title="#{cmsg.manage_keyword_dialog_apply_filter_tooltip}" />

	<f:verbatim>
		<br /><br /><br />
	</f:verbatim>

	<h:selectManyListbox id="set-keywords-list" style="max-width: 100%;" size="8">
		<f:selectItems value="#{DialogManager.bean.interestGroupKeywords}" />
	</h:selectManyListbox>

	<f:verbatim>
		<br /><br /><br />
	</f:verbatim>

	<circabc:panel id="add-keywords-second-section" styleClass="signup_rub_title">
		<h:outputText id="set-keywords-add-to-list-space" value="2.&nbsp;" escape="false" />
		<h:commandButton value="#{msg.add_to_list_button}"
			actionListener="#{DialogManager.bean.addSelection}"
			styleClass="wizardButton" />

	</circabc:panel>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<h:dataTable value="#{DialogManager.bean.settedKeywordsDataModel}" var="row"
		rowClasses="selectedItemsRow,selectedItemsRowAlt"
		styleClass="selectedItems" headerClass="selectedItemsHeader"
		cellspacing="0" cellpadding="4"
		rendered="#{DialogManager.bean.settedKeywordsDataModel.rowCount != 0}">

		<h:column>
			<f:facet name="header">
				<h:outputText id="col-name" value="#{msg.name}" />
			</f:facet>
			<h:outputText value="#{row.string}" />
		</h:column>
		<h:column>
			<circabc:actionLink actionListener="#{DialogManager.bean.removeSelection}"
				image="/images/icons/delete.gif" value="#{msg.remove}" showLink="false"
				styleClass="pad6Left" tooltip="#{msg.remove}" />
		</h:column>
	</h:dataTable>
	<a:panel id="no-items"
		rendered="#{DialogManager.bean.settedKeywordsDataModel.rowCount == 0}">
		<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
			<tr>
				<td class='selectedItemsRow'>
					<h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
				</td>
			</tr>
		</table>
	</a:panel>

</circabc:panel>