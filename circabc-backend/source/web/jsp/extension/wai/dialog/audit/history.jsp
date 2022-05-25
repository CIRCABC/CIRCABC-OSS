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

<f:verbatim>
	<br />
</f:verbatim>

<circabc:panel id="history-panel-label" styleClass="signup_rub_title" tooltip="#{cmsg.history_description}">
	<h:outputText value="#{cmsg.history}"/>
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<%-- Audit list --%>
<a:richList id="auditRichList" viewMode="details"
	pageSize="#{BrowseBean.listElementNumber}" styleClass="recordSet"
	headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
	altRowStyleClass="recordSetRowAlt" width="100%"
	value="#{DialogManager.bean.auditList}" var="r"
	rendered="#{DialogManager.bean.auditList != null}"
	initialSortColumn="logDate" initialSortDescending="true">

	<%-- Component to display if the list is empty --%>
	<f:facet name="empty">
		<h:outputText
			value="<i>#{cmsg.no_audit_entry_found}</i><br /><br /><br />"
			escape="false" />
	</f:facet>
	<%-- Timestamp column --%>
	<a:column id="audit-date"
		style="text-align:left; white-space:nowrap; padding-right:5px">
		<f:facet name="header">
			<h:outputText id="audit-date-label" value="#{msg.date}"
				styleClass="header" />
		</f:facet>
		<h:outputText id="audit-date-txt" value="#{r.logDate}">
			<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
		</h:outputText>
	</a:column>

	<%-- User name column --%>
	<a:column id="audit-username"
		style="text-align:left; padding-right:5px">
		<f:facet name="header">
			<h:outputText id="audit-username-label" value="#{msg.username}"
				styleClass="header" />
		</f:facet>
		<h:outputText id="audit-username-txt" value="#{r.userName}" />
	</a:column>

	<%-- Service column --%>
	<a:column id="audit-service"
		style="text-align:left; padding-right:5px">
		<f:facet name="header">
			<h:outputText id="audit-service-label" value="#{cmsg.service}"
				styleClass="header" />
		</f:facet>
		<h:outputText id="audit-service-txt" value="#{r.serviceDescription}" />
	</a:column>

	<%-- Activity column --%>
	<a:column id="audit-method"
		style="text-align:left; padding-right:5px">
		<f:facet name="header">
			<h:outputText id="audit-method-label" value="#{cmsg.activity}"
				styleClass="header" />
		</f:facet>
		<h:outputText id="audit-method-txt" value="#{r.activityDescription}" />
	</a:column>

	<%-- Failed column --%>
	<a:column id="audit-status"
		style="text-align:left; padding-right:5px">
		<f:facet name="header">
			<h:outputText id="audit-status-label" value="#{msg.status}"
				styleClass="header" />
		</f:facet>
		<h:outputText id="audit-status-txt" value="#{r.status}" />

	</a:column>
	<circabc:dataPager id="history-pager" styleClass="pagerCirca" />
</a:richList>
