<#escape x as jsonUtils.encodeJSONString(x)>

{
	"data": [
		<#list news.data as newsInfo>
			<#include "news.get.json.ftl">
			<#if (newsInfo_has_next)>, </#if>
		</#list>
	],
	"total": ${news.total?c}
}
	
</#escape>