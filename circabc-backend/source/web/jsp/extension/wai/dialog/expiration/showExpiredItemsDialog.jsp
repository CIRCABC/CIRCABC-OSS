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
		document.getElementById("FormPrincipal:txbxSearch").focus();
		updateSearchButtonState();
	}
	
	// Disable the search buttons if there is not text in the search box
	function updateSearchButtonState()
	{
		var txbxSearch = document.getElementById("FormPrincipal:txbxSearch");
		var btnSearchByName = document.getElementById("FormPrincipal:btnSearchByName");
		var btnSearchByText = document.getElementById("FormPrincipal:btnSearchByText");
		
		if(txbxSearch.value.length == 0)
		{
			btnSearchByName.disabled = true;
			btnSearchByText.disabled = true;
		}
		else
		{
			btnSearchByName.disabled = false;
			btnSearchByText.disabled = false;
		}
	}
</script>
<circabc:panel id="contentMainForm" styleClass="contentMainForm">
	<circabc:panel id="panelExpirationList">
	
		<%--
		<circabc:panel id="panelExpirationListInfo" styleClass="infoPanel" styleClassLabel="infoContent" rendered="#{WaiDialogManager.bean.message}" >
			<h:graphicImage id="manage-applicants-image-info" value="/images/icons/info_icon.gif"  />
			<h:outputText id="manage-applicants-text-info-spaces" value="&nbsp;&nbsp;" escape="false" />
			<h:outputText id="information" value="#{WaiDialogManager.bean.message}"/>
		</circabc:panel>
		 --%>
		
		<circabc:panel id="panelExpirationListSearch" styleClass="panelSearch">
			<h:inputText id="txbxSearch"
				value="#{WaiDialogManager.bean.searchText}" size="35"
				maxlength="1024"
				onkeydown="updateSearchButtonState()"
				onblur="updateSearchButtonState()"
				onchange="updateSearchButtonState()" />
			<h:commandButton id="btnSearchByName"
				value="#{msg.search_deleted_items_name}"
				actionListener="#{WaiDialogManager.bean.searchName}" 
				disabled="false" />
			<h:commandButton id="btnSearchByText"
				value="#{msg.search_deleted_items_text}"
				actionListener="#{WaiDialogManager.bean.searchContent}"
				disabled="false" />
			<h:commandButton id="btnSearchAll" 
				value="#{msg.show_all}"
				actionListener="#{WaiDialogManager.bean.searchAll}" />
		</circabc:panel>
		
		<circabc:panel id="panelExpirationListFilter" styleClass="panelFilter">
			<h:graphicImage id="image-filter-when" url="/images/icons/filter.gif" alt="" width="16" height="16" />
			<h:outputText value="#{msg.date_filter_when}:" styleClass="filterTitle" />
			<a:modeList itemSpacing="0" iconColumnWidth="0" horizontal="true" styleClass="filterOptions"
				selectedLinkStyle="font-weight:bold"
				value="#{WaiDialogManager.bean.dateFilter}"
				actionListener="#{WaiDialogManager.bean.dateFilterChanged}">
				<a:listItem value="all" label="#{msg.date_filter_all}" />
				<a:listItem value="today" label="#{msg.date_filter_today}" />
				<a:listItem value="week" label="#{msg.date_filter_week}" />
				<a:listItem value="month" label="#{msg.date_filter_month}" />
			</a:modeList>
			<span>&nbsp;</span>
		</circabc:panel>

		<circabc:panel id="panelExpirationListSelected" styleClass="panelActions">
			<circabc:actionLink id="action-link-delete-all-items" 
				value="#{cmsg.show_expired_items_delete_all_action}"
				image="/images/icons/delete_all.gif" 
				actionListener="#{WaiDialogManager.bean.setupConfirmation}"
				action="wai:dialog:circabcDeleteExpiredItems"
				tooltip="" >
			</circabc:actionLink>
		</circabc:panel>

		<circabc:richList id="listExpiredItems"
			viewMode="circa" value="#{WaiDialogManager.bean.items}" binding="#{WaiDialogManager.bean.itemsRichList}" 
			pageSize="#{BrowseBean.listElementNumber}" styleClass="recordSet"
			headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
			altRowStyleClass="recordSetRowAlt"
			var="r"
			initialSortColumn="deletedDate" initialSortDescending="true">

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
			
			<circabc:column id="column-path" >
				<f:facet name="header">
					<circabc:sortLink id="sort-link-path"
						label="#{msg.path}" value="displayPath"
						styleClass="header" tooltipAscending="" tooltipDescending=""  />
				</f:facet>
				<r:nodePath value="#{r.path}" showLeaf="true" />
			</circabc:column>

			<circabc:column id="column-expiration-date" >
				<f:facet name="header">
					<circabc:sortLink id="sort-link-expiration-date" value="expirationDate" styleClass="header"
						label="#{msg.expiration_date}" tooltipAscending="" tooltipDescending="" />
				</f:facet>
				<h:outputText id="text-expiration-date" value="#{r.expirationDate}">
					<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
				</h:outputText>
			</circabc:column>
			
			<circabc:column id="column-user" >
				<f:facet name="header">
					<circabc:sortLink id="sort-link-author" value="author" styleClass="header" 
						label="#{msg.author}" tooltipAscending="" tooltipDescending=""  />
				</f:facet>
				<h:outputText id="text-author" value="#{r.author}" />
			</circabc:column>

			<circabc:column id="column-actions" actions="true" >
				<f:facet name="header">
					<h:outputText id="text-actiopns" value="#{msg.actions}" />
				</f:facet>

				<%-- 
				<circabc:actionLink id="action-link-delete-multilingual-item" value="#{msg.delete}"
					image="/images/extension/icons/delete_multilingual.gif" showLink="false"
					action="wai:dialog:circabcDeleteExpiredItems"
					actionListener="#{DeleteExpiredItemsDialog.setupDialog}"
					tooltip="" rendered="#{r.isMultilingualContainer}" >
					<f:param name="deleteNodeId" value="#{r.id}" />
				</circabc:actionLink>
				--%>
			
				<circabc:actionLink id="action-link-delete-item" value="#{msg.delete}"
					image="/images/icons/delete.gif" showLink="false"
					actionListener="#{WaiDialogManager.bean.setupConfirmation}"
					action="wai:dialog:circabcDeleteExpiredItems"
					tooltip="" >
					<f:param name="deleteNodeId" value="#{r.id}" />
				</circabc:actionLink>

			</circabc:column>
			

			<circabc:dataPager id="showExpiredItemsDialog-pager" styleClass="pager" />
		</circabc:richList>
		
		<h:message for="listExpiredItems" styleClass="statusMessage" />
	</circabc:panel>
</circabc:panel>
