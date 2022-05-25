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

<circabc:panel id="contentMainTopic" styleClass="contentMain">

	<!--  Actions  -->
	<h:panelGrid columns="1" cellpadding="2" cellspacing="1" border="0" id="filter-forum-action-panel" styleClass="wai_dialog_more_action" >

			<circabc:displayer rendered="#{WaiNavigationManager.bean.filterPendingAllowed == true}">
				<h:graphicImage value="/images/icons/filter.gif" title="#{cmsg.newsgroups_topic_subject_action_filter_title}" alt="#{cmsg.newsgroups_topic_subject_action_filter_title}" />
				<circabc:actionLink id="filter-forum-action" value="#{cmsg.newsgroups_topic_subject_action_filter_title}" tooltip="#{cmsg.newsgroups_topic_subject_action_filter_tooltip}" actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();"  >
					<circabc:param id="filter-forum-action-id" name="id" value="#{NavigationBean.currentNode.id}" />
					<circabc:param id="filter-forum-action-boolean" name="hide" value="true" />
				</circabc:actionLink>
			</circabc:displayer>
			<circabc:displayer rendered="#{WaiNavigationManager.bean.filterAllAllowed == true}">
				<h:graphicImage value="/images/icons/filter.gif" title="#{cmsg.newsgroups_topic_subject_action_all_title}" alt="#{cmsg.newsgroups_topic_subject_action_all_title}" />
				<circabc:actionLink id="filter-forum-action" value="#{cmsg.newsgroups_topic_subject_action_all_title}" tooltip="#{cmsg.newsgroups_topic_subject_action_all_tooltip}" actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();"  >
					<circabc:param id="filter-forum-action-id" name="id" value="#{NavigationBean.currentNode.id}" />
					<circabc:param id="filter-forum-action-boolean" name="hide" value="false" />
				</circabc:actionLink>
			</circabc:displayer>

			<circabc:displayer rendered="#{NavigationBean.guest == false}">
				<h:graphicImage value="/images/extension/icons/edit_notification.png" title="#{cmsg.newsgroups_topic_subject_action_watch_title}" alt="#{cmsg.newsgroups_topic_subject_action_watch_title}" />
				<circabc:actionLink id="watch-forum-action" value="#{cmsg.newsgroups_topic_subject_action_watch_title}" tooltip="#{cmsg.newsgroups_topic_subject_action_watch_tooltip}" action="wai:dialog:editOwnNotificationDialogWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="watch-forum-action-id" name="id" value="#{NavigationBean.currentNode.id}" />
				</circabc:actionLink>
			</circabc:displayer>
	</h:panelGrid>
	<f:verbatim><br /><br /></f:verbatim>

	<circabc:richList id="newsGroupsTopicList" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{WaiNavigationManager.bean.posts}" var="p" pageSize="#{WaiNavigationManager.bean.listPageSize}">
		<circabc:column>
			<f:facet name="header">
				<h:outputText value=""  />
			</f:facet>
			<circabc:displayer rendered="#{p.abuseSignaled}">
				<h:graphicImage value="/images/icons/warning.gif" alt="#{cmsg.post_moderation_status_signaled}" title="#{cmsg.post_moderation_status_signaled} #{p.abuseDetails}"/>
			</circabc:displayer>
			<circabc:displayer rendered="#{p.rejected}">
				<h:graphicImage value="/images/icons/error.gif" alt="#{cmsg.post_moderation_status_rejected}" title="#{cmsg.post_moderation_status_rejected} #{p.rejectDetails}" />
			</circabc:displayer>
			<circabc:displayer rendered="#{p.waitingApproval}">
				<h:graphicImage value="/images/icons/Help_icon.gif" alt="#{cmsg.post_moderation_status_pending}" title="#{cmsg.post_moderation_status_pending}"  />
			</circabc:displayer>
		</circabc:column>
		<circabc:column>
			<f:facet name="header">
				<h:outputText value="#{cmsg.author}"  />
			</f:facet>
			<h:outputText value="<b>#{p.creatorUserName}</b>" escape="false" />
			<f:verbatim><br /></f:verbatim>
			<h:graphicImage value="#{p.creatorAvatar}" title="#{cmsg.newsgroups_topic_default_avatar}" alt="#{cmsg.newsgroups_topic_default_avatar}" styleClass="avatar" />
			<f:verbatim><br /></f:verbatim>
			<h:outputFormat value="#{cmsg.newsgroups_topic_subject_user_profile}"  style="font-size:x-small;">
				<circabc:param value="#{p.creatorProfile}" />
			</h:outputFormat>
		</circabc:column>

		<circabc:column>
			<f:facet name="header">
				<h:outputFormat value="#{cmsg.newsgroups_topic_subject_message}"  >
					<circabc:param value="#{WaiNavigationManager.bean.topicTitle}" />
				</h:outputFormat>
			</f:facet>

			<circabc:panel id="post-created-on" styleClass="postDate">
				<circabc:actionLink value="" tooltip="#{cmsg.newsgroups_topic_subject_action_viewtopic_tooltip}"  actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();">
				  	<circabc:displayer rendered="#{p.hasAttachement}">
					  	<h:graphicImage value="/images/icons/attachment.gif" alt="#{cmsg.newsgroups_topic_attachement_available}" title="#{cmsg.newsgroups_topic_attachement_available}"  />
					  	<f:verbatim>&nbsp;</f:verbatim>
				  	</circabc:displayer>
					<h:outputText value="#{cmsg.newsgroups_topic_subject_posted_on}:&nbsp;" escape="false" />
					<h:outputText value="#{p.created}">
						<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
					</h:outputText>
					<circabc:param id="param-id-post-created-on-link" name="id" value="#{p.id}" />
				</circabc:actionLink>

				<h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" escape="false"/>
				<circabc:actions id="newsGroupsTopicActions" value="newsgroup_browse_post_actions_wai" context="#{p}" showLink="false" style="float:right;" />
				<f:verbatim><br /></f:verbatim>
			</circabc:panel>
			<f:verbatim><br /></f:verbatim>
			<circabc:panel id="panelNewsGroupsPost"  styleClassLabel="panelNewsGroupsPostLabel" tooltip="#{cmsg.newsgroups_post_label_tooltip}">
				<h:outputText value="#{p.message}" escape="false" />
				<h:outputText value="#{p.creatorSignature}" escape="false" style="font-size:50%;"/>
			</circabc:panel>
			<f:verbatim><br /></f:verbatim>
			<circabc:panel id="moderation-action" styleClass="postDate">
				<circabc:actions id="newsGroupsTopicModerationActions" value="newsgroup_browse_post_moderation_actions_wai" context="#{p}" showLink="true" style="float:right;" />
				<f:verbatim><br /></f:verbatim>
			</circabc:panel>
		</circabc:column>
		<circabc:dataPager id="topic-pager" styleClass="pagerCirca" />
	</circabc:richList>

	<circabc:panel id="topOfPageAnchorTopic" styleClass="topOfPageAnchor" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorTopic-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorTopic-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
</circabc:panel>
