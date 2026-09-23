<#escape x as jsonUtils.encodeJSONString(x)>

{
	"data":
	[
		<#if requests.data??>
		  <#list requests.data as request>
		  	<#include "categories.group.request.get.json.ftl">
			<#if (request_has_next)>,</#if>
		  </#list>  	
		</#if>
	],
	"total": ${requests.total?c}
}

</#escape>