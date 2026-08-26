<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainFormImportIg" styleClass="contentMainForm">
	<div style="margin:10px;">
		<div style="float:left;margin-right:10px;">
			<h:outputText value="Iteration" /><br/>
			<h:selectOneMenu id="selectionOfIteration" value="#{WaiDialogManager.bean.selectedIteration}" style="width:350px;">
				<f:selectItems value="#{WaiDialogManager.bean.iterations}" />
			</h:selectOneMenu>
		</div>
		<div style="float:left;margin-right:10px;">
			<h:outputText value="Action" /><br/>
			<h:selectOneMenu id="selectionOfAction" value="#{WaiDialogManager.bean.action}" style="width:150px;">
				<f:selectItems value="#{WaiDialogManager.bean.actions}" />
			</h:selectOneMenu>
		</div>
		<div style="clear:both;"></div>
		<br/>
		<h:outputText value="Validate: checks the XML structure. Dry Run: simulates the import without changes. Run Import: performs the actual import." style="font-style:italic;color:#666;" />
		<br/><br/>
		<h:outputText value="Note: The export XML must contain absolute content URLs (e.g. https://source-instance/circabc/d/d/...). Configure 'circabc.export.alfresco.base.url' on the source instance before exporting. Remote credentials are configured via 'circabc.import.remote.username' and 'circabc.import.remote.password' properties." style="font-style:italic;color:#999;font-size:0.9em;" />
	</div>
</circabc:panel>
