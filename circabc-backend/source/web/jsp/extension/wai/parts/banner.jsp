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
<%@ page buffer="32kb" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page isELIgnored="false"%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<f:loadBundle basename="alfresco.extension.config.circabc-global-settings" var="contactProps" />

<!-- Banner START -->
<h:form acceptcharset="UTF-8" id="FormBanner">

<div id="bannerBackground">
<div id="bannerTop">
 <div id="langsSelector">
                      <script type="text/javascript" language="JavaScript">
							document.write('<h:selectOneMenu style="font-size:11pt;" id="language" value="#{UserPreferencesBean.language}" onchange="document.forms[0].submit(); return true;"><f:selectItems value="#{UserPreferencesBean.languages}" /></h:selectOneMenu>');
						</script>
						<noscript id="langsel_noscript">
							<c:forEach var="langueJavaDisable" items="${UserPreferencesBean.languages}">
								<c:choose>
									<c:when test="${langueJavaDisable.value == currentLocale}">
										<span class="languagenolink" lang="${langueJavaDisable.value}" title="${langueJavaDisable.label}">${langueJavaDisable.value}</span>
									</c:when>
									<c:otherwise>
										<input type="submit" class="languageBanner" lang="${langueJavaDisable.value}" value="${langueJavaDisable.value}" name="FormPrincipal:language" alt="${langueJavaDisable.label}" />
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</noscript>
                  </div>
                  
	<circabc:displayer id="defaultBannerLogoDisplayer" rendered="#{NavigationBean.customBannerLink == null}">                  
		<img id="logo" src="${currentContextPath}/images/extension/banner/circabc-title-banner-${currentLocale}.png" alt="European Commission CIRCABC logo"/>
	</circabc:displayer>
	<circabc:displayer id="customBannerLogoDisplayer" rendered="#{NavigationBean.customBannerLink != null}">                  
		<h:graphicImage id="customBannerLogo" value="#{NavigationBean.customBannerLink }" style="max-height:74px;margin-left:40px; margin-top:10px;"></h:graphicImage>
	</circabc:displayer>
            <div class="bannerRight">
                
                <div id="linkBox">
                	
                	<c:if test="${!NavigationBean.echaEnabled}">

	                    <div id="linkBoxTools">
							<ul>
									<!-- search -->
									<circabc:displayer id="searchFormDisplayer" rendered="#{NavigationBean.searchLinkDisplayed == true}">
										<li class="first"><a accesskey="4" href="http://ec.europa.eu/geninfo/query/search_${currentLocale}.html" target="_blank"><h:outputText id="searchtext" value="#{cmsg.igroot_search}"/></a></li>
									|
									</circabc:displayer>
									<!-- contact -->
									<li>
										<h:outputLink id="customContactLink" rendered="#{NavigationBean.contactLink != null }" value="#{NavigationBean.contactLink}"><h:outputText id="contactText" value="#{cmsg.contact}"/></h:outputLink>
										
										<circabc:displayer id="contactFormDisplayer" rendered="#{NavigationBean.contactLink == null}">
											<a id="contactLink" accesskey="7" href="" >Contact</a>
											<div id="contactLinkDiv">
												<img src="${currentContextPath}/images/extension/help.png" style="float:right;margin:10px;width:100px;" alt="/images/extension/help.png"/>
												<h:outputText escape="false" id="messagepart1" value="#{cmsg.contact_message_part_one}"></h:outputText>
												<h:outputText escape="false" id="messagepart2" value="#{cmsg.contact_message_part_two}"></h:outputText>
												<img src="${currentContextPath}/images/extension/mails.png" alt="mails" style="float:right;"/>
												<h:outputText escape="false" id="messagepart3" value="#{cmsg.contact_message_part_three}"></h:outputText>
												
												<hr noshade="noshade" style="color:#F5F5F5;"/>
												<br/>
												<b><h:outputText id="valuemessagecontact" value="#{cmsg.contact_message_or}"></h:outputText></b>
												<br/>

												<circabc:actionLink id="contact_form_link" value="#{cmsg.contact_message_go_to_link}" action="wai:dialog:contactFormWai"  tooltip="#{cmsg.contact_message_go_to_title}" actionListener="#{WaiDialogManager.setupParameters}"/>
												
											</div>
										</circabc:displayer>
										
									</li>
									<circabc:displayer id="legalFormDisplayer" rendered="#{NavigationBean.legalLinkDisplayed == true}">
									|
									<!-- legal notice -->		<li><a accesskey="8" id="legalNotice" href="http://ec.europa.eu/geninfo/legal_notices_${currentLocale}.htm"  target="_blank">Legal notice</a></li>
									</circabc:displayer>
							</ul>
	                     </div>
                	
                	</c:if>
                	
                	<c:if test="${NavigationBean.echaEnabled}">
                	
						<ul style="list-style-type: none;">
								<!-- search -->
								<circabc:displayer id="searchFormDisplayer2" rendered="#{NavigationBean.searchLinkDisplayed == true}">
									<li class="first"><a accesskey="4" href="http://ec.europa.eu/geninfo/query/search_${currentLocale}.html" target="_blank"><h:outputText id="searchtext2" value="#{cmsg.igroot_search}"/></a></li>
								|
								</circabc:displayer>
								<!-- contact -->
								<li>
									<h:outputLink id="customContactLink2" rendered="#{NavigationBean.contactLink != null }" value="#{NavigationBean.contactLink}"><h:outputText id="contactText2" value="#{cmsg.contact}"/></h:outputLink>
									
									<circabc:displayer id="contactFormDisplayer2" rendered="#{NavigationBean.contactLink == null}">
										<a id="contactLink2" accesskey="7" style="position: relative; left: -5px; top: -12px; text-decoration: none; font-size: 14px;" href="" >Contact</a>
										<div id="contactLinkDiv2" class="contentMain" style="height: 240px;">

											<div style="float: right; width: 15%;">
												<div style="height: 160px;">
													<img src="${currentContextPath}/images/extension/help.png" alt="/images/extension/help.png" style="max-height: 100%; max-width: 100%; height: auto; width: auto;" />
												</div>
												<div>
													<img src="${currentContextPath}/images/extension/mails.png" alt="mails" style="max-height: 100%; max-width: 100%; height: auto; width: auto;" />
												</div>
											</div>
											<div style="float: left; width: 85%;">
												<table class="tableBigLink">
													<tbody>
														<tr>
															<td>
																<a href="https://comments.echa.europa.eu/comments_cms/SCIRCABCHelpdesk.aspx" id="a6" class="bigLink" title="ECHA" target="_blank"></a>
															</td>
															<td>
																<circabc:actionLink id="a10" styleClass="bigLink" value="" 
																	action="wai:dialog:contactFormWai" tooltip="EC" 
																	actionListener="#{WaiDialogManager.setupParameters}"/>
															</td>
														</tr>
														<tr>
															<td colspan="2">
																<circabc:actionLink value="#{cmsg.welcome_page_help}"
																	tooltip="#{cmsg.welcome_page_help}"
																	href="#{WelcomeBean.helpLink}" id="a9" styleClass="bigLink" target="_blank">
																</circabc:actionLink>
															</td>
