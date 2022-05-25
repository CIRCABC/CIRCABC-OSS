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

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormSwitchModeration" styleClass="contentMainForm">

	<h:outputText value="<h3>#{cmsg.bulk_upload_file_status}</h3>" escape="false;"></h:outputText>
	<h:outputText value="&nbsp;#{cmsg.forum_moderation_status_enabled }" rendered="#{SwitchForumModerationDialog.statusBoolean == true }" escape="false"/>
	<h:outputText value="&nbsp;#{cmsg.forum_moderation_status_disabled }" rendered="#{SwitchForumModerationDialog.statusBoolean == false }" escape="false"/>
		
	<h:graphicImage value="/images/extension/icons/check-64.png" rendered="#{SwitchForumModerationDialog.statusBoolean == true }" styleClass="inlineIcon64" ></h:graphicImage>
	<h:graphicImage value="/images/extension/icons/cross-64.png"  rendered="#{SwitchForumModerationDialog.statusBoolean == false }" styleClass="inlineIcon64"></h:graphicImage>
	
	<f:verbatim><br/><br/><br/></f:verbatim>
	
	<f:verbatim><fieldset class="formArea"></f:verbatim>
	
	<h:outputLabel for="status" value="#{cmsg.switch_forum_moderation_status_label }"></h:outputLabel>
	
	<h:selectOneRadio id="status" value="#{SwitchForumModerationDialog.moderationStatus }" valueChangeListener="#{SwitchForumModerationDialog.statusValueChanged }" onchange="submit()">
		<f:selectItem itemValue="enabled" itemLabel="#{cmsg.access_profile_enable_action }" />
		<f:selectItem itemValue="disabled" itemLabel="#{cmsg.access_profile_disable_action }" />
	</h:selectOneRadio>
	<f:verbatim><br/><br/></f:verbatim>
	<h:panelGroup rendered="#{SwitchForumModerationDialog.moderationStatus=='disabled' && SwitchForumModerationDialog.statusBoolean==true}">
	
		<h:outputLabel for="action" value="#{cmsg.switch_forum_moderation_moderation_action_label }"></h:outputLabel>
		
		<h:selectOneRadio id="action" value="#{SwitchForumModerationDialog.moderationAction }">
			<f:selectItem itemValue="accept" itemLabel="#{cmsg.invite_all_circabc_user_action_tooltip }"/>
			<f:selectItem itemValue="refuse" itemLabel="#{cmsg.refuse_all_applicants_action_tooltip }"/>
		</h:selectOneRadio>
		
		<f:verbatim><br/></f:verbatim>
	
		<h:outputText value="#{cmsg.switch_forum_moderation_moderation_action_helper}" escape="false;" styleClass="inputHelper"></h:outputText>
		<f:verbatim><br/></f:verbatim>
		<h:outputText style="margin:5px;" value="#{cmsg.switch_forum_moderation_more_information }" escape="false"></h:outputText>
	</h:panelGroup>
	
	<f:verbatim></fieldset></f:verbatim>
	
	

</circabc:panel>
