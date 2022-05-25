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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />

<circabc:panel id="invite-ig-notify-section-1" styleClass="signup_rub_title" >
	<h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</circabc:panel>

<f:verbatim><br /></f:verbatim>
<h:outputText value="&nbsp;&nbsp;#{msg.send_email}" escape="false"/>

<h:selectOneRadio id="SendNotificationRadio" value="#{WizardManager.bean.notify}"  >
	<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
	<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
</h:selectOneRadio>

<f:verbatim><br /></f:verbatim>

<circabc:panel id="invite-user-notify-section-2" styleClass="signup_rub_title" >
	<h:outputText value="&nbsp;#{msg.email_message}" escape="false" />
</circabc:panel>

<f:verbatim><br /></f:verbatim>

<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
<h:outputText value="&nbsp;#{msg.subject}&nbsp;" escape="false" />
<h:inputText id="subject" value="#{WizardManager.bean.subject}" size="85" maxlength="1024" onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();" />

<f:verbatim><br /><br /></f:verbatim>
<h:outputText value="&nbsp;&nbsp;#{msg.action_mail_template}&nbsp;" escape="false" />
<h:selectOneMenu id="SendNotification" value="#{WizardManager.bean.template}">
	<f:selectItem itemValue="none" itemLabel="#{cmsg.no_template}"/>
	<f:selectItems value="#{WizardManager.bean.emailTemplates}" />
</h:selectOneMenu>
<h:outputText value="&nbsp;&nbsp;&nbsp;" escape="false" />
<h:commandButton value="#{msg.insert_template}" actionListener="#{WizardManager.bean.insertTemplate}" styleClass="wizardButton" />
<h:commandButton value="#{msg.discard_template}" actionListener="#{WizardManager.bean.discardTemplate}" styleClass="wizardButton" disabled="#{WizardManager.bean.usingTemplate == null}" />

<f:verbatim><br /><br /></f:verbatim>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0" >

	<h:outputText value="&nbsp;&nbsp;#{msg.message}" escape="false" />
	<h:inputTextarea value="#{WizardManager.bean.body}" rows="12" cols="60" disabled="#{WizardManager.bean.usingTemplate != null}" />

	<h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;(*)" escape="false" />
	<h:outputText value="#{cmsg.invite_circabc_user_template_mail_regex_explaination}"/>
</h:panelGrid>


