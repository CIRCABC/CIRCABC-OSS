<%--+
    |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
    |
    |  		   http://ec.europa.eu/idabc/en/document/6523
    |
    +--%>
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

	<c:set var="currentTitle"
		value="${cmsg.delete_users_dialog_browser_title}" />

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
			alt="#{cmsg.delete_users_dialog_icon_tooltip}"
			title="#{cmsg.delete_users_dialog_icon_tooltip}"></h:graphicImage></div>
		<div class="ContentHeaderText"><span class="ContentHeaderTitle"><h:outputText
			value="#{cmsg.delete_users_dialog_page_title}" /></span><br />
		<span class="ContentHeaderSubTitle"><h:outputText
			value="#{cmsg.delete_users_dialog_page_description}" /></span></div>
		</div>
		</div>

		<div id="ContentMain"><h:form acceptcharset="UTF-8"
			id="FormSecondary12">
			<div class="ContentMainButton">
			<div class="divButtonDialog"><h:commandButton
				id="finish-button" styleClass="dialogButton" value="#{msg.ok}"
				action="#{DeleteUsersBean.finish}" onclick="showWaitProgress();" /><br />
			<h:commandButton id="cancel-button" styleClass="dialogButton"
				value="#{msg.cancel}" action="finish" /></div>
			</div>
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:outputText value="user name" escape="false"
				style="font-style: bold;" />
			<h:inputText value="#{DeleteUsersBean.userName}" />
			<f:verbatim>
				<br />
			</f:verbatim>
			
			
			<h:outputText value="first name" escape="false"
				style="font-style: bold;" />
			<h:inputText value="#{DeleteUsersBean.firstName}" />
			<f:verbatim>
				<br />
			</f:verbatim>
			
			
			<h:outputText value="last name" escape="false"
				style="font-style: bold;" />
			<h:inputText value="#{DeleteUsersBean.lastName}" />
			<f:verbatim>
				<br />
			</f:verbatim>
			
			
			<h:outputText value="email" escape="false"
				style="font-style: bold;" />
			<h:inputText value="#{DeleteUsersBean.email}" />
			<f:verbatim>
				<br />
			</f:verbatim>
						
			<h:outputText value="ECAS user name" escape="false"
				style="font-style: bold;" />
			<h:inputText value="#{DeleteUsersBean.ecasUserName}" />
			<f:verbatim>
				<br />
			</f:verbatim>
			<h:commandButton
				id="search-button" styleClass="dialogButton" value="Search"
				action="#{DeleteUsersBean.search}" onclick="showWaitProgress();" /><br />
		</div>
		<div>
			<f:verbatim>
				<br />
			</f:verbatim>
			<h:outputText value="users to be deleted" escape="false" style="font-style: italic;" />
			<f:verbatim>
				<br />
			</f:verbatim>
			 <h:selectManyListbox size="10" id="users" value="#{DeleteUsersBean.selectedItems}">
			 	<f:selectItems value="#{DeleteUsersBean.selectItems}" />
             </h:selectManyListbox>
		</div>
		</h:form>
	</circabc:displayer>

</circabc:view>
