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
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<%-- the main content --%>
<circabc:panel id="contentMainFormNodeDetailsDialog" styleClass="contentMainForm">

    <%-- Display the properties  --%>
    <circabc:panel id="panelNodeDetails" label="#{msg.properties}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel" tooltip="#{WaiDialogManager.bean.panelTooltip}" >
    	<circabc:panel id="node-det-padd-list" styleClass="pad8px" />
        <circabc:panel id="contentHeaderSubPanelDocdet">
            <circabc:panel id="contentHeaderIconDocdet" styleClass="contentHeaderIcon" >
                <circabc:actionLink id="node-logo1" value="#{WaiDialogManager.bean.actionNode.name}" actionListener="#{BrowseBean.clickWai}" tooltip="#{WaiDialogManager.bean.nodeIconAltText}"  target="new" image="/images/icons/#{DialogManager.bean.icon}.gif" showLink="false">
                	<circabc:param id="param" name="id" value="#{WaiDialogManager.bean.actionNode.id}" />
                </circabc:actionLink>
            </circabc:panel>
            <circabc:panel id="contentHeaderTextDocdet">
                <circabc:propertySheetGrid id="node-props" value="#{WaiDialogManager.bean.actionNode}" var="nodeProps" columns="1"
                        mode="view"  labelStyleClass="propertiesLabelTiny" cellpadding="2" cellspacing="2" externalConfig="true" />
                <h:message id="msg1" for="node-props" styleClass="statusMessage" />
            </circabc:panel>
        </circabc:panel>
    </circabc:panel>

    <circabc:panel id="topOfPageAnchorDocDetails" styleClass="topOfPageAnchor"  >
        <%-- Display the "back to top icon first and display the text after." --%>
        <circabc:actionLink id="act-link-doc-det-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
        <circabc:actionLink id="act-link-doc-det-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
    </circabc:panel>

	<%-- Include the notification status panel --%>
	<circabc:displayer id="node-det-display-notification-panel" rendered="#{WaiDialogManager.bean.eventsNotifiables == true}">
	   	<%@ include file="/jsp/extension/wai/dialog/notification/notification-status-panel.jsp" %>
   	</circabc:displayer>

</circabc:panel>

