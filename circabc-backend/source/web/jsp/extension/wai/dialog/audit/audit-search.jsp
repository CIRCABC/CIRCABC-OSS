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

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ taglib uri="/WEB-INF/myfaces_sandbox.tld" prefix="s"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<!--  Select language panel -->
<circabc:panel id="recursive-audit-search-main-section" styleClass="signup_rub_title">
	<h:outputText value="#{cmsg.search_filter}"/>
</circabc:panel>



<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">

   <%-- the service filter drop down --%>
   <h:outputText value="#{cmsg.user}:"/>
   <h:selectOneMenu id="users" value="#{DialogManager.bean.user}" onchange="submit();" valueChangeListener="#{DialogManager.bean.changeUserSelection}" immediate="false">
      <f:selectItem  itemLabel="#{msg.All}"   itemValue="null"/>
      <f:selectItems value="#{DialogManager.bean.users}"/>
   </h:selectOneMenu>

   <%-- the service filter drop down --%>
   <h:outputText value="#{cmsg.service}:"/>
   <h:selectOneMenu id="services" value="#{DialogManager.bean.service}" onchange="submit();" valueChangeListener="#{DialogManager.bean.changeServiceSelection}" immediate="false">
      <f:selectItem  itemLabel="#{msg.All}"   itemValue="null"/>
      <f:selectItems value="#{DialogManager.bean.services}"/>
   </h:selectOneMenu>

   <%-- the method filter drop down --%>
   <h:outputText value="#{cmsg.method}:"/>
   <h:selectOneMenu id="methods" value="#{DialogManager.bean.method}" immediate="false">
      <f:selectItem  itemLabel="#{msg.All}"   itemValue="null"/>
      <f:selectItems value="#{DialogManager.bean.methods}"/>
   </h:selectOneMenu>

   <%-- the from date filter Date Picker --%>
   <h:outputText value="#{msg.from}:"/>
   <circabc:inputDatePicker value="#{DialogManager.bean.fromDate}" yearCount="#{AuditSearchDialog.yearCount}" startYear="#{AuditSearchDialog.startYear}" id="date-from" initialiseIfNull="true" />

   <%-- the to date filter Date Picker --%>
   <h:outputText value="#{msg.to}:"/>
   <circabc:inputDatePicker value="#{DialogManager.bean.toDate}" yearCount="#{AuditSearchDialog.yearCount}" startYear="#{AuditSearchDialog.startYear}" id="date-to" initialiseIfNull="true" />
</h:panelGrid>

<circabc:panel id="occurences-info-panel" styleClass="infoPanel" styleClassLabel="infoContent" rendered="#{DialogManager.bean.occurence > -1}" >
   <h:outputFormat value="#{cmsg.occurence_found}">
		<circabc:param value="#{DialogManager.bean.occurence}" />
   </h:outputFormat>
   <h:panelGrid columns="1" rendered="#{DialogManager.bean.warningFounds}" >
       <h:outputText  value="#{cmsg.but_warning_found}" styleClass="infoContent"/>
	   <h:dataTable var="warning" value="#{DialogManager.bean.warningMessages}" >
	   	   <h:column>
				<h:graphicImage value="/images/icons/deploy_successful.gif" alt="#{cmsg.message_warn_tooltip}" />
  	  	   </h:column>
		   <h:column>
   				<h:outputText value="#{warning}"/>
  	  	   </h:column>
       </h:dataTable>
   </h:panelGrid>
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<h:panelGroup id="audit-panel-facets">
	<f:facet name="title">
		<a:panel id="audit-page-control" style="font-size:9px">
	      <h:outputText value="#{msg.items_per_page}" id="items-txt2"/>
	      <h:inputText id="audit-pages" value="#{DialogManager.bean.pagination}" style="width:24px;margin-left:4px" maxlength="3"  />
	      <div style="display:none">
	      		<circabc:actionLink id="audit-apply" value="" tooltip="apply" actionListener="#{DialogManager.bean.updateAuditPageSize}" />
	      </div>
	   </a:panel>
	</f:facet>
