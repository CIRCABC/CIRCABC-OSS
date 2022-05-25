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
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc" %>


<%@ page isELIgnored="false" %>

<r:page titleId="title_surveys">

<circabc:view>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<h:form acceptcharset="UTF-8" id="browse-surveys">

<%-- Main outer table --%>
<table cellspacing=0 cellpadding=2>

<%-- Title bar --%>
<tr>
<td colspan=2>
<%@ include file="../../parts/titlebar.jsp" %>
</td>
</tr>

<%-- Main area --%>
<tr valign=top>
<%-- Shelf --%>
<td>
<%@ include file="../../parts/shelf.jsp" %>
</td>

<%-- Work Area --%>
<td width=100%>
<table cellspacing=0 cellpadding=0 width=100%>
<%-- Breadcrumb --%>
<%@ include file="../../parts/breadcrumb.jsp" %>

<%-- Status and Actions --%>
<tr>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width=4></td>
<td bgcolor="#dfe6ed">

<%-- Status and Actions inner contents table --%>
<%-- Generally this consists of an icon, textual summary and actions for the current object --%>
<table cellspacing=4 cellpadding=0 width=100%>
<tr>

<%-- actions for surveys --%>
<a:panel id="surveys-actions">
<td width=32>
<h:graphicImage id="space-logo" url="/images/icons/space-icon-pen.gif" width="32" height="32" />
</td>
<td>
<%-- Summary --%>
<div class="mainTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" id="msg2" /></div>
<div class="mainSubText"><h:outputText value="#{cmsg.surveys_info}" id="msg3" /></div>
<div class="mainSubText"><h:outputText value="#{NavigationBean.nodeProperties.description}" id="msg4" /></div>
</td>
<td style="padding-left:4px" width=100>
<%-- Create actions menu --%>
<a:menu id="createMenu" itemSpacing="4" label="#{msg.create_options}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
<r:actions id="acts_create" value="surveys_create_menu" context="#{NavigationBean.currentNode}" />
</a:menu>
</td>
</a:panel>
</tr>
</table>

</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width=4></td>
</tr>

<%-- separator row with gradient shadow --%>
<tr>
<td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width=4 height=9></td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
<td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width=4 height=9></td>
</tr>

<%-- Details - Spaces --%>
<tr valign=top>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
<td style="padding:4px">

<%-- wrapper comment used by the panel to add additional component facets --%>
<h:panelGroup id="spaces-panel-facets">
<f:facet name="title">
<a:panel id="page-controls1" style="font-size:9px">
<h:outputText value="#{msg.items_per_page}" id="items-txt1"/>
<h:inputText id="spaces-pages" value="#{BrowseBean.pageSizeSpacesStr}" style="width:24px;margin-left:4px" maxlength="3" onkeyup="return applySizeSpaces(event);" />
<div style="display:none"><circabc:actionLink id="spaces-apply" tooltip="apply" value="" actionListener="#{BrowseBean.updateSpacesPageSize}" /></div>
</a:panel>
</f:facet>
</h:panelGroup>
<a:panel id="spaces-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle"
label="#{msg.browse_spaces}" progressive="true" facetsId="spaces-panel-facets">

<%-- Spaces List --%>
<a:richList id="spacesList" binding="#{BrowseBean.spacesRichList}" viewMode="#{BrowseBean.browseViewMode}" pageSize="#{BrowseBean.listElementNumber}"
styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
value="#{BrowseBean.nodes}" var="r">

<%-- component to display if the list is empty --%>
<f:facet name="empty">
<%-- TODO: either build complete message in BrowseBean or have no icon... --%>
<h:outputFormat id="no-space-items" value="#{msg.no_space_items}" rendered="#{NavigationBean.searchContext == null}">
<circabc:param value="#{msg.create_space}" />
</h:outputFormat>
</f:facet>

