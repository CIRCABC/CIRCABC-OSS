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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<h:panelGrid columns="1">

	<h:dataTable value="#{WizardManager.bean.categoryListDataModel}" var="row"
                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                styleClass="selectedItems" headerClass="selectedItemsHeader"
                cellspacing="0" cellpadding="4">

      <h:column>
         <f:facet name="header">
			<h:outputText value=" " />
	     </f:facet>
		<h:selectOneRadio value="#{WizardManager.bean.categoryHeaderId}"  onclick="dataTableSelectOneRadio(this);">
			<f:selectItem itemValue="#{row.id}" itemLabel=""/>
		</h:selectOneRadio>
	  </h:column>

      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.name}" />
         </f:facet>
         <h:outputText value="#{row.name}" />
      </h:column>

      <h:column>
         <f:facet name="header">
            <h:outputText value="#{msg.description}" />
         </f:facet>
         <h:outputText value="#{row.description}" />
      </h:column>

   </h:dataTable>
</h:panelGrid>


<script type="text/javascript">

	if(!isSomethingChecked())
	{
		checkedFirst();
	}

	function isSomethingChecked()
	{
		var el = document.all;
        for (var i = 0; i < el.length; i++) 
        {
        	if (el[i].type == 'radio') {
        		
                if (el[i].checked )
                {
                	return true;
                }
            }
        }
        return false;
	}

	function checkedFirst()
	{
		var el = document.all;
        for (var i = 0; i < el.length; i++) 
        {
        	if (el[i].type == 'radio') 
        	{
                el[i].checked = true;
                return;
            }
        }
	}
    function dataTableSelectOneRadio(radio)
    {
        var id = radio.name.substring(radio.name.lastIndexOf(':'));
        var el = radio.form.elements;
        for (var i = 0; i < el.length; i++) {
            if (el[i].name.substring(el[i].name.lastIndexOf(':')) == id) {
                el[i].checked = false;
            }
        }
        radio.checked = true;
    }
</script>
