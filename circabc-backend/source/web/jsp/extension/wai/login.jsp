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
<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

	<%@ page import="org.alfresco.web.app.servlet.AuthenticationHelper"%>
	<%@ page import="javax.servlet.http.Cookie"%>

	<%@ page isELIgnored="false"%>

 	<c:set var="currentTitle" value="${cmsg.title_login}" />
 	<c:set var="xuaCompatible" value="IE=9" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">

<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>

<div id="maincontent">
<%-- Content START --%>
<div id="ContentHeader">
	<span class="ContentHeaderTitle"><h:outputText value="#{LoginBean.loginTitleAdapted}" /></span><br />
	<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.login_title_desc}" /></span>
</div>

<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

<div id="ContentMain">
		
		<div style="padding:50px 25px 15px 25px; width:50%; margin-left:auto; margin-right:auto; border-bottom:1px solid #CCC;">
		
			<!-- IF ENT - ECAS login form -->
			<circabc:displayer rendered="#{NavigationBean.enterpriseEnabled}">
					<h:outputText value="#{LoginBean.loginTextAdapted}" escape="false"/>
					
					<circabc:displayer rendered="#{LoginBean.isEcha}">
						<br/>
						<h:outputText value="#{cmsg.login_page_text_echa_complement}" escape="false"/>
					</circabc:displayer>
					<br/>
					<img src="${currentContextPath}/images/extension/transparent.gif" alt="" class="logon3" />
					<div style="text-align:center">
						<h:commandButton id="ecas" action="ecaspage" value="#{cmsg.ecas}" styleClass="logon2" />
					</div>
					<br/>
			</circabc:displayer>
			
			<!-- IF OSS - standard login form -->
			<circabc:displayer rendered="#{NavigationBean.enterpriseEnabled == false}">
				<p><h:outputText value="#{cmsg.login_text_0_oss}" /></p>
			
				<div class="LoginFormSub">
						<label for="FormPrincipal:user-name" class="textLogin"><h:outputText value="#{cmsg.username}" /> :</label>
						<h:inputText id="user-name" value="#{LoginBean.username}"	validator="#{LoginBean.validateUsername}" styleClass="logon" />
						<br />
						<label for="FormPrincipal:user-password" class="textLogin"><h:outputText value="#{cmsg.password}" /> :</label>
						<h:inputSecret id="user-password" value="#{LoginBean.password}"	validator="#{LoginBean.validatePassword}" styleClass="logon" />
						<br />
				</div>
				<h:commandButton id="submit" action="#{LoginBean.loginCirca}" value="#{cmsg.login}" styleClass="logon2" /><img src="${currentContextPath}/images/extension/transparent.gif" alt="" class="logon3" />
			</circabc:displayer>
			
		</div>
		
		<div style="width:50%; margin-left:auto; margin-right:auto; color:#CCC; text-align:center">
		<circabc:displayer rendered="#{NavigationBean.enterpriseEnabled && !LoginBean.isEcha}">
			<br />
			<circabc:actionLink value="#{cmsg.welcome_page_create_account}"
				tooltip="#{cmsg.welcome_page_create_account}"
				href="https://webgate.ec.europa.eu/cas/eim/external/register.cgi" target="_blank" styleClass="greyLink">
			</circabc:actionLink>
			<f:verbatim>&nbsp;|&nbsp;
			</f:verbatim>
			<circabc:actionLink value="#{cmsg.welcome_page_reset_password}"
				tooltip="#{cmsg.welcome_page_reset_password}"
				href="https://webgate.ec.europa.eu/cas/init/passwordResetRequest.cgi" target="_blank" styleClass="greyLink">
			</circabc:actionLink>
		</circabc:displayer>
		
		<circabc:displayer rendered="#{NavigationBean.enterpriseEnabled == false}">
			<br />
			<p>
				<h:outputText value="#{cmsg.login_text_11}"  />
				<circabc:actionLink tooltip="#{cmsg.login_text_12_tooltip}" value="#{cmsg.login_text_12}" action="wai:dialog:resendOwnPasswordWai" immediate="true" >
					<circabc:param name="new" value="true" />
				</circabc:actionLink>
				<h:outputText value="#{cmsg.login_text_13}"/>
			</p>
			<p>
				<h:outputText value="#{cmsg.login_text_21}" escape="false" />
				<circabc:actionLink value="#{cmsg.self_sign_up}" action="wai:dialog:selfRegisterWai" actionListener="#{DialogManager.setupParameters}" tooltip="#{cmsg.self_sign_up_tooltip}" immediate="true">
					<circabc:param name="new" value="true" />
				</circabc:actionLink>
				<h:outputText value="#{cmsg.login_text_23}" />
			</p>
		</circabc:displayer>
		</div>

</div>
<%-- Content END --%>
</div>
</h:form>
<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
