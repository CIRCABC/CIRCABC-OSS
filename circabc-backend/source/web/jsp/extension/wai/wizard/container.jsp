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

<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

  	<c:set var="currentTitle" value="<%=WaiApplication.getWizardManager().getBrowserTitle() %>" />

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
							<circabc:displayer rendered="#{InterestGroupLogoBean.wizardDisplayed}">
								<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="wizard-ig-logo" />
							</circabc:displayer>
						</circabc:panel>
						<circabc:panel id="contentHeaderNavigationLibrary" styleClass="contentHeaderNavigationLibrary" rendered="#{WaiWizardManager.navigationVisible}">
								<circabc:navigationList id="navigationLibrary" value="#{WaiWizardManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" />
						</circabc:panel>
						<circabc:panel id="contentHeaderSubPanel">
							<circabc:panel id="contentHeaderIcon" styleClass="contentHeaderIcon" rendered="#{WaiWizardManager.iconVisible}">
								<h:graphicImage url="#{WaiWizardManager.icon}" alt="#{WaiWizardManager.iconAlt}" title="#{WaiWizardManager.iconAlt}"/>
							</circabc:panel>
							<circabc:panel id="contentHeaderText">
								<h:outputText value="#{WaiWizardManager.title}" styleClass="contentHeaderTitle" escape="false"/>
								<f:verbatim><br /></f:verbatim>
								<h:outputText value="#{WaiWizardManager.description}" styleClass="contentHeaderSubTitle" escape="false"/>
								<f:verbatim><br /></f:verbatim>
							</circabc:panel>
						</circabc:panel>
					</circabc:panel>


					<circabc:panel id="contentMain" styleClass="contentMain">
					    <%-- the right menu --%>
					    <circabc:panel id="contentMainButton" styleClass="contentMainButton" >
					        <%--  The finish/cancel button --%>
					        <circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
								<h:commandButton id="wai-next-button" styleClass="wizardButton" value="#{WaiWizardManager.nextButtonLabel}" action="#{WaiWizardManager.next}" disabled="#{WaiWizardManager.nextButtonDisabled}" />
					            <br />
								<h:commandButton id="wai-back-button" styleClass="wizardButton" value="#{WaiWizardManager.backButtonLabel}" action="#{WaiWizardManager.back}" disabled="#{WaiWizardManager.backButtonDisabled}" />
					            <br />
					            <h:commandButton id="wai-finish-button" styleClass="wizardButton" value="#{WaiWizardManager.finishButtonLabel}" action="#{WaiWizardManager.finish}" disabled="#{WaiWizardManager.finishButtonDisabled}" onclick="showWaitProgress();" />
					            <br />
					            <h:commandButton id="wai-cancel-button" styleClass="wizardButton" value="#{WaiWizardManager.cancelButtonLabel}" action="#{WaiWizardManager.cancel}" immediate="true" />
					        </circabc:panel>

							<h:outputText value="&nbsp;" escape="false"/>

						    <%--  The action list --%>
					        <circabc:displayer rendered="#{WaiWizardManager.visibleActions}">
							    <circabc:panel id="panelActions" label="#{cmsg.actions}" tooltip="#{cmsg.actions_tooltip}" styleClass="panelActionsGlobal" styleClassLabel="panelActionsLabel" >
							            <circabc:actions id="actions_dialog" value="#{WaiWizardManager.actionsList.actions}" context="#{WaiWizardManager.actionsList.context}" vertical="true"/>
							    </circabc:panel>
					        </circabc:displayer>

					    </circabc:panel>

						<circabc:panel id="contentMainForm" styleClass="contentMainForm">
							<div class="wizardStepsPanel" >
								<h:outputText styleClass="mainSubTitle" value="#{msg.steps}"/><br/>
								<a:modeList itemSpacing="3" horizontal="true" iconColumnWidth="2" selectedStyleClass="mainSubTitle" value="#{WaiWizardManager.currentStepAsString}" disabled="true">
									<a:listItems value="#{WaiWizardManager.stepItems}" />
								</a:modeList>
							</div>
								<f:verbatim><br /><br /><br /><br /></f:verbatim>
							<div class="wizardIncludedPagePanel" >
								<h:outputText id="out-step-title" value="#{WaiWizardManager.stepTitle}" styleClass="mainSubTitle"  escape="false"/><br />
								<h:outputText id="out-step-desc" value="#{WaiWizardManager.stepDescription}" styleClass="mainSubText" escape="false" /><br />
								<h:outputText id="out-step-space" value="" styleClass="mainSubText" />
								<f:verbatim><br /></f:verbatim>
								<%-- include the main content --%>
								<jsp:include flush="true" page="<%=WaiApplication.getWizardManager().getPage() %>" />
								<f:verbatim><br /><br /></f:verbatim>
								<%-- <h:outputText  id="wzdmgr-text-step-description" value="#{WaiWizardManager.stepInstructions}" escape="false"/> --%>
							</div>
						</circabc:panel>
					</circabc:panel>
				</circabc:panel>
			</h:form>
	<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
