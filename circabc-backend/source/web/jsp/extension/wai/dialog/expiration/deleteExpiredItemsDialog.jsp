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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainForm" styleClass="contentMainForm">

	<circabc:panel id="panelMessage" styleClass="panelMessage">
		<h:outputText value="#{cmsg.delete_expired_items_dialog_warning}" />
	</circabc:panel>

	<h:dataTable value="#{WaiDialogManager.bean.confirmationItems}" var="item" 
		binding="#{WaiDialogManager.bean.confirmationTable}" styleClass="selectedItems">
		<h:column>
			<f:facet name="header">
				<h:outputText value="#{cmsg.name}"/>
			</f:facet>    
             <h:outputText value="#{item.name}"></h:outputText>
		</h:column>
		<h:column>
			<f:facet name="header" >
				<h:outputText value="#{cmsg.expiration_date}"/>
			</f:facet>
			<h:outputText value="#{item.expirationDate}"></h:outputText>
		</h:column>
		<h:column>
			<f:facet name="header" >
				<h:outputText value="#{cmsg.author}"/>
			</f:facet>    
			<h:outputText value="#{item.author}"></h:outputText>
		</h:column>
	</h:dataTable>

</circabc:panel>