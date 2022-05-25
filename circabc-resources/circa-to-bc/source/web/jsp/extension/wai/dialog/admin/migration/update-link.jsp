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

	<c:set var="currentTitle"
		value="${cmsg.update_links_dialog_browser_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp"%>
	<%@ include file="/jsp/extension/wai/parts/banner.jsp"%>

	<h:form acceptcharset="UTF-8" id="FormPrincipal">
		<%@ include file="/jsp/extension/wai/parts/left-menu.jsp"%>
	</h:form>
	<circabc:displayer id="displayer-is-admin"
		rendered="#{NavigationBean.currentUser.admin == true}">
		
		<div id="maincontent">
		<circabc:errors styleClass="messageGen"
			infoClass="messageInfo" warnClass="messageWarn"
			errorClass="messageError" /> 
			<%-- Content START --%>
			
		<div id="ContentHeader">
		
		<div class="ContentHeaderNavigationLibrary">
		
		<h:form
			acceptcharset="UTF-8" id="FormPrincipal2">
			<circabc:navigationList id="navigationLibrary"
				value="#{WaiNavigationManager.navigation}"
				actionListener="#{BrowseBean.clickWai}" separatorFirst="false"
				styleClass="navigationLibrary" />
		</h:form></div>
		<div>
		
		<div class="ContentHeaderIcone"><h:graphicImage
			url="/images/icons/add_content_large.gif"
			alt="#{cmsg.update_links_dialog_icon_tooltip}"
			title="#{cmsg.update_links_dialog_icon_tooltip}"></h:graphicImage></div>
		<div class="ContentHeaderText"><span class="ContentHeaderTitle"><h:outputText
			value="#{cmsg.update_links_dialog_page_title}" /></span><br />
		<span class="ContentHeaderSubTitle"><h:outputText
			value="#{cmsg.update_links_dialog_page_description}" /></span></div>
		</div>
		</div>

		<div id="ContentMain">
		<h:form acceptcharset="UTF-8" id="FormSecondary12">
			<div class="ContentMainButton">
			<div class="divButtonDialog"><h:commandButton
				id="finish-button" styleClass="dialogButton" value="#{msg.ok}"
				action="#{UpdateLinksBean.finish}" onclick="showWaitProgress();" /><br />
			<h:commandButton id="cancel-button" styleClass="dialogButton"
				value="#{msg.cancel}" action="finish" /></div>
			</div>
			<f:verbatim>
				<br />
			</f:verbatim>

			<h:outputText value="Select category: " escape="false"
				style="font-style: bold;" />
				<h:selectOneMenu value="#{UpdateLinksBean.selectedCategory }" >
					<f:selectItems value="#{UpdateLinksBean.categories }"/>
				</h:selectOneMenu>
				
			<h:commandButton value="Refresh Ig list" action="#{UpdateLinksBean.updateIgs}"></h:commandButton>
			
			<f:verbatim>
				<br />
			</f:verbatim>
			

			<h:outputText value="Select interest group: " escape="false"
				style="font-style: bold;" />
				<h:selectOneMenu value="#{UpdateLinksBean.selectedInterestGroup }">
					<f:selectItems value="#{UpdateLinksBean.igs}"/>
				</h:selectOneMenu>
				
				<h:commandButton action="#{UpdateLinksBean.select}" value="Select"></h:commandButton>
				
			<f:verbatim>
				<hr />
			</f:verbatim>

			<h:outputText value="CIRCA Category: " escape="false"	style="font-style: bold;" />
			<h:inputText value="#{UpdateLinksBean.circaCateg }"></h:inputText>
			<f:verbatim>
				<br />
			</f:verbatim>
			<h:outputText value="CIRCA Interest group: " escape="false" style="font-style: bold;" />
			<h:inputText value="#{UpdateLinksBean.circaIg }"></h:inputText>
			
			<f:verbatim>
				<hr />
			</f:verbatim>
			
			<h:outputText value="Root server: " escape="false" style="font-style: bold;" /><h:inputText value="#{UpdateLinksBean.rootServerUrl }"></h:inputText>&nbsp;&nbsp;&nbsp;<h:commandButton value="Prepare Update" action="#{UpdateLinksBean.prepareUpdate}"></h:commandButton>
			
			<f:verbatim>
				<br />
			</f:verbatim>
			
			<h:dataTable var="row" value="#{UpdateLinksBean.data }">
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="NodeRef"/>
			  </f:facet> 
			   <h:outputText value="#{row[0]}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="Name"/>
			  </f:facet> 
			   <h:outputText value="#{row[1]}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="Target"/>
			  </f:facet> 
			   <h:outputText value="#{row[2]}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="New Target"/>
			  </f:facet> 
			   <h:outputText value="#{row[3]}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="Target Name"/>
			  </f:facet> 
			   <h:outputText value="#{row[6]}"></h:outputText>
			</h:column>
			
			<h:column>
			  <f:facet name="header">
			  <h:outputText value="Status"/>
			  </f:facet> 
			   <h:outputText value="#{row[4]}"></h:outputText>
			</h:column>
			
			</h:dataTable>

		</div>
		</h:form>
	</circabc:displayer>

</circabc:view>
