<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? – as soon they
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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />


<script language="JavaScript1.2" type="text/javascript">

window.onload = pageLoaded;

function pageLoaded()
{
document.getElementById("trashcan:search-text").focus();
updateButtonState();
}

function updateButtonState()
{
if (document.getElementById("trashcan:search-text").value.length == 0)
{
document.getElementById("trashcan:search-btn1").disabled = true;
document.getElementById("trashcan:search-btn2").disabled = true;
}
else
{
document.getElementById("trashcan:search-btn1").disabled = false;
document.getElementById("trashcan:search-btn2").disabled = false;
}
}

function userSearch(e)
{
var keycode;
if (window.event) keycode = window.event.keyCode;
else if (e) keycode = e.which;
if (keycode == 13)
{
document.forms['trashcan']['trashcan:modelist'].value='trashcan:user-filter:user';
document.forms['trashcan'].submit();
return false;
}
return true;
}
</script>
<circabc:panel id="contentMainForm" styleClass="contentMainForm">
	<circabc:panel id="panelTrashCan">
		<circabc:panel id="trashcan-info" styleClass="infoPanel" styleClassLabel="infoContent" >
			<h:graphicImage id="manage-applicants-image-info" value="/images/icons/info_icon.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
			<h:outputText id="manage-applicants-text-info-spaces" value="&nbsp;&nbsp;" escape="false" />
			<h:outputText id="information" value="#{cmsg.menage_deleted_items_information}" />
		</circabc:panel>
		<circabc:panel id="panelTrashCanSearch">
		<h:inputText id="search-text"
			value="#{CircabcTrashcanDialogProperty.searchText}" size="35"
			maxlength="1024" onkeyup="updateButtonState();"
			onchange="updateButtonState();" />
		<h:commandButton id="search-btn1"
			value="#{msg.search_deleted_items_name}"
			actionListener="#{WaiDialogManager.bean.searchName}" disabled="false" />
		<h:commandButton id="search-btn2"
			value="#{msg.search_deleted_items_text}"
			actionListener="#{WaiDialogManager.bean.searchContent}"
			disabled="false" />
		<h:commandButton id="clear-btn" value="#{msg.show_all}"
			actionListener="#{WaiDialogManager.bean.clearSearch}" />
		</circabc:panel>
		<circabc:panel id="panelTrashCanFilter">
		<h:graphicImage id="image-filter-when" url="/images/icons/filter.gif"
			alt="" width="16" height="16" />
		<h:outputText value="#{msg.date_filter_when}" />
	<a:modeList itemSpacing="2" iconColumnWidth="0" horizontal="true"
			selectedLinkStyle="font-weight:bold"
			value="#{CircabcTrashcanDialogProperty.dateFilter}"
			actionListener="#{WaiDialogManager.bean.dateFilterChanged}">
			<a:listItem value="all" label="#{msg.date_filter_all}" />
			<a:listItem value="today" label="#{msg.date_filter_today}" />
			<a:listItem value="week" label="#{msg.date_filter_week}" />
			<a:listItem value="month" label="#{msg.date_filter_month}" />
		</a:modeList>
		</circabc:panel>


		<circabc:panel id="panelTrashCanSelected">
		<%-- Recover Listed Items actions --%>
		<a:actionLink value="#{msg.recover_listed_items}"
			image="/images/icons/recover_all.gif"
			action="wai:dialog:circabcRecoverListedItems"
			actionListener="#{CircabcTrashcanRecoverListedItemsDialog.setupListAction}" />
		<a:actionLink value="#{msg.delete_listed_items}"
			image="/images/icons/delete_all.gif"
			action="wai:dialog:circabcDeleteListedItems"
			actionListener="#{CircabcTrashcanDeleteListedItemsDialog.setupListAction}" />
		</circabc:panel>

		<circabc:richList id="trashcan-list"
			binding="#{CircabcTrashcanDialogProperty.itemsRichList}" viewMode="circa"
			pageSize="#{BrowseBean.listElementNumber}" styleClass="recordSet"
			headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
			altRowStyleClass="recordSetRowAlt"
			value="#{WaiDialogManager.bean.items}" var="r"
			initialSortColumn="deletedDate" initialSortDescending="true">

			<%-- Primary column showing item name --%>
			<circabc:column id="column-item-name" primary="true">
				<f:facet name="header">
					<circabc:sortLink id="header-sort-link-name" label="#{msg.name}"
						value="name" mode="case-insensitive" styleClass="header" tooltipAscending="" tooltipDescending="" />
				</f:facet>
				<f:facet name="small-icon">
					<circabc:actionLink id="small-icon-sort-link-name" value="#{r.name}"
						image="#{r.typeIcon}" showLink="false" styleClass="inlineAction" tooltip="">
						<f:param name="id" value="#{r.id}" />
					</circabc:actionLink>
				</f:facet>
				<circabc:actionLink id="action-link-name" value="#{r.name}" tooltip="">
					<f:param name="id" value="#{r.id}" />
				</circabc:actionLink>
			</circabc:column>
			<%-- Original Location Path column --%>
			<circabc:column id="column-location-path" >
				<f:facet name="header">
					<circabc:sortLink id="sort-link-orirignal-location"
						label="#{msg.original_location}" value="displayPath"
						styleClass="header" tooltipAscending="" tooltipDescending=""  />
				</f:facet>
				<r:nodePath value="#{r.locationPath}" showLeaf="true" />
			</circabc:column>

			<%-- Deleted Date column --%>
			<circabc:column id="column-deleted-date" >
				<f:facet name="header">
					<circabc:sortLink id="sort-link-deleted-date" label="#{msg.deleted_date}"
						value="deletedDate" styleClass="header"
						tooltipAscending="" tooltipDescending="" />
				</f:facet>
				<h:outputText id="text-deleted-date" value="#{r.deletedDate}">
					<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
				</h:outputText>
			</circabc:column>

			<%-- Deleted by user column --%>
			<circabc:column id="column-user" >
				<f:facet name="header">
					<circabc:sortLink id="sort-link-deleted-by" label="#{msg.deleted_user}"
						value="deletedBy" styleClass="header" tooltipAscending="" tooltipDescending=""  />
				</f:facet>
				<h:outputText id="text-deleted-by" value="#{r.deletedBy}" />
			</circabc:column>

			<%-- Actions column --%>
			<circabc:column id="column-actions" actions="true" >
				<f:facet name="header">
					<h:outputText id="text-actiopns" value="#{msg.actions}" />
				</f:facet>
				<circabc:actionLink id="action-link-recover-item" value="#{msg.recover}"
					image="/images/icons/recover.gif" showLink="false"
					action="wai:dialog:circabcRecoverItem"
					actionListener="#{CircabcTrashcanRecoverItemDialog.setupItemAction}" tooltip="" >
					<f:param name="id" value="#{r.id}" />
					<circabc:param id="param-recover-item-imageName" name="imageName" value="recover_#{r.name}" />
					<circabc:param id="param-recover-item-service" name="service" value="Administration" />
					<circabc:param id="param-recover-item-activity" name="activity" value="Recover Item" />
				</circabc:actionLink>
				<circabc:actionLink id="action-link-delete-item" value="#{msg.delete}"
					image="/images/icons/delete.gif" showLink="false"
					action="wai:dialog:circabcDeleteItem"
					actionListener="#{CircabcTrashcanDeleteItemDialog.setupItemAction}" tooltip="" >
					<f:param name="id" value="#{r.id}" />
					<circabc:param id="param-delete-item-imageName" name="imageName" value="delete_#{r.name}" />
				</circabc:actionLink>
			</circabc:column>


			<circabc:dataPager id="trash-list-pager" styleClass="pager" />
		</circabc:richList>
		<h:message for="trashcan-list" styleClass="statusMessage" />
	</circabc:panel>
</circabc:panel>
