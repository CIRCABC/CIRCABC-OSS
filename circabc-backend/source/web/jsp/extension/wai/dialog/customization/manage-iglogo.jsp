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
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/noscript.js" ></script>

<%@ page isELIgnored="false" %>

<circabc:panel id="contentMainFormManageIgLogoCustomization" styleClass="contentMainForm">

	<circabc:panel id="manage-iglogo-info" styleClass="infoPanel" styleClassLabel="infoContent" >
		<h:graphicImage id="manage-iglogo-image-info" value="/images/icons/info_icon.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
		<h:outputText id="manage-iglogo-message" value="&nbsp;&nbsp;#{DialogManager.bean.infoMessage}" escape="false" />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>


	<circabc:panel id="manage-iglogo-uploadlogo-section-file" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.manage_iglogo_dialog_define_ighome_addnew_logo_comp}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:upload id="uploader" value="#{DialogManager.bean.iconFile}" framework="FormPrincipal"/>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="manage-iglogo-uploadlogo-section-repo" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.manage_iglogo_dialog_define_ighome_addnew_logo_lib}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<circabc:nodeSelector id="select-image-repo" value="#{DialogManager.bean.iconLink}"
	 		styleClass="selector"
	 		rootNode="#{DialogManager.bean.libraryId}"
	 		label="#{cmsg.manage_iglogo_dialog_define_ighome_select_library}"
	 		pathLabel="#{cmsg.path_label}"
			pathErrorMessage="#{cmsg.path_error_message}"
	 		showContents="true"/>
	<f:verbatim>
		<br />
	</f:verbatim>



	<f:verbatim>
		<br />
	</f:verbatim>



	<circabc:panel id="manage-iglogo-alllogos-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.manage_iglogo_dialog_define_ighome_select_logo}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:dataTable id="allLogosTable" value="#{DialogManager.bean.logosDataModel}" var="logo"
		rowClasses="recordSetRow,recordSetRowAlt"
		styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
		cellspacing="0" cellpadding="4" width="100%">

		<h:column id="logo-diaplay-col">
			<f:facet name="header">
				<h:outputText id="logo-display-title" value="#{cmsg.manage_iglogo_dialog_preview}" styleClass="logoInList"/>
			</f:facet>
			<h:graphicImage id="logo-diaplay" value="#{logo.downloadPath}" title="#{logo.name}" styleClass="logoInList"/>
		</h:column>

		<h:column id="logo-name-col" >
			<f:facet name="header">
				<h:outputText id="logo-name-title" value="#{cmsg.name}" />
			</f:facet>
			<h:outputText id="logo-name" value="#{logo.name}" escape="false" />
		</h:column>

		<h:column id="logo-path-col">
			<f:facet name="header">
				<h:outputText id="logo-path-title" value="#{cmsg.manage_navigation_dialog_inherited}" />
			</f:facet>
			<h:outputText id="logo-path" value="#{logo.definedOnPath}" escape="false" />
		</h:column>

		<h:column id="actions">
			<f:facet name="header">
				<h:outputText value="#{cmsg.actions}" />
			</f:facet>

			<a:booleanEvaluator value="#{logo.selected}">
				<circabc:actionLink id="deselectLogoAction" actionListener="#{DialogManager.bean.deselect}" onclick="showWaitProgress();"
					image="/images/icons/blog_remove.png" value="#{cmsg.manage_iglogo_dialog_deselect_title}"
					tooltip="#{cmsg.manage_iglogo_dialog_deselect_tooltip}" showLink="false" styleClass="pad6Left" />
				<f:verbatim>&nbsp;</f:verbatim>
			</a:booleanEvaluator>

			<a:booleanEvaluator value="#{logo.selectable}">
				<circabc:actionLink id="selectLogoAction" actionListener="#{DialogManager.bean.select}" onclick="showWaitProgress();"
					image="/images/icons/blog_post.png" value="#{cmsg.manage_iglogo_dialog_select_title}"
					tooltip="#{cmsg.manage_iglogo_dialog_select_tooltip}" showLink="false" styleClass="pad6Left" />
				<f:verbatim>&nbsp;</f:verbatim>
			</a:booleanEvaluator>

			<circabc:displayer rendered="#{logo.removable}">
				<circabc:actionLink id="removeLogoAction" action="wai:dialog:removeLogoPreferenceDialog" actionListener="#{WaiDialogManager.setupParameters}"
					image="/images/icons/delete.gif" value="#{cmsg.remove_iglogo_action_tooltip}"
					tooltip="#{cmsg.remove_iglogo_action_tooltip}" showLink="false"  >
	            	<circabc:param id="remove-logo-param-id" name="id" value="#{DialogManager.bean.actionNode.id}" />
		            <circabc:param id="remove-logo-param-name" name="logoName" value="#{logo.name}" />
		        	<circabc:param id="remove-logo-param-service" name="service" value="Administration" />
		        	<circabc:param id="remove-logo-param-activity" name="activity" value="Delete logo" />
	    	    </circabc:actionLink>
			</circabc:displayer>
			<f:verbatim>&nbsp;</f:verbatim>
		</h:column>
	</h:dataTable>

	<f:verbatim><br /></f:verbatim>

	<circabc:displayer rendered="#{DialogManager.bean.homeConfigAllowed}">

		<circabc:panel id="manage-iglogo-mainpage-section" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.manage_iglogo_dialog_define_ighome_logo_display}" />
		</circabc:panel>

		<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
			<h:outputText value="#{cmsg.manage_iglogo_dialog_display}:" />
			<h:selectBooleanCheckbox value="#{DialogManager.bean.logoDisplayedOnMainPage}" disabled="#{DialogManager.bean.logoSelected == false}"/>
			<h:outputText value="" style="font-style: italic;" />

			<h:outputText value="#{cmsg.manage_iglogo_dialog_atleft}:"  />
			<h:selectBooleanCheckbox value="#{DialogManager.bean.mainPageLogoAtLeft}" disabled="#{DialogManager.bean.logoSelected == false}"/>
			<h:outputText value="#{cmsg.manage_iglogo_dialog_atleft_desc}"  style="font-style: italic;"/>

			<h:outputText value="#{cmsg.manage_iglogo_dialog_width}:"  />
			<h:inputText value="#{DialogManager.bean.mainPageLogoWidth}" disabled="#{DialogManager.bean.logoSelected == false}"/>
			<h:outputText value="#{cmsg.manage_iglogo_dialog_width_desc}"  style="font-style: italic;"/>

			<h:outputText value="#{cmsg.manage_iglogo_dialog_height}:"  />
			<h:inputText value="#{DialogManager.bean.mainPageLogoHeight}" disabled="#{DialogManager.bean.logoSelected == false}"/>
			<h:outputText value="#{cmsg.manage_iglogo_dialog_height_desc}"  style="font-style: italic;"/>

			<h:outputText value="#{cmsg.manage_iglogo_dialog_force}:"  />
			<h:selectBooleanCheckbox value="#{DialogManager.bean.mainPageSizeForced}" disabled="#{DialogManager.bean.logoSelected == false}"/>
			<h:outputText value="#{cmsg.manage_iglogo_dialog_force_desc}"  style="font-style: italic;"/>

		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>
	</circabc:displayer>


	<circabc:panel id="manage-iglogo-otherpages-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.manage_iglogo_dialog_define_allpages_logo_display}"  />
	</circabc:panel>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
		<h:outputText value="#{cmsg.manage_iglogo_dialog_display}:"  />
		<h:selectBooleanCheckbox value="#{DialogManager.bean.logoDisplayedOnAllPages}" disabled="#{DialogManager.bean.logoSelected == false}"/>
		<h:outputText value=""  style="font-style: italic;" />

		<h:outputText value="#{cmsg.manage_iglogo_dialog_width}:"  />
		<h:inputText value="#{DialogManager.bean.otherPagesLogoWidth}" disabled="true"/>
		<h:outputText value="#{cmsg.manage_iglogo_dialog_not_editable}"  style="font-style: italic;"/>

		<h:outputText value="#{cmsg.manage_iglogo_dialog_height}:"  />
		<h:inputText value="#{DialogManager.bean.otherPagesLogoHeight}" disabled="true"/>
		<h:outputText value="#{cmsg.manage_iglogo_dialog_not_editable}"  style="font-style: italic;"/>
	</h:panelGrid>

	<f:verbatim><br /></f:verbatim>

	<circabc:displayer rendered="#{DialogManager.bean.serviceConfigAllowed}">

		<circabc:panel id="manage-iglogo-service-section" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.manage_iglogo_dialog_define_services_logo_display}"  />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:dataTable id="allServicesTable" value="#{DialogManager.bean.servicesDataModel}" var="serv"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" >

			<h:column id="service-name-col">
				<f:facet name="header">
					<h:outputText id="service-name-title" value="#{cmsg.service}"  styleClass="logoInList"/>
				</f:facet>
				<h:outputText id="service-name" value="#{serv.serviceName}" styleClass="selectedItemsNoBack"/>
			</h:column>


			<h:column id="service-over-col">
				<f:facet name="header">
					<h:outputText id="service-over-title" value="#{cmsg.manage_iglogo_dialog_override}"  styleClass="logoInList"/>
				</f:facet>
				<h:outputText id="service-over-text" value="#{serv.overrided}" converter="javax.faces.Boolean"/>
			</h:column>

			<h:column id="service-display-col">
				<f:facet name="header">
					<h:outputText id="service-display-title" value="#{cmsg.manage_iglogo_dialog_display}"  styleClass="logoInList"/>
				</f:facet>
				<h:selectBooleanCheckbox id="service-display" value="#{serv.display}" />
			</h:column>

			<h:column id="service-logo-col">
				<f:facet name="header">
					<h:outputText id="service-logo-title" value="#{cmsg.manage_iglogo_dialog_logo}"  styleClass="logoInList"/>
				</f:facet>
				<h:selectOneMenu id="service-over" value="#{serv.selectedImageRef}">
					<f:selectItems value="#{serv.availableImages}"/>
				</h:selectOneMenu>
			</h:column>
		</h:dataTable>

		<f:verbatim><br /></f:verbatim>
	</circabc:displayer>

	<f:verbatim><br /></f:verbatim>

	<circabc:panel id="topOfPageAnchorIgLogo" styleClass="topOfPageAnchor"  >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorIgLogo-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorIgLogo-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
</circabc:panel>