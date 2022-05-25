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

<circabc:panel id="contentMainCircabcHomeMembership"
	styleClass="contentMain" rendered="#{WelcomeBean.registered == true}">

	<circabc:panel id="panelWelcome"
	label="#{cmsg.browse_circabc_categories}"
	styleClass="panelWelcomeGlobal" styleClassLabel="panelWelcomeLabel">
	<circabc:richList id="spacesList" viewMode="circa"
		styleClass="recordSet" headerStyleClass="recordSetHeader"
		rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
		value="#{WelcomeBean.categoryHeaders}" var="r"
		initialSortDescending="false" initialSortColumn="name">
		<%-- Primary column for details view mode --%>
		<circabc:column id="col1" primary="true">
			<f:facet name="header">
				<h:outputText value="#{cmsg.category_name}"  />
			</f:facet>
			<circabc:actionLink value="#{r.name}" tooltip="#{r.description}"
				actionListener="#{BrowseBean.clickWai}" anchor="#{r.id}"
				onclick="showWaitProgress();">
				<circabc:param name="id" value="#{r.id}" />
			</circabc:actionLink>
		</circabc:column>

		<%-- component to display if the list is empty --%>
		<f:facet name="empty">
			<h:outputFormat id="no-list-items" value="#{cmsg.no_list_items}"
				 />
		</f:facet>
	</circabc:richList>
</circabc:panel>

	<circabc:panel id="contentMainCircabcHomeMembershipList"
		label="#{cmsg.view_user_membership_header}"
		styleClass="panelWelcomeGlobal" styleClassLabel="panelWelcomeLabel">

		<f:verbatim>
			<br />
		</f:verbatim>


		<circabc:richList id="categoryRoles" viewMode="circa"
			styleClass="recordSet" headerStyleClass="recordSetHeader"
			rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt"
			value="#{WelcomeBean.categoryRoles}" var="u">
			<%-- category column --%>
			<circabc:column id="category-category">
				<f:facet name="header">
					<h:outputText id="category-category-header"
						value="#{cmsg.category_column_header}" styleClass="header" />
				</f:facet>
				<circabc:actionLink value="#{u.category}" tooltip="#{u.category}"
					actionListener="#{BrowseBean.clickWai}"
					onclick="showWaitProgress();">
					<circabc:param name="id" value="#{u.categoryNodeId}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column id="category-profile">
				<f:facet name="header">
					<h:outputText id="category-profile-header"
						value="#{cmsg.profile_column_header}" styleClass="header"/>
				</f:facet>
				<h:outputText id="category-profile-text" value="#{u.profile}" />
			</circabc:column>
			<f:facet name="empty">
				<h:outputFormat id="no-items-category-results"
					value="#{cmsg.view_user_membership_no_list_items_category}" />
			</f:facet>
		</circabc:richList>

		<circabc:richList id="igRoles" viewMode="circa" styleClass="recordSet"
			headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
			altRowStyleClass="recordSetRowAlt" value="#{WelcomeBean.igRoles}"
			var="u">
			<circabc:column id="category-group-column">
				<f:facet name="header">
					<h:outputText id="category_title-column-header"
						value="#{cmsg.category_column_header}" styleClass="header" />
				</f:facet>
				<h:outputText id="category-text" value="" />
				<circabc:actionLink value="#{u.categoryTitle}"
					tooltip="#{u.categoryTitle}"
					actionListener="#{BrowseBean.clickWai}"
					onclick="showWaitProgress();">
					<circabc:param name="id" value="#{u.categoryNodeId}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column id="interest-group-column">
				<f:facet name="header">
					<h:outputText id="interest-group-column-header"
						value="#{cmsg.interest_group_column_header}" styleClass="header" />
				</f:facet>
				<h:outputText id="interest-group-text" value="" />
				<circabc:actionLink value="#{u.interestGroupTitle}"
					tooltip="#{u.interestGroupTitle}"
					actionListener="#{BrowseBean.clickWai}"
					onclick="showWaitProgress();">
					<circabc:param name="id" value="#{u.interestGroupNodeId}" />
				</circabc:actionLink>
			</circabc:column>
			<circabc:column id="profile-column">
				<f:facet name="header">
					<h:outputText id="profile-column-header"
						value="#{cmsg.profile_column_header}" styleClass="header" />
				</f:facet>
				<h:outputText id="profile-text" value="#{u.profileTitle}"  />
			</circabc:column>
			<f:facet name="empty">
				<h:outputFormat id="no-items-interest-group-results"
					value="#{cmsg.view_user_membership_no_list_items_ig}" />
			</f:facet>
		</circabc:richList>
	</circabc:panel>
