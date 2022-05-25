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
<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainFormSearchResult" styleClass="contentMainForm">
	
	<circabc:panel id="search-result-info" styleClass="infoPanel" styleClassLabel="infoContent">
		<h:graphicImage id="search-result-image-info" value="/images/icons/info_icon.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}" />
		<h:outputText id="search-result-text-info-spaces" value="&nbsp;&nbsp;" escape="false" />
		<h:outputText value="#{AutonomySearchResultDialog.informationMessage}" />
	</circabc:panel>
	
	<f:verbatim>
		<br />
	</f:verbatim>
	
	<circabc:panel id="panelSearchContainer" label="#{cmsg.autonomy_search_results_results}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.autonomy_search_results_results}">
		
		<circabc:richList id="searchContainerList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{AutonomySearchResultDialog.results}" var="s" initialSortColumn="name" pageSize="#{AutonomySearchResultDialog.listPageSize}">
			
			<circabc:column id="col-container-list-name">
				<f:facet name="header">
					<h:outputText id="text-lib-title-name" value="#{cmsg.autonomy_search_results_col_name}"  />
				</f:facet>
				<h:outputText id="text-lib-cont-name" value="#{s.name}"  />
			</circabc:column>
			
			<circabc:column id="col-container-list-title">
				<f:facet name="header">
					<h:outputText id="text-lib-title-title" value="#{cmsg.autonomy_search_results_col_title}"  />
				</f:facet>
				<h:outputText id="text-lib-cont-title" value="#{s.title}"  />
			</circabc:column>
			
			<circabc:column id="col-container-list-path">
				<f:facet name="header">
					<h:outputText id="text-lib-title-path" value="#{cmsg.autonomy_search_results_col_reference}"  />
				</f:facet>
				<circabc:actionLink id="link-content-col-path" value="#{s.link}" tooltip="#{cmsg.igroot_home_whats_new_space_tooltip}" href="#{s.link}" target="_blank" />
			</circabc:column>
			
			<circabc:column id="col-container-list-modified">
				<f:facet name="header">
					<h:outputText id="text-lib-cont-modif-title" value="#{cmsg.autonomy_search_results_col_summary}"  />
				</f:facet>
				<h:outputText id="text-lib-cont-modif" value="#{s.description}" escape="false" />
			</circabc:column>
			
			<f:facet name="empty">
				<h:outputFormat id="no-list-items-Search-container" value="#{cmsg.library_container_no_list_items}"  />
			</f:facet>
			
			<circabc:dataPager id="search-result-container-pager" styleClass="pagerCirca" />
			
		</circabc:richList>
		
	</circabc:panel>
	
	<circabc:panel id="topOfPageAnchorLibHomeContainer" styleClass="topOfPageAnchor">
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorLibHomeContainer-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorLibHomeContainer-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
	
</circabc:panel>