<%-- Primary column for details view mode --%>
<a:column id="col1" primary="true" width="200" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
<f:facet name="header">
<a:sortLink id="col1-sort" label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header"/>
</f:facet>
<f:facet name="small-icon">
<circabc:actionLink id="col1-act1" tooltip="#{r.name}" value="#{r.name}" image="/images/icons/#{r.smallIcon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
<circabc:param name="id" value="#{r.id}" />
</circabc:actionLink>
</f:facet>
<circabc:actionLink id="col1-act2" tooltip="#{r.name}" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}">
<circabc:param name="id" value="#{r.id}" />
</circabc:actionLink>
</a:column>

<%-- Primary column for icons view mode --%>
<a:column id="col2" primary="true" style="padding:2px;text-align:left;vertical-align:top;" rendered="#{BrowseBean.browseViewMode == 'icons'}">
<f:facet name="large-icon">
<circabc:actionLink id="col2-act1" tooltip="#{r.name}" value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
<circabc:param name="id" id="col2-act1-param1" value="#{r.id}" />
</circabc:actionLink>
</f:facet>
<circabc:actionLink id="col2-act2" tooltip="#{r.name}" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="header">
<circabc:param name="id" id="col2-act1-param2" value="#{r.id}" />
</circabc:actionLink>
</a:column>

<%-- Primary column for list view mode --%>
<a:column id="col3" primary="true" style="padding:2px;text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
<f:facet name="large-icon">
<circabc:actionLink id="col3-act1" tooltip="#{r.name}" value="#{r.name}" image="/images/icons/#{r.icon}.gif" actionListener="#{BrowseBean.clickSpace}" showLink="false">
<circabc:param name="id" value="#{r.id}" id="col3-act1-param1" />
</circabc:actionLink>
</f:facet>
<circabc:actionLink id="col3-act2" tooltip="#{r.name}" value="#{r.name}" actionListener="#{BrowseBean.clickSpace}" styleClass="title">
<circabc:param name="id" value="#{r.id}" />
</circabc:actionLink>
</a:column>

<%-- Description column for all view modes --%>
<a:column id="col4" style="text-align:left">
<f:facet name="header">
<a:sortLink id="col4-sort" label="#{msg.description}" value="description" styleClass="header"/>
</f:facet>
<h:outputText id="col4-txt" value="#{r.description}" />
</a:column>

<%-- Path column for search mode in details view mode --%>
<a:column id="col5" style="text-align:left" rendered="#{NavigationBean.searchContext != null && BrowseBean.browseViewMode == 'details'}">
<f:facet name="header">
<a:sortLink id="col5-sort" label="#{msg.path}" value="displayPath" styleClass="header"/>
</f:facet>
<r:nodePath id="col5-path" value="#{r.path}" actionListener="#{BrowseBean.clickSpacePath}" />
</a:column>

<%-- Created Date column for details view mode --%>
<a:column id="col6" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details'}">
<f:facet name="header">
<a:sortLink id="col6-sort" label="#{msg.created}" value="created" styleClass="header"/>
</f:facet>
<h:outputText id="col6-txt" value="#{r.created}">
<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
</h:outputText>
</a:column>

<%-- Modified Date column for details/icons view modes --%>
<a:column id="col7" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'details' || BrowseBean.browseViewMode == 'icons'}">
<f:facet name="header">
<a:sortLink id="col7-sort" label="#{msg.modified}" value="modified" styleClass="header"/>
</f:facet>
<h:outputText id="col7-txt" value="#{r.modified}">
<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
</h:outputText>
</a:column>

<%-- Node Descendants links for list view mode --%>
<a:column id="col8" style="text-align:left" rendered="#{BrowseBean.browseViewMode == 'list'}">
<r:nodeDescendants id="col8-kids" value="#{r.nodeRef}" styleClass="header" actionListener="#{BrowseBean.clickDescendantSpace}" />
</a:column>

<%-- Space Actions column --%>
<a:column id="col9" actions="true" style="text-align:left">
<f:facet name="header">
<h:outputText id="col9-txt" value="#{msg.actions}"/>
</f:facet>

<%-- actions are configured in web-client-config-actions.xml --%>
<r:actions id="col9-acts1" value="space_browse" context="#{r}" showLink="false" styleClass="inlineAction" />

