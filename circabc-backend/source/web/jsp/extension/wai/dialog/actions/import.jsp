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

  	<c:set var="currentTitle" value="${cmsg.import_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
</h:form>

<div id="maincontent">
	<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />
	<%-- Content START --%>
	<div id="ContentHeader">
		<circabc:panel id="contentHeaderUrlsPanel" styleClass="ContentHeaderUrls"  rendered="#{ImportDialogWai.hasPermission}" >
			<f:verbatim><br /></f:verbatim>
			<circabc:displayer rendered="#{InterestGroupLogoBean.dialogDisplay}">
				<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="addcontent-ig-logo" />
			</circabc:displayer>
		</circabc:panel>
		<div class="ContentHeaderNavigationLibrary">
		<h:form acceptcharset="UTF-8" id="FormPrincipal2"><circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></h:form></div>
		<div>
			<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/import_large.gif" alt="#{cmsg.import_icon_tooltip}" title="#{cmsg.import_icon_tooltip}"></h:graphicImage></div>
			<div class="ContentHeaderText">
				<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.import_title}" /></span><br />
				<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.import_title_desc}" /></span>
			</div>
		</div>
	</div>
	<div id="ContentMain">
		<div class="ContentMainForm">
			<br/>
			<circabc:panel id="upload-main-section" >
				<h:outputText value="#{cmsg.import_label}" escape="false" />
			</circabc:panel>

			<circabc:uploadForm returnPage="/jsp/extension/wai/dialog/actions/import.jsp" submitCallback="ImportDialogWai.addFile">
				<%-- Locate the content --%>
				<h:outputText id="text1" value="1. #{cmsg.import_locate_content}" styleClass="signup_subrub_title"/><br />
				<h:outputText id="text-max-file-size" value="#{ImportDialogWai.maxFileSizeMessage}" /><br />
				<input type="file" size="75" name="alfFileInput"/><br />
				<%-- Upload the file --%>
				<h:outputText id="text2" value="2. #{cmsg.import_click_upload}" styleClass="signup_subrub_title"/><br />
				<input type="submit" id="submitFile" name="submitFile" value="${cmsg.import_upload}" title="${cmsg.import_upload_tooltip}" onclick="showWaitProgress();" <h:outputText value="#{ImportDialogWai.disabledInput}"/>/><br />
			</circabc:uploadForm>
			
				<h:form acceptcharset="UTF-8" rendered="#{ImportDialogWai.uploadedFileCount == 1}" id="FormSecondary12">
					<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
						<%-- Notify user about import --%>
						<h:selectBooleanCheckbox id="notify-user" immediate="true" value="#{ImportDialogWai.notifyUser}" onchange="this.form.submit();" valueChangeListener="#{ImportDialogWai.notifyUserChanged}" />
						<h:outputText id="notify-user-text" value="#{cmsg.import_notify_user}" styleClass="signup_subrub_title"/>
						<%-- Delete file after import --%>
						<h:selectBooleanCheckbox id="delete-file-after-import" immediate="true" value="#{ImportDialogWai.deleteFileAfterImport}" onchange="this.form.submit();"   valueChangeListener="#{ImportDialogWai.deleteFileAfterImportChanged}" />
						<h:outputText id="delete-file-after-import-text" value="#{cmsg.import_delete_file}" styleClass="signup_subrub_title"/><br />
						<h:selectBooleanCheckbox id="disable-file-notification" immediate="true" value="#{ImportDialogWai.disableFileNotification}" onchange="this.form.submit();" valueChangeListener="#{ImportDialogWai.disableFileNotificationChanged}" />
						<h:outputText id="notification_disabled_for_upload" value="#{cmsg.notification_disabled_for_upload}" styleClass="signup_subrub_title"/><br />
					</h:panelGrid>
					<br/>
					<h:outputText value="#{cmsg.encoding}:"></h:outputText>
					<h:selectOneMenu id="encoding" value="#{ImportDialogWai.selectedEncoding}">
						<f:selectItem itemValue="CP437" itemLabel="CP437"/>
						<f:selectItem itemValue="UTF-8" itemLabel="UTF-8"/>
					</h:selectOneMenu>
					
					<br/><br/>
					
					<h:commandButton id="finish-button" styleClass="dialogButton" value="#{cmsg.import_title}" action="#{ImportDialogWai.finish}"  disabled="#{ImportDialogWai.uploadedFileCount != 1}" onclick="showWaitProgress();"/>
                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{ImportDialogWai.cancel}" />
                </h:form>	
                
                <h:form acceptcharset="UTF-8"  id="FormSecondary22">
                	<br/>
                <hr/>
                <br/>
                
              
				<p>
					<h:graphicImage id="infoImg" value="/images/extension/info.png" title="info" style="width:48px; margin-right:10px; float:left;"></h:graphicImage>
					<br/>
					<h:outputText id="infoDescr" value="#{cmsg.import_help_index_description }"></h:outputText>
					<br/>
					<h:outputText id="infoDescrBis" value="#{cmsg.import_help_index_description_bis }"></h:outputText>
					
					<h:commandLink id="downloadLink" style="font-weight:bold;" value="#{cmsg.import_help_index_download_link }" title="download" action="wai:dialog:ImportDialogWai" actionListener="#{ImportDialogWai.generateEmptyTemplate}"></h:commandLink>
					<h:graphicImage id="downImg" value="/images/icons/download_doc.gif" title="download" ></h:graphicImage>
					
				</p>
                </h:form>
                
                


		</div>
	</div>
<%-- Content END --%>
</div>
<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>