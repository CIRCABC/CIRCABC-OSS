<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormExportIg" styleClass="contentMainForm">
	<div style="margin:10px;">
		<div style="float:left;margin-right:10px;">
			<h:outputText value="Header" /><br/>
			<h:selectOneMenu id="selectionOfHeader" value="#{WaiDialogManager.bean.sourceHeader}"
							 style="width:200px;" onchange="submit()">
				<f:selectItems value="#{WaiDialogManager.bean.allHeaders}" />
			</h:selectOneMenu>
		</div>
		<div style="float:left;margin-right:10px;">
			<h:outputText value="Category" /><br/>
			<h:selectOneMenu id="selectionOfCategory" value="#{WaiDialogManager.bean.sourceCategory}"
							 style="width:200px;" onchange="submit()">
				<f:selectItems value="#{WaiDialogManager.bean.sourceCategories}" />
			</h:selectOneMenu>
		</div>
		<div style="float:left">
			<h:outputText value="Interest Group" /><br/>
			<h:selectOneMenu id="selectionOfIg" value="#{WaiDialogManager.bean.sourceIg}"
							 style="width:200px;">
				<f:selectItems value="#{WaiDialogManager.bean.sourceIgs}" />
			</h:selectOneMenu>
		</div>
		<div style="clear:both;"></div>
	</div>
</circabc:panel>

<circabc:panel id="contentExportIgResult" styleClass="contentMainForm"
			   rendered="#{WaiDialogManager.bean.exportCompleted}">
	<div style="margin:10px;">
		<h:outputText value="Exported file: " style="font-weight:bold;" />
		<h:outputLink id="exportedFileDownloadLink"
					  value="#{WaiDialogManager.bean.exportedFileDownloadUrl}" target="_blank">
			<h:outputText value="#{WaiDialogManager.bean.exportedFileName}" />
		</h:outputLink>
	</div>
</circabc:panel>
