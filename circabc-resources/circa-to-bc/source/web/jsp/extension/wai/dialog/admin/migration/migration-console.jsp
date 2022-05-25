<%--+
    |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
    |
    |  		   http://ec.europa.eu/idabc/en/document/6523
    |
    +--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />


	<%@ page isELIgnored="false"%>

  	<c:set var="currentTitle" value="${cmsg.migration_console_browser_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>

	<div id="maincontent">
	<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

	<%-- Content START --%>
	<div id="ContentHeader">
		<div class="ContentHeaderNavigationLibrary">
		<circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></div>
		<div>
			<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.importation_dialog_icon_tooltip}" title="#{cmsg.importation_dialog_icon_tooltip}"></h:graphicImage></div>
			<div class="ContentHeaderText">
				<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.migration_console_page_title}" /></span><br />
				<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.migration_console_page_description}" /></span>
			</div>
		</div>
	</div>

	<div id="ContentMain">
			<div class="ContentMainButton">
				<div class="divButtonDialog">
					<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.ok}" action="wai:browse-wai" />
				</div>
			</div>
			<div class="contentMainFormAdminConsole">

				<%-- Console for the super admin --%>
				<circabc:displayer rendered="#{NavigationBean.currentUser.admin == true}" id="displayer-migration-console">
					<h:outputText value="#{cmsg.migration_console_page_subtitle}:" styleClass="adminConsoleSeparatorTitle"/>
					<circabc:panel id="migration-actions-panel" styleClass="adminConsoleActions">
					<ul>

						<li>
						<circabc:actionLink id="migration-console-action-stats" value="#{cmsg.migration_console_action_stats}" action="manage-stats" actionListener="#{ManageExportationStatisticsBean.reset}" tooltip="#{cmsg.migration_console_action_stats}" rendered="#{ManageExportationsBean.exportAvailable == true}" /><br />
						</li>						<li>
						<circabc:actionLink id="migration-console-action-export" value="#{cmsg.migration_console_action_export}" action="manage-export" actionListener="#{ManageExportationsBean.reset}" tooltip="#{cmsg.migration_console_action_export}" rendered="#{ManageExportationsBean.exportAvailable == true}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-bulk-user" value="#{cmsg.migration_console_action_big_bang}" action="manage-bulk-user-export" actionListener="#{ManageBulkUserExport.reset}" tooltip="#{cmsg.migration_console_action_big_bang}" rendered="#{ManageExportationsBean.exportAvailable == true}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-upload" value="#{cmsg.migration_console_action_upload}" action="upload" actionListener="#{UploadImportationFileBean.reset}" tooltip="#{cmsg.migration_console_action_upload}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-etl"    value="#{cmsg.migration_console_action_etl}"   action="etl"     actionListener="#{MigrationETLBean.reset}" tooltip="#{cmsg.migration_console_action_etl}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-etl-one-shot"    value="#{cmsg.migration_console_action_etl_one_shot}"   action="etl-one-shot"     actionListener="#{MigrationETLOneShotBean.reset}" tooltip="#{cmsg.migration_console_action_etl_one_shot}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-import" value="#{cmsg.migration_console_action_import}" action="manage-import" actionListener="#{ManageImportationsBean.reset}" tooltip="#{cmsg.migration_console_action_import}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-status" value="#{cmsg.manage_importations_action_running_title}" action="running" actionListener="#{RunningImportsBean.reset}" tooltip="#{cmsg.manage_importations_action_running_alt}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-copy-user"    value="#{cmsg.migration_console_action_copy_user}"   action="copy-user"     actionListener="#{CopyUserBean.reset}" tooltip="#{cmsg.migration_console_action_copy_user}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-update-circa-link"    value="#{cmsg.migration_console_update_circa_links}"   action="update-link"     actionListener="#{UpdateLinksBean.reset}" tooltip="#{cmsg.migration_console_update_circa_links}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-delete-users"    value="#{cmsg.migration_console_action_delete_users}"   action="delete-users"     actionListener="#{DeleteUsersBean.reset}" tooltip="#{cmsg.migration_console_action_delete_users}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-export-category-ig"    value="#{cmsg.migration_console_action_export_category_ig}"   action="export-category-ig"     actionListener="#{ExporterBean.reset}" tooltip="#{cmsg.migration_console_action_export_category_ig}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-import-category-ig"    value="#{cmsg.migration_console_action_import_category_ig}"   action="import-category-ig"     actionListener="#{ImporterBean.reset}" tooltip="#{cmsg.migration_console_action_import_category_ig}" /><br />
						</li>
						<li>
						<circabc:actionLink id="migration-console-action-scan-missing-users"    value="Scan for Missing Users"   action="scan-missing-users"     actionListener="#{ScanMissingUserBean.reset}" tooltip="Scan for Missing Users" /><br />
						</li>
					</ul>
					</circabc:panel>
					<f:verbatim><br /></f:verbatim>
				</circabc:displayer>
			</div>
	</div>
</h:form>
</circabc:view>






