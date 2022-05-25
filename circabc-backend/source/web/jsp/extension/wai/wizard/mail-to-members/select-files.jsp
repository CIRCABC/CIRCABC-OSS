<%--+
    |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
    |
    |  		   http://ec.europa.eu/idabc/en/document/6523
    |
    +--%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/noscript.js" ></script>
<%@ page isELIgnored="false"%>

<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="contentMainFormMailToMembersStep3" styleClass="contentMainForm" rendered="#{WizardManager.bean.insideIg == true}">

	<circabc:panel id="attchement-add-section-repo" styleClass="signup_rub_title">
			<h:outputText id="sect-attac-2" value="#{cmsg.create_content_dialog_attachement_add_link}"   />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<circabc:panel id="select-link-panel">

			<circabc:nodeSelector id="selector-repo" value="#{WizardManager.bean.attachLink}"
			 		styleClass="selector"
			 		rootNode="#{WizardManager.bean.rootIg}"
			 		initialSelection="#{WizardManager.bean.rootIg}"
			 		label="#{cmsg.create_content_dialog_attachement_select_link}"
			 		showContents="true"
			 		pathLabel="#{cmsg.path_label}"
					pathErrorMessage="#{cmsg.path_error_message}"
			 		/>

		</circabc:panel>

		<f:verbatim><br /><br /></f:verbatim>

		<circabc:panel id="attachement-all-att-section" styleClass="signup_rub_title">
			<h:outputText id="sect-attac-3" value="#{cmsg.create_content_dialog_attachement_all}"  />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>
		
		<h:dataTable id="allattachsTable" value="#{WizardManager.bean.attachedLinks}" var="attach"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" width="100%">

			<h:column id="attach-name-col">
				<f:facet name="header">
					<h:outputText id="attach-display-title" value="#{cmsg.create_content_dialog_attachement_title}"  styleClass="attachInList"/>
				</f:facet>
				<h:outputText id="attach-name" value="#{attach.name}"  />
			</h:column>

			<h:column id="attach-type-col">
				<f:facet name="header">
					<h:outputText id="attach-display-type" value="#{cmsg.create_content_dialog_attachement_type}"  styleClass="attachInList"/>
				</f:facet>
				<h:outputText id="attach-type" value="#{attach.type}"  />
			</h:column>

			<h:column id="actions">
				<f:facet name="header">
					<h:outputText value="#{cmsg.actions}" />
				</f:facet>

				<circabc:actionLink id="removeAttachAction" actionListener="#{WizardManager.bean.removeAttachement}"
						image="/images/icons/delete.gif" value="#{cmsg.create_content_dialog_attachement_remove_action}"
						tooltip="#{cmsg.create_content_dialog_attachement_remove_action}" showLink="false">
					<f:param value="#{attach.attachRef.id}" name="removingNoderef"/>		
				</circabc:actionLink>

			</h:column>
			
			
		</h:dataTable>


</circabc:panel>

<circabc:panel id="contentMainFormMailToMembersStep3bis" styleClass="contentMainForm" rendered="#{WizardManager.bean.insideIg == false}">
		<h:outputText value="#{cmsg.mail_to_members_disabled_from_category }"></h:outputText>
</circabc:panel>

