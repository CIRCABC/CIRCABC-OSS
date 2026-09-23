<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": <#if link.id ??>"${link.id}"<#else>""</#if>,
	"title": <#assign map = link.title>
			<#include "i18nMapIterator.json.ftl">,
	"href": <#if link.href ??>"${link.href}"<#else>""</#if>
}

</#escape>