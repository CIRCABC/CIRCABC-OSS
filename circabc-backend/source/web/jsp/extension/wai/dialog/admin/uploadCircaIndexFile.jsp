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

  	<c:set var="currentTitle" value="${cmsg.title_add_content}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
</h:form>


<circabc:displayer id="displayer-is-admin" rendered="#{NavigationBean.currentUser.admin == true}">
		<div id="maincontent">
		<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

		<%-- Content START --%>
		<div id="ContentHeader">
			<div class="ContentHeaderNavigationLibrary">
			<h:form acceptcharset="UTF-8" id="FormPrincipal2"><circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></h:form></div>
			<div>
				<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.add_content_icon_tooltip}" title="#{cmsg.add_content_icon_tooltip}"></h:graphicImage></div>
				<div class="ContentHeaderText">
					<span class="ContentHeaderTitle"><h:outputText value="Upload a Circa index files" /></span><br />
					<span class="ContentHeaderSubTitle"><h:outputText value="The index file must be 'tab' separated value and target content must exists." /></span>
				</div>
			</div>
		</div>

		<div id="ContentMain">
			<circabc:displayer id="displayer12" rendered="#{MigratePropertiesBean.hasBeenUploaded == false}">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:form acceptcharset="UTF-8" id="FormSecondary12">
							<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" disabled="true" /><br />
		                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{MigratePropertiesBean.cancel}" />
		                </h:form>
					</div>
				</div>
				<div class="ContentMainForm">
					<circabc:panel id="panelAddContentFirst" label="#{cmsg.add_content_label}" styleClass="panelAddContentGlobal" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.add_content_label_tooltip}">
						<circabc:uploadForm returnPage="/jsp/extension/wai/dialog/admin/uploadCircaIndexFile.jsp">
							<%-- Locate the content --%>
							<h:outputText id="text1" value="1. #{cmsg.add_content_locate_content}" styleClass="signup_subrub_title"/><br />
							<input type="file" size="75" name="alfFileInput"/><br />

							<%-- Upload the file --%>
							<h:outputText id="text2" value="2. #{cmsg.add_content_click_upload}" styleClass="signup_subrub_title"/><br />
							<input type="submit" id="submitFile" name="submitFile" value="${cmsg.add_content_upload}" title="${cmsg.add_content_upload_tooltip}" onclick="showWaitProgress();" />
						</circabc:uploadForm>
					</circabc:panel>
				</div>
			</circabc:displayer>
			<circabc:displayer rendered="#{MigratePropertiesBean.hasBeenUploaded}" id="displayer21">
				<h:form acceptcharset="UTF-8" id="FormSecondary21">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" action="#{MigratePropertiesBean.finish}" /><br />
		                <h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{MigratePropertiesBean.cancel}" />
					</div>
				</div>
				<div class="ContentMainForm">
					<circabc:panel id="panelAddContentSecond" label="#{cmsg.add_content_file_uploaded}" styleClass="panelAddContentYellow" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.add_content_file_uploaded_tooltip}" rendered="#{MigratePropertiesBean.renderFileUploadMessage}">
					     <h:graphicImage value="/images/icons/info_icon.gif" alt="#{cmsg.message_info_tooltip}" />
						 <h:outputText id="text3" value="#{MigratePropertiesBean.fileUploadSuccessMsg}" />
					</circabc:panel>
					<br />
					<circabc:panel id="panelAddContentTer" label="#{cmsg.add_content_label}" styleClass="panelAddContentGlobal" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.add_content_label_tooltip}">
						<br />
						<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
						<h:outputText id="text4" value="#{msg.name}:" />
						<h:inputText id="file-name" value="#{MigratePropertiesBean.fileName}" maxlength="1024" size="35" />
						<br /><br />
						<h:outputText id="textigPath" value="2. InterestGroup: "/><br />
						<h:inputText id="input-ig-path" value="#{MigratePropertiesBean.interestGroupPath}" />
						<br /><br />
					</circabc:panel>
				</div>
				</h:form>
			</circabc:displayer>
		</div>
	<%-- Content END --%>
	</div>
</circabc:displayer>

<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
	<h1>Only super adminstrator can access to this page!</h1>
</circabc:displayer>



<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
