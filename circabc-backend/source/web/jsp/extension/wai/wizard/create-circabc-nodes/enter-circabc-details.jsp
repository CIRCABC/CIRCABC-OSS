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

<circabc:panel id="enter-circabc-details-section-1" styleClass="signup_rub_title" >
	<h:outputText value="&nbsp;#{msg.properties}" escape="false" />
</circabc:panel>


<f:verbatim>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td align="middle">
         </f:verbatim>
         <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.name}:" />
         <f:verbatim>
      </td>
      <td width="85%">
         </f:verbatim>
         <h:inputText id="name" value="#{DialogManager.bean.name}" size="35" maxlength="1024"
                      disabled="#{DialogManager.bean.propertiesReadOnly}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.title}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="title" value="#{DialogManager.bean.title}" size="35" maxlength="1024" disabled="#{DialogManager.bean.propertiesReadOnly}" />
         <f:verbatim>
      </td>
   </tr>


   <tr>
      <td></td>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.description}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:inputText id="description" value="#{DialogManager.bean.description}" size="35" maxlength="1024" disabled="#{DialogManager.bean.propertiesReadOnly}" />
         <f:verbatim>
      </td>
   </tr>

   <tr>
      <td></td>
      <td>
         </f:verbatim>
      	     <h:outputText value="#{cmsg.igroot_home_contact_information}:" rendered="#{DialogManager.bean.contactInformationDisplayed}"/>
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
              <h:inputTextarea id="contact" value="#{DialogManager.bean.contact}" readonly="false" immediate="true" rows="5" cols="50" rendered="#{DialogManager.bean.contactInformationDisplayed}"/>
         <f:verbatim>
      </td>
   </tr>

   <tr>
      <td></td>
      <td>
         </f:verbatim>
      	<h:outputText value="#{msg.icon}:" />
         <f:verbatim>
     	</td>
      <td>
         <table border="0" cellpadding="0" cellspacing="0"><tr><td>
         </f:verbatim>
         <a:imagePickerRadioPanel id="space-icon" columns="6" spacing="4" value="#{DialogManager.bean.icon}"
                                  panelBorder="greyround" panelBgcolor="#F5F5F5">
            <a:listItems value="#{DialogManager.bean.icons}" />
         </a:imagePickerRadioPanel>
         <f:verbatim>
         </td></tr></table>
      </td>
   </tr>
</table>
</f:verbatim>
