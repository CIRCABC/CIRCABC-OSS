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

<%@ page isELIgnored="false"%>

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

<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="invite-user-notify-section-1" styleClass="signup_rub_title" >
	<h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</circabc:panel>


<f:verbatim><br /></f:verbatim>
<h:outputText value="#{msg.send_email}" rendered="#{WizardManager.bean.applicantPresents == false || (WizardManager.bean.applicantPresents == true && WizardManager.bean.nonApplicantPresents == false)}" escape="false"/>
<h:outputText value="#{cmsg.send_email_when_applicant}" rendered="#{WizardManager.bean.applicantPresents == true && WizardManager.bean.nonApplicantPresents == true}" escape="false"/>

<h:selectOneRadio id="SendNotificationRadio" value="#{WizardManager.bean.notify}" disabled="#{WizardManager.bean.applicantPresents == true && WizardManager.bean.nonApplicantPresents == false}" >
	<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
	<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
</h:selectOneRadio>

<h:outputText value="#{cmsg.send_email_to_dir_admins}"  escape="false" rendered="#{WizardManager.bean.interestGroup}" />
<h:selectOneRadio id="SendNotificationDirAdminRadio" value="#{WizardManager.bean.notifyDirAdmins}" rendered="#{WizardManager.bean.interestGroup}" >
	<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
	<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
</h:selectOneRadio>

<f:verbatim><br /></f:verbatim>

<circabc:panel id="invite-user-notify-section-2" styleClass="signup_rub_title" >
	<h:outputText value="&nbsp;#{msg.email_message}" escape="false" />
</circabc:panel>

<f:verbatim><br /></f:verbatim>

<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
<h:outputText value="&nbsp;#{msg.subject}&nbsp;" escape="false" />
<h:inputText id="subject" value="#{WizardManager.bean.subject}" size="85" maxlength="1024" onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();" disabled="#{WizardManager.bean.usingTemplate != null}" />

<!--<f:verbatim><br /><br /></f:verbatim>
<h:outputText value="&nbsp;&nbsp;#{msg.action_mail_template}&nbsp;" escape="false" />
<h:selectOneMenu id="SendNotification" value="#{WizardManager.bean.template}">
	<f:selectItem itemValue="none" itemLabel="#{cmsg.no_template}"/>
	<f:selectItems value="#{WizardManager.bean.emailTemplates}" />
</h:selectOneMenu>
<h:outputText value="&nbsp;&nbsp;&nbsp;" escape="false" />
<h:commandButton value="#{msg.insert_template}" actionListener="#{WizardManager.bean.insertTemplate}" styleClass="wizardButton" />
<h:commandButton value="#{msg.discard_template}" actionListener="#{WizardManager.bean.discardTemplate}" styleClass="wizardButton" disabled="#{WizardManager.bean.usingTemplate == null}" />-->

<f:verbatim><br /><br /></f:verbatim>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

	<h:outputText value="&nbsp;&nbsp;#{msg.message}" escape="false" />
	<h:inputTextarea id="editor" value="#{WizardManager.bean.body}" rows="12" cols="60" disabled="#{WizardManager.bean.usingTemplate != null}"  />

	<h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;(*)" escape="false" />
	<h:outputText value="#{cmsg.invite_circabc_user_template_mail_regex_explaination}"/>
</h:panelGrid>
