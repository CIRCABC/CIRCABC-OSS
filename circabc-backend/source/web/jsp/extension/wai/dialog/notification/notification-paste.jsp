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


<%@ page isELIgnored="false" %>

<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainNotificationOnCopyMove" styleClass="contentMainForm">
	<h:outputText id="notify-paste-all-text" value="#{cmsg.notify_paste_all}" styleClass="mainSubTitle"  />
	<h:selectBooleanCheckbox id="notify-paste-all" value="#{WaiDialogManager.bean.notifyPasteAll}" />
	<f:verbatim><br /></f:verbatim>
	<h:outputText id="notify-paste-text" value="#{cmsg.notify_paste}" styleClass="mainSubTitle" />
	<h:selectBooleanCheckbox id="notify-on-move" value="#{WaiDialogManager.bean.notifyPaste}" />
</circabc:panel>