</circabc:panel>

<circabc:panel id="contentMainCircabcHomeText" styleClass="contentMain"
	rendered="#{WelcomeBean.registered == false}">

	<f:verbatim>
		<p class="pUnderlined">
	</f:verbatim>
	<h:outputFormat value="#{cmsg.welcome_page_start}" >
		<f:param value="#{NavigationBean.appName}"/>
	</h:outputFormat>
	<f:verbatim>
		</p>
		<table class="tableBigLink">
		<tbody>
		<tr><td><span style=" color: #999; float:right; position:relative; top:80px; margin-right:15px;">
	</f:verbatim>
	<!-- ECAS -->
	<circabc:displayer rendered="#{NavigationBean.enterpriseEnabled }" >
		<circabc:actionLink value="#{cmsg.welcome_page_create_account}"
			tooltip="#{cmsg.welcome_page_create_account}"
			href="https://webgate.ec.europa.eu/cas/eim/external/register.cgi" target="_blank">
		</circabc:actionLink>
		<f:verbatim>&nbsp;|&nbsp;
		</f:verbatim>
		<circabc:actionLink value="#{cmsg.welcome_page_reset_password}"
			tooltip="#{cmsg.welcome_page_reset_password}"
			href="https://webgate.ec.europa.eu/cas/init/passwordResetRequest.cgi" target="_blank">
		</circabc:actionLink>
		<f:verbatim>
			</span>
		</f:verbatim>
		<circabc:actionLink value="#{cmsg.welcome_page_connect_ecas}"
			tooltip="#{cmsg.welcome_page_connect_ecas}"
			action="ecaspage" id="a1" styleClass="bigLink" immediate="true">
		</circabc:actionLink>
	</circabc:displayer>
	<!-- END ECAS -->
	<!-- OSS -->
	<circabc:displayer rendered="#{!NavigationBean.enterpriseEnabled }" >
		<circabc:actionLink value="#{cmsg.self_sign_up}" action="wai:dialog:selfRegisterWai"  actionListener="#{DialogManager.setupParameters}" tooltip="#{cmsg.self_sign_up_tooltip}" immediate="true">
						<circabc:param name="new" value="true" />
					 </circabc:actionLink>
		<f:verbatim>&nbsp;|&nbsp;
		</f:verbatim>
		<circabc:actionLink tooltip="#{cmsg.login_text_12_tooltip}" value="#{cmsg.login_text_12}" action="wai:dialog:resendOwnPasswordWai" immediate="true" >
				<circabc:param name="new" value="true" />
			</circabc:actionLink>
		<f:verbatim>
			</span>
		</f:verbatim>
		<circabc:actionLink value="#{cmsg.self_login}" action="wai:dialog:logout" tooltip="#{cmsg.self_login_tooltip}" id="a1_oss" styleClass="bigLink" immediate="true" />
	</circabc:displayer>
	<!-- END OSS -->
	<f:verbatim>
		</td>
		<td>
	</f:verbatim>
	<circabc:actionLink value="#{cmsg.welcome_page_help}"
		tooltip="#{cmsg.welcome_page_help}"
		href="#{WelcomeBean.helpLink}" id="a2" styleClass="bigLink" target="_blank">
	</circabc:actionLink>
	<f:verbatim>
		</td>
		</tr>
		<tr>
		<td>
			<a href="<%=request.getContextPath()%>/html/docs/CIRCABC_User_Guide.pdf" id="a3" class="bigLink" target="_blank">
				</f:verbatim>
				<h:outputText value="#{cmsg.welcome_page_user_guide}" escape="false" />
				<f:verbatim>
			</a>
