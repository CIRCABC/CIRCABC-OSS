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

<circabc:panel id="eventInfoDialogMainForm" styleClass="contentMainForm">

	<%-- Include the the modify recurrence selection --%>
	<%@ include file="/jsp/extension/wai/dialog/event/modify-recurrence.jsp" %>

		<circabc:panel id="event-info-wz-section" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.event_create_event_wizard_step1_sectionTitle}"  />
		</circabc:panel>

		<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_language}: "/>
			<h:selectOneMenu id="event-language" value="#{WaiDialogManager.bean.appointment.language}">
				<f:selectItems value="#{WaiDialogManager.bean.languages}"/>
			</h:selectOneMenu>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_meeting_title}: "/>
			<h:inputText id="event-title" value="#{WaiDialogManager.bean.appointment.title}" maxlength="1024" size="35" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_abstract}: "/>
			<h:inputTextarea id="event-abstract" value="#{WaiDialogManager.bean.appointment.eventAbstract}" rows="3" cols="40" readonly="false"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_event_wizard_step1_type}: "/>
			<h:selectOneMenu id="event-type" value="#{WaiDialogManager.bean.appointment.eventType}" converter="eu.cec.digit.circabc.faces.EnumConverter">
				<f:selectItems value="#{WaiDialogManager.bean.eventTypes}"/>
			</h:selectOneMenu>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_event_wizard_step1_priority}: "/>
			<h:selectOneMenu id="event-priority" value="#{WaiDialogManager.bean.appointment.priority}" converter="eu.cec.digit.circabc.faces.EnumConverter">
				<f:selectItems value="#{WaiDialogManager.bean.priorities}"/>
			</h:selectOneMenu>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_date}: "/>
			<circabc:inputDatePicker id="event-date" showTime="false" showDate="true" timeAsList="false" value="#{WaiDialogManager.bean.appointment.dateAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_date}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.appointment.dateAsDate}" >
				<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
			</h:outputText>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_date}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.appointment.startDateAsDate}" >
				<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
			</h:outputText>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.occurenceAsString}"  />

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_time}: "/>
			<circabc:inputDatePicker id="event-start-date" showTime="true" showDate="false" timeAsList="true" value="#{WaiDialogManager.bean.appointment.startTimeAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_end_time}: "/>
			<circabc:inputDatePicker id="event-end-date" showTime="true" showDate="false" timeAsList="true" value="#{WaiDialogManager.bean.appointment.endTimeAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_time_zone}: " styleClass="propertiesLabel" />
			<h:outputText value="#{AppointmentDetailsBean.appointment.timeZoneId}"  />


			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_location}: "/>
			<h:inputText id="event-location" value="#{WaiDialogManager.bean.appointment.location}" maxlength="1024" size="40" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_message}: "/>
			<h:inputTextarea id="event-message" value="#{WaiDialogManager.bean.appointment.invitationMessage}" rows="3" cols="40" readonly="false"/>

		</h:panelGrid>
</circabc:panel>
