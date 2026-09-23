<#escape x as jsonUtils.encodeJSONString(x)>

{
	"header":
		{
			"id": "${groupPath.header.id}",
			"name": "${groupPath.header.name}",
			<#if groupPath.header.description??>
			"description": 
						<#assign map = groupPath.header.description>
						<#include "i18nMapIterator.json.ftl">
			</#if>
		},
	"category": 
		{
			"id": "${groupPath.category.id}",
			"name": "${groupPath.category.name}",
			"title": <#assign map = groupPath.category.title>
					<#include "i18nMapIterator.json.ftl">
					,
			"logoRef": <#if groupPath.category.logoRef??>"${groupPath.category.logoRef}"<#else>""</#if>
		},
	"group":
		<#assign g = groupPath.group>
		<#include "groups.get.json.ftl">
} 

</#escape>