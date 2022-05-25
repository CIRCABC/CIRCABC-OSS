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

<circabc:panel id="contentMainSaveSearch" styleClass="contentMainForm">

	<circabc:panel id="save-search--warning" styleClass="infoPanel" styleClassLabel="infoContent" rendered="true" >
		<h:graphicImage id="save-search-image-warning" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
		<h:outputText id="save-search-text-warning-spaces" value="&nbsp;&nbsp;" escape="false" />
		<h:outputFormat id="save-search-text-no-more" value="#{cmsg.save_search_dialog_no_dyn_prop_saved}"  />
	</circabc:panel>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="5" border="0"  >
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{msg.name}:&nbsp;" escape="false"/>
			<h:inputText id="name" value="#{CircabcSearchProperties.searchName}" size="35" maxlength="1024"/>

			<h:outputText value=" " />
			<h:outputText value="#{msg.description}:&nbsp;" escape="false"/>
			<h:inputText value="#{CircabcSearchProperties.searchDescription}" size="35" maxlength="1024" />
	</h:panelGrid>

	<h:selectBooleanCheckbox value="#{CircabcSearchProperties.searchSaveGlobal}" />
	<h:outputText value="#{msg.save_search_global}" />

</circabc:panel>

