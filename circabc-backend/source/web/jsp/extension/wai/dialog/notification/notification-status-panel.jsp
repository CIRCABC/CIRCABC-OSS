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


<%@ page isELIgnored="false" %>

<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:displayer id="notification-status-panel-displayer" rendered="#{NotificationStatusPanel.panelDisplayed}">

     <circabc:panel id="notification-status-panel-properties" label="#{cmsg.notification_panel_title}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.notification_panel_tooltip}">

     	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0"  rendered="#{NotificationStatusPanel.profile == null}"  >
       	<h:outputText id="notification-status-panel-global" value="#{cmsg.notification_panel_global_status}:" styleClass="propertiesLabelTiny"/>
       	<h:outputText id="notification-status-panel-global-value" value="#{NotificationStatusPanel.globalNotificationStatus}" />

       	<h:outputFormat id="notification-status-panel-user" value="#{cmsg.notification_panel_user_status}:" styleClass="propertiesLabelTiny">
			<circabc:param value="#{NotificationStatusPanel.userDisplayName}" />
       	</h:outputFormat>
       	<h:outputText id="notification-status-panel-user-value" value="#{NotificationStatusPanel.userNotificationStatus}" />
	</h:panelGrid>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" rendered="#{NotificationStatusPanel.profile != null}">
       	<h:outputText id="notification-status-panel-global-2" value="#{cmsg.notification_panel_global_status}:" styleClass="propertiesLabelTiny"/>
       	<h:outputText id="notification-status-panel-global-value-2" value="#{NotificationStatusPanel.globalNotificationStatus}" />

       	<h:outputFormat id="notification-status-panel-user-2" value="#{cmsg.notification_panel_user_status}:" styleClass="propertiesLabelTiny">
			<circabc:param value="#{NotificationStatusPanel.userDisplayName}" />
       	</h:outputFormat>
       	<h:outputText id="notification-status-panel-user-value-2" value="#{NotificationStatusPanel.userNotificationStatus}" />

		<h:outputFormat id="notification-status-panel-profile" value="#{cmsg.notification_panel_profile_status}:" styleClass="propertiesLabelTiny">
			<circabc:param value="#{NotificationStatusPanel.profile}" />
		</h:outputFormat>
    	 <h:outputText id="notification-status-panel-profile-value" value="#{NotificationStatusPanel.profileNotificationStatus}" />
	</h:panelGrid>

	<f:verbatim><br /></f:verbatim>

	<circabc:panel id="notification-status-panel-warning" styleClass="infoPanel" styleClassLabel="infoContent" >
		<h:graphicImage id="notification-status-panel-image-warning" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
		<h:outputText id="notification-status-panel-text-warning-spaces" value="&nbsp;&nbsp;" escape="false" />
		<h:outputText id="notification-status-panel-text-warning-result" value="#{NotificationStatusPanel.notificationResult}" />
	</circabc:panel>

      	<f:verbatim><br /></f:verbatim>
     </circabc:panel>

     <circabc:panel id="topOfPageAnchonotification-status-panel" styleClass="topOfPageAnchor"  >
         <%-- Display the "back to top icon first and display the text after." --%>
         <circabc:actionLink id="notification-status-panel-anchor-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
         <circabc:actionLink id="notification-status-panel-anchor-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
     </circabc:panel>

</circabc:displayer>

