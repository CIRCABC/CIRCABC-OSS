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

<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainForm" styleClass="contentMainForm">

		<circabc:panel id="createspace-main-section" styleClass="signup_rub_title" >
			<h:outputText value="#{cmsg.library_space_properties}"  />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >

			<%--  The title --%>
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{msg.title}:" styleClass="propertiesLabelTiny"/>
			<h:inputText id="space_title" value="#{WaiDialogManager.bean.title}" maxlength="200" size="35" immediate="false"/>

			<%--  The description --%>
			<h:outputText value="" />
			<h:outputText value="#{msg.description}:" styleClass="propertiesLabelTiny"/>
			<h:inputTextarea id="previous" value="#{WaiDialogManager.bean.description}" rows="3" cols="55" readonly="false" />

			<%--  The space type --%>
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.library_create_space_select_type}:" styleClass="propertiesLabelTiny"/>
			<h:selectOneRadio id="select-type" layout="pageDirection" value="#{WaiDialogManager.bean.containerType}" onclick="showPanel(this.value);" >
				<f:selectItems  value="#{WaiDialogManager.bean.types}" />
			</h:selectOneRadio>
		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>

		<circabc:panel id="space-block" rendered="#{WaiDialogManager.bean.viewSpacePanel}">
			<%-- Visible if select type: SPACE--%>
			<circabc:panel id="title-space" styleClass="signup_rub_title" >
				<h:outputText value="#{cmsg.library_create_space_space_option}"  />
			</circabc:panel>

			<circabc:panel id="warn-space" styleClass="infoPanel" styleClassLabel="infoContent" >
				<h:graphicImage id="warn-space-icon" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
				<h:outputText id="warn-space-txt" value="&nbsp;&nbsp;#{cmsg.library_create_space_space_option_warn}" escape="false" />
			</circabc:panel>

			<h:panelGrid id="panel-space" columns="3" cellpadding="3" cellspacing="3" border="0" >
				<%--  The expiration date --%>
				<h:graphicImage id="focus-for-space" value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
				<h:outputText value="#{msg.icon}:" styleClass="propertiesLabelTiny"/>
				<a:imagePickerRadioPanel id="space-icon" columns="6" spacing="4" value="#{WaiDialogManager.bean.spaceIcon}"
					panelBorder="greyround" panelBgcolor="#F5F5F5">
					<a:listItems value="#{DialogManager.bean.spaceIcons}" />
				</a:imagePickerRadioPanel>

				<h:outputText value="" rendered="#{WaiDialogManager.bean.viewExpirationDate}"/>
				<h:outputText value="#{cmsg.library_create_space_expiration_date}: " rendered="#{WaiDialogManager.bean.viewExpirationDate}" styleClass="propertiesLabelTiny"/>
				<circabc:inputDatePicker value="#{WaiDialogManager.bean.expirationDate}" startYear="#{WaiDialogManager.bean.startYear}" yearCount="#{DatePickerGenerator.yearCount}" rendered="#{WaiDialogManager.bean.viewExpirationDate}"/>

				<h:outputText value="" rendered="#{WaiDialogManager.bean.viewShareThisSpace}"/>
				<h:outputText  value="#{cmsg.library_create_space_open_share_space}:" styleClass="propertiesLabelTiny" rendered="#{WaiDialogManager.bean.viewShareThisSpace}"/>
				<h:selectBooleanCheckbox value="#{WaiDialogManager.bean.manageShareAfter}" title="#{cmsg.library_create_space_open_share_space_tooltip}" rendered="#{WaiDialogManager.bean.viewShareThisSpace}"/>

			</h:panelGrid>
		</circabc:panel>

		<circabc:panel id="dossier-block" rendered="#{WaiDialogManager.bean.viewDossierPanel}">
			<%-- Visible if select type: DOSSIER --%>
			<circabc:panel id="title-dossier" styleClass="signup_rub_title" >
				<h:outputText value="#{cmsg.library_create_space_dossier_option}"  />
			</circabc:panel>

			<circabc:panel id="warn-dossier" styleClass="infoPanel" styleClassLabel="infoContent" >
				<h:graphicImage id="warn-dossier-icon" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
				<h:outputText id="warn-dossier-txt" value="&nbsp;&nbsp;#{cmsg.library_create_space_dossier_option_warn}" escape="false" />
			</circabc:panel>

			<h:panelGrid id="panel-dossier" columns="3" cellpadding="3" cellspacing="3" border="0" >
				<h:graphicImage id="focus-for-dossier" value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
				<h:outputText value="#{msg.icon}:" styleClass="propertiesLabelTiny"/>
				<a:imagePickerRadioPanel id="dossier-icon" columns="6" spacing="4" value="#{WaiDialogManager.bean.dossierIcon}"
					panelBorder="greyround" panelBgcolor="#F5F5F5">
					<a:listItems value="#{DialogManager.bean.dossierIcons}" />
				</a:imagePickerRadioPanel>
			</h:panelGrid>
		</circabc:panel>

		<circabc:panel id="shared-block" rendered="#{WaiDialogManager.bean.viewSharePanel}">
			<%-- Visible if select type: SHARE SPACE LINK--%>
			<circabc:panel id="title-share" styleClass="signup_rub_title" >
				<h:outputText value="#{cmsg.library_create_space_sharedspace_option}"  />
			</circabc:panel>

			<circabc:panel id="warn-share" styleClass="infoPanel" styleClassLabel="infoContent"  >
				<h:graphicImage id="warn-share-icon" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
				<h:outputText id="warn-share-txt" value="&nbsp;&nbsp;#{cmsg.library_create_space_sharedspace_option_warn}" escape="false" />
			</circabc:panel>

			<f:verbatim><br /></f:verbatim>
			<h:panelGrid id="panel-share" columns="3" cellpadding="3" cellspacing="3" border="0" >
				<h:graphicImage id="focus-for-share" value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
				<h:outputLabel  value="#{cmsg.library_create_space_select_shared_space}:" styleClass="propertiesLabelTiny"/>
				<h:selectOneMenu value="#{DialogManager.bean.selectedSharedSpace}">
					<f:selectItems value="#{DialogManager.bean.availableSharedSpaces}" />
				</h:selectOneMenu>
			</h:panelGrid>
		</circabc:panel>

