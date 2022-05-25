<%--+
    |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
    |
    |  		   http://ec.europa.eu/idabc/en/document/6523
    |
    +--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>
<%@ taglib uri="/WEB-INF/circabc.tld" prefix="circabc"%>

<%@ page isELIgnored="false"%>

<script language="javascript"
type="text/javascript"
src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js">&#160;</script>
<script language="javascript"
type="text/javascript"
src="${pageContext.request.contextPath}/scripts/ajax/common.js">&#160;</script>


<script type="text/javascript">var ie = 0;</script>
<!--[if IE]>
<script type="text/javascript">ie = 1;</script>
<![endif]--> 


<script language="javascript" type="text/javascript">


	// Init the Tiny MCE in-line HTML editor
	var alfresco = typeof alfresco == "undefined" ? {} : alfresco;
	alfresco.constants = typeof alfresco.constants == "undefined" ? {} : alfresco.constants;

	alfresco.constants.WEBAPP_CONTEXT = "${pageContext.request.contextPath}";
	alfresco.constants.AVM_WEBAPP_URL = ".";

	alfresco.resources =
	{
		//XXXarielb deal with encoding
		add_content: "{msg.add_content}",
		cancel: "{msg.cancel}",
		change: "{msg.change}",
		go_up: "{msg.go_up}",
		ide: "{msg.idle}",
		loading: "{msg.loading}",
		path: "{msg.path}",
		select: "{msg.select}",
		upload: "{msg.upload}"
	};

	var clickOnSave = "true";

	window.onload = pageLoaded;

	function pageLoaded()
    {
    	var closeTipLink = document.getElementById("close-tips-link").style.display =  'none' ;

		var afterSave = document.getElementById("FormPrincipal:openAfterSave");
		var defaultMode = document.getElementById("FormPrincipal:defaultMode");
		var submitButton = document.getElementById("FormPrincipal:finish-button");
		var cancelButton = document.getElementById("FormPrincipal:cancel-button");

    	showPanel(defaultMode.value,false);

    	// if user clicks on SAVE, afterSave.value='true'. The page will be reloaded.
    	// Else it clicks on FINISH, afterSave.value='false'. The page will be closed.
    	afterSave.value='true';
    	submitButton.onmousedown = function (evt)
    	{
    		afterSave.value = 'false';
		};
		
    	cancelButton.onclick = function (evt)
    	{
	   		clickOnSave = false;
		};
		
		var table = document.getElementById("select-link-panelGlobal");
		
		var aList = Array()
		aList=table.getElementsByTagName("a");

		for(i=0;i<aList.length;i++)
		{
			

			if (ie == 1)
			{
				var script = aList[i].getAttributeNode("onclick").value;
				aList[i].onclick = new Function("onAddAttachment();" + script);				
			}
			else
			{
				aList[i].setAttribute("onclick", "onAddAttachment();" + aList[i].getAttribute("onclick"));
			}
		}
		
		var inputList = Array()
		inputList=table.getElementsByTagName("*");
		for(i=0;i<inputList.length;i++)
		{
			if(inputList[i].type=='button')
			{
				if (ie == 1)
				{
					var script = inputList[i].getAttributeNode("onclick").value;
					inputList[i].onclick = new Function("onAddAttachment();" + script);
				}
				else
				{
					inputList[i].setAttribute("onclick", "onAddAttachment();" + inputList[i].getAttribute("onclick"));
				}
			}
		}

		
	}

    function showPanel(status,postForm)
    {
		var blockHtml = document.getElementById("edit-as-html-panel");
		var blockText = document.getElementById("edit-as-text-panel");
		var mode = document.getElementById("FormPrincipal:editMode");
		var defaultMode = document.getElementById("FormPrincipal:defaultMode");

		if(status == "Html")
		{
			blockHtml.style.display =  'block' ;
			blockText.style.display =  'none' ;
			mode.value = "Html";
		}
		else
		{
			blockText.style.display =  'block' ;
			blockHtml.style.display =  'none' ;
			mode.value = "Text";
		}
		
		defaultMode.value = mode.value;
		
		if ( postForm ) 
		{
			document.getElementById("FormPrincipal").submit();	
		}
			
		
	}

	function showTips()
    {
		var openTipLink = document.getElementById("view-tips-link");
		var closeTipLink = document.getElementById("close-tips-link");
		var allTipsPanel = document.getElementById("all-tips-panel");

		allTipsPanel.style.display =  'block' ;
		closeTipLink.style.display =  'block' ;
		openTipLink.style.display  =  'none' ;
	}

	function closeTips()
    {
		var openTipLink = document.getElementById("view-tips-link");
		var closeTipLink = document.getElementById("close-tips-link");
		var allTipsPanel = document.getElementById("all-tips-panel");

		allTipsPanel.style.display =  'none' ;
		closeTipLink.style.display =  'none' ;
		openTipLink.style.display  =  'block' ;
	}

	<%-- Init the Tiny MCE in-line HTML editor --%>
	tinyMCE.init(
	{

		theme : "advanced",
		mode : "exact",
		plugins : "safari,spellchecker,pagebreak,style,layer,table,save,advhr,advimage,advlink,emotions,iespell,inlinepopups,insertdatetime,media,searchreplace,print,contextmenu,paste,directionality,fullscreen,visualchars,nonbreaking,xhtmlxtras,template",
		convert_urls: false,
		relative_urls: false,
		elements : "editor",
		save_callback : "saveContent",
		language : "<%=request.getLocale().getLanguage()%>",

		theme_advanced_buttons1 : "save,newdocument,|,bold,italic,underline,strikethrough,forecolor,backcolor,|,justifyleft,justifycenter,justifyright,justifyfull,|,formatselect,fontselect,fontsizeselect",
		theme_advanced_buttons2 : "cut,copy,paste,pastetext,pasteword,|,search,replace,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|,link,unlink,anchor,image,media,|,insertdate,inserttime,preview",
		theme_advanced_buttons3 : "tablecontrols,|,hr,removeformat,visualaid,|,sub,sup,|,charmap,emotions,iespell,advhr,|,ltr,rtl",
		theme_advanced_buttons4 : "styleprops,|,cite,abbr,acronym,del,ins,attribs,|,visualchars,nonbreaking,blockquote,pagebreak,|,help,code,fullscreen",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_path : false,
		theme_advanced_resizing : false,
		forced_root_block : '',
		force_p_newlines : false,
		force_br_newlines : true,
		convert_newlines_to_brs : true,
		theme_advanced_disable: "styleselect",
		theme_advanced_styles : "Code=codeStyle;Quote=quoteStyle",

		
		// get a list of common used urls
		external_link_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/links.jsp",
		// get a list of available images
		external_image_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/images.jsp",
		// get a list of available media
		media_external_list_url : "${pageContext.request.contextPath}/faces/jsp/extension/wai/dialog/content/edit/medias.jsp",

		extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
	});

	function onAddAttachment()
	{
		clickOnSave = 'false';
		tinyMCE.triggerSave();
	}

	function saveContent(id, content)
	{
		var mode = document.getElementById("FormPrincipal:editMode");
		var isWebkit = 'webkitRequestAnimationFrame' in window;
		if (typeof console != "undefined") { 
		console.log(isWebkit) ;
		}
		if(typeof(content) != "undefined" && mode.value == "Html")
		{
			document.getElementById("FormPrincipal:editorOutput").value=content;
			if(clickOnSave == "true" && !isWebkit)
			{
				document.getElementById("FormPrincipal:finish-button").click();
			}
			// else don't save the post, user is adding a new file.
		}
	}
