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


	<circabc:panel id="edit-post--warning" styleClass="infoPanel" styleClassLabel="infoContent" rendered="#{DialogManager.bean.rejected}" >
		<h:graphicImage id="edit-post-image-warning" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
		<h:outputFormat id="edit-post-text-moderator" value="&nbsp;&nbsp;#{cmsg.edit_post_dialog_refused_by}&nbsp;" escape="false"  >
			<circabc:param value="<b>#{DialogManager.bean.rejectModerator}</b>" />
			<circabc:param value="<b>#{DialogManager.bean.rejectDate}</b>" />
		</h:outputFormat>
		<h:outputFormat id="edit-post-text-reason" value="#{cmsg.edit_post_dialog_refused_reason}" escape="false" rendered="#{DialogManager.bean.rejectReasonAvailable}" >
			<circabc:param value="<i>#{DialogManager.bean.rejectMessage}</i>" />
		</h:outputFormat>

		<f:verbatim><br /><br /></f:verbatim>

		<h:outputFormat id="edit-post-otherversions" value="#{cmsg.edit_post_dialog_refused_restore}" />

		<f:verbatim><br /><br /></f:verbatim>

		<h:selectOneMenu id="edit-post-version" value="#{DialogManager.bean.selectedVersion}"  >
			<f:selectItems id="edit-post-versions" value="#{DialogManager.bean.olderVersions}" />
		</h:selectOneMenu>

		<f:verbatim>&nbsp;&nbsp;</f:verbatim>

		<h:commandButton id="submit-select-version" styleClass="" value="#{cmsg.edit_post_dialog_select}" action="wai:dialog:close:wai:dialog:editPostWai" rendered="true" title="#{cmsg.edit_post_dialog_select_tooltip}" />
	</circabc:panel>



	<f:verbatim><br /></f:verbatim>

	<%@ include file="/jsp/extension/wai/dialog/content/edit/edit-online.jsp" %>

</circabc:panel>
