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



<circabc:panel id="contentMainFormAdvancedSearch"
	styleClass="contentMainForm">

	<circabc:richList value="#{ViewUserSavedSearchDialog.savedSearches}"
		viewMode="circa" styleClass="recordSet"
		headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
		altRowStyleClass="recordSetRowAlt" initialSortDescending="false"
		initialSortColumn="label" var="u">
		<circabc:column id="label-column">
			<f:facet name="header">
				<h:outputText id="label-header" value="#{msg.name}"
					styleClass="header" />
			</f:facet>
			<h:outputText id="label-text" value="#{u.label}" />
		</circabc:column>
		<circabc:column id="description-column">
			<f:facet name="header">
				<h:outputText id="description-header" value="#{msg.description}"
					styleClass="header" />
			</f:facet>
			<h:outputText id="description-text" value="#{u.description}" />
		</circabc:column>
		<circabc:column id="delete-search-action-column">
			<circabc:actionLink id="delete-search-action-remove"
				value="delete search" tooltip="delete search"
				image="/images/icons/delete.gif" showLink="false"
				action="wai:dialog:deleteSearchWai"
				actionListener="#{WaiDialogManager.setupParameters}">
				<circabc:param id="delete-search-action-edit-par-id" name="id"
					value="#{u.value}" />
				<circabc:param id="delete-search-action-edit-par-service"
					name="service" value="Administration" />
				<circabc:param id="delete-search-action-edit-par-activity"
					name="activity" value="Delete search" />
			</circabc:actionLink>
		</circabc:column>

		<f:facet name="empty">
			<h:outputFormat id="no-items-category-results"
				value="no saved search" />
		</f:facet>
	</circabc:richList>
</circabc:panel>




