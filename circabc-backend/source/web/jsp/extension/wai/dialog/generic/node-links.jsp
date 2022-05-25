<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or ? � as soon they
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

<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>


<circabc:panel id="contentMainFormAllLinks" styleClass="contentMainForm">
	<%-- Get the url root for generator --%>
	<h:inputHidden id="root-url" value="#{WaiDialogManager.bean.rootUrl}" />
	<h:inputHidden id="context-path" value="#{WaiDialogManager.bean.contextPath}" />

	<circabc:panel id="nodelinks-main-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.node_links_dialog_details}" />
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>

	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
		<h:outputText id="text-reference" value="#{cmsg.node_links_dialog_reference}:" styleClass="propertiesLabel"/>
		<h:outputText id="input-reference" value="#{WaiDialogManager.bean.reference}"/>

		<h:outputText id="text-path" value="#{cmsg.node_links_dialog_path}:" styleClass="propertiesLabel"/>
		<h:outputText id="input-path" value="#{WaiDialogManager.bean.path}"/>

		<h:outputText id="text-browse" value="#{cmsg.node_links_dialog_browse_url}:" styleClass="propertiesLabel"/>
		<h:outputText id="input-browse" value="#{WaiDialogManager.bean.rootUrl}#{WaiDialogManager.bean.browseUrl}"/>

		<h:outputText id="text-downl" value="#{cmsg.node_links_dialog_download_url}:" rendered="#{WaiDialogManager.bean.content}" styleClass="propertiesLabel"/>
		<h:outputText id="input-downl" value="#{WaiDialogManager.bean.rootUrl}#{WaiDialogManager.bean.downloadUrl}" rendered="#{WaiDialogManager.bean.content}"/>
	</h:panelGrid>

	<f:verbatim><br /></f:verbatim>

	<circabc:panel id="nodelinks-second-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.node_links_dialog_urlgerenator}"  />
	</circabc:panel>

	<f:verbatim><br /></f:verbatim>

	<h:panelGrid columns="6" cellpadding="3" cellspacing="3" border="0">
		<h:outputText id="text-use-relative" value="#{cmsg.node_links_dialog_url_type}:" styleClass="propertiesLabel" />
		<h:selectOneRadio id="sel-relative" value="#{WaiDialogManager.bean.urlType}" layout="pageDirection" onchange="updateLinks();">
			<f:selectItem itemValue="relative" itemLabel="#{cmsg.node_links_dialog_relative}"/>
			<f:selectItem itemValue="absolute" itemLabel="#{cmsg.node_links_dialog_absolute}"/>
		</h:selectOneRadio>

		<h:outputText id="text-use-browse" value="#{cmsg.node_links_dialog_url_mode}:"  styleClass="propertiesLabel"/>
		<h:selectOneRadio id="sel-browse" value="#{WaiDialogManager.bean.urlMode}" layout="pageDirection" onchange="updateLinks();">
			<f:selectItem itemValue="#{WaiDialogManager.bean.browseUrl}" itemLabel="#{cmsg.node_links_dialog_browse}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.downloadUrl}" itemLabel="#{cmsg.node_links_dialog_down}" itemDisabled="#{WaiDialogManager.bean.content == false}" />
		</h:selectOneRadio>

		<h:outputText id="text-use-mode" value="#{cmsg.node_links_dialog_url_target}:" styleClass="propertiesLabel"/>
		<h:selectOneRadio id="sel-mode" value="#{WaiDialogManager.bean.urlTarget}" layout="pageDirection" onchange="updateLinks();">
			<f:selectItem itemValue="_blank" itemLabel="#{cmsg.node_links_dialog_new}" />
			<f:selectItem itemValue="_self" itemLabel="#{cmsg.node_links_dialog_this}" />
		</h:selectOneRadio>

		<h:outputText id="text-use-name" value="#{cmsg.node_links_dialog_url_value}:" styleClass="propertiesLabel"/>
		<h:selectOneRadio id="sel-name" value="#{WaiDialogManager.bean.urlText}" layout="pageDirection" onchange="updateLinks();">
			<f:selectItem itemValue="_" itemLabel="#{cmsg.node_links_dialog_nothing}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.name}" itemLabel="#{cmsg.node_links_dialog_name}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.bestTitle}" itemLabel="#{cmsg.node_links_dialog_url_title}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.path}" itemLabel="#{cmsg.node_links_dialog_path}"/>
		</h:selectOneRadio>

		<h:outputText id="text-use-title" value="#{cmsg.node_links_dialog_url_besttitle}:" styleClass="propertiesLabel"/>
		<h:selectOneRadio id="sel-title" value="#{WaiDialogManager.bean.urlTitle}" layout="pageDirection" onchange="updateLinks();">
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.name}" itemLabel="#{cmsg.node_links_dialog_name}" />
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.bestTitle}" itemLabel="#{cmsg.node_links_dialog_url_title}" />
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.path}" itemLabel="#{cmsg.node_links_dialog_path}" />
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.description}" itemLabel="#{cmsg.node_links_dialog_desc}" />
		</h:selectOneRadio>

		<h:outputText id="text-use-icon" value="#{cmsg.node_links_dialog_url_icon}:" styleClass="propertiesLabel"/>
		<h:selectOneRadio id="sel-icon" value="#{WaiDialogManager.bean.urlIcon}" layout="pageDirection" onchange="updateLinks();">
			<f:selectItem itemValue="_" itemLabel="#{cmsg.node_links_dialog_nothing}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.iconUrl}" itemLabel="#{cmsg.node_links_dialog_icon}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.actionNode.smallIconUrl}" itemLabel="#{cmsg.node_links_dialog_smallicon}"/>
			<f:selectItem itemValue="#{WaiDialogManager.bean.circabcLogo}" itemLabel="#{cmsg.node_links_dialog_circabc_logo}" itemDisabled="#{WaiDialogManager.bean.circabcLogo == '_'}" />			
			<f:selectItem itemValue="#{WaiDialogManager.bean.igLogo}" itemLabel="#{cmsg.node_links_dialog_ig_logo}" itemDisabled="#{WaiDialogManager.bean.igLogo == '_'}" />			
			<f:selectItem itemValue="#{WaiDialogManager.bean.downloadUrl}" itemLabel="#{cmsg.node_links_dialog_image}" itemDisabled="#{WaiDialogManager.bean.content == false}" />
		</h:selectOneRadio>

	</h:panelGrid>

	<f:verbatim><br /></f:verbatim>
	<h:outputText value="<i>#{cmsg.node_links_dialog_preview_explain}:</i>&nbsp;" styleClass="signup_rub_title" escape="false"/>
	<h:commandButton id="submit-change" styleClass="" value="#{cmsg.node_links_dialog_apply}" action="wai:dialog:close:wai:dialog:nodeLinksWai" rendered="true" title="#{cmsg.node_links_dialog_apply_tooltip}" />

	<f:verbatim><br /></f:verbatim>

	<circabc:panel id="nodelinks-third-section" styleClass="signup_rub_title">
		<h:outputText value="#{cmsg.node_links_dialog_preview_rendered}"  />
	</circabc:panel>
	<f:verbatim><br /></f:verbatim>
	<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
		<h:outputText value="#{cmsg.node_links_dialog_preview}:" styleClass="propertiesLabel" />
		<h:outputText id="rendered" value="#{WaiDialogManager.bean.generatedUrl}" escape="false" />
		<h:outputText value="#{cmsg.node_links_dialog_rendered}:" styleClass="propertiesLabel" />
		<h:inputTextarea id="preview" value="#{WaiDialogManager.bean.generatedUrl}" rows="5" cols="60" />
	</h:panelGrid>


