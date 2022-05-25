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

<%-- the main content --%>
<circabc:panel id="contentMainForm" styleClass="contentFullPage">

	<circabc:displayer rendered="#{WaiDialogManager.bean.guest == false}">
			<h:outputText value="<p>#{WaiDialogManager.bean.interestGroupDesc}</p>" escape="false" />
	</circabc:displayer>

	<circabc:displayer rendered="#{WaiDialogManager.bean.guest == true}">
	
	<f:verbatim><div style="width:85%; margin-left:auto; margin-right:auto;" ></f:verbatim>	
	
		<h:outputText value="<br/><b>#{cmsg.apply_for_membership_no_log_page_explaination_1}</b>" escape="false" />
		<f:verbatim><br /><br /></f:verbatim>
			<circabc:actionLink value="#{cmsg.welcome_page_connect_ecas}"
		tooltip="#{cmsg.welcome_page_connect_ecas}"
		action="ecaspage" immediate="true" styleClass="aFakeButton">
	</circabc:actionLink>
	<h:outputText value=" #{cmsg.contact_message_or} " style="margin-left:20px; margin-right:20px; font-style: italic;"/>
		<circabc:actionLink value="#{cmsg.welcome_page_create_account}"
		tooltip="#{cmsg.welcome_page_create_account}"
		href="https://webgate.ec.europa.eu/cas/eim/external/register.cgi" target="_blank" styleClass="aFakeButton">	</circabc:actionLink>
		
	<f:verbatim><br /><br /><br /></f:verbatim>	
	<f:verbatim></div></f:verbatim>	
	</circabc:displayer>

	<%-- Content START --%>
	<f:verbatim><div style="width:85%; margin-left:auto; margin-right:auto;" ></f:verbatim>	
	<circabc:panel id="contact_info" label="#{cmsg.igroot_home_contact_information}" styleClass="panelIGHomeContact" styleClassLabel="panelIGHomeContactLabel" tooltip="#{cmsg.igroot_home_contact_information_tooltip}">
		<h:outputText value="#{WaiDialogManager.bean.contactInfo}" escape="false" />
	</circabc:panel>
	<f:verbatim></div></f:verbatim>	
	<f:verbatim><br /></f:verbatim>

	<circabc:panel id="panel-application-form" label="#{cmsg.application_form_membership}" styleClass="panelApplyMembershipApplication" styleClassLabel="panelApplyMembershipApplicationLabel" rendered="#{WaiDialogManager.bean.guest == false}" tooltip="#{cmsg.application_form_membership_tooltip}">

		<h:outputText value="#{cmsg.introduce_your_application}<br /><br />" escape="false" />
		<h:outputText value="#{cmsg.information_forwarded_to_diradmins}<br /><br />" escape="false" />
		<circabc:panel id="applicationFormCenter" styleClass="applicationFormCenter">
			<h:inputTextarea id="application-message" value="#{WaiDialogManager.bean.message}" cols="80" rows="10" />
			<f:verbatim><br /></f:verbatim>
			<h:commandButton id="submit" action="#{WaiDialogManager.finish}" value="#{msg.submit}" />
		</circabc:panel>
	</circabc:panel>

     <circabc:panel id="topOfPageAnchorDocDetails" styleClass="topOfPageAnchor"  >
         <%-- Display the "back to top icon first and display the text after." --%>
         <circabc:actionLink value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
         <circabc:actionLink value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
     </circabc:panel>

</circabc:panel>
