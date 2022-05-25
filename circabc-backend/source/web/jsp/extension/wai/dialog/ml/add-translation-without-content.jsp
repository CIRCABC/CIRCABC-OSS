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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<circabc:panel id="add-empty-translation-section1" styleClass="signup_rub_title" tooltip="#{cmsg.add_translation_label_tooltip}">
	<h:outputText value="#{cmsg.manage_multilingual_details_properties}"  />
</circabc:panel>

<f:verbatim>
	<br />
</f:verbatim>

<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0" >
	<h:outputText value="" />
	<h:outputText value="#{msg.title}: " />
	<h:inputText id="translation_title" value="#{CircabcAddTranslationWithoutContentDialog.title}" maxlength="1024" size="35" immediate="false"/>

	<h:outputText value="" />
	<h:outputText value="#{msg.description}: "/>
	<h:inputTextarea id="translation_description" value="#{CircabcAddTranslationWithoutContentDialog.description}" rows="3" cols="55" readonly="false"/>

	<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
	<h:outputText value="#{msg.author}: "/>
	<h:inputText id="translation_author" value="#{CircabcAddTranslationWithoutContentDialog.author}" maxlength="1024" size="35" immediate="false"/>

	<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
	<h:outputText value="#{msg.language}: "/>
	<h:selectOneMenu id="translation_language" value="#{CircabcAddTranslationWithoutContentDialog.language}">
		<f:selectItem  itemLabel="#{msg.select_language}" itemValue="null"/>
		<f:selectItems value="#{CircabcAddTranslationWithoutContentDialog.unusedLanguages}"/>
	</h:selectOneMenu>
</h:panelGrid>

<f:verbatim>
	<br /><br />
</f:verbatim>

<circabc:panel id="add-empty-translation-section2" styleClass="signup_rub_title" tooltip="#{cmsg.add_translation_label_tooltip}">
	<h:outputText value="#{msg.other_properties}"  />
</circabc:panel>

<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
		<h:selectBooleanCheckbox id="check_modify" value="#{CircabcAddTranslationWithoutContentDialog.showOtherProperties}" />
		<h:outputText id="translation_modify" value="#{msg.modify_props_when_page_closes}"/><br /><br />
</h:panelGrid>