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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
	
<circabc:panel id="contentMainFormEditNodeDetails" styleClass="contentMainForm">
	<circabc:propertySheetGrid id="${DialogManager.bean.idPrefix}" value="#{DialogManager.bean.editableNode}"
                        var="spaceProps" columns="1" labelStyleClass="propertiesLabel"
                        externalConfig="true" cellpadding="2" cellspacing="2" />
    <h:outputText value="<br/>" escape="false" />
    <h:outputText style="font-style:italic; margin:8px;" value="#{DialogManager.bean.translationNote}" escape="false" />
</circabc:panel>


