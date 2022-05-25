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

<circabc:panel id="contentMainIGHome" styleClass="contentMain">

	<%-- the description of the interest group --%>
	<f:verbatim><br /></f:verbatim>

	<circabc:displayer rendered="#{InterestGroupLogoBean.mainPageDisplay}">
		<h:graphicImage binding="#{InterestGroupLogoBean.mainPageIconBinding}" id="ig-home-logo" />
	</circabc:displayer>
	<h:outputText style="margin-left:5px; margin-right:5px;" binding="#{InterestGroupLogoBean.mainPageDescBinding}" id="ig-home-description" value="#{NavigationBean.currentIGRoot.safeDescription}" escape="false" />

	<f:verbatim><p style="clear:both;" /><br /></f:verbatim>

<circabc:displayer id="ig-home-displayer-join" rendered="#{InterestGroupBean.joinAllowed}">
		<circabc:actionLink id="ig-home-join-group" value="#{cmsg.category_home_join_this_group}" tooltip="#{cmsg.category_home_join_this_group_tooltip}" action="wai:dialog:applyForMembershipWai" actionListener="#{WaiDialogManager.setupParameters}"  styleClass="joinLink" >
			<circabc:param name="id" value="#{NavigationBean.currentIGRoot.id}" />
			<circabc:param name="service" value="Administration" />
			<circabc:param name="activity" value="Apply for membership" />
		</circabc:actionLink>
		<h:outputText id="ig-home-1-spaces-yet" value="<br /><br />" escape="false" />
	</circabc:displayer>

	<circabc:panel id="ig-home-contact-info-panel" label="#{cmsg.igroot_home_contact_information}" styleClass="panelIGHomeContact" styleClassLabel="panelIGHomeContactLabel" tooltip="#{cmsg.igroot_home_contact_information_tooltip}">
		<h:outputText id="ig-home-contact-info" value="#{NavigationBean.currentIGRoot.contactInfo}" escape="false" />
	</circabc:panel>

	<h:outputText id="ig-home-1-spaces2" value="<br />" escape="false" />

	<circabc:panel id="panelWhatsNew" label="#{cmsg.igroot_home_whats_new}" styleClass="panelIGHomeWhatNewGlobal" styleClassLabel="panelIGHomeWhatNewLabel" rendered="#{NavigationBean.currentIGRoot.library != null}" tooltip="#{cmsg.igroot_home_whats_new_panel_tooltip}">
		<circabc:richList id="whatsNewList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{InterestGroupBean.whatsNewNodes}" var="wn" initialSortColumn="modified" initialSortDescending="true" pageSize="#{InterestGroupBean.listPageSize}">
			<circabc:column id="ig-home-wnew-col-content-title" primary="true" rendered="#{wn.isContainer == false}">
				<f:facet name="header">
					<h:outputText id="ig-home-wnew-col-name-content-title" value="#{cmsg.title}" escape="false" />
				</f:facet>
				<f:facet name="small-icon">
					<circabc:actionLink id="ig-home-wnew-col-name-icon" value="#{wn.bestTitle}" href="#{wn.url}" target="new" image="#{wn.fileType16}" showLink="false" tooltip="#{cmsg.igroot_home_whats_new_link_icon_tooltip}" />
				</f:facet>
				<circabc:actionLink id="ig-home-wnew-content-icon"  value="#{wn.bestTitle}" href="#{wn.url}" target="new" tooltip="#{cmsg.igroot_home_whats_new_link_tooltip} #{wn.bestTitle}" escape="true"/>
				<h:outputLabel id="ig-home-wnew-col-content-lang"  value="#{wn.lang}" styleClass="langCode" rendered="#{wn.lang != null}" />
				<circabc:lockIcon id="ig-home-wnew-col-content-lock" value="#{wn.nodeRef}" />
			</circabc:column>
			<circabc:column id="ig-home-wnew-col-container-title" rendered="#{wn.isContainer == true}">
				<f:facet name="header">
					<h:outputText id="ig-home-wnew-col-name-container" value="#{cmsg.igroot_home_name}" escape="false" />
				</f:facet>
				<f:facet name="small-icon">
					<circabc:actionLink id="linkcontainer_________ico" value="#{wn.bestTitle}" image="/images/icons/#{wn.smallIcon}.gif" actionListener="#{BrowseBean.clickWai}" showLink="false" tooltip="#{cmsg.igroot_home_whats_new_space_icon_tooltip}">
						<circabc:param name="id" value="#{wn.id}" />
					</circabc:actionLink>
				</f:facet>
				<circabc:actionLink id="linkcontainer_________nam" value="#{wn.bestTitle}" actionListener="#{BrowseBean.clickWai}" tooltip="#{cmsg.igroot_home_whats_new_space_tooltip} #{wn.bestTitle}" escape="true">
					<circabc:param name="id" value="#{wn.id}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column id="ig-home-wnew-col-path">
				<f:facet name="header">
					<h:outputText id="ig-home-wnew-col-name-path" value="#{cmsg.igroot_home_path}" escape="false" />
				</f:facet>
				<circabc:actionLink id="whatsNewItemPathLink" value="#{wn.displayLigthPath}" tooltip="#{cmsg.igroot_home_whats_new_space_tooltip}"
					actionListener="#{BrowseBean.clickWai}" >
					<circabc:param name="id" value="#{wn.parentId}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column id="ig-home-wnew-col-size">
				<f:facet name="header">
					<h:outputText id="ig-home-wnew-col-name-size" value="#{cmsg.igroot_home_size}" escape="false" />
				</f:facet>
				<h:outputText id="ig-home-wnew-size" value="#{wn.size}" escape="false">
					<a:convertSize />
				</h:outputText>
			</circabc:column>
			<circabc:column id="ig-home-wnew-col-modified">
				<f:facet name="header">
					<h:outputText id="ig-home-wnew-col-name-modified" value="#{cmsg.igroot_home_modified}" escape="false" />
				</f:facet>
				<h:outputText id="ig-home-wnew-modified" value="#{wn.modified}" escape="false">
					<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
				</h:outputText>
			</circabc:column>
			<f:facet name="empty">
				<h:outputFormat id="no-list-items-whatsnews" value="#{cmsg.no_list_items}" escape="false" />
			</f:facet>
			<circabc:dataPager id="ig-home-new-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>
	
	<circabc:panel id="panelVisibleDocs" label="#{cmsg.igroot_home_visible_docs}" styleClass="panelIGHomeWhatNewGlobal" styleClassLabel="panelIGHomeWhatNewLabel" rendered="#{NavigationBean.currentIGRoot.library == null}" tooltip="#{cmsg.igroot_home_visible_docs_panel_tooltip}">
		<circabc:richList id="visibleDocsList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{InterestGroupBean.visibleDocuments}" var="wn" initialSortColumn="modified" initialSortDescending="true" pageSize="#{InterestGroupBean.listPageSize}">
			<%-- Primary column for details view mode --%>
			<circabc:column id="ig-home-vd-col-content-title" primary="true" rendered="#{wn.isContainer == false}">
				<f:facet name="header">
					<h:outputText id="ig-home-vd-col-name-content-title" value="#{cmsg.title}" escape="false" />
				</f:facet>
				<f:facet name="small-icon">
					<circabc:actionLink id="ig-home-vd-col-name-icon" value="#{wn.bestTitle}" href="#{wn.url}" target="new" image="#{wn.fileType16}" showLink="false" tooltip="#{cmsg.igroot_home_whats_new_link_icon_tooltip}" escape="true"/>
				</f:facet>
				<circabc:actionLink id="ig-home-vd-content-icon"  value="#{wn.bestTitle}" href="#{wn.url}" target="new" tooltip="#{cmsg.igroot_home_whats_new_link_tooltip} #{wn.bestTitle}"/>
				<h:outputLabel id="ig-home-vd-col-content-lang"  value="#{wn.lang}" styleClass="langCode" rendered="#{wn.lang != null}" />
				<circabc:lockIcon id="ig-home-vd-col-content-lock" value="#{wn.nodeRef}" />
			</circabc:column>
			<circabc:column id="ig-home-vd-col-container-title" rendered="#{wn.isContainer == true}">
				<f:facet name="header">
					<h:outputText id="ig-home-vd-col-name-container" value="#{cmsg.igroot_home_name}" escape="false" />
				</f:facet>
				<f:facet name="small-icon">
					<circabc:actionLink id="linkcontainer_________ico" value="#{wn.bestTitle}" image="/images/icons/#{wn.smallIcon}.gif" actionListener="#{BrowseBean.clickWai}" showLink="false" tooltip="#{cmsg.igroot_home_whats_new_space_icon_tooltip}">
						<circabc:param name="id" value="#{wn.id}" />
					</circabc:actionLink>
				</f:facet>
				<circabc:actionLink id="linkcontainer_________nam" value="#{wn.bestTitle}" actionListener="#{BrowseBean.clickWai}" tooltip="#{cmsg.igroot_home_whats_new_space_tooltip} #{wn.bestTitle}" escape="true">
					<circabc:param name="id" value="#{wn.id}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column id="ig-home-vd-col-path">
				<f:facet name="header">
					<h:outputText id="ig-home-vd-col-name-path" value="#{cmsg.igroot_home_path}" escape="false" />
				</f:facet>
				<h:outputText id="ig-home-vd-path" value="#{wn.displayLigthPath}" escape="false" />
			</circabc:column>
			<circabc:column id="ig-home-vd-col-size">
				<f:facet name="header">
					<h:outputText id="ig-home-vd-col-name-size" value="#{cmsg.igroot_home_size}" escape="false" />
				</f:facet>
				<h:outputText id="ig-home-vd-size" value="#{wn.size}" escape="false">
					<a:convertSize />
				</h:outputText>
			</circabc:column>
			<circabc:column id="ig-home-vd-col-modified">
				<f:facet name="header">
					<h:outputText id="ig-home-vd-col-name-modified" value="#{cmsg.igroot_home_modified}" escape="false" />
				</f:facet>
				<h:outputText id="ig-home-vd-modified" value="#{wn.modified}" escape="false">
					<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
				</h:outputText>
			</circabc:column>
			<%-- component to display if the list is empty - normally not seen --%>
			<f:facet name="empty">
				<h:outputFormat id="no-list-items-vd" value="#{cmsg.no_list_items}" escape="false" />
			</f:facet>
			<circabc:dataPager id="ig-home-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>
	

	<circabc:panel id="topOfPageIgHomeAnchor" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.library != null}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="ig-home-anchor-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="ig-home-anchor-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:actionLink id="linkcontainer" value="" tooltip="" actionListener="#{BrowseBean.clickWai}" rendered="true" noDisplay="true">
		<circabc:param name="id" value="" />
	</circabc:actionLink>
</circabc:panel>