</h:panelGroup>
<circabc:panel id="ScrollPanel" styleClass="scrollPanel" >

	<%-- Audit list --%>
	<a:richList id="auditRichList" viewMode="details" pageSize="#{DialogManager.bean.pagination}"
	      styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
	      value="#{DialogManager.bean.auditList}" var="r" rendered="#{DialogManager.bean.auditList != null}"
	      initialSortColumn="logDate" initialSortDescending="true">

	   <%-- Component to display if the list is empty --%>
	   <f:facet name="empty">
	      <h:outputText value="<i>#{cmsg.no_audit_entry_found}</i><br /><br /><br />" escape="false"/>
	   </f:facet>
	    <%-- Timestamp column --%>
		<a:column id="audit-date" style="text-align:left; white-space:nowrap; padding-right:5px">
		    <f:facet name="header">
		       <h:outputText id="audit-date-label" value="#{msg.date}" styleClass="header"/>
		    </f:facet>
		    <h:outputText id="audit-date-txt" value="#{r.logDate}">
		       <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
			</h:outputText>
		</a:column>

	    <%-- User name column --%>
	    <a:column id="audit-username" style="text-align:left; padding-right:5px">
	       <f:facet name="header">
	          <h:outputText id="audit-username-label" value="#{msg.username}" styleClass="header"/>
	       </f:facet>
	       <h:outputText id="audit-username-txt" value="#{r.userName}" />
	    </a:column>

	    <%-- Service column --%>
	    <a:column id="audit-service" style="text-align:left; padding-right:5px">
	       <f:facet name="header">
	          <h:outputText id="audit-service-label" value="#{cmsg.service}"  styleClass="header" />
	       </f:facet>
	       <h:outputText id="audit-service-txt" value="#{r.serviceDescription}" />
	    </a:column>

	    <%-- Activity column --%>
	    <a:column id="audit-method" style="text-align:left; padding-right:5px">
	       <f:facet name="header">
	          <h:outputText id="audit-method-label" value="#{cmsg.activity}" styleClass="header"/>
	       </f:facet>
	       <h:outputText id="audit-method-txt" value="#{r.activityDescription}" />
	    </a:column>

		<%-- Failed column --%>
	    <a:column id="audit-status" style="text-align:left; padding-right:5px">
	       <f:facet name="header">
	          <h:outputText id="audit-status-label" value="#{msg.status}" styleClass="header"/>
	       </f:facet>
	       <h:outputText id="audit-status-txt" value="#{r.status}" />

	    </a:column>

	    <%-- Path column --%>
	    <a:column id="audit-path" style="text-align:left; padding-right:5px">
	       <f:facet name="header">
	          <h:outputText id="audit-path-label" value="#{msg.path}"  styleClass="header"/>
	       </f:facet>
	       <h:outputText id="audit-path-txt" value="#{r.path}" />
	    </a:column>

	    <%-- Path info --%>
	    <a:column id="audit-info" style="text-align:left; padding-right:5px">
	       <f:facet name="header">
	          <h:outputText id="audit-info-label" value="#{cmsg.additionalInformation}" styleClass="header"/>
	       </f:facet>
	       <h:outputText id="audit-info-txt" value="#{r.info}" />
	    </a:column>

	    <circabc:dataPager id="audit-search-pager" styleClass="pagerCircaLeft" />
	</a:richList>
</circabc:panel>
<circabc:panel id="panelExport" label="#{cmsg.export_label}" tooltip="#{cmsg.export_label}" styleClass="panelSearchGlobal" styleClassLabel="panelSearchLabel">
		<f:verbatim><br /></f:verbatim>
		<h:selectOneMenu id="export-type" value="#{DialogManager.bean.exportType}" immediate="true"  >
			<f:selectItems id="export-type-options" value="#{DialogManager.bean.exportTypes}" />
		</h:selectOneMenu>
		<h:outputText id="export-space3" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="export-button" action="#{DialogManager.bean.export}" value="#{cmsg.export}" />
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:panel>

<script language="JavaScript1.2">

   function applySizeAudit(e)
   {
      return applySize(e, 'audit-apply');
   }

   function applySize(e, field)
   {
      var keycode;
      if (window.event) keycode = window.event.keyCode;
      else if (e) keycode = e.which;
      if (keycode == 13)
      {
         document.forms['browse']['browse:act'].value='browse:' + field;
         document.forms['browse'].submit();
         return false;
      }
      return true;
   }
</script>

