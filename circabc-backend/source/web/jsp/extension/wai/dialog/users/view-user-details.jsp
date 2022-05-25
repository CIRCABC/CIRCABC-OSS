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

<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainForm" styleClass="contentMainForm" rendered="#{WaiDialogManager.bean.dataSuccessfullyLoaded == true}">

	<circabc:panel id="view-my-account-more-action-section" styleClass="wai_dialog_more_action" rendered="#{WaiDialogManager.bean.editDetailsAllowed}">
		<h:graphicImage value="/images/icons/edit_group.gif" title="#{cmsg.edit_user_details_icon_tooltip}" alt="#{cmsg.edit_user_details_icon_tooltip}" />
		<h:outputText id="view-my-account-more-action-space" value="&nbsp;" escape="false" />
		<circabc:actionLink id="view-my-account-more-action-link" value="#{cmsg.edit_user_details_header}" tooltip="#{cmsg.edit_user_details_icon_tooltip}" action="wai:dialog:userConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
			<circabc:param id="view-my-account-more-action-id" name="id" value="#{WaiDialogManager.bean.userDetails.nodeRef.id}" />
		</circabc:actionLink>
	</circabc:panel>

	<h:outputText id="brbr" value="<br /><br />" escape="false" rendered="#{WaiDialogManager.bean.editDetailsAllowed}" />

	<circabc:panel id="identityPart" styleClass="editUserDetailsPart">
		<h:outputText value="#{cmsg.edit_user_details_identity}"/>
	</circabc:panel>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
		<h:graphicImage value="#{WaiDialogManager.bean.avatarPath}" title="#{cmsg.edit_user_details_avatar_tooltip}" alt="#{cmsg.edit_user_details_avatar_tooltip}" styleClass="avatar"/>
		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
			<h:outputText value="#{cmsg.edit_user_details_username}: " styleClass="propertiesLabel"/>
			<h:outputText value="#{WaiDialogManager.bean.userDetails.displayId}"  />

			<h:outputText value="#{cmsg.edit_user_details_firstname}: " styleClass="propertiesLabel"/>
			<h:outputText value="#{WaiDialogManager.bean.userDetails.firstName}"  />

			<h:outputText value="#{cmsg.edit_user_details_lastname}: " styleClass="propertiesLabel"/>
			<h:outputText value="#{WaiDialogManager.bean.userDetails.lastName}" />

			<h:outputText value="#{cmsg.edit_user_details_email}: " styleClass="propertiesLabel" rendered="#{WaiDialogManager.bean.userDetails.personalDataHidden == false}"/>
			<h:outputText value="#{WaiDialogManager.bean.userDetails.email}" rendered="#{WaiDialogManager.bean.userDetails.personalDataHidden == false}"/>

			<h:outputText value="#{cmsg.edit_user_details_profile_url}: " styleClass="propertiesLabel" />
			<h:outputText value="#{WaiDialogManager.bean.profileUrl}" />
		</h:panelGrid>
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

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" rendered="#{WaiDialogManager.bean.userDetails.personalDataHidden == false}" >
		<h:outputText value="#{cmsg.edit_user_details_title_prop}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.title}"   />

		<h:outputText value="#{cmsg.edit_user_details_organisation}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.organisation}"   />

		<h:outputText value="#{cmsg.edit_user_details_phone}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.phone}"   />

		<h:outputText value="#{cmsg.edit_user_details_postal_address}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.postalAddress}"   />

		<h:outputText value="#{cmsg.edit_user_details_description}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.description}"  />

		<h:outputText value="#{cmsg.edit_user_details_fax}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.fax}"   />

		<h:outputText value="#{cmsg.edit_user_details_url_address}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.url}"  />
	</h:panelGrid>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" rendered="#{WaiDialogManager.bean.userDetails.personalDataHidden == true}" >
		<h:outputText value="#{cmsg.edit_user_details_organisation}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.userDetails.organisation}"   />
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
		<h:outputText value="#{WaiDialogManager.bean.interfaceLanguageStr}" id="ContentFilterLanguage" />

		<h:outputText value="#{msg.language}:" styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.contentLanguageStr}" id="InterfaceLanguage" />

		<h:outputText value="#{cmsg.edit_user_details_global_notification}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.notificationStr}" id="GlobalNotification" />

		<h:outputText value="#{cmsg.edit_user_details_visibility}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{WaiDialogManager.bean.visibilityStr}" id="PersonalVisibiliy" />

		<h:outputText value="#{cmsg.edit_user_details_signature}: " styleClass="propertiesLabel"/>
		<h:outputText value="#{DialogManager.bean.userDetails.signature}" escape="false" />
	</h:panelGrid>

	<circabc:panel id="topOfPageAnchorUserOpt" styleClass="topOfPageAnchor" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	<circabc:panel id="space3" styleClass="divspacer3px" />

</circabc:panel>
