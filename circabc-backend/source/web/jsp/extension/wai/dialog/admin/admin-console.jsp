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

<circabc:panel id="contentMainFormAdminConsoleWai" styleClass="contentMainFormAdminConsole" >


	<%-- Console personal administration  --%>
	<circabc:displayer rendered="#{NavigationBean.guest == false}" id="displayer-personal-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_personal_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentUserName}" />
	   	</h:outputFormat>
	   	<circabc:panel id="personal-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_personal_admin_user" value="personal_admin_actions_wai" context="#{NavigationBean.currentUser}" vertical="true" />
			<circabc:actions id="actions_personal_admin_node" value="personal_node_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
	   	<f:verbatim><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the super admin --%>
	<circabc:displayer rendered="#{NavigationBean.currentUser.admin == true}" id="displayer-super-admin-console">
		<h:outputText value="#{cmsg.admin_console_dialog_super_admintasks}" styleClass="adminConsoleSeparatorTitle"/>
		<circabc:panel id="super-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_super_admin" value="super_admin_global_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>
		<h:outputLink id="gotomigration-console" value="../dialog/admin/migration/migration-console.jsp" style="margin-left:25px;"><h:outputText value="Go to migration console page (through prod/acc load balancer)"></h:outputText></h:outputLink>
		<f:verbatim><br /></f:verbatim>
		<h:outputLink id="gotomigration-console-2" value="../dialog/admin/migration/migration-console.jsp" style="margin-left:25px;"><h:outputText value="Go to migration console page (by nodes)"></h:outputText></h:outputLink>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the super admin ON Circabc --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayRootMenuForSuperAdmin == true}" id="displayer-root-super-admin-console">
		<h:outputText value="#{cmsg.admin_console_dialog_root_admintasks}" styleClass="adminConsoleSeparatorTitle"/>
	   	<circabc:panel id="super-admin-root-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_root_super_admin" value="super_admin_circabc_actions_wai" context="#{NavigationBean.circabcHomeNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the circabc admin --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayRootMenuForCircabcAdmin == true}" id="displayer-root-admin-console">
		<h:outputText value="#{cmsg.admin_console_dialog_root_admintasks}" styleClass="adminConsoleSeparatorTitle"/>
		<circabc:panel id="root-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_root_admin" value="circabc_admin_actions_wai" context="#{NavigationBean.circabcHomeNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the super admin or circabc admin ON category header --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayHeaderMenuForAdmin == true}" id="displayer-header-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_category_header_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentNode.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="cat-header-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_cat_header_admin" value="admin_header_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the category admin --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayCategoryMenu == true}" id="displayer-category-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_category_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentCategory.name}" />
	   	</h:outputFormat>
	   	<f:verbatim><br /><br /></f:verbatim>
	   	<circabc:panel id="category-admin-actions-panel-adm" styleClass="adminConsoleActions">
			<h:outputText id="actions_cat_admin_sub_adm" value="#{cmsg.admin_console_dialog_igroot_sub_adm}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_category_admin_adm" value="category_admin_actions_wai_adm" context="#{NavigationBean.currentCategory}" vertical="true" />
		</circabc:panel>
	   	<f:verbatim><br /></f:verbatim>
	   	<circabc:panel id="category-admin-actions-panel-user" styleClass="adminConsoleActions">
		   	<h:outputText id="actions_cat_admin_sub_user" value="#{cmsg.admin_console_dialog_igroot_sub_user}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_category_admin_user" value="category_admin_actions_wai_user" context="#{NavigationBean.currentCategory}" vertical="true" />
		</circabc:panel>
	   	<f:verbatim><br /></f:verbatim>
	   	<circabc:panel id="category-admin-actions-panel-look" styleClass="adminConsoleActions">
			<h:outputText id="actions_cat_admin_sub_look" value="#{cmsg.admin_console_dialog_igroot_sub_look}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_category_admin_look" value="category_admin_actions_wai_look" context="#{NavigationBean.currentCategory}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the ig leader --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayIgMenu == true}" id="displayer-ig-root-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_igroot_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
	   	</h:outputFormat>
	   	<f:verbatim><br /><br /></f:verbatim>
	   	<circabc:panel id="igroot-admin-actions-panel-adm" styleClass="adminConsoleActions">
			<h:outputText id="actions_igroot_admin_sub_adm" value="#{cmsg.admin_console_dialog_igroot_sub_adm}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_igroot_admin_adm" value="igroot_leader_actions_wai_admin" context="#{NavigationBean.currentIGRoot}" vertical="true" />
		</circabc:panel>
	   	<f:verbatim><br /></f:verbatim>
		<circabc:panel id="igroot-admin-actions-panel-user" styleClass="adminConsoleActions">
		   	<h:outputText id="actions_igroot_admin_sub_user" value="#{cmsg.admin_console_dialog_igroot_sub_user}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_igroot_admin_user" value="igroot_leader_actions_wai_user" context="#{NavigationBean.currentIGRoot}" vertical="true" />
		</circabc:panel>
	   	<f:verbatim><br /></f:verbatim>
	   	<circabc:panel id="igroot-admin-actions-panel-doc" styleClass="adminConsoleActions">
 		   	<h:outputText id="actions_igroot_admin_sub_doc" value="#{cmsg.admin_console_dialog_igroot_sub_doc}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_igroot_admin_doc" value="igroot_leader_actions_wai_doc" context="#{NavigationBean.currentIGRoot}" vertical="true" />
		</circabc:panel>
	   	<f:verbatim><br /></f:verbatim>
	   	<circabc:panel id="igroot-admin-actions-panel-look" styleClass="adminConsoleActions">
			<h:outputText id="actions_igroot_admin_sub_look" value="#{cmsg.admin_console_dialog_igroot_sub_look}:" styleClass="adminConsoleSubRub" />
			<circabc:actions id="actions_igroot_admin_look" value="igroot_leader_actions_wai_look" context="#{NavigationBean.currentIGRoot}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the information admin (test if the current node is a information root) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayInformationMenu == true}" id="displayer-inf-root-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_information_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="inf-root-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_inf_root_admin" value="information_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the lib admin (test if the current node is a library root) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayLibraryMenu == true}" id="displayer-library-root-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_library_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="library-root-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_library_root_admin" value="lib_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the news admin (test if the current node is a newsgroup root) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayNewsgroupMenu == true}" id="displayer-news-root-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_newsgroup_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="news-root-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_news_root_admin" value="news_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the event admin (test if the current node is a event root) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayEventsMenu == true}" id="displayer-eve-root-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_event_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="eve-root-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_eve_root_admin" value="events_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the survey admin (test if the current node is a survey root) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displaySurveyMenu == true}" id="displayer-survey-root-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_survey_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentIGRoot.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="survey-root-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_survey_root_admin" value="survey_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>



	<%-- Console for the inf admin (test if the current node is a information child) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayInformationChildSpaceMenu == true}" id="displayer-inf-space-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_library_space_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentNode.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="inf-space-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_survey_space_admin" value="information_child_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the lib admin (test if the current node is a library child) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayLibraryChildMenu == true}" id="displayer-library-space-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_library_space_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentNode.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="library-space-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_library_space_admin" value="lib_child_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the news admin (test if the current node is a forum child) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayForumChildMenu == true}" id="displayer-forum-space-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_library_space_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentNode.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="forum-space-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_forum_space_admin" value="forum_child_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for the sur admin (test if the current node is a survey child) --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displaySurveyChildSpaceMenu == true}" id="displayer-survey-space-admin-console">
		<h:outputFormat value="#{cmsg.admin_console_dialog_library_space_admintasks}" styleClass="adminConsoleSeparatorTitle">
			<circabc:param value="#{NavigationBean.currentNode.name}" />
	   	</h:outputFormat>
	   	<circabc:panel id="survey-space-admin-actions-panel" styleClass="adminConsoleActions">
			<circabc:actions id="actions_survey_space_admin" value="survey_child_admin_actions_wai" context="#{NavigationBean.currentNode}" vertical="true" />
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
	</circabc:displayer>

	<%-- Console for other services adminsitration  --%>
	<circabc:displayer rendered="#{CircabcAdminConsoleDialog.displayOtherServiceConsole == true}" id="displayer-other-service-admin-console">
		<h:outputText value="#{cmsg.admin_console_dialog_other_services}" styleClass="adminConsoleSeparatorTitle"/>
	   	<circabc:panel id="other-service-admin-actions-panel" styleClass="adminConsoleActions">
			<r:permissionEvaluator value="#{NavigationBean.currentIGRoot}" allow="DirManageMembers">
				<f:verbatim><br /></f:verbatim>
				<circabc:actionLink id="link-ig-admin" image="/images/icons/shortcut.gif" value="#{cmsg.admin_console_dialog_other_ig}" tooltip="#{cmsg.admin_console_dialog_other_ig}" action="wai:dialog:close:wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="ig-id" name="id" value="#{NavigationBean.currentIGRoot.id}" />
				</circabc:actionLink>
			</r:permissionEvaluator>
			<r:permissionEvaluator value="#{NavigationBean.currentIGRoot.information}" allow="InfManage">
				<f:verbatim><br /></f:verbatim>
				<circabc:actionLink id="link-inf-admin" image="/images/icons/shortcut.gif" value="#{cmsg.admin_console_dialog_other_inf}" tooltip="#{cmsg.admin_console_dialog_other_inf}" action="wai:dialog:close:wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="ig-inf" name="id" value="#{NavigationBean.currentIGRoot.information.id}" />
				</circabc:actionLink>
			</r:permissionEvaluator>
			<r:permissionEvaluator value="#{NavigationBean.currentIGRoot.library}" allow="LibAdmin">
				<f:verbatim><br /></f:verbatim>
				<circabc:actionLink id="link-lib-admin" image="/images/icons/shortcut.gif" value="#{cmsg.admin_console_dialog_other_lib}" tooltip="#{cmsg.admin_console_dialog_other_lib}" action="wai:dialog:close:wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="ig-lib" name="id" value="#{NavigationBean.currentIGRoot.library.id}" />
				</circabc:actionLink>
			</r:permissionEvaluator>
			<r:permissionEvaluator value="#{NavigationBean.currentIGRoot.event}" allow="EveAdmin">
				<f:verbatim><br /></f:verbatim>
				<circabc:actionLink id="link-eve-admin" image="/images/icons/shortcut.gif" value="#{cmsg.admin_console_dialog_other_eve}" tooltip="#{cmsg.admin_console_dialog_other_eve}" action="wai:dialog:close:wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="ig-eve" name="id" value="#{NavigationBean.currentIGRoot.event.id}" />
				</circabc:actionLink>
			</r:permissionEvaluator>
			<r:permissionEvaluator value="#{NavigationBean.currentIGRoot.newsgroup}" allow="NwsModerate">
				<f:verbatim><br /></f:verbatim>
				<circabc:actionLink id="link-new-admin" image="/images/icons/shortcut.gif" value="#{cmsg.admin_console_dialog_other_new}" tooltip="#{cmsg.admin_console_dialog_other_new}" action="wai:dialog:close:wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="ig-new" name="id" value="#{NavigationBean.currentIGRoot.newsgroup.id}" />
				</circabc:actionLink>
			</r:permissionEvaluator>
			<r:permissionEvaluator value="#{NavigationBean.currentIGRoot.survey}" allow="SurAdmin">
				<f:verbatim><br /></f:verbatim>
				<circabc:actionLink id="link-sur-admin" image="/images/icons/shortcut.gif" value="#{cmsg.admin_console_dialog_other_sur}" tooltip="#{cmsg.admin_console_dialog_other_sur}" action="wai:dialog:close:wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" >
					<circabc:param id="ig-sur" name="id" value="#{NavigationBean.currentIGRoot.survey.id}" />
				</circabc:actionLink>
			</r:permissionEvaluator>
		</circabc:panel>
		<f:verbatim><br /><br /></f:verbatim>
		
	</circabc:displayer>


</circabc:panel>





