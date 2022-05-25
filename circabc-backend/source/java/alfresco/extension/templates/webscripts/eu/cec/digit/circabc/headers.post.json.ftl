<#escape x as jsonUtils.encodeJSONString(x)>
	{
		"id": "${header.id}",
		"name": "${header.name}",
		"description": 
			<#assign map = header.description>
			<#include "i18nMapIterator.json.ftl">
			,
		"categories": []
	}
</#escape>