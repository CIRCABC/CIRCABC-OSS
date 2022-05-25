<%--
Copyright 2006 European Community

 Licensed under the EUPL, Version 1.1 or – as soon they
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:outputText value="#{msg.reassign_select_user}<br/><br/>" escape="false" />

<a:genericPicker id="user-picker" showAddButton="false" filters="#{DialogManager.bean.filters}"
queryCallback="#{DialogManager.bean.pickerCallback}" multiSelect="false" />

<f:verbatim>
<script type="text/javascript">
addEventToElement(window, 'load', pageLoaded, false);
function pageLoaded()
{
document.getElementById("dialog:dialog-body:user-picker_results").onchange = checkButtonState;
}

function checkButtonState()
{
var button = document.getElementById("dialog:finish-button");
var list = document.getElementById("dialog:dialog-body:user-picker_results");
button.disabled = (list.selectedIndex == -1);
}
</script>
</f:verbatim>
