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

<h:inputHidden value="#{TranslatePropertyDialog.mode}" id="editorMode" />
<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js">&#160;</script>
<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/common.js">&#160;</script>
<script language="javascript" type="text/javascript">
var editorMode = document.getElementById('FormPrincipal:editorMode').value;
tinyMCE.init(
{
	// general options
	theme : "advanced",
	mode : "none",
	plugins : "safari,spellchecker,pagebreak,style,layer,table,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,visualchars,nonbreaking,xhtmlxtras,template",
	convert_urls: false,
	relative_urls: false,
	language : "en",
	font_size_style_values : "10pt,12pt,14pt,18pt,24pt",

	// theme options
	theme_advanced_buttons1 : "fontsizeselect,separator,bold,italic,underline,strikethrough,separator,forecolor,separator,link,unlink,separator,cut,copy,paste,pastetext,pasteword",
	theme_advanced_buttons2 : "",
    theme_advanced_buttons3 : "",
	theme_advanced_toolbar_location : "top",
	theme_advanced_toolbar_align : "left",
	theme_advanced_path : false,
	theme_advanced_resizing : true,
	theme_advanced_statusbar_location : "bottom",
	theme_advanced_disable: "styleselect",
	theme_advanced_styles : "Code=codeStyle;Quote=quoteStyle",

	// Drop lists for link/image/media/template dialogs
	external_link_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/links.jsp",
	external_image_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/images.jsp",
	media_external_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/medias.jsp",
	extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
});

function changerichtype(selectElementId, tetxtAreaID){
	var elt = document.getElementById(selectElementId);
    if(elt.options[elt.selectedIndex].value == 'Simple'){
        tinyMCE.execCommand('mceRemoveControl', false, tetxtAreaID); 
    }else{
        tinyMCE.execCommand('mceAddControl', false, tetxtAreaID);
    }
}
function setUpRichTextAreas()
{ 
 	if ( document.getElementById('FormPrincipal:select-text-area-type')  != null){
		changerichtype('FormPrincipal:select-text-area-type','FormPrincipal:translate-property-value-area');
	}

	if ( document.getElementById('FormPrincipal:select-text-area-type-selected')  != null){
		changerichtype('FormPrincipal:select-text-area-type-selected','FormPrincipal:translate-property-selected-value-area');
	}    
}

window.onload = setUpRichTextAreas;



</script>


