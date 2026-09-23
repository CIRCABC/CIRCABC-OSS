<#escape x as jsonUtils.encodeJSONString(x)>
{
	"id": "${category.id}",
	"name": "${category.name}",
	"title" : 
	<#assign map = category.title>
	<#include "i18nMapIterator.json.ftl">,
	"logoRef": <#if category.logoRef??>"${category.logoRef}"<#else>""</#if>
}
</#escape>