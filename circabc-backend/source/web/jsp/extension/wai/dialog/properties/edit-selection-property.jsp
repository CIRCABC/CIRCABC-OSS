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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>



<%-- load a bundle of properties with I18N strings --%>
<f:loadBundle basename="alfresco.messages.webclient" var="msg" />
<f:loadBundle basename="alfresco.extension.webclient" var="cmsg" />

<circabc:panel id="edit-selection-property-section">



	<circabc:panel id="edit-selection-definition">

		<f:verbatim>
			<br />
		</f:verbatim>
		<h:outputText value="#{cmsg.edit_selection_list_of_values}"  />
		<f:verbatim>
			<br />
			<br />
		</f:verbatim>
		<h:inputText id="add-text" value="#{DialogManager.bean.addText}"></h:inputText>
		<h:commandButton id="add-button" value="#{cmsg.edit_selection_add_value}"
			actionListener="#{DialogManager.bean.add}"></h:commandButton>
		<f:verbatim>
			<br /><br />
		</f:verbatim>
		<h:selectOneListbox styleClass="standardSelect"
			id="list-of-valid-values" value="#{DialogManager.bean.currentValue}">
			<f:selectItems value="#{DialogManager.bean.listOfValidValues}" />
		</h:selectOneListbox>
		<circabc:panel id="edit-selection-definition-move-action"
			styleClass="leftFloatingActions">
			<h:commandButton id="up-button" value="#{cmsg.edit_selection_move_value_up}"
				actionListener="#{DialogManager.bean.up}"></h:commandButton>
			<h:commandButton id="down-button" value="#{cmsg.edit_selection_move_value_down}"
				actionListener="#{DialogManager.bean.down}"></h:commandButton>
<f:verbatim>
				<hr class="tinyLineSpacer" />
			</f:verbatim>
			<h:commandButton id="select-button" value="#{cmsg.edit_selection_select_value}"
				actionListener="#{DialogManager.bean.select}"></h:commandButton>
			<h:inputText id="update-text"
				value="#{DialogManager.bean.updateText}"></h:inputText>
			<h:commandButton id="update-button" value="#{cmsg.edit_selection_update_value}"
				actionListener="#{DialogManager.bean.update}"></h:commandButton>
			<f:verbatim>
				<hr class="tinyLineSpacer" />
			</f:verbatim>
			<h:commandButton id="remove-button" value="#{cmsg.edit_selection_remove_value}"
				actionListener="#{DialogManager.bean.remove}"></h:commandButton>
		</circabc:panel>
		
		
	</circabc:panel>

	<f:verbatim>
		<br />
		<br />
	</f:verbatim>

	<h:outputLabel id="update-existing-properties-text"
		value="#{cmsg.edit_selection_update_existing_values}"></h:outputLabel>
	<h:selectBooleanCheckbox id="update-existing-properties"
		value="#{DialogManager.bean.updateExistingProperties}"></h:selectBooleanCheckbox>

</circabc:panel>



