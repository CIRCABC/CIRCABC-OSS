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

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="refuse-applicant-section-1" styleClass="signup_rub_title" >
   <h:outputText value="&nbsp;#{cmsg.refusing_applicants}"/>
</circabc:panel>

<f:verbatim><br /></f:verbatim>

<h:outputText value="#{cmsg.refuse_applicant_users}" style="padding-top: 4px; padding-bottom: 4px; font-style: oblique;"/>

<h:dataTable value="#{DialogManager.bean.applicants}" var="row">
     <h:column>
      <h:graphicImage url="/images/icons/user_console.gif" />
     </h:column>
     <h:column>
        <h:outputText value="#{row.firstName}  #{row.lastName}  (#{row.login})" />
     </h:column>
 </h:dataTable>

<f:verbatim><br /></f:verbatim>

<h:outputText value="#{cmsg.send_email_to_dir_admins}" escape="false"/>
	<h:selectOneRadio id="SendNotificationDirAdminRadio" value="#{DialogManager.bean.notifyDirAdmins}"  >
		<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
		<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
</h:selectOneRadio>

<f:verbatim><br /></f:verbatim>

<h:outputText value="#{cmsg.send_email_to_refused_users}" />
	<h:selectOneRadio id="SendNotificationRefusedUsersRadio" value="#{DialogManager.bean.notifyRefusedUsers}"  >
		<f:selectItem itemValue="yes" itemLabel="#{msg.yes}" />
		<f:selectItem itemValue="no" itemLabel="#{msg.no}" />
</h:selectOneRadio>

<f:verbatim><br /></f:verbatim>

<circabc:panel id="refuse-applicant-section-2" styleClass="signup_rub_title" >
	<h:outputText value="#{cmsg.enter_optional_content}" style="padding-top: 4px; padding-bottom: 4px;"/>
</circabc:panel>

<f:verbatim><br /></f:verbatim>

<h:inputTextarea id="RefusalText" value="#{DialogManager.bean.message}" rows="10" cols="75" readonly="false"/>