<!--
	</f:verbatim>
	<circabc:actionLink value="#{cmsg.welcome_page_user_guide}"
		tooltip="#{cmsg.welcome_page_user_guide}"
		href="${pageContext.request.contextPath}/html/docs/CIRCABC_User_Guide.pdf" id="a3" styleClass="bigLink" target="_blank">
	</circabc:actionLink>
	<f:verbatim>
-->
		</td>
		<td>
			<a href="<%=request.getContextPath()%>/html/docs/CIRCABC_Leader_Guide.pdf" id="a4" class="bigLink" target="_blank">
				</f:verbatim>
				<h:outputText value="#{cmsg.welcome_page_leader_guide}" escape="false" />
				<f:verbatim>
			</a>
<!--			
	</f:verbatim>
	<circabc:actionLink value="#{cmsg.welcome_page_leader_guide}"
		tooltip="#{cmsg.welcome_page_leader_guide}"
		href="${pageContext.request.contextPath}/html/docs/CIRCABC_Leader_Guide.pdf" id="a4" styleClass="bigLink" target="_blank">
	</circabc:actionLink>
	<f:verbatim>
-->	
		</td>
		</tr>
	</f:verbatim>
	<circabc:displayer rendered="#{WaiLeftMenuBean.eLearningLink != null && WaiLeftMenuBean.eLearningEnabled == true}" >
		<f:verbatim>
			<tr>
			<td>
			<img src="<%=request.getContextPath()%>/images/extension/icons/new-orange-ribbon-bottom-right.png" alt="new" style="float:right; margin-top:46px;"/>
		</f:verbatim>
		<circabc:actionLink value="#{WaiLeftMenuBean.eLearningLinkTitle}"
			tooltip="#{WaiLeftMenuBean.eLearningLinkTitle}"
			href="#{WelcomeBean.eLearningLink}" id="a5" styleClass="bigLink" target="_blank">
		</circabc:actionLink>
		<f:verbatim>
			</td>
			</tr>
		</f:verbatim>
	</circabc:displayer>
	<f:verbatim>
		</tbody>
		</table>
		<p style="font-size:110%; color:#CCC; margin:10px;padding:10px;">
	</f:verbatim>

	<f:verbatim>
		<div class="welcomeSubPart">
		<img src="<%=request.getContextPath()%>/images/extension/monitor.png" title="Screen group" alt="Screen group" style="float:right; margin:00px 60px 0px 60px; " width="256"/>
		<ul>
			<li><img src="<%=request.getContextPath()%>/images/extension/world.png" title="Widespread group" alt="Widespread group"/>
	</f:verbatim>
	<h:outputText value="#{cmsg.welcome_page_text_line_1}&nbsp;" escape="false" />
	<f:verbatim>
			</li>
			<li><img src="<%=request.getContextPath()%>/images/extension/group.png" title="people group" alt="people group"/>
	</f:verbatim>
	<h:outputText value="#{cmsg.welcome_page_text_line_2}&nbsp;" escape="false" />
	<f:verbatim>
			</li>
			<li><img src="<%=request.getContextPath()%>/images/extension/lock.png" title="people group" alt="people group"/>
	</f:verbatim>
	<h:outputText value="#{cmsg.welcome_page_text_line_3}&nbsp;" escape="false" />
	<f:verbatim>
			</li>
			<li style="font-weight:bold; font-size:110%; padding: 10px 10px 10px 62px;">
	</f:verbatim>
	<h:outputFormat value="#{cmsg.welcome_page_text_line_4}&nbsp;" escape="false">
		<f:param value="#{NavigationBean.appName}"/>
	</h:outputFormat>
	<f:verbatim>
			</li>
		</ul>
		<br style="clear:left;"/>
		</div>
	</f:verbatim>
</circabc:panel>

