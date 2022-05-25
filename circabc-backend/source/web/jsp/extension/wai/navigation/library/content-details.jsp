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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<style type="text/css">
<!--

.pdfViewStyle {
   margin-left: 15px;
   margin-right: 15px;
   margin-bottom: 15px;
   height: 500px;
}

#pdf p {
   padding: 1em;
}

#pdf object {
   display: block;
   border: solid 1px #666;
}

-->
</style>

<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/pdfobject_source.js"></script>

<script type="text/javascript">

function viewPDF() {
	
	var obj = {
		id: "viewInlinePDF",
		pdfOpenParams: {
			navpanes: 0,
			toolbar: 0,
			statusbar: 0,
			view: "FitBH",
		}
	};
	
	obj['url'] = document.getElementById("FormPrincipal:docUrl").value;
	
	var pObj = new PDFObject(obj);
	
	var pdfElement = document.getElementById("pdf");
	
	if (pObj) {
		
		var pluginType = pObj.get("pluginTypeFound");
		
		if (pluginType) {
			pdfElement.className = "pdfViewStyle";
			pObj.embed("pdf");
		}
		else {
			pdfElement.innerHTML = "No valid PDF plugin found. Please download the file from the link in the actions menu.";
		}
	}
	else {
		pdfElement.innerHTML = "Could not create embedded PDF object.";
	}
};

</script>

<%-- <h:inputHidden id="docUrl" value="#{LibContentDetailsBean.documentUrl}" /> --%>

