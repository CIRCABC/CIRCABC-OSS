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

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript" >
  window.onload = pageLoaded;

   function pageLoaded()
   {
      document.getElementById("FormPrincipal:first-name").focus();
      updateButtonState();
   }

   function updateButtonState()
   {
      if (document.getElementById("FormPrincipal:first-name-input").value.length == 0 ||
          document.getElementById("FormPrincipal:last-name-input").value.length == 0 ||
          (document.getElementById("FormPrincipal:email-input").value.length == 0 &&
           document.getElementById("FormPrincipal:email-input").disabled == false ) ||
           (document.getElementById("FormPrincipal:phone-id-input").value.length == 0 &&
           document.getElementById("FormPrincipal:phone-id-input").disabled == false ) ||
           (document.getElementById("FormPrincipal:postal-id-input").value.length == 0 &&
           document.getElementById("FormPrincipal:postal-id-input").disabled == false ) )
      {
         document.getElementById("FormPrincipal:wai-next-button").disabled = true;
      }
      else
      {
         document.getElementById("FormPrincipal:wai-next-button").disabled = false;
      }
   }

</script>
</f:verbatim>

<circabc:displayer id="displayer-panel-pers-properties-wiz-update" rendered="#{createCircabcUserWizard.readOnly == false}">

	<circabc:panel id="person-properties-section-1" styleClass="signup_rub_title" >
		<h:outputText id="pers-prop-page-title" value="&nbsp;#{msg.general_properties}" escape="false" />
	</circabc:panel>

	<%-- Modificattion allowed panel --%>
	<h:panelGrid id="panel-pers-properties-wiz-update" columns="3" cellpadding="3" cellspacing="3" border="0" >
		<h:graphicImage id="first-name-req" value="/images/icons/required_field.gif" alt="Required Field" />
		<h:outputText id="first-name" value="#{msg.first_name}:"/>
		<h:inputText  id="first-name-input" value="#{createCircabcUserWizard.firstName}" disabled="#{!createCircabcUserWizard.iscircadomain}" onkeyup="updateButtonState();" onchange="updateButtonState();" maxlength="1024" size="35"  />

		<h:graphicImage id="last-name-requ" value="/images/icons/required_field.gif" alt="Required Field" />
		<h:outputText id="last-name" value="#{msg.last_name}:"/>
		<h:inputText id="last-name-input" value="#{createCircabcUserWizard.lastName}" disabled="#{!createCircabcUserWizard.iscircadomain}" onkeyup="updateButtonState();" onchange="updateButtonState();" maxlength="1024" size="35" />

		<h:outputText id="title-outout-blank" value=""/>
		<h:outputText id="title" value="#{cmsg.person_title}:"/>
		<h:inputText id="title-input" value="#{createCircabcUserWizard.title}" disabled="#{!createCircabcUserWizard.iscircadomain}" maxlength="1024" size="35"  rendered="#{!createCircabcUserWizard.readOnly}" />

		<h:graphicImage id="email-requ" value="/images/icons/required_field.gif" alt="Required Field" />
		<h:outputText id="email" value="#{msg.email}:"/>
		<h:inputText id="email-input" value="#{createCircabcUserWizard.email}" disabled="#{!createCircabcUserWizard.iscircadomain}" onkeyup="updateButtonState();" onchange="updateButtonState();" maxlength="1024" size="35"  />

		<h:outputText id="company-id-blank" value=""/>
		<h:outputText id="company-id" value="#{msg.company_id}:"/>
		<h:inputText id="company-id-input" value="#{createCircabcUserWizard.companyId}" disabled="#{!createCircabcUserWizard.iscircadomain}" maxlength="1024" size="35"  />

		<h:graphicImage id="phone-id-req" value="/images/icons/required_field.gif" alt="Required Field" />
		<h:outputText id="phone-id" value="#{cmsg.phone}:"/>
		<h:inputText id="phone-id-input" value="#{createCircabcUserWizard.phone}" onkeyup="updateButtonState();" onchange="updateButtonState();" maxlength="1024" size="35" disabled="#{!createCircabcUserWizard.iscircadomain}" />

		<h:outputText id="fax-id-blank" value=""/>
		<h:outputText id="fax-id" value="#{cmsg.fax}:"/>
		<h:inputText id="fax-id-input" value="#{createCircabcUserWizard.fax}" maxlength="1024" size="35"  />

		<h:outputText id="url-id-blank" value=""/>
		<h:outputText id="url-id" value="#{cmsg.url}:"/>
		<h:inputText id="url-id-input" value="#{createCircabcUserWizard.url}" maxlength="1024" size="35"  />

		<h:graphicImage id="postal-id-req" value="/images/icons/required_field.gif" alt="Required Field" />
		<h:outputText id="postal-id" value="#{cmsg.postal_adress}:"/>
		<h:inputTextarea id="postal-id-input" value="#{createCircabcUserWizard.postalAddress}" onkeyup="updateButtonState();" onchange="updateButtonState();" rows="4" cols="60" disabled="#{!createCircabcUserWizard.iscircadomain}" readonly="false"/>

		<h:outputText id="description-person-blank" value=""/>
		<h:outputText id="description-person" value="#{msg.description}:"/>
		<h:inputTextarea id="description-person-input" value="#{createCircabcUserWizard.description}" rows="4" cols="60" readonly="false"/>

		<h:outputText id="orgdepnumber-id-blank" value=""/>
		<h:outputText id="orgdepnumber-id" value="#{cmsg.org_dep_num}:"/>
		<h:inputText id="orgdepnumber-id-input" value="#{createCircabcUserWizard.orgDepNumber}" disabled="true" maxlength="1024" size="35" />
	</h:panelGrid>
