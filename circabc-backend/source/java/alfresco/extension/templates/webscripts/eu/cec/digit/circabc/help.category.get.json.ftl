<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": "${category.id}",
	"title": <#assign map = category.title>
			<#include "i18nMapIterator.json.ftl">
			,
	"sortOrder": <#if category.sortOrder??>${category.sortOrder?c}<#else>0</#if>,
	"numberOfArticles": <#if category.numberOfArticles??>${category.numberOfArticles?c}<#else>0</#if>
}

</#escape>