</script>
<script language="javascript"
type="text/javascript"
src="${pageContext.request.contextPath}/scripts/ajax/dojo/dojo.js">&#160;</script>
<script language="javascript"
type="text/javascript"
src="${pageContext.request.contextPath}/scripts/ajax/tiny_mce_wcm_extensions.js">&#160;</script>
<script language="javascript"
type="text/javascript"
src="${pageContext.request.contextPath}/scripts/ajax/ajax_helper.js">&#160;</script>
<script language="javascript"
type="text/javascript"
src="${pageContext.request.contextPath}/scripts/ajax/file_picker_widget.js">&#160;</script>
<script language="javascript"
type="text/javascript"
src="<%=request.getContextPath()%>/scripts/noscript.js" >&#160;</script>

<circabc:panel id="contentMainCreateContentHtml" styleClass="contentMainForm">

	<f:verbatim><div id="edit-as-html-panel" style="display:none;"></f:verbatim>
		<circabc:panel id="create-online-content-action-totext" styleClass="wai_dialog_more_action" >
			<circabc:actionLink href="#" id="create-html-html-doc" image="/images/icons/edit_online.gif" tooltip="#{cmsg.edit_inline_html_warning}" value="#{cmsg.edit_inline_html_simplest_action}" padding="2" showLink="true" onclick="showPanel('Text',true);return true;" />
			<f:verbatim><br /></f:verbatim>
			<circabc:actionLink href="#" id="view-tips-link" image="/images/icons/Help_icon.gif" tooltip="#{cmsg.create_content_dialog_attachement_opentip_tooltip}" value="#{cmsg.create_content_dialog_attachement_opentip}" padding="2" showLink="true" onclick="showTips();return true;" />
			<circabc:actionLink href="#" id="close-tips-link" image="/images/icons/close_panel.gif" tooltip="#{cmsg.create_content_dialog_attachement_closetip}" value="#{cmsg.create_content_dialog_attachement_closetip}" padding="2" showLink="true" onclick="closeTips();return true;" />
		</circabc:panel>
		<f:verbatim><br /></f:verbatim>
		<f:verbatim><div id="all-tips-panel" style="display:none;"></f:verbatim>
			<f:verbatim><br /></f:verbatim>
			<circabc:panel id="all-tips-sub-panel" styleClass="infoPanel" styleClassLabel="infoContent"  >
				<f:verbatim><ul></f:verbatim>
				<h:outputText id="tip1" value="<li>#{cmsg.create_content_dialog_attachement_howto_1}</li>" escape="false"/>
				<h:outputText id="tip2" value="<li>#{cmsg.create_content_dialog_attachement_howto_2}</li>" escape="false"/>
				<h:outputText id="tip3" value="<li>#{cmsg.create_content_dialog_attachement_howto_3}</li>" escape="false"/>
				<f:verbatim></ul></f:verbatim>
			</circabc:panel>
		<f:verbatim></div></f:verbatim>

		<circabc:panel id="edit-as-html">
			<f:verbatim><br /><br /><div id="editor" style="width: 100%; height: 450px;" ></f:verbatim>
				<h:outputText id="html-output" value="#{WaiDialogManager.bean.htmlContent}" escape="false" style="" />
			<f:verbatim></div ></f:verbatim>
			<h:inputHidden id="editorOutput" value="#{WaiDialogManager.bean.htmlContent}" />
		</circabc:panel>



	<f:verbatim></div></f:verbatim>


	<f:verbatim><div id="edit-as-text-panel" style="display:block;"></f:verbatim>

		<circabc:panel id="create-online-content-action-tohtml" styleClass="wai_dialog_more_action" >
			<circabc:actionLink href="#" id="create-text-simple-text" image="/images/icons/edit_online.gif" tooltip="#{cmsg.edit_inline_text_warning}" value="#{cmsg.edit_inline_text_simplest_action}" padding="2" showLink="true"  onclick="showPanel('Html',true);return true;" />
		</circabc:panel>

		<circabc:panel id="edit-as-text">
			<f:verbatim><br /><br /></f:verbatim>
			<h:inputTextarea id="text-output" value="#{WaiDialogManager.bean.textContent}" rows="20"  style="width: 100%;"/>
			<f:verbatim><br /><br /></f:verbatim>
		</circabc:panel>

	<f:verbatim></div></f:verbatim>

	<h:inputHidden id="editMode" value="#{WaiDialogManager.bean.editMode}" />
	<h:inputHidden id="defaultMode" value="#{WaiDialogManager.bean.defaultMode}" />
	<h:inputHidden id="openAfterSave" value="#{WaiDialogManager.bean.openAfterSave}" />

	<circabc:displayer rendered="#{WaiDialogManager.bean.attachementAllowed}">

		<f:verbatim><br /></f:verbatim>

		<circabc:panel id="attchement-add--section-file" styleClass="signup_rub_title">
			<h:outputText id="sect-attac-1" value="#{cmsg.create_content_dialog_attachement_add_file}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<circabc:upload id="uploader" value="#{DialogManager.bean.attachFile}"  framework="FormPrincipal" onSubmit="onAddAttachment()" />

		<f:verbatim><br /><br /></f:verbatim>

		<circabc:panel id="attchement-add-section-repo" styleClass="signup_rub_title">
			<h:outputText id="sect-attac-2" value="#{cmsg.create_content_dialog_attachement_add_link}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<circabc:panel id="select-link-panel">

			<circabc:nodeSelector id="selector-repo" value="#{DialogManager.bean.attachLink}"
			 		styleClass="selector"
			 		rootNode="#{DialogManager.bean.rootId}"
			 		label="#{cmsg.create_content_dialog_attachement_select_link}"
			 		showContents="true"
			 		pathLabel="#{cmsg.path_label}"
					pathErrorMessage="#{cmsg.path_error_message}"
			 		/>

		</circabc:panel>

		<f:verbatim>

		<script type="text/javascript" language="javascript">


      </script>
		</f:verbatim>

		<f:verbatim><br /><br /></f:verbatim>

		<circabc:panel id="attachement-all-att-section" styleClass="signup_rub_title">
			<h:outputText id="sect-attac-3" value="#{cmsg.create_content_dialog_attachement_all}" />
		</circabc:panel>

		<f:verbatim><br /></f:verbatim>

		<h:outputText id="no-files" value="#{cmsg.create_content_dialog_attachement_nothing}" rendered="#{DialogManager.bean.attachementFound == false}" styleClass="noItem" />

		<h:dataTable id="allattachsTable" value="#{DialogManager.bean.attachmentDataModel}" var="attach"
			rowClasses="recordSetRow,recordSetRowAlt"
			styleClass="selectedItems" headerClass="selectedItemsHeader" footerClass="selectedItemsHeader"
			cellspacing="0" cellpadding="4" width="100%" rendered="#{DialogManager.bean.attachementFound == true}">

			<h:column id="attach-name-col">
				<f:facet name="header">
					<h:outputText id="attach-display-title" value="#{cmsg.create_content_dialog_attachement_title}" styleClass="attachInList"/>
				</f:facet>
				<h:outputText id="attach-name" value="#{attach.name}" escape="false" />
			</h:column>

			<h:column id="attach-type-col">
				<f:facet name="header">
					<h:outputText id="attach-display-type" value="#{cmsg.create_content_dialog_attachement_type}" styleClass="attachInList"/>
				</f:facet>
				<h:outputText id="attach-type" value="#{attach.type}" escape="false" />
			</h:column>

			<h:column id="actions">
				<f:facet name="header">
					<h:outputText value="#{cmsg.actions}" />
				</f:facet>

				<circabc:actionLink id="removeAttachAction" actionListener="#{DialogManager.bean.removeAttachement}"
						image="/images/icons/delete.gif" value="#{cmsg.create_content_dialog_attachement_remove_action}"
						tooltip="#{cmsg.create_content_dialog_attachement_remove_action}" showLink="false"  />

			</h:column>
		</h:dataTable>

	</circabc:displayer>

</circabc:panel>
