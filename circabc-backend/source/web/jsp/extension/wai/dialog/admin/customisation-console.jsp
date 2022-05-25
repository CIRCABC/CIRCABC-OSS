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
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<fieldset>
	<legend><h:outputText id="circabclogotitle" value="#{cmsg.customisation_console_circabc_logo }"></h:outputText></legend>
	<h:outputText id="circabclogodescription" value="#{cmsg.customisation_console_circabc_logo_description }"></h:outputText>
	<br/><br/>
	<h:outputText id="currentlogo" value="#{cmsg.customisation_console_circabc_logo_current }"></h:outputText>
	<br/>
	<h:graphicImage id="currentlogoimage" alt="circabc_logo.gif" url="#{CircabcCustomisationDialog.pictureUrl }"></h:graphicImage>
	<br/><br/>	
	
	<t:inputFileUpload id="alfFileInput" size="50" value="#{CircabcCustomisationDialog.appLogo}" storage="file">
	</t:inputFileUpload>
	
	<h:commandButton id="uploadapplogo" value="#{cmsg.import_upload}" actionListener="#{CircabcCustomisationDialog.uploadAppLogo}"/>
	
</fieldset>

<br/>
<div class="signup_rub_title"><h:outputText id="menutitle" value="#{cmsg.customisation_console_circabc_menu}"></h:outputText></div>
<br/>

<fieldset style="width:75%;">
	<legend><h:outputText id="menulinks" value="#{cmsg.customisation_console_circabc_menu_links }"></h:outputText></legend>
	<h:outputText  id="menulinksdesc" value="#{cmsg.customisation_console_circabc_menu_links_description }"></h:outputText>
	<br/><br/>
	
	<h:outputText  id="eLearninglinkdesc" value="eLearning link"></h:outputText>
	<br/><br/>
	
	<h:inputText id="eLearninglinkvalue" value="#{CircabcCustomisationDialog.eLearningLink }"></h:inputText>
	
	<h:commandButton id="eLearninglinkupdate" value="#{cmsg.bulk_invite_bulk_update_title}" actionListener="#{CircabcCustomisationDialog.updateeLearningLink}"/>
	
	<b><h:outputText id="menueLearninglink" value="eLearning"></h:outputText>:</b>
	
	<circabc:actionLink id="eLearninglinkshow" value="#{cmsg.ig_summary_statistic_to_display_button}" actionListener="#{CircabcCustomisationDialog.displayeLearningLink}" rendered="#{CircabcCustomisationDialog.eLearningLinkDisplayed == false}" tooltip="">
		<circabc:param value="show" name="status" id="statuselearningshow"/>
	</circabc:actionLink>
	<circabc:actionLink id="eLearninglinkhide" value="#{cmsg.circabc_uncatched_error_page_hide}" actionListener="#{CircabcCustomisationDialog.displayeLearningLink}" rendered="#{CircabcCustomisationDialog.eLearningLinkDisplayed == true}" tooltip="">
		<circabc:param value="hide" name="status" id="statuselearninghide"/>
	</circabc:actionLink>
	<br/><br/>
</fieldset>

<br/>
<div class="signup_rub_title"><h:outputText id="bannertitle" value="#{cmsg.customisation_console_circabc_banner }"></h:outputText></div>
<br/>

<fieldset style="float:right; width:25%;">
	<legend><h:outputText id="bannerlinks" value="#{cmsg.customisation_console_circabc_banner_links }"></h:outputText></legend>
	<h:outputText  id="bannerlinksdesc" value="#{cmsg.customisation_console_circabc_banner_links_description }"></h:outputText>
	<br/><br/>
	
	<b><h:outputText id="bannersearchlink" value="#{cmsg.event_view_appointments_dialog_action_search_title }"></h:outputText>:</b>
	
	<circabc:actionLink id="searchlinkshow" value="#{cmsg.ig_summary_statistic_to_display_button}" actionListener="#{CircabcCustomisationDialog.displaySearchLink}" rendered="#{CircabcCustomisationDialog.searchLinkDisplayed == false}" tooltip="">
		<circabc:param value="show" name="status" id="statussearchshow"/>
	</circabc:actionLink>
	<circabc:actionLink id="searchlinkhide" value="#{cmsg.circabc_uncatched_error_page_hide}" actionListener="#{CircabcCustomisationDialog.displaySearchLink}" rendered="#{CircabcCustomisationDialog.searchLinkDisplayed == true}" tooltip="">
		<circabc:param value="hide" name="status" id="statussearchhide"/>
	</circabc:actionLink>
	<br/><br/>
	
	<b><h:outputText id="bannerlegallink" value="Legal notice"></h:outputText>:</b>
	
	<circabc:actionLink id="legallinkshow" value="#{cmsg.ig_summary_statistic_to_display_button}" actionListener="#{CircabcCustomisationDialog.displayLegalLink}" rendered="#{CircabcCustomisationDialog.legalLinkDisplayed == false}" tooltip="">
		<circabc:param value="show" name="status" id="statuslegalshow"/>
	</circabc:actionLink>
	<circabc:actionLink id="legallinkhide" value="#{cmsg.circabc_uncatched_error_page_hide}" actionListener="#{CircabcCustomisationDialog.displayLegalLink}" rendered="#{CircabcCustomisationDialog.legalLinkDisplayed == true}" tooltip="">
		<circabc:param value="hide" name="status" id="statuslegalhide"/>
	</circabc:actionLink>
	
</fieldset>

