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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>


<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_system_info">

<f:view>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
<f:loadBundle basename="alfresco.version" var="version"/>
<%-- <f:loadBundle basename="alfresco.extension.build-config" var="config"/> --%>
<f:loadBundle basename="alfresco-global" var="config"/>

<h:form acceptcharset="UTF-8" id="system-information-form">

<%-- Main outer table --%>
<table cellspacing="0" cellpadding="2">

<%-- Title bar --%>
<tr>
<td colspan="2">
<%@ include file="../../parts/titlebar.jsp" %>
</td>
</tr>

<%-- Main area --%>
<tr valign="top">
<%-- Shelf --%>
<td>
<%@ include file="../../parts/shelf.jsp" %>
</td>

<%-- Work Area --%>
<td width="100%">
<table cellspacing="0" cellpadding="0" width="100%">
<%-- Breadcrumb --%>
<%@ include file="../../parts/breadcrumb.jsp" %>

<%-- Status and Actions --%>
<tr>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
<td bgcolor="#dfe6ed">

<%-- Status and Actions inner contents table --%>
<%-- Generally this consists of an icon, textual summary and actions for the current object --%>
<table cellspacing="4" cellpadding="0" width="100%">
<tr>
<td width="32">
<h:graphicImage id="wizard-logo" url="/images/icons/file_large.gif" />
</td>
<td>
<div class="mainTitle"><h:outputText value="#{msg.system_info}" /></div>
<div class="mainSubTitle"><h:outputText value="#{msg.current_user}" />: <h:outputText value="#{NavigationBean.currentUser.userName}" /></div>
<div class="mainSubText"><h:outputText value="#{msg.version}" />: <h:outputText value='#{version["version.edition"]} - v#{version["version.major"]}.#{version["version.minor"]}.#{version["version.revision"]}' /> <h:outputText rendered='#{version["version.label"] != ""}' value='(#{version["version.label"]})' /></div>
</td>
</tr>
</table>

</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width="4"></td>
</tr>

<%-- separator row with gradient shadow --%>
<tr>
<td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9"></td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
<td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9"></td>
</tr>

<%-- Details --%>
<tr valign=top>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
<td>
<table cellspacing="0" cellpadding="3" border="0" width="100%">
<tr>
<td width="100%" valign="top">
<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
<tr>
<td colspan="2">

<circabc:displayer id="displayer-is-admin" rendered="#{NavigationBean.currentUser.admin}">
	<a:panel label="#{cmsg.system_info_extended_panel_deployment}"
		id="circabc-build-config-info" border="white"
		bgcolor="white" titleBorder="lbgrey"
		expandedTitleBorder="dotted" titleBgcolor="white"
		progressive="true" styleClass="mainSubTitle" expanded="true">

		<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">

			<a:outputText id="build_config_label" value="#{cmsg.system_info_extended_panel_deployment_task}:"/>
			<a:outputText id="build_config_value" value='#{config["build.configuration.task"]}'/>

			<a:outputText id="build_desc_label" value="#{cmsg.system_info_extended_panel_deployment_description}:"/>
			<a:outputText id="build_desc_value" value='#{config["build.configuration.description"]}'/>

			<a:outputText id="space1" value="**********************************"/>
			<a:outputText id="space2" value="**********************************"/>

			<a:outputText id="request_contextPath_label" value="request.contextPath:"/>
			<a:outputText id="request_contextPath_value" value="<%=request.getContextPath()%>"/>

			<a:outputText id="request_localAddr_label" value="request.localAddr:"/>
			<a:outputText id="request_localAddr_value" value="<%=request.getLocalAddr() + ':' + request.getLocalPort() %>"/>

			<a:outputText id="request_localName_label" value="request.localName:"/>
			<a:outputText id="request_localName_value" value="<%=request.getLocalName()%>"/>

			<a:outputText id="request_requestURL_label" value="request.requestURL:"/>
			<a:outputText id="request_requestURL_value" value="<%=request.getRequestURL().toString()%>"/>

			<a:outputText id="request_fullurl_label" value="request.header.full-url (if LB request):"/>
			<a:outputText id="request_fullurl_value" value='<%=request.getHeader("full-url")%>'/>

		</h:panelGrid>
	</a:panel>
	<br/>

	<a:panel label="#{cmsg.system_info_extended_panel_config_files}" id="circabc-configuration-files" border="white" bgcolor="white"
			 titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
			 expanded="false">
		<circabc:configFileReader id="circacfr" />
	</a:panel>
	<br/>

	<a:panel label="#{cmsg.system_info_extended_panel_config_from_beans}" id="circabc-configuration" border="white" bgcolor="white"
			 titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
			 expanded="false">
		<circabc:serverConfiguration id="circasc" />
	</a:panel>
	<br/>


	<a:panel label="#{cmsg.system_info_extended_panel_statistics}" id="circabc-statistics" border="white" bgcolor="white"
			 titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
			 expanded="false">
		<circabc:circabcStatistics id="circacs" />
	</a:panel>
	<br/>

	<a:panel label="#{cmsg.system_info_extended_panel_triggers}" id="circabc-triggers" border="white" bgcolor="white"
			 titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
			 expanded="false">
		<circabc:schedulerReporter id="circatrig" />
	</a:panel>
	<br/>

	<a:panel label="#{cmsg.system_info_extended_panel_memory}" id="circabc-memory" border="white" bgcolor="white"
			 titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
			 expanded="false">
		<circabc:memoryReporter id="circamemor" />
	</a:panel>
	<br/>
</circabc:displayer>
</td>
</tr>
</table>
<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
</td>

<td valign="top">
<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
<table cellpadding="1" cellspacing="1" border="0">
<tr>
<td align="center">
<h:commandButton value="#{msg.close}" action="dialog:close" styleClass="wizardButton" />
</td>
</tr>
</table>
<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
</td>
</tr>
</table>
</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
</tr>

<%-- Error Messages --%>
<tr valign="top">
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
<td>
<%-- messages tag to show messages not handled by other specific message tags --%>
<h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
</tr>

<%-- separator row with bottom panel graphics --%>
<tr>
<td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4"></td>
<td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
<td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4"></td>
</tr>

</table>
</td>
</tr>
</table>

</h:form>

</f:view>

</r:page>
