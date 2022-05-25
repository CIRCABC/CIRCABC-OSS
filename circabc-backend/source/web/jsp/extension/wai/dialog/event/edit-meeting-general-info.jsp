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

<circabc:panel id="meetingInfoDialogMainForm" styleClass="contentMainForm">

	<%-- Include the the modify recurrence selection --%>
	<%@ include file="/jsp/extension/wai/dialog/event/modify-recurrence.jsp" %>

	<circabc:panel id="meeting-info-wz-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_sectionTitle}"  />
	</circabc:panel>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_language}: "/>
			<h:selectOneMenu id="meeting-language" value="#{WaiDialogManager.bean.appointment.language}">
				<f:selectItems value="#{WaiDialogManager.bean.languages}"/>
			</h:selectOneMenu>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_meeting_title}: "/>
			<h:inputText id="meeting-title" value="#{WaiDialogManager.bean.appointment.title}" maxlength="1024" size="35" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_abstract}: "/>
			<h:inputTextarea id="meeting-abstract" value="#{WaiDialogManager.bean.appointment.eventAbstract}" rows="3" cols="40" readonly="false"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_type}: "/>
			
			<h:inputText id="meeting-type" value="#{WaiDialogManager.bean.appointment.meetingTypeString}" maxlength="100" size="35" immediate="false"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_date}: "/>
			<circabc:inputDatePicker id="meeting-date" showTime="false" showDate="true" timeAsList="false" value="#{WaiDialogManager.bean.appointment.dateAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />


			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_date}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.appointment.startDateAsDate}">
				<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
			</h:outputText>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.occurenceAsString}"  />

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_time}: "/>
			<circabc:inputDatePicker id="meeting-start-date" showTime="true" showDate="false" timeAsList="true" value="#{WaiDialogManager.bean.appointment.startTimeAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />


			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_end_time}: "/>
			<circabc:inputDatePicker id="meeting-end-date" showTime="true" showDate="false" timeAsList="true" value="#{WaiDialogManager.bean.appointment.endTimeAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />

			<h:outputText value=" " styleClass="propertiesLabel" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_time_zone}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.appointment.timeZoneId}"  />

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_organization}: "/>
			<h:inputText id="meeting-organization" value="#{WaiDialogManager.bean.appointment.organization}" maxlength="1024" size="40" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_location}: "/>
			<h:inputText id="meeting-location" value="#{WaiDialogManager.bean.appointment.location}" maxlength="1024" size="40" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_message}: "/>
			<h:inputTextarea id="meeting-message" value="#{WaiDialogManager.bean.appointment.invitationMessage}" rows="3" cols="40" readonly="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_agenda}: "/>
			<h:inputTextarea id="meeting-agenda" value="#{WaiDialogManager.bean.appointment.agenda}" rows="3" cols="40" readonly="false"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_availability}: "/>
			<h:selectOneRadio id="meeting-availability" layout="lineDirection" value="#{WaiDialogManager.bean.appointment.availability}" converter="eu.cec.digit.circabc.faces.EnumConverter">
				<f:selectItems value="#{WaiDialogManager.bean.availabilities}"/>
			</h:selectOneRadio>
	</h:panelGrid>

</circabc:panel>
