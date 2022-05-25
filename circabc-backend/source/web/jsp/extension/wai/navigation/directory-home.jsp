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
<%@ taglib uri="/WEB-INF/myfaces_sandbox.tld" prefix="s"%>
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainDirectoryHome" styleClass="contentMain">

	<f:verbatim><br /></f:verbatim>
	<p>
		<h:outputText id="directory-home-intro" escape="false" value="#{cmsg.members_home_text_1}"/>
	</p>
	<f:verbatim><br /><br /></f:verbatim>

	<circabc:panel id="panelMembersSearch" label="#{cmsg.members_home_search_label}" tooltip="#{cmsg.members_home_search_label_tooltip}" styleClass="panelMembersSearchGlobal" styleClassLabel="panelMembersSearchLabel">
		<f:verbatim><br /></f:verbatim>
		<h:outputText id="directory-home-name-label" value="#{cmsg.members_home_name}&nbsp;" escape="false" />
		<h:inputText  id="directory-home-name" value="#{DirectoryBean.name}" />
		<h:outputText id="directory-home-txt-page-size" value="&nbsp;&nbsp;#{cmsg.members_home_results_per_page}&nbsp;" escape="false" />
		<h:selectOneMenu id="directory-home-sel-page-size" value="#{DirectoryBean.pageSize}">
			<f:selectItems value="#{DirectoryBean.pageSizes}" />
		</h:selectOneMenu>
		<f:verbatim><br /><br /></f:verbatim>
		<h:outputText id="directory-home-txt-filterby" value="#{cmsg.members_home_filter_by}&nbsp;" escape="false" />
		<h:selectOneMenu id="directory-home-sel-member" value="#{DirectoryBean.member}" rendered="#{DirectoryBean.advanced}" onchange="checkProfileFilter();">
			<f:selectItem id="directory-home-item-member" itemValue="members" itemLabel="#{cmsg.members_home_members_only}" />
			<f:selectItem id="directory-home-item-all-member" itemValue="allcircabcuser" itemLabel="#{cmsg.members_home_all_members}" />
		</h:selectOneMenu>			
		<h:outputText id="directory-home-space" value="&nbsp;" escape="false"/>
		<h:selectOneMenu id="directory-home-sel-domain" value="#{DirectoryBean.domain}">
			<f:selectItems value="#{DirectoryBean.filters}" />
		</h:selectOneMenu>
		<h:outputText value="&nbsp;" escape="false"/>
		<h:selectOneMenu id="directory-home-sel-profile" value="#{DirectoryBean.profile}" >
			<f:selectItem id="directory-home-item-empty" itemValue="" itemLabel="#{cmsg.members_home_disable_profile_filter}" />
			<f:selectItems value="#{DirectoryBean.profiles}" />
		</h:selectOneMenu>
		<h:outputText value="&nbsp;" escape="false"/>
		<h:outputText id="directory-home-space2" value="&nbsp;" escape="false"/>
		<h:commandButton id="directory-home-search-action" action="#{DirectoryBean.search}" value="#{cmsg.members_home_search}" />
		<f:verbatim><br /><br /></f:verbatim>		
	</circabc:panel>
	
	<f:verbatim><br/></f:verbatim>
	<h:outputText styleClass="inpage_information" id="members_elements" value="#{DirectoryBean.userCount}" />
	<f:verbatim><br/></f:verbatim>

	<circabc:displayer id="displayer-dir-home-noguest" rendered="#{NavigationBean.isGuest == false}">
		<circabc:panel id="panelMembersResultsNoguest" label="#{cmsg.members_home_results_label}" tooltip="#{cmsg.members_home_results_label_tooltip}" styleClass="panelMembersResultsGlobal" styleClassLabel="panelMembersResultsLabel">
			<circabc:richList id="membersResultsListNoguest" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{DirectoryBean.users}" var="u" initialSortColumn="lastName" pageSize="#{DirectoryBean.pageSize}">
				<circabc:column id="directory-home-noguest-col-name-surname">
					<f:facet name="header">
						<circabc:sortLink id="directory-home-noguest-col-surname" label="#{cmsg.members_home_surname}" value="lastName" tooltipAscending="#{cmsg.members_home_sort_asc}" tooltipDescending="#{cmsg.members_home_sort_desc}"/>
					</f:facet>
					<circabc:actionLink id="directory-home-noguest-lastname" value="#{u.lastName}" action="wai:dialog:viewUserDetailsWai" actionListener="#{WaiDialogManager.setupParameters}" tooltip="#{cmsg.members_home_view_details_tooltip}">
						<circabc:param name="id" value="#{u.id}" />
					</circabc:actionLink>
				</circabc:column>
				<circabc:column id="directory-home-noguest-col-first">
					<f:facet name="header">
						<h:outputText id="directory-home-noguest-col-name-first" value="#{cmsg.members_home_firstname}" />
					</f:facet>
					<circabc:actionLink id="directory-home-noguest-first" value="#{u.firstName}" action="wai:dialog:viewUserDetailsWai" actionListener="#{WaiDialogManager.setupParameters}" tooltip="#{cmsg.members_home_view_details_tooltip}">
						<circabc:param name="id" value="#{u.id}" />
					</circabc:actionLink>
				</circabc:column>
				<circabc:column id="directory-home-noguest-col-email">
					<f:facet name="header">
						<h:outputText id="directory-home-noguest-col-name-email" value="#{cmsg.members_home_email}" />
					</f:facet>
					<circabc:actionLink id="directory-home-noguest-email" value="#{u.email}" action="wai:dialog:viewUserDetailsWai" actionListener="#{WaiDialogManager.setupParameters}" tooltip="#{cmsg.members_home_view_details_tooltip}">
						<circabc:param name="id" value="#{u.id}" />
					</circabc:actionLink>
				</circabc:column>
				<circabc:column id="directory-home-noguest-col-profile">
					<f:facet name="header">
						<h:outputText id="directory-home-noguest-col-name-profile" value="#{cmsg.members_home_access_profile}" />
					</f:facet>
					<h:outputText id="directory-home-noguest-profile" value="#{u.profile}" />
				</circabc:column>
				<circabc:column id="directory-home-noguest-col-actions" >
					<f:facet name="header">
						<h:outputText id="directory-home-noguest-col-name-actions" value="#{cmsg.members_home_action}" />
					</f:facet>
					<circabc:actions id="membersResultsViewActionsNoguest" value="members_results_view_wai" context="#{u}" showLink="false" />
				</circabc:column>
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-members-results" value="#{cmsg.members_home_no_list_items}" />
				</f:facet>
				<circabc:dataPager id="dir-home-pager" styleClass="pagerCirca" />
			</circabc:richList>
		</circabc:panel>
	</circabc:displayer>

	<circabc:displayer id="displayer-dir-home-guest" rendered="#{NavigationBean.isGuest == true}">
		<circabc:panel id="panelMembersResultsGuest" label="#{cmsg.members_home_results_label}" tooltip="#{cmsg.members_home_results_label_tooltip}" styleClass="panelMembersResultsGlobal" styleClassLabel="panelMembersResultsLabel">
			<circabc:richList id="membersResultsListGuest" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{DirectoryBean.users}" var="u" initialSortColumn="name" pageSize="#{DirectoryBean.pageSize}">
				<circabc:column id="directory-home-guest-col-surname">
					<f:facet name="header">
						<circabc:sortLink id="directory-home-guest-col-name-surname" label="#{cmsg.members_home_surname}" value="surname" tooltipAscending="#{cmsg.members_home_sort_asc}" tooltipDescending="#{cmsg.members_home_sort_desc}"/>
					</f:facet>
					<h:outputText id="directory-home-guest-last" value="#{u.lastName}" />
				</circabc:column>
				<circabc:column id="directory-home-guest-col-first">
					<f:facet name="header">
						<h:outputText id="directory-home-guest-col-name-first" value="#{cmsg.members_home_firstname}" />
					</f:facet>
					<h:outputText id="directory-home-guest-first" value="#{u.firstName}" />
				</circabc:column>
				<circabc:column id="directory-home-guest-col-profile">
					<f:facet name="header">
						<h:outputText id="directory-home-guest-col-name-profile" value="#{cmsg.members_home_access_profile}" />
					</f:facet>
					<h:outputText id="directory-home-guest-profile" value="#{u.profile}" />
				</circabc:column>
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-members-results-guest" value="#{cmsg.members_home_no_list_items}" />
				</f:facet>
				<circabc:dataPager id="dir-home-guest-pager" styleClass="pagerCirca" />
			</circabc:richList>
		</circabc:panel>
	</circabc:displayer>
	
	<f:verbatim><br /></f:verbatim>	
	
	<t:dataTable id="exportDataTable" rendered="false" var="u"
		binding="#{DirectoryBean.dataTable}"
		value="#{DirectoryBean.users}">
		
		<%-- Moniked column --%>
		<t:column>
			<f:facet name="header">
				<h:outputText id="exportDataTable-label0" value="Username" styleClass="header" />
			</f:facet>		
			<h:outputText id="audit-username-txt" value="#{u.ecMoniker}" />
		</t:column>
		
		<%-- first name column --%>
		<t:column>
			<f:facet name="header">
				<h:outputText id="exportDataTable-label1" value="FirstName" styleClass="header" />
			</f:facet>		
			<h:outputText id="audit-firstname-txt" value="#{u.firstName}" />
		</t:column>

		<%-- last name column --%>
		<t:column>
			<f:facet name="header">
				<h:outputText id="exportDataTable-label2" value="LastName" styleClass="header" />
			</f:facet>
			<h:outputText id="audit-date-txt" value="#{u.lastName}" />
		</t:column>

	
		<%-- email column --%>
		<t:column>
			<f:facet name="header">
				<h:outputText id="exportDataTable-label3" value="Email" styleClass="header" />
			</f:facet>
