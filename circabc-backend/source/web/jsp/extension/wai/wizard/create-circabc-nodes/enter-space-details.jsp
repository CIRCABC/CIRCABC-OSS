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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />

<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js">&#160;</script>
<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/common.js">&#160;</script>
<script language="javascript" type="text/javascript">
tinyMCE.init(
{
	// general options
	theme : "advanced",
	mode : "exact",
	elements : "FormPrincipal:description,FormPrincipal:contact",
	plugins : "safari,spellchecker,pagebreak,style,layer,table,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,visualchars,nonbreaking,xhtmlxtras,template",
	relative_urls: true,
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

<circabc:panel id="enter-space-details-section-1" styleClass="signup_rub_title" >
	<h:outputText value="&nbsp;#{msg.properties}" escape="false" />
</circabc:panel>

<f:verbatim>

<table cellpadding="2" cellspacing="2" border="0" width="100%">

   <tr>
      <td align="middle">
         </f:verbatim>
         <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.name}:" />
         <f:verbatim>
      </td>
      <td width="85%">
         </f:verbatim>
         <h:inputText id="name" value="#{WizardManager.bean.name}" size="35" maxlength="42"
                     disabled="#{WizardManager.bean.propertiesReadOnly}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.title}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="title" value="#{WizardManager.bean.title}" size="35" maxlength="1024" disabled="#{WizardManager.bean.propertiesReadOnly}" />
         <f:verbatim>
      </td>
   </tr>

   <tr>
      <td></td>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.description}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
              <h:inputTextarea id="description" value="#{WizardManager.bean.description}" readonly="false" 
              immediate="true" rows="5" cols="60" disabled="#{WizardManager.bean.propertiesReadOnly}"/>
         <f:verbatim>
      </td>
   </tr>
	</f:verbatim>
	
      	<h:outputText value="<tr><td></td><td>#{cmsg.lightDescription}:</td><td>" escape="false" rendered="#{WizardManager.bean.isInterestGroupToCreate}"/>

              <h:inputTextarea id="lightDescription" value="#{WizardManager.bean.lightDescription}" readonly="false" 
              immediate="true" rows="5" cols="60" disabled="#{WizardManager.bean.propertiesReadOnly}" rendered="#{WizardManager.bean.isInterestGroupToCreate}"/>
              
         <h:outputText value="</td></tr>" escape="false" rendered="#{WizardManager.bean.isInterestGroupToCreate}"/>     
   
   <f:verbatim>

   <tr>
      <td></td>
      <td>
         </f:verbatim>
      	     <h:outputText value="#{cmsg.igroot_home_contact_information}:" rendered="#{WizardManager.bean.contactInformationDisplayed}"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
              <h:inputTextarea id="contact" value="#{WizardManager.bean.contact}" readonly="false" 
              immediate="true" rows="5" cols="60" rendered="#{WizardManager.bean.contactInformationDisplayed}"/>
         <f:verbatim>
      </td>
   </tr>

   <tr>
      <td></td>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.icon}:" />
         <f:verbatim>
     	</td>
      <td>
         <table border="0" cellpadding="0" cellspacing="0"><tr><td>
         </f:verbatim>
         <a:imagePickerRadioPanel id="space-icon" columns="6" spacing="4" value="#{WizardManager.bean.icon}"
                                  panelBorder="greyround" panelBgcolor="#F5F5F5">
            <a:listItems value="#{WizardManager.bean.icons}" />
         </a:imagePickerRadioPanel>
         <f:verbatim>
         </td></tr></table>
      </td>
   </tr>
</table>
</f:verbatim>
