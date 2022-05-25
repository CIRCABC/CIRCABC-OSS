<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->


<#compress>

${formatMessage("mails_common_dear_helpdesk", helpdesk)}

<br /><br />

${formatMessage("request_for_support_body",
		userFullName,
		email,
		organisation,
		subject
		)
	}
<br />
<hr />

${contentMessage}


<br /><hr />

${formatMessage("mails_common_signature", appName)}

</#compress>