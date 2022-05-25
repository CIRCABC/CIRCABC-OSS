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
<%@ page isELIgnored="false"%>

	<circabc:panel id="contentMainFormSearchResult" styleClass="contentMainForm">

		<circabc:panel id="search-result-info" styleClass="infoPanel" styleClassLabel="infoContent" >
			<h:graphicImage id="search-result-image-info" value="/images/icons/info_icon.gif" title="#{cmsg.message_info_tooltip}" alt="#{cmsg.message_info_tooltip}"  />
			<h:outputText id="search-result-text-info-spaces" value="&nbsp;&nbsp;" escape="false" />
			<h:outputText value="#{SearchResultDialog.informationMessage}" />
		</circabc:panel>

		<f:verbatim>
			<br />
		</f:verbatim>

		<circabc:panel id="panelSearchContainer" label="#{cmsg.library_panel_container_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.library_panel_container_tooltip}">
			<circabc:richList id="searchContainerList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{SearchResultDialog.containers}" var="s" initialSortColumn="name" pageSize="#{SearchResultDialog.listPageSize}">
				
				<circabc:column id="col-container-list-title">
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_container_title}" value="title" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<h:outputText id="text-lib-cont-title" value="#{s.title}" escape="false"/>
				</circabc:column>
				
				<circabc:column id="col-container-list-path">
					<f:facet name="header">
						<circabc:sortLink label="#{msg.path}" value="displayLigthPath" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<circabc:actionLink id="link-content-col-path" value="#{s.displayLigthPath}" tooltip="#{cmsg.igroot_home_whats_new_space_tooltip}"
						actionListener="#{BrowseBean.clickWai}" >
						<circabc:param name="id" value="#{s.parentId}" />
					</circabc:actionLink>
				</circabc:column>
				
				<circabc:column id="col-container-list-modified">
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_container_modified}" value="modified" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<h:outputText id="text-lib-cont-modif" value="#{s.modified}" >
						<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
					</h:outputText>
				</circabc:column>
				
				<circabc:column id="col-container-list-actions">
					<f:facet name="header">
						<h:outputText id="text-lib-cont-act" value="#{cmsg.actions}"  />
					</f:facet>
					<circabc:actions id="SearchContainerActions" value="space_browse_library_wai" context="#{s}" showLink="false" />
				</circabc:column>
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-Search-container" value="#{cmsg.library_container_no_list_items}"  />
				</f:facet>
				<circabc:dataPager id="search-result-container-pager" styleClass="pagerCirca" />
			</circabc:richList>
		</circabc:panel>

		<circabc:panel id="topOfPageAnchorLibHomeContainer" styleClass="topOfPageAnchor"  >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink id="topOfPageAnchorLibHomeContainer-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink id="topOfPageAnchorLibHomeContainer-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

		<circabc:panel id="panelSearchContent" label="#{cmsg.library_panel_content_label}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.library_panel_content_tooltip}">
			<circabc:richList id="SearchContentList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{SearchResultDialog.contents}" var="c" initialSortColumn="name" pageSize="#{SearchResultDialog.listPageSize}">
				
				<circabc:column id="col-contents-list-name" >
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_content_name}" value="name" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<h:outputText id="content-col-name" value="#{c.name}"  escape="false"/>
				</circabc:column>
				
				<circabc:column id="col-contents-list-title" >
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_content_title}" value="title" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }" />
					</f:facet>
					<h:outputText id="content-col-title" value="#{c.title}" escape="false"/>
				</circabc:column>
				
				<circabc:column id="col-contents-list-path" >
					<f:facet name="header">
						<circabc:sortLink label="#{msg.path}" value="displayLigthPath" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<circabc:actionLink id="content-col-path" value="#{c.displayLigthPath}" tooltip="#{cmsg.igroot_home_whats_new_space_tooltip}"
						actionListener="#{BrowseBean.clickWai}" >
						<circabc:param name="id" value="#{c.parentId}" />
					</circabc:actionLink>
				</circabc:column>
				
				<circabc:column id="col-contents-list-size" >
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_content_size}" value="size" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<h:outputText id="content-col-size" value="#{c.size}" >
						<a:convertSize />
					</h:outputText>
				</circabc:column>
				
				<circabc:column id="col-contents-list-modified" >
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_content_modified}" value="modified" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<h:outputText id="content-col-modified" value="#{c.modified}" >
						<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
					</h:outputText>
				</circabc:column>
				
				<circabc:column id="col-contents-list-actions" >
					<f:facet name="header">
						<h:outputText id="content-col-name-actions" value="#{cmsg.actions}"  />
					</f:facet>
					<circabc:actions id="libraryContentActions" value="document_browse_library_wai" context="#{c}" showLink="false" />
				</circabc:column>
			
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-library-content" value="#{cmsg.library_content_no_list_items}"  />
				</f:facet>
				<circabc:dataPager id="search-result-content-pager" styleClass="pagerCirca" />
			</circabc:richList>
		</circabc:panel>

		<circabc:panel id="topOfPageAnchorLibHomeContent" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.library != null}" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink id="topOfPageAnchorLibHomeContent-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink id="topOfPageAnchorLibHomeContent-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

	<circabc:panel id="panelSearchPosts" label="#{cmsg.newsgroups_topic_post}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.newsgroups_topic_browse_post_tooltip}">
			<circabc:richList id="SearchPostList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{SearchResultDialog.posts}" var="p" initialSortColumn="name" pageSize="#{SearchResultDialog.listPageSize}">
				
				<circabc:column>
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.newsgroups_topic_post}" value="id" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<circabc:actionLink value="#{p.fromTopic}" actionListener="#{BrowseBean.clickWai}" tooltip="#{cmsg.newsgroups_topic_post_tooltip}">
						<circabc:param name="id" value="#{p.id}" />
					</circabc:actionLink>
				</circabc:column>
				
				<circabc:column>
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.newsgroups_topic_author}" value="creator" tooltipAscending="${cmsg.define_property_dialog_translation_sort_asc }" tooltipDescending="${cmsg.define_property_dialog_translation_sort_desc }"/>
					</f:facet>
					<h:outputText value="#{p.creator}" />
				</circabc:column>
				
				<circabc:column>
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.newsgroups_topic_posted}" value="created" tooltipAscending="#{cmsg.newsgroups_topic_post_sort_asc}" tooltipDescending="#{cmsg.newsgroups_topic_post_desc}"/>
					</f:facet>
					<h:outputText value="#{p.created}">
						<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
					</h:outputText>
				</circabc:column>
				
				<circabc:column id="col-post-list-modified" >
					<f:facet name="header">
						<circabc:sortLink label="#{cmsg.library_content_modified}" value="modified" tooltipAscending="#{cmsg.newsgroups_topic_post_sort_asc}" tooltipDescending="#{cmsg.newsgroups_topic_post_desc}"/>
					</f:facet>
					<h:outputText id="post-col-modified" value="#{p.modified}" >
						<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
					</h:outputText>
				</circabc:column>
				
				<circabc:column id="col-post-list-actions" >
					<f:facet name="header">
						<h:outputText id="posts-col-name-actions" value="#{cmsg.actions}"  />
					</f:facet>
					<circabc:actions id="newsgroup_browse_post_actions_wai" value="newsgroup_browse_post_actions_wai" context="#{p}" showLink="false" />
				</circabc:column>
				
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-library-post" value="#{cmsg.library_content_no_list_items}"  />
				</f:facet>
				<circabc:dataPager id="search-result-post-pager" styleClass="pagerCirca" />
			</circabc:richList>
		</circabc:panel>


		<circabc:panel id="topOfPageAnchorLibHomepost" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.library != null}" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink id="topOfPageAnchorLibHomepost-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink id="topOfPageAnchorLibHomepost-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>
	</circabc:panel>

