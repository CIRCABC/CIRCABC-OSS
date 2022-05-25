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
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="eu.cec.digit.circabc.config.CircabcConfiguration" %>

<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page isELIgnored="false"%>

<c:set var="currentLocale" value="<%=FacesContext.getCurrentInstance().getViewRoot().getLocale()%>" />
<c:set var="currentContextPath" value="<%=request.getContextPath()%>" />
<c:set var="pageTitle" value="<%=CircabcConfiguration.getApplicationName()%>" />

<c:set var="piwikEnabled" value="<%=CircabcConfiguration.getProperty(CircabcConfiguration.PIWIK_ENABLED)%>" />
<c:set var="piwikSiteId" value="<%=CircabcConfiguration.getProperty(CircabcConfiguration.PIWIK_SITE_ID)%>" />
<c:set var="piwikSitePath" value="<%=CircabcConfiguration.getProperty(CircabcConfiguration.PIWIK_SITE_PATH)%>" />
<c:set var="piwikInstance" value="<%=CircabcConfiguration.getProperty(CircabcConfiguration.PIWIK_INSTANCE)%>" />

<f:loadBundle basename="alfresco.extension.messages.circabc-version" var="rev" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="${currentLocale}" lang="${currentLocale}">
	<head>
		<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
		<meta name="Reference" content="CIRCABC" />
		<meta name="Title" content="<h:outputText value="#{currentTitle }"/>" />
		<meta name="Creator" content="European Commission" />
		<meta name="Language" content="<c:out value="${currentLocale}" />" />
		<meta name="Type" content="Numeric code given in the list of document types" />
		<meta name="Classification" content="Numeric code from the alphabetical classification list common to all the institutions" />
		<meta name="Keywords" content="European Commission, CIRCABC, documents, files" />
		<meta name="Description" content="CIRCABC is a collaborative platform, which offers an easy distribution and management of documents." />
		<meta http-equiv="X-UA-Compatible" content="<c:out value="${xuaCompatible}" default="IE=8" />" >
		<title>${pageTitle} - <c:out value="${currentTitle}" escapeXml="true"/></title>
		<link rel="stylesheet" href="${currentContextPath}/css/extension/jquery-ui-1.9.2.custom/css/circabc-preview-theme/jquery-ui-1.9.2.custom.min.css" type="text/css" />
		<link rel="stylesheet" href="${currentContextPath}/css/extension/pdf_preview.css" type="text/css" />
		<link rel="stylesheet" href="${currentContextPath}/css/extension/circabc.css?rev=<h:outputText value="#{rev['version.revision']}" />" type="text/css" />
		<link rel="stylesheet" href="${currentContextPath}/css/extension/commission2012.css" type="text/css" />
		<link rel="icon" type="image/gif" href="${currentContextPath}/images/favicon.gif"/>
		<link rel="shortcut icon" type="image/x-icon" href="${currentContextPath}/images/favicon.ico" />
		
		<script src="${currentContextPath}/scripts/extension/language.js" type="text/javascript"></script>
		<script>var gContextPath = "${currentContextPath}"</script>
		
		<c:if test="${ piwikEnabled == 'true' }">
			<script defer src="//europa.eu/webtools/load.js" type="text/javascript"></script>
			<script type="application/json">
				{ "utility": "piwik", "siteID": ${piwikSiteId}, "sitePath": ["${piwikSitePath}"], "instance": "${piwikInstance}" }
			</script>
		</c:if>
		<script src="https://ec.europa.eu/wel/cookie-consent/consent.js" type="text/javascript"></script>
		<script>
		  // add polyfill for  endsWith
		  if (!String.prototype.endsWith) {
      	String.prototype.endsWith = function(search, this_len) {
      		if (this_len === undefined || this_len > this.length) {
      			this_len = this.length;
      		}
      		return this.substring(this_len - search.length, this_len) === search;
      	};
      }
      function clickLogin() {
          var referrer = document.referrer;
          if (referrer.indexOf('/ui/') !== -1){
            var tabMenu =document.getElementById('tabMenu');
            if (tabMenu !== null) {
              var links = tabMenu.getElementsByTagName('a') ;
              if (links.length >  1) {
                  var text = links[1].innerHTML;
                  if (text.indexOf('(') === -1){
                    links[1].click();
                  }
              }
            }
          }
      }

      // in case the document is already rendered
      if (document.readyState!='loading') clickLogin();
      // modern browsers
      else if (document.addEventListener) document.addEventListener('DOMContentLoaded', clickLogin);
      // IE <= 8
      else document.attachEvent('onreadystatechange', function(){
          if (document.readyState=='complete') clickLogin();
      });
    </script>
	</head>
<body>
<a name="top"></a>