</circabc:panel>




<script language="javascript">

	    function updateLinks()
	    {
			var relative = getCheckedValue("FormPrincipal:sel-relative");
			var browse = getCheckedValue("FormPrincipal:sel-browse");
			var mode = getCheckedValue("FormPrincipal:sel-mode");
			var name = getCheckedValue("FormPrincipal:sel-name");
			var title = getCheckedValue("FormPrincipal:sel-title");
			var icon = getCheckedValue("FormPrincipal:sel-icon");

			var linkPrefix;

			if(relative == "relative")
			{
				linkPrefix = document.getElementById("FormPrincipal:context-path").value;
			}
			else
			{
				linkPrefix = document.getElementById("FormPrincipal:root-url").value;
			}

			var link = "<a href=\"" + linkPrefix + browse + "\" target=\"" + mode + "\" title=\"" + title + "\">";
			if(icon != "")
			{
				link = link + "<img src=\"" + linkPrefix + icon + "\"/>&nbsp;";
			}
			link = link + name + "</a>";

			document.getElementById("FormPrincipal:rendered").innerHTML = link;
			document.getElementById("FormPrincipal:preview").innerHTML = link;

	    }


		function getCheckedValue(radioId)
		{
			var select = document.getElementsByName(radioId);

			if(!select)
			{
			 	return "";
			}

			for (var i=0; i < select.length; i++)
			{
				 if (select[i].checked)
				 {
				 	if(select[i].value == "_")
				 	{
				 		return "";
				 	}
				 	else
				 	{
					 	return select[i].value;
				 	}
				 }
			}

			return "";
		}

</script>


