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
<%@page import="eu.cec.digit.circabc.web.Beans"%>
<%@page import="eu.cec.digit.circabc.web.wai.bean.admin.MigrationETLBean"%>
<circabc:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />


	<%@ page isELIgnored="false"%>

  	<c:set var="currentTitle" value="${cmsg.importation_etl_dialog_browser_title}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
    <%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

<h:form acceptcharset="UTF-8" id="FormPrincipal">
	<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
</h:form>


<circabc:displayer id="displayer-is-admin" rendered="#{NavigationBean.currentUser.admin == true}">
		<div id="maincontent">
			<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />

			<%-- Content START --%>
			<div id="ContentHeader">
				<div class="ContentHeaderNavigationLibrary">
					<h:form acceptcharset="UTF-8" id="FormPrincipal2"><circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" /></h:form></div>
					<div>
						<div class="ContentHeaderIcone"><h:graphicImage url="/images/icons/add_content_large.gif" alt="#{cmsg.importation_etl_dialog_icon_tooltip}" title="#{cmsg.importation_etl_dialog_icon_tooltip}"></h:graphicImage></div>
						<div class="ContentHeaderText">
							<span class="ContentHeaderTitle"><h:outputText value="#{cmsg.importation_etl_dialog_page_title}" /></span><br />
							<span class="ContentHeaderSubTitle"><h:outputText value="#{cmsg.importation_etl_dialog_page_description}" /></span>
						</div>
					</div>
			</div>

			<div id="ContentMain">

<%-- FIRST STEP --%>

<circabc:displayer id="displayer12" rendered="#{MigrationETLOneShotBean.firstStep}">

	    <h:form acceptcharset="UTF-8" id="FormSecondary12">	
		<div class="ContentMainButton">
			<div class="divButtonDialog">
					<h:commandButton id="finish-button" styleClass="dialogButton" value="#{cmsg.nextButtonLabel}"  action="#{MigrationETLOneShotBean.finish}" onclick="showWaitProgress();"/><br />
               		<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="finish" />
			</div>
		</div>
		<div class="ContentMainForm">
			<circabc:panel id="panelAddSelectIteration" label="#{cmsg.importation_etl_dialog_select_iteration}" styleClass="panelAddContentGlobal" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.importation_etl_dialog_select_iteration}">
				<f:verbatim><br /></f:verbatim>
				<h:dataTable value="#{MigrationETLOneShotBean.iterationsDataModel}" var="row"
			                rowClasses="selectedItemsRow,selectedItemsRowAlt"
			                styleClass="selectedItems" headerClass="selectedItemsHeader"
			                cellspacing="0" cellpadding="4">
				      <h:column>
				         <f:facet name="header">
							<h:outputText value=" " />
					     </f:facet>
						<h:selectOneRadio value="#{MigrationETLOneShotBean.selectedIteration}"  onclick="dataTableSelectOneRadio(this);">
							<f:selectItem itemValue="#{row.identifier}" itemLabel=""/>
						</h:selectOneRadio>
					  </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_date}" />
				         </f:facet>
						<h:outputText value="#{row.iterationStartDate}" escape="false">
							<a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
						</h:outputText>
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_identifier}" />
				         </f:facet>
				         <h:outputText value="#{row.identifier}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_user}" />
				         </f:facet>
				         <h:outputText value="#{row.creator}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_description}" />
				         </f:facet>
				         <h:outputText value="#{row.description}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_num_etl}" />
				         </f:facet>
				         <h:outputText value="#{row.transformationDatesSize}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_num_transformed}" />
				         </f:facet>
				         <h:outputText value="#{row.importedDatesSize}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.manage_importations_dialog_page_num_failed}" />
				         </f:facet>
				         <h:outputText value="#{row.failedImportationSize}" />
				      </h:column>
				</h:dataTable>
			</circabc:panel>
			</h:form>
		</div>
</circabc:displayer>

<%-- SECOND STEP --%>

