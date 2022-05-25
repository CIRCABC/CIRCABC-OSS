<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

${formatMessage("mails_common_dear_user", fullName)}

<br /><br />

${formatMessage("resend_password_mail_body")}
	
<br /><br />

<u><i>${formatMessage("self_registration_mail_username")}</i></u>: ${userName}<br />
<u><i>${formatMessage("resend_password_mail_password")}</i></u>: ${newPassword}<br />

<br />

<b>
    ${formatMessage("resend_password_mail_change")}
</b>

<br /><br />

${formatMessage("resend_password_mail_info")}


<br /><br />

${formatMessage("mails_common_signature", appName)}

</#compress>