<fieldset>
	<legend><h:outputText id="contactlink" value="#{cmsg.customisation_console_circabc_contact_link }"></h:outputText></legend>
	<h:outputText  id="contactlinkdesc" value="#{cmsg.customisation_console_circabc_contact_link_description }"></h:outputText>
	<br/><br/>
	
	<h:inputText id="contactlinkvalue" value="#{CircabcCustomisationDialog.contactLink }"></h:inputText>
	
	<h:commandButton id="contactlinkupdate" value="#{cmsg.bulk_invite_bulk_update_title}" actionListener="#{CircabcCustomisationDialog.updateContactLink}"/>
	
</fieldset>

<fieldset>
	<legend><h:outputText id="bannerlogo" value="#{cmsg.customisation_console_circabc_banner_logo }"></h:outputText></legend>
	<h:outputText id="bannerlogodesc" value="#{cmsg.customisation_console_circabc_banner_logo_description }"></h:outputText>
	<br/><br/>
	<h:outputText id="currentbanner" value="#{cmsg.customisation_console_circabc_banner_logo_current }"></h:outputText>
	<br/>
	<h:graphicImage id="currentbannerlogo" alt="no logo defined" url="#{CircabcCustomisationDialog.bannerLogoUrl }"></h:graphicImage>
	<br/><br/>	
	
	<t:inputFileUpload id="alfbannerInput" size="50" value="#{CircabcCustomisationDialog.bannerLogo}" storage="file">
	</t:inputFileUpload>
	
	<h:commandButton id="uploadbanner" value="#{cmsg.import_upload}" actionListener="#{CircabcCustomisationDialog.uploadBannerLogo}"/>
	
	<h:commandLink id="resetbanner" value="#{cmsg.customisation_console_circabc_banner_logo_reset }" actionListener="#{CircabcCustomisationDialog.removeBannerLogo}"/>
	
</fieldset>
<br/>
<div class="signup_rub_title"><h:outputText id="templatetitle" value="#{cmsg.manage_bulk_user_export_dialog_notification_emails }"></h:outputText></div>
<br/>
<fieldset>
	<legend><h:outputText id="templates" value="#{cmsg.customisation_console_templates}"></h:outputText></legend>
	<h:outputText id="templatesdesc" value="#{cmsg.customisation_console_templates_description }"></h:outputText>
	<br/><br/>	
	<h:dataTable id="listoftemplates" var="item" value="#{CircabcCustomisationDialog.listOfTemplateUrls }">
		<h:column id="headername">
			<f:facet name="header"></f:facet>
			<h:outputText value="#{item.name}"/>
		</h:column>
		<h:column id="headerlink">
			<f:facet name="link"></f:facet>
			<h:outputLink value="#{item.url}" ><h:outputText value="#{item.url}"/></h:outputLink>
		</h:column>
	</h:dataTable>
	
	<hr/>
	
	<h:selectOneMenu id="selectedtemplate" value="#{CircabcCustomisationDialog.selectedTemplate }">
		<f:selectItems value="#{CircabcCustomisationDialog.selectListOfTemplateUrls }"/>
	</h:selectOneMenu>
	
	<t:inputFileUpload id="alfTemplateInput" size="50" value="#{CircabcCustomisationDialog.template}" storage="file">
	</t:inputFileUpload>
	
	<h:commandButton id="updatetemplatecommand" value="#{cmsg.import_upload}" actionListener="#{CircabcCustomisationDialog.uploadTemplate}"/>
	
</fieldset>

<fieldset>
	<legend><h:outputText id="disclaimerlogo" value="#{cmsg.customisation_console_circabc_disclaimer_logo }"></h:outputText></legend>
	<h:outputText id="disclaimerlogodesc" value="#{cmsg.customisation_console_circabc_disclaimer_logo_description }"></h:outputText>
	<br/><br/>
	<h:outputText id="currentdisclaimer" value="#{cmsg.customisation_console_circabc_logo_current }"></h:outputText>
	<br/>
	<h:graphicImage id="currentdisclaimerlogo" alt="circabc_logo.gif" url="#{CircabcCustomisationDialog.disclaimerUrl }"></h:graphicImage>
	<br/><br/>	
	
	<t:inputFileUpload id="alfDisclaimerInput" size="50" value="#{CircabcCustomisationDialog.disclaimer}" storage="file">
	</t:inputFileUpload>
	
	<h:commandButton id="uploaddisclaimer" value="#{cmsg.import_upload}" actionListener="#{CircabcCustomisationDialog.uploadDisclaimerLogo}"/>
	
</fieldset>

<fieldset>
	<legend><h:outputText id="errorPageTitle" value="#{cmsg.customisation_console_circabc_error_page_title }"></h:outputText></legend>
	<h:outputText id="errorPagedesc" value="#{cmsg.customisation_console_circabc_error_page_description }"></h:outputText>
	<br/><br/>
	<h:outputText id="errorPageContent" value="#{cmsg.customisation_console_circabc_error_page_content }:"></h:outputText>
	<br/>
	<h:inputTextarea id="errorPageContentInput" value="#{CircabcCustomisationDialog.errorMessageContent}"></h:inputTextarea>
	
	<h:commandButton id="updateErrorMessageContent" value="#{cmsg.bulk_invite_bulk_update_title}" actionListener="#{CircabcCustomisationDialog.updateErrorMessageContent}"/>
	
</fieldset>

<script type="text/javascript" language="javascript">
var formTag = document.getElementById("FormPrincipal");
formTag.setAttribute("enctype","multipart/form-data");

</script>