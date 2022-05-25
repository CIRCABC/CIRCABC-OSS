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

<circabc:panel id="contentMainRejectPost" styleClass="contentMainForm">

		<f:verbatim><br /></f:verbatim>

		<!--  Fill the message -->

		<circabc:panel id="reject-post-section-reason" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.moderation_reject_post_dialog_section_message}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:inputTextarea id="reject-reason" value="#{WaiDialogManager.bean.message}" rows="5" cols="50"/>

		<!--  Details of the post-->

		<f:verbatim><br /><br /></f:verbatim>

		<circabc:panel id="reject-post-section-post-details" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.moderation_reject_post_dialog_section_details}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:outputFormat id="post-details" value="#{cmsg.newsgroups_post_created_by_on}" escape="false">
			<circabc:param value="#{WaiDialogManager.bean.creator}" />
			<circabc:param value="#{WaiDialogManager.bean.created}"  />
		</h:outputFormat>

		<f:verbatim><br /><br /></f:verbatim>

		<circabc:panel id="panelNewsGroupsPost" label="#{cmsg.newsgroups_post_label}" styleClass="panelNewsGroupsPostGlobal" styleClassLabel="panelNewsGroupsPostLabel" tooltip="#{cmsg.newsgroups_post_label_tooltip}">
		<h:outputText value="#{WaiDialogManager.bean.content}" />
		</circabc:panel>



		<circabc:panel id="topOfPageAnchorPost" styleClass="topOfPageAnchor" >
			<%-- Display the "back to top icon first and display the text after." --%>
			<circabc:actionLink id="topOfPageAnchorPost-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
			<circabc:actionLink id="topOfPageAnchorPost-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
		</circabc:panel>

</circabc:panel>
