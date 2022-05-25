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

	<circabc:panel id="confirmationPanel" styleClass="descIntroText" >
		<h:outputText value="#{cmsg.delete_category_confirmation_admin}" rendered="#{DeleteCategoryDialogBean.deleteCategoryNode}" />
		<h:outputText value="#{cmsg.delete_category_confirmation}" rendered="#{not empty DeleteCategoryDialogBean.nodesForDeletion}"/>
		<h:outputText value="#{cmsg.delete_category_nothing}" rendered="#{empty DeleteCategoryDialogBean.nodesForDeletion}"/>
	</circabc:panel>
	
	<circabc:panel id="confirmationListPanel" rendered="#{!DeleteCategoryDialogBean.deleteCategoryNode}">
		<circabc:richList id="confirmationList" 
			viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" 
			value="#{DeleteCategoryDialogBean.nodesForDeletion}" var="node" initialSortColumn="name"
			rendered="#{not empty DeleteCategoryDialogBean.nodesForDeletion}">
			
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{msg.name}" />
				</f:facet>
				<h:outputText value="#{node.name}" />
			</circabc:column>
			
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{msg.path}" />
				</f:facet>
				<h:outputText value="#{node.path}" />
			</circabc:column>
			
			<circabc:column>
				<f:facet name="header">
					<h:outputText value="#{msg.author}" />
				</f:facet>
				<h:outputText value="#{node.author}" />
			</circabc:column>
			
			<circabc:dataPager id="deleteCategory-pager" styleClass="pagerCirca" />
		</circabc:richList>
	</circabc:panel>
</circabc:panel>