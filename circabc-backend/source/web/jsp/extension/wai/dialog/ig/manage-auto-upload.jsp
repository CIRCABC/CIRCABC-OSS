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
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/noscript.js" ></script>
	
<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<f:verbatim><h3></f:verbatim>

<h:outputText value="#{cmsg.manage_auto_upload_dialog_list_of_configuration }"/>

<f:verbatim></h3></f:verbatim>

			

			<h:dataTable id="configurations" styleClass="recordSet" value="#{ManageAutoUploadDialog.listOfConfigurations}" var="wn" >
				<%-- Primary column for details view mode --%>
				<h:column id="config_status">
					<f:facet name="header">
						<h:outputText id="header_config_status" value="#{cmsg.manage_auto_upload_status_list_title}" />
					</f:facet>
					<h:graphicImage alt="enabled" id="statusConfYes" title="Yes" value="/images/extension/icons/BulletGreen.png" rendered="#{wn.status == 1}"></h:graphicImage>
					<h:graphicImage alt="disabled" id="statusConfNo" title="No" value="/images/extension/icons/BulletRed.png" rendered="#{wn.status == 0}"></h:graphicImage>
					<h:graphicImage alt="problem" id="statusConfError" title="Problem" value="/images/extension/icons/BulletOrange.png" rendered="#{wn.status == -1}"></h:graphicImage>
					
				</h:column>
				
				<h:column id="config_path_col">
					<f:facet name="header">
						<h:outputText id="header_config_path" value="#{cmsg.manage_auto_upload_path_list_title}"  />
					</f:facet>
					<h:outputText id="conf_path" value="#{wn.parentNodeRef}"/>
				</h:column>
				
				<h:column id="config_file_col">
					<f:facet name="header">
						<h:outputText id="header_config_file" value="#{cmsg.manage_auto_upload_filename}"  />
					</f:facet>
					<h:outputText id="conf_file" value="#{wn.fileNodeRef}"/>
				</h:column>
				
				<h:column id="config_host_col">
					<f:facet name="header">
						<h:outputText id="header_config_host" value="#{cmsg.manage_auto_upload_host_list_title}"  />
					</f:facet>
					<h:outputText id="conf_host" value="#{wn.ftpHost}"/>
				</h:column>
								
				<h:column id="config_notification">
					<f:facet name="header">
						<h:outputText id="header_config_notification" value="#{cmsg.manage_auto_upload_notify}"  />
					</f:facet>
					
					<h:graphicImage alt="enabled" id="notifyYes" title="Yes" value="/images/extension/icons/BulletGreen.png" rendered="#{wn.jobNotifications == true}"></h:graphicImage>
					<h:graphicImage alt="disabled" id="notifyNo" title="No" value="/images/extension/icons/BulletRed.png" rendered="#{wn.jobNotifications == false}"></h:graphicImage>
					
				</h:column>
				
				<h:column id="config_auto_extract">
					<f:facet name="header">
						<h:outputText id="header_config_auto_extract" value="#{cmsg.manage_auto_upload_auto_extract_simple}"  />
					</f:facet>
					
					<h:graphicImage alt="enabled" id="autoExtractYes" title="Yes" value="/images/extension/icons/BulletGreen.png" rendered="#{wn.autoExtract == true}"></h:graphicImage>
					<h:graphicImage alt="disabled" id="autoExtractNo" title="No" value="/images/extension/icons/BulletRed.png" rendered="#{wn.autoExtract == false}"></h:graphicImage>
					
				</h:column>
				
				<h:column id="config_date_restriction">
					<f:facet name="header">
						<h:outputText id="header_date_restriction" value="#{cmsg.manage_auto_upload_date_restriction}"  />
					</f:facet>
					<h:outputText id="date_restriction" value="#{wn.dateRestriction}"/>
					
				</h:column>
				
				<h:column id="config_actions">
					<f:facet name="header">
						<h:outputText id="header_config_actions" value="#{cmsg.manage_auto_upload_actions}"  />
					</f:facet>
					
					<circabc:actionLink id="enable_config-link" value="#{cmsg.manage_auto_upload_enable_configuration}" tooltip="#{cmsg.profile_edit_title}" action="wai:dialog:ManageAutoUploadDialog" actionListener="#{ManageAutoUploadDialog.enableConfiguration}">
						<circabc:param id="idConfigEnable" name="idConfig" value="#{wn.idConfiguration}" />
					</circabc:actionLink>
					
					<f:verbatim>&nbsp;|&nbsp;</f:verbatim>
					
					<circabc:actionLink id="disable_config-link" value="#{cmsg.manage_auto_upload_disable_configuration}" tooltip="#{cmsg.profile_edit_title}" action="wai:dialog:ManageAutoUploadDialog" actionListener="#{ManageAutoUploadDialog.disableConfiguration}">
						<circabc:param id="idConfigDisable" name="idConfig" value="#{wn.idConfiguration}" />
					</circabc:actionLink>
					
					<f:verbatim>&nbsp;|&nbsp;</f:verbatim>
					
					<circabc:actionLink id="remove_config-link" value="#{cmsg.profile_remove_title}" tooltip="#{cmsg.profile_edit_title}" action="wai:dialog:ManageAutoUploadDialog" actionListener="#{ManageAutoUploadDialog.removeConfiguration}">
						<circabc:param id="idConfigRemove" name="idConfig" value="#{wn.idConfiguration}" />
					</circabc:actionLink>
					
					
					
				</h:column>
				
				<%-- component to display if the list is empty - normally not seen --%>
				<f:facet name="empty">
					<h:outputFormat id="no-list-items-whatsnews" value="#{cmsg.no_list_items}"  />
				</f:facet>
			</h:dataTable>
			
			<f:verbatim>
			<br/>
			<div id="legend">
			</f:verbatim>
				
				<h:graphicImage alt="enabled" id="statusConfYes2" title="Yes" value="/images/extension/icons/BulletGreen.png" ></h:graphicImage>&nbsp;<h:outputText value="#{cmsg.manage_auto_upload_status_1}"></h:outputText>
				<f:verbatim>
				<br/>
				</f:verbatim>
				<h:graphicImage alt="disabled" id="statusConfNo2" title="No" value="/images/extension/icons/BulletRed.png" ></h:graphicImage>&nbsp;<h:outputText value="#{cmsg.manage_auto_upload_status_0}"></h:outputText>
				<f:verbatim>
				<br/>
				</f:verbatim>
				<h:graphicImage alt="disabled" id="statusConfError2" title="Problem" value="/images/extension/icons/BulletOrange.png" ></h:graphicImage>&nbsp;<h:outputText value="#{cmsg.manage_auto_upload_status_minus_1}"></h:outputText>
			
			<f:verbatim>	
			</div>
				
				<br/>
				
			<fieldset class="formArea">
				<legend>
			</f:verbatim>
				
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_fieldset_legend }"></h:outputText>
				
				<f:verbatim></legend></f:verbatim>
				
				<h:inputHidden value="#{ManageAutoUploadDialog.idConfiguration }" id="idConfigRef"></h:inputHidden>
			
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_ftp_host }" for="host"/>
				<h:inputText styleClass="textNormalWidth" id="host" value="#{ManageAutoUploadDialog.host }"></h:inputText>
				
				<f:verbatim><br/></f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_ftp_port }" for="port"/>
				<h:inputText styleClass="textNormalWidth" id="port" value="#{ManageAutoUploadDialog.port }"></h:inputText>
				
				<f:verbatim>
				<br/>
				<br/>
				</f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_ftp_path }" for="path"/>
				<h:inputText styleClass="textLongWidth" id="path" value="#{ManageAutoUploadDialog.path }"></h:inputText>

				<f:verbatim>
				<br/>
				<br/>
				</f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_ftp_username }" for="username"/>
				<h:inputText styleClass="textNormalWidth" id="username" value="#{ManageAutoUploadDialog.username }"></h:inputText>
				
				<f:verbatim><br/></f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_ftp_password }" for="password"/>
				<h:inputSecret styleClass="textNormalWidth" id="password" value="#{ManageAutoUploadDialog.password }" redisplay="true"></h:inputSecret>
				
				<f:verbatim><br/></f:verbatim>
				
				<h:outputLabel value=""/>
				<h:commandButton id="testConnection" action="wai:dialog:ManageAutoUploadDialog" actionListener="#{ManageAutoUploadDialog.testConnection}" value="#{cmsg.configure_auto_upload_dialog_test_connection }"></h:commandButton>
				<h:outputText  value="#{cmsg.configure_auto_upload_dialog_test_connection_failed }" rendered="#{ManageAutoUploadDialog.testResult == -2 }"></h:outputText>
				<h:outputText escape="false" value="#{cmsg.configure_auto_upload_dialog_test_connection_wrong_path }" rendered="#{ManageAutoUploadDialog.testResult == -1 }"></h:outputText>
				<h:outputText escape="false" value="#{cmsg.configure_auto_upload_dialog_test_connection_file_not_exist }" rendered="#{ManageAutoUploadDialog.testResult == 0 }"></h:outputText>
				<h:outputText escape="false" value="#{cmsg.configure_auto_upload_dialog_test_connection_success }" rendered="#{ManageAutoUploadDialog.testResult == 1 }"></h:outputText>
				
				<f:verbatim>
				<br/>
				<br/>
				</f:verbatim>
				
				<circabc:nodeSelector id="spaceSelector" value="#{ManageAutoUploadDialog.librarySection}"
						 styleClass="selector" 
						 rootNode="#{ManageAutoUploadDialog.libraryId}"
						 initialSelection="#{ManageAutoUploadDialog.libraryId}"
						 label="#{msg.select_destination_prompt}"
						 pathLabel="#{cmsg.path_label}"
						 pathErrorMessage="#{cmsg.path_error_message}"
						 showContents="false" />
				
				<f:verbatim>
				<br/>
				</f:verbatim>
				
				
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_frequency_title }"></h:outputLabel>
				
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_frequency_day }" />
				<h:selectOneMenu value="#{ManageAutoUploadDialog.selectedDayChoice }" id="day_frequency" >
					<f:selectItems value="#{ManageAutoUploadDialog.availableDayChoices }"/>
				</h:selectOneMenu>
				&nbsp;
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_frequency_at }"/>
				&nbsp;
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_frequency_hour }"/>
				<h:selectOneMenu value="#{ManageAutoUploadDialog.selectedHourChoice }" id="hour_frequency">
					<f:selectItems value="#{ManageAutoUploadDialog.availableHourOptions }"/>
				</h:selectOneMenu>
				
				<f:verbatim>
				<br/>
				<br/>
				</f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_auto_extract }" for="auto_extract"></h:outputLabel>
				<h:selectBooleanCheckbox styleClass="textNormalWidth" id="auto_extract" value="#{ManageAutoUploadDialog.autoExtract}"></h:selectBooleanCheckbox>
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_auto_extract_helper}" />
				
				<f:verbatim><br/></f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_job_notification }" for="job_notification"></h:outputLabel>
				<h:selectBooleanCheckbox styleClass="textNormalWidth" id="job_notification" value="#{ManageAutoUploadDialog.jobNotifications}"></h:selectBooleanCheckbox>
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_status_helper}" />
				
				<f:verbatim><br/></f:verbatim>
				
				<h:outputLabel value="#{cmsg.configure_auto_upload_dialog_emails }" for="emails"/>
				<h:inputTextarea styleClass="textNormalWidth" id="emails" value="#{ManageAutoUploadDialog.emails }"></h:inputTextarea>
				<h:outputText value="#{cmsg.configure_auto_upload_dialog_emails_helped}"  styleClass="inputHelper"/>
				
				<f:verbatim><br/>
				
				<div class="submitArea"></f:verbatim>
					<h:commandButton value="#{cmsg.manage_auto_upload_add_configuration }" action="wai:dialog:ManageAutoUploadDialog" actionListener="#{ManageAutoUploadDialog.addConfiguration}" title="#{cmsg.manage_auto_upload_add_configuration }"></h:commandButton>
					<h:commandLink value="#{cmsg.manage_auto_upload_cancel_form }" action="wai:dialog:ManageAutoUploadDialog" actionListener="#{ManageAutoUploadDialog.initInputs}" title="#{cmsg.manage_auto_upload_cancel_form }"></h:commandLink>
				<f:verbatim>
				</div>
			
		</fieldset></f:verbatim>