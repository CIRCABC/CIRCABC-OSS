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

<circabc:panel id="contentMainInfContentDetails" styleClass="contentMain">

    <%-- the right menu --%>
    <circabc:panel id="contentInfMainButton" styleClass="contentMainButton">
        <%--  The close button --%>
        <circabc:panel id="divButtonInfDialog" styleClass="divButtonDialog">
            <h:commandButton id="close-buttonInf" styleClass="dialogButton" value="#{cmsg.close}" action="wai:browse-wai:close" />
        </circabc:panel>
        <f:verbatim><br /><br /><br /></f:verbatim>
        <%--  The action list --%>
		<circabc:panel id="id-Infdivspacer10px" styleClass="divspacer10px" />
       	<circabc:panel id="panelInfActions" label="#{cmsg.actions}" tooltip="#{cmsg.actions_tooltip}" styleClass="panelActionsGlobal" styleClassLabel="panelActionsLabel" rendered="#{InfContentDetailsBean.documentLocked == false}">
           <circabc:actions id="actionsInf_doc" value="inf_doc_details_actions_wai" context="#{InfContentDetailsBean.document}" vertical="true"/>
        </circabc:panel>
    </circabc:panel>

    <%-- the main content --%>
    <circabc:panel id="contentInfMainForm" styleClass="contentMainForm">

		<circabc:displayer id="Inf-displayer-locked" rendered="#{InfContentDetailsBean.documentLocked}">
		    <%-- If the current document is a locked, display details and a link of the working copy --%>
            <circabc:panel id="working-copyInf" label="#{msg.working_copy_document}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{msg.document_details_panel_locked_detail_tooltip}" rendered="#{InfContentDetailsBean.workingCopyDocument != null}" >
                <h:outputText id="out-workingcopyInf" value="#{msg.working_copy_document}:&nbsp;&nbsp;" escape="false" />
                <circabc:actionLink id="act-detailsInf" rendered="#{InfContentDetailsBean.workingCopyDocument != null}" value="#{InfContentDetailsBean.workingCopyDocument.name}" tooltip="#{InfContentDetailsBean.workingCopyDocument.name}" actionListener="#{BrowseBean.clickWai}" >
                    <circabc:param name="id" value="#{InfContentDetailsBean.workingCopyDocument.id}" />
                </circabc:actionLink>
                <f:verbatim><br /></f:verbatim>
                <h:outputText id="out-workingcopy-spaceInf" value="&nbsp;" escape="false" />
            </circabc:panel>
            <f:verbatim><br /></f:verbatim>
		</circabc:displayer>

        <%-- Display the properties  --%>
        <circabc:panel id="panelDocumentDetailsInf" label="#{msg.properties}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_detail_tooltip}" >
        	<circabc:panel id="ml-detInf-padd-list" styleClass="pad8px" />
            <circabc:panel id="contentInfHeaderSubPanelDocdet">
                <circabc:panel id="contentInfHeaderIconDocdet" styleClass="contentHeaderIcon" >
                    <circabc:actionLink id="docInf-logo1" value="#{InfContentDetailsBean.document.name}" tooltip="#{cmsg.library_content_link_tooltip}: #{InfContentDetailsBean.document.name}" href="#{InfContentDetailsBean.document.properties.url}" target="new" image="#{InfContentDetailsBean.document.properties.fileType32}" showLink="false" />
                </circabc:panel>
                <circabc:panel id="contentInfHeaderTextDocdet">
                    <circabc:propertySheetGrid id="documentInf-props" value="#{InfContentDetailsBean.document}" var="documentProps" columns="1"
                            mode="view"  labelStyleClass="propertiesLabelTiny" cellpadding="2" cellspacing="2" externalConfig="true" />
                    <h:message id="msg1Inf" for="document-props" styleClass="statusMessage" />
                </circabc:panel>
            </circabc:panel>
        </circabc:panel>

        <circabc:panel id="topOfPageAnchorDocDetailsInf" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-docInf-det-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-docInf-det-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>

        <%-- Multilingual properties (if ml aspect applied) --%>
        <circabc:panel id="panelDocumentInfMlProps" label="#{msg.ml_content_info}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_mldetail_tooltip}" rendered="#{InfContentDetailsBean.multilingual}" >
            <%--  The details of the mlContainer --%>
            <circabc:propertySheetGrid id="ml-containerInf-props-sheet" value="#{InfContentDetailsBean.documentMlContainer}" var="mlContainerProps" columns="1"
                            labelStyleClass="propertiesLabel" externalConfig="true" cellpadding="2" cellspacing="2" mode="view"/>

            <%-- Separator blank space --%>
            <circabc:panel id="ml-detInf-padd-1" styleClass="pad8px" />

            <%--  The list of translations --%>
            <circabc:panel label="#{msg.related_translations}" id="related-translationInf-panel"  styleClass="inner_panel" styleClassLabel="panelLabel" >

	            <%-- list of translations --%>
	            <circabc:richList id="TranslationListInf" viewMode="circa" value="#{InfContentDetailsBean.translations}"
	                      var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"
	                      rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
	                      pageSize="10" initialSortColumn="Name" initialSortDescending="false">

	                <%-- Name and icon columns --%>
	                <circabc:column id="col21Inf">
	                    <f:facet name="small-icon">
	                        <h:graphicImage id="col21Inf-icon" url="/images/filetypes/_default.gif" width="16" height="16" alt="#{msg.name} "/>
	                    </f:facet>
	                    <f:facet name="header">
	                        <circabc:sortLink id="col21Inf-sortlink" label="#{msg.name}" value="Name" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
	                    </f:facet>
	                        <circabc:actionLink id="view-name" value="#{r.name}" tooltip="#{r.name}" href="#{r.url}" target="new" />
	                </circabc:column>

	                <%-- Language columns --%>
	                <circabc:column id="col22Inf" >
	                    <f:facet name="header">
	                        <circabc:sortLink id="col22Inf-link" label="#{msg.language}" value="language" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
	                    </f:facet>
	                    <circabc:actionLink id="view-language" value="#{r.language}" tooltip="#{r.language}" href="#{r.url}" target="new" />
	                </circabc:column>

	                <%-- view actions --%>
	                <circabc:column id="col25Inf" >
	                    <f:facet name="header">
	                        <h:outputText id="col25Inf-txt" value="#{msg.actions}"/>
	                    </f:facet>
	                    <circabc:actionLink id="view_detailsInf" value="#{cmsg.contentDetail}" tooltip="#{cmsg.contentDetail}" actionListener="#{BrowseBean.clickWai}" >
	                        <circabc:param id="col25Inf-param" name="id" value="#{r.id}" />
	                    </circabc:actionLink>
	                </circabc:column>
					<circabc:dataPager id="inf-content-details-ml-pager" styleClass="pagerCirca" />
	            </circabc:richList>
            </circabc:panel>
            <%-- Separator blank space --%>
            <circabc:panel id="ml-det-padd-endInf" styleClass="pad8px" />
        </circabc:panel>


        <%-- Multilingual properties (if NO ml aspect applied) --%>
        <circabc:panel id="panelDocumentNoMlPropInfs" label="#{msg.ml_content_info}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_mldetail_tooltip}" rendered="#{!InfContentDetailsBean.multilingual}">
            <h:outputText id="no-ml-msg" value="#{msg.not_multilingual}" />
            <%-- Action - Add Translation --%>
            <r:permissionEvaluator value="#{InfContentDetailsBean.document}" allow="Write" id="eval_make-multilingual">
                <circabc:panel id="no-ml-actions" styleClass="pad16px">
                    <circabc:actionLink id="act-make-multilingual" value="#{msg.make_multilingual}" tooltip="#{msg.make_multilingual}" action="wai:dialog:makeMultilingualWai" actionListener="#{DialogManager.setupParameters}" showLink="true" image="/images/icons/make_ml.gif" rendered="#{InfContentDetailsBean.documentLocked == false && InfContentDetailsBean.workingCopy == false}" >
                        <circabc:param name="id" value="#{InfContentDetailsBean.document.id}" />
                    </circabc:actionLink>
                </circabc:panel>
            </r:permissionEvaluator>
        </circabc:panel>

        <circabc:panel id="topOfPageAnchoMlPropInf" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-ml-propInf-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-ml-propInf-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>


        <%-- Version history (if versionnable aspect applied) --%>
        <circabc:panel id="panelDocumentVersionHistoryInf" label="#{msg.version_history}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_version_hitory_tooltip}" rendered="#{InfContentDetailsBean.versionable}">

            <circabc:richList id="versionHistoryInfList" viewMode="circa" value="#{InfContentDetailsBean.versionHistory}"
                  var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"
                  rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
                  pageSize="10" initialSortColumn="versionLabel" initialSortDescending="false">

                <%-- Primary column for details view mode --%>
                <circabc:column id="col1Inf" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.version}" value="versionLabel" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <circabc:actionLink id="labelInf" value="#{r.versionLabel}" tooltip="#{r.versionLabel}" href="#{r.url}" target="new" />
                </circabc:column>

                <%-- Version notes columns --%>
                <circabc:column id="col2Inf" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.notes}" value="notes" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <h:outputText id="notesInf" value="#{r.notes}" />
                </circabc:column>

                <%-- Description columns --%>
                <circabc:column id="col3Inf" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.author}" value="author" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <h:outputText id="authorInf" value="#{r.author}" />
                </circabc:column>

                <%-- Created Date column for details view mode --%>
                <circabc:column id="col4Inf" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.date}" value="versionDate" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <h:outputText id="dateInf" value="#{r.versionDate}">
                        <a:convertXMLDate type="bothInf" pattern="#{msg.date_time_pattern}" />
                    </h:outputText>
                </circabc:column>

                <%-- view the contents of the specific version --%>
                <circabc:column id="col5Inf" >
                    <f:facet name="header">
                        <h:outputText value="#{msg.actions}"/>
                    </f:facet>
                    <circabc:actionLink id="view-linkInf" value="#{msg.view}" tooltip="#{msg.view}" href="#{r.url}" target="new" />

                    <%-- Add the view versionned properties action. --%>
                    <h:outputText id="space-vhInf" value=" " />

                    <circabc:actionLink id="view-version-propsInf" value="#{msg.properties}" tooltip="#{msg.properties}" action="wai:dialog:showContentHistoryWai" actionListener="#{CircabcContentHistoryDialog.init}" >
                        <circabc:param name="id" value="#{InfContentDetailsBean.document.id}" />
                        <circabc:param name="versionLabel" value="#{r.versionLabel}" />
                    </circabc:actionLink>
                </circabc:column>
				<circabc:dataPager id="inf-content-details-history-pager" styleClass="pagerCirca" />
            </circabc:richList>
        </circabc:panel>


        <%-- Version history (if NO versionnable aspect applied) --%>
        <circabc:panel id="panelDocumentNoVersionHistoryInf" label="#{msg.version_history}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_version_hitory_tooltip}" rendered="#{!InfContentDetailsBean.versionable}">
            <h:outputText id="no-history-msgInf" value="#{msg.not_versioned}"  />
            <r:permissionEvaluator value="#{InfContentDetailsBean.document}" allow="Write" id="eval_ver">
	            <circabc:panel id="no-version-actionsInf" styleClass="pad16px">
                	<circabc:actionLink id="make-versionableInf" value="#{msg.allow_versioning}" tooltip="#{msg.allow_versioning}" image="/images/icons/versionHistory_icon.gif"
                                     action="#{InfContentDetailsBean.applyVersionable}" rendered="#{InfContentDetailsBean.documentLocked == false}" />
                </circabc:panel>
            </r:permissionEvaluator>

        </circabc:panel>

        <circabc:panel id="topOfPageAnchorVerHistInf" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-ver-hist-1Inf" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-ver-hist-2Inf" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>
    </circabc:panel>
</circabc:panel>

