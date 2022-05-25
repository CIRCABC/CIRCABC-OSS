<%--+
    |     Copyright European Community 2013 - Licensed under the EUPL V.1.0
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
<%@ taglib uri="/WEB-INF/myfaces_sandbox.tld" prefix="s"%>
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false" %>

<script language="javascript">
    function updateList() {
        document.getElementById("FormPrincipal:submit-change-filter").click();
    }
</script>

<circabc:panel id="contentMainDirectoryHome" styleClass="contentMainForm">
	
	<f:verbatim><br/></f:verbatim>
	
	<circabc:panel id="panelMembersSearch" label="#{cmsg.members_home_search_label}" tooltip="#{cmsg.members_home_search_label_tooltip}" styleClass="panelMembersSearchGlobal" styleClassLabel="panelMembersSearchLabel">
		<f:verbatim><br/></f:verbatim>
		<h:outputText id="directory-home-name-label" value="#{cmsg.members_home_name}&nbsp;" escape="false" />
		<h:inputText  id="directory-home-name" value="#{ViewUserMembershipsDialog.name}" valueChangeListener="#{ViewUserMembershipsDialog.updateName}" immediate="true" />

		<f:verbatim><br/><br/></f:verbatim>
		<h:outputText id="directory-home-space" value="&nbsp;" escape="false" />
		<h:selectOneMenu id="directory-home-sel-domain" value="#{ViewUserMembershipsDialog.domain}" onchange="updateList()" valueChangeListener="#{ViewUserMembershipsDialog.updateDomain}" immediate="true">
			<f:selectItems value="#{ViewUserMembershipsDialog.filters}" />
		</h:selectOneMenu>
		<h:outputText value="&nbsp;" escape="false" />
		<h:outputText id="directory-home-space2" value="&nbsp;" escape="false" />
		<h:commandButton id="directory-home-search-action" action="#{ViewUserMembershipsDialog.search}" value="#{cmsg.members_home_search}" immediate="true" />
		<f:verbatim><br/><br/></f:verbatim>
	</circabc:panel>
	
	<h:outputText styleClass="inpage_information" id="members_elements" value="#{ViewUserMembershipsDialog.userCount}"/>
	
	<f:verbatim><br/></f:verbatim>
	
	<circabc:panel id="panelMembersResultsUser" label="#{cmsg.members_home_results_label}" tooltip="#{cmsg.members_home_results_label_tooltip}" styleClass="panelMembersResultsGlobal" styleClassLabel="panelMembersResultsLabel">
		
		<circabc:richList id="membersResultsListUser" 
				viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" 
				pageSize="#{DirectoryBean.pageSize}" 
				value="#{ViewUserMembershipsDialog.searchResult}" var="u" initialSortColumn="lastName">
			<circabc:column id="directory-home-user-col-name-id">
				<f:facet name="header">
					<circabc:sortLink id="directory-home-user-col-id" label="#{cmsg.members_home_username}" value="id" tooltipAscending="#{cmsg.members_home_sort_asc}" tooltipDescending="#{cmsg.members_home_sort_desc}" />
				</f:facet>
				<h:outputText id="directory-home-user-id" value="#{u.id}" />
			</circabc:column>
			<circabc:column id="directory-home-user-col-name-surname">
				<f:facet name="header">
					<circabc:sortLink id="directory-home-user-col-surname" label="#{cmsg.members_home_surname}" value="lastName" tooltipAscending="#{cmsg.members_home_sort_asc}" tooltipDescending="#{cmsg.members_home_sort_desc}" />
				</f:facet>
				<h:outputText id="directory-home-user-lastname" value="#{u.lastName}" />
			</circabc:column>
			<circabc:column id="directory-home-user-col-first">
				<f:facet name="header">
					<h:outputText id="directory-home-user-col-name-first" value="#{cmsg.members_home_firstname}" />
				</f:facet>
				<h:outputText id="directory-home-user-first" value="#{u.firstName}" />
			</circabc:column>
			<circabc:column id="directory-home-user-col-email">
				<f:facet name="header">
					<h:outputText id="directory-home-user-col-name-email" value="#{cmsg.members_home_email}" />
				</f:facet>
				<h:outputText id="directory-home-user-email" value="#{u.email}" />
			</circabc:column>
			<circabc:column id="directory-home-user-col-actions">
				<f:facet name="header">
					<h:outputText id="directory-home-user-col-name-actions" value="#{cmsg.members_home_action}" />
				</f:facet>
				<circabc:actions id="membersResultsViewActionsUser" value="members_results_view_no_actions_wai" context="#{u}" showLink="false" />
				<circabc:actions id="membershipsViewActionsUser" value="other_user_account_actions_wai_admin" context="#{u}" showLink="false" />
			</circabc:column>
			<f:facet name="empty">
				<h:outputFormat id="no-list-items-members-results" value="#{cmsg.members_home_no_list_items}" />
			</f:facet>
			<circabc:dataPager id="dir-home-pager" styleClass="pagerCirca" />
		</circabc:richList>
		
	</circabc:panel>
	
	<f:verbatim><br/></f:verbatim>
	
	<circabc:panel id="topOfPageAnchorDirHome" styleClass="topOfPageAnchor">
		<circabc:actionLink id="topOfPageAnchorDirHome-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorDirHome-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	
</circabc:panel>
