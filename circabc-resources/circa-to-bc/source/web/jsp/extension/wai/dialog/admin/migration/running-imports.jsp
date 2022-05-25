<%--+
    |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
    |
    |  		   http://ec.europa.eu/idabc/en/document/6523
    |
    +--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

	<%@ page isELIgnored="false"%>

  	<c:set var="currentTitle" value="${cmsg.view_running_imports_dialog_browser_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>

<circabc:displayer id="displayer-is-admin" rendered="#{NavigationBean.currentUser.admin == true}">

	<div id="maincontent">

		<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

		<%-- Content START --%>
		<div id="ContentHeader">
			<div class="ContentHeaderNavigationLibrary">
			<circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></div>
			<div>
				<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/workflow_item_large.gif" alt="#{cmsg.view_running_imports_dialog_icon_tooltip}" title="#{cmsg.view_running_imports_dialog_icon_tooltip}"></h:graphicImage></div>
				<div class="ContentHeaderText">
					<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.view_running_imports_dialog_page_title}" /></span><br />
					<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.view_running_imports_dialog_page_description}" /></span>
				</div>
			</div>
		</div>

		<div id="ContentMain">
			<div class="ContentMainButton">
				<div class="divButtonDialog">
                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{cmsg.close}" action="finish" />
				</div>
			</div>

			<circabc:panel id="running-more-action" styleClass="wai_dialog_more_action">
				<h:graphicImage value="/images/icons/ajax_anim.gif" title="#{cmsg.manage_importations_action_refresh_alt}" alt="#{cmsg.manage_importations_action_refresh_alt}" />
				<h:outputText id="import-space" value="&nbsp;" escape="false" />
				<circabc:actionLink id="running-action-load-link" value="#{cmsg.manage_importations_action_refresh_title}"  tooltip="#{cmsg.manage_importations_action_refresh_alt}" actionListener="#{RunningImportsBean.doNothing}" onclick="submit();"  />
			</circabc:panel>

			<f:verbatim><br /><br /></f:verbatim>

			<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
				<h:outputText value="#{cmsg.view_running_imports_dialog_view_report}:" escape="false" />
				<h:selectBooleanCheckbox id="print-report" value="#{RunningImportsBean.printReport}" />

				<h:outputText value="#{cmsg.view_running_imports_dialog_view_logs}:" escape="false" />
				<h:inputText id="logs-lines" value="#{RunningImportsBean.logsLinesStr}" />

				<h:outputText value="#{cmsg.view_running_imports_dialog_view_validation}:" escape="false" />
				<h:inputText id="validation-lines" value="#{RunningImportsBean.validationLinesStr}" />

				<h:commandButton id="submit-changes" value="#{cmsg.view_running_imports_dialog_apply}" actionListener="#{RunningImportsBean.doNothing}" rendered="true" title="#{cmsg.view_running_imports_dialog_apply_tooltip}" />
			</h:panelGrid>

			<f:verbatim><br /><br /></f:verbatim>


			<circabc:panel id="running-first-section" styleClass="signup_rub_title">
					<h:outputText value="#{cmsg.view_running_imports_dialog_section_1}" escape="false" />
			</circabc:panel>

			<f:verbatim><br /></f:verbatim>

			<circabc:richList id="view-running-list" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{RunningImportsBean.runningImportations}" var="s" initialSortDescending="false" initialSortColumn="type"  >

				<circabc:column id="view-running-col-processname">
					<f:facet name="header">
						<circabc:sortLink id="view-running-processname-sorter" label="#{cmsg.manage_exportations_dialog_processname}" value="iterationName" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
					</f:facet>
					<h:outputText id="view-running-processname-value" value="#{s.processName}" escape="false" />
				</circabc:column>

				<circabc:column id="view-running-col-iteration">
					<f:facet name="header">
						<circabc:sortLink id="view-running-iteration-sorter" label="#{cmsg.manage_exportations_dialog_selected_iteration}" value="iterationName" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
					</f:facet>
					<h:outputText id="view-running-iteration-value" value="#{s.iterationName}" escape="false" />
				</circabc:column>

				<circabc:column id="view-running-col-type">
					<f:facet name="header">
						<circabc:sortLink id="view-running-type-sorter" label="#{cmsg.manage_exportations_dialog_processname}" value="iterationName" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
					</f:facet>
					<h:outputText id="view-running-type-value" value="#{s.type}" escape="false" converter="eu.cec.digit.circabc.faces.EnumConverter"/>
				</circabc:column>

				<circabc:column id="view-running-col-StartDate">
					<f:facet name="header">
						<circabc:sortLink id="view-running-StartDate-sorter" label="#{cmsg.view_running_imports_dialog_startdate}" value="startDate" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
					</f:facet>
					<h:outputText id="view-running-StartDate-value" value="#{s.startDate}" escape="false" >
						<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
					</h:outputText>
				</circabc:column>

				<circabc:column id="view-running-col-currentprocess">
					<f:facet name="header">
						<circabc:sortLink id="view-running-currentprocess-sorter" label="#{cmsg.view_running_imports_dialog_currentprocess}" value="runningPhase" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
					</f:facet>
					<h:outputText id="view-running-currentprocess-value" value="#{s.runningPhase}" escape="false" />
				</circabc:column>

				<circabc:column id="Actions" >
					<f:facet name="header">
						<h:outputText value="#{cmsg.actions}" />
					</f:facet>

					<circabc:actionLink id="viewDetails" actionListener="#{RunningImportsBean.viewDetails}" onclick="showWaitProgress();"
						image="/images/icons/View_details.gif" value="#{cmsg.view_running_imports_dialog_view_details}&nbsp;#{s.iterationName}" tooltip="#{cmsg.view_running_imports_dialog_view_details}&nbsp;#{s.iterationName}" showLink="false" styleClass="pad6Left" >
						<f:param name="iteration" value="#{s.iterationName}"/>
					</circabc:actionLink>

					<f:verbatim>&nbsp;</f:verbatim>
				</circabc:column>

				<f:facet name="empty">
					<h:outputFormat id="view-running-nolist" value="#{cmsg.view_running_imports_dialog_emprty_list}" escape="false" styleClass="noItem"/>
				</f:facet>

			</circabc:richList>

			<f:verbatim><br /><br /></f:verbatim>

			<circabc:displayer id="displayer-is-selected" rendered="#{RunningImportsBean.iterationSelected}">

				<circabc:panel id="running-second-section" styleClass="signup_rub_title">
					<h:outputFormat value="#{cmsg.view_running_imports_dialog_section_2}" styleClass="adminConsoleSeparatorTitle">
						<circabc:param value="#{RunningImportsBean.selectedIteration}" />
			   		</h:outputFormat>
				</circabc:panel>

				<f:verbatim><br /></f:verbatim>

				<h:outputText value="#{RunningImportsBean.selectedIterationMessage}" escape="false" style="width:80%;height: 30px;" />
			</circabc:displayer>

		</div>
	</div>
</circabc:displayer>

<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
	<h1>Only super adminstrator can access to this page!</h1>
</circabc:displayer>

<script type="text/javascript">

    function dataTableSelectOneRadio(radio)
    {
        var id = radio.name.substring(radio.name.lastIndexOf(':'));
        var el = radio.form.elements;
        for (var i = 0; i < el.length; i++) {
            if (el[i].name.substring(el[i].name.lastIndexOf(':')) == id) {
                el[i].checked = false;
            }
        }
        radio.checked = true;
    }
</script>
</h:form>
</circabc:view>






