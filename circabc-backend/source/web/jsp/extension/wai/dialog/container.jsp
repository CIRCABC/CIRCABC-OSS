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
<%@ page import="eu.cec.digit.circabc.web.wai.app.WaiApplication" %>
<%@ page import="eu.cec.digit.circabc.web.WebClientHelper" %>

<%
	// synchronize the JSF language and Alfresco language to the browser language
	WebClientHelper.synchronizeLanguages(request.getLocale());
%>

<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

  	<c:set var="currentTitle" value="<%=WaiApplication.getDialogManager().getBrowserTitle() %>" />
  	<c:set var="xuaCompatible" value="<%=WaiApplication.getDialogManager().getIECompatibilityMode() %>" />
	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
	<%@ include file="/jsp/extension/wai/parts/banner.jsp" %>


			<h:form acceptcharset="UTF-8" id="FormPrincipal" styleClass="FormPrincipal" >

				<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>

				<circabc:panel id="maincontent" styleClass="maincontent" >
					<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

					<%-- The title bar --%>
					<circabc:panel id="contentHeader" styleClass="contentHeader">
						<circabc:panel id="contentHeaderUrlsPanel" styleClass="ContentHeaderUrls" >
							<f:verbatim><br /></f:verbatim>
							<circabc:displayer id="display-ig-log" rendered="#{InterestGroupLogoBean.dialogDisplay}">
								<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="dialog-ig-logo" />
							</circabc:displayer>
						</circabc:panel>
						<circabc:panel id="contentHeaderNavigationLibrary" styleClass="contentHeaderNavigationLibrary" rendered="#{WaiDialogManager.navigationVisible}">
								<circabc:navigationList id="navigationLibrary" renderPropertyName="#{WaiNavigationManager.renderPropertyNameFromBean}" value="#{WaiDialogManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" />
						</circabc:panel>
						<circabc:panel id="contentHeaderSubPanel">
							<circabc:panel id="contentHeaderIcon" styleClass="contentHeaderIcon" rendered="#{WaiDialogManager.iconVisible}">
								<h:graphicImage url="#{WaiDialogManager.icon}" alt="#{WaiDialogManager.iconAlt}" title="#{WaiDialogManager.iconAlt}"/>
							</circabc:panel>
							<circabc:panel id="contentHeaderText">
								<h:outputText value="#{WaiDialogManager.title}" styleClass="contentHeaderTitle" escape="false"/>
								<f:verbatim><br /></f:verbatim>
								<h:outputText value="#{WaiDialogManager.description}" styleClass="contentHeaderSubTitle" escape="false"/>
							</circabc:panel>
						</circabc:panel>
					</circabc:panel>

					<circabc:panel id="contentMain" styleClass="contentMain">

					    <%-- the right menu --%>
					    <circabc:panel id="contentMainButton" styleClass="contentMainButton" rendered="#{WaiDialogManager.visibleRightMenu}">
					        <%--  The finish/cancel button --%>
					        <circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
					            <h:commandButton id="finish-button" styleClass="dialogButton" value="#{WaiDialogManager.finishButtonLabel}" action="#{WaiDialogManager.finish}" disabled="#{WaiDialogManager.finishButtonDisabled}" rendered="#{WaiDialogManager.visibleOKButton}"  onclick="showWaitProgress();" />
					            <h:panelGroup rendered="#{WaiDialogManager.visibleOKAsynkButton}">
					            	<f:verbatim><br /></f:verbatim>
					            </h:panelGroup>
					            <h:commandButton id="finish-async-button" styleClass="dialogButton" title="#{cmsg.action_background}" value="#{WaiDialogManager.finishAsynchButtonLabel}" action="#{WaiDialogManager.finishAsync}" disabled="#{WaiDialogManager.finishAsyncButtonDisabled}" rendered="#{WaiDialogManager.visibleOKAsynkButton}"  onclick="showWaitProgress();" />
					            <h:panelGroup rendered="#{WaiDialogManager.visibleOKAsynkButton}">
					            	<f:verbatim><br /></f:verbatim>
					            </h:panelGroup>
					            <circabc:dialogButtons id="dialog-buttons" styleClass="dialogButton" />
					            <h:commandButton id="cancel-button" styleClass="dialogButton" value="#{WaiDialogManager.cancelButtonLabel}" action="#{WaiDialogManager.cancel}" rendered="#{WaiDialogManager.visibleCancelButton}" immediate="true" />
					        </circabc:panel>

							<h:outputText value="&nbsp;" escape="false"/>

						    <%--  The action list --%>
					        <circabc:displayer rendered="#{WaiDialogManager.visibleActions}">
							    <circabc:panel id="panelActions" label="#{cmsg.actions}" tooltip="#{cmsg.actions_tooltip}" styleClass="panelActionsGlobal" styleClassLabel="panelActionsLabel" >
							            <circabc:actions id="actions_dialog" value="#{WaiDialogManager.actionsList.actions}" context="#{WaiDialogManager.actionsList.context}" vertical="true"/>
							    </circabc:panel>
					        </circabc:displayer>

					    </circabc:panel>

						<%-- include the main content --%>
						<jsp:include flush="true" page="<%=WaiApplication.getDialogManager().getPage() %>" />

					</circabc:panel>

				</circabc:panel>
			</h:form>
	<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
