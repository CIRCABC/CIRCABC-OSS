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

	<c:set var="currentTitle" value="<%=WaiApplication.getNavigationManager().getBrowserTitle() %>" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
	<%@ include file="/jsp/extension/wai/parts/banner.jsp" %>
	<circabc:displayer id="container-displayer-init" rendered="#{WaiNavigationManager.bean.init}" />


			<h:form acceptcharset="UTF-8" id="FormPrincipal" styleClass="FormPrincipal" >

				<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>

				<circabc:panel id="maincontent" styleClass="maincontent" >
					<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

					<%-- The title bar --%>
					<circabc:panel id="contentHeader" styleClass="contentHeader">
						<circabc:panel id="contentHeaderUrlsPanel" styleClass="ContentHeaderUrls" >
							<circabc:actionLink id="url-http-link" value="Http" target="new" href="#{WaiNavigationManager.httpUrl}" tooltip="#{cmsg.browse_http_url_tooltip}" rendered="#{WaiNavigationManager.httpUrl != null}" onclick="document.location = document.getElementById('url-http-link-action').getAttribute('href');return false;" />
							<f:verbatim>&nbsp;|&nbsp;</f:verbatim>
							<circabc:actionLink id="url-http-link-action" value="Links | " tooltip=""
										action="wai:dialog:nodeLinksWai"
										actionListener="#{WaiDialogManager.setupParameters}" >
								<circabc:param name="id" value="#{NavigationBean.currentNode.id}"></circabc:param>
							 </circabc:actionLink>
							<circabc:actionLink id="url-http-previous-link"  value="#{cmsg.browse_previous_http_url}" target="" href="#{WaiNavigationManager.previousHttpUrl}" tooltip="#{cmsg.browse_previous_http_url_tooltip}" rendered="#{WaiNavigationManager.previousHttpUrl != null}"  />
							<h:outputText value="<br />" escape="false"></h:outputText>
							<circabc:displayer rendered="#{InterestGroupLogoBean.navigationDisplay}">
								<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="navigation-ig-logo" />
							</circabc:displayer>
						</circabc:panel>
						<circabc:panel id="contentHeaderNavigationLibrary" styleClass="contentHeaderNavigationLibrary" rendered="#{WaiNavigationManager.navigationVisible}">
								<circabc:navigationList id="navigationLibrary" renderPropertyName="#{WaiNavigationManager.renderPropertyNameFromBean}" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" onclick="showWaitProgress();" />
						</circabc:panel>
						<circabc:panel id="contentHeaderSubPanel">
							<circabc:panel id="contentHeaderIconPanel" styleClass="contentHeaderIcon" rendered="#{WaiNavigationManager.iconVisible}">
								<h:graphicImage id="contentHeaderIcon" url="#{WaiNavigationManager.icon}" alt="#{WaiNavigationManager.iconAlt}" title="#{WaiNavigationManager.iconAlt}"/>
							</circabc:panel>
							<circabc:panel id="contentHeaderText">
								<h:outputText id="container-title" value="#{WaiNavigationManager.title}" styleClass="contentHeaderTitle"  escape="false"/>
								<f:verbatim><br /></f:verbatim>
								<h:outputText id="container-desc" value="#{WaiNavigationManager.description}" styleClass="contentHeaderSubTitle" escape="false"/>
							</circabc:panel>

						</circabc:panel>
					</circabc:panel>

					<%-- include the main content --%>
					<jsp:include flush="true" page="<%=WaiApplication.getNavigationManager().getPage() %>" />

				</circabc:panel>
			</h:form>
	<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>

<script type="text/javascript">

   window.onload = pageLoaded;

   function pageLoaded()
   {
      document.getElementById("url-http-link-action").innerHTML = "";
   }
</script>
