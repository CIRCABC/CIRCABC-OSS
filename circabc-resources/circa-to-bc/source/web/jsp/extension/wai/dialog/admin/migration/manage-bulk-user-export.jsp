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

  	<c:set var="currentTitle" value="${cmsg.manage_bulk_user_export_dialog_browser_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>

<circabc:displayer id="displayer-is-admin" rendered="#{NavigationBean.currentUser.admin == true}">

	<div id="maincontent">

		<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

		<%-- Content START --%>
		<div id="ContentHeader">
			<div class="ContentHeaderNavigationLibrary">
			<circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></div>
			<div>
				<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.manage_bulk_user_export_dialog_icon_tooltip}" title="#{cmsg.manage_bulk_user_export_dialog_icon_tooltip}"></h:graphicImage></div>
				<div class="ContentHeaderText">
					<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.manage_bulk_user_export_dialog_title}" /></span><br />
					<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.manage_bulk_user_export_dialog_description}" /></span>
				</div>
			</div>
		</div>

		<div id="ContentMain">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:commandButton id="add-button" title="#{cmsg.manage_bulk_user_export_dialog_add_action}" value="#{cmsg.manage_bulk_user_export_dialog_add_action}" action="refresh" actionListener="#{ManageBulkUserExport.addJob}" onclick="showWaitProgress();" />
	                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{cmsg.close}" action="finish" />
					</div>
				</div>

				<circabc:panel id="big-bang-more-action" styleClass="wai_dialog_more_action">
					<h:graphicImage value="/images/icons/ajax_anim.gif" title="#{cmsg.manage_importations_action_refresh_alt}" alt="#{cmsg.manage_importations_action_refresh_alt}" />
					<h:outputText id="big-bang-space" value="&nbsp;" escape="false" />
					<circabc:actionLink id="edit-nav-action-load-link" value="#{cmsg.manage_importations_action_refresh_title}" tooltip="#{cmsg.manage_importations_action_refresh_alt}" action="wai:dialog:close:refresh" />
					<f:verbatim><br /></f:verbatim>
					<h:graphicImage value="/images/icons/workflow_item.gif" title="#{cmsg.manage_importations_action_running_alt}" alt="#{cmsg.manage_importations_action_running_alt}" />
					<h:outputText id="import-bis-space" value="&nbsp;" escape="false" />
					<circabc:actionLink id="edit-nav-action-view-link" value="#{cmsg.manage_importations_action_running_title}" tooltip="#{cmsg.manage_importations_action_running_alt}" action="running" actionListener="#{RunningImportsBean.reset}" />
				</circabc:panel>

				<f:verbatim><br /><br /><br /></f:verbatim>

				<circabc:panel id="big-bang-first-section" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_section_1}" escape="false" />
				</circabc:panel>

				<f:verbatim><br /></f:verbatim>

				<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_name}:" escape="false" />
					<h:inputText value="#{ManageBulkUserExport.name}" />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_name_desc}" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_emails_query}:" escape="false" />
					<h:inputTextarea value="#{ManageBulkUserExport.emailList}" rows="5" cols="40" />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_emails_query_desc}" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_conjunction}:" escape="false" />
					<h:selectBooleanCheckbox value="#{ManageBulkUserExport.conjunction}"  />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_conjunction_desc}" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_negation}:" escape="false" />
					<h:selectBooleanCheckbox value="#{ManageBulkUserExport.negation}"  />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_negation_desc}" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_date}:" escape="false" />
					<circabc:inputDatePicker id="new-fire-date" showTime="true" showDate="true" timeAsList="true" value="#{ManageBulkUserExport.fireDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" step="1" />
					<h:outputText value="" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_notification_emails}:" escape="false" />
					<h:inputTextarea value="#{ManageBulkUserExport.notificationEmails}" rows="5" cols="40" />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_emails_query_desc}" escape="false" style="font-style: italic;"/>
				</h:panelGrid>

				<f:verbatim><br /><br /></f:verbatim>

				<circabc:panel id="dit-profiles-secnd-section" styleClass="signup_rub_title">
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_section_2}" escape="false" />
				</circabc:panel>

				<f:verbatim><br /></f:verbatim>

				<circabc:richList id="manage-big-bang-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{ManageBulkUserExport.registredJobs}" var="s" initialSortDescending="false" initialSortColumn="expectedFire"  >

					<circabc:column id="manage-big-bang-col-identifier">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-identifier-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_identifier}" value="identifier" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-identifier-value" value="#{s.identifier}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-expectedfire">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-expectedfire-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_expectedfire}" value="expectedFire" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-expectedfire-value" value="#{s.expectedFire}" escape="false" >
							<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
						</h:outputText>
					</circabc:column>

					<circabc:column id="manage-big-bang-col-query">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-query-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_query}" value="query" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-query-value" value="#{s.query}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-conjunction">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-conjunction-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_conjunction}" value="conjunction" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-conjunction-value" value="#{s.conjunction}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-negation">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-negation-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_negation}" value="negation" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-negation-value" value="#{s.negation}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-status">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-status-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_status}" value="status" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-status-value" value="#{s.status}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-realfire">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-realfire-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_realfire}" value="realFire" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-realfire-value" value="#{s.realFire}" escape="false" >
							<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
						</h:outputText>
					</circabc:column>

					<circabc:column id="manage-big-bang-col-xmlfile">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-xmlfile-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_xmlfile}" value="xmlFile" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-xmlfile-value" value="#{s.xmlFile}" escape="false" converter="eu.cec.digit.circabc.faces.ServiceDisplayPathConverter" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-numberofusers">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-numberofusers-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_numberofusers}" value="numberOfItems" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-numberofusers-value" value="#{s.numberOfItems}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-time">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-time-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_time}" value="processTimeMillis" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-time-value" value="#{s.processTimeMillis}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-big-bang-col-errormessage">
						<f:facet name="header">
							<circabc:sortLink id="manage-big-bang-errormessage-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_errormessage}" value="errorMessage" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-big-bang-errormessage-value" value="#{s.errorMessage}" escape="false" />
					</circabc:column>

					<f:facet name="empty">
						<h:outputFormat id="manage-big-bang-nolist" value="#{cmsg.no_list_items}" escape="false" styleClass="noItem"/>
					</f:facet>

				</circabc:richList>
		</div>
	</div>
</circabc:displayer>

<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
	<h1>Only super adminstrator can access to this page!</h1>
</circabc:displayer>

</h:form>

</circabc:view>






