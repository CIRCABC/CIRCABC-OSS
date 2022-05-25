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
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

	<circabc:panel id="translate-property-first-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.machine_translate_prop}"></h:outputText>
	</circabc:panel>
	
	<f:verbatim>
		<br />
	</f:verbatim>
	
	<h:outputText value="#{cmsg.machine_translate_common_from_language}:" ></h:outputText>
	
	
	<h:selectOneMenu id="source-language" onchange="this.form.submit();"
		valueChangeListener="#{MachineTranslationPropertyDialog.languageChanged}"
		value="#{MachineTranslationPropertyDialog.sourceLanguage}" immediate="true" style="width:20%;">
		<f:selectItems id="source-languages"
			value="#{MachineTranslationPropertyDialog.existingTranslations}" />
	</h:selectOneMenu>
	
	<h:outputText value="&nbsp;" escape="false"/>
	<h:outputText value="#{cmsg.machine_translate_common_text }: " style="margin-left:35px;"></h:outputText>
	<h:inputTextarea readonly="true" disabled="true" cols="33" rows="3"
		value="#{MachineTranslationPropertyDialog.text}" style="vertical-align:top;"></h:inputTextarea>
		
	<f:verbatim>
		<br /><br />
	</f:verbatim>
	
	<h:outputText value="#{cmsg.machine_translate_common_target_languages}:"></h:outputText>
	<f:verbatim>
		<br /><br />
	</f:verbatim>
	<h:selectBooleanCheckbox id="select" value="#{MachineTranslationPropertyDialog.selectAll}"
		valueChangeListener="#{MachineTranslationPropertyDialog.selectAllChanged}"
		onchange="submit()" style="margin-left:50px;"></h:selectBooleanCheckbox>
	<h:outputLabel value="#{cmsg.machine_translate_common_select_all}" for="select" />
	<f:verbatim>
		<br /><br />
	</f:verbatim>
	<t:selectManyCheckbox
		value="#{MachineTranslationPropertyDialog.selectedTargetTranslations}"
		layoutWidth="4" style="margin-left:25px;">
		<f:selectItems id="translate-property-languages"
			value="#{MachineTranslationPropertyDialog.targetTranslations}" />
	</t:selectManyCheckbox>
	<f:verbatim>
		<br /><br />
	</f:verbatim>
	<h:selectBooleanCheckbox id="notify"  value="#{MachineTranslationPropertyDialog.notify}" style="margin-left:50px;"></h:selectBooleanCheckbox>
	<h:outputLabel value="#{cmsg.machine_translate_common_notify}" for="notify" />
	<f:verbatim>
		<br />
		<br />
	</f:verbatim>
	<h:graphicImage value="/images/extension/icons/warning.png" alt="disclaimer" style="margin-left:25px; margin-right:25px; width:48px; float:left;"></h:graphicImage>
	<h:outputText value="#{cmsg.machine_translate_common_no_guarantee}" escape="false"></h:outputText>
	<f:verbatim>
		<a href="https://mtatec.ec.testa.eu/mtatec/html/help_en.htm" target="new">https://mtatec.ec.testa.eu/mtatec/html/help_en.htm</a>
	</f:verbatim>