<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? � as soon they
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
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page buffer="32kb" pageEncoding="UTF-8"
	contentType="text/html; charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormCustomiseCategoryNodeDetails"
	styleClass="contentMainForm">

	
		<circabc:panel id="panel_form_customisation">
		<br/>
		<h:outputFormat id="panel_form_customisation_title" value="#{cmsg.category_customise_dialog_manage_look_and_feel_title}" styleClass="panel_header_little" >		</h:outputFormat>
		<br/>
		<h:outputLabel for="banner_customisation_title" value="#{cmsg.category_customise_dialog_manage_navigation_list_title}" ></h:outputLabel>
		<h:selectOneRadio id="banner_customisation_title" value="#{WaiDialogManager.bean.selectedRenderChoice}"> <!--  -->
			<f:selectItems value="#{WaiDialogManager.bean.selectRenderChoices}"/>
		</h:selectOneRadio>
		<h:outputText value="#{cmsg.category_customise_dialog_manage_navigation_list_description }"></h:outputText>
		<br/><br/>
		</circabc:panel>
		


</circabc:panel>