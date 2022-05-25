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
<%@ taglib uri="/WEB-INF/tomahawk.tld" prefix="t"%>


<%@ page isELIgnored="false" %>

<circabc:panel id="contentMainFormDeleteKeyword" styleClass="contentMainForm">

		<f:verbatim><br /></f:verbatim>
		<h:outputText id="delete-keyword-confirmation" value="#{cmsg.delete_keyword_dialog_confirmation}" styleClass="mainSubTitle"/>
		<f:verbatim>
			<br /><br />
		</f:verbatim>
		<h:outputText id="delete-keyword-listing" value="#{WaiDialogManager.bean.keywordTranslations}" styleClass="mainContentNote" rendered="#{WaiDialogManager.bean.keywordToDelete != null }"/>
		
		<t:dataList value="#{WaiDialogManager.bean.selectedKeywords}"  var="kw" rendered="#{WaiDialogManager.bean.selectedKeywords != null }" layout="unorderedList">
			<h:outputText value="#{kw }" escape="false"/>
		</t:dataList>

</circabc:panel>