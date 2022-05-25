<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

${formatMessage("mails_common_dear_user", fullName(person))}

<br /><br />
<#if document?has_content>
${formatMessage("notification_message_new_edition_header",
		document.name,
		simpleAccessUrl(document),
		titleOrName(interestGroup),
		simpleAccessUrl(interestGroup),
		titleOrName(category),
		simpleAccessUrl(category))
	}
</#if>
<br /><br />

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/includes/propertyList.ftl"].nodeRef>

<br/><br/>

${formatMessage("notification_message_new_edition_footer")}

<br /><br />

${formatMessage("mails_common_signature", appName)}

</#compress>

