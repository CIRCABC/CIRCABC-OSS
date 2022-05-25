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
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false" %>


<script language="javascript">
    function updateList(){
        document.getElementById("FormPrincipal:submit-change-lang").click();
    }
</script>

<circabc:panel id="contentMainFormManageKeywords" styleClass="contentMainForm">

	<!--  Action add a new keyword -->
	<circabc:panel id="manage-keywords-add-keyword-section" styleClass="wai_dialog_more_action">
		<h:graphicImage value="/images/icons/add_category.gif" alt="#{cmsg.add_new_keyword_dialog_action_tooltip}" />
		<h:outputText id="manage-keywords-add-keyword-spaces" value="&nbsp;" escape="false" />
		<circabc:actionLink id="manage-keywords-act-add" value="#{cmsg.add_new_keyword_dialog_action_title}" tooltip="#{cmsg.add_new_keyword_dialog_action_tooltip}" action="wai:dialog:defineKeywordDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
			<circabc:param id="manage-keywords-act-id" name="id" value="#{DialogManager.bean.interestGroup.id}" />
			<circabc:param id="manage-keywords-service" name="service" value="Library" />
			<circabc:param id="manage-keywords-activity" name="activity" value="Add keyword" />
		</circabc:actionLink>
	</circabc:panel>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<!--  Select language panel -->
	<circabc:panel id="manage-keywords-main-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.manage_keyword_dialog_section_title}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<f:verbatim> <div style="float:right" > </f:verbatim>
		<circabc:actionLink id="manage-keywords-unused" value="#{cmsg.manage_keywords_dialog_filter_unused }" tooltip="" action="#{ManageKeywordsDialog.listUnusedKeywords }" rendered="#{ManageKeywordsDialog.unusedFiltered == false }" >
		</circabc:actionLink>
		<circabc:actionLink id="manage-keywords-unused-reset" value="#{cmsg.manage_keywords_dialog_show_all }" tooltip="" action="#{ManageKeywordsDialog.resetUnusedKeywords }" rendered="#{ManageKeywordsDialog.unusedFiltered == true }" >
		</circabc:actionLink>
	<f:verbatim> </div> </f:verbatim>

	<h:outputText id="manage-keywords-lang-text" value="#{cmsg.manage_keyword_dialog_language_filter}:&nbsp;" escape="false"/>
	<h:selectOneMenu id="manage-keywords-language" value="#{DialogManager.bean.selectedLanguage}" onchange="updateList()" valueChangeListener="#{ManageKeywordsDialog.updateList}" immediate="true"  >
		<f:selectItems id="manage-keywords-languages" value="#{DialogManager.bean.languages}" />
	</h:selectOneMenu>
	<h:outputText id="manage-keywords-lang-text-spaces-submit" value="&nbsp;&nbsp;" escape="false" />
	<h:outputText id="manage-keywords-number-text" value="Number of items:&nbsp;" escape="false"/>
	<h:selectOneMenu id="manage-keywords-number" value="#{DialogManager.bean.itemToDisplay}" onchange="updateList()" valueChangeListener="#{ManageKeywordsDialog.updateList}" immediate="true"  >
		<f:selectItems id="manage-keywords-numbers" value="#{DialogManager.bean.numberOfItems}" />
	</h:selectOneMenu>
	<h:commandButton id="submit-change-lang" styleClass="" value="#{cmsg.filter}" action="wai:dialog:close:wai:dialog:manageKeywordsDialogWai" rendered="true" immediate="true" title="#{cmsg.manage_keyword_dialog_apply_filter_tooltip}" />

	
	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<h:outputText value="#{cmsg.library_bulk_operation_dialog_select_all }"></h:outputText>
	<circabc:actionLink value="" tooltip="" image="/images/extension/icons/checked.png"  actionListener="#{ManageKeywordsDialog.unselectKeyword }" rendered="#{ManageKeywordsDialog.selectedAllKeywords == true}">
		<circabc:param value="true" name="all"></circabc:param>
	</circabc:actionLink>
	<circabc:actionLink value="" tooltip="" image="/images/extension/icons/unchecked.png"  actionListener="#{ManageKeywordsDialog.selectKeyword }" rendered="#{ManageKeywordsDialog.selectedAllKeywords == false}">
		<circabc:param value="true" name="all"></circabc:param>
	</circabc:actionLink>

	<f:verbatim>
		<br /><br />
	</f:verbatim>
	
	<h:dataTable id="keywordsTable" var="kw" value="#{DialogManager.bean.keywords}" rowClasses="recordSetRow,recordSetRowAlt" rows="#{DialogManager.bean.itemToDisplay}"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader" width="100%">
		
		<h:column id="manage-keywords-list-select">
			<f:facet name="header">
				<h:outputText value="-"/>
			</f:facet>

			<circabc:actionLink id="manage-keywords-act-unselect-key" value="" tooltip="" image="/images/extension/icons/checked.png" rendered="#{kw.selected == true}" actionListener="#{ManageKeywordsDialog.unselectKeyword }" >
			   	<circabc:param value="#{kw.id}" name="idKeyword"></circabc:param>
			</circabc:actionLink>
		   
		   <circabc:actionLink id="manage-keywords-act-select-key" value="" tooltip="" image="/images/extension/icons/unchecked.png" rendered="#{kw.selected == false}" actionListener="#{ManageKeywordsDialog.selectKeyword }" >
		   		<circabc:param value="#{kw.id}" name="idKeyword"></circabc:param>
		   </circabc:actionLink>
		   
		</h:column>
		<h:column id="manage-keywords-list-name">
			<f:facet name="header">
				<h:outputText id="manage-keywords-list-sorter" value="#{cmsg.manage_keyword_dialog_value}"/>
			</f:facet>
			<h:outputText id="manage-keywords-list-col-name" value="#{kw.value}"/>
		</h:column>
		<h:column id="manage-keywords-list-actions-col">
			<f:facet name="header">
				<h:outputText id="manage-keywords-list-cont-act" value="#{cmsg.actions}"  />
			</f:facet>

			<circabc:actionLink image="/images/icons/edit_category.gif" id="manage-keywords-act-modify-key" tooltip="#{cmsg.modify_keyword_dialog_action_tooltip}" value="#{cmsg.modify_keyword_dialog_action_tooltip}" showLink="false" action="wai:dialog:editKeywordDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
				<circabc:param id="param-modify-keyword-ig-id" name="id" value="#{DialogManager.bean.interestGroup.id}" />
				<circabc:param id="param-modify-keyword" name="keyword" value="#{kw.id}" />
				<circabc:param id="param-modify-keyword-imageName" name="imageName" value="modify_keyword_#{kw.value}" />
				<circabc:param id="param-modify-service" name="service" value="Library" />
				<circabc:param id="param-modify-activity" name="activity" value="Edit keyword" />
			</circabc:actionLink>
			<circabc:actionLink image="/images/icons/delete_category.gif" id="manage-keywords-act-delete-key" tooltip="#{cmsg.delete_keyword_dialog_action_tooltip}" value="#{cmsg.delete_keyword_dialog_action_tooltip}" showLink="false" action="wai:dialog:deleteKeywordDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
				<circabc:param id="param-delete-keyword-ig-id" name="id" value="#{DialogManager.bean.interestGroup.id}" />
				<circabc:param id="param-delete-keyword" name="keyword" value="#{kw.id}" />
				<circabc:param id="param-delete-keyword-imageName" name="imageName" value="delete_keyword_#{kw.value}" />
				<circabc:param id="param-delete-service" name="service" value="Library" />
				<circabc:param id="param-delete-activity" name="activity" value="Delete keyword" />
			</circabc:actionLink>
		</h:column>
		<f:facet name="empty">
			<h:outputFormat id="manage-keywords-list-container" value="#{cmsg.manage_keyword_dialog_no_list_items}"  />
		</f:facet>

			
		
	</h:dataTable>
	
	<f:verbatim><div style="float:right"></f:verbatim>
		<t:dataScroller  for="keywordsTable" paginator="true" paginatorMaxPages="5"  pageCountVar="pageCounter" pageIndexVar="pageIndex" paginatorActiveColumnStyle="font-weight:bold;" >
			<f:facet name="first" >
	           <h:graphicImage value="/images/icons/FirstPage.gif"></h:graphicImage>
	          </f:facet>
	          <f:facet name="last">
	            <h:graphicImage value="/images/icons/LastPage.gif"></h:graphicImage>
	          </f:facet>
	          <f:facet name="previous">
	            <h:graphicImage value="/images/icons/PreviousPage.gif"></h:graphicImage>
	          </f:facet>
	          <f:facet name="next">
	           <h:graphicImage value="/images/icons/NextPage.gif"></h:graphicImage>
	          </f:facet>	
		</t:dataScroller>
	<f:verbatim></div></f:verbatim>

	<f:verbatim>
		<br /><br />
	</f:verbatim>
	
	<f:verbatim><span style="float:right"></f:verbatim>
	<circabc:actionLink id="manage-keywords-act-delete-key" tooltip="#{cmsg.delete_keyword_dialog_action_tooltip}" value="#{cmsg.manage_keywords_delete_selected }" showLink="false" action="wai:dialog:deleteKeywordDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
		<circabc:param id="param-delete-keyword-ig-id" name="id" value="#{DialogManager.bean.interestGroup.id}" />
		<circabc:param id="param-delete-keyword-all" name="all" value="true" />
		<circabc:param id="param-delete-keyword-all-selected" name="selectedKeywords" value="#{DialogManager.bean.selectedKeywords}" />
		<circabc:param id="param-delete-keyword-imageName" name="imageName" value="delete_keyword" />
		<circabc:param id="param-delete-service" name="service" value="Library" />
		<circabc:param id="param-delete-activity" name="activity" value="Delete all keywords" />
	</circabc:actionLink>
		<f:verbatim></span></f:verbatim>	
	<h:commandButton style="float:right;" value="#{cmsg.manage_keywords_export_selected }" actionListener="#{DialogManager.bean.export }"></h:commandButton>
	
	<f:verbatim>
		<br/>
		<br/>
	</f:verbatim>
	
	<h:outputText value="#{cmsg.manage_keywords_import}:&nbsp" escape="false"></h:outputText>
	<t:inputFileUpload value="#{DialogManager.bean.importedFile }" ></t:inputFileUpload>
	<h:commandButton value="Upload" action="wai:dialog:manageKeywordsDialogWai" actionListener="#{DialogManager.bean.importFile }" ></h:commandButton>

	<h:commandLink style="color:#777" value="#{cmsg.bulk_invite_upload_file_template_link}"
		title="#{cmsg.bulk_invite_upload_file_template_link}"
		action="#{DialogManager.bean.downloadTemplate }"/>

	<!--  h:commandButton value="" id="submit-change-lang" immediate="true" action=""  /-->

</circabc:panel>

<script type="text/javascript" language="javascript">
var formTag = document.getElementById("FormPrincipal");
formTag.setAttribute("enctype","multipart/form-data");

</script>