<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or – as soon they
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
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript">
  window.onload = pageLoaded;

   function pageLoaded()
   {
      document.getElementById("wizard:wizard-body:user-prop-user-name-input").focus();
      updateButtonState();
   }

   function updateButtonState()
   {
      if (document.getElementById("wizard:wizard-body:user-prop-user-name-input").value.length == 0 ||
          document.getElementById("wizard:wizard-body:user-prop-password-input").value.length == 0 ||
          document.getElementById("wizard:wizard-body:user-prop-confirm-input").value.length == 0)
      {
         document.getElementById("wizard:next-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
      }
   }

</script>
</f:verbatim>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="user-properties-section-1" styleClass="signup_rub_title" >
	<h:outputText id="user-prop-page-title" value="&nbsp;#{msg.general_properties}" escape="false" />
</circabc:panel>

<h:panelGrid id="user-prop-panel-2" columns="4" cellpadding="3" cellspacing="3" border="0">
	<h:graphicImage id="user-prop-user-name-req" value="/images/icons/required_field.gif" alt="Required Field" />
	<h:outputText id="user-prop-user-name" value="#{cmsg.username}"/>
	<h:inputText id="user-prop-user-name-input" value="#{WizardManager.bean.userName}"  maxlength="1024" size="35" disabled="#{WizardManager.bean.editMode}" onkeyup="updateButtonState();" onchange="updateButtonState();" />
	<h:message id="errors1" for="user-name" style="color:red" />

	<h:graphicImage id="user-prop-password-req" value="/images/icons/required_field.gif" alt="Required Field" />
	<h:outputText id="user-prop-password" value="#{cmsg.password}"/>
	<h:inputSecret id="user-prop-password-input" value="#{WizardManager.bean.password}" size="35" maxlength="1024" disabled="#{!WizardManager.bean.iscircadomain}" onkeyup="updateButtonState();" onchange="updateButtonState();" redisplay="true" />
	<h:message id="errors2" for="password" style="color:red" />

	<h:graphicImage id="user-prop-confirm-req" value="/images/icons/required_field.gif" alt="Required Field" />
	<h:outputText id="user-prop-confirm" value="#{cmsg.confirm_password}"/>
	<h:inputSecret id="user-prop-confirm-input" value="#{WizardManager.bean.confirm}" size="35" maxlength="1024" disabled="#{!WizardManager.bean.iscircadomain}" onkeyup="updateButtonState();" onchange="updateButtonState();" redisplay="true" />
	<h:message id="errors3" for="confirm" style="color:red" />


	<h:outputText id="user-prop-content-filter-language-req" value=""/>
	<h:outputText id="user-prop-content-filter-language" value="#{msg.content_language_filter}" />
	<%-- Content Language Filter drop-down selector --%>
	<h:selectOneMenu id="user-prop-content-filter-language-input" value="#{WizardManager.bean.contentFilterLanguage}" >
		 <f:selectItems value="#{WizardManager.bean.contentFilterLanguages}" />
	</h:selectOneMenu>
	<h:outputText id="user-prop-content-filter-language-blankk" value=""/>

</h:panelGrid>
