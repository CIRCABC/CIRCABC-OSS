<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list categories as c>
	{
		"id": "${c.id}",
		"name": "${c.name}",
		"title" : 
		<#if c.title??>
			<#assign map = c.title>
			<#include "i18nMapIterator.json.ftl">
		<#else>{}</#if>
		
	}
	<#if (c_has_next)>,</#if>
  </#list>
]
</#escape>