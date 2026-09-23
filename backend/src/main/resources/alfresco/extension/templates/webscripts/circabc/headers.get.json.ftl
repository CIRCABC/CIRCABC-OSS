<#escape x as jsonUtils.encodeJSONString(x)>
<#if header??>
	{
		"id": "${header.id}",
		"name": "${header.name}",
		<#if header.description??>
		"description": 
					<#assign map = header.description>
					<#include "i18nMapIterator.json.ftl">,
		</#if>
		"categories": [
					  <#list header.categories as c>
						{
							"id": "${c.id}",
							"name": "${c.name}",
							"title" : {
									<#assign keys = c.title?keys>
									<#list keys as k>
										"${k}": "${c.title[k]}"
										<#if (k_has_next)>,</#if>
									</#list>
								},
							"logoRef": <#if c.logoRef??>"${c.logoRef}"<#else>""</#if>
						}<#if (c_has_next)>,</#if>
					  </#list>
					 ] 
	}
<#elseif categories??>
	[
	  <#list categories as c>
		{
			"id": "${c.id}",
			"name": "${c.name}",
			"title": <#assign map = c.title>
					<#include "i18nMapIterator.json.ftl">
					,
			"logoRef": <#if c.logoRef??>"${c.logoRef}"<#else>""</#if>
		}<#if (c_has_next)>,</#if>
	  </#list>
	 ] 
<#else>
  [ 	
  <#list headers as h>
  	
	{
		"id": "${h.id}",
		"name": "${h.name}"
		<#if h.description??>
		,
		"description": 
			<#assign map = h.description>
			<#include "i18nMapIterator.json.ftl">
		</#if>
		<#if h.categories??>
		,
		"categories": [
					  <#list h.categories as c>
						{
							"id": "${c.id}",
							"name": "${c.name}",
							"title" : 
								<#assign map = c.title>
								<#include "i18nMapIterator.json.ftl">
						}<#if (c_has_next)>,</#if>
					  </#list>
					 ]
		</#if>
	}<#if (h_has_next)>,</#if>
  </#list>
  ]
</#if>
</#escape>