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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
	
	<h:outputLabel value="#{cmsg.select_ig_col_ig}:" styleClass="propertiesLabelTiny"  />
	<h:selectOneMenu id="InterestGroups" value="#{WizardManager.bean.interestGroup}" >
		<f:selectItems value="#{WizardManager.bean.availableInterestGroups}" />
	</h:selectOneMenu>
	
	<h:outputLabel value="#{cmsg.select_ig_col_perm}:" styleClass="propertiesLabelTiny"  />
	<h:selectOneMenu id="MaximumPermision" value="#{WizardManager.bean.libraryPermission}" >
		<f:selectItems value="#{WizardManager.bean.orderedLibraryPermissions}" />
	</h:selectOneMenu>	
	
</h:panelGrid>


