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



<circabc:panel id="contentMainForm" styleClass="contentMainForm">
	<circabc:panel id="panelAddUrl" label="#{cmsg.library_add_url_properties}" styleClass="panelCreateReplyGlobal" styleClassLabel="panelCreateReplyLabel" tooltip="#{cmsg.library_add_url_description}">
		<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
			<%--  The name --%>
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{msg.title}: "/>
			<h:inputText id="url_name" value="#{WaiDialogManager.bean.title}" maxlength="200" size="35" immediate="false"/>

			<%--  The URL --%>
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="URL: " />
			<h:inputText id="url" value="#{WaiDialogManager.bean.url}" maxlength="200" size="35" immediate="false"/>
			
			<%--  Disable notifications checkbox --%>
			<h:selectBooleanCheckbox id="check-disable-notif" value="#{WaiDialogManager.bean.disableNotifications}" title="#{cmsg.library_add_url_notification_disable_mechanism}" />
			<h:outputText id="text-notif-disabled" value="#{cmsg.library_add_url_notification_disable_mechanism}"/>
			
		</h:panelGrid>
	</circabc:panel>
</circabc:panel>