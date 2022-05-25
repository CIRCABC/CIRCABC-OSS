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


<%@ page isELIgnored="false" %>
<f:view>
	<%-- load a bundle of properties I18N strings here --%>
	<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
	<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />
	<f:loadBundle basename="alfresco.extension.messages.bulk-upload" var="bmsg" />

  	<c:set var="currentTitle" value="${bmsg.bulk_upload_content}" />

	<%@ include file="/jsp/extension/wai/parts/header.jsp" %>
	<%@ include file="/jsp/extension/wai/parts/banner.jsp" %>

			<h:form acceptcharset="UTF-8" id="Form1Principal" styleClass="FormPrincipal" >
				<%@ include file="/jsp/extension/wai/parts/left-menu.jsp" %>
			</h:form>
			<circabc:panel id="maincontent" styleClass="maincontent" rendered="#{BulkUploadBean.hasPermission}" >
				<circabc:errors styleClass="messageGen" infoClass="messageInfo" warnClass="messageWarn" errorClass="messageError" />
				<%-- The title bar --%>
				<div id="ContentHeader">
					<circabc:panel id="contentHeaderUrlsPanel" styleClass="ContentHeaderUrls" >
						<f:verbatim><br /></f:verbatim>
						<circabc:displayer rendered="#{InterestGroupLogoBean.wizardDisplayed}">
							<h:graphicImage binding="#{InterestGroupLogoBean.otherPagesIconBinding}" id="bulkupload1-ig-logo" />
						</circabc:displayer>
					</circabc:panel>
					<div class="ContentHeaderNavigationLibrary">
						<h:form acceptcharset="UTF-8" id="Form2Principal">
							<circabc:navigationList id="navigationLibrary" value="#{WaiNavigationManager.navigation}" actionListener="#{BrowseBean.clickWai}" separatorFirst="false" styleClass="navigationLibrary" />
						</h:form>
					</div>
					<c:if test="${BulkUploadBean.isSecureTransport == false}">
						<div>
							<div class="ContentHeaderIcone"><h:graphicImage id="addContentSecureHeader" url="/images/icons/add_content_large.gif" alt="#{bmsg.bulk_content_icon_tooltip}" title="#{bmsg.bulk_content_icon_tooltip}"></h:graphicImage></div>
							<div class="ContentHeaderText">
								<span class="ContentHeaderTitle"><h:outputText id="ContentHeaderTitle" value="#{bmsg.bulk_content_title}" /></span><br />
								<span class="ContentHeaderSubTitle"><h:outputText value="#{bmsg.bulk_content_title_desc}" /></span>
							</div>
						</div>
					</c:if>
					<c:if test="${BulkUploadBean.isSecureTransport == true}">
						<div>
							<div class="ContentHeaderIcone"><h:graphicImage id="addContentNonSecureHeader" url="/images/extension/icons/add_content_large_ssl.gif" alt="#{bmsg.bulk_content_icon_tooltip}" title="#{bmsg.bulk_content_icon_tooltip}"></h:graphicImage></div>
							<div class="ContentHeaderText">
								<span class="ContentHeaderTitle"><h:outputText id="ContentHeaderTitleSecure" value="#{bmsg.bulk_content_secure_title}" /></span><br />
								<span class="ContentHeaderSubTitle"><h:outputText value="#{bmsg.bulk_content_title_desc}" /></span>
							</div>
						</div>
					</c:if>
				</div>
				<circabc:panel id="contentMain" styleClass="contentMain">

				    <%-- the right menu --%>
				    <h:form acceptcharset="UTF-8" id="Form3Principal" styleClass="FormPrincipal" >
					    <circabc:panel id="contentMainButton" styleClass="contentMainButton" >
					        <%--  The finish/cancel button --%>
					        <circabc:panel id="divButtonDialog" styleClass="divButtonDialog">
								<h:commandButton id="finish-button" styleClass="dialogButton" value="#{msg.ok}" action="#{BulkUploadBean.finish}"  disabled="#{BulkUploadBean.finishButtonDisabled }" onclick="showWaitProgress();"/><br />
            			    	<h:commandButton id="cancel-button" styleClass="dialogButton" value="#{msg.cancel}" action="#{BulkUploadBean.cancel}" />
							</circabc:panel>
							<h:outputText value="&nbsp;" escape="false"/>
					    </circabc:panel>
					</h:form>

					<div class="contentFullPage">

						<%-- Step 1: Upload --%>
						<circabc:displayer id="displayer12" rendered="#{BulkUploadBean.hasBeenUploaded == false}">
							<circabc:panel id="bulkupload-main-section" styleClass="signup_rub_title">
								<h:outputText value="#{cmsg.add_content_label}" />
							</circabc:panel>
							
							<circabc:uploadForm returnPage="/jsp/extension/wai/dialog/content/bulk-upload.jsp" submitCallback="BulkUploadBean.addFile">
								<%-- Locate the content --%>
								<h:outputText id="text1" value="1. #{cmsg.add_content_locate_content}" styleClass="signup_subrub_title"/><br />
								<input type="file" size="75" name="alfFileInput"/><br />

								<%-- Set options file --%>
								<h:outputText id="text2" value="2. #{cmsg.library_add_content_other_options}" styleClass="signup_subrub_title"/><br />
								<h:panelGrid columns="2" cellpadding="3" cellspacing="3" border="0">
									<h:selectBooleanCheckbox id="check-disable-index" value="#{BulkUploadBean.disableIndexFile}" title="#{cmsg.bulk_upload_ignore_index_file_tooltip}" />
									<h:outputText id="text-index-disabled" value="#{cmsg.bulk_upload_ignore_index_file}"/>
									<h:selectBooleanCheckbox id="check-disable-notif" value="#{BulkUploadBean.disableNotification}" title="#{cmsg.notification_disable_mechanism_tooltip}" />
									<h:outputText id="text-notif-disabled" value="#{cmsg.notification_disable_mechanism}"/>
								</h:panelGrid>

								<%-- Upload the file --%>
								<h:outputText id="text3" value="3. #{cmsg.add_content_click_upload}" styleClass="signup_subrub_title"/><br />
								<input type="submit" id="submitFile" name="submitFile" value="${cmsg.add_content_upload}" title="${cmsg.add_content_upload_tooltip}" onclick="showWaitProgress();" />
							</circabc:uploadForm>
						</circabc:displayer>

						<%-- Step 2: Simulation result --%>
						<circabc:displayer rendered="#{BulkUploadBean.hasBeenUploaded}" id="displayer21">
						    <h:form acceptcharset="UTF-8" id="Form4Principal" styleClass="FormPrincipal" >

								<br />
								<p><h:outputText value="#{cmsg.bulk_upload_status_title}" styleClass="mainSubTitle"  /></p>
								<ul>
									<h:outputText value="<li>#{cmsg.bulk_upload_status_indexfound}</li>" rendered="#{BulkUploadBean.indexFileFound == true}" escape="false"/>
									<h:outputText value="<li>#{cmsg.bulk_upload_status_noindexfound}</li>" rendered="#{BulkUploadBean.indexFileFound == false}" escape="false" />
									<h:outputText value="<li>#{cmsg.bulk_upload_status_errorfound}</li>" rendered="#{BulkUploadBean.errorFound == true}" escape="false" />
									<h:outputText value="<li>#{cmsg.bulk_upload_status_noerrorfound}</li>" rendered="#{BulkUploadBean.errorFound == false}" escape="false" />
								</ul>

								<f:verbatim><br /></f:verbatim>

								<div class="scrollPanel">
									<circabc:panel id="upload-message-2" styleClass="signup_rub_title">
										<h:outputText value="#{cmsg.bulk_upload_check_message_2}" />
									</circabc:panel>
									<f:verbatim><br /></f:verbatim>

									<circabc:richList id="bulkErrorList" viewMode="circa" styleClass="recordSet"
													headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow"
													altRowStyleClass="recordSetRowAlt" value="#{BulkUploadBean.errorMessages}"
													var="errorRecord" pageSize="#{BulkUploadBean.errorRecordPageSize}"
													initialSortColumn="rowNumber" initialSortDescending="false">
										<circabc:column id="rowNumber" primary="true">
											<f:facet name="header">
												<circabc:sortLink id="rowNumberText" label="#{bmsg.bulk_upload_row_number}" value="rowNumber" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{errorRecord.rowNumber}" />
										</circabc:column>
										<circabc:column id="errorType">
											<f:facet name="header">
												<circabc:sortLink id="errorTypeText" label="#{bmsg.bulk_upload_error_type}" value="errorType" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{errorRecord.errorType}" />
										</circabc:column>
										<circabc:column id="errorFileName">
											<f:facet name="header">
												<circabc:sortLink id="errorFileNameText" label="#{bmsg.bulk_upload_fileName}" value="fileName" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{errorRecord.fileName}" />
										</circabc:column>
										<circabc:column id="errorDescription">
											<f:facet name="header">
												<circabc:sortLink id="errorDescriptionText" label="#{bmsg.bulk_upload_error_description}" value="errorDescription" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{errorRecord.errorDescription}" />
										</circabc:column>
										<f:facet name="empty">
											<h:outputFormat id="error-empty" value="#{cmsg.bulk_upload_status_error_empty}" styleClass="noItem"/>
										</f:facet>
									</circabc:richList>
								</div>

								<br /><br />

								<div class="scrollPanel" >

									<circabc:panel id="upload-message-1" styleClass="signup_rub_title">
										<h:outputText value="#{cmsg.bulk_upload_check_message_1}" />
									</circabc:panel>
									<f:verbatim><br /></f:verbatim>

									<circabc:richList id="bulkInformationList"
													viewMode="circa"
													styleClass="recordSet"
													headerStyleClass="recordSetHeader"
													rowStyleClass="recordSetRow"
													altRowStyleClass="recordSetRowAlt"
													value="#{BulkUploadBean.indexRecords}"
													var="indexRecord"
													pageSize="#{BulkUploadBean.indexRecordPageSize}"
													initialSortColumn="name" initialSortDescending="true"
													>
										<circabc:column id="fileName" primary="true" rendered="#{BulkUploadBean.indexRecordNameCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="fileNameText" label="#{bmsg.bulk_upload_fileName}" value="name" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.name}" />
										</circabc:column>

										<circabc:column id="title" rendered="#{BulkUploadBean.indexRecordTitleCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="titleText" label="#{bmsg.bulk_upload_title}" value="title" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.title}" />
										</circabc:column>

										<circabc:column id="description"  rendered="#{BulkUploadBean.indexRecordDescriptionCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="descriptionText" label="#{bmsg.bulk_upload_description}" value="description" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.description}" />
										</circabc:column>
										<circabc:column id="author"  rendered="#{BulkUploadBean.indexRecordAuthorCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="authorText" label="#{bmsg.bulk_upload_author}" value="author" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.author}" />
										</circabc:column>
										<circabc:column id="keywords"  rendered="#{BulkUploadBean.indexRecordKeywordsCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="keywordsText" label="#{bmsg.bulk_upload_keywords}" value="keywords" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.keywords}" />
										</circabc:column>
										<circabc:column id="status"  rendered="#{BulkUploadBean.indexRecordStatusCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="statusText" label="#{bmsg.bulk_upload_status}" value="status" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.status}" />
										</circabc:column>
										<circabc:column id="issueDate" rendered="#{BulkUploadBean.indexRecordIssueDateCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="issueDateText" label="#{bmsg.bulk_upload_issue_date}" value="issueDate" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.issueDate}" />
										</circabc:column>
										<circabc:column id="reference" rendered="#{BulkUploadBean.indexRecordReferenceCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="referenceText" label="#{bmsg.bulk_upload_reference}" value="reference" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.reference}" />
										</circabc:column>
										<circabc:column id="expirationDate" rendered="#{BulkUploadBean.indexRecordExpirationDateCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="expirationDateText" label="#{bmsg.bulk_upload_expiration_date}" value="expirationDate" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.expirationDate}" />
										</circabc:column>
										<circabc:column id="securityRanking" rendered="#{BulkUploadBean.indexRecordSecurityRankingCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="securityRankingText" label="#{bmsg.bulk_upload_security_ranking}" value="securityRanking" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.securityRanking}" />
										</circabc:column>
										<circabc:column id="attri1" rendered="#{BulkUploadBean.indexRecordAttri1Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri1Text" label="#{bmsg.bulk_upload_attri1}" value="attri1" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri1}" />
										</circabc:column>
										<circabc:column id="attri2" rendered="#{BulkUploadBean.indexRecordAttri2Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri2Text" label="#{bmsg.bulk_upload_attri2}" value="attri2" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri2}" />
										</circabc:column>
										<circabc:column id="attri3" rendered="#{BulkUploadBean.indexRecordAttri3Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri3Text" label="#{bmsg.bulk_upload_attri3}" value="attri3" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri3}" />
										</circabc:column>
										<circabc:column id="attri4" rendered="#{BulkUploadBean.indexRecordAttri4Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri4Text" label="#{bmsg.bulk_upload_attri4}" value="attri4" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri4}" />
										</circabc:column>
										<circabc:column id="attri5" rendered="#{BulkUploadBean.indexRecordAttri5Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri5Text" label="#{bmsg.bulk_upload_attri5}" value="attri5" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri5}" />
										</circabc:column>
										<circabc:column id="attri6" rendered="#{BulkUploadBean.indexRecordAttri6Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri6Text" label="#{bmsg.bulk_upload_attri6}" value="attri6" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri6}" />
										</circabc:column>
										<circabc:column id="attri7" rendered="#{BulkUploadBean.indexRecordAttri7Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri7Text" label="#{bmsg.bulk_upload_attri7}" value="attri7" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri7}" />
										</circabc:column>
										<circabc:column id="attri8" rendered="#{BulkUploadBean.indexRecordAttri8Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri8Text" label="#{bmsg.bulk_upload_attri8}" value="attri8" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri8}" />
										</circabc:column>
										<circabc:column id="attri9" rendered="#{BulkUploadBean.indexRecordAttri9Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri9Text" label="#{bmsg.bulk_upload_attri9}" value="attri9" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri9}" />
										</circabc:column>
										
										<circabc:column id="attri10" rendered="#{BulkUploadBean.indexRecordAttri10Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri10Text" label="#{bmsg.bulk_upload_attri10}" value="attri10" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri10}" />
										</circabc:column>
										
										
										<circabc:column id="attri11" rendered="#{BulkUploadBean.indexRecordAttri11Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri11Text" label="#{bmsg.bulk_upload_attri1}" value="attri11" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri11}" />
										</circabc:column>
										<circabc:column id="attri12" rendered="#{BulkUploadBean.indexRecordAttri12Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri12Text" label="#{bmsg.bulk_upload_attri12}" value="attri12" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri12}" />
										</circabc:column>
										<circabc:column id="attri13" rendered="#{BulkUploadBean.indexRecordAttri13Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri13Text" label="#{bmsg.bulk_upload_attri3}" value="attri13" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri13}" />
										</circabc:column>
										<circabc:column id="attri14" rendered="#{BulkUploadBean.indexRecordAttri14Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri14Text" label="#{bmsg.bulk_upload_attri4}" value="attri14" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri14}" />
										</circabc:column>
										<circabc:column id="attri15" rendered="#{BulkUploadBean.indexRecordAttri15Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri15Text" label="#{bmsg.bulk_upload_attri5}" value="attri15" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri15}" />
										</circabc:column>
										<circabc:column id="attri16" rendered="#{BulkUploadBean.indexRecordAttri16Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri16Text" label="#{bmsg.bulk_upload_attri16}" value="attri16" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri16}" />
										</circabc:column>
										<circabc:column id="attri17" rendered="#{BulkUploadBean.indexRecordAttri17Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri17Text" label="#{bmsg.bulk_upload_attri17}" value="attri17" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri17}" />
										</circabc:column>
										<circabc:column id="attri18" rendered="#{BulkUploadBean.indexRecordAttri18Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri18Text" label="#{bmsg.bulk_upload_attri18}" value="attri8" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri18}" />
										</circabc:column>
										<circabc:column id="attri19" rendered="#{BulkUploadBean.indexRecordAttri19Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri19Text" label="#{bmsg.bulk_upload_attri19}" value="attri19" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri19}" />
										</circabc:column>
										<circabc:column id="attri20" rendered="#{BulkUploadBean.indexRecordAttri20Count > 0}">
											<f:facet name="header">
												<circabc:sortLink id="attri20Text" label="#{bmsg.bulk_upload_attri20}" value="attri20" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.attri20}" />
										</circabc:column>
										
										
										
										<circabc:column id="typeDocument" rendered="#{BulkUploadBean.indexRecordTypeDocumentCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="typeDocumentText" label="#{bmsg.bulk_upload_type_document}" value="typeDocument" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.typeDocument}" />
										</circabc:column>
										<circabc:column id="translator" rendered="#{BulkUploadBean.indexRecordTranslatorCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="translatorText" label="#{bmsg.bulk_upload_translator}" value="translator" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.translator}" />
										</circabc:column>
										<circabc:column id="docLang" rendered="#{BulkUploadBean.indexRecordDocLangCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="docLangText" label="#{bmsg.bulk_upload_doc_lang}" value="docLang" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.docLang}" />
										</circabc:column>
										<circabc:column id="noContent" rendered="#{BulkUploadBean.indexRecordNoContentCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="noContentText" label="#{bmsg.bulk_upload_no_content}" value="noContent" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.noContent}" />
										</circabc:column>
										<circabc:column id="oriLang" rendered="#{BulkUploadBean.indexRecordOriLangCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="oriLangText" label="#{bmsg.bulk_upload_ori_lang}" value="oriLang" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.oriLang}" />
										</circabc:column>
										<circabc:column id="relTrans" rendered="#{BulkUploadBean.indexRecordRelTransCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="relTransText" label="#{bmsg.bulk_upload_rel_trans}" value="relTrans" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.relTrans}" />
										</circabc:column>
										<circabc:column id="overwrite" rendered="#{BulkUploadBean.indexRecordOverwriteCount > 0}">
											<f:facet name="header">
												<circabc:sortLink id="overwriteText" label="#{bmsg.bulk_upload_overwrite}" value="overwrite" tooltipAscending="#{cmsg.generic_sort_asc}" tooltipDescending="#{cmsg.generic_sort_desc}"/>
											</f:facet>
											<h:outputText value="#{indexRecord.overwrite}" />
										</circabc:column>

										<f:facet name="empty">
											<h:outputFormat id="index-empty" value="#{cmsg.bulk_upload_status_index_empty}" styleClass="noItem"/>
										</f:facet>
									</circabc:richList>
								</div>
							</h:form>
						</circabc:displayer>
					</div>
				</circabc:panel>
			</circabc:panel>
	<%@ include file="/jsp/extension/wai/parts/footer.jsp" %>
</f:view>
