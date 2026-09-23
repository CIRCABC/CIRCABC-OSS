<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": <#if newsInfo.id ??>"${newsInfo.id}"<#else>""</#if>,
	"pattern": <#if newsInfo.pattern ??>"${newsInfo.pattern}"<#else>""</#if>,
	"layout": <#if newsInfo.layout ??>"${newsInfo.layout}"<#else>""</#if>,
	"size": <#if newsInfo.size ??>${newsInfo.size?c}<#else>1</#if>,
	"content": <#if newsInfo.content ??>"${newsInfo.content}"<#else>""</#if>,
	"modifier": <#if newsInfo.modifier ??>"${newsInfo.modifier}"<#else>""</#if>,
	"modified": <#if newsInfo.modified ??>"${newsInfo.modified}"<#else>""</#if>,
	"creator": <#if newsInfo.modifier ??>"${newsInfo.creator}"<#else>""</#if>,
	"created": <#if newsInfo.modified ??>"${newsInfo.created}"<#else>""</#if>,
	"date": <#if newsInfo.date ??>"${newsInfo.date}"<#else>""</#if>,
	"url": <#if newsInfo.url ??>"${newsInfo.url}"<#else>""</#if>,
	"permissions" : {
		<#assign perms = newsInfo.permissions?keys>
		<#list perms as p>
			"${p}": "${newsInfo.permissions[p]}"
			<#if (p_has_next)>,</#if>
		</#list>
	},
	"properties" : {
		<#assign props = newsInfo.properties?keys>
		<#list props as prop>
			"${prop}": "${newsInfo.properties[prop]}"
			<#if (prop_has_next)>,</#if>
		</#list>
	},
	<#if newsInfo.title ??>
		"title": <#assign map = newsInfo.title>
			<#include "i18nMapIterator.json.ftl">
		,
	</#if>
	"files": [
		<#list newsInfo.files as n>
			<#include "node.get.json.ftl">
		<#if (n_has_next)>,</#if>
		</#list>	
	]
}
	
</#escape>