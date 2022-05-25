<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

${formatMessage("mails_common_dear_user", fullName(person))}

<br /><br />

${formatMessage("change_permission_group_user_template_mail_body",
		fullName(me),
		accessUrl(location),
		titleOrName(location),
        permission)
	}

<br /><br />

${formatMessage("mails_common_signature", appName)}

</#compress>