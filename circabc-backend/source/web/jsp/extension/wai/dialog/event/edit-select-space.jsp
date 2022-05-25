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
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/noscript.js" ></script>
<circabc:panel id="selectSpaceDialogMainForm" styleClass="contentMainForm">

	<%-- Include the the modify recurrence selection --%>
	<%@ include file="/jsp/extension/wai/dialog/event/modify-recurrence.jsp" %>

	<circabc:panel id="select-space-meeting-wz-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.event_create_meetings_wizard_step2_details}"  />
	</circabc:panel>
	<f:verbatim><br /></f:verbatim>
	<circabc:nodeSelector id="space-selector"
				rootNode="#{WaiDialogManager.bean.libraryId}"
				initialSelection="#{WaiDialogManager.bean.libraryId}"
				label="#{msg.select_destination_prompt}"
				value="#{WaiDialogManager.bean.appointment.librarySection}"
				pathLabel="#{cmsg.path_label}"
				pathErrorMessage="#{cmsg.path_error_message}"
				styleClass="selector" />

</circabc:panel>
