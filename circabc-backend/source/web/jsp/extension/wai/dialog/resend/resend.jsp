<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or – as soon they
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
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>
<%@ taglib uri="/WEB-INF/myfaces_sandbox.tld" prefix="s"%>


<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainForm" styleClass="contentFullPage">

	<h:outputFormat value="<p>" escape="false" />
		<h:outputText value="#{cmsg.resend_page_introduction_text}" escape="false" />
	<h:outputFormat value="</p>" escape="false" />

	<!--  Identity info rubrique -->

	<circabc:panel id="panelFields" styleClass="applicationFormCenter">
		<h:panelGrid columns="4" cellpadding="3" cellspacing="3" border="0" >
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.userNameValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.userNameValidationError != null}"/>
			<h:outputText value="#{msg.username}:"/>
			<h:inputText id="user-name" value="#{WaiDialogManager.bean.userName}"  maxlength="1024" size="35"  />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.userNameValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.userNameValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.userNameValidationError != null}"/>
		</h:panelGrid>
	</circabc:panel>

	<!--  Captcha rubrique -->
	<circabc:panel id="signup-rub-verify" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.self_registration_page_verify}" escape="false" />
	</circabc:panel>

	<h:outputText value="#{cmsg.self_registration_page_captcha_description_first_part}<br /><br />" escape="false" />

	<circabc:panel id="panelImageCaptcha" styleClass="applicationFormCenter">
		<t:captcha captchaSessionKeyName="#{WaiDialogManager.bean.sessionKeyName}" />
	</circabc:panel>

	<h:outputText value="#{cmsg.self_registration_page_captcha_description_second_part}<br />" escape="false" />

	<circabc:panel id="panelRegenerateCaptcha" styleClass="applicationFormCenter">
		<h:commandButton id="refresh" action="#{WaiDialogManager.bean.regenerateCaptchas}" value="#{cmsg.self_refresh_captcha}" />
	</circabc:panel>

	<h:outputText value="#{cmsg.self_registration_page_captcha_please_enter}"  />
	<h:outputText value="<br /><br />" escape="false" />

	<circabc:panel id="panelValidatorError" styleClass="applicationFormCenter">
		<h:outputText value="#{WaiDialogManager.bean.captchaValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.captchaValidationError != null}"/>
		<h:outputText value="<br />" escape="false" />
		<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" /><h:outputText value="&nbsp;#{cmsg.self_registration_page_captcha_code}&nbsp;" escape="false" /><h:inputText id="captcha-value" value="#{WaiDialogManager.bean.captchaResponse}"  size="8" />
		<h:outputText value="<br /><br />" escape="false" />
		<h:outputText value="#{cmsg.self_registration_page_captcha_problem}"  />
	</circabc:panel>

	<!--  Submit -->
	<circabc:panel id="signup-submit" styleClass="submitCenter">
		<h:commandButton  id="finish-button" action="#{WaiDialogManager.finish}" value="#{cmsg.resend_page_submit}" />
	</circabc:panel>
</circabc:panel>
