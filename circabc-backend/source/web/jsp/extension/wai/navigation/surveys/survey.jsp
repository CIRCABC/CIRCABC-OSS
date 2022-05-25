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


	<%@ page isELIgnored="false"%>

  	<c:set var="currentTitle" value="${cmsg.title_surveys}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
</h:form>

<div id="maincontent" styleClass="contentFullPage">
	<%-- Content START --%>
	<div id="ContentHeader">
		<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/space-icon-pen.gif" alt="#{navigationBean.currentNode.name}" title="#{navigationBean.currentNode.name}"></h:graphicImage></div>
		<div class="ContentHeaderText">
			<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.survey_encode_title}" /></span><br />
			<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.survey_encode_title_desc}" /></span>
		</div>
	</div>
	<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />
	<div id="ContentMain">
		<div class="ContentMainButton">
			<div class="divButtonDialog">
				<h:form acceptcharset="UTF-8" id="FormSecondary">
                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{cmsg.close}" action="browse-wai" actionListener="#{SurveysBean.closeSurvey}" />
                </h:form>
			</div>
		</div>
		<div class="ContentMainForm">
			<div class="divSurveyEncode">
				<!-- BEGIN Survey -->
				<circabc:encode value="#{SurveysBean.url}" survey="#{SurveysBean.survey}" lang="#{SurveysBean.lang}"/>
				<!-- END Survey -->
				<!-- Ensure use of our css -->
				<link rel="stylesheet" href="${currentContextPath}/css/extension/d-commission.css" type="text/css" />
				<link rel="stylesheet" href="${currentContextPath}/css/extension/circabc.css" type="text/css" />
				<link rel="stylesheet" href="${currentContextPath}/css/extension/circabc-counter-ipm.css" type="text/css" />
			</div>
		</div>
	</div>
<%-- Content END --%>
</div>

<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