</circabc:panel>

<script type="text/javascript">
	window.onload = pageLoaded;

    function pageLoaded(){
    	// if this method is called, the client support JS. Remove warning messages
		document.getElementById("warn-spaceGlobal").style.display =  'none' ;
		document.getElementById("warn-shareGlobal").style.display =  'none' ;
		document.getElementById("warn-dossierGlobal").style.display =  'none' ;

		var select = document.getElementsByName("FormPrincipal:select-type");
		var val  = "__TYPE_SPACE";
		for (var i=0; i < select.length; i++)
		{
			 if (select[i].checked)
			 {
			 	var val = select[i].value;
			 }
		}

		showPanel(val);
	}

    function showPanel(type){

		blockSpace = document.getElementById("space-blockGlobal");
		blockShared = document.getElementById("shared-blockGlobal");
		blockDossier = document.getElementById("dossier-blockGlobal");

		if(type == "__TYPE_SPACE"){

			blockSpace.style.display = 'block';
			blockShared.style.display =  'none' ;
			blockDossier.style.display =  'none' ;
			document.getElementById("FormPrincipal:focus-for-space").focus();
		}
		else if(type == "__TYPE_SHARED_LINK"){

			blockSpace.style.display = 'none';
			blockShared.style.display =  'block';
			blockDossier.style.display =  'none' ;
			document.getElementById("FormPrincipal:focus-for-share").focus();
		}
		else if(type == "__TYPE_DOSSIER"){

			blockSpace.style.display = 'none';
			blockShared.style.display =  'none';
			blockDossier.style.display =  'block' ;
			document.getElementById("FormPrincipal:focus-for-dossier").focus();
		}
	}
</script>
