<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? – as soon they
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
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/noscript.js" ></script>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
<circabc:panel id="contentMainForm" styleClass="contentMainForm">
	<circabc:panel id="panelRecoverItems">
		<circabc:panel id="panelRecoverItemConfirm">
			<h:outputText value="#{cmsg.recover_items_confirm}">
			</h:outputText>
		</circabc:panel>
		<circabc:panel id="panelRecoverItemsList">
			<h:outputText
				value="#{WaiDialogManager.bean.listedItemsTable}"
				escape="false" />
		</circabc:panel>
		<circabc:panel id="panelRecoverItemsMessage" styleClass="infoPanel"
			styleClassLabel="infoContent">
			<h:outputText value="#{cmsg.recover_items_message}">
			</h:outputText>
		</circabc:panel>
		<circabc:panel id="panelRecoverItemsSpaceSelector">
		<h:outputText value="#{msg.destination}" /><f:verbatim>:&nbsp; </f:verbatim>
			<circabc:nodeSelector id="space-selector"
				rootNode="#{CircabcTrashcanDialog.currentSpaceNode}"
				initialSelection="#{CircabcTrashcanDialog.currentSpaceNode}"
				label="#{msg.select_destination_prompt}"
				value="#{CircabcTrashcanDialogProperty.destination}"
				pathLabel="#{cmsg.path_label}"
				pathErrorMessage="#{cmsg.path_error_message}"
				styleClass="selector" />
		</circabc:panel>
	</circabc:panel>

</circabc:panel>

