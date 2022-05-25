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


<%@ page isELIgnored="false"%>

<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

  	<c:set var="currentTitle" value="${cmsg.title_checkin}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
	<%@ include file="/jsp/extension/wai/parts/banner.jsp" %>
			<h:form acceptcharset="UTF-8" id="FormPrincipal">
				<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
			</h:form>

<div id="maincontent">
	<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

	<!-- Content START -->
	<div id="ContentHeader">
		<circabc:panel id="contentHeaderUrlsPanel" styleClass="ContentHeaderUrls" rendered="#{CircabcCCCheckinFileDialog.hasPermission}" >
			<f:verbatim><br /></f:verbatim>
			<circabc:displayer rendered="#{InterestGroupLogoBean.dialogDisplay}">
				<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="checkin-ig-logo" />
			</circabc:displayer>
		</circabc:panel>
		<div class="ContentHeaderNavigationLibrary"><h:form acceptcharset="UTF-8" id="FormPrincipal2"><circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></h:form></div>
		<div>
			<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/check_in_large.gif" alt="#{cmsg.checkin_icon_tooltip}" title="#{cmsg.checkin_icon_tooltip}"></h:graphicImage></div>
			<div class="ContentHeaderText">
				<span class="ContentHeaderTitle">
					<h:outputFormat value="#{cmsg.checkin_title}">
						<circabc:param value="#{CircabcCCCheckinFileDialog.actionNode.name}" />
					</h:outputFormat>
				</span><br />
				<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.checkin_title_desc}" rendered="#{CircabcCCCheckinFileDialog.checkforLastStep}" /></span>
			</div>
		</div>
	</div>

	<div id="ContentMain">
		<circabc:displayer id="displayer1" rendered="#{CircabcCCCheckinFileDialog.checkinFirstStep}" >
			<h:form acceptcharset="UTF-8" id="FormSecondary">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:commandButton id="finish-button" styleClass="dialogButton" value="#{cmsg.ok}" action="#{CircabcCCCheckinFileDialog.finish}" /><br />
		            	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{cmsg.cancel}" action="#{CircabcCCCheckinFileDialog.cancel}" />
					</div>
				</div>
				<div class="ContentMainForm">

					<circabc:panel id="checkin-options-panel" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.checkin_main_label}" />
					</circabc:panel>
					<f:verbatim><br /></f:verbatim>

					<h:panelGrid id="checkin-options-values-panel" columns="2" cellpadding="2" cellspacing="2" border="0" columnClasses="panelGridLabelColumn,panelGridValueColumn">
						<h:outputText value="#{cmsg.checkin_main_version_notes}:" styleClass="propertiesLabel"/>
						<h:inputTextarea id="version-note" value="#{CircabcCCCheckinFileDialog.versionNote}" rows="2" cols="50" />

						<h:outputText value="#{cmsg.checkin_main_minor_change}:"  styleClass="propertiesLabel"/>
						<h:selectBooleanCheckbox id="MinorChange" value="#{CircabcCCCheckinFileDialog.minorChange}" />

						<h:outputText value="#{cmsg.checkin_main_changes_info}:"  styleClass="propertiesLabel"/>
						<h:selectBooleanCheckbox id="KeepCheckedOut"  value="#{CircabcCCCheckinFileDialog.keepCheckedOut}" />
					</h:panelGrid>

					<f:verbatim><br /></f:verbatim>
					<circabc:panel id="workingcopy-location-panel" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.checkin_copy_label}" />
					</circabc:panel>
					<br />
					<h:outputText value="#{cmsg.checkin_copy_which_copy}" styleClass="signup_subrub_title" />
					<h:selectOneRadio id="copyLocation" value="#{CircabcCCCheckinFileDialog.copyLocation}" layout="pageDirection">
						<f:selectItem itemValue="__default_copy_location" itemLabel="#{cmsg.checkin_copy_which_copy_current}" />
						<f:selectItem itemValue="__new_copy_location" itemLabel="#{cmsg.checkin_copy_which_copy_other}" />
					</h:selectOneRadio>
				</div>
			</h:form>
		</circabc:displayer>

		<circabc:displayer id="displayer2" rendered="#{CircabcCCCheckinFileDialog.checkinSecondStep}">
			<div class="ContentMainButton">
				<div class="divButtonDialog">
					<h:form acceptcharset="UTF-8" id="FormSecondary">
						<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" disabled="true" /><br />
	                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{CircabcCCCheckinFileDialog.cancel}" />
	                </h:form>
				</div>
			</div>
			<div class="ContentMainForm">

				<circabc:panel id="upload-main-section" styleClass="signup_rub_title">
					<h:outputText value="#{cmsg.add_content_label}" />
				</circabc:panel>

				<circabc:uploadForm returnPage="/jsp/extension/wai/dialog/coci/checkin-file.jsp" submitCallback="CircabcCCCheckinFileDialog.addFile">
					<%-- Locate the content --%>
					<h:outputText id="text1" value="1. #{cmsg.add_content_locate_content}" styleClass="signup_subrub_title"/><br />
					<input type="file" size="75" name="alfFileInput"/><br />

					<%-- Set options file --%>
					<h:outputText id="text2" value="2. #{cmsg.library_add_content_other_options}" styleClass="signup_subrub_title"/><br />
					<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
						<h:selectBooleanCheckbox id="check-disable-notif" value="#{CircabcCCCheckinFileDialog.disableNotification}" title="#{cmsg.notification_disable_mechanism_tooltip}" />
						<h:outputText id="text-notif-disabled" value="#{cmsg.notification_disable_mechanism}"/>
					</h:panelGrid>

					<%-- Upload the file --%>
					<h:outputText id="text3" value="3. #{cmsg.add_content_click_upload}" styleClass="signup_subrub_title"/><br />
					<input type="submit" id="submitFile" name="submitFile" value="${cmsg.add_content_upload}" title="${cmsg.add_content_upload_tooltip}" onclick="showWaitProgress();" />
				</circabc:uploadForm>
			</div>
		</circabc:displayer>

		<circabc:displayer rendered="#{CircabcCCCheckinFileDialog.checkinLastStep}" id="displayer21">
			<h:form acceptcharset="UTF-8" id="FormSecondary">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.update}" action="#{CircabcCCCheckinFileDialog.finish}" /><br />
	                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{CircabcCCCheckinFileDialog.cancel}" />
					</div>
				</div>
				<div class="ContentMainForm">
					<h:outputText id="update-node-confirmation" value="#{cmsg.checkin_summary_click_ok}" styleClass="mainSubTitle" />
				</div>
			</h:form>
		</circabc:displayer>
	</div>
<!-- Content END -->
</div>
<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>