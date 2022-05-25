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
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%
	// ensure construction of the FacesContext before attemping a service call
	FacesContext fc = FacesHelper.getFacesContext(request, response, application);
%>

<circabc:view>

	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />


	<%@ page isELIgnored="false"%>

 	<c:set var="currentTitle" value="${cmsg.circabc_uncatched_error_page_main_title}" />
 
	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner-simple.jsp" %>

    <!-- real content START -->
	<table width="100%" border="0" align="center" cellspacing="0" cellpadding="0" id="content" summary="${cmsg.banner_content_page}" >
	<tr>
		<td>
			<h:form acceptcharset="UTF-8" id="FormPrincipal" styleClass="FormPrincipal"  >
               	   <%-- Content START --%>
				<circabc:panel id="maincontent" styleClass="errorMainContent" >
					<%-- No menu to display --%>

		               <circabc:panel id="contentHeader" styleClass="contentHeaderError">
								<h:outputText value="<br />" styleClass="contentHeaderSubTitle" escape="false"/>
								<h:outputFormat value="#{cmsg.circabc_uncatched_error_page_title}" styleClass="contentHeaderTitle" >
									<f:param value="#{NavigationBean.appName}"/>
								</h:outputFormat>
								<h:outputText value="<br /><br />" styleClass="contentHeaderSubTitle" escape="false"/>
			           </circabc:panel>

		               <circabc:panel id="contentMainErrorPage" styleClass="contentMain">
						   <h:outputText value="<br /><br />" escape="false"/>
						   <circabc:panel id="panelBackToCircabc" styleClass="errorBackToCircabc">
						   		<h:outputFormat value="#{NavigationBean.circabcUrl}" escape="false">
						   			<f:param value="#{NavigationBean.appName}"/>
								</h:outputFormat>
						   </circabc:panel>

						<br /><br />
						
						<circabc:displayer id="errorContentCustomized" rendered="#{ErrorBean.errorMessageContent != null }">
							<h:outputText id="errorContentDescription" value="#{ErrorBean.errorMessageContent }" escape="false"></h:outputText>
						</circabc:displayer>

						<circabc:displayer id="errorContentNotCustomized" rendered="#{ErrorBean.errorMessageContent == null || ErrorBean.errorMessageContent == ''}">
							
							<h:outputText value="#{cmsg.error_page_dear}"/>
						   <br /><br />
						   <h:outputText value="#{cmsg.error_page_content_line1}"/>
						   <br /><br />
						   <h:outputText value="#{cmsg.error_page_content_line2}"/>
						   <br />
						   <h:outputText value="#{cmsg.error_page_content_line3}" escape="false"/>
						   <br />
						   <h:outputText value="#{cmsg.error_page_content_line4}" escape="false"/>
						   <br /><br />
						   <h:outputText value="#{cmsg.error_page_content_line5}"/>
						   <br /><br />
						   <h:outputText value="#{cmsg.error_page_content_line6}"/>
						   <br /><br />
						   <h:outputText value="#{cmsg.the_circabc_team}"/>
						   <br />

						</circabc:displayer>
  
						   
		               </circabc:panel>
					</circabc:panel>
                   <%-- Content END --%>

           </h:form>
      <%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>
