<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or – as soon they
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
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<h:outputText value="#{msg.no_tasks}" rendered="#{empty CircabcWorkflowBean.tasksToDo}" />



<circabc:richList id="tasks-todo-list" viewMode="circa" value="#{CircabcWorkflowBean.tasksToDo}" var="r"
styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
altRowStyleClass="recordSetRowAlt"  pageSize="10"
initialSortColumn="created" initialSortDescending="true" 
rendered="#{not empty CircabcWorkflowBean.tasksToDo}"  refreshOnBind="true">

<%-- Primary column for details view mode --%>
<circabc:column id="col1" primary="true" >
<f:facet name="header">
<circabc:sortLink id="col1-sort"  label="#{msg.description}" value="bpm:description" mode="case-insensitive" styleClass="header" tooltipAscending="" tooltipDescending="" />
</f:facet>
<f:facet name="small-icon">
<circabc:actionLink id="col1-act1" value="#{r['bpm:description']}" image="/images/icons/workflow_task.gif" showLink="false"
actionListener="#{CircabcWorkflowBean.setupTaskDialog}" action="wai:dialog:circabcManageTask" tooltip="" >
<f:param name="id" value="#{r.id}" />
<f:param name="type" value="#{r.type}" />
</circabc:actionLink>
</f:facet>
<circabc:actionLink id="col1-act2" value="#{r['bpm:description']}" actionListener="#{CircabcWorkflowBean.setupTaskDialog}"
action="wai:dialog:circabcManageTask" tooltip=""  >
<f:param name="id" value="#{r.id}" />
<f:param name="type" value="#{r.type}" />
</circabc:actionLink>
</circabc:column>

<%-- Task type --%>
<circabc:column id="col2" >
<f:facet name="header">
<circabc:sortLink id="col2-sort"  label="#{msg.type}" value="name" mode="case-insensitive" styleClass="header" tooltipAscending="" tooltipDescending=""/>
</f:facet>
<h:outputText id="col2-txt" value="#{r.name}" />
</circabc:column>

<%-- Task id column --%>
<circabc:column id="col3" >
<f:facet name="header">
<circabc:sortLink id="col3-sort" label="#{msg.id}" value="bpm:taskId" styleClass="header" tooltipAscending="" tooltipDescending="" />
</f:facet>
<h:outputText id="col3-txt" value="#{r['bpm:taskId']}" />
</circabc:column>

<%-- Created Date column --%>
<circabc:column id="col4" >
<f:facet name="header">
<circabc:sortLink id="col4-sort" label="#{msg.created}" value="created" styleClass="header" tooltipAscending=" " tooltipDescending=""/>
</f:facet>
<h:outputText id="col4-txt" value="#{r.created}">
<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
</h:outputText>
</circabc:column>

<%-- Due date column --%>
<circabc:column id="col5" >
<f:facet name="header">
<circabc:sortLink id="col5-sort" label="#{msg.due_date}" value="bpm:dueDate" styleClass="header" tooltipAscending=" " tooltipDescending="" />
</f:facet>
<h:outputText id="col5-txt" value="#{r['bpm:dueDate']}">
<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
</h:outputText>
</circabc:column>

<%-- Status column --%>
<circabc:column id="col6" >
<f:facet name="header">
<circabc:sortLink id="col6-sort" label="#{msg.status}" value="bpm:status" styleClass="header" tooltipAscending=" " tooltipDescending="" />
</f:facet>
<h:outputText id="col6-txt" value="#{r['bpm:status']}" />
</circabc:column>

<%-- Priority column --%>
<circabc:column id="col7" >
<f:facet name="header">
<circabc:sortLink id="col7-sort" label="#{msg.priority}" value="bpm:priority" styleClass="header" tooltipAscending=" " tooltipDescending="" />
</f:facet>
<h:outputText id="col7-txt" value="#{r['bpm:priority']}" />
</circabc:column>

<%-- Actions column --%>
<circabc:column id="col8" actions="true" >
<f:facet name="header">
<h:outputText id="col8-txt" value="#{msg.actions}"/>
</f:facet>
<circabc:actions id="col8-actions" value="todo_actions_wai" context="#{r}" showLink="false"
styleClass="inlineAction" />
</circabc:column>

<circabc:dataPager styleClass="pager" />
</circabc:richList>
