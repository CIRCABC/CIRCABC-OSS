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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<circabc:panel id="contentMainForm" styleClass="contentMainForm">

	<circabc:displayer rendered="#{WaiDialogManager.bean.hasTranslationCheckedOut == true}">
		<circabc:panel id="newedition--warning" styleClass="infoPanel" styleClassLabel="infoContent"  >
			<h:graphicImage id="newedition-image-warning" value="/images/icons/warning.gif" title="#{cmsg.new_edition_dialog_prob_msg}" alt="#{cmsg.new_edition_dialog_prob_msg}"  />
			<h:outputText id="newedition-text-warning-text" value="&nbsp;&nbsp;#{cmsg.new_edition_dialog_prob_msg}" escape="false" />
		</circabc:panel>

		<f:verbatim>
			<br />
		</f:verbatim>

		<circabc:panel id="editions-checkedout-panel" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.new_edition_dialog_checkout_label}" escape="false" />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>

		<circabc:richList value="#{WaiDialogManager.bean.listTranslationsCheckedOutDataModel}" var="row" viewMode="circa" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt">
    		<circabc:column>
    			<f:facet name="header">
    				<h:outputText value="#{msg.language}" />
    			</f:facet>
    			<h:outputText value="(#{row.language})" />
    		</circabc:column>
    		<circabc:column>
    			<f:facet name="header">
    				<h:outputText value="#{msg.doc_name}" />
    			</f:facet>
    			<h:outputText value="#{row.name}" />
    		</circabc:column>
    		<circabc:column>
    			<f:facet name="header">
    				<h:outputText value="#{msg.checked_out_by}" />
    			</f:facet>
    			<h:outputText value="#{row.checkedOutBy}" />
    		</circabc:column>
    	</circabc:richList>
	</circabc:displayer>

	<circabc:displayer rendered="#{WaiDialogManager.bean.hasTranslationCheckedOut == false}" >
		<circabc:panel id="editions-options-panel" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.new_edition_dialog_edition_options}"  />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>

		<h:panelGrid id="editions-options-values-panel" columns="2" cellpadding="2" cellspacing="2" border="0" columnClasses="panelGridLabelColumn,panelGridValueColumn">
			<h:outputText value="#{cmsg.new_edition_dialog_create_notes}:"  styleClass="propertiesLabel"/>
			<h:inputTextarea id="edition-note" value="#{WaiDialogManager.bean.editionNotes}" rows="2" cols="50" />

			<h:outputText value="#{cmsg.new_edition_dialog_create_minor_change}:"  styleClass="propertiesLabel"/>
			<h:selectBooleanCheckbox id="MinorChange" value="#{WaiDialogManager.bean.minorChange}" />
		</h:panelGrid>

		<f:verbatim><br /></f:verbatim>
		<circabc:panel id="newpivot-location-panel" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.new_edition_dialog_create_choose}"  />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>
		<h:panelGrid id="editions-newpivot-values-panel" columns="2" cellpadding="2" cellspacing="2" border="0" columnClasses="panelGridLabelColumn,panelGridValueColumn">
			<h:outputText value="#{cmsg.new_edition_dialog_new_pivot}:"  styleClass="propertiesLabel"/>
	    	<h:selectOneMenu id="TranslationLanguage" value="#{WaiDialogManager.bean.selectedTranslationLanguage}">
    			<f:selectItems value="#{WaiDialogManager.bean.translatedDocuments}" ></f:selectItems>
    		</h:selectOneMenu>
		</h:panelGrid>
		<f:verbatim><br /></f:verbatim>
		<circabc:panel id="notification-options-panel" styleClass="signup_rub_title">
			<h:outputText value="#{cmsg.new_edition_dialog_notification}"  />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>
		<h:panelGrid columns="2" cellpadding="2" cellspacing="2" border="0">
			<h:selectBooleanCheckbox id="check-disable-notif" value="#{WaiDialogManager.bean.disableNotification}" title="#{cmsg.new_edition_dialog_disable_notification}" />
			<h:outputText id="text-notif-disabled" value="#{cmsg.new_edition_dialog_disable_notification}"/>
		</h:panelGrid>
	</circabc:displayer>

</circabc:panel>
