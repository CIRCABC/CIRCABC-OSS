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
<%@ page isELIgnored="false"%>

	<div id="tabMenu">
		<div id="tabMenuBlue">${cmsg.self_main_menu}</div>
		<div id="tabMenuSub">
			<ul>
				<circabc:displayer rendered="#{NavigationBean.circabcHomeNode.rootCategoryHeader != null}" >
					<li>
						<circabc:actionLink value="#{cmsg.self_browse_categories}" tooltip="#{cmsg.self_browse_categories_tooltip}"  actionListener="#{BrowseBean.clickWai}"  immediate="true"  onclick="showWaitProgress();">
							<circabc:param name="id" value="#{NavigationBean.circabcHomeNode.rootCategoryHeader.id}" />
						</circabc:actionLink>
					</li>
				</circabc:displayer>
				<circabc:displayer rendered="#{NavigationBean.enterpriseEnabled }" >
				    <li> <circabc:actionLink value="#{cmsg.self_login}" action="ecaspage" tooltip="#{cmsg.self_login_tooltip}" immediate="true" /></li>
				    <c:if test="${NavigationBean.multifactorEnabled}">
				    	<li> <circabc:actionLink value="#{cmsg.self_login_multifactor}" action="multifactor" tooltip="#{cmsg.self_login_tooltip}" immediate="true" /></li>
				    </c:if>
				</circabc:displayer>
			    <circabc:displayer rendered="#{!NavigationBean.enterpriseEnabled}" >
			    	<li> <circabc:actionLink value="#{cmsg.self_sign_up}" action="wai:dialog:selfRegisterWai"  actionListener="#{DialogManager.setupParameters}" tooltip="#{cmsg.self_sign_up_tooltip}" immediate="true">
						<circabc:param name="new" value="true" />
					 </circabc:actionLink> </li>
				<li> <circabc:actionLink value="#{cmsg.self_login}" action="wai:dialog:logout" tooltip="#{cmsg.self_login_tooltip}" immediate="true" /></li>
			    </circabc:displayer>
			    <li> <circabc:actionLink value="#{cmsg.self_help}" href="#{WaiLeftMenuBean.helpLink}" tooltip="#{cmsg.self_help_tooltip}" immediate="true" target="_blank" /></li>
			    
			    <circabc:displayer rendered="#{WaiLeftMenuBean.eLearningLink != null && WaiLeftMenuBean.eLearningEnabled == true}" >
					<li>
						<circabc:actionLink value="#{WaiLeftMenuBean.eLearningLinkTitle}" id="eLearningLinkMenu"
							tooltip="#{cmsg.welcome_page_elearning}"
							href="#{WaiLeftMenuBean.eLearningLink}" target="_blank">
						</circabc:actionLink>
					</li>
				</circabc:displayer>
			    
			</ul>
		</div>
	</div>
	<div id="tabLogo">
	  
      <p><a href="http://www.w3.org/WAI" title="${cmsg.self_w3c_wai_1_title}"> <img src="${currentContextPath}/images/extension/w3c2.gif" alt="${cmsg.self_w3c_wai_1_alt}" /></a></p>
	</div>
