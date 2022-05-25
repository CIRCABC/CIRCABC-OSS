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

<r:page titleId="title_survey">

<f:view>
<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<h:form acceptcharset="UTF-8" id="encode-survey">

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
<td bgcolor="##dfe6ed">

<%-- Status and Actions inner contents table --%>
<%-- Generally this consists of an icon, textual summary and actions for the current object --%>
<table cellspacing=4 cellpadding=0 width=100%>
<tr>

<%-- actions for the survey --%>
<a:panel id="survey-actions">
<td width=32>
<h:graphicImage id="space-logo" url="/images/icons/space-icon-pen.gif" width="32" height="32" />
</td>
<td>
<%-- Summary --%>
<div class="mainTitle"><h:outputText value="#{cmsg.survey_encode_title}" id="msg2" /></div>
</td>
<td style="padding-right:4px" width="100" align="right">
<circabc:actionLink tooltip="#{msg.close}" value="#{msg.close}" action="close" actionListener="#{SurveysBean.closeSurvey}" />

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

</h:form>

<%-- Survey to encode --%>
<circabc:encode value="#{SurveysBean.url}" survey="#{SurveysBean.survey}" lang="#{SurveysBean.lang}" />
</td>
<td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width=4>
</td>
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
<%-- restore alfresco css --%>
<link rel="stylesheet" href="<%=request.getContextPath()%>/css/main.css" type="text/css" />
</f:view>

</r:page>
