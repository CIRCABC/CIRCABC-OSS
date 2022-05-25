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
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js">&#160;</script>
<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/common.js">&#160;</script>
<script language="javascript" type="text/javascript">
tinyMCE.init(
{
	// general options
	theme : "advanced",
	mode : "textareas",
	plugins : "safari,spellchecker,pagebreak,style,layer,table,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,visualchars,nonbreaking,xhtmlxtras,template",
	convert_urls: false,
	relative_urls: false,
	language : "<%=request.getLocale().getLanguage()%>",
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
	//external_link_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/links.jsp",
	external_image_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/images.jsp",
	media_external_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/medias.jsp",
	extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
});
</script>

<circabc:panel id="contentMainFormManageSystemMessageUi" styleClass="contentMainForm">

	<circabc:panel id="formRow1" styleClass="formRow">
		<circabc:panel id="col1Row1" styleClass="formColumnNarrow">
			<h:outputLabel for="txtMessage">
				<h:outputText value="#{cmsg.manage_system_message_label_message}" />
			</h:outputLabel>
		</circabc:panel>
		<circabc:panel id="col2Row1" styleClass="formColumn">
			<h:inputTextarea id="txtMessage" value="#{DialogManager.bean.message}" styleClass="txtArea" />
		</circabc:panel>
	</circabc:panel>
	
	<circabc:panel id="formRow2" styleClass="formRow">
		<circabc:panel id="col1Row2" styleClass="formColumnNarrow">
			<h:outputText value=" " />
		</circabc:panel>
		<circabc:panel id="col2VRow2" styleClass="formColumn">
			<h:selectBooleanCheckbox id="cbxShowMessage" value="#{DialogManager.bean.showMessage}" />
			<h:outputLabel for="cbxShowMessage">
				<h:outputText value="#{cmsg.manage_system_message_label_showMessage}" />
			</h:outputLabel>
		</circabc:panel>
	</circabc:panel>
	
	<circabc:panel id="formRow3" styleClass="formRow">
		<circabc:panel id="col1Row3" styleClass="formColumnNarrow">
			<h:outputLabel for="helpLink">
				<h:outputText value="#{cmsg.manage_help_link_label_message}" />
			</h:outputLabel>
		</circabc:panel>
		<circabc:panel id="col2VRow3" styleClass="formColumn">
			<h:inputText value="#{DialogManager.bean.helpLink }" title="helpLink"></h:inputText>
		</circabc:panel>
	</circabc:panel>
</circabc:panel>
