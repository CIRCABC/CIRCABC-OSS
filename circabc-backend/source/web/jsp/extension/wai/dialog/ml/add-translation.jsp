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

  	<c:set var="currentTitle" value="${cmsg.add_translation_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
</h:form>

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
			<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.add_translation_icon_tooltip}" title="#{cmsg.add_translation_icon_tooltip}"></h:graphicImage></div>
			<div class="ContentHeaderText">
				<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.add_translation_title}" /></span><br />
				<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.add_translation_title_desc}" /></span>
			</div>
		</div>
	</div>
	<div id="ContentMain">
		<div class="ContentMainButton">
			<div class="divButtonDialog">
				<h:form acceptcharset="UTF-8" id="FormSecondary12">
					<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" action="#{CircabcAddTranslationBean.finish}"  disabled="#{CircabcAddTranslationBean.uploadedFileCount == 0}" onclick="showWaitProgress();"/><br />
                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{CircabcAddTranslationBean.cancel}" />
                </h:form>
			</div>
		</div>
		<div class="ContentMainForm">

			<circabc:panel id="upload-main-section" styleClass="signup_rub_title">
				<h:outputText value="#{cmsg.add_content_label}"  />
			</circabc:panel>

			<circabc:uploadForm returnPage="/jsp/extension/wai/dialog/ml/add-translation.jsp" submitCallback="CircabcAddTranslationBean.addFile">

				<%-- Locate the content --%>
				<h:outputText id="text1" value="1. #{cmsg.add_content_locate_content}" styleClass="signup_subrub_title"/><br />
				<input type="file" size="75" name="alfFileInput"/><br />

				<%-- Set language --%>
				<h:outputText id="text2" value="2. #{cmsg.add_translation_select_language}" styleClass="signup_subrub_title"/><br />
				<h:selectOneMenu id="content-language" value="#{CircabcAddTranslationBean.language}" >
					<f:selectItems id="valelrr" value="#{CircabcAddTranslationBean.unusedLanguages}" />
				</h:selectOneMenu>
				<br />

				<%-- Set options file --%>
				<h:outputText id="text3" value="3. #{cmsg.library_add_content_other_options}" styleClass="signup_subrub_title"/><br />
				<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
					<h:selectBooleanCheckbox id="check-disable-notif" value="#{CircabcAddTranslationBean.disableNotification}" title="#{cmsg.notification_disable_mechanism_tooltip}" />
					<h:outputText id="text-notif-disabled" value="#{cmsg.notification_disable_mechanism}"/>
				</h:panelGrid>


				<%-- Upload the file --%>
				<h:outputText id="text4" value="4. #{cmsg.add_content_click_upload}" styleClass="signup_subrub_title"/><br />
				<input type="submit" id="submitFile" name="submitFile" value="${cmsg.import_upload}" title="${cmsg.add_content_upload_tooltip}" onclick="showWaitProgress();"  />

			</circabc:uploadForm>

			<h:form acceptcharset="UTF-8" id="FormRemoveFile">

				<f:verbatim><br /></f:verbatim>

				<circabc:panel id="upload-uploadedfiles-section" styleClass="signup_rub_title" tooltip="#{cmsg.library_add_content_uploaded_files}">
					<h:outputText value="#{cmsg.library_add_content_uploaded_files}"  />
				</circabc:panel>

				<f:verbatim><br /></f:verbatim>

				<h:dataTable id="all-uploaded-files" value="#{CircabcAddTranslationBean.uploadedFilesDataModel}" var="row"
					rowClasses="recordSetRow,recordSetRowAlt"
					styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
					cellspacing="0" cellpadding="4"
					rendered="#{CircabcAddTranslationBean.uploadedFileCount > 0}">

					<h:column id="column-name">
						<f:facet name="header">
							<h:outputText value="#{cmsg.name}" />
						</f:facet>
						<h:outputText value="#{row.fileName}" title="#{row.fileName}"/>
					</h:column>
					<h:column id="column-notify">
						<f:facet name="header">
							<h:outputText value="#{cmsg.library_add_content_col_notif}" />
						</f:facet>
						<h:outputText value="#{row.notificationDisabled}"/>
					</h:column>
					<h:column id="column-language">
						<f:facet name="header">
							<h:outputText value="#{cmsg.language}" />
						</f:facet>
						<h:outputText value="#{row.language}" />
					</h:column>

					<h:column id="actions">
						<f:facet name="header">
							<h:outputText value="#{cmsg.actions}" />
						</f:facet>
						<circabc:actionLink id="remove-uploaded" actionListener="#{CircabcAddTranslationBean.removeSelection}"
							image="/images/icons/delete.gif" value="#{cmsg.remove_iglogo_action_tooltip}"
							tooltip="#{cmsg.remove_iglogo_action_tooltip}" showLink="false"  />
					</h:column>
				</h:dataTable>

				<h:outputText id="no-files" value="#{cmsg.library_add_content_no_uploaded_files}"  rendered="#{CircabcAddTranslationBean.uploadedFileCount == 0}" styleClass="noItem" />
		</h:form>




		</div>
	</div>
<%-- Content END --%>
</div>
<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
