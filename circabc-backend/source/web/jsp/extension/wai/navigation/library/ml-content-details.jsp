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

<circabc:panel id="contentMainMLContentDetails" styleClass="contentMain">

    <%-- the right menu --%>
    <circabc:panel id="contentMainButton" styleClass="contentMainButton">
        <%--  The close button --%>
        <circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
            <h:commandButton id="close-button" styleClass="dialogButton" value="#{cmsg.close}" action="wai:browse-wai:close" />
        </circabc:panel>
        <f:verbatim><br /><br /><br /></f:verbatim>
        <%--  The action list --%>
		<circabc:panel id="id-divspacer10px" styleClass="divspacer10px" />
        <circabc:panel id="panelActions" label="#{cmsg.actions}" tooltip="#{cmsg.actions_tooltip}" styleClass="panelActionsGlobal" styleClassLabel="panelActionsLabel" rendered="#{!LibContentDetailsBean.document.locked}">
            <circabc:actions id="actions_doc" value="manage_multilingual_container_wai" context="#{LibMLContentDetailsBean.documentMlContainer}" vertical="true"/>
        </circabc:panel>
    </circabc:panel>

	<%-- the main content --%>
   <circabc:panel id="contentMainForm" styleClass="contentMainForm">

		<%-- Display the properties of the ml content --%>
       <circabc:panel id="panelMMDPropertiesDetails" label="#{cmsg.manage_multilingual_details_properties}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.manage_multilingual_details_properties_tooltip}" >
           	<circabc:propertySheetGrid id="ml-container-props-sheet" value="#{LibMLContentDetailsBean.documentMlContainer}" var="mlContainerProps" columns="1" labelStyleClass="propertiesLabelTiny"
						externalConfig="true" cellpadding="2" cellspacing="2" mode="view"/>
       </circabc:panel>

       <circabc:panel id="topOfPageAnchorDetailsMLDetail" styleClass="topOfPageAnchor"  >
           <%-- Display the "back to top icon first and display the text after." --%>
           <circabc:actionLink id="topOfPageAnchorDetailsMLDetail-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
    	<circabc:actionLink id="topOfPageAnchorDetailsMLDetail-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
   	</circabc:panel>

		<%-- Display the translations related to the ml content --%>
	<circabc:panel id="panelMMDTranslationDetails" label="#{cmsg.manage_multilingual_details_translations}" tooltip="#{cmsg.manage_multilingual_details_translations_tooltip}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel">
		<%-- list of translations --%>
		<circabc:richList id="TranslationList" viewMode="circa" value="#{LibMLContentDetailsBean.translations}" var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"	rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" initialSortColumn="name" initialSortDescending="false">
			<%-- Name and icon columns --%>
			<circabc:column>
				<f:facet name="small-icon">
					<h:graphicImage url="/images/filetypes/_default.gif" width="16" height="16" alt=" "/>
				</f:facet>
				<f:facet name="header">
					<circabc:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header" tooltipAscending="${tooltipAscending}" tooltipDescending="${tooltipDescending}"/>
				</f:facet>
					<circabc:actionLink id="view-name" value="#{r.name}" tooltip="#{r.name}" href="#{r.url}" target="new" />
			</circabc:column>
			<%-- Language columns --%>
			<circabc:column>
				<f:facet name="header">
					<circabc:sortLink label="#{msg.language}" value="language" mode="case-insensitive" styleClass="header" tooltipAscending="${tooltipAscending}" tooltipDescending="${tooltipDescending}"/>
				</f:facet>
				<circabc:actionLink id="view-language" value="#{r.language}" tooltip="#{r.language}" href="#{r.url}" target="new" />
			</circabc:column>
			<%-- view actions --%>
			<circabc:column id="col25" >
				<f:facet name="header">
					<h:outputText value="#{msg.actions}"/>
				</f:facet>
				<%-- detail basic --%>
				<circabc:actionLink id="view-link" value="#{msg.view}" tooltip="#{msg.view}" href="#{r.url}" target="new" />
			</circabc:column>
			<circabc:dataPager id="ml-content-details-translations-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>

       <circabc:panel id="topOfPageAnchorTranslations" styleClass="topOfPageAnchor"  >
           <%-- Display the "back to top icon first and display the text after." --%>
           <circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
    	<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
   	</circabc:panel>

		<%-- Display the edition history of the ml content --%>
		<circabc:panel id="panelMMDEditionDetails" label="#{cmsg.manage_multilingual_details_edition_details_label}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.manage_multilingual_details_edition_details_label_tooltip}" >
		<circabc:richList id="EditionTitle" viewMode="circa" value="#{LibMLContentDetailsBean.listOfEditionForEditionHistory}" var="ed" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" initialSortColumn="editionLabel" initialSortDescending="true">
			<circabc:column>
				<f:facet name="header">
					<circabc:sortLink label="#{cmsg.manage_multilingual_details_edition}" value="name" tooltipAscending="#{cmsg.manage_multilingual_details_edition_sort_asc}" tooltipDescending="#{cmsg.manage_multilingual_details_edition_sort_desc}"/>
				</f:facet>
				<circabc:actionLink value="#{ed.editionLabel}" tooltip="#{cmsg.manage_multilingual_details_link_tooltip} #{ed.versionLabel}" action="wai:dialog:showMLContentHistoryWai"  actionListener="#{WaiDialogManager.setupParameters}">
					<circabc:param name="label" value="#{ed.editionLabel}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{cmsg.manage_multilingual_details_notes}" />
				</f:facet>
				<h:outputText value="#{ed.editionNotes}" />
			</circabc:column>
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{cmsg.manage_multilingual_details_author}" />
				</f:facet>
				<h:outputText value="#{ed.editionAuthor}" />
			</circabc:column>
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{cmsg.manage_multilingual_details_date}" />
				</f:facet>
				<a:outputText value="#{ed.editionDate}">
					<a:convertXMLDate type="both" pattern="#{msg.date_pattern}" />
				</a:outputText>
			</circabc:column>
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{cmsg.manage_multilingual_details_actions}" />
				</f:facet>
				<circabc:actionLink value="" image="/images/icons/View_details.gif" tooltip="#{cmsg.manage_multilingual_details_edition_detail_tooltip}" action="wai:dialog:showMLContentHistoryWai"  actionListener="#{WaiDialogManager.setupParameters}">
					<circabc:param name="label" value="#{ed.editionLabel}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:dataPager id="ml-content-details-editions-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>

       <circabc:panel id="topOfPageAnchorEndMlContent" styleClass="topOfPageAnchor"  >
           <%-- Display the "back to top icon first and display the text after." --%>
           <circabc:actionLink id="topOfPageAnchorEndMlContent-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
    	<circabc:actionLink id="topOfPageAnchorEndMlContent-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
   	</circabc:panel>
  </circabc:panel>
</circabc:panel>
