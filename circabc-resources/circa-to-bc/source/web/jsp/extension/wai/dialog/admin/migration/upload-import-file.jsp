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

  	<c:set var="currentTitle" value="${cmsg.upload_exportation_file_dialog_browser_title}" />

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
		<circabc:panel id="contentHeaderUrlsPanel" styleClass="ContentHeaderUrls" >
			<f:verbatim><br /></f:verbatim>
			<circabc:displayer rendered="#{InterestGroupLogoBean.dialogDisplay}">
				<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="addcontent-ig-logo" />
			</circabc:displayer>
		</circabc:panel>
		<div class="ContentHeaderNavigationLibrary">
		<h:form acceptcharset="UTF-8" id="FormPrincipal2"><circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></h:form></div>
		<div>
			<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.upload_exportation_file_dialog_icon_tooltip}" title="#{cmsg.upload_exportation_file_dialog_icon_tooltip}"></h:graphicImage></div>
			<div class="ContentHeaderText">
				<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.upload_exportation_file_dialog_page_title}" /></span><br />
				<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.upload_exportation_file_dialog_page_description}" /></span>
			</div>
		</div>
	</div>
	<div id="ContentMain">

		<circabc:displayer id="displayer-isupload-step" rendered="#{UploadImportationFileBean.uploadedFileCount < 2}">
			<div class="ContentMainButton">
				<div class="divButtonDialog">
					<h:form acceptcharset="UTF-8" id="FormSecondary12">
						<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" action="#{UploadImportationFileBean.finish}"  disabled="#{UploadImportationFileBean.uploadedFileCount < 2}" onclick="showWaitProgress();"/><br />
	                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{UploadImportationFileBean.cancel}" />
    	            </h:form>
				</div>
			</div>
			<div class="ContentMainForm">

				<circabc:panel id="upload-main-section" styleClass="signup_rub_title">
					<h:outputText value="#{UploadImportationFileBean.uploadPanelMessage}" escape="false" />
				</circabc:panel>

				<circabc:uploadForm returnPage="/jsp/extension/wai/dialog/admin/migration/upload-import-file.jsp" submitCallback="UploadImportationFileBean.addFile">
					<%-- Locate the content --%>
					<h:outputText id="text1" value="1. #{cmsg.add_content_locate_content}" styleClass="signup_subrub_title"/><br />
					<input type="file" size="75" name="alfFileInput"/><br />

					<%-- Upload the file --%>
					<h:outputText id="text3" value="2. #{cmsg.add_content_click_upload}" styleClass="signup_subrub_title"/><br />
					<input type="submit" id="submitFile" name="submitFile" value="${cmsg.add_content_upload}" title="${cmsg.add_content_upload_tooltip}" onclick="showWaitProgress();"  />

				</circabc:uploadForm>
			</div>
		</circabc:displayer>

		<circabc:displayer id="displayer-iterationdetails-step" rendered="#{UploadImportationFileBean.uploadedFileCount == 2}">
			<h:form acceptcharset="UTF-8" id="FormSecondary122">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" action="#{UploadImportationFileBean.finish}"  disabled="#{UploadImportationFileBean.uploadedFileCount < 2}" onclick="showWaitProgress();"/><br />
		                <h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{UploadImportationFileBean.cancel}" />
					</div>
				</div>
				<div class="ContentMainForm">
					<circabc:panel id="upload-second-section" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.upload_exportation_file_dialog_iteration_details}" escape="false" />
					</circabc:panel>

					<h:panelGrid id="grid-iterationdetails" columns="2" cellpadding="3" cellspacing="3" border="0">
						<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
						<h:outputText id="text21" value="#{cmsg.upload_exportation_file_dialog_identifier}" styleClass="signup_subrub_title"/>

						<h:outputText id="text22" value="&nbsp;" escape="false" styleClass="signup_subrub_title"/>
						<h:inputText id="file-name" value="#{UploadImportationFileBean.label}" maxlength="1024" size="35" />

						<h:outputText id="text23" value="&nbsp;" escape="false" styleClass="signup_subrub_title"/>
						<h:outputText id="text24" value="#{cmsg.upload_exportation_file_dialog_description}" styleClass="signup_subrub_title"/>

						<h:outputText id="text25" value="&nbsp;" escape="false" styleClass="signup_subrub_title"/>
						<h:inputTextarea id="description" value="#{UploadImportationFileBean.description}" rows="5" cols="70"/>
					</h:panelGrid>
				</div>
			</h:form>
		</circabc:displayer>
		
		<div class="ContentMainForm">

			<h:form acceptcharset="UTF-8" id="FormRemoveFile">
				<f:verbatim><br /></f:verbatim>
	
				<circabc:panel id="upload-uploadedfiles-section" styleClass="signup_rub_title" tooltip="#{cmsg.library_add_content_uploaded_files}">
					<h:outputText value="#{cmsg.library_add_content_uploaded_files}" escape="false" />
				</circabc:panel>
	
				<f:verbatim><br /></f:verbatim>
	
				<h:dataTable id="all-uploaded-files" value="#{UploadImportationFileBean.uploadedFilesDataModel}" var="row"
					rowClasses="recordSetRow,recordSetRowAlt"
					styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
					cellspacing="0" cellpadding="4"
					rendered="#{UploadImportationFileBean.uploadedFileCount > 0}">
	
					<h:column id="column-name">
						<f:facet name="header">
							<h:outputText value="#{cmsg.name}" />
						</f:facet>
						<h:outputText value="#{row.fileName}" title="#{row.fileName}"/>
					</h:column>
					<h:column id="column-uploadtarget">
						<f:facet name="header">
							<h:outputText value="#{cmsg.upload_exportation_file_dialog_target}" />
						</f:facet>
						<h:outputText value="#{row.language}" />
					</h:column>
	
					<h:column id="actions">
						<f:facet name="header">
							<h:outputText value="#{cmsg.actions}" />
						</f:facet>
						<circabc:actionLink id="remove-uploaded" actionListener="#{UploadImportationFileBean.removeSelection}"
							image="/images/icons/delete.gif" value="#{cmsg.remove_iglogo_action_tooltip}"
							tooltip="#{cmsg.remove_iglogo_action_tooltip}" showLink="false"  />
					</h:column>
				</h:dataTable>
	
				<h:outputText id="no-files" value="#{cmsg.library_add_content_no_uploaded_files}" escape="false" rendered="#{UploadImportationFileBean.uploadedFileCount == 0}" styleClass="noItem" />
			</h:form>
		</div>


		</div>
	</div>
<%-- Content END --%>
</div>
</circabc:displayer>

<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
	<h1>Only super adminstrator can access to this page!</h1>
</circabc:displayer>

<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
