<#escape x as jsonUtils.encodeJSONString(x)>
[
<#if id??>
  <#list groups as g>
  	<#include "groups.get.json.ftl">
	<#if (g_has_next)>,</#if>
  </#list>  
<#else>
  <#list categories as c>
	{
		"id": "${c.id}",
		"name": "${c.name}",
		"title" : 
		<#assign map = c.title>
		<#include "i18nMapIterator.json.ftl">,
		"logoRef": <#if c.logoRef??>"${c.logoRef}"<#else>""</#if>
	}<#if (c_has_next)>,</#if>
  </#list>	
</#if>
]
</#escape>