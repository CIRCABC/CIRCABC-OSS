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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<circabc:panel id="versionned-properties-section1" styleClass="signup_rub_title" tooltip="#{cmsg.document_details_panel_detail_tooltip}">
	<h:outputText value="#{cmsg.properties}" />
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<circabc:panel id="ml-det-padd-list" styleClass="pad8px" >
	<circabc:panel id="contentHeaderSubPanelDocdet">
   		<circabc:panel id="contentHeaderIconDocdet" styleClass="contentHeaderIcon" >
      		<circabc:actionLink id="doc-logo1" value="#{DialogManager.bean.name}" tooltip="#{DialogManager.bean.name}" href="#{DialogManager.bean.url}" target="new" image="#{DialogManager.bean.fileType32}" showLink="false" />
   		</circabc:panel>
   		<circabc:panel id="contentHeaderTextDocdet">
      		<circabc:propertySheetGrid id="document-props" value="#{DialogManager.bean.frozenStateDocument}" var="documentProps" columns="1"
                   mode="view"  labelStyleClass="propertiesLabelTiny" cellpadding="2" cellspacing="2" externalConfig="true" />
   		</circabc:panel>
   </circabc:panel>
</circabc:panel>

        <circabc:panel id="topOfPageAnchorDocDetails" styleClass="topOfPageAnchor"  >
            <%-- Display the "back to top icon first and display the text after." --%>
            <circabc:actionLink id="act-link-doc-det-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
            <circabc:actionLink id="act-link-doc-det-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
        </circabc:panel>
		
        <%-- Display the external repositories properties --%>
        <c:if test="${CircabcContentHistoryDialog.publishedInExternalRepository}">
	        <circabc:panel id="panelPublishInExternalRepository" label="#{cmsg.details_external_repositories_information}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{cmsg.details_external_repositories_information_tooltip}">
	        	<circabc:panel id="det-padd-listER" styleClass="pad8px" />
				<c:forEach var="repositoryInfo" items="${CircabcContentHistoryDialog.repositoriesInfo}">
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
