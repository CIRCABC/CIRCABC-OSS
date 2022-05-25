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

<circabc:panel id="contentMainAppointmentDetail" styleClass="contentMain">

    <%-- the right menu --%>
    <circabc:panel id="contentMainButton" styleClass="contentMainButton">
        <%--  The close button --%>
        <circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
            <h:commandButton id="close-button" styleClass="dialogButton" value="#{cmsg.close}" action="#{AppointmentDetailsBean.getCloseOutcome}" />
        </circabc:panel>
        <f:verbatim><br /><br /><br /></f:verbatim>
        <%--  The action list --%>
		<circabc:panel id="id-divspacer10px" styleClass="divspacer10px" />
       	<circabc:panel id="panelActions" label="#{cmsg.actions}" tooltip="#{cmsg.actions_tooltip}" styleClass="panelActionsGlobal" styleClassLabel="panelActionsLabel" >
           <circabc:actions id="actions_appointments" value="#{AppointmentDetailsBean.actionGroup}" context="#{AppointmentDetailsBean.appointmentNode}" vertical="true"/>
        </circabc:panel>
    </circabc:panel>

    <%-- the main content --%>
    <circabc:panel id="contentMainForm" styleClass="contentMainForm">

		<%-- Display the properties of an event --%>
        <circabc:panel id="panelGeneralDetails-event" label="#{cmsg.event_view_meetings_details_dialog_section1}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.event_view_meetings_details_dialog_section1_tooltip}"  rendered="#{AppointmentDetailsBean.meeting == false}">
			<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_language}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayLanguage}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_meeting_title}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.title}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_abstract}: " styleClass="propertiesLabel" />
				<h:inputTextarea readonly="true" cols="40" value="#{AppointmentDetailsBean.appointment.eventAbstract}"   />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_type}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayEventType}"  />

				<h:outputText value="#{cmsg.event_create_event_wizard_step1_priority}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayEventPriority}"  />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_date}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.dateAsDate}" >
					<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_date}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.startDateAsDate}" >
					<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.occurenceAsString}"  />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_time}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.startTimeAsDate}" >
					<a:convertXMLDate type="both" pattern="#{msg.time_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_end_time}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.endTimeAsDate}" >
					<a:convertXMLDate type="both" pattern="#{msg.time_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_time_zone}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.timeZoneId}"  />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_location}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.location}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_message}: " styleClass="propertiesLabel" />
				<h:inputTextarea readonly="true" cols="40" value="#{AppointmentDetailsBean.appointment.invitationMessage}" />

			</h:panelGrid>
        </circabc:panel>

		<%-- Display the properties of an meeting --%>
        <circabc:panel id="panelGeneralDetails-meeting" label="#{cmsg.event_view_meetings_details_dialog_section1}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.event_view_meetings_details_dialog_section1_tooltip}"  rendered="#{AppointmentDetailsBean.meeting == true}" >
			<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_language}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayLanguage}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_meeting_title}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.title}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_abstract}: " styleClass="propertiesLabel" />
				<h:inputTextarea readonly="true" cols="40" value="#{AppointmentDetailsBean.appointment.eventAbstract}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_type}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayMeetingType}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_date}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.dateAsDate}">
					<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_date}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.startDateAsDate}">
					<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.occurenceAsString}"  />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_time}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.startTimeAsDate}" >
					<a:convertXMLDate type="both" pattern="#{msg.time_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_end_time}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.endTimeAsDate}" >
					<a:convertXMLDate type="both" pattern="#{msg.time_pattern}" />
				</h:outputText>

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_time_zone}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.timeZoneId}"  />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_location}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.location}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_organization}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.organization}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_agenda}: " styleClass="propertiesLabel" />
				<h:inputTextarea readonly="true" cols="40" rows="#{AppointmentDetailsBean.agendaLines}" value="#{AppointmentDetailsBean.appointment.agenda}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_availability}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayMeetingAvailability}" />

				<h:outputText value="#{cmsg.event_view_meetings_details_dialog_your_status}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.userStatus}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_message}: " styleClass="propertiesLabel" />
				<h:inputTextarea readonly="true" cols="40" value="#{AppointmentDetailsBean.appointment.invitationMessage}" />

			</h:panelGrid>
        </circabc:panel>

        <circabc:panel id="topOfPageAnchorGeneralDetails" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-app-details-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-app-details-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>

		<%-- Display the relevant space --%>
        <circabc:displayer rendered="#{AppointmentDetailsBean.meeting}">
	        <circabc:panel id="panelRelevantSpace" label="#{cmsg.event_view_meetings_details_dialog_section2}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.event_view_meetings_details_dialog_section2_tooltip}" >
				<h:outputText value="#{cmsg.event_view_meetings_details_dialog_section2_description}: " styleClass="propertiesLabel" />
				<circabc:actionLink value="#{AppointmentDetailsBean.spaceDisplayPath}" actionListener="#{BrowseBean.clickWai}" tooltip="#{cmsg.event_view_meetings_details_dialog_section2_link_tooltip}" onclick="showWaitProgress();">
					<circabc:param  name="id" value="#{AppointmentDetailsBean.appointment.librarySection.id}" />
				</circabc:actionLink>
	        </circabc:panel>

	        <circabc:panel id="topOfPageAnchorRelevantSpace" styleClass="topOfPageAnchor"  >
	            <%-- Display the "back to top icon first and display the text after." --%>
	            <circabc:actionLink id="act-link-app-space-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
	            <circabc:actionLink id="act-link-app-space-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	        </circabc:panel>
        </circabc:displayer>

		<%-- Display the contact info --%>
        <circabc:panel id="panelContactInfo" label="#{cmsg.event_view_meetings_details_dialog_section3}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.event_view_meetings_details_dialog_section3_tooltip}" >
			<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_name}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.name}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_phone}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.phone}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_email}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.email}" />

				<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_url}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.appointment.url}" />
			</h:panelGrid>
        </circabc:panel>

        <circabc:panel id="topOfPageAnchorContactInfo" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-app-contact-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-app-contact-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>


		<%-- Display the audience --%>
        <circabc:panel id="panelAudience" label="#{cmsg.event_view_meetings_details_dialog_section4}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.event_view_meetings_details_dialog_section4_tooltip}" >
		        <h:outputText value="#{cmsg.event_create_meetings_wizard_step1_audience_status}: " styleClass="propertiesLabel" />
				<h:outputText value="#{AppointmentDetailsBean.displayAudienceStatus}" />

				<f:verbatim><br /><br /></f:verbatim>

				<h:dataTable var="user" value="#{AppointmentDetailsBean.audienceDetail}">
					<h:column>
                     	<h:outputText value="#{user.shortDisplayName}" styleClass="propertiesLabel" />
                  	</h:column>
                  	<h:column>
                     	<h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;" escape="false" />
                  	</h:column>
                  	<h:column>
                     	<h:outputText value="#{user.status}"/>
                  	</h:column>
				</h:dataTable>
        </circabc:panel>

        <circabc:panel id="topOfPageAnchorAudience" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-app-audience-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-app-audience-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>
	</circabc:panel>
</circabc:panel>

