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

<%-- load a bundle of properties I18N strings here --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js"></script>

<script type="text/javascript">

		tinyMCE.init(
		{

			theme : "advanced",
			mode : "exact",
			plugins : "safari,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,visualchars,nonbreaking,xhtmlxtras,template",
			convert_urls: false,
			relative_urls: false,
			elements : "FormPrincipal:signatureField",
			language : "<%=request.getLocale().getLanguage()%>",

			theme_advanced_buttons1 : "bold,italic,underline,strikethrough,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect,fontselect,fontsizeselect",
			theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,outdent,indent,blockquote,|,link,unlink,anchor,image,media",

			theme_advanced_toolbar_location : "top",
			theme_advanced_toolbar_align : "left",
			theme_advanced_path : false,
			theme_advanced_resizing : false,
			forced_root_block : '',
			force_p_newlines : false,
			force_br_newlines : true,
			convert_newlines_to_brs : true,
			theme_advanced_disable: "styleselect",
			theme_advanced_styles : "Code=codeStyle;Quote=quoteStyle",

			
			// get a list of common used urls
			external_link_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/links.jsp",
			// get a list of available images
			external_image_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/images.jsp",
			// get a list of available media
			media_external_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/medias.jsp",

			extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
		});

</script>


<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainForm" styleClass="contentMainForm">
	<h:outputText value="<p>#{cmsg.edit_user_details_introduction}<br /></p>" escape="false"/>

	<circabc:panel id="identityPart" styleClass="editUserDetailsPart">
		<h:outputText value="#{cmsg.edit_user_details_identity}"/>
	</circabc:panel>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
		<h:outputText value="#{cmsg.edit_user_details_username}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.displayId}"  />

		<h:outputText value="#{cmsg.edit_user_details_firstname}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.firstName}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_lastname}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.lastName}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_email}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.email}" size="70"  />
		
		<h:outputText value="#{cmsg.edit_user_details_profile_url}: " styleClass="propertiesLabel" />
		<h:outputText value="#{WaiDialogManager.bean.profileUrl}" />
	</h:panelGrid>

	<circabc:panel id="topOfPageAnchorIdentity" styleClass="topOfPageAnchor"  >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	<circabc:panel id="space1" styleClass="divspacer3px" />

	<circabc:panel id="contactInfoPart" styleClass="editUserDetailsPart">
		<h:outputText value="#{cmsg.edit_user_details_contact_information}"/>
	</circabc:panel>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">

		<h:outputText value="#{cmsg.edit_user_details_title_prop}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.title}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_organisation}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.organisation}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_phone}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.phone}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_postal_address}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.postalAddress}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_description}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.description}" size="70" />

		<h:outputText value="#{cmsg.edit_user_details_fax}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.fax}" size="70"  />

		<h:outputText value="#{cmsg.edit_user_details_url_address}: " styleClass="propertiesLabel"/>
		<h:inputText value="#{WaiDialogManager.bean.userDetails.url}" size="70" />
	</h:panelGrid>

	<circabc:panel id="topOfPageAnchorContactInfo" styleClass="topOfPageAnchor" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	<circabc:panel id="space2" styleClass="divspacer3px" />

	<circabc:panel id="userOptionInfoPart" styleClass="editUserDetailsPart">
		<h:outputText value="#{cmsg.edit_user_details_user_options}"/>
	</circabc:panel>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
		<h:outputText value="#{cmsg.edit_user_details_content_language_filter}: " styleClass="propertiesLabel"/>
		<h:selectOneMenu value="#{WaiDialogManager.bean.contentFilterLanguage}" id="ContentFilterLanguage" >
			<f:selectItems value="#{WaiDialogManager.bean.contentFilterLanguages}" />
		</h:selectOneMenu>

		<h:outputText value="#{msg.language}:" styleClass="propertiesLabel"/>
		<h:selectOneMenu value="#{WaiDialogManager.bean.userDetails.userInterfaceLanguage}" id="InterfaceLanguage">
		   	<f:selectItems value="#{WaiDialogManager.bean.languages}" />
		</h:selectOneMenu>

		<h:outputText value="#{cmsg.edit_user_details_global_notification}: " styleClass="propertiesLabel"/>
		<h:selectOneMenu value="#{WaiDialogManager.bean.userDetails.globalNotification}" id="GlobalNotification" converter="javax.faces.Boolean">
			<f:selectItems value="#{WaiDialogManager.bean.notificationOptions}" />
		</h:selectOneMenu>

		<h:outputText value="#{cmsg.edit_user_details_visibility}: " styleClass="propertiesLabel"/>
		<h:selectOneMenu value="#{WaiDialogManager.bean.userDetails.visibility}" id="PersonalVisibiliy" converter="javax.faces.Boolean">
			<f:selectItems value="#{WaiDialogManager.bean.visibilityOptions}" />
		</h:selectOneMenu>

		<h:outputText value="#{cmsg.edit_user_details_signature}: " styleClass="propertiesLabel"/>
		<h:inputTextarea value="#{DialogManager.bean.userDetails.signature}" rows="3" cols="100" id="signatureField"/>
	</h:panelGrid>


	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
		<h:outputText value="#{cmsg.edit_user_details_avatar}: " styleClass="propertiesLabel"/>
		<h:graphicImage value="#{WaiDialogManager.bean.avatarPath}" title="#{cmsg.edit_user_details_avatar_tooltip}" alt="#{cmsg.edit_user_details_avatar_tooltip}" styleClass="avatar"/>
		<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0">
			<circabc:actionLink value="#{cmsg.edit_user_avatar_action}" tooltip="#{cmsg.edit_user_avatar_action_tooltip}" image="/images/icons/upload_new_version.gif" action="wai:dialog:close:wai:dialog:updateAvatar" actionListener="#{WaiDialogManager.bean.launchUpdateAvatarDialog}" styleClass="translateLink">
				<circabc:param name="id" value="#{DialogManager.bean.userDetails.nodeRef.id}" ></circabc:param>
			</circabc:actionLink>
			<circabc:actionLink value="#{cmsg.edit_user_details_avatar_remove}" tooltip="#{cmsg.edit_user_details_avatar_remove_tooltip}" image="/images/icons/remove_item.gif" actionListener="#{WaiDialogManager.bean.removeAvatar}" styleClass="translateLink"/>
		</h:panelGrid>
	</h:panelGrid>
 	<%-- 
	<h:outputText value="#{cmsg.edit_user_details_avatar}: " styleClass="propertiesLabel"/>
	<circabc:panel id="avatarImgWrapper">
		<h:graphicImage value="#{WaiDialogManager.bean.avatarPath}" title="#{cmsg.edit_user_details_avatar_tooltip}" alt="#{cmsg.edit_user_details_avatar_tooltip}" styleClass="avatar"/>
	</circabc:panel>
	<circabc:panel id="avatarImgActionPanel">
		<circabc:actionLink value="#{cmsg.edit_user_avatar_action}" tooltip="#{cmsg.edit_user_avatar_action_tooltip}" image="/images/icons/upload_new_version.gif" action="wai:dialog:close:wai:dialog:updateAvatar" actionListener="#{WaiDialogManager.bean.launchUpdateAvatarDialog}" styleClass="translateLink">
			<circabc:param name="id" value="#{DialogManager.bean.userDetails.nodeRef.id}" ></circabc:param>
		</circabc:actionLink>
		<circabc:actionLink value="#{cmsg.edit_user_details_avatar_remove}" tooltip="#{cmsg.edit_user_details_avatar_remove_tooltip}" image="/images/icons/remove_item.gif" actionListener="#{WaiDialogManager.bean.removeAvatar}" styleClass="translateLink"/>
	</circabc:panel>
	--%>
 
	<circabc:panel id="topOfPageAnchorUserOpt" styleClass="topOfPageAnchor" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	<circabc:panel id="space3" styleClass="divspacer3px" />

	<circabc:panel id="privacyInfoPart" styleClass="editUserDetailsPart">
		<h:outputText value="#{cmsg.self_registration_privacy_statement_title}"/>
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>
	<circabc:actionLink value="#{cmsg.self_registration_privacy_statement_link}" tooltip="#{cmsg.self_registration_privacy_statement_link_tooltip}"  href="/html/CIRCABC_privacy_statement.pdf" target="_blank"  />

	<circabc:panel id="topOfPageAnchorLast" styleClass="topOfPageAnchor" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:displayer rendered="#{EditUserDetailsDialog.reloadbuttonAvailable}">
		<h:commandButton id="getDataFromCud"  value="#{cmsg.edit_user_details_get_cud_data}" actionListener="#{WaiDialogManager.bean.getFromLdap}"  />
	</circabc:displayer>
</circabc:panel>