<circabc:panel id="contentMainFormTranslateProperty" styleClass="contentMainForm">

	<!--  New keyword panel -->
	<circabc:panel id="translate-property-first-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.translate_property_dialog_section_specify}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:outputText value="#{cmsg.add_translation_language }: "></h:outputText>
	<h:selectOneMenu id="translate-property-language" value="#{TranslatePropertyDialog.language}" style=" width:20%;" >
		<f:selectItems  id="translate-property-languages" value="#{TranslatePropertyDialog.languages}"  />
	</h:selectOneMenu>

	
	<circabc:displayer rendered="#{TranslatePropertyDialog.displayAsArea == false}">	
		<h:outputText value="#{cmsg.machine_translate_common_text }: " style="margin-left:35px;"></h:outputText>
		<h:inputText id="translate-property-value-input" value="#{TranslatePropertyDialog.value}" style=" width:35%;" />
	</circabc:displayer>

	<circabc:displayer rendered="#{TranslatePropertyDialog.displayAsArea == true}">
		<f:verbatim>
				<br /><br />
		</f:verbatim>
		<h:outputText value="#{cmsg.machine_translate_common_text }: "></h:outputText>
		<h:selectOneMenu id="select-text-area-type" rendered="#{TranslatePropertyDialog.lightDescription == false}" value="#{TranslatePropertyDialog.textAreaType}"  onchange="changerichtype('FormPrincipal:select-text-area-type','FormPrincipal:translate-property-value-area')" >
			<f:selectItems  id="select-text-area-type-items" value="#{TranslatePropertyDialog.textAreaTypes}" />
		</h:selectOneMenu>
		<f:verbatim>
			<br /><br />
		</f:verbatim>
		<h:inputTextarea id="translate-property-value-area" rows="3" cols="33" value="#{TranslatePropertyDialog.value}" style="width:350px;"/>

	</circabc:displayer>
	
	<f:verbatim>
		<br />
	</f:verbatim>
	
	<h:outputText value="#{cmsg.lightDescription_no_html }" rendered="#{TranslatePropertyDialog.lightDescription == true}" style="color:#CCC;"></h:outputText>
	<f:verbatim>
		<br />
	</f:verbatim>
	<h:outputText value="#{cmsg.lightDescription_limit_500 }" rendered="#{TranslatePropertyDialog.lightDescription == true}" style="color:#CCC;"></h:outputText>


	<circabc:displayer rendered="#{TranslatePropertyDialog.displayAsArea == false}">
		<f:verbatim>
			<div style="margin-left:32%;">
		</f:verbatim>
	</circabc:displayer>
	<h:commandButton id="AddToList" value="#{msg.add_to_list_button}"
		actionListener="#{TranslatePropertyDialog.addSelection}"
		styleClass="wizardButton" />
		<h:commandButton id="RequestTranslation" value="#{cmsg.translate_property_dialog_request_translation_button}"
		action="#{TranslatePropertyDialog.getDialogCloseAndLaunchAction}" disabled="#{TranslatePropertyDialog.translationDataModel.rowCount == 0}"
		styleClass="wizardButton" style=" margin-left:10px;" rendered="#{TranslatePropertyDialog.machineTranslationEnabled}" />
	<circabc:displayer rendered="#{TranslatePropertyDialog.displayAsArea == false}">
		<f:verbatim>
			</div>
		</f:verbatim>
	</circabc:displayer>

	<f:verbatim>
		<br />	<br />
	</f:verbatim>

	<circabc:panel id="translate-property_dialog_section_machine_translation" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.manage_multilingual_details_translations}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>
	

	<h:dataTable value="#{TranslatePropertyDialog.translationDataModel}" var="row"
		rowClasses="selectedItemsRow,selectedItemsRowAlt"
		styleClass="selectedItems" headerClass="selectedItemsHeader"
		cellspacing="0" cellpadding="4"
		rendered="#{TranslatePropertyDialog.translationDataModel.rowCount != 0}">
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{msg.language}" />
			</f:facet>
			<h:outputText id="col-lang" value="#{row.language}" />
		</h:column>
		<h:column>
			<f:facet name="header">
				<h:outputText id="col-name" value="#{msg.name}" />
			</f:facet>
			<h:outputText value="#{row.value}" escape="false" />
		</h:column>
		<h:column>
			<circabc:actionLink tooltip="#{msg.edit}" actionListener="#{TranslatePropertyDialog.editSelection}"
				image="/images/icons/edit_icon.gif" value="#{msg.edit}" showLink="false"
				styleClass="pad6Left" />
		</h:column>
		<h:column>
			<circabc:actionLink tooltip="#{msg.remove}" actionListener="#{TranslatePropertyDialog.removeSelection}"
				image="/images/icons/delete.gif" value="#{msg.remove}" showLink="false"
				styleClass="pad6Left" />
		</h:column>
	</h:dataTable>
	<a:panel id="no-items"
		rendered="#{TranslatePropertyDialog.translationDataModel.rowCount == 0}">
		<table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
			<tr>
				<td class='selectedItemsRow'>
					<h:outputText id="no-items-msg" value="#{msg.no_selected_items}" />
				</td>
			</tr>
		</table>
	</a:panel>
	<f:verbatim>
		<br /><br /><br />
	</f:verbatim>
	<circabc:panel id="translate-property_dialog_section_save" styleClass="signup_rub_title" rendered="#{TranslatePropertyDialog.selectedLanguage != null }" >
	<h:outputText value="3.&nbsp;#{cmsg.translate_property_dialog_section_save}&nbsp;" escape="false" />
	<h:outputText value="#{TranslatePropertyDialog.selectedLanguageName}"  />
	<f:verbatim><br /><br /></f:verbatim>
	<circabc:displayer rendered="#{TranslatePropertyDialog.displayAsArea == false}">
		<h:inputText id="translate-property-selected-value-input" value="#{TranslatePropertyDialog.selectedValue}" />
	</circabc:displayer>
	<circabc:displayer rendered="#{TranslatePropertyDialog.displayAsArea == true}">
		<h:selectOneMenu id="select-text-area-type-selected" rendered ="#{TranslatePropertyDialog.lightDescription == false}" value="#{TranslatePropertyDialog.selectedTextAreaType}" onchange="changerichtype('FormPrincipal:select-text-area-type-selected','FormPrincipal:translate-property-selected-value-area')" >
			<f:selectItems  id="select-text-area-type-select-items" value="#{TranslatePropertyDialog.textAreaTypes}" />
		</h:selectOneMenu>
		<f:verbatim>
			<br />
		</f:verbatim>
		<h:inputTextarea id="translate-property-selected-value-area" rows="3" cols="33" value="#{TranslatePropertyDialog.selectedValue}" />
	</circabc:displayer>
		<f:verbatim>
		<br /><br /><br />
	</f:verbatim>
	<h:commandButton id="SaveSelection" value="#{cmsg.save}"
		actionListener="#{TranslatePropertyDialog.saveSelection}" 
		styleClass="wizardButton" />
	</circabc:panel>
</circabc:panel>

