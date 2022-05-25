// Prepares the PDF viewer using the PDFObject library
function viewPDF(docUrl) {
	
	var obj = {
		id: "viewInlinePDF",
		pdfOpenParams: {
			pagemode: "thumbs",
			search: "pdfobject",
			messages: 0,
			navpanes: 1,
			toolbar: 0,
			statusbar: 0,
			view: "FitBH"
		}
	};
	
	obj['url'] = docUrl;
	
	var pObj = new PDFObject(obj);
	
	var pdfElement = document.getElementById("pdf");
	
	if (pObj) {
		
		var pluginType = pObj.get("pluginTypeFound");
		
		if (pluginType) {
			pdfElement.className = "pdfViewStyle";
			pObj.embed("pdf");
		}
		else {
			pdfElement.innerHTML = "No valid PDF plugin found. Please download the file from the link in the actions menu.";
		}
	}
	else {
		pdfElement.innerHTML = "Could not create embedded PDF object.";
	}
}

// Displays the "Preview Not Available" message
function displayPreviewNotAvailable() {
	
	document.getElementById("pdf").innerHTML = 
		'<h3 class="a-center"><br/><br/><br/><br/><br/>' + 
		document.getElementById("FormPrincipal:previewNotAvailableText").value + 
		'<br/><br/><br/><br/><br/></h3>';
}

// Queries the repository servlet via Ajax to retrieve the PDF rendition, or error message, if not retrievable	
function displayPDF(documentId) {
	
	$("#spinner-img").show();
	
	$.ajax({url: gContextPath + "/pdfRendition?documentId=workspace://SpacesStore/" + documentId, 
		async: true, 
		success: function(result) {
			$("#spinner-img").hide();
			if (result == "") {
				displayPreviewNotAvailable();
				return;
			}
			
			viewPDF(result);
		},
		error: function(result) {
			$("#spinner-img").hide();
			displayPreviewNotAvailable();
		}
	});
}

// Displays the content of the given id in an iFrame
function displayDoc(documentId) {
	
	$("#spinner-img").show();
	
	$.ajax({url: gContextPath + "/pdfRendition?documentId=workspace://SpacesStore/" + documentId, 
		async: true, 
		success: function(result) {
			$("#spinner-img").hide();
			if (result == "") {
				displayPreviewNotAvailable();
				return;
			}
			
			document.getElementById("pdf").innerHTML = '<iframe src="' + result + '" width="740" height="430" style="border:none"></iframe>';
		},
		error: function(result) {
			$("#spinner-img").hide();
			displayPreviewNotAvailable();
		}
	});
}

// Checks if the given string end with the given suffix
function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

// This function is called by the generated html by JSF, so the invocation is not visible in the current source
function previewDocument(documentId, downloadUrl, browseUrl) {
	
	// Removes the existing components when the dialog is invoked because they will have to be rendered new every time
	if ($("#menu").length != 0) {
		$("#menu").remove(); // Remove Menu
	}
	if ($("#language-document").length != 0) {
		$("#language-document").remove(); // Remove Languages
	}
	if ($("#download-href").length != 0) {
		$("#download-href").remove(); // Remove Download
	}
	if ($("#details-href").length != 0) {
		$("#details-href").remove(); // Remove Details View
	}
	
	downloadUrl = decodeURI(downloadUrl);
	
	var fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1, 
						downloadUrl.length);
	
	// Add document name to title
	$("#dialog").dialog("option", "title", fileName);
	
	// Full server URL
	var fullURL = location.protocol + '//' + location.hostname + 
					(location.port ? ':' + location.port : '');
	
	// Add actions menu
	$("#dialog").prepend('<ul id="menu" style="float:left; margin-left:15px;"><li><a>' + 
		document.getElementById("FormPrincipal:actionText").value + '</a><ul><li><a href="' + 
		downloadUrl + '" target="_blank">' + document.getElementById("FormPrincipal:downloadText").value + 
		'</a></li><li><a href="mailto:?subject=Emailing%20URL%20of%20' + fileName + 
		'&body=The%20document%20can%20be%20downloaded%20here:%20' + fullURL + 
		escape(downloadUrl) + '" target="_blank">' + 
		document.getElementById("FormPrincipal:sendToText").value + 
		'</a></li></ul></li></ul>');// class="ui-state-disabled" if file size exceeds threshold
	$("#menu").menu();
	
	// Add language select
	var docWithoutName = downloadUrl.substring(0, downloadUrl.lastIndexOf("/"));
	var documentId = docWithoutName.substring(docWithoutName.lastIndexOf("/") + 1, docWithoutName.length);
	
	var buttonText = "FormPrincipal:downloadText";
	
	// URL instead of content
	if (endsWith(fileName, '.html') || endsWith(fileName, '.jpg') || 
			endsWith(fileName, '.gif') || endsWith(fileName, '.jpeg') || 
				endsWith(fileName, '.png')) {
		displayDoc(documentId);
		buttonText = "FormPrincipal:openUrlText";
	}
	else {
		// Get language options and build select (Ajax -> WS)
		$.ajax({url: gContextPath + "/translations?documentId=workspace://SpacesStore/" + documentId, 
				async: false, 
				success: function(result) {
					
					// Check this!
					if (result.options == undefined) {
						return;
					}
					
					if (result.options.length == 0) {
						return;
					}
					
					var selectTag = document.createElement("select");
					selectTag.setAttribute("name", "language-document");
					selectTag.setAttribute("id", "language-document");
					selectTag.setAttribute("style", "float:right; margin-right:15px;");
					
					for (var idx = 0; idx < result.options.length; idx ++) {
					    
						var option = result.options[idx];
					    
					    var optionTag = document.createElement("option");
					    optionTag.setAttribute("value", option.documentId);
						// Default option
					    if (option.documentId == documentId) {
					    	optionTag.setAttribute("selected", "selected");
					    }
					    optionTag.innerHTML = option.language;
					    selectTag.appendChild(optionTag);
					}
					
					$("#dialog").prepend(selectTag);
				}
		});
		
		$("#language-document").on("change", function() {
			displayPDF(this.value);
		});
		
		displayPDF(documentId);
	}
	
	// Add Details link (button)
	$("#dialog").append('<a href="' + browseUrl + 
		'" id="details-href" style="float:left" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button" aria-disabled="false"><span class="ui-button-text">' + 
		document.getElementById("FormPrincipal:detailsText").value + '</span></a>');
	
	// Attach details click with dialog close
	$("#details-href").on("onclick", function() {
		$("#dialog").dialog("close");
	});
	
	// Add Download link (button)
	$("#dialog").append('<a href="' + downloadUrl + 
		'" id="download-href" style="float:right" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only" role="button" aria-disabled="false" target="_blank"><span class="ui-button-text">' + 
		document.getElementById(buttonText).value + '</span></a>');
	
	// Hook to the event to hide the spinner when the dialog is closed but the 
	// document was not yet rendered/loaded
	$("#dialog").bind('dialogclose', function(event) {
		$("#spinner-img").hide();
	});
	
	$("#dialog").dialog("open");
}