<circabc:panel id="contentMainContentDetails" styleClass="contentMain">

    <%-- the right menu --%>
    <circabc:panel id="contentMainButton" styleClass="contentMainButton">
        <%--  The close button --%>
        <circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
            <h:commandButton id="close-button" styleClass="dialogButton" value="#{cmsg.close}" action="wai:browse-wai:close" />
        </circabc:panel>
        <f:verbatim><br /><br /><br /></f:verbatim>
        <%--  The action list --%>
		<circabc:panel id="id-divspacer10px" styleClass="divspacer10px" />
       	<circabc:panel id="panelActions" label="#{cmsg.actions}" tooltip="#{cmsg.actions_tooltip}" styleClass="panelActionsGlobal" styleClassLabel="panelActionsLabel" rendered="#{LibContentDetailsBean.documentLocked == false}">
           <circabc:actions id="actions_doc" value="doc_details_actions_wai" context="#{LibContentDetailsBean.document}" vertical="true"/>
        </circabc:panel>
    </circabc:panel>

    <%-- the main content --%>
    <circabc:panel id="contentMainForm" styleClass="contentMainForm">

		<circabc:displayer id="library-displayer-locked" rendered="#{LibContentDetailsBean.documentLocked}">
		    <%-- If the current document is a locked, display details and a link of the working copy --%>
            <circabc:panel id="working-copy" label="#{msg.working_copy_document}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{msg.document_details_panel_locked_detail_tooltip}" rendered="#{LibContentDetailsBean.workingCopyDocument != null}" >
                <h:outputText id="out-workingcopy" value="#{msg.working_copy_document}:&nbsp;&nbsp;" escape="false" />
                <circabc:actionLink id="act-details" rendered="#{LibContentDetailsBean.workingCopyDocument != null}" value="#{LibContentDetailsBean.workingCopyDocument.name}" tooltip="#{LibContentDetailsBean.workingCopyDocument.name}" actionListener="#{BrowseBean.clickWai}" >
                    <circabc:param name="id" value="#{LibContentDetailsBean.workingCopyDocument.id}" />
                </circabc:actionLink>
                <f:verbatim><br /></f:verbatim>
                <h:outputText id="out-workingcopy-space" value="&nbsp;" escape="false" />
            </circabc:panel>
            <f:verbatim><br /></f:verbatim>
		</circabc:displayer>

        <%-- Display the properties  --%>
        <circabc:panel id="panelDocumentDetails" label="#{msg.properties}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_detail_tooltip}" >
        	<circabc:panel id="ml-det-padd-list" styleClass="pad8px" />
            <circabc:panel id="contentHeaderSubPanelDocdet">
                <circabc:panel id="contentHeaderIconDocdet" styleClass="contentHeaderIcon" >
                    <circabc:actionLink id="doc-logo1" value="#{LibContentDetailsBean.document.name}" tooltip="#{cmsg.library_content_link_tooltip}: #{LibContentDetailsBean.document.name}" href="#{LibContentDetailsBean.document.properties.url}" target="new" image="#{LibContentDetailsBean.document.properties.fileType32}" showLink="false" />
                </circabc:panel>
                <circabc:panel id="contentHeaderTextDocdet">
                    <circabc:propertySheetGrid id="document-props" value="#{LibContentDetailsBean.document}" var="documentProps" columns="1"
                            mode="view"  labelStyleClass="propertiesLabelTiny" cellpadding="2" cellspacing="2" externalConfig="true" />
                    <h:message id="msg1" for="document-props" styleClass="statusMessage" />
                </circabc:panel>
            </circabc:panel>
        </circabc:panel>
		
        <circabc:panel id="topOfPageAnchorDocDetails" styleClass="topOfPageAnchor">
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-doc-det-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-doc-det-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>
		
		<%-- Include the notification status panel --%>
	   	<%@ include file="/jsp/extension/wai/dialog/notification/notification-status-panel.jsp" %>
		
        <%-- Display the external repositories properties --%>
        <c:if test="${LibContentDetailsBean.publishedInExternalRepository}">
	        <circabc:panel id="panelPublishInExternalRepository" label="#{cmsg.details_external_repositories_information}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.details_external_repositories_information_tooltip}">
	        	<circabc:panel id="det-padd-listER" styleClass="pad8px" />
				<c:forEach var="repositoryInfo" items="${LibContentDetailsBean.repositoriesInfo}">
					<div id="panelDocumentDetailsHeader" class="panelDocumentDetailsLabel">
						<img src="${currentContextPath}/images/extension/storage_icon_small.png" class="panelDocumentDetailsLabel" /><c:out value="${repositoryInfo.key}" />
					</div>
					<div id="contentHeaderTextDocdetGlobal">
						<table cellpadding="2" cellspacing="2">
							<tbody>
								<c:forEach var="docInfo" items="${repositoryInfo.value}">
									<tr><td></td><th class="propertiesLabelTiny"><c:out value="${docInfo.key}: " /></th><td align="left">
									<c:set var="value" value="${docInfo.value}"/>
									<c:if test="${fn:startsWith(value, 'http://') || fn:startsWith(value, 'https://')}">
										<a href="<c:out value="${value}" />" target="_blank"><c:out value="${value}" /></a>
									</c:if>
									<c:if test="${!fn:startsWith(value, 'http://') && !fn:startsWith(value, 'https://')}">
										<c:out value="${value}" />
									</c:if>
									</td></tr>
								</c:forEach>
								<tr><td></td></tr>
							</tbody>
						</table>
					</div>
				</c:forEach>
	        </circabc:panel>
	        <circabc:panel id="topOfPageAnchoERProp" styleClass="topOfPageAnchor">
	            <%-- Display the "back to top icon first and display the text after." --%>
	            <circabc:actionLink id="act-link-er-prop-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
	            <circabc:actionLink id="act-link-er-prop-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	        </circabc:panel>
		</c:if>
		
        <%-- Multilingual properties (if ml aspect applied) --%>
        <circabc:panel id="panelDocumentMlProps" label="#{msg.ml_content_info}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_mldetail_tooltip}" rendered="#{LibContentDetailsBean.multilingual}" >
            <%--  The details of the mlContainer --%>
            <circabc:propertySheetGrid id="ml-container-props-sheet" value="#{LibContentDetailsBean.documentMlContainer}" var="mlContainerProps" columns="1"
                            labelStyleClass="propertiesLabel" externalConfig="true" cellpadding="2" cellspacing="2" mode="view"/>

            <%-- Separator blank space --%>
            <circabc:panel id="ml-det-padd-1" styleClass="pad8px" />

            <%--  The list of translations --%>
            <circabc:panel label="#{msg.related_translations}" id="related-translation-panel"  styleClass="inner_panel" styleClassLabel="panelLabel" >

	            <%-- list of translations --%>
	            <circabc:richList id="TranslationList" viewMode="circa" value="#{LibContentDetailsBean.translations}"
	                      var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"
	                      rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
	                      pageSize="10" initialSortColumn="Name" initialSortDescending="false">

	                <%-- Name and icon columns --%>
	                <circabc:column id="col21">
	                    <f:facet name="small-icon">
	                        <h:graphicImage id="col21-icon" url="/images/filetypes/_default.gif" width="16" height="16" alt="#{msg.name} "/>
	                    </f:facet>
	                    <f:facet name="header">
	                        <circabc:sortLink id="col21-sortlink" label="#{msg.name}" value="Name" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
	                    </f:facet>
	                        <circabc:actionLink id="view-name" value="#{r.name}" tooltip="#{r.name}" href="#{r.url}" target="new" />
	                </circabc:column>

	                <%-- Language columns --%>
	                <circabc:column id="col22" >
	                    <f:facet name="header">
	                        <circabc:sortLink id="col22-link" label="#{msg.language}" value="language" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
	                    </f:facet>
	                    <circabc:actionLink id="view-language" value="#{r.language}" tooltip="#{r.language}" href="#{r.url}" target="new" />
	                </circabc:column>

	                <%-- view actions --%>
	                <circabc:column id="col25" >
	                    <f:facet name="header">
	                        <h:outputText id="col25-txt" value="#{msg.actions}"/>
	                    </f:facet>
	                    <circabc:actionLink id="view_details" value="#{cmsg.contentDetail}" tooltip="#{cmsg.contentDetail}" actionListener="#{BrowseBean.clickWai}" >
	                        <circabc:param id="col25-param" name="id" value="#{r.id}" />
	                    </circabc:actionLink>
	                </circabc:column>
					<circabc:dataPager id="lib-content-details-ml-pager-2" styleClass="pagerCirca" />
	            </circabc:richList>
            </circabc:panel>
            <%-- Separator blank space --%>
            <circabc:panel id="ml-det-padd-end" styleClass="pad8px" />
        </circabc:panel>


        <%-- Multilingual properties (if NO ml aspect applied) --%>
        <circabc:panel id="panelDocumentNoMlProps" label="#{msg.ml_content_info}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_mldetail_tooltip}" rendered="#{!LibContentDetailsBean.multilingual}">
            <h:outputText id="no-ml-msg" value="#{msg.not_multilingual}" />
            <%-- Action - Add Translation --%>
            <r:permissionEvaluator value="#{LibContentDetailsBean.document}" allow="Write" id="eval_make-multilingual">
                <circabc:panel id="no-ml-actions" styleClass="pad16px">
                    <circabc:actionLink id="act-make-multilingual" value="#{msg.make_multilingual}" tooltip="#{msg.make_multilingual}" action="wai:dialog:makeMultilingualWai" actionListener="#{DialogManager.setupParameters}" showLink="true" image="/images/icons/make_ml.gif" rendered="#{LibContentDetailsBean.documentLocked == false && LibContentDetailsBean.workingCopy == false}" >
                        <circabc:param name="id" value="#{LibContentDetailsBean.document.id}" />
                    </circabc:actionLink>
                </circabc:panel>
            </r:permissionEvaluator>
        </circabc:panel>

        <circabc:panel id="topOfPageAnchoMlProp" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-ml-prop-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-ml-prop-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>


        <%-- Version history (if versionnable aspect applied) --%>
        <circabc:panel id="panelDocumentVersionHistory" label="#{msg.version_history}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_version_hitory_tooltip}" rendered="#{LibContentDetailsBean.versionable}">

            <circabc:richList id="versionHistoryList" viewMode="circa" value="#{LibContentDetailsBean.versionHistory}"
                  var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"
                  rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
                  pageSize="10" initialSortColumn="versionLabel" initialSortDescending="false">

                <%-- Primary column for details view mode --%>
                <circabc:column id="col1" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.version}" value="versionLabel" mode="case-insensitive" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <circabc:actionLink id="label" value="#{r.versionLabel}" tooltip="#{r.versionLabel}" href="#{r.url}" target="new" />
                </circabc:column>

                <%-- Version notes columns --%>
                <circabc:column id="col2" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.notes}" value="notes" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <h:outputText id="notes" value="#{r.notes}" />
                </circabc:column>

                <%-- Description columns --%>
                <circabc:column id="col3" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.author}" value="author" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <h:outputText id="author" value="#{r.author}" />
                </circabc:column>

                <%-- Created Date column for details view mode --%>
                <circabc:column id="col4" >
                    <f:facet name="header">
                        <circabc:sortLink label="#{msg.date}" value="versionDate" styleClass="header" tooltipAscending="#{cmsg.tooltipAscending}" tooltipDescending="#{cmsg.tooltipDescending}"/>
                    </f:facet>
                    <h:outputText id="date" value="#{r.versionDate}">
                        <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
                    </h:outputText>
                </circabc:column>

                <%-- view the contents of the specific version --%>
                <circabc:column id="col5" >
                    <f:facet name="header">
                        <h:outputText value="#{msg.actions}"/>
                    </f:facet>
                    <circabc:actionLink id="view-link" value="#{msg.view}" tooltip="#{msg.view}" href="#{r.url}" target="new" />

                    <%-- Add the view versionned properties action. --%>
                    <h:outputText id="space-vh" value=" " />

                    <circabc:actionLink id="view-version-props" value="#{msg.properties}" tooltip="#{msg.properties}" action="wai:dialog:showContentHistoryWai" actionListener="#{CircabcContentHistoryDialog.init}" >
                        <circabc:param name="id" value="#{LibContentDetailsBean.document.id}" />
                        <circabc:param name="versionLabel" value="#{r.versionLabel}" />
                    </circabc:actionLink>
                </circabc:column>
				<circabc:dataPager id="lib-content-details-noml-pager" styleClass="pagerCirca" />
            </circabc:richList>
        </circabc:panel>


        <%-- Version history (if NO versionnable aspect applied) --%>
        <circabc:panel id="panelDocumentNoVersionHistory" label="#{msg.version_history}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.document_details_panel_version_hitory_tooltip}" rendered="#{!LibContentDetailsBean.versionable}">
            <h:outputText id="no-history-msg" value="#{msg.not_versioned}"  />
            <r:permissionEvaluator value="#{LibContentDetailsBean.document}" allow="Write" id="eval_ver">
	            <circabc:panel id="no-version-actions" styleClass="pad16px">
                	<circabc:actionLink id="make-versionable" value="#{msg.allow_versioning}" tooltip="#{msg.allow_versioning}" image="/images/icons/versionHistory_icon.gif"
                                     action="#{LibContentDetailsBean.applyVersionable}" rendered="#{LibContentDetailsBean.documentLocked == false}" />
                </circabc:panel>
            </r:permissionEvaluator>

        </circabc:panel>
        
        <circabc:panel id="topOfPageAnchorDocWkf" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-doc-wkf-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-doc-wkf-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>
		
        <circabc:panel label="#{msg.workflows}" id="workflow-panel"  styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{msg.workflows}" rendered="false" >
			<r:nodeWorkflowInfo id="workflow-info" value="#{LibContentDetailsBean.document}" />
		</circabc:panel>

        <circabc:panel id="topOfPageAnchorVerHist" styleClass="topOfPageAnchor" rendered="false" >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-ver-hist-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-ver-hist-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>
        
    </circabc:panel>
</circabc:panel>

