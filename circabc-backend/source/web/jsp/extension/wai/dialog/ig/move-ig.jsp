
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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormMoveIgDetails" styleClass="contentMainForm">
	<div style="margin:10px;">
		<p>
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_source}"  />
		</p>
		<div style="float:left;margin-right:10px;">
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_source_header}"  /><br/>
			<h:selectOneMenu id="selectionOfHeader" value="#{WaiDialogManager.bean.sourceHeader}" 
							 style="width:200px;" onchange="submit()">
				<f:selectItems value="#{WaiDialogManager.bean.allHeaders}" />
			</h:selectOneMenu>
		</div>
		<div style="float:left;margin-right:10px;">
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_source_category}"  /><br/>
			<h:selectOneMenu id="selectionOfCategory" value="#{WaiDialogManager.bean.sourceCategory}" 
							 style="width:200px;" onchange="submit()">
				<f:selectItems value="#{WaiDialogManager.bean.sourceCategories}" />
			</h:selectOneMenu>
		</div>
		<div style="float:left">
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_source_ig}"  /><br/>
			<h:selectOneMenu id="selectionOfIg" value="#{WaiDialogManager.bean.sourceIg}"
							 style="width:200px;">
				<f:selectItems value="#{WaiDialogManager.bean.sourceIgs}" />
			</h:selectOneMenu>
		</div>
		<div style="clear:both;"></div>
	</div>
	
	<div style="margin:20px 10px;">
		<p>
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_target}"  />
		</p>
		<div style="float:left;margin-right:10px;">
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_target_header}"  /><br/>
			<h:selectOneMenu id="selectionOfTargetHeader" value="#{WaiDialogManager.bean.targetHeader}" 
							 style="width:305px;" onchange="submit()">
				<f:selectItems value="#{WaiDialogManager.bean.allHeaders}" />
			</h:selectOneMenu>
		</div>
		<div style="float:left">
			<h:outputText value="#{cmsg.move_ig_dialog_lbl_target_category}"  /><br/>
			<h:selectOneMenu id="selectionOfTargetCategory" value="#{WaiDialogManager.bean.targetCategory}" 
							 style="width:305px">
				<f:selectItems value="#{WaiDialogManager.bean.targetCategories}" />
			</h:selectOneMenu>
		</div>
		<div style="clear:both;"></div>
	</div>
	
</circabc:panel>
<f:verbatim><br /></f:verbatim>
<circabc:panel id="panelExport" label="#{cmsg.export_label}" tooltip="#{cmsg.export_label}" styleClass="panelSearchGlobal" styleClassLabel="panelSearchLabel">
		<f:verbatim><br /></f:verbatim>
		<h:outputText id="move-ig-space1" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="export-button" action="#{WaiDialogManager.bean.export}" value="#{cmsg.export}" />
</circabc:panel>
<f:verbatim><br /></f:verbatim>
<circabc:panel id="panelSync" label="#{cmsg.synchronize_label}" tooltip="#{cmsg.synchronize_label}" styleClass="panelSearchGlobal" styleClassLabel="panelSearchLabel">
		<f:verbatim><br /></f:verbatim>
		<h:outputText id="move-ig-space2" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="sync-button" action="#{WaiDialogManager.bean.syncIG}" value="#{cmsg.synchronize_interest_group}" />
		<h:outputText id="move-ig-space3" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="sync-cat-button" action="#{WaiDialogManager.bean.syncCat}" value="#{cmsg.synchronize_cat_interest_group}" />
		<h:outputText id="move-ig-space4" value="&nbsp;&nbsp;" escape="false"/>
		<h:commandButton id="sync-users-button" action="#{WaiDialogManager.bean.syncUsers}" value="#{cmsg.synchronize_users}" />
		<h:commandButton id="sync-all-button" action="#{WaiDialogManager.bean.syncAll}" value="#{cmsg.synchronize_all}" />
		<h:outputText id="move-ig-space5" value="&nbsp;&nbsp;" escape="false"/>
        <h:commandButton id="sync-circabc-admins-button" action="#{WaiDialogManager.bean.syncCircabcAdmins}" value="#{cmsg.synchronize_circabc_admins}" />
		<f:verbatim><br /><br /></f:verbatim>
</circabc:panel>
