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


<%@ page isELIgnored="false" %>

<circabc:panel id="contentMainFormEditUserProfile" styleClass="contentMainForm">

	<circabc:panel id="manage-edit-profile-main-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.edit_user_profile_section}"  />
	</circabc:panel>

	<h:outputFormat value="#{cmsg.title_edit_users_profile}:&nbsp;&nbsp;&nbsp;" id="msg12" escape="false">
		<circabc:param value="#{WaiDialogManager.bean.fullName}" id="username-param" />
	</h:outputFormat>

	<h:selectOneMenu id="roles" style="width:250px" value="#{WaiDialogManager.bean.userProfileGroup}" >
		<f:selectItems value="#{WaiDialogManager.bean.profiles}" />
	</h:selectOneMenu>

	<f:verbatim><br /><br /></f:verbatim>

</circabc:panel>