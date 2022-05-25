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

<circabc:panel id="contentMainPost" styleClass="contentMain">

	<circabc:panel id="contentMainButton" styleClass="contentMainButton">
		<circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
			<h:commandButton id="close-button" styleClass="dialogButton" value="#{cmsg.close}" action="wai:browse-wai:close" />
		</circabc:panel>
	</circabc:panel>

	<circabc:panel id="contentMainForm" styleClass="contentMainForm">

		<f:verbatim><br /></f:verbatim>

		<h:outputFormat id="post-details" value="#{cmsg.newsgroups_post_created_by_on}"  styleClass="noItem">
			<circabc:param value="#{WaiNavigationManager.bean.creator}" />
			<circabc:param value="#{WaiNavigationManager.bean.created}" />
		</h:outputFormat>

		<circabc:displayer rendered="#{WaiNavigationManager.bean.reply}">
			<circabc:actionLink value="" tooltip="#{cmsg.newsgroups_post_on_reply_of_tooltip}"  actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();" styleClass="noItem">
				<h:outputFormat id="post-related-post" value="&nbsp;#{cmsg.newsgroups_post_on_reply_of}" escape="false">
					<circabc:param value="#{WaiNavigationManager.bean.referenceCreator}" />
					<circabc:param value="#{WaiNavigationManager.bean.referenceDate}" />
				</h:outputFormat>
				<circabc:param name="id" value="#{WaiNavigationManager.bean.referenceId}" > </circabc:param>
			</circabc:actionLink>
		</circabc:displayer>

		<f:verbatim><br /><br /></f:verbatim>

		<circabc:panel id="panelNewsGroupsPost" label="#{cmsg.newsgroups_post_label}" styleClass="panelNewsGroupsPostGlobal" styleClassLabel="panelNewsGroupsPostLabel" tooltip="#{cmsg.newsgroups_post_label_tooltip}">
			<h:outputText value="#{WaiNavigationManager.bean.message}" escape="false"></h:outputText>
		</circabc:panel>

		<circabc:panel id="topOfPageAnchorPost" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink id="topOfPageAnchorPost-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink id="topOfPageAnchorPost-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>


		<circabc:displayer rendered="#{WaiNavigationManager.bean.attachementAvailable}">
			<circabc:panel id="panelAttachements" label="#{cmsg.newsgroups_post_related_elements}" styleClass="panelNewsGroupsPostGlobal" styleClassLabel="panelNewsGroupsPostLabel" tooltip="#{cmsg.newsgroups_post_related_elements}">
				<c:set var="count" value="0" />
				<f:verbatim><table cellpadding='4' ><tr></f:verbatim>
				<c:forEach var="attach" items="${WaiNavigationManager.bean.attachements}">
					<c:choose>
						<c:when test="${count == 4}">
	      					<f:verbatim></tr><tr></f:verbatim>
	      					<c:set var="count" value="1" />
	      				</c:when>
						<c:otherwise>
	      					<c:set var="count" value="${count + 1}" />
	      		 		</c:otherwise>
		      	    </c:choose>
		      	    <f:verbatim><td></f:verbatim>
					<c:choose>
						<c:when test="${attach.isContainer == true}">
	      					<circabc:actionLink value=" ${attach.name}" tooltip="${attach.name}" image="/images/icons/${attach.smallIcon}.gif"  actionListener="#{BrowseBean.clickWai}"  onclick="showWaitProgress();">
								<circabc:param name="id" value="${attach.id}" />
							</circabc:actionLink>
	      				</c:when>
						<c:otherwise>
							<c:if test="${attach.downloadUrl == null}">
								<circabc:actionLink value=" ${attach.name}" tooltip="${attach.name}" image="${attach.fileType16}" href="javascript:void(0)" onclick="${attach.onclick}"/>
							</c:if>
							<c:if test="${attach.downloadUrl != null}">
								<circabc:actionLink value=" ${attach.name}" tooltip="${attach.name}" image="${attach.fileType16}" href="${attach.downloadUrl}" target="new"/>
							</c:if>
	      		 		</c:otherwise>
		      	    </c:choose>
		      	    <f:verbatim></td></f:verbatim>
				</c:forEach>
				<f:verbatim></tr></table></f:verbatim>

				<f:verbatim>
					<div id="dialog">
						<div id="pdf" style="clear:left;margin-top:60px;"></div>
						<div id="spin-div" style="position:fixed; left:50%; top:50%;">
							<center><img id="spinner-img" src="<%=request.getContextPath()%>/images/extension/spinner-wait.gif" /></center>
						</div>
					</div>
				</f:verbatim>

			</circabc:panel>
			<circabc:panel id="topOfPageAnchorAttachement" styleClass="topOfPageAnchor" >
				<%-- Display the "back to top icon first and display the text after." --%>
				<circabc:actionLink id="topOfPageAnchorAttach-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
				<circabc:actionLink id="topOfPageAnchorAttach-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
			</circabc:panel>
		</circabc:displayer>

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
