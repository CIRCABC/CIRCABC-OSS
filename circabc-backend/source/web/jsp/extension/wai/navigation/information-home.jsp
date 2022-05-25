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

<%@page import="eu.cec.digit.circabc.web.Beans"%>
<%@page import="eu.cec.digit.circabc.web.wai.bean.navigation.InformationBean"%>

<circabc:panel id="contentMainInformationHomeBrowse" styleClass="contentMain" rendered="#{InformationBean.editingView == false}">

	<circabc:displayer id="rendererIndexNotFound" rendered="#{InformationBean.indexFileFound == false}">
		<circabc:panel id="no-index-file--warning" styleClass="infoPanel" styleClassLabel="infoContent" >
			<h:graphicImage id="no-index-file-image-warning" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
			<h:outputText id="no-index-file-text-warning" value="&nbsp;&nbsp;#{cmsg.information_no_index_file}" escape="false" />
		</circabc:panel>
	</circabc:displayer>

	<circabc:displayer id="rendererIndexFound" rendered="#{InformationBean.indexFileFound == true}">
		<circabc:displayer id="renderNewWindow" rendered="#{InformationBean.renderNewWindow == true}">
				<f:verbatim>
					<script language="JavaScript">
						function open_mini_site()
						{
							new_window = window.open('<%=((InformationBean)Beans.getBean(InformationBean.BEAN_NAME)).getWebdavUrl(false)%>')
						}
						window.onLoad = open_mini_site();
					</script>
				</f:verbatim>
				<circabc:actionLink id="browseNewPage" value="#{cmsg.information_click_new_windows}" tooltip="#{cmsg.information_click_new_windows_tooltip}" immediate="true" showLink="false" href="#{InformationBean.webdavUrl}" target="new" />
			</circabc:displayer>

			<c:if test="${InformationBean.renderNewWindow == false}">
				<f:verbatim>
				<IFRAME src="<%=request.getContextPath()%><%=((InformationBean)Beans.getBean(InformationBean.BEAN_NAME)).getWebdavUrl()%>" frameborder="1" scrolling="auto" width="98%" height="600">
				</f:verbatim>
					<h:outputFormat id="no-iframe" value="#{cmsg.information_no_iframe}" escape="false">
						<circabc:param value="<%=request.getContextPath()%>" />
						<circabc:param value="<%=((InformationBean)Beans.getBean(InformationBean.BEAN_NAME)).getWebdavUrl()%>" />
					</h:outputFormat>
				<f:verbatim>
				</IFRAME>
				</f:verbatim>
			</c:if>
	</circabc:displayer>
</circabc:panel>


<circabc:panel id="contentMainInformationHomeEdit" styleClass="contentMain" rendered="#{InformationBean.editingView}">
	<!--  Action preview -->
	<circabc:panel id="edit-information-preview" styleClass="wai_dialog_more_action">
		<h:graphicImage value="/images/icons/comparetocurrent.png" alt="#{cmsg.information_preview_action_image_tooltip}" />
		<h:outputText id="edit-information-preview-spaces" value="&nbsp;" escape="false" />
		<circabc:actionLink id="previewNewPage" value="#{cmsg.information_preview_action_title}" tooltip="#{cmsg.information_preview_action_tooltip}" immediate="true" showLink="false" href="#{InformationBean.webdavUrl}" target="new"/>
	</circabc:panel>

	<f:verbatim>
		<br /><br />
	</f:verbatim>

	<circabc:panel id="panelInformationContainer" label="#{cmsg.library_panel_container_label}" styleClass="panelLibraryContainerGlobal" styleClassLabel="panelLibraryContainerLabel" tooltip="#{cmsg.library_panel_container_tooltip}">
		<circabc:customList id="InformationContainerList" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{InformationBean.containers}" configuration="#{InformationBean.containerNavigationPreference}" />
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorInfHomeContainer" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.information != null}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorInfHomeContainer-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorInfHomeContainer-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>

	<circabc:panel id="panelInformationContent" label="#{cmsg.library_panel_content_label}" styleClass="panelLibraryContentGlobal" styleClassLabel="panelLibraryContentLabel" tooltip="#{cmsg.library_panel_content_tooltip}">
		<circabc:customList id="informationContentList"  styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{InformationBean.contents}" configuration="#{InformationBean.contentNavigationPreference}" />
		<f:verbatim>
			<div id="dialog">
				<div id="pdf" style="clear:left;margin-top:60px;"></div>
				<div id="spin-div" style="position:fixed; left:50%; top:50%;">
					<center><img id="spinner-img" src="<%=request.getContextPath()%>/images/extension/spinner-wait.gif" /></center>
				</div>
			</div>
		</f:verbatim>
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorInfHomeContent" styleClass="topOfPageAnchor" rendered="#{NavigationBean.currentIGRoot.information != null}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorInfHomeContent-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorInfHomeContent-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
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