<circabc:displayer id="displayer123" rendered="#{MigrationETLOneShotBean.secondStep}">


	<h:form acceptcharset="UTF-8" id="FormPrincipalSelectSecon">
		<div class="ContentMainButton">
			<div class="divButtonDialog">
				<%-- h:form acceptcharset="UTF-8" id="FormSecondary12Bis" --%>
				<h:commandButton id="finish-button" styleClass="dialogButton" value="#{cmsg.nextButtonLabel}"  action="#{MigrationETLOneShotBean.finish}" onclick="showWaitProgress();" /><br />
               	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="finish" />
               	<%-- /h:form --%>
			</div>
		</div>
		<div class="ContentMainForm">
			<h:inputHidden id="selectListAnchor" value="#{MigrationETLOneShotBean.lastVisitedAnchor}"/>
			<%-- SECOND STEP: first section (if selected) --%>

			<!--  Actions  -->
			<h:panelGrid columns="1" cellpadding="2" cellspacing="1" border="0" id="godown" styleClass="wai_dialog_more_action">
				<f:verbatim>
					<a id="goinvalid" href="#uptovalid" title="Go to the invalid user list" >Invalid list</a>
				</f:verbatim>
				<f:verbatim>
					<a id="golastvisited" href="#uptovalid" title="Last visited row" >Last visited</a>
				</f:verbatim>
			</h:panelGrid>

			<circabc:displayer id="displayerEditUser" rendered="#{MigrationETLOneShotBean.userToEdit != null}">
				<circabc:panel id="edit-user-panel" styleClass="infoPanel" styleClassLabel="infoContent"  >

					<%-- SECOND STEP: first section (if selected), first panel: edit user data  --%>

					<circabc:panel id="edit-user-panel-sub" styleClass="signup_rub_title">
						<h:outputText value="#{cmsg.importation_etl_dialog_action_edit_data}:" escape="false" />
					</circabc:panel>

					<h:panelGrid columns="4" cellpadding="3" cellspacing="3" border="0" style="padding-top: 4px;"
								 rowClasses="wizardSectionHeading, paddingRow">

						<h:outputText value="#{cmsg.importation_etl_dialog_ecasname}:&nbsp;" escape="false"/>
						<h:inputText value="#{MigrationETLOneShotBean.searchForUid}" onkeypress="if (event.keyCode == 13){ document.getElementById('FormPrincipalSelectSecon:manual-edit-search-uid').click(); return false;}"/>
						<h:commandButton id="manual-edit-search-uid" value="#{cmsg.importation_etl_dialog_action_search_uid}" actionListener="#{MigrationETLOneShotBean.searchEcas}" rendered="true" title="#{cmsg.importation_etl_dialog_action_search_uid}" onclick="submit();showWaitProgress();" image="/images/icons/comparetoprevious.png" >
							<circabc:param  name="userid" value="true" />
						</h:commandButton>
						<circabc:panel id="info-search-uid" styleClass="etlSearchComment">
							<h:outputText value="#{cmsg.importation_etl_dialog_action_search_exactly}" escape="false"/>
						</circabc:panel>

						<h:outputText value="#{cmsg.importation_etl_dialog_moniker}:&nbsp;" escape="false"/>
						<h:inputText value="#{MigrationETLOneShotBean.searchForMoniker}" onkeypress="if (event.keyCode == 13){ document.getElementById('FormPrincipalSelectSecon:manual-edit-search-moniker').click(); return false;}"/>
						<h:commandButton id="manual-edit-search-moniker" value="#{cmsg.importation_etl_dialog_action_search_moniker}" actionListener="#{MigrationETLOneShotBean.searchEcas}" rendered="true" title="#{cmsg.importation_etl_dialog_action_search_moniker}" onclick="submit();showWaitProgress();" image="/images/icons/comparetoprevious.png" >
							<circabc:param  name="moniker" value="true" />
						</h:commandButton>
						<circabc:panel id="info-search-moniker" styleClass="etlSearchComment">
							<h:outputText value="#{cmsg.importation_etl_dialog_action_search_approximatively}" escape="false" />
						</circabc:panel>

						<h:outputText value="#{cmsg.importation_etl_dialog_email}:&nbsp;" escape="false"/>
						<h:inputText value="#{MigrationETLOneShotBean.searchForEmail}" onkeypress="if (event.keyCode == 13){ document.getElementById('FormPrincipalSelectSecon:manual-edit-search-email').click(); return false;}"/>
						<h:commandButton id="manual-edit-search-email" value="#{cmsg.importation_etl_dialog_action_search_email}" actionListener="#{MigrationETLOneShotBean.searchEcas}" rendered="true" title="#{cmsg.importation_etl_dialog_action_search_email}" onclick="submit();showWaitProgress();" image="/images/icons/comparetoprevious.png" >
							<circabc:param  name="email" value="true" />
						</h:commandButton>
						<circabc:panel id="info-search-email" styleClass="etlSearchComment">
							<h:outputText value="#{cmsg.importation_etl_dialog_action_search_approximatively}" escape="false" />
						</circabc:panel>

						<h:outputText value="#{cmsg.importation_etl_dialog_cn}:&nbsp;" escape="false"/>
						<h:inputText value="#{MigrationETLOneShotBean.searchForCn}" onkeypress="if (event.keyCode == 13){ document.getElementById('FormPrincipalSelectSecon:manual-edit-search-cn').click(); return false;}"/>
						<h:commandButton id="manual-edit-search-cn" value="#{cmsg.importation_etl_dialog_action_search_cn}" actionListener="#{MigrationETLOneShotBean.searchEcas}" rendered="true" title="#{cmsg.importation_etl_dialog_action_search_cn" onclick="submit();showWaitProgress();" image="/images/icons/comparetoprevious.png" >
							<circabc:param  name="cn" value="true" />
						</h:commandButton>
						<circabc:panel id="info-search-cn" styleClass="etlSearchComment">
							<h:outputText value="#{cmsg.importation_etl_dialog_action_search_approximatively}" escape="false" />
						</circabc:panel>

					</h:panelGrid>

					<%-- SECOND STEP: first section (if selected), second panel: view etl propositins  --%>

					<circabc:panel id="edit-user-panel-sub2"  styleClass="signup_rub_title">
						<h:outputFormat value="#{cmsg.importation_etl_dialog_action_available_propositions}:" escape="false" >
							<circabc:param value="#{MigrationETLOneShotBean.userToEdit.person.userId.value}" />
							<circabc:param value="#{MigrationETLOneShotBean.userToEdit.person.email.value}" />
						</h:outputFormat>
					</circabc:panel>
					<f:verbatim><br /></f:verbatim>

					<h:dataTable value="#{MigrationETLOneShotBean.userToEdit.propositionsKeys}" var="key"
				                rowClasses="selectedItemsRow,selectedItemsRowAlt"
				                styleClass="selectedItems" headerClass="selectedItemsHeader"
				                cellspacing="0" cellpadding="4">

					      <h:column>
					         <f:facet name="header">
					            <h:outputText value="#{cmsg.importation_etl_dialog_ecasname}" />
					         </f:facet>
					         <h:outputText value="#{key}" />
					      </h:column>
					      <h:column>
					         <f:facet name="header">
					            <h:outputText value="#{cmsg.importation_etl_dialog_moniker}" />
					         </f:facet>
					         <h:outputText value="#{MigrationETLOneShotBean.userToEdit.propositions[key].moniker}" />
					      </h:column>
					      <h:column>
					         <f:facet name="header">
					            <h:outputText value="#{cmsg.importation_etl_dialog_full_name}" />
					         </f:facet>
					         <h:outputText value="#{MigrationETLOneShotBean.userToEdit.propositions[key].firstname}&nbsp;#{MigrationETLOneShotBean.userToEdit.propositions[key].lastname}" escape="false"/>
					      </h:column>
					   	  <h:column>
					         <f:facet name="header">
					            <h:outputText value="#{cmsg.importation_etl_dialog_email}" />
					         </f:facet>
					         <h:outputText value="#{MigrationETLOneShotBean.userToEdit.propositions[key].email}" />
					      </h:column>
						<h:column>
					         <f:facet name="header">
					            <h:outputText value="#{cmsg.importation_etl_dialog_action}" />
					         </f:facet>
							<circabc:actionLink actionListener="#{MigrationETLOneShotBean.removeSelection}"
								image="/images/icons/approve.gif" value="#{cmsg.importation_etl_dialog_action_make_valid}" tooltip="#{cmsg.importation_etl_dialog_action_make_valid}" showLink="false" styleClass="pad6Left" >
								<circabc:param  name="userid" value="#{key}" />
							</circabc:actionLink>
							<f:verbatim>&nbsp;</f:verbatim>
							<circabc:actionLink actionListener="#{MigrationETLOneShotBean.updateSelection}"
								image="/images/icons/blog_update.png" value="#{cmsg.importation_etl_dialog_action_select_edit}" tooltip="#{cmsg.importation_etl_dialog_action_select_edit}" showLink="false" styleClass="pad6Left" >
								<circabc:param  name="userid" value="#{key}" />
							</circabc:actionLink>
							<f:verbatim>&nbsp;</f:verbatim>
						</h:column>
					</h:dataTable>
				</circabc:panel>
				<f:verbatim><br /><hr /><br /></f:verbatim>
			</circabc:displayer>


			<%-- SECOND STEP: second section (valid users) --%>

			<circabc:panel id="panelvalidUserDataModel" label="#{cmsg.importation_etl_dialog_valid_list}" styleClass="panelAddContentGlobal" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.importation_etl_dialog_valid_list}">
				<f:verbatim><br /></f:verbatim>
				<h:dataTable value="#{MigrationETLOneShotBean.validUserDataModel}" var="val"
			                rowClasses="selectedItemsRow,selectedItemsRowAlt"
			                styleClass="selectedItems" headerClass="selectedItemsHeader"
			                cellspacing="0" cellpadding="4">

				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.importation_etl_dialog_circaname}" />
				         </f:facet>
				         <h:outputText value="#{val.originalPerson.userId.value}" />
				      </h:column>
				      <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.importation_etl_dialog_ecasname}" />
				         </f:facet>
				         <h:outputText value="#{val.validUserId}" />
				      </h:column>
				   	  <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.importation_etl_dialog_email}" />
				         </f:facet>
				         <h:outputText value="#{val.validUserData.email}" />
				      </h:column>
					<h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.importation_etl_dialog_action}" />
				         </f:facet>
						<circabc:actionLink actionListener="#{MigrationETLOneShotBean.makePathologic}"
							image="/images/icons/abort_submission.gif" value="#{cmsg.importation_etl_dialog_action_make_pathologic}" tooltip="#{cmsg.importation_etl_dialog_action_make_pathologic}" showLink="false" styleClass="pad6Left" />
						<f:verbatim>&nbsp;</f:verbatim>
					</h:column>
			    </h:dataTable>
			</circabc:panel>

			<circabc:panel id="topOfPageAnchorCatHomeRegistredMid" styleClass="topOfPageAnchor">
				<%-- Display the "back to top icon first and display the text after." --%>
				<circabc:actionLink id="topOfPageAnchorCatHomeRegistredMid-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="true" />
				<circabc:actionLink id="topOfPageAnchorCatHomeRegistredMid-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#top" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
			</circabc:panel>

			<%-- SECOND STEP: third section: invalid user  --%>

			<f:verbatim><br /><hr /><br /></f:verbatim>
			<f:verbatim><a name="uptovalid"></a></f:verbatim>

			<circabc:panel id="panelinvalidUserDataModel" label="#{cmsg.importation_etl_dialog_invalid_list}" styleClass="panelAddContentGlobal" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.importation_etl_dialog_invalid_list}">
				<f:verbatim><br /></f:verbatim>
				<h:dataTable value="#{MigrationETLOneShotBean.invalidUserDataModel}" var="inval"
			                rowClasses="selectedItemsRow,selectedItemsRowAlt"
			                styleClass="selectedItems" headerClass="selectedItemsHeader"
			                cellspacing="0" cellpadding="4">

				  <h:column>
			         <f:facet name="header"  >
			            <h:outputText value="" />
			         </f:facet>
					 <h:outputText value="#{MigrationETLOneShotBean.incAnchorCounter}" />
			      </h:column>

			      <h:column>
			         <f:facet name="header"  >
			            <h:outputText value="#{cmsg.importation_etl_dialog_circaname}" />
			         </f:facet>
					 <f:verbatim><div style="width: 120px; word-wrap: break-word;"></f:verbatim>
				         <h:outputText value="#{inval.person.userId.value}"/><f:verbatim><br /></f:verbatim>
					 <f:verbatim></div></f:verbatim>
			      </h:column>
			      <h:column>
			         <f:facet name="header">
			            <h:outputText value="#{cmsg.importation_etl_dialog_message}"  />
			         </f:facet>
					 <f:verbatim><div style="width: 120px; word-wrap: break-word;"></f:verbatim>
			         	<h:outputText value="#{inval.message}" />
					 <f:verbatim></div></f:verbatim>
			      </h:column>
			      <h:column>
			         <f:facet name="header">
			            <h:outputText value="#{cmsg.importation_etl_dialog_email}"  />
			         </f:facet>
					 <f:verbatim><div style="width: 120px; word-wrap: break-word;"></f:verbatim>
				         <h:outputText value="#{inval.person.email.value}"  />
					 <f:verbatim></div></f:verbatim>
			      </h:column>
				  <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.importation_etl_dialog_propositions}"  />
				         </f:facet>
				        <h:selectOneMenu id="language" value="#{inval.selectedUser}" onchange="document.getElementById('FormPrincipalSelectSecon:selectListAnchor').setAttribute('value','#'+this.id); submit(); return true;" disabled="#{inval.disabled}" style="width: 200px; word-wrap: break-word;" >
								<f:selectItem  itemLabel="#{cmsg.importation_etl_dialog_select_one}" itemValue="__NULL___"/>
								<f:selectItems value="#{inval.propositionItems}"/>
						</h:selectOneMenu>
				  </h:column>
				  <h:column>
				         <f:facet name="header">
				            <h:outputText value="#{cmsg.importation_etl_dialog_action}" />
				         </f:facet>
						<circabc:actionLink actionListener="#{MigrationETLOneShotBean.makeValid}"
							image="/images/icons/approve.gif" value="#{cmsg.importation_etl_dialog_action_make_valid}" tooltip="#{cmsg.importation_etl_dialog_action_make_valid}" showLink="false" styleClass="pad6Left" >
							<circabc:param name="anchor" value="ninvalidAnchor#{MigrationETLOneShotBean.anchorCounter}" />
						</circabc:actionLink>
						<f:verbatim>&nbsp;</f:verbatim>
						<circabc:actionLink actionListener="#{MigrationETLOneShotBean.selectForEditing}"
							image="/images/icons/blog_update.png" value="#{cmsg.importation_etl_dialog_action_select_edit}" tooltip="#{cmsg.importation_etl_dialog_action_select_edit}" showLink="false" styleClass="pad6Left" >
							<circabc:param name="anchor" value="ninvalidAnchor#{MigrationETLOneShotBean.anchorCounter}" />
						</circabc:actionLink>
						<f:verbatim>&nbsp;</f:verbatim>
					</h:column>
				</h:dataTable>
			</circabc:panel>
			<f:verbatim><br /></f:verbatim>

			<circabc:panel id="topOfPageAnchorCatHomeRegistredEnd" styleClass="topOfPageAnchor">
				<%-- Display the "back to top icon first and display the text after." --%>
				<circabc:actionLink id="topOfPageAnchorCatHomeRegistredEnd-1" value="#{cmsg.top_page}&nbsp;" tooltip="#{cmsg.back_top_page}" href="#uptovalid" styleClass="topOfPageAnchor" showLink="true" />
				<circabc:actionLink id="topOfPageAnchorCatHomeRegistredEnd-2" value="#{cmsg.top_page}" tooltip="#{cmsg.back_top_page}" href="#uptovalid" styleClass="topOfPageAnchor" showLink="false" image="#{currentContextPath}/images/extension/top_ns.gif" />
			</circabc:panel>
		</div>
	</h:form>
