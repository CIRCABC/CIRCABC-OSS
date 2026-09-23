<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": <#if article.id ??>"${article.id}"<#else>""</#if>,
	"title": <#assign map = article.title>
			<#include "i18nMapIterator.json.ftl">,
	"content": <#assign map = article.content>
			<#include "i18nMapIterator.json.ftl">,
	"lastUpdate": <#if article.lastUpdate ??>"${article.lastUpdate}"<#else>""</#if>,
	"author": <#if article.author ??>"${article.author}"<#else>""</#if>,
	"highlighted": <#if article.highlighted ??>${article.highlighted?string}<#else>false</#if>,
	"parentId": <#if article.parentId ??>"${article.parentId}"<#else>""</#if>,
	"visitCounter": <#if article.visitCounter ??>${article.visitCounter?c}<#else>0</#if>
}

</#escape>