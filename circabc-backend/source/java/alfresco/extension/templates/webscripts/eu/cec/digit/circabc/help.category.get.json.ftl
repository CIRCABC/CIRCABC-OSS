<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": "${category.id}",
	"title": <#assign map = category.title>
			<#include "i18nMapIterator.json.ftl">
			,
	"numberOfArticles": <#if category.numberOfArticles??>${category.numberOfArticles?c}<#else>0</#if>
}

</#escape>