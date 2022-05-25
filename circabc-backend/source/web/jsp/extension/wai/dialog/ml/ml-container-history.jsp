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

<circabc:panel id="contentMainForm" styleClass="contentMainForm">
	<%-- Properties details --%>
	<circabc:panel id="panelMMDPropertiesDetails" label="#{cmsg.manage_multilingual_details_properties}" tooltip="#{cmsg.manage_multilingual_details_properties_tooltip}" styleClass="panelMMDPropertiesDetailsGlobal" styleClassLabel="panelMMDPropertiesDetailsLabel">
          	<circabc:propertySheetGrid id="ml-container-props-sheet" value="#{WaiDialogManager.bean.documentMlContainer}"
				var="mlContainerProps" columns="1" labelStyleClass="propertiesLabelTiny"
				externalConfig="true" cellpadding="2" cellspacing="2" mode="view"/>
	</circabc:panel>

    <circabc:panel id="topOfPageAnchorMLPropDetails" styleClass="topOfPageAnchor"  >
        <%-- Display the "back to top icon first and display the text after." --%>
        <circabc:actionLink id="act-link-prop-det-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
        <circabc:actionLink id="act-link-prop-det-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
    </circabc:panel>

	<%-- Translation details --%>
	<circabc:panel id="panelMMDTranslationDetails" label="#{cmsg.manage_multilingual_details_translations}" tooltip="#{cmsg.manage_multilingual_details_translations_tooltip}" styleClass="panelDocumentDetailsGlobal" styleClassLabel="panelDocumentDetailsLabel">
		<%-- list of translations --%>
		<circabc:richList id="TranslationList" viewMode="circa" value="#{WaiDialogManager.bean.translations}" var="r" styleClass="recordSet" headerStyleClass="recordSetHeader"	rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" initialSortColumn="name" initialSortDescending="false">

			<%-- Name and icon columns --%>
			<circabc:column>
				<f:facet name="small-icon">
					<h:graphicImage url="/images/filetypes/_default.gif" width="16" height="16" alt=" "/>
				</f:facet>
				<f:facet name="header">
					<circabc:sortLink label="#{msg.name}" value="name" mode="case-insensitive" styleClass="header" tooltipAscending="${tooltipAscending}" tooltipDescending="${tooltipDescending}"/>
				</f:facet>
				<circabc:actionLink id="view-name" value="#{r.name}" tooltip="#{r.name}" href="#{r.versionUrl}" target="new" />
			</circabc:column>

			<%-- Language columns --%>
			<circabc:column>
				<f:facet name="header">
					<circabc:sortLink label="#{msg.language}" value="language" mode="case-insensitive" styleClass="header" tooltipAscending="${tooltipAscending}" tooltipDescending="${tooltipDescending}"/>
				</f:facet>
				<circabc:actionLink id="view-language" value="#{r.versionLanguage != null? r.versionLanguage : '-'}" tooltip="#{r.versionLanguage}" href="#{r.versionUrl}" target="new" />
			</circabc:column>

			<%-- view actions --%>
			<circabc:column id="col25" >
				<f:facet name="header">
					<h:outputText value="#{msg.actions}"/>
				</f:facet>
				<%-- detail basic --%>
				<circabc:actionLink id="view-link" value="#{msg.view}" tooltip="#{msg.view}" href="#{r.versionUrl}" target="new" />
			</circabc:column>

			<circabc:dataPager id="ml-container-history-pager" styleClass="pager" />

		</circabc:richList>
	</circabc:panel>

    <circabc:panel id="topOfPageAnchorLast" styleClass="topOfPageAnchor"  >
        <%-- Display the "back to top icon first and display the text after." --%>
        <circabc:actionLink id="act-link-ver-hist-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
        <circabc:actionLink id="act-link-ver-hist-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
    </circabc:panel>
</circabc:panel>

