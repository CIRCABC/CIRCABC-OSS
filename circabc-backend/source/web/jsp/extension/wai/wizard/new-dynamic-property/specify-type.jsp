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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<h:outputText value="1.&nbsp;#{cmsg.new_dynamic_property_section_1}"  escape="false"/>
<f:verbatim>
	<br /><br />
</f:verbatim>
<h:selectOneMenu id="PropertyType" value="#{WizardManager.bean.type}">
	<f:selectItems value="#{WizardManager.bean.types}" />
</h:selectOneMenu>
<f:verbatim>
	<br /><br />
</f:verbatim>
<h:outputText value="2.&nbsp;#{cmsg.new_dynamic_property_section_2}" escape="false" />
<f:verbatim>
	<br /><br />
</f:verbatim>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
	<h:inputTextarea  id="PropertyValue" value="#{WizardManager.bean.validValues}" rows="5" cols="50" />
	<h:outputText value="#{cmsg.new_dynamic_property_section_separetd_value}"  style="font-style: italic;"/>
</h:panelGrid>
