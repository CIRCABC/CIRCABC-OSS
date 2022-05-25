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

<script type="text/javascript">
	window.onload = pageLoaded;

    function pageLoaded(){
    	// get collection of radio buttons
	  	audienceStatusRadioButtons = document.getElementsByName("FormPrincipal:meeting-audienceStatus");
	  	for(i=0;i<audienceStatusRadioButtons.length;i++)
	  	{
	  		if (audienceStatusRadioButtons[i].type=="radio"){
	  			if (audienceStatusRadioButtons[i].checked){
	  				showPanel(audienceStatusRadioButtons[i].value);
	  				break;
	  			}
	  		}
		}
	}

    function showPanel(status){
    	//alert(status);
		blockOpen = document.getElementById("block-openGlobal");
		blockClosed = document.getElementById("block-closedGlobal");

		if(status == "Open"){
			blockClosed.style.display =  'none' ;
			blockOpen.style.display = 'block';

			document.getElementById("FormPrincipal:meeting-audience-send-to-all").focus();
		}
		else{
			blockOpen.style.display =  'none';
			blockClosed.style.display = 'block';

			document.getElementById("FormPrincipal:meeting-external-user").focus();
		}
	}
</script>

<circabc:panel id="audienceWizardMainForm" styleClass="contentFullPage">

	<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
	<h:outputText value="&nbsp;#{cmsg.event_create_meetings_wizard_step1_audience_status}: " escape="false"/>
	<h:selectOneRadio id="meeting-audienceStatus" layout="lineDirection" value="#{WaiWizardManager.bean.appointment.audienceStatus}" converter="eu.cec.digit.circabc.faces.EnumConverter" onclick="showPanel(this.value);" >
		<f:selectItems  value="#{WaiWizardManager.bean.audienceStatuses}" />
	</h:selectOneRadio>

	<f:verbatim><br /></f:verbatim>

	<circabc:panel id="block-open"  >

		<circabc:panel id="audience-open-info" styleClass="infoPanel" styleClassLabel="infoContent" >
			<h:graphicImage id="audience-open-info-image" value="/images/extension/icons/info.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
			<h:outputText id="audience-open-info-text" value="#{cmsg.event_create_meetings_wizard_step3_info_open}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:selectBooleanCheckbox id="meeting-audience-send-to-all" value="#{WaiWizardManager.bean.appointment.enableNotification}" title="#{cmsg.event_create_meetings_wizard_step3_send_details}" />
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step3_send_details}"  />
	</circabc:panel>

	<circabc:panel id="block-closed"  >
	
	<circabc:panel id="meeting-audience-internalUser-section" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step3_section1}"  />
		</circabc:panel>

		<a:genericPicker id="picker" showAddButton="false" filters="#{WaiWizardManager.bean.filters}" queryCallback="#{WaiWizardManager.bean.pickerCallback}" width="350" />

		<h:commandButton id="AddToList" value="#{msg.add_to_list_button}"
	   	              actionListener="#{WaiWizardManager.bean.addSelection}"
			          styleClass="wizardButton" />

		<h:dataTable value="#{WaiWizardManager.bean.userDataModel}" var="row"
			rowClasses="selectedItemsRow,selectedItemsRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4"
			rendered="#{WaiWizardManager.bean.userDataModel.rowCount != 0}">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msg.name}" />
				</f:facet>
				<h:outputText value="#{row.shortDisplayName}" />
			</h:column>
			<h:column>
				<circabc:actionLink actionListener="#{WaiWizardManager.bean.removeSelection}"
					image="/images/icons/delete.gif" tooltip="#{msg.remove}"
					value="#{msg.remove}" showLink="false" styleClass="pad6Left" />
			</h:column>
		</h:dataTable>
		

		<f:verbatim><br /><br /><br /></f:verbatim>

		
		<circabc:panel id="audience-closed-info" styleClass="infoPanel" styleClassLabel="infoContent" >
			<h:graphicImage id="audience-closed-info-image" value="/images/extension/icons/info.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
			<h:outputText id="audience-closed-info-text" value="#{cmsg.event_create_meetings_wizard_step3_info_closed}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<circabc:panel id="meeting-audience-externalUser-section" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step3_section2}"  />
		</circabc:panel>
		<h:outputText value="#{event_create_meetings_wizard_step3_section2_description}" />
		<h:inputTextarea id="meeting-external-user" value="#{WaiWizardManager.bean.externalUser}" rows="5" cols="40" readonly="false"/>
	</circabc:panel>
</circabc:panel>
