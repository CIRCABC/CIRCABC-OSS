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
			<h:outputText value="#{cmsg.self_registration_page_introduction_text}" escape="false" />
			<h:outputFormat value="<br /><br />" escape="false" />
			<h:outputText value="#{cmsg.self_registration_page_login_text_firstpart}&nbsp;" escape="false" />
			<circabc:actionLink value="#{cmsg.self_login}" action="logout" tooltip="#{cmsg.self_login_tooltip}" immediate="true" styleClass="signup_login_link"/>
			<h:outputText value="#{cmsg.self_registration_page_login_text_secondpart}"  />
			<h:outputFormat value="<br />" escape="false" />
		<h:outputFormat value="</p>" escape="false" />
		<%-- Content START --%>

		<!--  Identity info rubrique -->
		<circabc:panel id="signup-rub-identity" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.self_registration_page_identity}"  />
		</circabc:panel>

		<h:panelGrid columns="4" cellpadding="3" cellspacing="3" border="0" >
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.firstNameValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.firstNameValidationError != null}"/>
			<h:outputText value="#{msg.first_name}:" styleClass="" />
			<h:inputText  id="first-name" value="#{WaiDialogManager.bean.firstName}" maxlength="55" size="35" />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.firstNameValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.firstNameValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.firstNameValidationError != null}"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.lastNameValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.lastNameValidationError != null}"/>
			<h:outputText value="#{msg.last_name}:" styleClass="" />
			<h:inputText id="last-name" value="#{WaiDialogManager.bean.lastName}" maxlength="1024" size="35" />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.lastNameValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.lastNameValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.lastNameValidationError != null}"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.emailValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.emailValidationError != null}"/>
			<h:outputText value="#{msg.email}:" styleClass="" />
			<h:inputText id="email" value="#{WaiDialogManager.bean.email}" maxlength="1024" size="35"  />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.emailValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.emailValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.emailValidationError != null}"/>

			<h:outputText value=""/>
			<h:outputText value=""/>
			<h:outputText value="#{cmsg.self_registration_page_username_restriction}"/>
			<h:outputText value=""/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.userNameValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.userNameValidationError != null}"/>
			<h:outputText value="#{msg.username}:"/>
			<h:inputText id="user-name" value="#{WaiDialogManager.bean.userName}"  maxlength="1024" size="35"  />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.userNameValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.userNameValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.userNameValidationError != null}"/>


			<h:outputText value=""/>
			<h:outputText value=""/>
			<h:outputText value="#{cmsg.self_registration_page_password_restriction}"/>
			<h:outputText value=""/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.passwordValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.passwordValidationError != null}"/>
			<h:outputText value="#{msg.password}:"/>
			<h:inputSecret id="password" value="#{WaiDialogManager.bean.password}" size="35" maxlength="1024"  redisplay="true" />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.passwordValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.passwordValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.passwordValidationError != null}"/>


			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.confirmValidationError == null && WaiDialogManager.bean.passwordValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.confirmValidationError != null || WaiDialogManager.bean.passwordValidationError != null}"/>
			<h:outputText value="#{msg.confirm}:"/>
			<h:inputSecret id="confirm" value="#{WaiDialogManager.bean.confirm}" size="35" maxlength="1024" redisplay="true" />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.confirmValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.confirmValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.confirmValidationError != null}"/>
		</h:panelGrid>

		<circabc:panel id="topOfPageAnchorFirst" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

		<!--  Contact info rubrique -->
		<circabc:panel id="signup-rub-contact-info" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.self_registration_page_contact_information}"  />
		</circabc:panel>

		<h:panelGrid columns="4" cellpadding="3" cellspacing="3" border="0">
			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
			<h:outputText value="#{cmsg.person_title}:"/>
			<h:inputText id="title" value="#{WaiDialogManager.bean.title}"  maxlength="1024" size="35"  />
			<h:outputText value=""/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.postalAddressValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.postalAddressValidationError != null}"/>
			<h:outputText value="#{cmsg.postal_adress}:"/>
			<h:inputTextarea id="postal-id" value="#{WaiDialogManager.bean.postalAddress}"  rows="4" cols="75" readonly="false" />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.postalAddressValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.postalAddressValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.postalAddressValidationError != null}"/>

			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null && WaiDialogManager.bean.phoneNumberValidationError == null}"/>
			<h:graphicImage value="/images/icons/deploy_failed.gif" alt="#{validation_field_error}" rendered="#{WaiDialogManager.bean.phoneNumberValidationError != null}"/>
			<h:outputText value="#{cmsg.phone}:"/>
			<h:inputText id="phone-id" value="#{WaiDialogManager.bean.phone}"  maxlength="1024" size="35"  />
			<h:outputText value="" rendered="#{WaiDialogManager.bean.phoneNumberValidationError == null}"/>
			<h:outputText value="#{WaiDialogManager.bean.phoneNumberValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.phoneNumberValidationError != null}"/>

			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
			<h:outputText value="#{msg.company_id}:"/>
			<h:inputText id="company-id" value="#{WaiDialogManager.bean.companyId}"  maxlength="1024" size="35"  />
			<h:outputText value=""/>

			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
			<h:outputText value="#{msg.description}:"/>
			<h:inputTextarea value="#{WaiDialogManager.bean.description}" rows="4" cols="75" readonly="false"/>
			<h:outputText value=""/>

			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
			<h:outputText value="#{cmsg.fax}:"/>
			<h:inputText id="fax-id" value="#{WaiDialogManager.bean.fax}" maxlength="1024" size="35"  />
			<h:outputText value=""/>

			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
			<h:outputText value="#{cmsg.url}:"/>
			<h:inputText id="url-id" value="#{WaiDialogManager.bean.url}" maxlength="1024" size="35"  />
			<h:outputText value=""/>
		</h:panelGrid>

		<circabc:panel id="topOfPageAnchorSecond" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

	    <!--  User Options rubrique -->
		<circabc:panel id="signup-rub-console-info" styleClass="signup_rub_title">
			<h:outputText value="#{msg.user_console_info}"  />
		</circabc:panel>

		<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
			<h:outputText value="#{msg.interface_language}" />:&nbsp;
			<h:selectOneMenu id="language" value="#{WaiDialogManager.bean.userInterfaceLanguage}" onchange="document.forms['user-console'].submit(); return true;">
				<f:selectItems value="#{WaiDialogManager.bean.languages}" />
            </h:selectOneMenu>

			<h:outputText value="" rendered="#{WaiDialogManager.bean.allValidationErrors == null}"/>
			<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{validation_field_succes}" rendered="#{WaiDialogManager.bean.allValidationErrors != null}"/>
            <h:outputText value="#{msg.content_language_filter}" />:&nbsp;
            <h:selectOneMenu id="content-filter-language" value="#{WaiDialogManager.bean.contentFilterLanguage}" onchange="document.forms['user-console'].submit(); return true;">
                	<f:selectItems value="#{WaiDialogManager.bean.contentFilterLanguages}" />
			</h:selectOneMenu>
		</h:panelGrid>

		<circabc:panel id="topOfPageAnchorThird" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>
		
		

		<!--  Captcha rubrique -->
		<circabc:panel id="signup-rub-verify" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.self_registration_page_verify}"  />
		</circabc:panel>
		<h:outputText value="#{cmsg.self_registration_page_captcha_description_first_part}<br /><br />" escape="false" />

		<circabc:panel id="signup-center-image-captcha" styleClass="applicationFormCenter">
			<t:captcha captchaSessionKeyName="#{WaiDialogManager.bean.sessionKeyName}" />
		</circabc:panel>

		<h:outputText value="#{cmsg.self_registration_page_captcha_description_second_part}<br/>" escape="false" />

		<circabc:panel id="signup-center-refresh-image-captcha" styleClass="applicationFormCenter">
			<h:commandButton id="refresh" action="#{WaiDialogManager.bean.regenerateCaptchas}" value="#{cmsg.self_refresh_captcha}" />
		</circabc:panel>
				
		<circabc:panel id="signup-center-validate-captcha" styleClass="applicationFormCenter">
			<h:outputText value="#{WaiDialogManager.bean.captchaValidationError}" style="color:red" rendered="#{WaiDialogManager.bean.captchaValidationError != null}"/>
			<h:outputText value="<br />" escape="false" />
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{cmsg.required_field}" /><h:outputText value="&nbsp;#{cmsg.self_registration_page_captcha_code}" escape="false" /><h:inputText id="captcha-value" value="#{WaiDialogManager.bean.captchaResponse}"  size="8" />
			<h:outputText value="<br /><br />" escape="false" />
			<h:outputText value="#{cmsg.self_registration_page_captcha_problem}"  />
		</circabc:panel>
		<circabc:panel id="topOfPageAnchorFourth" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

		<circabc:panel id="signup-privacy-stmt" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.}"  />
		</circabc:panel>

		<h:outputText value="<br />" escape="false" />
		<circabc:actionLink value="#{cmsg.self_registration_privacy_statement_link}" tooltip="#{cmsg.self_registration_privacy_statement_link_tooltip}" href="/html/CIRCABC_privacy_statement.pdf" target="_blank" />
		<h:outputText value="<br />" escape="false" />

		<circabc:panel id="topOfPageAnchorLast" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

		<!--  Submit -->
		<circabc:panel id="signup-submit" styleClass="submitCenter">
			<h:commandButton  id="finish-button" action="#{WaiDialogManager.finish}" value="#{cmsg.self_sign_up}" />
		</circabc:panel>

</circabc:panel>
