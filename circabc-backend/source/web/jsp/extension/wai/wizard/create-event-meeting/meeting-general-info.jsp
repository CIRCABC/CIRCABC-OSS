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

<circabc:panel id="meetingInfoWizardMainForm" styleClass="contentFullPage">

	<circabc:panel id="meeting-info-wz-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_sectionTitle}"  />
	</circabc:panel>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_language}: "/>
			<h:selectOneMenu id="meeting-language" value="#{WaiWizardManager.bean.appointment.language}">
				<f:selectItems value="#{WaiWizardManager.bean.languages}"/>
			</h:selectOneMenu>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_meeting_title}: "/>
			<h:inputText id="meeting-title" value="#{WaiWizardManager.bean.appointment.title}" maxlength="1024" size="35" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_abstract}: "/>
			<h:inputTextarea id="meeting-abstract" value="#{WaiWizardManager.bean.appointment.eventAbstract}" rows="3" cols="40" readonly="false"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_type}: "/>
			<h:inputText id="meeting-type" value="#{WaiWizardManager.bean.appointment.meetingTypeString}" maxlength="100" size="35" immediate="false"/>
			
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_date}: "/>
			<circabc:inputDatePicker id="meeting-date" value="#{WaiWizardManager.bean.appointment.startDateAsDate}" yearCount="#{DatePickerGenerator.yearCount}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs}: "/>
			<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

				<h:selectOneRadio id="meeting-main-occurence" layout="pageDirection" value="#{WaiWizardManager.bean.appointment.occurenceRate.mainOccurence}" converter="eu.cec.digit.circabc.faces.EnumConverter">
					<f:selectItems value="#{WaiWizardManager.bean.mainOccurences}"/>
				</h:selectOneRadio>
				<circabc:panel id="meeting-occurence-specification">
					<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs_once}" />
					<f:verbatim><br /></f:verbatim>

					<h:selectOneMenu id="meeting-time-occurence" value="#{WaiWizardManager.bean.appointment.occurenceRate.timesOccurence}" converter="eu.cec.digit.circabc.faces.EnumConverter">
						<f:selectItems value="#{WaiWizardManager.bean.timesOccurences}"/>
					</h:selectOneMenu>
					<h:outputText value="&nbsp;#{cmsg.event_create_meetings_wizard_step1_occurs_for}&nbsp;" escape="false"/>
					<h:inputText id="meeting-times-one" size="3" value="#{WaiWizardManager.bean.timesOneAsString}" immediate="false" />
					<h:outputText value="&nbsp;#{cmsg.event_create_meetings_wizard_step1_occurs_times}" escape="false"/>
					<f:verbatim><br /></f:verbatim>

					<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_occurs_every}&nbsp;" escape="false"/>
					<h:inputText  id="meeting-every" size="3" value="#{WaiWizardManager.bean.everyAsString}" immediate="false" />
					<h:outputText value="&nbsp;" escape="false"/>
					<h:selectOneMenu id="event-every-time-occurence" value="#{WaiWizardManager.bean.appointment.occurenceRate.everyTimesOccurence}" converter="eu.cec.digit.circabc.faces.EnumConverter">
						<f:selectItems value="#{WaiWizardManager.bean.everyTimesOccurences}"/>
					</h:selectOneMenu>
					<h:outputText value="&nbsp;#{cmsg.event_create_meetings_wizard_step1_occurs_for}&nbsp;" escape="false"/>
					<h:inputText id="meeting-times-two" size="3" value="#{WaiWizardManager.bean.timesTwoAsString}" immediate="false" />
					<h:outputText value="&nbsp;#{cmsg.event_create_meetings_wizard_step1_occurs_times}" escape="false"/>
				</circabc:panel>
			</h:panelGrid>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_start_time}: "/>
			<circabc:inputDatePicker id="meeting-start-date" showTime="true" showDate="false" timeAsList="true" value="#{WaiWizardManager.bean.appointment.startTimeAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_end_time}: "/>
			<circabc:inputDatePicker id="meeting-end-date" showTime="true" showDate="false" timeAsList="true" value="#{WaiWizardManager.bean.appointment.endTimeAsDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" />


			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_time_zone}: "/>
			<h:selectOneMenu id="meeting-timezone" value="#{WaiWizardManager.bean.appointment.timeZoneId}"  >
				<f:selectItems value="#{WaiWizardManager.bean.timeZones}"/>
			</h:selectOneMenu>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_organization}: "/>
			<h:inputText id="meeting-organization" value="#{WaiWizardManager.bean.appointment.organization}" maxlength="1024" size="40" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_location}: "/>
			<h:inputText id="meeting-location" value="#{WaiWizardManager.bean.appointment.location}" maxlength="1024" size="40" immediate="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_message}: "/>
			<h:inputTextarea id="meeting-message" value="#{WaiWizardManager.bean.appointment.invitationMessage}" rows="3" cols="40" readonly="false"/>

			<h:outputText value="" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_agenda}: "/>
			<h:inputTextarea id="meeting-agenda" value="#{WaiWizardManager.bean.appointment.agenda}" rows="3" cols="40" readonly="false"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.event_create_meetings_wizard_step1_availability}: "/>
			<h:selectOneRadio id="meeting-availability" layout="lineDirection" value="#{WaiWizardManager.bean.appointment.availability}" converter="eu.cec.digit.circabc.faces.EnumConverter">
				<f:selectItems value="#{WaiWizardManager.bean.availabilities}"/>
			</h:selectOneRadio>
	</h:panelGrid>

</circabc:panel>
