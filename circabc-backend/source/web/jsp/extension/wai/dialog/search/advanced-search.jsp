
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

<script type="text/javascript">

window.onload = pageLoaded;

function pageLoaded()
{
	document.getElementById("FormPrincipal:search-text").focus();
}


function loadSearch()
{
        document.getElementById("FormPrincipal:submit-load-search").click();
}
</script>


<circabc:panel id="contentMainFormAdvancedSearch" styleClass="contentMainForm">

	<circabc:panel id="advanced-search-look-for-section" styleClass="wai_dialog_more_action_left_desc">
		<h:outputText value="#{msg.look_for}" style="font-weight:bold" /><f:verbatim>:&nbsp;</f:verbatim>
		<h:inputText id="search-text" value="#{CircabcSearchProperties.text}" size="48" maxlength="1024" />
		<f:verbatim>&nbsp;&nbsp;</f:verbatim>
		<h:outputText value="#{cmsg.advanced_search_dialog_force_and}" style="font-weight:bold" title="#{cmsg.advanced_search_dialog_force_and_tooltip}"/><f:verbatim>:&nbsp;</f:verbatim>
		<h:selectBooleanCheckbox id="search-force-and" value="#{CircabcAdvancedSearchDialog.forceAnd}" title="#{cmsg.advanced_search_dialog_force_and_tooltip}"/>
	</circabc:panel>

	<circabc:panel id="advanced-search-reset-section" styleClass="wai_dialog_more_action">
		<circabc:actionLink value="#{msg.resetall}" tooltip="#{msg.resetall}" image="/images/icons/delete.gif" padding="2" actionListener="#{CircabcAdvancedSearchDialog.reset}" />
	</circabc:panel>

	<f:verbatim><br /><br /></f:verbatim>

	<circabc:panel id="panelAdvSearchLoadSaved" label="#{cmsg.advanced_search_dialog_load_title}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{msg.advanced_search_dialog_load_title}">
		<f:verbatim><br /></f:verbatim>
		<h:selectOneMenu id="searches" value="#{CircabcAdvancedSearchDialog.savedSearch}" onchange="loadSearch()" valueChangeListener="#{CircabcAdvancedSearchDialog.updateSavedSearch}" immediate="true">
				<f:selectItems value="#{CircabcAdvancedSearchDialog.savedSearches}" />
		</h:selectOneMenu>
		<h:outputText id="adv-search-load-search-spaces" value="&nbsp;&nbsp;" escape="false" />
		<h:commandButton id="submit-load-search" styleClass="" value="#{cmsg.advanced_search_dialog_load}" action="wai:dialog:close:wai:dialog:advancedSearchDialogWai" rendered="true" immediate="true" title="#{cmsg.advanced_search_dialog_load_tooltip}" actionListener="#{WaiDialogManager.setupParameters}"  >
			 <circabc:param id="param-no-reset" name="reset" value="false" />
		</h:commandButton>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:panel>


	<circabc:panel id="topOfPageAnchorAdvSearch-0" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorAdvSearch-0-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-0-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelAdvSearchResFor" label="#{msg.show_results_for}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{msg.show_results_for}">
		<h:selectOneRadio value="#{CircabcSearchProperties.mode}" layout="pageDirection" id="radioMode" >
			<f:selectItem itemValue="all" itemLabel="#{msg.all_items}" id="selectAll" />
			<f:selectItem itemValue="files_text" itemLabel="#{msg.file_names_contents}" id="selectFilesText" />
			<f:selectItem itemValue="files" itemLabel="#{msg.file_names}" id="selectFiles" />
			<f:selectItem itemValue="folders" itemLabel="#{msg.space_names}" id="selectFolders" />
		</h:selectOneRadio>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-1" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorAdvSearch-1-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-1-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelAdvSearchLookin" label="#{msg.look_in}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{msg.look_in}" rendered="true" >
		<h:selectOneRadio value="#{CircabcSearchProperties.lookin}" layout="pageDirection" id="radiolookin"  >
			<f:selectItem itemValue="interestGroup" itemLabel="#{cmsg.advanced_search_dialog_look_current_ig}" id="selectInterestGroup"  />
			<f:selectItem itemValue="service" itemLabel="#{cmsg.advanced_search_dialog_look_current_service}" id="selectServive" itemDisabled="#{CircabcAdvancedSearchDialog.lookinCurrentLocationDisable}" />
			<f:selectItem itemValue="currentLocation" itemLabel="#{cmsg.advanced_search_dialog_look_current_location}" id="selectCurrentLocation" itemDisabled="#{CircabcAdvancedSearchDialog.lookinCurrentLocationDisable}" />
		</h:selectOneRadio>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-2" styleClass="topOfPageAnchor" rendered="true" >
		<circabc:actionLink id="topOfPageAnchorAdvSearch-2-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-2-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelAdvSearchAlso" label="#{msg.also_search_results}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{msg.also_search_results}" >

		<h:panelGrid columns="2" cellpadding="3" cellspacing="5" border="0"  >

			<h:outputText value="#{msg.content_format}&nbsp;:" id="contentFormat" escape="false"/>
			<h:selectOneMenu value="#{CircabcSearchProperties.contentFormat}" id="selectContentFormat">
				<f:selectItems value="#{CircabcAdvancedSearchDialog.contentFormats}" id="contentFormats" />
			</h:selectOneMenu>

			<h:outputText value="#{msg.title}&nbsp;:" id="title" escape="false"/>
			<h:inputText value="#{CircabcSearchProperties.title}" size="#{TextFieldGenerator.size}" maxlength="1024" id="txtTitle" />

			<h:outputText value="#{msg.description}&nbsp;:" id="desc" escape="false"/>
			<h:inputText value="#{CircabcSearchProperties.description}" size="#{TextFieldGenerator.size}" maxlength="1024" id="txtDesc" />

			<h:outputText value="#{msg.author}&nbsp;:" id="author" escape="false"/>
			<h:inputText value="#{CircabcSearchProperties.author}" size="#{TextFieldGenerator.size}" maxlength="1024" id="txtAuthor" />
		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>
		<h:selectBooleanCheckbox value="#{CircabcSearchProperties.modifiedDateChecked}" id="chkModDate" />
		<h:outputText value="#{msg.modified_date}&nbsp;:" id="modDate" escape="false"/>

		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >
			<h:outputText value="#{msg.from}&nbsp;:" id="modDateFrom" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.modifiedDateFrom}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateModFrom" initialiseIfNull="true" />

			<h:outputText value="#{msg.to}&nbsp;:" id="modDateTo" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.modifiedDateTo}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateModTo" initialiseIfNull="true" />
		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>
		<h:selectBooleanCheckbox value="#{CircabcSearchProperties.createdDateChecked}" id="chkCreateDate" />
		<h:outputText value="#{msg.created_date}&nbsp;:" id="createDate" escape="false"/>

		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >
			<h:outputText value="#{msg.from}&nbsp;:" id="createDateFrom" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.createdDateFrom}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateCreatedFrom" initialiseIfNull="true" />

			<h:outputText value="#{msg.to}&nbsp;:" id="createDateTo" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.createdDateTo}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateCreatedTo" initialiseIfNull="true" />
		</h:panelGrid>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-3" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorAdvSearch-3-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-3-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelAdvAdditOptions" label="#{msg.additional_options}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{msg.additional_options_tooltip}" >

		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

			<h:outputText value="#{cmsg.keywords_property_label}&nbsp;:" id="keyword" escape="false"/>

			<circabc:panel id="add-keyword-selection-subpanel">
				<h:outputText value="#{CircabcAdvancedSearchDialog.displayKeywords}" id="keywordsString" />
				<h:commandButton id="select-keyword" styleClass="dialogButton" value="#{cmsg.keywords_property_pick}" title="#{cmsg.keywords_property_pick_tooltip}"  action="wai:dialog:selectKeywordsForSearchDialogWai" actionListener="#{WaiDialogManager.setupParameters}" />
			</circabc:panel>

			<h:outputText value="#{msg.status}&nbsp;:" id="status" escape="false"/>
			<h:selectOneMenu value="#{CircabcSearchProperties.status}" id="selectStatus">
				<f:selectItem itemValue="any" itemLabel="#{cmsg.advanced_search_dialog_any}" id="anyStatus" />
				<f:selectItems value="#{CircabcAdvancedSearchDialog.statuses}" id="statuses" />
			</h:selectOneMenu>

			<h:outputText value="#{cmsg.reference}&nbsp;:" id="reference" escape="false"/>
			<h:inputText value="#{CircabcSearchProperties.reference}" size="#{TextFieldGenerator.size}" maxlength="1024" id="txtReference" />

			<h:outputText value="#{cmsg.security_ranking}&nbsp;:" id="security_ranking" escape="false"/>
			<h:selectOneMenu value="#{CircabcSearchProperties.securityRanking}" id="selectSecurityRanking">
				<f:selectItem itemValue="any" itemLabel="#{cmsg.advanced_search_dialog_any}" id="anySecurityRanking" />
				<f:selectItems value="#{CircabcAdvancedSearchDialog.securityRankings}" id="securityRankings" />
			</h:selectOneMenu>

			<h:outputText value="#{cmsg.url}&nbsp;:" id="url" escape="false"/>
			<h:inputText value="#{CircabcSearchProperties.url}" size="#{TextFieldGenerator.size}" maxlength="1024" id="txtUrl" />
		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>
		<h:selectBooleanCheckbox value="#{CircabcSearchProperties.expirationDateChecked}" id="chkExpDate" />
		<h:outputText value="#{msg.expiration_date}&nbsp;:" id="expDate" escape="false"/>

		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >
			<h:outputText value="#{msg.from}&nbsp;:" id="expDateFrom" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.expirationDateFrom}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateExpFrom" initialiseIfNull="true" />

			<h:outputText value="#{msg.to}&nbsp;:" id="expDateTo" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.expirationDateTo}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateExpTo" initialiseIfNull="true" />
		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>
		<h:selectBooleanCheckbox value="#{CircabcSearchProperties.issueDateChecked}" id="chkissueDate" />
		<h:outputText value="#{cmsg.issue_date}&nbsp;:" id="issueDate" escape="false"/>

		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >
			<h:outputText value="#{msg.from}&nbsp;:" id="issueDateFrom" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.issueDateFrom}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateIssueFrom" initialiseIfNull="true" />

			<h:outputText value="#{msg.to}&nbsp;:" id="issueDateTo" escape="false"/>
			<a:inputDatePicker value="#{CircabcSearchProperties.issueDateTo}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{DatePickerGenerator.startYear}" id="dateIssueExpTo" initialiseIfNull="true" />
		</h:panelGrid>

	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-4" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorAdvSearch-4-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-4-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelAdvSearchLangOpt" label="#{cmsg.advanced_search_dialog_language_search_option}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.advanced_search_dialog_language_search_option}">
		<h:selectOneRadio value="#{CircabcSearchProperties.selectedLanguageOption}" layout="pageDirection" id="radiolangopt"  >
			<f:selectItem itemValue="allLanguage" itemLabel="#{cmsg.advanced_search_dialog_language_all}" id="allLanguage"  />
			<f:selectItem itemValue="currentLanguage" itemLabel="#{cmsg.advanced_search_dialog_language_current}" id="currentLanguage" />
			<f:selectItem itemValue="specifyLanguage" itemLabel="#{cmsg.advanced_search_dialog_language_specify}" id="specifyLanguage" />
		</h:selectOneRadio>
		<circabc:panel id="panel-list-language" styleClass="pad16px">
			<h:selectOneMenu id="languages" value="#{CircabcSearchProperties.language}" immediate="false" >
				<f:selectItems value="#{CircabcAdvancedSearchDialog.languages}"/>
			</h:selectOneMenu>
		</circabc:panel>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-5" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorAdvSearch-5-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-5-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelDynPropertiesOptions" label="#{cmsg.advanced_search_dialog_dynamic_properties_panel}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.advanced_search_dialog_dynamic_properties_panel}" >
		<circabc:searchDynamicProperties id="dunamicProps" bean="CircabcAdvancedSearchDialog" var="getInterestGroupDynamicProperties" styleClass="advSearchDynPropGrid" />
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-6" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorAdvSearch-6-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-6-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>


	<%--   DON T REMOVE THIS CODE ... NOT IMPLEMENTED YET !!!
	<circabc:panel id="panelAdvSearchDeepOpt" label="#{cmsg.advanced_search_dialog_version_search_option}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.advanced_search_dialog_version_search_option}">
		<h:selectOneRadio value="#{CircabcSearchProperties.selectedDeepOption}" layout="pageDirection" id="radiodeepopt"  >
			<f:selectItem itemValue="lastVersion" itemLabel="#{cmsg.advanced_search_dialog_version_current}" id="deepCurrent" />
			<f:selectItem itemValue="allVersion" itemLabel="#{cmsg.advanced_search_dialog_version_all}" id="deepAll" />
		</h:selectOneRadio>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorAdvSearch-7" styleClass="topOfPageAnchor">

		<circabc:actionLink id="topOfPageAnchorAdvSearch-7-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorAdvSearch-7-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	--%>
</circabc:panel>


