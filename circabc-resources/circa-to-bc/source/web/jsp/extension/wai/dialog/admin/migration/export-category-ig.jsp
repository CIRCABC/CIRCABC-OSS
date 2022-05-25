<%--+
    |     Copyright European Community 2015 - Licensed under the EUPL V.1.0
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
		value="${cmsg.migration_export_category_ig_dialog_page_title}" />

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
			alt="#{cmsg.migration_export_category_ig_dialog_page_title}"
			title="#{cmsg.migration_export_category_ig_dialog_page_title}"></h:graphicImage></div>
		<div class="ContentHeaderText"><span class="ContentHeaderTitle"><h:outputText
			value="#{cmsg.migration_export_category_ig_dialog_page_title}" /></span><br />
		<span class="ContentHeaderSubTitle"><h:outputText
			value="#{cmsg.migration_export_category_ig_dialog_page_description}" /></span></div>
		</div>
		</div>
		
		<div id="ContentMain">
		<h:form acceptcharset="UTF-8" id="FormSecondary12">
			
			<div class="ContentMainButton">
				<div class="divButtonDialog">
					<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}"
						action="#{ExporterBean.finish}" onclick="showWaitProgress();" />
					<br />
					<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" 
						action="finish" />
				</div>
			</div>
			
			<f:verbatim>
				<br />
			</f:verbatim>
			
			<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_note_select}" escape="false" />
			
			<f:verbatim>
				<br />
			</f:verbatim>
			
			<div>
				<f:verbatim>
					<br />
				</f:verbatim>
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_select_category}" escape="false" />
				<h:selectOneMenu value="#{ExporterBean.selectedCategory}" onchange="submit()" valueChangeListener="#{ExporterBean.selectIgs}">
					<f:selectItems value="#{ExporterBean.categories}" />
	            </h:selectOneMenu>
			</div>
			
			<div>
				<f:verbatim>
					<br/>
				</f:verbatim>
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_select_igs}" escape="false" />
				<f:verbatim>
					<br/>&nbsp; &nbsp;
				</f:verbatim>
				<h:selectManyListbox value="#{ExporterBean.selectedIG}" onchange="submit()">
                    <f:selectItems value="#{ExporterBean.igs}" />
                </h:selectManyListbox>
				<br/>
				<i><h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_note_no_select}" escape="false" /></i>
			</div>
			
			<div>
				<f:verbatim>
					<br/>
				</f:verbatim>
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_export_start}" escape="false" />
				<t:inputDate value="#{ExporterBean.exportStartDate}" type="both" popupCalendar="false" ampm="false" />
				<br/>
				<i><h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_note_select_date_now}" escape="false" /></i>
			</div>
			
			<div>
				<f:verbatim>
					<br />
				</f:verbatim>
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_export_server_directory}" escape="false" />
				<h:inputText value="#{ExporterBean.exportDirectory}" />
			</div>
			
			<div>
				<f:verbatim>
					<br />
				</f:verbatim>
				<h:selectBooleanCheckbox value="#{ExporterBean.exportAuthorities}" />
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_export_authorities}" escape="false" />
			</div>
			
			<div>
				<f:verbatim>
					<br />
				</f:verbatim>
				<h:selectBooleanCheckbox value="#{ExporterBean.exportStructure}" />
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_export_structure}" escape="false" />
			</div>
			
			<div>
				<f:verbatim>
					<br />&nbsp; &nbsp; &nbsp; 
				</f:verbatim>
				<h:selectBooleanCheckbox value="#{ExporterBean.exportOnlyRoot}" />
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_export_only_root}" escape="false" />
			</div>
			
			<div>
				<f:verbatim>
					<br />
				</f:verbatim>
				<h:selectBooleanCheckbox value="#{ExporterBean.exportHeaders}" />
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_export_headers}" escape="false" />
			</div>
			
			<f:verbatim>
				<br />
			</f:verbatim>
			
			<div>
				<f:verbatim>
					<br />
					<b>
				</f:verbatim>
				<h:outputText value="#{cmsg.migration_export_category_ig_dialog_page_statuses}" escape="false" />
				<f:verbatim>
					</b>
				</f:verbatim>
				<h:outputText value="#{ExporterBean.workingExportNames}" escape="false" />
			</div>
			
			<f:verbatim>
				<br />
			</f:verbatim>
			
		</h:form>
		</div>
		
	</circabc:displayer>
	
</circabc:view>