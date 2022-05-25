<#escape x as jsonUtils.encodeJSONString(x)>
{
	"id": "${dp.id}",
	"index": "${dp.index}",
	"name": "${dp.name}",
	"propertyType": "${dp.propertyType}",
	"possibleValues": [
		<#list dp.possibleValues as possibleValue>
			"${possibleValue}"
			<#if (possibleValue_has_next)>,</#if>
		</#list>
	],
	"title":
	<#assign map = dp.title>
	<#include "i18nMapIterator.json.ftl">
}
</#escape>