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

  	<c:set var="currentTitle" value="${cmsg.manage_importations_dialog_browser_title}" />

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
				<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.manage_importations_dialog_icon_tooltip}" title="#{cmsg.manage_importations_dialog_icon_tooltip}"></h:graphicImage></div>
				<div class="ContentHeaderText">
					<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.manage_importations_dialog_page_title}" /></span><br />
					<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.manage_importations_dialog_page_description}" /></span>
				</div>
			</div>
		</div>

		<div id="ContentMain">
				<div class="ContentMainButton">
					<div class="divButtonDialog">
						<h:commandButton id="add-button" title="#{cmsg.manage_bulk_user_export_dialog_add_action}" value="#{cmsg.manage_bulk_user_export_dialog_add_action}" action="refresh" actionListener="#{ManageImportationsBean.addJob}" onclick="showWaitProgress();" />
	                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{cmsg.close}" action="finish" />
					</div>
				</div>

				<circabc:panel id="import-more-action" styleClass="wai_dialog_more_action">
					<h:graphicImage value="/images/icons/ajax_anim.gif" title="#{cmsg.manage_importations_action_refresh_alt}" alt="#{cmsg.manage_importations_action_refresh_alt}" />
					<h:outputText id="import-space" value="&nbsp;" escape="false" />
					<circabc:actionLink id="edit-nav-action-load-link" value="#{cmsg.manage_importations_action_refresh_title}"  tooltip="#{cmsg.manage_importations_action_refresh_alt}" action="refresh"  />
					<f:verbatim><br /></f:verbatim>
					<h:graphicImage value="/images/icons/workflow_item.gif" title="#{cmsg.manage_importations_action_running_alt}" alt="#{cmsg.manage_importations_action_running_alt}" />
					<h:outputText id="import-bis-space" value="&nbsp;" escape="false" />
					<circabc:actionLink id="edit-nav-action-view-link" value="#{cmsg.manage_importations_action_running_title}" tooltip="#{cmsg.manage_importations_action_running_alt}" action="running" actionListener="#{RunningImportsBean.reset}" />
				</circabc:panel>

				<f:verbatim><br /><br /><br /></f:verbatim>

				<circabc:panel id="import-first-section" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_section_1}" escape="false" />
				</circabc:panel>

				<f:verbatim><br /></f:verbatim>

				<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_name}:" escape="false" />
					<h:inputText value="#{ManageImportationsBean.name}" />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_name_desc}" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_date}:" escape="false" />
					<circabc:inputDatePicker id="new-fire-date" showTime="true" showDate="true" timeAsList="true" value="#{ManageImportationsBean.fireDate}" startYear="#{CircabcDatePickerGenerator.thisYear}" initialiseIfNull="true" step="1" />
					<h:outputText value="" escape="false" style="font-style: italic;"/>

					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_notification_emails}:" escape="false" />
					<h:inputTextarea value="#{ManageImportationsBean.notificationEmails}" rows="5" cols="40" />
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_emails_query_desc}" escape="false" style="font-style: italic;"/>
				</h:panelGrid>

				<f:verbatim><br /></f:verbatim>

				<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0">
					<f:verbatim><br /></f:verbatim>
					<h:dataTable value="#{ManageImportationsBean.iterationsDataModel}" var="row"
				                rowClasses="selectedItemsRow,selectedItemsRowAlt"
				                styleClass="selectedItems" headerClass="selectedItemsHeader"
				                cellspacing="0" cellpadding="4">

				      <h:column>
				         <f:facet name="header">
							<h:outputText value=" " />
					     </f:facet>
						<h:selectOneRadio value="#{ManageImportationsBean.selectedIteration}"  onchange="dataTableSelectOneRadio(this);">
							<f:selectItem itemValue="#{row.identifier}" itemLabel=""/>
						</h:selectOneRadio>
					  </h:column>

				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_date}" />
				         </f:facet>
						<h:outputText value="#{row.iterationStartDate}" escape="false">
							<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
						</h:outputText>
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_identifier}" />
				         </f:facet>
				         <h:outputText value="#{row.identifier}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_user}" />
				         </f:facet>
				         <h:outputText value="#{row.creator}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_description}" />
				         </f:facet>
				         <h:outputText value="#{row.description}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_num_etl}" />
				         </f:facet>
				         <h:outputText value="#{row.transformationDatesSize}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_num_transformed}" />
				         </f:facet>
				         <h:outputText value="#{row.importedDatesSize}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_num_failed}" />
				         </f:facet>
				         <h:outputText value="#{row.failedImportationSize}" />
				      </h:column>
				   </h:dataTable>
				</h:panelGrid>

				<f:verbatim><br /><br /></f:verbatim>

				<circabc:panel id="dit-profiles-secnd-section" styleClass="signup_rub_title">
					<h:outputText value="#{cmsg.manage_bulk_user_export_dialog_section_2}" escape="false" />
				</circabc:panel>

				<f:verbatim><br /></f:verbatim>

				<circabc:richList id="manage-import-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{ManageImportationsBean.registredJobs}" var="s" initialSortDescending="false" initialSortColumn="expectedFire"  >

					<circabc:column id="manage-import-col-identifier">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-identifier-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_identifier}" value="identifier" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-identifier-value" value="#{s.identifier}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-import-col-expectedfire">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-expectedfire-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_expectedfire}" value="expectedFire" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-expectedfire-value" value="#{s.expectedFire}" escape="false" >
							<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
						</h:outputText>
					</circabc:column>

					<circabc:column id="manage-import-col-iteration">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-iteration-sorter" label="#{cmsg.manage_exportations_dialog_selected_iteration}" value="iterationName" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-iteration-value" value="#{s.iterationName}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-import-col-status">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-status-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_status}" value="status" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-status-value" value="#{s.status}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-import-col-realfire">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-realfire-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_realfire}" value="realFire" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-realfire-value" value="#{s.realFire}" escape="false" >
							<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
						</h:outputText>
					</circabc:column>

					<circabc:column id="manage-import-col-xmlfile">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-xmlfile-sorter" label="#{cmsg.manage_importations_dialog_col_xmlfile}" value="xmlFile" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-xmlfile-value" value="#{s.xmlFile}" escape="false" converter="eu.cec.digit.circabc.faces.ServiceDisplayPathConverter" />
					</circabc:column>

					<circabc:column id="manage-import-col-numberofusers">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-numberofusers-sorter" label="#{cmsg.manage_importations_dialog_col_items}" value="numberOfItems" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-numberofusers-value" value="#{s.numberOfItems}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-import-col-time">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-time-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_time}" value="processTimeMillis" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-time-value" value="#{s.processTimeMillis}" escape="false" />
					</circabc:column>

					<circabc:column id="manage-import-col-errormessage">
						<f:facet name="header">
							<circabc:sortLink id="manage-import-errormessage-sorter" label="#{cmsg.manage_bulk_user_export_dialog_col_errormessage}" value="errorMessage" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
						</f:facet>
						<h:outputText id="manage-import-errormessage-value" value="#{s.errorMessage}" escape="false" />
					</circabc:column>

					<f:facet name="empty">
						<h:outputFormat id="manage-import-nolist" value="#{cmsg.no_list_items}" escape="false" styleClass="noItem"/>
					</f:facet>

				</circabc:richList>
		</div>
	</div>
</circabc:displayer>

<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
	<h1>Only super adminstrator can access to this page!</h1>
</circabc:displayer>

<script type="text/javascript">

    function dataTableSelectOneRadio(radio)
    {
        var id = radio.name.substring(radio.name.lastIndexOf(':'));
        var el = radio.form.elements;
        for (var i = 0; i < el.length; i++) {
            if (el[i].name.substring(el[i].name.lastIndexOf(':')) == id) {
                el[i].checked = false;
            }
        }
        radio.checked = true;
    }
</script>
</h:form>
</circabc:view>