</circabc:displayer>
<%-- Read only panel --%>

<circabc:displayer id="displayer-panel-pers-properties-wiz-read-only" rendered="#{createCircabcUserWizard.readOnly}" >

	<circabc:panel id="contentMainFormManageApplicants" styleClass="contentMainFormWithBorder">

		<circabc:panel id="ro-person-properties-section-1" styleClass="signup_rub_title" >
			<h:outputText id="ro-pers-prop-page-title" value="&nbsp;#{msg.general_properties}" escape="false" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:panelGrid id="panel-pers-properties-wiz-read-only" columns="2" cellpadding="3" cellspacing="3" border="0" >

			<h:outputText id="read-first-name-1" value="#{msg.first_name}:"/>
			<h:outputText id="read-first-name-2" value="#{createCircabcUserWizard.firstName}" />

			<h:outputText id="read-last-name-1" value="#{msg.last_name}:"/>
			<h:outputText id="read-last-name-2" value="#{createCircabcUserWizard.lastName}" />

			<h:outputText id="read-email-1" value="#{msg.email}:"/>
			<h:outputText id="read-email-2" value="#{createCircabcUserWizard.email}" />

			<h:outputText id="read-title-1" value="#{cmsg.person_title}:"/>
			<h:outputText id="read-title-2" value="#{createCircabcUserWizard.title}" />

			<h:outputText id="read-postal-1" value="#{cmsg.postal_adress}:"/>
			<h:inputTextarea id="read-postal-2" value="#{createCircabcUserWizard.postalAddress}" rows="4" cols="60" readonly="true"/>

			<h:outputText id="read-phone-1" value="#{cmsg.phone}:"/>
			<h:outputText id="read-phone-2" value="#{createCircabcUserWizard.phone}" />

			<h:outputText id="read-comp-id-1" value="#{msg.company_id}:"/>
			<h:outputText id="read-comp-id-2" value="#{createCircabcUserWizard.companyId}" />

			<h:outputText id="read-desc-1" value="#{msg.description}:"/>
			<h:inputTextarea id="read-desc-2" value="#{createCircabcUserWizard.description}" rows="4" cols="60" readonly="true"/>

			<h:outputText id="read-org-dep-1" value="#{cmsg.org_dep_num}:"/>
			<h:outputText id="read-org-dep-2" value="#{createCircabcUserWizard.orgDepNumber}" />

			<h:outputText id="read-fax-1" value="#{cmsg.fax}:"/>
			<h:outputText id="read-fax-2" value="#{createCircabcUserWizard.fax}" />

			<h:outputText id="read-url-1" value="#{cmsg.url}:"/>
			<h:outputText id="read-url-2" value="#{createCircabcUserWizard.url}" />
		</h:panelGrid>
	</circabc:panel>
</circabc:displayer>


