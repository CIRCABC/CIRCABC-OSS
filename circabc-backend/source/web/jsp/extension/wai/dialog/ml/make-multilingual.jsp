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

<%@ page isELIgnored="false"%>

	<circabc:panel id="make-multilingual-panel-label" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.make_multilingual_panel_label}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>

	<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
		<h:outputText value="&nbsp;#{msg.author}:&nbsp;&nbsp;" escape="false"/>
		<h:inputText id="author" value="#{DialogManager.bean.author}"  maxlength="1024" size="35" immediate="false"/>

		<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
		<h:outputText value="&nbsp;#{msg.language}:&nbsp;&nbsp;" escape="false"/>
		<h:selectOneMenu id="language" value="#{DialogManager.bean.language}" immediate="false" >
			<f:selectItem  itemLabel="#{msg.select_language}"  itemValue="null"/>
			<f:selectItems value="#{DialogManager.bean.filterLanguages}"/>
		</h:selectOneMenu>
	</h:panelGrid>

	<f:verbatim>
		<br />
	</f:verbatim>


	<circabc:panel id="make-multilingual-other-options" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.make_multilingual_other_options}"  />
	</circabc:panel>

	<f:verbatim>
		<br />
	</f:verbatim>
	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
	<h:outputText value="#{cmsg.make_multilingual_after_close_label}" />
	<h:selectOneRadio id="selectActionAfterCloseEnt" rendered="#{CircabcMakeMultilingualDialog.machineTranslationEnabled == true}" value="#{DialogManager.bean.actionAfterClose}">
		<f:selectItem itemValue="nothing" itemLabel="#{cmsg.make_multilingual_after_close_nothing}" />
		<f:selectItem itemValue="addTranslation" itemLabel="#{cmsg.make_multilingual_after_close_add_translation}" />
		<f:selectItem  itemValue="machineTranslation" itemLabel="#{cmsg.make_multilingual_after_close_automatic_translation}" />
	</h:selectOneRadio>
	<h:selectOneRadio id="selectActionAfterCloseOss" rendered="#{CircabcMakeMultilingualDialog.machineTranslationEnabled == false}" value="#{DialogManager.bean.actionAfterClose}">
		<f:selectItem itemValue="nothing" itemLabel="#{cmsg.make_multilingual_after_close_nothing}" />
		<f:selectItem itemValue="addTranslation" itemLabel="#{cmsg.make_multilingual_after_close_add_translation}" />
	</h:selectOneRadio>
	</h:panelGrid>
	
	
