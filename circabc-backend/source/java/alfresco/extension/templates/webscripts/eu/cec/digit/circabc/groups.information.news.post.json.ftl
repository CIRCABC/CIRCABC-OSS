<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": "${newsInfo.id}",
	"pattern": "${newsInfo.pattern}",
	"layout": "${newsInfo.layout}",
	"size": ${newsInfo.size},
	"content": "${newsInfo.content}", 
	"modifier": <#if newsInfo.modifier ??>"${newsInfo.modifier}"<#else>""</#if>, 
	"modified": <#if newsInfo.modified ??>"${newsInfo.modified}"<#else>""</#if>, 
	"date": <#if newsInfo.date ??>"${newsInfo.date}"<#else>""</#if>,
		
	"title":
		<#assign map = newsInfo.title>
		<#include "i18nMapIterator.json.ftl">
	,
	"files": [
		<#list newsInfo.files as n>
			<#include "node.get.json.ftl">
		<#if (n_has_next)>,</#if>
		</#list>	
	]
			
}
	
</#escape>