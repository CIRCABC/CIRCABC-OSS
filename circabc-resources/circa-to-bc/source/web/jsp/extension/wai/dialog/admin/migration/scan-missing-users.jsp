<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? 
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


	<%@ page isELIgnored="false"%>

	<%@ include file="/jsp/extension/wai/parts/header.jsp"%>
	<%@ include file="/jsp/extension/wai/parts/banner.jsp"%>

	<h:form acceptcharset="UTF-8" id="FormPrincipal">
		<%@ include file="/jsp/extension/wai/parts/left-menu.jsp"%>
	</h:form>
	<circabc:displayer id="displayer-is-admin"
		rendered="#{NavigationBean.currentUser.admin == true}">
		<div id="maincontent"><circabc:errors styleClass="messageGen"
			infoClass="messageInfo" warnClass="messageWarn"
			errorClass="messageError" /> <%-- Content START --%>
		<div id="ContentHeader">
		<div class="ContentHeaderNavigationLibrary"><h:form
			acceptcharset="UTF-8" id="FormPrincipal2">
			<circabc:navigationList id="navigationLibrary"
				value="#{WaiNavigationManager.navigation}"
				actionListener="#{BrowseBean.clickWai}" separatorFirst="false"
				styleClass="navigationLibrary" />
		</h:form></div>
		<div>
		<div class="ContentHeaderIcone"><h:graphicImage
			url="/images/icons/add_content_large.gif"
			title="Scann missing users"></h:graphicImage></div>
		<div class="ContentHeaderText"><span class="ContentHeaderTitle"><h:outputText
			value="Scann missing users" /></span><br />
		<span class="ContentHeaderSubTitle"><h:outputText
			value="Scann missing users and create them if possible in the Alfresco repository" /></span></div>
		</div>
		</div>

		<div id="ContentMain">
		<h:form acceptcharset="UTF-8"
			id="FormSecondary12">
			<div class="ContentMainButton">
			<div class="divButtonDialog">
			<h:commandButton
				id="finish-button" styleClass="dialogButton" value="#{msg.ok}"
				action="#{ScanMissingUserBean.finish}" onclick="showWaitProgress();" /><br />
			<h:commandButton id="cancel-button" styleClass="dialogButton"
				value="#{msg.cancel}" action="cancel" /></div>
			</div>
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:outputText value="Scanning for non-existing referrences of userid in the permission and the notification configuration for each Interest Group in CIRCABC"></h:outputText>

		</h:form>
		
		</div>
	</circabc:displayer>

</circabc:view>