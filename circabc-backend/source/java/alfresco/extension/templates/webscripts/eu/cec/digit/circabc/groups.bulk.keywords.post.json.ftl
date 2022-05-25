<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list keywords as keyword>
		{
			"id": "${keyword.id}", 
			"title":
				<#assign map = keyword.title>
				<#include "i18nMapIterator.json.ftl">
			,
			"name": "${keyword.id}"
		}
		<#if (keyword_has_next)>, </#if>
	</#list>
]
</#escape>