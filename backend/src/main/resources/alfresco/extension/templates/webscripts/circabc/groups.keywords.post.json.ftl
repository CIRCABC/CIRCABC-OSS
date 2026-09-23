<#escape x as jsonUtils.encodeJSONString(x)>
{
	"id": "${keyword.id}", 
	"title":	<#assign map = keyword.title>
				<#include "i18nMapIterator.json.ftl">,
	"name": "${keyword.id}"
}
</#escape>