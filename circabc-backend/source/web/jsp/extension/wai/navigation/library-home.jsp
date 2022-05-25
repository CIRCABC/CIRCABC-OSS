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

<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/jquery-1.10.2.min.js"></script>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/jquery-ui-1.9.2.min.js"></script>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/pdfobject_source.js"></script>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/preview.js"></script>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/circabc-webdav.js"></script>

<circabc:panel id="contentMainLibraryHome" styleClass="contentMain">

	<%-- the description of the interest group --%>
	<f:verbatim><br /></f:verbatim>

	<circabc:displayer rendered="#{InterestGroupLogoBean.mainPageDisplay}">
		<h:graphicImage binding="#{InterestGroupLogoBean.mainPageIconBinding}" id="ig-home-logo" />
	</circabc:displayer>
	<h:outputText style="margin-left:5px; margin-right:5px;" binding="#{InterestGroupLogoBean.mainPageDescBinding}" id="ig-home-description" value="#{LibraryBean.currentNodeDescription}" escape="false" />

	<h:outputText styleClass="inpage_information" id="library_elements" value="#{LibraryBean.spaceNumberOfElements}" />
	

	<h:outputText styleClass="infoPanel" id="mem_ig" value="#{LibraryBean.generateBackLinkText}" escape="false" rendered="#{LibraryBean.sessionIgRef != null }">
	</h:outputText>
	<h:commandButton id="close-back-link" value="#{cmsg.close}" action="#{LibraryBean.closeBackLinkText}" rendered="#{LibraryBean.sessionIgRef != null }"></h:commandButton>
	<f:verbatim><br /></f:verbatim>
<f:verbatim><br /></f:verbatim>

	
	<circabc:panel id="panelLibraryContainer" label="#{cmsg.library_panel_container_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.library_panel_container_tooltip}">
		<circabc:customList id="libraryContainerList" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{LibraryBean.containers}" configuration="#{LibraryBean.containerNavigationPreference}" />
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorLibHomeContainer" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.library != null}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorLibHomeContainer-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorLibHomeContainer-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelLibraryContent" label="#{cmsg.library_panel_content_label}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.library_panel_content_tooltip}">
		<circabc:customList id="libraryContentList" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{LibraryBean.contents}" configuration="#{LibraryBean.contentNavigationPreference}" />
		<f:verbatim>
			<div id="dialog">
				<div id="pdf" style="clear:left;margin-top:60px;"></div>
				<div id="spin-div" style="position:fixed; left:50%; top:50%;">
					<center><img id="spinner-img" src="<%=request.getContextPath()%>/images/extension/spinner-wait.gif" /></center>
				</div>
			</div>
		</f:verbatim>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorLibHomeContent" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.library != null}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorLibHomeContent-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorLibHomeContent-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

</circabc:panel>

<!-- Add this for the preview -->

<f:verbatim>
	<h:inputHidden id="dialogTitle" value="#{cmsg.inline_view_title}" />
	<h:inputHidden id="downloadText" value="#{cmsg.node_links_dialog_down}" />
	<h:inputHidden id="openUrlText" value="#{cmsg.node_links_dialog_open_url}" />
	<h:inputHidden id="detailsText" value="#{cmsg.library_container_detail_tooltip}" />
	<h:inputHidden id="sendToText" value="#{cmsg.send_email_to_attachment}" />
	<h:inputHidden id="actionText" value="#{cmsg.action}" />
	<h:inputHidden id="previewNotAvailableText" value="#{cmsg.preview_not_available}" />
</f:verbatim>

<script>
	
	$(function() {
		
		$("#dialog").dialog({
			autoOpen: false,
			title: document.getElementById("FormPrincipal:dialogTitle").value,
			closeOnEscape: true,
			modal: true,
			width: 800,
	        height: 600,
	        top: '50%',
	        left: '50%'
		});
		
		$("#spinner-img").hide();
	});
	
</script>
