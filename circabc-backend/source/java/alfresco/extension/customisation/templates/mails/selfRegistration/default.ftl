<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

${formatMessage("mails_common_dear_user", fullName(person))}

<br /><br />

${formatMessage("self_registration_mail_body")}

<br /><br />

<u><i>${formatMessage("self_registration_mail_username")}</i></u>: ${person.properties.userName}<br />
<u><i>${formatMessage("self_registration_mail_link")}</i></u>: ${activationUrl}<br />

<br />

<b>
    ${formatMessage("self_registration_mail_warning", expiration)}
</b>

<br /><br />

${formatMessage("mails_common_signature", appName)}

</#compress>

