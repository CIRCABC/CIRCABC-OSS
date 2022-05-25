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

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGroup rendered="#{WizardManager.bean.hasStartableWorkflows == false}">
<f:verbatim>
<%PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");%>
<table><tr><td>
</f:verbatim>
<h:graphicImage url="/images/icons/info_icon.gif" />
<f:verbatim>
</td><td>
</f:verbatim>
<h:outputText value="#{msg.start_workflow_no_workflows}" />
<f:verbatim>
</td></tr></table>
<%PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");%>
</f:verbatim>
</h:panelGroup>

<h:panelGrid columns="1" styleClass="workflowSelection" rendered="#{WizardManager.bean.hasStartableWorkflows}">
<h:outputText value="#{msg.available_workflows}:"/>
<h:selectOneRadio id="selected-workflow" value="#{WizardManager.bean.selectedWorkflow}"
layout="pageDirection">
<f:selectItems value="#{WizardManager.bean.startableWorkflows}" />
</h:selectOneRadio>
</h:panelGrid>