</circabc:displayer>

				<%-- THIRD STEP  --%>

<circabc:displayer id="displayer1234" rendered="#{MigrationETLOneShotBean.thirdStep}">
	<h:form acceptcharset="UTF-8" id="FormPrincipalSelectThird">
		<div class="ContentMainButton">
			<div class="divButtonDialog">
                	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.close}" action="finish" />
			</div>
		</div>
		<div class="ContentMainForm">
			<circabc:panel id="panelAddSelectIteration" label="#{cmsg.add_content_label}" styleClass="panelAddContentGlobal" styleClassLabel="panelAddContentLabel" tooltip="#{cmsg.add_content_label_tooltip}">
				<circabc:panel id="etl--warning" styleClass="infoPanel" styleClassLabel="infoContent" >
					<h:graphicImage id="etl-image-warning" value="/images/icons/warning.gif" title="#{cmsg.message_warn_tooltip}" alt="#{cmsg.message_warn_tooltip}"  />
					<h:outputText id="etl-text-warning" value="#{MigrationETLOneShotBean.message}" escape="false" />
				</circabc:panel>
			</circabc:panel>
		</div>
	</h:form>
</circabc:displayer>

<%-- Content END --%>

		</div>
	</div>
</circabc:displayer>

<circabc:displayer id="displayer-isnot-admin" rendered="#{NavigationBean.currentUser.admin == false}">
	<h1>Only super adminstrator can access to this page!</h1>
</circabc:displayer>



<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</circabc:view>

<script type="text/javascript">

	if (document.getElementById('FormPrincipalSelectSecon:selectListAnchor') !== null)
	{
		document.getElementById('golastvisited').href = document.getElementById('FormPrincipalSelectSecon:selectListAnchor').value;
	}
    function dataTableSelectOneRadio(radio)
    {
        var el = radio.form.elements;
        for (var i = 0; i < el.length; i++) 
        {
        	if (el[i].type == 'radio') {
                el[i].checked = false;
            }
        }
        radio.checked = true;
    }

 </script>


</script>

