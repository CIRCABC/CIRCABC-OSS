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
    
	<script type="text/javascript">
		
		/*
		   Function handleKeyPress is encoded inside the text field of the 
		   circabc:autonomySearch component (class 
		   eu.cec.digit.circabc.web.ui.repo.component.UIAutonomySearch) 
		   This implementation forces a 'Go' button click when the user 
		   presses ENTER, to execute the search action in case there is 
		   another submit defined on the same page.
		*/
		function handleKeyPress(inField, e) {
			
		    var charCode;
		    
		    if (e && e.which) {
		        charCode = e.which;
		    } 
		    else if (window.event) {
		        e = window.event;
		        charCode = e.keyCode;
		    }
			
		    if (charCode == 13) {
		    	document.getElementById('FormPrincipal:searchAutonomy').click();
		    }
		}
		
	</script>
	
	<div id="tabMenu">
		<div id="tabMenuBlue">${cmsg.self_main_menu}</div>
		<div id="tabMenuSub">
			<ul>
				<li>
					<circabc:actionLink value="#{cmsg.self_browse_categories}" tooltip="#{cmsg.self_browse_categories_tooltip}"  actionListener="#{BrowseBean.clickWai}" rendered="#{NavigationBean.circabcHomeNode.rootCategoryHeader != null}" immediate="true"  onclick="showWaitProgress();">
						<circabc:param name="id" value="#{NavigationBean.circabcHomeNode.rootCategoryHeader.id}" />
					</circabc:actionLink>
				</li>
				<li> <circabc:actionLink value="#{cmsg.self_logout} (#{NavigationBean.currentUserName})" action="#{LoginBean.logout}" tooltip="#{cmsg.self_logout_tooltip}" immediate="true" /></li>
				<li> <circabc:actionLink value="#{cmsg.self_help}" href="#{WaiLeftMenuBean.helpLink}" tooltip="#{cmsg.self_help_tooltip}" immediate="true" target="_blank"/></li>
				<circabc:displayer rendered="#{WaiLeftMenuBean.eLearningLink != null && WaiLeftMenuBean.eLearningEnabled == true}" >
					<li>
						<circabc:actionLink value="#{WaiLeftMenuBean.eLearningLinkTitle}" id="eLearningLinkMenu"
							tooltip="#{cmsg.welcome_page_elearning}"
							href="#{WaiLeftMenuBean.eLearningLink}" target="_blank">
						</circabc:actionLink>
					</li>
				</circabc:displayer>
				<c:if test="${WaiLeftMenuBean.interestGroupDisplay == false}">
					<li>
						<circabc:actionLink value="#{cmsg.self_admininitration}" action="wai:dialog:adminConsoleWai" actionListener="#{WaiDialogManager.setupParameters}" tooltip="#{cmsg.self_admininitration_tooltip}">
							<circabc:param name="id" value="#{NavigationBean.currentNode.id}" />
						</circabc:actionLink>
					</li>
				</c:if>
			</ul>
		</div>
		<c:if test="${WaiLeftMenuBean.autonomyEnabled}">
			<div id="tabMenuBlue">${cmsg.autonomy_search_menu_title}</div>
			<circabc:autonomySearch id="searchAutonomy" label="#{cmsg.autonomy_search_box_label}" labelAltText="#{cmsg.search_box_label_tooltip}" button="#{cmsg.search_box_button}" buttonAltText="#{cmsg.autonomy_search_box_button_tooltip}" actionListener="#{WaiLeftMenuBean.searchAutonomy}" styleClass="searchSimple" />
		</c:if>
	</div>
	<div id="tabLogo">
      
	  <%--  Should test if the current dialog is WAI or Not --%>
      <c:if test="${true}">
      	<p><a href="http://www.w3.org/WAI" title="${cmsg.self_w3c_wai_1_title}" tooltip="${cmsg.self_w3c_wai_1_tooltip}" > <img src="${currentContextPath}/images/extension/w3c2.gif" alt="${cmsg.self_w3c_wai_1_alt}" /></a></p>
      </c:if>
	</div>
