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

<c:set var="currentTitle" value="${cmsg.manage_statistics_dialog_browser_title}" />

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
					<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.manage_statistics_dialog_icon_tooltip}" title="#{cmsg.manage_statistics_dialog_icon_tooltip}"></h:graphicImage></div>
					<div class="ContentHeaderText">
						<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.manage_statistics_dialog_page_title}" /></span><br />
						<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.manage_statistics_dialog_page_description}" /></span>
					</div>
				</div>
			</div>

			<div id="ContentMain">
					<div class="ContentMainButton">
						<div class="divButtonDialog">
							<h:commandButton id="add-button" title="#{cmsg.manage_bulk_user_export_dialog_add_action}" value="#{cmsg.manage_bulk_user_export_dialog_add_action}" action="refresh" actionListener="#{ManageExportationStatisticsBean.addJob}" onclick="showWaitProgress();" />
		                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{cmsg.close}" action="finish" />
						</div>
					</div>

					<circabc:panel id="stats-more-action" styleClass="wai_dialog_more_action">
						<h:graphicImage value="/images/icons/ajax_anim.gif" title="#{cmsg.manage_importations_action_refresh_alt}" alt="#{cmsg.manage_importations_action_refresh_alt}" />
						<h:outputText id="stats-space" value="&nbsp;" escape="false" />
						<circabc:actionLink id="edit-nav-action-load-link" value="#{cmsg.manage_importations_action_refresh_title}" tooltip="#{cmsg.manage_importations_action_refresh_alt}" action="wai:dialog:close:refresh"  />
						<f:verbatim><br /></f:verbatim>
						<h:graphicImage value="/images/icons/workflow_item.gif" title="#{cmsg.manage_importations_action_running_alt}" alt="#{cmsg.manage_importations_action_running_alt}" />
						<h:outputText id="import-bis-space" value="&nbsp;" escape="false" />
						<circabc:actionLink id="edit-nav-action-view-link" value="#{cmsg.manage_importations_action_running_title}" tooltip="#{cmsg.manage_importations_action_running_alt}" action="running" actionListener="#{RunningImportsBean.reset}" />
					</circabc:panel>

					<f:verbatim><br /><br /><br /></f:verbatim>

					<circabc:panel id="stats-first-section" styleClass="signup_rub_title">
							<h:outputText id="stats-first-section-txt" value="#{cmsg.manage_bulk_user_export_dialog_section_1}" escape="false" />
					</circabc:panel>

					<f:verbatim><br /></f:verbatim>
					<h:panelGrid id="stats-panel-options" columns="3" cellpadding="3" cellspacing="3" border="0">

						<h:outputText id="stats-opt-identifier-txt" value="#{cmsg.manage_bulk_user_export_dialog_name}:" escape="false" />
						<h:inputText id="stats-opt-identifier-val" value="#{ManageExportationStatisticsBean.name}" />
						<h:outputText id="stats-opt-identifier-desc" value="#{cmsg.manage_bulk_user_export_dialog_name_desc}" escape="false" style="font-style: italic;"/>

						<h:outputText id="stats-opt-allig-txt" value="#{cmsg.manage_statistics_dialog_page_all_ig}:" escape="false" />
						<h:selectBooleanCheckbox id="stats-opt-allig-val" value="#{ManageExportationStatisticsBean.allInterestGroup}" />
						<h:outputText id="stats-opt-allig-desc" value="#{cmsg.manage_statistics_dialog_page_all_ig_desc}" escape="false" style="font-style: italic;"/>

						<h:outputText id="stats-opt-cat-txt" value="#{cmsg.manage_exportations_dialog_category}:" escape="false" />
						<h:selectOneMenu id="stats_cat_list" value="#{ManageExportationStatisticsBean.selectedCategory}" onchange="updateIGList();" valueChangeListener="#{ManageExportationStatisticsBean.updateInterestGroupList}" >
							<f:selectItems id="stats-opt-cat-items" value="#{ManageExportationStatisticsBean.categories}" />
						</h:selectOneMenu>
						<h:commandButton id="select-category" styleClass="" value="List" action="refresh" actionListener="#{WaiLeftMenuBean.doNothingAction}" />

						<h:outputText id="stats-opt-ig-text" value="#{cmsg.manage_exportations_dialog_interestgroups}:" escape="false" />
						<h:selectManyListbox id="stats_ig_list" valueChangeListener="#{ManageExportationStatisticsBean.updateSelectedIgList}" size="12" >
							<f:selectItems id="stats-opt-ig-items" value="#{ManageExportationStatisticsBean.interestGroups}" />
						</h:selectManyListbox>
						<h:commandButton id="select-ig" styleClass="" value="Select" action="refresh" />

						<h:outputText id="stats-opt-date" value="#{cmsg.manage_bulk_user_export_dialog_date}:" escape="false" />
						<circabc:inputDatePicker id="new-fire-date" showTime="true" showDate="true" timeAsList="true" value="#{ManageExportationStatisticsBean.fireDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" step="1" />
						<h:outputText id="stats-opt-date-desc" value="" escape="false" style="font-style: italic;"/>

						<h:outputText id="stats-opt-emails" value="#{cmsg.manage_bulk_user_export_dialog_notification_emails}:" escape="false" />
						<h:inputTextarea id="stats-opt-email-inp" value="#{ManageExportationStatisticsBean.notificationEmails}" rows="5" cols="40" />
						<h:outputText id="stats-opt-emails-desc" value="#{cmsg.manage_bulk_user_export_dialog_emails_query_desc}" escape="false" style="font-style: italic;"/>

						<h:outputText id="stats-opt-sel-ig-text" value="#{cmsg.manage_exportations_dialog_interestgroups}:" escape="false" />
						<h:dataTable id="stats-opt-sel-ig-table" value="#{ManageExportationStatisticsBean.interestGroupDataModel}" var="row"
							rowClasses="selectedItemsRow,selectedItemsRowAlt"
							styleClass="selectedItems" headerClass="selectedItemsHeader"
							cellspacing="0" cellpadding="4"
							rendered="#{ManageExportationStatisticsBean.interestGroupDataModel.rowCount != 0}">
							<h:column>
								<f:facet name="header">
									<h:outputText id="stats-opt-sel-ig-col" value="#{msg.name}" />
								</f:facet>
								<h:outputText id="stats-opt-sel-ig-row" value="#{row}" />
							</h:column>
							<h:column>
								<circabc:actionLink id="stats-opt-sel-ig-remove" actionListener="#{ManageExportationStatisticsBean.removeSelection}"
									image="/images/icons/delete.gif" tooltip="#{cmsg.remove}" value="#{msg.remove}" showLink="false"
									styleClass="pad6Left" />
							</h:column>
						</h:dataTable>
						<a:panel id="no-items" rendered="#{ManageExportationStatisticsBean.interestGroupDataModel.rowCount == 0}">
							<h:outputText id="no-item-txt" value="#{msg.name}: #{msg.no_selected_items}" />
						</a:panel>
						<h:outputText id="stats-opt-sel-ig-row-des" value="" escape="false" style="font-style: italic;"/>
					</h:panelGrid>


					<f:verbatim><br /><br /></f:verbatim>

					<circabc:panel id="dit-profiles-secnd-section" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_section_2}" escape="false" />
					</circabc:panel>

					<f:verbatim><br /></f:verbatim>

					<circabc:richList id="manage-export-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{ManageExportationStatisticsBean.registredJobs}" var="s" initialSortDescending="false" initialSortColumn="expectedFire"  >

 					<circabc:column id="manage-import-col-identifier">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-identifier-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_identifier}" value="identifier" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-identifier-value" value="#{s.identifier}" escape="false" />
					</circabc:column>

						<circabc:column id="manage-export-col-expectedfire">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-expectedfire-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_expectedfire}" value="expectedFire" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-expectedfire-value" value="#{s.expectedFire}" escape="false" >
								<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
							</h:outputText>
						</circabc:column>

						<circabc:column id="manage-export-col-igs">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-igs-sorter" label="#{cmsg.manage_exportations_dialog_interestgroups}" value="interestGroups" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-igs-value" value="#{s.interestGroups}" escape="false" />
						</circabc:column>

						<circabc:column id="manage-export-col-status">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-status-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_status}" value="status" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-status-value" value="#{s.status}" escape="false" />
						</circabc:column>

						<circabc:column id="manage-export-col-realfire">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-realfire-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_realfire}" value="realFire" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-realfire-value" value="#{s.realFire}" escape="false" >
								<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
							</h:outputText>
						</circabc:column>

						<circabc:column id="manage-export-col-xmlfile">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-xmlfile-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_xmlfile}" value="xmlFile" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-xmlfile-value" value="#{s.xmlFile}" escape="false" converter="eu.cec.digit.circabc.faces.ServiceDisplayPathConverter" />
						</circabc:column>

						<circabc:column id="manage-expoprt-col-time">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-time-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_time}" value="processTimeMillis" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-time-value" value="#{s.processTimeMillis}" escape="false" />
						</circabc:column>

						<circabc:column id="manage-export-col-errormessage">
							<f:facet name="header">
								<circabc:sortLink id="manage-export-errormessage-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_errormessage}" value="errorMessage" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
							</f:facet>
							<h:outputText id="manage-export-errormessage-value" value="#{s.errorMessage}" escape="false" />
						</circabc:column>

						<f:facet name="empty">
							<h:outputFormat id="manage-export-nolist" value="#{cmsg.no_list_items}" escape="false" styleClass="noItem"/>
						</f:facet>

					</circabc:richList>

			</div>
		</div>
	</circabc:displayer>

	<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
		<h1>Only super adminstrator can access to this page!</h1>
	</circabc:displayer>

	<script language="javascript">
	    function updateIGList(){
	        document.getElementById("FormPrincipal:select-category").click();
	    }
	    function updateList(){
	        document.getElementById("FormPrincipal:select-ig").click();
	    }
	</script>
</h:form>
</circabc:view>






