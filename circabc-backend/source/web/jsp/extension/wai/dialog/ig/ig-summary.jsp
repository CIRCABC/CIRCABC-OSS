<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? � as soon they
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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page buffer="32kb" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />


		<circabc:panel id="panel_statistic_choice">
		<h:outputFormat id="avalaible_statistics_title" value="#{cmsg.ig_summary_statistic_available_title}" styleClass="panel_header_little" >		</h:outputFormat>
		<br/>
		<h:outputLabel for="select_statistic_to_display" value="#{cmsg.ig_summary_statistic_to_display_label}" ></h:outputLabel>
		<h:selectOneMenu id="select_statistic_to_display" value="#{IgSummaryDialog.selectedStatisticChoices}"> <!--  -->
			<f:selectItems value="#{IgSummaryDialog.availableStatisticChoices}"/>
		</h:selectOneMenu>
		<h:commandButton value="#{cmsg.ig_summary_statistic_to_display_button}" actionListener="#{IgSummaryDialog.refresh}"></h:commandButton>
		<br/><br/>
		</circabc:panel>
		
		
		<circabc:panel id="panel_statistic_table">
		<h:outputFormat id="statistic_table_title" value="#{cmsg.ig_summary_statistic_table_title}"  styleClass="panel_header_little" rendered="#{IgSummaryDialog.igStatisticsChoosen }"></h:outputFormat>
		<h:outputFormat id="structure_title" value="#{cmsg.ig_summary_structure_title}"  styleClass="panel_header_little" rendered="#{IgSummaryDialog.igStructureChoosen }"></h:outputFormat>
		<h:outputFormat id="timeline_activity_title" value="#{cmsg.ig_summary_timeline_activity_title}"  styleClass="panel_header_little" rendered="#{IgSummaryDialog.igTimelineChoosen }"></h:outputFormat>
		<br/>
		<h:dataTable id="data" value="#{IgSummaryDialog.igData }" var="row" rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader" width="100%" rendered="#{IgSummaryDialog.igStatisticsChoosen }"
			binding="#{IgSummaryDialog.dataTable}">
		<h:column>
			  <f:facet name="header">
			  <h:outputText value="#{cmsg.ig_summary_statistic_dimension_name}" />
			  </f:facet> 
			   <h:outputText value="#{row.dataName}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="#{cmsg.ig_summary_statistic_dimension_value}"/>
			  </f:facet> 
			   <h:outputText value="#{row.dataValue}"></h:outputText>
			</h:column>
		</h:dataTable>
		<h:dataTable id="timeline" value="#{IgSummaryDialog.igTimeLineActivity }" var="row" rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader" width="100%" rendered="#{IgSummaryDialog.igTimelineChoosen }"
			binding="#{IgSummaryDialog.dataTable2}">
		<h:column>
			  <f:facet name="header">
			  <h:outputText value="#{cmsg.ig_summary_timeline_activity_month_title}" />
			  </f:facet> 
			   <h:outputText value="#{row.monthActivity}">
			   		<a:convertXMLDate type="both" pattern="#{cmsg.ig_summary_timeline_date_time_pattern}" />
			   </h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="#{cmsg.ig_summary_timeline_activity_service_title}"/>
			  </f:facet> 
			   <h:outputText value="#{row.service}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="#{cmsg.ig_summary_timeline_activity_activity_title}"/>
			  </f:facet> 
			   <h:outputText value="#{row.activity}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="#{cmsg.ig_summary_timeline_activity_number_title}"/>
			  </f:facet> 
			   <h:outputText value="#{row.actionNumber}"></h:outputText>
			</h:column>
		</h:dataTable>
		<h:outputText value="#{cmsg.ig_summary_structure_description}" rendered="#{IgSummaryDialog.igStructureChoosen }" ></h:outputText>
		<br/><br/>
		<h:outputText value="#{IgSummaryDialog.htmlServiceTree }" rendered="#{IgSummaryDialog.igStructureChoosen }" escape="false"></h:outputText>
		<br/>
		</circabc:panel>
		
		
		<circabc:panel id="panel_statistic_export" rendered="#{IgSummaryDialog.igStatisticsChoosen or IgSummaryDialog.igTimelineChoosen}">
		<h:outputFormat id="statistic_export_option_title" value="#{cmsg.ig_summary_statistic_table_export_title}"  styleClass="panel_header_little" >		</h:outputFormat>
		<br/>
		<h:outputLabel for="statistic_export_option" value="#{cmsg.ig_summary_statistic_to_display_label}" ></h:outputLabel>
		<h:selectOneMenu id="statistic_export_option" value="#{IgSummaryDialog.selectedStatisticExport}" >  
			<f:selectItems value="#{IgSummaryDialog.availableExportOptions}"/>
		</h:selectOneMenu>
		<h:commandButton value="#{cmsg.ig_summary_statistic_export_select}" actionListener="#{IgSummaryDialog.export}"></h:commandButton>
		</circabc:panel> 
	