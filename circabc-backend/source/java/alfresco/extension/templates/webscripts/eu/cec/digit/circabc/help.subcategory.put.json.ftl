<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": "${subcategory.id}",
	"title": <#assign map = subcategory.title>
			<#include "i18nMapIterator.json.ftl">
			,
	"sortOrder": <#if subcategory.sortOrder??>${subcategory.sortOrder?c}<#else>0</#if>,
	"parentId": <#if subcategory.parentId??>"${subcategory.parentId}"<#else>null</#if>,
	"numberOfArticles": <#if subcategory.numberOfArticles??>${subcategory.numberOfArticles?c}<#else>0</#if>
}

</#escape>
