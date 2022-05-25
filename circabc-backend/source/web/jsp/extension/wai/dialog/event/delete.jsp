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

<circabc:panel id="deleteAppointmentDialogMainForm" styleClass="contentMainForm">

	<f:verbatim><br /></f:verbatim>
	<h:outputText value="#{WaiDialogManager.bean.confirmation} "/>

	<f:verbatim><br /></f:verbatim>

	<h:selectOneRadio value="#{WaiDialogManager.bean.occurenceChoice}" rendered="#{WaiDialogManager.bean.recurrent}" layout="pageDirection" >
		<f:selectItem itemValue="Single" itemLabel="#{cmsg.event_delete_meeting_dialog_confirmation_del_this}" />
		<f:selectItem itemValue="FuturOccurences" itemLabel="#{cmsg.event_delete_meeting_dialog_confirmation_del_futur}" />
		<f:selectItem itemValue="AllOccurences" itemLabel="#{cmsg.event_delete_meeting_dialog_confirmation_del_series}" />
	</h:selectOneRadio>

	<f:verbatim><br /><i></f:verbatim>
		<h:outputText value="#{WaiDialogManager.bean.mailInformation}"/>
	<f:verbatim><i></f:verbatim>

</circabc:panel>