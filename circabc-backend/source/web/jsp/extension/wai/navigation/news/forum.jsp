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

<circabc:panel id="contentMainForum" styleClass="contentMain">

	<circabc:panel id="panelNewsGroupsSubForums" label="#{cmsg.newsgroups_forums_label}" styleClass="panelNewsGroupsForumsGlobal" styleClassLabel="panelNewsGroupsForumsLabel" tooltip="#{cmsg.newsgroups_forums_label_tooltip}" rendered="#{WaiNavigationManager.bean.subForumAllowed}" >
		<circabc:customList id="newsGroupsSubForumsList"  styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{WaiNavigationManager.bean.forums}" configuration="#{WaiNavigationManager.bean.forumNavigationPreference}" />
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorNewsHomeSub" styleClass="topOfPageAnchor" rendered="#{WaiNavigationManager.bean.subForumAllowed}" >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorNewsHomeSub-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorNewsHomeSub-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>


	<circabc:panel id="panelNewsGroupsForum" label="#{cmsg.newsgroups_forum_label}" styleClass="panelNewsGroupsForumGlobal" styleClassLabel="panelNewsGroupsForumLabel" tooltip="#{cmsg.newsgroups_forum_label_tooltip}">
		<circabc:customList id="newsGroupsTopicList"  styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" value="#{WaiNavigationManager.bean.topics}" configuration="#{WaiNavigationManager.bean.topicNavigationPreference}" />
	</circabc:panel>

	<circabc:panel id="topOfPageAnchorForum" styleClass="topOfPageAnchor"  >
		<%-- Display the "back to top icon first and display the text after." --%>
		<circabc:actionLink id="topOfPageAnchorForum-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
		<circabc:actionLink id="topOfPageAnchorForum-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
	</circabc:panel>
</circabc:panel>
