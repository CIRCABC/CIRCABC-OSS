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

<circabc:panel id="contentMainForm" styleClass="contentMainForm">


	<circabc:displayer rendered="#{WaiDialogManager.bean.alreadySaved == false}">
		<circabc:panel id="adddiscuss-first-section" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.create_topic_first_section}"  />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>
	
		<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
	
			<h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
			<h:outputText value="#{cmsg.create_topic_subject}:" />
			<h:inputText id="topic_title" value="#{WaiDialogManager.bean.name}" maxlength="200" size="35" immediate="false"/>
	
			<h:outputText value=""/>
			<h:outputText value="#{cmsg.create_topic_icon}:"/>
			<a:imagePickerRadioPanel id="topic-icon" columns="6" spacing="4" value="#{WaiDialogManager.bean.icon}"
				panelBorder="greyround" panelBgcolor="#F5F5F5">
				<a:listItems value="#{WaiDialogManager.bean.icons}" />
			</a:imagePickerRadioPanel>
	
			<%--  Moderate --%>
			<h:outputText value="" />
			<h:outputText value="#{cmsg.newsgroups_forum_create_forum_apply_moderation}: " />
			<h:selectBooleanCheckbox id="moderate" value="#{WaiDialogManager.bean.moderated}" />
		</h:panelGrid>
		<f:verbatim><br /></f:verbatim>
	</circabc:displayer>	

	<circabc:panel id="adddiscuss-second-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.create_topic_second_section}"  />
	</circabc:panel>
	<f:verbatim><br /></f:verbatim>

	<%@ include file="/jsp/extension/wai/dialog/content/edit/edit-online.jsp" %>

</circabc:panel>
