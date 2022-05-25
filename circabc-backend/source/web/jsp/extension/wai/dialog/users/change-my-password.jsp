<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or – as soon they
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

<circabc:panel id="contentMainForm" styleClass="contentMainFormWithBorder">
		<h:outputText value="<br /><table><tr><td>" escape="false" />
			<h:outputText value="#{cmsg.change_own_password_username}: " />
		<h:outputText value="</td><td>" escape="false" />
			<h:inputText value="#{WaiDialogManager.bean.userName}" size="70" disabled="true" />
		<h:outputText value="</td></tr><tr><td>" escape="false" />
			<h:outputText value="#{cmsg.change_own_password_old_password}: " />
		<h:outputText value="</td><td>" escape="false" />
			<h:inputSecret value="#{WaiDialogManager.bean.oldPassword}" size="70" />
		<h:outputText value="</td></tr><tr><td>" escape="false" />
			<h:outputText value="#{cmsg.change_own_password_new_password}: " />
		<h:outputText value="</td><td>" escape="false" />
			<h:inputSecret value="#{WaiDialogManager.bean.newPassword}" size="70" />
		<h:outputText value="</td></tr><tr><td>" escape="false" />
			<h:outputText value="#{cmsg.change_own_password_confirm_password}: " />
		<h:outputText value="</td><td>" escape="false" />
			<h:inputSecret value="#{WaiDialogManager.bean.confirmPassword}" size="70" />
		<h:outputText value="</td></tr></table>" escape="false" />

		<circabc:panel id="topOfPageAnchorLast" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>
</circabc:panel>