<%-- More actions menu --%>
<a:menu id="spaces-more-menu" itemSpacing="4" image="/images/icons/more.gif" tooltip="#{msg.more_actions}" menuStyleClass="moreActionsMenu">
<r:actions id="col9-acts2" value="space_browse_menu" context="#{r}" />
</a:menu>
</a:column>

<a:dataPager id="pager1" styleClass="pager" />
</a:richList>

</a:panel>

</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
</tr>

<%-- Details - Surveys --%>
<tr valign=top>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
<td style="padding:4px">
<a:panel id="surveys-panel" border="white" bgcolor="white" titleBorder="blue" titleBgcolor="#D3E6FE" styleClass="mainSubTitle" label="#{cmsg.browse_surveys}">

<%-- Surveys List --%>
<a:richList id="surveysList" binding="#{SurveysBean.surveysRichList}" viewMode="details"
styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%"
value="#{SurveysBean.surveys}" var="r">

<%-- component to display if the list is empty --%>
<f:facet name="empty">
<h:outputFormat id="message-empty-surveys" value="#{cmsg.survey_no_survey}"/>
</f:facet>

<%-- Primary column  --%>
<a:column primary="true" width="200" style="padding:2px;text-align:left">
<f:facet name="header">
<a:sortLink label="#{cmsg.survey_questionnaire}" value="name" mode="case-insensitive" styleClass="header"/>
</f:facet>
<f:facet name="small-icon">
	<h:graphicImage title="#{r.name}" url="/images/filetypes/_default.gif" />
</f:facet>
<h:outputText value="#{r.name}" />
</a:column>

<%-- Subject column  --%>
<a:column style="text-align:left">
<f:facet name="header">
<a:sortLink label="#{cmsg.survey_subject}" value="subject" styleClass="header"/>
</f:facet>
<h:outputText value="#{r.subject}" />
</a:column>

<%-- Status column  --%>
<a:column style="text-align:left" >
<f:facet name="header">
<a:sortLink label="#{cmsg.survey_status}" value="status" styleClass="header"/>
</f:facet>
<h:outputText value="#{r.status}" />
</a:column>

<%-- Start Date column  --%>
<a:column style="text-align:left" >
<f:facet name="header">
<a:sortLink label="#{cmsg.survey_start_date}" value="startDate" styleClass="header"/>
</f:facet>
<h:outputText value="#{r.startDate}">
<a:convertXMLDate type="date" pattern="#{msg.date_pattern}" />
</h:outputText>
</a:column>

<%-- Close Date column --%>
<a:column style="text-align:left" >
<f:facet name="header">
<a:sortLink label="#{cmsg.survey_close_date}" value="closeDate" styleClass="header"/>
</f:facet>
<h:outputText value="#{r.closeDate}">
<a:convertXMLDate type="date" pattern="#{msg.date_pattern}" />
</h:outputText>
</a:column>

<%-- Translations column --%>
<a:column style="text-align:left">
<f:facet name="header">
<h:outputText value="#{cmsg.survey_translations}"/>
</f:facet>
<a:menu image="/images/icons/search_controls.gif" style="white-space:nowrap" menuStyle="background-color:#eeeeee;border-top:thin solid #FFFFFF;border-left:thin solid #FFFFFF;border-right:thin solid #444444;border-bottom:thin solid #444444;width: 2em;">
	<circabc:surveyLangs value="#{r}" wai="false" />
</a:menu>
</a:column>

<a:dataPager styleClass="pager" />
</a:richList>

</a:panel>

</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
</tr>

<%-- Error Messages --%>
<tr valign=top>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width=4></td>
<td>
<%-- messages tag to show messages not handled by other specific message tags --%>
<h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4></td>
</tr>

<%-- separator row with bottom panel graphics --%>
<tr>
<td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width=4 height=4></td>
<td width=100% align=center style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
<td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width=4 height=4></td>
</tr>

</table>
</td>
</tr>
</table>

</h:form>
</circabc:view>

</r:page>
