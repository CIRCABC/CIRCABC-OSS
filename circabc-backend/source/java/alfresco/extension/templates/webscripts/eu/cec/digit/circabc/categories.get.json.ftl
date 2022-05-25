<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if category??>
	"id": "${category.id}",
	"name": "${category.name}",
	"title":
		<#assign map = category.title>
		<#include "i18nMapIterator.json.ftl">
		,
	"logoRef": <#if category.logoRef??>"${category.logoRef}"<#else>""</#if>,
	"useSingleContact": <#if category.useSingleContact??>${category.useSingleContact?string}<#else>false</#if>,
	"contactEmails": [
		<#if category.contactEmails??>
			<#list category.contactEmails as email>
				"${email}"
				<#if (email_has_next)>,</#if>
			</#list>
		</#if>
	],
	"contactVerified": <#if category.contactVerified??>${category.contactVerified?string}<#else>false</#if>
</#if>
}
</#escape>