<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or - as soon they
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

<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>


<f:verbatim>

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
	external_link_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/links.jsp",
	external_image_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/images.jsp",
	media_external_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/medias.jsp",
	extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
});
</script>

</f:verbatim>

<p><img src="${currentContextPath}/images/extension/help.png" alt="help" style="float:left;width:100px;"/><br/>
<h:outputText value="#{cmsg.contact_dialog_request_help_text }" escape="false"></h:outputText>
</p>

<f:verbatim><fieldset class="formArea">
	<legend></f:verbatim>
	
	<h:outputText value="#{cmsg.contact_dialog_request_legend }"></h:outputText>
	
	<f:verbatim>
		</legend>
		<br/>
	</f:verbatim>
	
	<h:outputText value="#{ContactFormDialog.organisationHelpdesk }" escape="false"></h:outputText>
	
	<f:verbatim>
	<br/><br/>
	</f:verbatim>
	
	<h:outputLabel for="contact" value="#{cmsg.contact_dialog_contact}"></h:outputLabel>
	<c:if test="${!NavigationBean.echaEnabled}">
		<h:selectOneMenu id="contact" value="#{ContactFormDialog.selectedContact}">
			<f:selectItems value="#{ContactFormDialog.contactAsItems}"/>
		</h:selectOneMenu>
	</c:if>
	<c:if test="${NavigationBean.echaEnabled}">
		<h:selectOneMenu id="contact" value="#{ContactFormDialog.selectedContact}" disabled="#{ContactFormDialog.loggedIn}">
			<f:selectItems value="#{ContactFormDialog.contactAsItems}"/>
		</h:selectOneMenu>
	</c:if>
	
	<f:verbatim>
	<br/>
	</f:verbatim>
	
	<c:if test="${!NavigationBean.echaEnabled}">
		<h:outputText value="#{ContactFormDialog.guessedHelpdesk }" styleClass="inputHelper"/>
	</c:if>
	<c:if test="${NavigationBean.echaEnabled}">
		<h:outputText escape="false" value="#{cmsg.contact_dialog_request_helpdesk_selection_guessed_scircabc}" styleClass="inputHelper" rendered="#{!ContactFormDialog.loggedIn}" />
	</c:if>
	
	<f:verbatim>
	<br/><br/>
	</f:verbatim>
	
	<h:outputLabel for="mail" value="#{cmsg.contact_dialog_mail }" rendered="#{NavigationBean.isGuest == true}"></h:outputLabel>
	<h:inputText id="mail" value="#{ContactFormDialog.mail }" styleClass="textLongWidth" rendered="#{NavigationBean.isGuest == true}"></h:inputText>
	<h:outputText value="<br/>" escape="false" rendered="#{NavigationBean.isGuest == true}"></h:outputText>
	
	<f:verbatim>
	<br/>
	</f:verbatim>
	
	<h:outputLabel for="subject" value="#{cmsg.contact_dialog_subject }"></h:outputLabel>
	<h:inputText id="subject" value="#{ContactFormDialog.subject }" styleClass="textLongWidth"></h:inputText>
	
	<f:verbatim>
	<br/>
	</f:verbatim>
	
	<h:outputLabel for="type" value="#{cmsg.contact_dialog_type }"></h:outputLabel>
	<h:selectOneMenu id="type" value="#{ContactFormDialog.selectedType }">
		<f:selectItems value="#{ContactFormDialog.types }"/>
	</h:selectOneMenu>
	
	<f:verbatim>
	<br/><br/><br/>
	</f:verbatim>
	
	<h:outputLabel for="description" value="#{cmsg.contact_dialog_description }"></h:outputLabel>
	<h:inputTextarea id="description" value="#{ContactFormDialog.description }" styleClass="textLongWidth"></h:inputTextarea>
	
	<f:verbatim>
	<br/>
</fieldset>

</f:verbatim>

