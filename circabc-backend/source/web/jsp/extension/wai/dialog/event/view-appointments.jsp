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

<circabc:panel id="view-appointments-home-search-section" styleClass="signup_rub_title" tooltip="#{cmsg.view_appointments_home_search_label}" >
	<h:outputText value="#{cmsg.view_appointments_home_search_label}"  />
</circabc:panel>

<h:selectOneRadio id="view-appointments-sel-period" value="#{ViewAppointments.periodSelection}" layout="pageDirection">
	<f:selectItem id="view-appointments-item-current-future"
		itemValue="future"
		itemLabel="#{cmsg.view_appointments_current_future}" />
	<f:selectItem id="view-appointments-item-current-previous"
		itemValue="previous"
		itemLabel="#{cmsg.view_appointments_current_previous}" />
	<f:selectItem id="view-appointments-item-exact-date"
		itemValue="exact" itemLabel="#{cmsg.view_appointments_exact_date}" />
</h:selectOneRadio>

<f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>

<circabc:inputDatePicker value="#{ViewAppointments.exactDate}"
			yearCount="#{CircabcDatePickerGenerator.yearCount}"
			startYear="#{CircabcDatePickerGenerator.threeYearsAgo}" id="date-exact"
			initialiseIfNull="true" />

<f:verbatim>
	<br /><br />
</f:verbatim>

<h:selectOneRadio id="view-appointments-sel-interes-group" value="#{ViewAppointments.interestGroupSelection}" layout="pageDirection" >
	<f:selectItem id="view-appointments-item-current-ig" itemValue="ig" itemLabel="#{cmsg.view_appointments_current_interest_group}" itemDisabled="#{ViewAppointments.currentIgDisabled}"/>
	<f:selectItem id="view-appointments-item-all-ig" itemValue="all" itemLabel="#{cmsg.view_appointments_all_interest_group}" />
</h:selectOneRadio>


<f:verbatim>
	<br /><br />
</f:verbatim>

<circabc:panel id="view-appointments-home-result-section" styleClass="signup_rub_title" tooltip="#{cmsg.view_appointments_results_label_tooltip}" >
	<h:outputText value="#{cmsg.view_appointments_results_label}"  />
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<circabc:panel id="panelViewAppointmentsSearchResults" tooltip="#{cmsg.view_appointments_results_label_tooltip}" styleClass="scrollPanel" >
	<circabc:richList id="appointmentsResultsListNoguest" viewMode="circa"
		styleClass="recordSet" headerStyleClass="recordSetHeader"
		rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
		value="#{ViewAppointments.appointments}" var="u"
		initialSortColumn="date"
		pageSize="#{EventBean.listPageSize}">
		<circabc:column id="interest-group-column">
			<f:facet name="header">
				<circabc:sortLink id="interest-group-column-header" label="#{cmsg.interest_group_column_header}" value="interestGroupTitle" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>

			<h:outputText id="interest-group-text" value="#{u.interestGroupTitle}" escape="false"/>
		</circabc:column>
		<circabc:column id="title-column">
			<f:facet name="header">
				<circabc:sortLink id="title-column-header" label="#{cmsg.view_appointments_title_column_header}" value="title" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="title-text" value="#{u.title}" />
		</circabc:column>
		<circabc:column id="date-column">
			<f:facet name="header">
				<circabc:sortLink id="date-column-header" label="#{cmsg.view_appointments_date_column_header}" value="date" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="date-text" value="#{u.date}" />
		</circabc:column>
		<circabc:column id="event-type-column">
			<f:facet name="header">
				<circabc:sortLink id="type-column-header" label="#{cmsg.view_appointments_event_type_column_header}" value="eventType" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="event-type-text" value="#{u.eventType}" />
		</circabc:column>
		<circabc:column id="contact-column">
			<f:facet name="header">
				<circabc:sortLink id="contact-column-header" label="#{cmsg.view_appointments_contact_column_header}" value="contact" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="contact-text" value="#{u.contact}" />
		</circabc:column>
		<circabc:column id="status-column">
			<f:facet name="header">
				<circabc:sortLink id="status-column-header" label="#{cmsg.view_appointments_status_column_header}" value="meetingStatus" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
			</f:facet>
			<h:outputText id="status-text" value="#{u.meetingStatus}" />
		</circabc:column>
		<circabc:column id="col-event-list-actions">
			<f:facet name="header">
				<h:outputText id="text-lib-cont-act" value="#{cmsg.actions}"  />
			</f:facet>
			<circabc:actionLink image="/images/icons/View_details.gif" id="view-details" tooltip="#{cmsg.event_view_meeting_action_tooltip}" value="#{cmsg.event_view_meeting_action_tooltip}" showLink="false" action="wai:dialog:viewEventDetailsWai" actionListener="#{BrowseBean.clickWai}">
          		<circabc:param id="param-event-view-node-id" name="id" value="#{u.eventNodeRef.id}" />
          		<circabc:param id="param-event-view-node-dialogCall" name="dialogAfterClose" value="wai:dialog:viewEventsMeetingWai" />
          		<circabc:param id="param-event-view-node-nodeIdAfterClose" name="nodeIdAfterClose" value="#{NavigationBean.currentNode.id}" />
          		<circabc:param id="param-event-view-node-imageName" name="imageName" value="view_details_#{u.interestGroup50}_#{u.title50}" />
        	</circabc:actionLink>
        	<circabc:actionLink image="/images/icons/delete.gif" id="delete-event" tooltip="#{cmsg.event_delete_meeting_action_tooltip}" value="#{cmsg.event_delete_meeting_action_tooltip}" showLink="false" action="wai:dialog:deleteAppointmentWai" actionListener="#{WaiDialogManager.setupParameters}">
          		<circabc:param id="param-event-delete-node-id" name="id" value="#{u.eventNodeRef.id}" />
          		<circabc:param id="param-event-delete-node-imageName" name="imageName" value="delete_#{u.interestGroup50}_#{u.title50}" />
          		<circabc:param id="param-event-delete-service" name="service" value="Events" />
				<circabc:param id="param-event-delete-acttvity" name="activity" value="Delete Event" />
        	</circabc:actionLink>
		</circabc:column>
		<circabc:dataPager id="view-appointments-pager" styleClass="pagerCirca" />
	</circabc:richList>
	
		<f:verbatim><br /></f:verbatim>	
	
	<circabc:panel id="panelExport" label="#{cmsg.export_label}" tooltip="#{cmsg.export_label}" styleClass="panelSearchGlobal" styleClassLabel="panelSearchLabel">
		<f:verbatim><br /></f:verbatim>
		<h:selectOneMenu id="export-type" value="#{ViewAppointments.exportType}" immediate="true"  >
			<f:selectItems id="export-type-options" value="#{ViewAppointments.exportTypes}" />
		</h:selectOneMenu>
		<h:outputText id="export-space3" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="export-button" action="#{ViewAppointments.export}" value="#{cmsg.export}" />
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:panel>
</circabc:panel>