<!-- 															<td>
																<circabc:actionLink id="a8" styleClass="bigLink" value="" 
																	action="wai:dialog:contactFormWai" tooltip="S-CIRCABC" 
																	actionListener="#{WaiDialogManager.setupParameters}"/>
															</td> -->
														</tr>
													</tbody>
												</table>
											</div>
										</div>
									</circabc:displayer>
								</li>
								<circabc:displayer id="legalFormDisplayer2" rendered="#{NavigationBean.legalLinkDisplayed == true}">
								|
								<!-- legal notice -->		<li><a accesskey="8" id="legalNotice2" href="http://ec.europa.eu/geninfo/legal_notices_${currentLocale}.htm"  target="_blank">Legal notice</a></li>
								</circabc:displayer>
						</ul>
					
					</c:if>
					
                </div>
           

                  

				 </div>	
        </div>
          <div id="bannerBottom">
          <div style="float: right;  position: relative;  top: 7px; right: 7px">
        		<circabc:actionLink href="#{NavigationBean.switchNewUiLink}" value="" tooltip="#{cmsg.welcome_page_swhitch_help}" styleClass="switch-link-ui">
        		</circabc:actionLink>
        	</div>
  		</div>
  </div>

        
        <div id="path">
            <div>
                <ul>
                    <li class="first-child">
                        <a id="firstTab" href="http://europa.eu/index_${currentLocale}.htm">${cmsg.banner_europa}</a>
                    </li>
                    &gt;
                    <li>
						<a href="http://ec.europa.eu/index_${currentLocale}.htm">${cmsg.banner_european_commission}</a>
					</li>
					&gt;
                    <li>
						<circabc:actionLink id="circabchomelink" value="#{NavigationBean.appName}" actionListener="#{BrowseBean.clickCircabcHome}" tooltip="#{NavigationBean.appName}" onclick="showWaitProgress();"/>
					</li>

						<circabc:navigationList bannerStyle="banner" id="navigationTitle" value="#{BrowseBean.navigationListTitleBar}" renderPropertyName="#{BrowseBean.bannerRenderPropertyName}" actionListener="#{BrowseBean.clickWai}" onclick="showWaitProgress();"/>
<!-- #{BrowseBean.renderPropertyName} -->
                </ul>
            </div>
        </div>


<!-- System Message -->
<h:outputText rendered="#{SystemMessageBean.showMessage}" value="#{SystemMessageBean.message}" styleClass="systemMessage" escape="false" />

</h:form>

		
		

<link rel="stylesheet" href="<%=request.getContextPath()%>/css/extension/smoothbox.css" type="text/css" />
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/smoothbox.js" ></script>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/mootools-1.2.1-core-nc.js" ></script>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/progress.js" ></script>




<!-- Banner STOP -->