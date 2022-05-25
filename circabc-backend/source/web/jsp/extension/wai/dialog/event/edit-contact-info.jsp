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

<circabc:panel id="contactInfoDialogMainForm" styleClass="contentMainForm">

	<%-- Include the the modify recurrence selection --%>
	<%@ include file="/jsp/extension/wai/dialog/event/modify-recurrence.jsp" %>

	<circabc:panel id="meeting-audience-contactInfo-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_properties}" />
	</circabc:panel>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_name}: "/>
		<h:inputText id="meeting-contact-name" value="#{WaiDialogManager.bean.appointment.name}" maxlength="1024" size="35" immediate="false"/>

		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_phone}: "/>
		<h:inputText id="meeting-contact-phone" value="#{WaiDialogManager.bean.appointment.phone}" maxlength="1024" size="35" immediate="false"/>

		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_email}: "/>
		<h:inputText id="meeting-contact-email" value="#{WaiDialogManager.bean.appointment.email}" maxlength="1024" size="35" immediate="false"/>

		<h:outputText value="" />
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step4_url}: "/>
		<h:inputText id="meeting-contact-url" value="#{WaiDialogManager.bean.appointment.url}" maxlength="1024" size="35" immediate="false"/>
	</h:panelGrid>


</circabc:panel>