<%-- 			Migration 3.1 -> 3.4.6 - 20/12/2011 - Changed because the faces id was duplicated --%>
<%--			<h:outputText id="audit-username-txt" value="#{u.email}" /> --%>
			<h:outputText id="audit-email-txt" value="#{u.email}" />
		</t:column>		
		
		<%-- email column --%>
		<t:column>
			<f:facet name="header">
				<h:outputText id="exportDataTable-label4" value="Email" styleClass="header" />
			</f:facet>
<%-- 			Migration 3.1 -> 3.4.6 - 20/12/2011 - Changed because the faces id was duplicated --%>
<%--			<h:outputText id="audit-username-txt" value="#{u.email}" /> --%>
			<h:outputText id="audit-profile-txt" value="#{u.profile}" />
		</t:column>	
		
	</t:dataTable>	
	
	<circabc:panel id="panelExport" label="#{cmsg.export_label}" tooltip="#{cmsg.export_label}" styleClass="panelSearchGlobal" styleClassLabel="panelSearchLabel" rendered="#{DirectoryBean.userAllowedToExport }">
		<f:verbatim><br /></f:verbatim>
		<h:selectOneMenu id="export-type" value="#{DirectoryBean.exportType}" immediate="true"  >
			<f:selectItems id="export-type-options" value="#{DirectoryBean.exportTypes}" />
		</h:selectOneMenu>
		<h:outputText id="export-space3" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="export-button" action="#{DirectoryBean.export}" value="#{cmsg.export}" />
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorDirHome" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.library != null}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorDirHome-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorDirHome-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:displayer id="displayer-dir-home-advanced" rendered="#{DirectoryBean.advanced}">
		<%-- We have the right because it's an admin and administrative tasks require javacript --%>
		<script type="text/javascript">
			function checkProfileFilter()
			{
				if (document.getElementById("FormPrincipal:directory-home-sel-member").value == "allcircabcuser" )
				{
					document.getElementById("FormPrincipal:directory-home-sel-profile").disabled = true;
				}
				else
				{
					document.getElementById("FormPrincipal:directory-home-sel-profile").disabled = false;
				}
			}
		</script>
	</circabc:displayer>

</circabc:panel>
