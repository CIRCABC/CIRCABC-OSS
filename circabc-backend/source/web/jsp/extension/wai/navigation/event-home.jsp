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

<%@ taglib uri="http://comin.cz/tag/lib/jscalendar" prefix="jsc"%>

<%@ page isELIgnored="false"%>

<script language="javascript">
    function updateCalendar(){
        document.getElementById("FormPrincipal:apply").click();
    }
</script>


<circabc:panel id="contentMainEventHome" styleClass="contentMain">

		<%-- css For jscalendar popup --%>
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/jscalendar-1.0/calendar-blue.css" type="text/css" />
		<%-- css For jsf calendar --%>
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/calendartag/bigcalendar.css" type="text/css" />
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/calendartag/calendar.css" type="text/css" />
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/calendartag/minicalendar.css" type="text/css" />
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/calendartag/verticlecalendar.css" type="text/css" />

		<circabc:panel id="centerCalendar" styleClass="applicationFormCenter">

				<h:inputText id="date" value="#{EventBean.startDateAsString}"  title="#{cmsg.event_home_input_date_title}" size="8" />
 				<f:verbatim>&nbsp;</f:verbatim>
 			    <jsc:jscalendar for="FormPrincipal:date"
							showsTime="false"
							ifFormat="%d-%m-%Y"
							locale="#{EventBean.userLang}"
							icon="#{EventBean.popupIcon}"
 			    			title="#{cmsg.event_home_popup_button_title}"/>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:selectOneMenu id="camendar-view-mode-language" value="#{EventBean.viewMode}" title="#{cmsg.event_home_input_mode_title}" >
					<f:selectItems value="#{EventBean.viewModes}"/>
				</h:selectOneMenu>
				<f:verbatim>&nbsp;</f:verbatim>
				<h:commandButton id="apply" actionListener="#{BrowseBean.clickWai}" value="#{cmsg.event_home_popup_action_apply}" title="#{cmsg.event_home_popup_action_title}" onclick="showWaitProgress();">
					<f:param name="id" value="#{NavigationBean.currentNode.id}" />
					<f:param name="browseDate" value="#{EventBean.startDateAsString}" />
					<f:param name="viewMode" value="#{EventBean.viewMode}" />
				</h:commandButton>


			<circabc:calendar date="#{EventBean.startDate}" weekStart="#{EventBean.weekStart}" decorator="eu.cec.digit.circabc.web.wai.bean.navigation.event.EventServiceDecorator" showPreviousNextLinks="true" beyond="true" actionListener="#{BrowseBean.clickWai}" viewMode="#{EventBean.viewMode}"  />
		</circabc:panel>

		<%-- Workaround to avoid a css conflict --%>
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/d-commission.css" type="text/css" />
		<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/circabc.css" type="text/css" />
	</circabc:panel>

