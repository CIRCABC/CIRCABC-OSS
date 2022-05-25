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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<h:outputText value="#{msg.no_tasks}" rendered="#{empty WorkflowBean.allActiveTasks}" />

<a:richList id="tasks-active-list" viewMode="details" value="#{CircabcWorkflowBean.categoryActiveTasks}" var="r"
styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
initialSortColumn="created" initialSortDescending="true"
rendered="#{not empty WorkflowBean.allActiveTasks}" refreshOnBind="true">

<%-- Primary column for details view mode --%>
<a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col1-sort" label="#{msg.description}" value="bpm:description" mode="case-insensitive" styleClass="header"/>
</f:facet>
<f:facet name="small-icon">
<a:actionLink id="col1-act1" value="#{r['bpm:description']}" image="/images/icons/workflow_task.gif" showLink="false"
actionListener="#{WorkflowBean.setupTaskDialog}" action="wai:dialog:circabcManageTask">
<f:param name="id" value="#{r.id}" />
<f:param name="type" value="#{r.type}" />
</a:actionLink>
</f:facet>
<a:actionLink id="col1-act2" value="#{r['bpm:description']}" actionListener="#{WorkflowBean.setupTaskDialog}"
action="wai:dialog:circabcManageTask">
<f:param name="id" value="#{r.id}" />
<f:param name="type" value="#{r.type}" />
</a:actionLink>
</a:column>

<%-- Task type --%>
<a:column id="col2" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col2-sort" label="#{msg.type}" value="name" mode="case-insensitive" styleClass="header"/>
</f:facet>
<h:outputText id="col2-txt" value="#{r.name}" />
</a:column>

<%-- Task owner column --%>
<a:column id="col2a" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col2a-sort" label="#{msg.owner}" value="cm:owner" styleClass="header"/>
</f:facet>
<h:outputText id="col2a-txt" value="#{r['cm:owner']}" />
</a:column>

<%-- Task id column --%>
<a:column id="col3" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col3-sort" label="#{msg.id}" value="bpm:taskId" styleClass="header"/>
</f:facet>
<h:outputText id="col3-txt" value="#{r['bpm:taskId']}" />
</a:column>

<%-- Created Date column --%>
<a:column id="col4" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col4-sort" label="#{msg.created}" value="created" styleClass="header"/>
</f:facet>
<h:outputText id="col4-txt" value="#{r.created}">
<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
</h:outputText>
</a:column>

<%-- Due date column --%>
<a:column id="col5" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col5-sort" label="#{msg.due_date}" value="bpm:dueDate" styleClass="header"/>
</f:facet>
<h:outputText id="col5-txt" value="#{r['bpm:dueDate']}">
<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
</h:outputText>
</a:column>

<%-- Status column --%>
<a:column id="col6" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col6-sort" label="#{msg.status}" value="bpm:status" styleClass="header"/>
</f:facet>
<h:outputText id="col6-txt" value="#{r['bpm:status']}" />
</a:column>

<%-- Priority column --%>
<a:column id="col7" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink id="col7-sort" label="#{msg.priority}" value="bpm:priority" styleClass="header"/>
</f:facet>
<h:outputText id="col7-txt" value="#{r['bpm:priority']}" />
</a:column>

<%-- Actions column --%>
<a:column id="col8" actions="true" style="padding:2px;text-align:left">
<f:facet name="header">
<h:outputText id="col8-txt" value="#{msg.actions}"/>
</f:facet>
<r:actions id="col8-actions" value="todo_actions_wai" context="#{r}" showLink="false"
styleClass="inlineAction" />
</a:column>

<a:dataPager styleClass="pager" />
</a:richList>
