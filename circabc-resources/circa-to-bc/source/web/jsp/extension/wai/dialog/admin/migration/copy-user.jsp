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
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>
<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />


	<%@ page isELIgnored="false"%>

	<c:set var="currentTitle"
		value="${cmsg.copy_user_dialog_browser_title}" />

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
			alt="#{cmsg.copy_user_dialog_icon_tooltip}"
			title="#{cmsg.copy_user_dialog_icon_tooltip}"></h:graphicImage></div>
		<div class="ContentHeaderText"><span class="ContentHeaderTitle"><h:outputText
			value="#{cmsg.copy_user_dialog_page_title}" /></span><br />
		<span class="ContentHeaderSubTitle"><h:outputText
			value="#{cmsg.copy_user_dialog_page_description}" /></span></div>
		</div>
		</div>

		<div id="ContentMain"><h:form acceptcharset="UTF-8"
			id="FormSecondary12" enctype="multipart/form-data">
			<div class="ContentMainButton">
			<div class="divButtonDialog"><h:commandButton
				id="finish-button" styleClass="dialogButton" value="#{msg.ok}"
				action="#{CopyUserBean.finish}" onclick="showWaitProgress();" /><br />
			<h:commandButton id="cancel-button" styleClass="dialogButton"
				value="#{msg.cancel}" action="finish" /></div>
			</div>
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:outputText value="delete old user" escape="false"
				style="font-style: bold;" />
			<f:verbatim>
				<br />
			</f:verbatim>
			<h:selectBooleanCheckbox value="#{CopyUserBean.deleteOldUser}" />

			<f:verbatim>
				<br />
			</f:verbatim>

			<h:outputText value="copy membership" escape="false"
				style="font-style: bold;" />
			<f:verbatim>
				<br />
			</f:verbatim>


			<h:selectBooleanCheckbox value="#{CopyUserBean.copyMembership}" />
			<f:verbatim>
				<br />
			</f:verbatim>


			<h:outputText value="take ownership" escape="false"
				style="font-style: bold;" />
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:selectBooleanCheckbox value="#{CopyUserBean.takeOwnership}" />

			<f:verbatim>
				<br />
			</f:verbatim>






			<h:outputText value="from" escape="false" style="font-style: italic;" />
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:inputText value="#{CopyUserBean.oldUserName}" />
			<f:verbatim>
				<br />
			</f:verbatim>




			<h:outputText value="to" escape="false" style="font-style: italic;" />
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:inputText value="#{CopyUserBean.newUserName}" />
			
			
			<t:inputFileUpload id="alfFileInput" size="50" value="#{CopyUserBean.submittedFile}" storage="file">
			</t:inputFileUpload>
			
			

		
		</h:form>
		
		</div>
	</circabc:displayer>

</circabc:view>
